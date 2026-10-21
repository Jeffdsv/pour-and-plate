import { useState } from 'react'
import type { Place } from './places'
import type { Restaurant, RestaurantStatus } from './types'
import { saveRestaurant } from './restaurants'

// one slider from 1 to 10 with its label and current value
function Slider({ label, value, onChange }: { label: string; value: number; onChange: (v: number) => void }) {
  return (
    <label className="slider">
      <span>{label}</span>
      <input
        type="range"
        min={1}
        max={10}
        step={1}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      />
      <strong>{value}</strong>
    </label>
  )
}

// what you see when you click a grey dot
// first two buttons, and if you pick visited you get three sliders
export default function PlacePopup({ place, onSaved }: { place: Place; onSaved: (r: Restaurant) => void }) {
  // false shows the two buttons, true shows the sliders
  const [rating, setRating] = useState(false)

  // the three slider values, all start in the middle
  const [food, setFood] = useState(5)
  const [atmosphere, setAtmosphere] = useState(5)
  const [value, setValue] = useState(5)

  // message from my api if saving fails
  const [error, setError] = useState<string | null>(null)

  // live preview of my score while moving the sliders
  const score = ((food + atmosphere + value) / 3).toFixed(1)

  // sends the place to my api, ratings are only included when visited
  const save = (status: RestaurantStatus) => {
    setError(null)
    saveRestaurant({
      placeId: place.id,
      name: place.name,
      address: place.address,
      city: place.city,
      latitude: place.latitude,
      longitude: place.longitude,
      status,
      ...(status === 'VISITED' ? { foodRating: food, atmosphereRating: atmosphere, valueRating: value } : {}),
    })
      .then(onSaved)
      .catch((e: Error) => setError(e.message))
  }

  return (
    <div className="place-popup">
      <strong>{place.name}</strong>
      <br />
      {place.category.replaceAll('_', ' ')}
      {place.address && <><br />{place.address}</>}

      {rating ? (
        <>
          <Slider label="Food" value={food} onChange={setFood} />
          <Slider label="Atmosphere" value={atmosphere} onChange={setAtmosphere} />
          <Slider label="Value" value={value} onChange={setValue} />
          <p>My score: <strong>{score}</strong></p>
          <div className="buttons">
            <button onClick={() => setRating(false)}>Back</button>
            <button onClick={() => save('VISITED')}>Save</button>
          </div>
        </>
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