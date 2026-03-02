<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">标书管理</h3>
          <p class="text-xs text-slate-500 mt-1">管理已发布、草稿及归档标书</p>
        </div>
        <div class="flex gap-3">
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-2"><Download /></el-icon> 导出列表
          </el-button>
          <el-button type="primary" @click="navigateTo('/admin/tenders/publish')">
            <el-icon class="mr-2"><Plus /></el-icon> 发布新标书
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="mb-4" @tab-change="handleTabChange">
        <el-tab-pane name="published">
          <template #label>
            <span class="flex items-center gap-2">
              已发布
              <el-tag size="small" type="success" effect="plain" round>{{ getCount('published') }}</el-tag>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="draft">
          <template #label>
            <span class="flex items-center gap-2">
              草稿箱
              <el-tag size="small" type="info" effect="plain" round>{{ getCount('draft') }}</el-tag>
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="expired">
          <template #label>
            <span class="flex items-center gap-2">
              已归档
              <el-tag size="small" type="danger" effect="plain" round>{{ getCount('expired') }}</el-tag>
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <div class="flex gap-3 flex-wrap">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标书标题/编号..."
          :prefix-icon="Search"
          class="w-64"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="filters.org" placeholder="采购组织" class="w-32" clearable @change="handleSearch">
          <el-option label="UNDP" value="UNDP" />
          <el-option label="WHO" value="WHO" />
          <el-option label="UNICEF" value="UNICEF" />
          <el-option label="UNGM" value="UNGM" />
        </el-select>
        <el-select v-model="filters.category" placeholder="标书类别" class="w-32" clearable @change="handleSearch">
          <el-option label="医疗器械" value="medical" />
          <el-option label="IT设备" value="it" />
          <el-option label="建筑工程" value="construction" />
        </el-select>
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="发布开始"
          end-placeholder="发布结束"
          value-format="YYYY-MM-DD"
          class="!w-64"
          @change="handleSearch"
        />
        <el-button type="primary" plain @click="handleSearch">查询</el-button>
        <el-button plain @click="handleReset">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <el-table v-loading="apiLoading" :data="displayList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
        <el-table-column prop="tenderNo" label="标书编号" width="170" fixed>
          <template #default="scope">
            <span class="font-mono text-xs font-bold">{{ scope.row.tenderNo }}</span>
          </template>
        </el-table-column>

        <el-table-column label="标书信息" min-width="320">
          <template #default="scope">
            <div>
              <div class="font-bold text-slate-800 text-sm mb-1 line-clamp-1" :title="scope.row.title">
                {{ scope.row.title }}
              </div>
              <div class="text-xs text-slate-500 line-clamp-2" :title="scope.row.description">
                {{ scope.row.description || '暂无描述' }}
              </div>
              <div class="flex gap-2 mt-1">
                <el-tag size="small" type="info">{{ scope.row.categoryLabel }}</el-tag>
                <el-tag size="small" v-if="scope.row.isTop" type="danger">置顶</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="organization" label="采购组织" width="120">
          <template #default="scope">
            <el-tag :type="getOrgTag(scope.row.organization)" effect="light">
              {{ scope.row.organization }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="价格 / 销量" width="140" align="right">
          <template #default="scope">
            <div class="text-sm font-bold text-orange-600">¥{{ scope.row.price }}</div>
            <div class="text-xs text-slate-400">已售 {{ scope.row.sales }} 份</div>
          </template>
        </el-table-column>

        <el-table-column prop="publishDate" label="发布日期" width="120" sortable />

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handlePreview(scope.row)">
              <el-icon class="mr-1"><View /></el-icon> 预览
            </el-button>

            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon class="mr-1"><Edit /></el-icon> 编辑
            </el-button>

            <el-button
              v-if="activeTab !== 'expired'"
              link
              type="danger"
              size="small"
              @click="handleArchive(scope.row)"
            >
              <el-icon class="mr-1"><Delete /></el-icon> {{ activeTab === 'published' ? '下架' : '归档' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :total="totalItems"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="previewDialogVisible" title="标书详情预览" width="800px">
      <div v-if="currentTender">
        <div class="flex justify-between items-start mb-6 pb-4 border-b border-slate-100">
          <div>
            <h2 class="text-xl font-bold text-slate-900 mb-2">{{ currentTender.title }}</h2>
            <div class="flex gap-2">
              <el-tag>{{ currentTender.organization }}</el-tag>
              <el-tag type="info">{{ currentTender.categoryLabel }}</el-tag>
              <el-tag type="warning">{{ currentTender.tenderNo }}</el-tag>
            </div>
          </div>
          <div class="text-right">
            <div class="text-2xl font-bold text-orange-600">¥{{ currentTender.price }}</div>
            <div class="text-sm text-slate-500">发布时间 {{ currentTender.publishDate }}</div>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <h4 class="font-bold text-slate-800 mb-2">项目描述</h4>
            <p class="text-slate-600 leading-relaxed text-sm bg-slate-50 p-4 rounded">
              {{ currentTender.description || '暂无描述' }}
            </p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(currentTender)">去编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Plus, Search, Download, View, Edit, Delete } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { navigateTo } from '#app'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type TenderTab = 'published' | 'draft' | 'expired'

const activeTab = ref<TenderTab>('published')
const currentPage = ref(1)
const pageSize = ref(10)
const previewDialogVisible = ref(false)
const currentTender = ref<any>(null)

const apiLoading = ref(false)
const apiTenders = ref<any[]>([])
const totalItems = ref(0)
const tabCounts = ref({ published: 0, draft: 0, expired: 0 })

const filters = ref({
  keyword: '',
  org: '',
  category: '',
  dateRange: [] as string[]
})

const toDate = (v: any) => {
  if (!v) return ''
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const statusParamMap: Record<TenderTab, string> = {
  published: 'published',
  draft: 'draft',
  expired: 'archived'
}

const normalizeTender = (x: any) => ({
  id: x.id,
  tenderNo: x.tenderNo || x.tender_code || `T-${x.id}`,
  title: x.title || '未命名标书',
  description: x.description || '',
  organization: x.organization || x.org || '-',
  category: x.category || 'other',
  categoryLabel: x.categoryLabel || x.category_label || x.category || '未分类',
  price: Number(x.price ?? x.amount ?? 0),
  sales: Number(x.sales ?? x.sale_count ?? 0),
  publishDate: toDate(x.publishDate || x.publish_date || x.createdAt || x.created_at),
  status: x.status === 'archived' ? 'expired' : (x.status || 'draft'),
  isTop: Boolean(x.isTop ?? x.is_top ?? false)
})

const loadTenders = async () => {
  apiLoading.value = true
  try {
    const qs = new URLSearchParams({
      page: String(currentPage.value),
      page_size: String(pageSize.value),
      tender_status: statusParamMap[activeTab.value],
      keyword: filters.value.keyword || ''
    })
    const res: any = await apiRequest(`/v3/admin/tenders?${qs.toString()}`)
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    apiTenders.value = items.map(normalizeTender)
    totalItems.value = Number(res?.data?.total ?? 0)
    await loadTenderStats()
  } catch (e: any) {
    apiTenders.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '标书加载失败')
  } finally {
    apiLoading.value = false
  }
}

const loadTenderStats = async () => {
  try {
    const res: any = await apiRequest('/v3/admin/tenders/stats')
    tabCounts.value = {
      published: Number(res?.data?.published || 0),
      draft: Number(res?.data?.draft || 0),
      expired: Number(res?.data?.archived || 0)
    }
  } catch {
    // stats 接口失败时不阻断主流程
  }
}

const displayList = computed(() => apiTenders.value)

const getCount = (status: TenderTab) => tabCounts.value[status] || 0

const getOrgTag = (org: string) => {
  const map: Record<string, string> = {
    UNDP: 'primary',
    WHO: 'success',
    UNICEF: 'warning',
    UNGM: 'info'
  }
  return map[org] || 'info'
}

onMounted(async () => {
  await loadTenders()
})

const handleSearch = async () => {
  currentPage.value = 1
  await loadTenders()
  ElMessage.success('查询完成')
}

const handleReset = async (silent = false) => {
  filters.value = { keyword: '', org: '', category: '', dateRange: [] }
  currentPage.value = 1
  await loadTenders()
  if (!silent) ElMessage.info('筛选已重置')
}

const handleTabChange = async () => {
  await handleReset(true)
}

const handlePageChange = async () => {
  if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
  await loadTenders()
}

const handleSizeChange = async () => {
  currentPage.value = 1
  await loadTenders()
}

const handlePreview = (row: any) => {
  currentTender.value = row
  previewDialogVisible.value = true
}

const handleEdit = (row: any) => {
  if (!row?.id) return
  navigateTo(`/admin/tenders/publish?id=${row.id}`)
}

const handleArchive = (row: any) => {
  const actionText = activeTab.value === 'published' ? '下架' : '归档'
  ElMessageBox.confirm(`确定${actionText}标书 "${row.title}" 吗？`, '警告', {
    type: 'warning',
    confirmButtonText: `确认${actionText}`,
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await apiRequest(`/v3/admin/tenders/${row.id}/transition`, {
        method: 'POST',
        body: {
          to_status: 'archived',
          reason: activeTab.value === 'published' ? 'manual offline from manage' : 'manual archive from manage'
        }
      })
      currentPage.value = 1
      await loadTenders()
      ElMessage.success(activeTab.value === 'published' ? '标书已下架并归档' : '标书已归档')
    } catch (e: any) {
      ElMessage.error(e?.message || '状态变更失败')
    }
  }).catch(() => {})
}

const handleExport = () => {
  if (!import.meta.client) return

  const header = '标书编号,标题,组织,分类,价格,销量,发布日期,状态'
  const rows = apiTenders.value.map(item =>
    `"${item.tenderNo}","${item.title}","${item.organization}","${item.categoryLabel}","${item.price}","${item.sales}","${item.publishDate}","${item.status}"`
  )
  const csvContent = '\uFEFF' + [header, ...rows].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = `标书列表_${activeTab.value}_${new Date().toISOString().slice(0, 10)}.csv`
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('列表导出成功')
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
