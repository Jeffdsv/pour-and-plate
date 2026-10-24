import { Link } from 'react-router'

// the three parts of the app, add more here later and they show up as cards
const SECTIONS = [
  { to: '/map', icon: '🍽️', title: 'Places', text: 'Restaurants, wine bars and pubs on the map' },
  { to: '/wine', icon: '🍷', title: 'Wine', text: 'Grapes and which wine goes with which meal' },
  { to: '/beer', icon: '🍺', title: 'Beer', text: 'Tips, styles and brewing notes' },
  { to: '/stats', icon: '📊', title: 'My stats', text: 'My taste profile and what we like' },
]

// the first page after logging in, one big card per part of the app
export default function HomePage({ name }: { name: string }) {
  return (
    <div className="page">
      <h1 className="home-title">Hi {name}, what are you in the mood for?</h1>
      <div className="home-cards">
        {SECTIONS.map((section) => (
          <Link key={section.to} to={section.to} className="home-card">
            <span className="home-icon">{section.icon}</span>
            <span className="home-card-title">{section.title}</span>
            <span className="home-card-text">{section.text}</span>
          </Link>
        ))}
      </div>
    </div>
  )
}