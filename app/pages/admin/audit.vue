<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\audit.vue
  版本: V1.7 (2026-02-27 收敛版)

  说明:
  1. AUDIT_1 / AUDIT_2 仅作为前端视图模式，不代表系统真实权限。
  2. 真实权限始终由后端鉴权与 SecurityContextHolder 决定。
  ========================================================================
-->
<template>
  <div class="audit-container space-y-6">
    <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm space-y-3">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2">
          <el-icon class="text-blue-600"><UserFilled /></el-icon>
          <span class="text-sm font-bold text-slate-700">审核视图模式：</span>
          <el-radio-group v-model="auditViewMode" size="small">
            <el-radio-button label="AUDIT_1">一审视图</el-radio-button>
            <el-radio-button label="AUDIT_2">二审视图</el-radio-button>
          </el-radio-group>
        </div>
        <div class="text-xs text-slate-400">
          {{ auditViewMode === 'AUDIT_1' ? '展示初审队列' : '展示终审队列' }}
        </div>
      </div>

      <el-alert
        title="提示：本页面的一审/二审仅为前端视图切换，不是权限切换；真实权限由后端鉴权决定。"
        type="info"
        :closable="false"
      />
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-sm text-slate-500 mb-1">待审核总数</div>
          <div class="text-2xl font-bold text-red-600">{{ pendingCount }}</div>
        </div>
        <div class="p-3 rounded-lg bg-red-50">
          <el-icon class="text-xl text-red-500"><Bell /></el-icon>
        </div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-sm text-slate-500 mb-1">服务商待办</div>
          <div class="text-2xl font-bold text-slate-800">{{ pendingStats.supplier }}</div>
        </div>
        <div class="p-3 rounded-lg bg-orange-50">
          <el-icon class="text-xl text-orange-500"><OfficeBuilding /></el-icon>
        </div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-sm text-slate-500 mb-1">内容待办</div>
          <div class="text-2xl font-bold text-slate-800">{{ pendingStats.content }}</div>
        </div>
        <div class="p-3 rounded-lg bg-blue-50">
          <el-icon class="text-xl text-blue-500"><Promotion /></el-icon>
        </div>
      </div>

      <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-sm text-slate-500 mb-1">采购需求待办</div>
          <div class="text-2xl font-bold text-slate-800">{{ pendingStats.demand }}</div>
        </div>
        <div class="p-3 rounded-lg bg-purple-50">
          <el-icon class="text-xl text-purple-500"><Document /></el-icon>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <el-tabs v-model="activeTab" class="px-6 pt-2">
        <el-tab-pane name="supplier">
          <template #label>
            <span class="flex items-center gap-2">
              <el-icon><OfficeBuilding /></el-icon> 服务商入驻
              <el-badge :value="pendingStats.supplier" :hidden="pendingStats.supplier === 0" type="danger" />
            </span>
          </template>

          <SupplierAuditList
            :role="auditViewMode"
            :items="supplierAuditItems"
            :loading="loading.supplier"
            @refresh="fetchPendingStats"
            @audit="handleSupplierAudit"
          />
        </el-tab-pane>

        <el-tab-pane name="content">
          <template #label>
            <span class="flex items-center gap-2">
              <el-icon><Promotion /></el-icon> 内容发布
              <el-badge :value="pendingStats.content" :hidden="pendingStats.content === 0" type="danger" />
            </span>
          </template>

          <ActivityAuditList
            :role="auditViewMode"
            :items="contentAuditItems"
            :loading="loading.content"
            @refresh="fetchPendingStats"
            @audit="handleContentAudit"
          />
        </el-tab-pane>

        <el-tab-pane name="demand">
          <template #label>
            <span class="flex items-center gap-2">
              <el-icon><Document /></el-icon> 采购需求
              <el-badge :value="pendingStats.demand" :hidden="pendingStats.demand === 0" type="danger" />
            </span>
          </template>

          <div class="p-6">
            <el-alert
              title="采购需求审核写接口尚未接入，当前模块处于只读占位状态。"
              type="warning"
              :closable="false"
              class="mb-6"
            />
            <el-empty description="暂无可处理数据（待后端接口接入）" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { UserFilled, OfficeBuilding, Promotion, Document, Bell } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

import SupplierAuditList from './components/SupplierAuditList.vue'
import ActivityAuditList from './components/ActivityAuditList.vue'

definePageMeta({ layout: 'admin' })

const auditViewMode = ref<'AUDIT_1' | 'AUDIT_2'>('AUDIT_1')
const activeTab = ref<'supplier' | 'content' | 'demand'>('supplier')

const supplierAuditItems = ref<any[]>([])
const contentAuditItems = ref<any[]>([])

