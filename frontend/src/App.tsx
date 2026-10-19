import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import type { Restaurant } from './types'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// green for places i have rated, orange for my wishlist
const COLORS = {
  VISITED: '#1D9E75',
  WANT_TO_VISIT: '#BA7517',
}

function App() {
  // my restaurants from the api, starts as an empty list
  const [restaurants, setRestaurants] = useState<Restaurant[]>([])

  // runs once when the page loads and fetches my restaurants
  useEffect(() => {
    fetch('/api/restaurants')
      .then((response) => response.json())
      .then((data: Restaurant[]) => setRestaurants(data))
      .catch((error) => console.error('could not load restaurants', error))
  }, [])

  return (
    <MapContainer center={STOCKHOLM} zoom={14} className="map">
      {/* the map images come from openstreetmap, credit is required */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* one colored circle per restaurant, click it to see the popup */}
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