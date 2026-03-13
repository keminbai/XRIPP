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
                <el-select v-model="filters.city" placeholder="选择城市" class="w-full" clearable>
                  <el-option v-for="city in cityOptions" :key="city" :label="city" :value="city" />
                </el-select>
              </div>

              <div>
                <label class="text-xs font-bold text-slate-500 uppercase mb-2 block">服务项目</label>
                <el-select v-model="filters.serviceType" placeholder="选择服务项目" class="w-full" clearable>
                  <el-option label="全部服务" value="" />
                  <el-option v-for="item in industryOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </div>

              <div>
                <label class="text-xs font-bold text-slate-500 uppercase mb-2 block">服务商级别</label>
                <el-select v-model="filters.applyType" placeholder="选择级别" class="w-full" clearable>
                  <el-option label="全部级别" value="" />
                  <el-option label="普通服务商" value="normal" />
                  <el-option label="战略服务商" value="strategic" />
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
                  <img
                    v-if="item.coverImageUrl"
                    :src="resolveFileUrl(item.coverImageUrl)"
                    :alt="item.name"
                    class="w-full h-full rounded-xl object-cover"
                  />
                  <template v-else>{{ item.name.substring(0, 1) }}</template>
                </div>

                <div class="flex-grow min-w-0">
                  <div class="flex justify-between items-start">
                    <div>
                      <h3 class="text-lg font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-1">
                        {{ item.name }}
                      </h3>
                      <div class="flex items-center gap-4 text-xs text-slate-500 mt-2 flex-wrap">
                        <span class="flex items-center gap-1"><el-icon><Location /></el-icon> {{ item.city }}</span>
                        <span class="flex items-center gap-1"><el-icon><Calendar /></el-icon> 入驻：{{ item.joinDate }}</span>
                        <span class="text-brand-600 bg-brand-50 px-2 py-0.5 rounded border border-brand-100 whitespace-nowrap">{{ item.mainBusiness }}</span>
                      </div>
                    </div>
                    <div class="flex gap-1 flex-shrink-0 ml-2">
                      <el-tag size="small" effect="plain" type="success" v-if="item.verified">已认证</el-tag>
                      <el-tag size="small" effect="dark" type="warning" v-if="item.type === 'strategic'">战略级</el-tag>
                      <el-tag size="small" effect="plain" v-else>普通</el-tag>
                    </div>
                  </div>

                  <div class="mt-3 text-sm text-slate-600 line-clamp-2">
                    {{ item.desc || '暂无公开简介' }}
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
                      {{ downloadingId === item.id ? '打开中...' : '下载介绍PDF' }}
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
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">企业概览</div>
          <div class="flex-grow">
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">服务商级别</div>
              <div class="p-2 flex-grow">{{ currentItem.typeLabel }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">公司名称</div>
              <div class="p-2 flex-grow font-bold text-base">{{ currentItem.name }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">服务城市</div>
              <div class="p-2 flex-grow">{{ currentItem.city }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">主营业务</div>
              <div class="p-2 flex-grow">{{ currentItem.mainBusiness }}</div>
              <div class="w-24 p-2 border-l border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">企业类型</div>
              <div class="p-2 flex-grow">{{ currentItem.nature || '-' }}</div>
            </div>
            <div class="flex border-b border-slate-300">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">成立日期</div>
              <div class="p-2 flex-grow">{{ currentItem.foundDate || '-' }}</div>
              <div class="w-24 p-2 border-l border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">注册资金</div>
              <div class="p-2 flex-grow">{{ currentItem.regCapital }}</div>
            </div>
            <div class="flex">
              <div class="w-24 p-2 border-r border-slate-300 text-slate-600 bg-slate-50 flex items-center">业务介绍</div>
              <div class="p-2 flex-grow text-xs leading-relaxed text-slate-500">主营 {{ currentItem.mainBusiness }}，{{ currentItem.desc || '暂无详细介绍' }}</div>
            </div>
          </div>
        </div>

        <div class="flex border-b border-slate-300">
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">服务能力</div>
          <div class="flex-grow">
            <div class="flex border-b border-slate-300 bg-slate-50">
              <div class="flex-1 p-2 border-r border-slate-300 text-center font-bold text-slate-600">行业分类</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center font-bold text-slate-600">ISO 体系</div>
              <div class="flex-1 p-2 text-center font-bold text-slate-600">公开材料</div>
            </div>
            <div class="flex">
              <div class="flex-1 p-2 border-r border-slate-300 text-center">{{ joinDisplay(currentItem.industryList) }}</div>
              <div class="flex-1 p-2 border-r border-slate-300 text-center">{{ joinDisplay(currentItem.isoList) }}</div>
              <div class="flex-1 p-2 text-center">{{ currentItem.companyPdfUrl ? '已提供 PDF 介绍' : '未提供 PDF' }}</div>
            </div>
          </div>
        </div>

        <div class="flex">
          <div class="w-32 bg-slate-100 p-4 font-bold border-r border-slate-300 flex items-center justify-center text-slate-700">公开资料</div>
          <div class="flex-grow p-4 space-y-4">
            <div v-if="currentItem.coverImageUrl || currentItem.promoImageUrl" class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <img v-if="currentItem.coverImageUrl" :src="resolveFileUrl(currentItem.coverImageUrl)" alt="宣传封面" class="w-full h-48 object-cover rounded-lg border border-slate-200" />
              <img v-if="currentItem.promoImageUrl" :src="resolveFileUrl(currentItem.promoImageUrl)" alt="企业宣传图" class="w-full h-48 object-cover rounded-lg border border-slate-200" />
            </div>
            <div v-else class="text-xs text-slate-500">该服务商暂未公开展示图片资料。</div>
            <div class="text-xs text-slate-500">
              公开名录仅展示服务介绍材料，不展示营业执照、银行许可证等审核附件。
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
import { useGlobalConfig } from '~/composables/useGlobalConfig'

useHead({ title: '服务商名录库 - XRIPP全球公共采购服务平台' })

const { industryOptions, cityOptions } = useGlobalConfig()

const filters = ref({ keyword: '', city: '', serviceType: '', applyType: '' })
const dialogVisible = ref(false)
const currentItem = ref<any>(null)
const downloadingId = ref(0)
const loading = ref(false)

const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const rawSuppliers = ref<any[]>([])

const parseServices = (json: string): string[] => {
  try { return JSON.parse(json) } catch { return json ? [json] : [] }
}

const mapRow = (item: any) => ({
  id: Number(item.id),
  name: item.companyName || '未命名企业',
  mainBusiness: item.mainServiceLabel || parseServices(item.serviceTypesJson)[0] || '综合服务',
  city: item.cityName || '',
  joinDate: item.joinDate || '',
  verified: true,
  type: item.applyType || 'normal',
  typeLabel: item.applyTypeLabel || '普通服务商',
  nature: item.nature || '',
  regCapital: item.regCapital || '',
  foundDate: item.foundDate || '',
  desc: item.intro || '',
  services: Array.isArray(item.serviceTypes) ? item.serviceTypes : parseServices(item.serviceTypesJson),
  industryList: Array.isArray(item.industryList) ? item.industryList : [],
  isoList: Array.isArray(item.isoList) ? item.isoList : [],
  coverImageUrl: item.coverImageUrl || '',
  promoImageUrl: item.promoImageUrl || '',
  companyPdfUrl: item.companyPdfUrl || ''
})

const loadSuppliers = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    if (filters.value.city) query.city = filters.value.city
    if (filters.value.serviceType) query.service_type = filters.value.serviceType
    if (filters.value.applyType) query.apply_type = filters.value.applyType
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
const filteredSuppliers = computed(() => suppliers.value)

const handlePageChange = () => {
  loadSuppliers()
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadSuppliers()
  ElMessage.success('查询完成')
}

const resetFilters = async () => {
  filters.value = { keyword: '', city: '', serviceType: '', applyType: '' }
  currentPage.value = 1
  await loadSuppliers()
  ElMessage.info('筛选已重置')
}

const handleView = async (item: any) => {
  try {
    const res: any = await apiRequest(`/v3/suppliers/${item.id}`)
    currentItem.value = mapRow(res?.data || item)
  } catch (e: any) {
    currentItem.value = item
    ElMessage.warning(e?.message || '读取服务商详情失败，已显示列表快照')
  }
  dialogVisible.value = true
}

const handleDownload = (item: any) => {
  if (!item) return
  if (!item.companyPdfUrl) {
    ElMessage.warning('该服务商暂未公开 PDF 介绍')
    return
  }
  downloadingId.value = item.id
  window.open(resolveFileUrl(item.companyPdfUrl), '_blank', 'noopener,noreferrer')
  setTimeout(() => {
    if (downloadingId.value === item.id) downloadingId.value = 0
  }, 300)
}

const resolveFileUrl = (url?: string) => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/')) return url
  return `/${url}`
}

const joinDisplay = (items?: string[]) => Array.isArray(items) && items.length ? items.join('、') : '-'
</script>

<style scoped>
:deep(.custom-dialog .el-dialog__body) { padding: 20px; }
.border-slate-300 { border-color: #cbd5e1; }
</style>
