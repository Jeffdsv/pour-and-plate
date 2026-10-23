import { useEffect, useState } from 'react'
import { MapContainer, Marker, Popup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import VectorBaseMap from './VectorBaseMap'
import { countIcon } from './icons'
import { fetchGoTogether, type GoTogetherPlace } from './people'

// center of stockholm as latitude and longitude
const STOCKHOLM: [number, number] = [59.3293, 18.0686]

// places that two or more of us want to visit, one number pin each with how many want to go
export default function TogetherPage() {
  const [places, setPlaces] = useState<GoTogetherPlace[]>([])

  // false until the answer has come, so the empty message does not flash while loading
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    fetchGoTogether()
      .then(setPlaces)
      .catch((error) => console.error('could not load go together', error))
      .finally(() => setLoaded(true))
  }, [])

  return (
    <>
      {loaded && places.length === 0 && (
        <div className="map-note">
          Nothing here yet. When two of you want to visit the same place, it shows up here.
        </div>
      )}

      <MapContainer center={STOCKHOLM} zoom={13} maxZoom={19} fadeAnimation={false} className="map">
        <VectorBaseMap />

        {places.map((place) => (
          <Marker key={place.placeId} position={[place.latitude, place.longitude]} icon={countIcon(place.wantCount)}>
            <Popup>
              <div className="place-popup">
                <h3 className="popup-title">{place.name}</h3>
                {place.address && <p className="popup-address">{place.address}</p>}
                <p>
                  <strong>{place.wantCount} want to go:</strong> {place.wantBy}
                </p>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </>
  )
}