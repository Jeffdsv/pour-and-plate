import { useState } from 'react'
import { login } from './auth'
import type { User } from './types'

// the first page you see when nobody is logged in
export default function LoginPage({ onLoggedIn }: { onLoggedIn: (user: User) => void }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  const submit = () => {
    setError(null)
    login(username, password)
      .then(onLoggedIn)
      .catch((err: Error) => setError(err.message))
  }

  return (
    <div className="login-page">
      <form
        className="login-box"
        onSubmit={(e) => {
          // stops the browser from reloading the page, react handles the form instead
          e.preventDefault()
          submit()
        }}
      >
        <h1>Pour &amp; Plate</h1>
        <p className="login-tagline">Food, friends and hidden gems</p>

        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        </label>

        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>

        <button type="submit">Log in</button>

        {error && <p className="error">{error}</p>}
      </form>
    </div>
  )
}