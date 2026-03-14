<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex items-start justify-between gap-4 mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">
            {{ isEdit ? `编辑服务：${serviceForm.title || '未命名服务'}` : '发布出海服务信息' }}
          </h3>
          <p class="text-xs text-slate-500 mt-1">数据已接入 `/v3/admin/overseas/services`，保存后可持久化</p>
        </div>
        <el-button v-if="isEdit" type="warning" plain @click="resetForm">取消编辑</el-button>
      </div>

      <el-form label-width="120px" class="max-w-4xl">
        <el-form-item label="服务类型" required>
          <el-select v-model="serviceForm.type" class="w-full" placeholder="请选择服务类型">
            <el-option v-for="item in OVERSEAS_SERVICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务项目" required>
          <el-select v-model="serviceForm.project" class="w-full" placeholder="请选择服务项目">
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="主题描述" required>
          <el-input v-model="serviceForm.title" maxlength="15" show-word-limit placeholder="请输入主题描述（限15字）" />
        </el-form-item>

        <el-form-item label="目标国家/地区" required>
          <el-select
            v-model="serviceForm.countries"
            multiple
            filterable
            allow-create
            default-first-option
            class="w-full"
            placeholder="请选择或输入国家/地区"
          >
            <el-option v-for="item in OVERSEAS_COUNTRY_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务简介" required>
          <el-input v-model="serviceForm.summary" type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="内容摘要（100字以内）" />
        </el-form-item>

        <el-form-item label="服务详情" required>
          <el-input v-model="serviceForm.content" type="textarea" :rows="8" maxlength="2000" show-word-limit placeholder="详细介绍服务内容、流程、优势等" />
        </el-form-item>

        <el-form-item label="宣传图上传" required>
          <el-upload
            v-model:file-list="promoFileList"
            list-type="picture-card"
            :limit="1"
            :http-request="uploadPromoImage"
            :before-upload="beforePromoUpload"
            :on-remove="handlePromoRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">建议上传 1 张 480x480 宣传图</div>
        </el-form-item>

        <el-form-item label="服务介绍上传">
          <el-upload
            v-model:file-list="introFileList"
            :limit="3"
            :http-request="uploadIntroFile"
            :before-upload="beforeIntroUpload"
            :on-remove="handleIntroRemove"
            accept=".pdf,.jpg,.jpeg"
          >
            <el-button type="primary" plain>
              <el-icon class="mr-2"><Plus /></el-icon>上传 PDF/JPEG
            </el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="服务价格">
          <el-radio-group v-model="serviceForm.priceType">
            <el-radio label="free">免费</el-radio>
            <el-radio label="fixed">固定价格</el-radio>
            <el-radio label="negotiable">价格面议</el-radio>
          </el-radio-group>
          <el-input-number v-if="serviceForm.priceType === 'fixed'" v-model="serviceForm.price" :min="0" :step="100" class="ml-4" />
          <span v-if="serviceForm.priceType === 'fixed'" class="ml-2 text-sm text-slate-500">元</span>
        </el-form-item>

        <el-form-item label="服务周期">
          <el-input v-model="serviceForm.duration" placeholder="如：3-5个工作日" />
        </el-form-item>

        <el-form-item label="联系方式">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 w-full">
            <el-input v-model="serviceForm.contactPerson" placeholder="联系人" />
            <el-input v-model="serviceForm.contactPhone" placeholder="联系电话" />
            <el-input v-model="serviceForm.contactEmail" placeholder="联系邮箱" class="md:col-span-2" />
          </div>
        </el-form-item>

        <el-form-item label="服务图片">
          <el-upload
            v-model:file-list="galleryFileList"
            list-type="picture-card"
            :limit="5"
            :http-request="uploadGalleryImage"
            :before-upload="beforeGalleryUpload"
            :on-remove="handleGalleryRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">最多 5 张，用于详情展示</div>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="serviceForm.tags" multiple allow-create filterable class="w-full" placeholder="输入或选择标签">
            <el-option v-for="item in OVERSEAS_SERVICE_TAG_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-radio-group v-model="serviceForm.status">
            <el-radio label="draft">保存草稿</el-radio>
            <el-radio label="published">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">
            <el-icon class="mr-2"><Check /></el-icon>{{ isEdit ? '保存修改' : serviceForm.status === 'draft' ? '保存草稿' : '发布服务' }}
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
            <h3 class="text-lg font-bold text-slate-800">出海服务列表</h3>
            <p class="text-xs text-slate-500 mt-1">支持真实查询、编辑、下架、删除</p>
          </div>
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="已发布" name="published" />
            <el-tab-pane label="草稿箱" name="draft" />
            <el-tab-pane label="已下架" name="offline" />
          </el-tabs>
        </div>

        <div class="flex gap-3 flex-wrap mt-4">
          <el-input v-model="filters.keyword" class="w-64" clearable placeholder="搜索服务标题/摘要" :prefix-icon="Search" />
          <el-select v-model="filters.country" class="w-36" clearable filterable placeholder="服务国家">
            <el-option v-for="item in OVERSEAS_COUNTRY_OPTIONS" :key="item" :label="item" :value="item" />
          </el-select>
          <el-select v-model="filters.type" class="w-36" clearable placeholder="服务类型">
            <el-option v-for="item in OVERSEAS_SERVICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="filters.project" class="w-44" clearable placeholder="服务项目">
            <el-option v-for="item in filterProjectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table v-loading="tableLoading" :data="serviceList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
          <el-table-column label="服务信息" min-width="360">
            <template #default="{ row }">
              <div class="flex gap-3 py-2">
                <el-image
                  v-if="row.promoImageUrl"
                  :src="row.promoImageUrl"
                  fit="cover"
                  class="w-20 h-20 rounded-lg border border-slate-200 flex-shrink-0"
                  :preview-src-list="[row.promoImageUrl]"
                />
                <div v-else class="w-20 h-20 rounded-lg border border-dashed border-slate-200 bg-slate-50 flex items-center justify-center text-xs text-slate-400 flex-shrink-0">
                  无图
                </div>
                <div class="min-w-0 flex-1">
                  <div class="font-bold text-slate-800">{{ row.title }}</div>
                  <div class="text-xs text-slate-500 mt-1 line-clamp-2">{{ row.summary || '-' }}</div>
                  <div class="flex gap-2 flex-wrap mt-2">
                    <el-tag size="small" :type="OVERSEAS_SERVICE_TYPE_TAG_MAP[row.type] || 'info'">
                      {{ OVERSEAS_SERVICE_TYPE_LABEL_MAP[row.type] || row.type }}
                    </el-tag>
                    <el-tag size="small" type="warning" effect="plain">
                      {{ OVERSEAS_PROJECT_MAP[row.project]?.label || row.project }}
                    </el-tag>
                    <el-tag v-for="country in row.countries.slice(0, 2)" :key="country" size="small" type="info" effect="plain">
                      {{ country }}
                    </el-tag>
                    <el-tag v-if="row.countries.length > 2" size="small" type="info" effect="plain">
                      +{{ row.countries.length - 2 }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="价格" width="120">
            <template #default="{ row }">
              <span v-if="row.priceType === 'free'" class="text-green-600 font-bold">免费</span>
              <span v-else-if="row.priceType === 'fixed'" class="text-blue-600 font-bold">¥{{ row.price || 0 }}</span>
              <span v-else class="text-orange-600 font-medium">面议</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="浏览量" width="90" align="center">
            <template #default="{ row }">{{ row.views }}</template>
          </el-table-column>

          <el-table-column label="咨询数" width="90" align="center">
            <template #default="{ row }">{{ row.inquiries }}</template>
          </el-table-column>

          <el-table-column prop="publishDate" label="发布时间" width="120" />

          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row.id)">查看</el-button>
              <el-button link type="warning" size="small" @click="startEdit(row.id)">编辑</el-button>
              <el-button
                v-if="row.status !== 'offline'"
                link
                type="info"
                size="small"
                @click="changeStatus(row.id, 'offline', '确认下架该服务吗？')"
              >
                下架
              </el-button>
              <el-button
                v-else
                link
                type="success"
                size="small"
                @click="changeStatus(row.id, 'published', '确认重新发布该服务吗？')"
              >
                发布
              </el-button>
              <el-button link type="danger" size="small" @click="removeItem(row.id, row.title)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="服务详情" width="780px" :close-on-click-modal="false">
      <div v-loading="detailLoading" class="space-y-4">
        <template v-if="detailItem">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="detail-item">
              <div class="detail-label">服务类型</div>
              <div class="detail-value">{{ OVERSEAS_SERVICE_TYPE_LABEL_MAP[detailItem.type] || detailItem.type }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">服务项目</div>
              <div class="detail-value">{{ OVERSEAS_PROJECT_MAP[detailItem.project]?.label || detailItem.project }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">主题描述</div>
              <div class="detail-value">{{ detailItem.title || '-' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">目标国家/地区</div>
              <div class="detail-value">{{ detailItem.countries.join(' / ') || '-' }}</div>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">服务简介</div>
            <div class="detail-value">{{ detailItem.summary || '-' }}</div>
          </div>

          <div class="detail-item">
            <div class="detail-label">服务详情</div>
            <div class="detail-value whitespace-pre-wrap">{{ detailItem.content || '-' }}</div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="detail-item">
              <div class="detail-label">价格</div>
              <div class="detail-value">
                <template v-if="detailItem.priceType === 'free'">免费</template>
                <template v-else-if="detailItem.priceType === 'fixed'">固定价格 ¥{{ detailItem.price || 0 }}</template>
                <template v-else>价格面议</template>
              </div>
            </div>
            <div class="detail-item">
              <div class="detail-label">服务周期</div>
              <div class="detail-value">{{ detailItem.duration || '-' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">联系人</div>
              <div class="detail-value">{{ detailItem.contactPerson || '-' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">联系电话</div>
              <div class="detail-value">{{ detailItem.contactPhone || '-' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">联系邮箱</div>
              <div class="detail-value">{{ detailItem.contactEmail || '-' }}</div>
            </div>
            <div class="detail-item">
              <div class="detail-label">状态</div>
              <div class="detail-value">{{ statusLabel(detailItem.status) }}</div>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">宣传图</div>
            <div class="detail-value">
              <div v-if="detailItem.promoImage.length" class="flex flex-wrap gap-2">
                <el-image
                  v-for="item in detailItem.promoImage"
                  :key="item.fileUrl"
                  :src="item.fileUrl"
                  fit="cover"
                  class="detail-image"
                  :preview-src-list="detailItem.promoImage.map(file => file.fileUrl)"
                />
              </div>
              <span v-else>-</span>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">服务图片</div>
            <div class="detail-value">
              <div v-if="detailItem.images.length" class="flex flex-wrap gap-2">
                <el-image
                  v-for="item in detailItem.images"
                  :key="item.fileUrl"
                  :src="item.fileUrl"
                  fit="cover"
                  class="detail-image"
                  :preview-src-list="detailItem.images.map(file => file.fileUrl)"
                />
              </div>
              <span v-else>-</span>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">介绍文件</div>
            <div class="detail-value">
              <div v-if="detailItem.introFiles.length" class="space-y-2">
                <div v-for="item in detailItem.introFiles" :key="item.fileUrl" class="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2">
                  <div>
                    <div class="font-medium text-slate-700">{{ item.fileName }}</div>
                    <div class="text-xs text-slate-400">{{ formatFileSize(item.fileSize) }}</div>
                  </div>
                  <el-button type="primary" link @click="openFile(item.fileUrl)">下载</el-button>
                </div>
              </div>
              <span v-else>-</span>
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
import { Plus, Check, RefreshLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { apiFetchRaw, apiRequest } from '@/utils/request'
import {
  OVERSEAS_COUNTRY_OPTIONS,
  OVERSEAS_PROJECT_MAP,
  OVERSEAS_SERVICE_TAG_OPTIONS,
  OVERSEAS_SERVICE_TYPE_LABEL_MAP,
  OVERSEAS_SERVICE_TYPE_OPTIONS,
  OVERSEAS_SERVICE_TYPE_TAG_MAP
} from '@/composables/useOverseasAdminCatalog'

definePageMeta({ layout: 'admin' })

type UploadMeta = {
  fileName: string
  fileUrl: string
  fileExt: string
  fileSize: number
}

type ServiceItem = {
  id: number
  type: string
  project: string
  title: string
  countries: string[]
  summary: string
  content: string
  priceType: string
  price: number
  duration: string
  contactPerson: string
  contactPhone: string
  contactEmail: string
  tags: string[]
  status: string
  views: number
  inquiries: number
  publishDate: string
  promoImageUrl: string
  promoImage: UploadMeta[]
  images: UploadMeta[]
  introFiles: UploadMeta[]
}

type UploadApiResponse = {
  code: number
  message: string
  data?: {
    url: string
    fileName?: string
    storedName?: string
    fileExt?: string
    fileSize?: number
  }
}

const createDefaultForm = () => ({
  type: '',
  project: '',
  title: '',
  countries: [] as string[],
  summary: '',
  content: '',
  priceType: 'negotiable',
  price: 0,
  duration: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  tags: [] as string[],
  status: 'published'
})

const serviceForm = reactive(createDefaultForm())
const submitLoading = ref(false)
const tableLoading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const detailItem = ref<ServiceItem | null>(null)
const serviceList = ref<ServiceItem[]>([])
const editId = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('published')

const filters = reactive({
  keyword: '',
  country: '',
  type: '',
  project: ''
})

const promoMeta = ref<UploadMeta[]>([])
const galleryMeta = ref<UploadMeta[]>([])
const introMeta = ref<UploadMeta[]>([])
const promoFileList = ref<UploadUserFile[]>([])
const galleryFileList = ref<UploadUserFile[]>([])
const introFileList = ref<UploadUserFile[]>([])

const isEdit = computed(() => editId.value !== null)
const projectOptions = computed(() => {
  const entries = Object.entries(OVERSEAS_PROJECT_MAP)
  return entries
    .filter(([, item]) => !serviceForm.type || item.type === serviceForm.type)
    .map(([value, item]) => ({ value, label: item.label }))
})

const filterProjectOptions = computed(() => {
  const entries = Object.entries(OVERSEAS_PROJECT_MAP)
  return entries
    .filter(([, item]) => !filters.type || item.type === filters.type)
    .map(([value, item]) => ({ value, label: item.label }))
})

const normalizeUploadMeta = (raw: any, fallbackName = ''): UploadMeta => ({
  fileName: String(raw?.fileName || raw?.name || fallbackName || '未命名文件'),
  fileUrl: String(raw?.fileUrl || raw?.url || ''),
  fileExt: String(raw?.fileExt || '').toLowerCase(),
  fileSize: Number(raw?.fileSize || 0)
})

const toUploadUserFile = (file: UploadMeta): UploadUserFile => ({
  name: file.fileName,
  url: file.fileUrl,
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

const beforePromoUpload = (rawFile: File) => validateUpload(rawFile, ['jpg', 'jpeg', 'png', 'webp'], 5, '宣传图')
const beforeGalleryUpload = (rawFile: File) => validateUpload(rawFile, ['jpg', 'jpeg', 'png', 'webp'], 5, '服务图片')
const beforeIntroUpload = (rawFile: File) => validateUpload(rawFile, ['pdf', 'jpg', 'jpeg'], 10, '服务介绍')

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

const uploadPromoImage = async (options: UploadRequestOptions) => {
  try {
    const uploaded = await uploadCommonFile(options, '宣传图')
    promoMeta.value = [uploaded]
    promoFileList.value = [toUploadUserFile(uploaded)]
    options.onSuccess?.(uploaded as any)
    ElMessage.success('宣传图上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '宣传图上传失败')
  }
}

const uploadGalleryImage = async (options: UploadRequestOptions) => {
  try {
    const uploaded = await uploadCommonFile(options, '服务图片')
    galleryMeta.value = [...galleryMeta.value, uploaded]
    galleryFileList.value = galleryMeta.value.map(toUploadUserFile)
    options.onSuccess?.(uploaded as any)
    ElMessage.success('服务图片上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '服务图片上传失败')
  }
}

const uploadIntroFile = async (options: UploadRequestOptions) => {
  try {
    const uploaded = await uploadCommonFile(options, '服务介绍')
    introMeta.value = [...introMeta.value, uploaded]
    introFileList.value = introMeta.value.map(toUploadUserFile)
    options.onSuccess?.(uploaded as any)
    ElMessage.success('服务介绍上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '服务介绍上传失败')
  }
}

const syncUploadState = (promo: UploadMeta[] = [], gallery: UploadMeta[] = [], intro: UploadMeta[] = []) => {
  promoMeta.value = [...promo]
  galleryMeta.value = [...gallery]
  introMeta.value = [...intro]
  promoFileList.value = promo.map(toUploadUserFile)
  galleryFileList.value = gallery.map(toUploadUserFile)
  introFileList.value = intro.map(toUploadUserFile)
}

const handlePromoRemove = () => {
  promoMeta.value = []
  promoFileList.value = []
}

const handleGalleryRemove = (file: UploadUserFile) => {
  galleryMeta.value = galleryMeta.value.filter(item => item.fileUrl !== file.url && item.fileName !== file.name)
  galleryFileList.value = galleryMeta.value.map(toUploadUserFile)
}

const handleIntroRemove = (file: UploadUserFile) => {
  introMeta.value = introMeta.value.filter(item => item.fileUrl !== file.url && item.fileName !== file.name)
  introFileList.value = introMeta.value.map(toUploadUserFile)
}

const statusLabel = (status: string) => ({
  published: '已发布',
  draft: '草稿',
  offline: '已下架'
} as Record<string, string>)[status] || status

const statusTagType = (status: string) => ({
  published: 'success',
  draft: 'info',
  offline: 'warning'
} as Record<string, string>)[status] || 'info'

const formatFileSize = (size = 0) => {
  if (!size) return '-'
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

const openFile = (url: string) => {
  if (!url || !import.meta.client) return
  window.open(url, '_blank')
}

const mapServiceItem = (raw: any): ServiceItem => ({
  id: Number(raw?.id || 0),
  type: String(raw?.type || raw?.serviceType || ''),
  project: String(raw?.project || raw?.projectCode || ''),
  title: String(raw?.title || ''),
  countries: Array.isArray(raw?.countries) ? raw.countries.map((item: any) => String(item)) : [],
  summary: String(raw?.summary || ''),
  content: String(raw?.content || ''),
  priceType: String(raw?.priceType || 'negotiable'),
  price: Number(raw?.price || 0),
  duration: String(raw?.duration || ''),
  contactPerson: String(raw?.contactPerson || ''),
  contactPhone: String(raw?.contactPhone || ''),
  contactEmail: String(raw?.contactEmail || ''),
  tags: Array.isArray(raw?.tags) ? raw.tags.map((item: any) => String(item)) : [],
  status: String(raw?.status || 'draft'),
  views: Number(raw?.views || 0),
  inquiries: Number(raw?.inquiries || 0),
  publishDate: String(raw?.publishDate || ''),
  promoImageUrl: String(raw?.promoImageUrl || raw?.promoImage?.[0]?.fileUrl || ''),
  promoImage: Array.isArray(raw?.promoImage) ? raw.promoImage.map((item: any) => normalizeUploadMeta(item)) : [],
  images: Array.isArray(raw?.images) ? raw.images.map((item: any) => normalizeUploadMeta(item)) : [],
  introFiles: Array.isArray(raw?.introFiles) ? raw.introFiles.map((item: any) => normalizeUploadMeta(item)) : []
})

const loadList = async () => {
  tableLoading.value = true
  try {
    const query: Record<string, any> = {
      page: page.value,
      page_size: pageSize.value,
      status: activeTab.value
    }
    if (filters.keyword) query.keyword = filters.keyword
    if (filters.country) query.country = filters.country
    if (filters.type) query.service_type = filters.type
    if (filters.project) query.project = filters.project

    const res = await apiRequest<any>('/v3/admin/overseas/services', { query })
    serviceList.value = Array.isArray(res.data?.items) ? res.data.items.map(mapServiceItem) : []
    total.value = Number(res.data?.total || 0)
  } catch (error: any) {
    ElMessage.error(error?.message || '服务列表加载失败')
  } finally {
    tableLoading.value = false
  }
}

const loadDetail = async (id: number) => {
  const res = await apiRequest<any>(`/v3/admin/overseas/services/${id}`)
  return mapServiceItem(res.data)
}

const openDetail = async (id: number) => {
  detailVisible.value = true
  detailLoading.value = true
  detailItem.value = null
  try {
    detailItem.value = await loadDetail(id)
  } catch (error: any) {
    detailVisible.value = false
    ElMessage.error(error?.message || '服务详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const startEdit = async (id: number) => {
  detailLoading.value = true
  try {
    const detail = await loadDetail(id)
    editId.value = detail.id
    Object.assign(serviceForm, {
      type: detail.type,
      project: detail.project,
      title: detail.title,
      countries: [...detail.countries],
      summary: detail.summary,
      content: detail.content,
      priceType: detail.priceType,
      price: detail.price || 0,
      duration: detail.duration,
      contactPerson: detail.contactPerson,
      contactPhone: detail.contactPhone,
      contactEmail: detail.contactEmail,
      tags: [...detail.tags],
      status: detail.status
    })
    syncUploadState(detail.promoImage, detail.images, detail.introFiles)
    if (import.meta.client) window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error: any) {
    ElMessage.error(error?.message || '服务详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const buildPayload = () => ({
  type: serviceForm.type,
  project: serviceForm.project,
  title: serviceForm.title.trim(),
  countries: serviceForm.countries.filter(Boolean),
  summary: serviceForm.summary.trim(),
  content: serviceForm.content.trim(),
  priceType: serviceForm.priceType,
  price: serviceForm.priceType === 'fixed' ? Number(serviceForm.price || 0) : 0,
  duration: serviceForm.duration.trim(),
  contactPerson: serviceForm.contactPerson.trim(),
  contactPhone: serviceForm.contactPhone.trim(),
  contactEmail: serviceForm.contactEmail.trim(),
  tags: serviceForm.tags.filter(Boolean),
  status: serviceForm.status,
  promoImage: promoMeta.value,
  images: galleryMeta.value,
  introFiles: introMeta.value
})

const validateForm = () => {
  if (!serviceForm.type || !serviceForm.project || !serviceForm.title.trim()) {
    return '请填写服务类型、服务项目、主题描述'
  }
  if (!serviceForm.countries.length || !serviceForm.summary.trim() || !serviceForm.content.trim()) {
    return '请填写目标国家、服务简介和服务详情'
  }
  if (!promoMeta.value.length) {
    return '请上传宣传图'
  }
  if (serviceForm.priceType === 'fixed' && Number(serviceForm.price || 0) <= 0) {
    return '固定价格需填写有效金额'
  }
  return ''
}

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
      await apiRequest(`/v3/admin/overseas/services/${editId.value}`, {
        method: 'PUT',
        body: payload
      })
      ElMessage.success('服务修改成功')
    } else {
      await apiRequest('/v3/admin/overseas/services', {
        method: 'POST',
        body: payload
      })
      ElMessage.success(serviceForm.status === 'draft' ? '草稿保存成功' : '服务发布成功')
    }
    activeTab.value = serviceForm.status
    page.value = 1
    resetForm()
    await loadList()
  } catch (error: any) {
    ElMessage.error(error?.message || '服务保存失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(serviceForm, createDefaultForm())
  editId.value = null
  syncUploadState()
}

const handleSearch = async () => {
  page.value = 1
  await loadList()
}

const resetFilters = async () => {
  Object.assign(filters, { keyword: '', country: '', type: '', project: '' })
  page.value = 1
  await loadList()
}

const handleTabChange = async () => {
  page.value = 1
  await loadList()
}

const changeStatus = async (id: number, status: 'published' | 'offline', title: string) => {
  try {
    await ElMessageBox.confirm(title, '状态确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiRequest(`/v3/admin/overseas/services/${id}`, {
      method: 'PUT',
      body: { status }
    })
    ElMessage.success(status === 'offline' ? '服务已下架' : '服务已发布')
    await loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '状态更新失败')
    }
  }
}

const removeItem = async (id: number, title: string) => {
  try {
    await ElMessageBox.confirm(`确定删除服务“${title}”吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiRequest(`/v3/admin/overseas/services/${id}`, { method: 'DELETE' })
    ElMessage.success('服务已删除')
    await loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '服务删除失败')
    }
  }
}

onMounted(loadList)
</script>

<style scoped>
.detail-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  background: #fff;
}

.detail-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.detail-value {
  color: #1e293b;
  line-height: 1.7;
}

.detail-image {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
