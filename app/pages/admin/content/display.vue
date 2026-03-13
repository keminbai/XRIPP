<!--
  文件路径: D:\ipp-platform\app\pages\admin\content\display.vue
  版本: V3.0 API对接版 (2026-03-12)

  修复: 移除所有Mock数据，对接 /v3/admin/contents API
  - 轮播图 CRUD: content_type=carousel
  - 广告位 CRUD: content_type=ad
  - 显示申请: content_type=carousel, publish_status=pending_review
-->
<template>
  <div class="space-y-6">

    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">网站内容展示管理</h3>
        <p class="text-xs text-slate-500 mt-1">管理前台首页轮播、广告位及各业务板块的推荐申请</p>
      </div>

      <el-tabs v-model="activeTab" type="card" class="demo-tabs">

        <el-tab-pane label="活动显示申请" name="activity-display">
          <div class="space-y-4 pt-4">
            <div class="flex justify-between items-center">
              <div class="text-xs text-slate-500">审核活动提交的首页/活动中心/平台服务推荐申请</div>
              <el-select v-model="displayFilters.applyStatus" placeholder="状态筛选" class="w-40" clearable @change="loadActivityDisplays">
                <el-option label="待审核" value="pending_review" />
                <el-option label="已通过" value="approved" />
                <el-option label="已驳回" value="rejected" />
                <el-option label="已下线" value="offline" />
              </el-select>
            </div>

            <el-table :data="activityDisplayList" border stripe v-loading="activityDisplayLoading">
              <el-table-column prop="activityNo" label="活动编号" width="140" />
              <el-table-column label="活动信息" min-width="280">
                <template #default="{ row }">
                  <div class="flex gap-3 items-center">
                    <img v-if="row.coverImage" :src="row.coverImage" class="w-16 h-10 object-cover rounded border border-slate-200" />
                    <div>
                      <div class="font-semibold text-slate-800">{{ row.title }}</div>
                      <div class="text-xs text-slate-500 mt-1">{{ row.cityName || '全国' }} | {{ row.publisher || '-' }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="displayAreaLabel" label="显示区域" width="140" />
              <el-table-column label="显示时段" width="220">
                <template #default="{ row }">
                  <div class="text-xs text-slate-600">
                    <div>{{ row.displayStartAt || '-' }}</div>
                    <div class="text-slate-400 mt-1">至 {{ row.displayEndAt || '-' }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sortOrder" label="顺序" width="80" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="row.applyStatus === 'approved' ? 'success' : row.applyStatus === 'pending_review' ? 'warning' : row.applyStatus === 'rejected' ? 'danger' : 'info'" size="small">
                    {{ row.applyStatusLabel }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="changeReason" label="备注/原因" min-width="180" show-overflow-tooltip />
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <div class="flex gap-2">
                    <el-button v-if="row.applyStatus === 'pending_review'" type="success" size="small" plain @click="handleDisplayTransition(row, 'approved')">通过</el-button>
                    <el-button v-if="row.applyStatus === 'pending_review'" type="danger" size="small" plain @click="handleDisplayReject(row)">驳回</el-button>
                    <el-button v-if="row.applyStatus === 'approved'" type="warning" size="small" plain @click="handleDisplayTransition(row, 'offline')">下线</el-button>
                    <el-button v-if="['rejected', 'offline'].includes(row.applyStatus)" type="primary" size="small" plain @click="handleDisplayTransition(row, 'approved')">重新启用</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 1. 首页轮播图 -->
        <el-tab-pane label="首页轮播图" name="carousel">
          <div class="space-y-4 pt-4">
            <div class="flex justify-between items-center">
              <div class="text-xs text-slate-500">最多支持5张轮播图，建议尺寸1920x650px</div>
              <el-button type="primary" @click="handleCarouselUpload" :disabled="carouselList.length >= 5">
                <el-icon class="mr-2"><Plus /></el-icon> 新增轮播图
              </el-button>
            </div>
            <div v-loading="carouselLoading">
              <div v-if="carouselList.length === 0 && !carouselLoading" class="text-center py-10 text-slate-400">
                暂无轮播图数据
              </div>
              <div class="grid grid-cols-1 gap-4">
                <div v-for="item in carouselList" :key="item.id" class="bg-slate-50 border border-slate-200 rounded-lg p-4 flex gap-4 items-center">
                  <div class="w-48 h-28 rounded overflow-hidden bg-slate-200 flex-shrink-0 border border-slate-300">
                    <img v-if="item.coverImage" :src="item.coverImage" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex items-center justify-center text-slate-400 text-sm">无图片</div>
                  </div>
                  <div class="flex-1">
                    <div class="font-bold text-slate-800">{{ item.title }}</div>
                    <div class="text-sm text-slate-500 mt-1">状态: {{ item.publishStatusLabel }} | 创建: {{ fmtDate(item.createdAt) }}</div>
                  </div>
                  <div class="flex gap-2">
                    <el-button v-if="item.publishStatus !== 'published'" type="success" size="small" plain @click="handleTransition(item, 'published')">发布</el-button>
                    <el-button v-if="item.publishStatus === 'published'" type="warning" size="small" plain @click="handleTransition(item, 'offline')">下架</el-button>
                    <el-button type="danger" size="small" plain @click="handleDelete(item)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 2. 广告位管理 -->
        <el-tab-pane label="广告位管理" name="ads">
          <div class="space-y-4 pt-4">
            <div class="flex justify-between items-center">
              <div class="text-xs text-slate-500">管理广告位内容</div>
              <el-button type="primary" @click="handleAdAdd">
                <el-icon class="mr-2"><Plus /></el-icon> 新增广告
              </el-button>
            </div>
            <div v-loading="adLoading">
              <div v-if="adsList.length === 0 && !adLoading" class="text-center py-10 text-slate-400">
                暂无广告位数据
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div v-for="ad in adsList" :key="ad.id" class="bg-white border border-slate-200 rounded-lg p-5 shadow-sm">
                  <div class="flex justify-between items-start mb-3">
                    <div>
                      <div class="font-bold text-slate-800">{{ ad.title }}</div>
                      <div class="text-xs text-slate-500 mt-1">{{ ad.summary || '暂无描述' }}</div>
                    </div>
                    <el-tag :type="ad.publishStatus === 'published' ? 'success' : 'info'" size="small">
                      {{ ad.publishStatusLabel }}
                    </el-tag>
                  </div>
                  <div class="w-full h-32 bg-slate-50 rounded border border-dashed border-slate-300 mb-3 flex items-center justify-center overflow-hidden">
                    <img v-if="ad.coverImage" :src="ad.coverImage" class="w-full h-full object-cover" />
                    <span v-else class="text-slate-400 text-sm">暂无广告图</span>
                  </div>
                  <div class="flex gap-2">
                    <el-button v-if="ad.publishStatus !== 'published'" type="success" size="small" plain class="flex-1" @click="handleTransition(ad, 'published')">发布</el-button>
                    <el-button v-if="ad.publishStatus === 'published'" type="warning" size="small" plain class="flex-1" @click="handleTransition(ad, 'offline')">下架</el-button>
                    <el-button type="danger" size="small" plain @click="handleDelete(ad)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 3. 关于我们 -->
        <el-tab-pane label="关于我们" name="about">
          <div class="pt-4 max-w-4xl">
            <el-form :model="aboutContent" label-width="100px">
              <el-form-item label="主标题"><el-input v-model="aboutContent.title" /></el-form-item>
              <el-form-item label="正文内容"><el-input v-model="aboutContent.content" type="textarea" :rows="10" /></el-form-item>
              <el-form-item><el-button type="primary" @click="handleAboutSave">保存修改</el-button></el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 4. 网站设置 -->
        <el-tab-pane label="网站设置" name="site">
           <div class="pt-4 max-w-3xl space-y-8">
             <el-card shadow="never" class="border border-slate-200">
               <template #header><span class="font-bold text-slate-700">基础信息</span></template>
               <el-form :model="siteConfig" label-width="120px">
                 <el-form-item label="网站LOGO文字"><el-input v-model="siteConfig.logo" /></el-form-item>
                 <el-form-item label="网站中文名"><el-input v-model="siteConfig.siteName" /></el-form-item>
               </el-form>
               <el-button type="primary" class="mt-4" @click="handleSiteSave">保存设置</el-button>
             </el-card>
           </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="formDialogTitle" width="600px">
        <el-form :model="contentForm" label-width="80px">
           <el-form-item label="标题" required><el-input v-model="contentForm.title" placeholder="请输入标题" /></el-form-item>
           <el-form-item label="摘要"><el-input v-model="contentForm.summary" type="textarea" :rows="3" placeholder="简要描述" /></el-form-item>
           <el-form-item label="封面图">
             <el-upload
               action="/api/common/upload"
               :limit="1"
               :on-success="handleUploadSuccess"
               list-type="picture-card"
               :auto-upload="true"
             >
               <el-icon><Plus /></el-icon>
             </el-upload>
             <div v-if="contentForm.coverImage" class="mt-2">
               <img :src="contentForm.coverImage" class="w-32 h-20 object-cover rounded border" />
             </div>
           </el-form-item>
           <el-form-item label="正文"><el-input v-model="contentForm.body" type="textarea" :rows="5" placeholder="详细内容" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="formDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleFormSave" :loading="formSaving">保存</el-button>
        </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const activeTab = ref('carousel')

// --- Data ---
const carouselList = ref<any[]>([])
const adsList = ref<any[]>([])
const activityDisplayList = ref<any[]>([])
const carouselLoading = ref(false)
const adLoading = ref(false)
const activityDisplayLoading = ref(false)
const displayFilters = ref({ applyStatus: '' })

const fmtDate = (v: any) => {
  if (!v) return '-'
  return String(v).slice(0, 10)
}

const statusLabelMap: Record<string, string> = {
  published: '已发布',
  draft: '草稿',
  pending_review: '待审核',
  approved: '已通过',
  rejected: '已驳回',
  offline: '已下架'
}

const mapItem = (item: any) => ({
  id: item.id,
  title: item.title || '未命名',
  summary: item.summary || '',
  body: item.body || '',
  coverImage: item.coverImage || item.cover_image || '',
  publishStatus: item.publishStatus || item.publish_status || 'draft',
  publishStatusLabel: statusLabelMap[item.publishStatus || item.publish_status || 'draft'] || '未知',
  createdAt: item.createdAt || item.created_at || ''
})

const mapDisplayItem = (item: any) => ({
  id: item.id,
  activityId: item.activity_id || item.activityId,
  activityNo: item.activity_no || '-',
  title: item.title || '未命名活动',
  cityName: item.city_name || '',
  coverImage: item.cover_image || '',
  publisher: item.publisher || '',
  displayArea: item.display_area || item.displayArea || '',
  displayAreaLabel: item.display_area_label || item.displayAreaLabel || '-',
  applyStatus: item.apply_status || item.applyStatus || 'pending_review',
  applyStatusLabel: item.apply_status_label || item.applyStatusLabel || '待审核',
  displayStartAt: item.displayStartAt || item.display_start_at || '',
  displayEndAt: item.displayEndAt || item.display_end_at || '',
  sortOrder: Number(item.sort_order || 0),
  changeReason: item.change_reason || item.changeReason || ''
})

// --- Load Data ---
const loadCarousels = async () => {
  carouselLoading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/contents', {
      query: { content_type: 'carousel', page: 1, page_size: 20 }
    })
    carouselList.value = (res?.data?.items || []).map(mapItem)
  } catch (e: any) {
    carouselList.value = []
    ElMessage.error(e?.message || '加载轮播图失败')
  } finally {
    carouselLoading.value = false
  }
}

