<!-- 文件路径: D:\ipp-platform\app\pages\procurement\index.vue -->
<template>
  <div class="bg-[#f8fafc] min-h-screen font-sans pb-20">
    
    <!-- 1. 顶部搜索 Hero (保持紧凑) -->
    <div class="pt-24 pb-16 min-h-[420px] relative overflow-hidden">
      <!-- 轮播背景 -->
      <div class="absolute inset-0">
        <img
          v-for="(img, idx) in heroImages"
          :key="img"
          :src="img"
          class="absolute inset-0 w-full h-full object-cover transition-opacity duration-700"
          :class="heroCurrent === idx ? 'opacity-100' : 'opacity-0'"
          alt="procurement hero"
        />
      </div>

      <!-- 遮罩和纹理 -->
      <div class="absolute inset-0 bg-slate-900/40"></div>
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-10"></div>
      <div class="absolute top-0 right-0 w-[800px] h-[800px] bg-blue-600/20 rounded-full blur-[120px] translate-x-1/3 -translate-y-1/2"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="flex items-center gap-2 text-sm text-slate-300 mb-4"></div>

        <h1 class="text-3xl font-bold text-white mb-6 tracking-tight flex items-center justify-center gap-3">
          <span class="p-2 bg-blue-500/20 rounded-lg border border-blue-500/30">📊</span>
          全球采购商机库
        </h1>

        <div class="bg-white/95 backdrop-blur p-2 rounded-2xl shadow-2xl flex flex-col md:flex-row gap-2 max-w-4xl mx-auto">
          <div class="flex-grow">
            <input
              v-model="keyword"
              type="text"
              placeholder="请输入标书关键词、采购编号或机构名称..."
              class="w-full h-12 px-4 rounded-xl border-none outline-none text-slate-700 placeholder:text-slate-400 focus:bg-slate-50 transition-colors"
              @keyup.enter="handleSearch"
            />
          </div>
          <button
            class="px-8 py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-lg hover:shadow-brand-500/30 transition-all flex items-center justify-center gap-2"
            @click="handleSearch"
          >
            <el-icon><Search /></el-icon> 搜索
          </button>
        </div>

        <!-- 轮播指示点 -->
        <div class="mt-5 flex justify-center gap-2">
          <button
            v-for="(_, i) in heroImages"
            :key="i"
            class="w-2.5 h-2.5 rounded-full transition-all"
            :class="heroCurrent === i ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80'"
            @click="heroCurrent = i"
          />
        </div>
      </div>
    </div>

    <!-- 2. 核心内容区 -->
    <div class="container mx-auto px-4 -mt-8 relative z-20">
      
      <!-- A. 公共采购能力服务矩阵 (✅ 核心修复：补全四大服务入口) -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6 animate-fade-in-up">
        <!-- 1. 成为UN供应商 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md cursor-pointer group transition-all" @click="navigateTo('/member/un-apply')">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center"><el-icon><Trophy /></el-icon></div>
            <h3 class="font-bold text-slate-800 group-hover:text-blue-600">成为UN供应商</h3>
          </div>
          <p class="text-xs text-slate-500">一站式协助注册 UNGM/UNPU</p>
        </div>

        <!-- 2. 标前服务 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md cursor-pointer group transition-all" @click="navigateTo('/member/tender-writing')">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center"><el-icon><Document /></el-icon></div>
            <h3 class="font-bold text-slate-800 group-hover:text-indigo-600">标前服务</h3>
          </div>
          <p class="text-xs text-slate-500">标书定制、翻译、资质预审</p>
        </div>

        <!-- 3. 中标交付 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md cursor-pointer group transition-all" @click="navigateTo('/overseas?mode=delivery')">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-full bg-green-100 text-green-600 flex items-center justify-center"><el-icon><Van /></el-icon></div>
            <h3 class="font-bold text-slate-800 group-hover:text-green-600">中标交付</h3>
          </div>
          <p class="text-xs text-slate-500">物流清关、货运保险、回款</p>
        </div>

        <!-- 4. 能力建设 -->
        <div class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md cursor-pointer group transition-all" @click="navigateTo('/services#training')">
          <div class="flex items-center gap-3 mb-2">
            <div class="w-10 h-10 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center"><el-icon><School /></el-icon></div>
            <h3 class="font-bold text-slate-800 group-hover:text-orange-600">能力建设</h3>
          </div>
          <p class="text-xs text-slate-500">国际采购培训、公益讲座</p>
        </div>
      </div>

      <!-- B. 筛选工具栏 -->
      <div class="bg-white rounded-xl shadow-lg border border-slate-100 p-4 mb-6">
        <div class="flex flex-col lg:flex-row justify-between items-center gap-4">
          
          <div class="flex bg-slate-100 p-1 rounded-lg">
            <button 
              v-for="tab in tabs" 
              :key="tab.id"
              class="px-6 py-2 rounded-md text-sm font-bold transition-all whitespace-nowrap"
              :class="activeTab === tab.id ? 'bg-white text-brand-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
              @click="activeTab = tab.id"
            >
              {{ tab.name }}
            </button>
          </div>

          <div class="flex items-center gap-3 w-full lg:w-auto overflow-x-auto no-scrollbar">
            <el-select v-model="filters.status" placeholder="项目状态" style="width: 120px" size="large" clearable>
              <el-option label="全部状态" value="" />
              <el-option label="进行中" value="open" />
              <el-option label="即将截止" value="urgent" />
            </el-select>

            <el-select v-model="filters.country" placeholder="受惠国家" style="width: 140px" size="large" clearable>
              <el-option label="全部国家" value="" />
              <el-option label="肯尼亚" value="Kenya" />
              <el-option label="马来西亚" value="Malaysia" />
              <el-option label="越南" value="Vietnam" />
              <el-option label="苏丹" value="Sudan" />
            </el-select>

            <el-date-picker
              v-model="filters.date"
              type="daterange"
              range-separator="-"
              start-placeholder="发布日期"
              end-placeholder="截止日期"
              size="large"
              style="width: 260px"
            />
            
            <el-button circle size="large" @click="resetFilters" title="重置筛选">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- C. 标书列表 -->
      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden min-h-[600px]">
        <div class="hidden lg:grid grid-cols-12 gap-4 px-6 py-3 bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-500 uppercase tracking-wider">
          <div class="col-span-5">项目信息</div>
          <div class="col-span-2">采购机构</div>
          <div class="col-span-2">目标国家</div>
          <div class="col-span-2">截止日期</div>
          <div class="col-span-1 text-right">操作</div>
        </div>

        <div class="divide-y divide-slate-100">
          <div v-if="filteredTenders.length === 0" class="p-12 text-center text-slate-400">
            <div class="text-5xl mb-3">📂</div>
            <div class="font-bold text-slate-600 mb-2">暂无匹配的商机</div>
            <p class="text-sm">请尝试调整关键词或筛选条件</p>
          </div>

          <div 
            v-for="item in paginatedTenders" 
            :key="item.id" 
            class="group hover:bg-blue-50/50 transition-all duration-200 cursor-pointer border-l-4 border-transparent hover:border-brand-500"
            @click="navigateTo(`/procurement/${item.id}`)"
          >
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 px-6 py-5 items-center">
              
              <div class="col-span-12 lg:col-span-5 flex items-start gap-4">
                <div 
                  class="flex-shrink-0 w-12 h-12 rounded-lg flex items-center justify-center text-xs font-bold border-2"
                  :class="getTypeStyle(item.type)"
                >
                  {{ getTypeAbbr(item.type) }}
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 mb-1 flex-wrap">
                    <span class="text-xs font-mono text-slate-400">#{{ item.id }}</span>
                    <span class="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-600 border border-slate-200">
                      {{ item.type }}
                    </span>
                  </div>
                  <h3 class="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors mb-1 line-clamp-1">
                    {{ item.titleZh }}
                  </h3>
                  <p class="text-xs text-slate-500 line-clamp-1">{{ item.title }}</p>
                </div>
              </div>

              <div class="col-span-6 lg:col-span-2 flex items-center text-sm">
                <el-icon class="mr-2 text-slate-400 lg:hidden"><OfficeBuilding /></el-icon>
                <span class="text-slate-700 truncate font-medium" :title="item.organization">
                  {{ item.organization }}
                </span>
              </div>

              <div class="col-span-6 lg:col-span-2 flex items-center text-sm">
                <el-icon class="mr-2 text-slate-400 lg:hidden"><Location /></el-icon>
                <span class="text-slate-700 font-medium">{{ item.country }}</span>
              </div>

              <div class="col-span-6 lg:col-span-2">
                <div class="flex items-center gap-2">
                  <div 
                    class="w-2 h-2 rounded-full" 
                    :class="getDeadlineIndicator(item.deadline)"
                  ></div>
                  <span 
                    class="text-sm font-medium"
                    :class="isUrgent(item.deadline) ? 'text-red-600' : 'text-slate-600'"
                  >
                    {{ formatDate(item.deadline) }}
                  </span>
                </div>
                <div class="text-xs text-slate-400 mt-0.5 ml-4">
                  剩余 {{ getDaysLeft(item.deadline) }} 天
                </div>
              </div>

              <div class="col-span-6 lg:col-span-1 flex justify-end items-center gap-2">
                <button
                  class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-bold text-slate-600 hover:bg-yellow-50 hover:text-yellow-700 hover:border-yellow-300 transition-all"
                  :class="isFavorited(item.id) ? 'text-yellow-700 border-yellow-300 bg-yellow-50' : ''"
                  @click.stop="toggleFavorite(item.id)"
                >
                  {{ isFavorited(item.id) ? '已收藏' : '收藏' }}
                </button>
                <button 
                  class="px-4 py-2 rounded-lg border border-slate-200 text-slate-600 text-xs font-bold hover:bg-brand-600 hover:text-white hover:border-brand-600 transition-all flex items-center gap-1 group-hover:shadow-md"
                  @click.stop="navigateTo(`/procurement/${item.id}`)"
                >
                  详情 <el-icon><ArrowRight /></el-icon>
                </button>
              </div>

            </div>
          </div>
        </div>

        <div class="p-6 border-t border-slate-100 flex justify-center bg-slate-50/30" v-if="filteredTenders.length > 0">
          <el-pagination 
            v-model:current-page="currentPage"
            background 
            layout="prev, pager, next, jumper" 
            :total="filteredTenders.length"
            :page-size="pageSize"
          />
        </div>
      </div>

      <!-- 合作伙伴广告位 -->
      <div class="mt-8">
          <div
          class="relative w-full h-40 rounded-2xl border border-slate-200 bg-gradient-to-r from-brand-700 to-brand-600 px-6 md:px-10 text-white shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all flex items-center"
            @click="navigateTo('/supplier-directory')"
          >
            <div class="absolute right-3 top-3 px-2 py-0.5 text-[10px] rounded border border-white/20 bg-white/10">合作伙伴</div>
            <div class="max-w-2xl">
              <div class="text-xs text-blue-200 font-semibold mb-2">精选合作伙伴</div>
              <div class="text-lg md:text-2xl font-bold mb-1">服务商风采 · 合作伙伴广告位</div>
              <p class="text-xs text-slate-200">展示优质服务商与服务伙伴，精准触达采购需求方。</p>
            </div>
            <div class="ml-auto hidden md:block">
              <div class="bg-white text-slate-900 px-5 py-2 rounded-lg text-xs font-bold">了解合作</div>
            </div>
          </div>
      </div>

      <!-- D. 相关服务推荐 (作为底部补充) -->
      <div class="mt-12 bg-gradient-to-br from-brand-50 to-blue-50 rounded-2xl p-8 border border-brand-100">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold text-slate-900 mb-1">需要更多投标支持？</h3>
            <p class="text-sm text-slate-500">专业团队助您提升中标率</p>
          </div>
          <NuxtLink 
            to="/services" 
            class="px-6 py-2 bg-white border border-slate-200 text-slate-700 font-bold rounded-lg hover:bg-brand-600 hover:text-white hover:border-brand-600 transition-all text-sm"
          >
            查看全部服务 →
          </NuxtLink>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div 
            v-for="service in relatedServices" 
            :key="service.title"
            class="bg-white p-5 rounded-xl border border-slate-100 hover:shadow-lg transition-all cursor-pointer group"
            @click="navigateTo(service.path)"
          >
            <div class="w-10 h-10 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center text-xl mb-3 group-hover:scale-110 transition-transform">
              {{ service.icon }}
            </div>
            <h4 class="font-bold text-slate-900 mb-1 group-hover:text-brand-600 transition-colors">
              {{ service.title }}
            </h4>
            <p class="text-xs text-slate-500">{{ service.desc }}</p>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Search, Refresh, OfficeBuilding, ArrowRight, Location, HomeFilled,
  Trophy, Document, Van, School // ✅ 引入新图标
  , Star
} from '@element-plus/icons-vue'
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'

