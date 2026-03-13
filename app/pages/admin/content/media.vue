<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\media.vue
  版本: V1.1（2026-02-27 收敛版）
  说明:
  1. 列表/详情/创建/编辑统一接入 GET|POST|PUT /v3/admin/contents
  2. 审核/上下架改为真实状态流转：POST /v3/admin/contents/{id}/transition
  3. 正文与扩展元数据分离：正文使用 body，分类/来源/作者使用 extra_json
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between"
      >
        <div>
          <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        </div>
        <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
          <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">媒体发布管理</h3>
            <p class="text-xs text-slate-500 mt-1">发布与管理平台新闻、行业动态、政策解读</p>
          </div>
          <el-button type="primary" color="#0f172a" @click="openPublishDialog">
            <el-icon class="mr-2"><Plus /></el-icon> 发布新闻
          </el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索标题/编号..."
            prefix-icon="Search"
            class="w-64"
            clearable
          />
          <el-select v-model="filters.category" placeholder="分类筛选" class="w-32" clearable>
            <el-option label="平台新闻" value="platform" />
            <el-option label="行业动态" value="industry" />
            <el-option label="政策解读" value="policy" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态筛选" class="w-32" clearable>
            <el-option label="已发布" value="published" />
            <el-option label="审核中" value="auditing" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已下架" value="offline" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>

        <el-alert
          v-if="currentUserRole === 'partner'"
          title="合作伙伴发布须知"
          type="warning"
          :closable="false"
          class="mt-4"
        >
          <div class="text-sm">
            合作伙伴发布的内容会先进入审核流程，审核通过后才会前台可见。
          </div>
        </el-alert>
      </div>

      <div class="p-6">
        <el-table
          v-loading="apiLoading"
          :data="filteredNewsList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
        >
          <el-table-column prop="newsNo" label="编号" width="160">
            <template #default="scope">
              <span class="font-mono text-xs text-slate-500">{{ scope.row.newsNo }}</span>
            </template>
          </el-table-column>

          <el-table-column label="新闻概览" min-width="350">
            <template #default="scope">
              <div class="flex gap-3 py-2">
                <div class="relative w-20 h-14 rounded overflow-hidden flex-shrink-0 bg-slate-100">
                  <img :src="scope.row.coverImage" class="w-full h-full object-cover" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-slate-800 text-sm line-clamp-1">{{ scope.row.title }}</div>
                  <div class="text-xs text-slate-500 mt-1 line-clamp-1">{{ scope.row.summary }}</div>
                  <div class="flex gap-2 mt-1">
                    <el-tag size="small" :type="getCategoryTag(scope.row.category)">
                      {{ getCategoryLabel(scope.row.category) }}
                    </el-tag>
                    <span class="text-xs text-slate-400">{{ scope.row.publishDate }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="发布者" width="120">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.publisher === '总部' ? 'danger' : 'primary'" effect="dark">
                {{ scope.row.publisher }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="阅读量" width="100" align="center">
            <template #default="scope">
              <div class="text-sm font-bold text-blue-600">{{ scope.row.viewCount }}</div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <div v-if="scope.row.status === 'auditing' && currentUserRole === 'admin'" class="flex gap-2">
                <el-button link type="success" size="small" @click="handleApprove(scope.row)">
                  <el-icon class="mr-1"><Check /></el-icon> 通过
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(scope.row)">
                  <el-icon class="mr-1"><Close /></el-icon> 驳回
                </el-button>
              </div>

              <div v-else-if="scope.row.status === 'rejected'" class="flex gap-2">
                <el-popover placement="top" width="300" trigger="hover">
                  <template #reference>
                    <el-button link type="warning" size="small">
                      <el-icon class="mr-1"><Warning /></el-icon> 查看原因
                    </el-button>
                  </template>
                  <div class="text-sm">
                    <div class="font-bold text-red-600 mb-2">驳回原因：</div>
                    <div class="text-slate-700">{{ scope.row.rejectReason || '无' }}</div>
                  </div>
                </el-popover>
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                  <el-icon class="mr-1"><Edit /></el-icon> 重新编辑
                </el-button>
              </div>

              <div v-else class="flex gap-2 flex-wrap">
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                  <el-icon class="mr-1"><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  link
                  :type="scope.row.status === 'published' ? 'danger' : 'warning'"
                  size="small"
                  @click="handleToggleStatus(scope.row)"
                  v-if="['published', 'offline'].includes(scope.row.status)"
                >
                  {{ scope.row.status === 'published' ? '下架' : '上架' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            background
            layout="total, prev, pager, next"
            :total="totalItems"
            :page-size="pageSize"
            @current-change="loadNews"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="publishDialogVisible" :title="isEdit ? '编辑新闻' : '发布新闻'" width="900px" top="5vh">
      <el-form :model="form" label-width="100px" ref="formRef">
        <el-form-item label="新闻分类" required>
          <el-select v-model="form.category" class="w-full" placeholder="请选择">
            <el-option label="平台新闻" value="platform" />
            <el-option label="行业动态" value="industry" />
            <el-option label="政策解读" value="policy" />
          </el-select>
        </el-form-item>

        <el-form-item label="新闻标题" required>
          <el-input v-model="form.title" placeholder="请输入新闻标题（50字以内）" maxlength="50" show-word-limit />
        </el-form-item>

        <el-form-item label="新闻摘要" required>
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入新闻摘要（200字以内）..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图片" required>
          <el-upload
            class="upload-demo"
            action="/api/common/upload"
            :limit="1"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            :file-list="imageFileList"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip text-xs text-slate-500 ml-2">
                建议尺寸 800x450px，JPG/PNG 格式，不超过 2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="正文内容" required>
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="请输入正文内容..." />
        </el-form-item>

        <el-form-item label="来源">
          <el-input v-model="form.source" placeholder="如：新华社、人民日报等（选填）" />
        </el-form-item>

        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="如：张三、李四等（选填）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="submitLoading">
          {{ isEdit ? '保存修改' : '提交发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Search, Plus, Edit, Check, Close, Warning,
  Notebook, View, ChatLineRound
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile, FormInstance } from 'element-plus'
import { apiRequest, getLoginUser } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const publishDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const apiLoading = ref(false)
const currentUserRole = ref<'admin' | 'partner'>(getLoginUser()?.role === 'partner' ? 'partner' : 'admin')
const formRef = ref<FormInstance>()
const imageFileList = ref<UploadUserFile[]>([])
const currentEditId = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const filters = ref({ keyword: '', category: '', status: '' })

const form = ref({
  category: '',
  title: '',
  summary: '',
  coverImage: '',
  content: '',
  source: '',
  author: ''
})

const allNewsList = ref<any[]>([])

const parseExtraJson = (raw: any) => {
  if (!raw || typeof raw !== 'string') return {}
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

const fmtDate = (v: any) => {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v).slice(0, 10)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const mapCategory = (raw?: string): 'platform' | 'industry' | 'policy' => {
  const c = String(raw || '').toLowerCase()
  if (['platform', 'news', 'company'].includes(c)) return 'platform'
  if (['policy', 'regulation'].includes(c)) return 'policy'
  return 'industry'
}

const mapStatus = (raw?: string) => {
  const s = String(raw || '').toLowerCase()
  if (s === 'published') return { key: 'published', label: '已发布' }
  if (s === 'offline') return { key: 'offline', label: '已下架' }
  if (s === 'rejected') return { key: 'rejected', label: '已驳回' }
  return { key: 'auditing', label: '审核中' }
}

const mapNewsRow = (item: any) => {
  const st = mapStatus(item?.status)
  const extra = parseExtraJson(item?.extraJson ?? item?.extra_json)
  const category = mapCategory(extra.category || item?.category)
  return {
    id: item?.id,
    newsNo: item?.contentNo || `N-${item?.id || ''}`,
    title: item?.title || '未命名内容',
    summary: item?.summary || item?.description || '',
    category,
    coverImage: item?.coverImage || 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=200',
    content: item?.body || extra.content || '',
    source: extra.source || '-',
    author: extra.author || '-',
    publisher: item?.publisher || '总部',
    publishDate: fmtDate(item?.publishDate || item?.createdAt || item?.updatedAt),
    status: st.key,
    statusLabel: st.label,
    rejectReason: item?.changeReason || item?.rejectReason || '',
    extraJson: item?.extraJson || item?.extra_json || '',
    viewCount: Number(item?.viewCount || 0)
  }
}

const loadNews = async () => {
  apiLoading.value = true
  try {
    const query: Record<string, any> = {
      content_type: 'media',
      page: currentPage.value,
      page_size: pageSize.value,
    }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    if (filters.value.status) query.publish_status = filters.value.status
    const res: any = await apiRequest('/v3/admin/contents', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allNewsList.value = items.map(mapNewsRow)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    allNewsList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取媒体内容失败')
  } finally {
    apiLoading.value = false
  }
}

const stats = computed(() => {
  const list = allNewsList.value
  const total = list.length
  const month = new Date().toISOString().slice(0, 7)
  const monthCount = list.filter(i => String(i.publishDate || '').startsWith(month)).length
  const totalViews = list.reduce((acc, cur) => acc + Number(cur.viewCount || 0), 0)
  const pending = list.filter(i => i.status === 'auditing').length
  return [
    { label: '累计发布', value: String(total), icon: Notebook, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '本月新增', value: String(monthCount), icon: Plus, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '总阅读量', value: String(totalViews), icon: View, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
    { label: '待审核', value: String(pending), icon: ChatLineRound, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
  ]
})

const filteredNewsList = computed(() => {
  let list = allNewsList.value
  if (filters.value.category) {
    list = list.filter(item => item.category === filters.value.category)
  }
  return list
})

const getCategoryTag = (category: string) => {
  const map: Record<string, string> = {
    platform: 'danger',
    industry: 'primary',
    policy: 'success'
  }
  return map[category] || 'info'
}

const getCategoryLabel = (category: string) => {
  const map: Record<string, string> = {
    platform: '平台新闻',
    industry: '行业动态',
    policy: '政策解读'
  }
  return map[category] || '未知'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    published: 'success',
    auditing: 'warning',
    rejected: 'info',
    offline: 'danger'
  }
  return map[status] || 'info'
}

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (!['image/jpeg', 'image/png'].includes(rawFile.type)) {
    ElMessage.error('只支持 JPG/PNG 格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleImageSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response?.code === 'OK' || response?.code === 200) {
    form.value.coverImage = response?.data?.url || response?.url || ''
    ElMessage.success('图片上传成功')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadNews()
}

const openPublishDialog = () => {
  isEdit.value = false
  currentEditId.value = null
  form.value = {
    category: '',
    title: '',
    summary: '',
    coverImage: '',
    content: '',
    source: '',
    author: ''
  }
  imageFileList.value = []
  publishDialogVisible.value = true
}

const loadNewsDetail = async (id: number) => {
  const res: any = await apiRequest(`/v3/admin/contents/${id}`)
  const item = res?.data || {}
  const extra = parseExtraJson(item?.extraJson ?? item?.extra_json)
  return {
    id: Number(item?.id || 0) || null,
    category: mapCategory(extra.category),
    title: item?.title || '',
    summary: item?.summary || '',
    coverImage: item?.coverImage || extra.coverImage || '',
    content: item?.body || '',
    source: extra.source || '',
    author: extra.author || '',
  }
}

const handlePublish = async () => {
  if (!form.value.title?.trim()) {
    return ElMessage.warning('请填写新闻标题')
  }
  if (!form.value.category) {
    return ElMessage.warning('请选择新闻分类')
  }
  if (!form.value.content?.trim()) {
    return ElMessage.warning('请填写正文内容')
  }

  submitLoading.value = true
  try {
    const extraJson = JSON.stringify({
      category: form.value.category,
      coverImage: form.value.coverImage,
      source: form.value.source,
      author: form.value.author
    })

    const method = isEdit.value && currentEditId.value ? 'PUT' : 'POST'
    const url = isEdit.value && currentEditId.value
      ? `/v3/admin/contents/${currentEditId.value}`
      : '/v3/admin/contents'

    await apiRequest(url, {
      method,
      body: {
        title: form.value.title.trim(),
        content_type: 'media',
        summary: form.value.summary || '',
        body: form.value.content || '',
        extra_json: extraJson,
        cover_image: form.value.coverImage || '',
        city_name: '',
        is_paid: false,
        fee: 0
      }
    })

    ElMessage.success(isEdit.value ? '新闻修改已保存' : '新闻已创建（草稿），可在列表中发布')
    publishDialogVisible.value = false
    await loadNews()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

const handleEdit = async (row: any) => {
  try {
    const detail = await loadNewsDetail(row.id)
    isEdit.value = true
    currentEditId.value = detail.id
    form.value = {
      category: detail.category,
      title: detail.title,
      summary: detail.summary,
      coverImage: detail.coverImage,
      content: detail.content,
      source: detail.source,
      author: detail.author
    }
    imageFileList.value = form.value.coverImage
      ? [{ name: 'cover-image', url: form.value.coverImage } as UploadUserFile]
      : []
    publishDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '读取新闻详情失败')
  }
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm(`确认通过 "${row.title}" 吗？`, '审核通过', { type: 'success' })
    .then(async () => {
      try {
        await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
          method: 'POST',
          body: {
            to_status: 'published',
            reason: 'content media approve'
          }
        })
        ElMessage.success('审核通过')
        await loadNews()
      } catch (e: any) {
        ElMessage.error(e?.message || '审核失败')
      }
    })
    .catch(() => {})
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    inputType: 'textarea',
    inputValidator: (value) => (value?.trim() ? true : '请输入驳回原因')
  })
    .then(async ({ value }) => {
      try {
        await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
          method: 'POST',
          body: {
            to_status: 'rejected',
            reason: String(value || 'content media reject')
          }
        })
        ElMessage.warning('已驳回')
        await loadNews()
      } catch (e: any) {
        ElMessage.error(e?.message || '驳回失败')
      }
    })
    .catch(() => {})
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 'published' ? '下架' : '上架'
  ElMessageBox.confirm(`确定${action}此内容吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const toStatus = row.status === 'published' ? 'offline' : 'published'
        await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
          method: 'POST',
          body: {
            to_status: toStatus,
            reason: toStatus === 'published' ? 'media manual up' : 'media manual down'
          }
        })
        ElMessage.success(`已${action}`)
        await loadNews()
      } catch (e: any) {
        ElMessage.error(e?.message || '状态更新失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
