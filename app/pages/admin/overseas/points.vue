<!--
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\overseas\points.vue
  版本: V2.0 (2026-03-12)
  数据来源: GET /v3/admin/overseas-points (真实接口)
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
        <div class="text-xs text-slate-500 mb-1">网点总数</div>
        <div class="text-2xl font-bold text-slate-800">{{ stats.total }}</div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
        <div class="text-xs text-slate-500 mb-1">已启用</div>
        <div class="text-2xl font-bold text-green-600">{{ stats.active }}</div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
        <div class="text-xs text-slate-500 mb-1">覆盖国家</div>
        <div class="text-2xl font-bold text-blue-600">{{ stats.countries }}</div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-5">
        <div class="text-xs text-slate-500 mb-1">覆盖大洲</div>
        <div class="text-2xl font-bold text-purple-600">{{ stats.continents }}</div>
      </div>
    </div>

    <!-- 服务网点列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">全球服务网点</h3>
            <p class="text-xs text-slate-500 mt-1">数据来源：/v3/admin/overseas-points（真实接口）</p>
          </div>
          <div class="flex gap-3">
            <el-button type="primary" @click="openAddDialog">
              <el-icon class="mr-2"><Plus /></el-icon> 添加网点
            </el-button>
            <el-button type="success" plain @click="handleExport">
              <el-icon class="mr-2"><Download /></el-icon> 导出列表
            </el-button>
          </div>
        </div>

        <!-- 筛选条件 -->
        <div class="flex gap-3 flex-wrap">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索网点/城市/国家/负责人..."
            :prefix-icon="Search"
            class="w-64"
            clearable
          />
          <el-select v-model="filters.continent" placeholder="大洲" class="w-32" clearable>
            <el-option label="亚洲" value="亚洲" />
            <el-option label="欧洲" value="欧洲" />
            <el-option label="北美洲" value="北美洲" />
            <el-option label="非洲" value="非洲" />
            <el-option label="大洋洲" value="大洋洲" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table v-loading="loading" :data="pointsList" stripe :header-cell-style="{ background: '#f8fafc', color: '#64748b' }">
          <el-table-column label="网点信息" min-width="300">
            <template #default="{ row }">
              <div class="py-2">
                <div class="flex items-start gap-3">
                  <div class="w-16 h-16 rounded bg-slate-100 flex items-center justify-center flex-shrink-0">
                    <el-icon class="text-2xl text-blue-500"><Location /></el-icon>
                  </div>
                  <div class="flex-1">
                    <div class="font-bold text-slate-800">{{ row.name }}</div>
                    <div class="text-xs text-slate-500 mt-1">{{ row.address || '-' }}</div>
                    <div class="flex gap-2 mt-2">
                      <el-tag size="small" type="warning">{{ row.continent }}</el-tag>
                      <el-tag size="small" type="primary">{{ row.country }}</el-tag>
                      <el-tag size="small" type="info">{{ row.city }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="服务类型" width="120" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.serviceType || '-' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="联系方式" width="200">
            <template #default="{ row }">
              <div class="text-sm">
                <div class="text-slate-700">{{ row.manager || '-' }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ row.phone || '-' }}</div>
                <div class="text-xs text-slate-500">{{ row.email || '-' }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="评分" width="80" align="center">
            <template #default="{ row }">
              <span class="text-yellow-500 font-bold">{{ row.rating || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
              <el-button link type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next, sizes"
            :total="totalItems"
            :page-sizes="[10, 20, 50]"
            @current-change="loadList"
            @size-change="() => { currentPage = 1; loadList() }"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑网点弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务网点' : '添加服务网点'" width="720px" :close-on-click-modal="false">
      <el-form :model="pointForm" label-width="120px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="大洲" required>
            <el-select v-model="pointForm.continent" placeholder="请选择大洲" class="w-full">
              <el-option label="亚洲" value="亚洲" />
              <el-option label="欧洲" value="欧洲" />
              <el-option label="北美洲" value="北美洲" />
              <el-option label="非洲" value="非洲" />
              <el-option label="大洋洲" value="大洋洲" />
            </el-select>
          </el-form-item>
          <el-form-item label="国家" required>
            <el-input v-model="pointForm.country" placeholder="请输入国家" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="城市" required>
            <el-input v-model="pointForm.city" placeholder="请输入城市" />
          </el-form-item>
          <el-form-item label="服务类型">
            <el-input v-model="pointForm.serviceType" placeholder="如：跨境企服" />
          </el-form-item>
        </div>
        <el-form-item label="网点名称" required>
          <el-input v-model="pointForm.name" placeholder="如：XRIPP纽约服务中心" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="pointForm.address" placeholder="详细地址" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="纬度" required>
            <el-input-number v-model="pointForm.lat" :precision="4" :step="0.01" class="w-full" />
          </el-form-item>
          <el-form-item label="经度" required>
            <el-input-number v-model="pointForm.lng" :precision="4" :step="0.01" class="w-full" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="联系人">
            <el-input v-model="pointForm.manager" placeholder="负责人姓名" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="pointForm.phone" placeholder="电话号码" />
          </el-form-item>
        </div>
        <el-form-item label="联系邮箱">
          <el-input v-model="pointForm.email" placeholder="service@example.com" />
        </el-form-item>
        <el-form-item label="服务范围">
          <el-input v-model="pointForm.servicesText" placeholder="多个用逗号分隔，如：标书翻译,法律咨询,资源对接" />
        </el-form-item>
        <el-form-item label="服务描述">
          <el-input v-model="pointForm.description" type="textarea" :rows="3" placeholder="网点服务概述" maxlength="500" show-word-limit />
        </el-form-item>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="评分">
            <el-input-number v-model="pointForm.rating" :min="1" :max="5" :precision="1" :step="0.1" class="w-full" />
          </el-form-item>
          <el-form-item label="响应时间">
            <el-input v-model="pointForm.responseTime" placeholder="如：2小时" />
          </el-form-item>
          <el-form-item label="成功案例数">
            <el-input-number v-model="pointForm.successCases" :min="0" class="w-full" />
          </el-form-item>
        </div>
        <el-form-item label="网点状态">
          <el-radio-group v-model="pointForm.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ isEdit ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="网点详情" width="720px">
      <div v-if="detailRow" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="网点名称" :span="2">{{ detailRow.name }}</el-descriptions-item>
          <el-descriptions-item label="大洲">{{ detailRow.continent }}</el-descriptions-item>
          <el-descriptions-item label="国家">{{ detailRow.country }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ detailRow.city }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ detailRow.serviceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="坐标">{{ detailRow.lat }}, {{ detailRow.lng }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detailRow.manager || '-' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detailRow.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailRow.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detailRow.rating || '-' }}</el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ detailRow.responseTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="成功案例">{{ detailRow.successCases || 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailRow.status === 'active' ? 'success' : 'info'" size="small">
              {{ detailRow.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detailRow.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务范围" :span="2">{{ parseServices(detailRow.servicesJson).join('、') || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailRow.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Plus, Location, Download, Search } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const detailVisible = ref(false)
const detailRow = ref<any>(null)

const pointsList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalItems = ref(0)

const stats = ref({ total: 0, active: 0, inactive: 0, countries: 0, continents: 0 })

const filters = ref({ keyword: '', continent: '', status: '' })

const defaultForm = () => ({
  id: 0,
  continent: '',
  country: '',
  city: '',
  name: '',
  address: '',
  serviceType: '',
  lat: 0,
  lng: 0,
  manager: '',
  phone: '',
  email: '',
  servicesText: '',
  description: '',
  rating: 5.0,
  responseTime: '2小时',
  successCases: 0,
  status: 'active'
})
const pointForm = ref(defaultForm())

const parseServices = (json: string) => {
  try { return JSON.parse(json || '[]') } catch { return [] }
}

const loadList = async () => {
  loading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    if (filters.value.keyword) query.keyword = filters.value.keyword.trim()
    if (filters.value.continent) query.continent = filters.value.continent
    if (filters.value.status) query.status = filters.value.status

    const res = await apiRequest<any>('/v3/admin/overseas-points', { query })
    pointsList.value = Array.isArray(res?.data?.items) ? res.data.items : []
    totalItems.value = Number(res?.data?.total) || 0
  } catch (e: any) {
    pointsList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '加载网点列表失败')
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await apiRequest<any>('/v3/admin/overseas-points/stats')
    if (res?.data) stats.value = res.data
  } catch { /* non-blocking */ }
}

onMounted(async () => {
  await Promise.all([loadList(), loadStats()])
})

const handleSearch = async () => {
  currentPage.value = 1
  await loadList()
}

const handleReset = async () => {
  filters.value = { keyword: '', continent: '', status: '' }
  currentPage.value = 1
  await loadList()
}

const openAddDialog = () => {
  isEdit.value = false
  pointForm.value = defaultForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  const services = parseServices(row.servicesJson)
  pointForm.value = {
    id: row.id,
    continent: row.continent || '',
    country: row.country || '',
    city: row.city || '',
    name: row.name || '',
    address: row.address || '',
    serviceType: row.serviceType || '',
    lat: Number(row.lat) || 0,
    lng: Number(row.lng) || 0,
    manager: row.manager || '',
    phone: row.phone || '',
    email: row.email || '',
    servicesText: services.join(','),
    description: row.description || '',
    rating: Number(row.rating) || 5.0,
    responseTime: row.responseTime || '2小时',
    successCases: Number(row.successCases) || 0,
    status: row.status || 'active'
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const f = pointForm.value
  if (!f.name || !f.continent || !f.country || !f.city) {
    return ElMessage.warning('请填写必填项（名称、大洲、国家、城市）')
  }
  if (!f.lat && !f.lng) {
    return ElMessage.warning('请填写坐标（纬度和经度）')
  }

  submitLoading.value = true
  try {
    const servicesArr = f.servicesText ? f.servicesText.split(/[,，]/).map(s => s.trim()).filter(Boolean) : []
    const body: Record<string, any> = {
      name: f.name,
      continent: f.continent,
      country: f.country,
      city: f.city,
      lat: f.lat,
      lng: f.lng,
      manager: f.manager,
      phone: f.phone,
      email: f.email,
      address: f.address,
      services_json: JSON.stringify(servicesArr),
      service_type: f.serviceType,
      description: f.description,
      rating: f.rating,
      response_time: f.responseTime,
      success_cases: f.successCases,
      status: f.status
    }

    if (isEdit.value) {
      await apiRequest(`/v3/admin/overseas-points/${f.id}`, { method: 'PUT', body })
      ElMessage.success('网点修改成功')
    } else {
      await apiRequest('/v3/admin/overseas-points', { method: 'POST', body })
      ElMessage.success('网点添加成功')
    }
    dialogVisible.value = false
    await Promise.all([loadList(), loadStats()])
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除服务网点 "${row.name}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await apiRequest(`/v3/admin/overseas-points/${row.id}`, { method: 'DELETE' })
        ElMessage.success('删除成功')
        await Promise.all([loadList(), loadStats()])
      } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const handleViewDetail = (row: any) => {
  detailRow.value = row
  detailVisible.value = true
}

const handleExport = () => {
  if (!pointsList.value.length) {
    return ElMessage.warning('暂无可导出的网点数据')
  }
  const headers = ['网点名称', '大洲', '国家', '城市', '服务类型', '负责人', '电话', '邮箱', '评分', '状态']
  const esc = (v: string) => `"${(v || '').replace(/"/g, '""')}"`
  const rows = pointsList.value.map(r => [
    r.name, r.continent, r.country, r.city, r.serviceType || '', r.manager || '',
    r.phone || '', r.email || '', String(r.rating || ''), r.status === 'active' ? '启用' : '禁用'
  ])
  const csv = [headers.map(esc).join(','), ...rows.map(r => r.map(esc).join(','))].join('\r\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `服务网点列表_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}
</script>
