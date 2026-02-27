<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\finance\profit.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [分成配置] 各城市合伙人分成比例设置
  2. [分成统计] 各合伙人分成金额统计
  3. [分成明细] 分成记录查询
  4. [结算管理] 分成结算处理
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div
        v-for="(stat, i) in stats"
        :key="i"
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm text-slate-500">{{ stat.label }}</div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
        </div>
        <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm">
      <el-tabs v-model="activeTab" class="p-6">
        <!-- 分成配置 -->
        <el-tab-pane label="分成配置" name="config">
          <div class="space-y-4">
            <div class="flex justify-between items-center mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-800">合伙人分成比例设置</h3>
                <p class="text-xs text-slate-500 mt-1">设置各城市合伙人的分成比例</p>
              </div>
              <div class="flex gap-2">
                <el-button @click="openConfigDialog()">
                  <el-icon class="mr-2"><Plus /></el-icon> 新增配置
                </el-button>
                <el-button type="primary" @click="handleSaveConfig">
                  <el-icon class="mr-2"><Check /></el-icon> 保存配置
                </el-button>
              </div>
            </div>

            <el-table :data="partnerConfig" border stripe>
              <el-table-column prop="city" label="城市" width="120" />
              <el-table-column prop="partnerName" label="合伙人" width="150" />
              <el-table-column label="分成比例" width="200">
                <template #default="scope">
                  <el-slider v-model="scope.row.percentage" :min="5" :max="30" show-stops />
                  <div class="text-xs text-slate-500 mt-1">{{ scope.row.percentage }}%</div>
                </template>
              </el-table-column>
              <el-table-column label="适用业务" min-width="250">
                <template #default="scope">
                  <el-checkbox-group v-model="scope.row.businessLines" size="small">
                    <el-checkbox label="membership">会员费</el-checkbox>
                    <el-checkbox label="tender">标书销售</el-checkbox>
                    <el-checkbox label="training">培训收入</el-checkbox>
                  </el-checkbox-group>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-switch v-model="scope.row.enabled" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="140">
                <template #default="scope">
                  <el-button link type="primary" @click="openConfigDialog(scope.row)">编辑</el-button>
                  <el-button link type="danger" @click="handleRemoveConfig(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 分成统计 -->
        <el-tab-pane label="分成统计" name="stats">
          <div class="space-y-4">
            <div class="mb-4">
              <h3 class="text-base font-bold text-slate-800">各合伙人分成统计</h3>
              <p class="text-xs text-slate-500 mt-1">查看各合伙人的分成金额和明细</p>
            </div>

            <el-table :data="profitStats" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
              <el-table-column prop="city" label="城市" width="120" />
              <el-table-column prop="partnerName" label="合伙人" width="150" />
              <el-table-column prop="percentage" label="分成比例" width="100">
                <template #default="scope">
                  <el-tag size="small">{{ scope.row.percentage }}%</el-tag>
                </template>
              </el-table-column>

              <el-table-column label="本月营收" width="150" align="right">
                <template #default="scope">
                  <span class="font-medium">¥{{ scope.row.monthRevenue.toLocaleString() }}</span>
                </template>
              </el-table-column>

              <el-table-column label="本月分成" width="150" align="right">
                <template #default="scope">
                  <span class="text-green-600 font-bold">¥{{ scope.row.monthProfit.toLocaleString() }}</span>
                </template>
              </el-table-column>

              <el-table-column label="累计分成" width="150" align="right">
                <template #default="scope">
                  <span class="font-medium">¥{{ scope.row.totalProfit.toLocaleString() }}</span>
                </template>
              </el-table-column>

              <el-table-column label="结算状态" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.settled ? 'success' : 'warning'" size="small">
                    {{ scope.row.settled ? '已结算' : '待结算' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                    查看明细
                  </el-button>
                  <el-button
                    v-if="!scope.row.settled"
                    link
                    type="success"
                    size="small"
                    @click="handleSettle(scope.row)"
                  >
                    结算
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 分成明细 -->
        <el-tab-pane label="分成明细" name="records">
          <div class="space-y-4">
            <div class="flex gap-3 mb-4">
              <el-select v-model="filters.city" placeholder="选择城市" class="w-32" clearable>
                <el-option label="上海" value="上海" />
                <el-option label="北京" value="北京" />
                <el-option label="深圳" value="深圳" />
              </el-select>
              <el-date-picker
                v-model="filters.dateRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                class="w-64"
              />
              <el-button type="primary" plain @click="handleSearch">查询</el-button>
            </div>

            <el-table :data="filteredRecords" stripe>
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="city" label="城市" width="100" />
              <el-table-column prop="orderNo" label="订单号" width="180">
                <template #default="scope">
                  <span class="font-mono text-xs">{{ scope.row.orderNo }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="businessLine" label="业务类型" width="120">
                <template #default="scope">
                  <el-tag size="small">{{ getBusinessLabel(scope.row.businessLine) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="订单金额" width="120" align="right">
                <template #default="scope">
                  ¥{{ scope.row.orderAmount.toLocaleString() }}
                </template>
              </el-table-column>
              <el-table-column prop="percentage" label="分成比例" width="100" align="center" />
              <el-table-column label="分成金额" width="120" align="right">
                <template #default="scope">
                  <span class="text-green-600 font-bold">¥{{ scope.row.profitAmount.toLocaleString() }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 配置弹窗 -->
    <el-dialog v-model="configDialogVisible" :title="configForm.id ? '编辑分成配置' : '新增分成配置'" width="600px">
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="城市" required>
          <el-input v-model="configForm.city" />
        </el-form-item>
        <el-form-item label="合伙人" required>
          <el-input v-model="configForm.partnerName" />
        </el-form-item>
        <el-form-item label="分成比例" required>
          <el-input-number v-model="configForm.percentage" :min="5" :max="30" :step="1" class="!w-full" />
        </el-form-item>
        <el-form-item label="适用业务">
          <el-checkbox-group v-model="configForm.businessLines">
            <el-checkbox label="membership">会员费</el-checkbox>
            <el-checkbox label="tender">标书销售</el-checkbox>
            <el-checkbox label="training">培训收入</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="configForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfigDialog">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Coin, TrendCharts, Money, UserFilled, Check, Plus } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const activeTab = ref('config')
const filters = ref({ city: '', dateRange: [] as string[] })

const stats = computed(() => [
  { label: '本月总分成', value: '¥68.5K', icon: Coin, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '待结算', value: '¥23.2K', icon: Money, bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
  { label: '合伙人数', value: String(partnerConfig.value.length), icon: UserFilled, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '累计分成', value: '¥856K', icon: TrendCharts, bgClass: 'bg-purple-50', textClass: 'text-purple-600' }
])

const partnerConfig = ref([
  { id: 1, city: '上海', partnerName: '张三', percentage: 15, businessLines: ['membership', 'tender'], enabled: true },
  { id: 2, city: '北京', partnerName: '李四', percentage: 18, businessLines: ['membership', 'tender', 'training'], enabled: true },
  { id: 3, city: '深圳', partnerName: '王五', percentage: 12, businessLines: ['tender'], enabled: true }
])

const profitStats = ref([
  { city: '上海', partnerName: '张三', percentage: 15, monthRevenue: 50000, monthProfit: 7500, totalProfit: 125000, settled: false },
  { city: '北京', partnerName: '李四', percentage: 18, monthRevenue: 68000, monthProfit: 12240, totalProfit: 285000, settled: true },
  { city: '深圳', partnerName: '王五', percentage: 12, monthRevenue: 42000, monthProfit: 5040, totalProfit: 98000, settled: false }
])

const profitRecords = ref([
  { date: '2026-01-28', city: '上海', orderNo: 'MEM202601280001', businessLine: 'membership', orderAmount: 9800, percentage: '15%', profitAmount: 1470 },
  { date: '2026-01-27', city: '北京', orderNo: 'TND202601270002', businessLine: 'tender', orderAmount: 299, percentage: '18%', profitAmount: 54 }
])

const filteredRecords = computed(() => {
  let list = profitRecords.value
  if (filters.value.city) list = list.filter(x => x.city === filters.value.city)
  return list
})

const getBusinessLabel = (line: string) => {
  const map: Record<string, string> = { membership: '会员费', tender: '标书销售', training: '培训收入' }
  return map[line] || '未知'
}

// 保存配置（占位）
const handleSaveConfig = () => ElMessage.success('分成配置已保存')
const handleViewDetail = () => ElMessage.info('查看明细功能开发中...')
const handleSearch = () => ElMessage.success('查询完成')

const handleSettle = (row: any) => {
  ElMessageBox.confirm(`确认结算 ${row.city} ${row.partnerName} 本月分成（¥${row.monthProfit.toLocaleString()}）吗？`, '确认结算', {
    type: 'success'
  }).then(() => {
    row.settled = true
    ElMessage.success('结算完成，款项将在3个工作日内到账')
  })
}

// 配置弹窗
const configDialogVisible = ref(false)
const configForm = ref({ id: 0, city: '', partnerName: '', percentage: 10, businessLines: [] as string[], enabled: true })

const openConfigDialog = (row?: any) => {
  configForm.value = row ? { ...row } : { id: 0, city: '', partnerName: '', percentage: 10, businessLines: [], enabled: true }
  configDialogVisible.value = true
}

const handleSaveConfigDialog = () => {
  if (!configForm.value.city || !configForm.value.partnerName) return ElMessage.warning('请填写城市和合伙人')
  const payload = { ...configForm.value }
  if (payload.id) {
    const idx = partnerConfig.value.findIndex(x => x.id === payload.id)
    if (idx !== -1) partnerConfig.value[idx] = payload
  } else {
    payload.id = Date.now()
    partnerConfig.value.push(payload)
  }
  configDialogVisible.value = false
  ElMessage.success('已保存')
}

const handleRemoveConfig = (row: any) => {
  ElMessageBox.confirm(`确定删除 ${row.city} - ${row.partnerName} 吗？`, '提示', { type: 'warning' }).then(() => {
    partnerConfig.value = partnerConfig.value.filter(x => x.id !== row.id)
    ElMessage.success('已删除')
  })
}
</script>
