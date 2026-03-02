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
    <div class="flex justify-center" v-if="totalItems > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        background
        layout="prev, pager, next"
        :total="totalItems"
        @current-change="loadFavorites"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Search, Delete, Clock, Timer, Location, Money, OfficeBuilding,
  Warning, Star, Document, Calendar
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '~/utils/request'

definePageMeta({ layout: 'member' })

const keyword = ref('')
const filterType = ref('')
const filterTime = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const selectedItems = ref<number[]>([])

const stats = ref([
  { label: '全部收藏', value: '0', icon: Star, bgClass: 'bg-yellow-50 group-hover:bg-yellow-100', textClass: 'text-yellow-600' },
  { label: '标书收藏', value: '0', icon: Document, bgClass: 'bg-blue-50 group-hover:bg-blue-100', textClass: 'text-blue-600' },
  { label: '活动收藏', value: '0', icon: Calendar, bgClass: 'bg-purple-50 group-hover:bg-purple-100', textClass: 'text-purple-600' },
  { label: '即将截止', value: '0', icon: Warning, bgClass: 'bg-red-50 group-hover:bg-red-100', textClass: 'text-red-600' },
])

const favorites = ref<any[]>([])

const loadFavorites = async () => {
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filterType.value) query.target_type = filterType.value
    const res: any = await apiRequest('/v3/member/favorites', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    favorites.value = items
    totalItems.value = Number(res?.data?.total ?? 0)
    updateStats()
  } catch (e: any) {
    favorites.value = []
    totalItems.value = 0
  }
}

const updateStats = () => {
  const all = favorites.value
  stats.value[0].value = String(totalItems.value)
  stats.value[1].value = String(all.filter((f: any) => f.type === 'tender').length)
  stats.value[2].value = String(all.filter((f: any) => f.type === 'activity').length)
  stats.value[3].value = String(all.filter((f: any) => f.isExpired === false && f.deadline).length)
}

// Client-side keyword filter (applied after API fetch; keyword search is simple text matching)
const filteredFavorites = computed(() => {
  if (!keyword.value) return favorites.value
  const kw = keyword.value.toLowerCase()
  return favorites.value.filter((item: any) =>
    (item.title || '').toLowerCase().includes(kw) ||
    (item.description || '').toLowerCase().includes(kw)
  )
})

const getIcon = (type: string) => {
  const icons: Record<string, string> = { tender: '📄', activity: '📅', supplier: '🏢' }
  return icons[type] || '📋'
}

const getIconBg = (type: string) => {
  const bgs: Record<string, string> = {
    tender: 'bg-blue-50 text-blue-600',
    activity: 'bg-purple-50 text-purple-600',
    supplier: 'bg-green-50 text-green-600'
  }
  return bgs[type] || 'bg-slate-50 text-slate-600'
}

const getTypeClass = (type: string) => {
  const classes: Record<string, string> = {
    tender: 'bg-blue-50 text-blue-700 border border-blue-200',
    activity: 'bg-purple-50 text-purple-700 border border-purple-200',
    supplier: 'bg-green-50 text-green-700 border border-green-200'
  }
  return classes[type] || 'bg-slate-50 text-slate-700'
}

const getTypeText = (type: string) => {
  const texts: Record<string, string> = { tender: '标书', activity: '活动', supplier: '服务商' }
  return texts[type] || '其他'
}

const isExpiringSoon = (deadline: string) => {
  if (!deadline) return false
  const days = Math.floor((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  return days <= 5 && days >= 0
}

const handleSearch = () => {
  currentPage.value = 1
  loadFavorites()
}

const handleViewDetail = (item: any) => {
  const targetId = item.targetId ?? item.id
  if (item.type === 'tender') {
    navigateTo(`/procurement/${targetId}`)
  } else if (item.type === 'activity') {
    ElMessage.info('查看活动: ' + item.title)
  } else {
    ElMessage.info('查看详情: ' + item.title)
  }
}

const handleDelete = async (item: any) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', { type: 'warning' })
    await apiRequest(`/v3/member/favorites/${item.id}`, { method: 'DELETE' })
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 条收藏吗？`, '批量删除', { type: 'warning' })
    for (const id of selectedItems.value) {
      await apiRequest(`/v3/member/favorites/${id}`, { method: 'DELETE' })
    }
    ElMessage.success(`已删除 ${selectedItems.value.length} 条收藏`)
    selectedItems.value = []
    loadFavorites()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

onMounted(() => {
  loadFavorites()
})
</script>