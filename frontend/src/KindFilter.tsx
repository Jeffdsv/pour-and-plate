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
  topRated,
  onTopRatedChange,
}: {
  selected: PlaceKind[]
  onChange: (kinds: PlaceKind[]) => void
  // NEW: is the top rated mode on, and what to do when its pill is clicked
  topRated: boolean
  onTopRatedChange: (on: boolean) => void
}) {
  // takes the kind out if it was on, puts it in if it was off
  const toggle = (kind: PlaceKind) => {
    onChange(selected.includes(kind) ? selected.filter((k) => k !== kind) : [...selected, kind])
  }

  return (
    <div className="kind-filter">
      {/* NEW: the top rated pill comes first, with a small gap so it reads as a mode, not a kind */}
      <button
        className={topRated ? 'kind-pill active top-rated-pill' : 'kind-pill top-rated-pill'}
        aria-pressed={topRated}
        onClick={() => onTopRatedChange(!topRated)}
      >
        ⭐ Top rated
      </button>

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