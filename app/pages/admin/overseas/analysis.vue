<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\overseas\analysis.vue
  版本: V2.0 (2026-02-06 优化版)
  
  ✅ 核心优化:
  1. [分析聚焦] 保持出海分析与趋势可视化
  2. [数据接入] 预留API对接，当前为Mock数据
  3. [交互优化] 增加加载状态、错误处理、数据更新时间
  4. [UI增强] 统一标签样式，增加导出功能
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <el-alert type="warning" :closable="false" show-icon>
      <template #title>
        <span class="font-bold">功能开发中</span> — 海外服务模块暂无后端API，当前数据仅为界面演示，不会持久化保存。
      </template>
    </el-alert>
    <!-- 顶部统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        <div class="text-xs text-slate-400 mt-1">{{ stat.subLabel }}</div>
      </div>
    </div>

    <!-- 服务网点分布 & 出海服务类型 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4">服务网点分布</h3>
        
        <el-skeleton :loading="loading" :rows="6" animated>
          <div v-if="networkDistribution.length" class="h-56">
            <ClientOnly>
              <AppChart :options="networkChartOptions" />
            </ClientOnly>
          </div>
          <el-empty v-else description="暂无数据" />
          
        </el-skeleton>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4">出海服务类型</h3>
        <el-skeleton :loading="loading" :rows="6" animated>
          <div v-if="serviceTypes.length" class="h-56">
            <ClientOnly>
              <AppChart :options="servicePieOptions" />
            </ClientOnly>
          </div>
          <el-empty v-else description="暂无数据" />
          
        </el-skeleton>
      </div>
    </div>

    <!-- 服务企业增长趋势 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between mb-4">
        <div>
          <h3 class="text-base font-bold text-slate-800">服务企业增长趋势</h3>
          <div v-if="trendSummary.total" class="text-xs text-slate-500 mt-1">
            当前累计 {{ trendSummary.total }} 家 · 本月新增 {{ monthlyNew }} 家
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-radio-group v-model="trendRange" size="small" @change="fetchTrendData">
            <el-radio-button label="3">近3月</el-radio-button>
            <el-radio-button label="6">近6月</el-radio-button>
            <el-radio-button label="12">近12月</el-radio-button>
          </el-radio-group>
          <div v-if="lastUpdateTime" class="text-xs text-slate-500">更新于：{{ lastUpdateTime }}</div>
        </div>
      </div>
      
      <el-skeleton :loading="loading" :rows="8" animated>
        <div class="h-64">
          <ClientOnly>
            <AppChart :options="trendLineOptions" />
          </ClientOnly>
        </div>
      </el-skeleton>
    </div>

    <!-- 出海企业行业分布 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-base font-bold text-slate-800">行业关注分布</h3>
          <div class="text-xs text-slate-400 mt-1">统计口径：行业报告占比</div>
        </div>
        <div class="flex items-center gap-2">
          <el-button
            v-if="industryTableData.length > 5"
            size="small"
            @click="showAllIndustries = !showAllIndustries"
          >
            {{ showAllIndustries ? '收起' : '展开全部' }}
          </el-button>
          <el-button type="success" size="small" @click="exportIndustryData">
            <el-icon class="mr-1"><Download /></el-icon> 导出数据
          </el-button>
        </div>
      </div>
      
      <el-skeleton :loading="loading" :rows="6" animated>
        <div v-if="industryTopData.length" class="h-64 mb-4">
          <ClientOnly>
            <AppChart :options="industryBarOptions" />
          </ClientOnly>
        </div>
        <el-empty v-else description="暂无数据" />

        <el-table v-if="industryDisplayData.length" :data="industryDisplayData" stripe>
          <el-table-column type="index" label="排名" width="80" align="center" />
          <el-table-column prop="industry" label="行业" width="200" />
          <el-table-column label="企业数" width="120">
            <template #default="scope">
              <span class="font-bold text-blue-600">{{ scope.row.count }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比" width="100">
            <template #default="scope">
              <el-tag size="small">{{ scope.row.percentage }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分布图" min-width="300">
            <template #default="scope">
              <div class="flex items-center gap-2">
                <div class="flex-1 bg-slate-100 rounded-full h-2">
                  <div class="bg-blue-500 h-full rounded-full transition-all duration-500" 
                       :style="{ width: `${scope.row.percentage}%` }"></div>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-skeleton>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Promotion, Location, TrendCharts, OfficeBuilding, Download } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

// --- 状态管理 ---
const loading = ref(false)
const lastUpdateTime = ref('')
const trendRange = ref('12')
const showAllIndustries = ref(false)

// 统一数据口径（与 points/services/reports 保持结构一致）
const pointsData = ref([
  {
    id: 1,
    region: 'NorthAmerica',
    country: '美国',
    city: '纽约',
    name: 'XRIPP纽约服务中心',
    address: '350 5th Avenue, New York, NY 10118',
    partnerId: 'PART-NY-001',
    manager: 'John Smith',
    phone: '+1-212-736-3100',
    email: 'newyork@xripp.com',
    services: ['market', 'legal', 'business'],
    description: '为北美地区客户提供全方位出海服务',
    status: 'active',
    establishDate: '2024-05-15',
    createDate: '2025-01-15'
  },
  {
    id: 2,
    region: 'Europe',
    country: '英国',
    city: '伦敦',
    name: 'XRIPP伦敦办事处',
    address: 'The Shard, 32 London Bridge St, London SE1 9SG',
    partnerId: 'PART-LD-002',
    manager: 'David Brown',
    phone: '+44-20-7234-5678',
    email: 'london@xripp.com',
    services: ['market', 'legal', 'business', 'exhibition'],
    description: '欧洲区域总部，覆盖欧盟市场',
    status: 'active',
    establishDate: '2024-08-20',
    createDate: '2025-02-20'
  },
  {
    id: 3,
    region: 'SoutheastAsia',
    country: '新加坡',
    city: '新加坡',
    name: 'XRIPP新加坡中心',
    address: 'Marina Bay Sands, 10 Bayfront Avenue',
    partnerId: 'PART-SG-003',
    manager: 'Lee Wei Ming',
    phone: '+65-6688-8888',
    email: 'singapore@xripp.com',
    services: ['market', 'business', 'logistics', 'registration'],
    description: '东南亚和亚太地区服务枢纽',
    status: 'active',
    establishDate: '2025-01-10',
    createDate: '2025-03-10'
  }
])

const servicesData = ref([
  {
    id: 1,
    type: 'trade',
    project: 'customs',
    title: '美国清关与报关服务',
    countries: ['美国'],
    summary: '提供美国清关、报关与文件处理服务，确保货物合规快速通关',
    content: '详细服务内容...',
    priceType: 'fixed',
    price: 15000,
    duration: '15-20个工作日',
    contactPerson: 'John Smith',
    contactPhone: '+1-212-736-3100',
    contactEmail: 'market@xripp.com',
    promoImage: [],
    introFiles: [],
    views: 256,
    inquiries: 32,
    status: 'published',
    publishDate: '2026-01-20'
  },
  {
    id: 2,
    type: 'ip',
    project: 'legal_service',
    title: '欧盟法律合规咨询',
    countries: ['德国', '法国', '英国'],
    summary: '提供欧盟法律法规咨询，协助企业合规经营',
    content: '详细服务内容...',
    priceType: 'negotiable',
    price: 0,
    duration: '按项目确定',
    contactPerson: 'David Brown',
    contactPhone: '+44-20-7234-5678',
    contactEmail: 'legal@xripp.com',
    promoImage: [],
    introFiles: [],
    views: 189,
    inquiries: 28,
    status: 'published',
    publishDate: '2026-01-18'
  }
])

const reportsData = ref([
  {
    id: 1,
    title: '2025年美国医疗器械市场研究报告',
    country: '美国',
    industry: 'medical',
    type: 'research',
    year: '2025',
    publishDate: '2026-01-20'
  },
  {
    id: 2,
    title: '全球IT行业数字化转型报告',
    country: '全球',
    industry: 'it',
    type: 'industry',
    year: '2025',
    publishDate: '2026-01-18'
  }
])

// 基础数据（来自统一口径）
const trendData = ref<any[]>([])

const uniqueCountries = computed(() => new Set(pointsData.value.map(item => item.country)).size)

// --- 计算属性 ---
const networkDistribution = computed(() => {
  const counter = new Map<string, number>()
  pointsData.value.forEach(item => {
    counter.set(item.country, (counter.get(item.country) || 0) + 1)
  })
  return Array.from(counter.entries()).map(([country, count]) => ({ country, count }))
})

const serviceTypePalette: Record<string, { label: string; color: string }> = {
  trade: { label: '外贸服务', color: '#3b82f6' },
  ip: { label: '知识产权', color: '#10b981' },
  cross: { label: '跨境企服', color: '#8b5cf6' },
  invest: { label: '出海投资', color: '#f59e0b' },
  other: { label: '其他服务', color: '#64748b' }
}

const getIndustryLabel = (industry: string) => {
  const map: Record<string, string> = {
    medical: '医疗健康',
    it: '信息技术',
    manufacturing: '制造业',
    finance: '金融服务',
    consumer: '消费品',
    energy: '能源环保',
    agriculture: '农业',
    comprehensive: '综合'
  }
  return map[industry] || industry
}

const serviceTypes = computed(() => {
  const counter = new Map<string, number>()
  servicesData.value.forEach(item => {
    counter.set(item.type, (counter.get(item.type) || 0) + 1)
  })
  return Array.from(counter.entries())
    .map(([type, count]) => ({
      label: serviceTypePalette[type]?.label || type,
      count,
      color: serviceTypePalette[type]?.color || '#94a3b8'
    }))
    .sort((a, b) => b.count - a.count)
})

const industryRaw = computed(() => {
  const counter = new Map<string, number>()
  reportsData.value.forEach(item => {
    const label = getIndustryLabel(item.industry)
    counter.set(label, (counter.get(label) || 0) + 1)
  })
  return Array.from(counter.entries()).map(([industry, count]) => ({ industry, count }))
})

const totalPoints = computed(() => pointsData.value.length)

const coveredCountries = computed(() => uniqueCountries.value)

const totalCompanies = computed(() => {
  const emails = new Set(servicesData.value.map(item => item.contactEmail).filter(Boolean))
  return emails.size || servicesData.value.length
})

const monthlyNew = computed(() => {
  return trendData.value.length > 0 
    ? trendData.value[trendData.value.length - 1]?.newCount || 0
    : 0
})

const trendSummary = computed(() => {
  const last = trendData.value[trendData.value.length - 1]
  return {
    total: last?.total || 0,
    newCount: last?.newCount || 0
  }
})

const stats = computed(() => [
  { 
    label: '服务网点', 
    value: String(totalPoints.value), 
    subLabel: '当前运营网点', 
    icon: Location, 
    bgClass: 'bg-blue-50', 
    textClass: 'text-blue-600' 
  },
  { 
    label: '服务企业', 
    value: String(totalCompanies.value), 
    subLabel: '累计服务企业', 
    icon: OfficeBuilding, 
    bgClass: 'bg-green-50', 
    textClass: 'text-green-600' 
  },
  { 
    label: '覆盖国家', 
    value: String(coveredCountries.value), 
    subLabel: '重点市场覆盖', 
    icon: Promotion, 
    bgClass: 'bg-purple-50', 
    textClass: 'text-purple-600' 
  }
])

const industryTableData = computed(() => {
  const total = industryRaw.value.reduce((sum, item) => sum + item.count, 0) || 1
  return industryRaw.value.map(item => ({
    ...item,
    percentage: Number(((item.count / total) * 100).toFixed(1))
  }))
})

const industryTopData = computed(() => industryTableData.value.slice(0, 5))
const industryDisplayData = computed(() => (
  showAllIndustries.value ? industryTableData.value : industryTopData.value
))

// --- 图表配置 ---
const networkChartOptions = computed(() => ({
  grid: { top: 10, left: 10, right: 10, bottom: 0, containLabel: true },
  xAxis: {
    type: 'value',
    axisLabel: { color: '#94a3b8' },
    splitLine: { lineStyle: { color: '#f1f5f9' } }
  },
  yAxis: {
    type: 'category',
    data: networkDistribution.value.map(item => item.country),
    axisLabel: { color: '#64748b' }
  },
  series: [{
    type: 'bar',
    data: networkDistribution.value.map(item => item.count),
    barWidth: 12,
    itemStyle: { color: '#3b82f6', borderRadius: [6, 6, 6, 6] }
  }]
}))

const servicePieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
  color: serviceTypes.value.map(item => item.color),
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['50%', '45%'],
    label: { show: false },
    labelLine: { show: false },
    data: serviceTypes.value.map(item => ({ name: item.label, value: item.count }))
  }]
}))

