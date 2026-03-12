<!--
  文件路径: D:\ipp-platform\app\pages\media\[id].vue
  版本: V2.0 API对接版 (2026-03-12)

  修复: 移除硬编码文章数组，对接 /v3/contents/{id} 公共API
-->
<template>
  <div class="bg-white min-h-screen">
    <div class="max-w-5xl mx-auto px-4 py-10">
      <div class="mb-6 flex items-center justify-between">
        <button
          class="px-4 py-2 rounded-lg border border-slate-200 text-slate-600 text-sm hover:bg-slate-50"
          @click="navigateTo('/services#media')"
        >
          返回媒体中心
        </button>
        <button
          class="px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-bold hover:bg-brand-700"
          @click="navigateTo('/member/feedback')"
        >
          咨询客服
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-20">
        <div class="text-slate-400 text-lg">正在加载文章...</div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="text-center py-20">
        <div class="text-xl text-red-500 mb-4">{{ error }}</div>
        <button
          class="px-6 py-2 rounded-lg bg-brand-600 text-white text-sm font-bold hover:bg-brand-700"
          @click="loadArticle"
        >
          重新加载
        </button>
      </div>

      <!-- 文章内容 -->
      <div v-else-if="article" class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="p-8">
          <div class="flex items-center gap-3 text-xs text-slate-400 mb-3">
            <span class="px-2 py-1 bg-slate-100 border border-slate-200 rounded text-slate-600 font-medium">
              {{ article.contentType === 'media' ? '新闻' : article.contentType || '文章' }}
            </span>
            <span>{{ article.publishDate }}</span>
          </div>

          <h1 class="text-3xl font-bold text-slate-900 mb-4">{{ article.title }}</h1>

          <div v-if="article.summary" class="text-slate-500 text-sm mb-6 pb-4 border-b border-slate-100">
            {{ article.summary }}
          </div>

          <div class="text-slate-600 leading-8 whitespace-pre-line">{{ article.body }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: false })

const route = useRoute()
const id = route.params.id

const article = ref<any>(null)
const loading = ref(false)
const error = ref('')

const loadArticle = async () => {
  loading.value = true
  error.value = ''

  try {
    const res: any = await apiRequest(`/v3/contents/${id}`)
    if (res?.data) {
      article.value = res.data
    } else {
      error.value = '文章不存在或已下架'
    }
  } catch (e: any) {
    error.value = e?.message || '加载文章失败'
  } finally {
    loading.value = false
  }
}

useHead({
  title: () => article.value?.title ? `${article.value.title} - XRIPP媒体中心` : '媒体中心 - XRIPP'
})

onMounted(() => {
  loadArticle()
})
</script>
