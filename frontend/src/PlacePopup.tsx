import { useEffect, useState } from 'react'
import type { Place } from './places'
import type { Restaurant, RestaurantStatus } from './types'
import { saveRestaurant } from './restaurants'
import RatingForm, { type Rating } from './RatingForm'
import WebsiteLink from './WebsiteLink'

// what you see when you click a red pin
// first two buttons, and if you pick visited you get the rating form
export default function PlacePopup({
  place,
  onSaved,
  onClose,
  onResize,
}: {
  place: Place
  onSaved: (r: Restaurant) => void
  onClose: () => void
  onResize: () => void
}) {
  // false shows the two buttons, true shows the rating form
  const [rating, setRating] = useState(false)

  // message from my api if saving fails
  const [error, setError] = useState<string | null>(null)
     useEffect(() => {
    // only when the form opens, not when the popup itself opens
    if (rating) {
      onResize()
    }
  }, [rating, onResize])

  // sends the place to my api, ratings and notes only come with visited
  const save = (status: RestaurantStatus, r?: Rating) => {
    setError(null)
    saveRestaurant({
      placeId: place.id,
      name: place.name,
      address: place.address,
      city: place.city,
      latitude: place.latitude,
      longitude: place.longitude,
      website: place.website,
      status,
      ...(r
        ? { foodRating: r.food, atmosphereRating: r.atmosphere, valueRating: r.value, notes: r.notes || undefined }
        : {}),
    })
      .then(onSaved)
      .catch((e: Error) => setError(e.message))
  }

    return (
    <div className="place-popup">
      <button className="close-button" onClick={onClose} aria-label="Close">×</button>

      <h3 className="popup-title">{place.name}</h3>
      <span className="chip">{place.category.replaceAll('_', ' ')}</span>
            {place.address && <p className="popup-address">{place.address}</p>}
      <WebsiteLink url={place.website} />

      {rating ? (
        <RatingForm
          initial={{ food: 5, atmosphere: 5, value: 5, notes: '' }}
          onSave={(r) => save('VISITED', r)}
          onCancel={() => setRating(false)}
        />
      ) : (
        <div className="buttons">
          <button className="pill" onClick={() => save('WANT_TO_VISIT')}>Want to visit</button>
          <button className="pill primary" onClick={() => setRating(true)}>Visited</button>
        </div>
      )}

      {error && <p className="error">{error}</p>}
    </div>
  )
}