import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { fetchTaste, type TasteRow } from './restaurants'
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

// my stats: a few numbers at the top, then my taste profile per kind of food
export default function StatsPage() {
  const [rows, setRows] = useState<TasteRow[]>([])

  // false until the answer has come, so the empty message does not flash while loading
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    fetchTaste()
      .then(setRows)
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

        <Link to="/" className="back-link">← Back to start</Link>
      </div>
    </div>
  )
}