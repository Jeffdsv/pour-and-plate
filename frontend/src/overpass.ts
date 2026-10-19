// a restaurant from openstreetmap that i have not necessarily saved
export interface OsmRestaurant {
  osmId: string
  name: string
  latitude: number
  longitude: number
  cuisine: string | null
  address: string | null
}

// the visible part of the map, as four edges
export interface Bounds {
  south: number
  west: number
  north: number
  east: number
}

// main server overpass-api.de was timing out, this is a public mirror of the same service
const OVERPASS_URL = 'https://overpass.kumi.systems/api/interpreter'

// asks overpass for every named restaurant inside the box
export async function fetchOsmRestaurants(b: Bounds): Promise<OsmRestaurant[]> {
  const box = `${b.south},${b.west},${b.north},${b.east}`

  // overpass has its own query language
  // node = a single point, way = a building outline, out center gives a middle point for ways
  const query = `
    [out:json][timeout:25];
    (
      node["amenity"="restaurant"]["name"](${box});
      way["amenity"="restaurant"]["name"](${box});
    );
    out center tags;
  `

  const response = await fetch(OVERPASS_URL, {
    method: 'POST',
    body: new URLSearchParams({ data: query }),
  })
  if (!response.ok) {
    throw new Error(`overpass answered ${response.status}`)
  }

  const json = await response.json()
  return json.elements.map(toRestaurant)
}

// what overpass actually sends back for each place
interface OverpassElement {
  type: 'node' | 'way'
  id: number
  lat?: number
  lon?: number
  center?: { lat: number; lon: number }
  tags?: Record<string, string>
}

// turns one overpass element into our own simpler shape
function toRestaurant(e: OverpassElement): OsmRestaurant {
  const tags = e.tags ?? {}
  const street = tags['addr:street']
  const number = tags['addr:housenumber']

  return {
    // same format as the osmId we save in our database, like node/12345
    osmId: `${e.type}/${e.id}`,
    name: tags.name,
    // points have lat and lon, buildings only have a center
    latitude: e.lat ?? e.center!.lat,
    longitude: e.lon ?? e.center!.lon,
    cuisine: tags.cuisine ?? null,
    address: street ? `${street} ${number ?? ''}`.trim() : null,
  }
}