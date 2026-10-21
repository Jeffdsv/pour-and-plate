import { useEffect, useState } from 'react'
import { fetchMe, logout } from './auth'
import { fetchUsers } from './people'
import type { User } from './types'
import LoginPage from './LoginPage'
import SignupPage from './SignupPage'
import MapPage from './MapPage'
import InviteButton from './InviteButton'

// decides what to show: the sign up page, the login page or the map
function App() {
  // undefined while checking, null when nobody is logged in, a user when logged in
  const [user, setUser] = useState<User | null | undefined>(undefined)

  // everyone with an account, for the dropdown
  const [users, setUsers] = useState<User[]>([])

  // whose map is shown, null means my own
  const [viewUserId, setViewUserId] = useState<number | null>(null)

  // on page load, ask my api if this browser is already logged in through its session cookie
  useEffect(() => {
    fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  // load the list of people once someone has logged in
  useEffect(() => {
    if (user) {
      fetchUsers()
        .then(setUsers)
        .catch((error) => console.error('could not load users', error))
    }
  }, [user])

  const handleLogout = () => {
    logout().finally(() => {
      setViewUserId(null)
      setUser(null)
    })
  }

  // an invite link looks like /invite/abc123, then show the sign up page instead of everything else
  if (window.location.pathname.startsWith('/invite/')) {
    return <SignupPage token={window.location.pathname.substring('/invite/'.length)} />
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
        {/* only the admin can invite friends */}
        {user.role === 'ADMIN' && <InviteButton />}

        {/* choose whose map to look at */}
        <select
          value={viewUserId ?? ''}
          onChange={(e) => setViewUserId(e.target.value === '' ? null : Number(e.target.value))}
        >
          <option value="">My map</option>
          {users
            .filter((u) => u.id !== user.id)
            .map((u) => (
              <option key={u.id} value={u.id}>{u.displayName}</option>
            ))}
        </select>

        <span className="user-name">{user.displayName}</span>
        <button onClick={handleLogout}>Log out</button>
      </header>

      {/* a new key makes react start the map page fresh when switching person */}
      <MapPage key={viewUserId ?? 'me'} viewUserId={viewUserId} />
    </div>
  )
}

export default App