useHead({ title: '采购商机 - XRIPP全球公共采购服务平台' })

const { tenders } = useMockData()
const keyword = ref('')
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = 10

const heroImages = [
  '/images/procurement/procurement-1.jpg',
  '/images/procurement/procurement-2.jpg',
  '/images/procurement/procurement-3.jpg',
  '/images/procurement/procurement-4.jpg'
]

const heroCurrent = ref(0)
let heroTimer: ReturnType<typeof setInterval> | null = null

const startHeroAutoPlay = () => {
  heroTimer = setInterval(() => {
    heroCurrent.value = (heroCurrent.value + 1) % heroImages.length
  }, 5500)
}

const filters = ref({
  status: '',
  country: '',
  date: []
})

const favoriteIds = ref<string[]>([])
const FAVORITE_KEY = 'procurementFavorites'

const loadFavorites = () => {
  if (!process.client) return
  const raw = localStorage.getItem(FAVORITE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) favoriteIds.value = parsed.map(String)
  } catch {}
}

const persistFavorites = () => {
  if (!process.client) return
  localStorage.setItem(FAVORITE_KEY, JSON.stringify(favoriteIds.value))
}

onMounted(() => {
  loadFavorites()
  startHeroAutoPlay()
})

watch(favoriteIds, persistFavorites, { deep: true })

