<template>
  <div class="space-y-6">
    <div class="bg-gradient-to-r from-blue-600 to-cyan-700 p-8 rounded-xl text-white shadow-lg">
      <div class="flex items-center justify-between gap-6">
        <div>
          <h1 class="text-2xl font-bold mb-2">欢迎，{{ partnerInfo.name }}</h1>
          <p class="text-blue-100 text-sm">
            当前入口已收敛为合伙人活动发布。新建、查看、编辑重提、附件上传均已接入真实接口。
          </p>
        </div>
        <div class="text-right">
          <div class="text-3xl font-bold">{{ partnerInfo.region }}</div>
          <div class="text-xs text-blue-200 mt-1">服务区域</div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow"
      >
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
            <div class="text-2xl font-bold" :class="stat.valueClass">{{ stat.value }}</div>
          </div>
          <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="border-b border-slate-100">
        <el-tabs v-model="activeTab" class="px-6 pt-2">
          <el-tab-pane label="我的发布" name="myPublish">
            <template #label>
              <span class="flex items-center gap-2">
                <el-icon><Document /></el-icon>
                我的活动
                <el-badge v-if="myPublishList.length > 0" :value="myPublishList.length" type="primary" />
              </span>
            </template>
          </el-tab-pane>

          <el-tab-pane :label="isEdit ? '编辑活动' : '新建活动'" name="create">
            <template #label>
              <span class="flex items-center gap-2">
                <el-icon><Plus /></el-icon>
                {{ isEdit ? '编辑活动' : '新建活动' }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-if="activeTab === 'myPublish'" class="p-6">
        <div class="mb-4 flex flex-wrap items-center gap-3">
          <el-select v-model="statusFilter" placeholder="审核状态" class="w-36" clearable>
            <el-option label="审核中" value="auditing" />
            <el-option label="已发布" value="published" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已下架" value="offline" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <div class="text-xs text-slate-400">
            说明：培训、商机类内容已迁移到总部内容管理入口，本页仅处理合伙人活动发布。
          </div>
        </div>

        <el-table
          v-loading="tableLoading"
          :data="filteredPublishList"
          style="width: 100%"
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
        >
          <el-table-column prop="submitTime" label="提交时间" width="170" />

          <el-table-column label="活动概览" min-width="320">
            <template #default="scope">
              <div class="flex gap-3 py-2">
                <img :src="resolveFileUrl(scope.row.image)" class="w-16 h-10 rounded object-cover border border-slate-200" />
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-slate-800 text-sm line-clamp-1">{{ scope.row.title }}</div>
                  <div class="flex items-center gap-2 mt-1 flex-wrap">
                    <el-tag size="small" :type="scope.row.typeTag">{{ scope.row.typeLabel }}</el-tag>
                    <span class="text-xs text-slate-400">{{ scope.row.activityNo }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="前台位置" width="180">
            <template #default="scope">
              <div class="text-xs">
                <div class="font-medium text-slate-700">{{ scope.row.frontModuleLabel }}</div>
                <div class="text-slate-400 mt-0.5">{{ scope.row.frontPositionLabel }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="150">
            <template #default="scope">
              <div class="flex items-center gap-2">
                <span class="w-2 h-2 rounded-full" :class="getStatusDotColor(scope.row.status)" />
                <span class="text-sm font-medium" :class="getStatusTextColor(scope.row.status)">
                  {{ scope.row.statusLabel }}
                </span>
              </div>
              <div v-if="scope.row.statusTime" class="text-[10px] text-slate-400 mt-0.5">
                {{ scope.row.statusTime }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="250" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleView(scope.row)">
                <el-icon><View /></el-icon> 查看
              </el-button>
              <el-button link type="warning" size="small" @click="handleEdit(scope.row)">
                <el-icon><Edit /></el-icon>
                {{ ['published', 'offline'].includes(scope.row.status) ? '编辑重提' : '修改' }}
              </el-button>
              <el-button
                v-if="scope.row.status === 'auditing'"
                link
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
              >
                <el-icon><Delete /></el-icon> 撤回
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="filteredPublishList.length === 0" class="text-center py-16 text-slate-400">
          <el-icon class="text-6xl mb-4"><DocumentRemove /></el-icon>
          <div class="text-lg">暂无活动发布记录</div>
          <el-button type="primary" class="mt-4" @click="openCreateTab">立即发布活动</el-button>
        </div>
      </div>

      <div v-if="activeTab === 'create'" class="p-6">
        <el-form ref="formRef" :model="form" label-width="130px" :rules="formRules">
          <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-blue-500 pl-2">活动基础信息</h4>

            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="活动类型" prop="subType" required>
                <el-select v-model="form.subType" class="w-full">
                  <el-option label="亨嘉之会" value="hengjia" />
                  <el-option label="公益行" value="charity" />
                  <el-option label="出海考察" value="inspection" />
                  <el-option label="行业沙龙" value="salon" />
                </el-select>
              </el-form-item>

              <el-form-item label="前台主模块" prop="frontModule" required>
                <el-select v-model="form.frontModule" class="w-full">
                  <el-option label="首页轮播图" value="home-banner" />
                  <el-option label="活动中心" value="activity-center" />
                  <el-option label="服务页推荐" value="service" />
                </el-select>
              </el-form-item>
            </div>

            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="详细位置" prop="frontPosition" required>
                <el-select v-model="form.frontPosition" class="w-full">
                  <el-option label="顶部焦点位" value="top" />
                  <el-option label="列表推荐位" value="list" />
                  <el-option label="侧边栏" value="sidebar" />
                </el-select>
              </el-form-item>

              <el-form-item label="活动时间" prop="activityDate" required>
                <el-date-picker
                  v-model="form.activityDate"
                  type="datetime"
                  class="!w-full"
                  placeholder="选择活动时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
            </div>

            <el-form-item label="活动标题" prop="title" required>
              <el-input
                v-model="form.title"
                placeholder="请输入标题（30字以内）"
                maxlength="30"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="投放城市" prop="cities" required>
              <el-select
                v-model="form.cities"
                multiple
                collapse-tags
                placeholder="选择投放城市"
                class="w-full"
              >
                <el-option v-for="city in availableCities" :key="city" :label="city" :value="city" />
              </el-select>
            </el-form-item>
          </div>

          <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-orange-500 pl-2">媒体素材</h4>

            <el-form-item label="封面图片" prop="coverImage" required>
              <el-upload
                action="/api/common/upload"
                list-type="picture-card"
                :limit="1"
                :file-list="imageFileList"
                :before-upload="beforeImageUpload"
                :on-success="handleImageSuccess"
                :on-error="handleUploadError"
                :on-remove="handleImageRemove"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="text-xs text-slate-400 mt-1">建议尺寸：1200x675px，不超过 2MB</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="宣传视频">
              <el-upload
                action="/api/common/upload"
                :limit="1"
                :file-list="videoFileList"
                :before-upload="beforeVideoUpload"
                :on-success="handleVideoSuccess"
                :on-error="handleUploadError"
                :on-remove="handleVideoRemove"
              >
                <el-button type="primary" plain>
                  <el-icon class="mr-1"><VideoPlay /></el-icon>
                  上传视频 (MP4/MOV)
                </el-button>
                <template #tip>
                  <div class="text-xs text-slate-400 mt-2">选填，不超过 50MB</div>
                </template>
              </el-upload>
              <div v-if="form.videoUrl" class="mt-2 text-xs text-slate-500">
                当前视频：
                <a :href="form.videoUrl" target="_blank" class="text-blue-600 hover:text-blue-700">查看已上传视频</a>
              </div>
            </el-form-item>
          </div>

          <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-green-500 pl-2">业务规则</h4>

            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="名额限制">
                <el-input-number v-model="form.maxLimit" :min="0" :max="500" class="!w-full" />
              </el-form-item>

              <el-form-item label="费用设置">
                <div class="space-y-3 w-full">
                  <el-radio-group v-model="form.feeType">
                    <el-radio label="free">免费活动</el-radio>
                    <el-radio label="paid">收费活动</el-radio>
                  </el-radio-group>

                  <div v-if="form.feeType === 'paid'" class="flex items-center gap-2">
                    <el-select v-model="form.feeItemId" placeholder="请选择收费项目" class="w-full">
                      <el-option
                        v-for="item in feeOptions"
                        :key="item.id"
                        :label="`${item.name} (¥${item.price})`"
                        :value="item.id"
                      />
                    </el-select>
                    <div class="text-xs text-slate-400 whitespace-nowrap">
                      * 按总部配置收费标准
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>

          <el-form-item label="活动摘要" prop="summary" required>
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="请输入活动摘要，将显示在列表预览中"
            />
          </el-form-item>

          <div class="flex justify-center gap-4 pt-6 border-t border-slate-200">
            <el-button size="large" @click="resetForm">重置</el-button>
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              <el-icon v-if="!submitting" class="mr-2"><Upload /></el-icon>
              {{ submitting ? (isEdit ? '保存中...' : '提交中...') : (isEdit ? '保存并重新提交' : '提交审核') }}
            </el-button>
          </div>

          <div class="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div class="flex items-start gap-2 text-sm text-blue-700">
              <el-icon class="mt-0.5"><InfoFilled /></el-icon>
              <div>
                <div class="font-bold mb-1">温馨提示</div>
                <ul class="text-xs space-y-1 list-disc list-inside text-blue-600">
                  <li>活动提交后将由总部审核，请确保信息真实合规。</li>
                  <li>已发布活动如重新编辑，会重新进入审核状态。</li>
                  <li>封面图与视频已接入真实上传，不再只是本地临时选择。</li>
                </ul>
              </div>
            </div>
          </div>
        </el-form>
      </div>
    </div>

    <el-dialog v-model="viewDialogVisible" title="活动详情" width="720px">
      <div v-if="currentViewItem" class="space-y-4">
        <div class="flex gap-4">
          <img :src="resolveFileUrl(currentViewItem.image)" class="w-32 h-20 rounded-lg object-cover border border-slate-200" />
          <div class="flex-1">
            <div class="font-bold text-lg text-slate-800 mb-2">{{ currentViewItem.title }}</div>
            <div class="flex gap-2 mb-2 flex-wrap">
              <el-tag size="small" :type="currentViewItem.typeTag">{{ currentViewItem.typeLabel }}</el-tag>
              <el-tag size="small" type="info">{{ currentViewItem.statusLabel }}</el-tag>
            </div>
            <div class="text-xs text-slate-500">提交时间：{{ currentViewItem.submitTime || '-' }}</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3 p-3 bg-slate-50 rounded">
          <div>
            <div class="text-xs text-slate-500 mb-1">前台位置</div>
            <div class="font-medium text-slate-800">{{ currentViewItem.frontModuleLabel }}</div>
            <div class="text-xs text-slate-400">{{ currentViewItem.frontPositionLabel }}</div>
          </div>
          <div>
            <div class="text-xs text-slate-500 mb-1">活动时间</div>
            <div class="font-medium text-slate-800">{{ currentViewItem.activityDate || '-' }}</div>
          </div>
          <div>
            <div class="text-xs text-slate-500 mb-1">投放城市</div>
            <div class="font-medium text-slate-800">{{ currentViewItem.cities.join(' / ') }}</div>
          </div>
          <div>
            <div class="text-xs text-slate-500 mb-1">费用</div>
            <div class="font-medium text-slate-800">{{ currentViewItem.feeDisplay }}</div>
          </div>
        </div>

        <div>
          <div class="text-xs text-slate-500 mb-1">活动摘要</div>
          <div class="text-sm text-slate-700 leading-6 bg-slate-50 p-3 rounded">
            {{ currentViewItem.summary || '暂无摘要' }}
          </div>
        </div>

        <div
          v-if="currentViewItem.status === 'rejected' && currentViewItem.rejectReason"
          class="p-4 bg-red-50 border border-red-200 rounded-lg"
        >
          <div class="font-bold text-red-700 mb-2 flex items-center gap-2">
            <el-icon><WarningFilled /></el-icon>
            驳回原因
          </div>
          <div class="text-sm text-red-600">{{ currentViewItem.rejectReason }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleEditFromView" v-if="currentViewItem">
          {{ ['published', 'offline'].includes(currentViewItem.status) ? '编辑重提' : '修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  CircleCheck,
  CircleClose,
  Clock,
  Delete,
  Document,
  DocumentRemove,
  Edit,
  InfoFilled,
  Plus,
  Upload,
  View,
  VideoPlay,
  WarningFilled,
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import type { FormInstance, UploadProps, UploadUserFile } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest, getLoginUser } from '@/utils/request'
import { resolveFileUrl } from '@/utils/file-url'

definePageMeta({ layout: 'admin' })

type PublishRow = {
  id: number
  activityNo: string
  title: string
  image: string
  coverImage: string
  videoUrl: string
  typeLabel: string
  typeTag: 'success' | 'warning' | 'primary' | 'info'
  frontModuleLabel: string
  frontPositionLabel: string
  frontModule: string
  frontPosition: string
  status: string
  statusLabel: string
  submitTime: string
  statusTime: string
  summary: string
  rejectReason: string
  cities: string[]
  activityDate: string
  feeDisplay: string
  feeType: string
  feeItemId: string
  fee: number
  maxLimit: number
  subType: string
}

const DEFAULT_COVER = 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=200'

const createEmptyForm = (region = '上海') => ({
  subType: 'hengjia',
  title: '',
  frontModule: 'activity-center',
  frontPosition: 'list',
  coverImage: '',
  videoUrl: '',
  cities: [region],
  activityDate: '',
  maxLimit: 50,
  feeType: 'free',
  feeItemId: '',
  summary: '',
})

const partnerInfo = ref({
  name: '合伙人',
  region: '上海',
  id: '',
})

const activeTab = ref<'myPublish' | 'create'>('myPublish')
const statusFilter = ref('')
const viewDialogVisible = ref(false)
const currentViewItem = ref<PublishRow | null>(null)
const submitting = ref(false)
const tableLoading = ref(false)
const isEdit = ref(false)
const currentEditId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const imageFileList = ref<UploadUserFile[]>([])
const videoFileList = ref<UploadUserFile[]>([])

const feeOptions = ref([
  { id: 'FEE001', name: '标准活动票', price: 199 },
  { id: 'FEE002', name: '高端沙龙票', price: 299 },
  { id: 'FEE003', name: '专业培训课', price: 599 },
  { id: 'FEE004', name: '出海考察团', price: 19800 },
])

const myPublishList = ref<PublishRow[]>([])
const form = ref(createEmptyForm())

const formRules = {
  subType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  frontModule: [{ required: true, message: '请选择前台主模块', trigger: 'change' }],
  frontPosition: [{ required: true, message: '请选择详细位置', trigger: 'change' }],
  coverImage: [{ required: true, message: '请上传封面图片', trigger: 'change' }],
  cities: [{ required: true, message: '请选择投放城市', trigger: 'change' }],
  activityDate: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  summary: [{ required: true, message: '请输入活动摘要', trigger: 'blur' }],
}

const availableCities = computed(() => {
  return Array.from(new Set(['全国', partnerInfo.value.region, '上海', '苏州', '杭州', '南京', '无锡']))
})

const stats = computed(() => {
  const all = myPublishList.value.length
  const auditing = myPublishList.value.filter(item => item.status === 'auditing').length
  const published = myPublishList.value.filter(item => item.status === 'published').length
  const rejected = myPublishList.value.filter(item => item.status === 'rejected').length

  return [
    { label: '累计发布', value: String(all), valueClass: 'text-blue-600', icon: Document, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '审核中', value: String(auditing), valueClass: 'text-orange-600', icon: Clock, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已发布', value: String(published), valueClass: 'text-green-600', icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已驳回', value: String(rejected), valueClass: 'text-red-600', icon: CircleClose, bgClass: 'bg-red-50', textClass: 'text-red-600' },
  ]
})

const filteredPublishList = computed(() => {
  if (!statusFilter.value) return myPublishList.value
  return myPublishList.value.filter(item => item.status === statusFilter.value)
})

const formatDateTime = (value: any) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const normalizeStatus = (status: any) => {
  const raw = String(status || '').toLowerCase()
  if (raw === 'pending') return 'auditing'
  if (raw === 'approved') return 'published'
  return raw || 'auditing'
}

const statusLabel = (status: string) => {
  if (status === 'published') return '已发布'
  if (status === 'rejected') return '已驳回'
  if (status === 'offline') return '已下架'
  return '审核中'
}

const activityTypeMeta = (subType: any) => {
  const raw = String(subType || '').toLowerCase()
  if (raw === 'charity') return { label: '公益行', tag: 'warning' as const }
  if (raw === 'inspection') return { label: '出海考察', tag: 'primary' as const }
  if (raw === 'salon') return { label: '行业沙龙', tag: 'info' as const }
  return { label: '亨嘉之会', tag: 'success' as const }
}

const frontModuleLabel = (value: any) => {
  const raw = String(value || '')
  if (raw === 'home-banner') return '首页轮播图'
  if (raw === 'service') return '服务页推荐'
  if (raw === 'activity-center') return '活动中心'
  return raw || '-'
}

const frontPositionLabel = (value: any) => {
  const raw = String(value || '')
  if (raw === 'top') return '顶部焦点位'
  if (raw === 'sidebar') return '侧边栏'
  if (raw === 'list') return '列表推荐位'
  return raw || '-'
}

const feeDisplay = (item: any) => {
  const fee = Number(item?.fee || 0)
  if (fee <= 0 || item?.is_free === true || item?.isFree === true || item?.fee_type === 'free' || item?.feeType === 'free') {
    return '免费'
  }
  const name = String(item?.fee_item_name || item?.feeItemName || '')
  return name ? `${name} (¥${fee})` : `¥${fee}`
}

const normalizeCities = (item: any) => {
  const cities = Array.isArray(item?.cities) ? item.cities.filter(Boolean) : []
  if (cities.length > 0) return cities
  const cityName = String(item?.city_name || item?.cityName || '').trim()
  return cityName ? [cityName] : ['全国']
}

const mapPublishRow = (item: any): PublishRow => {
  const status = normalizeStatus(item?.status)
  const activityType = activityTypeMeta(item?.sub_type || item?.subType)
  return {
    id: Number(item?.id || 0),
    activityNo: String(item?.activity_no || item?.activityNo || ''),
    title: String(item?.title || '未命名活动'),
    image: String(item?.cover_image || item?.coverImage || item?.image_url || DEFAULT_COVER),
    coverImage: String(item?.cover_image || item?.coverImage || item?.image_url || ''),
    videoUrl: String(item?.video_url || item?.videoUrl || ''),
    typeLabel: activityType.label,
    typeTag: activityType.tag,
    frontModuleLabel: frontModuleLabel(item?.front_module || item?.frontModule),
    frontPositionLabel: frontPositionLabel(item?.front_position || item?.frontPosition),
    frontModule: String(item?.front_module || item?.frontModule || 'activity-center'),
    frontPosition: String(item?.front_position || item?.frontPosition || 'list'),
    status,
    statusLabel: String(item?.status_label || item?.statusLabel || statusLabel(status)),
    submitTime: formatDateTime(item?.submit_time || item?.created_at || item?.createdAt),
    statusTime: formatDateTime(item?.changed_at || item?.changedAt || item?.updated_at || item?.updatedAt),
    summary: String(item?.summary || item?.description || ''),
    rejectReason: status === 'rejected' ? String(item?.change_reason || item?.changeReason || '') : '',
    cities: normalizeCities(item),
    activityDate: formatDateTime(item?.start_time || item?.startTime),
    feeDisplay: feeDisplay(item),
    feeType: String(item?.fee_type || item?.feeType || 'free'),
    feeItemId: String(item?.fee_item_id || item?.feeItemId || ''),
    fee: Number(item?.fee || 0),
    maxLimit: Number(item?.max_limit || item?.maxLimit || 0),
    subType: String(item?.sub_type || item?.subType || 'hengjia'),
  }
}

const getStatusDotColor = (status: string) => {
  if (status === 'published') return 'bg-green-500'
  if (status === 'rejected') return 'bg-red-500'
  if (status === 'offline') return 'bg-slate-400'
  return 'bg-orange-400 animate-pulse'
}

const getStatusTextColor = (status: string) => {
  if (status === 'published') return 'text-green-600'
  if (status === 'rejected') return 'text-red-600'
  if (status === 'offline') return 'text-slate-600'
  return 'text-orange-600'
}

const validateImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('封面图片只支持 JPG/PNG/WEBP 格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('封面图片大小不能超过 2MB')
    return false
  }
  return true
}

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => validateImageUpload(rawFile)

const beforeVideoUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['video/mp4', 'video/quicktime']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('视频只支持 MP4/MOV 格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 50) {
    ElMessage.error('视频大小不能超过 50MB')
    return false
  }
  return true
}

const handleImageSuccess: UploadProps['onSuccess'] = (response: any, uploadFile) => {
  if (Number(response?.code) !== 200 || !response?.data?.url) {
    ElMessage.error(response?.message || '图片上传失败')
    return
  }
  form.value.coverImage = response.data.url
  imageFileList.value = [{ name: uploadFile.name, url: response.data.url } as UploadUserFile]
  ElMessage.success('封面图片上传成功')
}

const handleVideoSuccess: UploadProps['onSuccess'] = (response: any, uploadFile) => {
  if (Number(response?.code) !== 200 || !response?.data?.url) {
    ElMessage.error(response?.message || '视频上传失败')
    return
  }
  form.value.videoUrl = response.data.url
  videoFileList.value = [{ name: uploadFile.name, url: response.data.url } as UploadUserFile]
  ElMessage.success('宣传视频上传成功')
}

const handleUploadError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败，请重试')
}

const handleImageRemove: UploadProps['onRemove'] = () => {
  form.value.coverImage = ''
  imageFileList.value = []
}

const handleVideoRemove: UploadProps['onRemove'] = () => {
  form.value.videoUrl = ''
  videoFileList.value = []
}

const resetForm = () => {
  isEdit.value = false
  currentEditId.value = null
  formRef.value?.clearValidate()
  form.value = createEmptyForm(partnerInfo.value.region)
  imageFileList.value = []
  videoFileList.value = []
}

const openCreateTab = () => {
  resetForm()
  activeTab.value = 'create'
}

const fillForm = (row: PublishRow) => {
  isEdit.value = true
  currentEditId.value = row.id
  form.value = {
    subType: row.subType || 'hengjia',
    title: row.title || '',
    frontModule: row.frontModule || 'activity-center',
    frontPosition: row.frontPosition || 'list',
    coverImage: row.coverImage || '',
    videoUrl: row.videoUrl || '',
    cities: row.cities?.length ? [...row.cities] : ['全国'],
    activityDate: row.activityDate || '',
    maxLimit: row.maxLimit || 0,
    feeType: row.fee > 0 ? 'paid' : 'free',
    feeItemId: row.feeItemId || '',
    summary: row.summary || '',
  }
  imageFileList.value = row.coverImage
    ? [{ name: 'cover-image', url: resolveFileUrl(row.coverImage) } as UploadUserFile]
    : []
  videoFileList.value = row.videoUrl
    ? [{ name: 'promo-video', url: resolveFileUrl(row.videoUrl) } as UploadUserFile]
    : []
}

const loadMyPublishes = async () => {
  tableLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/partner/publishes?page=1&page_size=100')
    const items = Array.isArray(res.data?.items) ? res.data.items : []
    myPublishList.value = items.map(mapPublishRow)
  } catch (error: any) {
    myPublishList.value = []
    ElMessage.error(error?.message || '读取发布列表失败')
  } finally {
    tableLoading.value = false
  }
}

const loadPublishDetail = async (id: number) => {
  const res = await apiRequest<any>(`/v3/partner/publishes/${id}`)
  return mapPublishRow(res.data || {})
}

const handleSearch = () => {
  // 当前页筛选为前端本地筛选，数据源统一由 loadMyPublishes() 拉取
}

const handleView = async (row: PublishRow) => {
  try {
    currentViewItem.value = await loadPublishDetail(row.id)
    viewDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(error?.message || '读取活动详情失败')
  }
}

const handleEdit = async (row: PublishRow) => {
  try {
    const detail = await loadPublishDetail(row.id)
    fillForm(detail)
    activeTab.value = 'create'
  } catch (error: any) {
    ElMessage.error(error?.message || '读取活动详情失败')
  }
}

const handleEditFromView = async () => {
  if (!currentViewItem.value) return
  viewDialogVisible.value = false
  await handleEdit(currentViewItem.value)
}

const handleDelete = (row: PublishRow) => {
  ElMessageBox.confirm('确定要撤回此活动发布申请吗？', '撤回确认', { type: 'warning' })
    .then(async () => {
      try {
        await apiRequest(`/v3/partner/publishes/${row.id}`, { method: 'DELETE' })
        ElMessage.success('已撤回')
        await loadMyPublishes()
      } catch (error: any) {
        ElMessage.error(error?.message || '撤回失败')
      }
    })
    .catch(() => {})
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请填写完整表单')
    return
  }

  if (form.value.feeType === 'paid' && !form.value.feeItemId) {
    ElMessage.warning('收费活动必须选择收费项目')
    return
  }

  const selectedFee = feeOptions.value.find(item => item.id === form.value.feeItemId)
  const fee = form.value.feeType === 'paid' ? Number(selectedFee?.price || 0) : 0

  submitting.value = true
  try {
    const url = isEdit.value && currentEditId.value
      ? `/v3/partner/publishes/${currentEditId.value}`
      : '/v3/partner/publishes'
    const method = isEdit.value && currentEditId.value ? 'PUT' : 'POST'

    await apiRequest(url, {
      method,
      body: {
        title: form.value.title.trim(),
        sub_type: form.value.subType,
        front_module: form.value.frontModule,
        front_position: form.value.frontPosition,
        summary: form.value.summary.trim(),
        cities: form.value.cities,
        cover_image: form.value.coverImage,
        video_url: form.value.videoUrl,
        fee_type: form.value.feeType,
        fee_item_id: form.value.feeType === 'paid' ? form.value.feeItemId : '',
        fee_item_name: form.value.feeType === 'paid' ? (selectedFee?.name || '') : '免费',
        fee,
        max_limit: form.value.maxLimit || 0,
        activity_time: form.value.activityDate,
      },
    })

    ElMessage.success(isEdit.value ? '修改已保存，活动已重新进入审核' : '提交成功，活动已进入审核队列')
    resetForm()
    activeTab.value = 'myPublish'
    await loadMyPublishes()
  } catch (error: any) {
    ElMessage.error(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const user = getLoginUser()
  partnerInfo.value = {
    name: user?.username || '合伙人',
    region: '上海',
    id: String(user?.id || ''),
  }
  resetForm()
  loadMyPublishes()
})
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
