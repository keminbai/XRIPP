<!-- 文件路径: D:\ipp-platform\app\layouts\member.vue -->
<template>
  <div class="bg-[#f1f5f9] min-h-screen pb-20 font-sans">
    
    <!-- 沉浸式头部 -->
    <div class="relative h-64 bg-[#0F172A] overflow-hidden">
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
      <div class="absolute top-[-50%] right-[-10%] w-[500px] h-[500px] bg-blue-600/30 rounded-full blur-[100px] animate-pulse-slow"></div>
      <div class="absolute bottom-[-20%] left-[-10%] w-[400px] h-[400px] bg-indigo-600/20 rounded-full blur-[80px]"></div>
      
      <div class="container mx-auto px-4 pt-10 relative z-10 flex justify-between items-start">
        <!-- 左侧：问候语 -->
        <div class="text-white">
          <div class="text-blue-200 text-sm font-medium mb-1 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span> 系统运行正常
          </div>
          <h1
            class="text-3xl font-bold tracking-tight cursor-pointer hover:text-blue-200 transition-colors flex items-center gap-2"
            @click="navigateTo('/member')"
            title="返回控制台首页"
          >
            会员控制台
          </h1>
          <p class="text-slate-400 text-sm mt-1">{{ greeting }}，愿您今日商机无限</p>
        </div>
        
        <!-- 右侧：系统功能区 -->
        <div class="flex gap-3">
          <el-tooltip content="返回门户首页" placement="bottom">
            <button class="flex items-center justify-center w-10 h-10 rounded-lg bg-white/10 hover:bg-white/20 backdrop-blur-md border border-white/10 text-white transition-all hover:scale-105" @click="navigateTo('/')">
              <el-icon class="text-lg"><HomeFilled /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="3" class="item">
              <button class="flex items-center justify-center w-10 h-10 rounded-lg bg-white/10 hover:bg-white/20 backdrop-blur-md border border-white/10 text-white transition-all hover:scale-105">
                <el-icon class="text-lg"><Bell /></el-icon>
              </button>
            </el-badge>
          </el-tooltip>
          <el-tooltip content="账号设置" placement="bottom">
            <button class="flex items-center justify-center w-10 h-10 rounded-lg bg-white/10 hover:bg-white/20 backdrop-blur-md border border-white/10 text-white transition-all hover:scale-105" @click="navigateTo('/member/settings')">
              <el-icon class="text-lg"><Setting /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="安全退出" placement="bottom">
            <button class="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-500/80 hover:bg-red-600 backdrop-blur-md border border-red-400/30 text-white text-sm font-bold transition-all hover:shadow-lg ml-2" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出
            </button>
          </el-tooltip>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4 -mt-20 relative z-20">
      <div class="flex flex-col lg:flex-row gap-6">
        
        <!-- 左侧：身份卡 & 导航 -->
        <div class="w-full lg:w-80 flex-shrink-0 space-y-6">
          
          <!-- 身份卡片 -->
          <div class="bg-white rounded-2xl shadow-xl border border-slate-100 overflow-hidden relative group">
            <div class="h-2 bg-gradient-to-r from-slate-800 via-yellow-600 to-slate-800"></div>
            
            <div class="p-5 text-center relative">
              <div class="w-20 h-20 mx-auto rounded-full p-1 bg-gradient-to-br from-yellow-400 to-yellow-600 shadow-lg mb-3 group-hover:scale-105 transition-transform duration-300">
                <div class="w-full h-full rounded-full border-[3px] border-white overflow-hidden bg-white">
                  <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="w-full h-full object-cover" />
                </div>
              </div>
              <h2 class="text-lg font-bold text-slate-900">张伟 总经理</h2>
              <p class="text-xs text-slate-500 mt-1 mb-3">上海宏大进出口贸易有限公司</p>
              
              <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900 text-[#FCD34D] text-xs font-bold shadow-md shadow-slate-200/50">
                <el-icon class="text-sm"><Trophy /></el-icon> SVIP 超级会员
              </div>

              <div class="grid grid-cols-2 gap-2 mt-6 text-left">
                <div class="bg-slate-50 p-2 rounded border border-slate-100 hover:border-blue-200 transition-colors">
                  <div class="text-[10px] text-slate-400">标书查看</div>
                  <div class="text-sm font-bold text-slate-800">185<span class="text-[10px] font-normal text-slate-400">/200</span></div>
                </div>
                <div class="bg-slate-50 p-2 rounded border border-slate-100 hover:border-blue-200 transition-colors">
                  <div class="text-[10px] text-slate-400">短信剩余</div>
                  <div class="text-sm font-bold text-slate-800">198<span class="text-[10px] font-normal text-slate-400">/200</span></div>
                </div>
                <div class="bg-slate-50 p-2 rounded border border-slate-100 hover:border-blue-200 transition-colors">
                  <div class="text-[10px] text-slate-400">报告下载</div>
                  <div class="text-sm font-bold text-slate-800">5<span class="text-[10px] font-normal text-slate-400">份</span></div>
                </div>
                <div class="bg-slate-50 p-2 rounded border border-slate-100 hover:border-blue-200 transition-colors">
                  <div class="text-[10px] text-slate-400">联合国入库</div>
                  <div class="text-sm font-bold text-green-600">1<span class="text-[10px] font-normal text-slate-400">次</span></div>
                </div>
                <div class="bg-slate-50 p-2 rounded border border-slate-100 col-span-2 flex justify-between items-center hover:border-blue-200 transition-colors cursor-pointer group/sub" @click="navigateTo('/member/sub-accounts')">
                  <div class="text-[10px] text-slate-400">子账号设置</div>
                  <div class="text-sm font-bold text-slate-800 flex items-center">
                    2<span class="text-[10px] font-normal text-slate-400">/5 个</span>
                    <el-icon class="ml-1 text-slate-300 group-hover/sub:text-blue-500"><ArrowRight /></el-icon>
                  </div>
                </div>
                <div class="bg-slate-50 p-2 rounded border border-slate-100 col-span-2 flex justify-between items-center hover:border-blue-200 transition-colors">
                  <div class="text-[10px] text-slate-400">收费活动免费名额</div>
                  <div class="text-sm font-bold text-brand-600">1 次</div>
                </div>
              </div>
              
              <div class="mt-4 pt-4 border-t border-slate-100 flex justify-between items-end">
                <div class="text-left">
                  <div class="text-[10px] text-slate-400 mb-0.5">会员有效期至</div>
                  <div class="text-xs font-bold text-slate-700">2026-12-31</div>
                </div>
                <el-button type="primary" link size="small" class="text-xs font-bold !p-0">立即续费 ></el-button>
              </div>
            </div>
          </div>

          <!-- 导航菜单 -->
          <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-2 space-y-1">
            
            <div class="text-xs font-bold text-slate-400 px-4 py-2 mt-2 mb-1 uppercase tracking-wider">控制台</div>
            <NuxtLink 
              v-for="(item, index) in menuItems.console" 
              :key="index"
              :to="item.path"
              class="flex items-center justify-between px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
              active-class="bg-blue-50 text-blue-700 font-bold"
              inactive-class="text-slate-600 hover:bg-slate-50 hover:text-slate-900"
            >
              <div class="flex items-center gap-3">
                <el-icon class="text-lg transition-transform group-hover:scale-110"><component :is="item.icon" /></el-icon>
                <span>{{ item.name }}</span>
              </div>
            </NuxtLink>

            <div class="text-xs font-bold text-slate-400 px-4 py-2 mt-4 mb-1 uppercase tracking-wider">增值服务</div>
            <NuxtLink 
              v-for="(item, index) in menuItems.service" 
              :key="index"
              :to="item.path"
              class="flex items-center justify-between px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
              active-class="bg-blue-50 text-blue-700 font-bold"
              inactive-class="text-slate-600 hover:bg-slate-50 hover:text-slate-900"
            >
              <div class="flex items-center gap-3">
                <el-icon class="text-lg transition-transform group-hover:scale-110"><component :is="item.icon" /></el-icon>
                <span>{{ item.name }}</span>
              </div>
            </NuxtLink>

            <div class="text-xs font-bold text-slate-400 px-4 py-2 mt-4 mb-1 uppercase tracking-wider">业务支持</div>
            <NuxtLink 
              v-for="(item, index) in menuItems.support" 
              :key="index"
              :to="item.path"
              class="flex items-center justify-between px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
              active-class="bg-blue-50 text-blue-700 font-bold"
              inactive-class="text-slate-600 hover:bg-slate-50 hover:text-slate-900"
            >
              <div class="flex items-center gap-3">
                <el-icon class="text-lg transition-transform group-hover:scale-110"><component :is="item.icon" /></el-icon>
                <span>{{ item.name }}</span>
              </div>
            </NuxtLink>

            <div class="text-xs font-bold text-slate-400 px-4 py-2 mt-4 mb-1 uppercase tracking-wider">账户管理</div>
            <NuxtLink 
              v-for="(item, index) in menuItems.account" 
              :key="index"
              :to="item.path"
              class="flex items-center justify-between px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden"
              active-class="bg-blue-50 text-blue-700 font-bold"
              inactive-class="text-slate-600 hover:bg-slate-50 hover:text-slate-900"
            >
              <div class="flex items-center gap-3">
                <el-icon class="text-lg transition-transform group-hover:scale-110"><component :is="item.icon" /></el-icon>
                <span>{{ item.name }}</span>
              </div>
            </NuxtLink>

          </div>
        </div>

        <!-- 右侧：子页面内容 -->
        <div class="flex-grow">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  Bell, Setting, Trophy, ArrowRight, Monitor, Document, Star, User, 
  HomeFilled, SwitchButton, Medal, Message, Reading, OfficeBuilding, 
  Edit, TrendCharts, Headset
} from '@element-plus/icons-vue'
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const menuItems = {
  console: [
    { name: '控制台首页', icon: Monitor, path: '/member' },
    { name: '我的订单', icon: Document, path: '/member/orders' },
    { name: '我的收藏', icon: Star, path: '/member/favorites' },
  ],
  service: [
    { name: '发布需求', icon: Reading, path: '/member/publish-demand' },
    { name: '服务商入驻', icon: Trophy, path: '/member/supplier-apply' },
    { name: '联合国协助认证', icon: OfficeBuilding, path: '/member/un-apply' },
    { name: '标书代写', icon: Edit, path: '/member/tender-writing' },
    { name: '英文建站', icon: Monitor, path: '/member/english-website' },
    { name: '广告投放', icon: TrendCharts, path: '/member/ad-promotion' },
  ],
  support: [
    { name: '证书下载', icon: Medal, path: '/member/certificates' },
    { name: '短信设置', icon: Message, path: '/member/sms-settings' },
  ],
  account: [
    { name: '账号管理', icon: User, path: '/member/settings' },
    { name: '子账号管理', icon: User, path: '/member/sub-accounts' },
    { name: '客服工单', icon: Headset, path: '/member/feedback' },
  ]
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    // 这里可以添加实际登出逻辑，如清除 token
    navigateTo('/login')
  }).catch(() => {})
}
</script>

<style scoped>
:deep(.el-badge__content.is-fixed) { top: 5px; right: 5px; }
</style>