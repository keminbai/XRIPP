<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\members\orders.vue
  版本: V1.3 (2026-02-04)
  
  ✅ 终极修复记录:
  1. [逻辑] 实现真实分页切分 (paginatedOrderList)，不再是全量展示
  2. [数据] 统一时间格式为 YYYY-MM-DD HH:mm:ss，修复日期筛选 Bug
  3. [交互] 筛选或切换 Tab 时自动重置页码到第一页
  4. [保留] 包含 V1.2 的所有字段补全、发票入口、状态分离功能
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div 
        v-for="(stat, i) in stats" 
        :key="i" 
        class="bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between cursor-pointer"
        @click="handleStatClick(stat.type)"
      >
        <div>
          <div class="text-xs text-slate-500 mb-1">{{ stat.label }}</div>
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
            <h3 class="text-lg font-bold text-slate-800">会员订单管理</h3>
            <p class="text-xs text-slate-500 mt-1">管理前台用户订单和后台服务订单</p>
          </div>
          <el-button type="primary" @click="openCreateServiceOrderDialog">
            <el-icon class="mr-2"><Plus /></el-icon> 创建服务订单
          </el-button>
        </div>

        <!-- Tab切换 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mb-4">
          <el-tab-pane name="all">
            <template #label>
              <span class="flex items-center gap-2">
                全部订单
                <el-tag size="small" type="info" effect="plain" round>{{ allOrders.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="member">
            <template #label>
              <span class="flex items-center gap-2">
                前台用户订单
                <el-tag size="small" type="primary" effect="plain" round>{{ frontendOrders.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="service">
            <template #label>
              <span class="flex items-center gap-2">
                后台服务订单
                <el-tag size="small" type="success" effect="plain" round>{{ backendOrders.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 筛选条件 -->
        <div class="flex gap-3 flex-wrap">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索订单号/公司名称/联系人..." 
            :prefix-icon="Search" 
            class="w-64" 
            clearable 
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-select v-model="filters.orderType" placeholder="订单类型" class="w-40" clearable @change="handleSearch">
            <el-option label="会员购买" value="会员购买" />
            <el-option label="标书购买" value="标书购买" />
            <el-option label="活动报名" value="活动报名" />
            <el-option label="服务商入驻" value="服务商入驻" />
            <el-option label="投标服务" value="投标服务" />
            <el-option label="交付服务" value="交付服务" />
            <el-option label="标书代写" value="标书代写" />
            <el-option label="出海咨询" value="出海咨询" />
          </el-select>

          <el-input 
            v-model="filters.tenderNo" 
            placeholder="关联标的(标书编号)" 
            class="w-48" 
            clearable 
            @keyup.enter="handleSearch"
          />

          <el-select 
            v-model="filters.serviceStage" 
            placeholder="当前阶段" 
            class="w-32" 
            clearable 
            @change="handleSearch"
          >
            <el-option label="咨询阶段" value="咨询阶段" />
            <el-option label="进行阶段" value="进行阶段" />
            <el-option label="完结阶段" value="完结阶段" />
          </el-select>

          <el-select v-model="filters.status" placeholder="订单状态" class="w-32" clearable @change="handleSearch">
            <el-option label="待支付" value="待支付" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
          </el-select>
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="w-64"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleSearch"
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

      <!-- 订单列表表格 (绑定分页数据 paginatedOrderList) -->
      <div class="p-6">
        <el-table 
          :data="paginatedOrderList" 
          style="width: 100%" 
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
          stripe
        >
          <el-table-column prop="orderNo" label="订单号" width="150" fixed>
            <template #default="scope">
              <div class="font-mono text-xs">{{ scope.row.orderNo }}</div>
              <el-tag 
                size="small" 
                :type="scope.row.source === 'frontend' ? 'primary' : 'success'" 
                effect="plain"
                class="mt-1"
              >
                {{ scope.row.source === 'frontend' ? '前台' : '后台' }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="订单信息" min-width="250">
            <template #default="scope">
              <div class="py-2">
                <div class="font-bold text-slate-800 text-sm mb-1">{{ scope.row.orderType }}</div>
                <div class="text-xs text-slate-500">{{ scope.row.orderTitle }}</div>
                <div class="text-xs text-slate-400 mt-1">{{ scope.row.createTime }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="tenderNo" label="关联标的" width="160">
            <template #default="scope">
              <span class="font-mono text-xs text-slate-600">{{ scope.row.tenderNo || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="客户信息" width="200">
            <template #default="scope">
              <div class="text-sm">
                <div class="font-medium text-slate-700">{{ scope.row.companyName }}</div>
                <div class="text-xs text-slate-500 mt-0.5 flex items-center gap-1">
                   {{ scope.row.contactPerson }} | {{ scope.row.contactPhone }}
                </div>
                <el-tag v-if="scope.row.industry" size="small" type="info" class="mt-1 scale-90 origin-left">
                  {{ scope.row.industry }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="订单金额" width="120" align="right">
            <template #default="scope">
              <div class="font-bold text-orange-600 text-sm">¥{{ scope.row.amount }}</div>
              <div class="text-xs text-slate-400 mt-0.5">{{ scope.row.paymentMethod || '-' }}</div>
            </template>
          </el-table-column>

          <el-table-column label="状态 / 进度" width="160">
            <template #default="scope">
              <div class="flex flex-col gap-2">
                <el-tag 
                  :type="getStatusType(scope.row.status)" 
                  effect="dark"
                  size="small"
                  class="w-fit"
                >
                  {{ scope.row.status }}
                </el-tag>
                
                <div v-if="scope.row.source === 'backend' && scope.row.serviceStage" class="w-full">
                  <el-progress 
                    :percentage="getStagePercentage(scope.row.serviceStage)" 
                    :stroke-width="4" 
                    :show-text="false"
                    :color="scope.row.status === '已完成' ? '#67c23a' : '#409eff'"
                  />
                  <div class="text-[10px] text-slate-500 mt-1 flex justify-between">
                    <span>{{ scope.row.serviceStage }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                查看
              </el-button>
              
              <el-button 
                link 
                type="warning" 
                size="small" 
                @click="handleUpdateStage(scope.row)" 
                v-if="scope.row.source === 'backend' && scope.row.status === '进行中'"
              >
                进度
              </el-button>

              <el-button link type="info" size="small" @click="handleUploadInvoice(scope.row)">
                 <el-icon class="mr-1"><Upload /></el-icon>发票
              </el-button>
              
              <el-button 
                link 
                type="success" 
                size="small" 
                @click="handleComplete(scope.row)" 
                v-if="scope.row.status === '进行中'"
              >
                完成
              </el-button>
              
              <el-button 
                link 
                type="danger" 
                size="small" 
                @click="handleCancel(scope.row)" 
                v-if="scope.row.status === '待支付' || scope.row.status === '进行中'"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <!-- 真实分页组件 -->
          <el-pagination 
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background 
            layout="total, sizes, prev, pager, next" 
            :total="filteredOrderList.length"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

    </div>

    <!-- 创建服务订单弹窗 -->
    <el-dialog 
      v-model="createServiceOrderDialogVisible" 
      title="创建服务订单" 
      width="800px" 
      :close-on-click-modal="false"
    >
      <el-form :model="serviceOrderForm" label-width="120px" ref="formRef" :rules="formRules">
        
        <el-alert 
          title="提示" 
          type="info" 
          :closable="false"
          class="mb-4"
        >
          <div class="text-xs">
            后台服务订单用于手动添加线下洽谈的业务，请确保信息准确。
          </div>
        </el-alert>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="订单类型" required prop="orderType">
            <el-select v-model="serviceOrderForm.orderType" class="w-full" placeholder="请选择">
              <el-option label="投标服务" value="投标服务" />
              <el-option label="交付服务" value="交付服务" />
              <el-option label="标书代写" value="标书代写" />
              <el-option label="出海咨询" value="出海咨询" />
            </el-select>
          </el-form-item>

          <el-form-item label="客户选择" required prop="customerId">
            <el-select 
              v-model="serviceOrderForm.customerId" 
              class="w-full" 
              filterable 
              placeholder="搜索会员企业"
              @change="handleCustomerChange"
            >
              <el-option 
                v-for="member in memberOptions" 
                :key="member.id" 
                :label="member.companyName" 
                :value="member.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="公司名称" required prop="companyName">
            <el-input v-model="serviceOrderForm.companyName" placeholder="自动填充或手动输入" />
          </el-form-item>
          <el-form-item label="联系人" required prop="contactPerson">
            <el-input v-model="serviceOrderForm.contactPerson" placeholder="请输入联系人" />
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="联系电话" required prop="contactPhone">
            <el-input v-model="serviceOrderForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="行业类别" required prop="industry">
            <el-select v-model="serviceOrderForm.industry" class="w-full" placeholder="请选择行业">
              <el-option label="制造业" value="制造业" />
              <el-option label="互联网" value="互联网" />
              <el-option label="贸易" value="贸易" />
              <el-option label="服务业" value="服务业" />
            </el-select>
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="邀请码">
             <el-input v-model="serviceOrderForm.invitationCode" placeholder="选填，关联邀请人" :prefix-icon="Key" />
          </el-form-item>
          <el-form-item label="订单金额" required prop="amount">
            <el-input-number 
              v-model="serviceOrderForm.amount" 
              :min="0" 
              :precision="2"
              class="w-full"
            >
              <template #prefix>¥</template>
            </el-input-number>
          </el-form-item>
        </div>

        <el-form-item label="关联标的(标书编号)" prop="tenderNo">
          <el-input 
            v-model="serviceOrderForm.tenderNo" 
            placeholder="请输入标书编号，如 TENDER-2026-001"
          />
        </el-form-item>

        <el-form-item label="订单描述" required prop="orderDesc">
          <el-input 
            v-model="serviceOrderForm.orderDesc" 
            placeholder="请输入订单标题摘要（15字以内）" 
            maxlength="15"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="当前阶段" required prop="serviceStage">
          <el-select v-model="serviceOrderForm.serviceStage" class="w-full">
            <el-option label="咨询阶段" value="咨询阶段" />
            <el-option label="进行阶段" value="进行阶段" />
            <el-option label="完结阶段" value="完结阶段" />
          </el-select>
        </el-form-item>

        <el-form-item label="备注信息">
          <el-input 
            v-model="serviceOrderForm.remark" 
            type="textarea" 
            :rows="2"
            placeholder="可填写具体服务内容、沟通记录等..."
          />
        </el-form-item>

      </el-form>
      
      <template #footer>
        <el-button @click="createServiceOrderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateServiceOrder" :loading="submitLoading">
          创建订单
        </el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="订单详情" 
      width="900px"
    >
      <div v-if="currentOrderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单类型">
            <el-tag size="small">{{ currentOrderDetail.orderType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单来源">
            <el-tag size="small" :type="currentOrderDetail.source === 'frontend' ? 'primary' : 'success'">
              {{ currentOrderDetail.source === 'frontend' ? '前台用户订单' : '后台服务订单' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrderDetail.status)">{{ currentOrderDetail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称" :span="2">{{ currentOrderDetail.companyName }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentOrderDetail.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrderDetail.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="行业" v-if="currentOrderDetail.industry">{{ currentOrderDetail.industry }}</el-descriptions-item>
          <el-descriptions-item label="邀请码" v-if="currentOrderDetail.invitationCode">{{ currentOrderDetail.invitationCode }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="text-orange-600 font-bold text-lg">¥{{ currentOrderDetail.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrderDetail.paymentMethod || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentOrderDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单标题" :span="2">{{ currentOrderDetail.orderTitle }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentOrderDetail.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrderDetail.source === 'backend'" class="mt-6">
          <h4 class="text-sm font-bold mb-3">服务阶段进度</h4>
          <el-steps :active="getStageIndex(currentOrderDetail.serviceStage)" align-center>
            <el-step title="咨询阶段" description="初步沟通需求" />
            <el-step title="进行阶段" description="服务执行中" />
            <el-step title="完结阶段" description="服务已完成" />
          </el-steps>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  ShoppingCart, Wallet, Timer, CircleCheck, CircleClose,
  Plus, Search, RefreshLeft, Download, View, Check, Close, Key, Upload
} from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

definePageMeta({ layout: 'admin' })

// 状态管理
const activeTab = ref('all')
const currentPage = ref(1) // 新增：当前页
const pageSize = ref(10)   // 新增：页容量
const createServiceOrderDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentOrderDetail = ref<any>(null)

const filters = ref({
  keyword: '',
  orderType: '',
  status: '',
  tenderNo: '',
  serviceStage: '',
  dateRange: [] as string[]
})

// 表单数据模型
const serviceOrderForm = ref({
  orderType: '',
  customerId: '',
  orderDesc: '',
  amount: 0,
  serviceStage: '咨询阶段',
  remark: '',
  companyName: '',
  contactPerson: '',
  contactPhone: '',
  industry: '制造业',
  invitationCode: '',
  tenderNo: ''
})

const formRules = {
  orderType: [{ required: true, message: '请选择订单类型', trigger: 'change' }],
  tenderNo: [{ required: false, message: '请输入标书编号', trigger: 'blur' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  orderDesc: [{ required: true, message: '请输入订单描述', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入订单金额', trigger: 'blur' }],
  serviceStage: [{ required: true, message: '请选择当前阶段', trigger: 'change' }],
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const memberOptions = ref([
  { id: 'M001', companyName: '上海宏大贸易有限公司', contactPerson: '张经理', phone: '138****0001', industry: '贸易' },
  { id: 'M002', companyName: '苏州精密制造股份公司', contactPerson: '李总', phone: '139****0002', industry: '制造业' }
])

// 统计数据
const stats = [
  { label: '订单总数', value: '1,258', subLabel: '累计金额 ¥3.2M', type: 'all', icon: ShoppingCart, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '待支付', value: '23', subLabel: '¥45.8K', type: '待支付', icon: Wallet, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '进行中', value: '87', subLabel: '¥182.5K', type: '进行中', icon: Timer, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '已完成', value: '1,102', subLabel: '¥2.9M', type: '已完成', icon: CircleCheck, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '已取消', value: '46', subLabel: '¥72.1K', type: '已取消', icon: CircleClose, bgClass: 'bg-red-50', textClass: 'text-red-600' }
]

// Mock数据 (修复数据格式)
const allOrders = ref([
  {
    id: 1,
    orderNo: 'ORD202601280001',
    source: 'frontend',
    orderType: '会员购买',
    orderTitle: 'VIP会员年费',
    companyName: '上海宏大贸易有限公司',
    contactPerson: '张经理',
    contactPhone: '138****0001',
    industry: '贸易',
    amount: 2999,
    paymentMethod: '微信支付',
    status: '已完成',
    serviceStage: '',
    tenderNo: '', // 没有关联标的
    createTime: '2026-01-28 10:30:25'
  },
  {
    id: 2,
    orderNo: 'SRV202601280002',
    source: 'backend',
    orderType: '投标服务',
    orderTitle: '联合国UNDP项目投标全流程服务',
    companyName: '苏州精密制造股份公司',
    contactPerson: '李总',
    contactPhone: '139****0002',
    industry: '制造业',
    amount: 15800,
    paymentMethod: '-',
    status: '进行中',
    serviceStage: '进行阶段',
    tenderNo: 'TENDER-2026-001',
    createTime: '2026-01-27 14:20:10',
    remark: '包含标书翻译、文件提交、问题澄清等服务'
  },
  {
    id: 4,
    orderNo: 'SRV202601270004',
    source: 'backend',
    orderType: '出海咨询',
    orderTitle: '马来西亚公司注册及税务咨询',
    companyName: '北京国际贸易集团',
    contactPerson: '赵经理',
    contactPhone: '135****0004',
    industry: '贸易',
    amount: 28000,
    paymentMethod: '-',
    status: '进行中',
    serviceStage: '咨询阶段',
    tenderNo: '', // 没有关联标的
    createTime: '2026-01-27 11:45:22',
    remark: '客户计划在马来西亚设立分公司'
  }
])

const frontendOrders = computed(() => allOrders.value.filter(o => o.source === 'frontend'))
const backendOrders = computed(() => allOrders.value.filter(o => o.source === 'backend'))

// 过滤逻辑
const filteredOrderList = computed(() => {
  let list = allOrders.value
  
  if (activeTab.value === 'member') {
    list = frontendOrders.value
  } else if (activeTab.value === 'service') {
    list = backendOrders.value
  }
  
  if (filters.value.keyword) {
    list = list.filter(item => 
      item.orderNo.includes(filters.value.keyword) ||
      item.companyName.includes(filters.value.keyword) ||
      item.contactPerson.includes(filters.value.keyword)
    )
  }
  
  if (filters.value.orderType) {
    list = list.filter(item => item.orderType === filters.value.orderType)
  }
  
  if (filters.value.status) {
    list = list.filter(item => item.status === filters.value.status)
  }

  if (filters.value.tenderNo) {
    list = list.filter(item => (item.tenderNo || '').includes(filters.value.tenderNo))
  }

  if (filters.value.serviceStage) {
    list = list.filter(item => item.serviceStage === filters.value.serviceStage)
  }

  // 日期筛选
  if (filters.value.dateRange?.length === 2) {
    const [start, end] = filters.value.dateRange
    list = list.filter(item => {
      // 兼容 YYYY-MM-DD 和 YYYY/MM/DD 格式对比
      const time = item.createTime.replace(/\//g, '-').split(' ')[0]
      return time >= start && time <= end
    })
  }
  
  return list
})

// 分页切分逻辑 (修复)
const paginatedOrderList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrderList.value.slice(start, end)
})

// 工具函数
const getStatusType = (status: string) => {
  const map: Record<string, string> = { '待支付': 'warning', '进行中': 'primary', '已完成': 'success', '已取消': 'info' }
  return map[status] || 'info'
}

const getStageIndex = (stage: string) => {
  const map: Record<string, number> = { '咨询阶段': 1, '进行阶段': 2, '完结阶段': 3 }
  return map[stage] || 0
}

const getStagePercentage = (stage: string) => {
  const map: Record<string, number> = { '咨询阶段': 33, '进行阶段': 66, '完结阶段': 100 }
  return map[stage] || 0
}

// 标准化时间格式
const formatDate = (date: Date) => {
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 交互逻辑
const handleStatClick = (type: string) => {
  if (type === 'all') filters.value.status = ''
  else filters.value.status = type
  currentPage.value = 1 // 重置页码
}

const handleTabChange = () => {
  filters.value = { keyword: '', orderType: '', status: '', tenderNo: '', serviceStage: '', dateRange: [] }
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1 // 重置页码
  ElMessage.success('查询完成')
}

const handleReset = () => {
  filters.value = { keyword: '', orderType: '', status: '', tenderNo: '', serviceStage: '', dateRange: [] }
  currentPage.value = 1
  ElMessage.info('筛选条件已重置')
}

// 分页处理
const handleSizeChange = () => currentPage.value = 1
const handleCurrentChange = () => {
  // 可以添加回到顶部的逻辑
}

const csvEscape = (val: any) => {
  const str = String(val ?? '')
  return `"${str.replace(/"/g, '""')}"`
}

const handleExport = () => {
  if (!import.meta.client) return
  const header = [
    '订单号','来源','订单类型','关联标的','订单标题','公司名称','联系人','联系电话',
    '订单金额','状态','当前阶段','创建时间'
  ]
  const rows = filteredOrderList.value.map(i => [
    i.orderNo,
    i.source === 'frontend' ? '前台' : '后台',
    i.orderType,
    i.tenderNo || '',
    i.orderTitle,
    i.companyName,
    i.contactPerson,
    i.contactPhone,
    i.amount,
    i.status,
    i.serviceStage || '',
    i.createTime
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `会员订单_${new Date().toISOString().slice(0,10)}.csv`
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 100)

  ElMessage.success('导出成功')
}

const openCreateServiceOrderDialog = () => {
  serviceOrderForm.value = {
    orderType: '', customerId: '', orderDesc: '', amount: 0, serviceStage: '咨询阶段', remark: '',
    companyName: '', contactPerson: '', contactPhone: '', industry: '制造业', invitationCode: ''
  }
  createServiceOrderDialogVisible.value = true
}

const handleCustomerChange = (val: string) => {
  const member = memberOptions.value.find(m => m.id === val)
  if (member) {
    serviceOrderForm.value.companyName = member.companyName
    serviceOrderForm.value.contactPerson = member.contactPerson
    serviceOrderForm.value.contactPhone = member.phone
    serviceOrderForm.value.industry = member.industry
  }
}

// 真实创建订单逻辑 (修复时间格式)
const handleCreateServiceOrder = () => {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (!valid) return ElMessage.warning('请填写必填项')
    
    submitLoading.value = true
    setTimeout(() => {
      const now = new Date()
      // 真实写入表格
      const newOrder = {
        id: Date.now(),
        orderNo: 'SRV' + now.toISOString().replace(/\D/g, '').slice(0, 14),
        source: 'backend',
        orderType: serviceOrderForm.value.orderType,
        orderTitle: serviceOrderForm.value.orderDesc,
        companyName: serviceOrderForm.value.companyName,
        contactPerson: serviceOrderForm.value.contactPerson,
        contactPhone: serviceOrderForm.value.contactPhone,
        industry: serviceOrderForm.value.industry,
        invitationCode: serviceOrderForm.value.invitationCode,
        amount: serviceOrderForm.value.amount,
        paymentMethod: '-',
        status: '进行中',
        serviceStage: serviceOrderForm.value.serviceStage,
        createTime: formatDate(now), // 使用标准格式
        tenderNo: serviceOrderForm.value.tenderNo,
        remark: serviceOrderForm.value.remark
      }
      
      allOrders.value.unshift(newOrder)
      submitLoading.value = false
      ElMessage.success('服务订单创建成功')
      createServiceOrderDialogVisible.value = false
    }, 500)
  })
}

const handleViewDetail = (row: any) => {
  currentOrderDetail.value = row
  detailDialogVisible.value = true
}

const handleUpdateStage = (row: any) => {
  ElMessageBox.prompt('请选择新的阶段', '更新服务阶段', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'select',
    inputOptions: { '咨询阶段': '咨询阶段', '进行阶段': '进行阶段', '完结阶段': '完结阶段' }
  }).then(({ value }) => {
    row.serviceStage = value
    ElMessage.success('阶段已更新')
  })
}

const handleUploadInvoice = (row: any) => {
  ElMessageBox.alert(`请前往财务系统为订单 ${row.orderNo} 关联发票信息`, '上传发票', {
    confirmButtonText: '知道了'
  })
}

const handleComplete = (row: any) => {
  ElMessageBox.confirm('确认完成此订单吗？', '提示', { type: 'success' })
    .then(() => {
      row.status = '已完成'
      if (row.source === 'backend') row.serviceStage = '完结阶段'
      ElMessage.success('订单已完成')
    })
}

const handleCancel = (row: any) => {
  ElMessageBox.confirm('确认取消此订单吗？取消后无法恢复', '提示', { type: 'warning' })
    .then(() => {
      row.status = '已取消'
      ElMessage.success('订单已取消')
    })
}
</script>

<style scoped>
:deep(.el-steps--simple) {
  padding: 0;
  border-radius: 4px;
  font-size: 12px;
}
</style>