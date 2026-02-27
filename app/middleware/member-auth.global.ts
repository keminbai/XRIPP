import { getToken, getLoginUser } from '~/utils/request'

export default defineNuxtRouteMiddleware((to) => {
  if (!to.path.startsWith('/member')) return

  const token = getToken()
  const user = getLoginUser()

  if (!token || !user) {
    return navigateTo(`/login?redirect=${encodeURIComponent(to.fullPath)}`, { replace: true })
  }

  const role = String(user.role || '').toLowerCase()

  if (role === 'member') return

  if (role === 'admin') {
    return navigateTo('/admin', { replace: true })
  }

  if (role === 'partner') {
    return navigateTo('/admin/partner-publish', { replace: true })
  }

  return navigateTo('/login', { replace: true })
})
