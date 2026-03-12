<!--
  文件路径: D:\ipp-platform\app\pages\admin\members\analysis.vue
  版本: V4.0 API对接版 (2026-03-12)

  修复: 移除300条本地Mock随机生成器，改为从 /v3/admin/members API 加载真实数据
  统计卡片、饼图、排名、趋势图全部从真实数据 computed 聚合
-->
<template>
  <div class="space-y-6">

    <!-- 1. 顶部筛选与操作 -->
    <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-wrap gap-4 items-center justify-between">
      <div class="flex flex-wrap gap-3 items-center">
        <el-radio-group v-model="timeRange" size="default" @change="handleTimeRangeChange">
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="quarter">本季</el-radio-button>
          <el-radio-button value="year">本年</el-radio-button>
        </el-radio-group>

        <el-input
          v-model="filters.keyword"
          placeholder="关键词（姓名/公司）"
          :prefix-icon="Search"
          class="w-56"
          clearable
          @keyup.enter="loadMembers"
        />

        <el-select v-model="filters.level" placeholder="会员等级" class="w-32" clearable @change="loadMembers">
          <el-option label="普通" value="NORMAL" />
          <el-option label="VIP" value="VIP" />
          <el-option label="SVIP" value="SVIP" />
        </el-select>

        <el-button type="primary" plain :icon="Search" @click="loadMembers">查询</el-button>
        <el-button plain :icon="RefreshRight" @click="handleReset">重置</el-button>
      </div>

      <el-button type="success" plain :icon="Download" @click="handleExport">导出报表</el-button>
    </div>

    <!-- 2. 核心统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div v-for="(stat, i) in currentStats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass" class="text-lg"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800 mb-2">{{ stat.value }}</div>
        <div class="text-xs text-slate-400">{{ stat.subLabel }}</div>
      </div>
    </div>

    <!-- 3. 趋势分析 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex justify-between items-center mb-6">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <el-icon class="text-blue-500"><TrendCharts /></el-icon> 会员入驻趋势
          </h3>
          <el-radio-group v-model="trendDimension" size="small" @change="refreshChart">
            <el-radio-button value="day">按日</el-radio-button>
            <el-radio-button value="month">按月</el-radio-button>
          </el-radio-group>
        </div>
        <div class="h-80">
          <ClientOnly>
            <AppChart :options="trendChartOptions" />
          </ClientOnly>
        </div>
      </div>

      <!-- 4. 占比分布 -->
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon> 会员构成分析
        </h3>

        <el-tabs v-model="compositionTab">
          <!-- 行业分布 -->
          <el-tab-pane label="行业分布" name="industry">
            <div class="h-80 grid grid-cols-12 gap-4 items-center" v-if="compositionTab === 'industry'">
              <div class="col-span-7 h-full relative">
                <ClientOnly><AppChart :options="industryPieOptions" /></ClientOnly>
              </div>
              <div class="col-span-5 h-full content-center pl-2">
                <div class="space-y-1">
                  <div v-for="(row, idx) in industryRank.slice(0, 8)" :key="row.name"
                       class="flex justify-between items-center py-1.5 px-3 rounded hover:bg-slate-50 transition-colors cursor-default">
                    <div class="flex items-center gap-2 overflow-hidden">
                      <span class="w-1.5 h-1.5 rounded-full flex-shrink-0"
                            :class="idx < 3 ? 'bg-purple-500' : 'bg-slate-300'"></span>
                      <span class="text-xs text-slate-600 truncate max-w-[140px]" :title="row.name">{{ row.name }}</span>
                    </div>
                    <span class="text-xs font-bold text-slate-700">{{ row.value }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 等级分布 -->
          <el-tab-pane label="等级分布" name="level">
            <div class="h-80 w-full" v-if="compositionTab === 'level'">
              <ClientOnly><AppChart :options="levelPieOptions" /></ClientOnly>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 5. 订单概览 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h3 class="font-bold text-slate-800">会员等级分布</h3>
        </div>
        <el-table :data="levelDistribution" stripe size="small" :header-cell-style="{background:'#f8fafc'}">
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="level" label="等级" />
          <el-table-column prop="count" label="数量" align="right" />
          <el-table-column prop="percent" label="占比" width="120">
            <template #default="scope">
              <el-progress :percentage="scope.row.percent" :show-text="false" />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col">
        <div class="flex justify-between items-center mb-6">
          <h3 class="font-bold text-slate-800">订单概览</h3>
          <el-button link type="primary" @click="goToOrders">查看详情 ></el-button>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div class="bg-blue-50 p-4 rounded-xl border border-blue-100">
            <div class="text-sm text-blue-600 mb-1">会员总数</div>
            <div class="text-2xl font-bold text-blue-900">{{ totalMembers }}</div>
          </div>
          <div class="bg-green-50 p-4 rounded-xl border border-green-100">
            <div class="text-sm text-green-600 mb-1">VIP/SVIP</div>
            <div class="text-2xl font-bold text-green-900">{{ vipCount + svipCount }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading overlay -->
    <div v-if="loading" class="fixed inset-0 bg-white/50 z-50 flex items-center justify-center">
      <el-icon class="text-4xl animate-spin text-brand-600"><Loading /></el-icon>
    </div>

  </div>
</template>

<script setup lang="ts">
import {
  UserFilled, TrendCharts, PieChart, Trophy,
  Download, Search, RefreshRight, Connection, OfficeBuilding, Loading
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, markRaw, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// 1. 状态与筛选
const timeRange = ref<'month' | 'quarter' | 'year'>('year')
const trendDimension = ref<'day' | 'month'>('month')
const compositionTab = ref<'industry' | 'level'>('industry')
const loading = ref(false)

const filters = ref({
  keyword: '',
  level: ''
})

// 2. 真实数据
const allMembers = ref<any[]>([])

// 3. 从 API 加载会员数据
const loadMembers = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: 1, page_size: 500 }
    if (filters.value.keyword?.trim()) query.keyword = filters.value.keyword.trim()
    if (filters.value.level) query.member_level = filters.value.level

    const res: any = await apiRequest('/v3/admin/members', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allMembers.value = items.map((m: any) => ({
      id: m.id,
      name: m.realName || m.username || `会员_${m.id}`,
      company: m.companyName || m.company_name || '-',
      industry: m.industry || '未分类',
      level: m.memberLevel || m.member_level || 'NORMAL',
      registerDate: (m.createdAt || m.created_at || '').slice(0, 10),
      province: m.province || '',
      city: m.city || ''
    }))
  } catch (e: any) {
    allMembers.value = []
    ElMessage.error(e?.message || '加载会员数据失败')
  } finally {
    loading.value = false
  }
}

// 4. 统计 computed
const totalMembers = computed(() => allMembers.value.length)
const vipCount = computed(() => allMembers.value.filter(m => m.level === 'VIP').length)
const svipCount = computed(() => allMembers.value.filter(m => m.level === 'SVIP').length)

const currentStats = computed(() => {
  const list = allMembers.value
  const total = list.length
  const vip = list.filter(m => m.level === 'VIP').length
  const svip = list.filter(m => m.level === 'SVIP').length
  const normal = list.filter(m => m.level === 'NORMAL').length

  const industryMap: Record<string, number> = {}
  list.forEach(m => { industryMap[m.industry] = (industryMap[m.industry] || 0) + 1 })
  const topIndustryName = Object.entries(industryMap).sort((a, b) => b[1] - a[1])[0]?.[0] || '暂无'

  return [
    { label: '会员总数', value: String(total), subLabel: '平台注册会员', icon: markRaw(UserFilled), bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '普通会员', value: String(normal), subLabel: '免费会员', icon: markRaw(Connection), bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: 'VIP会员', value: String(vip), subLabel: '付费VIP', icon: markRaw(TrendCharts), bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: 'SVIP会员', value: String(svip), subLabel: '高阶会员', icon: markRaw(Trophy), bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: 'Top行业', value: topIndustryName, subLabel: '最多会员行业', icon: markRaw(OfficeBuilding), bgClass: 'bg-slate-100', textClass: 'text-slate-600' }
  ]
})

// 5. 饼图数据
const buildCountMap = (key: string) => {
  const map: Record<string, number> = {}
  allMembers.value.forEach((m: any) => {
    const k = m[key] || '未分类'
    map[k] = (map[k] || 0) + 1
  })
  return map
}

const industryRank = computed(() => {
  const map = buildCountMap('industry')
  return Object.entries(map).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

const levelRank = computed(() => {
  const map = buildCountMap('level')
  return Object.entries(map).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

const levelDistribution = computed(() => {
  const total = allMembers.value.length || 1
  return levelRank.value.map(r => ({
    level: r.name === 'NORMAL' ? '普通会员' : r.name,
    count: r.value,
    percent: Math.round((r.value / total) * 100)
  }))
})

// 6. 趋势数据
const trendData = ref({ labels: [] as string[], values: [] as number[] })
const refreshChart = () => {
  const map: Record<string, number> = {}
  allMembers.value.forEach(m => {
    if (!m.registerDate) return
    const key = trendDimension.value === 'day' ? m.registerDate : m.registerDate.slice(0, 7)
    map[key] = (map[key] || 0) + 1
  })
  const labels = Object.keys(map).sort()
  trendData.value = { labels, values: labels.map(l => map[l]) }
}

watch(allMembers, () => { refreshChart() })

// 7. 图表配置
const pieBase = {
  radius: ['50%', '80%'],
  center: ['50%', '50%'],
  avoidLabelOverlap: false,
  itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
  label: { show: false },
  labelLine: { show: false },
  tooltip: { show: true, confine: true, appendToBody: true },
  emphasis: { scale: true, scaleSize: 8 }
}

const trendChartOptions = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
  xAxis: { type: 'category', data: trendData.value.labels, boundaryGap: false },
  yAxis: { type: 'value' },
  series: [{
    name: '新增会员',
    type: 'line',
    smooth: true,
    data: trendData.value.values,
    itemStyle: { color: '#3b82f6' },
    areaStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(59,130,246,0.3)' },
        { offset: 1, color: 'rgba(59,130,246,0)' }
      ])
    }
  }]
}))

const industryPieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    ...pieBase,
    name: '行业分布',
    type: 'pie',
    data: industryRank.value.length ? industryRank.value.map(i => ({ value: i.value, name: i.name })) : [{ value: 1, name: '无数据' }]
  }]
}))

const levelPieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    ...pieBase,
    name: '等级分布',
    type: 'pie',
    data: levelRank.value.length ? levelRank.value.map(i => ({ value: i.value, name: i.name })) : [{ value: 1, name: '无数据' }]
  }]
}))

// 8. 交互
const handleTimeRangeChange = () => {
  trendDimension.value = timeRange.value === 'month' ? 'day' : 'month'
  refreshChart()
}

const handleReset = () => {
  filters.value = { keyword: '', level: '' }
  timeRange.value = 'year'
  trendDimension.value = 'month'
  loadMembers()
}

const csvEscape = (val: any) => `"${String(val ?? '').replace(/"/g, '""')}"`
const handleExport = () => {
  if (!import.meta.client) return
  if (!allMembers.value.length) return ElMessage.warning('暂无数据可导出')
  const header = ['ID', '姓名', '公司', '行业', '等级', '入驻时间']
  const rows = allMembers.value.map(m => [
    m.id, m.name, m.company, m.industry, m.level, m.registerDate
  ])
  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `会员统计_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(url), 100)
  ElMessage.success('导出成功')
}

const goToOrders = () => navigateTo('/admin/members/orders')

onMounted(() => {
  loadMembers()
})
</script>
