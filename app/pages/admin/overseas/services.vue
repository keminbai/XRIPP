<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\overseas\services.vue
  版本: V1.0 (2026-01-29)
  
  ✅ 核心功能:
  1. [信息发布] 发布出海服务信息
  2. [服务管理] 管理各类出海服务内容
  3. [信息审核] 合伙人发布需审核
  4. [信息上架] 控制服务信息的展示状态
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 信息发布表单 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="flex items-start justify-between mb-6">
        <div>
          <h3 class="text-lg font-bold text-slate-800">
            {{ isEdit ? `编辑服务信息：${serviceForm.title || '未命名服务'}` : '发布出海服务信息' }}
          </h3>
          <p v-if="isEdit" class="text-xs text-amber-600 mt-1">
            正在编辑中，保存后将覆盖原记录
          </p>
        </div>
        <el-button v-if="isEdit" type="warning" plain @click="handleCancelEdit">
          取消编辑
        </el-button>
      </div>
      
      <el-form :model="serviceForm" label-width="120px" class="max-w-3xl">
        <el-form-item label="服务类型" required>
          <el-select v-model="serviceForm.type" placeholder="请选择服务类型" class="w-full">
            <el-option label="外贸服务" value="trade" />
            <el-option label="知识产权" value="ip" />
            <el-option label="跨境企服" value="cross" />
            <el-option label="出海投资" value="invest" />
            <el-option label="其他服务" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务项目" required>
          <el-select v-model="serviceForm.project" placeholder="请选择服务项目" class="w-full">
            <el-option
              v-for="item in projectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="主题描述" required>
          <el-input 
            v-model="serviceForm.title" 
            placeholder="请输入主题描述（限15字）"
            maxlength="15"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="目标国家/地区" required>
          <el-select 
            v-model="serviceForm.countries" 
            multiple 
            filterable
            allow-create
            placeholder="请选择或输入国家"
            class="w-full"
          >
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

        <el-form-item label="服务简介" required>
          <el-input 
            v-model="serviceForm.summary" 
            type="textarea" 
            :rows="2"
            placeholder="内容摘要（100字以内）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="服务详情" required>
          <el-input 
            v-model="serviceForm.content" 
            type="textarea" 
            :rows="8"
            placeholder="详细介绍服务内容、流程、优势等..."
            maxlength="2000"
            show-word-limit
          />
          <div class="text-xs text-slate-400 mt-1">
            💡 提示：未来可接入富文本编辑器
          </div>
        </el-form-item>

        <el-form-item label="宣传图上传" required>
          <el-upload
            action="/api/common/upload"
            list-type="picture-card"
            :limit="1"
            :file-list="serviceForm.promoImage"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">上传一张480x480宣传图</div>
        </el-form-item>

        <el-form-item label="服务介绍上传">
          <el-upload
            action="/api/common/upload"
            :limit="3"
            :file-list="serviceForm.introFiles"
            accept=".pdf,.jpg,.jpeg"
          >
            <el-button type="primary" plain>
              <el-icon class="mr-2"><Plus /></el-icon> 上传PDF/JPEG
            </el-button>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">支持PDF、JPEG格式，可上传业务方案/服务介绍</div>
        </el-form-item>

        <el-form-item label="服务价格">
          <el-radio-group v-model="serviceForm.priceType">
            <el-radio label="free">免费</el-radio>
            <el-radio label="fixed">固定价格</el-radio>
            <el-radio label="negotiable">价格面议</el-radio>
          </el-radio-group>
          <el-input-number 
            v-if="serviceForm.priceType === 'fixed'"
            v-model="serviceForm.price" 
            :min="0" 
            :step="100"
            class="ml-4"
          />
          <span v-if="serviceForm.priceType === 'fixed'" class="ml-2 text-sm text-slate-500">元</span>
        </el-form-item>

        <el-form-item label="服务周期">
          <el-input v-model="serviceForm.duration" placeholder="如：3-5个工作日" />
        </el-form-item>

        <el-form-item label="联系方式">
          <div class="grid grid-cols-2 gap-4">
            <el-input v-model="serviceForm.contactPerson" placeholder="联系人" />
            <el-input v-model="serviceForm.contactPhone" placeholder="联系电话" />
          </div>
          <el-input v-model="serviceForm.contactEmail" placeholder="联系邮箱" class="mt-2" />
        </el-form-item>

        <el-form-item label="服务图片">
          <el-upload
            action="/api/common/upload"
            list-type="picture-card"
            :limit="5"
            :file-list="serviceForm.images"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="text-xs text-slate-400 mt-1">最多上传5张，建议尺寸800x600px</div>
        </el-form-item>

        <el-form-item label="标签">
          <el-select 
            v-model="serviceForm.tags" 
            multiple 
            placeholder="选择服务标签"
            class="w-full"
          >
            <el-option label="专业认证" value="certified" />
            <el-option label="快速响应" value="fast" />
            <el-option label="一站式" value="onestop" />
            <el-option label="性价比高" value="affordable" />
            <el-option label="经验丰富" value="experienced" />
          </el-select>
        </el-form-item>

        <el-form-item label="发布状态">
          <el-radio-group v-model="serviceForm.status">
            <el-radio label="draft">保存草稿</el-radio>
            <el-radio label="published">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handlePublish" :loading="submitLoading">
            <el-icon class="mr-2"><Check /></el-icon>
            {{ isEdit ? '保存修改' : (serviceForm.status === 'draft' ? '保存草稿' : '发布服务') }}
          </el-button>
          <el-button @click="handleReset">
            <el-icon class="mr-2"><RefreshLeft /></el-icon> {{ isEdit ? '重置编辑' : '重置' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 服务信息列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">出海服务列表</h3>
            <p class="text-xs text-slate-500 mt-1">管理已发布的出海服务信息</p>
          </div>
        </div>

        <!-- Tab切换 -->
        <el-tabs v-model="activeTab" class="mb-4">
          <el-tab-pane name="published">
            <template #label>
              <span class="flex items-center gap-2">
                已发布 <el-tag size="small" type="success" effect="plain">{{ publishedList.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="draft">
            <template #label>
              <span class="flex items-center gap-2">
                草稿箱 <el-tag size="small" type="info" effect="plain">{{ draftList.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="offline">
            <template #label>
              <span class="flex items-center gap-2">
                已下架 <el-tag size="small" type="warning" effect="plain">{{ offlineList.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- 筛选条件 -->
        <div class="flex gap-3 flex-wrap">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索服务标题..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.country" placeholder="服务国家" class="w-32" clearable filterable>
            <el-option label="美国" value="美国" />
            <el-option label="英国" value="英国" />
            <el-option label="德国" value="德国" />
            <el-option label="日本" value="日本" />
            <el-option label="新加坡" value="新加坡" />
          </el-select>
          <el-select v-model="filters.type" placeholder="服务类型" class="w-32" clearable>
            <el-option label="外贸服务" value="trade" />
            <el-option label="知识产权" value="ip" />
            <el-option label="跨境企服" value="cross" />
            <el-option label="出海投资" value="invest" />
            <el-option label="其他服务" value="other" />
          </el-select>
          <el-select v-model="filters.project" placeholder="服务项目" class="w-40" clearable>
            <el-option
              v-for="item in filterProjectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>
      </div>

      <div class="p-6">
        <el-table :data="filteredServiceList" stripe :header-cell-style="{background:'#f8fafc', color:'#64748b'}">
          <el-table-column label="服务信息" min-width="350">
            <template #default="scope">
              <div class="py-2">
                <div class="font-bold text-slate-800 mb-1">{{ scope.row.title }}</div>
                <div class="text-xs text-slate-500 line-clamp-2">{{ scope.row.summary }}</div>
                <div class="flex gap-2 mt-2 flex-wrap">
                  <el-tag :type="getTypeTag(scope.row.type)" size="small">
                    {{ getTypeLabel(scope.row.type) }}
                  </el-tag>
                  <el-tag size="small" type="warning">
                    {{ getProjectLabel(scope.row.project) }}
                  </el-tag>
                  <el-tag 
                    v-for="country in scope.row.countries.slice(0, 2)" 
                    :key="country" 
                    size="small" 
                    type="info"
                  >
                    {{ country }}
                  </el-tag>
                  <el-tag v-if="scope.row.countries.length > 2" size="small" type="info">
                    +{{ scope.row.countries.length - 2 }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="服务类型" width="120">
            <template #default="scope">
              <el-tag :type="getTypeTag(scope.row.type)" size="small">
                {{ getTypeLabel(scope.row.type) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="服务项目" width="140">
            <template #default="scope">
              <span class="text-slate-700">{{ getProjectLabel(scope.row.project) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="价格" width="120">
            <template #default="scope">
              <span v-if="scope.row.priceType === 'free'" class="text-green-600 font-bold">免费</span>
              <span v-else-if="scope.row.priceType === 'fixed'" class="text-blue-600 font-bold">
                ¥{{ scope.row.price }}
              </span>
              <span v-else class="text-orange-600 font-medium">面议</span>
            </template>
          </el-table-column>

          <el-table-column label="浏览量" width="100" align="center">
            <template #default="scope">
              <span class="text-blue-600 font-bold">{{ scope.row.views }}</span>
            </template>
          </el-table-column>

          <el-table-column label="咨询数" width="100" align="center">
            <template #default="scope">
              <span class="text-green-600 font-bold">{{ scope.row.inquiries }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="publishDate" label="发布时间" width="120" />

          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="handleView(scope.row)">
                查看
              </el-button>
              <el-button link type="warning" size="small" @click="handleEdit(scope.row)">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="handleOffline(scope.row)">
                下架
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
            :total="filteredServiceList.length"
            :page-size="10"
          />
        </div>
      </div>
    </div>

    <!-- 服务详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="服务详情"
      width="760px"
      :close-on-click-modal="false"
    >
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">服务类型</div>
            <div class="detail-value">{{ getTypeLabel(detailRow.type) }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">服务项目</div>
            <div class="detail-value">{{ getProjectLabel(detailRow.project) }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">主题描述</div>
            <div class="detail-value">{{ detailRow.title || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">服务国家</div>
            <div class="detail-value">{{ (detailRow.countries || []).join(' / ') || '-' }}</div>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">内容摘要</div>
          <div class="detail-value">{{ detailRow.summary || '-' }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">服务详情</div>
          <div class="detail-value whitespace-pre-wrap">{{ detailRow.content || '-' }}</div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">价格类型</div>
            <div class="detail-value">
              <span v-if="detailRow.priceType === 'free'">免费</span>
              <span v-else-if="detailRow.priceType === 'fixed'">固定价格 ¥{{ detailRow.price }}</span>
              <span v-else>价格面议</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">服务周期</div>
            <div class="detail-value">{{ detailRow.duration || '-' }}</div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="detail-item">
            <div class="detail-label">联系人</div>
            <div class="detail-value">{{ detailRow.contactPerson || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">联系电话</div>
            <div class="detail-value">{{ detailRow.contactPhone || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">联系邮箱</div>
            <div class="detail-value">{{ detailRow.contactEmail || '-' }}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">发布状态</div>
            <div class="detail-value">{{ detailRow.status === 'draft' ? '草稿' : detailRow.status === 'offline' ? '已下架' : '已发布' }}</div>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">宣传图</div>
          <div class="detail-value">
            <div v-if="detailRow.promoImage && detailRow.promoImage.length" class="flex flex-wrap gap-2">
              <el-image
                v-for="(img, idx) in detailRow.promoImage"
                :key="idx"
                :src="img.url || img"
                :preview-src-list="detailRow.promoImage.map(i => i.url || i)"
                fit="cover"
                class="detail-image"
              />
            </div>
            <span v-else>-</span>
          </div>
        </div>

        <div class="detail-item">
          <div class="detail-label">服务图片</div>
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
import { Plus, Check, RefreshLeft, Search } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

const submitLoading = ref(false)
const activeTab = ref('published')
const isEdit = ref(false)
const editId = ref<number | null>(null)
const detailVisible = ref(false)
const detailRow = ref<any>({})

const serviceForm = ref({
  type: '',
  project: '',
  title: '',
  countries: [] as string[],
  summary: '',
  content: '',
  priceType: 'negotiable',
  price: 0,
  duration: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  images: [] as any[],
  promoImage: [] as any[],
  introFiles: [] as any[],
  tags: [] as string[],
  status: 'published'
})

const filters = ref({
  keyword: '',
  country: '',
  type: '',
  project: ''
})

const allServiceList = ref([
  {
    id: 1,
    type: 'trade',
    project: 'customs',
    title: '美国清关与报关服务',
    countries: ['美国'],
    summary: '提供美国清关、报关与文件处理服务，确保货物合规快速通关',
    content: '详细服务内容...',
    priceType: 'fixed',
    price: 15000,
    duration: '15-20个工作日',
    contactPerson: 'John Smith',
    contactPhone: '+1-212-736-3100',
    contactEmail: 'market@xripp.com',
    promoImage: [],
    introFiles: [],
    views: 256,
    inquiries: 32,
    status: 'published',
    publishDate: '2026-01-20'
  },
  {
    id: 2,
    type: 'ip',
    project: 'legal_service',
    title: '欧盟法律合规咨询',
    countries: ['德国', '法国', '英国'],
    summary: '提供欧盟法律法规咨询，协助企业合规经营',
    content: '详细服务内容...',
    priceType: 'negotiable',
    price: 0,
    duration: '按项目确定',
    contactPerson: 'David Brown',
    contactPhone: '+44-20-7234-5678',
    contactEmail: 'legal@xripp.com',
    promoImage: [],
    introFiles: [],
    views: 189,
    inquiries: 28,
    status: 'published',
    publishDate: '2026-01-18'
  }
])

const publishedList = computed(() => allServiceList.value.filter(item => item.status === 'published'))
const draftList = computed(() => allServiceList.value.filter(item => item.status === 'draft'))
const offlineList = computed(() => allServiceList.value.filter(item => item.status === 'offline'))

const currentTabList = computed(() => {
  if (activeTab.value === 'draft') return draftList.value
  if (activeTab.value === 'offline') return offlineList.value
  return publishedList.value
})

const filteredServiceList = computed(() => {
  let list = currentTabList.value
  
  if (filters.value.keyword) {
    list = list.filter(item => 
      item.title.includes(filters.value.keyword) ||
      item.summary.includes(filters.value.keyword)
    )
  }
  
  if (filters.value.country) {
    list = list.filter(item => item.countries.includes(filters.value.country))
  }

  if (filters.value.type) {
    list = list.filter(item => item.type === filters.value.type)
  }

  if (filters.value.project) {
    list = list.filter(item => item.project === filters.value.project)
  }
  
  return list
})

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'trade': 'primary',
    'ip': 'success',
    'cross': 'warning',
    'invest': 'danger',
    'other': 'info'
  }
  return map[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'trade': '外贸服务',
    'ip': '知识产权',
    'cross': '跨境企服',
    'invest': '出海投资',
    'other': '其他服务'
  }
  return map[type] || type
}

const projectMap: Record<string, { label: string; type: string }> = {
  // 外贸服务
  'customs': { label: '报关清关', type: 'trade' },
  'shipping': { label: '物流海运', type: 'trade' },
  'overseas_warehouse': { label: '海外仓', type: 'trade' },
  'cargo_insurance': { label: '货运保险', type: 'trade' },
  'cross_border_payment': { label: '跨境支付与结算', type: 'trade' },
  // 知识产权
  'legal_service': { label: '法律服务', type: 'ip' },
  'trademark': { label: '商标注册', type: 'ip' },
  'certification': { label: '认证', type: 'ip' },
  'audit': { label: '财务审计', type: 'ip' },
  'tax_accounting': { label: '记账报税', type: 'ip' },
  'due_diligence': { label: '尽职调查', type: 'ip' },
  'tax_service': { label: '税务服务', type: 'ip' },
  'compliance': { label: '跨境合规', type: 'ip' },
  // 跨境企服
  'company_setup': { label: '公司设立', type: 'cross' },
  'company_dissolution': { label: '公司注销', type: 'cross' },
  'visa': { label: '签证', type: 'cross' },
  // 出海投资
  'project_research': { label: '项目调研', type: 'invest' },
  'industry_report': { label: '行业分析报告', type: 'invest' },
  'factory_build': { label: '投资建厂', type: 'invest' },
  'landing_consulting': { label: '落地咨询', type: 'invest' },
  // 其他服务
  'other_service': { label: '其他服务', type: 'other' }
}

const projectOptions = computed(() => {
  if (!serviceForm.value.type) {
    return Object.entries(projectMap).map(([value, item]) => ({ value, label: item.label }))
  }
  return Object.entries(projectMap)
    .filter(([, item]) => item.type === serviceForm.value.type)
    .map(([value, item]) => ({ value, label: item.label }))
})

const filterProjectOptions = computed(() => {
  if (!filters.value.type) {
    return Object.entries(projectMap).map(([value, item]) => ({ value, label: item.label }))
  }
  return Object.entries(projectMap)
    .filter(([, item]) => item.type === filters.value.type)
    .map(([value, item]) => ({ value, label: item.label }))
})

const getProjectLabel = (project: string) => {
  return projectMap[project]?.label || project || '未选择'
}

const handlePublish = () => {
  if (
    !serviceForm.value.type ||
    !serviceForm.value.project ||
    !serviceForm.value.title ||
    !serviceForm.value.countries.length
  ) {
    return ElMessage.warning('请填写必填项')
  }

  if (!serviceForm.value.promoImage || !serviceForm.value.promoImage.length) {
    return ElMessage.warning('请上传宣传图')
  }

  if (serviceForm.value.priceType === 'fixed' && (!serviceForm.value.price || serviceForm.value.price <= 0)) {
    return ElMessage.warning('固定价格需填写有效金额')
  }

  if (serviceForm.value.introFiles && serviceForm.value.introFiles.length) {
    const allowed = ['.pdf', '.jpg', '.jpeg']
    const invalid = serviceForm.value.introFiles.find((file: any) => {
      const name = (file?.name || file?.url || '').toLowerCase()
      return name && !allowed.some(ext => name.endsWith(ext))
    })
    if (invalid) {
      return ElMessage.warning('服务介绍仅支持PDF/JPEG格式')
    }
  }
  
  submitLoading.value = true
  setTimeout(() => {
    const payload = {
      ...serviceForm.value,
      promoImage: serviceForm.value.promoImage || [],
      introFiles: serviceForm.value.introFiles || []
    }

    if (isEdit.value && editId.value !== null) {
      const index = allServiceList.value.findIndex(item => item.id === editId.value)
      if (index !== -1) {
        allServiceList.value[index] = {
          ...allServiceList.value[index],
          ...payload
        }
      }
    } else {
      allServiceList.value.unshift({
        id: Date.now(),
        ...payload,
        views: 0,
        inquiries: 0,
        publishDate: new Date().toISOString().split('T')[0]
      })
    }
    submitLoading.value = false
    ElMessage.success(isEdit.value ? '保存修改成功' : (serviceForm.value.status === 'draft' ? '保存成功' : '发布成功'))
    handleReset()
  }, 1000)
}

const handleReset = () => {
  serviceForm.value = {
    type: '',
    project: '',
    title: '',
    countries: [],
    summary: '',
    content: '',
    priceType: 'negotiable',
    price: 0,
    duration: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    images: [],
    promoImage: [],
    introFiles: [],
    tags: [],
    status: 'published'
  }
  isEdit.value = false
  editId.value = null
}

const handleCancelEdit = () => {
  handleReset()
}

const handleSearch = () => {
  ElMessage.success('查询完成')
}

const handleView = (row: any) => {
  detailRow.value = { ...row }
  detailVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  editId.value = row.id
  serviceForm.value = {
    type: row.type || '',
    project: row.project || '',
    title: row.title || '',
    countries: row.countries ? [...row.countries] : [],
    summary: row.summary || '',
    content: row.content || '',
    priceType: row.priceType || 'negotiable',
    price: row.price || 0,
    duration: row.duration || '',
    contactPerson: row.contactPerson || '',
    contactPhone: row.contactPhone || '',
    contactEmail: row.contactEmail || '',
    images: row.images ? [...row.images] : [],
    promoImage: row.promoImage ? [...row.promoImage] : [],
    introFiles: row.introFiles ? [...row.introFiles] : [],
    tags: row.tags ? [...row.tags] : [],
    status: row.status || 'published'
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleOffline = (row: any) => {
  ElMessageBox.confirm(`确定下架服务 "${row.title}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    const index = allServiceList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      allServiceList.value[index] = {
        ...allServiceList.value[index],
        status: 'offline'
      }
    }
    ElMessage.success('已下架')
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除服务 "${row.title}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    const index = allServiceList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      allServiceList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  })
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
