<!-- 文件路径: D:\ipp-platform\app\pages\procurement\[id].vue -->
<template>
  <div class="bg-[#f8fafc] min-h-screen font-sans pb-20">
    
    <!-- 1. 顶部 Hero 区域 (优化版：高度收紧) -->
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
          <a @click="$router.back()" class="cursor-pointer hover:text-white transition-colors">采购商机</a>
          <el-icon class="text-xs"><ArrowRight /></el-icon>
          <span class="text-brand-200 font-medium">详情预览</span>
        </div>

        <!-- 标题区 -->
        <div class="max-w-5xl">
          <div class="flex flex-wrap items-center gap-3 mb-3">
            <span class="px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-xs font-bold uppercase tracking-wider backdrop-blur-sm">
              联合国采购
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
            应急野战医院医疗设备供应项目
          </h1>
          
          <!-- 关键指标（去掉“预估金额”，改为 3 列） -->
          <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
              <div class="text-slate-400 text-xs mb-1">采购机构</div>
              <div class="font-bold text-base text-white">UNOPS</div>
            </div>
            <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
              <div class="text-slate-400 text-xs mb-1">目标国家</div>
              <div class="font-bold text-base text-white flex items-center gap-2">
                <img src="https://flagcdn.com/20x15/sd.png" class="rounded-[2px] opacity-80" alt="Sudan"/> 苏丹
              </div>
            </div>
            <div class="bg-white/5 rounded-xl border border-white/10 p-3 backdrop-blur-md hover:bg-white/10 transition-colors">
              <div class="text-slate-400 text-xs mb-1">截止日期</div>
              <div class="font-bold text-base text-red-400">2026-02-20</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 主体内容区（其余部分不变） -->
    <div class="container mx-auto px-4 -mt-8 relative z-20">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        <!-- 左侧：详情正文 -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden relative min-h-[600px]">
            <div class="h-1 bg-gradient-to-r from-brand-600 to-blue-500"></div>
            
            <div class="p-8 md:p-10 relative">
              
              <h3 class="text-xl font-bold text-slate-900 mb-8 flex items-center gap-3 pb-4 border-b border-slate-100">
                <span class="w-10 h-10 rounded-lg bg-brand-50 text-brand-600 flex items-center justify-center text-xl">📄</span>
                项目详细需求
              </h3>
              
              <div 
                class="prose max-w-none text-slate-600 leading-relaxed space-y-8" 
                :class="{ 'blur-sm select-none pointer-events-none': isGuest }"
              >
                <div>
                  <h4 class="font-bold text-slate-900 mb-3 text-lg">1. 项目背景</h4>
                  <p class="text-justify">
                    联合国项目事务署（UNOPS）计划为苏丹达尔富尔地区部署的三个一级野战医院采购关键医疗设备。该项目旨在提升当地在紧急情况下的医疗响应能力，预计服务人口超过50万人。
                  </p>
                </div>
                <div>
                  <h4 class="font-bold text-slate-900 mb-3 text-lg">2. 采购清单概述</h4>
                  <div class="bg-slate-50 p-6 rounded-xl border border-slate-100">
                    <ul class="space-y-3">
                      <li class="flex items-start gap-2">
                        <el-icon class="mt-1 text-brand-500"><CircleCheck /></el-icon>
                        <span>移动式数字X光机 (3台) - 需含备用电池组及稳压器</span>
                      </li>
                      <li class="flex items-start gap-2">
                        <el-icon class="mt-1 text-brand-500"><CircleCheck /></el-icon>
                        <span>重症监护呼吸机 (15台) - 适用于成人及儿童，带PEEP功能</span>
                      </li>
                      <li class="flex items-start gap-2">
                        <el-icon class="mt-1 text-brand-500"><CircleCheck /></el-icon>
                        <span>模块化手术台及无影灯套件 (6套) - 需符合WHO野战医院标准</span>
                      </li>
                      <li class="flex items-start gap-2">
                        <el-icon class="mt-1 text-brand-500"><CircleCheck /></el-icon>
                        <span>便携式超声诊断仪 (10台) - 具备多普勒功能及心脏探头</span>
                      </li>
                    </ul>
                  </div>
                </div>
                <div>
                  <h4 class="font-bold text-slate-900 mb-3 text-lg">3. 资质要求</h4>
                  <p class="text-justify mb-3">
                    投标方需具备 ISO 13485 医疗器械质量管理体系认证，并提供过去3年内类似项目的成功案例证明。所有设备需符合WHO预认证标准或等效国际认证。
                  </p>
                  <div class="bg-amber-50 border border-amber-200 rounded-lg p-4 text-sm">
                    <strong class="text-amber-800">重要提示：</strong>
                    <span class="text-amber-700">交货期要求为合同签署后60天内，需包含现场安装调试及人员培训服务。</span>
                  </div>
                </div>
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
          
          <!-- 演示控制 -->
          <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg flex justify-between items-center text-sm text-yellow-800">
            <span>📢 演示模式：当前身份为 <strong>{{ isGuest ? '游客' : '会员' }}</strong></span>
            <el-button type="warning" size="small" @click="isGuest = !isGuest">切换身份</el-button>
          </div>
        </div>

        <!-- 右侧：侧边栏（不变） -->
        <div class="space-y-6">
          
          <!-- 倒计时卡片 -->
          <div class="bg-white p-6 rounded-2xl shadow-lg border border-slate-200 text-center relative overflow-hidden group">
            <div class="absolute inset-0 bg-gradient-to-br from-brand-50 to-transparent opacity-50"></div>
            <div class="relative z-10">
              <p class="text-sm text-slate-500 font-medium mb-2">距离截标仅剩</p>
              <div class="text-3xl font-bold text-slate-900 font-mono mb-6 flex justify-center items-center gap-2">
                <el-icon class="text-red-500 animate-pulse"><Timer /></el-icon> 
                <span>12天 08小时</span>
              </div>
              <el-button 
                type="primary" 
                size="large" 
                class="w-full mb-3" 
                :disabled="isGuest"
              >
                <el-icon class="mr-2"><Download /></el-icon> 下载完整标书
              </el-button>
              <el-button
                size="large"
                class="w-full"
                :type="isFavorited(currentId) ? 'warning' : 'info'"
                plain
                @click="toggleFavorite(currentId)"
              >
                {{ isFavorited(currentId) ? '已收藏' : '收藏' }}
              </el-button>
              <p class="text-xs text-slate-400">仅限 VIP/SVIP 会员 (剩余50次)</p>
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
                <span class="font-mono text-slate-700">ITB-2026-9987</span>
              </div>
              <div class="flex justify-between text-sm border-b border-slate-50 pb-2">
                <span class="text-slate-500">采购方式</span>
                <span class="text-slate-700">公开招标 (ITB)</span>
              </div>
              <div class="flex justify-between text-sm border-b border-slate-50 pb-2">
                <span class="text-slate-500">所属行业</span>
                <span class="text-slate-700">医疗器械</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-slate-500">发布日期</span>
                <span class="text-slate-700">2026-01-15</span>
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
                UN
              </div>
              <div>
                <div class="font-bold text-slate-800">UNOPS</div>
                <div class="text-xs text-slate-500">联合国项目事务署</div>
              </div>
            </div>
            <p class="text-xs text-slate-500 leading-relaxed text-justify">
              UNOPS 是联合国的核心运营部门，负责每年超过10亿美元的采购项目，主要集中在基础设施、项目管理和采购服务领域。
            </p>
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
  </div>
</template>

<script setup lang="ts">
import { 
  ArrowRight, Lock, Download, HomeFilled, CircleCheck, Timer,
  InfoFilled, OfficeBuilding, Star
} from '@element-plus/icons-vue'
import { computed, ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const isGuest = ref(true)
const route = useRoute()
const currentId = computed(() => String(route.params.id || ''))

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

onMounted(loadFavorites)
watch(favoriteIds, persistFavorites, { deep: true })

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

useHead({ title: '标书详情 - XRIPP全球公共采购服务平台' })
</script>
