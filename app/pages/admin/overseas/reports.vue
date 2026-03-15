<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex items-start justify-between gap-4 mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">
            {{ isEdit ? `编辑报告：${reportForm.title || '未命名报告'}` : '上传行业报告' }}
          </h3>
          <p class="text-xs text-slate-500 mt-1">数据已接入 `/v3/admin/overseas/reports`，上传和下载计数均真实持久化</p>
        </div>
        <el-button v-if="isEdit" type="warning" plain @click="resetForm">取消编辑</el-button>
      </div>

      <el-form label-width="120px" class="max-w-4xl">
        <el-form-item label="报告标题" required>
          <el-input v-model="reportForm.title" maxlength="100" show-word-limit placeholder="请输入报告标题" />
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <el-form-item label="目标国家/地区" required>
            <el-select v-model="reportForm.country" class="w-full" placeholder="请选择国家/地区">
              <el-option v-for="item in OVERSEAS_COUNTRY_OPTIONS" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>

          <el-form-item label="行业分类" required>
            <el-select v-model="reportForm.industry" class="w-full" placeholder="请选择行业">
              <el-option v-for="item in OVERSEAS_REPORT_INDUSTRY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="报告类型" required>
          <el-select v-model="reportForm.type" class="w-full" placeholder="请选择报告类型">
            <el-option v-for="item in OVERSEAS_REPORT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <el-form-item label="报告年份">
            <el-date-picker v-model="reportForm.year" type="year" value-format="YYYY" format="YYYY" placeholder="选择年份" class="w-full" />
          </el-form-item>

          <el-form-item label="发布时间" required>
            <el-date-picker v-model="reportForm.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="选择发布时间" class="w-full" />
          </el-form-item>
        </div>

        <el-form-item label="报告摘要" required>
          <el-input v-model="reportForm.summary" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入报告摘要" />
        </el-form-item>

        <el-form-item label="报告封面">
          <el-upload
            v-model:file-list="coverFileList"
            list-type="picture-card"
            :limit="1"
            :http-request="uploadCoverFile"
            :before-upload="beforeCoverUpload"
            :on-remove="handleCoverRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">建议尺寸 400x560，支持 JPG/PNG/WEBP</div>
        </el-form-item>

        <el-form-item label="报告文件" required>
          <el-upload
            v-model:file-list="reportFileList"
            :limit="1"
            :http-request="uploadReportFile"
            :before-upload="beforeReportFileUpload"
            :on-remove="handleReportFileRemove"
            accept=".pdf,.doc,.docx"
          >
            <el-button type="primary">
              <el-icon class="mr-2"><Upload /></el-icon>上传报告文件
            </el-button>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">支持 PDF、DOC、DOCX，大小不超过 20MB</div>
        </el-form-item>

        <el-form-item label="关键词">
          <el-select v-model="reportForm.keywords" multiple allow-create filterable class="w-full" placeholder="输入关键词后回车添加" />
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <el-form-item label="报告来源">
            <el-input v-model="reportForm.source" placeholder="如：麦肯锡、Gartner、德勤" />
          </el-form-item>

          <el-form-item label="关联收费">
            <el-select v-model="reportForm.feeLevel" class="w-full" placeholder="请选择收费类型">
              <el-option v-for="item in OVERSEAS_REPORT_FEE_LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="访问权限">
          <el-radio-group v-model="reportForm.accessLevel">
            <el-radio v-for="item in OVERSEAS_REPORT_ACCESS_LEVEL_OPTIONS" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否推荐">
          <el-switch v-model="reportForm.isRecommended" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">
            <el-icon class="mr-2"><Upload /></el-icon>{{ isEdit ? '保存修改' : '上传报告' }}
          </el-button>
          <el-button @click="resetForm">
            <el-icon class="mr-2"><RefreshLeft /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h3 class="text-lg font-bold text-slate-800">行业报告库</h3>
            <p class="text-xs text-slate-500 mt-1">共 {{ total }} 条，当前页累计下载 {{ currentPageDownloads }} 次</p>
          </div>
          <el-button type="success" plain @click="exportCurrentList">导出当前页</el-button>
        </div>

        <div class="flex gap-3 flex-wrap mt-4">
          <el-input v-model="filters.keyword" class="w-64" clearable placeholder="搜索标题/摘要/来源" :prefix-icon="Search" />
          <el-select v-model="filters.country" class="w-36" clearable placeholder="国家/地区">
            <el-option v-for="item in OVERSEAS_COUNTRY_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.industry" class="w-36" clearable placeholder="行业">
            <el-option v-for="item in OVERSEAS_REPORT_INDUSTRY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="filters.type" class="w-36" clearable placeholder="报告类型">
            <el-option v-for="item in OVERSEAS_REPORT_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="filters.feeLevel" class="w-32" clearable placeholder="收费类型">
            <el-option v-for="item in OVERSEAS_REPORT_FEE_LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-date-picker
            v-model="filters.publishRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table v-loading="tableLoading" :data="reportList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
          <el-table-column label="报告信息" min-width="360">
            <template #default="{ row }">
              <div class="flex gap-3 py-2">
                <div class="w-20 h-28 rounded-lg border border-slate-200 overflow-hidden bg-slate-50 flex-shrink-0">
                  <img v-if="row.coverImage" :src="resolveFileUrl(row.coverImage)" class="w-full h-full object-cover" />
                  <div v-else class="w-full h-full flex items-center justify-center text-xs text-slate-400">无封面</div>
                </div>
                <div class="min-w-0 flex-1">
                  <div class="font-bold text-slate-800">{{ row.title }}</div>
                  <div class="text-xs text-slate-500 mt-1 line-clamp-2">{{ row.summary }}</div>
                  <div class="flex gap-2 flex-wrap mt-2">
                    <el-tag size="small" effect="plain">{{ row.country }}</el-tag>
                    <el-tag size="small" type="success" effect="plain">{{ OVERSEAS_REPORT_INDUSTRY_LABEL_MAP[row.industry] || row.industry }}</el-tag>
                    <el-tag v-if="row.isRecommended" size="small" type="danger">推荐</el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="报告类型" width="110">
            <template #default="{ row }">{{ OVERSEAS_REPORT_TYPE_LABEL_MAP[row.type] || row.type }}</template>
          </el-table-column>

          <el-table-column prop="year" label="年份" width="80" align="center" />

          <el-table-column prop="expireDate" label="截止日期" width="120">
            <template #default="{ row }">{{ row.expireDate || '-' }}</template>
          </el-table-column>

          <el-table-column label="下载量" width="90" align="center">
            <template #default="{ row }">{{ row.downloads }}</template>
          </el-table-column>

          <el-table-column label="访问权限" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="accessTagType(row.accessLevel)" size="small">
                {{ OVERSEAS_REPORT_ACCESS_LEVEL_LABEL_MAP[row.accessLevel] || row.accessLevel }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="publishDate" label="发布时间" width="120" />

          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" size="small" @click="downloadReport(row)">下载</el-button>
              <el-button link type="primary" size="small" @click="openExpireDialog(row)">设置截止</el-button>
              <el-button link type="warning" size="small" @click="startEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="removeItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next"
            :total="total"
            @current-change="loadList"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="expireDialogVisible" title="设置到期下架时间" width="420px" :close-on-click-modal="false">
      <el-date-picker v-model="expireDate" type="date" value-format="YYYY-MM-DD" class="w-full" placeholder="请选择截止日期" />
      <template #footer>
        <el-button @click="expireDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="expireSubmitting" @click="saveExpireDate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Plus, Upload, RefreshLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { apiFetchRaw, apiRequest } from '@/utils/request'
import { resolveFileUrl } from '@/utils/file-url'
import {
  OVERSEAS_COUNTRY_OPTIONS,
  OVERSEAS_REPORT_ACCESS_LEVEL_LABEL_MAP,
  OVERSEAS_REPORT_ACCESS_LEVEL_OPTIONS,
  OVERSEAS_REPORT_FEE_LEVEL_LABEL_MAP,
  OVERSEAS_REPORT_FEE_LEVEL_OPTIONS,
  OVERSEAS_REPORT_INDUSTRY_LABEL_MAP,
  OVERSEAS_REPORT_INDUSTRY_OPTIONS,
  OVERSEAS_REPORT_TYPE_LABEL_MAP,
  OVERSEAS_REPORT_TYPE_OPTIONS
} from '@/composables/useOverseasAdminCatalog'

definePageMeta({ layout: 'admin' })

type UploadMeta = {
  fileName: string
  fileUrl: string
  previewUrl?: string
  fileExt: string
  fileSize: number
}

type ReportItem = {
  id: number
  title: string
  country: string
  industry: string
  type: string
  year: string
  publishDate: string
  expireDate: string
  summary: string
  coverImage: string
  coverImageName: string
  coverImageExt: string
  coverImageSize: number
  fileUrl: string
  fileName: string
  fileExt: string
  fileSize: number
  downloads: number
  feeLevel: string
  accessLevel: string
  isRecommended: boolean
  keywords: string[]
  source: string
}

type UploadApiResponse = {
  code: number
  message: string
  data?: {
    url: string
    fileName?: string
    fileExt?: string
    fileSize?: number
  }
}

const createDefaultForm = () => ({
  title: '',
  country: '',
  industry: '',
  type: '',
  year: '',
  publishDate: '',
  summary: '',
  keywords: [] as string[],
  source: '',
  feeLevel: 'free',
  accessLevel: 'public',
  isRecommended: false
})

const reportForm = reactive(createDefaultForm())
const reportList = ref<ReportItem[]>([])
const tableLoading = ref(false)
const submitLoading = ref(false)
const editId = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const coverMeta = ref<UploadMeta | null>(null)
const reportMeta = ref<UploadMeta | null>(null)
const coverFileList = ref<UploadUserFile[]>([])
const reportFileList = ref<UploadUserFile[]>([])
const expireDialogVisible = ref(false)
const expireSubmitting = ref(false)
const expireTargetId = ref<number | null>(null)
const expireDate = ref('')

const filters = reactive({
  keyword: '',
  country: '',
  industry: '',
  type: '',
  feeLevel: '',
  publishRange: [] as string[]
})

const isEdit = computed(() => editId.value !== null)
const currentPageDownloads = computed(() => reportList.value.reduce((sum, item) => sum + item.downloads, 0))

const normalizeUploadMeta = (raw: any, fallbackName = ''): UploadMeta => ({
  fileUrl: String(raw?.fileUrl || raw?.url || ''),
  fileName: String(raw?.fileName || raw?.name || fallbackName || '未命名文件'),
  previewUrl: resolveFileUrl(String(raw?.fileUrl || raw?.url || '')),
  fileExt: String(raw?.fileExt || '').toLowerCase(),
  fileSize: Number(raw?.fileSize || 0)
})

const toUploadUserFile = (file: UploadMeta): UploadUserFile => ({
  name: file.fileName,
  url: file.previewUrl || resolveFileUrl(file.fileUrl),
  status: 'success'
})

const validateUpload = (rawFile: File, allowedExts: string[], maxMb: number, label: string) => {
  const ext = rawFile.name.split('.').pop()?.toLowerCase() || ''
  if (!allowedExts.includes(ext)) {
    ElMessage.warning(`${label}仅支持 ${allowedExts.join('/').toUpperCase()} 格式`)
    return false
  }
  if (rawFile.size > maxMb * 1024 * 1024) {
    ElMessage.warning(`${label}大小不能超过 ${maxMb}MB`)
    return false
  }
  return true
}

const beforeCoverUpload = (rawFile: File) => validateUpload(rawFile, ['jpg', 'jpeg', 'png', 'webp'], 5, '封面图')
const beforeReportFileUpload = (rawFile: File) => validateUpload(rawFile, ['pdf', 'doc', 'docx'], 20, '报告文件')

const uploadCommonFile = async (options: UploadRequestOptions, label: string) => {
  const formData = new FormData()
  formData.append('file', options.file)
  const response = await apiFetchRaw<UploadApiResponse>('/common/upload', {
    method: 'POST',
    body: formData
  })
  if (Number(response?.code) !== 200 || !response?.data?.url) {
    throw new Error(response?.message || `${label}上传失败`)
  }
  return normalizeUploadMeta(response.data, options.file.name)
}

const uploadCoverFile = async (options: UploadRequestOptions) => {
  try {
    const uploaded = await uploadCommonFile(options, '封面图')
    coverMeta.value = uploaded
    coverFileList.value = [toUploadUserFile(uploaded)]
    options.onSuccess?.(uploaded as any)
    ElMessage.success('封面上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '封面上传失败')
  }
}

const uploadReportFile = async (options: UploadRequestOptions) => {
  try {
    const uploaded = await uploadCommonFile(options, '报告文件')
    reportMeta.value = uploaded
    reportFileList.value = [toUploadUserFile(uploaded)]
    options.onSuccess?.(uploaded as any)
    ElMessage.success('报告文件上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '报告文件上传失败')
  }
}

const handleCoverRemove = () => {
  coverMeta.value = null
  coverFileList.value = []
}

const handleReportFileRemove = () => {
  reportMeta.value = null
  reportFileList.value = []
}

const accessTagType = (level: string) => ({
  public: 'success',
  vip: 'warning',
  svip: 'danger'
} as Record<string, string>)[level] || 'info'

const formatFileSize = (size = 0) => {
  if (!size) return '-'
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

const mapReportItem = (raw: any): ReportItem => ({
  id: Number(raw?.id || 0),
  title: String(raw?.title || ''),
  country: String(raw?.country || ''),
  industry: String(raw?.industry || raw?.industryCode || ''),
  type: String(raw?.type || raw?.reportType || ''),
  year: String(raw?.year || ''),
  publishDate: String(raw?.publishDate || ''),
  expireDate: String(raw?.expireDate || ''),
  summary: String(raw?.summary || ''),
  coverImage: String(raw?.coverImage || ''),
  coverImageName: String(raw?.coverImageName || ''),
  coverImageExt: String(raw?.coverImageExt || ''),
  coverImageSize: Number(raw?.coverImageSize || 0),
  fileUrl: String(raw?.fileUrl || raw?.reportFileUrl || ''),
  fileName: String(raw?.fileName || raw?.reportFileName || ''),
  fileExt: String(raw?.fileExt || raw?.reportFileExt || ''),
  fileSize: Number(raw?.fileSize || raw?.reportFileSize || 0),
  downloads: Number(raw?.downloads || 0),
  feeLevel: String(raw?.feeLevel || 'free'),
  accessLevel: String(raw?.accessLevel || 'public'),
  isRecommended: Boolean(raw?.isRecommended),
  keywords: Array.isArray(raw?.keywords) ? raw.keywords.map((item: any) => String(item)) : [],
  source: String(raw?.source || '')
})

const loadList = async () => {
  tableLoading.value = true
  try {
    const query: Record<string, any> = {
      page: page.value,
      page_size: pageSize.value
    }
    if (filters.keyword) query.keyword = filters.keyword
    if (filters.country) query.country = filters.country
    if (filters.industry) query.industry = filters.industry
    if (filters.type) query.type = filters.type
    if (filters.feeLevel) query.fee_level = filters.feeLevel
    if (filters.publishRange.length === 2) {
      query.publish_start = filters.publishRange[0]
      query.publish_end = filters.publishRange[1]
    }

    const res = await apiRequest<any>('/v3/admin/overseas/reports', { query })
    reportList.value = Array.isArray(res.data?.items) ? res.data.items.map(mapReportItem) : []
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error?.message || '报告列表加载失败')
  } finally {
    tableLoading.value = false
  }
}

const validateForm = () => {
  if (!reportForm.title.trim() || !reportForm.country || !reportForm.industry || !reportForm.type) {
    return '请填写标题、国家、行业和报告类型'
  }
  if (!reportForm.publishDate || !reportForm.summary.trim()) {
    return '请填写发布时间和报告摘要'
  }
  if (!reportMeta.value?.fileUrl) {
    return '请上传报告文件'
  }
  return ''
}

const buildPayload = () => ({
  title: reportForm.title.trim(),
  country: reportForm.country,
  industry: reportForm.industry,
  type: reportForm.type,
  year: reportForm.year,
  publishDate: reportForm.publishDate,
  summary: reportForm.summary.trim(),
  keywords: reportForm.keywords.filter(Boolean),
  source: reportForm.source.trim(),
  feeLevel: reportForm.feeLevel,
  accessLevel: reportForm.accessLevel,
  isRecommended: reportForm.isRecommended,
  cover: coverMeta.value ? { ...coverMeta.value } : {},
  reportFile: reportMeta.value ? { ...reportMeta.value } : {}
})

const submitForm = async () => {
  const message = validateForm()
  if (message) {
    ElMessage.warning(message)
    return
  }

  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (editId.value) {
      await apiRequest(`/v3/admin/overseas/reports/${editId.value}`, {
        method: 'PUT',
        body: payload
      })
      ElMessage.success('报告修改成功')
    } else {
      await apiRequest('/v3/admin/overseas/reports', {
        method: 'POST',
        body: payload
      })
      ElMessage.success('报告上传成功')
    }
    resetForm()
    page.value = 1
    await loadList()
  } catch (error: any) {
    ElMessage.error(error?.message || '报告保存失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(reportForm, createDefaultForm())
  editId.value = null
  coverMeta.value = null
  reportMeta.value = null
  coverFileList.value = []
  reportFileList.value = []
}

const startEdit = (row: ReportItem) => {
  editId.value = row.id
  Object.assign(reportForm, {
    title: row.title,
    country: row.country,
    industry: row.industry,
    type: row.type,
    year: row.year,
    publishDate: row.publishDate,
    summary: row.summary,
    keywords: [...row.keywords],
    source: row.source,
    feeLevel: row.feeLevel,
    accessLevel: row.accessLevel,
    isRecommended: row.isRecommended
  })
  coverMeta.value = row.coverImage ? normalizeUploadMeta({
    fileName: row.coverImageName || 'cover',
    fileUrl: row.coverImage,
    fileExt: row.coverImageExt,
    fileSize: row.coverImageSize
  }) : null
  reportMeta.value = row.fileUrl ? normalizeUploadMeta({
    fileName: row.fileName,
    fileUrl: row.fileUrl,
    fileExt: row.fileExt,
    fileSize: row.fileSize
  }) : null
  coverFileList.value = coverMeta.value ? [toUploadUserFile(coverMeta.value)] : []
  reportFileList.value = reportMeta.value ? [toUploadUserFile(reportMeta.value)] : []
  if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
}

const removeItem = async (row: ReportItem) => {
  try {
    await ElMessageBox.confirm(`确定删除报告“${row.title}”吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiRequest(`/v3/admin/overseas/reports/${row.id}`, { method: 'DELETE' })
    ElMessage.success('报告已删除')
    await loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '报告删除失败')
    }
  }
}

const downloadReport = async (row: ReportItem) => {
  try {
    const res = await apiRequest<any>(`/v3/admin/overseas/reports/${row.id}/download`, {
      method: 'POST'
    })
    const url = resolveFileUrl(String(res.data?.fileUrl || row.fileUrl || ''))
    if (url && import.meta.client) {
      window.open(url, '_blank')
    }
    row.downloads = Number(res.data?.downloads || row.downloads + 1)
    ElMessage.success('下载记录已更新')
  } catch (error: any) {
    ElMessage.error(error?.message || '报告下载失败')
  }
}

const openExpireDialog = (row: ReportItem) => {
  expireTargetId.value = row.id
  expireDate.value = row.expireDate || ''
  expireDialogVisible.value = true
}

const saveExpireDate = async () => {
  if (!expireTargetId.value) return
  expireSubmitting.value = true
  try {
    await apiRequest(`/v3/admin/overseas/reports/${expireTargetId.value}/expire`, {
      method: 'POST',
      body: { expireDate: expireDate.value || null }
    })
    ElMessage.success('截止日期已更新')
    expireDialogVisible.value = false
    await loadList()
  } catch (error: any) {
    ElMessage.error(error?.message || '截止日期更新失败')
  } finally {
    expireSubmitting.value = false
  }
}

const handleSearch = async () => {
  page.value = 1
  await loadList()
}

const resetFilters = async () => {
  Object.assign(filters, {
    keyword: '',
    country: '',
    industry: '',
    type: '',
    feeLevel: '',
    publishRange: []
  })
  page.value = 1
  await loadList()
}

const exportCurrentList = () => {
  if (!reportList.value.length) {
    ElMessage.warning('当前页没有可导出的报告')
    return
  }

  const escapeCsv = (value: unknown) => {
    const normalized = String(value ?? '').replace(/\u0000/g, '').replace(/\r?\n/g, ' ').replace(/"/g, '""')
    return `"${normalized}"`
  }

  const rows = [
    ['报告标题', '国家/地区', '行业', '报告类型', '年份', '发布时间', '截止日期', '下载量', '访问权限', '收费类型', '来源', '关键词'],
    ...reportList.value.map(item => ([
      item.title,
      item.country,
      OVERSEAS_REPORT_INDUSTRY_LABEL_MAP[item.industry] || item.industry,
      OVERSEAS_REPORT_TYPE_LABEL_MAP[item.type] || item.type,
      item.year || '-',
      item.publishDate,
      item.expireDate || '-',
      item.downloads,
      OVERSEAS_REPORT_ACCESS_LEVEL_LABEL_MAP[item.accessLevel] || item.accessLevel,
      OVERSEAS_REPORT_FEE_LEVEL_LABEL_MAP[item.feeLevel] || item.feeLevel,
      item.source || '-',
      item.keywords.join('; ')
    ]))
  ]

  const csv = rows.map(row => row.map(escapeCsv).join(',')).join('\r\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  if (!import.meta.client) return
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `行业报告清单_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  setTimeout(() => URL.revokeObjectURL(url), 100)
  ElMessage.success('导出成功')
}

onMounted(loadList)
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