const industryBarOptions = computed(() => ({
  grid: { top: 10, left: 10, right: 20, bottom: 0, containLabel: true },
  xAxis: {
    type: 'value',
    axisLabel: { color: '#94a3b8' },
    splitLine: { lineStyle: { color: '#f1f5f9' } }
  },
  yAxis: {
    type: 'category',
    data: industryTopData.value.map(item => item.industry),
    axisLabel: { color: '#64748b' }
  },
  series: [{
    type: 'bar',
    data: industryTopData.value.map(item => item.count),
    barWidth: 14,
    itemStyle: { color: '#60a5fa', borderRadius: [6, 6, 6, 6] },
    label: {
      show: true,
      position: 'right',
      color: '#475569',
      formatter: (params: any) => `${params.value}家`
    }
  }]
}))

const trendLineOptions = computed(() => {
  const labels = trendData.value.map(item => item.month)
  const values = trendData.value.map(item => item.total)
  const newValues = trendData.value.map(item => item.newCount)

  return {
    tooltip: { trigger: 'axis' },
    grid: { top: 20, left: 10, right: 20, bottom: 0, containLabel: true },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#94a3b8' } },
    yAxis: { 
      type: 'value', 
      axisLabel: { color: '#94a3b8' }, 
      splitLine: { lineStyle: { color: '#f1f5f9' } } 
    },
    series: [{
      name: '服务企业数',
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: { color: '#3b82f6' },
      areaStyle: { color: 'rgba(59,130,246,0.12)' }
    }, {
      name: '新增企业',
      type: 'line',
      smooth: true,
      data: newValues,
      itemStyle: { color: '#f59e0b' },
      lineStyle: { width: 2, type: 'dashed' }
    }]
  }
})