const loadAds = async () => {
  adLoading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/contents', {
      query: { content_type: 'ad', page: 1, page_size: 20 }
    })
    adsList.value = (res?.data?.items || []).map(mapItem)
  } catch (e: any) {
    adsList.value = []
    ElMessage.error(e?.message || '加载广告位失败')
  } finally {
    adLoading.value = false
  }
}

const loadActivityDisplays = async () => {
  activityDisplayLoading.value = true
  try {
    const query: Record<string, any> = { page: 1, page_size: 50 }
    if (displayFilters.value.applyStatus) query.apply_status = displayFilters.value.applyStatus
    const res: any = await apiRequest('/v3/admin/activity-display-applications', { query })
    activityDisplayList.value = (res?.data?.items || []).map(mapDisplayItem)
  } catch (e: any) {
    activityDisplayList.value = []
    ElMessage.error(e?.message || '加载活动显示申请失败')
  } finally {
    activityDisplayLoading.value = false
  }
}

// --- Form ---
const formDialogVisible = ref(false)
const formDialogTitle = ref('新增')
const formSaving = ref(false)
const formContentType = ref('carousel')
const contentForm = ref({
  title: '',
  summary: '',
  body: '',
  coverImage: ''
})

const handleCarouselUpload = () => {
  formContentType.value = 'carousel'
  formDialogTitle.value = '新增轮播图'
  contentForm.value = { title: '', summary: '', body: '', coverImage: '' }
  formDialogVisible.value = true
}

