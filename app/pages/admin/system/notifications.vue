<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        通知中心已接入真实 API。通知类型设置、模板管理和发送记录会真实持久化；当前发送动作先落发送记录，第三方短信/邮件网关暂未接入。
      </template>
    </el-alert>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">通知类型设置</h3>
          <p class="text-xs text-slate-500 mt-1">设置通知渠道与触发类型</p>
        </div>
        <div class="flex gap-2">
          <el-button :loading="typeLoading" @click="loadTypeSettings">刷新</el-button>
          <el-button type="primary" :loading="typeSaving" @click="saveTypeSettings">保存设置</el-button>
        </div>
      </div>

      <el-table :data="typeSettings" border stripe v-loading="typeLoading">
        <el-table-column prop="displayName" label="通知类型" width="200" />
        <el-table-column label="短信" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.sms" /></template>
        </el-table-column>
        <el-table-column label="站内信" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.inApp" /></template>
        </el-table-column>
        <el-table-column label="邮件" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.email" /></template>
        </el-table-column>
        <el-table-column prop="desc" label="说明" min-width="260" />
      </el-table>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">批量通知推送</h3>
          <p class="text-xs text-slate-500 mt-1">面向全体/标签/指定用户发送通知，当前先记录发送请求与处理结果</p>
        </div>
      </div>

      <el-form :model="pushForm" label-width="120px" class="max-w-4xl">
        <el-form-item label="通知类型">
          <el-select v-model="pushForm.type" class="w-full">
            <el-option label="付费通知" value="payment" />
            <el-option label="活动通知" value="activity" />
            <el-option label="应急重点通知" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="推送范围">
          <el-select v-model="pushForm.target" class="w-full">
            <el-option label="全体用户" value="all" />
            <el-option label="会员用户" value="member" />
            <el-option label="服务商" value="supplier" />
            <el-option label="合伙人" value="partner" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知标题">
          <el-input v-model="pushForm.title" />
        </el-form-item>
        <el-form-item label="通知内容">
          <el-input v-model="pushForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="sendSaving" @click="handleSend">记录发送</el-button>
          <el-button @click="resetPushForm">清空</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">通知模板</h3>
          <p class="text-xs text-slate-500 mt-1">常用通知模板管理</p>
        </div>
        <div class="flex gap-2">
          <el-button :loading="templateLoading" @click="loadTemplates">刷新</el-button>
          <el-button type="primary" @click="openTemplateDialog()">
            <el-icon class="mr-2"><Plus /></el-icon> 新增模板
          </el-button>
        </div>
      </div>

      <el-table :data="templates" border stripe v-loading="templateLoading">
        <el-table-column prop="name" label="模板名称" width="220" />
        <el-table-column prop="type" label="类型" width="160">
          <template #default="scope">
            <el-tag size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">
              {{ scope.row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openTemplateDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-4 gap-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">通知发送记录</h3>
          <p class="text-xs text-slate-500 mt-1">最近通知发送记录</p>
        </div>
        <el-button :loading="logLoading" @click="loadSendLogs">刷新</el-button>
      </div>

      <el-table :data="sendLogs" stripe v-loading="logLoading" :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
        <el-table-column prop="date" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="140">
          <template #default="scope">
            <el-tag size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="target" label="范围" width="140">
          <template #default="scope">{{ getTargetLabel(scope.row.target) }}</template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)" size="small">
              {{ scope.row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultMessage" label="处理结果" min-width="280" show-overflow-tooltip />
      </el-table>
    </div>

    <el-dialog v-model="templateDialogVisible" :title="templateForm.id ? '编辑模板' : '新增模板'" width="600px">
      <el-form :model="templateForm" label-width="120px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="templateForm.type" class="w-full">
            <el-option label="付费通知" value="payment" />
            <el-option label="活动通知" value="activity" />
            <el-option label="应急重点通知" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="templateForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="templateForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="templateSaving" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type TypeSettingItem = {
  id: number
  type: string
  displayName: string
  sms: boolean
  inApp: boolean
  email: boolean
  desc: string
}

type TemplateItem = {
  id: number
  name: string
  type: string
  content: string
  enabled: boolean
  sortOrder: number
}

type SendLogItem = {
  id: number
  date: string
  type: string
  title: string
  target: string
  status: string
  statusLabel: string
  resultMessage: string
}

const typeLoading = ref(false)
const templateLoading = ref(false)
const logLoading = ref(false)
const typeSaving = ref(false)
const templateSaving = ref(false)
const sendSaving = ref(false)

const typeSettings = ref<TypeSettingItem[]>([])
const templates = ref<TemplateItem[]>([])
const sendLogs = ref<SendLogItem[]>([])

const pushForm = ref({
  type: 'payment',
  target: 'all',
  title: '',
  content: ''
})

const templateDialogVisible = ref(false)
const templateForm = ref({
  id: 0,
  name: '',
  type: 'payment',
  content: '',
  enabled: true,
  sortOrder: 0
})

const normalizeTypeSettings = (items: any[]): TypeSettingItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    type: item?.type || '',
    displayName: item?.displayName || getTypeLabel(item?.type || ''),
    sms: Boolean(item?.sms),
    inApp: Boolean(item?.inApp),
    email: Boolean(item?.email),
    desc: item?.desc || ''
  }))
}

