<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        客服系统已接入真实 API，留言记录、工单处理与附件查看都会真实持久化。
      </template>
    </el-alert>

    <div class="grid grid-cols-1 md:grid-cols-5 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm"
      >
        <div class="text-sm text-slate-500 mb-2">{{ stat.label }}</div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4 gap-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">留言记录</h3>
            <p class="text-xs text-slate-500 mt-1">查询客服留言与处理状态</p>
          </div>
          <el-button :loading="messageLoading" @click="loadMessages">刷新</el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input v-model="messageFilters.keyword" placeholder="搜索姓名/手机号/留言内容..." class="w-72" clearable />
          <el-select v-model="messageFilters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="未处理" value="pending" />
            <el-option label="已处理" value="done" />
          </el-select>
          <el-button type="primary" plain :loading="messageLoading" @click="loadMessages">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="messages" stripe v-loading="messageLoading">
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column prop="content" label="留言内容" min-width="320" show-overflow-tooltip />
          <el-table-column prop="date" label="时间" width="180" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'" size="small">
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handledAt" label="处理时间" width="180" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="scope">
              <el-button
                link
                type="primary"
                :disabled="scope.row.status === 'done'"
                @click="openMessageDialog(scope.row)"
              >
                处理
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4 gap-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">客服工单</h3>
            <p class="text-xs text-slate-500 mt-1">工单处理、回复与状态流转</p>
          </div>
          <div class="flex gap-2">
            <el-button :loading="ticketLoading" @click="loadTickets">刷新</el-button>
            <el-button type="primary" @click="openTicketDialog()">新增工单</el-button>
          </div>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="ticketFilters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="待处理" value="open" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="closed" />
          </el-select>
          <el-select v-model="ticketFilters.priority" placeholder="优先级" class="w-32" clearable>
            <el-option label="低" value="low" />
            <el-option label="一般" value="normal" />
            <el-option label="高" value="high" />
            <el-option label="非常紧急" value="urgent" />
          </el-select>
          <el-input v-model="ticketFilters.keyword" placeholder="搜索工单号/标题/联系人..." class="w-72" clearable />
          <el-button type="primary" plain :loading="ticketLoading" @click="loadTickets">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="tickets" stripe v-loading="ticketLoading">
          <el-table-column prop="ticketNo" label="工单编号" width="190" />
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="contact" label="联系人" width="180" show-overflow-tooltip />
          <el-table-column label="优先级" width="120">
            <template #default="scope">
              <el-tag :type="getPriorityTag(scope.row.priority)">
                {{ scope.row.priorityLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTag(scope.row.status)">
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="date" label="提交时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="openTicketDialog(scope.row)">处理</el-button>
              <el-button link type="info" @click="viewTicketDetail(scope.row)">详情</el-button>
              <el-button link type="danger" @click="removeTicket(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="messageDialogVisible" title="处理留言" width="600px">
      <el-form :model="messageForm" label-width="110px">
        <el-form-item label="姓名">
          <el-input v-model="messageForm.name" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="messageForm.phone" disabled />
        </el-form-item>
        <el-form-item label="留言内容">
          <el-input v-model="messageForm.content" type="textarea" :rows="4" disabled />
        </el-form-item>
        <el-form-item label="处理结果">
          <el-select v-model="messageForm.status" class="w-full">
            <el-option label="已处理" value="done" />
            <el-option label="未处理" value="pending" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="messageForm.handleRemark" type="textarea" :rows="3" placeholder="填写处理情况或回访记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="messageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="messageSaving" @click="saveMessageHandle">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ticketDialogVisible" :title="ticketForm.id ? '处理工单' : '新增工单'" width="760px">
      <el-form :model="ticketForm" label-width="110px">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4">
          <el-form-item label="标题" required>
            <el-input v-model="ticketForm.title" />
          </el-form-item>
          <el-form-item label="工单类型" required>
            <el-select v-model="ticketForm.type" class="w-full">
              <el-option label="财务" value="finance" />
              <el-option label="账号" value="account" />
              <el-option label="标书" value="tender" />
              <el-option label="活动" value="activity" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系人" required>
            <el-input v-model="ticketForm.contactName" />
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input v-model="ticketForm.contactPhone" />
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="ticketForm.priority" class="w-full">
              <el-option label="低" value="low" />
              <el-option label="一般" value="normal" />
              <el-option label="高" value="high" />
              <el-option label="非常紧急" value="urgent" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="ticketForm.status" class="w-full">
              <el-option label="待处理" value="open" />
              <el-option label="处理中" value="processing" />
              <el-option label="已完成" value="closed" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="会员ID">
          <el-input-number v-model="ticketForm.userId" class="!w-full" :min="1" :controls="false" placeholder="可选，关联具体会员" />
        </el-form-item>

        <el-form-item label="问题描述" required>
          <el-input v-model="ticketForm.content" type="textarea" :rows="5" />
        </el-form-item>

        <el-form-item label="处理备注">
          <el-input v-model="ticketForm.reply" type="textarea" :rows="4" placeholder="填写处理结果或回复内容" />
        </el-form-item>

        <el-form-item label="附件">
          <div v-if="ticketForm.attachments.length" class="w-full space-y-2">
            <div
              v-for="file in ticketForm.attachments"
              :key="file.id || file.fileUrl"
              class="flex items-center justify-between rounded-lg border border-slate-200 px-4 py-3"
            >
              <div>
                <div class="text-sm font-medium text-slate-800">{{ file.fileName }}</div>
                <div class="text-xs text-slate-500">{{ formatFileMeta(file.fileExt, file.fileSize) }}</div>
              </div>
              <div class="flex gap-2">
                <el-button v-if="isImageFile(file.fileExt)" link type="primary" @click="openFile(file.fileUrl)">预览</el-button>
                <el-button link type="success" @click="openFile(file.fileUrl)">下载</el-button>
              </div>
            </div>
          </div>
          <div v-else class="text-sm text-slate-400">暂无附件</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ticketDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ticketSaving" @click="saveTicket">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="ticketDetailVisible" title="工单详情" width="820px">
      <div v-loading="ticketDetailLoading" class="space-y-5">
        <template v-if="ticketDetail">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">工单编号</div>
              <div class="font-mono text-sm text-slate-800">{{ ticketDetail.ticketNo }}</div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">状态</div>
              <div class="text-sm font-bold text-slate-800">{{ ticketDetail.statusLabel }}</div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">优先级</div>
              <div class="text-sm text-slate-800">{{ ticketDetail.priorityLabel }}</div>
            </div>
          </div>

          <div class="rounded-xl border border-slate-200 p-4">
            <div class="text-sm font-bold text-slate-900 mb-2">会员信息</div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm text-slate-700">
              <div>会员ID：{{ ticketDetail.memberProfile?.userId || '-' }}</div>
              <div>用户名：{{ ticketDetail.memberProfile?.username || '-' }}</div>
              <div>企业名称：{{ ticketDetail.memberProfile?.companyName || '-' }}</div>
              <div>会员等级：{{ ticketDetail.memberProfile?.memberLevel || '-' }}</div>
            </div>
          </div>

          <div class="rounded-xl border border-slate-200 p-4">
            <div class="text-sm font-bold text-slate-900 mb-2">问题描述</div>
            <div class="text-sm text-slate-700 whitespace-pre-wrap">{{ ticketDetail.content || '-' }}</div>
          </div>

          <div class="rounded-xl border border-slate-200 p-4">
            <div class="text-sm font-bold text-slate-900 mb-2">处理备注</div>
            <div class="text-sm text-slate-700 whitespace-pre-wrap">{{ ticketDetail.reply || '暂无处理备注' }}</div>
          </div>

          <div>
            <div class="text-sm font-bold text-slate-900 mb-3">附件</div>
            <div v-if="ticketDetail.attachments?.length" class="space-y-2">
              <div
                v-for="file in ticketDetail.attachments"
                :key="file.id || file.fileUrl"
                class="flex items-center justify-between rounded-xl border border-slate-200 px-4 py-3"
              >
                <div>
                  <div class="text-sm font-medium text-slate-800">{{ file.fileName }}</div>
                  <div class="text-xs text-slate-500">{{ formatFileMeta(file.fileExt, file.fileSize) }}</div>
                </div>
                <div class="flex gap-2">
                  <el-button v-if="isImageFile(file.fileExt)" link type="primary" @click="openFile(file.fileUrl)">预览</el-button>
                  <el-button link type="success" @click="openFile(file.fileUrl)">下载</el-button>
                </div>
              </div>
            </div>
            <div v-else class="rounded-xl border border-dashed border-slate-200 p-4 text-sm text-slate-400">
              暂无附件
            </div>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button @click="ticketDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type MessageItem = {
  id: number
  name: string
  phone: string
  content: string
  date: string
  status: string
  statusLabel: string
  handledAt?: string
  handleRemark?: string
}

type TicketAttachment = {
  id?: number
  fileName: string
  fileUrl: string
  fileExt: string
  fileSize: number
}

type TicketItem = {
  id: number
  ticketNo: string
  title: string
  type: string
  typeLabel: string
  priority: string
  priorityLabel: string
  status: string
  statusLabel: string
  contactName: string
  contactPhone: string
  contact: string
  date: string
  content: string
  reply: string
  attachments: TicketAttachment[]
  userId?: number | null
  memberProfile?: Record<string, any>
}

const messageLoading = ref(false)
const ticketLoading = ref(false)
const messageSaving = ref(false)
const ticketSaving = ref(false)
const ticketDetailLoading = ref(false)

const overview = ref({
  todayMessageCount: 0,
  pendingMessageCount: 0,
  openTicketCount: 0,
  processingTicketCount: 0,
  closedTicketCount: 0
})

const stats = computed(() => ([
  { label: '今日新增留言', value: String(overview.value.todayMessageCount) },
  { label: '待处理留言', value: String(overview.value.pendingMessageCount) },
  { label: '待处理工单', value: String(overview.value.openTicketCount) },
  { label: '处理中工单', value: String(overview.value.processingTicketCount) },
  { label: '已完成工单', value: String(overview.value.closedTicketCount) }
]))

const messages = ref<MessageItem[]>([])
const tickets = ref<TicketItem[]>([])

const messageFilters = ref({ keyword: '', status: '' })
const ticketFilters = ref({ status: '', priority: '', keyword: '' })

const messageDialogVisible = ref(false)
const messageForm = ref({
  id: 0,
  name: '',
  phone: '',
  content: '',
  status: 'done',
  handleRemark: ''
})

const ticketDialogVisible = ref(false)
const ticketDetailVisible = ref(false)
const ticketDetail = ref<TicketItem | null>(null)
const ticketForm = ref({
  id: 0,
  title: '',
  type: 'other',
  priority: 'normal',
  status: 'open',
  contactName: '',
  contactPhone: '',
  userId: undefined as number | undefined,
  content: '',
  reply: '',
  attachments: [] as TicketAttachment[]
})

const normalizeMessages = (items: any[]): MessageItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    name: item?.name || '',
    phone: item?.phone || '',
    content: item?.content || '',
    date: item?.date || '',
    status: item?.status || 'pending',
    statusLabel: item?.statusLabel || (item?.status === 'done' ? '已处理' : '未处理'),
    handledAt: item?.handledAt || '',
    handleRemark: item?.handleRemark || ''
  }))
}

