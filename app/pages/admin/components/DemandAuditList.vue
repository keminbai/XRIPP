<template>
  <div class="demand-audit-list">
    <div class="mb-4 flex gap-3">
      <el-input v-model="keyword" placeholder="需求标题/发布企业" class="w-64" clearable>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <el-table :data="filteredRows" border stripe v-loading="loading">
      <el-table-column prop="title" label="需求标题" min-width="200" />
      <el-table-column prop="company" label="发布企业" width="180" />
      <el-table-column prop="budget" label="预算" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="160" />
      <el-table-column label="当前状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'pending_level1'" type="warning">待一审</el-tag>
          <el-tag v-else-if="row.status === 'pending_level2'" type="primary">待二审</el-tag>
          <el-tag v-else-if="row.status === 'approved'" type="success">已通过</el-tag>
          <el-tag v-else-if="row.status === 'rejected'" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">{{ row.status || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <template v-if="canAudit(row)">
            <el-button link type="success" size="small" @click="handleAudit(row, 'pass')">通过</el-button>
            <el-button link type="danger" size="small" @click="handleAudit(row, 'reject')">驳回</el-button>
          </template>
          <span v-else class="text-xs text-gray-400">无权操作</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

type DemandRow = {
  id?: number | string
  title?: string
  company?: string
  budget?: string | number
  submitTime?: string
  status?: string
  [key: string]: unknown
}

const props = defineProps<{
  role?: 'AUDIT_1' | 'AUDIT_2' | string
  items?: DemandRow[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
  (e: 'audit', payload: { row: DemandRow; action: 'pass' | 'reject' }): void
}>()

const keyword = ref('')
const loading = computed(() => Boolean(props.loading))

const filteredRows = computed(() => {
  const kw = String(keyword.value || '').trim()
  const rows = Array.isArray(props.items) ? props.items : []

  return rows.filter((item) => {
    const roleMatch =
      (props.role === 'AUDIT_1' && item.status === 'pending_level1') ||
      (props.role === 'AUDIT_2' && item.status === 'pending_level2')
    if (!roleMatch) return false

    if (!kw) return true
    return String(item.title || '').includes(kw) || String(item.company || '').includes(kw)
  })
})

const handleSearch = () => {}

const handleReset = () => {
  keyword.value = ''
  handleSearch()
}

const canAudit = (row: DemandRow) => {
  if (props.role === 'AUDIT_1') return row.status === 'pending_level1'
  if (props.role === 'AUDIT_2') return row.status === 'pending_level2'
  return false
}

const handleAudit = (row: DemandRow, action: 'pass' | 'reject') => {
  const actionText = action === 'pass' ? '通过' : '驳回'
  ElMessageBox.confirm(`确认${actionText}该需求申请吗？`, '提示', { type: 'warning' })
    .then(() => {
      emit('audit', { row, action })
      ElMessage.info('已提交审核请求，请等待列表刷新')
      emit('refresh')
    })
    .catch(() => {})
}
</script>

<style scoped>
.demand-audit-list {
  padding: 10px 0;
}
</style>
