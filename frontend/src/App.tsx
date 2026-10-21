import { useCallback, useEffect, useState } from 'react'
import { MapContainer, TileLayer, CircleMarker, Popup, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import type { Restaurant } from './types'
import { fetchPlaces, type Bounds, type Place } from './places'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// only load places when zoomed in this far, my api refuses areas that are too big
const MIN_ZOOM = 14

// green for places i have rated, orange for my wishlist, grey for everything else
const COLORS = {
  VISITED: '#1D9E75',
  WANT_TO_VISIT: '#BA7517',
  UNSAVED: '#888780',
}

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

  // runs once when the page loads and fetches my restaurants
  useEffect(() => {
    fetch('/api/restaurants')
      .then((response) => response.json())
      .then((data: Restaurant[]) => setRestaurants(data))
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

  // skip places i have already saved, so they are not drawn twice
  const savedIds = new Set(restaurants.map((r) => r.osmId))
  const unsaved = places.filter((p) => !savedIds.has(p.id))

  return (
    <MapContainer center={STOCKHOLM} zoom={15} className="map">
      {/* the map images come from openstreetmap, credit is required */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <VisibleAreaWatcher onChange={loadArea} />

      {/* grey dots first, so my own pins are drawn on top */}
      {unsaved.map((p) => (
        <CircleMarker
          key={p.id}
          center={[p.latitude, p.longitude]}
          radius={6}
          pathOptions={{ color: COLORS.UNSAVED, fillColor: COLORS.UNSAVED, fillOpacity: 0.7 }}
        >
          <Popup>
            <strong>{p.name}</strong>
            {/* sushi_restaurant becomes sushi restaurant */}
            <br />
            {p.category.replaceAll('_', ' ')}
            {p.address && <><br />{p.address}</>}
            {p.website && <><br /><a href={p.website} target="_blank" rel="noreferrer">Website</a></>}
          </Popup>
        </CircleMarker>
      ))}

      {/* my saved restaurants, bigger and colored */}
      {restaurants.map((r) => (
        <CircleMarker
          key={r.id}
          center={[r.latitude, r.longitude]}
          radius={10}
          pathOptions={{ color: COLORS[r.status], fillColor: COLORS[r.status], fillOpacity: 0.8 }}
        >
          <Popup>
            <strong>{r.name}</strong>
            <br />
            {r.status === 'VISITED' ? `My score: ${r.myScore}` : 'Want to visit'}
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  )
}

export default App