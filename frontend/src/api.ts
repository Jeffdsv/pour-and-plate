// reads one cookie by name, document.cookie looks like "a=1; XSRF-TOKEN=abc"
function readCookie(name: string): string | null {
  const cookie = document.cookie.split('; ').find((c) => c.startsWith(`${name}=`))
  return cookie ? decodeURIComponent(cookie.substring(name.length + 1)) : null
}

// spring puts a csrf token in a cookie, every request that changes something sends it back in a header
// an evil website cannot read my cookies, so it cannot send this header
export function csrfHeaders(): Record<string, string> {
  const token = readCookie('XSRF-TOKEN')
  return token ? { 'X-XSRF-TOKEN': token } : {}
}

// after logging out the csrf cookie is gone, any get request makes spring hand out a new one
export async function ensureCsrfToken(): Promise<void> {
  if (readCookie('XSRF-TOKEN') === null) {
    await fetch('/api/auth/me')
  }
}