onBeforeUnmount(() => {
  if (heroTimer) clearInterval(heroTimer)
})

const isFavorited = (id: string) => favoriteIds.value.includes(String(id))

const toggleFavorite = (id: string) => {
  const key = String(id)
  if (isFavorited(key)) {
    favoriteIds.value = favoriteIds.value.filter(v => v !== key)
    ElMessage.success('已取消收藏')
    return
  }
  favoriteIds.value = [...favoriteIds.value, key]
  ElMessage.success('收藏成功')
}

const tabs = [
  { id: 'all', name: '全部商机' },
  { id: 'un', name: '联合国采购' },
  { id: 'gov', name: '国际政府采购' },
  { id: 'org', name: '国际组织采购' },
  { id: 'other', name: '其他采购' }
]

// 相关服务推荐
const relatedServices = [
  {
    icon: '📝',
    title: '标书定制服务',
    desc: '专业团队撰写技术标书和商务标书',
    path: '/member/tender-writing'
  },
  {
    icon: '🌐',
    title: '标书翻译服务',
    desc: '英/法/西语等多语种翻译',
    path: '/services'
  },
  {
    icon: '📋',
    title: '配套服务商',
    desc: '提交文件、资质认证等一站式服务',
    path: '/supplier-directory'
  }
]

// 筛选逻辑
const filteredTenders = computed(() => {
  return tenders.filter(t => {
    // Tab 筛选
    let matchTab = true
    if (activeTab.value === 'un') matchTab = t.type === '联合国采购'
    if (activeTab.value === 'gov') matchTab = t.type === '国际政府采购'
    if (activeTab.value === 'org') matchTab = t.type === '国际组织采购'
    if (activeTab.value === 'other') matchTab = t.type === '其他采购'

    // 关键词
    const k = keyword.value.toLowerCase()
    const matchKeyword = !k || 
      t.titleZh.toLowerCase().includes(k) || 
      t.title.toLowerCase().includes(k) || 
      t.country.toLowerCase().includes(k) ||
      t.id.toLowerCase().includes(k)

    // 国家
    const matchCountry = !filters.value.country || t.country === filters.value.country

    return matchTab && matchKeyword && matchCountry
  })
})

