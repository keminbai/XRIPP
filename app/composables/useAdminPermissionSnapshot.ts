import { computed } from 'vue'
import { apiRequest, getLoginUser } from '@/utils/request'

export type AdminPermissionGrant = {
  moduleCode: string
  moduleName: string
  canView: boolean
  canCreate: boolean
  canEdit: boolean
  canDelete: boolean
  canReview: boolean
  canExport: boolean
}

export type AdminPermissionSnapshot = {
  userId: number | null
  role: string
  partnerId: number | null
  permissionProfileId: number | null
  profileCode: string
  profileName: string
  baseRole: string
  description: string
  active: boolean
  isSuperAdmin: boolean
  source: string
  modules: AdminPermissionGrant[]
}

export type AdminPermissionModuleCatalogItem = {
  code: string
  name: string
  description: string
}

type RoutePermissionRule = {
  prefix: string
  moduleCode: string
}

export const ADMIN_PERMISSION_MODULES: AdminPermissionModuleCatalogItem[] = [
  { code: 'audit_center', name: '业务审核中心', description: '查看审核待办并执行审核动作' },
  { code: 'content_management', name: '信息发布管理', description: '活动、培训、商机、媒体内容管理与审核前处理' },
  { code: 'overseas_management', name: '出海发布系统', description: '海外网点、服务信息、行业报告与分析页管理' },
  { code: 'activity_display', name: '广告与显示', description: '活动显示申请与轮播广告管理' },
  { code: 'supplier_onboarding_review', name: '服务商入驻审核', description: '入驻详情复查、附件核对与状态流转' },
  { code: 'member_management', name: '会员管理', description: '会员统计、列表查看与基础资料管理' },
  { code: 'member_orders', name: '会员订单管理', description: '订单列表、订单状态流转与相关财务查看' },
  { code: 'system_permissions', name: '系统权限中心', description: '登录模式、权限档案与账号绑定管理' },
  { code: 'system_notifications', name: '通知中心', description: '通知类型、模板与发送记录管理' },
  { code: 'system_customer_service', name: '客服系统', description: '留言记录、工单处理与附件查看' },
  { code: 'system_logs', name: '操作日志', description: '聚合审计日志与状态流转日志查询' },
  { code: 'system_config_center', name: '系统配置中心', description: '系统设置、登录配置、证书模板等配置页' }
]

const ROUTE_PERMISSION_RULES: RoutePermissionRule[] = [
  { prefix: '/admin/system/permissions', moduleCode: 'system_permissions' },
  { prefix: '/admin/system/notifications', moduleCode: 'system_notifications' },
  { prefix: '/admin/system/customer-service', moduleCode: 'system_customer_service' },
  { prefix: '/admin/system/logs', moduleCode: 'system_logs' },
  { prefix: '/admin/system/settings', moduleCode: 'system_config_center' },
  { prefix: '/admin/system/login-config', moduleCode: 'system_config_center' },
  { prefix: '/admin/system/certificates', moduleCode: 'system_config_center' },
  { prefix: '/admin/content/display', moduleCode: 'activity_display' },
  { prefix: '/admin/content', moduleCode: 'content_management' },
  { prefix: '/admin/overseas', moduleCode: 'overseas_management' },
  { prefix: '/admin/audit', moduleCode: 'audit_center' },
  { prefix: '/admin/suppliers/audit', moduleCode: 'supplier_onboarding_review' },
  { prefix: '/admin/members/orders', moduleCode: 'member_orders' },
  { prefix: '/admin/members/analysis', moduleCode: 'member_management' },
  { prefix: '/admin/members/list', moduleCode: 'member_management' }
]

const ADMIN_FALLBACK_CANDIDATES = [
  '/admin/content/activities',
  '/admin/overseas/analysis',
  '/admin/audit',
  '/admin/suppliers/audit',
  '/admin/members/list',
  '/admin/members/orders',
  '/admin/system/notifications',
  '/admin/system/customer-service',
  '/admin/system/logs',
  '/admin/system/settings',
  '/admin/system/permissions'
]

const normalizeGrant = (item: any): AdminPermissionGrant => ({
  moduleCode: String(item?.moduleCode || item?.module_code || '').trim(),
  moduleName: String(item?.moduleName || item?.module_name || '').trim(),
  canView: Boolean(item?.canView),
  canCreate: Boolean(item?.canCreate),
  canEdit: Boolean(item?.canEdit),
  canDelete: Boolean(item?.canDelete),
  canReview: Boolean(item?.canReview),
  canExport: Boolean(item?.canExport)
})

