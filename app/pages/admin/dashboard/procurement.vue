<!--
  文件路径: D:\ipp-platform\app\pages\admin\dashboard\procurement.vue
  版本: V2.0 API对接版 (2026-03-12)

  修复: 移除纯手工录入模式，改为从 /v3/admin/tenders/stats + /v3/admin/tenders 读取真实数据
-->
<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- 最新标书列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-lg font-bold text-slate-800">采购标书数据</h3>
            <p class="text-xs text-slate-500 mt-1">最新采购标书信息</p>
          </div>
          <el-button type="primary" plain @click="navigateTo('/admin/tenders/publish')">
            发布新标书
          </el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="tenderList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}" v-loading="loading">
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column prop="title" label="标书标题" min-width="300" show-overflow-tooltip />
          <el-table-column prop="organization" label="采购机构" width="200" show-overflow-tooltip />
          <el-table-column prop="country" label="国家" width="120" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.tenderStatus === 'published' ? 'success' : scope.row.tenderStatus === 'draft' ? 'info' : 'warning'" size="small">
                {{ statusLabel(scope.row.tenderStatus || scope.row.tender_status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="截止日期" width="120">
            <template #default="scope">{{ (scope.row.deadline || '').slice(0, 10) }}</template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            background layout="total, prev, pager, next"
            :total="totalItems" :page-size="pageSize"
            @current-change="loadTenders"
          />
        </div>
      </div>
    </div>

    <!-- 状态分布图 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="text-base font-bold text-slate-800 mb-4">标书状态分布</h3>
      <div class="h-64">
        <ClientOnly>
          <AppChart :options="statusChartOptions" />
        </ClientOnly>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Document, TrendCharts, DataLine, Files } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const loading = ref(false)
const tenderList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const tenderStats = ref<any>({})

const statusLabel = (s: string) => {
  const map: Record<string, string> = { published: '已发布', draft: '草稿', archived: '已归档' }
  return map[s] || s || '未知'
}

const stats = computed(() => {
  const data = tenderStats.value
  const published = Number(data.published || 0)
  const draft = Number(data.draft || 0)
  const archived = Number(data.archived || 0)
  const total = published + draft + archived

  return [
    { label: '标书总量', value: total.toLocaleString(), icon: Document, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '已发布', value: String(published), icon: TrendCharts, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '草稿', value: String(draft), icon: Files, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已归档', value: String(archived), icon: DataLine, bgClass: 'bg-slate-50', textClass: 'text-slate-600' }
  ]
})

const statusChartOptions = computed(() => {
  const data = tenderStats.value
  const items = [
    { name: '已发布', value: Number(data.published || 0) },
    { name: '草稿', value: Number(data.draft || 0) },
    { name: '已归档', value: Number(data.archived || 0) }
  ].filter(i => i.value > 0)

  return {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: items.length ? items : [{ name: '无数据', value: 1 }],
      label: { formatter: '{b}: {c} ({d}%)' }
    }],
    color: ['#22c55e', '#f97316', '#94a3b8']
  }
})

const loadTenders = async () => {
  loading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/tenders', {
      query: { page: currentPage.value, page_size: pageSize.value }
    })
    tenderList.value = Array.isArray(res?.data?.items) ? res.data.items : []
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    tenderList.value = []
    ElMessage.error(e?.message || '加载标书失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res: any = await apiRequest('/v3/admin/tenders/stats')
    tenderStats.value = res?.data || {}
  } catch { tenderStats.value = {} }
}

onMounted(() => {
  loadTenders()
  loadStats()
})
</script>
