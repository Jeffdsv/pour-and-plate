import { useCallback, useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import type { Restaurant } from './types'
import { fetchPlaces, type Bounds, type Place } from './places'
import { fetchRestaurants } from './restaurants'
import PlacePopup from './PlacePopup'
import { ICONS } from './icons'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// only load places when zoomed in this far, my api refuses areas that are too big
const MIN_ZOOM = 14

// sits inside the map and tells App which area is visible after every move or zoom
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

function App() {
  // my saved restaurants from my own api
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])

  // all places in the visible area, also from my own api
  const [places, setPlaces] = useState<Place[]>([])

  // the red pin that was clicked, its popup shows while this is not null
  const [selectedPlace, setSelectedPlace] = useState<Place | null>(null)

  // runs once when the page loads and fetches my restaurants
  useEffect(() => {
    fetchRestaurants()
      .then(setRestaurants)
      .catch((error) => console.error('could not load restaurants', error))
  }, [])

  // called by VisibleAreaWatcher every time the map stops moving
  // useCallback keeps it the same function between redraws, so the watcher doesnt restart
  const loadArea = useCallback((bounds: Bounds | null) => {
    if (bounds === null) {
      setPlaces([])
      return
    }
    fetchPlaces(bounds)
      .then(setPlaces)
      .catch((error) => console.error('could not load places', error))
  }, [])

  // when a place is saved, add it to my restaurants and close the popup
  const handleSaved = (saved: Restaurant) => {
    setRestaurants((current) => [...current.filter((r) => r.id !== saved.id), saved])
    setSelectedPlace(null)
  }

  // skip places i have already saved, so they are not drawn twice
  const savedIds = new Set(restaurants.map((r) => r.placeId))
  const unsaved = places.filter((p) => !savedIds.has(p.id))

  return (
    <MapContainer center={STOCKHOLM} zoom={15} className="map">
      {/* the map images come from openstreetmap, credit is required */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <VisibleAreaWatcher onChange={loadArea} />

      {/* red pins first, so my own pins are drawn on top */}
      {unsaved.map((p) => (
        <Marker
          key={p.id}
          position={[p.latitude, p.longitude]}
          icon={ICONS.UNSAVED}
          eventHandlers={{ click: () => setSelectedPlace(p) }}
        />
      ))}

      {/* my saved restaurants, orange with a fork or green with a smiley */}
      {restaurants.map((r) => (
        <Marker key={r.id} position={[r.latitude, r.longitude]} icon={ICONS[r.status]}>
          <Popup>
            <strong>{r.name}</strong>
            <br />
            {r.status === 'VISITED' ? `My score: ${r.myScore}` : 'Want to visit'}
          </Popup>
        </Marker>
      ))}

      {/* one popup for the selected red pin, only the close button or another pin closes it */}
      {selectedPlace && (
        <Popup
          position={[selectedPlace.latitude, selectedPlace.longitude]}
          offset={[0, -8]}
          closeButton={false}
          closeOnClick={false}
          autoClose={false}
        >
          <PlacePopup
            key={selectedPlace.id}
            place={selectedPlace}
            onSaved={handleSaved}
            onClose={() => setSelectedPlace(null)}
          />
        </Popup>
      )}
    </MapContainer>
  )
}

export default App