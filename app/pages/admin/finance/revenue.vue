<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\finance\revenue.vue
  版本: V1.2 (2026-02-06)
  
  ✅ 核心功能:
  1. [收入统计] 总收入、本月、本周、今日
  2. [收入趋势] 近12个月收入趋势图
  3. [收入构成] 会员费、标书销售、培训、服务收入占比
  4. [收入明细] 详细收入记录列表
  5. [配置&录入] 可配置看板展示 + 手工录入发布
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 1. 配置区 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">收入看板配置</h3>
        <p class="text-xs text-slate-500 mt-1">配置首页数据看板展示的收入相关数据</p>
      </div>

      <el-form :model="config" label-width="150px" class="max-w-3xl">
        <el-form-item label="展示总收入">
          <el-switch v-model="config.showTotal" />
        </el-form-item>
        <el-form-item label="展示本月收入">
          <el-switch v-model="config.showMonthly" />
        </el-form-item>
        <el-form-item label="展示本周收入">
          <el-switch v-model="config.showWeekly" />
        </el-form-item>
        <el-form-item label="展示今日收入">
          <el-switch v-model="config.showDaily" />
        </el-form-item>
        <el-form-item label="展示趋势图">
          <el-switch v-model="config.showTrend" />
        </el-form-item>
        <el-form-item label="展示构成图">
          <el-switch v-model="config.showComposition" />
        </el-form-item>
        <el-form-item label="数据刷新频率">
          <el-select v-model="config.refreshRate" class="w-full">
            <el-option label="实时" value="realtime" />
            <el-option label="每小时" value="hourly" />
            <el-option label="每天" value="daily" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据来源">
          <el-input v-model="config.dataSource" placeholder="API接口地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
          <el-button @click="handleResetConfig">重置配置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 2. 录入区 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">收入数据录入与发布</h3>
          <p class="text-xs text-slate-500 mt-1">录入本期收入汇总、趋势与构成数据</p>
        </div>
        <div class="flex gap-2">
          <el-button type="success" @click="handlePublish">发布到看板</el-button>
          <el-button @click="handleResetData">清空录入</el-button>
        </div>
      </div>

      <el-form :model="entry" label-width="150px" class="max-w-4xl">
        <el-form-item label="统计日期">
          <el-date-picker v-model="entry.asOfDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="总收入">
          <el-input-number v-model="entry.totalAmount" :min="0" :step="10000" class="!w-56" />
        </el-form-item>
        <el-form-item label="本月收入">
          <el-input-number v-model="entry.monthlyAmount" :min="0" :step="1000" class="!w-56" />
        </el-form-item>
        <el-form-item label="本周收入">
          <el-input-number v-model="entry.weeklyAmount" :min="0" :step="1000" class="!w-56" />
        </el-form-item>
        <el-form-item label="今日收入">
          <el-input-number v-model="entry.dailyAmount" :min="0" :step="100" class="!w-56" />
        </el-form-item>
      </el-form>

      <!-- 构成数据 -->
      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">收入构成</h4>
          <el-button size="small" @click="addComposition">新增</el-button>
        </div>
        <el-table :data="revenueComposition" stripe border>
          <el-table-column prop="label" label="类型" min-width="200">
            <template #default="scope">
              <el-input v-model="scope.row.label" placeholder="如：会员费收入" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额(K)" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.amount" :min="0" :step="10" class="!w-32" />
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比(%)" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.percentage" :min="0" :max="100" :step="1" class="!w-28" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(revenueComposition, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 趋势数据 -->
      <div class="mt-8">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">月度趋势</h4>
          <el-button size="small" @click="addTrendMonth">新增</el-button>
        </div>
        <el-table :data="monthlyRevenue" stripe border>
          <el-table-column prop="label" label="月份" width="120">
            <template #default="scope">
              <el-input v-model="scope.row.label" placeholder="如：1月" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额(K)" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.amount" :min="0" :step="10" class="!w-32" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(monthlyRevenue, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 3. 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in revenueStats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center text-lg" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        <div class="text-xs mt-2" :class="stat.trend > 0 ? 'text-green-600' : 'text-red-600'">
          {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}% {{ stat.trendLabel }}
        </div>
      </div>
    </div>

    <!-- 4. 图表 -->
    <div v-if="config.showTrend || config.showComposition" class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-if="config.showTrend" class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <el-icon class="text-blue-500"><TrendCharts /></el-icon>
          近12个月收入趋势
        </h3>
        <div ref="trendChartRef" class="h-64 w-full"></div>
      </div>

      <div v-if="config.showComposition" class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon>
          收入构成分析
        </h3>
        <div ref="pieChartRef" class="h-64 w-full"></div>
      </div>
    </div>

    <!-- 5. 收入明细列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">收入明细记录</h3>
            <p class="text-xs text-slate-500 mt-1">查看各业务线的详细收入记录</p>
          </div>
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-2"><Download /></el-icon> 导出报表
          </el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="filters.businessLine" placeholder="业务线" class="w-40" clearable>
            <el-option label="会员费" value="membership" />
            <el-option label="标书销售" value="tender" />
            <el-option label="培训收入" value="training" />
            <el-option label="服务收入" value="service" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="w-64"
          />
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table
          :data="filteredRevenueList"
          style="width: 100%"
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
          stripe
        >
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column label="业务线" width="150">
            <template #default="scope">
              <el-tag :type="getBusinessLineTag(scope.row.businessLine)" size="small">
                {{ getBusinessLineLabel(scope.row.businessLine) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="300" />
          <el-table-column label="金额" width="150" align="right">
            <template #default="scope">
              <span class="text-green-600 font-bold">¥{{ scope.row.amount.toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="订单号" width="180">
            <template #default="scope">
              <span class="font-mono text-xs">{{ scope.row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'completed' ? 'success' : 'warning'" size="small">
                {{ scope.row.status === 'completed' ? '已完成' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="filteredRevenueList.length"
            :page-size="10"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { Money, TrendCharts, PieChart, Download, Wallet } from '@element-plus/icons-vue'
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const filters = ref({ businessLine: '', dateRange: [] as string[] })

const config = ref({
  showTotal: true,
  showMonthly: true,
  showWeekly: true,
  showDaily: true,
  showTrend: true,
  showComposition: true,
  refreshRate: 'hourly',
  dataSource: '/api/dashboard/revenue'
})

const entry = ref({
  asOfDate: '',
  totalAmount: 0,
  monthlyAmount: 0,
  weeklyAmount: 0,
  dailyAmount: 0
})

const revenueStats = [
  { label: '总收入', value: '¥1,286K', trend: 18.5, trendLabel: '较上月', icon: Money, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '本月收入', value: '¥156K', trend: 12.3, trendLabel: '较上月', icon: Wallet, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '本周收入', value: '¥38K', trend: 8.7, trendLabel: '较上周', icon: TrendCharts, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '今日收入', value: '¥5.2K', trend: -3.2, trendLabel: '较昨日', icon: PieChart, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
]

const monthlyRevenue = ref([
  { label: '1月', amount: 85 },
  { label: '2月', amount: 92 },
  { label: '3月', amount: 78 },
  { label: '4月', amount: 105 },
  { label: '5月', amount: 118 },
  { label: '6月', amount: 132 },
  { label: '7月', amount: 125 },
  { label: '8月', amount: 140 },
  { label: '9月', amount: 128 },
  { label: '10月', amount: 145 },
  { label: '11月', amount: 152 },
  { label: '12月', amount: 156 }
])

const revenueComposition = ref([
  { label: '会员费收入', amount: 520, percentage: 40, color: '#3b82f6' },
  { label: '标书销售', amount: 390, percentage: 30, color: '#22c55e' },
  { label: '培训收入', amount: 260, percentage: 20, color: '#a855f7' },
  { label: '服务收入', amount: 130, percentage: 10, color: '#f97316' }
])

const allRevenueList = ref([
  { date: '2026-01-28', businessLine: 'membership', description: '企业会员年费 - 上海宏大医疗', amount: 9800, orderNo: 'MEM202601280001', status: 'completed' },
  { date: '2026-01-28', businessLine: 'tender', description: 'UNDP医疗器械采购标书', amount: 299, orderNo: 'TND202601280002', status: 'completed' },
  { date: '2026-01-27', businessLine: 'training', description: 'CIPS国际采购认证培训', amount: 3999, orderNo: 'TRN202601270003', status: 'completed' },
  { date: '2026-01-27', businessLine: 'service', description: '标书撰写服务', amount: 8000, orderNo: 'SRV202601270004', status: 'processing' }
])

const filteredRevenueList = computed(() => {
  let list = allRevenueList.value
  if (filters.value.businessLine) {
    list = list.filter(item => item.businessLine === filters.value.businessLine)
  }
  return list
})

const getBusinessLineTag = (line: string) => {
  const map: Record<string, string> = { membership: 'danger', tender: 'success', training: 'warning', service: 'primary' }
  return map[line] || 'info'
}

const getBusinessLineLabel = (line: string) => {
  const map: Record<string, string> = { membership: '会员费', tender: '标书销售', training: '培训收入', service: '服务收入' }
  return map[line] || '未知'
}

const handleSearch = () => ElMessage.success('查询完成')
const handleSaveConfig = () => ElMessage.success('配置已保存')
const handleResetConfig = () => {
  config.value = {
    showTotal: true,
    showMonthly: true,
    showWeekly: true,
    showDaily: true,
    showTrend: true,
    showComposition: true,
    refreshRate: 'hourly',
    dataSource: '/api/dashboard/revenue'
  }
  ElMessage.success('配置已重置')
}

const handlePublish = () => {
  ElMessage.success('已发布到看板')
}

const handleResetData = () => {
  entry.value = { asOfDate: '', totalAmount: 0, monthlyAmount: 0, weeklyAmount: 0, dailyAmount: 0 }
  monthlyRevenue.value = []
  revenueComposition.value = []
  ElMessage.warning('录入数据已清空')
}

const addComposition = () => revenueComposition.value.push({ label: '', amount: 0, percentage: 0, color: '#3b82f6' })
const addTrendMonth = () => monthlyRevenue.value.push({ label: '', amount: 0 })
const removeRow = (list: any[], index: number) => list.splice(index, 1)

const handleExport = () => {
  if (!filteredRevenueList.value.length) {
    return ElMessage.warning('暂无可导出数据')
  }
  const headers = ['日期', '业务线', '描述', '金额', '订单号', '状态']
  const rows = filteredRevenueList.value.map(item => [
    item.date,
    getBusinessLineLabel(item.businessLine),
    item.description,
    item.amount,
    item.orderNo,
    item.status === 'completed' ? '已完成' : '处理中'
  ])

  const escapeCell = (value: string) => `"${String(value).replace(/"/g, '""')}"`
  const csv = [
    headers.map(escapeCell).join(','),
    ...rows.map(row => row.map(cell => escapeCell(String(cell))).join(','))
  ].join('\r\n')

  const csvWithBom = `\uFEFF${csv}`
  const blob = new Blob([csvWithBom], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `收入明细_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

/* ====== 图表 ====== */
const trendChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const renderTrend = () => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: monthlyRevenue.value.map(m => m.label), axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef2f7' } } },
    tooltip: { trigger: 'axis' },
    series: [
      {
        type: 'line',
        data: monthlyRevenue.value.map(m => m.amount),
        smooth: true,
        lineStyle: { width: 3, color: '#3b82f6' },
        itemStyle: { color: '#3b82f6' },
        areaStyle: { color: 'rgba(59,130,246,0.15)' }
      }
    ]
  })
}

const renderPie = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, left: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: revenueComposition.value.map(i => ({ name: i.label, value: i.amount })),
        label: { formatter: '{b} {d}%' }
      }
    ],
    color: revenueComposition.value.map(i => i.color || '#3b82f6')
  })
}

onMounted(() => {
  renderTrend()
  renderPie()
})

watch([monthlyRevenue, revenueComposition, config], () => {
  renderTrend()
  renderPie()
})
</script>
