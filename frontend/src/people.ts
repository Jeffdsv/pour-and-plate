import type { Restaurant } from './types'
import { apiFetch } from './api'

// everyone who has an account, sorted by name
export async function fetchUsers(): Promise<Person[]> {
  const response = await apiFetch('/api/users')
  if (!response.ok) {
    throw new Error(`users api answered ${response.status}`)
  }
  return response.json()
}

// someone elses saved restaurants, read only
export async function fetchRestaurantsOf(userId: number): Promise<Restaurant[]> {
  const response = await apiFetch(`/api/users/${userId}/restaurants`)
  if (!response.ok) {
    throw new Error(`restaurants of user ${userId} answered ${response.status}`)
  }
  return response.json()
}

// one place on the everyone map, same fields as PlaceSummary in java
export interface PlaceSummary {
  placeId: string
  name: string
  latitude: number
  longitude: number
  visitedCount: number
  wantCount: number
  averageScore: number | null
  visitedBy: string | null
  wantBy: string | null
}

// every place anyone has saved, combined into one row per place
export async function fetchSummary(): Promise<PlaceSummary[]> {
  const response = await apiFetch('/api/restaurants/summary')
  if (!response.ok) {
    throw new Error(`summary answered ${response.status}`)
  }
  return response.json()
}

// a user plus their counts, what GET /api/users gives back now
export interface Person {
  id: number
  username: string
  displayName: string
  role: string
  visitedCount: number
  wantCount: number
}

// a place two or more of us want to visit, from GET /api/restaurants/together
export interface GoTogetherPlace {
  placeId: string
  name: string
  address: string | null
  latitude: number
  longitude: number
  wantCount: number
  wantBy: string
}

// places two or more of us want to visit, the most wanted first
export async function fetchGoTogether(): Promise<GoTogetherPlace[]> {
  const response = await apiFetch('/api/restaurants/together')
  if (!response.ok) {
    throw new Error(`go together answered ${response.status}`)
  }
  return response.json()
}