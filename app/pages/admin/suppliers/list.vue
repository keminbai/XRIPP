<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\suppliers\list.vue
  版本: V1.4  (2026-02-27)

  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">服务商名录库</h3>
          <p class="text-xs text-slate-500 mt-1">数据来源：/v3/admin/supplier-onboarding（真实接口）</p>
        </div>
        <div class="flex items-center gap-3">
          <el-tag type="warning" effect="plain">只读模式</el-tag>
          <el-button type="success" plain @click="handleExport" :disabled="filteredSupplierList.length === 0">
            <el-icon class="mr-2"><Download /></el-icon> 导出名录
          </el-button>
        </div>
      </div>

      <el-alert
        title="新增、编辑、资料上传功能尚未接入服务商写接口，当前页面仅支持真实数据查看与导出。"
        type="warning"
        :closable="false"
        class="mb-4"
      />

      <div class="flex gap-3 flex-wrap items-center">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索公司/联系人/手机号..."
          :prefix-icon="Search"
          class="w-64"
          clearable
        />
        <el-select v-model="filters.status" placeholder="审核状态" class="w-40" clearable>
          <el-option label="待预审" value="submitted" />
          <el-option label="预审通过" value="precheck_passed" />
          <el-option label="终审通过/已激活" value="active" />
          <el-option label="审核未通过" value="rejected" />
          <el-option label="已停用" value="inactive" />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="提交开始"
          end-placeholder="提交结束"
          value-format="YYYY-MM-DD"
          class="!w-64"
          clearable
        />
        <el-button type="primary" plain @click="handleSearch">查询</el-button>
        <el-button plain @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <el-table
        v-loading="loading"
        :data="pagedList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="applyNo" label="申请编号" width="160" />
        <el-table-column label="服务商信息" min-width="260">
          <template #default="{ row }">
            <div class="font-bold text-slate-800">{{ row.name }}</div>
            <div class="text-xs text-slate-500 mt-1">{{ row.contact }} / {{ row.phone }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="statusLabel" label="审核状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.statusTag" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="submitDate" label="提交时间" width="170" />
        <el-table-column prop="partnerId" label="归属合伙人ID" width="140" />

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :total="filteredSupplierList.length"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" :title="`${currentItem?.name || ''} - 服务商详情`" width="760px">
      <div v-if="currentItem" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">{{ currentItem.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="currentItem.statusTag" size="small">{{ currentItem.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称" :span="2">{{ currentItem.name }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentItem.contact }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentItem.phone }}</el-descriptions-item>
          <el-descriptions-item label="行业">{{ currentItem.industry || '-' }}</el-descriptions-item>
          <el-descriptions-item label="归属合伙人ID">{{ currentItem.partnerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentItem.submitDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentItem.reviewedDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="驳回原因" :span="2">{{ currentItem.rejectReason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Download, Search } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type SupplierRow = {
  id: number
  applyNo: string
  name: string
  contact: string
  phone: string
  partnerId: number | null
  industry: string
  status: string
  statusLabel: string
  statusTag: 'warning' | 'success' | 'danger' | 'info' | 'primary'
  submitDate: string
  reviewedDate: string
  rejectReason: string
}

const loading = ref(false)
const rawList = ref<SupplierRow[]>([])
const selectedRows = ref<SupplierRow[]>([])

const currentPage = ref(1)
const pageSize = ref(10)

const detailDialogVisible = ref(false)
const currentItem = ref<SupplierRow | null>(null)

const filters = ref({
  keyword: '',
  status: '',
  dateRange: [] as string[]
})

const statusMap = (s?: string) => {
  if (s === 'active') return { status: 'active', label: '已激活', tag: 'success' as const }
  if (s === 'final_review_passed') return { status: 'active', label: '终审通过', tag: 'success' as const }
  if (s === 'precheck_passed') return { status: 'precheck_passed', label: '预审通过', tag: 'primary' as const }
  if (s === 'precheck_failed') return { status: 'rejected', label: '预审未通过', tag: 'danger' as const }
  if (s === 'final_review_failed') return { status: 'rejected', label: '终审未通过', tag: 'danger' as const }
  if (s === 'inactive') return { status: 'inactive', label: '已停用', tag: 'info' as const }
  if (s === 'submitted') return { status: 'submitted', label: '待预审', tag: 'warning' as const }
  return { status: 'draft', label: '草稿', tag: 'info' as const }
}

const fmtDate = (v: unknown) => {
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

const mapRow = (item: any): SupplierRow => {
  const st = statusMap(item.onboardingStatus)
  return {
    id: Number(item.id),
    applyNo: `SO${String(item.id).padStart(6, '0')}`,
    name: item.companyName || '未命名企业',
    contact: item.cityName || '-',
    phone: item.onboardingStatusLabel || '-',
    partnerId: item.partnerId ?? null,
    industry: '',
    status: st.status,
    statusLabel: st.label,
    statusTag: st.tag,
    submitDate: fmtDate(item.createdAt),
    reviewedDate: fmtDate(item.updatedAt),
    rejectReason: ''
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = {
      page: 1,
      page_size: 200
    }
    if (filters.value.status) {
      const statusToApi: Record<string, string> = {
        submitted: 'submitted',
        precheck_passed: 'precheck_passed',
        active: 'active',
        rejected: 'precheck_failed',
        inactive: 'inactive'
      }
      query.onboarding_status = statusToApi[filters.value.status] || filters.value.status
    }
    if (filters.value.keyword.trim()) {
      query.keyword = filters.value.keyword.trim()
    }

    const res = await apiRequest<any>('/v3/admin/supplier-onboarding', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    rawList.value = items.map(mapRow)
  } catch (e: any) {
    rawList.value = []
    ElMessage.error(e?.message || '读取服务商列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadList)

const filteredSupplierList = computed(() => {
  const kw = filters.value.keyword.trim().toLowerCase()
  return rawList.value.filter(item => {
    if (kw) {
      const match =
        item.name.toLowerCase().includes(kw) ||
        item.contact.toLowerCase().includes(kw) ||
        item.phone.toLowerCase().includes(kw) ||
        item.applyNo.toLowerCase().includes(kw)
      if (!match) return false
    }

    if (filters.value.status && item.status !== filters.value.status) return false

    if (filters.value.dateRange.length === 2) {
      const [start, end] = filters.value.dateRange
      const d = item.submitDate.slice(0, 10)
      if (d < start || d > end) return false
    }

    return true
  })
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredSupplierList.value.slice(start, start + pageSize.value)
})

const handleSelectionChange = (rows: SupplierRow[]) => {
  selectedRows.value = rows
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadList()
  ElMessage.success('查询完成')
}

const handleReset = async () => {
  filters.value = { keyword: '', status: '', dateRange: [] }
  currentPage.value = 1
  await loadList()
  ElMessage.info('筛选已重置')
}

const handleView = (row: SupplierRow) => {
  currentItem.value = row
  detailDialogVisible.value = true
}

const csvEscape = (val: unknown) => `"${String(val ?? '').replace(/"/g, '""')}"`

const handleExport = () => {
  if (!import.meta.client) return
  const list = selectedRows.value.length ? selectedRows.value : filteredSupplierList.value
  const header = ['申请编号', '公司名称', '联系人', '联系电话', '行业', '审核状态', '提交时间', '审核时间', '驳回原因']
  const rows = list.map(i => [
    i.applyNo, i.name, i.contact, i.phone, i.industry, i.statusLabel, i.submitDate, i.reviewedDate, i.rejectReason
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `服务商名录_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}
</script>
