<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
      <div v-for="item in stats" :key="item.label" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500">{{ item.label }}</div>
        <div class="text-3xl font-bold text-slate-800 mt-3">{{ item.value }}</div>
        <div class="text-xs text-slate-400 mt-2">{{ item.subLabel }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4">服务网点分布</h3>
        <el-skeleton :loading="loading" :rows="6" animated>
          <div v-if="networkDistribution.length" class="h-72">
            <ClientOnly>
              <AppChart :options="networkChartOptions" />
            </ClientOnly>
          </div>
          <el-empty v-else description="暂无网点数据" />
        </el-skeleton>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4">出海服务类型</h3>
        <el-skeleton :loading="loading" :rows="6" animated>
          <div v-if="serviceTypes.length" class="h-72">
            <ClientOnly>
              <AppChart :options="servicePieOptions" />
            </ClientOnly>
          </div>
          <el-empty v-else description="暂无服务数据" />
        </el-skeleton>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between mb-4">
        <div>
          <h3 class="text-base font-bold text-slate-800">服务企业增长趋势</h3>
          <div class="text-xs text-slate-500 mt-1">
            当前累计 {{ summary.totalCompanies }} 家服务企业，本月新增 {{ summary.monthlyNew }} 家
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-radio-group v-model="trendRange" size="small" @change="loadAnalysis">
            <el-radio-button label="3">近3月</el-radio-button>
            <el-radio-button label="6">近6月</el-radio-button>
            <el-radio-button label="12">近12月</el-radio-button>
          </el-radio-group>
          <div v-if="updatedAt" class="text-xs text-slate-500">更新于：{{ updatedAt }}</div>
        </div>
      </div>

      <el-skeleton :loading="loading" :rows="8" animated>
        <div v-if="trendData.length" class="h-72">
          <ClientOnly>
            <AppChart :options="trendLineOptions" />
          </ClientOnly>
        </div>
        <el-empty v-else description="暂无趋势数据" />
      </el-skeleton>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between mb-4">
        <div>
          <h3 class="text-base font-bold text-slate-800">行业关注分布</h3>
          <div class="text-xs text-slate-400 mt-1">统计口径：后台行业报告库占比</div>
        </div>
        <div class="flex items-center gap-2">
          <el-button v-if="industryDistribution.length > 5" size="small" @click="showAllIndustries = !showAllIndustries">
            {{ showAllIndustries ? '收起' : '展开全部' }}
          </el-button>
          <el-button type="success" size="small" @click="exportIndustryData">导出数据</el-button>
        </div>
      </div>

      <el-skeleton :loading="loading" :rows="6" animated>
        <div v-if="industryTopData.length" class="h-72 mb-4">
          <ClientOnly>
            <AppChart :options="industryBarOptions" />
          </ClientOnly>
        </div>
        <el-empty v-else description="暂无行业报告数据" />

        <el-table v-if="industryDisplayData.length" :data="industryDisplayData" stripe>
          <el-table-column type="index" label="排名" width="80" align="center" />
          <el-table-column label="行业" min-width="180">
            <template #default="{ row }">
              {{ OVERSEAS_REPORT_INDUSTRY_LABEL_MAP[row.industry] || row.industry }}
            </template>
          </el-table-column>
          <el-table-column label="报告数" width="100" align="center">
            <template #default="{ row }">
              <span class="font-bold text-blue-600">{{ row.count }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.percentage }}%</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分布图" min-width="300">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <div class="flex-1 bg-slate-100 rounded-full h-2">
                  <div class="bg-blue-500 h-full rounded-full transition-all duration-500" :style="{ width: `${row.percentage}%` }" />
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'
import {
  OVERSEAS_REPORT_INDUSTRY_LABEL_MAP,
  OVERSEAS_SERVICE_TYPE_LABEL_MAP
} from '@/composables/useOverseasAdminCatalog'

definePageMeta({ layout: 'admin' })

type StatItem = {
  label: string
  value: number
  subLabel: string
}

type CountItem = {
  count: number
}

type NetworkItem = CountItem & {
  country: string
}

type ServiceTypeItem = CountItem & {
  type: string
}

type IndustryItem = CountItem & {
  industry: string
  percentage: number
}

type TrendItem = {
  month: string
  newCount: number
  total: number
}

const loading = ref(false)
const updatedAt = ref('')
const trendRange = ref('12')
const showAllIndustries = ref(false)

