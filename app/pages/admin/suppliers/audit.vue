<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\suppliers\audit.vue
  版本: V1.3 (2026-02-27 收敛版)

  说明:
  1. 主数据源已切换到 /v3/admin/member-verifications。
  2. 审核动作调用 /v3/admin/member-verifications/{id}/review。
  3. 证书上传/下载接口未接入，保留入口并受控降级。
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between"
      >
        <div>
          <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
          <div class="text-xs text-slate-400 mt-1">{{ stat.subLabel }}</div>
        </div>
        <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
          <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 space-y-4">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-lg font-bold text-slate-800">服务商入驻审核</h3>
            <p class="text-xs text-slate-500 mt-1">真实数据源：`/v3/admin/member-verifications`</p>
          </div>
          <el-button type="success" plain @click="handleBatchAudit" :disabled="selectedRows.length === 0 || activeTab !== 'pending'">
            <el-icon class="mr-2"><Check /></el-icon> 批量通过({{ selectedRows.length }})
          </el-button>
        </div>

        <el-alert
          title="证书上传/下载接口尚未接入，当前页面仅审核流转可用；证书能力为受控降级。"
          type="warning"
          :closable="false"
        />

        <el-tabs v-model="activeTab" class="mb-1" @tab-change="handleTabChange">
          <el-tab-pane name="pending">
            <template #label>
              <span class="flex items-center gap-2">待审核 <el-badge :value="pendingList.length" type="danger" /></span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="approved">
            <template #label>
              <span class="flex items-center gap-2">已通过 <el-tag size="small" type="success" effect="plain" round>{{ approvedList.length }}</el-tag></span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="rejected">
            <template #label>
              <span class="flex items-center gap-2">已驳回 <el-tag size="small" type="info" effect="plain" round>{{ rejectedList.length }}</el-tag></span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <div class="flex gap-3 flex-wrap">
          <el-input v-model="filters.keyword" placeholder="搜索公司名称/联系人..." :prefix-icon="Search" class="w-64" clearable />
          <el-input v-model="filters.city" placeholder="城市（模糊）" class="w-36" clearable />
          <el-input v-model="filters.serviceType" placeholder="服务类型/行业（模糊）" class="w-44" clearable />
          <el-button type="primary" plain @click="handleSearch"><el-icon class="mr-1"><Search /></el-icon> 查询</el-button>
          <el-button plain @click="handleReset"><el-icon class="mr-1"><RefreshLeft /></el-icon> 重置</el-button>
          <el-button plain :loading="loading" @click="loadList">刷新</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table
          v-loading="loading"
          :data="pagedList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" v-if="activeTab === 'pending'" />

          <el-table-column prop="applyNo" label="申请编号" width="150" />

          <el-table-column label="企业信息" min-width="280">
            <template #default="{ row }">
              <div class="py-2">
                <div class="font-bold text-slate-800 text-sm">{{ row.companyName }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ row.contactName }} / {{ row.phone }}</div>
                <div class="text-xs text-slate-400 mt-1">{{ row.industry || '未填写行业' }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="地区" width="150">
            <template #default="{ row }">
              {{ row.city || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.statusLabel)" effect="dark" size="small">{{ row.statusLabel }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="提交时间" width="170" align="center">
            <template #default="{ row }">
              {{ row.submitDate || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="300" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">
                <el-icon class="mr-1"><View /></el-icon> 查看详情
              </el-button>

              <template v-if="row.statusCode === 'pending'">
                <el-button link type="success" size="small" @click="handleApprove(row)">
                  <el-icon class="mr-1"><Check /></el-icon> 通过
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(row)">
                  <el-icon class="mr-1"><Close /></el-icon> 驳回
                </el-button>
              </template>

              <template v-else>
                <el-button link type="warning" size="small" @click="handleCertNotReady('upload')">
                  <el-icon class="mr-1"><Upload /></el-icon> 上传证书(待接入)
                </el-button>
                <el-button link type="info" size="small" @click="handleCertNotReady('download')">
                  <el-icon class="mr-1"><Download /></el-icon> 下载证书(待接入)
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next, sizes"
            :total="filteredAuditList.length"
            :page-sizes="[10, 20, 50]"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" :title="`${currentDetailItem?.companyName || ''} - 入驻申请详情`" width="900px">
      <div v-if="currentDetailItem" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">{{ currentDetailItem.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentDetailItem.submitDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="公司名称" :span="2">{{ currentDetailItem.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentDetailItem.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetailItem.phone }}</el-descriptions-item>
          <el-descriptions-item label="行业">{{ currentDetailItem.industry || '-' }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ currentDetailItem.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(currentDetailItem.statusLabel)">{{ currentDetailItem.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驳回原因" :span="2">{{ currentDetailItem.rejectReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-alert title="证书模块待后端接口接入，当前仅保留操作入口。" type="info" :closable="false" />
      </div>

      <template #footer v-if="currentDetailItem?.statusCode === 'pending'">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleReject(currentDetailItem)">
          <el-icon class="mr-1"><Close /></el-icon> 驳回申请
        </el-button>
        <el-button type="success" @click="handleApprove(currentDetailItem)">
          <el-icon class="mr-1"><Check /></el-icon> 通过审核
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="600px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因，将通知申请企业..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmReject" :loading="submitLoading">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Clock,
  CircleCheck,
  CircleClose,
  TrendCharts,
  Check,
  Close,
  Search,
  RefreshLeft,
  View,
  Upload,
  Download
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type AuditRow = {
  id: number
  applyNo: string
  companyName: string
  contactName: string
  phone: string
  industry: string
  city: string
  statusCode: 'pending' | 'approved' | 'rejected'
  statusLabel: '待审核' | '已通过' | '已驳回'
  submitDate: string
  rejectReason: string
}

const activeTab = ref<'pending' | 'approved' | 'rejected'>('pending')
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const submitLoading = ref(false)
const currentDetailItem = ref<AuditRow | null>(null)
const currentRejectItem = ref<AuditRow | null>(null)
const selectedRows = ref<AuditRow[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const filters = ref({
  keyword: '',
  city: '',
  serviceType: ''
})

const rejectForm = ref({ reason: '' })
const allAuditList = ref<AuditRow[]>([])

const toDateTime = (v: unknown) => {
  if (!v) return ''
  const d = new Date(String(v))
  if (Number.isNaN(d.getTime())) return String(v)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

const normalizeStatus = (s?: string): { code: AuditRow['statusCode']; label: AuditRow['statusLabel'] } => {
  if (s === 'approved') return { code: 'approved', label: '已通过' }
  if (s === 'rejected') return { code: 'rejected', label: '已驳回' }
  return { code: 'pending', label: '待审核' }
}

const mapRow = (item: any): AuditRow => {
  const st = normalizeStatus(item.verificationStatus)
  return {
    id: Number(item.id),
    applyNo: `SUP${String(item.id).padStart(8, '0')}`,
    companyName: item.companyName || '未命名企业',
    contactName: item.contactName || '-',
    phone: item.phone || '-',
    industry: item.industry || '',
    city: item.city || '',
    statusCode: st.code,
    statusLabel: st.label,
    submitDate: toDateTime(item.submittedAt || item.createdAt),
    rejectReason: item.changeReason || ''
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: 1, page_size: 300 }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    const res = await apiRequest<any>('/v3/admin/member-verifications', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allAuditList.value = items.map(mapRow)
  } catch (e: any) {
    allAuditList.value = []
    ElMessage.error(e?.message || '读取服务商审核列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadList()
})

const pendingList = computed(() => allAuditList.value.filter(item => item.statusCode === 'pending'))
const approvedList = computed(() => allAuditList.value.filter(item => item.statusCode === 'approved'))
const rejectedList = computed(() => allAuditList.value.filter(item => item.statusCode === 'rejected'))

const filteredAuditList = computed(() => {
  let list = allAuditList.value

  if (activeTab.value === 'pending') list = pendingList.value
  if (activeTab.value === 'approved') list = approvedList.value
  if (activeTab.value === 'rejected') list = rejectedList.value

  const kw = filters.value.keyword.trim().toLowerCase()
  if (kw) {
    list = list.filter(item =>
      item.companyName.toLowerCase().includes(kw) ||
      item.contactName.toLowerCase().includes(kw)
    )
  }

  if (filters.value.city.trim()) {
    const city = filters.value.city.trim().toLowerCase()
    list = list.filter(item => String(item.city || '').toLowerCase().includes(city))
  }

  if (filters.value.serviceType.trim()) {
    const s = filters.value.serviceType.trim().toLowerCase()
    list = list.filter(item => String(item.industry || '').toLowerCase().includes(s))
  }

  return list
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAuditList.value.slice(start, start + pageSize.value)
})

const stats = computed(() => {
  const total = allAuditList.value.length
  const pending = pendingList.value.length
  const approved = approvedList.value.length
  const rejected = rejectedList.value.length
  const passRate = total ? `${Math.round((approved / total) * 100)}%` : '0%'

  return [
    { label: '待审核', value: String(pending), subLabel: '实时待办', icon: Clock, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已通过', value: String(approved), subLabel: `通过率 ${passRate}`, icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已驳回', value: String(rejected), subLabel: '已记录原因', icon: CircleClose, bgClass: 'bg-red-50', textClass: 'text-red-600' },
    { label: '累计申请', value: String(total), subLabel: '真实接口统计', icon: TrendCharts, bgClass: 'bg-blue-50', textClass: 'text-blue-600' }
  ]
})

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    待审核: 'warning',
    已通过: 'success',
    已驳回: 'info'
  }
  return map[status] || 'info'
}

const handleTabChange = () => {
  selectedRows.value = []
  currentPage.value = 1
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadList()
  ElMessage.success('查询完成')
}

const handleReset = async () => {
  filters.value = { keyword: '', city: '', serviceType: '' }
  currentPage.value = 1
  await loadList()
  ElMessage.info('筛选条件已重置')
}

const handleSelectionChange = (selection: AuditRow[]) => {
  selectedRows.value = selection
}

const reviewOne = async (row: AuditRow, action: 'approve' | 'reject', reason: string) => {
  await apiRequest(`/v3/admin/member-verifications/${row.id}/review`, {
    method: 'POST',
    body: { action, reason }
  })
}

const handleBatchAudit = () => {
  ElMessageBox.confirm(`确认批量通过 ${selectedRows.value.length} 个申请吗？`, '提示', { type: 'success' })
    .then(async () => {
      submitLoading.value = true
      try {
        await Promise.all(selectedRows.value.map(row => reviewOne(row, 'approve', 'batch approve')))
        ElMessage.success('批量审核完成')
        selectedRows.value = []
        await loadList()
      } catch (e: any) {
        ElMessage.error(e?.message || '批量审核失败')
      } finally {
        submitLoading.value = false
      }
    })
    .catch(() => {})
}

const handleViewDetail = (row: AuditRow) => {
  currentDetailItem.value = row
  detailDialogVisible.value = true
}

const handleApprove = (row: AuditRow) => {
  ElMessageBox.confirm(`确认通过 ${row.companyName} 的入驻申请吗？`, '提示', { type: 'success' })
    .then(async () => {
      try {
        await reviewOne(row, 'approve', 'manual approve')
        ElMessage.success('审核通过')
        detailDialogVisible.value = false
        await loadList()
      } catch (e: any) {
        ElMessage.error(e?.message || '审核失败')
      }
    })
    .catch(() => {})
}

const handleReject = (row: AuditRow) => {
  currentRejectItem.value = row
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
  detailDialogVisible.value = false
}

const handleConfirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  if (!currentRejectItem.value) return

  submitLoading.value = true
  try {
    await reviewOne(currentRejectItem.value, 'reject', rejectForm.value.reason.trim())
    ElMessage.success('已驳回申请')
    rejectDialogVisible.value = false
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.message || '驳回失败')
  } finally {
    submitLoading.value = false
  }
}

const handleCertNotReady = (mode: 'upload' | 'download') => {
  ElMessage.warning(mode === 'upload' ? '证书上传接口待接入' : '证书下载接口待接入')
}
</script>

<style scoped>
/* 保持布局风格一致 */
</style>

