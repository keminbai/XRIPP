<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="false" show-icon>
      <template #title>
        分成统计与结算已接真实 API。当前按“已配置合伙人 + 已配置业务线 + 真实订单金额”实时计算，结算结果按月份落库。
      </template>
    </el-alert>

    <div class="grid grid-cols-1 gap-6 md:grid-cols-4">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
      >
        <div class="mb-3 flex items-center justify-between">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="flex h-10 w-10 items-center justify-center rounded-lg" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white shadow-sm">
      <el-tabs v-model="activeTab" class="p-6">
        <el-tab-pane label="分成配置" name="config">
          <div class="space-y-4">
            <div class="mb-4 flex items-center justify-between">
              <div>
                <h3 class="text-base font-bold text-slate-800">合伙人分成比例设置</h3>
                <p class="mt-1 text-xs text-slate-500">每个合伙人一条配置，支持按业务线启停</p>
              </div>
              <div class="flex gap-2">
                <el-button @click="loadConfigData">
                  刷新
                </el-button>
                <el-button type="primary" @click="openConfigDialog()">
                  <el-icon class="mr-2"><Plus /></el-icon>新增配置
                </el-button>
              </div>
            </div>

            <el-table :data="partnerConfig" border stripe v-loading="configLoading">
              <el-table-column prop="city" label="城市" width="120" />
              <el-table-column prop="partnerName" label="合伙人" min-width="160" />
              <el-table-column label="分成比例" width="110" align="center">
                <template #default="scope">
                  <el-tag type="warning" size="small">{{ scope.row.percentage }}%</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="适用业务" min-width="220">
                <template #default="scope">
                  <div class="flex flex-wrap gap-2">
                    <el-tag
                      v-for="line in scope.row.businessLines"
                      :key="line"
                      size="small"
                      effect="plain"
                    >
                      {{ getBusinessLabel(line) }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="110" align="center">
                <template #default="scope">
                  <el-switch
                    :model-value="scope.row.enabled"
                    @change="value => handleToggleEnabled(scope.row, value)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="更新时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" @click="openConfigDialog(scope.row)">编辑</el-button>
                  <el-button link type="danger" @click="handleRemoveConfig(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="分成统计" name="stats">
          <div class="space-y-4">
            <div class="mb-4 flex items-center justify-between">
              <div>
                <h3 class="text-base font-bold text-slate-800">各合伙人分成统计</h3>
                <p class="mt-1 text-xs text-slate-500">默认展示当月收入、当月分成、累计分成与结算状态</p>
              </div>
              <el-button @click="loadProfitStats">刷新统计</el-button>
            </div>

            <el-table
              :data="profitStats"
              stripe
              v-loading="statsLoading"
              :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
            >
              <el-table-column prop="city" label="城市" width="120" />
              <el-table-column prop="partnerName" label="合伙人" min-width="150" />
              <el-table-column prop="percentage" label="分成比例" width="100" align="center">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.percentage }}%</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="本月营收" width="150" align="right">
                <template #default="scope">
                  <span class="font-medium">¥{{ formatMoney(scope.row.monthRevenue) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="本月分成" width="150" align="right">
                <template #default="scope">
                  <span class="font-bold text-green-600">¥{{ formatMoney(scope.row.monthProfit) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="累计分成" width="150" align="right">
                <template #default="scope">
                  <span class="font-medium">¥{{ formatMoney(scope.row.totalProfit) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="结算状态" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.settled ? 'success' : 'warning'" size="small">
                    {{ scope.row.settled ? '已结算' : '待结算' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                    查看明细
                  </el-button>
                  <el-button
                    v-if="!scope.row.settled"
                    link
                    type="success"
                    size="small"
                    @click="handleSettle(scope.row)"
                  >
                    结算
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="分成明细" name="records">
          <div class="space-y-4">
            <div class="mb-4 flex gap-3">
              <el-select v-model="filters.city" placeholder="选择城市" class="w-32" clearable>
                <el-option
                  v-for="city in cityOptions"
                  :key="city"
                  :label="city"
                  :value="city"
                />
              </el-select>
              <el-date-picker
                v-model="filters.dateRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                class="w-64"
              />
              <el-button type="primary" plain @click="handleSearch">查询</el-button>
            </div>

            <el-table :data="filteredRecords" stripe v-loading="recordsLoading">
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="city" label="城市" width="100" />
              <el-table-column prop="partnerName" label="合伙人" width="150" />
              <el-table-column prop="orderNo" label="订单号" width="180">
                <template #default="scope">
                  <span class="font-mono text-xs">{{ scope.row.orderNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="businessLine" label="业务类型" width="120">
                <template #default="scope">
                  <el-tag size="small">{{ getBusinessLabel(scope.row.businessLine) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="订单金额" width="120" align="right">
                <template #default="scope">
                  ¥{{ formatMoney(scope.row.orderAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="分成比例" width="100" align="center">
                <template #default="scope">
                  {{ scope.row.percentage }}%
                </template>
              </el-table-column>
              <el-table-column label="分成金额" width="120" align="right">
                <template #default="scope">
                  <span class="font-bold text-green-600">¥{{ formatMoney(scope.row.profitAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.settled ? 'success' : 'info'" size="small">
                    {{ scope.row.settled ? '已结算' : '未结算' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="configDialogVisible" :title="configForm.id ? '编辑分成配置' : '新增分成配置'" width="620px">
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="合伙人" required>
          <el-select v-model="configForm.partnerId" class="!w-full" filterable placeholder="选择合伙人">
            <el-option
              v-for="partner in partnerOptions"
              :key="partner.id"
              :label="`${partner.cityName || '未设城市'} - ${partner.partnerName}`"
              :value="partner.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input :model-value="selectedPartner?.cityName || ''" disabled />
        </el-form-item>
        <el-form-item label="分成比例" required>
          <el-input-number v-model="configForm.percentage" :min="1" :max="100" :step="1" class="!w-full" />
        </el-form-item>
        <el-form-item label="适用业务">
          <el-checkbox-group v-model="configForm.businessLines">
            <el-checkbox label="membership">会员费</el-checkbox>
            <el-checkbox label="tender">标书销售</el-checkbox>
            <el-checkbox label="training">培训收入</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="configForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="configSaving" @click="handleSaveConfigDialog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Coin, TrendCharts, Money, UserFilled, Plus } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type PartnerOption = {
  id: number
  partnerName: string
  cityName: string
}

type ProfitConfig = {
  id: number
  partnerId: number
  partnerName: string
  city: string
  percentage: number
  businessLines: string[]
  enabled: boolean
  updatedAt: string
}

type ProfitStat = {
  partnerId: number
  city: string
  partnerName: string
  percentage: number
  monthRevenue: number
  monthProfit: number
  totalProfit: number
  settled: boolean
  settlementMonth: string
}

type ProfitRecord = {
  date: string
  city: string
  partnerName: string
  orderNo: string
  businessLine: string
  orderAmount: number
  percentage: number
  profitAmount: number
  settled: boolean
}

const activeTab = ref('config')
const filters = ref({ city: '', dateRange: [] as Date[] })

const totalRevenue = ref(0)
const completedRevenue = ref(0)
const partnerCount = ref(0)

const configLoading = ref(false)
const statsLoading = ref(false)
const recordsLoading = ref(false)
const configSaving = ref(false)

const partnerOptions = ref<PartnerOption[]>([])
const partnerConfig = ref<ProfitConfig[]>([])
const profitStats = ref<ProfitStat[]>([])
const profitRecords = ref<ProfitRecord[]>([])

const configDialogVisible = ref(false)
const configForm = ref({
  id: 0,
  partnerId: null as number | null,
  percentage: 10,
  businessLines: ['membership', 'tender'] as string[],
  enabled: true
})

const stats = computed(() => [
  { label: '平台总收入', value: `¥${formatMoney(completedRevenue.value)}`, icon: Coin, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '订单总金额', value: `¥${formatMoney(totalRevenue.value)}`, icon: Money, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '合伙人数', value: String(partnerCount.value), icon: UserFilled, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '分成配置数', value: String(partnerConfig.value.length), icon: TrendCharts, bgClass: 'bg-purple-50', textClass: 'text-purple-600' }
])

const selectedPartner = computed(() =>
  partnerOptions.value.find(item => item.id === configForm.value.partnerId) || null
)

const cityOptions = computed(() => {
  const set = new Set<string>()
  partnerConfig.value.forEach(item => {
    if (item.city) set.add(item.city)
  })
  return Array.from(set)
})

const filteredRecords = computed(() => profitRecords.value)

const getBusinessLabel = (line: string) => {
  const map: Record<string, string> = {
    membership: '会员费',
    tender: '标书销售',
    training: '培训收入'
  }
  return map[line] || line || '未知'
}

const formatMoney = (value: unknown) => {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) ? amount.toLocaleString() : '0'
}

const monthValue = (date: Date | string | null | undefined) => {
  if (!date) return ''
  const d = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const normalizeNumber = (value: unknown) => {
  const num = Number(value ?? 0)
  return Number.isFinite(num) ? num : 0
}

const loadRealStats = async () => {
  await Promise.allSettled([
    apiRequest('/v3/admin/orders/stats').then((res: any) => {
      const data = res?.data || {}
      totalRevenue.value = normalizeNumber(data?.totalAmount)
      completedRevenue.value =
        normalizeNumber(data?.inProgressAmount) +
        normalizeNumber(data?.completedAmount)
    }),
    apiRequest('/v3/admin/partners', { query: { page: 1, page_size: 200 } }).then((res: any) => {
      const data = res?.data
      partnerCount.value = normalizeNumber(data?.total)
      partnerOptions.value = Array.isArray(data?.items)
        ? data.items.map((item: any) => ({
            id: normalizeNumber(item.id),
            partnerName: item.partnerName || '',
            cityName: item.cityName || ''
          }))
        : []
    })
  ])
}

const loadConfigData = async () => {
  configLoading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/profit-sharing/configs')
    partnerConfig.value = Array.isArray(res?.data)
      ? res.data.map((item: any) => ({
          id: normalizeNumber(item.id),
          partnerId: normalizeNumber(item.partnerId),
          partnerName: item.partnerName || '',
          city: item.city || '',
          percentage: normalizeNumber(item.percentage),
          businessLines: Array.isArray(item.businessLines) ? item.businessLines : [],
          enabled: Boolean(item.enabled),
          updatedAt: item.updatedAt || ''
        }))
      : []
  } finally {
    configLoading.value = false
  }
}

const loadProfitStats = async () => {
  statsLoading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/profit-sharing/stats')
    profitStats.value = Array.isArray(res?.data)
      ? res.data.map((item: any) => ({
          partnerId: normalizeNumber(item.partnerId),
          city: item.city || '',
          partnerName: item.partnerName || '',
          percentage: normalizeNumber(item.percentage),
          monthRevenue: normalizeNumber(item.monthRevenue),
          monthProfit: normalizeNumber(item.monthProfit),
          totalProfit: normalizeNumber(item.totalProfit),
          settled: Boolean(item.settled),
          settlementMonth: item.settlementMonth || monthValue(new Date())
        }))
      : []
  } finally {
    statsLoading.value = false
  }
}

const loadProfitRecords = async () => {
  recordsLoading.value = true
  try {
    const query: Record<string, string> = {}
    if (filters.value.city) {
      query.city = filters.value.city
    }
    if (filters.value.dateRange.length === 2) {
      query.month_from = monthValue(filters.value.dateRange[0])
      query.month_to = monthValue(filters.value.dateRange[1])
    }
    const res: any = await apiRequest('/v3/admin/profit-sharing/records', { query })
    profitRecords.value = Array.isArray(res?.data)
      ? res.data.map((item: any) => ({
          date: item.date || '',
          city: item.city || '',
          partnerName: item.partnerName || '',
          orderNo: item.orderNo || '',
          businessLine: item.businessLine || '',
          orderAmount: normalizeNumber(item.orderAmount),
          percentage: normalizeNumber(item.percentage),
          profitAmount: normalizeNumber(item.profitAmount),
          settled: Boolean(item.settled)
        }))
      : []
  } finally {
    recordsLoading.value = false
  }
}

const loadAll = async () => {
  await loadRealStats()
  await Promise.all([loadConfigData(), loadProfitStats(), loadProfitRecords()])
}

const openConfigDialog = (row?: ProfitConfig) => {
  configForm.value = row
    ? {
        id: row.id,
        partnerId: row.partnerId,
        percentage: row.percentage,
        businessLines: [...row.businessLines],
        enabled: row.enabled
      }
    : {
        id: 0,
        partnerId: null,
        percentage: 10,
        businessLines: ['membership', 'tender'],
        enabled: true
      }
  configDialogVisible.value = true
}

const handleSaveConfigDialog = async () => {
  if (!configForm.value.partnerId) {
    ElMessage.warning('请选择合伙人')
    return
  }
  if (!configForm.value.businessLines.length) {
    ElMessage.warning('请至少选择一个适用业务')
    return
  }

  configSaving.value = true
  try {
    const payload = {
      partner_id: configForm.value.partnerId,
      percentage: configForm.value.percentage,
      business_lines: configForm.value.businessLines,
      enabled: configForm.value.enabled
    }
    if (configForm.value.id) {
      await apiRequest(`/v3/admin/profit-sharing/configs/${configForm.value.id}`, {
        method: 'PUT',
        body: payload
      })
      ElMessage.success('分成配置已更新')
    } else {
      await apiRequest('/v3/admin/profit-sharing/configs', {
        method: 'POST',
        body: payload
      })
      ElMessage.success('分成配置已创建')
    }
    configDialogVisible.value = false
    await Promise.all([loadConfigData(), loadProfitStats(), loadProfitRecords()])
  } finally {
    configSaving.value = false
  }
}

const handleToggleEnabled = async (row: ProfitConfig, enabled: boolean) => {
  await apiRequest(`/v3/admin/profit-sharing/configs/${row.id}`, {
    method: 'PUT',
    body: {
      partner_id: row.partnerId,
      percentage: row.percentage,
      business_lines: row.businessLines,
      enabled
    }
  })
  ElMessage.success(enabled ? '配置已启用' : '配置已停用')
  await Promise.all([loadConfigData(), loadProfitStats(), loadProfitRecords()])
}

const handleRemoveConfig = async (row: ProfitConfig) => {
  await ElMessageBox.confirm(`确定删除 ${row.city || '未设城市'} - ${row.partnerName} 的分成配置吗？`, '提示', {
    type: 'warning'
  })
  await apiRequest(`/v3/admin/profit-sharing/configs/${row.id}`, { method: 'DELETE' })
  ElMessage.success('配置已删除')
  await Promise.all([loadConfigData(), loadProfitStats(), loadProfitRecords()])
}

const handleViewDetail = async (row: ProfitStat) => {
  activeTab.value = 'records'
  filters.value.city = row.city
  await loadProfitRecords()
}

const handleSearch = async () => {
  await loadProfitRecords()
}

const handleSettle = async (row: ProfitStat) => {
  await ElMessageBox.confirm(
    `确认结算 ${row.city} ${row.partnerName} ${row.settlementMonth} 的分成（¥${formatMoney(row.monthProfit)}）吗？`,
    '确认结算',
    { type: 'success' }
  )

  await apiRequest('/v3/admin/profit-sharing/settlements', {
    method: 'POST',
    body: {
      partner_id: row.partnerId,
      settlement_month: row.settlementMonth
    }
  })
  ElMessage.success('结算已完成')
  await Promise.all([loadProfitStats(), loadProfitRecords()])
}

onMounted(async () => {
  await loadAll()
})
</script>
