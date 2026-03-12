<!--
  文件路径: D:\ipp-platform\app\pages\admin\index.vue
  版本: V3.0 API对接版 (2026-03-12)

  修复: 移除所有Mock统计数据，对接真实stats API
  - 统计卡片: /v3/admin/orders/stats, /v3/admin/tenders/stats, /v3/admin/members (count)
  - 待处理任务: /v3/admin/members?status=pending (真实待审核)
  - 最近活动: /v3/admin/contents?page_size=5 (最新内容)
-->
<template>
  <div class="space-y-8">

    <!-- 1. 数据驾驶舱启动入口 -->
    <div class="relative bg-gradient-to-r from-[#0f172a] to-[#1e293b] rounded-xl p-8 text-white shadow-xl overflow-hidden group cursor-pointer" @click="navigateTo('/dashboard')">
      <div class="absolute right-0 top-0 h-full w-1/2 bg-brand-500/10 skew-x-12 transform origin-bottom-right transition-transform duration-700 group-hover:scale-110"></div>
      <div class="absolute right-20 top-1/2 -translate-y-1/2 w-32 h-32 bg-blue-500/20 rounded-full blur-3xl group-hover:bg-blue-400/30 transition-colors"></div>

      <div class="relative z-10 flex justify-between items-center">
        <div>
          <div class="flex items-center gap-3 mb-2">
            <div class="p-2 bg-blue-500/20 rounded-lg border border-blue-400/30 backdrop-blur-sm">
              <el-icon class="text-2xl text-blue-300 animate-pulse"><DataBoard /></el-icon>
            </div>
            <h2 class="text-2xl font-bold tracking-wide">XRIPP 全球公共采购数据驾驶舱</h2>
          </div>
          <p class="text-blue-200/80 text-sm max-w-2xl pl-1">
            实时监控全球采购动态、会员入驻分布及合伙人业绩大屏。支持双地图自动切换与多维数据钻取。
          </p>
        </div>

        <el-button type="primary" size="large" round class="!bg-blue-500 !border-none hover:!bg-blue-400 shadow-lg shadow-blue-500/30 font-bold px-8 py-6 text-base transition-transform group-hover:scale-105">
          立即启动 <el-icon class="ml-2"><Monitor /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 2. 核心数据卡片 (从API加载) -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all hover:-translate-y-1 duration-300 cursor-pointer group">
        <div class="flex justify-between items-start mb-4">
          <div class="p-3 rounded-lg transition-colors" :class="stat.bgClass + ' group-hover:scale-110'">
            <el-icon class="text-xl transition-transform" :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
          <span class="text-xs font-bold px-2 py-1 rounded bg-slate-50 text-slate-500 group-hover:bg-brand-50 group-hover:text-brand-600 transition-colors">{{ stat.period }}</span>
        </div>
        <div class="text-3xl font-bold text-slate-900 mb-1 group-hover:text-brand-600 transition-colors">{{ stat.value }}</div>
        <div class="text-xs text-slate-500">{{ stat.label }}</div>
      </div>
    </div>
    <div class="h-px bg-slate-100"></div>

    <!-- 3. 数据看板模块 -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-base font-bold text-slate-800">数据看板入口</h3>
        <p class="text-xs text-slate-500 mt-1">进入数据录入与发布模块，统一维护数据口径</p>
      </div>
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full" @click="navigateTo('/admin/tenders/publish')">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-blue-600"><Document /></el-icon>
            <h3 class="text-base font-bold text-slate-800">公共采购数据</h3>
          </div>
          <el-button type="primary" size="small" plain @click.stop="navigateTo('/admin/tenders/publish')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">标书总量: {{ tenderStats.total || 0 }}，已发布: {{ tenderStats.published || 0 }}</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-blue-600"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full" @click="navigateTo('/admin/members/list')">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-green-600"><User /></el-icon>
            <h3 class="text-base font-bold text-slate-800">会员数据整理</h3>
          </div>
          <el-button type="primary" size="small" plain @click.stop="navigateTo('/admin/members/list')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">会员总数: {{ memberCount }}</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-green-600"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full" @click="navigateTo('/admin/overseas/points')">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-purple-600"><Location /></el-icon>
            <h3 class="text-base font-bold text-slate-800">国内/国际服务网点</h3>
          </div>
          <el-button type="warning" size="small" plain @click.stop="navigateTo('/admin/overseas/points')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">服务点城市设置，自动匹配服务点总数与省份数量</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-purple-600"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
    <div class="h-px bg-slate-100"></div>

    <!-- 4. 快捷操作区 -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-base font-bold text-slate-800">快捷操作</h3>
        <p class="text-xs text-slate-500 mt-1">日常高频任务快速入口</p>
      </div>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-gradient-to-br from-brand-600 to-brand-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group" @click="navigateTo('/admin/tenders/publish')">
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><CirclePlus /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">发布新标书</h3>
            <p class="text-xs text-brand-100">快速发布采购信息</p>
          </div>
        </div>
        <el-icon class="ml-auto"><ArrowRight /></el-icon>
      </div>

      <div class="bg-gradient-to-br from-purple-600 to-purple-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group" @click="navigateTo('/admin/audit')">
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><UserFilled /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">业务审核</h3>
            <p class="text-xs text-purple-100">处理待审核申请</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span v-if="pendingContentCount > 0" class="bg-white/20 px-2 py-0.5 rounded text-xs animate-pulse">{{ pendingContentCount }} 待处理</span>
          <el-icon class="ml-auto"><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="bg-gradient-to-br from-green-600 to-green-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group" @click="navigateTo('/admin/finance/revenue')">
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><Wallet /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">财务管理</h3>
            <p class="text-xs text-green-100">查看收益与定价策略</p>
          </div>
        </div>
        <el-icon class="ml-auto"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 5. 待处理任务表格 (从API加载) -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
        <div>
          <h3 class="font-bold text-slate-800 mb-1">最新会员</h3>
          <p class="text-xs text-slate-500">最近注册的会员</p>
        </div>
        <el-button type="primary" link @click="navigateTo('/admin/members/list')">查看全部 →</el-button>
      </div>

      <el-table :data="recentMembers" style="width: 100%" :header-cell-style="{background:'#f8fafc', color:'#64748b', fontWeight: 600}" v-loading="membersLoading">
        <el-table-column prop="createdAt" label="注册日期" width="150">
          <template #default="scope">{{ (scope.row.createdAt || scope.row.created_at || '').slice(0, 10) }}</template>
        </el-table-column>
        <el-table-column label="企业名称">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded bg-slate-100 flex items-center justify-center text-slate-600 font-bold text-xs">
                {{ (scope.row.companyName || scope.row.company_name || scope.row.username || '?').substring(0, 2) }}
              </div>
              <span class="font-medium">{{ scope.row.companyName || scope.row.company_name || scope.row.username || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="100">
          <template #default="scope">
            <span class="px-2.5 py-1 rounded-full text-xs font-bold"
              :class="(scope.row.memberLevel || scope.row.member_level) === 'SVIP' ? 'bg-orange-50 text-orange-600 border border-orange-200' : 'bg-blue-50 text-blue-600 border border-blue-200'">
              {{ scope.row.memberLevel || scope.row.member_level || 'NORMAL' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 6. 最近活动 (从API加载) -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="font-bold text-slate-800 mb-6 flex items-center gap-2">
        <el-icon class="text-brand-500"><Clock /></el-icon> 最近发布内容
      </h3>
      <div v-if="recentContents.length === 0" class="text-center py-8 text-slate-400">暂无内容记录</div>
      <div class="space-y-4">
        <div v-for="(log, i) in recentContents" :key="i" class="flex items-start gap-4 p-4 rounded-lg hover:bg-slate-50 transition-colors">
          <div class="w-2 h-2 rounded-full mt-2" :class="log.publishStatus === 'published' ? 'bg-green-500' : log.publishStatus === 'pending_review' ? 'bg-orange-500' : 'bg-blue-500'"></div>
          <div class="flex-grow">
            <p class="text-sm text-slate-700 mb-1">{{ log.title || '未命名内容' }}</p>
            <span class="text-xs text-slate-400">{{ (log.createdAt || log.created_at || '').slice(0, 16) }} · {{ log.contentType || log.content_type || '内容' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  User, Document, Money, TrendCharts, CirclePlus, UserFilled, Wallet, Location,
  ArrowRight, Clock, DataBoard, Monitor
} from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// --- Reactive State ---
const stats = ref([
  { label: '会员总数', value: '-', icon: User, period: '累计', bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '标书总量', value: '-', icon: Document, period: '累计', bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '订单总量', value: '-', icon: Money, period: '累计', bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '内容总量', value: '-', icon: TrendCharts, period: '累计', bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
])

const memberCount = ref(0)
const tenderStats = ref<any>({ total: 0, published: 0 })
const pendingContentCount = ref(0)
const recentMembers = ref<any[]>([])
const recentContents = ref<any[]>([])
const membersLoading = ref(false)

// --- Load all dashboard data ---
const loadDashboard = async () => {
  membersLoading.value = true

  await Promise.allSettled([
    // 1. Members count
    apiRequest('/v3/admin/members', { query: { page: 1, page_size: 5 } }).then((res: any) => {
      const total = res?.data?.total ?? 0
      memberCount.value = total
      stats.value[0].value = String(total)
      recentMembers.value = Array.isArray(res?.data?.items) ? res.data.items : []
    }),

    // 2. Tenders stats
    apiRequest('/v3/admin/tenders/stats').then((res: any) => {
      const data = res?.data || {}
      const published = Number(data.published || 0)
      const draft = Number(data.draft || 0)
      const archived = Number(data.archived || 0)
      const total = published + draft + archived
      tenderStats.value = { total, published }
      stats.value[1].value = total.toLocaleString()
    }),

    // 3. Orders stats
    apiRequest('/v3/admin/orders/stats').then((res: any) => {
      const data = res?.data || {}
      let totalOrders = 0
      Object.values(data).forEach((v: any) => {
        totalOrders += Number(v?.count || 0)
      })
      stats.value[2].value = String(totalOrders)
    }),

    // 4. Contents stats
    apiRequest('/v3/admin/contents/stats').then((res: any) => {
      const data = res?.data || {}
      let totalContents = 0
      Object.values(data).forEach((v: any) => {
        totalContents += Number(v || 0)
      })
      pendingContentCount.value = Number(data.pending_review || 0)
      stats.value[3].value = String(totalContents)
    }),

    // 5. Recent contents
    apiRequest('/v3/admin/contents', { query: { page: 1, page_size: 5 } }).then((res: any) => {
      recentContents.value = Array.isArray(res?.data?.items) ? res.data.items : []
    })
  ])

  membersLoading.value = false
}

onMounted(() => {
  loadDashboard()
})
</script>
