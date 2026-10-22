import { Link } from 'react-router'

// a placeholder for parts of the app that are not built yet
export default function ComingSoonPage({ icon, title, text }: { icon: string; title: string; text: string }) {
  return (
    <div className="page coming-soon">
      <span className="home-icon">{icon}</span>
      <h1>{title}</h1>
      <p>{text}</p>
      <p className="muted">Coming soon</p>
      <Link to="/" className="back-link">← Back to start</Link>
    </div>
  )
}