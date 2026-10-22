import type { PlaceKind } from './places'

// one pill per kind, in the order they show on the map
const OPTIONS: { kind: PlaceKind; label: string }[] = [
  { kind: 'RESTAURANT', label: '🍽️ Restaurants' },
  { kind: 'CAFE', label: '☕ Cafés' },
  { kind: 'WINE', label: '🍷 Wine bars' },
  { kind: 'BEER', label: '🍺 Beer places' },
  { kind: 'BAR', label: '🍸 Bars' },
]

// a row of pills on top of the map, click one to turn that kind on or off
export default function KindFilter({
  selected,
  onChange,
}: {
  selected: PlaceKind[]
  onChange: (kinds: PlaceKind[]) => void
}) {
  // takes the kind out if it was on, puts it in if it was off
  const toggle = (kind: PlaceKind) => {
    onChange(selected.includes(kind) ? selected.filter((k) => k !== kind) : [...selected, kind])
  }

  return (
    <div className="kind-filter">
      {OPTIONS.map((option) => {
        const isOn = selected.includes(option.kind)
        return (
          <button
            key={option.kind}
            className={isOn ? 'kind-pill active' : 'kind-pill'}
            aria-pressed={isOn}
            onClick={() => toggle(option.kind)}
          >
            {option.label}
          </button>
        )
      })}
    </div>
  )
}