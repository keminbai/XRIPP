<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\overseas\points.vue
  版本: V1.0 (2026-01-29)
  
  ✅ 核心功能:
  1. [网点设置] 添加和管理海外服务网点
  2. [地图展示] 可视化展示全球服务网络
  3. [网点信息] 联系方式、服务范围等详细信息
  4. [网点状态] 启用/禁用网点
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 服务网点列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">全球服务网点</h3>
            <p class="text-xs text-slate-500 mt-1">
              已设置 <span class="text-blue-600 font-bold">{{ pointsList.length }}</span> 个服务网点，
              覆盖 <span class="text-green-600 font-bold">{{ uniqueCountries }}</span> 个国家
            </p>
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
            placeholder="输入国际城市关键词..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.region" placeholder="所属区域" class="w-32" clearable>
            <el-option label="欧洲" value="Europe" />
            <el-option label="北美" value="NorthAmerica" />
            <el-option label="东南亚" value="SoutheastAsia" />
            <el-option label="中亚" value="CentralAsia" />
          </el-select>
          <el-select v-model="filters.country" placeholder="国家" class="w-32" clearable>
            <el-option label="美国" value="美国" />
            <el-option label="英国" value="英国" />
            <el-option label="德国" value="德国" />
            <el-option label="新加坡" value="新加坡" />
          </el-select>
          <el-select v-model="filters.status" placeholder="状态" class="w-32" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredPointsList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
          <el-table-column label="网点信息" min-width="300">
            <template #default="scope">
              <div class="py-2">
                <div class="flex items-start gap-3">
                  <div class="w-16 h-16 rounded bg-slate-100 flex items-center justify-center flex-shrink-0">
                    <el-icon class="text-2xl text-blue-500"><Location /></el-icon>
                  </div>
                  <div class="flex-1">
                    <div class="font-bold text-slate-800">{{ scope.row.name }}</div>
                    <div class="text-xs text-slate-500 mt-1">{{ scope.row.address }}</div>
                    <div class="flex gap-2 mt-2">
                      <el-tag size="small" type="warning">{{ getRegionLabel(scope.row.region) }}</el-tag>
                      <el-tag size="small" type="primary">{{ scope.row.country }}</el-tag>
                      <el-tag size="small" type="info">{{ scope.row.city }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="合伙人编号" width="140" align="center">
            <template #default="scope">
              <span class="font-mono text-xs text-slate-600 bg-slate-100 px-2 py-1 rounded">
                {{ scope.row.partnerId || '-' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="联系方式" width="200">
            <template #default="scope">
              <div class="text-sm">
                <div class="text-slate-700">{{ scope.row.manager || 'N/A' }}</div>
                <div class="text-xs text-slate-500 mt-1">{{ scope.row.phone }}</div>
                <div class="text-xs text-slate-500">{{ scope.row.email }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="服务范围" width="200">
            <template #default="scope">
              <div class="flex flex-wrap gap-1">
                <el-tag 
                  v-for="service in scope.row.services" 
                  :key="service" 
                  size="small" 
                  type="success"
                  effect="plain"
                >
                  {{ getServiceLabel(service) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
                {{ scope.row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="establishDate" label="设立时间" width="120" />
          <el-table-column prop="createDate" label="创建时间" width="120" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                <el-icon class="mr-1"><View /></el-icon> 详情
              </el-button>
              <el-button link type="warning" size="small" @click="handleEdit(scope.row)">
                <el-icon class="mr-1"><Edit /></el-icon> 编辑
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
            :total="filteredPointsList.length"
            :page-size="10"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑网点弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑服务网点' : '添加服务网点'"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form :model="pointForm" label-width="120px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="所属区域" required>
            <el-select v-model="pointForm.region" placeholder="请选择区域" class="w-full">
              <el-option label="欧洲" value="Europe" />
              <el-option label="北美" value="NorthAmerica" />
              <el-option label="东南亚" value="SoutheastAsia" />
              <el-option label="中亚" value="CentralAsia" />
            </el-select>
          </el-form-item>

          <el-form-item label="设立时间" required>
            <el-date-picker
              v-model="pointForm.establishDate"
              type="date"
              placeholder="选择设立时间"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="国家/地区" required>
            <el-select v-model="pointForm.country" placeholder="请选择国家" class="w-full" filterable>
              <el-option label="美国" value="美国" />
              <el-option label="英国" value="英国" />
              <el-option label="德国" value="德国" />
              <el-option label="法国" value="法国" />
              <el-option label="日本" value="日本" />
              <el-option label="韩国" value="韩国" />
              <el-option label="新加坡" value="新加坡" />
              <el-option label="阿联酋" value="阿联酋" />
              <el-option label="澳大利亚" value="澳大利亚" />
              <el-option label="加拿大" value="加拿大" />
            </el-select>
          </el-form-item>

          <el-form-item label="城市" required>
            <el-input v-model="pointForm.city" placeholder="请输入城市名称" />
          </el-form-item>
        </div>

        <el-form-item label="网点名称" required>
          <el-input v-model="pointForm.name" placeholder="如：IPP纽约服务中心" />
        </el-form-item>

        <el-form-item label="合伙人编号" required>
          <el-input v-model="pointForm.partnerId" placeholder="如：PART-NY-001" />
        </el-form-item>

        <el-form-item label="详细地址" required>
          <el-input v-model="pointForm.address" placeholder="请输入详细地址（包括街道、门牌号等）" />
        </el-form-item>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="联系人" required>
            <el-input v-model="pointForm.manager" placeholder="负责人姓名" />
          </el-form-item>

          <el-form-item label="联系电话" required>
            <el-input v-model="pointForm.phone" placeholder="+1-xxx-xxx-xxxx" />
          </el-form-item>
        </div>

        <el-form-item label="联系邮箱">
          <el-input v-model="pointForm.email" placeholder="service@example.com" />
        </el-form-item>

        <el-form-item label="服务范围">
          <el-checkbox-group v-model="pointForm.services">
            <el-checkbox label="market">市场调研</el-checkbox>
            <el-checkbox label="legal">法律咨询</el-checkbox>
            <el-checkbox label="business">商务对接</el-checkbox>
            <el-checkbox label="exhibition">展会服务</el-checkbox>
            <el-checkbox label="logistics">物流支持</el-checkbox>
            <el-checkbox label="registration">企业注册</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="服务点概述">
          <el-input
            v-model="pointForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入服务点概述..."
            maxlength="300"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="网点图片">
          <el-upload
            action="/api/common/upload"
            list-type="picture-card"
            :limit="3"
            :file-list="pointForm.images"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">最多上传3张，建议尺寸800x600px</div>
        </el-form-item>

        <el-form-item label="网点状态">
          <el-radio-group v-model="pointForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
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
    <el-dialog
      v-model="detailVisible"
      title="网点详情"
      width="720px"
      :close-on-click-modal="false"
    >
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">所属区域</div>
            <div class="detail-value">{{ getRegionLabel(detailRow.region) }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">设立时间</div>
            <div class="detail-value">{{ detailRow.establishDate || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">国家/地区</div>
            <div class="detail-value">{{ detailRow.country || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">城市</div>
            <div class="detail-value">{{ detailRow.city || '-' }}</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">网点名称</div>
            <div class="detail-value">{{ detailRow.name || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">合伙人编号</div>
            <div class="detail-value">{{ detailRow.partnerId || '-' }}</div>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">详细地址</div>
          <div class="detail-value">{{ detailRow.address || '-' }}</div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">联系人</div>
            <div class="detail-value">{{ detailRow.manager || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">联系电话</div>
            <div class="detail-value">{{ detailRow.phone || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">联系邮箱</div>
            <div class="detail-value">{{ detailRow.email || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">网点状态</div>
            <div class="detail-value">{{ detailRow.status === 'active' ? '启用' : '禁用' }}</div>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">服务范围</div>
          <div class="detail-value flex flex-wrap gap-2">
            <el-tag
              v-for="service in (detailRow.services || [])"
              :key="service"
              size="small"
              type="success"
              effect="plain"
            >
              {{ getServiceLabel(service) }}
            </el-tag>
            <span v-if="!detailRow.services || !detailRow.services.length">-</span>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">服务点概述</div>
          <div class="detail-value">{{ detailRow.description || '-' }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">网点图片</div>
          <div class="detail-value">
            <div v-if="detailRow.images && detailRow.images.length" class="flex flex-wrap gap-2">
              <el-image
                v-for="(img, idx) in detailRow.images"
                :key="idx"
                :src="img.url || img"
                :preview-src-list="detailRow.images.map(i => i.url || i)"
                fit="cover"
                class="detail-image"
              />
            </div>
            <span v-else>-</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { Plus, Location, Download, Search, View, Edit } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const detailVisible = ref(false)
const detailRow = ref<any>({})

const pointForm = ref({
  id: 0,
  region: '',
  country: '',
  city: '',
  name: '',
  address: '',
  partnerId: '',
  manager: '',
  phone: '',
  email: '',
  services: [] as string[],
  description: '',
  images: [] as any[],
  status: 'active',
  establishDate: '',
  createDate: ''
})

const filters = ref({
  keyword: '',
  region: '',
  country: '',
  status: ''
})

const pointsList = ref([
  {
    id: 1,
    region: 'NorthAmerica',
    country: '美国',
    city: '纽约',
    name: 'XRIPP纽约服务中心',
    address: '350 5th Avenue, New York, NY 10118',
    partnerId: 'PART-NY-001',
    manager: 'John Smith',
    phone: '+1-212-736-3100',
    email: 'newyork@xripp.com',
    services: ['market', 'legal', 'business'],
    description: '为北美地区客户提供全方位出海服务',
    status: 'active',
    establishDate: '2024-05-15',
    createDate: '2025-01-15'
  },
  {
    id: 2,
    region: 'Europe',
    country: '英国',
    city: '伦敦',
    name: 'XRIPP伦敦办事处',
    address: 'The Shard, 32 London Bridge St, London SE1 9SG',
    partnerId: 'PART-LD-002',
    manager: 'David Brown',
    phone: '+44-20-7234-5678',
    email: 'london@xripp.com',
    services: ['market', 'legal', 'business', 'exhibition'],
    description: '欧洲区域总部，覆盖欧盟市场',
    status: 'active',
    establishDate: '2024-08-20',
    createDate: '2025-02-20'
  },
  {
    id: 3,
    region: 'SoutheastAsia',
    country: '新加坡',
    city: '新加坡',
    name: 'XRIPP新加坡中心',
    address: 'Marina Bay Sands, 10 Bayfront Avenue',
    partnerId: 'PART-SG-003',
    manager: 'Lee Wei Ming',
    phone: '+65-6688-8888',
    email: 'singapore@xripp.com',
    services: ['market', 'business', 'logistics', 'registration'],
    description: '东南亚和亚太地区服务枢纽',
    status: 'active',
    establishDate: '2025-01-10',
    createDate: '2025-03-10'
  }
])

const uniqueCountries = computed(() => {
  return new Set(pointsList.value.map(p => p.country)).size
})

const filteredPointsList = computed(() => {
  let list = pointsList.value
  
  if (filters.value.keyword) {
    const keyword = filters.value.keyword.trim()
    const lowerKeyword = keyword.toLowerCase()
    list = list.filter(item => 
      item.city.includes(keyword) ||
      item.city.toLowerCase().includes(lowerKeyword)
    )
  }
  
  if (filters.value.region) {
    list = list.filter(item => item.region === filters.value.region)
  }
  
  if (filters.value.country) {
    list = list.filter(item => item.country === filters.value.country)
  }
  
  if (filters.value.status) {
    list = list.filter(item => item.status === filters.value.status)
  }
  
  return list
})

const getServiceLabel = (service: string) => {
  const map: Record<string, string> = {
    'market': '市场调研',
    'legal': '法律咨询',
    'business': '商务对接',
    'exhibition': '展会服务',
    'logistics': '物流支持',
    'registration': '企业注册'
  }
  return map[service] || service
}

const getRegionLabel = (region: string) => {
  const map: Record<string, string> = {
    'Europe': '欧洲',
    'NorthAmerica': '北美',
    'SoutheastAsia': '东南亚',
    'CentralAsia': '中亚'
  }
  return map[region] || region || '未设置'
}

const handleSubmit = () => {
  if (
    !pointForm.value.region ||
    !pointForm.value.country ||
    !pointForm.value.city ||
    !pointForm.value.name ||
    !pointForm.value.partnerId ||
    !pointForm.value.manager ||
    !pointForm.value.phone ||
    !pointForm.value.address ||
    !pointForm.value.establishDate
  ) {
    return ElMessage.warning('请填写必填项')
  }

  const partnerId = pointForm.value.partnerId.trim()
  const partnerIdPattern = /^PART-[A-Z]{2,5}-\d{3,}$/i
  if (!partnerIdPattern.test(partnerId)) {
    return ElMessage.warning('合伙人编号格式不正确（示例：PART-NY-001）')
  }
  pointForm.value.partnerId = partnerId.toUpperCase()
  
  submitLoading.value = true
  setTimeout(() => {
    const existing = isEdit.value
      ? pointsList.value.find(item => item.id === pointForm.value.id)
      : undefined
    const payload = {
      ...pointForm.value,
      id: isEdit.value ? pointForm.value.id : Date.now(),
      createDate: isEdit.value
        ? (pointForm.value.createDate || existing?.createDate || new Date().toISOString().split('T')[0])
        : new Date().toISOString().split('T')[0]
    }

    if (isEdit.value) {
      const index = pointsList.value.findIndex(item => item.id === payload.id)
      if (index !== -1) {
        pointsList.value[index] = payload
      }
    } else {
      pointsList.value.unshift(payload)
    }

    submitLoading.value = false
    ElMessage.success(isEdit.value ? '服务网点修改成功' : '服务网点添加成功')
    handleReset()
    dialogVisible.value = false
  }, 1000)
}

const handleReset = () => {
  pointForm.value = {
    id: 0,
    region: '',
    country: '',
    city: '',
    name: '',
    address: '',
    partnerId: '',
    manager: '',
    phone: '',
    email: '',
    services: [],
    description: '',
    images: [],
    status: 'active',
    establishDate: '',
    createDate: ''
  }
}

const handleSearch = () => {
  ElMessage.success('查询完成')
}

const handleExport = () => {
  if (!pointsList.value.length) {
    ElMessage.warning('暂无可导出的网点数据')
    return
  }

  const headers = [
    '所属区域',
    '国家/地区',
    '城市',
    '网点名称',
    '合伙人编号',
    '联系人',
    '联系电话',
    '联系邮箱',
    '服务范围',
    '设立时间',
    '创建时间',
    '状态',
    '详细地址',
    '服务点概述'
  ]

  const escapeCell = (value: string) => {
    const safe = value.replace(/"/g, '""')
    return `"${safe}"`
  }

  const rows = pointsList.value.map(item => {
    const services = (item.services || []).map(s => getServiceLabel(s)).join(' / ')
    const statusLabel = item.status === 'active' ? '启用' : '禁用'
    return [
      getRegionLabel(item.region),
      item.country || '',
      item.city || '',
      item.name || '',
      item.partnerId || '',
      item.manager || '',
      item.phone || '',
      item.email || '',
      services,
      item.establishDate || '',
      item.createDate || '',
      statusLabel,
      item.address || '',
      item.description || ''
    ]
  })

  const csvContent = [
    headers.map(escapeCell).join(','),
    ...rows.map(row => row.map(cell => escapeCell(String(cell))).join(','))
  ].join('\r\n')

  // Add UTF-8 BOM to improve Excel compatibility for Chinese characters
  const csvWithBom = `\uFEFF${csvContent}`
  const blob = new Blob([csvWithBom], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `服务网点列表_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('导出成功')
}

const handleViewDetail = (row: any) => {
  detailRow.value = { ...row }
  detailVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  pointForm.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除服务网点 "${row.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    const index = pointsList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      pointsList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  })
}

const openAddDialog = () => {
  isEdit.value = false
  handleReset()
  dialogVisible.value = true
}
</script>
