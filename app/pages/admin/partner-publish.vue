<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\partner-publish.vue
  版本: V1.3 (2026-02-07)
  
  ✅ 核心修正 (P2):
  1. [退款策略] 已彻底删除 refundPolicy 相关字段及 UI。
  2. [费用关联] 收费模式改为关联后台配置的收费项目 (feeOptions)，禁止随意输入金额。
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 顶部欢迎与引导 -->
    <div class="bg-gradient-to-r from-blue-600 to-purple-700 p-8 rounded-xl text-white shadow-lg">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold mb-2">欢迎，{{ partnerInfo.name }} 🎉</h1>
          <p class="text-blue-100 text-sm">
            您可以在此发布活动、培训、商机等内容，提交后将由总部审核，审核通过后自动上线至前台。
          </p>
        </div>
        <div class="text-right">
          <div class="text-3xl font-bold">{{ partnerInfo.region }}</div>
          <div class="text-xs text-blue-200 mt-1">服务区域</div>
        </div>
      </div>
    </div>

    <!-- 统计数据 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
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

    <!-- 主体内容区 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      
      <!-- Tabs 切换 -->
      <div class="border-b border-slate-100">
        <el-tabs v-model="activeTab" class="px-6 pt-2">
          <el-tab-pane label="我的发布" name="myPublish">
            <template #label>
              <span class="flex items-center gap-2">
                <el-icon><Document /></el-icon> 我的发布
                <el-badge :value="myPublishList.length" type="primary" v-if="myPublishList.length > 0" />
              </span>
            </template>
          </el-tab-pane>
          
          <el-tab-pane label="新建内容" name="create">
            <template #label>
              <span class="flex items-center gap-2">
                <el-icon><Plus /></el-icon> 新建内容
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- Tab 1: 我的发布列表 -->
      <div v-if="activeTab === 'myPublish'" class="p-6">
        
        <!-- 筛选栏 -->
        <div class="mb-4 flex gap-3">
          <el-select v-model="statusFilter" placeholder="审核状态" class="w-32" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
          <el-select v-model="typeFilter" placeholder="内容类型" class="w-32" clearable>
            <el-option label="活动" value="activity" />
            <el-option label="培训" value="training" />
            <el-option label="商机" value="business" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>

        <!-- 发布列表 -->
        <el-table v-loading="tableLoading" :data="filteredPublishList" style="width: 100%" :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
          <el-table-column prop="submitTime" label="提交时间" width="160" />
          
          <el-table-column label="内容概览" min-width="280">
            <template #default="scope">
              <div class="flex gap-3 py-2">
                <img :src="scope.row.image" class="w-16 h-10 rounded object-cover border border-slate-200" />
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-slate-800 text-sm line-clamp-1">{{ scope.row.title }}</div>
                  <div class="flex gap-2 mt-1">
                    <el-tag size="small" :type="scope.row.typeTag">{{ scope.row.typeLabel }}</el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="前台位置" width="160">
            <template #default="scope">
              <div class="text-xs">
                <div class="font-medium text-slate-700">{{ scope.row.frontModuleLabel }}</div>
                <div class="text-slate-400 mt-0.5">{{ scope.row.frontPositionLabel }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="140">
            <template #default="scope">
              <div class="flex items-center gap-2">
                <span class="w-2 h-2 rounded-full" :class="getStatusDotColor(scope.row.status)"></span>
                <span class="text-sm font-medium" :class="getStatusTextColor(scope.row.status)">
                  {{ scope.row.statusLabel }}
                </span>
              </div>
              <div class="text-[10px] text-slate-400 mt-0.5" v-if="scope.row.auditTime">
                {{ scope.row.auditTime }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleView(scope.row)">
                <el-icon><View /></el-icon> 查看
              </el-button>
              <el-button 
                link 
                type="warning" 
                size="small" 
                @click="handleEdit(scope.row)" 
                v-if="scope.row.status === 'pending' || scope.row.status === 'rejected'"
              >
                <el-icon><Edit /></el-icon> 修改
              </el-button>
              <el-button 
                link 
                type="danger" 
                size="small" 
                @click="handleDelete(scope.row)" 
                v-if="scope.row.status === 'pending'"
              >
                <el-icon><Delete /></el-icon> 撤回
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="filteredPublishList.length === 0" class="text-center py-16 text-slate-400">
          <el-icon class="text-6xl mb-4"><DocumentRemove /></el-icon>
          <div class="text-lg">暂无发布记录</div>
          <el-button type="primary" class="mt-4" @click="activeTab = 'create'">立即发布内容</el-button>
        </div>

      </div>

      <!-- Tab 2: 新建内容表单 -->
      <div v-if="activeTab === 'create'" class="p-6">
        <el-form :model="form" label-width="130px" ref="formRef" :rules="formRules">
          
          <!-- A. 内容定位 -->
          <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-blue-500 pl-2">内容分类</h4>
            
            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="内容板块" prop="section" required>
                <el-select v-model="form.section" class="w-full" @change="handleSectionChange">
                  <el-option label="活动中心" value="activity">
                    <div class="flex items-center gap-2">
                      <el-icon><Calendar /></el-icon> 活动中心
                    </div>
                  </el-option>
                  <el-option label="培训中心" value="training">
                    <div class="flex items-center gap-2">
                      <el-icon><Reading /></el-icon> 培训中心
                    </div>
                  </el-option>
                  <el-option label="商机对接" value="business">
                    <div class="flex items-center gap-2">
                      <el-icon><Suitcase /></el-icon> 商机对接
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="细分类型" prop="subType" required>
                <el-select v-model="form.subType" class="w-full" :disabled="!form.section">
                  <template v-if="form.section === 'activity'">
                    <el-option label="享嘉之会" value="hengjia" />
                    <el-option label="公益行" value="charity" />
                    <el-option label="出海考察" value="inspection" />
                  </template>
                  <template v-if="form.section === 'training'">
                    <el-option label="线上课程" value="online" />
                    <el-option label="线下培训" value="offline" />
                    <el-option label="认证考试" value="exam" />
                  </template>
                  <template v-if="form.section === 'business'">
                    <el-option label="供需发布" value="matching" />
                    <el-option label="标书代写" value="tender" />
                    <el-option label="认证服务" value="cert" />
                  </template>
                </el-select>
              </el-form-item>
            </div>

            <!-- 前台展示位置配置 -->
            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="前台主模块" prop="frontModule" required>
                <el-select v-model="form.frontModule" class="w-full">
                  <el-option label="首页轮播图" value="home-banner" />
                  <el-option label="活动中心" value="activity-center" />
                  <el-option label="培训中心" value="training-center" />
                  <el-option label="商机对接" value="business-matching" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="详细位置" prop="frontPosition" required>
                <el-select v-model="form.frontPosition" class="w-full">
                  <el-option label="顶部焦点位" value="top" />
                  <el-option label="列表推荐位" value="list" />
                  <el-option label="侧边栏" value="sidebar" />
                </el-select>
              </el-form-item>
            </div>

            <el-form-item label="内容标题" prop="title" required>
              <el-input 
                v-model="form.title" 
                placeholder="请输入标题（30字以内）" 
                maxlength="30" 
                show-word-limit 
              />
            </el-form-item>
          </div>

          <!-- B. 媒体素材 -->
          <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-orange-500 pl-2">媒体素材</h4>
            
            <el-form-item label="封面图片" prop="coverImage" required>
              <el-upload 
                action="#" 
                list-type="picture-card" 
                :limit="1" 
                :auto-upload="false"
                :on-change="handleImageChange"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="text-xs text-slate-400 mt-1">建议尺寸：1200x675px，不超过2MB</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="宣传视频">
              <el-upload action="#" :limit="1" :auto-upload="false" accept="video/*">
                <el-button icon="VideoPlay" type="primary" plain>上传视频 (MP4)</el-button>
              </el-upload>
              <div class="text-xs text-slate-400 mt-2">选填，不超过 500MB</div>
            </el-form-item>
          </div>

          <!-- C. 业务规则（活动/培训专用） -->
          <div v-if="['activity', 'training'].includes(form.section)" class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
            <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-green-500 pl-2">业务规则</h4>
            
            <el-form-item label="投放城市" prop="cities" required>
              <el-select 
                v-model="form.cities" 
                multiple 
                collapse-tags 
                placeholder="选择投放城市 (支持多选)" 
                class="w-full"
              >
                <el-option v-for="c in availableCities" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>

            <div class="grid grid-cols-2 gap-6">
              <el-form-item label="活动时间" prop="activityDate">
                <el-date-picker 
                  v-model="form.activityDate" 
                  type="datetime" 
                  class="!w-full" 
                  placeholder="选择活动时间"
                />
              </el-form-item>
              
              <el-form-item label="名额限制">
                <el-input-number v-model="form.maxLimit" :min="0" :max="500" class="!w-full" />
              </el-form-item>
            </div>

            <!-- ✅ P2修正：关联后台配置的收费项目 -->
            <el-form-item label="费用设置">
              <div class="space-y-3 w-full">
                <el-radio-group v-model="form.feeType">
                  <el-radio label="free">免费活动</el-radio>
                  <el-radio label="paid">收费活动</el-radio>
                </el-radio-group>
                
                <div v-if="form.feeType === 'paid'" class="flex items-center gap-2">
                  <el-select v-model="form.feeItemId" placeholder="请选择关联的收费项目" class="w-full">
                    <el-option 
                      v-for="item in feeOptions" 
                      :key="item.id" 
                      :label="`${item.name} (¥${item.price})`" 
                      :value="item.id" 
                    />
                  </el-select>
                  <div class="text-xs text-slate-400 whitespace-nowrap">
                    * 费用标准由总部统一配置
                  </div>
                </div>
              </div>
            </el-form-item>

            <!-- ❌ P2修正：已彻底删除退款策略字段 -->
          </div>

          <!-- D. 内容摘要 -->
          <el-form-item label="内容摘要" prop="summary" required>
            <el-input 
              v-model="form.summary" 
              type="textarea" 
              :rows="4" 
              maxlength="200" 
              show-word-limit 
              placeholder="请输入内容摘要，将显示在列表预览中..."
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <div class="flex justify-center gap-4 pt-6 border-t border-slate-200">
            <el-button size="large" @click="handleReset">重置</el-button>
            <el-button 
              type="primary" 
              size="large" 
              @click="handleSubmit" 
              :loading="submitting"
            >
              <el-icon class="mr-2" v-if="!submitting"><Upload /></el-icon>
              {{ submitting ? '提交中...' : '提交审核' }}
            </el-button>
          </div>

          <!-- 温馨提示 -->
          <div class="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div class="flex items-start gap-2 text-sm text-blue-700">
              <el-icon class="mt-0.5"><InfoFilled /></el-icon>
              <div>
                <div class="font-bold mb-1">温馨提示</div>
                <ul class="text-xs space-y-1 list-disc list-inside text-blue-600">
                  <li>提交后将由总部审核，请确保内容真实、合规</li>
                  <li>审核通过后内容将自动上线至前台对应位置</li>
                  <li>如有疑问，请联系总部运营：400-123-4567</li>
                </ul>
              </div>
            </div>
          </div>

        </el-form>
      </div>

    </div>

    <!-- 详情查看弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="内容详情" width="650px">
      <div v-if="currentViewItem" class="space-y-4">
        
        <div class="flex gap-4">
          <img :src="currentViewItem.image" class="w-32 h-20 rounded-lg object-cover border" />
          <div class="flex-1">
            <div class="font-bold text-lg text-slate-800 mb-2">{{ currentViewItem.title }}</div>
            <div class="flex gap-2 mb-2">
              <el-tag size="small" :type="currentViewItem.typeTag">{{ currentViewItem.typeLabel }}</el-tag>
            </div>
            <div class="text-xs text-slate-500">提交时间: {{ currentViewItem.submitTime }}</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3 p-3 bg-slate-50 rounded">
          <div>
            <div class="text-xs text-slate-500 mb-1">前台位置</div>
            <div class="font-medium text-slate-800">{{ currentViewItem.frontModuleLabel }}</div>
            <div class="text-xs text-slate-400">{{ currentViewItem.frontPositionLabel }}</div>
          </div>
          <div>
            <div class="text-xs text-slate-500 mb-1">审核状态</div>
            <el-tag :type="getStatusTagType(currentViewItem.status)">
              {{ currentViewItem.statusLabel }}
            </el-tag>
          </div>
        </div>

        <!-- 驳回原因（如果有） -->
        <div v-if="currentViewItem.status === 'rejected' && currentViewItem.rejectReason" class="p-4 bg-red-50 border border-red-200 rounded-lg">
          <div class="font-bold text-red-700 mb-2 flex items-center gap-2">
            <el-icon><WarningFilled /></el-icon> 驳回原因
          </div>
          <div class="text-sm text-red-600">{{ currentViewItem.rejectReason }}</div>
        </div>

        <!-- 审核意见（如果有） -->
        <div v-if="currentViewItem.auditRemark" class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <div class="font-bold text-yellow-700 mb-2">审核意见</div>
          <div class="text-sm text-yellow-600">{{ currentViewItem.auditRemark }}</div>
        </div>

      </div>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button 
          type="warning" 
          @click="handleEditFromView" 
          v-if="currentViewItem && (currentViewItem.status === 'pending' || currentViewItem.status === 'rejected')"
        >
          修改重提
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Plus, Document, Calendar, Reading, Suitcase, VideoPlay, Upload, 
  View, Edit, Delete, DocumentRemove, InfoFilled, WarningFilled
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest, getLoginUser } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// 合伙人信息（从登录态获取）
const partnerInfo = ref({
  name: '合伙人',
  region: '上海',
  id: ''
})

const activeTab = ref('myPublish')
const statusFilter = ref('')
const typeFilter = ref('')
const viewDialogVisible = ref(false)
const currentViewItem = ref<any>(null)
const submitting = ref(false)
const tableLoading = ref(false)
const formRef = ref()

// 🆕 模拟从后台获取的收费项目配置
const feeOptions = ref([
  { id: 'FEE001', name: '标准活动票', price: 199 },
  { id: 'FEE002', name: '高端沙龙票', price: 299 },
  { id: 'FEE003', name: '专业培训课', price: 599 },
  { id: 'FEE004', name: '出海考察团', price: 19800 }
])

// 统计数据（实时）
const stats = computed(() => {
  const all = myPublishList.value.length
  const pending = myPublishList.value.filter(i => i.status === 'pending').length
  const approved = myPublishList.value.filter(i => i.status === 'approved').length
  const rejected = myPublishList.value.filter(i => i.status === 'rejected').length

  return [
    { label: '累计发布', value: String(all), valueClass: 'text-blue-600', icon: Document, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '待审核', value: String(pending), valueClass: 'text-orange-600', icon: 'Clock', bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已通过', value: String(approved), valueClass: 'text-green-600', icon: 'CircleCheck', bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已驳回', value: String(rejected), valueClass: 'text-red-600', icon: 'CircleClose', bgClass: 'bg-red-50', textClass: 'text-red-600' }
  ]
})

// 可用城市（根据合伙人权限）
const availableCities = computed(() => {
  // 合伙人只能选择自己服务区域及周边
  return ['全国', partnerInfo.value.region, '苏州', '杭州', '南京', '无锡']
})

// 我的发布列表（后端接口返回）
const myPublishList = ref<any[]>([])

// 表单数据
const form = ref({
  section: '',
  subType: '',
  title: '',
  frontModule: '',
  frontPosition: '',
  coverImage: null,
  cities: [],
  activityDate: '',
  maxLimit: 50,
  feeType: 'free',
  feeItemId: '', // 关联收费项目ID
  summary: ''
})

// 表单验证规则
const formRules = {
  section: [{ required: true, message: '请选择内容板块', trigger: 'change' }],
  subType: [{ required: true, message: '请选择细分类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  frontModule: [{ required: true, message: '请选择前台主模块', trigger: 'change' }],
  frontPosition: [{ required: true, message: '请选择详细位置', trigger: 'change' }],
  cities: [{ required: true, message: '请选择投放城市', trigger: 'change' }],
  summary: [{ required: true, message: '请输入内容摘要', trigger: 'blur' }]
}

// 筛选后的发布列表
const filteredPublishList = computed(() => {
  let list = myPublishList.value
  
  if (statusFilter.value) {
    list = list.filter(item => item.status === statusFilter.value)
  }
  
  if (typeFilter.value) {
    list = list.filter(item => item.section === typeFilter.value)
  }
  
  return list
})

// 工具函数
const getStatusDotColor = (status: string) => {
  const colors: Record<string, string> = {
    'pending': 'bg-orange-400 animate-pulse',
    'approved': 'bg-green-500',
    'rejected': 'bg-red-500'
  }
  return colors[status] || 'bg-slate-300'
}

const getStatusTextColor = (status: string) => {
  const colors: Record<string, string> = {
    'pending': 'text-orange-600',
    'approved': 'text-green-600',
    'rejected': 'text-red-600'
  }
  return colors[status] || 'text-slate-600'
}

const getStatusTagType = (status: string) => {
  const types: Record<string, any> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return types[status] || 'info'
}

// 交互逻辑
const formatDateTime = (v: any) => {
  if (!v) return ''
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const loadMyPublishes = async () => {
  tableLoading.value = true
  try {
    const res = await apiRequest<any>('/v3/partner/publishes?page=1&page_size=100')
    const items = res.data?.items || []
    myPublishList.value = items.map((item: any) => ({
      id: item.id,
      submitTime: formatDateTime(item.submit_time),
      title: item.title,
      typeLabel: '活动',
      typeTag: 'success',
      section: item.section || 'activity',
      image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=200',
      frontModule: 'activity-center',
      frontModuleLabel: '活动中心',
      frontPosition: 'list',
      frontPositionLabel: '列表推荐位',
      status: item.status || 'pending',
      statusLabel: item.status_label || '待审核'
    }))
  } catch (e: any) {
    ElMessage.error(e?.message || '读取发布列表失败')
    myPublishList.value = []
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  // 当前页面筛选为前端条件筛选，数据源由 loadMyPublishes() 统一拉取
}

const handleSectionChange = () => {
  form.value.subType = ''
}

const handleImageChange = (file: any) => {
  form.value.coverImage = file
}

const handleView = (row: any) => {
  currentViewItem.value = row
  viewDialogVisible.value = true
}

const handleEdit = (row: any) => {
  ElMessage.info('编辑功能开发中...')
  activeTab.value = 'create'
}

const handleEditFromView = () => {
  viewDialogVisible.value = false
  handleEdit(currentViewItem.value)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要撤回此发布申请吗？', '撤回确认', {
    type: 'warning'
  }).then(async () => {
    try {
      await apiRequest(`/v3/partner/publishes/${row.id}`, { method: 'DELETE' })
      ElMessage.success('已撤回')
      await loadMyPublishes()
    } catch (e: any) {
      ElMessage.error(e?.message || '撤回失败')
    }
  })
}

const handleReset = () => {
  formRef.value?.resetFields()
  form.value = {
    section: '',
    subType: '',
    title: '',
    frontModule: '',
    frontPosition: '',
    coverImage: null,
    cities: [],
    activityDate: '',
    maxLimit: 50,
    feeType: 'free',
    feeItemId: '',
    summary: ''
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请填写完整表单')
    return
  }

  if (form.value.feeType === 'paid' && !form.value.feeItemId) {
    ElMessage.warning('收费活动必须选择收费标准')
    return
  }

  const selectedFee = feeOptions.value.find(i => i.id === form.value.feeItemId)
  const fee = form.value.feeType === 'paid' ? Number(selectedFee?.price || 0) : 0

  submitting.value = true
  try {
    await apiRequest('/v3/partner/publishes', {
      method: 'POST',
      body: {
        title: form.value.title,
        section: form.value.section,
        sub_type: form.value.subType,
        front_module: form.value.frontModule,
        front_position: form.value.frontPosition,
        summary: form.value.summary,
        cities: form.value.cities,
        fee_type: form.value.feeType,
        fee_item_id: form.value.feeItemId,
        fee,
        activity_time: form.value.activityDate
      }
    })

    ElMessage.success({
      message: '提交成功！内容已进入审核队列',
      duration: 3000
    })
    handleReset()
    activeTab.value = 'myPublish'
    await loadMyPublishes()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const u = getLoginUser()
  partnerInfo.value = {
    name: u?.username || '合伙人',
    region: '上海',
    id: String(u?.id || '')
  }
  loadMyPublishes()
})
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}
</style>
