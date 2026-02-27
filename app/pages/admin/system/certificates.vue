<!-- 文件路径: D:\ipp-platform\app\pages\admin\system/certificates.vue -->
<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">证书模板管理</h3>
          <p class="text-xs text-slate-500 mt-1">
            仅管理证书模板（上传/预览/删除），证书颁发在业务模块执行
          </p>
        </div>
        <el-button type="primary" @click="uploadDialogVisible = true">
          <el-icon class="mr-2"><Upload /></el-icon> 上传新模板
        </el-button>
      </div>

      <!-- 模板列表 -->
      <el-table :data="templateList" stripe border>
        <el-table-column prop="name" label="证书名称" width="220" />
        <el-table-column prop="type" label="文件类型" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="证书类别" width="150" />
        <el-table-column prop="sourceModule" label="来源模块" width="160" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="预览" width="100">
          <template #default="scope">
            <el-button link type="primary" @click="handlePreview(scope.row)">
              <el-icon><View /></el-icon> 预览
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 上传模板弹窗 -->
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
            accept=".pdf,.png,.jpg,.jpeg"
          >
            <el-button type="primary">
              <el-icon class="mr-1"><Upload /></el-icon> 上传模板
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
        <el-button type="primary" @click="handleConfirmUpload" :loading="uploading">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="证书预览" width="800px">
      <div class="flex items-center justify-center h-96 bg-slate-50 rounded">
        <template v-if="previewItem?.fileUrl">
          <img
            v-if="previewItem.type !== 'PDF'"
            :src="previewItem.fileUrl"
            class="max-h-96 object-contain"
          />
          <div v-else class="text-slate-500 text-sm">
            PDF 模板无法直接预览，可下载查看。
          </div>
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
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'

definePageMeta({ layout: 'admin' })

const previewVisible = ref(false)
const previewItem = ref<any>(null)

const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadFileList = ref<UploadUserFile[]>([])

const uploadForm = ref({
  name: '',
  category: '',
  sourceModule: '',
  fileUrl: '',
  type: ''
})

const templateList = ref([
  { id: 1, name: '平台服务商证书', type: 'PDF', category: '资质证书', sourceModule: '服务商审核', updateTime: '2025-12-15', fileUrl: '' },
  { id: 2, name: '培训结业证书', type: 'PDF', category: '培训证书', sourceModule: '培训模块', updateTime: '2026-01-10', fileUrl: '' },
  { id: 3, name: '公益活动证书', type: 'Image', category: '荣誉证书', sourceModule: '活动模块', updateTime: '2026-01-20', fileUrl: '' }
])

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

const handleTemplateUploadSuccess: UploadProps['onSuccess'] = (response) => {
  if (response?.code === 200) {
    uploadForm.value.fileUrl = response.data.url
    ElMessage.success('模板上传成功')
  } else {
    ElMessage.error('上传失败，请重试')
  }
}

const handleTemplateUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败，请重试')
}

const handleConfirmUpload = () => {
  if (!uploadForm.value.name || !uploadForm.value.category || !uploadForm.value.sourceModule) {
    return ElMessage.warning('请填写完整信息')
  }
  if (!uploadForm.value.fileUrl) {
    return ElMessage.warning('请先上传模板文件')
  }

  uploading.value = true
  setTimeout(() => {
    uploading.value = false

    const type = uploadForm.value.fileUrl.endsWith('.pdf') ? 'PDF' : 'Image'
    templateList.value.unshift({
      id: Date.now(),
      name: uploadForm.value.name,
      type,
      category: uploadForm.value.category,
      sourceModule: uploadForm.value.sourceModule,
      updateTime: new Date().toISOString().split('T')[0],
      fileUrl: uploadForm.value.fileUrl
    })

    ElMessage.success('模板已保存')
    uploadDialogVisible.value = false
    uploadFileList.value = []
    uploadForm.value = { name: '', category: '', sourceModule: '', fileUrl: '', type: '' }
  }, 600)
}

const handlePreview = (row: any) => {
  previewItem.value = row
  previewVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除模板 "${row.name}" 吗？`, '提示', { type: 'warning' })
    .then(() => {
      templateList.value = templateList.value.filter(t => t.id !== row.id)
      ElMessage.success('已删除模板')
    })
}
</script>
