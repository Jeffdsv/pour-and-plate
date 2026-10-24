import { KIND_ICONS, type PlaceKind } from './places'

// one pill per kind, in the order they show on the map
const OPTIONS: { kind: PlaceKind; label: string }[] = [
  { kind: 'RESTAURANT', label: 'Restaurants' },
  { kind: 'CAFE', label: 'Cafés' },
  { kind: 'WINE', label: 'Wine bars' },
  { kind: 'BEER', label: 'Beer' },
  { kind: 'BAR', label: 'Bars' },
]

// a row of pills on top of the map, click one to turn that kind on or off
// on a phone only the icon shows, so the five fit in one row without scrolling
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
            // the name is hidden on a phone, so screen readers and hovering still get it from here
            aria-label={option.label}
            title={option.label}
            onClick={() => toggle(option.kind)}
          >
            <span className="kind-icon">{KIND_ICONS[option.kind]}</span>
            <span className="kind-label">{option.label}</span>
          </button>
        )
      })}
    </div>
  )
}