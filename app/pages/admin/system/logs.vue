<template>
  <div class="bg-white p-6 rounded-xl border border-slate-200 space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        操作日志页已接入真实 API，当前统一聚合审核日志与状态流转日志；未显示无真实来源的 IP 字段。
      </template>
    </el-alert>

    <div>
      <h3 class="text-lg font-bold text-slate-800">操作日志</h3>
      <p class="text-xs text-slate-500 mt-1">查询系统操作记录</p>
    </div>

    <div class="flex gap-3 flex-wrap">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索用户/操作/对象/备注..."
        class="w-72"
        clearable
      />
      <el-select v-model="filters.source" placeholder="日志来源" class="w-40" clearable>
        <el-option label="审核日志" value="audit" />
        <el-option label="状态流转" value="transition" />
      </el-select>
      <el-date-picker
        v-model="filters.dateRange"
        type="daterange"
        class="w-72"
        value-format="YYYY-MM-DD"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
      <el-button type="primary" plain :loading="loading" @click="loadLogs">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
      <el-button type="success" plain @click="handleExport">导出</el-button>
    </div>

    <el-table :data="logs" stripe v-loading="loading">
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="user" label="操作人" width="140" />
      <el-table-column prop="sourceLabel" label="来源" width="120" />
      <el-table-column prop="action" label="操作" width="160" show-overflow-tooltip />
      <el-table-column prop="module" label="模块" width="180" show-overflow-tooltip />
      <el-table-column prop="target" label="对象" width="180" show-overflow-tooltip />
      <el-table-column prop="comment" label="备注" min-width="220" show-overflow-tooltip />
    </el-table>

    <div class="flex justify-end">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :current-page="page"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type LogItem = {
  id: number
  time: string
  user: string
  source: string
  sourceLabel: string
  action: string
  module: string
  target: string
  comment: string
}

const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const logs = ref<LogItem[]>([])
const filters = ref({
  keyword: '',
  source: '',
  dateRange: [] as string[]
})

const loadLogs = async () => {
  loading.value = true
  try {
    const query = new URLSearchParams({
      page: String(page.value),
      page_size: String(pageSize)
    })
    if (filters.value.keyword.trim()) query.set('keyword', filters.value.keyword.trim())
    if (filters.value.source) query.set('source', filters.value.source)
    if (filters.value.dateRange.length === 2) {
      query.set('date_from', filters.value.dateRange[0])
      query.set('date_to', filters.value.dateRange[1])
    }
    const res = await apiRequest<any>(`/v3/admin/logs?${query.toString()}`)
    logs.value = Array.isArray(res.data?.items) ? res.data.items : []
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error?.message || '操作日志加载失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.value = { keyword: '', source: '', dateRange: [] }
  page.value = 1
  await loadLogs()
}

const handlePageChange = async (value: number) => {
  page.value = value
  await loadLogs()
}

const escapeCsv = (value: unknown) => {
  const text = value == null ? '' : String(value)
  return `"${text.replace(/"/g, '""')}"`
}

const handleExport = () => {
  if (!logs.value.length || !import.meta.client) {
    ElMessage.warning('当前无可导出的日志数据')
    return
  }
  const header = ['时间', '操作人', '来源', '操作', '模块', '对象', '备注']
  const lines = [
    header.map(escapeCsv).join(','),
    ...logs.value.map((item) => [
      item.time,
      item.user,
      item.sourceLabel,
      item.action,
      item.module,
      item.target,
      item.comment
    ].map(escapeCsv).join(','))
  ]
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `admin-logs-page-${page.value}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
  ElMessage.success('日志导出已生成')
}

onMounted(async () => {
  await loadLogs()
})
</script>
