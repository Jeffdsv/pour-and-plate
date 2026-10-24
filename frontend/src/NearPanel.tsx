import { useEffect, useState } from 'react'
import { ALL_KINDS, KIND_ICONS, categoryLabel, fetchNear, type NearbyPlace, type Place, type PlaceKind } from './places'

// all kinds, or just one
type KindChoice = 'ALL' | PlaceKind

// the type chips at the top of the panel
const KIND_CHIPS: { value: KindChoice; label: string; name: string }[] = [
  { value: 'ALL', label: 'All', name: 'All places' },
  { value: 'RESTAURANT', label: KIND_ICONS.RESTAURANT, name: 'Restaurants' },
  { value: 'CAFE', label: KIND_ICONS.CAFE, name: 'Cafés' },
  { value: 'WINE', label: KIND_ICONS.WINE, name: 'Wine bars' },
  { value: 'BEER', label: KIND_ICONS.BEER, name: 'Beer' },
  { value: 'BAR', label: KIND_ICONS.BAR, name: 'Bars' },
]

// the distance chips, in meters
const RADII = [500, 1000, 2000]

// 120 m, 1 km or 1.4 km, rounded so it is easy to read
function formatDistance(meters: number) {
  if (meters < 1000) {
    return `${Math.round(meters / 10) * 10} m`
  }
  // Number() drops a .0 at the end, so 1.0 km becomes 1 km
  return `${Number((meters / 1000).toFixed(1))} km`
}


// the near you panel: chips for type and distance, and a list of the closest places
export default function NearPanel({
  position,
  onPick,
  onClose,
}: {
  position: [number, number]
  onPick: (place: Place) => void
  onClose: () => void
}) {
  const [kind, setKind] = useState<KindChoice>('ALL')
  const [radius, setRadius] = useState(1000)

  // the last answer, together with what was asked for
  const [answer, setAnswer] = useState<{ key: string; places: NearbyPlace[] } | null>(null)

  // what we are asking for right now, as one text, changes when a chip or the position changes
  const requestKey = `${position[0]},${position[1]},${kind},${radius}`

  // ask again whenever the position or a chip changes
  useEffect(() => {
    let cancelled = false
    const kinds = kind === 'ALL' ? ALL_KINDS : [kind]

    fetchNear(position[0], position[1], radius, kinds)
      .then((found) => {
        if (!cancelled) {
          setAnswer({ key: requestKey, places: found })
        }
      })
      .catch((error) => console.error('could not load places near you', error))

    return () => {
      cancelled = true
    }
  }, [position, kind, radius, requestKey])

  // still loading while the answer we have is for an older question
  const loading = answer?.key !== requestKey
  const places = answer?.places ?? []

  return (
    <aside className="near-panel">
      <div className="near-header">
        <strong>Near you</strong>
        <button className="close-button-inline" onClick={onClose} aria-label="Close">
          ×
        </button>
      </div>

      <div className="chip-row">
        {KIND_CHIPS.map((chip) => (
          <button
            key={chip.value}
            className={kind === chip.value ? 'chip-button active' : 'chip-button'}
            title={chip.name}
            aria-label={chip.name}
            onClick={() => setKind(chip.value)}
          >
            {chip.label}
          </button>
        ))}
      </div>

      <div className="chip-row">
        {RADII.map((meters) => (
          <button
            key={meters}
            className={radius === meters ? 'chip-button active' : 'chip-button'}
            onClick={() => setRadius(meters)}
          >
            {formatDistance(meters)}
          </button>
        ))}
      </div>

      <ul className="near-list">
        {loading && <li className="near-empty">Looking around...</li>}

        {!loading && places.length === 0 && (
          <li className="near-empty">Nothing within {formatDistance(radius)}</li>
        )}

        {!loading &&
          places.map((place) => (
            <li key={place.id}>
              <button className="near-row" onClick={() => onPick(place)}>
                <span className="near-text">
                  <span className="result-name">{place.name}</span>
                  <span className="result-where">
                    {formatDistance(place.distanceMeters)} · {categoryLabel(place.category)}
                    {place.address ? ` · ${place.address}` : ''}
                  </span>
                </span>
                <span className="near-icon" aria-hidden="true">
                  {KIND_ICONS[place.kind]}
                </span>
              </button>
            </li>
          ))}
      </ul>
    </aside>
  )
}