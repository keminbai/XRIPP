<!-- 文件路径: D:\ipp-platform\app\pages\experts.vue -->
<template>
  <div class="bg-slate-50 min-h-screen pb-20">

    <!-- 顶部 Hero -->
    <div class="bg-slate-900 pt-24 pb-16 relative overflow-hidden shadow-inner">
      
      <!-- 背景纹理 -->
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>

      <!-- 蓝色光晕 -->
      <div class="absolute top-0 right-0 w-[900px] h-[900px] bg-blue-600/20 rounded-full blur-[140px] translate-x-1/3 -translate-y-1/2"></div>
      
      <div class="container mx-auto px-4 relative z-10">

        <!-- 小标签 -->
        <div class="flex justify-center mb-6">
          <span class="inline-flex items-center gap-2 px-4 py-1.5 text-xs font-semibold tracking-wide 
                       bg-blue-500/15 text-blue-300 rounded-full border border-blue-500/30 backdrop-blur-sm">
            👨‍💼 EXPERT NETWORK
          </span>
        </div>

        <!-- 主标题 -->
        <h1 class="text-3xl md:text-4xl font-bold text-white text-center tracking-tight mb-5">
          XRIPP 专家智库
        </h1>

        <!-- 副标题 -->
        <p class="text-slate-300 text-sm md:text-base text-center 
                  max-w-2xl mx-auto leading-relaxed mb-10">
          汇聚生命科学、医疗健康、智慧城市等领域资深专家，
          为企业提供一对一专业咨询与项目实战指导服务。
        </p>

        <!-- 统计数据 -->
        <div class="flex justify-center gap-14 flex-wrap">
          <div 
            v-for="stat in stats" 
            :key="stat.label" 
            class="text-center min-w-[80px]"
          >
            <div class="text-2xl md:text-3xl font-bold text-white">
              {{ stat.value }}
            </div>
            <div class="text-xs text-slate-400 mt-2 tracking-wide">
              {{ stat.label }}
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="container mx-auto px-4 -mt-8 relative z-20">
      <div class="bg-white rounded-2xl shadow-xl border border-slate-200 p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-4 items-center">
          
          <!-- 专家类型筛选 -->
          <div class="flex-1 w-full md:w-auto">
            <label class="text-xs font-bold text-slate-500 mb-2 block">专家委员会</label>
            <el-select v-model="filters.category" placeholder="全部专家委" size="large" class="w-full" clearable>
              <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value">
                <span class="flex items-center gap-2">
                  <span>{{ cat.icon }}</span>
                  <span>{{ cat.label }}</span>
                </span>
              </el-option>
            </el-select>
          </div>

          <!-- 行业领域筛选 -->
          <div class="flex-1 w-full md:w-auto">
            <label class="text-xs font-bold text-slate-500 mb-2 block">专业方向</label>
            <el-select v-model="filters.industry" placeholder="全部方向" size="large" class="w-full" clearable>
              <el-option v-for="ind in industries" :key="ind" :label="ind" :value="ind" />
            </el-select>
          </div>

          <!-- 关键词搜索 -->
          <div class="flex-1 w-full md:w-auto">
            <label class="text-xs font-bold text-slate-500 mb-2 block">关键词搜索</label>
            <el-input 
              v-model="filters.keyword" 
              placeholder="搜索专家姓名、专长..." 
              size="large"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 重置按钮 -->
          <div class="pt-6">
            <el-button size="large" @click="resetFilters" circle>
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 专家列表 -->
    <div class="container mx-auto px-4">
      
      <!-- 统计信息 -->
      <div class="flex justify-between items-center mb-6">
        <div class="text-sm text-slate-500">
          共找到 <span class="font-bold text-brand-600 text-lg">{{ filteredExperts.length }}</span> 位专家
        </div>
        <el-select v-model="sortBy" size="default" style="width: 140px">
          <el-option label="默认排序" value="default" />
          <el-option label="经验优先" value="experience" />
          <el-option label="最新入驻" value="newest" />
        </el-select>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-20">
        <el-icon class="text-4xl animate-spin text-brand-600"><Loading /></el-icon>
        <div class="text-slate-500 mt-4">正在加载专家数据...</div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="text-center py-20">
        <div class="text-xl text-red-500 mb-4">{{ error }}</div>
        <el-button type="primary" @click="fetchExperts">重新加载</el-button>
      </div>

      <!-- 专家卡片网格 -->
      <div v-else-if="sortedExperts.length > 0"
           class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
        <div 
          v-for="expert in sortedExperts" 
          :key="expert.id"
          class="bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-brand-200 hover:-translate-y-1 transition-all duration-300 overflow-hidden group"
        >
          <!-- 顶部装饰条 -->
          <div class="h-2 bg-gradient-to-r" :class="getCategoryGradient(expert.category)"></div>
          
          <div class="p-6">
            <!-- 头像+基本信息 -->
            <div class="flex items-start gap-4 mb-4">
              <div class="relative">
                <div class="w-16 h-16 rounded-full bg-gradient-to-br overflow-hidden border-4 border-white shadow-lg" :class="getCategoryBg(expert.category)">
                  <div class="w-full h-full flex items-center justify-center text-2xl font-bold text-white">
                    {{ expert.name.charAt(0) }}
                  </div>
                </div>
                <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-white rounded-full flex items-center justify-center shadow-md border border-slate-100">
                  <span class="text-sm">{{ getCategoryIcon(expert.category) }}</span>
                </div>
              </div>
              
              <div class="flex-1 min-w-0">
                <h3 class="text-lg font-bold text-slate-900 mb-0.5">{{ expert.name }}</h3>
                <p class="text-xs text-slate-500 mb-1">{{ expert.title }}</p>
                <div class="flex items-center gap-1 text-xs">
                  <el-tag size="small" effect="plain" :type="getCategoryTagType(expert.category)">
                    {{ getCategoryLabel(expert.category) }}
                  </el-tag>
                  <span class="text-slate-400">•</span>
                  <span class="text-slate-500">{{ expert.experience }}年经验</span>
                </div>
              </div>
            </div>

            <!-- 专长标签 -->
            <div class="mb-4">
              <div class="text-xs font-bold text-slate-500 mb-2">擅长领域</div>
              <div class="flex flex-wrap gap-1.5">
                <span 
                  v-for="skill in expert.skills" 
                  :key="skill" 
                  class="px-2 py-1 bg-slate-50 text-slate-600 text-xs rounded border border-slate-100 hover:bg-brand-50 hover:text-brand-700 hover:border-brand-200 transition-colors"
                >
                  {{ skill }}
                </span>
              </div>
            </div>

            <!-- 简介 -->
            <div class="mb-4">
              <p class="text-sm text-slate-600 leading-relaxed line-clamp-2">{{ expert.bio }}</p>
            </div>

            <!-- 底部按钮 -->
            <div class="flex gap-2 pt-4 border-t border-slate-100">
              <el-button type="primary" size="default" class="flex-1" @click="handleConsult(expert)">
                <el-icon class="mr-1"><ChatDotRound /></el-icon>
                预约咨询
              </el-button>
              <el-button size="default" @click="handleViewProfile(expert)" circle>
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果 -->
      <div v-else class="text-center py-20">
        <div class="text-6xl mb-4">🔍</div>
        <div class="text-xl font-bold text-slate-600 mb-2">未找到匹配的专家</div>
        <p class="text-slate-400 mb-6">请尝试调整筛选条件</p>
        <el-button type="primary" @click="resetFilters">
          <el-icon class="mr-1"><Refresh /></el-icon>
          重置筛选
        </el-button>
      </div>

    </div>

    <!-- 咨询弹窗 -->
    <el-dialog v-model="consultDialogVisible" :title="`预约咨询 - ${currentExpert?.name}`" width="550px" align-center>
      <div class="space-y-4">
        <div class="bg-slate-50 p-4 rounded-lg border border-slate-200 flex items-start gap-3">
          <el-icon class="text-blue-600 text-xl mt-0.5"><InfoFilled /></el-icon>
          <div class="text-sm text-slate-600">
            <p class="font-bold text-slate-800 mb-1">咨询服务说明</p>
            <p class="text-xs">专家将在1-3个工作日内与您联系，确认咨询时间与方式。咨询方式包括电话、视频会议或线下见面。</p>
          </div>
        </div>

        <el-form :model="consultForm" label-position="top" size="large">
          <el-form-item label="您的姓名" required>
            <el-input v-model="consultForm.name" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input v-model="consultForm.phone" placeholder="用于接收预约确认信息" />
          </el-form-item>
          <el-form-item label="公司名称">
            <el-input v-model="consultForm.company" placeholder="选填" />
          </el-form-item>
          <el-form-item label="咨询需求描述" required>
            <el-input 
              type="textarea" 
              :rows="4" 
              v-model="consultForm.description" 
              placeholder="请简要描述您的咨询需求，以便专家提前准备..."
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="flex gap-3">
          <el-button @click="consultDialogVisible = false" size="large" class="flex-1">取消</el-button>
          <el-button type="primary" @click="submitConsult" size="large" class="flex-1" :loading="submitting">
            <el-icon class="mr-1"><Check /></el-icon>
            提交预约
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Refresh, ChatDotRound, ArrowRight, 
  InfoFilled, Check, Loading 
} from '@element-plus/icons-vue'

