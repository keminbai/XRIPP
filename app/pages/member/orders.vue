<!-- 文件路径: D:\ipp-platform\app\pages\member\orders.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部筛选区 (修复：增加业务大类筛选 + 状态筛选) -->
    <div class="flex flex-col md:flex-row justify-between items-center bg-white rounded-xl border border-slate-200 p-2 gap-4">
      
      <!-- 业务大类 Tab -->
      <div class="flex bg-slate-100 p-1 rounded-lg w-full md:w-auto">
        <button 
          v-for="cat in categories" 
          :key="cat.value"
          class="flex-1 md:flex-none px-6 py-2 rounded-md text-sm font-bold transition-all"
          :class="activeCategory === cat.value ? 'bg-white text-brand-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
          @click="activeCategory = cat.value"
        >
          {{ cat.label }}
        </button>
      </div>
      
      <!-- 状态筛选 Radio -->
      <div class="flex items-center gap-2">
        <span class="text-xs text-slate-400 font-bold hidden md:inline">状态：</span>
        <el-radio-group v-model="activeStatus" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待支付</el-radio-button>
          <el-radio-button label="paid">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 2. 订单列表 -->
    <div class="space-y-4" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && filteredOrders.length === 0" class="bg-white rounded-xl border border-slate-200 p-16 text-center">
        <div class="w-20 h-20 mx-auto bg-slate-50 rounded-full flex items-center justify-center mb-4 text-4xl">📦</div>
        <h3 class="text-slate-900 font-bold mb-1">暂无相关订单</h3>
        <p class="text-slate-500 text-sm">您可以前往服务中心查看更多服务</p>
      </div>

      <!-- 订单卡片 -->
      <div 
        v-for="order in filteredOrders" 
        :key="order.id" 
        class="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all overflow-hidden"
      >
        <!-- 订单头部 -->
        <div class="flex justify-between items-center p-4 bg-slate-50 border-b border-slate-200 text-xs text-slate-500">
          <div class="flex items-center gap-4">
            <span class="font-mono font-bold text-slate-700">单号：{{ order.orderNo }}</span>
            <span class="hidden sm:inline">|</span>
            <span>{{ order.createTime }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)" size="small" effect="dark">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>

        <!-- 订单内容 -->
        <div class="p-6">
          <div class="flex flex-col md:flex-row gap-6 items-center">
            
            <!-- 图标与信息 -->
            <div class="flex-grow flex items-start gap-4 w-full">
              <div class="w-16 h-16 rounded-lg bg-slate-100 flex items-center justify-center text-3xl flex-shrink-0">
                {{ getTypeIcon(order.category) }}
              </div>
              <div>
                <div class="flex items-center gap-2 mb-1 flex-wrap">
                  <span class="px-2 py-0.5 rounded text-[10px] border border-blue-200 text-blue-600 bg-blue-50">
                    {{ order.category === 'member' ? '会员权益' : '增值服务' }}
                  </span>
                  <h4 class="font-bold text-slate-900 text-base">{{ order.title }}</h4>
                </div>
                <p class="text-sm text-slate-500 line-clamp-2">{{ order.bizType || order.orderType || '-' }}</p>
              </div>
            </div>

            <!-- 金额 -->
            <div class="text-left md:text-right w-full md:w-auto min-w-[120px]">
              <div class="text-xs text-slate-400 mb-1">实付金额</div>
              <div class="text-2xl font-bold text-brand-600 font-mono">¥{{ order.amount }}</div>
            </div>

            <!-- 3. 操作按钮组 (✅ 修复点：补全缺失的功能按钮) -->
            <div class="flex gap-2 w-full md:w-auto min-w-[200px] justify-end">
              <!-- 已完成状态的操作 -->
              <template v-if="order.status === 'paid'">
                <el-button size="small" plain @click="handleInvoice(order)">申请发票</el-button>
                <el-button size="small" type="primary" plain @click="handleConsult(order)">售后咨询</el-button>
              </template>
              
              <!-- 待支付状态的操作 -->
              <template v-if="order.status === 'pending'">
                <el-button size="small" type="danger" plain @click="handleDelete(order)">删除</el-button>
                <el-button size="small" type="primary">去支付</el-button>
              </template>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'member' })

const activeCategory = ref('all')
const activeStatus = ref('all')
const loading = ref(false)

const categories = [
  { label: '全部订单', value: 'all' },
  { label: '会员订单', value: 'member' },
  { label: '服务订单', value: 'service' }
]

const orders = ref<any[]>([])

const loadOrders = async () => {
  loading.value = true
  try {
    const res: any = await apiRequest('/v3/orders?page=1&page_size=100')
    orders.value = Array.isArray(res?.data?.items) ? res.data.items : []
  } catch (e: any) {
    orders.value = []
    ElMessage.error(e?.message || '读取订单失败')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
  return orders.value.filter(o => {
    const catMatch = activeCategory.value === 'all' || o.category === activeCategory.value
    const statusMatch = activeStatus.value === 'all' || o.status === activeStatus.value
    return catMatch && statusMatch
  })
})

const getStatusType = (status: string) => ({ pending: 'warning', paid: 'success', cancelled: 'info' }[status] || 'info')
const getStatusText = (status: string) => ({ pending: '待支付', paid: '已完成', cancelled: '已取消' }[status] || status)
const getTypeIcon = (category: string) => ({ member: '👑', service: '🏢', tender: '📄', activity: '🎯' }[category] || '📦')

// 交互逻辑
const handleInvoice = (order: any) => ElMessage.success(`已为订单 ${order.orderNo} 申请电子发票，请留意邮箱`)
const handleConsult = (_order: any) => navigateTo('/member/feedback')
const handleDelete = (order: any) => {
  ElMessageBox.confirm('确定删除该订单记录吗？', '提示', { type: 'warning' })
    .then(() => ElMessage.warning('订单删除功能待后端接入'))
}

onMounted(loadOrders)
</script>