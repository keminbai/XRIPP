<!-- 文件路径: D:\ipp-platform\app\pages\member\favorites.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 筛选与统计 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-5 rounded-xl border border-slate-200 hover:shadow-md transition-all cursor-pointer group">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-slate-500">{{ stat.label }}</span>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center transition-colors" :class="stat.bgClass">
            <el-icon class="text-lg" :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-900 group-hover:text-brand-600 transition-colors">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4">
      <div class="flex flex-wrap gap-3 items-center">
        <el-input 
          v-model="keyword" 
          placeholder="搜索标书标题..." 
          class="w-64"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        
        <el-select v-model="filterType" placeholder="收藏类型" class="w-40" clearable>
          <el-option label="全部" value="" />
          <el-option label="标书" value="tender" />
          <el-option label="活动" value="activity" />
          <el-option label="服务商" value="supplier" />
        </el-select>

        <el-select v-model="filterTime" placeholder="收藏时间" class="w-40" clearable>
          <el-option label="全部" value="" />
          <el-option label="最近7天" value="7" />
          <el-option label="最近30天" value="30" />
          <el-option label="更早" value="90" />
        </el-select>

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon> 搜索
        </el-button>

        <el-button v-if="selectedItems.length > 0" type="danger" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除 ({{ selectedItems.length }})
        </el-button>

        <div class="ml-auto text-sm text-slate-400">
          共 {{ filteredFavorites.length }} 条收藏
        </div>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="space-y-4">
      <div v-if="filteredFavorites.length === 0" class="bg-white rounded-xl border border-slate-200 p-16 text-center">
        <div class="w-24 h-24 mx-auto bg-slate-50 rounded-full flex items-center justify-center mb-4 text-5xl">
          {{ keyword ? '🔍' : '⭐' }}
        </div>
        <h3 class="text-lg font-bold text-slate-900 mb-2">
          {{ keyword ? '没有找到匹配的收藏' : '还没有收藏内容' }}
        </h3>
        <p class="text-slate-500 mb-6">
          {{ keyword ? '试试调整搜索条件' : '浏览感兴趣的标书时，点击"收藏"按钮即可' }}
        </p>
        <el-button type="primary" @click="navigateTo('/procurement')">
          <el-icon><Search /></el-icon> 去浏览商机
        </el-button>
      </div>

      <!-- 收藏卡片 -->
      <div 
        v-for="item in filteredFavorites" 
        :key="item.id"
        class="group bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-lg hover:border-brand-200 transition-all overflow-hidden cursor-pointer"
        @click="handleViewDetail(item)"
      >
        <div class="flex gap-6 p-6">
          <!-- 左侧：图标 -->
          <div class="flex-shrink-0">
            <div class="w-20 h-20 rounded-xl flex items-center justify-center text-4xl transition-transform group-hover:scale-110 duration-300" :class="getIconBg(item.type)">
              {{ getIcon(item.type) }}
            </div>
          </div>

          <!-- 中间：信息 -->
          <div class="flex-grow min-w-0">
            <!-- 标签行 -->
            <div class="flex items-center gap-3 mb-3">
              <span class="px-2.5 py-1 rounded-md text-xs font-bold uppercase" :class="getTypeClass(item.type)">
                {{ getTypeText(item.type) }}
              </span>
              <span v-if="item.organization" class="text-xs text-slate-400 flex items-center gap-1">
                <el-icon><OfficeBuilding /></el-icon> {{ item.organization }}
              </span>
              <span class="text-xs text-slate-400 flex items-center gap-1 ml-auto">
                <el-icon><Clock /></el-icon> 收藏于 {{ item.favoriteTime }}
              </span>
            </div>

            <!-- 标题 -->
            <h4 class="text-lg font-bold text-slate-900 mb-2 group-hover:text-brand-600 transition-colors line-clamp-1">
              {{ item.title }}
            </h4>

            <!-- 描述 -->
            <p class="text-sm text-slate-500 mb-4 line-clamp-2">
              {{ item.description }}
            </p>

            <!-- 底部元信息 -->
            <div class="flex items-center gap-4 text-xs text-slate-400">
              <span v-if="item.deadline" class="flex items-center gap-1">
                <el-icon><Timer /></el-icon> 
                <span :class="isExpiringSoon(item.deadline) ? 'text-red-500 font-bold' : ''">
                  截止: {{ item.deadline }}
                </span>
              </span>
              <span v-if="item.location" class="flex items-center gap-1">
                <el-icon><Location /></el-icon> {{ item.location }}
              </span>
              <span v-if="item.amount" class="flex items-center gap-1 text-green-600 font-bold">
                <el-icon><Money /></el-icon> {{ item.amount }}
              </span>
            </div>
          </div>

          <!-- 右侧：操作 -->
          <div class="flex flex-col justify-between items-end gap-3 min-w-[100px]">
            <!-- 选择框 -->
            <el-checkbox 
              v-model="selectedItems" 
              :label="item.id" 
              @click.stop
              class="!m-0"
            />

            <!-- 按钮组 -->
            <div class="flex flex-col gap-2 w-full">
              <el-button 
                size="small" 
                class="w-full"
                @click.stop="handleViewDetail(item)"
              >
                查看详情
              </el-button>
              <el-button 
                link 
                type="danger" 
                size="small"
                @click.stop="handleDelete(item)"
              >
                <el-icon><Star /></el-icon> 取消收藏
              </el-button>
            </div>
          </div>
        </div>

        <!-- 过期提示条 -->
        <div v-if="item.isExpired" class="bg-red-50 border-t border-red-100 px-6 py-3 flex items-center gap-2 text-red-600 text-sm">
          <el-icon><Warning /></el-icon>
          <span class="font-medium">此项目已过期或截止</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center" v-if="filteredFavorites.length > 10">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="10"
        background
        layout="prev, pager, next"
        :total="filteredFavorites.length"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Search, Delete, Clock, Timer, Location, Money, OfficeBuilding, 
  Warning, Star, Document, Calendar 
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'member' })

