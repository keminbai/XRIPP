<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow"
      >
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center text-lg" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex justify-between items-center mb-6">
          <h3 class="text-base font-bold text-slate-800 flex items-center gap-2">
            <el-icon class="text-blue-500"><TrendCharts /></el-icon>
            标书销售趋势
          </h3>
          <el-radio-group v-model="trendMode" size="small" @change="updateCharts">
            <el-radio-button label="day">按日</el-radio-button>
            <el-radio-button label="month">按月</el-radio-button>
          </el-radio-group>
        </div>
        <div v-if="filteredList.length" ref="trendChartRef" class="w-full h-72"></div>
        <div v-else class="h-72 flex items-center justify-center text-slate-400 text-sm">暂无趋势数据</div>
      </div>

      <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h3 class="text-base font-bold text-slate-800 mb-6 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon>
          销售额占比（按组织）
        </h3>
        <div v-if="filteredList.length" ref="pieChartRef" class="w-full h-72"></div>
        <div v-else class="h-72 flex items-center justify-center text-slate-400 text-sm">暂无占比数据</div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">订单明细管理</h3>
            <p class="text-xs text-slate-500 mt-1">查看购买会员信息、支付状态和下载情况</p>
          </div>
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-2"><Download /></el-icon> 导出报表
          </el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索订单号/标题/买家"
            :prefix-icon="Search"
            class="w-64"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filters.organization" placeholder="采购组织" class="w-40" clearable>
            <el-option v-for="org in orgOptions" :key="org" :label="org" :value="org" />
          </el-select>
          <el-select v-model="filters.paymentStatus" placeholder="支付状态" class="w-32" clearable>
            <el-option label="已支付" value="paid" />
            <el-option label="待支付" value="pending" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            class="w-64"
            clearable
          />
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table
          v-loading="apiLoading"
          :data="pagedList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
          stripe
        >
          <el-table-column prop="orderNo" label="订单号" width="180" fixed>
            <template #default="{ row }">
              <div class="font-mono text-xs font-bold">{{ row.orderNo }}</div>
              <div class="text-xs text-slate-400 mt-0.5">{{ row.orderTime.slice(0, 10) }}</div>
            </template>
          </el-table-column>

          <el-table-column label="标书信息" min-width="280">
            <template #default="{ row }">
              <div class="py-2">
                <div class="font-bold text-slate-800 text-sm mb-1 truncate" :title="row.tenderTitle">
                  {{ row.tenderTitle }}
                </div>
                <div class="flex gap-2">
                  <el-tag size="small" :type="getOrgTag(row.organization)">{{ row.organization }}</el-tag>
                  <span class="text-xs text-slate-500 flex items-center">编号: {{ row.tenderCode }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="买家信息" width="200">
            <template #default="{ row }">
              <div class="text-sm">
                <div class="font-medium text-slate-700 truncate" :title="row.buyerCompany">{{ row.buyerCompany }}</div>
                <div class="text-xs text-slate-500 mt-0.5">{{ row.buyerName }}</div>
                <el-tag size="small" effect="plain" class="mt-1 scale-90 origin-left">{{ row.memberLevel }}</el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="金额" width="120" align="right">
            <template #default="{ row }">
              <div class="font-bold text-orange-600">¥{{ row.amount }}</div>
              <div class="text-xs text-slate-400">{{ row.paymentMethod }}</div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.paymentStatus === 'paid' ? 'success' : 'warning'" effect="dark" size="small">
                {{ row.paymentStatus === 'paid' ? '已支付' : '待支付' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="下载情况" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.downloadCount > 0" type="info" size="small">已下载 {{ row.downloadCount }} 次</el-tag>
              <span v-else class="text-xs text-slate-400">未下载</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">查看详情</el-button>
              <el-button v-if="row.paymentStatus === 'pending'" link type="success" size="small" @click="handleNotify(row)">
                催付
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-between items-center">
          <div class="text-sm text-slate-500">
            总销售额: <span class="font-bold text-orange-600">¥{{ totalAmount.toLocaleString() }}</span>
          </div>
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next, sizes"
            :total="filteredList.length"
            :page-sizes="[10, 20, 50]"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="800px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentOrder.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentOrder.tenderTitle }}</el-descriptions-item>
          <el-descriptions-item label="采购组织">{{ currentOrder.organization }}</el-descriptions-item>
          <el-descriptions-item label="标书编号">{{ currentOrder.tenderCode }}</el-descriptions-item>
          <el-descriptions-item label="买家公司" :span="2">{{ currentOrder.buyerCompany }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentOrder.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.buyerPhone }}</el-descriptions-item>
          <el-descriptions-item label="支付金额"><span class="text-orange-600 font-bold">¥{{ currentOrder.amount }}</span></el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod }}</el-descriptions-item>
          <el-descriptions-item label="下载次数">{{ currentOrder.downloadCount }}</el-descriptions-item>
          <el-descriptions-item label="最后下载">{{ currentOrder.lastDownloadTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ShoppingCart, Money, TrendCharts, User, PieChart, Search, Download } from '@element-plus/icons-vue'
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type Row = {
  id: number
  orderNo: string
  orderTime: string
  tenderTitle: string
  tenderCode: string
  organization: string
  buyerCompany: string
  buyerName: string
  buyerPhone: string
  memberLevel: string
  amount: number
  paymentMethod: string
  paymentStatus: 'paid' | 'pending'
  downloadCount: number
  lastDownloadTime: string | null
}

const trendMode = ref<'day' | 'month'>('day')
const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const apiOrders = ref<Row[]>([])
const apiLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentOrder = ref<Row | null>(null)

const filters = ref({
  keyword: '',
  organization: '',
  paymentStatus: '' as '' | 'paid' | 'pending',
  dateRange: [] as string[]
})

const orgOptions = computed(() => {
  const set = new Set(apiOrders.value.map(i => i.organization).filter(Boolean))
  return Array.from(set)
})

const fmtDateTime = (v: unknown) => {
  if (!v) return ''
  const d = new Date(String(v))
  if (Number.isNaN(d.getTime())) return String(v)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
}

const mapPaymentStatus = (orderStatus: string) => {
  const s = (orderStatus || '').toLowerCase()
  return s === 'paid' || s === 'confirmed' || s === 'in_service' || s === 'completed' ? 'paid' : 'pending'
}

const loadOrders = async () => {
  apiLoading.value = true
  try {
    const res: any = await apiRequest('/v3/orders?page=1&page_size=200')
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    apiOrders.value = items.map((o: any) => ({
      id: Number(o.id),
      orderNo: o.orderNo || `ORD-${o.id}`,
      orderTime: fmtDateTime(o.createdAt),
      tenderTitle: o.bizType ? `订单类型: ${o.bizType}` : '服务订单',
      tenderCode: o.bizId ? String(o.bizId) : '-',
      organization: o.orderType || '-',
      buyerCompany: `用户#${o.userId || '-'}`,
      buyerName: '-',
      buyerPhone: '-',
      memberLevel: 'NORMAL',
      amount: Number(o.amount || 0),
      paymentMethod: o.currencyCode || 'CNY',
      paymentStatus: mapPaymentStatus(o.orderStatus),
      downloadCount: 0,
      lastDownloadTime: null
    }))
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '订单加载失败')
  } finally {
    apiLoading.value = false
  }
}