const normalizeTickets = (items: any[]): TicketItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    ticketNo: item?.ticketNo || '',
    title: item?.title || '',
    type: item?.type || 'other',
    typeLabel: item?.typeLabel || getTypeLabel(item?.type || 'other'),
    priority: item?.priority || 'normal',
    priorityLabel: item?.priorityLabel || getPriorityLabel(item?.priority || 'normal'),
    status: item?.status || 'open',
    statusLabel: item?.statusLabel || getStatusLabel(item?.status || 'open'),
    contactName: item?.contactName || '',
    contactPhone: item?.contactPhone || '',
    contact: item?.contact || [item?.contactName, item?.contactPhone].filter(Boolean).join(' / '),
    date: item?.date || '',
    content: item?.content || '',
    reply: item?.reply || '',
    attachments: Array.isArray(item?.attachments) ? item.attachments : [],
    userId: item?.userId,
    memberProfile: item?.memberProfile || {}
  }))
}

const loadOverview = async () => {
  try {
    const res = await apiRequest<any>('/v3/admin/customer-service/overview')
    overview.value = {
      todayMessageCount: Number(res.data?.todayMessageCount || 0),
      pendingMessageCount: Number(res.data?.pendingMessageCount || 0),
      openTicketCount: Number(res.data?.openTicketCount || 0),
      processingTicketCount: Number(res.data?.processingTicketCount || 0),
      closedTicketCount: Number(res.data?.closedTicketCount || 0)
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '客服统计加载失败')
  }
}

