<!--
  文件路径: D:\ipp-platform\app\pages\admin\dashboard\member.vue
  版本: V2.0 API对接版 (2026-03-12)

  修复: 移除手工录入模式，改为从 /v3/admin/members 读取真实数据并展示统计
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

    <!-- 会员列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">会员数据概览</h3>
            <p class="text-xs text-slate-500 mt-1">平台注册会员信息</p>
          </div>
          <el-button type="primary" plain @click="navigateTo('/admin/members/list')">管理会员</el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="filters.level" placeholder="会员等级" class="w-40" clearable @change="loadMembers">
            <el-option label="SVIP" value="SVIP" />
            <el-option label="VIP" value="VIP" />
            <el-option label="普通" value="NORMAL" />
          </el-select>
          <el-input v-model="filters.keyword" placeholder="搜索..." class="w-56" clearable @keyup.enter="loadMembers" />
          <el-button type="primary" plain @click="loadMembers">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="memberList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}" v-loading="loading">
          <el-table-column type="index" label="#" width="60" align="center" />
          <el-table-column label="用户名" width="150">
            <template #default="scope">{{ scope.row.username || '-' }}</template>
          </el-table-column>
          <el-table-column label="公司名称" min-width="200">
            <template #default="scope">{{ scope.row.companyName || scope.row.company_name || '-' }}</template>
          </el-table-column>
          <el-table-column label="等级" width="100" align="center">
            <template #default="scope">
              <el-tag :type="(scope.row.memberLevel || scope.row.member_level) === 'SVIP' ? 'danger' : (scope.row.memberLevel || scope.row.member_level) === 'VIP' ? 'warning' : 'info'" size="small">
                {{ scope.row.memberLevel || scope.row.member_level || 'NORMAL' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="行业" width="150">
            <template #default="scope">{{ scope.row.industry || '-' }}</template>
          </el-table-column>
          <el-table-column label="注册时间" width="120">
            <template #default="scope">{{ (scope.row.createdAt || scope.row.created_at || '').slice(0, 10) }}</template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            background layout="total, prev, pager, next"
            :total="totalItems" :page-size="pageSize"
            @current-change="loadMembers"
          />
        </div>
      </div>
    </div>

    <!-- 等级分布图 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="text-base font-bold text-slate-800 mb-4">会员等级分布</h3>
      <div class="h-64">
        <ClientOnly>
          <AppChart :options="levelChartOptions" />
        </ClientOnly>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Trophy, TrendCharts, UserFilled } from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const loading = ref(false)
const memberList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)
const filters = ref({ level: '', keyword: '' })

const stats = computed(() => {
  const total = totalItems.value
  const svip = memberList.value.filter(m => (m.memberLevel || m.member_level) === 'SVIP').length
  const vip = memberList.value.filter(m => (m.memberLevel || m.member_level) === 'VIP').length
  const normal = memberList.value.filter(m => (m.memberLevel || m.member_level) === 'NORMAL').length

  return [
    { label: '会员总数', value: String(total), icon: User, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: 'SVIP', value: String(svip), icon: Trophy, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: 'VIP', value: String(vip), icon: TrendCharts, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '普通会员', value: String(normal), icon: UserFilled, bgClass: 'bg-slate-50', textClass: 'text-slate-600' }
  ]
})

const levelChartOptions = computed(() => {
  const levelMap: Record<string, number> = {}
  memberList.value.forEach(m => {
    const lvl = m.memberLevel || m.member_level || 'NORMAL'
    levelMap[lvl] = (levelMap[lvl] || 0) + 1
  })
  const data = Object.entries(levelMap).map(([name, value]) => ({ name, value }))

  return {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.length ? data : [{ name: '无数据', value: 1 }],
      label: { formatter: '{b}: {c} ({d}%)' }
    }],
    color: ['#f97316', '#22c55e', '#94a3b8']
  }
})

const loadMembers = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.level) query.member_level = filters.value.level
    if (filters.value.keyword?.trim()) query.keyword = filters.value.keyword.trim()

    const res: any = await apiRequest('/v3/admin/members', { query })
    memberList.value = Array.isArray(res?.data?.items) ? res.data.items : []
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    memberList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '加载会员数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadMembers() })
</script>
