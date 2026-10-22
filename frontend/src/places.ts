import { apiFetch } from './api'

// the five filters on the map, same names as PlaceKind in the backend
export type PlaceKind = 'RESTAURANT' | 'CAFE' | 'WINE' | 'BEER' | 'BAR'

// all of them, what the map shows from the start
export const ALL_KINDS: PlaceKind[] = ['RESTAURANT', 'CAFE', 'WINE', 'BEER', 'BAR']

// one place from my own api, imported from overture maps
export interface Place {
  id: string
  name: string
  category: string
  address: string | null
  city: string | null
  latitude: number
  longitude: number
  website: string | null
  kind: PlaceKind
}

// the visible part of the map, as four edges
export interface Bounds {
  south: number
  west: number
  north: number
  east: number
}

// asks my own api for all places inside the box
// asks my own api for the places inside the box, only the chosen kinds
export async function fetchPlaces(b: Bounds, kinds: PlaceKind[] = ALL_KINDS): Promise<Place[]> {
  // builds the part after ? in the url, like south=59.32&west=18.05&kinds=WINE,BEER
  const params = new URLSearchParams({
    south: String(b.south),
    west: String(b.west),
    north: String(b.north),
    east: String(b.east),
    kinds: kinds.join(','),
  })

  const response = await apiFetch(`/api/places?${params}`)
  if (!response.ok) {
    throw new Error(`places api answered ${response.status}`)
  }
  return response.json()
}