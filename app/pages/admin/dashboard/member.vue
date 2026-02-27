<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">会员数据配置</h3>
        <p class="text-xs text-slate-500 mt-1">配置首页数据看板展示的会员相关数据</p>
      </div>

      <el-form :model="form" label-width="150px" class="max-w-3xl">
        <el-form-item label="展示会员总数">
          <el-switch v-model="form.showTotal" />
        </el-form-item>
        <el-form-item label="展示会员增长">
          <el-switch v-model="form.showGrowth" />
        </el-form-item>
        <el-form-item label="展示会员分布图">
          <el-switch v-model="form.showDistribution" />
        </el-form-item>
        <el-form-item label="会员等级展示">
          <el-checkbox-group v-model="form.memberLevels">
            <el-checkbox label="SVIP">SVIP会员</el-checkbox>
            <el-checkbox label="VIP">VIP会员</el-checkbox>
            <el-checkbox label="NORMAL">普通会员</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
          <el-button @click="handleResetConfig">重置配置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">会员数据录入与发布</h3>
          <p class="text-xs text-slate-500 mt-1">会员入驻情况、新会员增量、企业类型占比、会员概况</p>
        </div>
        <div class="flex gap-2">
          <el-button type="success" @click="handlePublish">发布数据</el-button>
          <el-button @click="handleResetData">清空录入</el-button>
        </div>
      </div>

      <el-form :model="entry" label-width="150px" class="max-w-4xl">
        <el-form-item label="统计口径日期">
          <el-date-picker v-model="entry.asOfDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="会员总数">
          <el-input-number v-model="entry.totalMembers" :min="0" :step="100" class="!w-56" />
        </el-form-item>
        <el-form-item label="本月新增会员">
          <el-input-number v-model="entry.newMembers" :min="0" :step="10" class="!w-56" />
        </el-form-item>
        <el-form-item label="活跃会员数">
          <el-input-number v-model="entry.activeMembers" :min="0" :step="10" class="!w-56" />
        </el-form-item>
        <el-form-item label="会员概况描述">
          <el-input v-model="entry.summary" type="textarea" :rows="3" placeholder="简要描述会员结构与趋势" />
        </el-form-item>
      </el-form>

      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">企业类型占比</h4>
          <el-button size="small" @click="addCompanyType">添加类型</el-button>
        </div>
        <el-table :data="companyTypes" stripe border>
          <el-table-column prop="type" label="企业类型" min-width="200">
            <template #default="scope">
              <el-input v-model="scope.row.type" placeholder="如：制造业" />
            </template>
          </el-table-column>
          <el-table-column prop="count" label="数量" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.count" :min="0" :step="10" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column prop="share" label="占比" width="120">
            <template #default="scope">
              <el-input v-model="scope.row.share" placeholder="%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(companyTypes, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="mt-8">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">会员增长趋势（月度）</h4>
          <el-button size="small" @click="addGrowth">添加月份</el-button>
        </div>
        <el-table :data="growth" stripe border>
          <el-table-column prop="month" label="月份" width="160">
            <template #default="scope">
              <el-input v-model="scope.row.month" placeholder="2026-02" />
            </template>
          </el-table-column>
          <el-table-column prop="new" label="新增" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.new" :min="0" :step="5" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column prop="churn" label="流失" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.churn" :min="0" :step="5" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(growth, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const form = ref({
  showTotal: true,
  showGrowth: true,
  showDistribution: true,
  memberLevels: ['SVIP', 'VIP', 'NORMAL']
})

const entry = ref({
  asOfDate: '2026-02-01',
  totalMembers: 12860,
  newMembers: 420,
  activeMembers: 7560,
  summary: '本月会员增长稳定，制造业与信息技术企业占比提升。'
})

const companyTypes = ref([
  { type: '制造业', count: 3400, share: '26%' },
  { type: '信息技术', count: 2600, share: '20%' }
])

const growth = ref([
  { month: '2026-01', new: 380, churn: 45 },
  { month: '2026-02', new: 420, churn: 52 }
])

const handleSaveConfig = () => ElMessage.success('会员数据配置已保存')
const handleResetConfig = () => {
  form.value = { showTotal: true, showGrowth: true, showDistribution: true, memberLevels: ['SVIP', 'VIP', 'NORMAL'] }
  ElMessage.success('已重置配置')
}

const handlePublish = () => ElMessage.success('会员数据已发布')
const handleResetData = () => {
  entry.value = { asOfDate: '', totalMembers: 0, newMembers: 0, activeMembers: 0, summary: '' }
  companyTypes.value = []
  growth.value = []
  ElMessage.warning('录入数据已清空')
}

const addCompanyType = () => companyTypes.value.push({ type: '', count: 0, share: '' })
const addGrowth = () => growth.value.push({ month: '', new: 0, churn: 0 })
const removeRow = (list: any[], index: number) => list.splice(index, 1)
</script>
