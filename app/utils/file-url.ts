export function resolveFileUrl(url?: string): string {
  const raw = String(url || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  if (raw.startsWith('/api/')) return raw
  if (raw.startsWith('/uploads/')) return `/api${raw}`
  if (raw.startsWith('uploads/')) return `/api/${raw}`
  return raw.startsWith('/') ? raw : `/${raw}`
}
