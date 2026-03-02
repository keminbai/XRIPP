<!-- 文件路径: app/pages/member/publish-demand.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 顶部 Hero -->
    <div class="relative bg-gradient-to-r from-slate-800 to-indigo-900 rounded-2xl p-8 overflow-hidden text-white shadow-lg">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-indigo-500/10 skew-x-12"></div>
      <div class="absolute right-16 top-1/2 -translate-y-1/2 w-32 h-32 bg-pink-500/10 rounded-full blur-3xl"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/30 border border-indigo-400/30 text-indigo-200 text-xs font-bold mb-4">
            <el-icon><TrendCharts /></el-icon> B2B MATCHING
          </div>
          <h1 class="text-3xl font-bold mb-2">采购需求管理</h1>
          <p class="text-indigo-100 text-sm max-w-xl leading-relaxed">
            高效管理您的企业采购需求。
            <br><span class="text-white font-bold">VIP会员权益</span>：免费发布需求，平台智能匹配优质服务商。
          </p>
        </div>
        <div class="flex-shrink-0 bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/10">
          <el-icon class="text-4xl text-indigo-300"><Promotion /></el-icon>
        </div>
      </div>
    </div>

    <!-- 主体容器 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden min-h-[600px] relative transition-all duration-300">
      
      <!-- =========================== -->
      <!-- 视图 A: 历史记录列表 (默认) -->
      <!-- =========================== -->
      <transition name="fade-slide" mode="out-in">
        <div v-if="viewMode === 'list'" key="list" class="p-0">
          
          <!-- 1. 顶部筛选工具栏 (最终修正版：修复图标语法 + 优化Flex结构) -->
          <div class="p-6 border-b border-slate-100 bg-slate-50/50">
            <div class="flex flex-col md:flex-row gap-3">
              
              <!-- 搜索框：占据剩余所有空间，增加 min-w-0 防止溢出 -->
              <div class="flex-grow min-w-0">
                <el-input 
                  v-model="searchKeyword" 
                  placeholder="请输入项目标题或需求编号..."
                  size="large" 
                  class="w-full shadow-sm"
                  prefix-icon="Search"
                  clearable
                />
              </div>
              
              <!-- 状态选择：固定宽度 160px -->
              <div class="flex-shrink-0 w-full md:w-40">
                <el-select 
                  v-model="filterStatus" 
                  placeholder="全部状态" 
                  size="large" 
                  class="w-full shadow-sm" 
                  clearable
                >
                  <el-option label="全部" value="" />
                  <el-option label="已发布" value="已发布" />
                  <el-option label="审核中" value="审核中" />
                  <el-option label="已驳回" value="审核驳回" />
                </el-select>
              </div>

              <!-- 查询按钮：修正图标语法 -->
              <div class="flex-shrink-0">
                <el-button type="primary" size="large" class="w-full md:w-auto px-6 shadow-sm" :icon="Search">
                  查询
                </el-button>
              </div>
              
            </div>
          </div>

          <!-- 2. 操作栏 (第二排：统计 + 核心按钮) -->
          <div class="px-6 py-5 flex justify-between items-center">
            <div class="flex items-center gap-3">
              <h3 class="text-lg font-bold text-slate-800">发布历史</h3>
              <span class="px-2.5 py-0.5 rounded-full bg-indigo-50 text-indigo-600 text-xs font-bold border border-indigo-100">
                共 {{ historyData.length }} 条
              </span>
            </div>
            
            <button 
              class="group relative px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg shadow-md hover:shadow-lg transition-all flex items-center gap-2 overflow-hidden"
              @click="switchToCreate"
            >
              <div class="absolute inset-0 bg-white/20 translate-y-full group-hover:translate-y-0 transition-transform duration-300"></div>
              <el-icon><Plus /></el-icon> 发布新需求
            </button>
          </div>

          <!-- 3. 数据表格 -->
          <div class="px-6 pb-8">
            <div v-if="historyData.length > 0" class="rounded-xl border border-slate-200 overflow-hidden shadow-sm">
              <el-table :data="historyData" style="width: 100%" :header-cell-style="{background:'#f8fafc', color:'#475569', fontWeight:'600', height:'50px'}">
                <el-table-column prop="id" label="需求编号" width="140">
                  <template #default="scope"><span class="font-mono text-slate-600 font-bold text-xs">{{ scope.row.id }}</span></template>
                </el-table-column>
                
                <el-table-column prop="title" label="项目标题" min-width="280">
                  <template #default="scope">
                    <div class="py-2">
                      <div class="font-bold text-slate-800 text-sm cursor-pointer hover:text-indigo-600 transition-colors line-clamp-1 mb-1" @click="handleView(scope.row)">
                        {{ scope.row.title }}
                      </div>
                      <div class="flex gap-2">
                        <el-tag size="small" type="info" effect="plain">{{ scope.row.orgType === 'ngo' ? 'NGO' : '企业' }}</el-tag>
                        <el-tag size="small" type="info" effect="plain">{{ scope.row.industry }}</el-tag>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column prop="date" label="发布日期" width="120" sortable />
                
                <el-table-column label="状态" width="120">
                  <template #default="scope">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full" :class="getStatusDot(scope.row.status)"></span>
                      <span class="text-sm">{{ scope.row.status }}</span>
                    </div>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="scope">
                    <div class="flex gap-3">
                      <el-button link type="primary" size="small" @click="handleReuse(scope.row)">
                        <el-icon class="mr-1"><DocumentCopy /></el-icon> 复制
                      </el-button>
                      <el-button link size="small" @click="handleView(scope.row)">详情</el-button>
                      <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 分页 -->
            <div class="mt-6 flex justify-center" v-if="historyData.length > 0">
              <el-pagination background layout="total, prev, pager, next" :total="historyData.length" :page-size="10" />
            </div>

            <!-- 空状态 -->
            <div v-else class="py-20 text-center bg-slate-50 rounded-xl border border-dashed border-slate-200">
              <div class="w-20 h-20 mx-auto bg-white rounded-full flex items-center justify-center text-4xl mb-4 shadow-sm border border-slate-100">📂</div>
              <h3 class="text-slate-900 font-bold mb-1">暂无发布记录</h3>
              <p class="text-slate-500 text-sm mb-6">您还没有发布过任何采购需求</p>
              <el-button type="primary" @click="switchToCreate">立即发布第一条</el-button>
            </div>
          </div>
        </div>

        <!-- =========================== -->
        <!-- 视图 B: 发布表单 (沉浸式) -->
        <!-- =========================== -->
        <div v-else-if="viewMode === 'form'" key="form" class="bg-white">
          
          <!-- 表单头部 -->
          <div class="border-b border-slate-100 p-6 flex items-center justify-between bg-slate-50/50 sticky top-0 z-10 backdrop-blur-sm">
            <div class="flex items-center gap-4">
              <button 
                class="w-10 h-10 rounded-full bg-white border border-slate-200 flex items-center justify-center hover:bg-indigo-50 hover:text-indigo-600 transition-all shadow-sm"
                @click="switchToList"
              >
                <el-icon><ArrowLeft /></el-icon>
              </button>
              <div>
                <h2 class="text-xl font-bold text-slate-900">发布新需求</h2>
                <p class="text-xs text-slate-500">填写详细信息，审核通过后将面向全平台展示</p>
              </div>
            </div>
            <div class="flex gap-3">
              <el-button @click="switchToList" size="large">取消</el-button>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">提交审核</el-button>
            </div>
          </div>

          <!-- 表单内容 -->
          <div class="p-8 md:p-12 max-w-4xl mx-auto">
            <el-form :model="formData" label-position="top" size="large">
              
              <!-- 1. 基础信息 -->
              <div class="bg-white p-8 rounded-xl border border-slate-200 shadow-sm mb-8 relative group hover:border-indigo-200 transition-colors">
                <div class="flex items-center gap-2 mb-6 pb-4 border-b border-slate-100">
                  <span class="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-sm font-bold">1</span>
                  <h3 class="text-lg font-bold text-slate-900">基本信息</h3>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="组织类型" required>
                    <el-select v-model="formData.orgType" class="w-full"><el-option label="国内NGO组织" value="ngo" /><el-option label="会员企业需求" value="enterprise" /></el-select>
                  </el-form-item>
                  <el-form-item label="组织名称" required>
                    <el-input v-model="formData.orgName" placeholder="企业或机构全称" />
                  </el-form-item>
                </div>
                <el-form-item label="项目标题" required>
                  <el-input v-model="formData.title" placeholder="简明扼要，25字以内" maxlength="25" show-word-limit />
                </el-form-item>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="发布日期" required>
                    <el-date-picker v-model="formData.publishDate" type="date" class="w-full" placeholder="选择日期" />
                  </el-form-item>
                  <el-form-item label="截止日期" required>
                    <el-date-picker v-model="formData.deadline" type="date" class="w-full" placeholder="选择日期" />
                  </el-form-item>
                </div>
              </div>

              <!-- 2. 需求详情 -->
              <div class="bg-white p-8 rounded-xl border border-slate-200 shadow-sm mb-8 relative group hover:border-indigo-200 transition-colors">
                <div class="flex items-center gap-2 mb-6 pb-4 border-b border-slate-100">
                  <span class="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-sm font-bold">2</span>
                  <h3 class="text-lg font-bold text-slate-900">详细要求</h3>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <el-form-item label="行业分类" required>
                    <el-select v-model="formData.industry" class="w-full" placeholder="请选择">
                      <el-option label="化工" value="chemical" /><el-option label="机械" value="machinery" /><el-option label="电子" value="electronics" />
                      <el-option label="纺织" value="textile" /><el-option label="建筑" value="construction" /><el-option label="医疗" value="medical" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="采购方式" required>
                    <el-select v-model="formData.category" class="w-full" placeholder="请选择">
                      <el-option label="招标 (ITB)" value="itb" /><el-option label="询价 (RFQ)" value="rfq" /><el-option label="征求建议 (RFP)" value="rfp" />
                    </el-select>
                  </el-form-item>
                </div>
                <el-form-item label="需求摘要" required>
                  <el-input v-model="formData.summary" type="textarea" :rows="6" maxlength="1000" show-word-limit placeholder="请详细描述采购物资规格、数量、质量标准、交付要求等..." />
                </el-form-item>
              </div>

              <!-- 3. 附件与推送 -->
              <div class="bg-white p-8 rounded-xl border border-slate-200 shadow-sm relative group hover:border-indigo-200 transition-colors">
                <div class="flex items-center gap-2 mb-6 pb-4 border-b border-slate-100">
                  <span class="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center text-sm font-bold">3</span>
                  <h3 class="text-lg font-bold text-slate-900">附件与服务</h3>
                </div>
                
                <el-upload class="upload-demo mb-8" drag multiple :limit="3" action="#" :auto-upload="false" :file-list="fileList">
                  <el-icon class="el-icon--upload text-indigo-400"><upload-filled /></el-icon>
                  <div class="el-upload__text text-slate-600">拖拽文件到此处，或 <em class="text-indigo-600 font-bold">点击上传</em></div>
                  <template #tip><div class="el-upload__tip text-slate-400">支持 .doc, .pdf, .xls 格式，单个文件不超过 20MB</div></template>
                </el-upload>

                <div class="bg-slate-50 p-5 rounded-xl flex items-center justify-between border border-slate-100">
                  <div>
                    <div class="font-bold text-slate-800 text-sm flex items-center gap-2">
                      <el-icon class="text-indigo-600"><Message /></el-icon> 启用智能短信推送
                    </div>
                    <div class="text-xs text-slate-500 mt-1">系统将自动匹配相关行业的优质服务商并发送短信提醒</div>
                  </div>
                  <el-switch v-model="formData.enableSms" size="large" active-text="开启" inactive-text="关闭" />
                </div>
              </div>

            </el-form>
          </div>
        </div>
      </transition>

    </div>

    <!-- 详情查看弹窗 -->
    <el-dialog v-model="previewVisible" title="需求详情预览" width="700px" align-center class="custom-dialog">
      <div v-if="currentViewItem" class="space-y-6">
        <div class="bg-slate-50 p-5 rounded-xl border border-slate-100">
          <div class="flex justify-between items-start mb-2">
            <h3 class="font-bold text-xl text-slate-900 leading-tight">{{ currentViewItem.title }}</h3>
            <el-tag :type="getStatusType(currentViewItem.status)">{{ currentViewItem.status }}</el-tag>
          </div>
          <div class="text-xs text-slate-500 font-mono">编号: {{ currentViewItem.demandNo || currentViewItem.id }}</div>
        </div>

        <div class="grid grid-cols-2 gap-y-4 gap-x-8 text-sm">
          <div><span class="text-slate-400 block mb-1">发布组织</span><span class="font-medium">{{ currentViewItem.orgName }}</span></div>
          <div><span class="text-slate-400 block mb-1">发布日期</span><span class="font-medium">{{ currentViewItem.publishDate || currentViewItem.date }}</span></div>
          <div><span class="text-slate-400 block mb-1">截止日期</span><span class="font-medium text-red-500">{{ currentViewItem.deadline }}</span></div>
          <div><span class="text-slate-400 block mb-1">所属行业</span><span class="font-medium">{{ currentViewItem.industry }}</span></div>
        </div>

        <div>
          <span class="text-slate-400 block mb-2 text-sm font-bold">需求摘要</span>
          <div class="bg-white p-4 rounded-lg border border-slate-100 text-slate-600 text-sm leading-relaxed whitespace-pre-wrap">
            {{ currentViewItem.summary }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false" size="large" class="w-full">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import {
  TrendCharts, Promotion, Plus, Search, DocumentCopy,
  ArrowLeft, UploadFilled, View, Message
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '~/utils/request'

definePageMeta({ layout: 'member' })

// --- 状态控制 ---
const viewMode = ref<'list' | 'form'>('list')
const submitting = ref(false)
const loading = ref(false)
const previewVisible = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const fileList = ref([])

// --- 表单数据 ---
const formData = ref(getEmptyForm())

function getEmptyForm() {
  return {
    orgType: 'enterprise', orgName: '', title: '', publishDate: '',
    deadline: '', category: '', industry: '', summary: '', enableSms: false
  }
}

// --- 历史数据 (from API) ---
const historyList = ref<any[]>([])

const loadHistory = async () => {
  loading.value = true
  try {
    const res: any = await apiRequest('/v3/member/demands?page=1&page_size=100')
    historyList.value = Array.isArray(res?.data?.items) ? res.data.items : []
  } catch (e: any) {
    historyList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadHistory)

const historyData = computed(() => {
  return historyList.value.filter((item: any) => {
    const matchKey = !searchKeyword.value || (item.title || '').includes(searchKeyword.value) || (item.demandNo || '').includes(searchKeyword.value)
    const matchStatus = !filterStatus.value || item.status === filterStatus.value
    return matchKey && matchStatus
  })
})

const currentViewItem = ref(null)

// --- 交互逻辑 ---
const switchToCreate = () => {
  formData.value = getEmptyForm()
  viewMode.value = 'form'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const switchToList = () => {
  viewMode.value = 'list'
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getStatusDot = (status: string) => {
  if (status === '已发布' || status === '已完成') return 'bg-green-500'
  if (status === '审核中') return 'bg-yellow-500'
  return 'bg-red-500'
}

const getStatusType = (status: string) => {
  if (status === '已发布' || status === '已完成') return 'success'
  if (status === '审核中') return 'warning'
  return 'danger'
}

const handleReuse = (row: any) => {
  formData.value = {
    orgType: row.orgType || 'enterprise',
    orgName: row.orgName || '',
    title: row.title || '',
    publishDate: '',
    deadline: '',
    category: row.category || '',
    industry: row.industry || '',
    summary: row.summary || '',
    enableSms: false
  }
  viewMode.value = 'form'
  ElMessage.success('已载入历史数据，请设定新的日期后提交')
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleView = (row: any) => {
  currentViewItem.value = row
  previewVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该记录吗？', '警告', { type: 'warning' })
    await apiRequest(`/v3/member/demands/${row.id}`, { method: 'DELETE' })
    historyList.value = historyList.value.filter((i: any) => i.id !== row.id)
    ElMessage.success('已删除')
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

const handleSubmit = async () => {
  if (!formData.value.title || !formData.value.summary) return ElMessage.warning('请填写必填项')

  submitting.value = true
  try {
    const payload: any = { ...formData.value }
    // format dates to yyyy-MM-dd string
    if (payload.publishDate) payload.publishDate = new Date(payload.publishDate).toISOString().split('T')[0]
    if (payload.deadline) payload.deadline = new Date(payload.deadline).toISOString().split('T')[0]

    await apiRequest('/v3/member/demands', { method: 'POST', body: payload })
    ElMessage.success('提交成功！')
    await loadHistory()
    switchToList()
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }
:deep(.el-upload-dragger) { padding: 30px; }
:deep(.custom-dialog .el-dialog__body) { padding: 20px 30px; }
</style>