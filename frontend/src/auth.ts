import { csrfHeaders, ensureCsrfToken } from './api'
import type { User } from './types'

// who is logged in in this browser, null if nobody
export async function fetchMe(): Promise<User | null> {
  const response = await fetch('/api/auth/me')
  if (response.status === 401) {
    return null
  }
  if (!response.ok) {
    throw new Error(`me answered ${response.status}`)
  }
  return response.json()
}

// sends username and password, the api answers with a session cookie and the user
export async function login(username: string, password: string): Promise<User> {
  await ensureCsrfToken()
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify({ username, password }),
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `login failed with ${response.status}`)
  }
  return response.json()
}

// ends the session on the server
export async function logout(): Promise<void> {
  await fetch('/api/auth/logout', { method: 'POST', headers: csrfHeaders() })
}