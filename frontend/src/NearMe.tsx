import { useState } from 'react'

// a button that finds where i am, the near you panel then shows what is around
export default function NearMe({ onLocated }: { onLocated: (lat: number, lon: number) => void }) {
  // a short message while looking, or when something went wrong
  const [status, setStatus] = useState<string | null>(null)

  const findMe = () => {
    if (!navigator.geolocation) {
      setStatus('Your browser cannot share its location')
      return
    }

    setStatus('Finding you...')
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setStatus(null)
        onLocated(position.coords.latitude, position.coords.longitude)
      },
      // the user said no, or the location could not be found
      () => setStatus('Your location was not shared'),
      { enableHighAccuracy: true, timeout: 10000 },
    )
  }

  return (
    <div className="near-me">
      <button className="near-me-button" onClick={findMe}>
        📍 Near me
      </button>
      {status && <div className="near-me-status">{status}</div>}
    </div>
  )
}