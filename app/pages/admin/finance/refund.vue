<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\finance\refund.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [退款申请] 退款申请列表查看
  2. [退款审核] 审核通过/驳回退款申请（含原因）
  3. [退款统计] 退款金额统计
  4. [退款记录] 历史退款记录查询
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 退款列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">退款申请管理</h3>
            <p class="text-xs text-slate-500 mt-1">审核和处理退款申请</p>
          </div>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon class="mr-2"><Plus /></el-icon> 新增退款申请
          </el-button>
        </div>

        <!-- Tab 切换 -->
        <el-tabs v-model="activeTab" class="mb-4">
          <el-tab-pane name="pending">
            <template #label>
              <span class="flex items-center gap-2">
                待审核
                <el-badge :value="pendingList.length" type="danger" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="approved" label="已通过" />
          <el-tab-pane name="rejected" label="已驳回" />
        </el-tabs>

        <!-- 筛选 -->
        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索订单号/公司名..."
            prefix-icon="Search"
            class="w-64"
            clearable
          />
          <el-select v-model="filters.type" placeholder="退款类型" class="w-32" clearable>
            <el-option label="会员费" value="membership" />
            <el-option label="标书费" value="tender" />
            <el-option label="培训费" value="training" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="resetFilters">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredRefundList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
          <el-table-column prop="refundNo" label="退款编号" width="150">
            <template #default="scope">
              <span class="font-mono text-xs">{{ scope.row.refundNo }}</span>
            </template>
          </el-table-column>

          <el-table-column label="订单信息" min-width="250">
            <template #default="scope">
              <div>
                <div class="font-medium text-slate-800">{{ scope.row.orderNo }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ scope.row.companyName }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="退款类型" width="120">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.type)" size="small">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="退款金额" width="120" align="right">
            <template #default="scope">
              <span class="text-red-600 font-bold">¥{{ scope.row.amount.toLocaleString() }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="reason" label="退款原因" min-width="200" />

          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="applyDate" label="申请时间" width="120" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <div v-if="scope.row.status === 'pending'" class="flex gap-2">
                <el-button link type="success" size="small" @click="handleApprove(scope.row)">
                  <el-icon class="mr-1"><Check /></el-icon> 通过
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(scope.row)">
                  <el-icon class="mr-1"><Close /></el-icon> 驳回
                </el-button>
              </div>
              <div v-else class="flex gap-2">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  查看详情
                </el-button>
                <el-button link type="danger" size="small" @click="handleRemove(scope.row)">
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination background layout="total, prev, pager, next" :total="filteredRefundList.length" :page-size="10" />
        </div>
      </div>
    </div>

    <!-- 新增/编辑 退款 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑退款申请' : '新增退款申请'" width="640px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="订单号" required>
          <el-input v-model="form.orderNo" />
        </el-form-item>
        <el-form-item label="公司名称" required>
          <el-input v-model="form.companyName" />
        </el-form-item>
        <el-form-item label="退款类型" required>
          <el-select v-model="form.type" class="w-full">
            <el-option label="会员费" value="membership" />
            <el-option label="标书费" value="tender" />
            <el-option label="培训费" value="training" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款金额" required>
          <el-input-number v-model="form.amount" :min="0" :step="1" class="!w-full" />
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker v-model="form.applyDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="退款详情" width="560px">
      <div class="space-y-3 text-sm">
        <div><span class="text-slate-500">退款编号：</span>{{ detailRow.refundNo }}</div>
        <div><span class="text-slate-500">订单号：</span>{{ detailRow.orderNo }}</div>
        <div><span class="text-slate-500">公司名称：</span>{{ detailRow.companyName }}</div>
        <div><span class="text-slate-500">类型：</span>{{ getTypeLabel(detailRow.type) }}</div>
        <div><span class="text-slate-500">金额：</span>¥{{ detailRow.amount }}</div>
        <div><span class="text-slate-500">原因：</span>{{ detailRow.reason }}</div>
        <div><span class="text-slate-500">状态：</span>{{ detailRow.statusLabel }}</div>
        <div v-if="detailRow.rejectReason"><span class="text-slate-500">驳回原因：</span>{{ detailRow.rejectReason }}</div>
        <div><span class="text-slate-500">申请时间：</span>{{ detailRow.applyDate }}</div>
        <div v-if="detailRow.processDate"><span class="text-slate-500">处理时间：</span>{{ detailRow.processDate }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { RefreshLeft, Money, TrendCharts, Check, Close, CircleCheck, CircleClose, Plus } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const activeTab = ref('pending')
const filters = ref({ keyword: '', type: '' })

const stats = computed(() => [
  { label: '待审核', value: String(pendingList.value.length), icon: RefreshLeft, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '本月退款', value: '¥12.8K', icon: Money, bgClass: 'bg-red-50', textClass: 'text-red-600' },
  { label: '已通过', value: String(allRefundList.value.filter(x => x.status === 'approved').length), icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '已驳回', value: String(allRefundList.value.filter(x => x.status === 'rejected').length), icon: CircleClose, bgClass: 'bg-slate-50', textClass: 'text-slate-600' }
])

const allRefundList = ref([
  {
    id: 1,
    refundNo: 'REF202601280001',
    orderNo: 'MEM202601150001',
    companyName: '上海宏大医疗',
    type: 'membership',
    amount: 9800,
    reason: '公司业务调整，暂不需要会员服务',
    status: 'pending',
    statusLabel: '待审核',
    applyDate: '2026-01-28'
  },
  {
    id: 2,
    refundNo: 'REF202601270002',
    orderNo: 'TND202601200002',
    companyName: '深圳科技公司',
    type: 'tender',
    amount: 299,
    reason: '标书内容与描述不符',
    status: 'pending',
    statusLabel: '待审核',
    applyDate: '2026-01-27'
  },
  {
    id: 3,
    refundNo: 'REF202601260003',
    orderNo: 'TRN202601100003',
    companyName: '北京贸易集团',
    type: 'training',
    amount: 3999,
    reason: '时间冲突无法参加培训',
    status: 'approved',
    statusLabel: '已通过',
    applyDate: '2026-01-26',
    processDate: '2026-01-27'
  }
])

const pendingList = computed(() => allRefundList.value.filter(item => item.status === 'pending'))
const filteredRefundList = computed(() => {
  let list = allRefundList.value
  if (activeTab.value === 'pending') list = list.filter(item => item.status === 'pending')
  else if (activeTab.value === 'approved') list = list.filter(item => item.status === 'approved')
  else if (activeTab.value === 'rejected') list = list.filter(item => item.status === 'rejected')

  if (filters.value.keyword) {
    list = list.filter(item => item.orderNo.includes(filters.value.keyword) || item.companyName.includes(filters.value.keyword))
  }
  if (filters.value.type) list = list.filter(item => item.type === filters.value.type)
  return list
})

const getTypeTag = (type: string) => {
  const map: Record<string, string> = { membership: 'danger', tender: 'success', training: 'warning' }
  return map[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { membership: '会员费', tender: '标书费', training: '培训费' }
  return map[type] || '未知'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { pending: 'warning', approved: 'success', rejected: 'info' }
  return map[status] || 'info'
}

const handleSearch = () => ElMessage.success('查询完成')
const resetFilters = () => (filters.value = { keyword: '', type: '' })

const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailRow = ref<any>({})

const form = ref({
  id: 0,
  refundNo: '',
  orderNo: '',
  companyName: '',
  type: 'membership',
  amount: 0,
  reason: '',
  status: 'pending',
  statusLabel: '待审核',
  applyDate: ''
})

const openCreateDialog = () => {
  form.value = {
    id: 0,
    refundNo: `REF${Date.now()}`,
    orderNo: '',
    companyName: '',
    type: 'membership',
    amount: 0,
    reason: '',
    status: 'pending',
    statusLabel: '待审核',
    applyDate: new Date().toISOString().split('T')[0]
  }
  dialogVisible.value = true
}

const handleSaveForm = () => {
  if (!form.value.orderNo || !form.value.companyName || !form.value.reason) {
    return ElMessage.warning('请完善必填项')
  }
  if (form.value.id) {
    const idx = allRefundList.value.findIndex(x => x.id === form.value.id)
    if (idx !== -1) allRefundList.value[idx] = { ...form.value }
  } else {
    form.value.id = Date.now()
    allRefundList.value.unshift({ ...form.value })
  }
  dialogVisible.value = false
  ElMessage.success('已保存')
}

const handleViewDetail = (row: any) => {
  detailRow.value = { ...row }
  detailVisible.value = true
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm(`确认批准 ${row.companyName} 的退款申请（¥${row.amount}）吗？`, '退款审核', {
    confirmButtonText: '确认退款',
    type: 'success'
  }).then(() => {
    row.status = 'approved'
    row.statusLabel = '已通过'
    row.processDate = new Date().toISOString().split('T')[0]
    ElMessage.success('退款已批准，将在3-5个工作日内到账')
  })
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回退款', { inputType: 'textarea' }).then(({ value }) => {
    row.status = 'rejected'
    row.statusLabel = '已驳回'
    row.rejectReason = value
    ElMessage.warning('已驳回退款申请')
  })
}

const handleRemove = (row: any) => {
  ElMessageBox.confirm(`确认删除退款记录 ${row.refundNo} 吗？`, '提示', { type: 'warning' }).then(() => {
    allRefundList.value = allRefundList.value.filter(x => x.id !== row.id)
    ElMessage.success('已删除')
  })
}
</script>
