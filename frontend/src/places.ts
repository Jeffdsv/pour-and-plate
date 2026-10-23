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

// searches all of sweden for places whose name contains the text, best match first
export async function searchPlaces(text: string): Promise<Place[]> {
  const params = new URLSearchParams({ q: text })
  const response = await apiFetch(`/api/places/search?${params}`)
  if (!response.ok) {
    throw new Error(`search answered ${response.status}`)
  }
  return response.json()
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

// a place close to me, the same as a place plus how far away it is
export interface NearbyPlace extends Place {
  distanceMeters: number
}

// the closest places of the chosen kinds within radius meters, closest first
export async function fetchNear(lat: number, lon: number, radius: number, kinds: PlaceKind[]): Promise<NearbyPlace[]> {
  const params = new URLSearchParams({
    lat: String(lat),
    lon: String(lon),
    radius: String(radius),
    kinds: kinds.join(','),
  })
  const response = await apiFetch(`/api/places/near?${params}`)
  if (!response.ok) {
    throw new Error(`near me answered ${response.status}`)
  }
  return response.json()
}