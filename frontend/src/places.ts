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
}

// the visible part of the map, as four edges
export interface Bounds {
  south: number
  west: number
  north: number
  east: number
}

// asks my own api for all places inside the box
export async function fetchPlaces(b: Bounds): Promise<Place[]> {
  // builds the part after ? in the url, like south=59.32&west=18.05
  const params = new URLSearchParams({
    south: String(b.south),
    west: String(b.west),
    north: String(b.north),
    east: String(b.east),
  })

  const response = await fetch(`/api/places?${params}`)
  if (!response.ok) {
    throw new Error(`places api answered ${response.status}`)
  }
  return response.json()
}