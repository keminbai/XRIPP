<!-- 文件路径: D:\ipp-platform\app\pages\admin\business\roles.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 提示说明 -->
    <el-alert 
      title="此处配置业务角色的操作权限与数据范围，系统管理员权限请前往「系统综管-权限管理」" 
      type="info" 
      :closable="false" 
      show-icon 
    />

    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">业务角色权限配置</h3>
        <p class="text-xs text-slate-500 mt-1">配置合伙人、审核员等业务角色的操作边界与数据可见范围</p>
      </div>

      <el-table :data="roleList" border stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
        <el-table-column prop="roleName" label="业务角色" width="180">
          <template #default="scope">
            <div class="font-bold text-slate-800">{{ scope.row.roleName }}</div>
            <div class="text-xs text-slate-400 mt-1">
              <el-tag size="small" type="info">{{ scope.row.code }}</el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="数据可见范围" width="200">
          <template #default="scope">
            <el-select v-model="scope.row.dataScope" size="small" class="w-full">
              <el-option label="仅本人数据" value="self">
                <span class="text-sm">仅本人数据</span>
                <span class="text-xs text-slate-400 ml-2">最严格</span>
              </el-option>
              <el-option label="本城市数据" value="city">
                <span class="text-sm">本城市数据</span>
                <span class="text-xs text-slate-400 ml-2">合伙人常用</span>
              </el-option>
              <el-option label="全平台数据" value="all">
                <span class="text-sm">全平台数据</span>
                <span class="text-xs text-slate-400 ml-2">最宽松</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="核心业务权限" min-width="450">
          <template #default="scope">
            <div class="grid grid-cols-2 gap-3">
              <el-checkbox v-model="scope.row.permissions.viewContact">
                <span class="text-sm">查看客户手机号</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.exportData">
                <span class="text-sm">导出业务数据</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.directPublish">
                <span class="text-sm">免审核直接发布</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.assignTask">
                <span class="text-sm">分配工单任务</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.editPrice">
                <span class="text-sm">修改报价</span>
              </el-checkbox>
              <el-checkbox v-model="scope.row.permissions.approveRefund">
                <span class="text-sm">审批退款申请</span>
              </el-checkbox>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleSave(scope.row)">
              保存配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="mt-4 flex justify-between items-center">
        <div class="text-xs text-slate-500">
          共 {{ roleList.length }} 个业务角色
        </div>
        <el-button size="small" @click="handleBatchSave">
          <el-icon class="mr-1"><Check /></el-icon> 批量保存所有配置
        </el-button>
      </div>
    </div>

    <!-- 权限变更日志 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <h4 class="font-bold text-slate-700 mb-4">最近权限变更记录</h4>
      <el-table :data="changeLog" size="small" :show-header="false">
        <el-table-column prop="time" width="160" />
        <el-table-column prop="admin" width="120" />
        <el-table-column prop="action" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const roleList = ref([
  { 
    id: 1, 
    roleName: '城市合伙人', 
    code: 'PARTNER', 
    dataScope: 'city',
    enabled: true,
    permissions: { 
      viewContact: true, 
      exportData: false, 
      directPublish: false, 
      assignTask: false,
      editPrice: false,
      approveRefund: false
    }
  },
  { 
    id: 2, 
    roleName: '发布管理员', 
    code: 'PUBLISHER', 
    dataScope: 'all',
    enabled: true,
    permissions: { 
      viewContact: false, 
      exportData: true, 
      directPublish: true, 
      assignTask: false,
      editPrice: false,
      approveRefund: false
    }
  },
  { 
    id: 3, 
    roleName: '审核管理员', 
    code: 'AUDITOR', 
    dataScope: 'all',
    enabled: true,
    permissions: { 
      viewContact: true, 
      exportData: false, 
      directPublish: true, 
      assignTask: true,
      editPrice: false,
      approveRefund: true
    }
  },
  {
    id: 4,
    roleName: '财务专员',
    code: 'FINANCE',
    dataScope: 'all',
    enabled: true,
    permissions: {
      viewContact: true,
      exportData: true,
      directPublish: false,
      assignTask: false,
      editPrice: true,
      approveRefund: true
    }
  }
])

const changeLog = ref([
  { time: '2026-02-01 14:30', admin: '张管理员', action: '修改了「城市合伙人」的数据可见范围' },
  { time: '2026-01-30 10:15', admin: '李管理员', action: '启用了「财务专员」角色' },
  { time: '2026-01-28 16:20', admin: '王管理员', action: '新增了「免审核直接发布」权限给审核管理员' }
])

const handleSave = (row: any) => {
  ElMessage.success(`${row.roleName} 权限配置已更新`)
}

const handleBatchSave = () => {
  ElMessage.success('所有角色权限配置已批量保存')
}
</script>