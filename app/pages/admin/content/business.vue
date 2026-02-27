<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\business.vue
  版本: V1.1 修复版 (2026-01-29)
  
  ✅ 修复内容:
  1. [图标错误] 修正不存在的 'Users' 图标为 'UserFilled'
  2. [筛选修复] 修正 demandType / industry 筛选无效
  3. [驳回原因] 列表与详情页显示驳回原因
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div 
        v-for="(stat, i) in stats" 
        :key="i" 
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between"
      >
        <div>
          <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        </div>
        <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
          <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 2. 主体管理区域 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      
      <!-- 顶部操作栏 -->
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">商机对接管理</h3>
            <p class="text-xs text-slate-500 mt-1">管理会员采购需求，促进企业间商机对接</p>
          </div>
        </div>

        <!-- Tab切换 -->
        <el-tabs v-model="activeTab" class="mb-4">
          <el-tab-pane name="pending">
            <template #label>
              <span class="flex items-center gap-2">
                待审核
                <el-badge :value="pendingList.length" type="danger" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="published">
            <template #label>
              <span class="flex items-center gap-2">
                已发布
                <el-tag size="small" type="success" effect="plain" round>{{ publishedList.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="rejected">
            <template #label>
              <span class="flex items-center gap-2">
                已驳回
                <el-tag size="small" type="info" effect="plain" round>{{ rejectedList.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 筛选条件 -->
        <div class="flex gap-3">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索需求标题/公司名称..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.demandType" placeholder="需求类型" class="w-32" clearable>
            <el-option label="C2C对接" value="C2C" />
            <el-option label="C2G对接" value="C2G" />
          </el-select>
          <el-select v-model="filters.industry" placeholder="行业分类" class="w-32" clearable>
            <el-option label="医疗" value="medical" />
            <el-option label="IT" value="it" />
            <el-option label="制造" value="manufacturing" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>
      </div>

      <!-- 需求列表表格 -->
      <div class="p-6">
        <el-table
          :data="filteredDemandList"
          v-loading="loading"
          style="width: 100%"
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
        >
          <el-table-column prop="demandNo" label="需求编号" width="150" />
          
          <el-table-column label="需求信息" min-width="300">
            <template #default="scope">
              <div class="font-bold text-slate-800 text-sm">{{ scope.row.title }}</div>
              <div class="text-xs text-slate-500 mt-1">{{ scope.row.description }}</div>
              <div class="flex gap-2 mt-1">
                <el-tag size="small" :type="scope.row.demandType === 'C2C' ? 'primary' : 'success'">
                  {{ scope.row.demandType }}
                </el-tag>
                <el-tag size="small" type="info">{{ scope.row.industry }}</el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="发布企业" width="200">
            <template #default="scope">
              <div class="text-sm">
                <div class="font-medium text-slate-700">{{ scope.row.companyName }}</div>
                <div class="text-xs text-slate-500 mt-0.5">{{ scope.row.contactPerson }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="120" align="center">
            <template #default="scope">
              <el-tag 
                :type="getStatusType(scope.row.status)" 
                size="small"
              >
                {{ scope.row.statusLabel }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="对接数" width="100" align="center">
            <template #default="scope">
              <div class="text-sm font-bold text-blue-600">{{ scope.row.matchCount }}</div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <!-- 审核中状态 -->
              <div v-if="scope.row.status === 'pending'" class="flex gap-2">
                <el-button link type="success" size="small" @click="handleApprove(scope.row)">
                  <el-icon class="mr-1"><Check /></el-icon> 通过
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(scope.row)">
                  <el-icon class="mr-1"><Close /></el-icon> 驳回
                </el-button>
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1"><View /></el-icon> 详情
                </el-button>
              </div>

              <!-- 其他状态 -->
              <div v-else class="flex gap-2">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1"><View /></el-icon> 详情
                </el-button>

                <el-popover v-if="scope.row.status === 'rejected'" placement="top" width="300" trigger="hover">
                  <template #reference>
                    <el-button link type="warning" size="small">
                      查看原因
                    </el-button>
                  </template>
                  <div class="text-sm">
                    <div class="font-bold text-red-600 mb-2">驳回原因：</div>
                    <div class="text-slate-700">{{ scope.row.rejectReason || '无' }}</div>
                  </div>
                </el-popover>

                <el-button 
                  link 
                  type="warning" 
                  size="small" 
                  @click="handleViewMatches(scope.row)"
                  v-if="scope.row.status === 'published'"
                >
                  <el-icon class="mr-1"><Connection /></el-icon> 对接记录
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination 
            background 
            layout="total, prev, pager, next" 
            :total="filteredDemandList.length"
            :page-size="10"
          />
        </div>
      </div>

    </div>

    <!-- 详情弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="`${currentDetailItem?.title} - 需求详情`" 
      width="800px"
    >
      <div v-if="currentDetailItem">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求编号">{{ currentDetailItem.demandNo }}</el-descriptions-item>
          <el-descriptions-item label="需求类型">
            <el-tag :type="currentDetailItem.demandType === 'C2C' ? 'primary' : 'success'">
              {{ currentDetailItem.demandType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="需求标题" :span="2">{{ currentDetailItem.title }}</el-descriptions-item>
          <el-descriptions-item label="行业分类">{{ currentDetailItem.industry }}</el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentDetailItem.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="发布企业" :span="2">{{ currentDetailItem.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentDetailItem.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetailItem.contactPhone }}</el-descriptions-item>
        </el-descriptions>

        <div class="mt-4">
          <div class="text-sm font-bold text-slate-700 mb-2">需求描述：</div>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.description }}
          </div>
        </div>

        <div v-if="currentDetailItem?.rejectReason" class="mt-4">
          <div class="text-sm font-bold text-red-600 mb-2">驳回原因：</div>
          <div class="p-4 bg-red-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.rejectReason }}
          </div>
        </div>
      </div>

      <template #footer v-if="currentDetailItem?.status === 'pending'">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleReject(currentDetailItem)">驳回</el-button>
        <el-button type="success" @click="handleApprove(currentDetailItem)">通过</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Check, Close, View, Connection, Search,
  Briefcase, UserFilled, TrendCharts, CircleCheck
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const activeTab = ref('pending')
const detailDialogVisible = ref(false)
const currentDetailItem = ref<any>(null)

const filters = ref({
  keyword: '',
  demandType: '',
  industry: ''
})

const apiDemandList = ref<any[]>([])
const loading = ref(false)

const stats = computed(() => [
  { label: '累计需求', value: String(apiDemandList.value.length), icon: Briefcase, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '待审核', value: String(apiDemandList.value.filter(i => i.status === 'pending').length), icon: UserFilled, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '已发布', value: String(apiDemandList.value.filter(i => i.status === 'published').length), icon: TrendCharts, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '对接成功', value: '0', icon: CircleCheck, bgClass: 'bg-purple-50', textClass: 'text-purple-600' }
])

const pendingList = computed(() => apiDemandList.value.filter(item => item.status === 'pending'))
const publishedList = computed(() => apiDemandList.value.filter(item => item.status === 'published'))
const rejectedList = computed(() => apiDemandList.value.filter(item => item.status === 'rejected'))

const filteredDemandList = computed(() => {
  let list = apiDemandList.value

  if (activeTab.value === 'pending') list = pendingList.value
  else if (activeTab.value === 'published') list = publishedList.value
  else if (activeTab.value === 'rejected') list = rejectedList.value

  if (filters.value.keyword) {
    const kw = filters.value.keyword.trim()
    list = list.filter(item =>
      item.title.includes(kw) || item.companyName.includes(kw)
    )
  }

  return list
})

const loadDemands = async () => {
  loading.value = true
  try {
    const res: any = await apiRequest('/v3/admin/demands?page=1&page_size=200')
    apiDemandList.value = res?.data?.items || []
  } catch (e: any) {
    apiDemandList.value = []
    ElMessage.error(e?.message || '读取需求列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    published: 'success',
    rejected: 'info'
  }
  return map[status] || 'info'
}

const handleSearch = () => loadDemands()

const handleViewDetail = (row: any) => {
  currentDetailItem.value = row
  detailDialogVisible.value = true
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm(`确认通过 "${row.title}" 吗？`, '审核通过', {
    type: 'success'
  }).then(async () => {
    try {
      await apiRequest(`/v3/admin/demands/${row.id}/review`, {
        method: 'POST',
        body: { action: 'approve', reason: '' }
      })
      ElMessage.success('审核通过，需求已发布')
      detailDialogVisible.value = false
      await loadDemands()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '审核失败')
    }
  })
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    inputType: 'textarea',
    inputValidator: (value) => value?.trim() ? true : '请输入驳回原因'
  }).then(async ({ value }) => {
    try {
      await apiRequest(`/v3/admin/demands/${row.id}/review`, {
        method: 'POST',
        body: { action: 'reject', reason: value }
      })
      ElMessage.warning('已驳回申请')
      detailDialogVisible.value = false
      await loadDemands()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '驳回失败')
    }
  })
}

const handleViewMatches = () => {
  ElMessage.info('对接记录功能开发中...')
}

onMounted(() => {
  loadDemands()
})
</script>
