<!-- 文件路径: D:\ipp-platform\app\pages\admin\business\promotions.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 提示说明 -->
    <el-alert 
      title="此处配置营销活动与折扣规则，标准定价请前往「财务配置-定价策略」" 
      type="info" 
      :closable="false" 
      show-icon 
    />

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
            <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
          </div>
          <div class="p-3 rounded-lg" :class="stat.bgClass">
            <el-icon class="text-xl" :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex justify-between items-center mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">营销促销规则</h3>
          <p class="text-xs text-slate-500 mt-1">设置限时优惠、组合折扣及新用户福利</p>
        </div>
        <el-button type="primary" @click="handleAddRule">
          <el-icon class="mr-2"><Plus /></el-icon> 新增规则
        </el-button>
      </div>

      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" class="mb-4">
        <el-tab-pane label="进行中" name="active" />
        <el-tab-pane label="已结束" name="ended" />
        <el-tab-pane label="草稿箱" name="draft" />
      </el-tabs>

      <el-table :data="filteredRules" stripe border :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
        <el-table-column prop="name" label="规则名称" width="200">
          <template #default="scope">
            <div class="font-medium text-slate-800">{{ scope.row.name }}</div>
            <div class="text-xs text-slate-400 mt-1">ID: {{ scope.row.id }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="促销类型" width="140">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="规则详情" min-width="280">
          <template #default="scope">
            <div class="text-sm">
              <span v-if="scope.row.type === 'discount'">
                {{ scope.row.target }} <span class="text-red-500 font-bold">{{ scope.row.discount }}折</span>
              </span>
              <span v-else-if="scope.row.type === 'reduce'">
                满 <span class="font-bold">¥{{ scope.row.threshold }}</span> 减 <span class="text-red-500 font-bold">¥{{ scope.row.reduce }}</span>
              </span>
              <span v-else-if="scope.row.type === 'gift'">
                购买 {{ scope.row.mainProduct }} 赠送 <span class="text-green-600 font-bold">{{ scope.row.giftProduct }}</span>
              </span>
              <span v-else>
                {{ scope.row.description }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="有效期" width="250">
          <template #default="scope">
            <div class="text-xs">
              <div>开始: {{ scope.row.startDate }}</div>
              <div class="mt-1">结束: {{ scope.row.endDate }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="100" align="center">
          <template #default="scope">
            <el-tag size="small" :type="getPriorityTag(scope.row.priority)">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" @change="handleToggleStatus(scope.row)" />
          </template>
        </el-table-column>
        
        <el-table-column label="使用次数" width="100" align="center">
          <template #default="scope">
            <span class="font-bold text-blue-600">{{ scope.row.usageCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleDuplicate(scope.row)">
              复制
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination 
          background 
          layout="total, prev, pager, next" 
          :total="filteredRules.length"
          :page-size="10"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Trophy, TrendCharts, Timer, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const activeTab = ref('active')

const stats = [
  { label: '进行中规则', value: '3', icon: Trophy, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '今日使用次数', value: '45', icon: TrendCharts, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '即将到期', value: '1', icon: Timer, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '累计收益', value: '¥12.5万', icon: Check, bgClass: 'bg-purple-50', textClass: 'text-purple-600' }
]

const rules = ref([
  { 
    id: 1, 
    name: '新春特惠', 
    type: 'discount', 
    target: 'SVIP会员年费', 
    discount: 8.5, 
    startDate: '2026-02-01', 
    endDate: '2026-02-28', 
    enabled: true, 
    priority: '高',
    usageCount: 23,
    status: 'active'
  },
  { 
    id: 2, 
    name: '企业团购优惠', 
    type: 'reduce', 
    threshold: 50000, 
    reduce: 5000, 
    startDate: '2026-01-01', 
    endDate: '2026-12-31', 
    enabled: true,
    priority: '中',
    usageCount: 8,
    status: 'active'
  },
  {
    id: 3,
    name: '新用户首单立减',
    type: 'reduce',
    threshold: 1000,
    reduce: 200,
    startDate: '2026-01-15',
    endDate: '2026-03-15',
    enabled: true,
    priority: '高',
    usageCount: 156,
    status: 'active'
  },
  {
    id: 4,
    name: '买VIP送培训课程',
    type: 'gift',
    mainProduct: 'VIP年费',
    giftProduct: '联合国采购实战课程',
    startDate: '2025-12-01',
    endDate: '2025-12-31',
    enabled: false,
    priority: '低',
    usageCount: 45,
    status: 'ended'
  }
])

const filteredRules = computed(() => {
  return rules.value.filter(rule => {
    if (activeTab.value === 'active') return rule.status === 'active'
    if (activeTab.value === 'ended') return rule.status === 'ended'
    if (activeTab.value === 'draft') return rule.status === 'draft'
    return true
  })
})

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'discount': 'danger',
    'reduce': 'success',
    'gift': 'warning'
  }
  return map[type] || 'info'
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    'discount': '限时折扣',
    'reduce': '满减优惠',
    'gift': '买赠活动'
  }
  return map[type] || '其他'
}

const getPriorityTag = (priority: string) => {
  const map: Record<string, string> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return map[priority] || 'info'
}

const handleAddRule = () => ElMessage.info('新增规则功能开发中...')
const handleEdit = (row: any) => ElMessage.info(`编辑规则: ${row.name}`)
const handleDuplicate = (row: any) => ElMessage.success(`已复制规则: ${row.name}`)
const handleToggleStatus = (row: any) => {
  ElMessage.success(`规则 ${row.enabled ? '已启用' : '已停用'}`)
}
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除规则「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(() => {
      const index = rules.value.findIndex(r => r.id === row.id)
      if (index > -1) rules.value.splice(index, 1)
      ElMessage.success('已删除')
    })
}
</script>