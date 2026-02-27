import { getToken, getLoginUser } from '~/utils/request'

const ADMIN_ONLY_PREFIXES = [
  '/admin/audit',
  '/admin/content',
  '/admin/tenders',
  '/admin/overseas',
  '/admin/dashboard',
  '/admin/business',
  '/admin/finance',
  '/admin/system',
  '/admin/members/un-audit',
  '/admin/suppliers/audit',
  '/admin/partners/list'
]

function readRoleFromToken(token: string): string {
  try {
    const parts = token.split('.')
    const payloadPart = parts[1]
    if (!payloadPart) return ''
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join('')
    )
    const payload = JSON.parse(json)
    return String(payload?.role || '').toLowerCase()
  } catch {
    return ''
  }
}

export default defineNuxtRouteMiddleware((to) => {
  if (!to.path.startsWith('/admin')) return

  const token = getToken()
  if (!token) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`, { replace: true })
  }

  const user = getLoginUser()
  const role = String(user?.role || '').toLowerCase() || readRoleFromToken(token)

  if (!role) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`, { replace: true })
  }

  if (role === 'member') {
    return navigateTo('/member', { replace: true })
  }

  if (role === 'partner') {
    const isAdminOnly = ADMIN_ONLY_PREFIXES.some((p) => to.path.startsWith(p))
    if (isAdminOnly || to.path === '/admin') {
      return navigateTo('/admin/partner-publish', { replace: true })
    }
  }

  // role === 'admin' -> pass
})
