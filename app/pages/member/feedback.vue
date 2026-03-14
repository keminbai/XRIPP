<template>
  <div class="space-y-6">
    <div class="bg-gradient-to-r from-teal-800 to-emerald-900 rounded-2xl p-8 text-white shadow-lg flex flex-col md:flex-row items-center justify-between gap-6">
      <div>
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 text-teal-100 text-xs font-bold mb-4">
          <el-icon><Headset /></el-icon> SERVICE CENTER
        </div>
        <h1 class="text-3xl font-bold mb-2">客户服务与工单系统</h1>
        <p class="text-teal-100 text-sm max-w-xl leading-relaxed">
          遇到问题可直接提交工单，后台会进入真实处理流程；也可以先留个简短留言，客服后续回访跟进。
        </p>
      </div>
      <div class="flex gap-4">
        <div class="text-center bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10">
          <div class="text-2xl font-bold font-mono">400-888-8888</div>
          <div class="text-xs text-teal-200 mt-1">24小时热线</div>
        </div>
        <div class="text-center bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10">
          <div class="text-2xl font-bold font-mono">service@xripp.com</div>
          <div class="text-xs text-teal-200 mt-1">官方邮箱</div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <div class="flex justify-between items-center mb-6 gap-4">
          <h3 class="font-bold text-slate-900 text-lg flex items-center gap-2">
            <span class="w-1 h-5 bg-teal-600 rounded-full"></span> 我的工单
          </h3>
          <div class="flex gap-2">
            <el-button :loading="loading" @click="loadTickets">刷新</el-button>
            <el-button type="primary" @click="openCreateDialog">
              <el-icon class="mr-1"><EditPen /></el-icon> 提交新工单
            </el-button>
          </div>
        </div>

        <div v-loading="loading">
          <el-table :data="tickets" style="width: 100%" :header-cell-style="{ background: '#f8fafc' }">
            <el-table-column prop="ticketNo" label="工单编号" width="190" />
            <el-table-column prop="title" label="问题标题" min-width="220" show-overflow-tooltip />
            <el-table-column label="类型" width="100">
              <template #default="scope">
                <el-tag size="small" effect="plain" :type="getTypeTag(scope.row.type)">
                  {{ scope.row.typeLabel || getTypeLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="scope">
                <span class="flex items-center gap-1.5 text-xs font-bold" :class="getStatusColor(scope.row.status)">
                  <span class="w-1.5 h-1.5 rounded-full" :class="getStatusBg(scope.row.status)"></span>
                  {{ scope.row.statusLabel || getStatusLabel(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="openDetail(scope.row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="!loading && tickets.length === 0" class="text-center py-12 text-slate-400">
            <el-icon class="text-4xl mb-2"><Document /></el-icon>
            <p>暂无工单记录</p>
          </div>
        </div>
      </div>

      <div class="space-y-6">
        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <h3 class="font-bold text-slate-900 text-lg mb-6 flex items-center gap-2">
            <el-icon class="text-teal-600"><QuestionFilled /></el-icon> 常见问题 (FAQ)
          </h3>

          <div class="space-y-4">
            <div
              v-for="(faq, i) in faqs"
              :key="i"
              class="p-4 bg-slate-50 rounded-xl border border-slate-100 hover:bg-teal-50 hover:border-teal-100 transition-colors group"
            >
              <div class="text-sm font-bold text-slate-800 mb-2 group-hover:text-teal-700">{{ faq.q }}</div>
              <div class="text-xs text-slate-500 leading-relaxed">{{ faq.a }}</div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <div class="flex items-center gap-2 mb-4">
            <el-icon class="text-teal-600"><ChatDotRound /></el-icon>
            <h3 class="font-bold text-slate-900 text-lg">快速留言</h3>
          </div>
          <p class="text-xs text-slate-500 leading-relaxed mb-4">
            留言会进入后台“留言记录”，适合简短咨询或希望客服稍后联系的场景。
          </p>
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入想咨询的问题或需要客服联系的内容"
          />
          <div class="mt-4 flex justify-end">
            <el-button type="primary" :loading="messageSubmitting" @click="submitMessage">
              提交留言
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="提交新工单" width="640px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="问题分类" required>
            <el-select v-model="form.type" placeholder="请选择" class="w-full">
              <el-option label="财务/发票" value="finance" />
              <el-option label="账号/登录" value="account" />
              <el-option label="标书下载问题" value="tender" />
              <el-option label="活动报名" value="activity" />
              <el-option label="其他建议" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="form.priority" placeholder="请选择" class="w-full">
              <el-option label="一般" value="normal" />
              <el-option label="较高" value="high" />
              <el-option label="非常紧急" value="urgent" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="工单标题" required>
          <el-input v-model="form.title" placeholder="请简要描述遇到的问题" />
        </el-form-item>

        <el-form-item label="详细描述" required>
          <el-input
            v-model="form.desc"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="请详细描述问题发生的场景、时间以及您的诉求"
          />
        </el-form-item>

        <el-form-item label="截图上传">
          <el-upload
            action="/api/common/upload"
            list-type="picture-card"
            :limit="3"
            accept=".png,.jpg,.jpeg,.webp"
            v-model:file-list="uploadFileList"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :on-error="handleUploadError"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="mt-2 text-xs text-slate-500">
            支持 PNG/JPG/WEBP，上传成功后会与工单一并持久化。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交工单</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="工单详情" width="760px">
      <div v-loading="detailLoading" class="space-y-5">
        <template v-if="ticketDetail">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">工单编号</div>
              <div class="font-mono text-sm text-slate-800">{{ ticketDetail.ticketNo }}</div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">当前状态</div>
              <div class="text-sm font-bold" :class="getStatusColor(ticketDetail.status)">
                {{ ticketDetail.statusLabel || getStatusLabel(ticketDetail.status) }}
              </div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">问题类型</div>
              <div class="text-sm text-slate-800">{{ ticketDetail.typeLabel || getTypeLabel(ticketDetail.type) }}</div>
            </div>
            <div class="rounded-xl bg-slate-50 p-4">
              <div class="text-xs text-slate-500 mb-1">优先级</div>
              <div class="text-sm text-slate-800">{{ ticketDetail.priorityLabel || getPriorityLabel(ticketDetail.priority) }}</div>
            </div>
          </div>

          <div>
            <div class="text-sm font-bold text-slate-900 mb-2">标题</div>
            <div class="rounded-xl border border-slate-200 p-4 text-sm text-slate-700">{{ ticketDetail.title }}</div>
          </div>

          <div>
            <div class="text-sm font-bold text-slate-900 mb-2">问题描述</div>
            <div class="rounded-xl border border-slate-200 p-4 text-sm text-slate-700 whitespace-pre-wrap">
              {{ ticketDetail.content || '-' }}
            </div>
          </div>

          <div>
            <div class="text-sm font-bold text-slate-900 mb-2">客服回复</div>
            <div class="rounded-xl border border-slate-200 p-4 text-sm text-slate-700 whitespace-pre-wrap min-h-24">
              {{ ticketDetail.reply || '客服暂未回复' }}
            </div>
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
                  <div class="text-xs text-slate-500">
                    {{ formatFileMeta(file.fileExt, file.fileSize) }}
                  </div>
                </div>
                <div class="flex gap-2">
                  <el-button
                    v-if="isImageFile(file.fileExt)"
                    link
                    type="primary"
                    @click="openFile(file.fileUrl)"
                  >
                    预览
                  </el-button>
                  <el-button link type="success" @click="openFile(file.fileUrl)">
                    下载
                  </el-button>
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
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Headset, EditPen, Document, Plus, QuestionFilled, ChatDotRound
} from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'member' })

type TicketListItem = {
  id: number
  ticketNo: string
  title: string
  type: string
  typeLabel: string
  priority: string
  priorityLabel: string
  status: string
  statusLabel: string
  updateTime: string
  content?: string
  reply?: string
  attachments?: AttachmentItem[]
}

type AttachmentItem = {
  id?: number
  uid?: string
  fileName: string
  fileUrl: string
  fileExt: string
  fileSize: number
  contentType?: string
}

type TicketDetail = TicketListItem & {
  userId?: number
  partnerId?: number | null
  createdByAdmin?: boolean
  sourceChannel?: string
}

const dialogVisible = ref(false)
const detailVisible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const messageSubmitting = ref(false)
const detailLoading = ref(false)

const form = ref({
  type: 'other',
  priority: 'normal',
  title: '',
  desc: ''
})

const messageForm = ref({
  content: ''
})

const tickets = ref<TicketListItem[]>([])
const ticketDetail = ref<TicketDetail | null>(null)
const uploadFileList = ref<UploadUserFile[]>([])
const uploadedAttachments = ref<AttachmentItem[]>([])

const faqs = [
  { q: '如何申请发票？', a: '在订单管理中找到已支付订单，点击申请发票，填写抬头信息后提交即可。' },
  { q: '标书下载次数用完了怎么办？', a: '可以联系合伙人协助扩充权益，或购买额外服务包。' },
  { q: '企业资料如何修改？', a: '请前往账号设置修改，涉及审核项的资料提交后会进入后台复核。' },
  { q: '工单多久会处理？', a: '一般问题会尽快处理，紧急问题建议在工单中标记优先级并同步电话联系。' }
]

const resetForm = () => {
  form.value = {
    type: 'other',
    priority: 'normal',
    title: '',
    desc: ''
  }
  uploadFileList.value = []
  uploadedAttachments.value = []
}

const normalizeTickets = (items: any[]): TicketListItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    ticketNo: item?.ticketNo || '',
    title: item?.title || '',
    type: item?.type || '',
    typeLabel: item?.typeLabel || getTypeLabel(item?.type || ''),
    priority: item?.priority || 'normal',
    priorityLabel: item?.priorityLabel || getPriorityLabel(item?.priority || 'normal'),
    status: item?.status || 'open',
    statusLabel: item?.statusLabel || getStatusLabel(item?.status || 'open'),
    updateTime: item?.updateTime || '',
    content: item?.content || '',
    reply: item?.reply || '',
    attachments: Array.isArray(item?.attachments) ? item.attachments : []
  }))
}

