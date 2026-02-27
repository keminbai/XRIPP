<!-- 文件路径: app/pages/member/ad-promotion.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部 Hero (商业暗金风格) -->
    <!-- ✅ 修正：Hero区域独立，不包含操作按钮 -->
    <div class="relative bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 rounded-2xl p-8 overflow-hidden text-white shadow-xl">
      <!-- 动态光效背景 -->
      <div class="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-10"></div>
      <div class="absolute -right-20 -top-20 w-96 h-96 bg-orange-500/20 rounded-full blur-[100px]"></div>
      <div class="absolute left-10 bottom-0 w-64 h-64 bg-blue-500/10 rounded-full blur-[80px]"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-orange-500/20 border border-orange-400/30 text-orange-300 text-xs font-bold mb-4 backdrop-blur-sm">
            <el-icon><DataLine /></el-icon> TRAFFIC & GROWTH
          </div>
          <h1 class="text-3xl font-bold mb-2 tracking-wide">品牌广告投放控制台</h1>
          <p class="text-slate-300 text-sm max-w-xl leading-relaxed">
            精准触达 4600+ 活跃会员企业与联合国采购官。
            <br>全站平均点击率 (CTR) <span class="text-orange-400 font-bold font-mono">3.5%</span>，助力品牌全球曝光。
          </p>
        </div>
      </div>
    </div>

    <!-- 主体容器 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden min-h-[600px] relative transition-all duration-300">
      
      <!-- =========================== -->
      <!-- 视图 A: 投放管理列表 (Dashboard模式) -->
      <!-- =========================== -->
      <transition name="fade-slide" mode="out-in">
        <div v-if="viewMode === 'list'" key="list" class="p-0">
          
          <!-- 1. 顶部筛选工具栏 (科学布局) -->
          <div class="p-6 border-b border-slate-100 bg-slate-50/50">
            <div class="flex flex-col md:flex-row gap-4">
              <!-- 搜索框 -->
              <div class="flex-grow min-w-0">
                <el-input 
                  v-model="searchKeyword" 
                  placeholder="搜索广告计划名称..." 
                  size="large" 
                  class="w-full shadow-sm"
                  prefix-icon="Search"
                  clearable
                />
              </div>
              
              <!-- 状态筛选 -->
              <div class="flex-shrink-0 w-full md:w-48">
                <el-select 
                  v-model="filterStatus" 
                  placeholder="全部状态" 
                  size="large" 
                  class="w-full shadow-sm" 
                  clearable
                >
                  <el-option label="全部" value="" />
                  <el-option label="🚀 投放中" value="active" />
                  <el-option label="⏳ 审核中" value="auditing" />
                  <el-option label="⏹️ 已结束" value="ended" />
                  <el-option label="❌ 已驳回" value="rejected" />
                </el-select>
              </div>

              <!-- 查询按钮 -->
              <div class="flex-shrink-0">
                <el-button type="primary" size="large" class="w-full md:w-auto px-8 shadow-sm" :icon="Search">
                  查询
                </el-button>
              </div>
            </div>
          </div>

          <!-- 2. 操作栏 (✅ 修正：放在主体白色容器内，筛选栏下方) -->
          <div class="px-6 py-4 bg-white border-b border-slate-100 flex justify-between items-center">
            <div class="flex items-center gap-3">
              <h3 class="text-lg font-bold text-slate-800">投放计划列表</h3>
              <span class="px-2.5 py-0.5 rounded-full bg-orange-50 text-orange-600 text-xs font-bold border border-orange-100">
                共 {{ historyData.length }} 条
              </span>
            </div>
            
            <button 
              class="group relative px-6 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold rounded-lg shadow-md hover:shadow-lg hover:scale-105 transition-all duration-300 flex items-center gap-2 overflow-hidden"
              @click="switchToCreate"
            >
              <div class="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
              <el-icon class="text-lg relative z-10"><Plus /></el-icon>
              <span class="relative z-10">新建投放计划</span>
            </button>
          </div>

          <!-- 3. 数据列表 (卡片流) -->
          <div class="p-6 bg-slate-50 min-h-[500px]">
            <div v-if="historyData.length > 0" class="space-y-5">
              
              <div 
                v-for="item in historyData" 
                :key="item.id" 
                class="group bg-white border border-slate-200 rounded-2xl p-0 hover:shadow-lg hover:border-orange-200 transition-all overflow-hidden flex flex-col md:flex-row"
              >
                <!-- 左侧：视觉封面 -->
                <div class="w-full md:w-72 h-48 md:h-auto bg-slate-100 relative overflow-hidden flex-shrink-0">
                  <img :src="item.image" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
                  <div class="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity backdrop-blur-sm">
                    <el-button round size="small" @click="handleView(item)">查看详情</el-button>
                  </div>
                  <div class="absolute top-3 left-3">
                    <el-tag :type="getStatusTag(item.status)" effect="dark" class="shadow-sm border-0 font-bold">
                      {{ getStatusText(item.status) }}
                    </el-tag>
                  </div>
                </div>

                <!-- 右侧：数据与信息 -->
                <div class="flex-grow p-6 flex flex-col justify-between">
                  <div class="flex justify-between items-start mb-4">
                    <div>
                      <h3 class="font-bold text-lg text-slate-900 mb-2 flex items-center gap-2">
                        {{ item.title }}
                        <span v-if="item.planName" class="px-2 py-0.5 rounded-md bg-orange-50 text-orange-600 text-xs font-normal border border-orange-100">{{ item.planName }}</span>
                      </h3>
                      <div class="text-xs text-slate-500 flex gap-4">
                        <span class="flex items-center gap-1"><el-icon><Calendar /></el-icon> 提交：{{ item.date }}</span>
                        <span class="flex items-center gap-1"><el-icon><Timer /></el-icon> 周期：{{ item.duration }}个月</span>
                      </div>
                    </div>
                    <el-dropdown trigger="click">
                      <el-button circle plain><el-icon><More /></el-icon></el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item @click="handleView(item)">查看详情</el-dropdown-item>
                          <el-dropdown-item v-if="item.status === 'rejected'" @click="handleEdit(item)">重新编辑</el-dropdown-item>
                          <el-dropdown-item divided class="text-red-500" @click="handleDelete(item)">删除记录</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>

                  <!-- 核心数据看板 -->
                  <div v-if="['active', 'ended'].includes(item.status)" class="grid grid-cols-3 gap-4 bg-slate-50 rounded-xl p-4 border border-slate-100">
                    <div class="text-center">
                      <div class="text-xs text-slate-400 uppercase mb-1">曝光量 (PV)</div>
                      <div class="text-xl font-bold text-slate-800 font-mono">{{ item.views }}</div>
                    </div>
                    <div class="text-center border-l border-slate-200">
                      <div class="text-xs text-slate-400 uppercase mb-1">点击量 (UV)</div>
                      <div class="text-xl font-bold text-slate-800 font-mono">{{ item.clicks }}</div>
                    </div>
                    <div class="text-center border-l border-slate-200">
                      <div class="text-xs text-slate-400 uppercase mb-1">点击率 (CTR)</div>
                      <div class="text-xl font-bold text-green-600 font-mono">{{ item.ctr }}%</div>
                    </div>
                  </div>
                  
                  <!-- 审核进度 -->
                  <div v-else-if="item.status === 'auditing'" class="bg-blue-50/50 rounded-xl p-4 border border-blue-100">
                    <div class="flex items-center justify-between text-sm mb-2">
                      <span class="font-bold text-blue-700">审核进度</span>
                      <span class="text-blue-500 text-xs">预计 24 小时内完成</span>
                    </div>
                    <el-progress :percentage="60" :stroke-width="8" status="warning" />
                  </div>

                  <!-- 驳回原因 -->
                  <div v-else-if="item.status === 'rejected'" class="bg-red-50 rounded-xl p-4 border border-red-100 flex gap-3 items-start">
                    <el-icon class="text-red-500 text-lg mt-0.5"><Warning /></el-icon>
                    <div class="text-sm">
                      <div class="font-bold text-red-700 mb-1">审核未通过</div>
                      <div class="text-red-600/80">{{ item.rejectReason }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="mt-8 flex justify-center" v-if="historyData.length > 0">
              <el-pagination background layout="prev, pager, next" :total="historyData.length" :page-size="5" />
            </div>

            <!-- 空状态 -->
            <div v-else class="py-24 text-center">
              <div class="w-24 h-24 mx-auto bg-slate-50 rounded-full flex items-center justify-center text-5xl mb-4 text-slate-300">📺</div>
              <h3 class="text-slate-900 font-bold mb-2">暂无投放记录</h3>
              <p class="text-slate-500 text-sm mb-8">开启您的第一次品牌曝光之旅</p>
              <el-button type="primary" size="large" class="px-8" color="#ea580c" @click="switchToCreate">
                <el-icon class="mr-2"><Plus /></el-icon> 立即投放
              </el-button>
            </div>
          </div>
        </div>

        <!-- =========================== -->
        <!-- 视图 B: 新建投放表单 (创作模式) -->
        <!-- =========================== -->
        <div v-else-if="viewMode === 'form'" key="form" class="bg-white flex flex-col h-full">
          
          <!-- 表单头部 -->
          <div class="border-b border-slate-100 px-8 py-5 flex items-center justify-between bg-slate-50/50 sticky top-0 z-20 backdrop-blur-sm">
            <div class="flex items-center gap-4">
              <button 
                class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center hover:bg-orange-50 hover:text-orange-600 transition-all shadow-sm"
                @click="switchToList"
              >
                <el-icon><ArrowLeft /></el-icon>
              </button>
              <div>
                <h2 class="text-lg font-bold text-slate-900">{{ isEdit ? '编辑广告信息' : '创建投放计划' }}</h2>
                <p class="text-xs text-slate-500">Step 1/3: 填写物料信息</p>
              </div>
            </div>
            <div class="flex gap-3">
              <el-button @click="switchToList">取消</el-button>
              <el-button type="primary" color="#ea580c" class="text-white" @click="handleSubmit" :loading="submitting">
                提交审核
              </el-button>
            </div>
          </div>

          <!-- 表单内容 -->
          <div class="p-8 md:p-12 max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
            
            <!-- 左侧：表单配置 -->
            <div class="lg:col-span-2 space-y-6">
              <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <span class="w-6 h-6 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center text-xs">1</span>
                  选择广告位套餐
                </h3>
                <div class="grid grid-cols-3 gap-4">
                  <div 
                    v-for="plan in plans" :key="plan.id"
                    class="border-2 rounded-xl p-4 cursor-pointer transition-all text-center relative hover:-translate-y-1"
                    :class="selectedPlan === plan.id ? 'border-orange-500 bg-orange-50' : 'border-slate-100 hover:border-orange-300 bg-white'"
                    @click="selectedPlan = plan.id"
                  >
                    <div class="text-xs text-slate-500 mb-1">{{ plan.name }}</div>
                    <div class="text-xl font-bold text-slate-900 font-mono">¥{{ plan.price }}</div>
                    <div class="text-[10px] text-slate-400 mt-2">{{ plan.desc }}</div>
                    <div v-if="plan.recommend" class="absolute top-0 right-0 bg-red-500 text-white text-[10px] px-2 py-0.5 rounded-bl-lg">HOT</div>
                  </div>
                </div>
              </div>

              <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100">
                <h3 class="font-bold text-slate-800 mb-4 flex items-center gap-2">
                  <span class="w-6 h-6 rounded-full bg-slate-100 text-slate-600 flex items-center justify-center text-xs">2</span>
                  上传广告物料
                </h3>
                <el-form :model="formData" label-position="top" size="large">
                  <el-form-item label="广告标题 (公司简称)" required>
                    <el-input v-model="formData.companyName" placeholder="例如：华为云" maxlength="10" show-word-limit />
                  </el-form-item>
                  <el-form-item label="广告语 (Slogan)" required>
                    <el-input v-model="formData.description" type="textarea" :rows="3" maxlength="50" show-word-limit placeholder="简短有力，吸引点击..." />
                  </el-form-item>
                  <el-form-item label="宣传图 (Banner)" required>
                    <el-upload
                      class="w-full" drag action="#" :auto-upload="false" :limit="1"
                      :on-change="(file) => formData.image = URL.createObjectURL(file.raw)"
                    >
                      <el-icon class="el-icon--upload text-orange-400"><upload-filled /></el-icon>
                      <div class="el-upload__text text-sm">点击或拖拽上传 (1200x400px)</div>
                    </el-upload>
                  </el-form-item>
                  <div class="grid grid-cols-2 gap-6">
                    <el-form-item label="投放时长">
                      <el-input-number v-model="formData.duration" :min="1" :max="12" class="!w-full">
                        <template #suffix>个月</template>
                      </el-input-number>
                    </el-form-item>
                    <el-form-item label="联系电话" required>
                      <el-input v-model="formData.phone" />
                    </el-form-item>
                  </div>
                </el-form>
              </div>
            </div>

            <!-- 右侧：实时预览 -->
            <div class="lg:col-span-1">
              <div class="sticky top-24">
                <div class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3 flex items-center gap-2">
                  <el-icon><View /></el-icon> 实时效果预览
                </div>
                <div class="bg-white rounded-xl shadow-xl border border-slate-200 overflow-hidden transform transition-all">
                  <div class="h-6 bg-slate-100 border-b border-slate-200 flex items-center px-3 gap-1.5">
                    <div class="w-2 h-2 rounded-full bg-red-400"></div>
                    <div class="w-2 h-2 rounded-full bg-yellow-400"></div>
                    <div class="w-2 h-2 rounded-full bg-green-400"></div>
                  </div>
                  <div class="h-40 bg-slate-100 relative group cursor-pointer">
                    <img v-if="formData.image" :src="formData.image" class="w-full h-full object-cover" />
                    <div v-else class="w-full h-full flex flex-col items-center justify-center text-slate-300">
                      <el-icon class="text-3xl mb-1"><Picture /></el-icon>
                      <span class="text-xs">图片预览区</span>
                    </div>
                    <div class="absolute top-2 right-2 bg-black/40 backdrop-blur px-2 py-0.5 rounded text-[10px] text-white/90">广告</div>
                  </div>
                  <div class="p-4">
                    <div class="flex justify-between items-center mb-2">
                      <h4 class="font-bold text-slate-900 text-sm line-clamp-1">{{ formData.companyName || '广告标题' }}</h4>
                      <span class="text-[10px] text-orange-500 bg-orange-50 px-1.5 rounded border border-orange-100">推荐</span>
                    </div>
                    <p class="text-xs text-slate-500 line-clamp-2 mb-3 h-8">{{ formData.description || '此处显示您的广告语，精彩文案能带来更多点击...' }}</p>
                    <button class="w-full py-1.5 bg-blue-600 text-white text-xs font-bold rounded-md">立即查看</button>
                  </div>
                </div>

                <div class="mt-6 bg-slate-800 rounded-xl p-5 text-white shadow-lg">
                  <div class="flex justify-between items-center mb-4 border-b border-slate-700 pb-4">
                    <span class="text-sm text-slate-300">套餐单价</span>
                    <span class="font-mono">¥{{ planPrice }}</span>
                  </div>
                  <div class="flex justify-between items-center mb-1">
                    <span class="text-sm text-slate-300">投放时长</span>
                    <span class="font-mono">x {{ formData.duration }} 月</span>
                  </div>
                  <div class="flex justify-between items-center pt-4">
                    <span class="text-sm font-bold">总计费用</span>
                    <span class="text-2xl font-bold text-orange-400 font-mono">¥{{ totalAmount.toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </transition>

    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="广告投放详情" width="600px" align-center class="custom-dialog">
      <div v-if="currentItem" class="space-y-5">
        <div class="aspect-video rounded-lg overflow-hidden bg-slate-100 shadow-inner">
          <img :src="currentItem.image" class="w-full h-full object-cover" />
        </div>
        <div class="bg-slate-50 rounded-xl p-4 border border-slate-100 grid grid-cols-2 gap-4 text-sm">
          <div><span class="text-slate-400 block text-xs mb-1">广告标题</span><span class="font-bold">{{ currentItem.title }}</span></div>
          <div><span class="text-slate-400 block text-xs mb-1">当前状态</span><el-tag size="small" :type="getStatusTag(currentItem.status)">{{ getStatusText(currentItem.status) }}</el-tag></div>
          <div><span class="text-slate-400 block text-xs mb-1">投放套餐</span>{{ currentItem.planName }}</div>
          <div><span class="text-slate-400 block text-xs mb-1">总费用</span>¥{{ (currentItem.duration * 5999).toLocaleString() }}</div>
        </div>
        <div class="text-xs text-slate-500 bg-white p-3 border border-slate-200 rounded-lg">
          <strong>广告语：</strong> {{ currentItem.description || '无详细描述' }}
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false" class="w-full">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Star, View, User, TrendCharts, CircleCheck, Close, Plus,
  Phone, Message, CreditCard, InfoFilled, Search, ArrowLeft,
  Promotion, UploadFilled, Warning, More, Calendar, Timer, Picture, DataLine
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'member' })

const viewMode = ref<'list' | 'form'>('list')
const isEdit = ref(false)
const submitting = ref(false)
const detailVisible = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')

const plans = [
  { id: 'basic', name: '基础版', price: 2999, desc: '侧边栏展示' },
  { id: 'pro', name: '专业版', price: 5999, desc: '首页轮播展示', recommend: true },
  { id: 'premium', name: '旗舰版', price: 9999, desc: '全站多点位展示' }
]

const selectedPlan = ref('pro')
const formData = ref(getEmptyForm())

function getEmptyForm() {
  return { id: null, companyName: '', description: '', phone: '', email: '', duration: 1, image: '' }
}

const historyList = ref([
  {
    id: 101, title: '上海宏大2025新品发布', planName: '专业版', duration: 3, 
    status: 'active', date: '2025-01-10', 
    image: 'https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=800',
    views: '125,040', clicks: '3,420', ctr: '2.7'
  },
  {
    id: 102, title: '宁波高新科技企业宣传', planName: '基础版', duration: 1, 
    status: 'auditing', date: '2026-01-20', 
    image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800',
    views: '-', clicks: '-', ctr: '-'
  },
  {
    id: 103, title: '冬季促销活动推广', planName: '旗舰版', duration: 1, 
    status: 'rejected', date: '2024-12-01', rejectReason: '图片含有违规水印，请去除后重新提交',
    image: 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=800',
    views: '-', clicks: '-', ctr: '-'
  }
])

const historyData = computed(() => {
  return historyList.value.filter(item => {
    const matchKey = !searchKeyword.value || item.title.includes(searchKeyword.value)
    const matchStatus = !filterStatus.value || item.status === filterStatus.value
    return matchKey && matchStatus
  })
})

const currentItem = ref(null)

const planPrice = computed(() => plans.find(p => p.id === selectedPlan.value)?.price || 0)
const totalAmount = computed(() => planPrice.value * formData.value.duration)

const switchToCreate = () => {
  isEdit.value = false
  formData.value = getEmptyForm()
  viewMode.value = 'form'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const switchToList = () => {
  viewMode.value = 'list'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getStatusTag = (status: string) => ({ active: 'success', auditing: 'warning', ended: 'info', rejected: 'danger' }[status] || 'info')
const getStatusText = (status: string) => ({ active: '投放中', auditing: '审核中', ended: '已结束', rejected: '已驳回' }[status] || status)

const handleEdit = (item: any) => {
  isEdit.value = true
  formData.value = { 
    id: item.id, companyName: item.title, description: '模拟回填描述...', 
    phone: '13800000000', email: '', duration: item.duration, image: item.image 
  }
  viewMode.value = 'form'
}

const handleView = (item: any) => {
  currentItem.value = item
  detailVisible.value = true
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm('确定删除该投放记录吗？', '提示', { type: 'warning' }).then(() => {
    historyList.value = historyList.value.filter(i => i.id !== item.id)
    ElMessage.success('已删除')
  })
}

const handleSubmit = () => {
  if (!formData.value.companyName) return ElMessage.warning('请填写广告标题')
  
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    const newItem = {
      id: Date.now(), title: formData.value.companyName, 
      planName: plans.find(p => p.id === selectedPlan.value)?.name, 
      duration: formData.value.duration, 
      status: 'auditing', 
      date: new Date().toISOString().split('T')[0],
      image: formData.value.image || 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=800',
      views: '-', clicks: '-', ctr: '-'
    }
    
    if (isEdit.value && formData.value.id) {
      const idx = historyList.value.findIndex(i => i.id === formData.value.id)
      if (idx !== -1) historyList.value[idx] = { ...historyList.value[idx], ...newItem, status: 'auditing' }
    } else {
      historyList.value.unshift(newItem)
    }
    
    ElMessage.success('提交成功，请等待审核！')
    switchToList()
  }, 1500)
}
</script>

<style scoped>
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }
:deep(.el-upload-dragger) { padding: 30px; }
:deep(.custom-dialog .el-dialog__body) { padding: 20px; }
</style>