<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        证书模板管理已接入真实 API。模板文件继续走上传服务，模板元数据持久化到后台配置中心。
      </template>
    </el-alert>

    <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-slate-800">证书模板管理</h3>
          <p class="mt-1 text-xs text-slate-500">
            仅管理证书模板的上传、预览、下载与删除，证书颁发仍在具体业务模块执行
          </p>
        </div>
        <div class="flex gap-2">
          <el-button :loading="loading" @click="loadTemplates">刷新配置</el-button>
          <el-button type="primary" @click="openUploadDialog()">
            <el-icon class="mr-2"><Upload /></el-icon>
            上传新模板
          </el-button>
        </div>
      </div>

      <el-table :data="templateList" stripe border v-loading="loading">
        <el-table-column prop="name" label="证书名称" width="220" />
        <el-table-column prop="type" label="文件类型" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="证书类别" width="150" />
        <el-table-column prop="sourceModule" label="来源模块" width="160" />
        <el-table-column prop="fileName" label="原始文件名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="预览" width="100">
          <template #default="scope">
            <el-button link type="primary" @click="handlePreview(scope.row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="下载" width="100">
          <template #default="scope">
            <el-button link type="success" @click="handleDownload(scope.row)">
              下载
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button link type="danger" size="small" :loading="saving" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="uploadDialogVisible" title="上传证书模板" width="600px">
      <el-form :model="uploadForm" label-width="110px">
        <el-form-item label="证书名称" required>
          <el-input v-model="uploadForm.name" placeholder="请输入证书名称" />
        </el-form-item>
        <el-form-item label="证书类别" required>
          <el-select v-model="uploadForm.category" class="w-full" placeholder="选择类别">
            <el-option label="资质证书" value="资质证书" />
            <el-option label="培训证书" value="培训证书" />
            <el-option label="荣誉证书" value="荣誉证书" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源模块" required>
          <el-select v-model="uploadForm.sourceModule" class="w-full" placeholder="选择来源">
            <el-option label="服务商审核" value="服务商审核" />
            <el-option label="培训模块" value="培训模块" />
            <el-option label="活动模块" value="活动模块" />
            <el-option label="通用模板" value="通用模板" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板文件" required>
          <el-upload
            action="/api/common/upload"
            :limit="1"
            :file-list="uploadFileList"
            :before-upload="beforeTemplateUpload"
            :on-success="handleTemplateUploadSuccess"
            :on-error="handleTemplateUploadError"
            :on-remove="handleTemplateRemove"
            accept=".pdf,.png,.jpg,.jpeg"
          >
            <el-button type="primary">
              <el-icon class="mr-1"><Upload /></el-icon>
              上传模板
            </el-button>
            <template #tip>
              <div class="el-upload__tip text-xs text-slate-500">
                支持 PDF/JPG/PNG，大小不超过 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleConfirmUpload">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="证书预览" width="900px">
      <div class="flex min-h-96 items-center justify-center rounded bg-slate-50 p-4">
        <template v-if="previewItem?.fileUrl">
          <img
            v-if="previewItem.type !== 'PDF'"
            :src="resolveFileUrl(previewItem.fileUrl)"
            class="max-h-96 object-contain"
          />
          <iframe
            v-else
            :src="resolveFileUrl(previewItem.fileUrl)"
            class="h-[70vh] w-full rounded border border-slate-200 bg-white"
          />
        </template>
        <template v-else>
          <el-icon class="text-6xl text-slate-300"><Document /></el-icon>
        </template>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Upload, View, Document } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'
import { resolveFileUrl } from '@/utils/file-url'

definePageMeta({ layout: 'admin' })

type CertificateTemplate = {
  id: number
  name: string
  type: 'PDF' | 'Image'
  category: string
  sourceModule: string
  updateTime: string
  fileUrl: string
  fileName: string
  storedName: string
  fileExt: string
  fileSize: number
  contentType: string
}

type UploadResponse = {
  code?: number
  data?: {
    url?: string
    fileName?: string
    storedName?: string
    fileExt?: string
    fileSize?: number
    contentType?: string
  }
}

