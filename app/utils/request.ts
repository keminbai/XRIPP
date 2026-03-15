// app/utils/request.ts
type V3Response<T> = {
  code: string
  message: string
  data: T
  request_id: string
  timestamp: string
}

export type LoginUser = {
  id: number
  username: string
  role: 'admin' | 'partner' | 'member' | string
  partner_id?: number | null
  permission_profile_id?: number | null
  member_level?: 'NORMAL' | 'VIP' | 'SVIP'
  status?: number
}

type LoginData = {
  token: string
  token_type: string
  expires_in: number
  user: LoginUser
}

const TOKEN_KEY = 'xripp_token'
const USER_KEY = 'xripp_user'

const COOKIE_OPTIONS = {
  sameSite: 'lax' as const,
  path: '/'
}

function safeParseUser(raw: string | null): LoginUser | null {
  if (!raw) return null
  try {
    return JSON.parse(raw) as LoginUser
  } catch {
    try {
      return JSON.parse(decodeURIComponent(raw)) as LoginUser
    } catch {
      return null
    }
  }
}

function decodeBase64Url(value: string): string {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/')
  const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')

  if (import.meta.client) {
    return atob(normalized)
  }

  return Buffer.from(normalized, 'base64').toString('utf-8')
}

function parseUserFromToken(token: string): LoginUser | null {
  if (!token) return null
  try {
    const payloadPart = token.split('.')[1]
    if (!payloadPart) return null
    const payload = JSON.parse(decodeBase64Url(payloadPart))

    const id = Number(payload?.userId)
    if (!Number.isFinite(id) || id <= 0) return null

    return {
      id,
      username: String(payload?.username || ''),
      role: String(payload?.role || ''),
      partner_id: payload?.partnerId == null ? null : Number(payload.partnerId),
      permission_profile_id: payload?.permissionProfileId == null ? null : Number(payload.permissionProfileId)
    }
  } catch {
    return null
  }
}

function redirectToLogin() {
  if (!import.meta.client) return
  const path = window.location.pathname + window.location.search
  window.location.href = `/login?redirect=${encodeURIComponent(path)}`
}

function makeBizError(code: string, message: string) {
  const err = new Error(message || 'request failed') as Error & { code?: string }
  err.code = code
  return err
}

// 内存态：解决“刚写cookie就被middleware读取”时序问题
const tokenState = () => useState<string | null>('auth_token', () => null)
const userState = () => useState<LoginUser | null>('auth_user', () => null)

export function getToken(): string {
  const s = tokenState()
  if (s.value) return s.value
  const c = useCookie<string | null>(TOKEN_KEY, COOKIE_OPTIONS)
  const token = c.value || ''
  if (token) s.value = token
  return token
}

export function getLoginUser(): LoginUser | null {
  const s = userState()
  if (s.value) return s.value
  const c = useCookie<LoginUser | string | null>(USER_KEY, COOKIE_OPTIONS)
  const raw = c.value
  const user = typeof raw === 'string'
    ? safeParseUser(raw)
    : (raw && typeof raw === 'object' ? raw as LoginUser : null)
  if (user) s.value = user
  if (user) return user

  const tokenUser = parseUserFromToken(getToken())
  if (tokenUser) {
    s.value = tokenUser
    return tokenUser
  }

  return null
}

export function setAuth(token: string, user: LoginUser) {
  tokenState().value = token
  userState().value = user

  const tokenCookie = useCookie<string | null>(TOKEN_KEY, COOKIE_OPTIONS)
  const userCookie = useCookie<LoginUser | string | null>(USER_KEY, COOKIE_OPTIONS)
  tokenCookie.value = token
  userCookie.value = user
}

export function clearAuth() {
  tokenState().value = null
  userState().value = null

  const tokenCookie = useCookie<string | null>(TOKEN_KEY, COOKIE_OPTIONS)
  const userCookie = useCookie<LoginUser | string | null>(USER_KEY, COOKIE_OPTIONS)
  tokenCookie.value = null
  userCookie.value = null
}

export const apiFetchRaw = $fetch.create({
  baseURL: '/api',
  onRequest({ options }) {
    const token = getToken()
    options.headers = {
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    }
  },
  onResponseError({ response }) {
    if (response?.status === 401) {
      clearAuth()
      redirectToLogin()
    }
  }
})

export const apiFetch = apiFetchRaw

export async function apiRequest<T>(
  url: string,
  options?: Parameters<typeof apiFetchRaw>[1]
): Promise<V3Response<T>> {
  const res = await apiFetchRaw<V3Response<T>>(url, options)

  if (!res) throw makeBizError('INTERNAL_ERROR', 'empty response')

  if (res.code !== 'OK') {
    if (res.code === 'AUTH_UNAUTHORIZED') {
      clearAuth()
      redirectToLogin()
    }
    throw makeBizError(res.code, res.message || 'request failed')
  }

  return res
}

export async function loginByPassword(username: string, password: string) {
  const res = await apiRequest<LoginData>('/v3/auth/login', {
    method: 'POST',
    body: { username, password }
  })

  if (!res.data?.token || !res.data?.user) {
    throw makeBizError('AUTH_UNAUTHORIZED', '登录响应无 token/user')
  }

  setAuth(res.data.token, res.data.user)
  return res
}
