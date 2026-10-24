import type { Restaurant, RestaurantStatus } from './types'
import { apiFetch, csrfHeaders } from './api'
import type { PlaceKind } from './places'

// what we send when saving, same fields as SaveRestaurantRequest in java
export interface SaveRestaurantRequest {
  placeId: string
  name: string
  address: string | null
  city: string | null
  latitude: number
  longitude: number
  status: RestaurantStatus
  foodRating?: number
  atmosphereRating?: number
  valueRating?: number
  notes?: string
  website?: string | null
}

// gets all my saved restaurants
export async function fetchRestaurants(): Promise<Restaurant[]> {
  const response = await apiFetch('/api/restaurants')
  if (!response.ok) {
    throw new Error(`restaurants api answered ${response.status}`)
  }
  return response.json()
}

// saves a restaurant, or updates it if this place is already saved
export async function saveRestaurant(request: SaveRestaurantRequest): Promise<Restaurant> {
  const response = await apiFetch('/api/restaurants', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    // my api sends errors as ProblemDetail json, the detail field has the message
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `saving failed with ${response.status}`)
  }
  return response.json()

}

// removes my review, the place itself stays on the map as a red pin
export async function deleteRestaurant(id: number): Promise<void> {
    const response = await apiFetch(`/api/restaurants/${id}`, { method: 'DELETE', headers: csrfHeaders() })
  if (!response.ok) {
    throw new Error(`removing failed with ${response.status}`)
  }
}

// one row in my taste profile, from GET /api/restaurants/taste
export interface TasteRow {
  category: string
  visits: number
  averageScore: number
  averageFood: number
  averageAtmosphere: number
  averageValue: number
}

// my average ratings per kind of food, best first
export async function fetchTaste(): Promise<TasteRow[]> {
  const response = await apiFetch('/api/restaurants/taste')
  if (!response.ok) {
    throw new Error(`taste answered ${response.status}`)
  }
  return response.json()
}

// one thing a friend did lately, from GET /api/restaurants/activity
export interface FriendActivity {
  id: number
  name: string
  status: RestaurantStatus
  score: number | null
  updatedAt: string
  displayName: string
}

// what my friends have done lately, at most 3 from each, newest first
export async function fetchActivity(): Promise<FriendActivity[]> {
  const response = await apiFetch('/api/restaurants/activity')
  if (!response.ok) {
    throw new Error(`activity answered ${response.status}`)
  }
  return response.json()
}


// one ranked place, from GET /api/restaurants/top
// it has the same details as a place, so the map can open the normal place popup
export interface TopPlace {
  placeId: string
  name: string
  latitude: number
  longitude: number
  category: string
  address: string | null
  city: string | null
  website: string | null
  kind: PlaceKind
  likelyClosed: boolean
  reviewCount: number
  averageScore: number
  rank: number
}

// the best rated places, at least 2 reviews each, best first
// no kinds gives the overall ranking, with kinds the numbers start at 1 within those kinds
export async function fetchTopPlaces(kinds: PlaceKind[]): Promise<TopPlace[]> {
  const query = kinds.length > 0 ? `?kinds=${kinds.join(',')}` : ''
  const response = await apiFetch(`/api/restaurants/top${query}`)
  if (!response.ok) {
    throw new Error(`top places answered ${response.status}`)
  }
  return response.json()
}