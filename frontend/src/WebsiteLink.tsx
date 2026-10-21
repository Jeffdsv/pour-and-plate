// a link to a restaurants website, shown as just the domain like "ekstedt.nu"
// returns nothing if the place has no website
export default function WebsiteLink({ url }: { url: string | null }) {
  if (!url) {
    return null
  }

  // some sites in the data are saved without https in front
  const full = url.startsWith('http') ? url : `https://${url}`

  // only real web links, never something like javascript: that could run code
  let host: string
  try {
    const parsed = new URL(full)
    if (parsed.protocol !== 'https:' && parsed.protocol !== 'http:') {
      return null
    }
    host = parsed.hostname.replace(/^www\./, '')
  } catch {
    // not a valid address at all, skip it
    return null
  }

  return (
    <a className="website-link" href={full} target="_blank" rel="noopener noreferrer">
      🌐 {host}
    </a>
  )
}