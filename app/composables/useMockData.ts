export const useMockData = () => {
  // 1. 标书数据
  const tenders = [
    {
      id: 'A0000001',
      title: 'Supply of Medical Equipment for Emergency Field Hospitals',
      titleZh: '应急野战医院医疗设备供应项目',
      type: '联合国采购',
      organization: 'UNOPS (联合国项目事务署)',
      country: '苏丹',
      publishDate: '2025-01-10',
      deadline: '2025-02-20',
      status: 'Open',
      amount: '$2,500,000',
      category: '医疗设备',
      description: '为苏丹达尔富尔地区部署的野战医院采购X光机、呼吸机、手术套件等医疗设备。'
    },
    {
      id: 'B0000023',
      title: 'Construction of 50MW Solar Power Plant Phase II',
      titleZh: '50兆瓦太阳能发电站二期建设工程',
      type: '国际政府采购',
      organization: '马来西亚能源部',
      country: '马来西亚',
      publishDate: '2025-01-08',
      deadline: '2025-03-01',
      status: 'Open',
      amount: '$45,000,000',
      category: '新能源建设',
      description: '马来西亚槟城50MW太阳能农场的EPC总承包合同。'
    },
    {
      id: 'A0000015',
      title: 'Educational Tablets for Primary Schools - 5000 Units',
      titleZh: '小学教育平板电脑采购(5000台)',
      type: '联合国采购',
      organization: 'UNICEF (联合国儿童基金会)',
      country: '肯尼亚',
      publishDate: '2025-01-05',
      deadline: '2025-02-15',
      status: 'Open',
      amount: '$750,000',
      category: '教育设备',
      description: '为肯尼亚农村地区小学供应5000台加固型平板电脑。'
    },
    {
      id: 'C0000087',
      title: 'Emergency Water Purification Systems',
      titleZh: '应急水净化系统采购',
      type: '其他采购',
      organization: '国际红十字会',
      country: '越南',
      publishDate: '2025-01-11',
      deadline: '2025-02-28',
      status: 'Open',
      amount: '$1,200,000',
      category: '应急救援',
      description: '用于越南湄公河三角洲洪水救援行动的移动水净化装置。'
    },
    {
      id: 'A0000001',
      title: 'Supply of Medical Equipment for Emergency Field Hospitals',
      titleZh: '应急野战医院医疗设备供应项目',
      type: '联合国采购',
      organization: 'UNOPS (联合国项目事务署)',
      country: '苏丹',
      publishDate: '2025-01-10',
      deadline: '2025-02-20',
      status: 'Open',
      amount: '$2,500,000',
      category: '医疗设备',
      description: '为苏丹达尔富尔地区部署的野战医院采购X光机、呼吸机、手术套件等医疗设备。'
    },
    {
      id: 'B0000023',
      title: 'Construction of 50MW Solar Power Plant Phase II',
      titleZh: '50兆瓦太阳能发电站二期建设工程',
      type: '国际政府采购',
      organization: '马来西亚能源部',
      country: '马来西亚',
      publishDate: '2025-01-08',
      deadline: '2025-03-01',
      status: 'Open',
      amount: '$45,000,000',
      category: '新能源建设',
      description: '马来西亚槟城50MW太阳能农场的EPC总承包合同。'
    },
    {
      id: 'A0000015',
      title: 'Educational Tablets for Primary Schools - 5000 Units',
      titleZh: '小学教育平板电脑采购(5000台)',
      type: '联合国采购',
      organization: 'UNICEF (联合国儿童基金会)',
      country: '肯尼亚',
      publishDate: '2025-01-05',
      deadline: '2025-02-15',
      status: 'Open',
      amount: '$750,000',
      category: '教育设备',
      description: '为肯尼亚农村地区小学供应5000台加固型平板电脑。'
    },
    {
      id: 'C0000087',
      title: 'Emergency Water Purification Systems',
      titleZh: '应急水净化系统采购',
      type: '其他采购',
      organization: '国际红十字会',
      country: '越南',
      publishDate: '2025-01-11',
      deadline: '2025-02-28',
      status: 'Open',
      amount: '$1,200,000',
      category: '应急救援',
      description: '用于越南湄公河三角洲洪水救援行动的移动水净化装置。'
    },
 {
      id: 'A0000001',
      title: 'Supply of Medical Equipment for Emergency Field Hospitals',
      titleZh: '应急野战医院医疗设备供应项目',
      type: '联合国采购',
      organization: 'UNOPS (联合国项目事务署)',
      country: '苏丹',
      publishDate: '2025-01-10',
      deadline: '2025-02-20',
      status: 'Open',
      amount: '$2,500,000',
      category: '医疗设备',
      description: '为苏丹达尔富尔地区部署的野战医院采购X光机、呼吸机、手术套件等医疗设备。'
    },
    {
      id: 'B0000023',
      title: 'Construction of 50MW Solar Power Plant Phase II',
      titleZh: '50兆瓦太阳能发电站二期建设工程',
      type: '国际政府采购',
      organization: '马来西亚能源部',
      country: '马来西亚',
      publishDate: '2025-01-08',
      deadline: '2025-03-01',
      status: 'Open',
      amount: '$45,000,000',
      category: '新能源建设',
      description: '马来西亚槟城50MW太阳能农场的EPC总承包合同。'
    },
    {
      id: 'A0000015',
      title: 'Educational Tablets for Primary Schools - 5000 Units',
      titleZh: '小学教育平板电脑采购(5000台)',
      type: '联合国采购',
      organization: 'UNICEF (联合国儿童基金会)',
      country: '肯尼亚',
      publishDate: '2025-01-05',
      deadline: '2025-02-15',
      status: 'Open',
      amount: '$750,000',
      category: '教育设备',
      description: '为肯尼亚农村地区小学供应5000台加固型平板电脑。'
    },
    {
      id: 'C0000087',
      title: 'Emergency Water Purification Systems',
      titleZh: '应急水净化系统采购',
      type: '其他采购',
      organization: '国际红十字会',
      country: '越南',
      publishDate: '2025-01-11',
      deadline: '2025-02-28',
      status: 'Open',
      amount: '$1,200,000',
      category: '应急救援',
      description: '用于越南湄公河三角洲洪水救援行动的移动水净化装置。'
    },
    {
      id: 'A0000001',
      title: 'Supply of Medical Equipment for Emergency Field Hospitals',
      titleZh: '应急野战医院医疗设备供应项目',
      type: '联合国采购',
      organization: 'UNOPS (联合国项目事务署)',
      country: '苏丹',
      publishDate: '2025-01-10',
      deadline: '2025-02-20',
      status: 'Open',
      amount: '$2,500,000',
      category: '医疗设备',
      description: '为苏丹达尔富尔地区部署的野战医院采购X光机、呼吸机、手术套件等医疗设备。'
    },
    {
      id: 'B0000023',
      title: 'Construction of 50MW Solar Power Plant Phase II',
      titleZh: '50兆瓦太阳能发电站二期建设工程',
      type: '国际政府采购',
      organization: '马来西亚能源部',
      country: '马来西亚',
      publishDate: '2025-01-08',
      deadline: '2025-03-01',
      status: 'Open',
      amount: '$45,000,000',
      category: '新能源建设',
      description: '马来西亚槟城50MW太阳能农场的EPC总承包合同。'
    },
    {
      id: 'A0000015',
      title: 'Educational Tablets for Primary Schools - 5000 Units',
      titleZh: '小学教育平板电脑采购(5000台)',
      type: '联合国采购',
      organization: 'UNICEF (联合国儿童基金会)',
      country: '肯尼亚',
      publishDate: '2025-01-05',
      deadline: '2025-02-15',
      status: 'Open',
      amount: '$750,000',
      category: '教育设备',
      description: '为肯尼亚农村地区小学供应5000台加固型平板电脑。'
    },
    {
      id: 'C0000087',
      title: 'Emergency Water Purification Systems',
      titleZh: '应急水净化系统采购',
      type: '其他采购',
      organization: '国际红十字会',
      country: '越南',
      publishDate: '2025-01-11',
      deadline: '2025-02-28',
      status: 'Open',
      amount: '$1,200,000',
      category: '应急救援',
      description: '用于越南湄公河三角洲洪水救援行动的移动水净化装置。'
    }
  ];

  // 2. 活动数据 (之前漏掉的)
  const activities = [
    {
      id: 'ACT001',
      title: '2025全球公共采购高峰论坛',
      date: '2025-03-15',
      location: '上海国际会议中心',
      type: '亨嘉之会',
      isPaid: true,
      price: 299,
      image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
      description: '汇聚联合国采购官、国际组织代表，解读2025年全球公共采购趋势'
    },
    {
      id: 'ACT002',
      title: '如何参与联合国项目投标 - 实战培训',
      date: '2025-02-10',
      location: '线上直播 (Zoom)',
      type: '公益行',
      isPaid: false,
      image: 'https://images.unsplash.com/photo-1591115765373-5207764f72e7?w=800',
      description: '免费公益培训，手把手教你注册UNGM、准备投标文件'
    },
    {
      id: 'ACT003',
      title: '东南亚制造业基地考察团(7天6夜)',
      date: '2025-04-01',
      location: '越南 & 马来西亚',
      type: '出海考察',
      isPaid: true,
      price: 19999,
      image: 'https://images.unsplash.com/photo-1552733407-5d5c46c3bb3b?w=800',
      description: '实地考察胡志明、吉隆坡工业园，对接当地政府及服务商资源'
    },
    {
      id: 'ACT004',
      title: '医疗器械行业沙龙 - 打入国际市场',
      date: '2025-02-28',
      location: '北京',
      type: '行业沙龙',
      isPaid: true,
      price: 199,
      image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800',
      description: '医疗器械企业专场，分享CE认证、FDA认证及联合国采购案例'
    }
  ];

  // 3. 服务网点数据 (之前漏掉的)
  const servicePoints = [
    { id: 'SP1', city: '上海', country: '中国', lat: 31.23, lng: 121.47, manager: '总部运营中心', services: ['标书翻译', '服务商注册', '法律咨询'] },
    { id: 'SP2', city: '吉隆坡', country: '马来西亚', lat: 3.13, lng: 101.68, manager: '李经理', services: ['清关服务', '本地化支持'] },
    { id: 'SP3', city: '内罗毕', country: '肯尼亚', lat: -1.29, lng: 36.82, manager: 'Amani女士', services: ['物流协调', '当地资源对接'] },
    { id: 'SP4', city: '日内瓦', country: '瑞士', lat: 46.20, lng: 6.14, manager: 'Smith先生', services: ['联合国对接', '标前咨询'] },
    { id: 'SP5', city: '迪拜', country: '阿联酋', lat: 25.20, lng: 55.27, manager: 'Ahmed先生', services: ['中东市场拓展'] },
    { id: 'SP6', city: '悉尼', country: '澳大利亚', lat: -33.86, lng: 151.20, manager: 'Johnson女士', services: ['大洋洲项目支持'] }
  ];

  return {
    tenders,
    activities,
    servicePoints
  };
};