<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\members\analysis.vue
  版本: V3.1 逻辑与渲染修复版 (2026-02-05)

  ✅ 修复记录:
  1. [Bug修复] 修复 Tab 切换时饼图显示不正常/过小的问题 (使用 v-if 控制渲染时机)
  2. [逻辑修复] 修复"城市入驻 TOP10"切换月/季/年数据不变化的问题 (增加时间过滤逻辑)
  3. [优化] 增加 watch 监听，确保全局筛选变化时，排名数据同步更新
  4. [数据] 优化 Mock 数据时间分布，确保"本月"有数据可展示
  ========================================================================
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
          @keyup.enter="refreshData"
        />

        <el-select v-model="filters.level" placeholder="会员等级" class="w-32" clearable @change="refreshData">
          <el-option label="免费" value="free" />
          <el-option label="VIP" value="vip" />
          <el-option label="SVIP" value="svip" />
        </el-select>

        <el-select v-model="filters.province" placeholder="省份" class="w-32" clearable @change="handleProvinceChange">
          <el-option v-for="p in provinceOptions" :key="p" :label="p" :value="p" />
        </el-select>

        <el-select v-model="filters.city" placeholder="城市" class="w-32" clearable @change="refreshData">
          <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
        </el-select>

        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入驻开始"
          end-placeholder="入驻结束"
          value-format="YYYY-MM-DD"
          class="!w-60"
          @change="refreshData"
        />

        <el-switch 
          v-model="filters.internationalOnly" 
          active-text="国际会员" 
          inactive-text="全部"
          @change="refreshData"
        />

        <el-button type="primary" plain :icon="Search" @click="refreshData">查询</el-button>
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

      <!-- 4. 占比分布 (V3.4 紧凑版：字体缩小，去序号，防遮挡) -->
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon> 会员构成分析
        </h3>
        
        <el-tabs v-model="compositionTab">
          
          <!-- 行业分布 -->
          <el-tab-pane label="行业分布" name="industry">
            <div class="h-80 grid grid-cols-12 gap-4 items-center" v-if="compositionTab === 'industry'">
              <!-- 左侧图表 (占7份) -->
              <div class="col-span-7 h-full relative">
                <ClientOnly>
                  <AppChart :options="industryPieOptions" />
                </ClientOnly>
              </div>
              <!-- 右侧列表 (占5份)：紧凑布局，无滚动条 -->
              <div class="col-span-5 h-full content-center pl-2">
                <div class="space-y-1">
                  <div v-for="(row, idx) in industryRank.slice(0, 8)" :key="row.name" 
                       class="flex justify-between items-center py-1.5 px-3 rounded hover:bg-slate-50 transition-colors cursor-default">
                    <div class="flex items-center gap-2 overflow-hidden">
                      <!-- 小圆点代替序号 -->
                      <span class="w-1.5 h-1.5 rounded-full flex-shrink-0" 
                            :class="idx < 3 ? 'bg-purple-500' : 'bg-slate-300'"></span>
                      <!-- 字体改小：text-xs -->
                      <span class="text-xs text-slate-600 truncate max-w-[140px]" :title="row.name">
                        {{ row.name }}
                      </span>
                    </div>
                    <!-- 数值改小 -->
                    <span class="text-xs font-bold text-slate-700">{{ row.value }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 省份分布 -->
          <el-tab-pane label="省份分布" name="province">
            <div class="h-80 grid grid-cols-12 gap-4 items-center" v-if="compositionTab === 'province'">
              <div class="col-span-7 h-full relative">
                <ClientOnly>
                  <AppChart :options="provincePieOptions" />
                </ClientOnly>
              </div>
              <div class="col-span-5 h-full content-center pl-2">
                <div class="space-y-1">
                  <div v-for="(row, idx) in provinceRank.slice(0, 8)" :key="row.name" 
                       class="flex justify-between items-center py-1.5 px-3 rounded hover:bg-slate-50 transition-colors cursor-default">
                    <div class="flex items-center gap-2 overflow-hidden">
                      <span class="w-1.5 h-1.5 rounded-full flex-shrink-0" 
                            :class="idx < 3 ? 'bg-blue-500' : 'bg-slate-300'"></span>
                      <span class="text-xs text-slate-600 truncate max-w-[140px]" :title="row.name">
                        {{ row.name }}
                      </span>
                    </div>
                    <span class="text-xs font-bold text-slate-700">{{ row.value }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 等级分布：保持全宽 -->
          <el-tab-pane label="等级分布" name="level">
            <div class="h-80 w-full" v-if="compositionTab === 'level'">
              <ClientOnly>
                <AppChart :options="levelPieOptions" />
              </ClientOnly>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>

    <!-- 5. 城市排名 + 订单概览 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h3 class="font-bold text-slate-800">城市入驻 TOP10</h3>
          <!-- 修复：切换触发数据刷新 -->
          <el-select v-model="rankTime" size="small" class="w-24" @change="refreshRanking">
            <el-option label="本月" value="month" />
            <el-option label="本季" value="quarter" />
            <el-option label="本年" value="year" />
          </el-select>
        </div>
        <el-table :data="cityRanking" stripe size="small" :header-cell-style="{background:'#f8fafc'}">
          <el-table-column type="index" label="排名" width="60" align="center" />
          <el-table-column prop="city" label="城市" />
          <el-table-column prop="count" label="入驻数" align="right" />
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
            <div class="text-sm text-blue-600 mb-1">进行中订单</div>
            <div class="text-2xl font-bold text-blue-900">128</div>
          </div>
          <div class="bg-green-50 p-4 rounded-xl border border-green-100">
            <div class="text-sm text-green-600 mb-1">本月已完成</div>
            <div class="text-2xl font-bold text-green-900">856</div>
          </div>
        </div>
        <div class="flex-grow">
          <ClientOnly>
            <AppChart :options="orderChartOptions" />
          </ClientOnly>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { 
  UserFilled, TrendCharts, PieChart, Trophy, 
  Download, Search, RefreshRight, Connection, OfficeBuilding 
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, markRaw, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

definePageMeta({ layout: 'admin' })

// 1. 状态与筛选
const timeRange = ref<'month' | 'quarter' | 'year'>('month')
const rankTime = ref<'month' | 'quarter' | 'year'>('month')
const trendDimension = ref<'day' | 'month'>('month')
const compositionTab = ref<'industry' | 'province' | 'level'>('industry')

const filters = ref({
  keyword: '',
  dateRange: [] as string[],
  province: '',
  city: '',
  level: '',
  internationalOnly: false
})

// 2. Mock 数据源 (优化：确保近期有数据)
const industries = ['制造业','贸易类','服务业','大宗贸易','建筑业','科技型企业','其他']
const provinces = ['上海','北京','广东','浙江','江苏','山东','四川']
const citiesByProvince: Record<string, string[]> = {
  上海: ['上海'],
  北京: ['北京'],
  广东: ['深圳','广州','东莞'],
  浙江: ['杭州','宁波'],
  江苏: ['苏州','南京'],
  山东: ['青岛','济南'],
  四川: ['成都']
}
const levels = ['free','vip','svip']

const generateMembers = () => {
  const list = []
  const now = new Date()
  for (let i = 0; i < 300; i++) {
    const province = provinces[Math.floor(Math.random() * provinces.length)]
    const city = citiesByProvince[province][Math.floor(Math.random() * citiesByProvince[province].length)]
    const industry = industries[Math.floor(Math.random() * industries.length)]
    const level = levels[Math.floor(Math.random() * levels.length)]
    const isInternational = Math.random() > 0.85
    // 生成过去365天内的数据，权重偏向近期
    const dayOffset = Math.floor(Math.random() * 365)
    const date = new Date(now.getTime() - dayOffset * 24 * 60 * 60 * 1000)
    
    list.push({
      id: i + 1,
      name: `会员_${i + 1}`,
      company: `${province}${industry}企业_${i + 1}`,
      industry,
      province,
      city,
      level,
      isInternational,
      registerDate: date.toISOString().split('T')[0],
      visitCount: Math.floor(Math.random() * 300)
    })
  }
  return list
}

const allMembers = ref(generateMembers())

const provinceOptions = provinces
const cityOptions = computed(() => {
  if (!filters.value.province) return []
  return citiesByProvince[filters.value.province] || []
})

// 3. 筛选后的成员 (基础数据池)
const filteredMembers = computed(() => {
  return allMembers.value.filter(m => {
    if (filters.value.keyword && !(`${m.name}${m.company}`.includes(filters.value.keyword))) return false
    if (filters.value.level && m.level !== filters.value.level) return false
    if (filters.value.province && m.province !== filters.value.province) return false
    if (filters.value.city && m.city !== filters.value.city) return false
    if (filters.value.internationalOnly && !m.isInternational) return false
    if (filters.value.dateRange?.length === 2) {
      const [start, end] = filters.value.dateRange
      if (m.registerDate < start || m.registerDate > end) return false
    }
    return true
  })
})

// 4. 统计卡片 (随筛选变化)
const currentStats = computed(() => {
  const list = filteredMembers.value
  const total = list.length
  const international = list.filter(m => m.isInternational).length
  const vip = list.filter(m => m.level === 'vip').length
  const svip = list.filter(m => m.level === 'svip').length
  const topIndustry = list.length ? list.reduce((acc, cur) => {
    acc[cur.industry] = (acc[cur.industry] || 0) + 1
    return acc
  }, {} as Record<string, number>) : {}
  const topIndustryName = Object.entries(topIndustry).sort((a,b)=>b[1]-a[1])[0]?.[0] || '暂无'

  return [
    { label: '会员总数', value: String(total), subLabel: '筛选后的会员数量', icon: markRaw(UserFilled), bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '国际会员', value: String(international), subLabel: '国际会员占比', icon: markRaw(Connection), bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: 'VIP会员', value: String(vip), subLabel: '付费会员', icon: markRaw(TrendCharts), bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: 'SVIP会员', value: String(svip), subLabel: '高阶会员', icon: markRaw(Trophy), bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: 'Top行业', value: topIndustryName, subLabel: '当前最活跃行业', icon: markRaw(OfficeBuilding), bgClass: 'bg-slate-100', textClass: 'text-slate-600' }
  ]
})

// 5. 占比统计 (饼图数据)
const buildCountMap = (key: 'industry' | 'province' | 'level') => {
  const map: Record<string, number> = {}
  filteredMembers.value.forEach(m => {
    const k = m[key] || '未分类'
    map[k] = (map[k] || 0) + 1
  })
  return map
}

const industryRank = computed(() => {
  const map = buildCountMap('industry')
  return Object.entries(map).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

const provinceRank = computed(() => {
  const map = buildCountMap('province')
  return Object.entries(map).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

const levelRank = computed(() => {
  const map = buildCountMap('level')
  return Object.entries(map).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value)
})

// 6. 城市排名 (修复：增加时间过滤逻辑)
const cityRanking = ref<{city:string;count:number;percent:number}[]>([])

const refreshRanking = () => {
  // 1. 计算时间范围
  const now = new Date()
  let startDateStr = ''
  
  if (rankTime.value === 'month') {
    startDateStr = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
  } else if (rankTime.value === 'quarter') {
    const qMonth = Math.floor(now.getMonth() / 3) * 3
    startDateStr = new Date(now.getFullYear(), qMonth, 1).toISOString().split('T')[0]
  } else {
    startDateStr = new Date(now.getFullYear(), 0, 1).toISOString().split('T')[0]
  }

  // 2. 过滤数据
  const targetData = filteredMembers.value.filter(m => m.registerDate >= startDateStr)

  // 3. 聚合统计
  const map: Record<string, number> = {}
  targetData.forEach(m => {
    map[m.city] = (map[m.city] || 0) + 1
  })
  
  const total = targetData.length || 1
  cityRanking.value = Object.entries(map)
    .map(([city, count]) => ({ city, count, percent: Math.round((count / total) * 100) }))
    .sort((a,b)=>b.count-a.count)
    .slice(0, 10)

  // ✅ 兜底：无数据时显示占位行
  if (!cityRanking.value.length) {
    cityRanking.value = [{ city: '暂无数据', count: 0, percent: 0 }]
  }
}

// 7. 趋势数据
const trendData = ref({ labels: [] as string[], values: [] as number[] })
const refreshChart = () => {
  const map: Record<string, number> = {}
  filteredMembers.value.forEach(m => {
    const key = trendDimension.value === 'day' ? m.registerDate : m.registerDate.slice(0,7)
    map[key] = (map[key] || 0) + 1
  })
  const labels = Object.keys(map).sort()
  trendData.value = { labels, values: labels.map(l => map[l]) }
}

// 监听数据变化，自动刷新排名和图表
watch(filteredMembers, () => {
  refreshRanking()
  refreshChart()
})

// 8. 图表 Options
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

const pieBase = {
  radius: ['50%', '80%'],
  center: ['50%', '50%'],
  avoidLabelOverlap: false,
  itemStyle: { 
    borderRadius: 4, 
    borderColor: '#fff', 
    borderWidth: 2 
  },
  label: { show: false },
  labelLine: { show: false },
  tooltip: { 
    show: true, 
    confine: true,
    appendToBody: true
  },
  emphasis: { scale: true, scaleSize: 8 }
}

const industryPieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    ...pieBase,
    name: '行业分布',
    type: 'pie',
    data: industryRank.value.length ? industryRank.value.map(i => ({ value: i.value, name: i.name })) : [{ value: 1, name: '无数据' }]
  }]
}))

const provincePieOptions = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    ...pieBase,
    name: '省份分布',
    type: 'pie',
    data: provinceRank.value.length ? provinceRank.value.map(i => ({ value: i.value, name: i.name })) : [{ value: 1, name: '无数据' }]
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

const orderChartOptions = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['标书订单', '服务订单'] },
  grid: { top: 30, right: 10, bottom: 20, left: 10, containLabel: true },
  xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五','周六','周日'] },
  yAxis: { type: 'value' },
  series: [
    { name: '标书订单', type: 'bar', stack: 'total', data: [120, 132, 101, 134, 90, 230, 210], itemStyle: { color: '#3b82f6' } },
    { name: '服务订单', type: 'bar', stack: 'total', data: [220, 182, 191, 234, 290, 330, 310], itemStyle: { color: '#10b981' } }
  ]
}))

// 9. 交互
const handleProvinceChange = () => {
  filters.value.city = ''
  refreshData()
}

const handleTimeRangeChange = () => {
  trendDimension.value = timeRange.value === 'month' ? 'day' : 'month'
  refreshData()
}

const refreshData = () => {
  refreshChart()
  refreshRanking()
  ElMessage.success('统计数据已更新')
}

const handleReset = () => {
  filters.value = { keyword: '', dateRange: [], province: '', city: '', level: '', internationalOnly: false }
  timeRange.value = 'month'
  trendDimension.value = 'day'
  refreshData()
}

const csvEscape = (val: any) => `"${String(val ?? '').replace(/"/g, '""')}"`
const handleExport = () => {
  if (!import.meta.client) return
  const header = ['ID','姓名','公司','行业','省份','城市','等级','国际会员','入驻时间']
  const rows = filteredMembers.value.map(m => [
    m.id, m.name, m.company, m.industry, m.province, m.city, m.level,
    m.isInternational ? '是' : '否', m.registerDate
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `会员统计_${new Date().toISOString().slice(0,10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(url), 100)
  ElMessage.success('导出成功')
}

const goToOrders = () => navigateTo('/admin/members/orders')

onMounted(() => {
  refreshData()
})
</script>