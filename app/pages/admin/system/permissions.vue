<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        系统权限中心已接入真实 RBAC API。登录模式、权限档案和账号绑定会真实持久化；当前管理员若刚绑定/解绑档案，需要重新登录以刷新 JWT 内的权限快照。
      </template>
    </el-alert>

    <el-alert
      v-if="currentSnapshot?.source === 'admin_fallback'"
      type="warning"
      :closable="false"
      show-icon
      title="当前账号仍处于历史 admin 兼容模式：未绑定权限档案时继续保留全量后台权限。若要验证子权限边界，请在下方绑定具体档案后重新登录。"
    />

    <div class="grid grid-cols-1 gap-6 md:grid-cols-4">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div class="mb-2 text-sm text-slate-500">{{ card.label }}</div>
        <div class="text-2xl font-bold text-slate-800">{{ card.value }}</div>
        <div class="mt-1 text-xs text-slate-400">{{ card.desc }}</div>
      </div>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">当前生效权限快照</h3>
          <p class="mt-1 text-xs text-slate-500">来自 `GET /v3/admin/permissions/current`，用于菜单显隐与后台路由校验</p>
        </div>
        <el-button :loading="pageLoading" @click="loadAll(true)">刷新快照</el-button>
      </div>

      <div class="grid grid-cols-1 gap-4 md:grid-cols-4">
        <div class="rounded-lg bg-slate-50 p-4">
          <div class="text-xs text-slate-500">来源</div>
          <div class="mt-2 text-sm font-bold text-slate-800">{{ sourceLabel }}</div>
        </div>
        <div class="rounded-lg bg-slate-50 p-4">
          <div class="text-xs text-slate-500">当前档案</div>
          <div class="mt-2 text-sm font-bold text-slate-800">{{ currentSnapshot?.profileName || '未绑定' }}</div>
        </div>
        <div class="rounded-lg bg-slate-50 p-4">
          <div class="text-xs text-slate-500">状态</div>
          <div class="mt-2">
            <el-tag :type="currentSnapshot?.active === false ? 'danger' : 'success'" size="small">
              {{ currentSnapshot?.active === false ? '停用' : '生效中' }}
            </el-tag>
          </div>
        </div>
        <div class="rounded-lg bg-slate-50 p-4">
          <div class="text-xs text-slate-500">模块授权数</div>
          <div class="mt-2 text-sm font-bold text-slate-800">{{ currentSnapshot?.modules?.length || 0 }}</div>
        </div>
      </div>

      <el-table :data="currentSnapshot?.modules || []" stripe class="mt-6">
        <el-table-column prop="moduleName" label="模块" min-width="180" />
        <el-table-column prop="moduleCode" label="代码" min-width="180" />
        <el-table-column label="查看" width="90" align="center">
          <template #default="scope"><el-tag size="small" :type="scope.row.canView ? 'success' : 'info'">{{ scope.row.canView ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="新增" width="90" align="center">
          <template #default="scope"><el-tag size="small" :type="scope.row.canCreate ? 'success' : 'info'">{{ scope.row.canCreate ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="编辑" width="90" align="center">
          <template #default="scope"><el-tag size="small" :type="scope.row.canEdit ? 'success' : 'info'">{{ scope.row.canEdit ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="审核" width="90" align="center">
          <template #default="scope"><el-tag size="small" :type="scope.row.canReview ? 'warning' : 'info'">{{ scope.row.canReview ? '是' : '否' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="导出" width="90" align="center">
          <template #default="scope"><el-tag size="small" :type="scope.row.canExport ? 'success' : 'info'">{{ scope.row.canExport ? '是' : '否' }}</el-tag></template>
        </el-table-column>
      </el-table>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">登录模式</h3>
          <p class="mt-1 text-xs text-slate-500">绑定 base role 与默认权限档案，替代旧版前端内存态“登录模式”</p>
        </div>
        <div class="flex gap-2">
          <el-button :loading="modesLoading" @click="loadLoginModes">刷新</el-button>
          <el-button type="primary" :loading="modesSaving" @click="saveLoginModes">保存模式</el-button>
        </div>
      </div>

      <el-table :data="loginModes" border stripe v-loading="modesLoading">
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="modeCode" label="代码" min-width="160" />
        <el-table-column label="基础角色" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.baseRole === 'partner' ? 'partner' : 'admin' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="启用" width="100" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" />
          </template>
        </el-table-column>
        <el-table-column label="默认档案" min-width="220">
          <template #default="scope">
            <el-select v-model="scope.row.defaultProfileId" class="w-full" clearable>
              <el-option
                v-for="profile in availableProfiles(scope.row.baseRole)"
                :key="profile.id"
                :label="profile.name"
                :value="profile.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" />
      </el-table>
    </div>

    <div class="grid grid-cols-1 gap-6 xl:grid-cols-[1.1fr_1fr]">
      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="mb-4 flex items-center justify-between gap-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">权限档案列表</h3>
            <p class="mt-1 text-xs text-slate-500">系统级后台子权限档案；`SUPER_ADMIN` 保留历史 admin 全量兼容能力</p>
          </div>
          <div class="flex gap-2">
            <el-button :loading="profilesLoading" @click="loadProfiles">刷新</el-button>
            <el-button type="primary" @click="createProfileDraft">新增档案</el-button>
          </div>
        </div>

        <el-table
          :data="profiles"
          border
          stripe
          highlight-current-row
          v-loading="profilesLoading"
          @current-change="selectProfile"
        >
          <el-table-column prop="name" label="档案名称" min-width="180" />
          <el-table-column prop="code" label="代码" min-width="160" />
          <el-table-column label="角色" width="110">
            <template #default="scope">
              <el-tag size="small">{{ scope.row.baseRole }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.status ? 'success' : 'info'">{{ scope.row.status ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="系统" width="90">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.isSystem ? 'warning' : 'info'">{{ scope.row.isSystem ? '内置' : '自定义' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="grantCount" label="授权数" width="90" />
        </el-table>
      </div>

      <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="mb-4 flex items-center justify-between gap-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">{{ profileForm.id ? '编辑权限档案' : '新增权限档案' }}</h3>
            <p class="mt-1 text-xs text-slate-500">保存后立即落库；若给当前账号改绑档案，需要重新登录才会切换当前会话权限</p>
          </div>
          <el-button type="primary" :loading="profileSaving" @click="saveProfile">保存档案</el-button>
        </div>

        <el-form :model="profileForm" label-width="100px">
          <el-form-item label="档案名称" required>
            <el-input v-model="profileForm.name" />
          </el-form-item>
          <el-form-item label="档案代码" required>
            <el-input v-model="profileForm.code" :disabled="profileForm.isSystem" placeholder="如 CONTENT_PUBLISHER" />
          </el-form-item>
          <el-form-item label="基础角色">
            <el-select v-model="profileForm.baseRole" class="w-full">
              <el-option label="admin" value="admin" />
              <el-option label="partner" value="partner" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="profileForm.status" />
          </el-form-item>
          <el-form-item label="超级管理">
            <el-switch v-model="profileForm.isSuperAdmin" :disabled="profileForm.isSystem" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="profileForm.description" type="textarea" :rows="3" />
          </el-form-item>
        </el-form>

        <div class="mt-6">
          <div class="mb-3 text-sm font-bold text-slate-700">模块授权</div>
          <el-table :data="profileForm.grants" size="small" border>
            <el-table-column prop="moduleName" label="模块" min-width="160" />
            <el-table-column label="查看" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canView" /></template>
            </el-table-column>
            <el-table-column label="新增" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canCreate" /></template>
            </el-table-column>
            <el-table-column label="编辑" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canEdit" /></template>
            </el-table-column>
            <el-table-column label="删除" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canDelete" /></template>
            </el-table-column>
            <el-table-column label="审核" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canReview" /></template>
            </el-table-column>
            <el-table-column label="导出" width="80" align="center">
              <template #default="scope"><el-switch v-model="scope.row.canExport" /></template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">账号权限绑定</h3>
          <p class="mt-1 text-xs text-slate-500">把 `sys_user.id` 绑定到指定权限档案。该绑定写入数据库，但当前登录会话要重新登录才能拿到新 JWT 权限档案。</p>
        </div>
      </div>

      <el-form :model="bindForm" label-width="120px" class="max-w-3xl">
        <el-form-item label="用户ID" required>
          <el-input-number v-model="bindForm.userId" class="!w-full" :min="1" :controls="false" />
        </el-form-item>
        <el-form-item label="权限档案">
          <el-select v-model="bindForm.profileId" class="w-full" clearable placeholder="留空表示解绑，回到历史 admin fallback">
            <el-option v-for="profile in profiles" :key="profile.id" :label="`${profile.name} (${profile.code})`" :value="profile.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="bindingSaving" @click="saveBinding">保存绑定</el-button>
          <el-button @click="resetBindingForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'
import {
  ADMIN_PERMISSION_MODULES,
  useAdminPermissionSnapshot,
  type AdminPermissionGrant,
  type AdminPermissionSnapshot
} from '@/composables/useAdminPermissionSnapshot'

definePageMeta({ layout: 'admin' })

type LoginModeItem = {
  id: number
  modeCode: string
  name: string
  baseRole: 'admin' | 'partner'
  enabled: boolean
  defaultProfileId: number | null
  description: string
}

type PermissionProfileItem = {
  id: number
  code: string
  name: string
  baseRole: 'admin' | 'partner'
  status: boolean
  isSystem: boolean
  isSuperAdmin: boolean
  description: string
  grantCount: number
  grants: AdminPermissionGrant[]
}

type PermissionProfileForm = {
  id: number | null
  code: string
  name: string
  baseRole: 'admin' | 'partner'
  status: boolean
  isSystem: boolean
  isSuperAdmin: boolean
  description: string
  grants: AdminPermissionGrant[]
}

const { snapshot, refreshSnapshot } = useAdminPermissionSnapshot()

const pageLoading = ref(false)
const modesLoading = ref(false)
const modesSaving = ref(false)
const profilesLoading = ref(false)
const profileSaving = ref(false)
const bindingSaving = ref(false)

const loginModes = ref<LoginModeItem[]>([])
const profiles = ref<PermissionProfileItem[]>([])

const bindForm = ref({
  userId: null as number | null,
  profileId: null as number | null
})

const createEmptyGrantList = (): AdminPermissionGrant[] => ADMIN_PERMISSION_MODULES.map((item) => ({
  moduleCode: item.code,
  moduleName: item.name,
  canView: false,
  canCreate: false,
  canEdit: false,
  canDelete: false,
  canReview: false,
  canExport: false
}))

const createEmptyProfileForm = (): PermissionProfileForm => ({
  id: null,
  code: '',
  name: '',
  baseRole: 'admin',
  status: true,
  isSystem: false,
  isSuperAdmin: false,
  description: '',
  grants: createEmptyGrantList()
})

const profileForm = ref<PermissionProfileForm>(createEmptyProfileForm())

const currentSnapshot = computed<AdminPermissionSnapshot | null>(() => snapshot.value)

const summaryCards = computed(() => {
  const current = currentSnapshot.value
  const totalGrantCount = profiles.value.reduce((sum, item) => sum + item.grants.length, 0)
  return [
    {
      label: '当前档案',
      value: current?.profileName || '未绑定',
      desc: current?.isSuperAdmin ? '当前会话为 super admin' : '当前 JWT 中生效的权限档案'
    },
    {
      label: '登录模式数',
      value: String(loginModes.value.length),
      desc: '数据库中的真实登录模式定义'
    },
    {
      label: '权限档案数',
      value: String(profiles.value.length),
      desc: '系统内置 + 自定义档案总量'
    },
    {
      label: '授权明细数',
      value: String(totalGrantCount),
      desc: '所有档案累计的模块授权行数'
    }
  ]
})

const sourceLabel = computed(() => {
  const source = currentSnapshot.value?.source || ''
  if (source === 'profile') return 'profile 档案生效'
  if (source === 'admin_fallback') return '历史 admin fallback'
  if (source === 'missing_profile') return '档案丢失'
  if (source === 'role_baseline') return '基础角色'
  return '未知'
})

const normalizeGrantList = (items: any[]): AdminPermissionGrant[] => {
  const map = new Map<string, AdminPermissionGrant>()
  for (const item of Array.isArray(items) ? items : []) {
    const code = String(item?.moduleCode || item?.module_code || '').trim()
    if (!code) continue
    map.set(code, {
      moduleCode: code,
      moduleName: String(item?.moduleName || item?.module_name || code).trim(),
      canView: Boolean(item?.canView),
      canCreate: Boolean(item?.canCreate),
      canEdit: Boolean(item?.canEdit),
      canDelete: Boolean(item?.canDelete),
      canReview: Boolean(item?.canReview),
      canExport: Boolean(item?.canExport)
    })
  }
  const knownItems = ADMIN_PERMISSION_MODULES.map((item) => {
    const current = map.get(item.code)
    return current || {
      moduleCode: item.code,
      moduleName: item.name,
      canView: false,
      canCreate: false,
      canEdit: false,
      canDelete: false,
      canReview: false,
      canExport: false
    }
  })
  const extraItems = Array.from(map.values()).filter(
    (item) => !ADMIN_PERMISSION_MODULES.some((module) => module.code === item.moduleCode)
  )
  return [...knownItems, ...extraItems]
}

const normalizeLoginModes = (items: any[]): LoginModeItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    modeCode: String(item?.modeCode || ''),
    name: String(item?.name || ''),
    baseRole: item?.baseRole === 'partner' ? 'partner' : 'admin',
    enabled: item?.enabled !== false,
    defaultProfileId: item?.defaultProfileId == null ? null : Number(item.defaultProfileId),
    description: String(item?.description || '')
  }))
}