const normalizeSnapshot = (raw: any): AdminPermissionSnapshot => ({
  userId: raw?.userId == null ? null : Number(raw.userId),
  role: String(raw?.role || ''),
  partnerId: raw?.partnerId == null ? null : Number(raw.partnerId),
  permissionProfileId: raw?.permissionProfileId == null ? null : Number(raw.permissionProfileId),
  profileCode: String(raw?.profileCode || ''),
  profileName: String(raw?.profileName || ''),
  baseRole: String(raw?.baseRole || ''),
  description: String(raw?.description || ''),
  active: raw?.active !== false,
  isSuperAdmin: Boolean(raw?.isSuperAdmin),
  source: String(raw?.source || ''),
  modules: Array.isArray(raw?.modules) ? raw.modules.map(normalizeGrant) : []
})

const findRouteRule = (path: string) => ROUTE_PERMISSION_RULES.find((item) => path.startsWith(item.prefix)) || null

const hasGrantAnyCapability = (grant: AdminPermissionGrant | undefined) => {
  if (!grant) return false
  return [
    grant.canView,
    grant.canCreate,
    grant.canEdit,
    grant.canDelete,
    grant.canReview,
    grant.canExport
  ].some(Boolean)
}

export const useAdminPermissionSnapshot = () => {
  const snapshot = useState<AdminPermissionSnapshot | null>('admin_permission_snapshot', () => null)
  const loading = useState<boolean>('admin_permission_snapshot_loading', () => false)
  const loadedUserId = useState<number | null>('admin_permission_snapshot_user_id', () => null)

  const moduleGrantMap = computed(() => {
    const map = new Map<string, AdminPermissionGrant>()
    for (const item of snapshot.value?.modules || []) {
      if (item.moduleCode) {
        map.set(item.moduleCode, item)
      }
    }
    return map
  })

  const clearSnapshot = () => {
    snapshot.value = null
    loadedUserId.value = null
  }

  const refreshSnapshot = async (force = false) => {
    const user = getLoginUser()
    if (!user || String(user.role).toLowerCase() !== 'admin') {
      clearSnapshot()
      return null
    }

    if (!force && snapshot.value && loadedUserId.value === user.id) {
      return snapshot.value
    }

    loading.value = true
    try {
      const res = await apiRequest<any>('/v3/admin/permissions/current')
      snapshot.value = normalizeSnapshot(res.data || {})
      loadedUserId.value = user.id
      return snapshot.value
    } finally {
      loading.value = false
    }
  }

  const hasModuleAccess = (moduleCode: string, targetSnapshot = snapshot.value) => {
    if (!targetSnapshot) return false
    if (!targetSnapshot.active) return false
    if (targetSnapshot.isSuperAdmin) return true
    return hasGrantAnyCapability(targetSnapshot.modules.find((item) => item.moduleCode === moduleCode))
  }

  const canAccessAdminRoute = (path: string, targetSnapshot = snapshot.value) => {
    if (!path.startsWith('/admin')) return true
    const user = getLoginUser()
    const role = String(user?.role || '').toLowerCase()

    if (role === 'partner') {
      return !(
        path === '/admin'
        || path.startsWith('/admin/audit')
        || path.startsWith('/admin/content')
        || path.startsWith('/admin/tenders')
        || path.startsWith('/admin/overseas')
        || path.startsWith('/admin/dashboard')
        || path.startsWith('/admin/business')
        || path.startsWith('/admin/finance')
        || path.startsWith('/admin/system')
        || path.startsWith('/admin/members/un-audit')
        || path.startsWith('/admin/suppliers/audit')
        || path.startsWith('/admin/partners/list')
      )
    }

    if (role !== 'admin') {
      return path === '/admin'
    }

    if (path === '/admin') {
      return Boolean(targetSnapshot?.isSuperAdmin)
    }

    const rule = findRouteRule(path)
    if (!rule) {
      return Boolean(targetSnapshot?.isSuperAdmin)
    }
    return hasModuleAccess(rule.moduleCode, targetSnapshot)
  }

  const resolveAdminFallbackRoute = (targetSnapshot = snapshot.value) => {
    const user = getLoginUser()
    const role = String(user?.role || '').toLowerCase()
    if (role === 'partner') {
      return '/admin/partner-publish'
    }
    if (role !== 'admin') {
      return '/login'
    }
    if (targetSnapshot?.isSuperAdmin) {
      return '/admin'
    }
    for (const candidate of ADMIN_FALLBACK_CANDIDATES) {
      if (canAccessAdminRoute(candidate, targetSnapshot)) {
        return candidate
      }
    }
    return '/login'
  }

  return {
    snapshot,
    loading,
    moduleGrantMap,
    refreshSnapshot,
    clearSnapshot,
    hasModuleAccess,
    canAccessAdminRoute,
    resolveAdminFallbackRoute
  }
}
