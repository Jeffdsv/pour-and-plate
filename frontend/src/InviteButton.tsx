import { useState } from 'react'
import { createInvite, type Invite } from './invites'

// admin only: makes an invite link and shows it so it can be copied and sent
export default function InviteButton() {
  const [invite, setInvite] = useState<Invite | null>(null)
  const [copied, setCopied] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const create = () => {
    setError(null)
    setCopied(false)
    createInvite()
      .then(setInvite)
      .catch((e: Error) => setError(e.message))
  }

  // copies the link so it can be pasted into a message
  const copy = () => {
    if (invite === null) {
      return
    }
    navigator.clipboard.writeText(invite.link).then(() => setCopied(true))
  }

  // shows the time the link stops working, like 16:22
  const expires = invite
    ? new Date(invite.expiresAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : ''

  return (
    <div className="invite">
      <button onClick={create}>Invite friend</button>

      {invite && (
        <div className="invite-box">
          <input readOnly value={invite.link} onFocus={(e) => e.target.select()} />
          <button onClick={copy}>{copied ? 'Copied' : 'Copy'}</button>
          <span>Works once, until {expires}</span>
          <button className="close-button-inline" onClick={() => setInvite(null)} aria-label="Close">×</button>
        </div>
      )}

      {error && <span className="error">{error}</span>}
    </div>
  )
}