const filteredList = computed(() => {
  return apiOrders.value.filter(item => {
    if (filters.value.organization && item.organization !== filters.value.organization) return false
    if (filters.value.paymentStatus && item.paymentStatus !== filters.value.paymentStatus) return false
    if (filters.value.dateRange.length === 2) {
      const [start, end] = filters.value.dateRange
      const d = item.orderTime.slice(0, 10)
      if (d < start || d > end) return false
    }
    if (filters.value.keyword) {
      const kw = filters.value.keyword.toLowerCase()
      if (
        !item.orderNo.toLowerCase().includes(kw) &&
        !item.tenderTitle.toLowerCase().includes(kw) &&
        !item.buyerCompany.toLowerCase().includes(kw)
      ) return false
    }
    return true
  })
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const totalAmount = computed(() => filteredList.value.reduce((sum, i) => sum + i.amount, 0))

const stats = computed(() => {
  const list = filteredList.value
  const total = list.length
  const totalSales = list.reduce((sum, i) => sum + i.amount, 0)
  const paidCount = list.filter(i => i.paymentStatus === 'paid').length
  const pendingCount = list.filter(i => i.paymentStatus === 'pending').length

  return [
    { label: '订单总数', value: String(total), icon: ShoppingCart, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '销售总额', value: `¥${totalSales.toLocaleString()}`, icon: Money, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已支付', value: String(paidCount), icon: TrendCharts, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: '待支付', value: String(pendingCount), icon: User, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '客单价', value: `¥${total ? Math.round(totalSales / total) : 0}`, icon: PieChart, bgClass: 'bg-pink-50', textClass: 'text-pink-600' }
  ]
})

const updateCharts = async () => {
  await nextTick()
  if (!import.meta.client || !trendChart || !pieChart) return

  const list = filteredList.value
  if (!list.length) {
    trendChart.clear()
    pieChart.clear()
    return
  }

  const dateMap: Record<string, number> = {}
  for (const i of list) {
    const key = trendMode.value === 'month' ? i.orderTime.slice(0, 7) : i.orderTime.slice(0, 10)
    dateMap[key] = (dateMap[key] || 0) + i.amount
  }
  const keys = Object.keys(dateMap).sort()

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: keys },
    yAxis: { type: 'value' },
    series: [{
      name: '销售额',
      type: 'line',
      smooth: true,
      data: keys.map(k => dateMap[k]),
      itemStyle: { color: '#3b82f6' },
      areaStyle: { opacity: 0.2 }
    }]
  })

  const orgMap: Record<string, number> = {}
  for (const i of list) {
    orgMap[i.organization] = (orgMap[i.organization] || 0) + i.amount
  }

  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      name: '销售额占比',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      data: Object.entries(orgMap).map(([name, value]) => ({ name, value })),
      itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 }
    }]
  })

  trendChart.resize()
  pieChart.resize()
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

