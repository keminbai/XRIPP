export type MemberLevel = 'NORMAL' | 'VIP' | 'SVIP'

export type BenefitType =
  | 'tender_download'
  | 'activity_free'
  | 'report_download'
  | 'supplier_quota'
  | 'sms_quota'

export interface BenefitPolicyItem {
  type: BenefitType
  label: string
  defaultByLevel: Record<MemberLevel, number>
  maxPerGrant: number
  unit: string
}

export const benefitPolicy: BenefitPolicyItem[] = [
  {
    type: 'tender_download',
    label: '标书下载次数',
    defaultByLevel: { NORMAL: 2, VIP: 80, SVIP: 400 },
    maxPerGrant: 500,
    unit: '份'
  },
  {
    type: 'activity_free',
    label: '收费活动免费名额',
    defaultByLevel: { NORMAL: 0, VIP: 1, SVIP: 3 },
    maxPerGrant: 50,
    unit: '次'
  },
  {
    type: 'report_download',
    label: '报告下载次数',
    defaultByLevel: { NORMAL: 0, VIP: 2, SVIP: 10 },
    maxPerGrant: 200,
    unit: '份'
  },
  {
    type: 'supplier_quota',
    label: '供应商入驻名额',
    defaultByLevel: { NORMAL: 0, VIP: 0, SVIP: 1 },
    maxPerGrant: 20,
    unit: '次'
  },
  {
    type: 'sms_quota',
    label: '短信精准推送额度',
    defaultByLevel: { NORMAL: 20, VIP: 100, SVIP: 300 },
    maxPerGrant: 5000,
    unit: '次'
  } 
]

export function getBenefitPolicy(type: BenefitType) {
  return benefitPolicy.find(x => x.type === type)
}
