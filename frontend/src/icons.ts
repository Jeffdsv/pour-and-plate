import L from 'leaflet'

// the white shapes inside the pins, drawn with svg on a 24 by 24 grid

// a white plate with a thin rim, covers about 70 percent of the pin
const PLATE = `
  <circle cx="12" cy="12" r="8.4" fill="white" />
  <circle cx="12" cy="12" r="5.4" fill="none" stroke="#E24B4A" stroke-opacity="0.35" stroke-width="1" />
`

// a white fork with three tines
const FORK = `
  <path fill="white" d="M9 4 V9 A3 3 0 0 0 11 11.83 V20 H13 V11.83 A3 3 0 0 0 15 9 V4
                         H13.8 V9 H12.6 V4 H11.4 V9 H10.2 V4 Z" />
`

// a white smiley: two eyes and a curved mouth
const SMILEY = `
  <circle cx="9" cy="10" r="1.4" fill="white" />
  <circle cx="15" cy="10" r="1.4" fill="white" />
  <path d="M8 14 Q12 18 16 14" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round" />
`

// builds one leaflet icon: a colored circle with a white shape inside
function makeIcon(color: string, shape: string, size: number) {
  return L.divIcon({
    // our own class, so leaflet does not add its default white square
    className: 'pin-icon',
    html: `<div class="pin-body" style="background:${color}"><svg viewBox="0 0 24 24">${shape}</svg></div>`,
    iconSize: [size, size],
    // the middle of the circle sits exactly on the place
    iconAnchor: [size / 2, size / 2],
    // popups open just above the pin
    popupAnchor: [0, -size / 2],
  })
}

// created once and reused for every pin, which is faster than making a new icon each time
export const ICONS = {
  UNSAVED: makeIcon('#E24B4A', PLATE, 24),
  WANT_TO_VISIT: makeIcon('#BA7517', FORK, 30),
  VISITED: makeIcon('#1D9E75', SMILEY, 30),
}

// green pins with a number, for the everyone map, made once per number and then reused
const countIcons = new Map<number, L.DivIcon>()

// a green pin showing how many people have visited the place
export function countIcon(count: number): L.DivIcon {
  const existing = countIcons.get(count)
  if (existing) {
    return existing
  }

  // big numbers would not fit inside the circle
  const label = count > 99 ? '99+' : String(count)
  const number = `
    <text x="12" y="16.5" text-anchor="middle" font-size="12" font-weight="bold"
          font-family="sans-serif" fill="white">${label}</text>
  `

  const icon = makeIcon('#1D9E75', number, 30)
  countIcons.set(count, icon)
  return icon
}


// pins with a rank number for top rated, made once per rank and then reused
const rankIcons = new Map<number, L.DivIcon>()

// gold and a bit bigger for the top 3, purple for the rest
export function rankIcon(rank: number): L.DivIcon {
  const existing = rankIcons.get(rank)
  if (existing) {
    return existing
  }

  const isTopThree = rank <= 3
  // dark text on gold is easier to read than white
  const textColor = isTopThree ? '#412402' : 'white'
  const number = `
    <text x="12" y="16.5" text-anchor="middle" font-size="12" font-weight="bold"
          font-family="sans-serif" fill="${textColor}">${rank}</text>
  `

  const icon = isTopThree ? makeIcon('#EF9F27', number, 34) : makeIcon('#534AB7', number, 30)
  rankIcons.set(rank, icon)
  return icon
}