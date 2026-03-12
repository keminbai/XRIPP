<!--
  文件路径: D:\ipp-platform\app\pages\admin\finance\refund.vue
  版本: V2.0 API对接版 (2026-03-12)

  修复: 移除Mock退款列表，对接 /v3/admin/orders?order_status=refunding API
-->
<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 退款订单列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">退款订单管理</h3>
            <p class="text-xs text-slate-500 mt-1">查看退款中的订单</p>
          </div>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索订单号..."
            class="w-64"
            clearable
          />
          <el-select v-model="filters.orderStatus" placeholder="状态" class="w-40" clearable @change="loadOrders">
            <el-option label="退款中" value="refunding" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="全部" value="" />
          </el-select>
          <el-button type="primary" plain @click="loadOrders">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}" v-loading="loading">
          <el-table-column label="订单号" width="200">
            <template #default="scope">
              <span class="font-mono text-xs">{{ scope.row.orderNo || scope.row.order_no || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="订单类型" width="120">
            <template #default="scope">
              <el-tag size="small">{{ getTypeLabel(scope.row.orderType || scope.row.order_type) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="描述" min-width="200">
            <template #default="scope">
              {{ scope.row.title || scope.row.bizType || scope.row.biz_type || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="金额" width="120" align="right">
            <template #default="scope">
              <span class="text-red-600 font-bold">¥{{ Number(scope.row.amount || 0).toLocaleString() }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="(scope.row.orderStatus || scope.row.order_status) === 'refunding' ? 'danger' : 'info'" size="small">
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
            background layout="total, prev, pager, next"
            :total="totalItems" :page-size="pageSize"
            @current-change="loadOrders"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RefreshLeft, Money, TrendCharts, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const loading = ref(false)
const orderList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const filters = ref({ keyword: '', orderStatus: 'refunding' })
const orderStats = ref<any>({})

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { activity: '活动', tender: '标书', service: '服务', report: '报告', other: '其他' }
  return map[type] || type || '未知'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = { refunding: '退款中', cancelled: '已取消', completed: '已完成', pending: '待支付', paid: '已支付' }
  return map[status] || status || '未知'
}

const filteredList = computed(() => {
  if (!filters.value.keyword) return orderList.value
  const kw = filters.value.keyword.toLowerCase()
  return orderList.value.filter(o => {
    const no = (o.orderNo || o.order_no || '').toLowerCase()
    return no.includes(kw)
  })
})

const stats = computed(() => {
  const data = orderStats.value
  const refundingInfo = data.refunding || {}
  const cancelledInfo = data.cancelled || {}

  return [
    { label: '退款中', value: String(refundingInfo.count || 0), icon: RefreshLeft, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '退款金额', value: `¥${Number(refundingInfo.total_amount || refundingInfo.totalAmount || 0).toLocaleString()}`, icon: Money, bgClass: 'bg-red-50', textClass: 'text-red-600' },
    { label: '已取消', value: String(cancelledInfo.count || 0), icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '取消金额', value: `¥${Number(cancelledInfo.total_amount || cancelledInfo.totalAmount || 0).toLocaleString()}`, icon: CircleClose, bgClass: 'bg-slate-50', textClass: 'text-slate-600' }
  ]
})

const loadOrders = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.orderStatus) query.order_status = filters.value.orderStatus

    const res: any = await apiRequest('/v3/admin/orders', { query })
    orderList.value = Array.isArray(res?.data?.items) ? res.data.items : []
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    orderList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '加载退款订单失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res: any = await apiRequest('/v3/admin/orders/stats')
    orderStats.value = res?.data || {}
  } catch { orderStats.value = {} }
}

onMounted(() => {
  loadOrders()
  loadStats()
})
</script>
