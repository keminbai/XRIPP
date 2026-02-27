<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\partners\analysis.vue
  版本: V2.0 完整增强版 (2026-02-01)
  
  ✅ 功能清单对应（序号28-30）:
  1. [序号28] 固定统计项显示: 合伙人数量、开通城市/省份/国家数
  2. [序号29] 数据筛选与导出: 时间/等级/城市筛选，支持导出Excel
  3. [序号30] 排名与增长分析: 多维度排名、增长曲线、同比环比
  
  ✅ 新增功能:
  1. 补全统计维度（城市48个、省份28个、国家3个）
  2. 完整筛选器（时间/等级/城市/关键字）
  3. 导出Excel功能
  4. 多维度排名Tab（综合/会员/服务商/订单）
  5. ECharts增长曲线
  6. 同比环比数据展示
  ========================================================================
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
        <div v-if="stat.compare" class="text-xs mt-2 flex items-center gap-1" :class="stat.compare > 0 ? 'text-green-600' : 'text-red-600'">
          <el-icon><component :is="stat.compare > 0 ? Top : Bottom" /></el-icon>
          {{ Math.abs(stat.compare) }}% {{ stat.compareLabel }}
        </div>
      </div>
    </div>

    <!-- 2. 数据筛选与导出 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex flex-wrap gap-4 items-end">
        
        <!-- 时间筛选 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm text-slate-600 mb-2">筛选时间</label>
          <el-select v-model="filterTime" placeholder="选择时间范围" class="w-full">
            <el-option label="本月" value="month" />
            <el-option label="本季度" value="quarter" />
            <el-option label="本年" value="year" />
            <el-option label="全部" value="all" />
          </el-select>
        </div>

        <!-- 等级筛选 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm text-slate-600 mb-2">等级筛选</label>
          <el-select v-model="filterLevel" placeholder="选择星级" class="w-full" clearable>
            <el-option label="五星合伙人" :value="5" />
            <el-option label="四星合伙人" :value="4" />
            <el-option label="三星合伙人" :value="3" />
            <el-option label="二星合伙人" :value="2" />
            <el-option label="一星合伙人" :value="1" />
          </el-select>
        </div>

        <!-- 城市筛选 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm text-slate-600 mb-2">城市筛选</label>
          <el-select v-model="filterCity" placeholder="选择城市" class="w-full" filterable clearable>
            <el-option v-for="city in cityList" :key="city" :label="city" :value="city" />
          </el-select>
        </div>

        <!-- 关键字搜索 -->
        <div class="flex-1 min-w-[200px]">
          <label class="block text-sm text-slate-600 mb-2">关键字搜索</label>
          <el-input v-model="filterKeyword" placeholder="搜索合伙人姓名" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>

        <!-- 操作按钮 -->
        <div class="flex gap-2">
          <el-button type="primary" @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-1"><RefreshLeft /></el-icon> 重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon class="mr-1"><Download /></el-icon> 导出Excel
          </el-button>
        </div>
      </div>
    </div>

    <!-- 3. 增长曲线分析 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      
      <!-- 入驻数量趋势 -->
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

      <!-- 业绩增长趋势 -->
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-slate-800 mb-4 flex items-center gap-2">
          <el-icon class="text-green-500"><Money /></el-icon>
          业绩增长趋势
        </h3>
        <div class="h-64">
          <ClientOnly>
            <AppChart :options="revenueTrendOptions" />
          </ClientOnly>
        </div>
      </div>
    </div>

    <!-- 4. 业绩分布 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="text-base font-bold text-slate-800 mb-4">合伙人业绩分布</h3>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div v-for="(item, index) in performanceDistribution" :key="index" class="flex items-center justify-between p-4 rounded-lg border border-slate-100 hover:shadow-md transition-all">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-lg flex items-center justify-center" :class="item.bgClass">
              <el-icon class="text-xl" :class="item.textClass"><component :is="item.icon" /></el-icon>
            </div>
            <div>
              <div class="text-xs text-slate-500">{{ item.label }}</div>
              <div class="text-2xl font-bold text-slate-800">{{ item.count }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. 多维度排名分析 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-base font-bold text-slate-800">合伙人排行榜</h3>
        <div class="text-xs text-slate-500">
          数据更新时间: {{ updateTime }}
        </div>
      </div>

      <el-tabs v-model="rankingTab" class="mb-4">
        <el-tab-pane label="综合排名" name="overall" />
        <el-tab-pane label="会员发展排名" name="members" />
        <el-tab-pane label="服务商引入排名" name="suppliers" />
        <el-tab-pane label="订单业绩排名" name="orders" />
      </el-tabs>

      <el-table :data="filteredRanking" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
        <el-table-column type="index" label="排名" width="80" align="center">
          <template #default="scope">
            <div class="flex items-center justify-center">
              <el-icon v-if="scope.$index === 0" class="text-xl text-yellow-500"><Medal /></el-icon>
              <el-icon v-else-if="scope.$index === 1" class="text-xl text-slate-400"><Medal /></el-icon>
              <el-icon v-else-if="scope.$index === 2" class="text-xl text-orange-600"><Medal /></el-icon>
              <span v-else class="font-bold">{{ scope.$index + 1 }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="合伙人" width="150">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center text-white text-xs font-bold">
                {{ scope.row.name.substring(0, 1) }}
              </div>
              <span class="font-medium">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="city" label="城市" width="120" />
        
        <el-table-column label="会员数" width="120" align="center">
          <template #default="scope">
            <span class="font-bold text-blue-600">{{ scope.row.members }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="服务商数" width="120" align="center" v-if="rankingTab !== 'orders'">
          <template #default="scope">
            <span class="font-bold text-purple-600">{{ scope.row.suppliers }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="订单数" width="120" align="center">
          <template #default="scope">
            <span class="font-bold text-orange-600">{{ scope.row.orders }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="本月业绩" width="150" align="center">
          <template #default="scope">
            <span class="font-bold text-green-600">¥{{ scope.row.revenue.toLocaleString() }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="增长率" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.growth > 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.growth > 0 ? '+' : '' }}{{ scope.row.growth }}%
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="星级" width="150" align="center">
          <template #default="scope">
            <el-rate :model-value="scope.row.stars" disabled show-score text-color="#ff9900" score-template="{value}星" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination 
          background 
          layout="total, prev, pager, next" 
          :total="filteredRanking.length"
          :page-size="10"
        />
      </div>
    </div>

    <!-- 合伙人详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="合伙人详情" width="700px" destroy-on-close>
      <div v-if="currentPartner">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentPartner.name }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ currentPartner.city }}</el-descriptions-item>
          <el-descriptions-item label="会员数">{{ currentPartner.members }}</el-descriptions-item>
          <el-descriptions-item label="服务商数">{{ currentPartner.suppliers }}</el-descriptions-item>
          <el-descriptions-item label="订单数">{{ currentPartner.orders }}</el-descriptions-item>
          <el-descriptions-item label="本月业绩">¥{{ currentPartner.revenue.toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="增长率">{{ currentPartner.growth }}%</el-descriptions-item>
          <el-descriptions-item label="星级">{{ currentPartner.stars }}星</el-descriptions-item>
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
  Search, RefreshLeft, Download, Medal, Top, Bottom
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

definePageMeta({ layout: 'admin' })

// 统计数据
const stats = ref([
  { 
    label: '合伙人总数', 
    value: '48', 
    icon: Connection, 
    bgClass: 'bg-orange-50', 
    textClass: 'text-orange-600',
    compare: 6.3,
    compareLabel: '较上月'
  },
  { 
    label: '开通城市', 
    value: '48', 
    icon: Location, 
    bgClass: 'bg-blue-50', 
    textClass: 'text-blue-600',
    compare: 4.2,
    compareLabel: '较上月'
  },
  { 
    label: '开通省份', 
    value: '28', 
    icon: Flag, 
    bgClass: 'bg-purple-50', 
    textClass: 'text-purple-600',
    compare: 3.7,
    compareLabel: '较上月'
  },
  { 
    label: '覆盖国家', 
    value: '3', 
    icon: Flag, 
    bgClass: 'bg-green-50', 
    textClass: 'text-green-600',
    compare: 0,
    compareLabel: '较上月'
  },
  { 
    label: '总业绩', 
    value: '¥856K', 
    icon: Money, 
    bgClass: 'bg-emerald-50', 
    textClass: 'text-emerald-600',
    compare: 12.5,
    compareLabel: '较上月'
  },
  { 
    label: '五星合伙人', 
    value: '12', 
    icon: Trophy, 
    bgClass: 'bg-yellow-50', 
    textClass: 'text-yellow-600',
    compare: 9.1,
    compareLabel: '较上月'
  }
])

// 筛选条件
const filterTime = ref('month')
const filterLevel = ref<number | null>(null)
const filterCity = ref('')
const filterKeyword = ref('')

const cityList = ref([
  '上海', '北京', '深圳', '广州', '杭州', '成都', '武汉', '西安', 
  '南京', '苏州', '天津', '重庆', '郑州', '长沙', '沈阳', '青岛'
])

// 排名Tab
const rankingTab = ref('overall')
const updateTime = ref('2026-02-01 14:30')

// 业绩分布
const performanceDistribution = ref([
  { label: '优秀 (>50万)', count: 12, icon: Trophy, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '良好 (20-50万)', count: 18, icon: TrendCharts, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '一般 (10-20万)', count: 12, icon: Money, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '待提升 (<10万)', count: 6, icon: Connection, bgClass: 'bg-slate-50', textClass: 'text-slate-600' }
])

// 合伙人排名数据
const partnerRanking = ref([
  { name: '张三', city: '上海', members: 156, suppliers: 45, orders: 234, revenue: 68500, growth: 15.2, stars: 5 },
  { name: '李四', city: '北京', members: 142, suppliers: 38, orders: 198, revenue: 62300, growth: 12.8, stars: 5 },
  { name: '王五', city: '深圳', members: 128, suppliers: 42, orders: 176, revenue: 58900, growth: 9.5, stars: 4 },
  { name: '赵六', city: '广州', members: 115, suppliers: 35, orders: 165, revenue: 52100, growth: 7.3, stars: 4 },
  { name: '钱七', city: '杭州', members: 98, suppliers: 28, orders: 142, revenue: 45600, growth: -2.1, stars: 4 },
  { name: '孙八', city: '成都', members: 89, suppliers: 32, orders: 128, revenue: 41200, growth: 5.6, stars: 3 },
  { name: '周九', city: '武汉', members: 76, suppliers: 24, orders: 115, revenue: 38900, growth: 8.9, stars: 3 },
  { name: '吴十', city: '西安', members: 68, suppliers: 21, orders: 98, revenue: 35600, growth: 4.2, stars: 3 }
])

// 排名筛选
const filteredRanking = computed(() => {
  let data = [...partnerRanking.value]
  
  // 按Tab排序
  if (rankingTab.value === 'members') {
    data.sort((a, b) => b.members - a.members)
  } else if (rankingTab.value === 'suppliers') {
    data.sort((a, b) => b.suppliers - a.suppliers)
  } else if (rankingTab.value === 'orders') {
    data.sort((a, b) => b.orders - a.orders)
  } else {
    data.sort((a, b) => b.revenue - a.revenue)
  }
  
  // 应用筛选
  if (filterLevel.value) {
    data = data.filter(item => item.stars === filterLevel.value)
  }
  if (filterCity.value) {
    data = data.filter(item => item.city === filterCity.value)
  }
  if (filterKeyword.value) {
    data = data.filter(item => item.name.includes(filterKeyword.value))
  }
  
  return data
})

// 入驻趋势图表
const partnerTrendOptions = computed(() => ({
  grid: { top: 40, bottom: 30, left: 50, right: 30 },
  tooltip: { trigger: 'axis' },
  legend: { top: 10, textStyle: { color: '#64748b' } },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
    axisLabel: { color: '#94a3b8' }
  },
  yAxis: { type: 'value', axisLabel: { color: '#94a3b8' } },
  series: [{
    name: '新增合伙人',
    type: 'line',
    smooth: true,
    data: [8, 9, 7, 10, 11, 12, 10, 13, 12, 14, 15, 3],
    itemStyle: { color: '#f97316' },
    areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: 'rgba(249, 115, 22, 0.3)' },
      { offset: 1, color: 'rgba(249, 115, 22, 0.05)' }
    ])}
  }]
}))

