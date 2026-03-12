<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">数据备份</h3>
          <p class="text-xs text-slate-500 mt-1">管理系统数据备份与恢复</p>
        </div>
        <el-button type="primary" @click="handleBackup">
          <el-icon class="mr-2"><FolderOpened /></el-icon> 立即备份
        </el-button>
      </div>

      <el-table :data="backups" stripe>
        <el-table-column prop="name" label="备份名称" />
        <el-table-column prop="size" label="大小" width="120" />
        <el-table-column prop="time" label="备份时间" width="180" />
        <el-table-column label="类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'auto' ? 'success' : 'primary'" size="small">
              {{ scope.row.type === 'auto' ? '自动' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="success" size="small" @click="handleDownload(scope.row)">
              下载
            </el-button>
            <el-button link type="warning" size="small" @click="handleRestore(scope.row)">
              恢复
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <h4 class="font-bold text-slate-800 mb-4">自动备份设置</h4>
      <el-form :model="autoBackupConfig" label-width="150px" class="max-w-3xl">
        <el-form-item label="启用自动备份">
          <el-switch v-model="autoBackupConfig.enabled" />
        </el-form-item>
        <el-form-item label="备份频率">
          <el-radio-group v-model="autoBackupConfig.frequency">
            <el-radio label="daily">每天</el-radio>
            <el-radio label="weekly">每周</el-radio>
            <el-radio label="monthly">每月</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="保留份数">
          <el-input-number v-model="autoBackupConfig.keepCount" :min="1" :max="30" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderOpened } from '@element-plus/icons-vue'

definePageMeta({ layout: 'admin' })

const backups = ref([
  { id: 1, name: 'backup_2026-01-28', size: '125MB', time: '2026-01-28 02:00:00', type: 'auto' },
  { id: 2, name: 'backup_2026-01-27', size: '123MB', time: '2026-01-27 02:00:00', type: 'auto' }
])

const autoBackupConfig = ref({ enabled: true, frequency: 'daily', keepCount: 7 })

const handleBackup = () => ElMessage.success('正在备份数据...')
const handleDownload = (row: any) => ElMessage.success(`正在下载 ${row.name}`)
const handleRestore = (row: any) => {
  ElMessageBox.confirm(`确认恢复到 ${row.time} 的备份吗？`, '警告', { type: 'warning' })
    .then(() => ElMessage.success('数据恢复中...'))
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除备份 ${row.name} 吗？`, '警告', { type: 'warning' })
    .then(() => {
      backups.value = backups.value.filter(item => item.id !== row.id)
      ElMessage.success('删除成功')
    })
}
const handleSaveConfig = () => ElMessage.success('备份配置已保存')
</script>