<template>
  <div class="bg-[#f8fafc] min-h-screen pb-20">
    <div class="bg-slate-900 pt-32 pb-12 mb-8 relative overflow-hidden shadow-inner">
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
      <div class="absolute top-0 right-0 w-[600px] h-[600px] bg-brand-600/10 rounded-full blur-[100px] translate-x-1/3 -translate-y-1/2"></div>

      <div class="container mx-auto px-4 relative z-10">
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-2 text-sm text-slate-400">
            <NuxtLink to="/" class="hover:text-white transition-colors flex items-center gap-1">
              <el-icon><HomeFilled /></el-icon> 首页
            </NuxtLink>
            <el-icon class="text-xs"><ArrowRight /></el-icon>
            <NuxtLink to="/services" class="hover:text-white transition-colors">平台服务</NuxtLink>
            <el-icon class="text-xs"><ArrowRight /></el-icon>
            <span class="text-brand-200 font-medium">服务商名录库</span>
          </div>
          <button @click="$router.back()" class="text-xs text-slate-400 hover:text-white flex items-center gap-1 transition-colors">
            <el-icon><ArrowLeft /></el-icon> 返回上一页
          </button>
        </div>

        <div class="max-w-4xl text-left">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-300 text-xs font-bold mb-4 backdrop-blur-md">
            <el-icon><Reading /></el-icon> SERVICE PROVIDER DATABASE
          </div>
          <h1 class="text-3xl md:text-4xl font-bold text-white tracking-tight mb-3">平台服务商名录库</h1>
          <p class="text-slate-400 text-lg font-light leading-relaxed max-w-2xl">
            汇聚优质服务商资源，为企业提供从咨询到交付的全链路协同服务。
          </p>
        </div>
      </div>
    </div>

    <div class="container mx-auto px-4">
      <div class="flex flex-col lg:flex-row gap-8">
        <aside class="w-full lg:w-72 flex-shrink-0">
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 sticky top-24 overflow-hidden">
            <div class="p-5 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
              <span class="font-bold text-slate-800 flex items-center gap-2">
                <el-icon><Filter /></el-icon> 数据筛选
              </span>
              <button class="text-xs text-brand-600 hover:text-brand-800" @click="resetFilters">重置</button>
            </div>

            <div class="p-5 space-y-6">
              <div>
                <label class="text-xs font-bold text-slate-500 uppercase mb-2 block">关键词</label>
                <el-input
                  v-model="filters.keyword"
                  placeholder="输入企业名称或主营业务"
                  prefix-icon="Search"
                  clearable
                />
              </div>

              <div>
                <label class="text-xs font-bold text-slate-500 uppercase mb-2 block">城市查询</label>
                <el-cascader
                  v-model="filters.location"
                  :options="cityOptions"
                  placeholder="选择省份/城市"
                  class="w-full"
                  clearable
                />
              </div>

              <div>
                <label class="text-xs font-bold text-slate-500 uppercase mb-2 block">服务项目</label>
                <el-select v-model="filters.service" placeholder="选择服务项目" class="w-full" clearable>
                  <el-option label="全部服务" value="" />
                  <el-option label="外贸服务" value="trade" />
                  <el-option label="知识产权" value="ip" />
                  <el-option label="跨境企服" value="corp" />
                  <el-option label="出海投资" value="invest" />
                </el-select>
              </div>

              <el-button type="primary" class="w-full !rounded-lg" @click="handleSearch">
                立即查询
              </el-button>
            </div>
          </div>
        </aside>

        <div class="flex-grow">
          <div class="mb-6 flex justify-between items-center bg-white p-4 rounded-xl border border-slate-100 shadow-sm">
            <span class="text-sm text-slate-600">
              共找到 <strong class="text-brand-600 text-lg mx-1">{{ totalItems }}</strong> 家符合条件的企业
            </span>

            <button
              class="group relative px-6 py-2.5 rounded-lg bg-gradient-to-r from-brand-600 to-blue-700 text-white text-sm font-bold shadow-md hover:shadow-lg hover:shadow-brand-500/30 transition-all duration-300 overflow-hidden"
              @click="navigateTo('/member/supplier-apply')"
            >
              <div class="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
              <span class="relative flex items-center gap-2">
                <el-icon><Plus /></el-icon> 立即申请入驻
              </span>
            </button>
          </div>

          <div class="space-y-4">
            <div
              v-for="item in filteredSuppliers"
              :key="item.id"
              class="bg-white p-6 rounded-xl border border-slate-200 hover:shadow-xl hover:border-brand-200 transition-all duration-300 group relative"
            >
              <div class="flex items-start gap-5">
                <div class="w-16 h-16 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-center text-2xl font-bold text-brand-700 group-hover:bg-brand-50 group-hover:scale-105 transition-all flex-shrink-0">
                  {{ item.name.substring(0, 1) }}
                </div>

                <div class="flex-grow min-w-0">
                  <div class="flex justify-between items-start">
                    <div>
                      <h3 class="text-lg font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-1">
                        {{ item.name }}
                      </h3>
                      <div class="flex items-center gap-4 text-xs text-slate-500 mt-2 flex-wrap">
                        <span class="flex items-center gap-1"><el-icon><Location /></el-icon> {{ item.province }} - {{ item.city }}</span>
                        <span class="flex items-center gap-1"><el-icon><Calendar /></el-icon> 入驻：{{ item.joinDate }}</span>
                        <span class="text-brand-600 bg-brand-50 px-2 py-0.5 rounded border border-brand-100 whitespace-nowrap">{{ item.mainBusiness }}</span>
                      </div>
                    </div>
                    <div class="flex gap-1 flex-shrink-0 ml-2">
                      <el-tag size="small" effect="plain" type="success" v-if="item.verified">已认证</el-tag>
                      <el-tag size="small" effect="dark" type="warning" v-if="item.type === 'strategic'">战略级</el-tag>
                    </div>
                  </div>

                  <div class="mt-5 pt-4 border-t border-slate-100 flex gap-3">
                    <button
                      class="px-5 py-2 rounded-lg border border-brand-200 text-brand-600 text-sm font-bold hover:bg-brand-600 hover:text-white hover:border-brand-600 transition-all duration-300 flex items-center gap-1 shadow-sm"
                      @click="handleView(item)"
                    >
                      <el-icon><View /></el-icon> 查看详情表
                    </button>

                    <button
                      class="px-5 py-2 rounded-lg bg-slate-50 text-slate-600 text-sm font-medium hover:bg-slate-100 hover:text-slate-900 transition-all duration-300 flex items-center gap-1"
                      @click="handleDownload(item)"
                      :disabled="downloadingId === item.id"
                    >
                      <el-icon v-if="downloadingId === item.id" class="animate-spin"><Loading /></el-icon>
                      <el-icon v-else><Download /></el-icon>
                      {{ downloadingId === item.id ? '生成中...' : '下载介绍PDF' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 flex justify-center">
            <el-pagination
              v-model:current-page="currentPage"
              background
              layout="prev, pager, next"
              :total="totalItems"
              :page-size="pageSize"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="服务商基本信息表" width="900px" align-center class="custom-dialog">
      <div v-if="currentItem" class="border border-slate-400 text-sm bg-white">
        <div class="flex border-b border-slate-300">
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">基本信息</div>
          <div class="flex-grow">
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">公司性质</div>
              <div class="p-2 flex-grow">{{ currentItem.nature }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">公司名称</div>
              <div class="p-2 flex-grow font-bold text-base">{{ currentItem.name }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">详细地址</div>
              <div class="p-2 flex-grow">{{ currentItem.address }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">资本形态</div>
              <div class="p-2 flex-grow">{{ currentItem.capitalType }}</div>
              <div class="w-24 p-2 border-l border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">注册资金</div>
              <div class="p-2 flex-grow">{{ currentItem.regCapital }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">成立日期</div>
              <div class="p-2 flex-grow">{{ currentItem.joinDate }}</div>
              <div class="w-24 p-2 border-l border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">去年营业额</div>
              <div class="p-2 flex-grow">{{ currentItem.turnover }}</div>
            </div>
            <div class="flex">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">业务介绍</div>
              <div class="p-2 flex-grow text-xs leading-relaxed text-slate-500">主营 {{ currentItem.mainBusiness }}，{{ currentItem.desc || '暂无详细介绍' }}</div>
            </div>
          </div>
        </div>

        <div class="flex border-b border-slate-300">
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">管理信息</div>
          <div class="flex-grow">
            <div class="flex border-b border-slate-300 bg-slate-50">
              <div class="flex-1 p-2 border-r border-slate-300 text-center font-bold text-slate-600">管理岗</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center font-bold text-slate-600">联系人</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center font-bold text-slate-600">邮箱</div>
              <div class="flex-1 p-2 text-center font-bold text-slate-600">电话</div>
            </div>
            <div class="flex">
              <div class="flex-1 p-2 border-r border-slate-300 text-center">销售负责人</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center">{{ currentItem.contact }}</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center text-xs">sales@example.com</div>
              <div class="flex-1 p-2 text-center">{{ currentItem.phone }}</div>
            </div>
          </div>
        </div>

        <div class="flex border-b border-slate-300">
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">制造信息</div>
          <div class="flex-grow">
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50">厂房面积</div>
              <div class="p-2 flex-grow border-r border-slate-300">{{ currentItem.factoryArea }}</div>
              <div class="p-2 flex-grow">{{ currentItem.factoryType }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50">员工构成</div>
              <div class="p-2 flex-grow text-xs">总人数: {{ currentItem.staffTotal }}</div>
            </div>
            <div class="flex">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50">认证体系</div>
              <div class="p-2 flex-grow">{{ currentItem.certs }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-center gap-4 pt-4">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleDownload(currentItem)" :loading="downloadingId === currentItem?.id">
            <el-icon class="mr-1"><Download /></el-icon> 下载 PDF 介绍
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Search, Filter, Location, Calendar, View, Download,
  Reading, Plus, HomeFilled, ArrowRight, ArrowLeft, Loading
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

useHead({ title: '服务商名录库 - XRIPP全球公共采购服务平台' })

const filters = ref({ keyword: '', location: [], service: '', dateRange: [] })
const dialogVisible = ref(false)
const currentItem = ref<any>(null)
const downloadingId = ref(0)
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const cityOptions = [
  { value: 'shanghai', label: '上海市', children: [{ value: 'shanghai', label: '上海市' }] },
  { value: 'beijing', label: '北京市', children: [{ value: 'beijing', label: '北京市' }] },
  { value: 'guangdong', label: '广东省', children: [{ value: 'shenzhen', label: '深圳市' }, { value: 'guangzhou', label: '广州市' }] },
  { value: 'zhejiang', label: '浙江省', children: [{ value: 'hangzhou', label: '杭州市' }] },
  { value: 'jiangsu', label: '江苏省', children: [{ value: 'suzhou', label: '苏州市' }, { value: 'nanjing', label: '南京市' }] },
  { value: 'henan', label: '河南省', children: [{ value: 'zhengzhou', label: '郑州市' }] },
  { value: 'sichuan', label: '四川省', children: [{ value: 'chengdu', label: '成都市' }] },
  { value: 'hubei', label: '湖北省', children: [{ value: 'wuhan', label: '武汉市' }] }
]

const rawSuppliers = ref<any[]>([])

const parseServices = (json: string): string[] => {
  try { return JSON.parse(json) } catch { return json ? [json] : [] }
}

const mapRow = (item: any) => ({
  id: Number(item.id),
  name: item.companyName || '未命名企业',
  mainBusiness: parseServices(item.serviceTypesJson)[0] || '综合服务',
  province: '',
  city: item.cityName || '',
  address: '',
  contact: '',
  phone: '',
  joinDate: item.joinDate || '',
  verified: true,
  type: 'normal',
  nature: '民营',
  capitalType: '',
  regCapital: '',
  turnover: '',
  factoryArea: '',
  factoryType: '',
  staffTotal: 0,
  certs: '',
  desc: '',
  services: parseServices(item.serviceTypesJson)
})

const loadSuppliers = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    const loc = filters.value.location as string[]
    if (loc?.length > 0) {
      const city = loc[loc.length - 1]
      const cityLabel = cityOptions
        .flatMap(p => p.children)
        .find(c => c.value === city)?.label || ''
      if (cityLabel) query.city = cityLabel.replace(/[市省]/g, '')
    }
    const res: any = await apiRequest('/v3/suppliers', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    rawSuppliers.value = items.map(mapRow)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    rawSuppliers.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取服务商列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadSuppliers)

const suppliers = computed(() => rawSuppliers.value)

const locationValueMap: Record<string, string> = {
  shanghai: '上海市',
  beijing: '北京市',
  guangdong: '广东省',
  zhejiang: '浙江省',
  jiangsu: '江苏省',
  henan: '河南省',
  sichuan: '四川省',
  hubei: '湖北省',
  shenzhen: '深圳市',
  guangzhou: '广州市',
  hangzhou: '杭州市',
  suzhou: '苏州市',
  nanjing: '南京市',
  zhengzhou: '郑州市',
  chengdu: '成都市',
  wuhan: '武汉市'
}

const serviceValueMap: Record<string, string[]> = {
  trade: ['出海咨询', '物流报关'],
  ip: ['标书代写'],
  corp: ['认证服务'],
  invest: ['出海咨询']
}

const filteredSuppliers = computed(() => {
  return suppliers.value.filter((item) => {
    // keyword and city are now handled server-side; only province and service remain client-side
    const location = filters.value.location as string[]
    const selectedProvince = location?.[0] ? locationValueMap[location[0]] : ''
    const selectedCity = location?.[1] ? locationValueMap[location[1]] : ''
    const matchProvince = !selectedProvince || item.province === selectedProvince
    const matchCity = !selectedCity || item.city === selectedCity || `${item.city}市` === selectedCity

    const selectedService = filters.value.service as string
    const expectedServices = selectedService ? (serviceValueMap[selectedService] || []) : []
    const matchService = !selectedService || expectedServices.some((s) => item.services?.includes(s))

    return matchProvince && matchCity && matchService
  })
})

const handlePageChange = () => {
  loadSuppliers()
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadSuppliers()
  ElMessage.success('查询完成')
}

const resetFilters = async () => {
  filters.value = { keyword: '', location: [], service: '', dateRange: [] }
  currentPage.value = 1
  await loadSuppliers()
  ElMessage.info('筛选已重置')
}

const handleView = (item: any) => {
  currentItem.value = item
  dialogVisible.value = true
}

const handleDownload = (item: any) => {
  if (!item) return
  ElMessage.warning('PDF 下载接口待后端接入，当前受控降级')
}
</script>

<style scoped>
:deep(.custom-dialog .el-dialog__body) { padding: 20px; }
.border-slate-300 { border-color: #cbd5e1; }
</style>
