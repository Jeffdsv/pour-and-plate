import { useEffect, useState } from 'react'
import { acceptInvite, checkInvite } from './invites'
import { login } from './auth'

// the page a friend sees when they open an invite link
export default function SignupPage({ token }: { token: string }) {
  // null while checking the link, then true or false
  const [valid, setValid] = useState<boolean | null>(null)

  const [displayName, setDisplayName] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  // checks the link once when the page opens
  useEffect(() => {
    checkInvite(token)
      .then(setValid)
      .catch(() => setValid(false))
  }, [token])

  const submit = () => {
    setError(null)
    acceptInvite(token, { username, displayName, password })
      // log in right away with the new account, then go to the map
      .then(() => login(username, password))
      .then(() => window.location.replace('/'))
      .catch((e: Error) => setError(e.message))
  }

  // still checking, show nothing for a moment
  if (valid === null) {
    return null
  }

  if (!valid) {
    return (
      <div className="login-page">
        <div className="login-box">
          <h1>Pour &amp; Plate</h1>
          <p>This invite link is used or expired. Ask for a new one.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="login-page">
      <form
        className="login-box"
        onSubmit={(e) => {
          e.preventDefault()
          submit()
        }}
      >
        <h1>Join Pour &amp; Plate</h1>

        <label>
          Your name
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} />
          <small>This is the name others will see</small>
        </label>

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
            autoComplete="new-password"
          />
          <small>At least 8 characters</small>
        </label>

        <button type="submit">Create account</button>

        {error && <p className="error">{error}</p>}
      </form>
    </div>
  )
}