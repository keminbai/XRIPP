<!-- 
  文件路径: D:\ipp-platform\app\pages\admin\components\ActivityAuditList.vue
  功能: 活动/广告/培训的内容审核列表
-->
<template>
  <div class="activity-audit-list">
    <!-- 筛选 -->
    <div class="flex gap-3 mb-4">
      <el-radio-group v-model="filters.type" size="default" @change="handleSearch">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="activity">活动</el-radio-button>
        <el-radio-button label="training">培训</el-radio-button>
        <el-radio-button label="ad">广告</el-radio-button>
      </el-radio-group>
      <div class="flex-grow"></div>
      <el-input v-model="filters.keyword" placeholder="标题/发布人" class="w-64" clearable>
        <template #append><el-button @click="handleSearch">搜索</el-button></template>
      </el-input>
    </div>

    <!-- 列表 -->
    <el-table :data="filteredRows" border stripe v-loading="loading">
      <el-table-column prop="typeLabel" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.typeColor" size="small">{{ row.typeLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题/内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="applicant" label="发布人" width="120" />
      <el-table-column prop="submitTime" label="提交时间" width="160" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'pending_level1'" type="warning">待一审</el-tag>
          <el-tag v-else-if="row.status === 'pending_level2'" type="primary">待二审</el-tag>
          <el-tag v-else type="success">已通过</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handlePreview(row)">预览</el-button>
          <template v-if="canAudit(row)">
            <el-button link type="success" size="small" @click="handleAudit(row, 'pass')">通过</el-button>
            <el-button link type="danger" size="small" @click="handleAudit(row, 'reject')">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="内容预览" width="600px" append-to-body>
      <div v-if="currentItem">
        <h3 class="font-bold text-lg mb-2">{{ currentItem.title }}</h3>
        <div class="bg-slate-50 p-4 rounded text-sm text-slate-600 mb-4">
          {{ currentItem.content || '暂无详细内容' }}
        </div>
        <img v-if="currentItem.image" :src="currentItem.image" class="w-full rounded border" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  role: { type: String, default: 'AUDIT_1' },
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['refresh', 'audit'])

const filters = reactive({ type: 'all', keyword: '' })
const previewVisible = ref(false)
const currentItem = ref(null)
const loading = computed(() => props.loading)

const handleSearch = () => {}

const filteredRows = computed(() => {
  const kw = String(filters.keyword || '').trim()
  return (props.items as any[]).filter((item: any) => {
    if (filters.type !== 'all' && item.type !== filters.type) return false

    // 默认仅展示当前审核角色待办；有关键词时放开状态限制便于追溯查询
    const roleMatch =
      (props.role === 'AUDIT_1' && item.status === 'pending_level1') ||
      (props.role === 'AUDIT_2' && item.status === 'pending_level2')
    if (!kw && !roleMatch) return false

    if (!kw) return true
    const title = String(item.title || '')
    const applicant = String(item.applicant || '')
    return title.includes(kw) || applicant.includes(kw)
  })
})

const canAudit = (row) => {
  if (props.role === 'AUDIT_1') return row.status === 'pending_level1'
  if (props.role === 'AUDIT_2') return row.status === 'pending_level2'
  return false
}

const handlePreview = (row) => {
  currentItem.value = row
  previewVisible.value = true
}

const handleAudit = (row, action) => {
  const actionText = action === 'pass' ? '通过' : '驳回'
  ElMessageBox.confirm(`确认${actionText}该条内容吗？`, '审核确认', { type: action === 'pass' ? 'success' : 'warning' })
    .then(() => {
      emit('audit', { row, action })
      ElMessage.info('已提交审核请求，请等待列表刷新')
      emit('refresh')
    })
}
</script>
<style scoped>
.activity-audit-list {
  padding: 10px 0;
}
</style>
