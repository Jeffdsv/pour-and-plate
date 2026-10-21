import { useEffect, useState } from 'react'
import { fetchMe, logout } from './auth'
import type { User } from './types'
import LoginPage from './LoginPage'
import MapPage from './MapPage'

// decides what to show: the login page or the map
function App() {
  // undefined while checking, null when nobody is logged in, a user when logged in
  const [user, setUser] = useState<User | null | undefined>(undefined)

  // on page load, ask my api if this browser is already logged in through its session cookie
  useEffect(() => {
    fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  const handleLogout = () => {
    logout().finally(() => setUser(null))
  }

  // still checking, show nothing for a moment
  if (user === undefined) {
    return null
  }

  if (user === null) {
    return <LoginPage onLoggedIn={setUser} />
  }

  return (
    <div className="app">
      <header className="top-bar">
        <strong>Pour &amp; Plate</strong>
        <span className="user-name">{user.displayName}</span>
        <button onClick={handleLogout}>Log out</button>
      </header>
      <MapPage />
    </div>
  )
}

export default App