const loadMessages = async () => {
  messageLoading.value = true
  try {
    const query = new URLSearchParams({
      page: '1',
      page_size: '50'
    })
    if (messageFilters.value.keyword.trim()) query.set('keyword', messageFilters.value.keyword.trim())
    if (messageFilters.value.status) query.set('status', messageFilters.value.status)
    const res = await apiRequest<any>(`/v3/admin/customer-service/messages?${query.toString()}`)
    messages.value = normalizeMessages(Array.isArray(res.data?.items) ? res.data.items : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '留言记录加载失败')
  } finally {
    messageLoading.value = false
  }
}

const loadTickets = async () => {
  ticketLoading.value = true
  try {
    const query = new URLSearchParams({
      page: '1',
      page_size: '50'
    })
    if (ticketFilters.value.keyword.trim()) query.set('keyword', ticketFilters.value.keyword.trim())
    if (ticketFilters.value.status) query.set('status', ticketFilters.value.status)
    if (ticketFilters.value.priority) query.set('priority', ticketFilters.value.priority)
    const res = await apiRequest<any>(`/v3/admin/customer-service/tickets?${query.toString()}`)
    tickets.value = normalizeTickets(Array.isArray(res.data?.items) ? res.data.items : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '工单列表加载失败')
  } finally {
    ticketLoading.value = false
  }
}

