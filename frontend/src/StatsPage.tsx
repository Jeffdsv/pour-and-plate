import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { fetchActivity, fetchTaste, type FriendActivity, type TasteRow } from './restaurants'
import { categoryLabel } from './places'
import Stars from './Stars'

// which of the three i rate highest overall, weighted by how many places each row has
function bestAspect(rows: TasteRow[]) {
  const total = rows.reduce((sum, row) => sum + row.visits, 0)
  const weighted = (pick: (row: TasteRow) => number) =>
    rows.reduce((sum, row) => sum + pick(row) * row.visits, 0) / total

  const aspects = [
    { name: 'Food', value: weighted((row) => row.averageFood) },
    { name: 'Atmosphere', value: weighted((row) => row.averageAtmosphere) },
    { name: 'Value', value: weighted((row) => row.averageValue) },
  ]
  // sort highest first and take the first one
  return aspects.sort((a, b) => b.value - a.value)[0].name
}

// how long ago, in words, like "5 minutes ago" or "yesterday"
const TIME_UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
  ['year', 31_536_000],
  ['month', 2_592_000],
  ['week', 604_800],
  ['day', 86_400],
  ['hour', 3_600],
  ['minute', 60],
]

function timeAgo(isoTime: string) {
  // negative, since it is in the past
  const seconds = (Date.parse(isoTime) - Date.now()) / 1000
  const format = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

  for (const [unit, size] of TIME_UNITS) {
    if (Math.abs(seconds) >= size) {
      return format.format(Math.round(seconds / size), unit)
    }
  }
  return 'just now'
}

// my stats: a few numbers at the top, my taste profile, and the latest from my friends
export default function StatsPage() {
  const [rows, setRows] = useState<TasteRow[]>([])
  const [activity, setActivity] = useState<FriendActivity[]>([])

  // false until the answers have come, so the empty messages do not flash while loading
  const [loaded, setLoaded] = useState(false)

  // both lists are loaded at the same time, and the page waits for both
  useEffect(() => {
    Promise.all([fetchTaste(), fetchActivity()])
      .then(([taste, latest]) => {
        setRows(taste)
        setActivity(latest)
      })
      .catch((error) => console.error('could not load my stats', error))
      .finally(() => setLoaded(true))
  }, [])

  const totalVisits = rows.reduce((sum, row) => sum + row.visits, 0)

  return (
    <div className="page">
      <div className="stats">
        <h1 className="home-title">My stats</h1>

        {loaded && rows.length === 0 && (
          <p className="muted stats-empty">Rate a few places you have visited and your taste profile shows up here.</p>
        )}

        {rows.length > 0 && (
          <>
            {/* three quick numbers at the top */}
            <div className="stat-cards">
              <div className="stat-card">
                <span className="stat-label">Places rated</span>
                <span className="stat-value">{totalVisits}</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">Favourite kind</span>
                <span className="stat-value">{categoryLabel(rows[0].category)}</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">You rate highest</span>
                <span className="stat-value">{bestAspect(rows)}</span>
              </div>
            </div>

            <h2 className="stats-heading">🍕 By kind of food</h2>
            <ul className="taste-list">
              {rows.map((row) => (
                <li key={row.category} className="taste-row">
                  <div className="taste-main">
                    <span className="result-name">{categoryLabel(row.category)}</span>
                    <span className="result-where">
                      {row.visits} visited · food {row.averageFood} · atmosphere {row.averageAtmosphere} · value{' '}
                      {row.averageValue}
                    </span>
                  </div>
                  <div className="taste-score">
                    <span className="score-box">{row.averageScore}</span>
                    <Stars score={row.averageScore} />
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}

        {/* the latest from my friends, shown even before i have rated anything myself */}
        {loaded && (
          <>
            <h2 className="stats-heading">📰 Latest from friends</h2>
            {activity.length === 0 ? (
              <p className="muted">Nothing from your friends yet.</p>
            ) : (
              <ul className="taste-list">
                {activity.map((item) => (
                  <li key={item.id} className="taste-row">
                    <div className="taste-main">
                      <span className="activity-text">
                        <strong>{item.displayName}</strong>{' '}
                        {item.status === 'VISITED' ? 'visited' : 'wants to visit'} <strong>{item.name}</strong>
                      </span>
                      <span className="result-where">{timeAgo(item.updatedAt)}</span>
                    </div>
                    {/* only visits have a score, wishes do not */}
                    {item.score !== null && <span className="score-box">{item.score}</span>}
                  </li>
                ))}
              </ul>
            )}
          </>
        )}

        <Link to="/" className="back-link">← Back to start</Link>
      </div>
    </div>
  )
}