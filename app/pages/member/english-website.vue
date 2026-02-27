<!-- 文件路径: app/pages/member/english-website.vue -->
<template>
  <div class="space-y-6">
    
    <!-- Hero Banner -->
    <div class="relative bg-slate-900 rounded-2xl p-10 overflow-hidden text-white shadow-xl flex flex-col md:flex-row items-center justify-between gap-8">
      <div class="relative z-10 max-w-xl">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-xs font-bold mb-4">
          <el-icon><Monitor /></el-icon> GLOBAL BRANDING
        </div>
        <h1 class="text-3xl font-bold mb-4">企业英文官网建设服务</h1>
        <p class="text-slate-300 text-sm leading-relaxed mb-6">
          很多中国企业的官网不符合国际审美，导致海外买家信任度降低。
          <br>XRIPP提供符合欧美浏览习惯的响应式英文网站制作服务，包含SEO基础优化。
        </p>
        <div class="flex gap-4">
          <div class="text-center px-4 py-2 bg-white/10 rounded-lg">
            <div class="text-xl font-bold text-brand-400">7天</div>
            <div class="text-xs text-slate-400">快速交付</div>
          </div>
          <div class="text-center px-4 py-2 bg-white/10 rounded-lg">
            <div class="text-xl font-bold text-brand-400">SEO</div>
            <div class="text-xs text-slate-400">谷歌收录</div>
          </div>
          <div class="text-center px-4 py-2 bg-white/10 rounded-lg">
            <div class="text-xl font-bold text-brand-400">HTTPS</div>
            <div class="text-xs text-slate-400">安全证书</div>
          </div>
        </div>
      </div>
      <!-- 右侧示意图 -->
      <div class="relative w-full md:w-1/3 h-48 bg-gradient-to-br from-slate-700 to-slate-800 rounded-lg border border-slate-600 flex items-center justify-center shadow-2xl transform rotate-3 hover:rotate-0 transition-transform duration-500">
        <div class="text-center">
          <el-icon class="text-5xl text-brand-500 mb-2"><Monitor /></el-icon>
          <div class="text-sm font-mono text-slate-400">www.your-company.com</div>
        </div>
      </div>
    </div>

    <!-- 申请表单 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
      <h3 class="text-lg font-bold text-slate-900 mb-6 border-b border-slate-100 pb-4">
        提交建站需求
      </h3>
      
      <el-form :model="form" label-position="top" size="large" class="max-w-3xl">
        <el-form-item label="企业中文名称" required>
          <el-input v-model="form.companyNameZh" placeholder="请输入公司全称" />
        </el-form-item>
        
        <el-form-item label="期望英文域名 (Domain)" required>
          <el-input v-model="form.domain" placeholder="例如：solar-tech.com">
            <template #prepend>www.</template>
          </el-input>
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <el-form-item label="网站风格偏好">
            <el-select v-model="form.style" class="w-full">
              <el-option label="工业/制造业 (严谨、展示产品参数)" value="industrial" />
              <el-option label="科技/互联网 (现代、扁平化)" value="tech" />
              <el-option label="消费品/贸易 (时尚、多图)" value="consumer" />
            </el-select>
          </el-form-item>
          <el-form-item label="预算范围">
             <el-select v-model="form.budget" class="w-full">
              <el-option label="基础展示型 (¥3,000起)" value="basic" />
              <el-option label="品牌营销型 (¥8,000起)" value="pro" />
              <el-option label="高端定制型 (¥15,000起)" value="custom" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="参考网站 (Benchmark)">
          <el-input v-model="form.benchmark" placeholder="请输入您喜欢的同行网站链接，供设计师参考" />
        </el-form-item>

        <el-form-item label="联系人与电话" required>
          <div class="flex gap-4">
            <el-input v-model="form.contactName" placeholder="联系人" />
            <el-input v-model="form.contactPhone" placeholder="手机号" />
          </div>
        </el-form-item>

        <div class="bg-blue-50 p-4 rounded-lg text-sm text-blue-800 mb-6 flex items-start gap-2">
          <el-icon class="mt-0.5 text-lg"><InfoFilled /></el-icon>
          <div>
            <div class="font-bold">服务流程：</div>
            提交需求 -> 客服回电确认 -> 支付定金 -> 设计师出图 -> 确认后开发上线。
          </div>
        </div>

        <el-button type="primary" size="large" class="px-8" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </el-form>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Monitor, InfoFilled } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'member' })

const submitting = ref(false)
const form = ref({
  companyNameZh: '',
  domain: '',
  style: '',
  budget: '',
  benchmark: '',
  contactName: '',
  contactPhone: ''
})

const handleSubmit = () => {
  if (!form.value.companyNameZh || !form.value.contactPhone) {
    return ElMessage.warning('请补全企业名称和联系方式')
  }
  
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    ElMessage.success('需求已提交！技术团队将在24小时内与您联系')
    navigateTo('/member/orders') // 模拟跳转到订单页
  }, 1500)
}
</script>