import type { Restaurant, User } from './types'

// everyone who has an account, sorted by name
export async function fetchUsers(): Promise<User[]> {
  const response = await fetch('/api/users')
  if (!response.ok) {
    throw new Error(`users api answered ${response.status}`)
  }
  return response.json()
}

// someone elses saved restaurants, read only
export async function fetchRestaurantsOf(userId: number): Promise<Restaurant[]> {
  const response = await fetch(`/api/users/${userId}/restaurants`)
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
  const response = await fetch('/api/restaurants/summary')
  if (!response.ok) {
    throw new Error(`summary answered ${response.status}`)
  }
  return response.json()
}