useHead({ title: '专家智库 - XRIPP全球公共采购服务平台' })

const route = useRoute()

// 统计数据
const stats = [
  { value: '120+', label: '资深专家' },
  { value: '6', label: '专业领域' },
  { value: '3,800+', label: '服务企业' },
]

// ✅ 6大类专家分类（根据截图需求）
const categories = [
  { value: '生命科学', label: '生命科学专家委', icon: '🧬' },
  { value: '生态环境', label: '生态环境专家委', icon: '🌱' },
  { value: '医疗健康', label: '医疗健康专家委', icon: '⚕️' },
  { value: '智慧城市', label: '智慧城市专家委', icon: '🏙️' },
  { value: '信息技术', label: '信息技术专家委', icon: '💻' },
  { value: '机器人制造', label: '机器人制造专家委', icon: '🤖' },
]

// 专业方向
const industries = [
  '基因编辑', '新药研发', '临床医学', '环保技术', 
  '智能交通', '5G通信', '人工智能', '工业机器人', '其他'
]

// 筛选条件
const filters = ref({
  category: '',
  industry: '',
  keyword: ''
})

const sortBy = ref('default')
const experts = ref<any[]>([])
const loading = ref(false)
const error = ref('')

// 专家智库暂无后端API，展示"即将上线"提示
const fetchExperts = async () => {
  loading.value = true
  error.value = ''

  try {
    // 专家智库模块尚在开发中，暂无后端 API
    // 显示空列表 + 提示
    experts.value = []
    error.value = '专家智库功能即将上线，敬请期待！'
  } finally {
    loading.value = false
  }
}

