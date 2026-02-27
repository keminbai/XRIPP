<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\suppliers\analysis.vue
  版本: V1.5 (2026-02-27 收敛版)

  说明:
  1. 主数据源切换为 /v3/admin/member-verifications。
  2. 统计口径基于“服务商入驻审核申请”数据聚合。
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">服务商数据统计</h3>
          <p class="text-xs text-slate-500 mt-1">
            共 <span class="font-bold text-blue-600">{{ filteredSuppliers.length }}</span> 条申请，
            覆盖 <span class="font-bold text-blue-600">{{ stats.provinceCount }}</span> 个省份 /
            <span class="font-bold text-blue-600">{{ stats.cityCount }}</span> 个城市
          </p>
        </div>
        <div class="flex gap-3">
          <el-button plain :loading="loading" @click="loadList">刷新</el-button>
          <el-button type="success" plain @click="handleExportDetail">
            <el-icon class="mr-2"><Download /></el-icon> 导出明细
          </el-button>
        </div>
      </div>

      <el-alert
        title="统计口径说明：当前图表基于服务商入驻审核申请数据（member-verifications），非最终 suppliers 运营主表。"
        type="info"
        :closable="false"
        class="mb-4"
      />

      <div class="flex gap-3 flex-wrap items-center">
        <el-input v-model="filters.keyword" placeholder="搜索公司名称..." :prefix-icon="Search" class="w-64" clearable />
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
        <el-input v-model="filters.city" placeholder="城市（模糊）" class="w-36" clearable />
        <el-input v-model="filters.industry" placeholder="行业（模糊）" class="w-40" clearable />

        <el-button plain @click="handleReset">
          <el-icon class="mr-1"><RefreshRight /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-xs text-slate-500 mb-1">申请总数</div>
          <div class="text-2xl font-bold text-slate-800">{{ filteredSuppliers.length }}</div>
        </div>
        <div class="w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center text-lg">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
      </div>

      <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-xs text-slate-500 mb-1">覆盖国家</div>
          <div class="text-2xl font-bold text-slate-800">{{ stats.countryCount }}</div>
        </div>
        <div class="w-10 h-10 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center text-lg">
          <el-icon><Location /></el-icon>
        </div>
      </div>

      <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-xs text-slate-500 mb-1">覆盖省份</div>
          <div class="text-2xl font-bold text-slate-800">{{ stats.provinceCount }}</div>
        </div>
        <div class="w-10 h-10 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-lg">
          <el-icon><MapLocation /></el-icon>
        </div>
      </div>

      <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-xs text-slate-500 mb-1">覆盖城市</div>
          <div class="text-2xl font-bold text-slate-800">{{ stats.cityCount }}</div>
        </div>
        <div class="w-10 h-10 rounded-lg bg-orange-50 text-orange-600 flex items-center justify-center text-lg">
          <el-icon><Place /></el-icon>
        </div>
      </div>

      <div class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between">
        <div>
          <div class="text-xs text-slate-500 mb-1">已通过数量</div>
          <div class="text-2xl font-bold text-slate-800">{{ stats.approvedCount }}</div>
        </div>
        <div class="w-10 h-10 rounded-lg bg-green-50 text-green-600 flex items-center justify-center text-lg">
          <el-icon><DocumentChecked /></el-icon>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="text-base font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-blue-500"><TrendCharts /></el-icon> 申请增长趋势 (近12个月)
        </h3>
        <div v-if="filteredSuppliers.length" ref="trendChartRef" class="w-full h-80"></div>
        <div v-else class="h-80 flex items-center justify-center text-slate-400 text-sm">暂无趋势数据</div>
      </div>

      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="text-base font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon> 状态占比（当前筛选）
        </h3>
        <div v-if="filteredSuppliers.length" ref="pieChartRef" class="w-full h-80"></div>
        <div v-else class="h-80 flex items-center justify-center text-slate-400 text-sm">暂无分类数据</div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="text-base font-bold text-slate-800 mb-4">行业分布 Top 10</h3>
      <el-table :data="industryStats" stripe border :header-cell-style="{ background: '#f8fafc' }">
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column prop="industry" label="行业名称" />
        <el-table-column prop="count" label="数量" width="150" align="right">
          <template #default="scope">
            <span class="font-bold text-slate-700">{{ scope.row.count }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="percent" label="占比" width="200">
          <template #default="scope">
            <el-progress :percentage="scope.row.percent" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Search,
  Download,
  RefreshRight,
  OfficeBuilding,
  Location,
  MapLocation,
  Place,
  DocumentChecked,
  TrendCharts,
  PieChart
} from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type SupplierAggRow = {
  id: number
  name: string
  industry: string
  city: string
  province: string
  country: string
  status: 'pending' | 'approved' | 'rejected'
  registerDate: string
}

const loading = ref(false)
const allData = ref<SupplierAggRow[]>([])