onMounted(async () => {
  await loadOrders()
  if (import.meta.client && trendChartRef.value && pieChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    pieChart = echarts.init(pieChartRef.value)
    await updateCharts()
    window.addEventListener('resize', handleResize)
  }
})

onBeforeUnmount(() => {
  if (import.meta.client) window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
  trendChart = null
  pieChart = null
})

watch([filteredList, trendMode], async () => {
  currentPage.value = 1
  await updateCharts()
})

const handleSearch = async () => {
  currentPage.value = 1
  await updateCharts()
  ElMessage.success('查询完成')
}

const handleReset = async () => {
  filters.value = { keyword: '', organization: '', paymentStatus: '', dateRange: [] }
  currentPage.value = 1
  await updateCharts()
  ElMessage.info('筛选已重置')
}

const handleViewDetail = (row: Row) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

const handleNotify = (row: Row) => {
  ElMessage.success(`已向 ${row.buyerCompany} 发送催付通知`)
}

const csvEscape = (val: unknown) => `"${String(val ?? '').replace(/"/g, '""')}"`

const handleExport = () => {
  if (!import.meta.client) return
  const header = ['订单号', '标题', '采购组织', '买家公司', '金额', '支付状态', '下单时间']
  const rows = filteredList.value.map(i => [
    i.orderNo,
    i.tenderTitle,
    i.organization,
    i.buyerCompany,
    i.amount,
    i.paymentStatus === 'paid' ? '已支付' : '待支付',
    i.orderTime
  ])

  const csv = '\uFEFF' + [header.map(csvEscape).join(','), ...rows.map(r => r.map(csvEscape).join(','))].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `订单报表_${new Date().toISOString().slice(0, 10)}.csv`
  a.style.display = 'none'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const getOrgTag = (org: string) => {
  const map: Record<string, string> = {
    UNDP: 'primary',
    WHO: 'success',
    UNGM: 'warning',
    UNICEF: 'danger'
  }
  return map[org] || 'info'
}
</script>
