<template>
  <div class="bg-white p-6 rounded-xl border border-slate-200">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <div class="mb-6">
      <h3 class="text-lg font-bold text-slate-800">操作日志</h3>
      <p class="text-xs text-slate-500 mt-1">查询系统操作记录</p>
    </div>

    <div class="flex gap-3 mb-4 flex-wrap">
      <el-input v-model="filters.keyword" placeholder="搜索用户/操作..." prefix-icon="Search" class="w-64" clearable />
      <el-select v-model="filters.action" placeholder="操作类型" class="w-32" clearable>
        <el-option label="登录" value="login" />
        <el-option label="审核" value="audit" />
        <el-option label="编辑" value="edit" />
      </el-select>
      <el-date-picker v-model="filters.dateRange" type="daterange" class="w-64" />
      <el-button type="primary" plain @click="handleSearch">查询</el-button>
      <el-button @click="resetFilters">重置</el-button>
      <el-button type="success" plain @click="handleExport">导出</el-button>
    </div>

    <el-table :data="filteredLogs" stripe>
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="user" label="操作人" width="140" />
      <el-table-column prop="action" label="操作" width="140" />
      <el-table-column prop="module" label="模块" width="180" />
      <el-table-column prop="target" label="对象" />
      <el-table-column prop="ip" label="IP地址" width="150" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const filters = ref({ keyword: '', action: '', dateRange: [] as string[] })

const logs = ref([
  { time: '2026-01-28 10:30:25', user: '张管理员', action: '审核通过', module: '联合国入库审核', target: '上海宏大医疗', ip: '192.168.1.100' },
  { time: '2026-01-28 09:15:10', user: '李合伙人', action: '发布活动', module: '活动管理', target: '国际采购培训会', ip: '192.168.1.101' }
])

const filteredLogs = computed(() => {
  let list = logs.value
  if (filters.value.keyword) {
    list = list.filter(l => l.user.includes(filters.value.keyword) || l.action.includes(filters.value.keyword))
  }
  if (filters.value.action) {
    list = list.filter(l => l.action.includes(filters.value.action))
  }
  return list
})

const handleSearch = () => ElMessage.success('查询完成')
const resetFilters = () => (filters.value = { keyword: '', action: '', dateRange: [] })
const handleExport = () => ElMessage.success('导出中...')
</script>