const filters = ref({
  keyword: '',
  dateRange: [] as string[],
  city: '',
  industry: ''
})

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const toDate = (v: unknown) => {
  if (!v) return ''
  const d = new Date(String(v))
  if (Number.isNaN(d.getTime())) return String(v)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const parseStatus = (s?: string): SupplierAggRow['status'] => {
  if (s === 'approved') return 'approved'
  if (s === 'rejected') return 'rejected'
  return 'pending'
}

const inferProvince = (city?: string) => {
  const c = String(city || '')
  if (c.includes('上海')) return '上海'
  if (c.includes('北京')) return '北京'
  if (c.includes('深圳') || c.includes('广州')) return '广东'
  if (c.includes('杭州')) return '浙江'
  if (c.includes('苏州')) return '江苏'
  return '未知'
}

const loadList = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: 1, page_size: 500 }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()

    const res = await apiRequest<any>('/v3/admin/member-verifications', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []

    allData.value = items.map((item: any) => {
      const city = item.city || ''
      return {
        id: Number(item.id),
        name: item.companyName || '未命名企业',
        industry: item.industry || '未分类',
        city,
        province: inferProvince(city),
        country: '中国',
        status: parseStatus(item.verificationStatus),
        registerDate: toDate(item.submittedAt || item.createdAt)
      }
    })
  } catch (e: any) {
    allData.value = []
    ElMessage.error(e?.message || '读取服务商统计数据失败')
  } finally {
    loading.value = false
  }
}

const filteredSuppliers = computed(() => {
  return allData.value.filter(item => {
    const kw = filters.value.keyword.trim().toLowerCase()
    if (kw && !item.name.toLowerCase().includes(kw)) return false

    if (filters.value.city.trim()) {
      const city = filters.value.city.trim().toLowerCase()
      if (!String(item.city || '').toLowerCase().includes(city)) return false
    }

    if (filters.value.industry.trim()) {
      const industry = filters.value.industry.trim().toLowerCase()
      if (!String(item.industry || '').toLowerCase().includes(industry)) return false
    }

    if (filters.value.dateRange?.length === 2) {
      const [start, end] = filters.value.dateRange
      if (item.registerDate < start || item.registerDate > end) return false
    }

    return true
  })
})

const stats = computed(() => {
  const list = filteredSuppliers.value
  const countries = new Set(list.map(i => i.country))
  const provinces = new Set(list.map(i => i.province))
  const cities = new Set(list.map(i => i.city).filter(Boolean))
  const approvedCount = list.filter(i => i.status === 'approved').length

  return {
    countryCount: countries.size,
    provinceCount: provinces.size,
    cityCount: cities.size,
    approvedCount
  }
})

const industryStats = computed(() => {
  const map: Record<string, number> = {}
  filteredSuppliers.value.forEach(i => {
    map[i.industry] = (map[i.industry] || 0) + 1
  })

  const total = filteredSuppliers.value.length || 1
  return Object.entries(map)
    .map(([k, v]) => ({ industry: k, count: v, percent: Math.round((v / total) * 100) }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
})

const updateCharts = () => {
  if (!trendChart || !pieChart || !trendChartRef.value || !pieChartRef.value) return

  const list = filteredSuppliers.value
  if (!list.length) {
    trendChart.clear()
    pieChart.clear()
    return
  }

  const monthMap: Record<string, number> = {}
  list.forEach(i => {
    const month = i.registerDate.substring(0, 7)
    monthMap[month] = (monthMap[month] || 0) + 1
  })
  const sortedMonths = Object.keys(monthMap).sort().slice(-12)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: sortedMonths },
    yAxis: { type: 'value' },
    series: [{
      data: sortedMonths.map(m => monthMap[m]),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.2 },
      itemStyle: { color: '#3b82f6' }
    }]
  })

  const statusMap: Record<string, number> = { 待审核: 0, 已通过: 0, 已驳回: 0 }
  list.forEach(i => {
    if (i.status === 'approved') statusMap['已通过'] += 1
    else if (i.status === 'rejected') statusMap['已驳回'] += 1
    else statusMap['待审核'] += 1
  })

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      name: '审核状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: Object.entries(statusMap).map(([k, v]) => ({ value: v, name: k })),
      itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 }
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  await loadList()
  trendChart = echarts.init(trendChartRef.value!)
  pieChart = echarts.init(pieChartRef.value!)
  updateCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
  trendChart = null
  pieChart = null
})

watch(filteredSuppliers, () => {
  updateCharts()
})

const handleReset = async () => {
  filters.value = { keyword: '', dateRange: [], city: '', industry: '' }
  await loadList()
  ElMessage.info('筛选已重置')
}

const csvEscape = (val: any) => {
  const str = String(val ?? '')
  return `"${str.replace(/"/g, '""')}"`
}

const handleExportDetail = () => {
  if (!import.meta.client) return

  const header = ['ID', '名称', '行业', '状态', '国家', '省份', '城市', '提交日期']
  const rows = filteredSuppliers.value.map(i => [
    i.id,
    i.name,
    i.industry,
    i.status === 'approved' ? '已通过' : i.status === 'rejected' ? '已驳回' : '待审核',
    i.country,
    i.province,
    i.city,
    i.registerDate
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `服务商统计明细_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)

  ElMessage.success('明细导出成功')
}
</script>