// ✅ 组件挂载时：1.获取数据 2.读取URL参数自动筛选
onMounted(async () => {
  await fetchExperts()
  
  // 从URL参数读取category并自动筛选
  if (route.query.category) {
    filters.value.category = route.query.category as string
  }
})

// 筛选逻辑
const filteredExperts = computed(() => {
  const { category, industry, keyword } = filters.value

  return experts.value.filter((expert) => {
    if (category && expert.category !== category) return false
    if (industry && expert.industry !== industry) return false
    
    if (keyword) {
      const k = keyword.toLowerCase()
      const nameMatch = expert.name?.toLowerCase().includes(k)
      const titleMatch = expert.title?.toLowerCase().includes(k)
      const bioMatch = expert.bio?.toLowerCase().includes(k)
      const skillMatch = expert.skills?.some((s: string) => s?.toLowerCase().includes(k))
      
      if (!(nameMatch || titleMatch || bioMatch || skillMatch)) return false
    }
    
    return true
  })
})

// 排序逻辑
const sortedExperts = computed(() => {
  const list = [...filteredExperts.value]
  
  if (sortBy.value === 'experience') {
    return list.sort((a, b) => b.experience - a.experience)
  }
  
  if (sortBy.value === 'newest') {
    return list.sort((a, b) => 
      new Date(b.created_at).getTime() - new Date(a.created_at).getTime()
    )
  }
  
  return list
})

const resetFilters = () => {
  filters.value = { category: '', industry: '', keyword: '' }
  sortBy.value = 'default'
}

// 获取类别样式
const getCategoryGradient = (category: string) => {
  const gradients: Record<string, string> = {
    '生命科学': 'from-blue-500 to-blue-600',
    '生态环境': 'from-green-500 to-green-600',
    '医疗健康': 'from-red-500 to-red-600',
    '智慧城市': 'from-purple-500 to-purple-600',
    '信息技术': 'from-cyan-500 to-cyan-600',
    '机器人制造': 'from-orange-500 to-orange-600',
  }
  return gradients[category] || 'from-slate-500 to-slate-600'
}

const getCategoryBg = (category: string) => {
  const bgs: Record<string, string> = {
    '生命科学': 'from-blue-400 to-blue-600',
    '生态环境': 'from-green-400 to-green-600',
    '医疗健康': 'from-red-400 to-red-600',
    '智慧城市': 'from-purple-400 to-purple-600',
    '信息技术': 'from-cyan-400 to-cyan-600',
    '机器人制造': 'from-orange-400 to-orange-600',
  }
  return bgs[category] || 'from-slate-400 to-slate-600'
}

const getCategoryIcon = (category: string) => {
  const cat = categories.find(c => c.value === category)
  return cat?.icon || '👤'
}

const getCategoryLabel = (category: string) => {
  const cat = categories.find(c => c.value === category)
  return cat?.label || '专家'
}

const getCategoryTagType = (category: string): any => {
  const types: Record<string, string> = {
    '生命科学': 'primary',
    '生态环境': 'success',
    '医疗健康': 'danger',
    '智慧城市': '',
    '信息技术': 'info',
    '机器人制造': 'warning',
  }
  return types[category] || ''
}

// 咨询相关
const consultDialogVisible = ref(false)
const currentExpert = ref<any>(null)
const submitting = ref(false)

const consultForm = ref({
  name: '',
  phone: '',
  company: '',
  description: ''
})

const handleConsult = (expert: any) => {
  currentExpert.value = expert
  consultDialogVisible.value = true
  consultForm.value = { name: '', phone: '', company: '', description: '' }
}

const submitConsult = async () => {
  if (!consultForm.value.name || 
      !consultForm.value.phone || 
      !consultForm.value.description) {
    return ElMessage.warning('请填写必填项')
  }

  submitting.value = true

  try {
    // 生产环境替换为真实API
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    consultDialogVisible.value = false
    ElMessage.success('预约提交成功！专家将在1-3个工作日内与您联系')
    
    consultForm.value = { name: '', phone: '', company: '', description: '' }
  } catch (e) {
    ElMessage.error('预约提交失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}

const handleViewProfile = (expert: any) => {
  navigateTo(`/experts/${expert.id}`)
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>