export const OVERSEAS_COUNTRY_OPTIONS = [
  '美国',
  '英国',
  '德国',
  '法国',
  '日本',
  '韩国',
  '新加坡',
  '阿联酋',
  '澳大利亚',
  '加拿大',
  '全球'
]

export const OVERSEAS_SERVICE_TYPE_OPTIONS = [
  { label: '外贸服务', value: 'trade' },
  { label: '知识产权', value: 'ip' },
  { label: '跨境企服', value: 'cross' },
  { label: '出海投资', value: 'invest' },
  { label: '其他服务', value: 'other' }
]

export const OVERSEAS_SERVICE_TYPE_LABEL_MAP: Record<string, string> = {
  trade: '外贸服务',
  ip: '知识产权',
  cross: '跨境企服',
  invest: '出海投资',
  other: '其他服务'
}

export const OVERSEAS_SERVICE_TYPE_TAG_MAP: Record<string, string> = {
  trade: 'primary',
  ip: 'success',
  cross: 'warning',
  invest: 'danger',
  other: 'info'
}

export const OVERSEAS_PROJECT_MAP: Record<string, { label: string; type: string }> = {
  customs: { label: '报关清关', type: 'trade' },
  shipping: { label: '物流海运', type: 'trade' },
  overseas_warehouse: { label: '海外仓', type: 'trade' },
  cargo_insurance: { label: '货运保险', type: 'trade' },
  cross_border_payment: { label: '跨境支付与结算', type: 'trade' },
  legal_service: { label: '法律服务', type: 'ip' },
  trademark: { label: '商标注册', type: 'ip' },
  certification: { label: '认证', type: 'ip' },
  audit: { label: '财务审计', type: 'ip' },
  tax_accounting: { label: '记账报税', type: 'ip' },
  due_diligence: { label: '尽职调查', type: 'ip' },
  tax_service: { label: '税务服务', type: 'ip' },
  compliance: { label: '跨境合规', type: 'ip' },
  company_setup: { label: '公司设立', type: 'cross' },
  company_dissolution: { label: '公司注销', type: 'cross' },
  visa: { label: '签证', type: 'cross' },
  project_research: { label: '项目调研', type: 'invest' },
  industry_report: { label: '行业分析报告', type: 'invest' },
  factory_build: { label: '投资建厂', type: 'invest' },
  landing_consulting: { label: '落地咨询', type: 'invest' },
  other_service: { label: '其他服务', type: 'other' }
}

export const OVERSEAS_SERVICE_TAG_OPTIONS = [
  { label: '专业认证', value: 'certified' },
  { label: '快速响应', value: 'fast' },
  { label: '一站式', value: 'onestop' },
  { label: '性价比高', value: 'affordable' },
  { label: '经验丰富', value: 'experienced' }
]

export const OVERSEAS_REPORT_INDUSTRY_OPTIONS = [
  { label: '医疗健康', value: 'medical' },
  { label: '信息技术', value: 'it' },
  { label: '制造业', value: 'manufacturing' },
  { label: '金融服务', value: 'finance' },
  { label: '消费品', value: 'consumer' },
  { label: '能源环保', value: 'energy' },
  { label: '农业', value: 'agriculture' },
  { label: '综合', value: 'comprehensive' }
]

export const OVERSEAS_REPORT_INDUSTRY_LABEL_MAP: Record<string, string> = {
  medical: '医疗健康',
  it: '信息技术',
  manufacturing: '制造业',
  finance: '金融服务',
  consumer: '消费品',
  energy: '能源环保',
  agriculture: '农业',
  comprehensive: '综合'
}

export const OVERSEAS_REPORT_TYPE_OPTIONS = [
  { label: '数据报告', value: 'data' },
  { label: '调研报告', value: 'research' },
  { label: '指南', value: 'guide' },
  { label: '行业报告', value: 'industry' },
  { label: '评估报告', value: 'evaluation' },
  { label: '可行性报告', value: 'feasibility' }
]

export const OVERSEAS_REPORT_TYPE_LABEL_MAP: Record<string, string> = {
  data: '数据报告',
  research: '调研报告',
  guide: '指南',
  industry: '行业报告',
  evaluation: '评估报告',
  feasibility: '可行性报告'
}

export const OVERSEAS_REPORT_FEE_LEVEL_OPTIONS = [
  { label: '免费', value: 'free' },
  { label: '报告收费1', value: 'fee_1' },
  { label: '报告收费2', value: 'fee_2' }
]

export const OVERSEAS_REPORT_FEE_LEVEL_LABEL_MAP: Record<string, string> = {
  free: '免费',
  fee_1: '报告收费1',
  fee_2: '报告收费2'
}

export const OVERSEAS_REPORT_ACCESS_LEVEL_OPTIONS = [
  { label: '公开（所有会员）', value: 'public' },
  { label: 'VIP会员', value: 'vip' },
  { label: 'SVIP会员', value: 'svip' }
]

export const OVERSEAS_REPORT_ACCESS_LEVEL_LABEL_MAP: Record<string, string> = {
  public: '公开',
  vip: 'VIP',
  svip: 'SVIP'
}
