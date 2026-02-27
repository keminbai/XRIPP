<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\overseas\reports.vue
  版本: V1.3 (2026-02-06 修复编码与导出)
  
  ✅ 核心修复:
  1. [编码修复] 修正文件乱码，恢复中文可读性
  2. [导出CSV] 保留UTF-8 BOM，确保Excel中文正常显示
  3. [过滤增强] 补齐收费类型与报告类型筛选
  4. [上传回显] 统一文件结构，确保Upload回显可用
  5. [交互优化] 完整校验与友好提示
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 报告上传表单 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex items-start justify-between mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">
            {{ isEdit ? `编辑报告：${reportForm.title || '未命名报告'}` : '上传行业报告' }}
          </h3>
          <p v-if="isEdit" class="text-xs text-amber-600 mt-1">正在编辑中，保存后将覆盖原记录</p>
        </div>
        <el-button v-if="isEdit" type="warning" plain @click="handleCancelEdit">
          取消编辑
        </el-button>
      </div>
      
      <el-form :model="reportForm" label-width="120px" class="max-w-3xl">
        <el-form-item label="报告标题" required>
          <el-input 
            v-model="reportForm.title" 
            placeholder="请输入报告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="目标国家/地区" required>
            <el-select v-model="reportForm.country" placeholder="请选择国家" class="w-full">
              <el-option label="美国" value="美国" />
              <el-option label="英国" value="英国" />
              <el-option label="德国" value="德国" />
              <el-option label="法国" value="法国" />
              <el-option label="日本" value="日本" />
              <el-option label="韩国" value="韩国" />
              <el-option label="新加坡" value="新加坡" />
              <el-option label="全球" value="全球" />
            </el-select>
          </el-form-item>

          <el-form-item label="行业分类" required>
            <el-select v-model="reportForm.industry" placeholder="请选择行业" class="w-full">
              <el-option label="医疗健康" value="medical" />
              <el-option label="信息技术" value="it" />
              <el-option label="制造业" value="manufacturing" />
              <el-option label="金融服务" value="finance" />
              <el-option label="消费品" value="consumer" />
              <el-option label="能源环保" value="energy" />
              <el-option label="农业" value="agriculture" />
              <el-option label="综合" value="comprehensive" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="报告类型" required>
          <el-select v-model="reportForm.type" class="w-full">
            <el-option label="数据报告" value="data" />
            <el-option label="调研报告" value="research" />
            <el-option label="指南" value="guide" />
            <el-option label="行业报告" value="industry" />
            <el-option label="评估报告" value="evaluation" />
            <el-option label="可行性报告" value="feasibility" />
          </el-select>
        </el-form-item>

        <el-form-item label="报告年份">
          <el-date-picker
            v-model="reportForm.year"
            type="year"
            placeholder="选择年份"
            format="YYYY"
            value-format="YYYY"
          />
        </el-form-item>

        <el-form-item label="发布时间" required>
          <el-date-picker
            v-model="reportForm.publishDate"
            type="date"
            placeholder="选择发布时间"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="报告摘要" required>
          <el-input 
            v-model="reportForm.summary" 
            type="textarea" 
            :rows="4"
            placeholder="请输入报告摘要，简要说明报告核心内容..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="报告封面">
          <el-upload
            action="#"
            :auto-upload="false"
            list-type="picture-card"
            :limit="1"
            :file-list="coverFileList"
            :on-change="handleCoverChange"
            :on-remove="handleCoverRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">建议尺寸400x560px，格式JPG/PNG</div>
        </el-form-item>

        <el-form-item label="报告文件" required>
          <el-upload
            action="#"
            :auto-upload="false"
            :limit="1"
            :file-list="reportFileList"
            accept=".pdf,.doc,.docx"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button type="primary">
              <el-icon class="mr-2"><Upload /></el-icon> 上传报告文件
            </el-button>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">
            支持PDF、DOC、DOCX格式，大小不超过50MB
          </div>
        </el-form-item>

        <el-form-item label="关键词">
          <el-select 
            v-model="reportForm.keywords" 
            multiple 
            allow-create
            filterable
            placeholder="输入关键词后按回车添加"
            class="w-full"
          />
        </el-form-item>

        <el-form-item label="报告来源">
          <el-input v-model="reportForm.source" placeholder="如：麦肯锡、BCG、德勤等" />
        </el-form-item>

        <el-form-item label="关联收费">
          <el-select v-model="reportForm.feeLevel" placeholder="选择收费类型" class="w-full">
            <el-option label="免费" value="free" />
            <el-option label="报告收费1" value="fee_1" />
            <el-option label="报告收费2" value="fee_2" />
          </el-select>
        </el-form-item>

        <el-form-item label="访问权限">
          <el-radio-group v-model="reportForm.accessLevel">
            <el-radio label="public">公开（所有会员）</el-radio>
            <el-radio label="vip">VIP会员</el-radio>
            <el-radio label="svip">SVIP会员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="是否推荐">
          <el-switch v-model="reportForm.isRecommended" />
          <span class="ml-2 text-xs text-slate-500">推荐后将在首页展示</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleUpload" :loading="submitLoading">
            <el-icon class="mr-2"><Upload /></el-icon>
            {{ isEdit ? '保存修改' : '上传报告' }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-2"><RefreshLeft /></el-icon> {{ isEdit ? '重置编辑' : '重置' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 报告列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">行业报告库</h3>
            <p class="text-xs text-slate-500 mt-1">
              共 <span class="text-blue-600 font-bold">{{ reportsList.length }}</span> 份报告，
              累计下载 <span class="text-green-600 font-bold">{{ totalDownloads }}</span> 次
            </p>
          </div>
          <el-button type="success" plain @click="handleExportList">
            <el-icon class="mr-2"><Download /></el-icon> 导出清单
          </el-button>
        </div>

        <!-- 筛选条件 -->
        <div class="flex gap-3 flex-wrap">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索报告标题..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.country" placeholder="国家" class="w-32" clearable>
            <el-option label="美国" value="美国" />
            <el-option label="英国" value="英国" />
            <el-option label="全球" value="全球" />
          </el-select>
          <el-select v-model="filters.industry" placeholder="行业" class="w-32" clearable>
            <el-option label="医疗健康" value="medical" />
            <el-option label="信息技术" value="it" />
            <el-option label="制造业" value="manufacturing" />
          </el-select>
          <el-select v-model="filters.type" placeholder="报告类型" class="w-32" clearable>
            <el-option label="数据报告" value="data" />
            <el-option label="调研报告" value="research" />
            <el-option label="指南" value="guide" />
            <el-option label="行业报告" value="industry" />
            <el-option label="评估报告" value="evaluation" />
            <el-option label="可行性报告" value="feasibility" />
          </el-select>
          <el-date-picker
            v-model="filters.publishRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
          <el-select v-model="filters.feeLevel" placeholder="关联收费" class="w-32" clearable>
            <el-option label="免费" value="free" />
            <el-option label="报告收费1" value="fee_1" />
            <el-option label="报告收费2" value="fee_2" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredReportsList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
          <el-table-column label="报告信息" min-width="350">
            <template #default="scope">
              <div class="flex gap-3 py-2">
                <div class="w-20 h-28 bg-slate-100 rounded flex-shrink-0 overflow-hidden border border-slate-200">
                  <img 
                    v-if="scope.row.coverImage" 
                    :src="scope.row.coverImage" 
                    class="w-full h-full object-cover" 
                  />
                  <div v-else class="w-full h-full flex items-center justify-center flex-col gap-2">
                    <el-icon class="text-2xl text-slate-400"><Document /></el-icon>
                    <span class="text-[10px] text-slate-400">无封面</span>
                  </div>
                </div>
                <div class="flex-1">
                  <div class="font-bold text-slate-800">{{ scope.row.title }}</div>
                  <div class="text-xs text-slate-500 mt-1 line-clamp-2" :title="scope.row.summary">
                    {{ scope.row.summary }}
                  </div>
                  <div class="flex gap-2 mt-2">
                    <el-tag size="small" effect="plain">{{ scope.row.country }}</el-tag>
                    <el-tag size="small" type="success" effect="plain">
                      {{ getIndustryLabel(scope.row.industry) }}
                    </el-tag>
                    <el-tag v-if="scope.row.isRecommended" size="small" type="danger" effect="dark">
                      推荐
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="报告类型" width="120">
            <template #default="scope">
              {{ getTypeLabel(scope.row.type) }}
            </template>
          </el-table-column>

          <el-table-column prop="year" label="年份" width="80" align="center" />

          <el-table-column prop="expireDate" label="截至日期" width="110">
            <template #default="scope">
              {{ scope.row.expireDate || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="下载量" width="90" align="center">
            <template #default="scope">
              <span class="text-blue-600 font-bold">{{ scope.row.downloads }}</span>
            </template>
          </el-table-column>

          <el-table-column label="访问权限" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getAccessLevelTag(scope.row.accessLevel)" size="small">
                {{ getAccessLevelLabel(scope.row.accessLevel) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="publishDate" label="发布时间" width="110" />

          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button link type="success" size="small" @click="handleDownload(scope.row)">
                <el-icon class="mr-1"><Download /></el-icon> 下载
              </el-button>
              <el-button link type="primary" size="small" @click="handleSetExpire(scope.row)">
                设置
              </el-button>
              <el-button link type="warning" size="small" @click="handleEdit(scope.row)">
                编辑
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
            :total="filteredReportsList.length"
            :page-size="10"
          />
        </div>
      </div>
    </div>

    <!-- 截止日期设置弹窗 -->
    <el-dialog
      v-model="expireDialogVisible"
      title="设置到期下架时间"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-date-picker
        v-model="expireDate"
        type="date"
        value-format="YYYY-MM-DD"
        placeholder="选择截止日期"
        class="w-full"
      />
      <template #footer>
        <el-button @click="expireDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveExpire">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { Plus, Upload, RefreshLeft, Download, Search, Document } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

interface ReportItem {
  id: number
  title: string
  country: string
  industry: string
  type: string
  year: string
  publishDate: string
  expireDate?: string
  summary: string
  coverImage?: string
  fileUrl?: string
  fileName?: string
  downloads: number
  feeLevel: string
  accessLevel: string
  isRecommended: boolean
  keywords?: string[]
  source?: string
  uploadDate: string
}

interface UploadFile {
  name: string
  url: string
}

const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const expireDialogVisible = ref(false)
const expireTargetId = ref<number | null>(null)
const expireDate = ref('')

const reportForm = ref({
  title: '',
  country: '',
  industry: '',
  type: '',
  year: '',
  publishDate: '',
  summary: '',
  coverImage: '',
  fileUrl: '',
  fileName: '',
  keywords: [] as string[],
  source: '',
  feeLevel: 'free',
  accessLevel: 'public',
  isRecommended: false
})

const coverFileList = ref<UploadFile[]>([])
const reportFileList = ref<UploadFile[]>([])

const filters = ref({
  keyword: '',
  country: '',
  industry: '',
  type: '',
  publishRange: [] as string[],
  feeLevel: ''
})

const reportsList = ref<ReportItem[]>([
  {
    id: 1,
    title: '2025年美国医疗器械市场研究报告',
    country: '美国',
    industry: 'medical',
    type: 'research',
    year: '2025',
    publishDate: '2026-01-20',
    expireDate: '',
    summary: '本报告全面分析了美国医疗器械市场的现状、趋势和机会，为中国企业进入美国市场提供参考。',
    coverImage: 'https://via.placeholder.com/400x560/3b82f6/ffffff?text=Medical+Report',
    fileUrl: '/files/medical-report-2025.pdf',
    fileName: '2025年美国医疗器械市场研究报告.pdf',
    downloads: 156,
    feeLevel: 'free',
    accessLevel: 'public',
    isRecommended: true,
    uploadDate: '2026-01-20',
    source: 'IMS Research',
    keywords: ['医疗器械', '美国市场', '市场分析']
  },
  {
    id: 2,
    title: '全球IT行业数字化转型报告',
    country: '全球',
    industry: 'it',
    type: 'industry',
    year: '2025',
    publishDate: '2026-01-18',
    expireDate: '',
    summary: '深度分析全球IT行业数字化转型趋势、关键技术和最佳实践。',
    coverImage: 'https://via.placeholder.com/400x560/10b981/ffffff?text=IT+Report',
    fileUrl: '/files/it-digital-transformation-2025.pdf',
    fileName: '全球IT行业数字化转型报告.pdf',
    downloads: 203,
    feeLevel: 'fee_1',
    accessLevel: 'vip',
    isRecommended: true,
    uploadDate: '2026-01-18',
    source: 'Gartner',
    keywords: ['数字化转型', 'IT行业', '全球趋势']
  }
])

const totalDownloads = computed(() => reportsList.value.reduce((sum, item) => sum + item.downloads, 0))

const filteredReportsList = computed(() => {
  let list = reportsList.value

  if (filters.value.keyword) {
    const k = filters.value.keyword.toLowerCase()
    list = list.filter(item => 
      item.title.toLowerCase().includes(k) ||
      item.summary.toLowerCase().includes(k)
    )
  }

  if (filters.value.country) list = list.filter(item => item.country === filters.value.country)
  if (filters.value.industry) list = list.filter(item => item.industry === filters.value.industry)
  if (filters.value.type) list = list.filter(item => item.type === filters.value.type)
  if (filters.value.feeLevel) list = list.filter(item => item.feeLevel === filters.value.feeLevel)

  if (filters.value.publishRange && filters.value.publishRange.length === 2) {
    const [start, end] = filters.value.publishRange
    list = list.filter(item => item.publishDate >= start && item.publishDate <= end)
  }

  return list
})

const getIndustryLabel = (val: string) => {
  const map: Record<string, string> = {
    medical: '医疗健康',
    it: '信息技术',
    manufacturing: '制造业',
    finance: '金融服务',
    consumer: '消费品',
    energy: '能源环保',
    agriculture: '农业',
    comprehensive: '综合'
  }
  return map[val] || val
}

const getTypeLabel = (val: string) => {
  const map: Record<string, string> = {
    data: '数据报告',
    research: '调研报告',
    guide: '指南',
    industry: '行业报告',
    evaluation: '评估报告',
    feasibility: '可行性报告'
  }
  return map[val] || val
}

const getAccessLevelTag = (level: string) => {
  return ({ public: 'success', vip: 'warning', svip: 'danger' } as Record<string, string>)[level] || 'info'
}

const getAccessLevelLabel = (level: string) => {
  return ({ public: '公开', vip: 'VIP', svip: 'SVIP' } as Record<string, string>)[level] || level
}

const getFeeLevelLabel = (level: string) => {
  return ({ free: '免费', fee_1: '报告收费1', fee_2: '报告收费2' } as Record<string, string>)[level] || level
}

const handleCoverChange = (file: any, fileList: any[]) => {
  coverFileList.value = fileList
  if (file.raw) {
    reportForm.value.coverImage = URL.createObjectURL(file.raw)
  }
}

const handleCoverRemove = () => {
  coverFileList.value = []
  reportForm.value.coverImage = ''
}

const handleFileChange = (file: any, fileList: any[]) => {
  reportFileList.value = fileList
  if (file.raw) {
    reportForm.value.fileUrl = URL.createObjectURL(file.raw)
    reportForm.value.fileName = file.name
  }
}

const handleFileRemove = () => {
  reportFileList.value = []
  reportForm.value.fileUrl = ''
  reportForm.value.fileName = ''
}

const handleUpload = () => {
  if (!reportForm.value.title || !reportForm.value.country || !reportForm.value.industry || !reportForm.value.publishDate) {
    return ElMessage.warning('请填写必填项（标题、国家、行业、发布时间）')
  }

  if (!reportForm.value.type) {
    return ElMessage.warning('请选择报告类型')
  }

  if (!reportForm.value.summary) {
    return ElMessage.warning('请填写报告摘要')
  }

  if (!reportForm.value.fileUrl) {
    return ElMessage.warning('请上传报告文件')
  }

  submitLoading.value = true

  setTimeout(() => {
    const payload: Partial<ReportItem> = {
      title: reportForm.value.title,
      country: reportForm.value.country,
      industry: reportForm.value.industry,
      type: reportForm.value.type,
      year: reportForm.value.year,
      publishDate: reportForm.value.publishDate,
      summary: reportForm.value.summary,
      coverImage: reportForm.value.coverImage,
      fileUrl: reportForm.value.fileUrl,
      fileName: reportForm.value.fileName,
      keywords: reportForm.value.keywords,
      source: reportForm.value.source,
      feeLevel: reportForm.value.feeLevel,
      accessLevel: reportForm.value.accessLevel,
      isRecommended: reportForm.value.isRecommended
    }

    if (isEdit.value && editId.value !== null) {
      const index = reportsList.value.findIndex(item => item.id === editId.value)
      if (index !== -1) {
        reportsList.value[index] = {
          ...reportsList.value[index],
          ...payload
        }
      }
      ElMessage.success('报告修改成功')
    } else {
      reportsList.value.unshift({
        id: Date.now(),
        downloads: 0,
        uploadDate: new Date().toISOString().split('T')[0],
        ...payload
      } as ReportItem)
      ElMessage.success('报告上传成功')
    }

    submitLoading.value = false
    handleReset()
  }, 600)
}

const handleReset = () => {
  reportForm.value = {
    title: '',
    country: '',
    industry: '',
    type: '',
    year: '',
    publishDate: '',
    summary: '',
    coverImage: '',
    fileUrl: '',
    fileName: '',
    keywords: [],
    source: '',
    feeLevel: 'free',
    accessLevel: 'public',
    isRecommended: false
  }
  coverFileList.value = []
  reportFileList.value = []
  isEdit.value = false
  editId.value = null
}

const handleSearch = () => {
  ElMessage.success(`查询完成，共找到 ${filteredReportsList.value.length} 条数据`)
}

const handleExportList = () => {
  const dataToExport = filteredReportsList.value

  if (!dataToExport || dataToExport.length === 0) {
    ElMessage.warning('当前列表没有数据，无法导出')
    return
  }

  const escapeCsv = (value: unknown): string => {
    if (value === null || value === undefined) return ''

    let str = String(value)
      .replace(/\u0000/g, '')
      .replace(/\r?\n/g, ' ')
      .trim()

    str = str.replace(/"/g, '""')
    return `"${str}"`
  }

  const headers = [
    '报告标题', '国家/地区', '行业分类', '报告类型', '年份',
    '发布时间', '截至日期', '下载量', '访问权限', '收费类型',
    '是否推荐', '来源', '关键词'
  ]

  const rows: string[] = []
  rows.push(headers.map(h => escapeCsv(h)).join(','))

  dataToExport.forEach(item => {
    const row = [
      item.title,
      item.country,
      getIndustryLabel(item.industry),
      getTypeLabel(item.type),
      item.year || '-',
      item.publishDate,
      item.expireDate || '-',
      item.downloads,
      getAccessLevelLabel(item.accessLevel),
      getFeeLevelLabel(item.feeLevel),
      item.isRecommended ? '是' : '否',
      item.source || '-',
      (item.keywords || []).join('; ')
    ]
    rows.push(row.map(val => escapeCsv(val)).join(','))
  })

  const csvContent = rows.join('\r\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const fileName = `行业报告清单_${new Date().toISOString().split('T')[0]}.csv`

  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()

  setTimeout(() => {
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  }, 100)

  ElMessage.success(`导出成功！文件大小：${(blob.size / 1024).toFixed(2)} KB`)
}

const handleDownload = (row: ReportItem) => {
  if (!row.fileUrl) {
    ElMessage.warning('该报告暂无文件可下载')
    return
  }

  const link = document.createElement('a')
  link.href = row.fileUrl
  link.download = row.fileName || row.title
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  const index = reportsList.value.findIndex(item => item.id === row.id)
  if (index !== -1) {
    reportsList.value[index].downloads++
  }
}

const handleEdit = (row: ReportItem) => {
  isEdit.value = true
  editId.value = row.id

  reportForm.value = {
    title: row.title,
    country: row.country,
    industry: row.industry,
    type: row.type,
    year: row.year,
    publishDate: row.publishDate,
    summary: row.summary,
    coverImage: row.coverImage || '',
    fileUrl: row.fileUrl || '',
    fileName: row.fileName || '',
    keywords: row.keywords ? [...row.keywords] : [],
    source: row.source || '',
    feeLevel: row.feeLevel,
    accessLevel: row.accessLevel,
    isRecommended: row.isRecommended
  }

  if (row.coverImage) {
    coverFileList.value = [{ name: 'cover.jpg', url: row.coverImage }]
  } else {
    coverFileList.value = []
  }

  if (row.fileUrl && row.fileName) {
    reportFileList.value = [{ name: row.fileName, url: row.fileUrl }]
  } else {
    reportFileList.value = []
  }

  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleDelete = (row: ReportItem) => {
  ElMessageBox.confirm(`确定删除报告 "${row.title}" 吗？此操作无法恢复。`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = reportsList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      reportsList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  }).catch(() => {})
}

const handleCancelEdit = () => {
  handleReset()
}

const handleSetExpire = (row: ReportItem) => {
  expireTargetId.value = row.id
  expireDate.value = row.expireDate || ''
  expireDialogVisible.value = true
}

const handleSaveExpire = () => {
  if (!expireTargetId.value) return

  const index = reportsList.value.findIndex(item => item.id === expireTargetId.value)
  if (index !== -1) {
    reportsList.value[index].expireDate = expireDate.value
    ElMessage.success('到期时间已更新')
  }

  expireDialogVisible.value = false
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
