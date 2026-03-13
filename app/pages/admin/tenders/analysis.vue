<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\tenders\analysis.vue
  版本: V2.0 (2026-03-12)
  数据来源: GET /v3/admin/tenders/analysis (真实接口，仅 published 标书)
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部筛选与控制 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">标书发布统计</h3>
          <p class="text-xs text-slate-500 mt-1">数据来源：/v3/admin/tenders/analysis（真实接口，仅 published 标书）</p>
        </div>
        <div class="flex items-center gap-3">
          <el-radio-group v-model="trendMode" size="small" @change="updateCharts">
            <el-radio-button label="month">按月查看</el-radio-button>
            <el-radio-button label="day">按日查看</el-radio-button>
          </el-radio-group>
          <el-button type="primary" plain @click="handleExport">
            <el-icon class="mr-1"><Download /></el-icon> 导出报表
          </el-button>
        </div>
      </div>

      <div class="flex gap-3 flex-wrap">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="!w-64"
          @change="updateCharts"
        />
        <el-select v-model="filters.organization" placeholder="采购组织" class="w-32" clearable @change="updateCharts">
          <el-option v-for="org in availableOrgs" :key="org" :label="org" :value="org" />
        </el-select>
        <el-select v-model="filters.category" placeholder="标书类别" class="w-32" clearable @change="updateCharts">
          <el-option label="医疗器械" value="medical" />
          <el-option label="IT设备" value="it" />
          <el-option label="建筑工程" value="construction" />
          <el-option label="办公用品" value="office" />
          <el-option label="咨询服务" value="consulting" />
        </el-select>
        <el-button plain @click="handleReset">
          <el-icon class="mr-1"><RefreshRight /></el-icon> 重置
        </el-button>
      </div>
    </div>

    <!-- 2. 核心统计卡片 (含同比环比) -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        <div class="flex items-center gap-3 mt-2 text-xs">
          <span v-if="stat.yoy !== undefined">
            <span class="text-slate-400">同比</span>
            <span :class="stat.yoy >= 0 ? 'text-red-500' : 'text-green-500'">
              {{ stat.yoy >= 0 ? '↑' : '↓' }} {{ Math.abs(stat.yoy) }}%
            </span>
          </span>
          <span v-if="stat.mom !== undefined">
            <span class="text-slate-400">环比</span>
            <span :class="stat.mom >= 0 ? 'text-red-500' : 'text-green-500'">
              {{ stat.mom >= 0 ? '↑' : '↓' }} {{ Math.abs(stat.mom) }}%
            </span>
          </span>
        </div>
      </div>
    </div>

    <!-- 3. 图表区域：发布趋势 & 占比分析 -->
    <div v-loading="loading" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="text-base font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-blue-500"><TrendCharts /></el-icon>
          标书发布趋势 ({{ trendMode === 'month' ? '月度' : '日度' }})
        </h3>
        <div v-if="filteredTenders.length" ref="trendChartRef" class="w-full h-80"></div>
        <div v-else class="h-80 flex items-center justify-center text-slate-400 text-sm">暂无趋势数据</div>
      </div>

      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="text-base font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon>
          标书类别占比分布
        </h3>
        <div v-if="filteredTenders.length" ref="pieChartRef" class="w-full h-80"></div>
        <div v-else class="h-80 flex items-center justify-center text-slate-400 text-sm">暂无分类数据</div>
      </div>
    </div>

    <!-- 4. 详细数据表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="text-base font-bold text-slate-800 mb-4">采购组织发布排行</h3>
      <el-table :data="orgDistribution" stripe border :header-cell-style="{background:'#f8fafc'}">
        <el-table-column type="index" label="排名" width="80" align="center">
          <template #default="scope">
            <div v-if="scope.$index < 3" class="flex justify-center">
              <span class="w-6 h-6 rounded-full bg-slate-800 text-white flex items-center justify-center text-xs font-bold">
                {{ scope.$index + 1 }}
              </span>
            </div>
            <span v-else>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="organization" label="采购组织" min-width="200" />
        <el-table-column label="发布数量" width="150" align="right">
          <template #default="scope">
            <span class="font-bold text-slate-700">{{ scope.row.count }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="percentage" label="占比" width="120">
          <template #default="scope">
            <span class="text-slate-500">{{ scope.row.percentage }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="贡献度" min-width="250">
          <template #default="scope">
            <el-progress :percentage="Number(scope.row.percentage)" :stroke-width="8" :color="getOrgColor(scope.$index)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

  </div>
</template>

<script setup lang="ts">
import {
  Files, TrendCharts, PieChart, DocumentCopy,
  Download, RefreshRight
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const trendMode = ref<'month' | 'day'>('month')
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
const loading = ref(false)

const filters = ref({
  dateRange: [] as string[],
  organization: '',
  category: ''
})

interface TenderRecord { organization: string; category: string; publishDate: string }

const allTenders = ref<TenderRecord[]>([])

const loadAnalysisData = async () => {
  loading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/tenders/analysis')
    allTenders.value = Array.isArray(res?.data) ? res.data : []
  } catch (e: any) {
    allTenders.value = []
    ElMessage.error(e?.message || '加载标书分析数据失败')
  } finally {
    loading.value = false
  }
}

const filteredTenders = computed(() => {
  return allTenders.value.filter(item => {
    if (filters.value.organization && item.organization !== filters.value.organization) return false
    if (filters.value.category && item.category !== filters.value.category) return false
    if (filters.value.dateRange?.length === 2) {
      const [start, end] = filters.value.dateRange
      if (item.publishDate < start || item.publishDate > end) return false
    }
    return true
  })
})

const calcGrowth = (current: number, previous: number) => {
  if (previous === 0) return current > 0 ? 100 : 0
  return Math.round(((current - previous) / previous) * 100)
}

const stats = computed(() => {
  const list = filteredTenders.value
  const total = list.length
  const now = new Date()

  const fmt = (d: Date) => d.toISOString().slice(0, 7)
  const currentMonth = fmt(now)
  const prev1 = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  const lastMonth = fmt(prev1)
  const prev12 = new Date(now.getFullYear() - 1, now.getMonth(), 1)
  const sameMonthLastYear = fmt(prev12)

  const thisMonthCount = list.filter(t => t.publishDate.startsWith(currentMonth)).length
  const lastMonthCount = list.filter(t => t.publishDate.startsWith(lastMonth)).length
  const sameMonthLastYearCount = list.filter(t => t.publishDate.startsWith(sameMonthLastYear)).length

  const mom = calcGrowth(thisMonthCount, lastMonthCount)
  const yoy = calcGrowth(thisMonthCount, sameMonthLastYearCount)

  const uniqueOrgs = new Set(list.map(t => t.organization)).size
  const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
  const avgDaily = thisMonthCount > 0 ? (thisMonthCount / Math.min(now.getDate(), daysInMonth)).toFixed(1) : '0'

  return [
    { label: '累计发布总量', value: total.toLocaleString(), icon: Files, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: '本月新增发布', value: thisMonthCount.toString(), icon: TrendCharts, bgClass: 'bg-blue-50', textClass: 'text-blue-600', yoy, mom },
    { label: '活跃采购组织', value: String(uniqueOrgs), icon: DocumentCopy, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '本月日均发布', value: avgDaily, icon: PieChart, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
  ]
})

const availableOrgs = computed(() => {
  const orgs = new Set(allTenders.value.map(t => t.organization))
  return [...orgs].sort()
})

const orgDistribution = computed(() => {
  const map: Record<string, number> = {}
  filteredTenders.value.forEach(i => map[i.organization] = (map[i.organization] || 0) + 1)
  const total = filteredTenders.value.length || 1
  
  return Object.entries(map)
    .map(([org, count]) => ({
      organization: org,
      count,
      percentage: ((count / total) * 100).toFixed(1)
    }))
    .sort((a, b) => b.count - a.count)
})

const updateCharts = () => {
  if (!trendChart || !pieChart) return
  const list = filteredTenders.value

  if (!list.length) {
    trendChart.clear()
    pieChart.clear()
    return
  }

  const dateMap: Record<string, number> = {}
  list.forEach(i => {
    const key = trendMode.value === 'month' ? i.publishDate.slice(0, 7) : i.publishDate
    dateMap[key] = (dateMap[key] || 0) + 1
  })
  
  const sortedKeys = Object.keys(dateMap).sort().slice(trendMode.value === 'month' ? -12 : -30)
  
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: sortedKeys, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      name: '发布数量',
      type: 'line',
      smooth: true,
      data: sortedKeys.map(k => dateMap[k]),
      itemStyle: { color: '#8b5cf6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(139, 92, 246, 0.5)' },
          { offset: 1, color: 'rgba(139, 92, 246, 0)' }
        ])
      }
    }]
  })

  const catMap: Record<string, number> = {}
  list.forEach(i => catMap[i.category] = (catMap[i.category] || 0) + 1)
  
  const nameMap: Record<string, string> = {
    medical: '医疗器械', it: 'IT设备', construction: '建筑工程',
    office: '办公用品', consulting: '咨询服务'
  }

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      name: '标书类别',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
      data: Object.entries(catMap).map(([k, v]) => ({ value: v, name: nameMap[k] || k }))
    }]
  })
}

