// 文件路径: D:\ipp-platform\app\composables\useGlobalConfig.ts
export const useGlobalConfig = () => {
  
  // 1. 行业分类
  const industryOptions = [
    { label: '制造业 (Manufacturing)', value: 'manufacturing' },
    { label: '贸易类 (Trade)', value: 'trade' },
    { label: '服务业 (Service)', value: 'service' },
    { label: '大宗贸易 (Bulk Trade)', value: 'bulk' },
    { label: '建筑业 (Construction)', value: 'construction' },
    { label: '科技型企业 (Tech)', value: 'tech' },
    { label: '其他 (Other)', value: 'other' }
  ]

  // 2. ✅ 服务商入库类型与价格（已更新：5888/28888，新增描述）
  const supplierPricing = {
    normal: {
      label: '普通服务商',
      price: 5888,
      period: '1年',
      features: ['名录库展示', '被动搜索'],
      description: '主要从事标准化产品供应或基础服务提供，适用于单项目或短期合作。'
    },
    strategic: {
      label: '战略服务商',
      price: 28888,
      period: '2年',
      features: ['首页推广', '商机推送', '活动优先推荐'],
      description: '具备长期合作意愿、综合服务能力与合规管理体系，适用于框架协议、联合履约与供应链协同项目。'
    }
  }

  // 3. 服务城市列表
  const cityOptions = [
    '上海', '北京', '深圳', '广州', '杭州', 
    '成都', '武汉', '南京', '苏州', '宁波', 
    '天津', '重庆', '绵阳'
  ]

  // 4. 服务类型
  const serviceTypes = [
    '全部', '报告下载', '外贸服务', '知识产权', 
    '跨境企服', '出海投资', '金融服务', '其他服务'
  ]

  return {
    industryOptions,
    supplierPricing,
    cityOptions,
    serviceTypes
  }
}