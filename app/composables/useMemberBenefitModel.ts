import { computed, onMounted, ref } from 'vue'
import { benefitPolicy, type BenefitType, type MemberLevel } from '@/composables/useBenefitPolicy'
import { apiRequest, getLoginUser } from '@/utils/request'

function normalizeLevel(raw: unknown): MemberLevel {
  const level = String(raw || '').toUpperCase()
  if (level === 'SVIP') return 'SVIP'
  if (level === 'VIP') return 'VIP'
  return 'NORMAL'
}

function getLevelFromLocal(): MemberLevel {
  const user = getLoginUser()
  return normalizeLevel(user?.member_level)
}

type UsageMap = Partial<Record<BenefitType, number>>

type QuotaInfo = {
  memberLevel?: string
  tenderDownloadTotalQuota?: number
  tenderDownloadRemaining?: number
  smsQuotaTotal?: number
  smsQuotaRemaining?: number
}

type BenefitUsageResponse = {
  usage?: Partial<Record<BenefitType, number>>
  quota?: QuotaInfo
}

export function useMemberBenefitModel() {
  const currentLevel = ref<MemberLevel>(getLevelFromLocal())
  const loading = ref(false)
  const quota = ref<QuotaInfo>({})

  const rightsByType = computed(() => {
    const map = {} as Record<BenefitType, number>
    for (const item of benefitPolicy) {
      map[item.type] = item.defaultByLevel[currentLevel.value]
    }
    if (quota.value.tenderDownloadTotalQuota != null) {
      map.tender_download = quota.value.tenderDownloadTotalQuota
    }
    return map
  })

  const usageByType = ref<UsageMap>({
    tender_download: 0,
    sms_quota: 0,
    activity_free: 0,
    report_download: 0,
    supplier_quota: 0
  })

  const refreshUsage = async () => {
    const user = getLoginUser()
    if (!user || String(user.role || '').toLowerCase() !== 'member') return

    loading.value = true
    try {
      const res = await apiRequest<BenefitUsageResponse>('/v3/member/benefits/usage')
      const usage = res.data?.usage || {}
      quota.value = res.data?.quota || {}
      usageByType.value = {
        tender_download: Number(usage.tender_download || 0),
        sms_quota: Number(usage.sms_quota || 0),
        activity_free: Number(usage.activity_free || 0),
        report_download: Number(usage.report_download || 0),
        supplier_quota: Number(usage.supplier_quota || 0)
      }
    } catch {
      usageByType.value = {
        tender_download: 0,
        sms_quota: 0,
        activity_free: 0,
        report_download: 0,
        supplier_quota: 0
      }
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    refreshUsage()
  })

  const remainingByType = computed(() => {
    const map = {} as Record<BenefitType, number>
    const rights = rightsByType.value
    const usage = usageByType.value

    for (const key of Object.keys(rights) as BenefitType[]) {
      map[key] = Math.max(0, rights[key] - (usage[key] || 0))
    }
    if (quota.value.tenderDownloadRemaining != null) {
      map.tender_download = quota.value.tenderDownloadRemaining
    }
    return map
  })

  return {
    currentLevel,
    rightsByType,
    usageByType,
    remainingByType,
    quota,
    loading,
    refreshUsage
  }
}
