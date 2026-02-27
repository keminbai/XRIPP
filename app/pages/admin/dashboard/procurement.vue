<template>
  <div class="space-y-6">
    <!-- 基础展示配置 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">采购数据配置</h3>
        <p class="text-xs text-slate-500 mt-1">配置首页数据看板展示的采购相关数据</p>
      </div>

      <el-form :model="form" label-width="150px" class="max-w-3xl">
        <el-form-item label="展示采购总额">
          <el-switch v-model="form.showTotal" />
        </el-form-item>
        <el-form-item label="展示本月新增">
          <el-switch v-model="form.showMonthly" />
        </el-form-item>
        <el-form-item label="展示采购趋势图">
          <el-switch v-model="form.showTrend" />
        </el-form-item>
        <el-form-item label="数据刷新频率">
          <el-select v-model="form.refreshRate" class="w-full">
            <el-option label="实时" value="realtime" />
            <el-option label="每小时" value="hourly" />
            <el-option label="每天" value="daily" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据来源">
          <el-input v-model="form.dataSource" placeholder="API接口地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
          <el-button @click="handleResetConfig">重置配置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 子项目录入与发布 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">公共采购数据录入与发布</h3>
          <p class="text-xs text-slate-500 mt-1">联合国采购数据、采购主体分布、采购类目排名与各国采购数据</p>
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
        <el-form-item label="联合国采购总额">
          <div class="flex items-center gap-2 w-full">
            <el-input-number v-model="entry.totalAmount" :min="0" :step="100000" class="!w-56" />
            <el-select v-model="entry.currency" class="w-24">
              <el-option label="USD" value="USD" />
              <el-option label="CNY" value="CNY" />
            </el-select>
            <span class="text-xs text-slate-500">用于首页总额显示</span>
          </div>
        </el-form-item>
        <el-form-item label="本月新增采购">
          <el-input-number v-model="entry.monthlyNew" :min="0" :step="1000" class="!w-56" />
        </el-form-item>
      </el-form>

      <!-- 采购主体分布 -->
      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">采购主体分布</h4>
          <el-button size="small" @click="addBuyer">添加主体</el-button>
        </div>
        <el-table :data="buyers" stripe border>
          <el-table-column prop="name" label="主体名称" min-width="200">
            <template #default="scope">
              <el-input v-model="scope.row.name" placeholder="如：UNICEF" />
            </template>
          </el-table-column>
          <el-table-column prop="country" label="所在国家" width="160">
            <template #default="scope">
              <el-input v-model="scope.row.country" placeholder="国家" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="采购金额" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.amount" :min="0" :step="10000" class="!w-32" />
            </template>
          </el-table-column>
          <el-table-column prop="share" label="占比" width="120">
            <template #default="scope">
              <el-input v-model="scope.row.share" placeholder="%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(buyers, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 采购类目排名 -->
      <div class="mt-8">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">采购类目排名</h4>
          <el-button size="small" @click="addCategory">添加类目</el-button>
        </div>
        <el-table :data="categories" stripe border>
          <el-table-column prop="rank" label="排名" width="90">
            <template #default="scope">
              <el-input-number v-model="scope.row.rank" :min="1" :step="1" class="!w-20" />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="类目" min-width="200">
            <template #default="scope">
              <el-input v-model="scope.row.name" placeholder="如：医疗器械" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="采购金额" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.amount" :min="0" :step="10000" class="!w-32" />
            </template>
          </el-table-column>
          <el-table-column prop="share" label="占比" width="120">
            <template #default="scope">
              <el-input v-model="scope.row.share" placeholder="%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(categories, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 各国采购数据录入 -->
      <div class="mt-8">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">各国采购数据</h4>
          <el-button size="small" @click="addCountry">添加国家</el-button>
        </div>
        <el-table :data="countryData" stripe border>
          <el-table-column prop="country" label="国家" min-width="180">
            <template #default="scope">
              <el-input v-model="scope.row.country" placeholder="国家" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="采购金额" width="160">
            <template #default="scope">
              <el-input-number v-model="scope.row.amount" :min="0" :step="10000" class="!w-32" />
            </template>
          </el-table-column>
          <el-table-column prop="projects" label="项目数量" width="140">
            <template #default="scope">
              <el-input-number v-model="scope.row.projects" :min="0" :step="1" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="发布状态" width="140">
            <template #default="scope">
              <el-select v-model="scope.row.status" size="small" class="w-full">
                <el-option label="草稿" value="draft" />
                <el-option label="已发布" value="published" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(countryData, scope.$index)">删除</el-button>
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
  showMonthly: true,
  showTrend: true,
  refreshRate: 'hourly',
  dataSource: '/api/dashboard/procurement'
})

const entry = ref({
  asOfDate: '2026-02-01',
  totalAmount: 120000000,
  monthlyNew: 8500000,
  currency: 'USD'
})

const buyers = ref([
  { name: 'UNICEF', country: '美国', amount: 25000000, share: '21%' },
  { name: 'WFP', country: '意大利', amount: 18000000, share: '15%' }
])

const categories = ref([
  { rank: 1, name: '医疗器械', amount: 22000000, share: '18%' },
  { rank: 2, name: '物流运输', amount: 17000000, share: '14%' }
])

const countryData = ref([
  { country: '美国', amount: 30000000, projects: 120, status: 'published' },
  { country: '德国', amount: 14000000, projects: 65, status: 'draft' }
])

const handleSaveConfig = () => ElMessage.success('采购数据配置已保存')
const handleResetConfig = () => {
  form.value = {
    showTotal: true,
    showMonthly: true,
    showTrend: true,
    refreshRate: 'hourly',
    dataSource: '/api/dashboard/procurement'
  }
  ElMessage.success('已重置配置')
}

const handlePublish = () => ElMessage.success('采购数据已发布')
const handleResetData = () => {
  entry.value = { asOfDate: '', totalAmount: 0, monthlyNew: 0, currency: 'USD' }
  buyers.value = []
  categories.value = []
  countryData.value = []
  ElMessage.warning('录入数据已清空')
}

const addBuyer = () => buyers.value.push({ name: '', country: '', amount: 0, share: '' })
const addCategory = () => categories.value.push({ rank: categories.value.length + 1, name: '', amount: 0, share: '' })
const addCountry = () => countryData.value.push({ country: '', amount: 0, projects: 0, status: 'draft' })
const removeRow = (list: any[], index: number) => list.splice(index, 1)
</script>