const normalizeProfiles = (items: any[]): PermissionProfileItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    code: String(item?.code || ''),
    name: String(item?.name || ''),
    baseRole: item?.baseRole === 'partner' ? 'partner' : 'admin',
    status: item?.status !== false,
    isSystem: Boolean(item?.isSystem),
    isSuperAdmin: Boolean(item?.isSuperAdmin),
    description: String(item?.description || ''),
    grantCount: Number(item?.grantCount || 0),
    grants: normalizeGrantList(Array.isArray(item?.grants) ? item.grants : [])
  }))
}

const availableProfiles = (baseRole: 'admin' | 'partner') => {
  return profiles.value.filter((item) => item.baseRole === baseRole && item.status)
}

const selectProfile = (row?: PermissionProfileItem | null) => {
  if (!row) return
  profileForm.value = {
    id: row.id,
    code: row.code,
    name: row.name,
    baseRole: row.baseRole,
    status: row.status,
    isSystem: row.isSystem,
    isSuperAdmin: row.isSuperAdmin,
    description: row.description,
    grants: row.grants.map((grant) => ({ ...grant }))
  }
}

const createProfileDraft = () => {
  profileForm.value = createEmptyProfileForm()
}

const loadLoginModes = async () => {
  modesLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/permissions/login-modes')
    loginModes.value = normalizeLoginModes(Array.isArray(res.data) ? res.data : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '登录模式加载失败')
  } finally {
    modesLoading.value = false
  }
}