const stats = ref<StatItem[]>([])
const summary = ref({
  totalPoints: 0,
  totalCompanies: 0,
  coveredCountries: 0,
  totalReports: 0,
  monthlyNew: 0
})
const networkDistribution = ref<NetworkItem[]>([])
const serviceTypes = ref<ServiceTypeItem[]>([])
const industryDistribution = ref<IndustryItem[]>([])
const trendData = ref<TrendItem[]>([])

const serviceTypeColors: Record<string, string> = {
  trade: '#3b82f6',
  ip: '#10b981',
  cross: '#f59e0b',
  invest: '#ef4444',
  other: '#64748b'
}

const industryTopData = computed(() => industryDistribution.value.slice(0, 5))
const industryDisplayData = computed(() => showAllIndustries.value ? industryDistribution.value : industryTopData.value)

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
    barWidth: 14,
    itemStyle: { color: '#3b82f6', borderRadius: [6, 6, 6, 6] }
  }]
}))

const servicePieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#64748b', fontSize: 12 } },
  color: serviceTypes.value.map(item => serviceTypeColors[item.type] || '#94a3b8'),
  series: [{
    type: 'pie',
    radius: ['42%', '68%'],
    center: ['50%', '42%'],
    label: { show: false },
    labelLine: { show: false },
    data: serviceTypes.value.map(item => ({
      name: OVERSEAS_SERVICE_TYPE_LABEL_MAP[item.type] || item.type,
      value: item.count
    }))
  }]
}))

const trendLineOptions = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { top: 20, left: 10, right: 20, bottom: 0, containLabel: true },
  xAxis: {
    type: 'category',
    data: trendData.value.map(item => item.month),
    axisLabel: { color: '#94a3b8' }
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#94a3b8' },
    splitLine: { lineStyle: { color: '#f1f5f9' } }
  },
  series: [
    {
      name: '服务企业数',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => item.total),
      itemStyle: { color: '#3b82f6' },
      areaStyle: { color: 'rgba(59,130,246,0.12)' }
    },
    {
      name: '新增企业',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => item.newCount),
      itemStyle: { color: '#f59e0b' },
      lineStyle: { width: 2, type: 'dashed' }
    }
  ]
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
    data: industryTopData.value.map(item => OVERSEAS_REPORT_INDUSTRY_LABEL_MAP[item.industry] || item.industry),
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
      formatter: (params: any) => `${params.value}份`
    }
  }]
}))

const loadAnalysis = async () => {
  loading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/overseas/analysis', {
      query: { range: trendRange.value }
    })
    stats.value = Array.isArray(res.data?.stats)
      ? res.data.stats.map((item: any) => ({
          label: String(item?.label || ''),
          value: Number(item?.value || 0),
          subLabel: String(item?.subLabel || '')
        }))
      : []
    summary.value = {
      totalPoints: Number(res.data?.summary?.totalPoints || 0),
      totalCompanies: Number(res.data?.summary?.totalCompanies || 0),
      coveredCountries: Number(res.data?.summary?.coveredCountries || 0),
      totalReports: Number(res.data?.summary?.totalReports || 0),
      monthlyNew: Number(res.data?.summary?.monthlyNew || 0)
    }
    networkDistribution.value = Array.isArray(res.data?.networkDistribution) ? res.data.networkDistribution : []
    serviceTypes.value = Array.isArray(res.data?.serviceTypes) ? res.data.serviceTypes : []
    industryDistribution.value = Array.isArray(res.data?.industryDistribution) ? res.data.industryDistribution : []
    trendData.value = Array.isArray(res.data?.trend) ? res.data.trend : []
    updatedAt.value = String(res.data?.updatedAt || '')
  } catch (error: any) {
    ElMessage.error(error?.message || '出海分析数据加载失败')
  } finally {
    loading.value = false
  }
}

const exportIndustryData = () => {
  if (!industryDistribution.value.length) {
    ElMessage.warning('暂无可导出的行业数据')
    return
  }

  const escapeCsv = (value: unknown) => {
    const normalized = String(value ?? '').replace(/\u0000/g, '').replace(/\r?\n/g, ' ').replace(/"/g, '""')
    return `"${normalized}"`
  }

  const rows = [
    ['行业', '报告数', '占比'],
    ...industryDistribution.value.map(item => ([
      OVERSEAS_REPORT_INDUSTRY_LABEL_MAP[item.industry] || item.industry,
      item.count,
      `${item.percentage}%`
    ]))
  ]

  const csv = rows.map(row => row.map(escapeCsv).join(',')).join('\r\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  if (!import.meta.client) return
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `出海行业分布_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(url), 100)
  ElMessage.success('导出成功')
}

onMounted(loadAnalysis)
</script>
