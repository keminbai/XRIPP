<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\media.vue
  版本: V1.1（2026-02-27 收敛版）
  说明:
  1. 列表读取改为真实接口：GET /v3/admin/tenders
  2. 审核/上下架改为真实状态流转：POST /v3/admin/tenders/{id}/transition
  3. 发布/编辑正文接口尚未独立落地，当前为受控降级（不假成功）
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
          <el-pagination background layout="total, prev, pager, next" :total="filteredNewsList.length" :page-size="10" />
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
          <div class="text-xs text-slate-400 mt-1">提示：当前页面写接口未接入，提交将进入受控降级提示。</div>
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
  if (c === 'it') return 'platform'
  if (c === 'construction') return 'policy'
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
  return {
    id: item?.id,
    newsNo: item?.contentNo || `N-${item?.id || ''}`,
    title: item?.title || '未命名内容',
    summary: item?.description || '',
    category: mapCategory(item?.category),
    coverImage: item?.coverImage || 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=200',
    content: item?.description || '',
    source: item?.organization || '-',
    author: '-',
    publisher: item?.publisher || '总部',
    publishDate: fmtDate(item?.publishDate || item?.createdAt || item?.updatedAt),
    status: st.key,
    statusLabel: st.label,
    rejectReason: item?.rejectReason || '',
    viewCount: Number(item?.viewCount || 0)
  }
}

const loadNews = async () => {
  apiLoading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/contents?content_type=media&page=1&page_size=200')
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allNewsList.value = items.map(mapNewsRow)
  } catch (e: any) {
    allNewsList.value = []
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

  if (filters.value.keyword) {
    list = list.filter(item =>
      String(item.title || '').includes(filters.value.keyword) ||
      String(item.newsNo || '').includes(filters.value.keyword)
    )
  }

  if (filters.value.category) {
    list = list.filter(item => item.category === filters.value.category)
  }

  if (filters.value.status) {
    list = list.filter(item => item.status === filters.value.status)
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

const handleSearch = () => {}

const openPublishDialog = () => {
  isEdit.value = false
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
  ElMessage.warning('发布接口未接入，当前为受控降级模式')
}

const handlePublish = () => {
  ElMessage.warning('发布/编辑接口未接入，暂不支持写入')
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = { ...row }
  publishDialogVisible.value = true
  ElMessage.warning('编辑接口未接入，当前仅支持查看与审核状态流转')
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
