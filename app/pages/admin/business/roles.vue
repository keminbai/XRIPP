<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        业务角色配置已接入真实 API，保存后会持久化到后台配置中心。
      </template>
    </el-alert>

    <el-alert
      title="此处配置业务角色的操作权限与数据范围，系统管理员权限请前往「系统综管-权限管理」"
      type="info"
      :closable="false"
      show-icon
    />

    <div class="grid grid-cols-1 gap-6 md:grid-cols-3">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div class="mb-2 text-sm text-slate-500">{{ stat.label }}</div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <div v-loading="loading" class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-slate-800">业务角色权限配置</h3>
          <p class="mt-1 text-xs text-slate-500">配置合伙人、审核员等业务角色的操作边界与数据可见范围</p>
        </div>
        <div class="flex gap-2">
          <el-button :loading="loading" @click="loadRoleConfigs">刷新配置</el-button>
          <el-button type="primary" :loading="saving" @click="handleBatchSave">
            <el-icon class="mr-1"><Check /></el-icon>
            批量保存所有配置
          </el-button>
        </div>
      </div>

      <el-table :data="roleList" border stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
        <el-table-column prop="roleName" label="业务角色" width="180">
          <template #default="scope">
            <div class="font-bold text-slate-800">{{ scope.row.roleName }}</div>
            <div class="mt-1 text-xs text-slate-400">
              <el-tag size="small" type="info">{{ scope.row.code }}</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="数据可见范围" width="200">
          <template #default="scope">
            <el-select v-model="scope.row.dataScope" size="small" class="w-full">
              <el-option label="仅本人数据" value="self" />
              <el-option label="本城市数据" value="city" />
              <el-option label="全平台数据" value="all" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="核心业务权限" min-width="450">
          <template #default="scope">
            <div class="grid grid-cols-2 gap-3">
              <el-checkbox v-model="scope.row.permissions.viewContact">
                <span class="text-sm">查看客户手机号</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.exportData">
                <span class="text-sm">导出业务数据</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.directPublish">
                <span class="text-sm">免审核直接发布</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.assignTask">
                <span class="text-sm">分配工单任务</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.editPrice">
                <span class="text-sm">修改报价</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.approveRefund">
                <span class="text-sm">审批退款申请</span>
              </el-checkbox>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :loading="saving" @click="handleSave(scope.row)">
              保存配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-loading="loading" class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h4 class="mb-4 font-bold text-slate-700">最近权限变更记录</h4>
      <el-table :data="changeLog" size="small" :show-header="false">
        <el-table-column prop="time" width="180" />
        <el-table-column prop="admin" width="120" />
        <el-table-column prop="action" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'

definePageMeta({ layout: 'admin' })

type DataScope = 'self' | 'city' | 'all'

interface RolePermissions {
  viewContact: boolean
  exportData: boolean
  directPublish: boolean
  assignTask: boolean
  editPrice: boolean
  approveRefund: boolean
}

interface RoleItem {
  id: number
  roleName: string
  code: string
  dataScope: DataScope
  enabled: boolean
  permissions: RolePermissions
}

interface ChangeLogItem {
  time: string
  admin: string
  action: string
}

