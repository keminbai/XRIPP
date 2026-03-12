import { getToken } from '@/utils/request'

/**
 * Download Excel file from backend export endpoint.
 * Uses fetch with JWT auth, creates blob URL for download.
 */
export async function downloadExcel(apiPath: string, filename: string, params?: Record<string, string>) {
  const query = params
    ? '?' + Object.entries(params).filter(([, v]) => v).map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')
    : ''

  const res = await fetch(`/api${apiPath}${query}`, {
    headers: {
      Authorization: `Bearer ${getToken()}`
    }
  })

  if (!res.ok) {
    throw new Error(`Export failed: ${res.status}`)
  }

  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