const loadProfiles = async () => {
  profilesLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/permissions/profiles')
    profiles.value = normalizeProfiles(Array.isArray(res.data) ? res.data : [])
    if (profileForm.value.id) {
      const current = profiles.value.find((item) => item.id === profileForm.value.id)
      if (current) {
        selectProfile(current)
      }
    } else if (!profiles.value.length) {
      createProfileDraft()
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '权限档案加载失败')
  } finally {
    profilesLoading.value = false
  }
}

const saveLoginModes = async () => {
  modesSaving.value = true
  try {
    await apiRequest('/v3/admin/permissions/login-modes', {
      method: 'POST',
      body: {
        items: loginModes.value.map((item) => ({
          mode_code: item.modeCode,
          name: item.name,
          base_role: item.baseRole,
          enabled: item.enabled,
          default_profile_id: item.defaultProfileId,
          description: item.description
        }))
      }
    })
    ElMessage.success('登录模式已保存')
    await loadLoginModes()
  } catch (error: any) {
    ElMessage.error(error?.message || '登录模式保存失败')
  } finally {
    modesSaving.value = false
  }
}

const saveProfile = async () => {
  const body = {
    code: profileForm.value.code.trim().toUpperCase().replace(/-/g, '_'),
    name: profileForm.value.name.trim(),
    base_role: profileForm.value.baseRole,
    status: profileForm.value.status,
    is_super_admin: profileForm.value.isSuperAdmin,
    description: profileForm.value.description.trim(),
    grants: profileForm.value.grants.map((item) => ({
      module_code: item.moduleCode,
      module_name: item.moduleName,
      can_view: item.canView,
      can_create: item.canCreate,
      can_edit: item.canEdit,
      can_delete: item.canDelete,
      can_review: item.canReview,
      can_export: item.canExport
    }))
  }

  if (!body.name || !body.code) {
    ElMessage.warning('请填写档案名称和代码')
    return
  }

  profileSaving.value = true
  try {
    if (profileForm.value.id) {
      await apiRequest(`/v3/admin/permissions/profiles/${profileForm.value.id}`, {
        method: 'PUT',
        body
      })
    } else {
      await apiRequest('/v3/admin/permissions/profiles', {
        method: 'POST',
        body
      })
    }
    ElMessage.success('权限档案已保存')
    await loadProfiles()
    await loadLoginModes()
    await loadAll(true)
  } catch (error: any) {
    ElMessage.error(error?.message || '权限档案保存失败')
  } finally {
    profileSaving.value = false
  }
}

const saveBinding = async () => {
  if (!bindForm.value.userId) {
    ElMessage.warning('请填写用户ID')
    return
  }
  bindingSaving.value = true
  try {
    await apiRequest(`/v3/admin/permissions/users/${bindForm.value.userId}/profile`, {
      method: 'POST',
      body: {
        permission_profile_id: bindForm.value.profileId
      }
    })
    ElMessage.success('账号权限绑定已保存，请使用该账号重新登录以刷新当前权限快照')
  } catch (error: any) {
    ElMessage.error(error?.message || '账号权限绑定保存失败')
  } finally {
    bindingSaving.value = false
  }
}

const resetBindingForm = () => {
  bindForm.value = {
    userId: null,
    profileId: null
  }
}

const loadAll = async (force = false) => {
  pageLoading.value = true
  try {
    await Promise.all([
      refreshSnapshot(force),
      loadLoginModes(),
      loadProfiles()
    ])
  } finally {
    pageLoading.value = false
  }
}

onMounted(async () => {
  await loadAll()
})
</script>