const loadTickets = async () => {
  loading.value = true
  try {
    const res = await apiRequest<any>('/v3/member/customer-service/tickets?page=1&page_size=50')
    tickets.value = normalizeTickets(Array.isArray(res.data?.items) ? res.data.items : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '工单加载失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.title.trim() || !form.value.desc.trim()) {
    ElMessage.warning('请补全工单标题和详细描述')
    return
  }

  submitting.value = true
  try {
    const res = await apiRequest<any>('/v3/member/customer-service/tickets', {
      method: 'POST',
      body: {
        ticket_type: form.value.type,
        priority: form.value.priority,
        title: form.value.title,
        desc: form.value.desc,
        attachments: uploadedAttachments.value
      }
    })
    ElMessage.success(`工单提交成功：${res.data?.ticketNo || ''}`)
    dialogVisible.value = false
    resetForm()
    await loadTickets()
  } catch (error: any) {
    ElMessage.error(error?.message || '工单提交失败')
  } finally {
    submitting.value = false
  }
}

const submitMessage = async () => {
  if (!messageForm.value.content.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }

  messageSubmitting.value = true
  try {
    await apiRequest('/v3/member/customer-service/messages', {
      method: 'POST',
      body: {
        content: messageForm.value.content
      }
    })
    ElMessage.success('留言已提交，客服将尽快跟进')
    messageForm.value.content = ''
  } catch (error: any) {
    ElMessage.error(error?.message || '留言提交失败')
  } finally {
    messageSubmitting.value = false
  }
}

const openDetail = async (row: TicketListItem) => {
  detailVisible.value = true
  detailLoading.value = true
  ticketDetail.value = null
  try {
    const res = await apiRequest<any>(`/v3/member/customer-service/tickets/${row.id}`)
    ticketDetail.value = {
      ...row,
      ...res.data,
      attachments: Array.isArray(res.data?.attachments) ? res.data.attachments : []
    }
  } catch (error: any) {
    detailVisible.value = false
    ElMessage.error(error?.message || '工单详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const handleUploadSuccess: UploadProps['onSuccess'] = (response: any, file) => {
  if (response?.code !== 200 || !response?.data?.url) {
    ElMessage.error('附件上传失败')
    return
  }

  uploadedAttachments.value.push({
    uid: String(file.uid),
    fileName: response.data.fileName || file.name,
    fileUrl: response.data.url,
    fileExt: response.data.fileExt || '',
    fileSize: Number(response.data.fileSize || 0),
    contentType: response.data.contentType || ''
  })
  ElMessage.success('附件上传成功')
}

const handleUploadRemove: UploadProps['onRemove'] = (file) => {
  uploadedAttachments.value = uploadedAttachments.value.filter(item => item.uid !== String(file.uid))
}

const handleUploadError: UploadProps['onError'] = () => {
  ElMessage.error('附件上传失败')
}

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    finance: 'success',
    tender: 'primary',
    account: 'warning',
    activity: 'danger'
  }
  return map[type] || 'info'
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
    high: '较高',
    urgent: '非常紧急'
  }
  return map[priority] || '一般'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    open: '待处理',
    processing: '处理中',
    closed: '已解决'
  }
  return map[status] || '待处理'
}

const getStatusColor = (status: string) => {
  if (status === 'processing') return 'text-blue-600'
  if (status === 'closed') return 'text-green-600'
  return 'text-slate-500'
}

const getStatusBg = (status: string) => {
  if (status === 'processing') return 'bg-blue-600'
  if (status === 'closed') return 'bg-green-600'
  return 'bg-slate-400'
}

const isImageFile = (fileExt: string) => ['png', 'jpg', 'jpeg', 'webp', 'gif'].includes((fileExt || '').toLowerCase())

const formatFileMeta = (fileExt: string, fileSize: number) => {
  const size = Number(fileSize || 0)
  let sizeLabel = ''
  if (size >= 1024 * 1024) {
    sizeLabel = `${(size / 1024 / 1024).toFixed(2)} MB`
  } else if (size >= 1024) {
    sizeLabel = `${(size / 1024).toFixed(1)} KB`
  } else if (size > 0) {
    sizeLabel = `${size} B`
  }
  return [fileExt?.toUpperCase?.() || '', sizeLabel].filter(Boolean).join(' · ')
}

const openFile = (url: string) => {
  if (!url || !import.meta.client) return
  window.open(url, '_blank')
}

onMounted(async () => {
  await loadTickets()
})
</script>