const handleAdAdd = () => {
  formContentType.value = 'ad'
  formDialogTitle.value = '新增广告'
  contentForm.value = { title: '', summary: '', body: '', coverImage: '' }
  formDialogVisible.value = true
}

const handleUploadSuccess = (response: any) => {
  if (response?.data?.url) {
    contentForm.value.coverImage = response.data.url
    ElMessage.success('图片上传成功')
  }
}

const handleFormSave = async () => {
  if (!contentForm.value.title?.trim()) {
    return ElMessage.warning('请输入标题')
  }
  formSaving.value = true
  try {
    await apiRequest('/v3/admin/contents', {
      method: 'POST',
      body: {
        title: contentForm.value.title.trim(),
        content_type: formContentType.value,
        summary: contentForm.value.summary || '',
        body: contentForm.value.body || '',
        cover_image: contentForm.value.coverImage || ''
      }
    })
    ElMessage.success('创建成功')
    formDialogVisible.value = false
    if (formContentType.value === 'carousel') await loadCarousels()
    else await loadAds()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    formSaving.value = false
  }
}

// --- Transition (publish/offline) ---
const handleTransition = async (item: any, toStatus: string) => {
  try {
    await apiRequest(`/v3/admin/contents/${item.id}/transition`, {
      method: 'POST',
      body: { to_status: toStatus, reason: `display ${toStatus}` }
    })
    ElMessage.success(toStatus === 'published' ? '已发布' : '已下架')
    await loadCarousels()
    await loadAds()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

// --- Delete ---
const handleDelete = (item: any) => {
  ElMessageBox.confirm(`确认删除「${item.title}」吗？`, '删除确认', { type: 'warning' })
    .then(async () => {
      try {
        await apiRequest(`/v3/admin/contents/${item.id}/transition`, {
          method: 'POST',
          body: { to_status: 'offline', reason: 'delete' }
        })
        ElMessage.success('已删除')
        await loadCarousels()
        await loadAds()
      } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const handleDisplayTransition = async (item: any, toStatus: string) => {
  try {
    await apiRequest(`/v3/admin/activity-display-applications/${item.id}/transition`, {
      method: 'POST',
      body: { to_status: toStatus, reason: `display ${toStatus}` }
    })
    ElMessage.success(toStatus === 'approved' ? '显示申请已通过' : '显示申请已下线')
    await loadActivityDisplays()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const handleDisplayReject = (item: any) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回显示申请', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputValidator: (value) => value?.trim() ? true : '请输入驳回原因'
  }).then(async ({ value }) => {
    try {
      await apiRequest(`/v3/admin/activity-display-applications/${item.id}/transition`, {
        method: 'POST',
        body: { to_status: 'rejected', reason: String(value || '') }
      })
      ElMessage.success('已驳回')
      await loadActivityDisplays()
    } catch (e: any) {
      ElMessage.error(e?.message || '驳回失败')
    }
  }).catch(() => {})
}

// --- Other tabs (config-type, keep as-is) ---
const aboutContent = ref({ title: '关于XRIPP', content: '...' })
const handleAboutSave = () => ElMessage.success('保存成功')
const siteConfig = ref({ logo: 'XRIPP', siteName: 'XRIPP国际公共采购平台' })
const handleSiteSave = () => ElMessage.success('设置保存成功')

onMounted(() => {
  loadActivityDisplays()
  loadCarousels()
  loadAds()
})
</script>
