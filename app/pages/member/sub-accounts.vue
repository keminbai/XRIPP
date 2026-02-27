<!-- 文件路径: app/pages/member/sub-accounts.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 顶部概览 -->
    <div class="bg-gradient-to-r from-indigo-900 to-blue-900 rounded-2xl p-8 text-white shadow-lg relative overflow-hidden">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-white/5 skew-x-12"></div>
      <div class="relative z-10 flex justify-between items-center">
        <div>
          <div class="flex items-center gap-2 mb-2">
            <el-icon class="text-xl"><User /></el-icon>
            <span class="font-bold text-lg">子账号管理</span>
            <span class="bg-yellow-500 text-black text-[10px] px-2 py-0.5 rounded font-bold ml-2">SVIP权益</span>
          </div>
          <p class="text-blue-200 text-sm">
            您可以创建子账号供员工使用，子账号共享主账号的权益池（如标书下载次数）。
          </p>
        </div>
        <div class="text-right">
          <div class="text-3xl font-bold font-mono">{{ accounts.length }} / 5</div>
          <div class="text-xs text-blue-300">已用名额</div>
        </div>
      </div>
    </div>

    <!-- 列表区 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
      <div class="flex justify-between items-center mb-6">
        <h3 class="font-bold text-slate-900">账号列表</h3>
        <el-button type="primary" @click="openAddDialog" :disabled="accounts.length >= 5">
          <el-icon class="mr-1"><Plus /></el-icon> 新增子账号
        </el-button>
      </div>

      <el-table :data="accounts" style="width: 100%">
        <el-table-column prop="name" label="使用人姓名" width="150" />
        <el-table-column prop="phone" label="手机号 (登录账号)" width="180" />
        <el-table-column prop="department" label="所属部门" width="150" />
        <el-table-column label="权限配置" min-width="200">
          <template #default="scope">
            <div class="flex gap-2">
              <el-tag v-if="scope.row.permissions.includes('tender')" size="small">下载标书</el-tag>
              <el-tag v-if="scope.row.permissions.includes('activity')" size="small" type="success">报名活动</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status" 
              active-value="active" 
              inactive-value="disabled"
              size="small"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑子账号' : '新增子账号'" width="500px">
      <el-form :model="form" label-position="top">
        <el-form-item label="使用人姓名" required>
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="作为登录账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="初始密码" v-if="!isEdit" required>
          <el-input v-model="form.password" placeholder="设置初始登录密码" show-password />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.department" placeholder="如：海外销售部" />
        </el-form-item>
        <el-form-item label="权限分配">
          <el-checkbox-group v-model="form.permissions">
            <el-checkbox label="tender">允许下载标书 (消耗主权益)</el-checkbox>
            <el-checkbox label="activity">允许报名活动</el-checkbox>
            <el-checkbox label="supplier">允许管理服务商信息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { User, Plus } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'member' })

const accounts = ref([
  { id: 1, name: '李晓明', phone: '13900001111', department: '海外一部', permissions: ['tender'], status: 'active' },
  { id: 2, name: '王丽', phone: '13900002222', department: '采购部', permissions: ['tender', 'activity'], status: 'active' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: null,
  name: '',
  phone: '',
  password: '',
  department: '',
  permissions: []
})

const openAddDialog = () => {
  isEdit.value = false
  form.value = { id: null, name: '', phone: '', password: '', department: '', permissions: ['tender'] }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = { ...row, password: '' } 
  dialogVisible.value = true
}

const handleSave = () => {
  if(!form.value.name || !form.value.phone) return ElMessage.warning('请补全必填信息')
  
  if (isEdit.value) {
    const index = accounts.value.findIndex(a => a.id === form.value.id)
    if (index !== -1) accounts.value[index] = { ...accounts.value[index], ...form.value }
    ElMessage.success('更新成功')
  } else {
    accounts.value.push({
      id: Date.now(),
      ...form.value,
      status: 'active'
    })
    ElMessage.success('子账号创建成功')
  }
  dialogVisible.value = false
}

const handleStatusChange = (row: any) => {
  const action = row.status === 'active' ? '启用' : '禁用'
  ElMessage.info(`已${action}子账号：${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除子账号 ${row.name} 吗？删除后无法恢复。`, '警告', {
    type: 'warning'
  }).then(() => {
    accounts.value = accounts.value.filter(a => a.id !== row.id)
    ElMessage.success('删除成功')
  })
}
</script>