const reloadAll = async () => {
  await Promise.all([loadOverview(), loadMessages(), loadTickets()])
}

const openMessageDialog = (row: MessageItem) => {
  messageForm.value = {
    id: row.id,
    name: row.name,
    phone: row.phone,
    content: row.content,
    status: row.status === 'done' ? 'done' : 'done',
    handleRemark: row.handleRemark || ''
  }
  messageDialogVisible.value = true
}

const saveMessageHandle = async () => {
  if (!messageForm.value.id) return
  messageSaving.value = true
  try {
    await apiRequest(`/v3/admin/customer-service/messages/${messageForm.value.id}/handle`, {
      method: 'POST',
      body: {
        status: messageForm.value.status,
        handle_remark: messageForm.value.handleRemark
      }
    })
    ElMessage.success('留言处理结果已保存')
    messageDialogVisible.value = false
    await Promise.all([loadOverview(), loadMessages()])
  } catch (error: any) {
    ElMessage.error(error?.message || '留言处理失败')
  } finally {
    messageSaving.value = false
  }
}

const openTicketDialog = async (row?: TicketItem) => {
  if (!row) {
    ticketForm.value = {
      id: 0,
      title: '',
      type: 'other',
      priority: 'normal',
      status: 'open',
      contactName: '',
      contactPhone: '',
      userId: undefined,
      content: '',
      reply: '',
      attachments: []
    }
    ticketDialogVisible.value = true
    return
  }

  ticketSaving.value = false
  ticketDetailLoading.value = true
  ticketDialogVisible.value = true
  try {
    const res = await apiRequest<any>(`/v3/admin/customer-service/tickets/${row.id}`)
    const detail = normalizeTickets([res.data])[0]
    ticketForm.value = {
      id: detail.id,
      title: detail.title,
      type: detail.type,
      priority: detail.priority,
      status: detail.status,
      contactName: detail.contactName,
      contactPhone: detail.contactPhone,
      userId: detail.userId ? Number(detail.userId) : undefined,
      content: detail.content,
      reply: detail.reply,
      attachments: detail.attachments || []
    }
  } catch (error: any) {
    ticketDialogVisible.value = false
    ElMessage.error(error?.message || '工单详情加载失败')
  } finally {
    ticketDetailLoading.value = false
  }
}

