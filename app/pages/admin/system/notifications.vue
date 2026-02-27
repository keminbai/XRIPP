<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\system\notifications.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [通知类型] 付费通知/活动通知/应急重点通知
  2. [批量推送] 支持群发配置
  3. [通知模板] 模板管理
  4. [发送记录] 历史记录查询
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 通知类型设置 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4">
        <h3 class="text-lg font-bold text-slate-800">通知类型设置</h3>
        <p class="text-xs text-slate-500 mt-1">设置通知渠道与触发类型</p>
      </div>

      <el-table :data="typeSettings" border stripe>
        <el-table-column prop="type" label="通知类型" width="200" />
        <el-table-column label="短信" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.sms" /></template>
        </el-table-column>
        <el-table-column label="站内信" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.inApp" /></template>
        </el-table-column>
        <el-table-column label="邮件" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.email" /></template>
        </el-table-column>
        <el-table-column prop="desc" label="说明" />
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-button type="primary" @click="saveTypeSettings">保存设置</el-button>
      </div>
    </div>

    <!-- 批量推送 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4">
        <h3 class="text-lg font-bold text-slate-800">批量通知推送</h3>
        <p class="text-xs text-slate-500 mt-1">面向全体/标签/指定用户发送通知</p>
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
          <el-button type="primary" @click="handleSend">立即发送</el-button>
          <el-button @click="resetPushForm">清空</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 通知模板 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">通知模板</h3>
          <p class="text-xs text-slate-500 mt-1">常用通知模板管理</p>
        </div>
        <el-button type="primary" @click="openTemplateDialog()">
          <el-icon class="mr-2"><Plus /></el-icon> 新增模板
        </el-button>
      </div>

      <el-table :data="templates" border stripe>
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="type" label="类型" width="160">
          <template #default="scope">
            <el-tag size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button link type="primary" @click="openTemplateDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发送记录 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4">
        <h3 class="text-lg font-bold text-slate-800">通知发送记录</h3>
        <p class="text-xs text-slate-500 mt-1">最近通知发送记录</p>
      </div>

      <el-table :data="sendLogs" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
        <el-table-column prop="date" label="时间" width="160" />
        <el-table-column prop="type" label="类型" width="140">
          <template #default="scope">
            <el-tag size="small">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="target" label="范围" width="140" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'warning'" size="small">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 模板弹窗 -->
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
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

definePageMeta({ layout: 'admin' })

const typeSettings = ref([
  { type: '付费通知', sms: true, inApp: true, email: false, desc: '会员费、标书、培训等支付通知' },
  { type: '活动通知', sms: true, inApp: true, email: true, desc: '活动报名、推广通知' },
  { type: '应急重点通知', sms: true, inApp: true, email: true, desc: '紧急事项快速触达' }
])

const pushForm = ref({
  type: 'payment',
  target: 'all',
  title: '',
  content: ''
})

const templates = ref([
  { id: 1, name: '会员费支付成功', type: 'payment', content: '您的会员费已支付成功，感谢支持。' },
  { id: 2, name: '活动报名成功', type: 'activity', content: '您已报名成功，请准时参加活动。' }
])

const sendLogs = ref([
  { date: '2026-02-05 10:12', type: 'payment', title: '会员费支付成功', target: '会员用户', status: 'success' },
  { date: '2026-02-04 18:20', type: 'activity', title: '活动通知', target: '全体用户', status: 'success' }
])

const templateDialogVisible = ref(false)
const templateForm = ref({ id: 0, name: '', type: 'payment', content: '' })

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { payment: '付费通知', activity: '活动通知', urgent: '应急重点通知' }
  return map[type] || '未知'
}

const saveTypeSettings = () => ElMessage.success('通知类型设置已保存')

const handleSend = () => {
  if (!pushForm.value.title || !pushForm.value.content) {
    return ElMessage.warning('请填写标题与内容')
  }
  sendLogs.value.unshift({
    date: new Date().toISOString().slice(0, 16).replace('T', ' '),
    type: pushForm.value.type,
    title: pushForm.value.title,
    target: pushForm.value.target,
    status: 'success'
  })
  ElMessage.success('已发送')
  resetPushForm()
}

const resetPushForm = () => {
  pushForm.value = { type: 'payment', target: 'all', title: '', content: '' }
}

const openTemplateDialog = (row?: any) => {
  templateForm.value = row ? { ...row } : { id: 0, name: '', type: 'payment', content: '' }
  templateDialogVisible.value = true
}

const saveTemplate = () => {
  if (!templateForm.value.name || !templateForm.value.content) return ElMessage.warning('请填写完整')
  if (templateForm.value.id) {
    const idx = templates.value.findIndex(t => t.id === templateForm.value.id)
    if (idx !== -1) templates.value[idx] = { ...templateForm.value }
  } else {
    templateForm.value.id = Date.now()
    templates.value.push({ ...templateForm.value })
  }
  templateDialogVisible.value = false
  ElMessage.success('模板已保存')
}

const removeTemplate = (row: any) => {
  ElMessageBox.confirm(`确认删除模板 "${row.name}" 吗？`, '提示', { type: 'warning' }).then(() => {
    templates.value = templates.value.filter(t => t.id !== row.id)
    ElMessage.success('已删除')
  })
}
</script>
