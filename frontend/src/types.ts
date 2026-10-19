// same values as RestaurantStatus in java
export type RestaurantStatus = 'VISITED' | 'WANT_TO_VISIT'

// same fields as RestaurantResponse in java
export interface Restaurant {
  id: number
  osmId: string | null
  name: string
  address: string | null
  city: string | null
  latitude: number
  longitude: number
  status: RestaurantStatus
  foodRating: number | null
  atmosphereRating: number | null
  valueRating: number | null
  myScore: number | null
  notes: string | null
  visitedOn: string | null
}