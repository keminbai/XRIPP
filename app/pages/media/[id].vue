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

      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <img :src="article.image" class="w-full h-[320px] object-cover" :alt="article.title" />
        <div class="p-8">
          <div class="flex items-center gap-3 text-xs text-slate-400 mb-3">
            <span class="px-2 py-1 bg-slate-100 border border-slate-200 rounded text-slate-600 font-medium">
              {{ article.tag }}
            </span>
            <span>{{ article.date }}</span>
            <span>浏览 {{ article.views }}</span>
          </div>

          <h1 class="text-3xl font-bold text-slate-900 mb-4">{{ article.title }}</h1>
          <p class="text-slate-600 leading-8 whitespace-pre-line">{{ article.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })

import { computed } from 'vue'

const route = useRoute()
const id = Number(route.params.id || 1)

const articles = [
  {
    id: 1,
    tag: '成功案例',
    date: '2025-01-15',
    views: '1.2k',
    title: 'XRIPP平台助力上海企业中标联合国$2.5M医疗设备项目',
    image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=1200&q=80',
    content:
      '该项目历时3个月完成投标准备，涵盖资质核验、技术响应、商务报价与合规复核。\\n\\n平台联合顾问团队对关键条款逐条校对，最终帮助企业成功中标。'
  },
  {
    id: 2,
    tag: '政策解读',
    date: '2025-01-10',
    views: '856',
    title: '2025年联合国采购政策重大调整解读',
    image: 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=1200&q=80',
    content:
      '本次政策调整重点涉及供应商准入、可持续采购要求与合同执行评估机制。\\n\\n建议企业优先补齐合规材料并更新组织级管理流程。'
  },
  {
    id: 3,
    tag: '战略合作',
    date: '2025-01-05',
    views: '643',
    title: 'XRIPP与马来西亚能源部签署战略合作协议',
    image: 'https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=1200&q=80',
    content:
      '双方将在能源基础设施、供应链协同与项目落地服务方面开展长期合作。\\n\\n后续将联合发布区域项目机会清单与对接计划。'
  }
]

const article = computed(() => articles.find(a => a.id === id) || articles[0])
</script>
