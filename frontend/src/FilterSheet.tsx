import KindFilter from './KindFilter'
import LayerToggles from './LayerToggles'
import type { PlaceKind } from './places'

// the panel that slides up from the bottom on a phone, with everything you can show or hide on the map
// it reuses the same buttons as the computer version, only the css makes them bigger
export default function FilterSheet({
  kinds,
  onKindsChange,
  topRated,
  visited,
  want,
  onTopRatedChange,
  onVisitedChange,
  onWantChange,
  onClose,
}: {
  kinds: PlaceKind[]
  onKindsChange: (kinds: PlaceKind[]) => void
  topRated: boolean
  visited: boolean
  want: boolean
  onTopRatedChange: (on: boolean) => void
  onVisitedChange: (on: boolean) => void
  onWantChange: (on: boolean) => void
  onClose: () => void
}) {
  return (
    <div className="filter-sheet" role="dialog" aria-label="Filters">
      <div className="filter-sheet-header">
        <h3>Show</h3>
        <button className="close-button-inline" aria-label="Close" onClick={onClose}>
          ✕
        </button>
      </div>
      <LayerToggles
        topRated={topRated}
        visited={visited}
        want={want}
        onTopRatedChange={onTopRatedChange}
        onVisitedChange={onVisitedChange}
        onWantChange={onWantChange}
      />

      <h3>Places</h3>
      <KindFilter selected={kinds} onChange={onKindsChange} />
    </div>
  )
}