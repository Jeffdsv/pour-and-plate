import { useEffect, useState } from 'react'

// the key from .env.local, vite only passes on variables that start with VITE_
const KEY = import.meta.env.VITE_GOOGLE_MAPS_KEY

// a button that opens a small google maps window for this place
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

  // no key, no box, so the app still works without one
  if (!KEY) {
    return null
  }

  // google searches for this text
  const query = [name, address, city].filter(Boolean).join(', ')
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