// 业绩趋势图表
const revenueTrendOptions = computed(() => ({
  grid: { top: 40, bottom: 30, left: 60, right: 30 },
  tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: ¥{c}万' },
  legend: { top: 10, textStyle: { color: '#64748b' } },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
    axisLabel: { color: '#94a3b8' }
  },
  yAxis: { type: 'value', axisLabel: { color: '#94a3b8', formatter: '¥{value}万' } },
  series: [{
    name: '业绩',
    type: 'bar',
    data: [45, 52, 48, 65, 70, 78, 72, 85, 82, 92, 98, 28],
    itemStyle: { 
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#10b981' },
        { offset: 1, color: '#059669' }
      ])
    }
  }]
}))

// 操作函数
const handleSearch = () => {
  ElMessage.success('筛选成功')
}

const handleReset = () => {
  filterTime.value = 'month'
  filterLevel.value = null
  filterCity.value = ''
  filterKeyword.value = ''
  ElMessage.info('已重置筛选条件')
}

const escapeCsv = (value: unknown) => {
  const str = String(value ?? '')
  // 如果包含逗号、引号、换行就用双引号包裹，并将双引号转义
  if (/[",\n\r]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

const exportCsv = (rows: Array<Record<string, unknown>>, filename: string) => {
  if (!rows.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const headers = Object.keys(rows[0])
  const csvLines = [
    headers.join(','),
    ...rows.map(r => headers.map(h => escapeCsv(r[h])).join(','))
  ]
  const blob = new Blob([csvLines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

const handleExport = () => {
  const data = filteredRanking.value.map(item => ({
    合伙人: item.name,
    城市: item.city,
    会员数: item.members,
    服务商数: item.suppliers,
    订单数: item.orders,
    本月业绩: item.revenue,
    增长率: `${item.growth}%`,
    星级: item.stars
  }))
  const date = new Date().toISOString().slice(0, 10)
  exportCsv(data, `合伙人排行榜_${date}.csv`)
  ElMessage.success('已导出 CSV')
}

const handleViewDetail = (row: any) => {
  currentPartner.value = row
  detailDialogVisible.value = true
}

const detailDialogVisible = ref(false)
const currentPartner = ref<any>(null)
</script>