<!-- 文件路径: app/pages/member/certificates.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 顶部 Hero：证书下载 -->
    <div class="relative bg-gradient-to-r from-emerald-900 to-slate-900 rounded-2xl p-8 overflow-hidden text-white shadow-lg">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-emerald-500/10 skew-x-12"></div>
      <div class="absolute right-10 top-1/2 -translate-y-1/2 w-32 h-32 bg-green-500/20 rounded-full blur-3xl"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/30 border border-emerald-400/30 text-emerald-200 text-xs font-bold mb-4">
            <el-icon><Medal /></el-icon> ACHIEVEMENT
          </div>
          <h1 class="text-3xl font-bold mb-2">荣誉证书下载中心</h1>
          <p class="text-emerald-100 text-sm max-w-xl leading-relaxed">
            查看并下载您在 XRIPP 平台获得的各类电子证书。
            <br>包含：平台服务商证书、联合国供应商入库证书、公益赞助证书等。
          </p>
        </div>
        <div class="flex-shrink-0 bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/10">
          <el-icon class="text-4xl text-emerald-300"><Trophy /></el-icon>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-4 flex items-center gap-4">
      <el-select v-model="filterType" placeholder="证书类型" class="w-48" clearable>
        <el-option label="全部" value="" />
        <el-option label="供应商证书" value="supplier" />
        <el-option label="会员证书" value="member" />
        <el-option label="公益证书" value="charity" />
      </el-select>
      
      <el-select v-model="filterStatus" placeholder="证书状态" class="w-40" clearable>
        <el-option label="全部" value="" />
        <el-option label="可下载" value="ready" />
        <el-option label="待发放" value="pending" />
      </el-select>

      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon> 查询
      </el-button>

      <div class="ml-auto text-sm text-slate-400">
        共 {{ filteredCertificates.length }} 个证书
      </div>
    </div>

    <!-- 证书列表 -->
    <div v-if="filteredCertificates.length === 0" class="bg-white rounded-xl border border-slate-200 p-16 text-center">
      <div class="w-24 h-24 mx-auto bg-slate-50 rounded-full flex items-center justify-center mb-4 text-5xl">
        📜
      </div>
      <h3 class="text-lg font-bold text-slate-900 mb-2">暂无证书</h3>
      <p class="text-slate-500 text-sm">您还没有获得任何证书，完成相关服务后即可获取</p>
    </div>

    <div v-else class="space-y-4">
      <div 
        v-for="cert in filteredCertificates" 
        :key="cert.id"
        class="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-lg transition-all overflow-hidden group"
      >
        <div class="flex items-center gap-6 p-6">
          
          <!-- 左侧图标 -->
          <div class="flex-shrink-0">
            <div class="w-20 h-20 rounded-xl flex items-center justify-center text-4xl transition-transform group-hover:scale-110 duration-300" :class="getCertIconBg(cert.type)">
              {{ getCertIcon(cert.type) }}
            </div>
          </div>

          <!-- 中间信息 -->
          <div class="flex-grow">
            <div class="flex items-center gap-3 mb-2">
              <h4 class="text-lg font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                {{ cert.name }}
              </h4>
              <el-tag :type="cert.status === 'ready' ? 'success' : 'info'" size="small">
                {{ cert.status === 'ready' ? '可下载' : '待发放' }}
              </el-tag>
            </div>

            <div class="flex items-center gap-6 text-sm text-slate-500 mb-3">
              <span class="flex items-center gap-1">
                <el-icon><Calendar /></el-icon> 发放日期: {{ cert.issueDate }}
              </span>
              <span v-if="cert.expireDate" class="flex items-center gap-1">
                <el-icon><Timer /></el-icon> 有效期至: {{ cert.expireDate }}
              </span>
              <span v-else class="flex items-center gap-1 text-green-600">
                <el-icon><CircleCheck /></el-icon> 永久有效
              </span>
            </div>

            <div class="text-xs text-slate-400">
              订单编号: {{ cert.orderNo }}
            </div>
          </div>

          <!-- 右侧操作 -->
          <div class="flex-shrink-0 flex flex-col gap-2">
            <el-button 
              v-if="cert.status === 'ready'"
              type="primary" 
              size="default"
              @click="handleDownload(cert)"
              class="min-w-[100px]"
            >
              <el-icon><Download /></el-icon> 下载证书
            </el-button>
            <el-button 
              v-else
              disabled
              size="default"
              class="min-w-[100px]"
            >
              <el-icon><Clock /></el-icon> 审核中
            </el-button>
            
            <el-button 
              v-if="cert.status === 'ready'"
              link 
              size="small"
              @click="handlePreview(cert)"
            >
              <el-icon><View /></el-icon> 预览
            </el-button>
          </div>

        </div>

        <!-- 底部提示条 (仅待发放显示) -->
        <div v-if="cert.status === 'pending'" class="bg-blue-50 border-t border-blue-100 px-6 py-3 flex items-center gap-2 text-blue-700 text-sm">
          <el-icon><InfoFilled /></el-icon>
          <span>您的申请正在审核中，预计1-3个工作日内完成审核并发放证书</span>
        </div>
      </div>
    </div>

    <!-- 证书申请提示 -->
    <div class="bg-slate-50 rounded-xl p-8 border border-slate-200">
      <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
        <el-icon class="text-brand-600"><QuestionFilled /></el-icon>
        如何获得证书？
      </h3>
      <ul class="space-y-3 text-sm text-slate-600">
        <li class="flex items-start gap-2">
          <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
          <span><strong>平台服务商证书</strong>：完成服务商入驻申请并支付费用后自动生成</span>
        </li>
        <li class="flex items-start gap-2">
          <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
          <span><strong>联合国供应商证书</strong>：提交UNGM注册申请，由我们协助完成注册后发放</span>
        </li>
        <li class="flex items-start gap-2">
          <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
          <span><strong>公益赞助证书</strong>：参与平台公益活动或捐赠后由平台颁发感谢证书</span>
        </li>
      </ul>
      <div class="mt-6 flex gap-4">
        <el-button type="primary" @click="navigateTo('/member/supplier-apply')">
          申请服务商入驻
        </el-button>
        <el-button @click="navigateTo('/services')">
          了解更多服务
        </el-button>
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="证书预览" width="800px">
      <div v-if="selectedCert" class="text-center p-8">
        <div class="w-full h-96 bg-slate-100 rounded-lg flex items-center justify-center mb-4">
          <div class="text-center">
            <div class="text-6xl mb-4">🏆</div>
            <div class="text-2xl font-bold text-slate-900 mb-2">{{ selectedCert.name }}</div>
            <div class="text-sm text-slate-500">证书编号: {{ selectedCert.orderNo }}</div>
          </div>
        </div>
        <p class="text-xs text-slate-400">*此为示意图，实际证书请下载后查看</p>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownload(selectedCert)">
          <el-icon><Download /></el-icon> 下载证书
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Medal, Search, Calendar, Timer, CircleCheck, Download, Clock, View,
  InfoFilled, QuestionFilled, Right
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'member' })