onMounted(async () => {
  await loadAnalysisData()
  if (trendChartRef.value) trendChart = echarts.init(trendChartRef.value)
  if (pieChartRef.value) pieChart = echarts.init(pieChartRef.value)
  updateCharts()
  window.addEventListener('resize', () => {
    trendChart?.resize()
    pieChart?.resize()
  })
})

onBeforeUnmount(() => {
  trendChart?.dispose()
  pieChart?.dispose()
  trendChart = null
  pieChart = null
})

watch(filteredTenders, () => {
  updateCharts()
})

const handleReset = () => {
  filters.value = { dateRange: [], organization: '', category: '' }
  trendMode.value = 'month'
  updateCharts()
  ElMessage.info('筛选已重置')
}

const handleExport = () => {
  const list = filteredTenders.value
  if (!list.length) return ElMessage.warning('暂无可导出数据')
  const nameMap: Record<string, string> = {
    medical: '医疗器械', it: 'IT设备', construction: '建筑工程',
    office: '办公用品', consulting: '咨询服务'
  }
  const headers = ['采购组织', '标书类别', '发布日期']
  const esc = (v: string) => `"${(v || '').replace(/"/g, '""')}"`
  const rows = list.map(r => [r.organization, nameMap[r.category] || r.category, r.publishDate])
  const csv = [headers.map(esc).join(','), ...rows.map(r => r.map(esc).join(','))].join('\r\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `标书发布统计_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const getOrgColor = (index: number) => {
  const colors = ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  return colors[index % colors.length]
}
</script>
