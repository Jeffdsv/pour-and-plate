import { useState } from 'react'
import type { Place } from './places'
import type { Restaurant, RestaurantStatus } from './types'
import { saveRestaurant } from './restaurants'
import RatingForm, { type Rating } from './RatingForm'

// what you see when you click a red pin
// first two buttons, and if you pick visited you get the rating form
export default function PlacePopup({
  place,
  onSaved,
  onClose,
}: {
  place: Place
  onSaved: (r: Restaurant) => void
  onClose: () => void
}) {
  // false shows the two buttons, true shows the rating form
  const [rating, setRating] = useState(false)

  // message from my api if saving fails
  const [error, setError] = useState<string | null>(null)

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

      <strong>{place.name}</strong>
      <br />
      {place.category.replaceAll('_', ' ')}
      {place.address && <><br />{place.address}</>}

      {rating ? (
        <RatingForm
          initial={{ food: 5, atmosphere: 5, value: 5, notes: '' }}
          onSave={(r) => save('VISITED', r)}
          onCancel={() => setRating(false)}
        />
      ) : (
        <div className="buttons">
          <button onClick={() => save('WANT_TO_VISIT')}>Want to visit</button>
          <button onClick={() => setRating(true)}>Visited</button>
        </div>
      )}

      {error && <p className="error">{error}</p>}
    </div>
  )
}