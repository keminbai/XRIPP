<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\tenders\reference.vue
  版本: V1.4  (2026-02-27)
  
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="mb-6 flex items-start justify-between">
        <div>
          <h3 class="text-lg font-bold text-slate-800">引用外部标讯</h3>
          <p class="text-xs text-slate-500 mt-1">当前阶段仅支持查看真实标书列表并导出；写接口未接入</p>
        </div>
        <el-tag type="warning" effect="plain">只读模式</el-tag>
      </div>

      <el-alert
        title="自动抓取、引用提交、删除功能尚未接入后端写接口，当前页面为只读展示。"
        type="warning"
        :closable="false"
        class="mb-5"
      />

      <el-form label-width="120px" class="max-w-3xl">
        <el-form-item label="标讯来源">
          <el-select v-model="form.source" class="w-full" placeholder="选择来源（仅记录）" disabled>
            <el-option label="UNGM" value="UNGM" />
            <el-option label="TED" value="TED" />
            <el-option label="WorldBank" value="WorldBank" />
            <el-option label="ADB" value="ADB" />
          </el-select>
        </el-form-item>

        <el-form-item label="标讯链接">
          <el-input v-model="form.url" placeholder="写接口未接入，暂不可提交" disabled>
            <template #append>
              <el-button disabled>
                <el-icon class="mr-1"><Refresh /></el-icon> 抓取信息
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="标讯标题">
          <el-input v-model="form.title" placeholder="只读模式" disabled />
        </el-form-item>

        <el-form-item label="采购组织">
          <el-input v-model="form.organization" placeholder="只读模式" disabled />
        </el-form-item>

        <el-form-item label="标讯编号">
          <el-input v-model="form.referenceNo" placeholder="只读模式" disabled />
        </el-form-item>

        <el-form-item label="标讯描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="只读模式" disabled />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" disabled>
            <el-icon class="mr-2"><Check /></el-icon> 引用提交（待接入）
          </el-button>
          <el-button disabled>重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">可引用标讯列表（真实数据）</h3>
            <p class="text-xs text-slate-500 mt-1">数据来源：`/v3/admin/tenders`</p>
          </div>
          <el-button type="success" plain @click="handleExport" :disabled="allReferenceList.length === 0">
            <el-icon class="mr-2"><Download /></el-icon> 导出列表
          </el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题/编号..."
            :prefix-icon="Search"
            class="w-64"
            clearable
          />
          <el-select v-model="filters.source" placeholder="来源平台" class="w-40" clearable>
            <el-option label="UNGM" value="UNGM" />
            <el-option label="TED" value="TED" />
            <el-option label="WorldBank" value="WorldBank" />
            <el-option label="ADB" value="ADB" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table v-loading="loading" :data="displayList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
          <el-table-column label="标讯信息" min-width="340">
            <template #default="{ row }">
              <div class="py-2">
                <div class="font-bold text-slate-800 mb-1">{{ row.title }}</div>
                <div class="text-xs text-slate-500 line-clamp-2">{{ row.description || '暂无描述' }}</div>
                <div class="flex gap-2 mt-2">
                  <el-tag size="small" :type="getSourceTag(row.source)">{{ row.source }}</el-tag>
                  <el-tag size="small" type="info" v-if="row.referenceNo">{{ row.referenceNo }}</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="organization" label="采购组织" width="150" />

          <el-table-column label="截止时间" width="180">
            <template #default="{ row }">
              <div class="text-sm">
                <div>{{ row.deadline || '-' }}</div>
                <div class="text-xs" :class="getDeadlineClass(row.deadline)">
                  {{ getDeadlineLabel(row.deadline) }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="createDate" label="创建时间" width="120" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewUrl(row)">
                <el-icon class="mr-1"><Link /></el-icon> 访问原文
              </el-button>
              <el-button link type="warning" size="small" disabled>
                <el-icon class="mr-1"><Edit /></el-icon> 编辑（待接入）
              </el-button>
              <el-button link type="danger" size="small" disabled>删除（待接入）</el-button>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { Refresh, Check, Download, Search, Link, Edit } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type RefRow = {
  id: number
  source: string
  url: string
  title: string
  organization: string
  referenceNo: string
  description: string
  deadline: string
  createDate: string
}

const loading = ref(false)
const allReferenceList = ref<RefRow[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const form = ref({
  source: '',
  url: '',
  title: '',
  organization: '',
  referenceNo: '',
  description: ''
})

const filters = ref({
  keyword: '',
  source: ''
})

const normalizeDate = (v: unknown, withTime = false) => {
  if (!v) return ''
  const d = new Date(String(v))
  if (Number.isNaN(d.getTime())) return String(v)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  if (!withTime) return `${yyyy}-${mm}-${dd}`
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

const mapTenderToReference = (t: any): RefRow => ({
  id: Number(t.id),
  source: t.source || 'UNGM',
  url: t.source_url || t.url || '',
  title: t.title || '未命名标讯',
  organization: t.organization || '-',
  referenceNo: t.reference_no || t.tenderNo || '',
  description: t.description || '',
  deadline: normalizeDate(t.deadline || t.deadline_at, true),
  createDate: normalizeDate(t.created_at || t.createdAt || t.publishDate)
})

const loadReferences = async () => {
  loading.value = true
  try {
    const qs = new URLSearchParams({
      page: String(currentPage.value),
      page_size: String(pageSize.value)
    })
    if (filters.value.keyword) qs.set('keyword', filters.value.keyword)
    const res: any = await apiRequest(`/v3/admin/tenders?${qs.toString()}`)
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allReferenceList.value = items.map(mapTenderToReference)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    allReferenceList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取标讯列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadReferences)

const displayList = computed(() => allReferenceList.value)

const getSourceTag = (source: string) => {
  const map: Record<string, string> = {
    UNGM: 'primary',
    TED: 'success',
    WorldBank: 'warning',
    ADB: 'danger'
  }
  return map[source] || 'info'
}

const getDeadlineClass = (deadline: string) => {
  if (!deadline) return 'text-slate-400'
  const d = new Date(deadline)
  if (Number.isNaN(d.getTime())) return 'text-slate-400'
  const days = Math.ceil((d.getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  if (days < 0) return 'text-red-600'
  if (days <= 7) return 'text-orange-600'
  return 'text-green-600'
}

const getDeadlineLabel = (deadline: string) => {
  if (!deadline) return '未设置'
  const d = new Date(deadline)
  if (Number.isNaN(d.getTime())) return '时间格式未知'
  const days = Math.ceil((d.getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  if (days < 0) return '已截止'
  if (days === 0) return '今日截止'
  return `剩余 ${days} 天`
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadReferences()
}

const handleReset = async () => {
  filters.value = { keyword: '', source: '' }
  currentPage.value = 1
  await loadReferences()
}

const handlePageChange = async () => {
  if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
  await loadReferences()
}

const handleSizeChange = async () => {
  currentPage.value = 1
  await loadReferences()
}

const csvEscape = (val: unknown) => `"${String(val ?? '').replace(/"/g, '""')}"`

const handleExport = () => {
  if (!import.meta.client) return

  const header = ['标题', '来源', '采购组织', '标讯编号', '截止时间', '创建时间', '链接']
  const rows = allReferenceList.value.map(i => [
    i.title, i.source, i.organization, i.referenceNo, i.deadline, i.createDate, i.url
  ])

  const csv = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `引用标讯_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

const handleViewUrl = (row: RefRow) => {
  if (!row.url) {
    ElMessage.warning('该条目暂无原文链接')
    return
  }
  window.open(row.url, '_blank', 'noopener,noreferrer')
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