// 分页数据
const paginatedTenders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredTenders.value.slice(start, end)
})

const handleSearch = () => { currentPage.value = 1 }

const resetFilters = () => {
  keyword.value = ''
  activeTab.value = 'all'
  filters.value = { status: '', country: '', date: [] }
  currentPage.value = 1
}

const getTypeStyle = (type: string) => {
  const styles = {
    '联合国采购': 'bg-blue-50 text-blue-600 border-blue-200',
    '国际政府采购': 'bg-indigo-50 text-indigo-600 border-indigo-200',
    '国际组织采购': 'bg-purple-50 text-purple-600 border-purple-200',
    '其他采购': 'bg-orange-50 text-orange-600 border-orange-200'
  }
  return styles[type as keyof typeof styles] || styles['其他采购']
}

const getTypeAbbr = (type: string) => {
  const abbr = { 
    '联合国采购': 'UN', 
    '国际政府采购': 'GOV', 
    '国际组织采购': 'ORG',
    '其他采购': 'NGO' 
  }
  return abbr[type as keyof typeof abbr] || 'N/A'
}

const getDaysLeft = (deadline: string) => {
  const days = Math.floor((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  return Math.max(days, 0)
}

const isUrgent = (deadline: string) => getDaysLeft(deadline) <= 7

const getDeadlineIndicator = (deadline: string) => {
  if (isUrgent(deadline)) return 'bg-red-500 animate-pulse'
  return 'bg-green-500'
}

const formatDate = (dateStr: string) => new Date(dateStr).toLocaleDateString('zh-CN')
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
.animate-fade-in-up { animation: fadeInUp 0.5s ease-out; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>