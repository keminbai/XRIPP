<!--
  文件路径: D:\ipp-platform\app\pages\admin\partners\analysis.vue
  版本: V3.0 API对接版 (2026-03-12)

  修复: 移除所有Mock统计/排名数据，对接 /v3/admin/partners API
  - 统计卡片: computed from loaded partners
  - 排名表: from real partner data
  - 趋势图: computed from partner createdAt
-->
<template>
  <div class="space-y-6">

    <!-- 1. 固定统计项显示 -->
    <div class="grid grid-cols-1 md:grid-cols-6 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 2. 数据筛选与导出 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex flex-wrap gap-4 items-end">
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm text-slate-600 mb-2">关键字搜索</label>
          <el-input v-model="filterKeyword" placeholder="搜索合伙人名称" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="flex gap-2">
          <el-button type="primary" @click="loadPartners">
            <el-icon class="mr-1"><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-1"><RefreshLeft /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon class="mr-1"><Download /></el-icon> 导出
          </el-button>
        </div>
      </div>
    </div>

    <!-- 3. 趋势图 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <el-icon class="text-orange-500"><TrendCharts /></el-icon>
          合伙人入驻趋势
        </h3>
        <div class="h-64">
          <ClientOnly>
            <AppChart :options="partnerTrendOptions" />
          </ClientOnly>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <el-icon class="text-green-500"><Money /></el-icon>
          合伙人状态分布
        </h3>
        <div class="h-64">
          <ClientOnly>
            <AppChart :options="statusPieOptions" />
          </ClientOnly>
        </div>
      </div>
    </div>

    <!-- 4. 合伙人排行榜 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-base font-bold text-slate-800">合伙人列表</h3>
      </div>

      <el-table :data="partnerList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}" v-loading="loading">
        <el-table-column type="index" label="#" width="60" align="center" />

        <el-table-column prop="companyName" label="公司名称" min-width="200">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xs font-bold">
                {{ (scope.row.companyName || '?').substring(0, 1) }}
              </div>
              <span class="font-medium">{{ scope.row.companyName || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="city" label="城市" width="120" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : scope.row.status === 'pending' ? 'warning' : 'info'" size="small">
              {{ scope.row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="星级" width="150" align="center">
          <template #default="scope">
            <el-rate :model-value="scope.row.starLevel || 0" disabled />
          </template>
        </el-table-column>

        <el-table-column label="入驻时间" width="120">
          <template #default="scope">{{ (scope.row.createdAt || '').slice(0, 10) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          background
          layout="total, prev, pager, next"
          :total="totalItems"
          :page-size="pageSize"
          @current-change="loadPartners"
        />
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="合伙人详情" width="700px" destroy-on-close>
      <div v-if="currentPartner">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="公司名称">{{ currentPartner.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentPartner.contactName }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ currentPartner.city }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentPartner.statusLabel }}</el-descriptions-item>
          <el-descriptions-item label="星级">{{ currentPartner.starLevel || 0 }}星</el-descriptions-item>
          <el-descriptions-item label="入驻时间">{{ (currentPartner.createdAt || '').slice(0, 10) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import {
  Connection, TrendCharts, Trophy, Money, Location, Flag,
  Search, RefreshLeft, Download, Medal
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// State
const loading = ref(false)
const partnerList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const filterKeyword = ref('')

const statusMap: Record<string, string> = {
  active: '已激活', pending: '待审核', suspended: '已冻结', expired: '已过期'
}

const mapPartner = (p: any) => ({
  ...p,
  companyName: p.companyName || p.company_name || '-',
  contactName: p.contactName || p.contact_name || '-',
  city: p.city || p.cityName || p.city_name || '-',
  status: p.status || 'active',
  statusLabel: statusMap[p.status] || p.status || '未知',
  starLevel: Number(p.starLevel || p.star_level || 0),
  createdAt: p.createdAt || p.created_at || ''
})

// Load from API
const loadPartners = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filterKeyword.value?.trim()) query.keyword = filterKeyword.value.trim()

    const res: any = await apiRequest('/v3/admin/partners', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    partnerList.value = items.map(mapPartner)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    partnerList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '加载合伙人数据失败')
  } finally {
    loading.value = false
  }
}

// Stats computed from loaded data
const stats = computed(() => {
  const list = partnerList.value
  const total = totalItems.value
  const activeCount = list.filter(p => p.status === 'active').length
  const cities = new Set(list.map(p => p.city).filter(Boolean))

  return [
    { label: '合伙人总数', value: String(total), icon: Connection, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已激活', value: String(activeCount), icon: Trophy, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '开通城市', value: String(cities.size), icon: Location, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '五星合伙人', value: String(list.filter(p => p.starLevel >= 5).length), icon: Trophy, bgClass: 'bg-yellow-50', textClass: 'text-yellow-600' },
    { label: '待审核', value: String(list.filter(p => p.status === 'pending').length), icon: Flag, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: '当页数量', value: String(list.length), icon: Money, bgClass: 'bg-emerald-50', textClass: 'text-emerald-600' }
  ]
})

// Trend chart from createdAt
const partnerTrendOptions = computed(() => {
  const map: Record<string, number> = {}
  partnerList.value.forEach(p => {
    const month = (p.createdAt || '').slice(0, 7)
    if (month) map[month] = (map[month] || 0) + 1
  })
  const labels = Object.keys(map).sort()

  return {
    grid: { top: 40, bottom: 30, left: 50, right: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
    series: [{
      name: '入驻数',
      type: 'line',
      smooth: true,
      data: labels.map(l => map[l]),
      itemStyle: { color: '#f97316' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(249, 115, 22, 0.3)' },
          { offset: 1, color: 'rgba(249, 115, 22, 0.05)' }
        ])
      }
    }]
  }
})

// Status pie chart
const statusPieOptions = computed(() => {
  const map: Record<string, number> = {}
  partnerList.value.forEach(p => {
    const s = p.statusLabel || '未知'
    map[s] = (map[s] || 0) + 1
  })
  const data = Object.entries(map).map(([name, value]) => ({ name, value }))

  return {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.length ? data : [{ name: '无数据', value: 1 }],
      label: { formatter: '{b} {d}%' }
    }]
  }
})

// Actions
const handleReset = () => {
  filterKeyword.value = ''
  currentPage.value = 1
  loadPartners()
}

const escapeCsv = (value: unknown) => {
  const str = String(value ?? '')
  if (/[",\n\r]/.test(str)) return `"${str.replace(/"/g, '""')}"`
  return str
}

const handleExport = () => {
  if (!partnerList.value.length) return ElMessage.warning('暂无数据')
  const headers = ['公司名称', '联系人', '城市', '状态', '星级', '入驻时间']
  const rows = partnerList.value.map(p => [
    p.companyName, p.contactName, p.city, p.statusLabel, p.starLevel, (p.createdAt || '').slice(0, 10)
  ])
  const csv = '\uFEFF' + [headers.map(escapeCsv).join(','), ...rows.map(r => r.map(escapeCsv).join(','))].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `合伙人分析_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('已导出 CSV')
}

const detailDialogVisible = ref(false)
const currentPartner = ref<any>(null)
const handleViewDetail = (row: any) => {
  currentPartner.value = row
  detailDialogVisible.value = true
}

onMounted(() => { loadPartners() })
</script>
