<!-- 文件路径: D:\ipp-platform\app\pages\about.vue -->
<template>
  <div class="bg-white min-h-screen">
    
    <!-- 1. Hero 区域：企业愿景（微调 slogan 与新文字呼应） -->
    <div class="relative h-[400px] flex items-center justify-center text-center overflow-hidden bg-slate-900">
      <div class="absolute inset-0 z-0">
        <img src="https://images.unsplash.com/photo-1497366216548-37526070297c?q=80&w=2000&auto=format&fit=crop" class="w-full h-full object-cover opacity-40" />
        <div class="absolute inset-0 bg-gradient-to-b from-transparent to-white/5"></div>
      </div>
      <div class="relative z-10 container mx-auto px-4">
        <h1 class="text-4xl md:text-6xl font-bold text-white mb-6 tracking-tight animate-fade-in-up">
          连接中国企业与全球公共采购
        </h1>
        <p class="text-xl text-blue-100 max-w-3xl mx-auto font-light animate-fade-in-up delay-100">
          依托上海中小企业国际合作协会34年国际资源积累，打造中国中小企业参与国际公共采购的基建级入口
        </p>
      </div>
    </div>

    <!-- 2. 平台简介（替换为客户提供文字） -->
    <div class="py-20 container mx-auto px-4">
      <div class="flex flex-col md:flex-row gap-12 items-center">
        <div class="w-full md:w-1/2">
          <img src="https://images.unsplash.com/photo-1556761175-5973dc0f32e7?q=80&w=1000&auto=format&fit=crop" class="rounded-2xl shadow-2xl" />
        </div>
        <div class="w-full md:w-1/2 space-y-6">
          <div class="text-2xl md:text-3xl font-bold text-slate-900">协会与平台介绍</div>
          <div class="text-sm md:text-base text-slate-500 font-medium">上海中小企业国际合作协会 · XRIPP平台</div>
          
          <div class="prose prose-slate max-w-none text-slate-600 leading-relaxed text-justify space-y-6">
            <div class="text-sm font-bold text-brand-600 tracking-wide">协会背景</div>
            <p>
              上海中小企业国际合作协会成立于<span class="font-bold">1992年9月21日</span>。
              正值邓小平南巡讲话，上海进入改革开放快速期，时任上海市委、市政府领导高屋建瓴组建本协会，并由市政府相关领导担任协会法人代表及会长。
            </p>

            <div class="text-sm font-bold text-brand-600 tracking-wide">国际合作网络</div>
            <p>
              在三十四年的发展过程中，作为上海唯一的国际合作协会，曾与<span class="font-bold">北美、日、欧、东南亚、俄罗斯、南美、非洲、中东等七十多个国家及地区</span>，在科技、市场、知识产权、金融、贸易等领域进行多方面各层次交流与合作。
            </p>

            <div class="text-sm font-bold text-brand-600 tracking-wide">平台价值</div>
            <p>
              作为中小企业最重要的资源整合平台、信息传播纽带、政府与企业桥梁，协会理论上为<span class="font-bold">上海100多万家企业</span>提供国际市场融通、供应链服务、科技创新支持等工作。
            </p>
            <p>
              XRIPP国际公共采购服务平台是依托上海中小企业国际合作协会的政企资源与国际供应链网络，打造的首个“G”to“B”服务平台。构建以“<span class="font-bold">数据+服务+生态</span>”三位一体的PC端综合解决方案，利用智能算法实现“需求-供应”精准匹配，同时整合协会国际供应链中心的资质预审通道、供应链金融资源及跨境履约服务体系，为中小企业提供“<span class="font-bold">找标-投标-中标-交付</span>”一站式的数字化服务工具。
            </p>
            <p>
              实现多语言AI智能翻译技术，平台既保障采购合规性，又降低企业参与门槛，形成“<span class="font-bold">数据驱动、资源协同、服务闭环</span>”的差异化壁垒，成为中国中小企业出海及参与国际公共采购的基建级入口。
            </p>
          </div>
          
          <!-- 保留统计卡片，增强说服力 -->
          <div class="grid grid-cols-2 gap-6 pt-6">
            <div class="border-l-4 border-brand-500 pl-4">
              <div class="text-3xl font-bold text-slate-900">34</div>
              <div class="text-sm text-slate-500">年协会历史</div>
            </div>
            <div class="border-l-4 border-yellow-500 pl-4">
              <div class="text-3xl font-bold text-slate-900">70+</div>
              <div class="text-sm text-slate-500">合作国家/地区</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 核心优势（保持不变，或根据需要微调） -->
    <div class="bg-slate-50 py-20">
      <div class="container mx-auto px-4 text-center">
        <h2 class="text-3xl font-bold text-slate-900 mb-12">我们的核心优势</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="item in advantages" :key="item.title" class="bg-white p-8 rounded-xl shadow-sm hover:shadow-lg transition-all hover:-translate-y-2 group">
            <div class="w-16 h-16 mx-auto bg-blue-50 rounded-full flex items-center justify-center text-3xl mb-6 group-hover:bg-brand-600 group-hover:text-white transition-colors">
              {{ item.icon }}
            </div>
            <h3 class="text-lg font-bold text-slate-900 mb-3">{{ item.title }}</h3>
            <p class="text-sm text-slate-500 leading-relaxed">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 联系方式（保持不变） -->
    <div class="py-20 container mx-auto px-4">
      <div class="bg-gradient-to-r from-[#0f172a] to-[#1e293b] rounded-3xl overflow-hidden text-white shadow-2xl flex flex-col md:flex-row">
        <div class="w-full md:w-1/2 p-10 md:p-12 flex flex-col justify-center">
          <h2 class="text-3xl font-bold mb-8">联系我们</h2>
          <div class="space-y-6">
            <div class="flex items-start gap-4">
              <div class="p-3 bg-white/10 rounded-lg"><el-icon class="text-xl"><Location /></el-icon></div>
              <div>
                <h4 class="font-bold text-lg">总部地址</h4>
                <p class="text-blue-200 text-sm">上海市黄浦区北京东路668号科技京城东楼18F</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="p-3 bg-white/10 rounded-lg"><el-icon class="text-xl"><Phone /></el-icon></div>
              <div>
                <h4 class="font-bold text-lg">服务热线</h4>
                <p class="text-blue-200 text-sm">400-888-8888</p>
              </div>
            </div>
            <div class="flex items-start gap-4">
              <div class="p-3 bg-white/10 rounded-lg"><el-icon class="text-xl"><Message /></el-icon></div>
              <div>
                <h4 class="font-bold text-lg">商务合作</h4>
                <p class="text-blue-200 text-sm">postmaster@xripp.com</p>
              </div>
            </div>
          </div>
        </div>

        <div class="w-full md:w-1/2 h-[400px] md:h-auto relative">
          <img src="https://images.unsplash.com/photo-1577086663218-9644f318a624?q=80&w=1000&auto=format&fit=crop" class="w-full h-full object-cover filter grayscale hover:grayscale-0 transition-all duration-500" />
          <div class="absolute inset-0 bg-blue-900/20 pointer-events-none"></div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Location, Phone, Message } from '@element-plus/icons-vue'

useHead({
  title: '关于我们 - XRIPP国际公共采购服务平台',
  meta: [
    { name: 'description', content: '依托上海中小企业国际合作协会34年资源积累，XRIPP平台为中国中小企业提供国际公共采购一站式数字化服务。' }
  ]
})

const advantages = [
  { icon: '🏛️', title: '官方背景', desc: '协会指导，与联合国机构及多国政府建立长期互信合作机制。' },
  { icon: '🌍', title: '全球网络', desc: '覆盖70+国家/地区，提供本地化采购支持。' },
  { icon: '💡', title: '专业团队', desc: '前联合国采购官、国际法务专家组成的智囊团。' },
  { icon: '⚡', title: '数据智能', desc: 'AI算法精准匹配 + 多语言翻译，降低出海门槛。' },
]
</script>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out forwards;
  opacity: 0;
}
.delay-100 {
  animation-delay: 0.2s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
