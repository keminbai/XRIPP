<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\members\un-audit.vue
  版本: V1.0 完整功能版 (2026-01-29)
  
  ✅ 核心功能:
  1. [简化流程] 提交申请表 → 审核中 → 审核通过/驳回
  2. [完整表单] 基于真实联合国企业信息审核表的所有字段
  3. [审核操作] 查看详情、通过/驳回、证书上传
  4. [状态管理] 待审核、已通过、已驳回、已上传证书
  5. [证书下载] 审核通过后可下载联合国供应商证书
  
  📋 依据文档: 联合国企业信息审核表.pdf + 客户需求简化流程
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
          <div class="text-xs text-slate-400 mt-1">{{ stat.subLabel }}</div>
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
            <h3 class="text-lg font-bold text-slate-800">联合国供应商入库审核</h3>
            <p class="text-xs text-slate-500 mt-1">审核企业联合国供应商资格申请，管理证书上传与下载</p>
          </div>
          <el-button 
            type="success" 
            plain 
            @click="handleBatchApprove" 
            :disabled="selectedRows.length === 0"
          >
            <el-icon class="mr-2"><Check /></el-icon> 批量通过({{ selectedRows.length }})
          </el-button>
        </div>

        <!-- Tab切换 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mb-4">
          <el-tab-pane name="pending">
            <template #label>
              <span class="flex items-center gap-2">
                待审核
                <el-badge :value="pendingList.length" type="danger" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="approved">
            <template #label>
              <span class="flex items-center gap-2">
                已通过
                <el-tag size="small" type="success" effect="plain" round>{{ approvedList.length }}</el-tag>
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
        <div class="flex gap-3 flex-wrap">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索公司名称/联系人..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.companyType" placeholder="公司类型" class="w-40" clearable>
            <el-option label="生产商" value="3" />
            <el-option label="贸易商" value="4" />
            <el-option label="服务（Services）" value="6" />
            <el-option label="咨询公司" value="2" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="w-64"
            clearable
          />
          <el-button type="primary" plain @click="handleSearch">
            <el-icon class="mr-1"><Search /></el-icon> 查询
          </el-button>
          <el-button plain @click="handleReset">
            <el-icon class="mr-1"><RefreshLeft /></el-icon> 重置
          </el-button>
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-1"><Download /></el-icon> 导出
          </el-button>
        </div>
      </div>

      <!-- 审核列表表格 -->
      <div class="p-6">
        <el-table 
          v-loading="apiLoading"
          :data="filteredAuditList" 
          style="width: 100%" 
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" v-if="activeTab === 'pending'" />
          
          <el-table-column prop="applyNo" label="申请编号" width="150">
            <template #default="scope">
              <div class="font-mono text-xs text-slate-600">{{ scope.row.applyNo }}</div>
              <div class="text-xs text-slate-400 mt-0.5">{{ scope.row.applyDate }}</div>
            </template>
          </el-table-column>
          
          <el-table-column label="公司信息" min-width="280">
            <template #default="scope">
              <div class="py-2">
                <div class="font-bold text-slate-800 text-sm">{{ scope.row.companyNameCn }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ scope.row.companyNameEn }}</div>
                <div class="flex gap-2 mt-1">
                  <el-tag size="small" type="primary" effect="plain">{{ getCompanyTypeLabel(scope.row.companyType) }}</el-tag>
                  <span class="text-xs text-slate-400">成立于 {{ scope.row.yearEstablished }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="联系信息" width="200">
            <template #default="scope">
              <div class="text-sm">
                <div class="font-medium text-slate-700">{{ scope.row.contactName }}</div>
                <div class="text-xs text-slate-500 mt-0.5">{{ scope.row.contactPosition }}</div>
                <div class="text-xs text-slate-400 mt-0.5">{{ scope.row.contactPhone }}</div>
                <div class="text-xs text-slate-400 mt-0.5">{{ scope.row.contactEmail }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="主营产品/服务" width="180">
            <template #default="scope">
              <div class="text-xs text-slate-600 line-clamp-3">
                {{ scope.row.mainProducts || '未填写' }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="审核状态" width="120" align="center">
            <template #default="scope">
              <el-tag 
                :type="getStatusType(scope.row.status)" 
                effect="dark"
                size="small"
              >
                {{ scope.row.statusLabel }}
              </el-tag>
              <div class="text-xs text-slate-400 mt-1" v-if="scope.row.auditDate">
                {{ scope.row.auditDate }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="证书状态" width="120" align="center">
            <template #default="scope">
              <div v-if="scope.row.status === 'approved'">
                <el-tag 
                  :type="scope.row.certificateUploaded ? 'success' : 'warning'" 
                  size="small"
                >
                  {{ scope.row.certificateUploaded ? '已上传' : '待上传' }}
                </el-tag>
                <el-button 
                  v-if="scope.row.certificateUploaded"
                  link 
                  type="primary" 
                  size="small"
                  class="mt-1"
                  @click="handleDownloadCert(scope.row)"
                >
                  <el-icon class="mr-1"><Download /></el-icon> 下载
                </el-button>
              </div>
              <span v-else class="text-xs text-slate-400">-</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <div class="flex gap-2 flex-wrap">
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1"><View /></el-icon> 查看详情
                </el-button>
                
                <template v-if="scope.row.status === 'pending'">
                  <el-button link type="success" size="small" @click="handleApprove(scope.row)">
                    <el-icon class="mr-1"><Check /></el-icon> 通过
                  </el-button>
                  <el-button link type="danger" size="small" @click="handleReject(scope.row)">
                    <el-icon class="mr-1"><Close /></el-icon> 驳回
                  </el-button>
                </template>
                
                <el-button 
                  v-if="scope.row.status === 'approved' && !scope.row.certificateUploaded"
                  link 
                  type="warning"
                  size="small" 
                  disabled
                  @click="handleUploadCertificate(scope.row)"
                >
                  <el-icon class="mr-1"><Upload /></el-icon> 证书接口待接入
                </el-button>

                <el-button
                  v-if="scope.row.status === 'rejected'"
                  link
                  type="info"
                  size="small"
                  @click="handleViewRejectReason(scope.row)"
                >
                  <el-icon class="mr-1"><Warning /></el-icon> 查看原因
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination 
            background 
            layout="total, prev, pager, next" 
            :total="filteredAuditList.length"
            :page-size="10"
          />
        </div>
      </div>

    </div>

    <!-- 详情查看弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="`${currentDetailItem?.companyNameCn} - 入库申请详情`" 
      width="1000px"
      top="5vh"
    >
      <div v-if="currentDetailItem" class="space-y-4">
        
        <!-- 基础信息 -->
        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <div class="font-bold text-slate-700">📋 公司基础信息</div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="申请编号">{{ currentDetailItem.applyNo }}</el-descriptions-item>
            <el-descriptions-item label="申请日期">{{ currentDetailItem.applyDate }}</el-descriptions-item>
            <el-descriptions-item label="公司中文名" :span="2">{{ currentDetailItem.companyNameCn }}</el-descriptions-item>
            <el-descriptions-item label="公司英文名" :span="2">
              <span class="font-mono text-sm">{{ currentDetailItem.companyNameEn }}</span>
              <el-tag size="small" type="warning" class="ml-2">请确保使用企业官方核定的标准英文名称</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ currentDetailItem.licenseNumber }}</el-descriptions-item>
            <el-descriptions-item label="公司类型">{{ getCompanyTypeLabel(currentDetailItem.companyType) }}</el-descriptions-item>
            <el-descriptions-item label="成立日期">{{ currentDetailItem.yearEstablished }}</el-descriptions-item>
            <el-descriptions-item label="员工数量">{{ currentDetailItem.employeeCount }} 人</el-descriptions-item>
            <el-descriptions-item label="公司网址" :span="2">
              <a :href="currentDetailItem.website" target="_blank" class="text-blue-600 hover:underline">
                {{ currentDetailItem.website || '未填写' }}
              </a>
            </el-descriptions-item>
            <el-descriptions-item label="公司地址" :span="2">
              {{ currentDetailItem.address }} (邮编: {{ currentDetailItem.postcode }})
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 联系人信息 -->
        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <div class="font-bold text-slate-700">👤 联系人信息</div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="联系人姓名">{{ currentDetailItem.contactName }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ currentDetailItem.contactGender }}</el-descriptions-item>
            <el-descriptions-item label="职位">{{ currentDetailItem.contactPosition }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentDetailItem.contactEmail }}</el-descriptions-item>
            <el-descriptions-item label="审核联系邮箱">{{ currentDetailItem.reviewEmail || currentDetailItem.contactEmail || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="公司座机">{{ currentDetailItem.telephone }}</el-descriptions-item>
            <el-descriptions-item label="手机号码">{{ currentDetailItem.contactPhone }}</el-descriptions-item>
            <el-descriptions-item label="传真号码">{{ currentDetailItem.faxNumber || '未填写' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 所有权与包容性 -->
        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <div class="font-bold text-slate-700">🏢 所有权与包容性信息</div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="所有权类型">{{ getOwnershipLabel(currentDetailItem.ownership) }}</el-descriptions-item>
            <el-descriptions-item label="女性控股">{{ getWomenOwnershipLabel(currentDetailItem.womenOwnership) }}</el-descriptions-item>
            <el-descriptions-item label="残疾包容">{{ getDisabilityInclusionLabel(currentDetailItem.disabilityInclusion) }}</el-descriptions-item>
          </el-descriptions>
          
          <!-- 所有权详细信息 -->
          <div v-if="ownershipSections.length" class="mt-4 space-y-3">
            <div
              v-for="section in ownershipSections"
              :key="section.title"
              class="p-4 bg-slate-50 rounded-lg border border-slate-200"
            >
              <div class="text-sm font-bold text-slate-700 mb-2">{{ section.title }}</div>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item
                  v-for="item in section.items"
                  :key="`${section.title}-${item.label}`"
                  :label="item.label"
                >
                  {{ item.value }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <div v-else-if="currentDetailItem.ownershipDetails" class="mt-4 p-4 bg-slate-50 rounded-lg">
            <div class="text-sm font-bold text-slate-700 mb-2">所有权详细信息：</div>
            <div class="text-sm text-slate-600 whitespace-pre-line">{{ currentDetailItem.ownershipDetails }}</div>
          </div>
        </el-card>

        <!-- 业务信息 -->
        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <div class="font-bold text-slate-700">💼 业务信息</div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="出口经验">{{ currentDetailItem.exportExperience }} 年</el-descriptions-item>
            <el-descriptions-item label="主要出口国" :span="2">{{ currentDetailItem.mainExportCountries || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="相关证书" :span="2">{{ currentDetailItem.certificates || '无' }}</el-descriptions-item>
            <el-descriptions-item label="主营产品/服务" :span="2">
              <div class="text-sm text-slate-700">{{ currentDetailItem.mainProducts }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 合规声明 -->
        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <div class="font-bold text-slate-700">✅ 合规声明</div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="声明确认">
              <el-tag :type="getDeclarationTagType(currentDetailItem)">
                {{ getDeclarationLabel(getDeclarationCode(currentDetailItem)) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="mt-4 p-4 rounded-lg border"
               :class="getDeclarationCode(currentDetailItem) === '1'
                 ? 'bg-blue-50 border-blue-100'
                 : getDeclarationCode(currentDetailItem) === '3'
                 ? 'bg-amber-50 border-amber-100'
                 : 'bg-red-50 border-red-100'">
            <div class="text-xs text-slate-700 leading-6" v-if="getDeclarationCode(currentDetailItem) === '1'">
              申请方声明：符合全部7项条件，并承诺后续变化将立即通知。
            </div>
            <div class="text-xs text-amber-700 leading-6" v-else-if="getDeclarationCode(currentDetailItem) === '3'">
              申请方声明：不确定是否符合7项条件，建议补充说明后再审。
            </div>
            <div class="text-xs text-red-700 leading-6" v-else>
              申请方声明：不符合7项条件，不建议通过当前申请。
            </div>
          </div>
        </el-card>

        <!-- 驳回原因（如果被驳回） -->
        <el-alert 
          v-if="currentDetailItem.status === 'rejected'" 
          type="error" 
          :closable="false"
        >
          <template #title>
            <div class="flex items-center gap-2">
              <el-icon><Close /></el-icon>
              <span class="font-bold">驳回原因</span>
            </div>
          </template>
          <div class="text-sm mt-2">{{ currentDetailItem.rejectReason }}</div>
        </el-alert>

      </div>

      <template #footer v-if="currentDetailItem?.status === 'pending'">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleReject(currentDetailItem)">
          <el-icon class="mr-1"><Close /></el-icon> 驳回申请
        </el-button>
        <el-button type="success" @click="handleApprove(currentDetailItem)">
          <el-icon class="mr-1"><Check /></el-icon> 通过审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 驳回原因弹窗 -->
    <el-dialog 
      v-model="rejectDialogVisible" 
      title="驳回申请" 
      width="600px"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-alert type="warning" :closable="false" class="mb-4">
          <div class="text-sm">驳回后，申请企业可以修改信息重新提交</div>
        </el-alert>

        <el-form-item label="驳回原因" required>
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请详细说明驳回原因，如：英文名称格式不正确、所有权信息不完整等..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="常见原因">
          <el-select 
            v-model="rejectForm.quickReason" 
            placeholder="选择常见原因快速填充"
            @change="handleQuickReasonSelect"
            class="w-full"
          >
            <el-option label="公司英文名称格式不正确或包含中文字符" value="reason1" />
            <el-option label="统一社会信用代码与营业执照不符" value="reason2" />
            <el-option label="所有权信息不完整，需补充详细信息" value="reason3" />
            <el-option label="主营产品/服务描述过于简单，需详细说明" value="reason4" />
            <el-option label="缺少必要的证书文件" value="reason5" />
            <el-option label="联系人信息不完整" value="reason6" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmReject" :loading="submitLoading">
          确认驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 上传证书弹窗 -->
    <el-dialog 
      v-model="uploadCertDialogVisible" 
      title="上传联合国供应商证书" 
      width="600px"
    >
      <el-form :model="uploadCertForm" label-width="120px">
        <el-form-item label="公司名称">
          <el-input :value="currentUploadCertItem?.companyNameCn" disabled />
        </el-form-item>

        <el-alert type="info" :closable="false" class="mb-4">
          <div class="text-sm">
            请上传审核通过后获得的联合国供应商证书（UNGM证书）
          </div>
        </el-alert>

        <el-form-item label="证书文件" required>
          <el-upload
            class="upload-demo"
            action="/api/common/upload"
            :limit="1"
            :on-success="handleCertUploadSuccess"
            :before-upload="beforeCertUpload"
            :file-list="certFileList"
            accept=".pdf,.jpg,.png"
          >
            <el-button type="primary">
              <el-icon class="mr-2"><Upload /></el-icon> 上传证书文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip text-xs text-slate-500">
                支持 PDF/JPG/PNG 格式，大小不超过 5MB
              </div>
            </template>
          </el-upload>
          <div v-if="uploadCertForm.certFile" class="mt-2 text-sm text-green-600">
            ✓ 文件已上传
          </div>
        </el-form-item>

        <el-form-item label="UNGM编号">
          <el-input 
            v-model="uploadCertForm.ungmNumber" 
            placeholder="请输入UNGM供应商编号"
          />
        </el-form-item>

        <el-form-item label="证书有效期">
          <el-date-picker
            v-model="uploadCertForm.validityDate"
            type="date"
            placeholder="选择证书有效期"
            class="w-full"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="uploadCertForm.remark" 
            type="textarea"
            :rows="3"
            placeholder="可填写证书相关说明..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="uploadCertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmUploadCert" :loading="submitLoading">
          确认上传
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Clock, CircleCheck, CircleClose, TrendCharts,
  Check, Close, Search, RefreshLeft, View, Upload, Download, Warning
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// 状态管理
const activeTab = ref('pending')
const detailDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const uploadCertDialogVisible = ref(false)
const submitLoading = ref(false)
const currentDetailItem = ref<any>(null)
const currentUploadCertItem = ref<any>(null)
const currentRejectItem = ref<any>(null)
const selectedRows = ref<any[]>([])
const certFileList = ref<UploadUserFile[]>([])

const filters = ref({
  keyword: '',
  companyType: '',
  dateRange: [] as string[]
})

const rejectForm = ref({
  reason: '',
  quickReason: ''
})

const uploadCertForm = ref({
  certFile: '',
  ungmNumber: '',
  validityDate: '',
  remark: ''
})

// 统计数据（基于真实列表计算）
const stats = computed(() => {
  const totalPending = apiAuditList.value.filter(i => i.status === 'pending').length
  const totalApproved = apiAuditList.value.filter(i => i.status === 'approved').length
  const totalRejected = apiAuditList.value.filter(i => i.status === 'rejected').length
  const total = apiAuditList.value.length
  const passRate = total > 0 ? Math.round((totalApproved / total) * 100) : 0

  return [
    { label: '待审核', value: String(totalPending), subLabel: '实时数据', icon: Clock, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
    { label: '已通过', value: String(totalApproved), subLabel: `通过率 ${passRate}%`, icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
    { label: '已驳回', value: String(totalRejected), subLabel: '实时数据', icon: CircleClose, bgClass: 'bg-red-50', textClass: 'text-red-600' },
    { label: '总申请数', value: String(total), subLabel: '当前筛选前基数', icon: TrendCharts, bgClass: 'bg-blue-50', textClass: 'text-blue-600' }
  ]
})

const normalizeAuditItem = (raw: any) => ({
  ...raw,
  companyNameCn: raw.companyNameCn ?? raw.companyNameZh ?? '',
  companyNameEn: raw.companyNameEn ?? '',
  companyType: String(raw.companyType ?? ''),
  yearEstablished: raw.yearEstablished ?? raw.establishedDate ?? '',
  employeeCount: raw.employeeCount ?? raw.employees ?? '',
  contactName: raw.contactName ?? '',
  contactPosition: raw.contactPosition ?? raw.contactTitle ?? '',
  contactPhone: raw.contactPhone ?? raw.mobile ?? '',
  contactEmail: raw.contactEmail ?? raw.email ?? '',
  telephone: raw.telephone ?? '',
  faxNumber: raw.faxNumber ?? raw.fax ?? '',
  ownership: String(raw.ownership ?? raw.ownershipTypeCode ?? ''),
  ownershipTypeCode: raw.ownershipTypeCode ?? null,
  privateHasAffiliates: raw.privateHasAffiliates ?? null,

  privateOwnerNameZh: raw.privateOwnerNameZh ?? '',
  privateOwnerNameEn: raw.privateOwnerNameEn ?? '',

  selfCompanyNameZh: raw.selfCompanyNameZh ?? '',
  selfCompanyNameEn: raw.selfCompanyNameEn ?? '',
  selfOwnerName: raw.selfOwnerName ?? '',
  selfCeoName: raw.selfCeoName ?? '',
  selfControllingPerson: raw.selfControllingPerson ?? '',

  parentCompanyNameZh: raw.parentCompanyNameZh ?? '',
  parentCompanyNameEn: raw.parentCompanyNameEn ?? '',
  parentOwnerName: raw.parentOwnerName ?? '',
  parentCeoName: raw.parentCeoName ?? '',
  parentControllingPerson: raw.parentControllingPerson ?? '',

  subCompanyNameZh: raw.subCompanyNameZh ?? '',
  subCompanyNameEn: raw.subCompanyNameEn ?? '',
  subOwnerName: raw.subOwnerName ?? '',
  subCeoName: raw.subCeoName ?? '',
  subControllingPerson: raw.subControllingPerson ?? '',

  groupSelfCompanyNameZh: raw.groupSelfCompanyNameZh ?? '',
  groupSelfCompanyNameEn: raw.groupSelfCompanyNameEn ?? '',
  groupSelfOwnerName: raw.groupSelfOwnerName ?? '',
  groupSelfCeoName: raw.groupSelfCeoName ?? '',
  groupSelfControllingPerson: raw.groupSelfControllingPerson ?? '',

  groupParentCompanyNameZh: raw.groupParentCompanyNameZh ?? '',
  groupParentCompanyNameEn: raw.groupParentCompanyNameEn ?? '',
  groupParentOwnerName: raw.groupParentOwnerName ?? '',
  groupParentCeoName: raw.groupParentCeoName ?? '',
  groupParentControllingPerson: raw.groupParentControllingPerson ?? '',

  groupSubCompanyNameZh: raw.groupSubCompanyNameZh ?? '',
  groupSubCompanyNameEn: raw.groupSubCompanyNameEn ?? '',
  groupSubOwnerName: raw.groupSubOwnerName ?? '',
  groupSubCeoName: raw.groupSubCeoName ?? '',
  groupSubControllingPerson: raw.groupSubControllingPerson ?? '',

  womenOwnership: String(raw.womenOwnership ?? raw.womenOwnedCode ?? ''),
  disabilityInclusion: String(raw.disabilityInclusion ?? raw.disabilityInclusionCode ?? ''),
  declaration: String(raw.declaration ?? raw.declarationSummaryCode ?? ''),
  exportExperience: raw.exportExperience ?? raw.exportYears ?? '',
  mainExportCountries: raw.mainExportCountries ?? raw.exportCountries ?? '',
  reviewEmail: raw.reviewEmail ?? ''
})

// Mock数据 - 基于真实审核表字段
const allAuditList = ref([
  {
    id: 1,
    applyNo: 'UN202601280001',
    applyDate: '2026-01-28',
    companyNameCn: '上海宏大国际贸易有限公司',
    companyNameEn: 'Shanghai Hongda International Trading Co., Ltd.',
    licenseNumber: '91310000MA1234567X',
    companyType: '4', // 贸易商
    yearEstablished: '2015-03-15',
    employeeCount: 150,
    contactName: '张经理',
    contactGender: '男',
    contactPosition: '总经理',
    contactEmail: 'zhang@hongda.com',
    contactPhone: '138****0001',
    telephone: '021-12345678',
    faxNumber: '021-12345679',
    website: 'https://www.hongda.com',
    address: '上海市浦东新区世纪大道1000号',
    postcode: '200120',
    ownership: '1', // 私有
    womenOwnership: '3', // 低于51%
    disabilityInclusion: '2', // 残疾包容
    ownershipDetails: '拥有者：张三\n董事总经理：李四\n母公司：暂无',
    exportExperience: 8,
    mainExportCountries: '美国、德国、日本、澳大利亚',
    certificates: 'ISO9001质量管理体系认证、CE认证、FDA认证',
    mainProducts: '医疗器械、实验室设备、防护用品',
    declaration: '1', // 符合所有条件
    status: 'pending',
    statusLabel: '待审核',
    certificateUploaded: false
  },
  {
    id: 2,
    applyNo: 'UN202601270002',
    applyDate: '2026-01-27',
    companyNameCn: '深圳科技创新股份有限公司',
    companyNameEn: 'Shenzhen Technology Innovation Co., Ltd.',
    licenseNumber: '91440300MA1234567Y',
    companyType: '3', // 生产商
    yearEstablished: '2010-08-20',
    employeeCount: 500,
    contactName: '李总',
    contactGender: '女',
    contactPosition: 'CEO',
    contactEmail: 'li@sztech.com',
    contactPhone: '139****0002',
    telephone: '0755-88888888',
    faxNumber: '',
    website: 'https://www.sztech.com',
    address: '深圳市南山区科技园南路2000号',
    postcode: '518000',
    ownership: '2', // 上市公司
    womenOwnership: '2', // 至少51%
    disabilityInclusion: '2',
    ownershipDetails: '',
    exportExperience: 12,
    mainExportCountries: '美国、欧盟各国、东南亚',
    certificates: 'ISO9001、ISO14001、OHSAS18001',
    mainProducts: '智能硬件、物联网设备、通信设备',
    declaration: '1',
    status: 'approved',
    statusLabel: '已通过',
    auditDate: '2026-01-28',
    certificateUploaded: true,
    certFile: '/certificates/UN202601270002.pdf',
    ungmNumber: 'UNGM-123456'
  },
  {
    id: 3,
    applyNo: 'UN202601260003',
    applyDate: '2026-01-26',
    companyNameCn: '北京服务咨询有限公司',
    companyNameEn: 'Beijing Service Consulting Co., Ltd',
    licenseNumber: '91110000MA1234567Z',
    companyType: '2', // 咨询公司
    yearEstablished: '2018-06-10',
    employeeCount: 80,
    contactName: '王经理',
    contactGender: '男',
    contactPosition: '项目经理',
    contactEmail: 'wang@bjservice.com',
    contactPhone: '136****0003',
    telephone: '010-66666666',
    faxNumber: '',
    website: 'https://www.bjservice.com',
    address: '北京市朝阳区建国路100号',
    postcode: '100020',
    ownership: '1',
    womenOwnership: '1',
    disabilityInclusion: '1',
    ownershipDetails: '拥有者：王五',
    exportExperience: 5,
    mainExportCountries: '日本、韩国、新加坡',
    certificates: '无',
    mainProducts: '管理咨询、培训服务、市场调研',
    declaration: '1',
    status: 'rejected',
    statusLabel: '已驳回',
    auditDate: '2026-01-27',
    rejectReason: '公司英文名称格式不正确，缺少公司类型后缀（Ltd.应为Ltd），请核对并修改后重新提交。另外，主营产品描述过于简单，请详细说明具体的咨询服务领域和典型项目案例。',
    certificateUploaded: false
  }
].map(normalizeAuditItem))

const pendingList = computed(() => apiAuditList.value.filter(item => item.status === 'pending'))
const approvedList = computed(() => apiAuditList.value.filter(item => item.status === 'approved'))
const rejectedList = computed(() => apiAuditList.value.filter(item => item.status === 'rejected'))

const displaySource = computed(() => apiAuditList.value)

const filteredAuditList = computed(() => {
  let list = [...displaySource.value]

  if (activeTab.value === 'pending') {
    list = list.filter(item => item.status === 'pending')
  } else if (activeTab.value === 'approved') {
    list = list.filter(item => item.status === 'approved')
  } else if (activeTab.value === 'rejected') {
    list = list.filter(item => item.status === 'rejected')
  }

  if (filters.value.keyword) {
    const k = filters.value.keyword.trim()
    list = list.filter(item =>
      item.companyNameCn?.includes(k) ||
      item.contactName?.includes(k) ||
      item.applyNo?.includes(k)
    )
  }

  if (filters.value.companyType) {
    list = list.filter(item => String(item.companyType) === String(filters.value.companyType))
  }

  if (filters.value.dateRange?.length === 2) {
    const [start, end] = filters.value.dateRange
    const startDate = new Date(start)
    const endDate = new Date(end)
    list = list.filter(item => {
      const d = new Date(item.applyDate)
      return d >= startDate && d <= endDate
    })
  }

  return list
})

// API 列表与加载方法
const apiAuditList = ref<any[]>([])
const apiLoading = ref(false)

const mapVerificationToRow = (item: any) => ({
  id: item.id,
  applyNo: `MV${String(item.id).padStart(6, '0')}`,
  applyDate: item.submittedAt ? String(item.submittedAt).slice(0, 10) : (item.createdAt ? String(item.createdAt).slice(0, 10) : '-'),
  companyNameCn: item.companyName || '未命名企业',
  companyNameEn: item.companyName || 'N/A',
  companyType: '',
  yearEstablished: '',
  contactName: item.contactName || '-',
  contactPosition: '',
  contactPhone: item.phone || '-',
  contactEmail: '',
  website: '',
  address: '',
  postcode: '',
  mainProducts: item.industry || '',
  auditDate: item.reviewedAt ? String(item.reviewedAt).slice(0, 10) : '',
  rejectReason: item.changeReason || '',
  certificateUploaded: false,
  status: mapStatus(item.verificationStatus),
  statusLabel: mapStatusLabel(item.verificationStatus),
  _raw: item
})

const mapStatus = (s?: string) => {
  switch (s) {
    case 'approved': return 'approved'
    case 'rejected': return 'rejected'
    case 'submitted':
    case 'under_review':
    case 'draft':
    default: return 'pending'
  }
}

const mapStatusLabel = (s?: string) => {
  switch (s) {
    case 'approved': return '已通过'
    case 'rejected': return '已驳回'
    case 'submitted':
    case 'under_review':
    case 'draft':
    default: return '待审核'
  }
}

const loadAuditList = async () => {
  apiLoading.value = true
  try {
    const statusMap: Record<string, string> = {
      pending: 'submitted',
      approved: 'approved',
      rejected: 'rejected'
    }

    const query: any = {
      page: 1,
      page_size: 100
    }

    if (activeTab.value && statusMap[activeTab.value]) {
      query.verification_status = statusMap[activeTab.value]
    }

    if (filters.value?.keyword) {
      query.keyword = filters.value.keyword
    }

    const res = await apiRequest<any>('/v3/admin/member-verifications', { query })
    const items = res?.data?.items || []
    apiAuditList.value = items.map(mapVerificationToRow)
  } catch (e: any) {
    apiAuditList.value = []
    ElMessage.error(e?.message || '读取审核列表失败')
  } finally {
    apiLoading.value = false
  }
}

onMounted(() => {
  loadAuditList()
})

// 工具函数 - 标签映射
const companyTypeMap: Record<string, string> = {
  '1': '授权代理人',
  '2': '咨询公司',
  '3': '生产商',
  '4': '贸易商',
  '5': '软件支持',
  '6': '服务',
  '7': '设计',
  '8': '出版社',
  '9': '研究',
  '10': '审计',
  '11': '非盈利组织'
}

const ownershipMap: Record<string, string> = {
  '1': '私有',
  '2': '上市公司',
  '3': '企业集团的一部分',
  '4': '不适用（女性所属企业）'
}

const womenOwnershipMap: Record<string, string> = {
  '1': '不适用',
  '2': '至少51%女性控制',
  '3': '低于51%女性控制'
}

const disabilityInclusionMap: Record<string, string> = {
  '1': '不明确',
  '2': '残疾人友好型供应商'
}

const declarationMap: Record<string, string> = {
  '1': '符合所有条件',
  '2': '不符合',
  '3': '不确定'
}

const getCompanyTypeLabel = (type: string) => companyTypeMap[type] || '未知'
const getOwnershipLabel = (type: string) => ownershipMap[type] || '未知'
const getWomenOwnershipLabel = (type: string) => womenOwnershipMap[type] || '未知'
const getDisabilityInclusionLabel = (type: string) => disabilityInclusionMap[type] || '未知'
const getDeclarationLabel = (type: string) => declarationMap[type] || '未知'

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'info'
  }
  return map[status] || 'info'
}

const getDeclarationCode = (row: any) =>
  String(row?.declarationSummaryCode ?? row?.declaration ?? '')

const getDeclarationTagType = (row: any) => {
  const code = getDeclarationCode(row)
  if (code === '1') return 'success'
  if (code === '3') return 'warning'
  return 'danger'
}

const toText = (v: any) => String(v ?? '').trim()

const buildOwnershipSections = (row: any) => {
  const sections: Array<{ title: string; items: Array<{ label: string; value: string }> }> = []
  const pushSection = (title: string, items: Array<{ label: string; value: any }>) => {
    const normalized = items
      .map(i => ({ label: i.label, value: toText(i.value) }))
      .filter(i => i.value)
    if (normalized.length) sections.push({ title, items: normalized })
  }

  const code = String(row?.ownershipTypeCode ?? row?.ownership ?? '')
  const hasAff = Number(row?.privateHasAffiliates ?? 0)

  if (code === '1' && hasAff === 0) {
    pushSection('私有企业（无母子附属）', [
      { label: '拥有者（法人）中文', value: row.privateOwnerNameZh },
      { label: 'Owner Name (EN)', value: row.privateOwnerNameEn }
    ])
  }

  if (code === '1' && hasAff === 1) {
    pushSection('贵公司（Your Company）', [
      { label: '公司名称（中）', value: row.selfCompanyNameZh },
      { label: 'Company Name (EN)', value: row.selfCompanyNameEn },
      { label: '拥有者（法人）', value: row.selfOwnerName },
      { label: 'CEO / General Manager', value: row.selfCeoName },
      { label: '控股权人士', value: row.selfControllingPerson }
    ])
    pushSection('母公司（Parent Company）', [
      { label: '公司名称（中）', value: row.parentCompanyNameZh },
      { label: 'Company Name (EN)', value: row.parentCompanyNameEn },
      { label: '拥有者（法人）', value: row.parentOwnerName },
      { label: 'CEO / General Manager', value: row.parentCeoName },
      { label: '控股权人士', value: row.parentControllingPerson }
    ])
    pushSection('子公司/附属公司（Subsidiary/Affiliate）', [
      { label: '公司名称（中）', value: row.subCompanyNameZh },
      { label: 'Company Name (EN)', value: row.subCompanyNameEn },
      { label: '拥有者（法人）', value: row.subOwnerName },
      { label: 'CEO / General Manager', value: row.subCeoName },
      { label: '控股权人士', value: row.subControllingPerson }
    ])
  }

  if (code === '3') {
    pushSection('集团-贵公司（Your Company）', [
      { label: '公司名称（中）', value: row.groupSelfCompanyNameZh },
      { label: 'Company Name (EN)', value: row.groupSelfCompanyNameEn },
      { label: '拥有者（法人）', value: row.groupSelfOwnerName },
      { label: 'CEO / General Manager', value: row.groupSelfCeoName },
      { label: '控股权人士', value: row.groupSelfControllingPerson }
    ])
    pushSection('集团-母公司（Parent Company）', [
      { label: '公司名称（中）', value: row.groupParentCompanyNameZh },
      { label: 'Company Name (EN)', value: row.groupParentCompanyNameEn },
      { label: '拥有者（法人）', value: row.groupParentOwnerName },
      { label: 'CEO / General Manager', value: row.groupParentCeoName },
      { label: '控股权人士', value: row.groupParentControllingPerson }
    ])
    pushSection('集团-子公司/附属公司（Subsidiary/Affiliate）', [
      { label: '公司名称（中）', value: row.groupSubCompanyNameZh },
      { label: 'Company Name (EN)', value: row.groupSubCompanyNameEn },
      { label: '拥有者（法人）', value: row.groupSubOwnerName },
      { label: 'CEO / General Manager', value: row.groupSubCeoName },
      { label: '控股权人士', value: row.groupSubControllingPerson }
    ])
  }

  return sections
}

const ownershipSections = computed(() =>
  currentDetailItem.value ? buildOwnershipSections(currentDetailItem.value) : []
)

// 快速原因选择
const quickReasonMap: Record<string, string> = {
  'reason1': '公司英文名称格式不正确，请确保使用企业官方核定的标准英文名称，注意大小写、空格和标点符号。',
  'reason2': '统一社会信用代码与营业执照不符，请核对后重新填写。',
  'reason3': '所有权信息不完整。根据贵公司所有权类型，需要补充提供拥有者姓名、董事总经理姓名、母公司/子公司信息等详细内容。',
  'reason4': '主营产品/服务描述过于简单。请详细说明具体的产品型号、规格、应用领域或服务内容、服务范围等。',
  'reason5': '缺少必要的证书文件。请上传相关的产品认证、质量管理体系认证、环境管理体系认证等证书扫描件。',
  'reason6': '联系人信息不完整。请补充完整的联系人姓名、职位、邮箱、电话等信息。'
}

// 交互逻辑
const handleTabChange = () => {
  selectedRows.value = []
  filters.value = { keyword: '', companyType: '', dateRange: [] }
  loadAuditList()
}

const handleSearch = () => {
  loadAuditList()
}

const handleReset = () => {
  filters.value = { keyword: '', companyType: '', dateRange: [] }
  ElMessage.info('筛选条件已重置')
  loadAuditList()
}

const csvEscape = (val: any) => {
  const str = String(val ?? '')
  return `"${str.replace(/"/g, '""')}"`
}

const handleExport = () => {
  if (!import.meta.client) return
  const list = selectedRows.value.length ? selectedRows.value : filteredAuditList.value

  const header = [
    '申请编号','申请日期','公司中文名','公司英文名',
    '公司类型','联系人','联系电话','邮箱','状态'
  ]

  const rows = list.map(i => [
    i.applyNo,
    i.applyDate,
    i.companyNameCn,
    i.companyNameEn,
    getCompanyTypeLabel(i.companyType),
    i.contactName,
    i.contactPhone,
    i.contactEmail,
    i.statusLabel
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `联合国入库申请_${new Date().toISOString().slice(0,10)}.csv`
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 100)

  ElMessage.success('导出成功')
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleBatchApprove = () => {
  ElMessageBox.confirm(`确认批量通过 ${selectedRows.value.length} 个申请吗？`, '提示', {
    type: 'success'
  }).then(async () => {
    if (!selectedRows.value.length) return

    submitLoading.value = true
    try {
      const tasks = selectedRows.value.map(row =>
        apiRequest(`/v3/admin/member-verifications/${row.id}/review`, {
          method: 'POST',
          body: { action: 'approve', reason: '' }
        })
      )
      const results = await Promise.allSettled(tasks)
      const successCount = results.filter(r => r.status === 'fulfilled').length
      const failedCount = results.length - successCount
      selectedRows.value = []
      await loadAuditList()

      if (failedCount > 0) {
        ElMessage.warning(`批量审核完成：成功 ${successCount}，失败 ${failedCount}`)
      } else {
        ElMessage.success(`批量审核完成：${successCount} 条`)
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '批量审核失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleViewDetail = (row: any) => {
  currentDetailItem.value = row
  detailDialogVisible.value = true
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm(`确认通过 ${row.companyNameCn} 的申请吗？`, '审核通过', {
    confirmButtonText: '确认通过',
    type: 'success'
  }).then(async () => {
    try {
      submitLoading.value = true
      await apiRequest(`/v3/admin/member-verifications/${row.id}/review`, {
        method: 'POST',
        body: {
          action: 'approve',
          reason: ''
        }
      })
      ElMessage.success('审核通过')
      detailDialogVisible.value = false
      await loadAuditList()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '审核失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleReject = (row: any) => {
  currentRejectItem.value = row
  rejectForm.value = { reason: '', quickReason: '' }
  rejectDialogVisible.value = true
  detailDialogVisible.value = false
}

const handleQuickReasonSelect = (value: string) => {
  if (value && quickReasonMap[value]) {
    rejectForm.value.reason = quickReasonMap[value]
  }
}

const handleConfirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    return ElMessage.warning('请输入驳回原因')
  }

  if (!currentRejectItem.value?.id) {
    return ElMessage.error('缺少申请ID，无法驳回')
  }

  try {
    submitLoading.value = true
    await apiRequest(`/v3/admin/member-verifications/${currentRejectItem.value.id}/review`, {
      method: 'POST',
      body: {
        action: 'reject',
        reason: rejectForm.value.reason
      }
    })

    ElMessage.success('已驳回申请')
    rejectDialogVisible.value = false
    await loadAuditList()
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '驳回失败')
  } finally {
    submitLoading.value = false
  }
}

const handleViewRejectReason = (row: any) => {
  ElMessageBox.alert(row.rejectReason, '驳回原因', {
    confirmButtonText: '知道了',
    type: 'warning'
  })
}

const handleUploadCertificate = (row: any) => {
  ElMessage.info('证书上传接口尚未接入，当前仅展示入口')
  currentUploadCertItem.value = row
  uploadCertForm.value = {
    certFile: '',
    ungmNumber: '',
    validityDate: '',
    remark: ''
  }
  certFileList.value = []
  uploadCertDialogVisible.value = true
}

const beforeCertUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持 PDF/JPG/PNG 格式')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }
  return true
}

const handleCertUploadSuccess: UploadProps['onSuccess'] = (response) => {
  if (response?.code === 200) {
    uploadCertForm.value.certFile = response.data.url
    ElMessage.success('证书文件上传成功')
  }
}

const handleConfirmUploadCert = () => {
  ElMessage.warning('证书提交流程尚未接入后端，当前不可提交')
}

const handleDownloadCert = (row: any) => {
  if (!row?.certFile) {
    ElMessage.warning('暂无可下载证书')
    return
  }
  window.open(row.certFile, '_blank')
}
</script>

<style scoped>
:deep(.el-card__header) {
  padding: 12px 20px;
  background-color: #f8fafc;
}

:deep(.line-clamp-3) {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
