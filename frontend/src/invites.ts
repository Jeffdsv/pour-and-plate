
import { csrfHeaders, ensureCsrfToken } from './api'

// what the api gives back when the admin makes a link
export interface Invite {
  link: string
  expiresAt: string
}

// admin only: makes a new link that works once, for 15 minutes
export async function createInvite(): Promise<Invite> {
  const response = await fetch('/api/invites', { method: 'POST', headers: csrfHeaders() })
  if (!response.ok) {
    throw new Error(`creating invite failed with ${response.status}`)
  }
  return response.json()
}

// true if the link can still be used, the sign up page checks this first
export async function checkInvite(token: string): Promise<boolean> {
  const response = await fetch(`/api/invites/${encodeURIComponent(token)}`)
  if (!response.ok) {
    return false
  }
  const body: { valid: boolean } = await response.json()
  return body.valid
}

// what a friend fills in on the sign up page
export interface SignupDetails {
  username: string
  displayName: string
  password: string
}

// creates the friend account and uses up the link
export async function acceptInvite(token: string, details: SignupDetails): Promise<void> {
  await ensureCsrfToken()
  const response = await fetch(`/api/invites/${encodeURIComponent(token)}/accept`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify(details),
  })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    // validation errors come as a list of fields, everything else as one message
    const message = problem?.errors
      ? Object.entries(problem.errors).map(([field, msg]) => `${field} ${msg}`).join(', ')
      : problem?.detail
    throw new Error(message ?? `sign up failed with ${response.status}`)
  }
}