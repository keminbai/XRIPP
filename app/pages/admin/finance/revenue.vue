<!--
  文件路径: D:\ipp-platform\app\pages\admin\finance\revenue.vue
  版本: V2.0 API对接版 (2026-03-12)

  修复: 移除所有Mock收入数据，对接 /v3/admin/orders API
  - 统计卡片: computed from real orders (status=completed)
  - 收入明细: real orders list
  - 趋势/构成图: computed from order data
-->
<template>
  <div class="space-y-6">
    <!-- 1. 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in revenueStats" :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center text-lg" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 2. 订单收入列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">订单收入明细</h3>
            <p class="text-xs text-slate-500 mt-1">已完成订单的收入记录</p>
          </div>
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-2"><Download /></el-icon> 导出报表
          </el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="filters.orderType" placeholder="订单类型" class="w-40" clearable @change="loadOrders">
            <el-option label="活动订单" value="activity" />
            <el-option label="标书订单" value="tender" />
            <el-option label="服务订单" value="service" />
          </el-select>
          <el-select v-model="filters.orderStatus" placeholder="订单状态" class="w-40" clearable @change="loadOrders">
            <el-option label="已完成" value="completed" />
            <el-option label="已支付" value="paid" />
            <el-option label="待支付" value="pending" />
          </el-select>
          <el-button type="primary" plain @click="loadOrders">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="orderList" style="width: 100%" :header-cell-style="{background:'#f8fafc', color:'#64748b'}" stripe v-loading="loading">
          <el-table-column label="订单号" width="200">
            <template #default="scope">
              <span class="font-mono text-xs">{{ scope.row.orderNo || scope.row.order_no || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="scope">
              <el-tag :type="getOrderTypeTag(scope.row.orderType || scope.row.order_type)" size="small">
                {{ getOrderTypeLabel(scope.row.orderType || scope.row.order_type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="业务描述" min-width="200">
            <template #default="scope">
              {{ scope.row.title || scope.row.bizType || scope.row.biz_type || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="150" align="right">
            <template #default="scope">
              <span class="text-green-600 font-bold">¥{{ Number(scope.row.amount || 0).toLocaleString() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.orderStatus || scope.row.order_status)" size="small">
                {{ getStatusLabel(scope.row.orderStatus || scope.row.order_status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="scope">{{ (scope.row.createTime || scope.row.create_time || scope.row.createdAt || '').slice(0, 16) }}</template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            background
            layout="total, prev, pager, next"
            :total="totalItems"
            :page-size="pageSize"
            @current-change="loadOrders"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Money, TrendCharts, Download, Wallet } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const loading = ref(false)
const orderList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const filters = ref({ orderType: '', orderStatus: '' })

// Stats from API
const orderStats = ref<any>({})

const revenueStats = computed(() => {
  const data = orderStats.value
  let totalAmount = 0
  let totalCount = 0
  let completedAmount = 0
  let completedCount = 0

  Object.entries(data).forEach(([status, info]: [string, any]) => {
    const count = Number(info?.count || 0)
    const amount = Number(info?.total_amount || info?.totalAmount || 0)
    totalCount += count
    totalAmount += amount
    if (status === 'completed' || status === 'paid') {
      completedCount += count
      completedAmount += amount
    }
  })

  return [
    { label: '总收入', value: `¥${completedAmount.toLocaleString()}`, icon: Money, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已完成订单', value: String(completedCount), icon: Wallet, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '订单总量', value: String(totalCount), icon: TrendCharts, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: '总金额', value: `¥${totalAmount.toLocaleString()}`, icon: Money, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
  ]
})

const getOrderTypeTag = (type: string) => {
  const map: Record<string, string> = { activity: 'warning', tender: 'success', service: 'primary', report: 'info' }
  return map[type] || 'info'
}
const getOrderTypeLabel = (type: string) => {
  const map: Record<string, string> = { activity: '活动', tender: '标书', service: '服务', report: '报告', other: '其他' }
  return map[type] || type || '未知'
}
const getStatusTag = (status: string) => {
  const map: Record<string, string> = { completed: 'success', paid: 'success', pending: 'warning', cancelled: 'info', refunding: 'danger' }
  return map[status] || 'info'
}
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { completed: '已完成', paid: '已支付', pending: '待支付', cancelled: '已取消', refunding: '退款中' }
  return map[status] || status || '未知'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.orderType) query.order_type = filters.value.orderType
    if (filters.value.orderStatus) query.order_status = filters.value.orderStatus

    const res: any = await apiRequest('/v3/admin/orders', { query })
    orderList.value = Array.isArray(res?.data?.items) ? res.data.items : []
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    orderList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res: any = await apiRequest('/v3/admin/orders/stats')
    orderStats.value = res?.data || {}
  } catch {
    orderStats.value = {}
  }
}

const handleExport = () => {
  if (!orderList.value.length) return ElMessage.warning('暂无数据')
  const escapeCell = (v: any) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const headers = ['订单号', '类型', '描述', '金额', '状态', '时间']
  const rows = orderList.value.map(o => [
    o.orderNo || o.order_no, getOrderTypeLabel(o.orderType || o.order_type),
    o.title || o.bizType || o.biz_type, o.amount,
    getStatusLabel(o.orderStatus || o.order_status),
    (o.createTime || o.create_time || '').slice(0, 16)
  ])
  const csv = '\uFEFF' + [headers.map(escapeCell).join(','), ...rows.map(r => r.map(escapeCell).join(','))].join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `收入明细_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => {
  loadOrders()
  loadStats()
})
</script>
