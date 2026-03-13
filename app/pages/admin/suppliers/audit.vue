<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="stat in statCards"
        :key="stat.key"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-between"
      >
        <div>
          <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
          <div class="text-xs text-slate-400 mt-1">{{ stat.subLabel }}</div>
        </div>
        <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
          <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 space-y-4">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-lg font-bold text-slate-800">服务商入驻复查</h3>
            <p class="text-xs text-slate-500 mt-1">真实数据源：`/v3/admin/supplier-onboarding`</p>
          </div>
          <el-button plain :loading="loading" @click="loadList">刷新</el-button>
        </div>

        <div class="flex gap-3 flex-wrap">
          <el-select v-model="filters.onboardingStatus" placeholder="审核状态" class="w-44" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="filters.keyword" placeholder="搜索公司/联系人" :prefix-icon="Search" class="w-64" clearable />
          <el-input v-model="filters.city" placeholder="城市" class="w-40" clearable />
          <el-input v-model="filters.serviceType" placeholder="业务/行业" class="w-44" clearable />
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table
          v-loading="loading"
          :data="rows"
          style="width: 100%"
          stripe
          :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
        >
          <el-table-column prop="applyNo" label="申请编号" width="150" />

          <el-table-column label="企业信息" min-width="280">
            <template #default="{ row }">
              <div class="py-2">
                <div class="font-bold text-slate-800 text-sm">{{ row.companyName }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ row.contactName || '-' }} / {{ row.contactPhone || '-' }}</div>
                <div class="text-xs text-slate-400 mt-1">{{ row.mainService || '未填写主营业务' }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="cityName" label="地区" width="140" />

          <el-table-column label="支付/资料" width="180" align="center">
            <template #default="{ row }">
              <div class="space-y-1">
                <el-tag :type="paymentTagType(row.paymentStatus)" size="small">{{ paymentStatusLabel(row.paymentStatus) }}</el-tag>
                <div class="text-xs text-slate-500">
                  附件 {{ row.attachmentsCompleted ? '已完整' : '未完整' }} / 资质 {{ row.certificatesCompleted ? '已完整' : '未完整' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="130" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.onboardingStatus)" effect="dark" size="small">
                {{ row.onboardingStatusLabel }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="submittedAt" label="提交时间" width="170" />

          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row)">
                <el-icon class="mr-1"><View /></el-icon> 查看详情
              </el-button>

              <template v-if="nextTransition(row.onboardingStatus).approve">
                <el-button link type="success" size="small" @click="reviewRow(row, 'approve')">
                  <el-icon class="mr-1"><Check /></el-icon>{{ nextTransition(row.onboardingStatus).approveLabel }}
                </el-button>
              </template>

              <template v-if="nextTransition(row.onboardingStatus).reject">
                <el-button link type="danger" size="small" @click="reviewRow(row, 'reject')">
                  <el-icon class="mr-1"><Close /></el-icon>{{ nextTransition(row.onboardingStatus).rejectLabel }}
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next, sizes"
            :total="totalItems"
            :page-sizes="[10, 20, 50]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" width="1080px" destroy-on-close>
      <template #header>
        <div class="flex items-center justify-between pr-6">
          <div>
            <div class="text-lg font-bold text-slate-800">{{ currentDetail?.companyName || '' }}</div>
            <div class="text-xs text-slate-500 mt-1">申请编号：{{ currentDetail?.applyNo || '-' }}</div>
          </div>
          <div class="flex items-center gap-2">
            <el-tag v-if="currentDetail" :type="paymentTagType(currentDetail.paymentStatus)">
              {{ paymentStatusLabel(currentDetail.paymentStatus) }}
            </el-tag>
            <el-tag v-if="currentDetail" :type="statusTagType(currentDetail.onboardingStatus)">
              {{ currentDetail.onboardingStatusLabel }}
            </el-tag>
          </div>
        </div>
      </template>

      <div v-if="detailLoading" class="py-20 text-center text-slate-500">详情加载中...</div>

      <div v-else-if="currentDetail" class="space-y-6">
        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
          <div class="bg-slate-50 rounded-xl border border-slate-200 p-5 space-y-4">
            <div class="text-sm font-bold text-slate-700">申请概览</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请编号">{{ currentDetail.applyNo }}</el-descriptions-item>
              <el-descriptions-item label="申请类型">{{ applyTypeLabel(currentDetail.applyType) }}</el-descriptions-item>
              <el-descriptions-item label="服务城市">{{ currentDetail.cityName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="主营业务">{{ currentDetail.mainService || '-' }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{ currentDetail.submittedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="复核时间">{{ currentDetail.reviewedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付金额">¥{{ moneyText(currentDetail.feeAmount) }}</el-descriptions-item>
              <el-descriptions-item label="支付确认">{{ currentDetail.paymentVerifiedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="资料完整度" :span="2">
                附件 {{ currentDetail.attachmentsCompleted ? '已完整' : '未完整' }} / 资质 {{ currentDetail.certificatesCompleted ? '已完整' : '未完整' }}
              </el-descriptions-item>
              <el-descriptions-item label="流转备注" :span="2">{{ currentDetail.changeReason || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="bg-slate-50 rounded-xl border border-slate-200 p-5 space-y-4">
            <div class="text-sm font-bold text-slate-700">企业与联系人</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称（中文）">{{ detailField('companyNameZh') || currentDetail.companyName }}</el-descriptions-item>
              <el-descriptions-item label="企业名称（英文）">{{ detailField('companyNameEn') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">{{ detailField('creditCode') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业类型">{{ detailField('nature') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册资本">{{ detailField('regCapital') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="成立日期">{{ detailField('foundDate') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册地址" :span="2">{{ detailField('address') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="总经理">{{ detailField('gmName') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="总经理手机">{{ detailField('gmPhone') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业务对接人">{{ detailField('contactName') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业务对接手机">{{ detailField('contactPhone') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业务对接职务">{{ detailField('contactTitle') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="业务对接邮箱">{{ detailField('contactEmail') || '-' }}</el-descriptions-item>
              <el-descriptions-item label="行业分类" :span="2">{{ joinValue(detailField('industryList')) || joinValue(currentDetail.serviceTypes) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产品/服务描述" :span="2">{{ detailField('productDesc') || currentDetail.intro || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-slate-200 p-5 space-y-4">
          <div class="flex items-center justify-between">
            <div class="text-sm font-bold text-slate-700">附件材料</div>
            <div class="text-xs text-slate-500">支持预览或下载，所有文件均来自结构化附件表</div>
          </div>

          <el-empty v-if="!currentDetail.attachments.length" description="暂无附件" />

          <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div
              v-for="file in currentDetail.attachments"
              :key="file.id"
              class="border border-slate-200 rounded-xl p-4 space-y-3"
            >
              <div class="flex items-start justify-between gap-4">
                <div class="min-w-0">
                  <div class="font-medium text-slate-800 break-all">{{ attachmentLabel(file.fileCategory) }}</div>
                  <div class="text-xs text-slate-500 mt-1 break-all">{{ file.fileName }}</div>
                  <div class="text-xs text-slate-400 mt-1">{{ file.fileExt || '-' }} / {{ formatFileSize(file.fileSize) }}</div>
                </div>
                <div class="flex gap-2 shrink-0">
                  <el-button link type="primary" @click="openFile(file.fileUrl)">预览</el-button>
                  <el-button link type="success" @click="downloadFile(file.fileUrl, file.fileName)">下载</el-button>
                </div>
              </div>

              <el-image
                v-if="isImageFile(file.fileExt)"
                :src="resolveFileUrl(file.fileUrl)"
                fit="cover"
                class="w-full h-48 rounded-lg border border-slate-100"
                :preview-src-list="[resolveFileUrl(file.fileUrl)]"
                preview-teleported
              />

              <div v-else class="h-24 rounded-lg border border-dashed border-slate-200 flex items-center justify-center text-xs text-slate-500">
                非图片文件，请使用预览或下载查看
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-slate-200 p-5 space-y-4">
          <div class="text-sm font-bold text-slate-700">资质证书</div>
          <el-table :data="currentDetail.certificates" size="small" border>
            <el-table-column prop="certName" label="资质名称" min-width="180" />
            <el-table-column label="是否具备" width="100" align="center">
              <template #default="{ row }">{{ row.hasCertificate ? '有' : '无' }}</template>
            </el-table-column>
            <el-table-column prop="certNo" label="证书编号" min-width="160" />
            <el-table-column prop="validTo" label="有效期至" width="140" />
            <el-table-column prop="issuerName" label="发证机构" min-width="160" />
            <el-table-column prop="remarks" label="备注" min-width="160" />
          </el-table>
        </div>

        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
          <div class="bg-slate-50 rounded-xl border border-slate-200 p-5 space-y-4">
            <div class="text-sm font-bold text-slate-700">支付信息</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="支付状态">{{ paymentStatusLabel(currentDetail.paymentStatus) }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ currentDetail.paymentOrder?.payChannel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付单号" :span="2">{{ currentDetail.paymentOrder?.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="金额">¥{{ moneyText(currentDetail.paymentOrder?.amount ?? currentDetail.feeAmount) }}</el-descriptions-item>
              <el-descriptions-item label="第三方流水">{{ currentDetail.paymentOrder?.providerTradeNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付完成时间">{{ currentDetail.paymentOrder?.paidAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="二维码过期">{{ currentDetail.paymentOrder?.expiredAt || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="bg-slate-50 rounded-xl border border-slate-200 p-5 space-y-4">
            <div class="text-sm font-bold text-slate-700">提交快照</div>
            <div class="text-xs text-slate-500">提交审核时冻结的关键字段、附件、资质信息</div>
            <pre class="bg-slate-900 text-slate-100 text-xs rounded-xl p-4 overflow-auto max-h-80">{{ snapshotText }}</pre>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-between items-center">
          <div class="text-xs text-slate-500">未支付或资料不完整的申请，不应进入后续审核流。</div>
          <div class="flex gap-2">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
            <template v-if="currentDetail && nextTransition(currentDetail.onboardingStatus).approve">
              <el-button
                v-if="nextTransition(currentDetail.onboardingStatus).reject"
                type="danger"
                plain
                @click="reviewRow(currentDetail, 'reject')"
              >
                {{ nextTransition(currentDetail.onboardingStatus).rejectLabel }}
              </el-button>
              <el-button type="success" @click="reviewRow(currentDetail, 'approve')">
                {{ nextTransition(currentDetail.onboardingStatus).approveLabel }}
              </el-button>
            </template>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Check,
  CircleCheck,
  CircleClose,
  Close,
  Files,
  Refresh,
  Search,
  View
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

type SupplierAuditRow = {
  id: number
  applyNo: string
  companyName: string
  cityName: string
  contactName: string
  contactPhone: string
  mainService: string
  onboardingStatus: string
  onboardingStatusLabel: string
  paymentStatus: string
  applyType: string
  feeAmount: number | string
  attachmentsCompleted: boolean
  certificatesCompleted: boolean
  submittedAt: string
  reviewedAt: string
}

type AttachmentItem = {
  id: number
  fileCategory: string
  fileName: string
  fileUrl: string
  fileExt: string
  fileSize: number
}

type CertificateItem = {
  id: number
  certType: string
  certName: string
  hasCertificate: boolean
  certNo: string
  validTo: string
  issuerName: string
  remarks: string
}

type SupplierAuditDetail = SupplierAuditRow & {
  intro: string
  serviceTypes: unknown[]
  detail: Record<string, any>
  changeReason: string
  paymentVerifiedAt: string
  paymentOrder?: Record<string, any>
  attachments: AttachmentItem[]
  certificates: CertificateItem[]
  submittedSnapshot: Record<string, any>
}

const loading = ref(false)
const detailLoading = ref(false)
const rows = ref<SupplierAuditRow[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentDetail = ref<SupplierAuditDetail | null>(null)
const stats = ref<Record<string, number>>({})

const filters = ref({
  onboardingStatus: '',
  keyword: '',
  city: '',
  serviceType: ''
})

const statusOptions = [
  { label: '待支付', value: 'pending_payment' },
  { label: '待预审', value: 'submitted' },
  { label: '预审通过', value: 'precheck_passed' },
  { label: '预审未通过', value: 'precheck_failed' },
  { label: '终审通过', value: 'final_review_passed' },
  { label: '终审未通过', value: 'final_review_failed' },
  { label: '已激活', value: 'active' },
  { label: '已停用', value: 'inactive' }
]

const statCards = computed(() => [
  {
    key: 'submitted',
    label: '待预审',
    value: Number(stats.value.submitted || 0),
    subLabel: '已提交待一审',
    icon: Refresh,
    bgClass: 'bg-orange-50',
    textClass: 'text-orange-600'
  },
  {
    key: 'precheck_passed',
    label: '待终审',
    value: Number(stats.value.precheck_passed || 0),
    subLabel: '一审通过待二审',
    icon: Files,
    bgClass: 'bg-blue-50',
    textClass: 'text-blue-600'
  },
  {
    key: 'active',
    label: '已通过',
    value: Number(stats.value.active || 0) + Number(stats.value.final_review_passed || 0),
    subLabel: '终审通过或已激活',
    icon: CircleCheck,
    bgClass: 'bg-green-50',
    textClass: 'text-green-600'
  },
  {
    key: 'rejected',
    label: '已驳回',
    value: Number(stats.value.precheck_failed || 0) + Number(stats.value.final_review_failed || 0),
    subLabel: '包含预审/终审驳回',
    icon: CircleClose,
    bgClass: 'bg-red-50',
    textClass: 'text-red-600'
  }
])

const snapshotText = computed(() => {
  const snapshot = currentDetail.value?.submittedSnapshot || {}
  return JSON.stringify(snapshot, null, 2)
})

const mapRow = (item: any): SupplierAuditRow => ({
  id: Number(item.id),
  applyNo: `SO${String(item.id).padStart(6, '0')}`,
  companyName: String(item.companyName || '未命名企业'),
  cityName: String(item.cityName || ''),
  contactName: String(item.contactName || ''),
  contactPhone: String(item.contactPhone || ''),
  mainService: String(item.mainService || ''),
  onboardingStatus: String(item.onboardingStatus || ''),
  onboardingStatusLabel: String(item.onboardingStatusLabel || ''),
  paymentStatus: String(item.paymentStatus || ''),
  applyType: String(item.applyType || ''),
  feeAmount: item.feeAmount ?? 0,
  attachmentsCompleted: Boolean(item.attachmentsCompleted),
  certificatesCompleted: Boolean(item.certificatesCompleted),
  submittedAt: String(item.submittedAt || item.createdAt || ''),
  reviewedAt: String(item.reviewedAt || '')
})

const mapDetail = (item: any): SupplierAuditDetail => ({
  ...mapRow(item),
  intro: String(item.intro || ''),
  serviceTypes: Array.isArray(item.serviceTypes) ? item.serviceTypes : [],
  detail: typeof item.detail === 'object' && item.detail ? item.detail : {},
  changeReason: String(item.changeReason || ''),
  paymentVerifiedAt: String(item.paymentVerifiedAt || ''),
  paymentOrder: typeof item.paymentOrder === 'object' && item.paymentOrder ? item.paymentOrder : {},
  attachments: Array.isArray(item.attachments) ? item.attachments : [],
  certificates: Array.isArray(item.certificates) ? item.certificates : [],
  submittedSnapshot: typeof item.submittedSnapshot === 'object' && item.submittedSnapshot ? item.submittedSnapshot : {}
})

const loadStats = async () => {
  try {
    const res = await apiRequest<Record<string, number>>('/v3/admin/supplier-onboarding/stats')
    stats.value = res.data || {}
  } catch {
    stats.value = {}
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = {
      page: currentPage.value,
      page_size: pageSize.value
    }
    if (filters.value.onboardingStatus) query.onboarding_status = filters.value.onboardingStatus
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    if (filters.value.city.trim()) query.city = filters.value.city.trim()
    if (filters.value.serviceType.trim()) query.service_type = filters.value.serviceType.trim()

    const res = await apiRequest<any>('/v3/admin/supplier-onboarding', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    rows.value = items.map(mapRow)
    totalItems.value = Number(res?.data?.total) || 0
  } catch (e: any) {
    rows.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取服务商入驻列表失败')
  } finally {
    loading.value = false
  }
}

const openDetail = async (row: SupplierAuditRow) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  currentDetail.value = null
  try {
    const res = await apiRequest<any>(`/v3/admin/supplier-onboarding/${row.id}`)
    currentDetail.value = mapDetail(res.data || {})
  } catch (e: any) {
    detailDialogVisible.value = false
    ElMessage.error(e?.message || '读取详情失败')
  } finally {
    detailLoading.value = false
  }
}

const nextTransition = (status?: string) => {
  if (status === 'submitted') {
    return {
      approve: 'precheck_passed',
      reject: 'precheck_failed',
      approveLabel: '预审通过',
      rejectLabel: '预审驳回'
    }
  }
  if (status === 'precheck_passed') {
    return {
      approve: 'final_review_passed',
      reject: 'final_review_failed',
      approveLabel: '终审通过',
      rejectLabel: '终审驳回'
    }
  }
  if (status === 'final_review_passed') {
    return {
      approve: 'active',
      reject: '',
      approveLabel: '激活服务商',
      rejectLabel: ''
    }
  }
  return {
    approve: '',
    reject: '',
    approveLabel: '',
    rejectLabel: ''
  }
}

const reviewRow = async (row: SupplierAuditRow | SupplierAuditDetail, action: 'approve' | 'reject') => {
  const transition = nextTransition(row.onboardingStatus)
  const toStatus = action === 'approve' ? transition.approve : transition.reject
  const actionLabel = action === 'approve' ? transition.approveLabel : transition.rejectLabel
  if (!toStatus) return

  let reason = ''
  if (action === 'reject') {
    try {
      const promptRes = await ElMessageBox.prompt('请输入驳回原因', actionLabel, {
        inputType: 'textarea',
        inputPlaceholder: '驳回原因会写入状态流转记录并供后续复查',
        inputValidator: (value) => (String(value || '').trim() ? true : '请填写驳回原因')
      })
      reason = String(promptRes.value || '').trim()
    } catch {
      return
    }
  } else {
    const confirmed = await ElMessageBox.confirm(`确认执行“${actionLabel}”吗？`, '提示', { type: 'success' })
      .then(() => true)
      .catch(() => false)
    if (!confirmed) return
    reason = actionLabel
  }

  try {
    await apiRequest(`/v3/admin/supplier-onboarding/${row.id}/transition`, {
      method: 'POST',
      body: { to_status: toStatus, reason }
    })
    ElMessage.success(`${actionLabel}成功`)
    await Promise.all([loadList(), loadStats()])
    if (detailDialogVisible.value && currentDetail.value?.id === row.id) {
      await openDetail(mapRow(row))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || `${actionLabel}失败`)
  }
}

const detailField = (key: string) => currentDetail.value?.detail?.[key]

const joinValue = (value: unknown) => {
  if (Array.isArray(value)) return value.join('、')
  return value ? String(value) : ''
}

const moneyText = (value: unknown) => {
  const num = Number(value)
  return Number.isFinite(num) ? num.toLocaleString() : '0'
}

const applyTypeLabel = (value?: string) => {
  if (value === 'strategic') return '战略服务商'
  if (value === 'normal') return '普通服务商'
  return value || '-'
}

const attachmentLabel = (value?: string) => {
  const map: Record<string, string> = {
    cover_image: '宣传封面',
    company_pdf: '公司介绍 PDF',
    promo_image: '企业宣传图',
    business_license: '营业执照',
    bank_license: '开户银行许可证',
    other: '其他附件'
  }
  return map[String(value || '')] || String(value || '-')
}

const paymentStatusLabel = (value?: string) => {
  const map: Record<string, string> = {
    unpaid: '未支付',
    pending: '待支付',
    paid: '已支付',
    waived: '免支付',
    closed: '已关闭',
    failed: '支付失败'
  }
  return map[String(value || '').toLowerCase()] || String(value || '-')
}

const paymentTagType = (value?: string) => {
  const normalized = String(value || '').toLowerCase()
  if (normalized === 'paid' || normalized === 'waived') return 'success'
  if (normalized === 'failed' || normalized === 'closed') return 'danger'
  return 'warning'
}

const statusTagType = (value?: string) => {
  const normalized = String(value || '')
  if (normalized === 'active' || normalized === 'final_review_passed') return 'success'
  if (normalized === 'precheck_failed' || normalized === 'final_review_failed' || normalized === 'inactive') return 'danger'
  if (normalized === 'precheck_passed') return 'primary'
  return 'warning'
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadList()
}

const handleReset = async () => {
  filters.value = {
    onboardingStatus: '',
    keyword: '',
    city: '',
    serviceType: ''
  }
  currentPage.value = 1
  await loadList()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadList()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadList()
}

const isImageFile = (ext?: string) => ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(String(ext || '').toLowerCase())

const resolveFileUrl = (url?: string) => {
  const raw = String(url || '').trim()
  if (!raw) return ''
  if (/^https?:\/\//i.test(raw)) return raw
  return raw.startsWith('/') ? raw : `/${raw}`
}

const openFile = (url?: string) => {
  const resolved = resolveFileUrl(url)
  if (!resolved) return
  window.open(resolved, '_blank', 'noopener')
}

const downloadFile = (url?: string, fileName?: string) => {
  const resolved = resolveFileUrl(url)
  if (!resolved) return
  const link = window.document.createElement('a')
  link.href = resolved
  link.target = '_blank'
  link.rel = 'noopener'
  if (fileName) {
    link.download = fileName
  }
  window.document.body.appendChild(link)
  link.click()
  window.document.body.removeChild(link)
}

const formatFileSize = (size?: number) => {
  const num = Number(size || 0)
  if (!num) return '-'
  if (num >= 1024 * 1024) return `${(num / 1024 / 1024).toFixed(2)} MB`
  if (num >= 1024) return `${(num / 1024).toFixed(1)} KB`
  return `${num} B`
}

onMounted(async () => {
  await Promise.all([loadList(), loadStats()])
})
</script>