const NAMESPACE = 'system_certificates'
const CONFIG_META = {
  template_list: { name: '证书模板列表', sortOrder: 10 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

const previewVisible = ref(false)
const previewItem = ref<CertificateTemplate | null>(null)

const uploadDialogVisible = ref(false)
const uploadFileList = ref<UploadUserFile[]>([])

const uploadForm = ref({
  name: '',
  category: '',
  sourceModule: '',
  fileUrl: '',
  fileName: '',
  storedName: '',
  fileExt: '',
  fileSize: 0,
  contentType: ''
})

const createDefaultTemplates = (): CertificateTemplate[] => ([
  {
    id: 1,
    name: '平台服务商证书',
    type: 'PDF',
    category: '资质证书',
    sourceModule: '服务商审核',
    updateTime: '2025-12-15',
    fileUrl: '',
    fileName: 'platform-supplier-template.pdf',
    storedName: '',
    fileExt: 'pdf',
    fileSize: 0,
    contentType: 'application/pdf'
  },
  {
    id: 2,
    name: '培训结业证书',
    type: 'PDF',
    category: '培训证书',
    sourceModule: '培训模块',
    updateTime: '2026-01-10',
    fileUrl: '',
    fileName: 'training-certificate-template.pdf',
    storedName: '',
    fileExt: 'pdf',
    fileSize: 0,
    contentType: 'application/pdf'
  },
  {
    id: 3,
    name: '公益活动证书',
    type: 'Image',
    category: '荣誉证书',
    sourceModule: '活动模块',
    updateTime: '2026-01-20',
    fileUrl: '',
    fileName: 'activity-certificate-template.png',
    storedName: '',
    fileExt: 'png',
    fileSize: 0,
    contentType: 'image/png'
  }
])

const templateList = ref<CertificateTemplate[]>(createDefaultTemplates())

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeTemplates = (value: unknown): CertificateTemplate[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return createDefaultTemplates()
  }
  return value.map((item: any, index) => {
    const fileExt = String(item?.fileExt || '').toLowerCase()
    const type = fileExt === 'pdf' || item?.type === 'PDF' ? 'PDF' : 'Image'
    return {
      id: normalizeNumber(item?.id, index + 1),
      name: item?.name || `模板${index + 1}`,
      type,
      category: item?.category || '',
      sourceModule: item?.sourceModule || '',
      updateTime: item?.updateTime || '',
      fileUrl: item?.fileUrl || '',
      fileName: item?.fileName || '',
      storedName: item?.storedName || '',
      fileExt: fileExt || (type === 'PDF' ? 'pdf' : 'png'),
      fileSize: normalizeNumber(item?.fileSize, 0),
      contentType: item?.contentType || ''
    }
  })
}

const resetUploadForm = () => {
  uploadForm.value = {
    name: '',
    category: '',
    sourceModule: '',
    fileUrl: '',
    fileName: '',
    storedName: '',
    fileExt: '',
    fileSize: 0,
    contentType: ''
  }
  uploadFileList.value = []
}

const loadTemplates = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    templateList.value = normalizeTemplates(itemMap.template_list)
  } catch (error: any) {
    ElMessage.error(error?.message || '证书模板加载失败')
  }
}

const persistTemplates = async (successMessage: string) => {
  try {
    await saveNamespaceItems([
      {
        key: 'template_list',
        value: templateList.value
      }
    ])
    ElMessage.success(successMessage)
    await loadTemplates()
  } catch (error: any) {
    ElMessage.error(error?.message || '证书模板保存失败')
  }
}

const beforeTemplateUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowed = ['application/pdf', 'image/png', 'image/jpeg']
  if (!allowed.includes(rawFile.type)) {
    ElMessage.error('仅支持 PDF/JPG/PNG 格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

const handleTemplateUploadSuccess: UploadProps['onSuccess'] = (response: UploadResponse, uploadFile) => {
  if (response?.code === 200 && response?.data?.url) {
    uploadForm.value.fileUrl = response.data.url || ''
    uploadForm.value.fileName = response.data.fileName || uploadFile.name || ''
    uploadForm.value.storedName = response.data.storedName || ''
    uploadForm.value.fileExt = response.data.fileExt || ''
    uploadForm.value.fileSize = normalizeNumber(response.data.fileSize, 0)
    uploadForm.value.contentType = response.data.contentType || ''
    uploadFileList.value = [{
      name: uploadForm.value.fileName || uploadFile.name,
      url: response.data.url
    }]
    ElMessage.success('模板上传成功')
    return
  }
  ElMessage.error('上传失败，请重试')
}

const handleTemplateUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败，请重试')
}

const handleTemplateRemove: UploadProps['onRemove'] = () => {
  uploadForm.value.fileUrl = ''
  uploadForm.value.fileName = ''
  uploadForm.value.storedName = ''
  uploadForm.value.fileExt = ''
  uploadForm.value.fileSize = 0
  uploadForm.value.contentType = ''
}

const openUploadDialog = () => {
  resetUploadForm()
  uploadDialogVisible.value = true
}

const handleConfirmUpload = async () => {
  if (!uploadForm.value.name || !uploadForm.value.category || !uploadForm.value.sourceModule) {
    return ElMessage.warning('请填写完整信息')
  }
  if (!uploadForm.value.fileUrl) {
    return ElMessage.warning('请先上传模板文件')
  }

  const type = uploadForm.value.fileExt.toLowerCase() === 'pdf' ? 'PDF' : 'Image'
  templateList.value.unshift({
    id: Date.now(),
    name: uploadForm.value.name,
    type,
    category: uploadForm.value.category,
    sourceModule: uploadForm.value.sourceModule,
    updateTime: new Date().toISOString().slice(0, 10),
    fileUrl: uploadForm.value.fileUrl,
    fileName: uploadForm.value.fileName,
    storedName: uploadForm.value.storedName,
    fileExt: uploadForm.value.fileExt,
    fileSize: uploadForm.value.fileSize,
    contentType: uploadForm.value.contentType
  })

  await persistTemplates('模板已保存')
  uploadDialogVisible.value = false
  resetUploadForm()
}

const handlePreview = (row: CertificateTemplate) => {
  previewItem.value = row
  previewVisible.value = true
}

const handleDownload = (row: CertificateTemplate) => {
  if (!row.fileUrl) {
    return ElMessage.warning('当前模板文件地址为空，无法下载')
  }
  const link = document.createElement('a')
  link.href = resolveFileUrl(row.fileUrl)
  link.download = row.fileName || row.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleDelete = (row: CertificateTemplate) => {
  ElMessageBox.confirm(`确定删除模板 "${row.name}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      templateList.value = templateList.value.filter(item => item.id !== row.id)
      await persistTemplates('已删除模板')
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadTemplates()
})
</script>
