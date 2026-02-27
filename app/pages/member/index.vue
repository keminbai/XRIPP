<!-- 文件路径: D:\ipp-platform\app\pages\member\index.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 1. 核心资产卡 (权益展示) -->
    <!-- ✅ 优化：移除账户余额，改为2列布局，更聚焦核心权益 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
      <!-- 标书权益卡 -->
      <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
        <div class="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-blue-50 to-transparent rounded-bl-[100px] -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 text-slate-500 text-sm font-medium mb-2">
            <div class="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center"><el-icon><Document /></el-icon></div>
            标书下载权益
          </div>
          <div class="flex items-baseline gap-2 mt-4">
            <span class="text-4xl font-bold text-slate-900 tracking-tight">{{ tenderRemaining }}</span>
            <span class="text-sm text-slate-400">/ {{ tenderTotal }} 次</span>
          </div>
          <div class="mt-4 flex items-center text-xs text-brand-600 font-bold cursor-pointer hover:underline" @click="navigateTo('/procurement')">
            去下载标书 <el-icon class="ml-1"><Right /></el-icon>
          </div>
        </div>
      </div>

      <!-- 短信权益卡 -->
      <div class="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
        <div class="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-purple-50 to-transparent rounded-bl-[100px] -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
        <div class="relative z-10">
          <div class="flex items-center gap-2 text-slate-500 text-sm font-medium mb-2">
            <div class="w-8 h-8 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center"><el-icon><Message /></el-icon></div>
            精准推送余额
          </div>
          <div class="flex items-baseline gap-2 mt-4">
            <span class="text-4xl font-bold text-slate-900 tracking-tight">{{ smsRemaining }}</span>
            <span class="text-sm text-slate-400">条</span>
          </div>
          <div class="mt-4 flex items-center text-xs text-purple-600 font-bold cursor-pointer hover:underline" @click="navigateTo('/member/sms-settings')">
            设置关键词 <el-icon class="ml-1"><Right /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 最新订单动态 (保持不变) -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="p-6 border-b border-slate-50 flex justify-between items-center bg-slate-50/50">
        <div>
          <h3 class="font-bold text-lg text-slate-900 flex items-center gap-2">
            <el-icon class="text-brand-500"><List /></el-icon> 最新订单
          </h3>
          <p class="text-xs text-slate-400 mt-1">实时追踪您的服务进度与交易状态</p>
        </div>
        <button class="text-sm font-bold text-brand-600 hover:text-brand-700 flex items-center gap-1 transition-colors" @click="navigateTo('/member/orders')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </button>
      </div>

      <div class="divide-y divide-slate-50">
        <div v-for="order in recentOrders" :key="order.id" class="p-5 hover:bg-slate-50 transition-colors group cursor-pointer" @click="navigateTo('/member/orders')">
          <div class="flex items-center justify-between gap-4">
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-lg" :class="order.iconBg">
                {{ order.icon }}
              </div>
              <div>
                <h4 class="font-bold text-slate-800 text-sm group-hover:text-brand-600 transition-colors">
                  {{ order.title }}
                </h4>
                <div class="text-xs text-slate-400 mt-1">{{ order.date }} · {{ order.id }}</div>
              </div>
            </div>
            
            <div class="flex items-center gap-6">
              <span class="font-bold text-slate-700">¥{{ order.amount }}</span>
              <span class="px-2.5 py-1 rounded-full text-xs font-bold" :class="order.statusClass">
                {{ order.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 常用工具 -->
    <!-- ✅ 核心优化：整合短信、证书、单据入口 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-100 p-6">
      <h3 class="font-bold text-lg text-slate-900 mb-6 flex items-center gap-2">
        <el-icon class="text-brand-500"><Menu /></el-icon> 常用工具
      </h3>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
        <div 
          v-for="tool in tools" 
          :key="tool.name" 
          class="flex flex-col items-center justify-center p-4 rounded-xl border border-slate-50 hover:border-brand-100 hover:bg-brand-50/50 hover:shadow-sm transition-all cursor-pointer group"
          @click="navigateTo(tool.path)"
        >
          <div class="w-12 h-12 rounded-full flex items-center justify-center text-2xl mb-3 transition-transform group-hover:scale-110 group-hover:rotate-3" :class="tool.bgClass">
            <el-icon><component :is="tool.icon" /></el-icon>
          </div>
          <span class="text-sm font-medium text-slate-600 group-hover:text-slate-900">{{ tool.name }}</span>
        </div>
      </div>
    </div>

    <!-- 4. 企业公益赞助 (保持入口) -->
    <!-- 合作伙伴广告位 -->
    <div>
      <div
        class="relative w-full h-40 rounded-2xl border border-slate-200 bg-gradient-to-r from-brand-700 to-brand-600 px-6 md:px-10 text-white shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all flex items-center"
        @click="navigateTo('/supplier-directory')"
      >
        <div class="absolute right-3 top-3 px-2 py-0.5 text-[10px] rounded border border-white/20 bg-white/10">合作伙伴</div>
        <div class="max-w-2xl">
          <div class="text-xs text-blue-200 font-semibold mb-2">精选合作伙伴</div>
          <div class="text-lg md:text-2xl font-bold mb-1">服务商风采 · 合作伙伴广告位</div>
          <p class="text-xs text-slate-200">面向会员展示优质服务商与合作伙伴资源。</p>
        </div>
        <div class="ml-auto hidden md:block">
          <div class="bg-white text-slate-900 px-5 py-2 rounded-lg text-xs font-bold">了解合作</div>
        </div>
      </div>
    </div>

    <div 
      class="relative bg-gradient-to-r from-red-50 via-pink-50 to-orange-50 rounded-2xl p-6 border border-pink-100 cursor-pointer overflow-hidden group shadow-sm hover:shadow-lg transition-all duration-300"
      @click="charityDialogVisible = true"
    >
      <!-- 爱心装饰背景 -->
      <div class="absolute right-0 bottom-0 text-9xl opacity-5 text-red-500 rotate-12 transform group-hover:scale-110 transition-transform duration-700">❤️</div>
      
      <div class="relative z-10 flex justify-between items-center">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 bg-gradient-to-br from-red-500 to-pink-500 text-white rounded-xl flex items-center justify-center text-3xl shadow-lg shadow-red-500/30 group-hover:scale-110 transition-transform">
            <el-icon><StarFilled /></el-icon>
          </div>
          <div>
            <h3 class="text-lg font-bold text-slate-800 mb-1">企业公益赞助</h3>
            <p class="text-sm text-slate-500">捐赠公益物品，获颁平台荣誉证书与会员权益</p>
          </div>
        </div>
        <div class="hidden md:flex items-center gap-2 bg-white/80 backdrop-blur-sm px-4 py-2 rounded-full text-red-600 text-sm font-bold group-hover:bg-white transition-colors border border-red-100">
          了解详情 <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <!-- 公益流程弹窗 -->
    <!-- ✅ 优化：接收机构脱敏为 XXXXXXX -->
    <el-dialog v-model="charityDialogVisible" title="公益赞助说明" width="600px" align-center>
      <div class="text-center mb-8">
        <p class="text-slate-600 text-sm leading-relaxed mb-4">
          XRIPP平台携手公益机构，诚邀爱心企业参与公益物资捐赠。
        </p>
        
        <!-- 流程图 -->
        <div class="flex items-center justify-center gap-4 text-xs text-slate-500 bg-slate-50 p-4 rounded-xl border border-slate-100">
          <div class="flex flex-col items-center gap-2">
            <div class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center text-lg">📦</div>
            <span>捐赠物品</span>
          </div>
          <div class="text-slate-300">→</div>
          <div class="flex flex-col items-center gap-2">
            <div class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center text-lg">📜</div>
            <span>获颁证书</span>
          </div>
          <div class="text-slate-300">→</div>
          <div class="flex flex-col items-center gap-2">
            <div class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center text-lg">🎁</div>
            <span>获会员机会</span>
          </div>
        </div>
      </div>
      
      <div class="bg-red-50 p-4 rounded-lg text-sm text-red-800 mb-6 flex gap-3 items-start">
        <el-icon class="mt-0.5 text-lg"><InfoFilled /></el-icon>
        <div>
          <div class="font-bold mb-1">接收机构：</div>
          <!-- ✅ 已脱敏 -->
          <div>XXXXXXX</div>
        </div>
      </div>

      <template #footer>
        <div class="flex gap-3 justify-center">
          <el-button @click="charityDialogVisible = false">关闭</el-button>
          <el-button type="primary" color="#e11d48" class="text-white" @click="handleContactService">
            <el-icon class="mr-2"><Headset /></el-icon> 咨询客服
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Document, Wallet, Right, ArrowRight, List, Message,
  Trophy, Menu, Edit, TrendCharts, OfficeBuilding,
  User, Monitor, Headset, Service, StarFilled, InfoFilled,
  Medal, Tickets
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useMemberBenefitModel } from '@/composables/useMemberBenefitModel'

definePageMeta({ layout: 'member' })

const charityDialogVisible = ref(false)

const { rightsByType, remainingByType } = useMemberBenefitModel()

const tenderTotal = computed(() => rightsByType.value.tender_download || 0)
const tenderRemaining = computed(() => remainingByType.value.tender_download || 0)
const smsRemaining = computed(() => remainingByType.value.sms_quota || 0)

const recentOrders = [
  { 
    id: '25120000001', title: 'SVIP超级会员 (1年)', date: '2025-12-26', amount: '19,999.00', 
    status: '待支付', statusClass: 'bg-orange-50 text-orange-600', icon: '👑', iconBg: 'bg-orange-100' 
  },
  { 
    id: '25120000002', title: '联合国供应商注册协助', date: '2025-12-20', amount: '1,500.00', 
    status: '服务中', statusClass: 'bg-blue-50 text-blue-600', icon: '🇺🇳', iconBg: 'bg-blue-100' 
  },
  { 
    id: '25120000003', title: '标书下载 (5份)', date: '2025-12-18', amount: '0.00', 
    status: '已完成', statusClass: 'bg-green-50 text-green-600', icon: '📄', iconBg: 'bg-green-100' 
  }
]

// ✅ 常用工具列表 (已包含短信、证书、单据)
const tools = [
  { name: '发布需求', icon: Document, path: '/member/publish-demand', bgClass: 'bg-blue-100 text-blue-600' },
  { name: '服务商入驻', icon: Trophy, path: '/member/supplier-apply', bgClass: 'bg-purple-100 text-purple-600' },
  { name: '联合国协助认证', icon: OfficeBuilding, path: '/member/un-apply', bgClass: 'bg-indigo-100 text-indigo-600' },
  { name: '标书定制服务', icon: Edit, path: '/member/tender-writing', bgClass: 'bg-orange-100 text-orange-600' },
  { name: '广告投放', icon: TrendCharts, path: '/member/ad-promotion', bgClass: 'bg-emerald-100 text-emerald-600' },
  { name: '英文建站', icon: Monitor, path: '/member/english-website', bgClass: 'bg-pink-100 text-pink-600' },
  { name: '子账号管理', icon: User, path: '/member/sub-accounts', bgClass: 'bg-cyan-100 text-cyan-600' },
  { name: '客服工单', icon: Headset, path: '/member/feedback', bgClass: 'bg-teal-100 text-teal-600' },
  { name: '我的收藏', icon: Trophy, path: '/member/favorites', bgClass: 'bg-yellow-100 text-yellow-600' },
  // ✅ 新增
  { name: '短信设置', icon: Message, path: '/member/sms-settings', bgClass: 'bg-purple-100 text-purple-600' },
  { name: '证书下载', icon: Medal, path: '/member/certificates', bgClass: 'bg-amber-100 text-amber-600' },
]

const handleContactService = () => {
  charityDialogVisible.value = false
  navigateTo('/member/feedback')
}
</script>
