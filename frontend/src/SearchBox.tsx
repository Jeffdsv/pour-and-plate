import { useEffect, useState } from 'react'
import { searchPlaces, type Place } from './places'

// how long to wait after the last key press before asking the api
const DELAY_MS = 300

// a search field with a list of matches under it, clicking one tells MapPage which place was picked
export default function SearchBox({ onPick }: { onPick: (place: Place) => void }) {
  const [text, setText] = useState('')
  const [results, setResults] = useState<Place[]>([])

  // search when the user stops typing for a moment, not on every single letter
  useEffect(() => {
    const trimmed = text.trim()
    if (trimmed.length < 2) {
      return
    }

    let cancelled = false
    const timer = setTimeout(() => {
      searchPlaces(trimmed)
        .then((found) => {
          if (!cancelled) {
            setResults(found)
          }
        })
        .catch((error) => console.error('search failed', error))
    }, DELAY_MS)

    // a new letter cancels the waiting search, and ignores an old answer that comes back late
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [text])

  // nothing to show until there are at least two letters
  const visible = text.trim().length < 2 ? [] : results

  const pick = (place: Place) => {
    onPick(place)
    setText('')
    setResults([])
  }

  return (
    <div className="search-box">
      <input
        className="search-input"
        type="search"
        placeholder="Search places in Sweden"
        aria-label="Search places"
        value={text}
        onChange={(e) => setText(e.target.value)}
      />

      {visible.length > 0 && (
        <ul className="search-results">
          {visible.map((place) => (
            <li key={place.id}>
              <button className="search-result" onClick={() => pick(place)}>
                <span className="result-name">{place.name}</span>
                <span className="result-where">{[place.address, place.city].filter(Boolean).join(', ')}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}