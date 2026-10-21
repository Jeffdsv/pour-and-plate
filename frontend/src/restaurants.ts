import type { Restaurant, RestaurantStatus } from './types'

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
}

// gets all my saved restaurants
export async function fetchRestaurants(): Promise<Restaurant[]> {
  const response = await fetch('/api/restaurants')
  if (!response.ok) {
    throw new Error(`restaurants api answered ${response.status}`)
  }
  return response.json()
}

// saves a restaurant, or updates it if this place is already saved
export async function saveRestaurant(request: SaveRestaurantRequest): Promise<Restaurant> {
  const response = await fetch('/api/restaurants', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
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
  const response = await fetch(`/api/restaurants/${id}`, { method: 'DELETE' })
  if (!response.ok) {
    throw new Error(`removing failed with ${response.status}`)
  }
}