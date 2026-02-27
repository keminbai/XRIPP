export interface ServiceProvider {
  id: number
  icon: string
  name: string
  city: string
  province: string
  category: string
  type: string
  joinDate: string
  phone: string
  services: string[]
  intro: string
  image: string
  address: string
  contact: string
}

export const useServiceProviders = () => {
  const serviceProviders: ServiceProvider[] = [
    {
      id: 1,
      icon: '🏭',
      name: '上海华联机械制造有限公司',
      city: '上海',
      province: '上海市',
      category: '机械制造',
      type: '私营企业',
      joinDate: '2024-03-15',
      phone: '021-6688-9900',
      services: ['标书代写', '出海咨询', '认证服务'],
      intro: '专注于工业自动化设备研发与制造,已为联合国多个项目提供配套服务。',
      image: 'https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=800&q=80',
      address: '上海市浦东新区张江高科园区88号',
      contact: '张经理'
    },
    {
      id: 2,
      icon: '🏥',
      name: '深圳迈瑞医疗设备股份公司',
      city: '深圳',
      province: '广东省',
      category: '医疗器械',
      type: '上市公司',
      joinDate: '2023-11-20',
      phone: '0755-8899-6677',
      services: ['认证服务', '标书代写'],
      intro: '全球领先的医疗器械制造商,产品远销190多个国家。',
      image: 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800&q=80',
      address: '深圳市南山区高新产业园A座',
      contact: '王工'
    },
    {
      id: 3,
      icon: '🌾',
      name: '河南中原农业科技集团',
      city: '郑州',
      province: '河南省',
      category: '农业机械',
      type: '国有企业',
      joinDate: '2024-01-10',
      phone: '0371-5566-7788',
      services: ['出海咨询', '认证服务'],
      intro: '专业从事农业机械研发与生产,参与多个联合国粮农组织项目。',
      image: 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=800&q=80',
      address: '郑州市金水区农业创新中心16号',
      contact: '李经理'
    },
    {
      id: 4,
      icon: '💡',
      name: '杭州绿能科技股份有限公司',
      city: '杭州',
      province: '浙江省',
      category: '新能源',
      type: '高新技术企业',
      joinDate: '2024-02-05',
      phone: '0571-8877-6655',
      services: ['标书代写', '认证服务', '物流报关'],
      intro: '专注太阳能光伏系统解决方案,产品出口至50多个国家。',
      image: 'https://images.unsplash.com/photo-1509391366360-2e959784a276?w=800&q=80',
      address: '杭州市滨江区新能源大道66号',
      contact: '陈总'
    },
    {
      id: 5,
      icon: '🏗️',
      name: '北京建工国际工程有限公司',
      city: '北京',
      province: '北京市',
      category: '建筑工程',
      type: '国有企业',
      joinDate: '2023-12-18',
      phone: '010-8899-5544',
      services: ['出海咨询', '标书代写'],
      intro: '承接国际基建项目,拥有30年海外工程经验。',
      image: 'https://images.unsplash.com/photo-1503387762-592deb58ef4e?w=800&q=80',
      address: '北京市朝阳区建国路100号',
      contact: '赵总工'
    },
    {
      id: 6,
      icon: '📱',
      name: '广州华信通讯技术有限公司',
      city: '广州',
      province: '广东省',
      category: '通信设备',
      type: '私营企业',
      joinDate: '2024-03-20',
      phone: '020-8877-4433',
      services: ['认证服务', '标书代写'],
      intro: '5G通讯设备制造商,产品符合国际电信联盟标准。',
      image: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&q=80',
      address: '广州市天河区信息港路18号',
      contact: '刘经理'
    },
    {
      id: 7,
      icon: '🚗',
      name: '苏州汽车零部件集团',
      city: '苏州',
      province: '江苏省',
      category: '汽车零部件',
      type: '上市公司',
      joinDate: '2024-01-25',
      phone: '0512-6677-8899',
      services: ['认证服务', '出海咨询'],
      intro: '为全球主要汽车品牌提供零部件,年出口额超10亿美元。',
      image: 'https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=800&q=80',
      address: '苏州市工业园区智造大道28号',
      contact: '吴总'
    },
    {
      id: 8,
      icon: '🧪',
      name: '成都生物医药研发中心',
      city: '成都',
      province: '四川省',
      category: '生物医药',
      type: '研发机构',
      joinDate: '2024-02-28',
      phone: '028-8899-3322',
      services: ['认证服务', '出海咨询'],
      intro: '专注创新药物研发,多项专利技术获国际认可。',
      image: 'https://images.unsplash.com/photo-1582719471137-c3967ffb1c42?w=800&q=80',
      address: '成都市高新区生物医药园3号',
      contact: '周博士'
    },
    {
      id: 9,
      icon: '⚡',
      name: '武汉智能电网科技公司',
      city: '武汉',
      province: '湖北省',
      category: '电力设备',
      type: '高新技术企业',
      joinDate: '2024-01-15',
      phone: '027-8877-2211',
      services: ['认证服务', '标书代写'],
      intro: '智能电网解决方案提供商,参与多个国际援助项目。',
      image: 'https://images.unsplash.com/photo-1473341304170-971dccb5ac1e?w=800&q=80',
      address: '武汉市东湖高新区能源路9号',
      contact: '孙工'
    },
    {
      id: 10,
      icon: '🌐',
      name: '南京跨境电商服务平台',
      city: '南京',
      province: '江苏省',
      category: '跨境电商',
      type: '私营企业',
      joinDate: '2024-03-01',
      phone: '025-8899-1100',
      services: ['物流报关', '出海咨询'],
      intro: '一站式跨境贸易服务,助力中小企业拓展国际市场。',
      image: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=800&q=80',
      address: '南京市建邺区跨境电商产业园12号',
      contact: '钱经理'
    }
  ]

  return { serviceProviders }
}
