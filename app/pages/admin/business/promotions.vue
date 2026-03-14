<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        营销促销规则已接入真实 API，配置修改会持久化到后台配置中心。
      </template>
    </el-alert>

    <el-alert
      title="此处配置营销活动与折扣规则，标准定价请前往「财务配置-定价策略」"
      type="info"
      :closable="false"
      show-icon
    />

    <div class="flex justify-end">
      <el-button :loading="loading" @click="loadRules">刷新配置</el-button>
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
            <el-icon class="text-xl" :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-slate-800">营销促销规则</h3>
          <p class="mt-1 text-xs text-slate-500">设置限时优惠、组合折扣及新用户福利</p>
        </div>
        <el-button type="primary" @click="openRuleDialog()">
          <el-icon class="mr-2"><Plus /></el-icon>
          新增规则
        </el-button>
      </div>

      <el-tabs v-model="activeTab" class="mb-4">
        <el-tab-pane label="进行中" name="active" />
        <el-tab-pane label="已结束" name="ended" />
        <el-tab-pane label="草稿箱" name="draft" />
      </el-tabs>

      <el-table
        :data="filteredRules"
        stripe
        border
        :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
      >
        <el-table-column prop="name" label="规则名称" width="200">
          <template #default="scope">
            <div class="font-medium text-slate-800">{{ scope.row.name }}</div>
            <div class="mt-1 text-xs text-slate-400">ID: {{ scope.row.id }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="促销类型" width="140">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="规则详情" min-width="280">
          <template #default="scope">
            <div class="text-sm">
              <span v-if="scope.row.type === 'discount'">
                {{ scope.row.target }} <span class="font-bold text-red-500">{{ scope.row.discount }}折</span>
              </span>
              <span v-else-if="scope.row.type === 'reduce'">
                满 <span class="font-bold">¥{{ scope.row.threshold }}</span> 减 <span class="font-bold text-red-500">¥{{ scope.row.reduce }}</span>
              </span>
              <span v-else-if="scope.row.type === 'gift'">
                购买 {{ scope.row.mainProduct }} 赠送 <span class="font-bold text-green-600">{{ scope.row.giftProduct }}</span>
              </span>
              <span v-else>
                {{ scope.row.description }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="有效期" width="250">
          <template #default="scope">
            <div class="text-xs">
              <div>开始: {{ scope.row.startDate }}</div>
              <div class="mt-1">结束: {{ scope.row.endDate }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="100" align="center">
          <template #default="scope">
            <el-tag size="small" :type="getPriorityTag(scope.row.priority)">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" :loading="saving" @change="handleToggleStatus(scope.row)" />
          </template>
        </el-table-column>

        <el-table-column label="使用次数" width="100" align="center">
          <template #default="scope">
            <span class="font-bold text-blue-600">{{ scope.row.usageCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openRuleDialog(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleDuplicate(scope.row)">复制</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="ruleForm.id ? '编辑规则' : '新增规则'" width="640px">
      <el-form :model="ruleForm" label-width="120px">
        <el-form-item label="规则名称" required>
          <el-input v-model="ruleForm.name" />
        </el-form-item>
        <el-form-item label="促销类型" required>
          <el-select v-model="ruleForm.type" class="w-full">
            <el-option label="限时折扣" value="discount" />
            <el-option label="满减优惠" value="reduce" />
            <el-option label="买赠活动" value="gift" />
            <el-option label="其他" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标商品" v-if="ruleForm.type === 'discount'">
          <el-input v-model="ruleForm.target" placeholder="如：SVIP会员年费" />
        </el-form-item>
        <el-form-item label="折扣力度" v-if="ruleForm.type === 'discount'">
          <el-input-number v-model="ruleForm.discount" :min="0.1" :max="10" :step="0.1" class="!w-full" />
        </el-form-item>
        <el-form-item label="满减门槛" v-if="ruleForm.type === 'reduce'">
          <el-input-number v-model="ruleForm.threshold" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="减免金额" v-if="ruleForm.type === 'reduce'">
          <el-input-number v-model="ruleForm.reduce" :min="0" :step="100" class="!w-full" />
        </el-form-item>
        <el-form-item label="主商品" v-if="ruleForm.type === 'gift'">
          <el-input v-model="ruleForm.mainProduct" />
        </el-form-item>
        <el-form-item label="赠品" v-if="ruleForm.type === 'gift'">
          <el-input v-model="ruleForm.giftProduct" />
        </el-form-item>
        <el-form-item label="规则说明" v-if="ruleForm.type === 'custom'">
          <el-input v-model="ruleForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始日期" required>
          <el-date-picker v-model="ruleForm.startDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
        </el-form-item>
        <el-form-item label="结束日期" required>
          <el-date-picker v-model="ruleForm.endDate" type="date" value-format="YYYY-MM-DD" class="!w-full" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="ruleForm.priority" class="w-full">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则状态">
          <el-select v-model="ruleForm.status" class="w-full">
            <el-option label="进行中" value="active" />
            <el-option label="已结束" value="ended" />
            <el-option label="草稿箱" value="draft" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
        <el-form-item label="使用次数">
          <el-input-number v-model="ruleForm.usageCount" :min="0" :step="1" class="!w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Plus, Trophy, TrendCharts, Timer, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'

definePageMeta({ layout: 'admin' })

type RuleType = 'discount' | 'reduce' | 'gift' | 'custom'
type RuleStatus = 'active' | 'ended' | 'draft'

interface PromotionRule {
  id: number
  name: string
  type: RuleType
  target: string
  discount: number
  threshold: number
  reduce: number
  mainProduct: string
  giftProduct: string
  description: string
  startDate: string
  endDate: string
  enabled: boolean
  priority: '高' | '中' | '低'
  usageCount: number
  status: RuleStatus
}

const NAMESPACE = 'business_promotions'
const CONFIG_META = {
  promotion_rules: { name: '营销促销规则', sortOrder: 10 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

const activeTab = ref<RuleStatus>('active')
const dialogVisible = ref(false)

const createDefaultRules = (): PromotionRule[] => ([
  {
    id: 1,
    name: '新春特惠',
    type: 'discount',
    target: 'SVIP会员年费',
    discount: 8.5,
    threshold: 0,
    reduce: 0,
    mainProduct: '',
    giftProduct: '',
    description: '',
    startDate: '2026-02-01',
    endDate: '2026-02-28',
    enabled: true,
    priority: '高',
    usageCount: 23,
    status: 'active'
  },
  {
    id: 2,
    name: '企业团购优惠',
    type: 'reduce',
    target: '',
    discount: 0,
    threshold: 50000,
    reduce: 5000,
    mainProduct: '',
    giftProduct: '',
    description: '',
    startDate: '2026-01-01',
    endDate: '2026-12-31',
    enabled: true,
    priority: '中',
    usageCount: 8,
    status: 'active'
  },
  {
    id: 3,
    name: '新用户首单立减',
    type: 'reduce',
    target: '',
    discount: 0,
    threshold: 1000,
    reduce: 200,
    mainProduct: '',
    giftProduct: '',
    description: '',
    startDate: '2026-01-15',
    endDate: '2026-03-15',
    enabled: true,
    priority: '高',
    usageCount: 156,
    status: 'active'
  },
  {
    id: 4,
    name: '买VIP送培训课程',
    type: 'gift',
    target: '',
    discount: 0,
    threshold: 0,
    reduce: 0,
    mainProduct: 'VIP年费',
    giftProduct: '联合国采购实战课程',
    description: '',
    startDate: '2025-12-01',
    endDate: '2025-12-31',
    enabled: false,
    priority: '低',
    usageCount: 45,
    status: 'ended'
  }
])

const rules = ref<PromotionRule[]>(createDefaultRules())
const ruleForm = ref<PromotionRule>({
  id: 0,
  name: '',
  type: 'discount',
  target: '',
  discount: 9,
  threshold: 0,
  reduce: 0,
  mainProduct: '',
  giftProduct: '',
  description: '',
  startDate: '',
  endDate: '',
  enabled: true,
  priority: '中',
  usageCount: 0,
  status: 'draft'
})

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeRules = (value: unknown): PromotionRule[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return createDefaultRules()
  }
  return value.map((item: any, index) => ({
    id: normalizeNumber(item?.id, index + 1),
    name: item?.name || `规则${index + 1}`,
    type: ['discount', 'reduce', 'gift', 'custom'].includes(item?.type) ? item.type : 'custom',
    target: item?.target || '',
    discount: normalizeNumber(item?.discount, 0),
    threshold: normalizeNumber(item?.threshold, 0),
    reduce: normalizeNumber(item?.reduce, 0),
    mainProduct: item?.mainProduct || '',
    giftProduct: item?.giftProduct || '',
    description: item?.description || '',
    startDate: item?.startDate || '',
    endDate: item?.endDate || '',
    enabled: Boolean(item?.enabled),
    priority: ['高', '中', '低'].includes(item?.priority) ? item.priority : '中',
    usageCount: normalizeNumber(item?.usageCount, 0),
    status: ['active', 'ended', 'draft'].includes(item?.status) ? item.status : 'draft'
  }))
}

const filteredRules = computed(() => {
  return rules.value.filter(rule => rule.status === activeTab.value)
})

const stats = computed(() => {
  const activeRules = rules.value.filter(rule => rule.status === 'active').length
  const totalUsage = rules.value.reduce((sum, rule) => sum + rule.usageCount, 0)
  const upcomingExpire = rules.value.filter((rule) => {
    if (rule.status !== 'active' || !rule.endDate) {
      return false
    }
    const diff = new Date(rule.endDate).getTime() - Date.now()
    return diff >= 0 && diff <= 1000 * 60 * 60 * 24 * 30
  }).length
  const totalRules = rules.value.length

  return [
    { label: '进行中规则', value: String(activeRules), icon: Trophy, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
    { label: '累计使用次数', value: String(totalUsage), icon: TrendCharts, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '30天内到期', value: String(upcomingExpire), icon: Timer, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '规则总数', value: String(totalRules), icon: Check, bgClass: 'bg-purple-50', textClass: 'text-purple-600' }
  ]
})

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    discount: 'danger',
    reduce: 'success',
    gift: 'warning'
  }
  return map[type] || 'info'
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    discount: '限时折扣',
    reduce: '满减优惠',
    gift: '买赠活动',
    custom: '其他'
  }
  return map[type] || '其他'
}

const getPriorityTag = (priority: string) => {
  const map: Record<string, string> = {
    高: 'danger',
    中: 'warning',
    低: 'info'
  }
  return map[priority] || 'info'
}

const persistRules = async (successMessage: string) => {
  try {
    await saveNamespaceItems([
      {
        key: 'promotion_rules',
        value: rules.value
      }
    ])
    ElMessage.success(successMessage)
    await loadRules()
  } catch (error: any) {
    ElMessage.error(error?.message || '营销规则保存失败')
  }
}

const loadRules = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    rules.value = normalizeRules(itemMap.promotion_rules)
  } catch (error: any) {
    ElMessage.error(error?.message || '营销规则加载失败')
  }
}

const resetRuleForm = () => {
  ruleForm.value = {
    id: 0,
    name: '',
    type: 'discount',
    target: '',
    discount: 9,
    threshold: 0,
    reduce: 0,
    mainProduct: '',
    giftProduct: '',
    description: '',
    startDate: '',
    endDate: '',
    enabled: true,
    priority: '中',
    usageCount: 0,
    status: 'draft'
  }
}

const openRuleDialog = (row?: PromotionRule) => {
  ruleForm.value = row ? { ...row } : {
    id: 0,
    name: '',
    type: 'discount',
    target: '',
    discount: 9,
    threshold: 0,
    reduce: 0,
    mainProduct: '',
    giftProduct: '',
    description: '',
    startDate: '',
    endDate: '',
    enabled: true,
    priority: '中',
    usageCount: 0,
    status: activeTab.value
  }
  dialogVisible.value = true
}

const validateRule = () => {
  if (!ruleForm.value.name || !ruleForm.value.startDate || !ruleForm.value.endDate) {
    return '请填写规则名称和起止日期'
  }
  if (ruleForm.value.type === 'discount' && !ruleForm.value.target) {
    return '请填写折扣目标商品'
  }
  if (ruleForm.value.type === 'gift' && (!ruleForm.value.mainProduct || !ruleForm.value.giftProduct)) {
    return '请填写买赠活动的主商品和赠品'
  }
  if (ruleForm.value.type === 'custom' && !ruleForm.value.description) {
    return '请填写规则说明'
  }
  return ''
}

const handleSaveRule = async () => {
  const message = validateRule()
  if (message) {
    return ElMessage.warning(message)
  }

  const payload = { ...ruleForm.value }
  if (payload.id) {
    const index = rules.value.findIndex(rule => rule.id === payload.id)
    if (index !== -1) {
      rules.value[index] = payload
    }
  } else {
    payload.id = Date.now()
    rules.value.unshift(payload)
  }
  dialogVisible.value = false
  await persistRules(`${payload.name} 已保存`)
  resetRuleForm()
}

const handleDuplicate = async (row: PromotionRule) => {
  rules.value.unshift({
    ...row,
    id: Date.now(),
    name: `${row.name}（复制）`,
    status: 'draft',
    enabled: false
  })
  activeTab.value = 'draft'
  await persistRules(`已复制规则：${row.name}`)
}

const handleToggleStatus = async (row: PromotionRule) => {
  await persistRules(`规则 ${row.enabled ? '已启用' : '已停用'}`)
}

const handleDelete = (row: PromotionRule) => {
  ElMessageBox.confirm(`确定删除规则「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      rules.value = rules.value.filter(rule => rule.id !== row.id)
      await persistRules('营销规则已删除')
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadRules()
})
</script>