const normalizeTemplates = (items: any[]): TemplateItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    name: item?.name || '',
    type: item?.type || 'payment',
    content: item?.content || '',
    enabled: item?.enabled !== false,
    sortOrder: Number(item?.sortOrder || 0)
  }))
}

const normalizeSendLogs = (items: any[]): SendLogItem[] => {
  return items.map((item: any) => ({
    id: Number(item?.id || 0),
    date: item?.date || '',
    type: item?.type || '',
    title: item?.title || '',
    target: item?.target || '',
    status: item?.status || 'accepted',
    statusLabel: item?.statusLabel || getStatusLabel(item?.status || 'accepted'),
    resultMessage: item?.resultMessage || ''
  }))
}

const loadTypeSettings = async () => {
  typeLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/notifications/type-settings')
    typeSettings.value = normalizeTypeSettings(Array.isArray(res.data) ? res.data : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '通知类型设置加载失败')
  } finally {
    typeLoading.value = false
  }
}

const saveTypeSettings = async () => {
  typeSaving.value = true
  try {
    await apiRequest('/v3/admin/notifications/type-settings', {
      method: 'POST',
      body: {
        items: typeSettings.value.map(item => ({
          type: item.type,
          displayName: item.displayName,
          sms: item.sms,
          inApp: item.inApp,
          email: item.email,
          desc: item.desc
        }))
      }
    })
    ElMessage.success('通知类型设置已保存')
    await loadTypeSettings()
  } catch (error: any) {
    ElMessage.error(error?.message || '通知类型设置保存失败')
  } finally {
    typeSaving.value = false
  }
}

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/notifications/templates')
    templates.value = normalizeTemplates(Array.isArray(res.data) ? res.data : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '通知模板加载失败')
  } finally {
    templateLoading.value = false
  }
}

const loadSendLogs = async () => {
  logLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/admin/notifications/send-logs')
    sendLogs.value = normalizeSendLogs(Array.isArray(res.data) ? res.data : [])
  } catch (error: any) {
    ElMessage.error(error?.message || '通知发送记录加载失败')
  } finally {
    logLoading.value = false
  }
}

const handleSend = async () => {
  if (!pushForm.value.title.trim() || !pushForm.value.content.trim()) {
    ElMessage.warning('请填写标题与内容')
    return
  }

  sendSaving.value = true
  try {
    await apiRequest('/v3/admin/notifications/send', {
      method: 'POST',
      body: {
        type: pushForm.value.type,
        target: pushForm.value.target,
        title: pushForm.value.title,
        content: pushForm.value.content
      }
    })
    ElMessage.success('发送请求已记录')
    resetPushForm()
    await loadSendLogs()
  } catch (error: any) {
    ElMessage.error(error?.message || '发送记录写入失败')
  } finally {
    sendSaving.value = false
  }
}

const resetPushForm = () => {
  pushForm.value = {
    type: 'payment',
    target: 'all',
    title: '',
    content: ''
  }
}

const openTemplateDialog = (row?: TemplateItem) => {
  templateForm.value = row
    ? { ...row }
    : { id: 0, name: '', type: 'payment', content: '', enabled: true, sortOrder: 0 }
  templateDialogVisible.value = true
}

const saveTemplate = async () => {
  if (!templateForm.value.name.trim() || !templateForm.value.content.trim()) {
    ElMessage.warning('请填写模板名称和内容')
    return
  }

  templateSaving.value = true
  try {
    const body = {
      name: templateForm.value.name,
      type: templateForm.value.type,
      content: templateForm.value.content,
      enabled: templateForm.value.enabled,
      sortOrder: templateForm.value.sortOrder
    }

    if (templateForm.value.id) {
      await apiRequest(`/v3/admin/notifications/templates/${templateForm.value.id}`, {
        method: 'PUT',
        body
      })
      ElMessage.success('模板已更新')
    } else {
      await apiRequest('/v3/admin/notifications/templates', {
        method: 'POST',
        body
      })
      ElMessage.success('模板已新增')
    }

    templateDialogVisible.value = false
    await loadTemplates()
  } catch (error: any) {
    ElMessage.error(error?.message || '模板保存失败')
  } finally {
    templateSaving.value = false
  }
}

const removeTemplate = (row: TemplateItem) => {
  ElMessageBox.confirm(`确认删除模板 "${row.name}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await apiRequest(`/v3/admin/notifications/templates/${row.id}`, {
        method: 'DELETE'
      })
      ElMessage.success('模板已删除')
      await loadTemplates()
    })
    .catch(() => {})
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    payment: '付费通知',
    activity: '活动通知',
    urgent: '应急重点通知'
  }
  return map[type] || '未知'
}

const getTargetLabel = (target: string) => {
  const map: Record<string, string> = {
    all: '全体用户',
    member: '会员用户',
    supplier: '服务商',
    partner: '合伙人',
    custom: '指定范围'
  }
  return map[target] || target
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    accepted: '已接收',
    processed: '已处理',
    failed: '失败'
  }
  return map[status] || status
}

const getStatusTag = (status: string) => {
  if (status === 'processed') return 'success'
  if (status === 'failed') return 'danger'
  return 'warning'
}

onMounted(async () => {
  await Promise.all([loadTypeSettings(), loadTemplates(), loadSendLogs()])
})
</script>
