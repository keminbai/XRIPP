<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-brand-900 flex items-center justify-center p-4 relative overflow-hidden">
    
    <!-- 背景装饰 -->
    <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
    <div class="absolute top-0 right-0 w-[600px] h-[600px] bg-brand-600/10 rounded-full blur-3xl"></div>
    <div class="absolute bottom-0 left-0 w-[500px] h-[500px] bg-blue-600/10 rounded-full blur-3xl"></div>
    
    <div class="relative z-10 text-center max-w-2xl">
      <!-- 404 大字 -->
      <div class="mb-8 relative">
        <div class="text-[180px] md:text-[240px] font-bold text-white/5 leading-none select-none">404</div>
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="text-6xl md:text-8xl font-bold text-white drop-shadow-2xl animate-bounce-slow">
            🔍
          </div>
        </div>
      </div>

      <!-- 文字说明 -->
      <h1 class="text-3xl md:text-5xl font-bold text-white mb-4 tracking-tight">
        页面走丢了...
      </h1>
      <p class="text-xl text-slate-300 mb-10 font-light leading-relaxed">
        您访问的页面可能已被移除、名称已更改<br>
        或者暂时不可用
      </p>

      <!-- 按钮组 -->
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button 
          class="px-8 py-4 bg-white text-brand-700 font-bold rounded-xl shadow-lg hover:shadow-2xl hover:scale-105 transition-all flex items-center justify-center gap-2"
          @click="navigateTo('/')"
        >
          <el-icon><HomeFilled /></el-icon> 返回首页
        </button>
        <button 
          class="px-8 py-4 bg-white/10 backdrop-blur-md border border-white/20 text-white font-bold rounded-xl hover:bg-white/20 transition-all flex items-center justify-center gap-2"
          @click="navigateTo('/procurement')"
        >
          <el-icon><Search /></el-icon> 浏览商机
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { HomeFilled, Search } from '@element-plus/icons-vue'

definePageMeta({
  layout: false
})

useHead({
  title: '404 - 页面未找到 | IPP全球公共采购服务平台'
})
</script>

<style scoped>
@keyframes bounce-slow {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}
.animate-bounce-slow {
  animation: bounce-slow 3s ease-in-out infinite;
}
</style>