const loading = reactive({
  supplier: false,
  content: false
})

const pendingStats = computed(() => ({
  supplier: supplierAuditItems.value.filter(i => i.status === 'pending_level1' || i.status === 'pending_level2').length,
  content: contentAuditItems.value.filter(i => i.status === 'pending_level1' || i.status === 'pending_level2').length,
  demand: 0
}))

const pendingCount = computed(() => pendingStats.value.supplier + pendingStats.value.content + pendingStats.value.demand)

const mapSupplierStatus = (s?: string) => {
  if (s === 'under_review') return 'pending_level2'
  if (s === 'submitted' || s === 'draft') return 'pending_level1'
  if (s === 'approved') return 'approved'
  if (s === 'rejected') return 'rejected'
  return 'pending_level1'
}

const mapSupplierRow = (item: any) => ({
  id: item.id,
  rawId: item.id,
  company: item.companyName || '未命名企业',
  contact: item.contactName || '-',
  phone: item.phone || '-',
  status: mapSupplierStatus(item.verificationStatus),
  submitTime: item.submittedAt || item.createdAt || '-'
})

const mapContentStatus = (s?: string) => {
  if (s === 'draft') return 'pending_level1'
  if (s === 'published') return 'approved'
  if (s === 'archived') return 'rejected'
  return 'approved'
}

const mapContentType = (category?: string) => {
  if (category === 'medical') return { type: 'activity', typeLabel: '医疗', typeColor: 'success' }
  if (category === 'it') return { type: 'training', typeLabel: 'IT', typeColor: 'warning' }
  if (category === 'construction') return { type: 'ad', typeLabel: '工程', typeColor: 'danger' }
  return { type: 'activity', typeLabel: '标讯', typeColor: 'info' }
}

const mapContentRow = (item: any) => {
  const t = mapContentType(item.category)
  return {
    id: item.id,
    type: t.type,
    typeLabel: t.typeLabel,
    typeColor: t.typeColor,
    title: item.title || '未命名内容',
    applicant: item.organization || '-',
    status: mapContentStatus(item.status),
    submitTime: item.publishDate || '-',
    content: item.description || ''
  }
}

const loadSupplierAudits = async () => {
  loading.supplier = true
  try {
    const res = await apiRequest<any>('/v3/admin/member-verifications?page=1&page_size=100')
    const items = res.data?.items || []
    supplierAuditItems.value = items.map(mapSupplierRow)
  } catch (e: any) {
    supplierAuditItems.value = []
    ElMessage.error(e?.message || '读取服务商审核列表失败')
  } finally {
    loading.supplier = false
  }
}

const loadContentAudits = async () => {
  loading.content = true
  try {
    const res = await apiRequest<any>('/v3/admin/tenders?page=1&page_size=100')
    const items = res.data?.items || []
    contentAuditItems.value = items.map(mapContentRow)
  } catch (e: any) {
    contentAuditItems.value = []
    ElMessage.error(e?.message || '读取内容审核列表失败')
  } finally {
    loading.content = false
  }
}

const fetchPendingStats = async () => {
  await Promise.all([loadSupplierAudits(), loadContentAudits()])
}

const handleSupplierAudit = async (payload: any) => {
  const id = payload?.row?.rawId || payload?.row?.id
  if (!id) {
    ElMessage.error('缺少审核ID')
    return
  }

  const action = payload?.action === 'approve' || payload?.action === 'pass' ? 'approve' : 'reject'
  const reason = String(payload?.comment || '')

  try {
    await apiRequest(`/v3/admin/member-verifications/${id}/review`, {
      method: 'POST',
      body: { action, reason }
    })
    ElMessage.success(action === 'approve' ? '审核通过' : '已驳回')
    await fetchPendingStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败')
  }
}

const handleContentAudit = async (payload: any) => {
  const id = payload?.row?.id
  if (!id) {
    ElMessage.error('缺少内容ID')
    return
  }

  const action = payload?.action === 'approve' || payload?.action === 'pass' ? 'approve' : 'reject'
  const toStatus = action === 'approve' ? 'published' : 'archived'

  try {
    await apiRequest(`/v3/admin/tenders/${id}/transition`, {
      method: 'POST',
      body: {
        to_status: toStatus,
        reason: action === 'approve' ? 'content audit approve' : 'content audit reject'
      }
    })
    ElMessage.success(action === 'approve' ? '内容审核通过' : '内容已驳回')
    await fetchPendingStats()
  } catch (e: any) {
    ElMessage.error(e?.message || '内容审核失败')
  }
}

onMounted(() => {
  fetchPendingStats()
})
</script>

<style scoped>
.audit-container {
  padding: 20px;
}
</style>