const NAMESPACE = 'business_roles'
const CONFIG_META = {
  role_list: { name: '业务角色权限配置', sortOrder: 10 },
  change_log: { name: '业务角色变更记录', sortOrder: 20 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

const createDefaultRoles = (): RoleItem[] => ([
  {
    id: 1,
    roleName: '城市合伙人',
    code: 'PARTNER',
    dataScope: 'city',
    enabled: true,
    permissions: {
      viewContact: true,
      exportData: false,
      directPublish: false,
      assignTask: false,
      editPrice: false,
      approveRefund: false
    }
  },
  {
    id: 2,
    roleName: '发布管理员',
    code: 'PUBLISHER',
    dataScope: 'all',
    enabled: true,
    permissions: {
      viewContact: false,
      exportData: true,
      directPublish: true,
      assignTask: false,
      editPrice: false,
      approveRefund: false
    }
  },
  {
    id: 3,
    roleName: '审核管理员',
    code: 'AUDITOR',
    dataScope: 'all',
    enabled: true,
    permissions: {
      viewContact: true,
      exportData: false,
      directPublish: true,
      assignTask: true,
      editPrice: false,
      approveRefund: true
    }
  },
  {
    id: 4,
    roleName: '财务专员',
    code: 'FINANCE',
    dataScope: 'all',
    enabled: true,
    permissions: {
      viewContact: true,
      exportData: true,
      directPublish: false,
      assignTask: false,
      editPrice: true,
      approveRefund: true
    }
  }
])

const createDefaultChangeLog = (): ChangeLogItem[] => ([
  { time: '2026-02-01 14:30', admin: '系统', action: '修改了「城市合伙人」的数据可见范围' },
  { time: '2026-01-30 10:15', admin: '系统', action: '启用了「财务专员」角色' },
  { time: '2026-01-28 16:20', admin: '系统', action: '新增了「免审核直接发布」权限给审核管理员' }
])

const roleList = ref<RoleItem[]>(createDefaultRoles())
const changeLog = ref<ChangeLogItem[]>(createDefaultChangeLog())

const normalizePermissions = (value: any): RolePermissions => ({
  viewContact: Boolean(value?.viewContact),
  exportData: Boolean(value?.exportData),
  directPublish: Boolean(value?.directPublish),
  assignTask: Boolean(value?.assignTask),
  editPrice: Boolean(value?.editPrice),
  approveRefund: Boolean(value?.approveRefund)
})

const normalizeRoles = (value: unknown): RoleItem[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return createDefaultRoles()
  }
  return value.map((item: any, index) => ({
    id: Number(item?.id || index + 1),
    roleName: item?.roleName || `角色${index + 1}`,
    code: item?.code || `ROLE_${index + 1}`,
    dataScope: ['self', 'city', 'all'].includes(item?.dataScope) ? item.dataScope : 'self',
    enabled: Boolean(item?.enabled),
    permissions: normalizePermissions(item?.permissions)
  }))
}

const normalizeChangeLog = (value: unknown): ChangeLogItem[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return createDefaultChangeLog()
  }
  return value.map((item: any) => ({
    time: item?.time || '',
    admin: item?.admin || '系统',
    action: item?.action || ''
  }))
}

const stats = computed(() => {
  const enabledCount = roleList.value.filter(item => item.enabled).length
  const allScopeCount = roleList.value.filter(item => item.dataScope === 'all').length
  const permissionGrantCount = roleList.value.reduce((sum, item) => {
    return sum + Object.values(item.permissions).filter(Boolean).length
  }, 0)
  return [
    { label: '启用角色数', value: String(enabledCount) },
    { label: '全平台权限角色', value: String(allScopeCount) },
    { label: '已授予权限项', value: String(permissionGrantCount) }
  ]
})

const nowLabel = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

const appendChangeLog = (action: string) => {
  changeLog.value = [
    {
      time: nowLabel(),
      admin: '当前管理员',
      action
    },
    ...changeLog.value
  ].slice(0, 20)
}

const persistRoleConfigs = async (successMessage: string) => {
  try {
    await saveNamespaceItems([
      {
        key: 'role_list',
        value: roleList.value
      },
      {
        key: 'change_log',
        value: changeLog.value
      }
    ])
    ElMessage.success(successMessage)
    await loadRoleConfigs()
  } catch (error: any) {
    ElMessage.error(error?.message || '业务角色配置保存失败')
  }
}

const loadRoleConfigs = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    roleList.value = normalizeRoles(itemMap.role_list)
    changeLog.value = normalizeChangeLog(itemMap.change_log)
  } catch (error: any) {
    ElMessage.error(error?.message || '业务角色配置加载失败')
  }
}

const handleSave = async (row: RoleItem) => {
  appendChangeLog(`保存了「${row.roleName}」角色配置`)
  await persistRoleConfigs(`${row.roleName} 权限配置已更新`)
}

const handleBatchSave = async () => {
  appendChangeLog('批量保存了全部业务角色配置')
  await persistRoleConfigs('所有角色权限配置已批量保存')
}

onMounted(async () => {
  await loadRoleConfigs()
})
</script>
