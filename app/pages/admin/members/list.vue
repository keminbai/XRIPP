<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\members\list.vue
  版本: V1.3 (2026-02-07)
  
  ✅ 核心修正 (P1):
  1. [福利分配] 升级为完整 Dialog 表单，支持选择福利类型和数量。
  2. [逻辑清洗] 移除所有前端业务计算逻辑，仅保留 API 调用骨架。
  3. [交互] 完善了更多操作的菜单。
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 顶部操作栏 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">会员管理列表</h3>
          <p class="text-xs text-slate-500 mt-1">查看和管理所有会员信息</p>
        </div>
        <div class="flex gap-3">
          <el-button type="success" plain @click="handleExport">
            <el-icon class="mr-2"><Download /></el-icon> 导出列表
          </el-button>
          <el-button type="primary" @click="handleAddMember">
            <el-icon class="mr-2"><Plus /></el-icon> 新增会员
          </el-button>
        </div>
      </div>

      <!-- 筛选条件 -->
      <div class="flex gap-3 flex-wrap items-center">
        <el-input 
          v-model="filters.keyword" 
          placeholder="搜索公司名称/联系人..." 
          :prefix-icon="Search" 
          class="w-56" 
          clearable 
          @keyup.enter="handleSearch"
        />
        
        <el-input 
          v-model="filters.invitationCode" 
          placeholder="邀请码" 
          :prefix-icon="Key" 
          class="w-40" 
          clearable 
        />

        <el-select v-model="filters.industry" placeholder="所属行业" class="w-32" clearable>
          <el-option label="制造业" value="制造业" />
          <el-option label="互联网" value="互联网" />
          <el-option label="贸易" value="贸易" />
          <el-option label="服务业" value="服务业" />
          <el-option label="科技型" value="科技型" />
        </el-select>

        <el-select v-model="filters.level" placeholder="会员等级" class="w-32" clearable>
          <el-option label="SVIP" value="SVIP" />
          <el-option label="VIP" value="VIP" />
          <el-option label="普通会员" value="NORMAL" />
        </el-select>

        <!-- 省份→城市联动 -->
        <el-select v-model="filters.province" placeholder="省份" class="w-32" clearable>
          <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
        </el-select>

        <el-select v-model="filters.city" placeholder="城市" class="w-32" clearable>
          <el-option v-for="c in cityOptions" :key="c" :label="c" :value="c" />
        </el-select>

        <el-select v-model="filters.status" placeholder="状态" class="w-28" clearable>
          <el-option label="正常" value="active" />
          <el-option label="已过期" value="expired" />
          <el-option label="已禁用" value="disabled" />
        </el-select>

        <!-- 国际会员筛选 -->
        <el-select v-model="filters.international" placeholder="国际会员" class="w-28" clearable>
          <el-option label="是" value="yes" />
          <el-option label="否" value="no" />
        </el-select>

        <!-- 入驻时间 -->
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入驻开始"
          end-placeholder="入驻结束"
          value-format="YYYY-MM-DD"
          class="!w-64"
          clearable
        />

        <el-button type="primary" plain @click="handleSearch">查询</el-button>
        <el-button plain @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 会员列表表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6">
        <el-table 
          :data="pagedMemberList" 
          stripe
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          
          <el-table-column label="公司信息" min-width="220">
            <template #default="scope">
              <div class="py-2">
                <div class="font-bold text-slate-800">{{ scope.row.companyName }}</div>
                <div class="text-xs text-slate-500 mt-1 flex items-center gap-2">
                  <span>{{ scope.row.contactPerson }}</span>
                  <span class="w-[1px] h-3 bg-slate-300"></span>
                  <span>{{ scope.row.contactPhone }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="industry" label="行业" width="100" show-overflow-tooltip />

          <el-table-column label="等级" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getLevelTag(scope.row.level)" size="small" effect="light" round>
                {{ scope.row.level }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="invitationCode" label="邀请码" width="100">
             <template #default="scope">
               <span class="font-mono text-slate-500 bg-slate-100 px-2 py-0.5 rounded text-xs">{{ scope.row.invitationCode }}</span>
             </template>
          </el-table-column>

          <el-table-column label="地区" width="120">
            <template #default="scope">
              {{ scope.row.province }} / {{ scope.row.city }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <div class="flex items-center justify-center gap-1.5">
                <span class="w-2 h-2 rounded-full" :class="{
                  'bg-green-500': scope.row.status === 'active',
                  'bg-yellow-500': scope.row.status === 'expired',
                  'bg-slate-400': scope.row.status === 'disabled'
                }"></span>
                <span class="text-xs">{{ scope.row.statusLabel }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="expireDate" label="到期时间" width="110" class-name="text-xs" />

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                详情
              </el-button>
              <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <!-- 🆕 P1修正：点击触发完整 Dialog -->
              <el-button link type="warning" size="small" @click="openWelfareDialog(scope.row)">
                <el-icon class="mr-1"><Present /></el-icon>福利
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleMoreAction(cmd, scope.row)">
                <el-button link type="info" size="small" class="ml-2">
                  更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="toggleStatus">
                      {{ scope.row.status === 'active' ? '禁用账号' : '启用账号' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" class="text-red-500">删除账号</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-between items-center">
          <div class="text-sm text-slate-500">
          已选择 <span class="text-blue-600 font-bold">{{ selectedRows.length }}</span> 项，
            共 <span class="text-slate-900 font-bold">{{ total }}</span> 条数据
          </div>
          <el-pagination 
            background 
            layout="total, prev, pager, next, jumper" 
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
            :page-sizes="[10, 20, 50, 100]"
          />
        </div>
      </div>
    </div>

    <!-- 1. 会员详情弹窗 (只读) -->
    <el-dialog v-model="detailDialogVisible" title="会员详情档案" width="700px" destroy-on-close>
      <div v-if="currentMember" class="p-2">
        <div class="flex items-center gap-4 mb-6 pb-6 border-b border-slate-100">
          <div class="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-2xl font-bold">
            {{ currentMember.companyName ? currentMember.companyName.charAt(0) : '无' }}
          </div>
          <div>
            <h4 class="text-lg font-bold text-slate-800">{{ currentMember.companyName }}</h4>
            <div class="flex gap-2 mt-2">
              <el-tag size="small" type="info">{{ currentMember.industry }}</el-tag>
              <el-tag size="small" :type="getLevelTag(currentMember.level)">{{ currentMember.level }}</el-tag>
            </div>
          </div>
        </div>
        <el-descriptions :column="2" class="mb-4">
          <el-descriptions-item label="邀请码">{{ currentMember.invitationCode }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag size="small" :type="getStatusType(currentMember.status)">{{ currentMember.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentMember.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentMember.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="电子邮箱">{{ currentMember.email }}</el-descriptions-item>
          <el-descriptions-item label="所在城市">{{ currentMember.city }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ currentMember.registerDate }}</el-descriptions-item>
          <el-descriptions-item label="到期时间">{{ currentMember.expireDate }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEditFromDetail(currentMember)">去编辑</el-button>
      </template>
    </el-dialog>

    <!-- 2. 新增/编辑会员表单弹窗 -->
    <el-dialog 
      v-model="formDialogVisible" 
      :title="isEditMode ? '编辑会员信息' : '新增会员'" 
      width="600px" 
      destroy-on-close
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="公司名称" required>
          <el-input v-model="formData.companyName" placeholder="请输入公司全称" />
        </el-form-item>
        
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="联系人" required>
            <el-input v-model="formData.contactPerson" placeholder="姓名" />
          </el-form-item>
          <el-form-item label="联系电话" required>
            <el-input v-model="formData.contactPhone" placeholder="手机号" />
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="所属行业">
            <el-select v-model="formData.industry" class="w-full">
              <el-option label="制造业" value="制造业" />
              <el-option label="互联网" value="互联网" />
              <el-option label="贸易" value="贸易" />
              <el-option label="服务业" value="服务业" />
            </el-select>
          </el-form-item>
          <el-form-item label="所在城市">
            <el-select v-model="formData.city" class="w-full">
              <el-option label="上海" value="上海" />
              <el-option label="北京" value="北京" />
              <el-option label="深圳" value="深圳" />
            </el-select>
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="会员等级">
             <el-select v-model="formData.level" class="w-full">
              <el-option label="SVIP" value="SVIP" />
              <el-option label="VIP" value="VIP" />
              <el-option label="普通会员" value="NORMAL" />
            </el-select>
          </el-form-item>
          <el-form-item label="邀请码">
            <el-input v-model="formData.invitationCode" placeholder="选填" />
          </el-form-item>
        </div>

        <el-form-item label="电子邮箱">
          <el-input v-model="formData.email" placeholder="example@company.com" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm">确定保存</el-button>
      </template>
    </el-dialog>

    <!-- 3. 🆕 福利发放 Dialog (P1修正) -->
    <el-dialog
      v-model="welfareDialog.visible"
      title="发放福利"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="welfareFormRef"
        :model="welfareDialog.form"
        :rules="welfareRules"
        label-width="100px"
      >
        <!-- 基础信息展示 (不进行前端库存计算) -->
        <el-form-item label="会员信息">
          <el-input v-model="welfareDialog.memberInfo" readonly disabled />
        </el-form-item>

        <el-form-item label="福利类型" prop="benefitType">
          <el-select
            v-model="welfareDialog.form.benefitType"
            placeholder="请选择福利类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in benefitPolicy"
              :key="item.type"
              :label="item.label"
              :value="item.type"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="发放数量" prop="amount">
          <el-input-number
            v-model="welfareDialog.form.amount"
            :min="1"
            :max="getCurrentBenefitMax()"
            style="width: 100%"
            placeholder="请输入数量"
          />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="welfareDialog.form.remark"
            type="textarea"
            :rows="3"
            placeholder="选填，记录发放原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="welfareDialog.visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirmWelfare"
          :loading="welfareDialog.submitting"
        >
          确认发放
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { Plus, Search, View, Edit, Download, Key, Present, ArrowDown } from '@element-plus/icons-vue'
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { benefitPolicy, getBenefitPolicy } from '@/composables/useBenefitPolicy'
import type { BenefitType, MemberLevel } from '@/composables/useBenefitPolicy'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

// ----------------------------------------------------
// 状态管理
// ----------------------------------------------------
const detailDialogVisible = ref(false)
const formDialogVisible = ref(false)
const isEditMode = ref(false)
const currentMember = ref<any>(null)

// 🆕 福利 Dialog 状态
const welfareDialog = reactive({
  visible: false,
  memberInfo: '',
  currentMemberId: 0,
  currentMemberLevel: 'NORMAL' as MemberLevel,
  form: {
    benefitType: '' as BenefitType | '',
    amount: 1,
    remark: ''
  },
  submitting: false
})
const welfareFormRef = ref<FormInstance>()

// 🆕 福利校验规则
const welfareRules = {
  benefitType: [{ required: true, message: '请选择福利类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

// 表单数据模型
const formData = ref({
  id: 0,
  companyName: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  industry: '制造业',
  level: 'NORMAL',
  city: '上海',
  invitationCode: '',
  status: 'active',
  statusLabel: '正常'
})

// 筛选条件模型
const filters = ref({
  keyword: '',
  invitationCode: '',
  industry: '',
  level: '',
  status: '',
  province: '',
  city: '',
  international: '',
  dateRange: [] as string[]
})

// 省份-城市联动
const provinceCityMap: Record<string, string[]> = {
  上海: ['上海'],
  北京: ['北京'],
  广东: ['广州', '深圳', '东莞'],
  浙江: ['杭州', '宁波'],
  江苏: ['苏州', '南京']
}

const provinces = Object.keys(provinceCityMap)

const cityOptions = computed(() => {
  if (!filters.value.province) return []
  return provinceCityMap[filters.value.province] || []
})

watch(() => filters.value.province, () => {
  filters.value.city = ''
})

watch(
  () => welfareDialog.form.benefitType,
  (type) => {
    if (!type) return
    const policy = getBenefitPolicy(type as BenefitType)
    if (!policy) return
    welfareDialog.form.amount = policy.defaultByLevel[welfareDialog.currentMemberLevel]
  }
)

const total = ref(0)
const tableLoading = ref(false)

function mapMemberItem(item: any) {
  const level = (item.memberLevel || item.level || 'normal').toUpperCase()
  return {
    id: item.id || item.userId,
    userId: item.userId || item.id,
    companyName: item.companyName || '',
    contactPerson: item.contactPerson || '',
    contactPhone: item.contactPhone || '',
    email: item.email || '',
    industry: item.industry || '',
    invitationCode: item.invitationCode || '',
    level,
    city: item.city || '',
    province: item.province || '',
    isInternational: item.isInternational || false,
    status: item.status || 'active',
    statusLabel: item.status === 'expired' ? '已过期' : item.status === 'disabled' ? '已禁用' : '正常',
    registerDate: (item.createdAt || item.registerDate || '').substring(0, 10),
    expireDate: (item.vipExpireTime || item.expireDate || '').substring(0, 10)
  }
}

// 数据源（从API加载）
const allMemberList = ref<any[]>([])

async function loadMembers() {
  tableLoading.value = true
  try {
    const params = new URLSearchParams()
    params.set('page', String(currentPage.value))
    params.set('page_size', String(pageSize.value))
    if (filters.value.keyword) params.set('keyword', filters.value.keyword)
    if (filters.value.level) params.set('member_level', filters.value.level.toLowerCase())

    const res = await apiRequest<any>(`/v3/admin/members?${params.toString()}`)
    const data = res.data || {}
    const items = data.items || data.records || []
    allMemberList.value = items.map(mapMemberItem)
    total.value = data.total || items.length
  } catch (e: any) {
    allMemberList.value = []
    total.value = 0
    ElMessage.error(e.message || '加载会员列表失败')
  } finally {
    tableLoading.value = false
  }
}

// ----------------------------------------------------
// 逻辑处理
// ----------------------------------------------------

// 列表过滤
const filteredMemberList = computed(() => {
  let list = allMemberList.value
  
  if (filters.value.keyword) {
    const k = filters.value.keyword.toLowerCase()
    list = list.filter(item => 
      item.companyName.toLowerCase().includes(k) ||
      item.contactPerson.toLowerCase().includes(k)
    )
  }
  
  if (filters.value.invitationCode) {
    list = list.filter(item => item.invitationCode.includes(filters.value.invitationCode))
  }

  if (filters.value.industry) {
    list = list.filter(item => item.industry === filters.value.industry)
  }
  
  if (filters.value.level) {
    list = list.filter(item => item.level === filters.value.level)
  }
  
  if (filters.value.status) {
    list = list.filter(item => item.status === filters.value.status)
  }
  
  if (filters.value.province) {
    list = list.filter(item => item.province === filters.value.province)
  }

  if (filters.value.city) {
    list = list.filter(item => item.city === filters.value.city)
  }

  if (filters.value.international) {
    const isIntl = filters.value.international === 'yes'
    list = list.filter(item => item.isInternational === isIntl)
  }

  if (filters.value.dateRange?.length === 2) {
    const [start, end] = filters.value.dateRange
    list = list.filter(item => item.registerDate >= start && item.registerDate <= end)
  }
  
  return list
})

// 样式工具
const getLevelTag = (level: string) => {
  const map: Record<string, string> = { 'SVIP': 'danger', 'VIP': 'warning', 'NORMAL': 'primary' }
  return map[level] || 'info'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { 'active': 'success', 'expired': 'warning', 'disabled': 'info' }
  return map[status] || 'info'
}

const getCurrentBenefitMax = () => {
  const type = welfareDialog.form.benefitType
  if (!type) return 100
  const policy = getBenefitPolicy(type as BenefitType)
  return policy?.maxPerGrant ?? 100
}

// ----------------------------------------------------
// 核心操作功能 (新增/编辑/保存)
// ----------------------------------------------------

// 点击新增
const handleAddMember = () => {
  isEditMode.value = false
  // 重置表单
  formData.value = {
    id: 0,
    companyName: '',
    contactPerson: '',
    contactPhone: '',
    email: '',
    industry: '制造业',
    level: 'NORMAL',
    city: '上海',
    invitationCode: '',
    status: 'active',
    statusLabel: '正常'
  }
  formDialogVisible.value = true
}

// 点击编辑
const handleEdit = (row: any) => {
  isEditMode.value = true
  // 浅拷贝数据到表单
  formData.value = { ...row }
  formDialogVisible.value = true
}

// 从详情页跳转编辑
const handleEditFromDetail = (row: any) => {
  if (!row) return
  detailDialogVisible.value = false
  handleEdit(row)
}

// 提交表单 (保存数据)
const handleSubmitForm = () => {
  if (!formData.value.companyName || !formData.value.contactPerson) {
    ElMessage.warning('请填写完整的公司名称和联系人信息')
    return
  }

  if (isEditMode.value) {
    // 编辑逻辑：找到对应 ID 更新
    const index = allMemberList.value.findIndex(item => item.id === formData.value.id)
    if (index !== -1) {
      // 保留原有的注册时间等不可变字段
      allMemberList.value[index] = { 
        ...allMemberList.value[index],
        ...formData.value 
      }
      ElMessage.success('会员信息更新成功')
    }
  } else {
    // 新增逻辑：生成 ID 并添加到列表顶部
    const newMember = {
      ...formData.value,
      id: Date.now(), // 模拟 ID
      registerDate: new Date().toISOString().split('T')[0],
      expireDate: new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0] // 默认一年后过期
    }
    allMemberList.value.unshift(newMember)
    ElMessage.success('新会员添加成功')
  }

  formDialogVisible.value = false
}

// 分页与选择逻辑
const selectedRows = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

const pagedMemberList = computed(() => {
  // Server-side pagination: filteredMemberList applies client-side filters to the API-loaded page
  return filteredMemberList.value
})

const clearSelection = () => {
  selectedRows.value = []
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  clearSelection()
  loadMembers()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadMembers()
}

// ----------------------------------------------------
// 其他操作
// ----------------------------------------------------

const handleSearch = () => {
  currentPage.value = 1
  clearSelection()
  loadMembers()
}

const handleReset = () => {
  filters.value = { keyword: '', invitationCode: '', industry: '', level: '', status: '', city: '', province: '', international: '', dateRange: [] }
  currentPage.value = 1
  clearSelection()
  loadMembers()
}

const csvEscape = (val: any) => {
  const str = String(val ?? '')
  return `"${str.replace(/"/g, '""')}"`
}

const handleExport = () => {
  if (!import.meta.client) return
  const list = selectedRows.value.length ? selectedRows.value : filteredMemberList.value

  const header = ['公司名称','联系人','电话','行业','等级','邀请码','省份','城市','国际会员','状态','注册时间','到期时间']
  const rows = list.map(i => [
    i.companyName,
    i.contactPerson,
    i.contactPhone,
    i.industry,
    i.level,
    i.invitationCode,
    i.province,
    i.city,
    i.isInternational ? '是' : '否',
    i.statusLabel,
    i.registerDate,
    i.expireDate
  ])

  const csvContent = '\uFEFF' + [
    header.map(csvEscape).join(','),
    ...rows.map(r => r.map(csvEscape).join(','))
  ].join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `会员列表_${new Date().toISOString().slice(0,10)}.csv`
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 100)

  ElMessage.success('导出成功')
}

const handleViewDetail = (row: any) => {
  currentMember.value = row
  detailDialogVisible.value = true
}

// 🆕 打开福利分配弹窗
const openWelfareDialog = (row: any) => {
  welfareDialog.visible = true
  welfareDialog.currentMemberId = row.id
  welfareDialog.currentMemberLevel = (row.level || 'NORMAL') as MemberLevel
  welfareDialog.memberInfo = `${row.companyName} (${row.contactPerson})`
  welfareDialog.form = { benefitType: '', amount: 1, remark: '' }
}

// 🆕 确认发放福利 (调用后端 benefit-pool API)
const handleConfirmWelfare = async () => {
  if (!welfareFormRef.value) return

  try {
    await welfareFormRef.value.validate()
  } catch {
    return
  }

  welfareDialog.submitting = true
  try {
    const partnerId = 0 // admin platform grant: partnerId=0 means no pool deduction
    await apiRequest(`/v3/admin/partners/${partnerId}/benefit-pool/grant`, {
      method: 'POST',
      body: {
        benefit_type: welfareDialog.form.benefitType,
        amount: welfareDialog.form.amount,
        member_id: welfareDialog.currentMemberId,
        remark: welfareDialog.form.remark || ''
      }
    })
    ElMessage.success('福利发放成功')
    welfareDialog.visible = false
  } catch (e: any) {
    ElMessage.error(e.message || '福利发放失败')
  } finally {
    welfareDialog.submitting = false
  }
}

// 更多操作 (真实删除/禁用)
const handleMoreAction = (command: string, row: any) => {
  if (command === 'toggleStatus') {
    const action = row.status === 'active' ? '禁用' : '启用'
    ElMessageBox.confirm(`确定${action}会员 "${row.companyName}" 吗？`, '提示', { type: 'warning' })
      .then(() => {
        row.status = row.status === 'active' ? 'disabled' : 'active'
        row.statusLabel = row.status === 'active' ? '正常' : '已禁用'
        ElMessage.success(`已${action}`)
      })
  } else if (command === 'delete') {
    // 真实删除逻辑
    ElMessageBox.confirm('此操作将永久删除该会员, 是否继续?', '严重警告', { 
      type: 'error',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    .then(() => {
      // 过滤掉当前 ID
      allMemberList.value = allMemberList.value.filter(m => m.id !== row.id)
      ElMessage.success('删除成功')
    })
  }
}

onMounted(loadMembers)
</script>