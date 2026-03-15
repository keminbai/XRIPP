<template>
  <div class="space-y-6">
    <div class="relative bg-gradient-to-r from-slate-900 via-blue-900 to-slate-900 rounded-2xl p-8 text-white shadow-lg overflow-hidden">
      <div class="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_top_right,_rgba(59,130,246,0.55),_transparent_35%),radial-gradient(circle_at_bottom_left,_rgba(16,185,129,0.35),_transparent_30%)]"></div>
      <div class="relative z-10">
        <div class="text-sm text-blue-200 font-bold mb-2">会员中心 / 服务商入驻</div>
        <h1 class="text-3xl font-bold mb-2">我的服务商申请</h1>
        <p class="text-blue-100 text-sm max-w-3xl leading-relaxed">
          这里集中查看您发起的服务商入驻申请、支付状态、审核进度、附件材料与资质信息。
        </p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-5">
      <div v-for="card in statCards" :key="card.key" class="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
        <div class="text-xs text-slate-400 mb-2">{{ card.label }}</div>
        <div class="text-3xl font-bold text-slate-900">{{ card.value }}</div>
        <div class="text-xs text-slate-500 mt-2">{{ card.subLabel }}</div>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div class="flex gap-3 flex-wrap">
            <el-select v-model="filters.status" placeholder="申请状态" class="w-44" clearable>
              <el-option v-for="option in statusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
            <el-input v-model="filters.keyword" placeholder="搜索公司名称" class="w-64" clearable />
            <el-button plain @click="resetFilters">重置</el-button>
          </div>
        <div class="flex gap-3">
          <el-button plain :loading="loading" @click="loadApplications">刷新</el-button>
          <el-button type="primary" @click="navigateTo('/member/supplier-apply')">发起新申请</el-button>
        </div>
      </div>

      <div class="p-6">
        <div v-if="loading" class="py-20 text-center text-slate-500">正在读取申请记录...</div>

        <div v-else-if="filteredApplications.length === 0" class="py-20 text-center">
          <div class="text-5xl mb-4">📋</div>
          <div class="text-lg font-bold text-slate-800 mb-2">暂无服务商申请</div>
          <div class="text-sm text-slate-500 mb-6">发起申请后，支付状态和审核时间轴都会显示在这里。</div>
          <el-button type="primary" @click="navigateTo('/member/supplier-apply')">立即申请</el-button>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in filteredApplications"
            :key="item.id"
            class="border border-slate-200 rounded-2xl p-5 hover:shadow-md transition-all"
          >
            <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-5">
              <div class="space-y-3">
                <div class="flex flex-wrap items-center gap-2">
                  <div class="text-lg font-bold text-slate-900">{{ item.companyName }}</div>
                  <el-tag :type="statusTagType(item.onboardingStatus)" size="small">{{ item.onboardingStatusLabel }}</el-tag>
                  <el-tag :type="paymentTagType(item.paymentStatus)" size="small">{{ paymentStatusLabel(item.paymentStatus) }}</el-tag>
                </div>
                <div class="text-sm text-slate-500 flex flex-wrap gap-x-5 gap-y-2">
                  <span>申请编号：SO{{ String(item.id).padStart(6, '0') }}</span>
                  <span>申请类型：{{ applyTypeLabel(item.applyType) }}</span>
                  <span>服务城市：{{ item.cityName || '-' }}</span>
                  <span>联系人：{{ item.contactName || '-' }}</span>
                </div>
                <div class="text-xs text-slate-400 flex flex-wrap gap-x-5 gap-y-2">
                  <span>提交时间：{{ item.submittedAt || item.createdAt || '-' }}</span>
                  <span>审核时间：{{ item.reviewedAt || '-' }}</span>
                  <span>资料完整：附件 {{ item.attachmentsCompleted ? '已完整' : '未完整' }} / 资质 {{ item.certificatesCompleted ? '已完整' : '未完整' }}</span>
                </div>
              </div>

              <div class="flex flex-col sm:flex-row gap-3 lg:items-center">
                <div class="text-right">
                  <div class="text-xs text-slate-400">支付金额</div>
                  <div class="text-2xl font-bold text-slate-900">¥{{ moneyText(item.feeAmount) }}</div>
                </div>
                <el-button plain @click="openDetail(item)">查看详情</el-button>
                <el-button
                  v-if="canContinuePayment(item)"
                  type="primary"
                  plain
                  @click="openPayment(item)"
                >
                  继续支付
                </el-button>
                <el-button
                  v-if="canSubmitReview(item)"
                  type="success"
                  @click="submitReview(item)"
                >
                  提交审核
                </el-button>
                <el-button
                  v-if="canRetryApplication(item)"
                  type="warning"
                  plain
                  @click="editApplication(item)"
                >
                  修改后重提
                </el-button>
              </div>
            </div>

            <div v-if="item.changeReason" class="mt-4 text-sm text-amber-700 bg-amber-50 border border-amber-100 rounded-xl p-3">
              处理说明：{{ item.changeReason }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailDialogVisible" width="1080px" destroy-on-close>
      <template #header>
        <div class="flex items-center justify-between pr-6">
          <div>
            <div class="text-lg font-bold text-slate-900">{{ detailItem?.companyName || '' }}</div>
            <div class="text-xs text-slate-500 mt-1">申请编号：SO{{ detailItem ? String(detailItem.id).padStart(6, '0') : '-' }}</div>
          </div>
          <div class="flex items-center gap-2">
            <el-tag v-if="detailItem" :type="statusTagType(detailItem.onboardingStatus)">{{ detailItem.onboardingStatusLabel }}</el-tag>
            <el-tag v-if="detailItem" :type="paymentTagType(detailItem.paymentStatus)">{{ paymentStatusLabel(detailItem.paymentStatus) }}</el-tag>
          </div>
        </div>
      </template>

      <div v-if="detailLoading" class="py-20 text-center text-slate-500">详情加载中...</div>

      <div v-else-if="detailItem" class="space-y-6">
        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
          <div class="bg-slate-50 border border-slate-200 rounded-2xl p-5">
            <div class="text-sm font-bold text-slate-700 mb-4">申请概览</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="申请类型">{{ applyTypeLabel(detailItem.applyType) }}</el-descriptions-item>
              <el-descriptions-item label="支付金额">¥{{ moneyText(detailItem.feeAmount) }}</el-descriptions-item>
              <el-descriptions-item label="服务城市">{{ detailItem.cityName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="主营业务">{{ detailField('mainService') || detailItem.mainService || '-' }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{ detailItem.submittedAt || detailItem.createdAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付确认">{{ detailItem.paymentVerifiedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核时间">{{ detailItem.reviewedAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="处理说明">{{ detailItem.changeReason || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="bg-slate-50 border border-slate-200 rounded-2xl p-5">
            <div class="text-sm font-bold text-slate-700 mb-4">进度时间轴</div>
            <el-timeline>
              <el-timeline-item
                v-for="node in timelineNodes"
                :key="node.title + node.time"
                :type="node.type"
                :timestamp="node.time"
                :hollow="node.hollow"
              >
                <div class="font-medium text-slate-800">{{ node.title }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ node.description }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>

        <div class="bg-white border border-slate-200 rounded-2xl p-5">
          <div class="text-sm font-bold text-slate-700 mb-4">企业与联系人</div>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称（中文）">{{ detailField('companyNameZh') || detailItem.companyName }}</el-descriptions-item>
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
            <el-descriptions-item label="行业分类" :span="2">{{ joinValue(detailField('industryList')) || joinValue(detailItem.serviceTypes) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品/服务描述" :span="2">{{ detailField('productDesc') || detailItem.intro || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="bg-white border border-slate-200 rounded-2xl p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="text-sm font-bold text-slate-700">附件材料</div>
            <div class="text-xs text-slate-500">可预览或下载您提交的原始材料</div>
          </div>
          <el-empty v-if="!detailItem.attachments.length" description="暂无附件" />
          <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div v-for="file in detailItem.attachments" :key="file.id" class="border border-slate-200 rounded-xl p-4 space-y-3">
              <div class="flex items-start justify-between gap-4">
                <div class="min-w-0">
                  <div class="font-medium text-slate-800">{{ attachmentLabel(file.fileCategory) }}</div>
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
                非图片文件，请点击预览或下载查看
              </div>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
          <div class="bg-white border border-slate-200 rounded-2xl p-5">
            <div class="text-sm font-bold text-slate-700 mb-4">资质证书</div>
            <el-table :data="detailItem.certificates" size="small" border>
              <el-table-column prop="certName" label="资质名称" min-width="170" />
              <el-table-column label="是否具备" width="100" align="center">
                <template #default="{ row }">{{ row.hasCertificate ? '有' : '无' }}</template>
              </el-table-column>
              <el-table-column prop="certNo" label="证书编号" min-width="150" />
              <el-table-column prop="validTo" label="有效期至" width="140" />
              <el-table-column prop="issuerName" label="发证机构" min-width="150" />
            </el-table>
          </div>

          <div class="bg-white border border-slate-200 rounded-2xl p-5 space-y-4">
            <div class="text-sm font-bold text-slate-700">支付与提交快照</div>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="支付状态">{{ paymentStatusLabel(detailItem.paymentStatus) }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ detailItem.paymentOrder?.payChannel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付单号" :span="2">{{ detailItem.paymentOrder?.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="支付时间">{{ detailItem.paymentOrder?.paidAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="二维码有效期">{{ detailItem.paymentOrder?.expiredAt || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div>
              <div class="text-xs text-slate-500 mb-2">提交快照</div>
              <pre class="bg-slate-900 text-slate-100 text-xs rounded-xl p-4 overflow-auto max-h-72">{{ snapshotText }}</pre>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-between items-center">
          <div class="text-xs text-slate-500">支付完成后可直接提交审核；未支付时也可继续查看二维码与支付状态。</div>
          <div class="flex gap-2">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
            <el-button
              v-if="detailItem && canContinuePayment(detailItem)"
              type="primary"
              plain
              @click="openPayment(detailItem)"
            >
              查看支付二维码
            </el-button>
            <el-button
              v-if="detailItem && canSubmitReview(detailItem)"
              type="success"
              @click="submitReview(detailItem)"
            >
              提交审核
            </el-button>
            <el-button
              v-if="detailItem && canRetryApplication(detailItem)"
              type="warning"
              plain
              @click="editApplication(detailItem)"
            >
              修改后重提
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentDialogVisible" title="继续支付" width="520px" destroy-on-close>
      <div v-if="paymentLoading" class="py-20 text-center text-slate-500">支付信息加载中...</div>
      <div v-else-if="paymentInfo" class="space-y-5">
        <div class="text-center">
          <div class="text-sm text-slate-500 mb-2">待支付金额</div>
          <div class="text-4xl font-bold text-blue-600">¥{{ moneyText(paymentInfo.feeAmount) }}</div>
          <div class="text-xs text-slate-400 mt-2">支付单号：{{ paymentInfo.paymentOrderNo }}</div>
        </div>
        <div class="flex justify-center">
          <div class="w-56 h-56 bg-white border border-slate-200 rounded-xl p-3 flex items-center justify-center">
            <img
              v-if="paymentInfo.qrCodeBase64"
              :src="paymentInfo.qrCodeBase64"
              alt="支付二维码"
              class="w-full h-full object-contain"
            />
            <div v-else class="text-xs text-slate-400">二维码生成中</div>
          </div>
        </div>
        <div class="text-xs text-center text-slate-500 space-y-1">
          <p>请使用微信扫码支付，以后端支付状态为准。</p>
          <p v-if="paymentInfo.expiredAt">有效期至：{{ paymentInfo.expiredAt }}</p>
          <p v-if="paymentInfo.paidAt" class="text-green-600">已于 {{ paymentInfo.paidAt }} 完成支付</p>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-between items-center">
          <div class="text-xs text-slate-500">支付完成后可刷新支付状态。</div>
          <div class="flex gap-2">
            <el-button @click="paymentDialogVisible = false">关闭</el-button>
            <el-button :loading="paymentChecking" @click="refreshPaymentStatus">刷新支付状态</el-button>
            <el-button
              v-if="currentPaymentRow && canSubmitReview(currentPaymentRow)"
              type="success"
              :loading="submittingReview"
              @click="submitReview(currentPaymentRow)"
            >
              提交审核
            </el-button>
            <el-button v-else type="primary" @click="goToApplyPage">返回申请页</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'
import { resolveFileUrl } from '@/utils/file-url'

definePageMeta({ layout: 'member' })

type SupplierApplicationRow = {
  id: number
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
  paymentOrderId?: number | null
  paymentVerifiedAt?: string
  attachmentsCompleted: boolean
  certificatesCompleted: boolean
  serviceTypes: unknown[]
  submittedAt: string
  reviewedAt: string
  createdAt: string
  updatedAt: string
  changeReason: string
}

type SupplierApplicationDetail = SupplierApplicationRow & {
  intro: string
  detail: Record<string, any>
  attachments: Array<{
    id: number
    fileCategory: string
    fileName: string
    fileUrl: string
    fileExt: string
    fileSize: number
  }>
  certificates: Array<{
    id: number
    certType: string
    certName: string
    hasCertificate: boolean
    certNo: string
    validFrom: string
    validTo: string
    issuerName: string
    remarks: string
  }>
  paymentOrder?: Record<string, any>
  submittedSnapshot: Record<string, any>
}

type SupplierApplyPaymentInfo = {
  applicationId: number
  feeAmount: number | string
  paymentOrderNo: string
  payStatus: string
  qrCodeBase64: string
  expiredAt?: string
  paidAt?: string
}

const loading = ref(false)
const detailLoading = ref(false)
const paymentLoading = ref(false)
const paymentChecking = ref(false)
const submittingReview = ref(false)
const applications = ref<SupplierApplicationRow[]>([])
const detailItem = ref<SupplierApplicationDetail | null>(null)
const paymentInfo = ref<SupplierApplyPaymentInfo | null>(null)
const detailDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const currentPaymentApplicationId = ref<number | null>(null)
const currentPaymentRow = ref<SupplierApplicationRow | SupplierApplicationDetail | null>(null)

const filters = ref({
  status: '',
  keyword: ''
})

const statusOptions = [
  { label: '待支付', value: 'pending_payment' },
  { label: '待审核', value: 'submitted' },
  { label: '预审通过', value: 'precheck_passed' },
  { label: '预审未通过', value: 'precheck_failed' },
  { label: '终审通过', value: 'final_review_passed' },
  { label: '终审未通过', value: 'final_review_failed' },
  { label: '已激活', value: 'active' },
  { label: '已停用', value: 'inactive' }
]

const filteredApplications = computed(() => {
  const keyword = filters.value.keyword.trim()
  return applications.value.filter(item => {
    const statusMatch = !filters.value.status || item.onboardingStatus === filters.value.status
    const keywordMatch = !keyword || item.companyName.includes(keyword)
    return statusMatch && keywordMatch
  })
})

const statCards = computed(() => {
  const list = applications.value
  return [
    {
      key: 'all',
      label: '申请总数',
      value: list.length,
      subLabel: '您发起的全部服务商申请'
    },
    {
      key: 'pending_payment',
      label: '待支付',
      value: list.filter(item => item.onboardingStatus === 'pending_payment').length,
      subLabel: '资料已保存，等待支付确认'
    },
    {
      key: 'reviewing',
      label: '审核中',
      value: list.filter(item => ['submitted', 'precheck_passed'].includes(item.onboardingStatus)).length,
      subLabel: '已提交审核，等待平台处理'
    },
    {
      key: 'finished',
      label: '已完成',
      value: list.filter(item => ['final_review_passed', 'active'].includes(item.onboardingStatus)).length,
      subLabel: '终审通过或已激活'
    }
  ]
})

const timelineNodes = computed(() => {
  const item = detailItem.value
  if (!item) return []

  const nodes: Array<{ title: string; description: string; time: string; type: '' | 'primary' | 'success' | 'warning' | 'danger'; hollow: boolean }> = []
  nodes.push({
    title: '申请已创建',
    description: '系统已保存您的企业资料草稿。',
    time: item.createdAt || '-',
    type: 'primary',
    hollow: false
  })

  if (item.onboardingStatus === 'pending_payment' || item.paymentStatus === 'pending') {
    nodes.push({
      title: '等待支付',
      description: '申请资料已保存，请完成支付后提交审核。',
      time: item.updatedAt || '-',
      type: 'warning',
      hollow: false
    })
  }

  if (item.paymentStatus === 'paid' || item.paymentStatus === 'waived') {
    nodes.push({
      title: '支付已确认',
      description: '平台已确认支付状态，可以进入审核流程。',
      time: item.paymentVerifiedAt || item.paymentOrder?.paidAt || '-',
      type: 'success',
      hollow: false
    })
  }

  if (item.submittedAt) {
    nodes.push({
      title: '已提交审核',
      description: '申请已进入平台审核队列。',
      time: item.submittedAt,
      type: 'primary',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'precheck_passed') {
    nodes.push({
      title: '预审通过',
      description: item.changeReason || '资料初审通过，进入终审阶段。',
      time: item.reviewedAt || '-',
      type: 'success',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'precheck_failed') {
    nodes.push({
      title: '预审未通过',
      description: item.changeReason || '预审未通过，请根据处理说明调整后重提。',
      time: item.reviewedAt || '-',
      type: 'danger',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'final_review_passed') {
    nodes.push({
      title: '终审通过',
      description: item.changeReason || '终审通过，待激活为正式服务商。',
      time: item.reviewedAt || '-',
      type: 'success',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'final_review_failed') {
    nodes.push({
      title: '终审未通过',
      description: item.changeReason || '终审未通过，请根据反馈继续处理。',
      time: item.reviewedAt || '-',
      type: 'danger',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'active') {
    nodes.push({
      title: '已激活为服务商',
      description: '申请流程已完成，您已成为平台正式服务商。',
      time: item.reviewedAt || item.updatedAt || '-',
      type: 'success',
      hollow: false
    })
  }

  if (item.onboardingStatus === 'inactive') {
    nodes.push({
      title: '服务商资格已停用',
      description: item.changeReason || '当前服务商资格已停用，请联系平台确认后续处理。',
      time: item.updatedAt || '-',
      type: 'warning',
      hollow: true
    })
  }

  return nodes
})

const snapshotText = computed(() => JSON.stringify(detailItem.value?.submittedSnapshot || {}, null, 2))

const loadApplications = async () => {
  loading.value = true
  try {
    const res = await apiRequest<SupplierApplicationRow[]>('/v3/member/supplier-apply')
    applications.value = Array.isArray(res.data) ? res.data : []
  } catch (e: any) {
    applications.value = []
    ElMessage.error(e?.message || '读取服务商申请失败')
  } finally {
    loading.value = false
  }
}

const openDetail = async (item: SupplierApplicationRow) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailItem.value = null
  try {
    const res = await apiRequest<SupplierApplicationDetail>(`/v3/member/supplier-apply/${item.id}`)
    detailItem.value = res.data
  } catch (e: any) {
    detailDialogVisible.value = false
    ElMessage.error(e?.message || '读取申请详情失败')
  } finally {
    detailLoading.value = false
  }
}

const openPayment = async (item: SupplierApplicationRow | SupplierApplicationDetail) => {
  currentPaymentRow.value = item
  currentPaymentApplicationId.value = item.id
  paymentDialogVisible.value = true
  paymentLoading.value = true
  paymentInfo.value = null
  try {
    const res = await apiRequest<SupplierApplyPaymentInfo>(`/v3/member/supplier-apply/${item.id}/payment`)
    paymentInfo.value = res.data
  } catch (e: any) {
    paymentDialogVisible.value = false
    ElMessage.error(e?.message || '读取支付信息失败')
  } finally {
    paymentLoading.value = false
  }
}

const refreshPaymentStatus = async () => {
  if (!currentPaymentApplicationId.value) return
  paymentChecking.value = true
  try {
    const res = await apiRequest<SupplierApplyPaymentInfo>(`/v3/member/supplier-apply/${currentPaymentApplicationId.value}/payment`)
    paymentInfo.value = res.data
    if (String(res.data?.payStatus || '').toLowerCase() === 'paid') {
      ElMessage.success('支付已确认')
      await loadApplications()
      if (detailItem.value?.id === currentPaymentApplicationId.value) {
        await openDetail(detailItem.value)
      }
      currentPaymentRow.value = applications.value.find(item => item.id === currentPaymentApplicationId.value) || currentPaymentRow.value
    } else {
      ElMessage.info('支付尚未确认，请稍后再试')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '刷新支付状态失败')
  } finally {
    paymentChecking.value = false
  }
}

const goToApplyPage = () => {
  paymentDialogVisible.value = false
  navigateTo('/member/supplier-apply')
}

const resetFilters = () => {
  filters.value = {
    status: '',
    keyword: ''
  }
}

const detailField = (key: string) => detailItem.value?.detail?.[key]

const joinValue = (value: unknown) => {
  if (Array.isArray(value)) return value.join('、')
  return value ? String(value) : ''
}

const applyTypeLabel = (value?: string) => {
  if (value === 'strategic') return '战略服务商'
  if (value === 'normal') return '普通服务商'
  return value || '-'
}

const canContinuePayment = (item?: SupplierApplicationRow | SupplierApplicationDetail | null) => {
  if (!item) return false
  const status = String(item.onboardingStatus || '')
  const paymentStatus = String(item.paymentStatus || '').toLowerCase()
  return status === 'pending_payment' && !['paid', 'waived'].includes(paymentStatus)
}

const canSubmitReview = (item?: SupplierApplicationRow | SupplierApplicationDetail | null) => {
  if (!item) return false
  const status = String(item.onboardingStatus || '')
  const paymentStatus = String(item.paymentStatus || '').toLowerCase()
  return ['draft', 'pending_payment'].includes(status) && ['paid', 'waived'].includes(paymentStatus)
}

const canRetryApplication = (item?: SupplierApplicationRow | SupplierApplicationDetail | null) => {
  if (!item) return false
  return ['precheck_failed', 'final_review_failed'].includes(String(item.onboardingStatus || ''))
}

const submitReview = async (item: SupplierApplicationRow | SupplierApplicationDetail) => {
  if (!canSubmitReview(item)) {
    ElMessage.warning('当前支付状态未确认，暂不能提交审核')
    return
  }

  submittingReview.value = true
  try {
    await apiRequest(`/v3/member/supplier-apply/${item.id}/submit`, { method: 'POST' })
    ElMessage.success('申请已提交审核')
    paymentDialogVisible.value = false
    await loadApplications()
    if (detailDialogVisible.value && detailItem.value?.id === item.id) {
      await openDetail(item)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '提交审核失败')
  } finally {
    submittingReview.value = false
  }
}

const editApplication = (item: SupplierApplicationRow | SupplierApplicationDetail) => {
  detailDialogVisible.value = false
  paymentDialogVisible.value = false
  navigateTo({
    path: '/member/supplier-apply',
    query: {
      application_id: String(item.id),
      mode: 'retry'
    }
  })
}

const paymentStatusLabel = (value?: string) => {
  const map: Record<string, string> = {
    unpaid: '未支付',
    pending: '待支付',
    paid: '已支付',
    waived: '免支付',
    failed: '支付失败',
    closed: '已关闭'
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
  if (normalized === 'precheck_failed' || normalized === 'final_review_failed') return 'danger'
  if (normalized === 'precheck_passed') return 'primary'
  return 'warning'
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

const moneyText = (value: unknown) => {
  const num = Number(value)
  return Number.isFinite(num) ? num.toLocaleString() : '0'
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
  if (fileName) link.download = fileName
  window.document.body.appendChild(link)
  link.click()
  window.document.body.removeChild(link)
}

const isImageFile = (ext?: string) => ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(String(ext || '').toLowerCase())

const formatFileSize = (size?: number) => {
  const num = Number(size || 0)
  if (!num) return '-'
  if (num >= 1024 * 1024) return `${(num / 1024 / 1024).toFixed(2)} MB`
  if (num >= 1024) return `${(num / 1024).toFixed(1)} KB`
  return `${num} B`
}

await loadApplications()
</script>
