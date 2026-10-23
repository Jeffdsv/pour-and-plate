import { useState } from 'react'
import { ALL_KINDS, fetchNear, type NearbyPlace, type Place, type PlaceKind } from './places'

// how far near me looks
const RADIUS_METERS = 1000

// 120 m or 1.4 km, rounded so it is easy to read
function formatDistance(meters: number) {
  return meters < 1000 ? `${Math.round(meters / 10) * 10} m` : `${(meters / 1000).toFixed(1)} km`
}

// a button that finds where i am and lists the closest places of the kinds i have picked
export default function NearMe({
  kinds,
  onLocated,
  onPick,
}: {
  kinds: PlaceKind[]
  onLocated: (lat: number, lon: number) => void
  onPick: (place: Place) => void
}) {
  // null means the list is closed
  const [results, setResults] = useState<NearbyPlace[] | null>(null)

  // a short message while looking, or when something went wrong
  const [status, setStatus] = useState<string | null>(null)

  const findNearMe = () => {
    if (!navigator.geolocation) {
      setStatus('Your browser cannot share its location')
      return
    }

    setStatus('Finding you...')
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        onLocated(latitude, longitude)

        // no pills picked means look for every kind
        fetchNear(latitude, longitude, RADIUS_METERS, kinds.length > 0 ? kinds : ALL_KINDS)
          .then((found) => {
            setResults(found)
            setStatus(found.length === 0 ? 'Nothing within 1 km' : null)
          })
          .catch(() => setStatus('Could not load places near you'))
      },
      // the user said no, or the location could not be found
      () => setStatus('Your location was not shared'),
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  const pick = (place: Place) => {
    onPick(place)
    setResults(null)
  }

  return (
    <div className="near-me">
      <button className="near-me-button" onClick={findNearMe}>
        📍 Near me
      </button>

      {status && <div className="near-me-status">{status}</div>}

      {results && results.length > 0 && (
        <ul className="search-results near-me-results">
          {results.map((place) => (
            <li key={place.id}>
              <button className="search-result" onClick={() => pick(place)}>
                <span className="result-name">{place.name}</span>
                <span className="result-where">
                  {formatDistance(place.distanceMeters)} · {place.address ?? place.city}
                </span>
              </button>
            </li>
          ))}
          <li>
            <button className="search-result close-results" onClick={() => setResults(null)}>
              Close list
            </button>
          </li>
        </ul>
      )}
    </div>
  )
}