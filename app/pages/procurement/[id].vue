<!-- 文件路径: D:\ipp-platform\app\pages\procurement\[id].vue -->
<template>
  <div class="bg-[#f8fafc] min-h-screen font-sans pb-20">

    <!-- Loading state -->
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <el-icon class="is-loading text-4xl text-brand-600"><Loading /></el-icon>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="flex flex-col items-center justify-center min-h-screen gap-4">
      <div class="text-6xl">📄</div>
      <h2 class="text-xl font-bold text-slate-700">标书未找到</h2>
      <p class="text-slate-500">该标书可能已下架或不存在</p>
      <el-button type="primary" @click="navigateTo('/procurement')">返回列表</el-button>
    </div>

    <template v-else-if="tender">
      <!-- 1. 顶部 Hero 区域 -->
      <div class="bg-slate-900 pt-24 pb-8 relative overflow-hidden shadow-inner">
        <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
        <div class="absolute top-0 right-0 w-[800px] h-[800px] bg-brand-600/10 rounded-full blur-[120px] translate-x-1/4 -translate-y-1/4"></div>

        <div class="container mx-auto px-4 relative z-10">
          <!-- 面包屑 -->
          <div class="flex items-center gap-2 text-sm text-slate-400 mb-4">
            <NuxtLink to="/" class="hover:text-white transition-colors flex items-center gap-1">
              <el-icon><HomeFilled /></el-icon> 首页
            </NuxtLink>
            <el-icon class="text-xs"><ArrowRight /></el-icon>
            <NuxtLink to="/procurement" class="hover:text-white transition-colors">采购商机</NuxtLink>
            <el-icon class="text-xs"><ArrowRight /></el-icon>
            <span class="text-brand-200 font-medium">详情预览</span>
          </div>

          <!-- 标题区 -->
          <div class="max-w-5xl">
            <div class="flex flex-wrap items-center gap-3 mb-3">
              <span v-if="tender.organization" class="px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-xs font-bold uppercase tracking-wider backdrop-blur-sm">
                {{ tender.organization }}
              </span>
              <span class="flex items-center gap-1.5 px-3 py-1 rounded-full bg-green-500/20 border border-green-400/30 text-green-300 text-xs font-bold uppercase backdrop-blur-sm">
                <span class="relative flex h-2 w-2">
                  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
                </span>
                开放投标
              </span>
            </div>
            <h1 class="text-2xl md:text-3xl font-bold text-white mb-6 leading-tight">
              {{ tender.title }}
            </h1>

            <!-- 关键指标 -->
            <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
                <div class="text-slate-400 text-xs mb-1">采购机构</div>
                <div class="font-bold text-base text-white">{{ tender.organization || '-' }}</div>
              </div>
              <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
                <div class="text-slate-400 text-xs mb-1">目标国家</div>
                <div class="font-bold text-base text-white">{{ tender.country || '-' }}</div>
              </div>
              <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
                <div class="text-slate-400 text-xs mb-1">截止日期</div>
                <div class="font-bold text-base" :class="isExpired ? 'text-slate-400' : 'text-red-400'">
                  {{ tender.deadline ? tender.deadline.split(' ')[0] : '-' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. 主体内容区 -->
      <div class="container mx-auto px-4 -mt-8 relative z-20">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

          <!-- 左侧：详情正文 -->
          <div class="lg:col-span-2 space-y-6">
            <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden relative min-h-[400px]">
              <div class="h-1 bg-gradient-to-r from-brand-600 to-blue-500"></div>

              <div class="p-8 md:p-10 relative">

                <h3 class="text-xl font-bold text-slate-900 mb-8 flex items-center gap-3 pb-4 border-b border-slate-100">
                  <span class="w-10 h-10 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center text-xl">📄</span>
                  项目详细需求
                </h3>

                <div
                  class="prose max-w-none text-slate-600 leading-relaxed"
                  :class="{ 'blur-sm select-none pointer-events-none': isGuest }"
                >
                  <div v-if="tender.description" class="whitespace-pre-wrap text-justify">{{ tender.description }}</div>
                  <div v-else class="text-slate-400 italic">暂无详细描述</div>
                </div>

                <!-- 游客拦截遮罩 -->
                <div v-if="isGuest" class="absolute inset-0 top-40 z-10 flex flex-col items-center justify-center bg-gradient-to-b from-white/40 via-white/90 to-white backdrop-blur-[1px]">
                  <div class="bg-white border border-slate-200 rounded-2xl p-8 shadow-2xl max-w-md text-center transform translate-y-10">
                    <div class="w-16 h-16 mx-auto bg-brand-50 rounded-full flex items-center justify-center mb-4">
                      <el-icon class="text-3xl text-brand-600"><Lock /></el-icon>
                    </div>
                    <h3 class="text-xl font-bold text-slate-900 mb-2">完整内容仅限会员查看</h3>
                    <p class="text-slate-500 mb-6 text-sm">
                      注册成为会员，即可查看完整技术规格书、商务条款及联系方式
                    </p>
                    <div class="flex gap-3 justify-center">
                      <el-button type="primary" size="large" class="px-8" @click="navigateTo('/login')">
                        立即登录
                      </el-button>
                      <el-button size="large" @click="navigateTo('/login')">
                        免费注册
                      </el-button>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          </div>

          <!-- 右侧：侧边栏 -->
          <div class="space-y-6">

            <!-- 倒计时卡片 -->
            <div class="bg-white p-6 rounded-2xl shadow-lg border border-slate-200 text-center relative overflow-hidden group">
              <div class="absolute inset-0 bg-gradient-to-br from-brand-50 to-transparent opacity-50"></div>
              <div class="relative z-10">
                <p class="text-sm text-slate-500 font-medium mb-2">
                  {{ isExpired ? '已截止' : '距离截标仅剩' }}
                </p>
                <div class="text-3xl font-bold text-slate-900 font-mono mb-6 flex justify-center items-center gap-2">
                  <el-icon :class="isExpired ? 'text-slate-400' : 'text-red-500 animate-pulse'"><Timer /></el-icon>
                  <span>{{ countdownText }}</span>
                </div>
                <el-button
                  type="primary"
                  size="large"
                  class="w-full mb-3"
                  :disabled="isGuest || isExpired"
                  @click="handleDownload"
                  :loading="downloading"
                >
                  <el-icon class="mr-2"><Download /></el-icon> 下载完整标书
                </el-button>
                <el-button
                  size="large"
                  class="w-full"
                  :type="isFavorited ? 'warning' : 'info'"
                  plain
                  @click="toggleFavorite"
                >
                  {{ isFavorited ? '已收藏' : '收藏' }}
                </el-button>
                <p v-if="!isGuest" class="text-xs text-slate-400 mt-2">VIP/SVIP 会员可下载</p>
              </div>
            </div>

            <!-- 项目属性 -->
            <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
              <h4 class="font-bold text-slate-900 mb-4 text-sm flex items-center gap-2">
                <el-icon><InfoFilled /></el-icon> 项目属性
              </h4>
              <div class="space-y-3">
                <div class="flex justify-between text-sm border-b border-slate-50 pb-2">
                  <span class="text-slate-500">采购编号</span>
                  <span class="font-mono text-slate-700">{{ tender.tenderNo || '-' }}</span>
                </div>
                <div class="flex justify-between text-sm border-b border-slate-50 pb-2">
                  <span class="text-slate-500">所属行业</span>
                  <span class="text-slate-700">{{ tender.categoryLabel || '-' }}</span>
                </div>
                <div class="flex justify-between text-sm border-b border-slate-50 pb-2">
                  <span class="text-slate-500">标书价格</span>
                  <span class="text-slate-700">{{ Number(tender.price) > 0 ? '¥' + tender.price : '免费' }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-slate-500">发布日期</span>
                  <span class="text-slate-700">{{ tender.publishDate || '-' }}</span>
                </div>
              </div>
            </div>

            <!-- 采购机构 -->
            <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
              <h4 class="font-bold text-slate-900 mb-4 text-sm flex items-center gap-2">
                <el-icon><OfficeBuilding /></el-icon> 采购机构
              </h4>
              <div class="flex items-center gap-3 mb-3">
                <div class="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center font-bold border border-blue-100 text-sm">
                  {{ (tender.organization || 'N/A').substring(0, 2) }}
                </div>
                <div>
                  <div class="font-bold text-slate-800">{{ tender.organization || '-' }}</div>
                </div>
              </div>
            </div>

            <!-- 配套服务推荐 -->
            <div class="bg-gradient-to-br from-brand-50 to-blue-50 p-6 rounded-2xl border border-brand-100">
              <h4 class="font-bold text-slate-900 mb-1 text-sm">需要投标支持？</h4>
              <p class="text-xs text-slate-500 mb-4">专业团队助您提升中标率</p>

              <div class="space-y-2">
                <button class="w-full text-left px-4 py-3 bg-white rounded-lg border border-slate-200 hover:border-brand-300 hover:shadow-md transition-all group">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded bg-brand-50 text-brand-600 flex items-center justify-center text-sm">📝</div>
                      <div>
                        <div class="text-sm font-bold text-slate-900 group-hover:text-brand-600 transition-colors">标书定制服务</div>
                        <div class="text-xs text-slate-500">专业撰写技术标书</div>
                      </div>
                    </div>
                    <el-icon class="text-slate-400 group-hover:text-brand-600 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
                  </div>
                </button>

                <button class="w-full text-left px-4 py-3 bg-white rounded-lg border border-slate-200 hover:border-brand-300 hover:shadow-md transition-all group">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                      <div class="w-8 h-8 rounded bg-brand-50 text-brand-600 flex items-center justify-center text-sm">🌐</div>
                      <div>
                        <div class="text-sm font-bold text-slate-900 group-hover:text-brand-600 transition-colors">标书翻译服务</div>
                        <div class="text-xs text-slate-500">多语种专业翻译</div>
                      </div>
                    </div>
                    <el-icon class="text-slate-400 group-hover:text-brand-600 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
                  </div>
                </button>
              </div>

              <NuxtLink
                to="/services"
                class="mt-4 w-full px-4 py-2 border border-brand-300 text-brand-600 font-bold rounded-lg hover:bg-brand-600 hover:text-white transition-all text-sm text-center block"
              >
                查看全部服务 →
              </NuxtLink>
            </div>

          </div>

        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowRight, Lock, Download, HomeFilled, CircleCheck, Timer,
  InfoFilled, OfficeBuilding, Loading
} from '@element-plus/icons-vue'
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '~/utils/request'

const route = useRoute()
const tenderId = computed(() => route.params.id)

// --- Data fetching (SSR-compatible) ---
const tender = ref<any>(null)
const loading = ref(true)
const error = ref(false)
const downloading = ref(false)

const { data: fetchResult } = await useAsyncData(
  `tender-${tenderId.value}`,
  () => apiRequest(`/v3/tenders/${tenderId.value}`),
  { server: true }
)

if (fetchResult.value?.data) {
  tender.value = fetchResult.value.data
  loading.value = false
} else {
  error.value = true
  loading.value = false
}

// --- Auth state ---
const tokenCookie = useCookie('xripp_token')
const isGuest = computed(() => !tokenCookie.value)

// --- Countdown ---
const isExpired = computed(() => {
  if (!tender.value?.deadline) return true
  return new Date(tender.value.deadline) < new Date()
})

const countdownText = computed(() => {
  if (!tender.value?.deadline) return '--'
  const diff = new Date(tender.value.deadline).getTime() - Date.now()
  if (diff <= 0) return '已截止'
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  return `${days}天 ${String(hours).padStart(2, '0')}小时`
})

// --- Download ---
const handleDownload = async () => {
  downloading.value = true
  try {
    const res: any = await apiRequest(`/v3/tenders/${tenderId.value}/download`, { method: 'POST' })
    if (res?.data?.isDuplicate) {
      ElMessage.info('该标书您已下载过，不重复扣费')
    } else {
      ElMessage.success('下载成功')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '下载失败')
  } finally {
    downloading.value = false
  }
}

// --- Favorites (localStorage until user_favorites API is built) ---
const FAVORITE_KEY = 'procurementFavorites'
const favoriteIds = ref<string[]>([])

onMounted(() => {
  try {
    const raw = localStorage.getItem(FAVORITE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) favoriteIds.value = parsed.map(String)
    }
  } catch {}
})

const isFavorited = computed(() => favoriteIds.value.includes(String(tenderId.value)))

const toggleFavorite = () => {
  const key = String(tenderId.value)
  if (isFavorited.value) {
    favoriteIds.value = favoriteIds.value.filter(v => v !== key)
    ElMessage.success('已取消收藏')
  } else {
    favoriteIds.value = [...favoriteIds.value, key]
    ElMessage.success('收藏成功')
  }
  try {
    localStorage.setItem(FAVORITE_KEY, JSON.stringify(favoriteIds.value))
  } catch {}
}

useHead({ title: computed(() => tender.value ? `${tender.value.title} - XRIPP` : '标书详情 - XRIPP') })
</script>