// --- API 请求方法 ---
const fetchAllData = async () => {
  loading.value = true
  try {
    // 实际项目中这里应该调用真实API
    // const res = await $fetch('/api/admin/overseas/analysis')
    
    // 模拟API延迟
    await new Promise(resolve => setTimeout(resolve, 600))
    lastUpdateTime.value = new Date().toLocaleString('zh-CN')
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('数据加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

const fetchTrendData = async () => {
  loading.value = true
  try {
    // 实际API: const res = await $fetch(`/api/admin/overseas/trend?range=${trendRange.value}`)
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const months = parseInt(trendRange.value)
    const now = new Date()
    let cumulative = 0
    trendData.value = Array.from({ length: months }).map((_, i) => {
      const date = new Date(now.getFullYear(), now.getMonth() - (months - 1 - i), 1)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
      const newCount = servicesData.value.filter(item => item.publishDate?.startsWith(monthKey)).length
      cumulative += newCount
      return {
        month: monthKey,
        total: cumulative,
        newCount
      }
    })
  } catch (error) {
    ElMessage.error('趋势数据加载失败')
  } finally {
    loading.value = false
  }
}

// --- 操作方法 ---
const exportIndustryData = () => {
  if (!industryTableData.value.length) {
    ElMessage.warning('暂无可导出的行业分布数据')
    return
  }

  const escapeCsv = (value: unknown) => {
    if (value === null || value === undefined) return ''
    let str = String(value).replace(/\u0000/g, '').replace(/\r?\n/g, ' ').trim()
    str = str.replace(/"/g, '""')
    return `"${str}"`
  }

  const headers = ['行业', '企业数', '占比']
  const rows = industryTableData.value.map(item => [
    item.industry,
    item.count,
    `${item.percentage}%`
  ])

  const csvContent = [
    headers.map(escapeCsv).join(','),
    ...rows.map(row => row.map(escapeCsv).join(','))
  ].join('\r\n')

  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `行业分布_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(url), 100)

  ElMessage.success('导出成功')
}

// --- 生命周期 ---
onMounted(() => {
  fetchAllData()
  fetchTrendData()
})
</script>

<style scoped>
/* 如需自定义样式可在此添加 */
</style>
