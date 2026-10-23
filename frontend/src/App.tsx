import { useEffect, useState } from 'react'
import { Link, Navigate, Route, Routes, useLocation } from 'react-router'
import { fetchMe, logout } from './auth'
import { fetchUsers } from './people'
import type { Person } from './people'
import type { User } from './types'
import LoginPage from './LoginPage'
import SignupPage from './SignupPage'
import MapPage from './MapPage'
import EveryonePage from './EveryonePage'
import SidePanel from './SidePanel'
import HomePage from './HomePage'
import ComingSoonPage from './ComingSoonPage'
import StatsPage from './StatsPage'
import TogetherPage from './TogetherPage'

// which map is shown: my own, everyone combined, or one friend by their id, and together if u want to visit same place.
type View = 'me' | 'everyone' | 'together' | number

// below this width we count it as a phone
const PHONE_WIDTH = 768

// decides what to show: the sign up page, the login page or the app with its pages
function App() {
  // undefined while checking, null when nobody is logged in, a user when logged in
  const [user, setUser] = useState<User | null | undefined>(undefined)

  // everyone with an account and their counts, for the side panel
  const [people, setPeople] = useState<Person[]>([])

  // the map that is shown right now
  const [view, setView] = useState<View>('me')

  // the panel starts open on a computer and hidden on a phone
  const [panelOpen, setPanelOpen] = useState(() => window.innerWidth > PHONE_WIDTH)

  // the current address, so the top bar knows if we are on the map
  const location = useLocation()
  const onMap = location.pathname === '/map'

  // on page load, ask my api if this browser is already logged in through its session cookie
  useEffect(() => {
    fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  // if any api call says 401, my session is gone, so show the login page again
  useEffect(() => {
    const handleExpired = () => {
      setView('me')
      setUser(null)
    }
    window.addEventListener('session-expired', handleExpired)

    // cleanup: stop listening if App is ever removed
    return () => window.removeEventListener('session-expired', handleExpired)
  }, [])

  // load the list of people once someone has logged in
  useEffect(() => {
    if (user) {
      fetchUsers()
        .then(setPeople)
        .catch((error) => console.error('could not load users', error))
    }
  }, [user])

  const handleLogout = () => {
    logout().finally(() => {
      setView('me')
      setUser(null)
    })
  }

  // on a phone the panel covers the map, so close it after picking someone
  const handleSelect = (next: View) => {
    setView(next)
    if (window.innerWidth <= PHONE_WIDTH) {
      setPanelOpen(false)
    }
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
        {/* the side panel only exists on the map page */}
        {onMap && (
          <button className="menu-button" onClick={() => setPanelOpen(!panelOpen)} aria-label="Show maps">
            ☰
          </button>
        )}
        {/* the logo always takes you back to the start page */}
        <Link to="/" className="logo">Pour &amp; Plate</Link>
        <span className="user-name">{user.displayName}</span>
        <button onClick={handleLogout}>Log out</button>
      </header>

      <Routes>
        <Route path="/" element={<HomePage name={user.displayName} />} />

        <Route
          path="/map"
          element={
            <div className="main">
              <SidePanel
                me={user}
                people={people}
                view={view}
                open={panelOpen}
                onSelect={handleSelect}
                onClose={() => setPanelOpen(false)}
              />

                            {/* everyone and go together get their own pages, my map and friends maps share MapPage */}
              {view === 'everyone' ? (
                <EveryonePage />
              ) : view === 'together' ? (
                <TogetherPage />
              ) : (
                <MapPage key={String(view)} viewUserId={view === 'me' ? null : view} />
              )}
            </div>
          }
        />

        <Route
          path="/wine"
          element={<ComingSoonPage icon="🍷" title="Wine" text="Grapes, and which wine goes with which meal." />}
        />
        <Route
          path="/beer"
          element={<ComingSoonPage icon="🍺" title="Beer" text="Tips, beer styles and my own brewing notes." />}
        />
        
        <Route path="/stats" element={<StatsPage />} />

        {/* any other address goes back to the start page */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}

export default App