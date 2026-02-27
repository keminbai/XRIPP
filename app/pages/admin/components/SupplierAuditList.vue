<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\components\SupplierAuditList.vue
  版本: V1.0 (2026-02-07)
  
  ✅ 核心功能:
  1. [角色过滤] 根据父组件传入的 role ('AUDIT_1'/'AUDIT_2') 自动筛选待办任务。
  2. [审核操作] 提供审核弹窗，输入意见后调用(模拟)API。
  3. [数据隔离] 只展示与当前审核角色相关的状态数据。
  ========================================================================
-->
<template>
  <div class="supplier-audit-list">
    <!-- 筛选区域 -->
    <div class="flex gap-3 mb-4">
      <el-select v-model="filters.status" placeholder="审核状态" class="w-40" clearable>
        <!-- 根据角色显示不同的待办状态选项 -->
        <el-option v-if="props.role === 'AUDIT_1'" label="待一审" value="pending_level1" />
        <el-option v-if="props.role === 'AUDIT_2'" label="待二审" value="pending_level2" />
        <el-option label="已通过" value="approved" />
        <el-option label="已驳回" value="rejected" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="公司名/联系人" class="w-64" clearable />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- 列表 -->
    <el-table :data="filteredRows" border stripe v-loading="loading">
      <el-table-column prop="id" label="申请ID" width="120" />
      <el-table-column prop="company" label="公司名称" min-width="200" />
      <el-table-column prop="contact" label="联系人" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      
      <!-- 状态列 -->
      <el-table-column label="当前状态" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'pending_level1'" type="warning">待一审</el-tag>
          <el-tag v-else-if="row.status === 'pending_level2'" type="primary">待二审</el-tag>
          <el-tag v-else-if="row.status === 'approved'" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已驳回</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="submitTime" label="提交时间" width="170" />

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
          
          <!-- 仅当处于当前角色可操作的状态时，显示审核按钮 -->
          <template v-if="canAudit(row)">
            <el-button link type="success" size="small" @click="openAuditDialog(row, 'approve')">
              {{ props.role === 'AUDIT_1' ? '通过(交二审)' : '批准发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="openAuditDialog(row, 'reject')">
              驳回
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审核操作弹窗 -->
    <el-dialog
      v-model="auditDialog.visible"
      :title="auditDialog.action === 'approve' ? '审核通过' : '审核驳回'"
      width="500px"
      append-to-body
    >
      <el-form :model="auditDialog.form">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditDialog.form.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button 
          :type="auditDialog.action === 'approve' ? 'success' : 'danger'"
          @click="handleConfirmAudit"
          :loading="submitting"
        >
          确认{{ auditDialog.action === 'approve' ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="700px" append-to-body>
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="申请ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ currentDetail.company }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentDetail.contact }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentDetail.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          {{ currentDetail.status }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  role: {
    type: String,
    required: true
  },
  items: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh', 'audit'])

const filters = reactive({ status: '', keyword: '' })
const submitting = ref(false)
const loading = computed(() => props.loading)

// 详情相关
const detailVisible = ref(false)
const currentDetail = ref(null)

// 审核弹窗相关
const auditDialog = reactive({
  visible: false,
  action: '',
  currentRow: null,
  form: { comment: '' }
})

const handleSearch = () => {}

const filteredRows = computed(() => {
  let rows = [...(props.items as any[])]

  if (filters.status) {
    rows = rows.filter(item => item.status === filters.status)
  } else {
    if (props.role === 'AUDIT_1') rows = rows.filter(item => item.status === 'pending_level1')
    if (props.role === 'AUDIT_2') rows = rows.filter(item => item.status === 'pending_level2')
  }

  const kw = String(filters.keyword || '').trim()
  if (kw) {
    rows = rows.filter(item => {
      const company = String(item.company || '')
      const contact = String(item.contact || '')
      return company.includes(kw) || contact.includes(kw)
    })
  }

  return rows
})

// 权限判断
const canAudit = (row) => {
  if (props.role === 'AUDIT_1') return row.status === 'pending_level1'
  if (props.role === 'AUDIT_2') return row.status === 'pending_level2'
  return false
}

const handleViewDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const openAuditDialog = (row, action) => {
  auditDialog.visible = true
  auditDialog.action = action
  auditDialog.currentRow = row
  auditDialog.form.comment = action === 'approve' ? '同意' : ''
}

const handleConfirmAudit = () => {
  if (!auditDialog.form.comment) return ElMessage.warning('请输入审核意见')
  
  submitting.value = true

  emit('audit', {
    row: auditDialog.currentRow,
    action: auditDialog.action,
    comment: auditDialog.form.comment
  })
  auditDialog.visible = false
  submitting.value = false
  ElMessage.info('已提交审核请求，请等待列表刷新')
  emit('refresh')
}
</script>

<style scoped>
.supplier-audit-list {
  padding: 10px 0;
}
</style>
