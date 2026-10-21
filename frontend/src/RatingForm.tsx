import { useState } from 'react'
import Stars from './Stars'

// what the form gives back when you press save
export interface Rating {
  food: number
  atmosphere: number
  value: number
  notes: string
}

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

// sliders for food, atmosphere and value plus a notes box
// used both when rating a new place and when changing an old rating
export default function RatingForm({
  initial,
  onSave,
  onCancel,
}: {
  initial: Rating
  onSave: (rating: Rating) => void
  onCancel: () => void
}) {
  // each value starts from what we got in, so old ratings show up pre filled
  const [food, setFood] = useState(initial.food)
  const [atmosphere, setAtmosphere] = useState(initial.atmosphere)
  const [value, setValue] = useState(initial.value)
  const [notes, setNotes] = useState(initial.notes)

  // live preview of my score while moving the sliders
  const score = ((food + atmosphere + value) / 3).toFixed(1)

    return (
    <div className="rating-form">
      <Slider label="Food" value={food} onChange={setFood} />
      <Slider label="Atmosphere" value={atmosphere} onChange={setAtmosphere} />
      <Slider label="Value" value={value} onChange={setValue} />

      {/* the same score box and stars as the popup, updates while you drag */}
      <div className="score-row">
        <span className="score-box">{score}</span>
        <Stars score={Number(score)} />
      </div>

      <textarea
        className="notes"
        placeholder="Notes, like what to order"
        rows={2}
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
      />

      <div className="buttons">
        <button className="pill" onClick={onCancel}>Back</button>
        <button className="pill primary" onClick={() => onSave({ food, atmosphere, value, notes })}>Save</button>
      </div>
    </div>
  )
}