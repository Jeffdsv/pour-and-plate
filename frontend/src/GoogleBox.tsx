import { useEffect, useState } from 'react'

// the key from .env.local, vite only passes on variables that start with VITE_
const KEY = import.meta.env.VITE_GOOGLE_MAPS_KEY

// same width as the phone layout in index.css
const PHONE_QUERY = '(max-width: 768px)'

// a button that opens a small google maps window for this place
// on a phone the box is too narrow for the rating, so it opens google maps instead
// google shows its own rating there, we never read or store it
export default function GoogleBox({
  name,
  address,
  city,
  onResize,
}: {
  name: string
  address: string | null
  city: string | null
  onResize: () => void
}) {
  const [open, setOpen] = useState(false)

  // the box makes the popup much taller, so the map has to fit it again
  useEffect(() => {
    if (open) {
      onResize()
    }
  }, [open, onResize])

  // google searches for this text, like "Ekstedt, Humlegårdsgatan 17, Stockholm"
  const query = [name, address, city].filter(Boolean).join(', ')

  // on a phone, a normal link to google maps, it opens the app if it is installed
  if (window.matchMedia(PHONE_QUERY).matches) {
    const mapsLink = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`
    return (
      <div className="google-box">
        <a className="google-toggle" href={mapsLink} target="_blank" rel="noopener noreferrer">
          ⭐ Google rating
        </a>
      </div>
    )
  }

  // no key, no box, so the app still works without one
  if (!KEY) {
    return null
  }

  const src = `https://www.google.com/maps/embed/v1/place?key=${KEY}&q=${encodeURIComponent(query)}`

  return (
    <div className="google-box">
      <button className="google-toggle" onClick={() => setOpen(!open)}>
        {open ? 'Hide Google' : '⭐ Google rating'}
      </button>

      {open && (
        <iframe
          className="google-frame"
          src={src}
          title={`${name} on Google Maps`}
          loading="lazy"
          referrerPolicy="no-referrer-when-downgrade"
          allowFullScreen
        />
      )}
    </div>
  )
}