<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\system\customer-service.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [留言记录] 留言记录查询
  2. [客服工单] 工单处理、回复、状态流转
  3. [标签管理] 优先级与标签
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="text-sm text-slate-500 mb-2">{{ stat.label }}</div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 留言记录 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">留言记录</h3>
            <p class="text-xs text-slate-500 mt-1">查询客服留言与处理状态</p>
          </div>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input v-model="messageFilters.keyword" placeholder="搜索姓名/手机号..." class="w-64" clearable />
          <el-select v-model="messageFilters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="未处理" value="pending" />
            <el-option label="已处理" value="done" />
          </el-select>
          <el-button type="primary" plain @click="handleMessageSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredMessages" stripe>
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column prop="content" label="留言内容" min-width="300" />
          <el-table-column prop="date" label="时间" width="160" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'" size="small">
                {{ scope.row.status === 'pending' ? '未处理' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 客服工单 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">客服工单</h3>
            <p class="text-xs text-slate-500 mt-1">工单处理、回复与状态流转</p>
          </div>
          <el-button type="primary" @click="openTicketDialog()">新增工单</el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="ticketFilters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="待处理" value="open" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="closed" />
          </el-select>
          <el-select v-model="ticketFilters.priority" placeholder="优先级" class="w-32" clearable>
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
          <el-button type="primary" plain @click="handleTicketSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredTickets" stripe>
          <el-table-column prop="ticketNo" label="工单编号" width="160" />
          <el-table-column prop="title" label="标题" min-width="200" />
          <el-table-column prop="contact" label="联系人" width="140" />
          <el-table-column label="优先级" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.priority === 'high' ? 'danger' : scope.row.priority === 'medium' ? 'warning' : 'info'">
                {{ scope.row.priorityLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'closed' ? 'success' : scope.row.status === 'processing' ? 'warning' : 'info'">
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="提交时间" width="160" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button link type="primary" @click="openTicketDialog(scope.row)">处理</el-button>
              <el-button link type="danger" @click="removeTicket(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 工单弹窗 -->
    <el-dialog v-model="ticketDialogVisible" :title="ticketForm.id ? '处理工单' : '新增工单'" width="640px">
      <el-form :model="ticketForm" label-width="120px">
        <el-form-item label="标题" required>
          <el-input v-model="ticketForm.title" />
        </el-form-item>
        <el-form-item label="联系人" required>
          <el-input v-model="ticketForm.contact" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="ticketForm.priority" class="w-full">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="ticketForm.status" class="w-full">
            <el-option label="待处理" value="open" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" required>
          <el-input v-model="ticketForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="ticketForm.reply" type="textarea" :rows="3" placeholder="填写处理结果或回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ticketDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTicket">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const stats = [
  { label: '今日新增留言', value: '12' },
  { label: '待处理工单', value: '5' },
  { label: '处理中工单', value: '3' },
  { label: '已完成工单', value: '128' }
]

const messages = ref([
  { id: 1, name: '张三', phone: '138****8888', content: '咨询会员服务价格', date: '2026-02-05 10:12', status: 'pending' },
  { id: 2, name: '李四', phone: '139****6666', content: '申请退款流程', date: '2026-02-04 15:40', status: 'done' }
])

const messageFilters = ref({ keyword: '', status: '' })
const filteredMessages = computed(() => {
  let list = messages.value
  if (messageFilters.value.keyword) {
    list = list.filter(m => m.name.includes(messageFilters.value.keyword) || m.phone.includes(messageFilters.value.keyword))
  }
  if (messageFilters.value.status) {
    list = list.filter(m => m.status === messageFilters.value.status)
  }
  return list
})
const handleMessageSearch = () => ElMessage.success('查询完成')

const tickets = ref([
  { id: 1, ticketNo: 'CS20260205001', title: '标书下载异常', contact: '王五', priority: 'high', priorityLabel: '高', status: 'open', statusLabel: '待处理', date: '2026-02-05 09:10', content: '无法下载标书', reply: '' },
  { id: 2, ticketNo: 'CS20260204002', title: '活动报名问题', contact: '赵六', priority: 'medium', priorityLabel: '中', status: 'processing', statusLabel: '处理中', date: '2026-02-04 14:30', content: '报名后未收到确认', reply: '已转运营核查' }
])

const ticketFilters = ref({ status: '', priority: '' })
const filteredTickets = computed(() => {
  let list = tickets.value
  if (ticketFilters.value.status) list = list.filter(t => t.status === ticketFilters.value.status)
  if (ticketFilters.value.priority) list = list.filter(t => t.priority === ticketFilters.value.priority)
  return list
})
const handleTicketSearch = () => ElMessage.success('查询完成')

const ticketDialogVisible = ref(false)
const ticketForm = ref({
  id: 0,
  ticketNo: '',
  title: '',
  contact: '',
  priority: 'medium',
  status: 'open',
  content: '',
  reply: ''
})

const openTicketDialog = (row?: any) => {
  ticketForm.value = row
    ? { ...row }
    : { id: 0, ticketNo: `CS${Date.now()}`, title: '', contact: '', priority: 'medium', status: 'open', content: '', reply: '' }
  ticketDialogVisible.value = true
}

const saveTicket = () => {
  if (!ticketForm.value.title || !ticketForm.value.contact || !ticketForm.value.content) {
    return ElMessage.warning('请完善必填项')
  }
  const payload = { ...ticketForm.value }
  payload.priorityLabel = payload.priority === 'high' ? '高' : payload.priority === 'medium' ? '中' : '低'
  payload.statusLabel = payload.status === 'closed' ? '已完成' : payload.status === 'processing' ? '处理中' : '待处理'
  if (payload.id) {
    const idx = tickets.value.findIndex(t => t.id === payload.id)
    if (idx !== -1) tickets.value[idx] = payload
  } else {
    payload.id = Date.now()
    payload.date = new Date().toISOString().slice(0, 16).replace('T', ' ')
    tickets.value.unshift(payload)
  }
  ticketDialogVisible.value = false
  ElMessage.success('已保存')
}

const removeTicket = (row: any) => {
  ElMessageBox.confirm(`确认删除工单 ${row.ticketNo} 吗？`, '提示', { type: 'warning' }).then(() => {
    tickets.value = tickets.value.filter(t => t.id !== row.id)
    ElMessage.success('已删除')
  })
}
</script>
