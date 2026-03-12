<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\business\packages.vue
  版本: V1.2 修复版 (2026-02-13)
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        业务配置模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
            <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
          </div>
          <div class="p-3 rounded-lg" :class="stat.bgClass">
            <el-icon class="text-xl" :class="stat.textClass">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 套餐配置 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        v-for="pkg in packages"
        :key="pkg.code"
        class="bg-white rounded-xl border-2 shadow-sm overflow-hidden hover:shadow-lg transition-all"
        :class="pkg.featured ? 'border-blue-500' : 'border-slate-200'"
      >
        <!-- 卡片头 -->
        <div class="p-4 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-slate-100 flex justify-between items-center">
          <div>
            <div class="font-bold text-slate-800 text-lg">{{ pkg.name }}</div>
            <div class="text-xs text-slate-500 mt-1">{{ pkg.code }}</div>
          </div>
          <div class="flex flex-col items-end gap-1">
            <el-tag :type="pkg.status === 'active' ? 'success' : 'info'" size="small">
              {{ pkg.status === 'active' ? '启用中' : '已停用' }}
            </el-tag>
            <el-tag v-if="pkg.featured" type="danger" size="small">推荐</el-tag>
          </div>
        </div>

        <!-- 权益配置（统一模型） -->
        <div class="p-6 space-y-4">
          <div
            v-for="(item, idx) in benefitPolicy"
            :key="item.type"
            class="flex justify-between items-center text-sm pb-3"
            :class="idx < benefitPolicy.length - 1 ? 'border-b border-slate-100' : ''"
          >
            <span class="text-slate-600 font-medium">{{ item.label }}</span>
            <div class="flex items-center gap-2">
              <el-input-number
                v-model="pkg.rights[item.type]"
                size="small"
                :min="0"
                :max="item.maxPerGrant"
                class="!w-24"
              />
              <span class="text-slate-400 text-xs">{{ item.unit }}</span>
            </div>
          </div>
        </div>

        <!-- 操作 -->
        <div class="p-4 bg-slate-50/50 border-t border-slate-100 flex gap-2">
          <el-button type="primary" class="flex-1" @click="handleSave(pkg)">
            <el-icon class="mr-1"><Check /></el-icon>
            保存策略
          </el-button>
          <el-button
            :type="pkg.status === 'active' ? 'danger' : 'success'"
            plain
            @click="toggleStatus(pkg)"
          >
            {{ pkg.status === 'active' ? '停用' : '启用' }}
          </el-button>
        </div>

        <div class="px-4 pb-3 text-xs text-slate-400 text-center">
          最后更新：{{ pkg.lastUpdate }}
        </div>
      </div>
    </div>

    <!-- 权益对比 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <h4 class="font-bold text-slate-700 mb-4">权益对比预览</h4>
      <el-table
        :data="comparisonData"
        border
        stripe
        :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
      >
        <el-table-column prop="feature" label="权益项目" width="220" />
        <el-table-column prop="normal" label="普通会员" align="center" />
        <el-table-column prop="vip" label="VIP会员" align="center" />
        <el-table-column prop="svip" label="SVIP会员" align="center" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Check, Box, Medal, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  benefitPolicy,
  type BenefitType,
  type MemberLevel
} from '@/composables/useBenefitPolicy'

definePageMeta({ layout: 'admin' })

type Rights = Record<BenefitType, number>

interface PackageItem {
  name: string
  code: MemberLevel
  status: 'active' | 'inactive'
  featured: boolean
  lastUpdate: string
  rights: Rights
}

function buildRights(level: MemberLevel): Rights {
  return benefitPolicy.reduce((acc, item) => {
    acc[item.type] = item.defaultByLevel[level]
    return acc
  }, {} as Rights)
}

const packages = ref<PackageItem[]>([
  {
    name: '普通会员',
    code: 'NORMAL',
    status: 'active',
    featured: false,
    lastUpdate: '2026-02-13',
    rights: buildRights('NORMAL')
  },
  {
    name: 'VIP会员',
    code: 'VIP',
    status: 'active',
    featured: true,
    lastUpdate: '2026-02-13',
    rights: buildRights('VIP')
  },
  {
    name: 'SVIP会员',
    code: 'SVIP',
    status: 'active',
    featured: false,
    lastUpdate: '2026-02-13',
    rights: buildRights('SVIP')
  }
])

const stats = computed(() => {
  const activeCount = packages.value.filter(p => p.status === 'active').length
  return [
    { label: '启用套餐数', value: String(activeCount), icon: Box, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '当前会员总数', value: '1,234', icon: Medal, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: 'SVIP会员数', value: '89', icon: Trophy, bgClass: 'bg-yellow-50', textClass: 'text-yellow-600' }
  ]
})

const packageMap = computed(() => {
  const map = {} as Record<MemberLevel, PackageItem>
  for (const pkg of packages.value) map[pkg.code] = pkg
  return map
})

const comparisonData = computed(() => {
  return benefitPolicy.map(item => ({
    feature: item.label,
    normal: `${packageMap.value.NORMAL.rights[item.type]}${item.unit}`,
    vip: `${packageMap.value.VIP.rights[item.type]}${item.unit}`,
    svip: `${packageMap.value.SVIP.rights[item.type]}${item.unit}`
  }))
})

const handleSave = (pkg: PackageItem) => {
  pkg.lastUpdate = new Date().toISOString().slice(0, 10)
  ElMessage.success(`${pkg.name} 权益策略已更新`)
}

const toggleStatus = (pkg: PackageItem) => {
  pkg.status = pkg.status === 'active' ? 'inactive' : 'active'
  ElMessage.warning(`${pkg.name} 已${pkg.status === 'active' ? '启用' : '停用'}`)
}
</script>