const filterType = ref('')
const filterStatus = ref('')
const previewVisible = ref(false)
const selectedCert = ref(null)

// 模拟证书数据
const certificates = ref([
  {
    id: 1,
    name: 'XRIPP平台服务商证书（战略级）',
    type: 'supplier',
    orderNo: '2026011312345678',
    issueDate: '2026-01-13',
    expireDate: '2029-01-13',
    status: 'ready'
  },
  {
    id: 2,
    name: 'UNGM联合国供应商入库证书',
    type: 'member',
    orderNo: 'UNGM-CN-2026-001234',
    issueDate: '2025-12-20',
    expireDate: null,
    status: 'ready'
  },
  {
    id: 3,
    name: '2025年度公益赞助荣誉证书',
    type: 'charity',
    orderNo: 'CHARITY-2025-5678',
    issueDate: '2025-12-31',
    expireDate: null,
    status: 'ready'
  },
  {
    id: 4,
    name: 'XRIPP平台服务商证书（普通级）',
    type: 'supplier',
    orderNo: '2026011412345679',
    issueDate: '待审核',
    expireDate: null,
    status: 'pending'
  }
])

const filteredCertificates = computed(() => {
  return certificates.value.filter(cert => {
    const matchType = !filterType.value || cert.type === filterType.value
    const matchStatus = !filterStatus.value || cert.status === filterStatus.value
    return matchType && matchStatus
  })
})

const getCertIcon = (type) => {
  const icons = {
    supplier: '🏢',
    member: '🌐',
    charity: '❤️'
  }
  return icons[type] || '📜'
}

const getCertIconBg = (type) => {
  const bgs = {
    supplier: 'bg-blue-50 text-blue-600',
    member: 'bg-green-50 text-green-600',
    charity: 'bg-red-50 text-red-600'
  }
  return bgs[type] || 'bg-slate-50 text-slate-600'
}

const handleSearch = () => {
  ElMessage.success('查询完成')
}

const handlePreview = (cert) => {
  selectedCert.value = cert
  previewVisible.value = true
}

const handleDownload = (cert) => {
  ElMessage.success(`正在下载《${cert.name}》...`)
  // 实际下载逻辑
  setTimeout(() => {
    ElMessage.info('证书下载完成！已保存至"下载"文件夹')
  }, 1500)
}
</script>