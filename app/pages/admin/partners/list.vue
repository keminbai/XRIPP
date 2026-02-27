<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\partners\list.vue
  版本: V1.1 (2026-02-04)
  
  ✅ 修复记录 (对应需求 Row 31-34, 36):
  1. [新增] 完整的"新增合伙人"弹窗，包含身份识别码、联系方式等 (Row 31)
  2. [逻辑] 新增成功后显示"初始密码" (Row 32)
  3. [操作] 增加"更多操作"：资格延期、福利发放、上传证书、重新入驻 (Row 34)
  4. [展示] 详情页展示邀请码、资格日期、头像等完整信息 (Row 36)
  5. [筛选] 增加"国家"筛选维度
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 顶部操作栏 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">合伙人管理</h3>
          <p class="text-xs text-slate-500 mt-1">管理城市合伙人入驻、资格与业绩</p>
        </div>
        <el-button type="primary" @click="openAddDialog">
          <el-icon class="mr-2"><Plus /></el-icon> 新增合伙人
        </el-button>
      </div>

      <div class="flex gap-3 flex-wrap">
        <el-input v-model="filters.keyword" placeholder="搜索合伙人姓名/电话/邀请码..." :prefix-icon="Search" class="w-64" clearable />
        
        <el-select v-model="filters.country" placeholder="国家" class="w-32" clearable>
          <el-option label="中国" value="China" />
          <el-option label="马来西亚" value="Malaysia" />
          <el-option label="新加坡" value="Singapore" />
        </el-select>

        <el-select v-model="filters.city" placeholder="城市" class="w-32" clearable>
          <el-option label="上海" value="上海" />
          <el-option label="北京" value="北京" />
          <el-option label="深圳" value="深圳" />
        </el-select>

        <el-select v-model="filters.level" placeholder="星级" class="w-32" clearable>
          <el-option label="五星合伙人" value="5" />
          <el-option label="四星合伙人" value="4" />
          <el-option label="三星合伙人" value="3" />
        </el-select>
        
        <el-button type="primary" plain @click="handleSearch">查询</el-button>
        <el-button plain @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 列表区域 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <el-table :data="pagedList" stripe>
        <el-table-column label="合伙人信息" min-width="220">
          <template #default="scope">
            <div class="flex items-center gap-3">
              <el-avatar :size="45" :src="scope.row.avatar">{{ scope.row.name[0] }}</el-avatar>
              <div>
                <div class="font-bold text-slate-800 flex items-center gap-2">
                  {{ scope.row.name }}
                  <el-tag size="small" type="info" class="font-mono">{{ scope.row.invitationCode }}</el-tag>
                </div>
                <div class="text-xs text-slate-500 mt-1">{{ scope.row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="区域" width="140">
           <template #default="scope">
             <span>{{ scope.row.country }} - {{ scope.row.city }}</span>
           </template>
        </el-table-column>

        <el-table-column label="会员数" width="100" align="center">
          <template #default="scope">
            <span class="text-blue-600 font-bold">{{ scope.row.memberCount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="本月业绩" width="120" align="right">
          <template #default="scope">
            <span class="text-green-600 font-bold">¥{{ scope.row.revenue.toLocaleString() }}</span>
          </template>
        </el-table-column>

        <el-table-column label="星级评定" width="140">
          <template #default="scope">
            <el-rate :model-value="scope.row.stars" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>

        <el-table-column label="资格有效期" width="120">
          <template #default="scope">
            <span class="text-xs">{{ scope.row.expireDate }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.statusLabel }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <el-button link type="info" size="small" class="ml-2">
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="extension">资格延期</el-dropdown-item>
                  <el-dropdown-item command="welfare">发放福利</el-dropdown-item>
                  <el-dropdown-item command="certificate">上传证书</el-dropdown-item>
                  <el-dropdown-item command="reentry" v-if="scope.row.status === 'expired'">重新入驻</el-dropdown-item>
                  <el-dropdown-item command="disable" divided class="text-red-500">
                    {{ scope.row.status === 'active' ? '禁用账号' : '启用账号' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, prev, pager, next, sizes"
          :total="filteredList.length"
          :page-sizes="[10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新增/编辑 合伙人弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑合伙人信息' : '城市合伙人入驻'" 
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="合伙人姓名" required>
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份识别码" required>
           <el-input v-model="form.idCode" placeholder="身份证/护照号" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="所在国家" required>
            <el-select v-model="form.country" class="w-full">
              <el-option label="中国" value="China" />
              <el-option label="马来西亚" value="Malaysia" />
            </el-select>
          </el-form-item>
          <el-form-item label="所在城市" required>
            <el-select v-model="form.city" class="w-full">
              <el-option label="上海" value="上海" />
              <el-option label="北京" value="北京" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="合伙人等级">
           <el-rate v-model="form.stars" />
        </el-form-item>
        
        <div v-if="isEdit">
          <el-divider content-position="left">扩展信息</el-divider>
          <el-form-item label="邀请码">
            <el-input v-model="form.invitationCode" disabled />
          </el-form-item>
          <el-form-item label="资格有效期">
            <el-date-picker v-model="form.expireDate" type="date" class="w-full" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定{{ isEdit ? '保存' : '入驻' }}</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

// 状态管理
const filters = ref({ keyword: '', city: '', level: '', country: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const form = ref({
  id: 0,
  name: '',
  phone: '',
  idCode: '',
  country: 'China',
  city: '',
  stars: 3,
  invitationCode: '',
  expireDate: ''
})

// 模拟数据 (补充字段 Row 36)
const partnerList = ref([
  { 
    id: 1, name: '张三', avatar: '', phone: '138****0001', idCode: '310101****', 
    country: 'China', city: '上海', memberCount: 156, revenue: 68500, stars: 5, 
    status: 'active', statusLabel: '在职', joinDate: '2024-01-15', expireDate: '2025-01-15', 
    invitationCode: 'SH888' 
  },
  { 
    id: 2, name: '李四', avatar: '', phone: '139****0002', idCode: '110101****', 
    country: 'China', city: '北京', memberCount: 142, revenue: 62300, stars: 5, 
    status: 'active', statusLabel: '在职', joinDate: '2024-02-20', expireDate: '2025-02-20', 
    invitationCode: 'BJ666' 
  },
  { 
    id: 3, name: '王五', avatar: '', phone: '137****0003', idCode: '440301****', 
    country: 'China', city: '深圳', memberCount: 128, revenue: 58900, stars: 4, 
    status: 'expired', statusLabel: '已过期', joinDate: '2023-03-10', expireDate: '2024-03-10', 
    invitationCode: 'SZ999' 
  }
])

// 过滤逻辑
const filteredList = computed(() => {
  return partnerList.value.filter(item => {
    const matchKeyword = !filters.value.keyword || item.name.includes(filters.value.keyword) || item.invitationCode.includes(filters.value.keyword)
    const matchCity = !filters.value.city || item.city === filters.value.city
    const matchLevel = !filters.value.level || item.stars === Number(filters.value.level)
    const matchCountry = !filters.value.country || item.country === filters.value.country
    return matchKeyword && matchCity && matchLevel && matchCountry
  })
})

const getStatusType = (status: string) => {
  return status === 'active' ? 'success' : status === 'expired' ? 'warning' : 'info'
}

// 分页逻辑
const currentPage = ref(1)
const pageSize = ref(10)

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const handlePageChange = () => {}
const handleSizeChange = () => {
  currentPage.value = 1
}

// ---------------------------
// 核心操作
// ---------------------------

const handleSearch = () => {
  currentPage.value = 1
  ElMessage.success('查询完成')
}

const handleReset = () => {
  filters.value = { keyword: '', city: '', level: '', country: '' }
  currentPage.value = 1
  ElMessage.info('已重置筛选条件')
}

const openAddDialog = () => {
  isEdit.value = false
  form.value = { id: 0, name: '', phone: '', idCode: '', country: 'China', city: '', stars: 3, invitationCode: '', expireDate: '' }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!form.value.name || !form.value.phone) return ElMessage.warning('请补全信息')
  
  dialogVisible.value = false
  if (isEdit.value) {
    ElMessage.success('保存成功')
  } else {
    // 新增合伙人逻辑 (Row 32: 发放初始密码)
    const newPartner = {
      ...form.value,
      id: Date.now(),
      invitationCode: 'NEW' + Math.floor(Math.random() * 1000),
      status: 'active',
      statusLabel: '在职',
      joinDate: new Date().toISOString().split('T')[0],
      expireDate: '2026-12-31',
      revenue: 0,
      memberCount: 0,
      avatar: ''
    }
    partnerList.value.unshift(newPartner)
    
    // 弹窗提示初始密码
    ElMessageBox.alert(
      `合伙人 ${form.value.name} 入驻成功！\n\n初始登录密码为：123456\n请通知合伙人尽快修改密码。`,
      '发放资格成功',
      { confirmButtonText: '复制并关闭' }
    )
  }
}

// 更多操作 (Row 34)
const handleCommand = (cmd: string, row: any) => {
  switch (cmd) {
    case 'extension':
      ElMessageBox.confirm('确认将该合伙人资格延长一年吗？', '资格延期').then(() => ElMessage.success('延期成功'))
      break
    case 'welfare':
      ElMessageBox.prompt('请输入福利金额或积分', '发放福利').then(({ value }) => ElMessage.success(`已发放：${value}`))
      break
    case 'certificate':
      ElMessage.success('请选择文件上传证书...')
      break
    case 'reentry':
      ElMessageBox.confirm('确认重新入驻该合伙人？将重置状态。', '重新入驻').then(() => {
        row.status = 'active'
        row.statusLabel = '在职'
        ElMessage.success('重新入驻成功')
      })
      break
    case 'disable':
      row.status = row.status === 'active' ? 'disabled' : 'active'
      row.statusLabel = row.status === 'active' ? '在职' : '已禁用'
      ElMessage.success('状态已变更')
      break
  }
}
</script>