import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { MapContainer, Marker, Popup, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import type { Restaurant } from './types'
import { fetchPlaces, type Bounds, type Place, type PlaceKind } from './places'
import KindFilter from './KindFilter'
import { fetchRestaurants } from './restaurants'
import { fetchRestaurantsOf } from './people'
import PlacePopup from './PlacePopup'
import RestaurantPopup from './RestaurantPopup'
import { ICONS } from './icons'
import VectorBaseMap from './VectorBaseMap'
import MarkerClusterGroup from 'react-leaflet-cluster'
import L from 'leaflet'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// only load places when zoomed in this far, my api refuses areas that are too big
const MIN_ZOOM = 14

// options shared by both popups: only the close button or another pin closes them
const POPUP_OPTIONS = {
  offset: [0, -8] as [number, number],
  closeButton: false,
  closeOnClick: false,
  autoClose: false,
  // space between the popup and the edge of the map when it slides into view
  autoPanPadding: [24, 24] as [number, number],
  // leaflet stops popups at 300px wide, the google box needs more room
  maxWidth: 460,
}

// the round bubble with a number that replaces a group of red pins
// bigger groups get a slightly bigger bubble
function clusterIcon(cluster: { getChildCount: () => number }) {
  const count = cluster.getChildCount()
  const size = count < 10 ? 32 : count < 100 ? 38 : 44
  return L.divIcon({
    html: `<div class="cluster-body">${count}</div>`,
    className: 'cluster-icon',
    iconSize: [size, size],
  })
}

// sits inside the map and tells MapPage which area is visible after every move or zoom
function VisibleAreaWatcher({ onChange }: { onChange: (bounds: Bounds | null) => void }) {
  const map = useMap()

  useEffect(() => {
    const report = () => {
      // zoomed out too far, so report nothing instead of a huge area
      if (map.getZoom() < MIN_ZOOM) {
        onChange(null)
        return
      }
      const b = map.getBounds()
      onChange({ south: b.getSouth(), west: b.getWest(), north: b.getNorth(), east: b.getEast() })
    }

    report()
    map.on('moveend', report)

    // cleanup: stop listening if this component is ever removed
    return () => {
      map.off('moveend', report)
    }
  }, [map, onChange])

  // draws nothing, it only listens
  return null
}

// shows my map, or a friends map read only when viewUserId is set
function MapPage({ viewUserId }: { viewUserId: number | null }) {
  // the saved restaurants shown on the map, mine or a friends
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])

  // all places in the visible area, from my own api
  const [places, setPlaces] = useState<Place[]>([])

  // the visible area, VisibleAreaWatcher sets it every time the map stops moving
  const [bounds, setBounds] = useState<Bounds | null>(null)

  // which kinds of places to show, all of them from the start
    // which kinds of places to show, none from the start so the map is clean until you pick something
  const [kinds, setKinds] = useState<PlaceKind[]>([])

  // the clicked pin, only one of these is set at a time
  const [selectedPlace, setSelectedPlace] = useState<Place | null>(null)
  const [selectedRestaurant, setSelectedRestaurant] = useState<Restaurant | null>(null)

  // points at the open popup, so we can ask leaflet to measure it again
  const popupRef = useRef<L.Popup>(null)

  // when the popup changes size, update moves the map so the whole popup fits
  const refreshPopup = useCallback(() => {
    popupRef.current?.update()
  }, [])

  // the popup position as one fixed array per selected pin
  // a new array on every redraw would make leaflet reopen the popup and stop it sliding into view
  const placePosition = useMemo<[number, number] | null>(
    () => (selectedPlace ? [selectedPlace.latitude, selectedPlace.longitude] : null),
    [selectedPlace],
  )
  const restaurantPosition = useMemo<[number, number] | null>(
    () => (selectedRestaurant ? [selectedRestaurant.latitude, selectedRestaurant.longitude] : null),
    [selectedRestaurant],
  )

  // loads my restaurants, or a friends restaurants when looking at their map
  useEffect(() => {
    const load = viewUserId === null ? fetchRestaurants() : fetchRestaurantsOf(viewUserId)
    load
      .then(setRestaurants)
      .catch((error) => console.error('could not load restaurants', error))
  }, [viewUserId])

  // load places again when the map moves or the filter changes
  useEffect(() => {
    // zoomed out too far, or every pill turned off, nothing to ask for
    if (bounds === null || kinds.length === 0) {
      return
    }

    // if the map moves again before the answer comes, the old answer is ignored
    let cancelled = false
    fetchPlaces(bounds, kinds)
      .then((result) => {
        if (!cancelled) {
          setPlaces(result)
        }
      })
      .catch((error) => console.error('could not load places', error))

    // cleanup runs before the next fetch, it marks this one as out of date
    return () => {
      cancelled = true
    }
  }, [bounds, kinds])

  // clicking a red pin closes any open popup for saved pins, and the other way round
  const selectPlace = (p: Place) => {
    setSelectedRestaurant(null)
    setSelectedPlace(p)
  }

  const selectRestaurant = (r: Restaurant) => {
    setSelectedPlace(null)
    setSelectedRestaurant(r)
  }

  const closePopups = () => {
    setSelectedPlace(null)
    setSelectedRestaurant(null)
  }

  // a saved or changed restaurant replaces the old one in the list
  const handleSaved = (saved: Restaurant) => {
    setRestaurants((current) => [...current.filter((r) => r.id !== saved.id), saved])
    closePopups()
  }

  // a removed restaurant disappears from the list and turns back into a red pin
  const handleRemoved = (id: number) => {
    setRestaurants((current) => current.filter((r) => r.id !== id))
    closePopups()
  }

  // on a friends map i only look, so no red pins and no editing
  const readOnly = viewUserId !== null

  // skip places already saved, so they are not drawn twice
  const savedIds = new Set(restaurants.map((r) => r.placeId))

  // nothing to show when zoomed out or when every pill is off
  const visiblePlaces = bounds === null || kinds.length === 0 ? [] : places
  const unsaved = readOnly ? [] : visiblePlaces.filter((p) => !savedIds.has(p.id))

  return (
    <>
      {/* the filter pills, only on my own map since friends maps have no red pins */}
      {!readOnly && <KindFilter selected={kinds} onChange={setKinds} />}

      <MapContainer center={STOCKHOLM} zoom={15} maxZoom={19} fadeAnimation={false} className="map">
        {/* the clean vector map background */}
        <VectorBaseMap />

        <VisibleAreaWatcher onChange={setBounds} />

        {/* red pins grouped into number bubbles that split up when you zoom in */}
        <MarkerClusterGroup
          chunkedLoading
          maxClusterRadius={90}
          showCoverageOnHover={false}
          disableClusteringAtZoom={18}
          iconCreateFunction={clusterIcon}
        >
          {unsaved.map((p) => (
            <Marker
              key={p.id}
              position={[p.latitude, p.longitude]}
              icon={ICONS.UNSAVED}
              eventHandlers={{ click: () => selectPlace(p) }}
            />
          ))}
        </MarkerClusterGroup>

        {/* saved restaurants, orange with a fork or green with a smiley */}
        {restaurants.map((r) => (
          <Marker
            key={r.id}
            position={[r.latitude, r.longitude]}
            icon={ICONS[r.status]}
            eventHandlers={{ click: () => selectRestaurant(r) }}
          />
        ))}

        {/* popup for a red pin */}
        {selectedPlace && placePosition && (
          <Popup ref={popupRef} position={placePosition} {...POPUP_OPTIONS}>
            <PlacePopup
              key={selectedPlace.id}
              place={selectedPlace}
              onSaved={handleSaved}
              onClose={closePopups}
              onResize={refreshPopup}
            />
          </Popup>
        )}

        {/* popup for a saved pin */}
        {selectedRestaurant && restaurantPosition && (
          <Popup ref={popupRef} position={restaurantPosition} {...POPUP_OPTIONS}>
            <RestaurantPopup
              key={selectedRestaurant.id}
              restaurant={selectedRestaurant}
              readOnly={readOnly}
              onSaved={handleSaved}
              onRemoved={handleRemoved}
              onClose={closePopups}
              onResize={refreshPopup}
            />
          </Popup>
        )}
      </MapContainer>
    </>
  )
}

export default MapPage