import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import { fetchSummary, type PlaceSummary } from './people'
import { ICONS, countIcon } from './icons'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// all saved places of everyone combined, each place shown once
export default function EveryonePage() {
  const [places, setPlaces] = useState<PlaceSummary[]>([])

  // the clicked pin, its popup shows while this is not null
  const [selected, setSelected] = useState<PlaceSummary | null>(null)

  // loads the combined list once when the page opens
  useEffect(() => {
    fetchSummary()
      .then(setPlaces)
      .catch((error) => console.error('could not load the summary', error))
  }, [])

  return (
    <MapContainer center={STOCKHOLM} zoom={14} className="map">
      {/* the map images come from openstreetmap, credit is required */}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {places.map((p) => (
        <Marker
          key={p.placeId}
          position={[p.latitude, p.longitude]}
          // a number when someone has visited, the orange fork when people only want to go
          icon={p.visitedCount > 0 ? countIcon(p.visitedCount) : ICONS.WANT_TO_VISIT}
          eventHandlers={{ click: () => setSelected(p) }}
        />
      ))}

      {/* only the close button or another pin closes the popup, same as on my map */}
      {selected && (
        <Popup
          position={[selected.latitude, selected.longitude]}
          offset={[0, -8]}
          closeButton={false}
          closeOnClick={false}
          autoClose={false}
        >
          <div className="place-popup">
            <button className="close-button" onClick={() => setSelected(null)} aria-label="Close">×</button>

            <strong>{selected.name}</strong>
            <p>{selected.visitedCount} visited · {selected.wantCount} want to go</p>

            {selected.averageScore !== null && (
              <p>Average score <strong>{selected.averageScore}</strong></p>
            )}

            {selected.visitedBy && <p>Visited by {selected.visitedBy}</p>}
            {selected.wantBy && <p>Wants to go: {selected.wantBy}</p>}
          </div>
        </Popup>
      )}
    </MapContainer>
  )
}