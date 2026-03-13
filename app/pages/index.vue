<!-- 文件路径: D:\ipp-platform\app\pages\index.vue -->
<template>
  <div class="flex flex-col">
    
    <!-- 1. 英雄区 (Hero Section) -->
    <!-- ✅ 优化：XRIPP 品牌更新，保留大气的轮播背景 -->
    <section class="relative h-[650px] overflow-hidden group">
      <el-carousel 
        height="650px" 
        trigger="click" 
        :interval="6000" 
        arrow="hover"
        class="absolute inset-0 z-0"
        @change="handleCarouselChange"
      >
        <el-carousel-item v-for="(item, index) in heroSlides" :key="index">
          <div class="relative w-full h-full overflow-hidden">
            <img 
              :src="item.image" 
              class="w-full h-full object-cover origin-center transition-opacity duration-500" 
              :class="activeIndex === index ? item.animation : ''"
              alt="Banner"
            />
            <div class="absolute inset-0 bg-slate-900/50 mix-blend-multiply"></div>
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-slate-900/40"></div>
          </div>
        </el-carousel-item>
      </el-carousel>

      <div class="absolute inset-0 z-10 flex items-center justify-center pointer-events-none">
        <div class="container mx-auto px-4 text-center pointer-events-auto">
          <div class="max-w-4xl mx-auto">
            <div class="inline-block px-4 py-1.5 bg-white/10 border border-white/20 rounded-full text-brand-200 text-sm font-medium mb-8 backdrop-blur-md animate-fade-in-up">
             🚀 助力中国企业走向世界 
            </div>
            
            <h1 class="text-5xl md:text-7xl font-bold leading-tight mb-8 tracking-tight text-white drop-shadow-2xl animate-fade-in-up delay-100">
              全球公共采购市场的<br />
              <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-200 to-white">中国门户</span>
            </h1>
            
            <p class="text-xl text-slate-200 mb-10 max-w-2xl mx-auto leading-relaxed font-light drop-shadow-md animate-fade-in-up delay-200">
              XRIPP Global 汇集联合国、国际组织及各国政府的采购信息，提供从信息获取、资质认证到投标支持的一站式全流程服务。
            </p>
            
            <div class="bg-white/10 backdrop-blur-xl p-3 rounded-2xl max-w-xl mx-auto flex gap-3 border border-white/30 mb-10 shadow-[0_20px_50px_rgba(0,0,0,0.5)] animate-fade-in-up delay-300">
              <el-input 
                v-model="searchQuery" 
                placeholder="🔍 搜索标书关键字、国家或机构..." 
                class="flex-grow custom-input"
                size="large"
                clearable
              />
              <el-button type="primary" size="large" class="px-8 font-bold text-lg !rounded-xl" @click="navigateTo('/procurement')">
                搜索
              </el-button>
            </div>

            <div class="flex flex-wrap gap-4 justify-center animate-fade-in-up delay-400">
              <el-button
                type="primary"
                size="large"
                round
                class="px-8 py-6 text-base shadow-lg hover:scale-105 transition-transform"
                @click="navigateTo('/procurement')"
              >
                浏览采购商机
              </el-button>

              <el-button
                size="large"
                round
                plain
                class="bg-white/5 text-white hover:bg-white/20 hover:text-white border-white/30 px-8 py-6 text-base backdrop-blur-sm"
                @click="navigateTo('/services')"
              >
                了解会员服务
              </el-button>

              <el-button
                size="large"
                round
                class="px-8 py-6 text-base font-semibold !text-white !border-0 shadow-lg hover:shadow-xl transition-all hover:-translate-y-0.5 bg-gradient-to-r from-amber-700 to-orange-800 hover:from-amber-800 hover:to-orange-900"
                @click="navigateTo('/member/tender-writing')"
              >
                中文标书购买
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. 统计数据 (Statistics) -->
    <section class="relative -mt-20 z-20 container mx-auto px-4 mb-20">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8 bg-white rounded-2xl shadow-xl p-8 border border-slate-100">
        <div v-for="(stat, index) in stats" :key="index" class="flex items-center gap-5 p-2 transition-transform hover:-translate-y-1 duration-300">
          <div class="w-14 h-14 rounded-full bg-brand-50 text-brand-600 flex items-center justify-center text-3xl">
            {{ stat.icon }}
          </div>
          <div>
            <p class="text-3xl font-bold text-slate-900 font-mono">{{ stat.value }}</p>
            <p class="text-sm text-slate-500 font-medium">{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. 最新采购商机 (Tender List) -->
    <!-- ✅ 核心优化：5行布局(10条)，移除金额，增加收藏，绑定真实Mock数据 -->
    <section class="py-20 bg-white">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-end mb-10">
          <div>
            <div class="text-brand-600 font-bold mb-2 tracking-wider text-sm uppercase">Latest Opportunities</div>
            <h2 class="text-3xl font-bold text-slate-900">最新采购商机</h2>
          </div>
          <button @click="navigateTo('/procurement')" class="hidden md:flex items-center gap-2 text-brand-600 hover:text-brand-700 font-bold text-base transition-colors group">
            查看全部 {{ totalTenders ? totalTenders.toLocaleString() : '0' }} 条标书
            <el-icon class="group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
          </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- ✅ 使用 displayTenders (真实数据切片) 而不是 i in 10 -->
          <div 
            v-for="(item, index) in displayTenders" 
            :key="item.id" 
            class="bg-white p-6 rounded-xl border border-slate-100 shadow-sm hover:shadow-xl hover:border-brand-200 transition-all cursor-pointer group" 
            @click="navigateTo(`/procurement/${item.id}`)"
          >
            <div class="flex justify-between items-start mb-3">
               <!-- 动态标签样式 -->
               <span 
                 class="px-2.5 py-1 text-xs font-bold rounded-full border"
                 :class="getTypeClass(item.type)"
               >
                 {{ item.type }}
               </span>
               <span class="text-slate-400 text-xs flex items-center gap-1">⏱️ 刚刚发布</span>
            </div>
            
            <h3 class="text-lg font-bold text-slate-900 mb-2 group-hover:text-brand-600 transition-colors line-clamp-1" :title="item.titleZh">
              {{ item.titleZh }}
            </h3>
            <p class="text-xs text-slate-500 mb-4 line-clamp-1">{{ item.organization }} · {{ item.country }}</p>
            
            <!-- ✅ 底部栏：截止日期 + 收藏按钮 (无金额) -->
            <div class="flex items-center justify-between text-xs pt-3 border-t border-slate-50">
              <span class="text-red-500 font-medium">截止: {{ item.deadline }}</span>
              
              <div 
                class="flex items-center gap-1 transition-colors z-10 p-1 rounded hover:bg-slate-100"
                :class="collectedItems.includes(item.id) ? 'text-yellow-500' : 'text-slate-400 hover:text-brand-600'"
                @click.stop="handleCollect(item.id)"
              >
                <el-icon class="text-base">
                  <component :is="collectedItems.includes(item.id) ? StarFilled : Star" />
                </el-icon>
                <span>{{ collectedItems.includes(item.id) ? '已收藏' : '收藏' }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="mt-8 text-center">
          <el-button size="large" plain round @click="navigateTo('/procurement')">加载更多商机</el-button>
        </div>
      </div>
    </section>

    <!-- 4. 广告位 (Featured Partner) -->
    <section class="py-10 bg-slate-50 border-t border-slate-200">
      <div class="container mx-auto px-4">
        <div class="relative w-full h-40 rounded-2xl overflow-hidden group cursor-pointer shadow-lg hover:shadow-2xl transition-all" @click="navigateTo('/services')">
          <img src="https://images.unsplash.com/photo-1557804506-669a67965ba0?q=80&w=2074&auto=format&fit=crop" class="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-700" alt="Ad">
          <div class="absolute inset-0 bg-gradient-to-r from-blue-900/90 to-transparent"></div>
          <div class="absolute top-2 right-2 px-2 py-0.5 bg-black/30 backdrop-blur text-[10px] text-white/70 rounded border border-white/10">广告</div>
          <div class="absolute inset-0 flex items-center px-12">
            <div class="max-w-xl">
              <div class="text-yellow-400 font-bold tracking-widest text-sm mb-2 uppercase">Featured Partner</div>
              <h3 class="text-3xl font-bold text-white mb-2">华为云 - 企业出海数字化解决方案</h3>
              <p class="text-blue-100 text-sm">专为外贸企业打造，一站式解决跨境网络与数据合规难题。XRIPP会员立享 8.5 折优惠。</p>
            </div>
            <div class="ml-auto hidden md:block">
              <button class="bg-white text-blue-900 px-6 py-3 rounded-lg font-bold hover:bg-blue-50 transition-colors shadow-lg">立即咨询</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. 最新活动 (Latest Events) -->
    <!-- ✅ 核心优化：改为4个规整卡片 (Grid)，解决走马灯比例问题 -->
    <section class="py-20 bg-slate-50 border-t border-slate-200">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-end mb-10">
          <div>
            <div class="text-brand-600 font-bold mb-2 tracking-wider text-sm uppercase">Latest Events</div>
            <h2 class="text-3xl font-bold text-slate-900">最新活动</h2>
            <p class="text-slate-500 mt-2 text-sm">
              想了解更多活动分类？
              <NuxtLink to="/services" class="text-brand-600 hover:text-brand-700 font-medium underline">
                前往活动中心 →
              </NuxtLink>
            </p>
          </div>
          <button @click="navigateTo('/services')" class="hidden md:flex items-center gap-2 text-brand-600 hover:text-brand-700 font-bold text-base transition-colors group">
            查看更多活动 <el-icon class="group-hover:translate-x-1 transition-transform"><ArrowRight /></el-icon>
          </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div 
            v-for="act in activities.slice(0, 4)" 
            :key="act.id" 
            class="bg-white rounded-xl border border-slate-200 overflow-hidden hover:shadow-xl hover:-translate-y-1 transition-all duration-300 cursor-pointer group" 
            @click="navigateTo('/services')"
          >
            <!-- 图片区域：固定高度，确保规整 -->
            <div class="h-40 overflow-hidden relative">
              <img :src="act.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              <div class="absolute top-3 left-3 px-2 py-1 bg-white/90 backdrop-blur rounded text-xs font-bold text-slate-800 shadow-sm">{{ act.type }}</div>
            </div>
            
            <div class="p-5">
              <div class="text-xs text-slate-500 mb-2 flex items-center gap-1">
                <el-icon><Calendar /></el-icon> {{ act.date }}
              </div>
              <h3 class="font-bold text-slate-900 mb-2 line-clamp-2 group-hover:text-brand-600 transition-colors h-10 leading-tight">
                {{ act.title }}
              </h3>
              <div class="flex justify-between items-center mt-3 pt-3 border-t border-slate-50">
                <span class="text-xs text-slate-400 flex items-center gap-1"><el-icon><Location /></el-icon> {{ act.location }}</span>
                <span class="text-brand-600 text-sm font-bold" v-if="act.isPaid">¥{{ act.price }}</span>
                <span class="text-green-600 text-sm font-bold" v-else>免费</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. 平台服务商风采 (Service Provider Showcase) -->
    <!-- ✅ 核心优化:更名,悬浮Logo设计,增强B端商业属性,去掉中标信息 -->
    <section class="py-24 bg-white">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-end mb-12">
          <div>
            <div class="text-brand-600 font-bold mb-2 tracking-wider text-sm uppercase flex items-center gap-2">
              <span class="w-8 h-0.5 bg-brand-600"></span> Service Provider Showcase
            </div>
            <h2 class="text-3xl md:text-4xl font-bold text-slate-900">平台服务商风采</h2>
            <p class="text-slate-500 mt-3 text-lg font-light">{{ supplierShowcaseSubtitle }}</p>
          </div>
          
          <div class="flex gap-3">
             <button class="w-12 h-12 rounded-full bg-white border border-slate-200 flex items-center justify-center hover:bg-brand-600 hover:text-white transition-all shadow-sm" @click="scrollMembers('left')"><el-icon><ArrowLeft /></el-icon></button>
             <button class="w-12 h-12 rounded-full bg-white border border-slate-200 flex items-center justify-center hover:bg-brand-600 hover:text-white transition-all shadow-sm" @click="scrollMembers('right')"><el-icon><ArrowRight /></el-icon></button>
          </div>
        </div>

        <!-- 滚动容器 -->
        <div ref="memberScrollRef" class="flex gap-6 overflow-x-auto pb-12 snap-x snap-mandatory scroll-smooth no-scrollbar px-1">
          <div v-for="company in memberShowcase" :key="company.id" class="min-w-[300px] flex-shrink-0 snap-start">
            <div class="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-brand-200 hover:-translate-y-2 transition-all duration-500 cursor-pointer h-full relative overflow-hidden flex flex-col" @click="navigateTo('/supplier-directory')">
              <!-- 封面图 -->
              <div class="h-32 bg-slate-200 relative overflow-hidden">
                <img :src="company.image" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
                <div class="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-60"></div>
              </div>
              
              <!-- 悬浮 Logo -->
              <div class="relative -mt-10 px-6 mb-2">
                <div class="w-20 h-20 rounded-2xl bg-white shadow-lg flex items-center justify-center text-3xl font-bold text-brand-700 border-2 border-white group-hover:border-brand-50 transition-colors z-10 relative">
                  {{ company.logo }}
                </div>
              </div>
              
              <div class="p-6 pt-2 flex flex-col flex-grow">
                <div class="mb-4">
                  <h4 class="font-bold text-lg text-slate-900 mb-1 line-clamp-1">{{ company.name }}</h4>
                  <p class="text-sm text-slate-400 font-medium flex items-center gap-2">
                    <span class="w-1.5 h-1.5 rounded-full bg-slate-300 group-hover:bg-brand-400 transition-colors"></span>
                    {{ company.industry }}
                  </p>
                </div>
                
                <!-- ✅ 修改:去掉"中标XX项",只保留行业标签和箭头 -->
                <div class="mt-auto pt-4 border-t border-slate-50 flex justify-between items-center">
                  <div class="text-xs text-slate-500">{{ company.applyTypeLabel }}</div>
                  <el-icon class="text-slate-300 group-hover:text-brand-600 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. 友情链接 (保持不变) -->
    <section class="py-16 bg-slate-50 border-t border-slate-200">
      <div class="container mx-auto px-4">
        <div class="text-center mb-8">
          <div class="inline-flex items-center gap-2 px-4 py-1.5 bg-white border border-slate-200 rounded-full text-xs font-bold text-slate-500 uppercase tracking-widest mb-3">
            <el-icon><Link /></el-icon> Strategic Partners
          </div>
          <h3 class="text-xl font-bold text-slate-800 mb-2">战略合作伙伴 & 友情链接</h3>
          <p class="text-sm text-slate-500">携手全球顶尖机构，共建国际采购生态</p>
        </div>
        
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <a v-for="link in friendlyLinks" :key="link.name" :href="link.url" target="_blank" class="group bg-white border border-slate-200 p-5 rounded-xl shadow-sm hover:shadow-lg hover:border-brand-300 transition-all duration-300 flex flex-col items-center justify-center gap-3 cursor-pointer min-h-[120px]">
            <div class="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center text-2xl group-hover:bg-brand-50 group-hover:scale-110 transition-all">{{ link.icon }}</div>
            <div class="text-center"><div class="text-sm font-bold text-slate-700 group-hover:text-brand-600 transition-colors line-clamp-2 leading-tight">{{ link.name }}</div></div>
            <div class="opacity-0 group-hover:opacity-100 transition-opacity"><el-icon class="text-brand-500 text-xs"><ArrowRight /></el-icon></div>
          </a>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight, ArrowLeft, Trophy, Search, Link, Star, StarFilled, Calendar, Location } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

const searchQuery = ref('')
const activeIndex = ref(0)

// 活动真实数据（/v3/activities 已 permitAll，audit_status=20 的已审核活动）
const activities = ref<any[]>([])

// 标书真实数据（/v3/tenders 已 permitAll）
const tenders = ref<any[]>([])
const totalTenders = ref(0)
const memberShowcase = ref<any[]>([])
const supplierTotal = ref(0)

const fmtDeadline = (v: string) => (v ? v.slice(0, 10) : '-')

const mapTenderRow = (item: any) => ({
  id: String(item.id),
  type: item.categoryLabel || '国际采购',
  titleZh: item.title || '未命名标书',
  organization: item.organization || '-',
  country: item.country || '-',
  deadline: fmtDeadline(item.deadline)
})

const displayTenders = computed(() => {
  return tenders.value.map(mapTenderRow).slice(0, 10)
})

// 收藏功能状态
const collectedItems = ref<string[]>([])

const handleCollect = (id: string) => {
  if (collectedItems.value.includes(id)) {
    collectedItems.value = collectedItems.value.filter(item => item !== id)
    ElMessage.success('已取消收藏')
  } else {
    collectedItems.value.push(id)
    ElMessage.success('收藏成功')
  }
}

// 标签样式辅助函数
const getTypeClass = (type: string) => {
  if (type === '联合国采购') return 'bg-blue-50 text-blue-700 border-blue-100'
  if (type === '国际政府采购') return 'bg-indigo-50 text-indigo-700 border-indigo-100'
  return 'bg-orange-50 text-orange-700 border-orange-100'
}

// 轮播图逻辑
const handleCarouselChange = (index: number) => { activeIndex.value = index }

const defaultSlides = [
  { image: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop', animation: 'animate-zoom-in' },
  { image: 'https://images.unsplash.com/photo-1578575437130-527eed3abbec?q=80&w=2070&auto=format&fit=crop', animation: 'animate-pan' },
  { image: 'https://images.unsplash.com/photo-1556761175-5973dc0f32e7?q=80&w=1932&auto=format&fit=crop', animation: 'animate-zoom-out' },
]
const animations = ['animate-zoom-in', 'animate-pan', 'animate-zoom-out']
const heroSlides = ref(defaultSlides)

// Load carousel banners from API
const loadBanners = async () => {
  try {
    const res: any = await apiRequest('/v3/contents', { query: { content_type: 'carousel', page_size: 5 } })
    const items = res?.data?.items || []
    if (items.length > 0) {
      heroSlides.value = items
        .filter((item: any) => item.coverImage)
        .map((item: any, i: number) => ({
          image: item.coverImage,
          animation: animations[i % animations.length]
        }))
      if (heroSlides.value.length === 0) heroSlides.value = defaultSlides
    }
  } catch {
    // Fallback to defaults
  }
}

// 统计数据 (从API加载)
const { stats: dashboardStats } = usePlatformStats()
const stats = computed(() => [
  { label: '招标信息总量', value: dashboardStats.value.totals.tenderCount ? dashboardStats.value.totals.tenderCount.toLocaleString() : '-', icon: '📄' },
  { label: '注册企业会员', value: dashboardStats.value.totals.memberCount ? dashboardStats.value.totals.memberCount.toLocaleString() : '-', icon: '🏢' },
  { label: '覆盖目标国家', value: String(dashboardStats.value.totals.countryCount), icon: '🌍' },
  { label: '采购机构数', value: String(dashboardStats.value.totals.organizationCount || 0), icon: '🏛️' },
])

const fallbackSupplierImage = 'https://images.unsplash.com/photo-1497366754035-f200968a6e72?q=80&w=2070&auto=format&fit=crop'

const supplierShowcaseSubtitle = computed(() => {
  if (!supplierTotal.value) return '公开服务商名录持续更新中，当前展示已通过审核并开放公开资料的服务商'
  return `${supplierTotal.value.toLocaleString()} 家服务商已开放公开资料，覆盖多个重点行业与服务方向`
})

const mapSupplierCard = (item: any) => ({
  id: String(item.id),
  name: item.companyName || '未命名服务商',
  logo: String(item.companyName || '?').trim().charAt(0) || '?',
  industry: item.mainServiceLabel || item.mainService || '综合服务',
  applyTypeLabel: item.applyTypeLabel || '服务商',
  image: item.coverImageUrl || item.promoImageUrl || fallbackSupplierImage
})

// 会员滚动逻辑
const memberScrollRef = ref<HTMLElement | null>(null)
const scrollMembers = (direction: 'left' | 'right') => {
  if (!memberScrollRef.value) return
  const scrollAmount = 320
  memberScrollRef.value.scrollBy({ left: direction === 'left' ? -scrollAmount : scrollAmount, behavior: 'smooth' })
}

// 首页数据并发加载
onMounted(async () => {
  await Promise.allSettled([
    apiRequest('/v3/tenders?page=1&page_size=20').then((res: any) => {
      tenders.value = Array.isArray(res?.data?.items) ? res.data.items : []
      totalTenders.value = Number(res?.data?.total ?? 0)
    }).catch(() => { tenders.value = [] }),

    apiRequest('/v3/activities?page=1&page_size=4&display_area=home').then((res: any) => {
      const items = Array.isArray(res?.data?.items) ? res.data.items : []
      if (items.length > 0) {
        activities.value = items
        return
      }
      return apiRequest('/v3/activities?page=1&page_size=4').then((fallback: any) => {
        activities.value = Array.isArray(fallback?.data?.items) ? fallback.data.items : []
      })
    }).catch(() => { activities.value = [] }),

    apiRequest('/v3/suppliers?page=1&page_size=10').then((res: any) => {
      const items = Array.isArray(res?.data?.items) ? res.data.items : []
      memberShowcase.value = items.map(mapSupplierCard)
      supplierTotal.value = Number(res?.data?.total ?? items.length)
    }).catch(() => {
      memberShowcase.value = []
      supplierTotal.value = 0
    }),

    loadBanners()
  ])
})

// 友情链接
const friendlyLinks = [
  { name: '联合国采购司', icon: '🇺🇳', url: 'https://www.un.org/procurement' },
  { name: '联合国项目事务署', icon: '🌍', url: 'https://www.unops.org' },
  { name: '世界银行', icon: '🏦', url: 'https://www.worldbank.org' },
  { name: '亚洲开发银行', icon: '💼', url: 'https://www.adb.org' },
  { name: '中国红十字会援外物资供应站', icon: '🏛️', url: '#' },
  { name: '中国贸促会', icon: '🤝', url: 'https://www.ccpit.org' },
]
</script>

<style scoped>
.custom-input:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: none;
  border: none;
}
.custom-input:deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #0ea5e9;
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 动画部分 */
@keyframes zoomIn { from { transform: scale(1); } to { transform: scale(1.15); } }
.animate-zoom-in { animation: zoomIn 10s ease-out forwards; }
@keyframes zoomOut { from { transform: scale(1.15); } to { transform: scale(1); } }
.animate-zoom-out { animation: zoomOut 10s ease-out forwards; }
@keyframes panRight { from { transform: scale(1.1) translateX(0); } to { transform: scale(1.1) translateX(-5%); } }
.animate-pan { animation: panRight 10s ease-in-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translate3d(0, 40px, 0); } to { opacity: 1; transform: translate3d(0, 0, 0); } }
.animate-fade-in-up { animation: fadeInUp 0.8s ease-out forwards; opacity: 0; }
.delay-100 { animation-delay: 0.1s; }
.delay-200 { animation-delay: 0.2s; }
.delay-300 { animation-delay: 0.3s; }
.delay-400 { animation-delay: 0.4s; }

:deep(.el-carousel__item) {
  border-radius: 12px;
}
</style>
