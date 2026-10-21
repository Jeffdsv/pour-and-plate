import { useState } from 'react'
import type { Restaurant } from './types'
import { deleteRestaurant, saveRestaurant } from './restaurants'
import RatingForm, { type Rating } from './RatingForm'
import Stars from './Stars'

// what you see when you click a saved pin, green or orange
// on a friends map it is read only, so the buttons are hidden
export default function RestaurantPopup({
  restaurant,
  readOnly,
  onSaved,
  onRemoved,
  onClose,
}: {
  restaurant: Restaurant
  readOnly: boolean
  onSaved: (r: Restaurant) => void
  onRemoved: (id: number) => void
  onClose: () => void
}) {
  // true shows the rating form instead of the info
  const [editing, setEditing] = useState(false)

  // message from my api if something fails
  const [error, setError] = useState<string | null>(null)

  const r = restaurant

  // saves new ratings and notes, this also turns a wishlist place into visited
  const saveRating = (rating: Rating) => {
    // saving finds the restaurant by its place id, so it must have one
    if (r.placeId === null) {
      setError('this restaurant is not linked to a place')
      return
    }
    setError(null)
    saveRestaurant({
      placeId: r.placeId,
      name: r.name,
      address: r.address,
      city: r.city,
      latitude: r.latitude,
      longitude: r.longitude,
      status: 'VISITED',
      foodRating: rating.food,
      atmosphereRating: rating.atmosphere,
      valueRating: rating.value,
      notes: rating.notes || undefined,
    })
      .then(onSaved)
      .catch((e: Error) => setError(e.message))
  }

  // asks first, then removes my review
  const remove = () => {
    if (!window.confirm(`Remove ${r.name} from my restaurants?`)) {
      return
    }
    setError(null)
    deleteRestaurant(r.id)
      .then(() => onRemoved(r.id))
      .catch((e: Error) => setError(e.message))
  }

    return (
    <div className="place-popup">
      <button className="close-button" onClick={onClose} aria-label="Close">×</button>

      <h3 className="popup-title">{r.name}</h3>
      {r.address && <p className="popup-address">{r.address}</p>}

      {editing ? (
        <RatingForm
          // start from the current ratings, or the middle if it was on the wishlist
          initial={{
            food: r.foodRating ?? 5,
            atmosphere: r.atmosphereRating ?? 5,
            value: r.valueRating ?? 5,
            notes: r.notes ?? '',
          }}
          onSave={saveRating}
          onCancel={() => setEditing(false)}
        />
      ) : (
        <>
          {r.status === 'VISITED' ? (
            <>
              {/* big score with stars next to it */}
              <div className="score-row">
                <span className="score-box">{r.myScore}</span>
                <Stars score={r.myScore ?? 0} />
              </div>

              <ul className="rating-list">
                <li><span>Food</span><strong>{r.foodRating}</strong></li>
                <li><span>Atmosphere</span><strong>{r.atmosphereRating}</strong></li>
                <li><span>Value</span><strong>{r.valueRating}</strong></li>
              </ul>
            </>
          ) : (
            <span className="chip wishlist">On the wishlist</span>
          )}

          {r.notes && <p className="notes-text">{r.notes}</p>}

          {/* only on my own map, a friends map is read only */}
          {!readOnly && (
            <div className="buttons">
              <button className="pill primary" onClick={() => setEditing(true)}>
                {r.status === 'VISITED' ? 'Change rating' : 'Visited'}
              </button>
              <button className="pill danger" onClick={remove}>Remove</button>
            </div>
          )}
        </>
      )}

      {error && <p className="error">{error}</p>}
    </div>
  )
}