<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        会员套餐配置已接入真实 API，保存后会持久化到后台配置中心。
      </template>
    </el-alert>

    <div class="flex justify-end">
      <el-button :loading="loading" @click="loadPackages">刷新配置</el-button>
    </div>

    <div class="grid grid-cols-1 gap-6 md:grid-cols-4">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <div>
            <div class="mb-1 text-sm text-slate-500">{{ stat.label }}</div>
            <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
          </div>
          <div class="rounded-lg p-3" :class="stat.bgClass">
            <el-icon class="text-xl" :class="stat.textClass">
              <component :is="stat.icon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="grid grid-cols-1 gap-6 md:grid-cols-3">
      <div
        v-for="pkg in packages"
        :key="pkg.code"
        class="overflow-hidden rounded-xl border-2 bg-white shadow-sm transition-all hover:shadow-lg"
        :class="pkg.featured ? 'border-blue-500' : 'border-slate-200'"
      >
        <div class="flex items-center justify-between border-b border-slate-100 bg-gradient-to-r from-slate-50 to-slate-100 p-4">
          <div>
            <div class="text-lg font-bold text-slate-800">{{ pkg.name }}</div>
            <div class="mt-1 text-xs text-slate-500">{{ pkg.code }}</div>
          </div>
          <div class="flex flex-col items-end gap-1">
            <el-tag :type="pkg.status === 'active' ? 'success' : 'info'" size="small">
              {{ pkg.status === 'active' ? '启用中' : '已停用' }}
            </el-tag>
            <el-tag v-if="pkg.featured" type="danger" size="small">推荐</el-tag>
          </div>
        </div>

        <div class="space-y-4 p-6">
          <div
            v-for="(item, idx) in benefitPolicy"
            :key="item.type"
            class="flex items-center justify-between pb-3 text-sm"
            :class="idx < benefitPolicy.length - 1 ? 'border-b border-slate-100' : ''"
          >
            <span class="font-medium text-slate-600">{{ item.label }}</span>
            <div class="flex items-center gap-2">
              <el-input-number
                v-model="pkg.rights[item.type]"
                size="small"
                :min="0"
                :max="item.maxPerGrant"
                class="!w-24"
              />
              <span class="text-xs text-slate-400">{{ item.unit }}</span>
            </div>
          </div>
        </div>

        <div class="flex gap-2 border-t border-slate-100 bg-slate-50/50 p-4">
          <el-button type="primary" class="flex-1" :loading="saving" @click="handleSave(pkg)">
            <el-icon class="mr-1"><Check /></el-icon>
            保存策略
          </el-button>
          <el-button
            :type="pkg.status === 'active' ? 'danger' : 'success'"
            plain
            :loading="saving"
            @click="toggleStatus(pkg)"
          >
            {{ pkg.status === 'active' ? '停用' : '启用' }}
          </el-button>
        </div>

        <div class="px-4 pb-3 text-center text-xs text-slate-400">
          最后更新：{{ pkg.lastUpdate }}
        </div>
      </div>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h4 class="mb-4 font-bold text-slate-700">权益对比预览</h4>
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
import { computed, onMounted, ref } from 'vue'
import { Check, Box, Medal, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  benefitPolicy,
  type BenefitType,
  type MemberLevel
} from '@/composables/useBenefitPolicy'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'

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

const NAMESPACE = 'business_packages'
const CONFIG_META = {
  packages: { name: '会员套餐配置', sortOrder: 10 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

function buildRights(level: MemberLevel): Rights {
  return benefitPolicy.reduce((acc, item) => {
    acc[item.type] = item.defaultByLevel[level]
    return acc
  }, {} as Rights)
}

function todayLabel() {
  return new Date().toISOString().slice(0, 10)
}

function createDefaultPackages(): PackageItem[] {
  return [
    {
      name: '普通会员',
      code: 'NORMAL',
      status: 'active',
      featured: false,
      lastUpdate: todayLabel(),
      rights: buildRights('NORMAL')
    },
    {
      name: 'VIP会员',
      code: 'VIP',
      status: 'active',
      featured: true,
      lastUpdate: todayLabel(),
      rights: buildRights('VIP')
    },
    {
      name: 'SVIP会员',
      code: 'SVIP',
      status: 'active',
      featured: false,
      lastUpdate: todayLabel(),
      rights: buildRights('SVIP')
    }
  ]
}

const packages = ref<PackageItem[]>(createDefaultPackages())

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeRights = (value: any, level: MemberLevel): Rights => {
  const defaults = buildRights(level)
  for (const item of benefitPolicy) {
    defaults[item.type] = normalizeNumber(value?.[item.type], defaults[item.type])
  }
  return defaults
}

const normalizePackages = (value: unknown): PackageItem[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return createDefaultPackages()
  }
  return value.map((item: any, index) => {
    const level = ['NORMAL', 'VIP', 'SVIP'].includes(item?.code) ? item.code as MemberLevel : createDefaultPackages()[index]?.code || 'NORMAL'
    return {
      name: item?.name || level,
      code: level,
      status: item?.status === 'inactive' ? 'inactive' : 'active',
      featured: Boolean(item?.featured),
      lastUpdate: item?.lastUpdate || todayLabel(),
      rights: normalizeRights(item?.rights, level)
    }
  })
}

const stats = computed(() => {
  const activeCount = packages.value.filter(item => item.status === 'active').length
  const featuredCount = packages.value.filter(item => item.featured).length
  const configuredBenefits = packages.value.reduce((sum, item) => {
    return sum + benefitPolicy.filter(policy => (item.rights[policy.type] || 0) > 0).length
  }, 0)
  const maxTenderDownload = Math.max(...packages.value.map(item => item.rights.tender_download || 0))

  return [
    { label: '启用套餐数', value: String(activeCount), icon: Box, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '推荐套餐数', value: String(featuredCount), icon: Trophy, bgClass: 'bg-red-50', textClass: 'text-red-600' },
    { label: '已配置权益项', value: String(configuredBenefits), icon: Medal, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '最高标书下载额度', value: `${maxTenderDownload}份`, icon: Check, bgClass: 'bg-yellow-50', textClass: 'text-yellow-600' }
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
    normal: `${packageMap.value.NORMAL?.rights[item.type] || 0}${item.unit}`,
    vip: `${packageMap.value.VIP?.rights[item.type] || 0}${item.unit}`,
    svip: `${packageMap.value.SVIP?.rights[item.type] || 0}${item.unit}`
  }))
})

const persistPackages = async (successMessage: string) => {
  try {
    await saveNamespaceItems([
      {
        key: 'packages',
        value: packages.value
      }
    ])
    ElMessage.success(successMessage)
    await loadPackages()
  } catch (error: any) {
    ElMessage.error(error?.message || '会员套餐配置保存失败')
  }
}

const loadPackages = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    packages.value = normalizePackages(itemMap.packages)
  } catch (error: any) {
    ElMessage.error(error?.message || '会员套餐配置加载失败')
  }
}

const handleSave = async (pkg: PackageItem) => {
  pkg.lastUpdate = todayLabel()
  await persistPackages(`${pkg.name} 权益策略已更新`)
}

const toggleStatus = async (pkg: PackageItem) => {
  pkg.status = pkg.status === 'active' ? 'inactive' : 'active'
  pkg.lastUpdate = todayLabel()
  await persistPackages(`${pkg.name} 已${pkg.status === 'active' ? '启用' : '停用'}`)
}

onMounted(async () => {
  await loadPackages()
})
</script>
