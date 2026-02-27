<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\tenders\publish.vue
  版本: V1.3  (2026-02-27)
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-start mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">{{ isEditMode ? '编辑标书' : '发布新标书' }}</h3>
          <p class="text-xs text-slate-500 mt-1">请填写完整标书信息，带 * 为必填项</p>
        </div>
        <div class="bg-slate-50 px-4 py-2 rounded-lg border border-slate-200 text-right">
          <div class="text-xs text-slate-400">标书编号</div>
          <div class="text-lg font-mono font-bold text-blue-600">{{ form.tenderNo || '自动生成' }}</div>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="max-w-4xl"
        status-icon
      >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <el-form-item label="采购组织" prop="organization">
            <el-select v-model="form.organization" class="w-full" placeholder="请选择采购组织" @change="tryGenerateTenderNo">
              <el-option label="UNDP" value="UNDP" />
              <el-option label="WHO" value="WHO" />
              <el-option label="UNICEF" value="UNICEF" />
              <el-option label="UNGM" value="UNGM" />
            </el-select>
          </el-form-item>

          <el-form-item label="标书类别" prop="category">
            <el-select v-model="form.category" class="w-full" placeholder="请选择类别" @change="tryGenerateTenderNo">
              <el-option label="医疗器械" value="medical" />
              <el-option label="IT设备" value="it" />
              <el-option label="建筑工程" value="construction" />
              <el-option label="办公用品" value="office" />
              <el-option label="咨询服务" value="consulting" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="标书标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标书标题（5-100 字）" maxlength="100" show-word-limit />
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <el-form-item label="销售价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" class="w-full" />
          </el-form-item>

          <el-form-item label="截止时间" prop="deadline">
            <el-date-picker
              v-model="form.deadline"
              type="datetime"
              placeholder="选择截止时间"
              class="w-full"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </div>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入项目描述..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标书文件">
          <el-upload
            v-model:file-list="form.files"
            action="#"
            :auto-upload="false"
            :limit="3"
            :before-upload="beforeFileUpload"
            accept=".pdf,.doc,.docx"
            class="w-full"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖拽到此处，或 <em>点击选择</em></div>
            <template #tip>
              <div class="text-xs text-slate-400 mt-2">仅前端暂存，后端文件上传接口接入后再落库</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="置顶显示">
          <el-switch v-model="form.isTop" active-text="置顶" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit('publish')">
            <el-icon class="mr-2"><Promotion /></el-icon> 立即发布
          </el-button>
          <el-button :loading="submitting" @click="handleSubmit('draft')">
            <el-icon class="mr-2"><DocumentChecked /></el-icon> 保存草稿
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UploadFilled, Promotion, DocumentChecked } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadProps } from 'element-plus'
import { navigateTo } from '#app'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const editId = computed(() => {
  const raw = route.query.id
  const s = typeof raw === 'string' ? raw : ''
  return s && /^\d+$/.test(s) ? Number(s) : null
})
const isEditMode = computed(() => editId.value !== null)

const form = reactive({
  tenderNo: '',
  title: '',
  organization: '',
  category: '',
  price: 299,
  deadline: '',
  description: '',
  files: [] as any[],
  isTop: false
})

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入标书标题', trigger: 'blur' },
    { min: 5, message: '标题长度不能少于 5 个字符', trigger: 'blur' }
  ],
  organization: [{ required: true, message: '请选择采购组织', trigger: 'change' }],
  category: [{ required: true, message: '请选择标书类别', trigger: 'change' }],
  price: [{ required: true, message: '请输入销售价格', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  description: [{ required: true, message: '请输入项目描述', trigger: 'blur' }]
})

const tryGenerateTenderNo = () => {
  if (form.tenderNo || !form.organization || !form.category) return
  const prefixMap: Record<string, string> = {
    medical: 'A',
    it: 'B',
    construction: 'C',
    office: 'D',
    consulting: 'E'
  }
  const prefix = prefixMap[form.category] || 'X'
  const date = new Date().toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  form.tenderNo = `${form.organization}-${prefix}${date}-${random}`
}

const beforeFileUpload: UploadProps['beforeUpload'] = (file) => {
  const allowed = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ]
  if (!allowed.includes(file.type)) {
    ElMessage.error('仅支持 PDF/DOC/DOCX 格式')
    return false
  }
  if (file.size / 1024 / 1024 > 20) {
    ElMessage.error('单文件不能超过 20MB')
    return false
  }
  return true
}

const fillFormFromDetail = (x: any) => {
  form.tenderNo = x.tenderNo || x.tender_no || ''
  form.title = x.title || ''
  form.organization = x.organization || x.org || ''
  form.category = x.category || ''
  form.price = Number(x.price ?? 0)
  form.deadline = x.deadline || ''
  form.description = x.description || ''
  form.isTop = Boolean(x.isTop ?? x.is_top ?? false)
}

const loadDetailIfEdit = async () => {
  if (!editId.value) return
  try {
    const res: any = await apiRequest(`/v3/admin/tenders/${editId.value}`)
    fillFormFromDetail(res?.data || {})
  } catch (e: any) {
    ElMessage.error(e?.message || '加载标书详情失败')
  }
}

const buildPayload = (mode: 'publish' | 'draft') => {
  const status = mode === 'publish' ? 'published' : 'draft'
  return {
    tender_no: form.tenderNo || undefined,
    title: form.title,
    organization: form.organization,
    category: form.category,
    price: Number(form.price || 0),
    deadline: form.deadline || undefined,
    description: form.description || '',
    is_top: form.isTop,
    tender_status: status
  }
}

const submitToApi = async (mode: 'publish' | 'draft') => {
  const payload = buildPayload(mode)

  if (editId.value) {
    await apiRequest(`/v3/admin/tenders/${editId.value}`, {
      method: 'PUT',
      body: payload
    })
    return
  }

  await apiRequest('/v3/admin/tenders', {
    method: 'POST',
    body: payload
  })
}

const handleSubmit = async (mode: 'publish' | 'draft') => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return ElMessage.warning('请先完善必填项')

  submitting.value = true
  try {
    tryGenerateTenderNo()
    await submitToApi(mode)
    ElMessage.success(mode === 'publish' ? '标书发布成功' : '草稿保存成功')
    await navigateTo('/admin/tenders/manage')
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('当前内容未保存，确认离开？', '提示', {
    confirmButtonText: '确认离开',
    cancelButtonText: '继续编辑',
    type: 'warning'
  }).then(() => {
    navigateTo('/admin/tenders/manage')
  }).catch(() => {})
}

onMounted(async () => {
  await loadDetailIfEdit()
  tryGenerateTenderNo()
})
</script>