const viewTicketDetail = async (row: TicketItem) => {
  ticketDetailVisible.value = true
  ticketDetailLoading.value = true
  ticketDetail.value = null
  try {
    const res = await apiRequest<any>(`/v3/admin/customer-service/tickets/${row.id}`)
    ticketDetail.value = normalizeTickets([res.data])[0]
    ticketDetail.value.memberProfile = res.data?.memberProfile || {}
  } catch (error: any) {
    ticketDetailVisible.value = false
    ElMessage.error(error?.message || '工单详情加载失败')
  } finally {
    ticketDetailLoading.value = false
  }
}

const saveTicket = async () => {
  if (!ticketForm.value.title.trim() || !ticketForm.value.contactName.trim() || !ticketForm.value.contactPhone.trim() || !ticketForm.value.content.trim()) {
    ElMessage.warning('请完善标题、联系人、联系电话和问题描述')
    return
  }

  ticketSaving.value = true
  try {
    const body = {
      title: ticketForm.value.title,
      ticket_type: ticketForm.value.type,
      priority: ticketForm.value.priority,
      status: ticketForm.value.status,
      contact_name: ticketForm.value.contactName,
      contact_phone: ticketForm.value.contactPhone,
      user_id: ticketForm.value.userId || undefined,
      content: ticketForm.value.content,
      reply: ticketForm.value.reply,
      attachments: ticketForm.value.attachments
    }

    if (ticketForm.value.id) {
      await apiRequest(`/v3/admin/customer-service/tickets/${ticketForm.value.id}`, {
        method: 'PUT',
        body
      })
      ElMessage.success('工单处理结果已保存')
    } else {
      await apiRequest('/v3/admin/customer-service/tickets', {
        method: 'POST',
        body
      })
      ElMessage.success('新工单已创建')
    }
    ticketDialogVisible.value = false
    await Promise.all([loadOverview(), loadTickets()])
  } catch (error: any) {
    ElMessage.error(error?.message || '工单保存失败')
  } finally {
    ticketSaving.value = false
  }
}

const removeTicket = (row: TicketItem) => {
  ElMessageBox.confirm(`确认删除工单 ${row.ticketNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await apiRequest(`/v3/admin/customer-service/tickets/${row.id}`, {
        method: 'DELETE'
      })
      ElMessage.success('工单已删除')
      await Promise.all([loadOverview(), loadTickets()])
    })
    .catch(() => {})
}

const getPriorityTag = (priority: string) => {
  if (priority === 'urgent' || priority === 'high') return 'danger'
  if (priority === 'normal') return 'warning'
  return 'info'
}

const getStatusTag = (status: string) => {
  if (status === 'closed') return 'success'
  if (status === 'processing') return 'warning'
  return 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    finance: '财务',
    account: '账号',
    tender: '标书',
    activity: '活动',
    other: '其他'
  }
  return map[type] || '其他'
}

const getPriorityLabel = (priority: string) => {
  const map: Record<string, string> = {
    low: '低',
    normal: '一般',
    high: '高',
    urgent: '非常紧急'
  }
  return map[priority] || '一般'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    open: '待处理',
    processing: '处理中',
    closed: '已完成'
  }
  return map[status] || '待处理'
}

const isImageFile = (fileExt: string) => ['png', 'jpg', 'jpeg', 'webp', 'gif'].includes((fileExt || '').toLowerCase())

const formatFileMeta = (fileExt: string, fileSize: number) => {
  const size = Number(fileSize || 0)
  let sizeLabel = ''
  if (size >= 1024 * 1024) sizeLabel = `${(size / 1024 / 1024).toFixed(2)} MB`
  else if (size >= 1024) sizeLabel = `${(size / 1024).toFixed(1)} KB`
  else if (size > 0) sizeLabel = `${size} B`
  return [fileExt?.toUpperCase?.() || '', sizeLabel].filter(Boolean).join(' · ')
}

const openFile = (url: string) => {
  if (!url || !import.meta.client) return
  window.open(url, '_blank')
}

onMounted(async () => {
  await reloadAll()
})
</script>
