<template>
  <div class="space-y-6">
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">服务网络配置</h3>
        <p class="text-xs text-slate-500 mt-1">配置首页数据看板展示的服务网络分布数据</p>
      </div>

      <el-form :model="form" label-width="150px" class="max-w-3xl">
        <el-form-item label="展示服务网点数">
          <el-switch v-model="form.showPoints" />
        </el-form-item>
        <el-form-item label="展示地图分布">
          <el-switch v-model="form.showMap" />
        </el-form-item>
        <el-form-item label="展示合伙人数量">
          <el-switch v-model="form.showPartners" />
        </el-form-item>
        <el-form-item label="地图类型">
          <el-radio-group v-model="form.mapType">
            <el-radio label="china">中国地图</el-radio>
            <el-radio label="world">世界地图</el-radio>
          </el-radio-group>
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
          <h3 class="text-lg font-bold text-slate-800">服务网点数据录入与发布</h3>
          <p class="text-xs text-slate-500 mt-1">服务点城市设置，自动匹配服务点总数与省份数量</p>
        </div>
        <div class="flex gap-2">
          <el-button type="success" @click="handlePublish">发布数据</el-button>
          <el-button @click="handleResetData">清空录入</el-button>
        </div>
      </div>

      <el-form :model="summary" label-width="150px" class="max-w-4xl">
        <el-form-item label="统计口径日期">
          <el-date-picker v-model="summary.asOfDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="服务点总数">
          <el-input-number v-model="summary.totalPoints" :min="0" :step="10" class="!w-56" />
        </el-form-item>
        <el-form-item label="覆盖国家数">
          <el-input-number v-model="summary.totalCountries" :min="0" :step="1" class="!w-56" />
        </el-form-item>
        <el-form-item label="覆盖省份数">
          <el-input-number v-model="summary.totalProvinces" :min="0" :step="1" class="!w-56" />
        </el-form-item>
        <el-form-item label="合伙人数量">
          <el-input-number v-model="summary.totalPartners" :min="0" :step="1" class="!w-56" />
        </el-form-item>
      </el-form>

      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <h4 class="font-bold text-slate-700">服务点城市设置</h4>
          <el-button size="small" @click="addCity">添加城市</el-button>
        </div>
        <el-table :data="cities" stripe border>
          <el-table-column prop="city" label="城市" min-width="180">
            <template #default="scope">
              <el-input v-model="scope.row.city" placeholder="城市" />
            </template>
          </el-table-column>
          <el-table-column prop="country" label="国家/地区" width="180">
            <template #default="scope">
              <el-input v-model="scope.row.country" placeholder="国家" />
            </template>
          </el-table-column>
          <el-table-column prop="region" label="区域" width="140">
            <template #default="scope">
              <el-input v-model="scope.row.region" placeholder="如：华东/欧洲" />
            </template>
          </el-table-column>
          <el-table-column prop="points" label="服务点数" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.points" :min="0" :step="1" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column prop="provinces" label="省份数" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.provinces" :min="0" :step="1" class="!w-24" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.status" size="small" class="w-full">
                <el-option label="启用" value="active" />
                <el-option label="停用" value="inactive" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" size="small" @click="removeRow(cities, scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="mt-8 max-w-4xl">
        <h4 class="font-bold text-slate-700 mb-3">自动匹配设置</h4>
        <el-form :model="autoConfig" label-width="150px">
          <el-form-item label="自动匹配服务点总数">
            <el-switch v-model="autoConfig.autoTotalPoints" />
            <span class="ml-2 text-xs text-slate-500">根据城市配置自动汇总</span>
          </el-form-item>
          <el-form-item label="自动匹配省份数量">
            <el-switch v-model="autoConfig.autoProvinceCount" />
            <span class="ml-2 text-xs text-slate-500">仅用于国内统计</span>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const form = ref({
  showPoints: true,
  showMap: true,
  showPartners: true,
  mapType: 'china'
})

const summary = ref({
  asOfDate: '2026-02-01',
  totalPoints: 86,
  totalCountries: 32,
  totalProvinces: 18,
  totalPartners: 46
})

const cities = ref([
  { city: '上海', country: '中国', region: '华东', points: 8, provinces: 1, status: 'active' },
  { city: '新加坡', country: '新加坡', region: '东南亚', points: 3, provinces: 0, status: 'active' }
])

const autoConfig = ref({ autoTotalPoints: true, autoProvinceCount: true })

const handleSaveConfig = () => ElMessage.success('服务网络配置已保存')
const handleResetConfig = () => {
  form.value = { showPoints: true, showMap: true, showPartners: true, mapType: 'china' }
  ElMessage.success('已重置配置')
}

const handlePublish = () => ElMessage.success('服务网点数据已发布')
const handleResetData = () => {
  summary.value = { asOfDate: '', totalPoints: 0, totalCountries: 0, totalProvinces: 0, totalPartners: 0 }
  cities.value = []
  ElMessage.warning('录入数据已清空')
}

const addCity = () => cities.value.push({ city: '', country: '', region: '', points: 0, provinces: 0, status: 'active' })
const removeRow = (list: any[], index: number) => list.splice(index, 1)
</script>
