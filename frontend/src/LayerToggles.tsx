import { FORK, SMILEY } from './icons'

// a white star, drawn on the same 24 by 24 grid as the pin shapes
const STAR = `
  <polygon fill="white" points="12,3 14.6,9 21,9.3 16,13.4 17.7,20 12,16.4 6.3,20 8,13.4 3,9.3 9.4,9" />
`

// one round button: in the pin color when on, grey when off
function ToggleButton({
  label,
  color,
  shape,
  on,
  onClick,
}: {
  label: string
  color: string
  shape: string
  on: boolean
  onClick: () => void
}) {
  return (
    <button
      className="layer-toggle"
      style={{ background: on ? color : '#B4B2A9' }}
      aria-pressed={on}
      aria-label={label}
      title={label}
      onClick={onClick}
    >
      {/* the same white shapes as the pins on the map */}
      <svg viewBox="0 0 24 24" dangerouslySetInnerHTML={{ __html: shape }} />
    </button>
  )
}

// three buttons left of the search: show or hide top rated, visited and want to visit
export default function LayerToggles({
  topRated,
  visited,
  want,
  onTopRatedChange,
  onVisitedChange,
  onWantChange,
}: {
  topRated: boolean
  visited: boolean
  want: boolean
  onTopRatedChange: (on: boolean) => void
  onVisitedChange: (on: boolean) => void
  onWantChange: (on: boolean) => void
}) {
  return (
    <div className="layer-toggles">
      <ToggleButton label="Top rated" color="#EF9F27" shape={STAR} on={topRated} onClick={() => onTopRatedChange(!topRated)} />
      <ToggleButton label="Visited" color="#1D9E75" shape={SMILEY} on={visited} onClick={() => onVisitedChange(!visited)} />
      <ToggleButton label="Want to visit" color="#BA7517" shape={FORK} on={want} onClick={() => onWantChange(!want)} />
    </div>
  )
}