const keyword = ref('')
const filterType = ref('')
const filterTime = ref('')
const currentPage = ref(1)
const selectedItems = ref([])

const stats = [
  { label: '全部收藏', value: '28', icon: Star, bgClass: 'bg-yellow-50 group-hover:bg-yellow-100', textClass: 'text-yellow-600' },
  { label: '标书收藏', value: '18', icon: Document, bgClass: 'bg-blue-50 group-hover:bg-blue-100', textClass: 'text-blue-600' },
  { label: '活动收藏', value: '7', icon: Calendar, bgClass: 'bg-purple-50 group-hover:bg-purple-100', textClass: 'text-purple-600' },
  { label: '即将截止', value: '3', icon: Warning, bgClass: 'bg-red-50 group-hover:bg-red-100', textClass: 'text-red-600' },
]

const favorites = ref([
  {
    id: 1,
    type: 'tender',
    title: '肯尼亚教育部 - 数字化教学设备采购项目 (第1期)',
    description: '为肯尼亚农村地区小学供应5000台加固型平板电脑及配套教学软件系统',
    organization: 'UNICEF (联合国儿童基金会)',
    location: '肯尼亚',
    deadline: '2026-02-15',
    amount: '$750,000',
    favoriteTime: '2026-01-10 14:30',
    isExpired: false
  },
  {
    id: 2,
    type: 'tender',
    title: '马来西亚能源部 - 50MW太阳能发电站二期建设工程',
    description: '马来西亚槟城50MW太阳能农场的EPC总承包合同',
    organization: '马来西亚能源部',
    location: '马来西亚',
    deadline: '2026-03-01',
    amount: '$45,000,000',
    favoriteTime: '2026-01-08 09:15',
    isExpired: false
  },
  {
    id: 3,
    type: 'tender',
    title: '应急野战医院医疗设备供应项目',
    description: '为苏丹达尔富尔地区部署的野战医院采购X光机、呼吸机、手术台等医疗设备',
    organization: 'UNOPS (联合国项目事务署)',
    location: '苏丹',
    deadline: '2026-01-20',
    amount: '$2,500,000',
    favoriteTime: '2025-12-28 16:45',
    isExpired: false
  },
  {
    id: 4,
    type: 'activity',
    title: '2025全球公共采购高峰论坛',
    description: '汇聚联合国采购官、国际组织代表，解读2025年全球公共采购趋势',
    organization: 'XRIPP Global',
    location: '上海',
    deadline: '2026-02-28',
    amount: null,
    favoriteTime: '2026-01-05 11:20',
    isExpired: false
  },
  {
    id: 5,
    type: 'supplier',
    title: '德国施耐德电气（中国）有限公司',
    description: '全球能源管理和自动化领域数字化转型专家',
    organization: null,
    location: '上海',
    deadline: null,
    amount: null,
    favoriteTime: '2025-12-20 08:30',
    isExpired: false
  },
  {
    id: 6,
    type: 'tender',
    title: '越南水利部 - 湄公河三角洲水净化设备采购',
    description: '用于越南湄公河三角洲洪水救援行动的移动水净化装置',
    organization: '国际红十字会',
    location: '越南',
    deadline: '2025-12-31',
    amount: '$1,200,000',
    favoriteTime: '2025-12-15 10:10',
    isExpired: true
  },
])

const filteredFavorites = computed(() => {
  return favorites.value.filter(item => {
    const matchKeyword = !keyword.value || 
      item.title.toLowerCase().includes(keyword.value.toLowerCase()) ||
      (item.description && item.description.includes(keyword.value))
    
    const matchType = !filterType.value || item.type === filterType.value
    
    // 时间筛选逻辑（简化版）
    const matchTime = !filterTime.value // 这里可以根据实际需求实现

    return matchKeyword && matchType && matchTime
  })
})

const getIcon = (type: string) => {
  const icons = {
    tender: '📄',
    activity: '📅',
    supplier: '🏢'
  }
  return icons[type] || '📋'
}

const getIconBg = (type: string) => {
  const bgs = {
    tender: 'bg-blue-50 text-blue-600',
    activity: 'bg-purple-50 text-purple-600',
    supplier: 'bg-green-50 text-green-600'
  }
  return bgs[type] || 'bg-slate-50 text-slate-600'
}

const getTypeClass = (type: string) => {
  const classes = {
    tender: 'bg-blue-50 text-blue-700 border border-blue-200',
    activity: 'bg-purple-50 text-purple-700 border border-purple-200',
    supplier: 'bg-green-50 text-green-700 border border-green-200'
  }
  return classes[type] || 'bg-slate-50 text-slate-700'
}

const getTypeText = (type: string) => {
  const texts = {
    tender: '标书',
    activity: '活动',
    supplier: '服务商'
  }
  return texts[type] || '其他'
}

const isExpiringSoon = (deadline: string) => {
  if (!deadline) return false
  const days = Math.floor((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  return days <= 5 && days >= 0
}

const handleSearch = () => {
  currentPage.value = 1
  ElMessage.success('搜索完成')
}

const handleViewDetail = (item: any) => {
  if (item.type === 'tender') {
    navigateTo('/procurement/' + item.id)
  } else {
    ElMessage.info('查看详情: ' + item.title)
  }
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm(
    '确定要取消收藏吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('已取消收藏: ' + item.title)
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedItems.value.length} 条收藏吗？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success(`已删除 ${selectedItems.value.length} 条收藏`)
    selectedItems.value = []
  }).catch(() => {})
}
</script>