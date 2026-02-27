<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\partners\resources.vue
  版本: V1.2 (2026-02-04)
  
  ✅ 修复与优化记录:
  1. [性能] 实现真实前端分页 (pagedResourcesList)，避免大量数据渲染卡顿
  2. [语法] 修复 el-input 图标属性警告 (:prefix-icon)
  3. [逻辑] 修复 el-upload 文件列表双向绑定，编辑时可回显文件
  4. [交互] 切换 Tab 或搜索时自动重置页码
  5. [需求] 包含 Row 35 要求的"适用等级/国家/城市"筛选及发布人展示
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 资源上传表单 (可折叠) -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="mb-6 cursor-pointer flex justify-between items-center" @click="toggleUpload">
        <div>
          <h3 class="text-lg font-bold text-slate-800">上传资源</h3>
          <p class="text-xs text-slate-500 mt-1">为合伙人提供培训资料、营销素材等资源</p>
        </div>
        <el-icon :class="showUpload ? 'rotate-180' : ''" class="transition-transform"><ArrowDown /></el-icon>
      </div>

      <el-collapse-transition>
        <div v-show="showUpload">
          <el-form :model="form" label-width="120px" class="max-w-3xl">
            <el-form-item label="资源分类" required>
              <el-select v-model="form.category" class="w-full" placeholder="请选择资源分类">
                <el-option label="培训资料" value="training" />
                <el-option label="营销素材" value="marketing" />
                <el-option label="文档模板" value="template" />
                <el-option label="操作手册" value="manual" />
                <el-option label="政策文件" value="policy" />
              </el-select>
            </el-form-item>

            <el-form-item label="资源标题" required>
              <el-input 
                v-model="form.title" 
                placeholder="请输入资源标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="资源描述" required>
              <el-input 
                v-model="form.description" 
                type="textarea" 
                :rows="3"
                placeholder="请输入资源描述..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <!-- 修复：v-model:file-list 双向绑定 -->
            <el-form-item label="资源文件" required>
              <el-upload
                action="/api/common/upload"
                :limit="5"
                v-model:file-list="form.files"
                accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.zip,.rar,.jpg,.png"
              >
                <el-button type="primary">
                  <el-icon class="mr-2"><Upload /></el-icon> 上传文件
                </el-button>
              </el-upload>
              <div class="text-xs text-slate-400 mt-1">
                支持PDF、Office文档、图片、压缩包等格式
              </div>
            </el-form-item>

            <!-- 修复：v-model:file-list 双向绑定 -->
            <el-form-item label="封面图片">
              <el-upload
                action="/api/common/upload"
                list-type="picture-card"
                :limit="1"
                v-model:file-list="form.coverImage"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>

            <el-form-item label="适用地区">
              <div class="flex gap-2 w-full">
                 <el-select v-model="form.targetCountry" placeholder="国家" class="w-1/2">
                   <el-option label="所有国家" value="all" />
                   <el-option label="中国" value="China" />
                   <el-option label="马来西亚" value="Malaysia" />
                 </el-select>
                 <el-select v-model="form.targetCity" placeholder="城市" class="w-1/2">
                   <el-option label="所有城市" value="all" />
                   <el-option label="上海" value="上海" />
                   <el-option label="北京" value="北京" />
                 </el-select>
              </div>
            </el-form-item>

            <el-form-item label="标签">
              <el-select 
                v-model="form.tags" 
                multiple 
                allow-create
                filterable
                placeholder="输入标签后按回车添加"
                class="w-full"
              >
                <el-option label="新手必读" value="beginner" />
                <el-option label="重要" value="important" />
              </el-select>
            </el-form-item>

            <el-form-item label="是否置顶">
              <el-switch v-model="form.isTop" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpload" :loading="uploading">
                <el-icon class="mr-2"><Upload /></el-icon> {{ isEditMode ? '保存修改' : '立即发布' }}
              </el-button>
              <el-button @click="handleReset" v-if="!isEditMode">重置</el-button>
              <el-button @click="cancelEdit" v-else>取消编辑</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>
    </div>

    <!-- 资源库列表 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">资源库列表</h3>
            <p class="text-xs text-slate-500 mt-1">
              共 <span class="text-blue-600 font-bold">{{ filteredResourcesList.length }}</span> 个资源
            </p>
          </div>
        </div>

        <!-- 修复：Tab 切换重置分页 -->
        <el-tabs v-model="activeTab" class="mb-4" @tab-change="handleTabChange">
          <el-tab-pane label="全部资源" name="all" />
          <el-tab-pane label="培训资料" name="training" />
          <el-tab-pane label="营销素材" name="marketing" />
          <el-tab-pane label="文档模板" name="template" />
          <el-tab-pane label="操作手册" name="manual" />
        </el-tabs>

        <!-- 筛选栏 (Row 35: 等级/国家/城市) -->
        <div class="flex gap-3 flex-wrap">
          <!-- 修复：:prefix-icon -->
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索资源标题..." 
            :prefix-icon="Search" 
            class="w-48" 
            clearable 
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          
          <el-select v-model="filters.level" placeholder="适用等级" class="w-32" clearable @change="handleSearch">
             <el-option label="所有等级" value="" />
             <el-option label="五星合伙人" value="5" />
             <el-option label="四星合伙人" value="4" />
          </el-select>

          <el-select v-model="filters.country" placeholder="国家" class="w-32" clearable @change="handleSearch">
             <el-option label="中国" value="China" />
             <el-option label="马来西亚" value="Malaysia" />
          </el-select>

          <el-select v-model="filters.city" placeholder="城市" class="w-32" clearable @change="handleSearch">
             <el-option label="上海" value="上海" />
             <el-option label="北京" value="北京" />
          </el-select>

          <el-button type="primary" plain @click="handleSearch">查询</el-button>
          <el-button plain @click="handleResetFilter">重置</el-button>
        </div>
      </div>

      <div class="p-6">
        <!-- 修复：渲染分页后的数据 pagedResourcesList -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div 
            v-for="item in pagedResourcesList" 
            :key="item.id"
            class="border border-slate-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow group"
          >
            <!-- 封面 -->
            <div class="h-40 bg-slate-50 flex items-center justify-center relative border-b border-slate-100">
              <img v-if="item.cover" :src="item.cover" class="w-full h-full object-cover" />
              <el-icon v-else class="text-5xl text-blue-300 group-hover:scale-110 transition-transform"><Document /></el-icon>
              <el-tag v-if="item.isTop" size="small" type="danger" class="absolute top-2 right-2">置顶</el-tag>
            </div>

            <!-- 内容 -->
            <div class="p-4">
              <div class="font-bold text-slate-800 mb-2 truncate" :title="item.title">{{ item.title }}</div>
              <div class="text-xs text-slate-500 mb-3 line-clamp-2 h-8" :title="item.description">{{ item.description }}</div>
              
              <div class="flex gap-2 mb-3 flex-wrap">
                <el-tag size="small" :type="getCategoryTag(item.category)">{{ getCategoryLabel(item.category) }}</el-tag>
                <el-tag size="small" type="info" effect="plain">{{ item.targetCountry === 'all' ? '全球' : item.targetCountry }}</el-tag>
              </div>

              <!-- Row 35: 显示发布人与联系方式 -->
              <div class="bg-slate-50 p-2 rounded text-xs text-slate-500 mb-3 flex justify-between items-center">
                <div class="flex items-center gap-1">
                   <el-icon><User /></el-icon> {{ item.uploader }}
                </div>
                <div>{{ item.uploadDate }}</div>
              </div>

              <div class="flex gap-2">
                <el-button type="primary" size="small" class="flex-1" @click="handleDownload(item)">
                  <el-icon class="mr-1"><Download /></el-icon> 下载 ({{ item.downloads }})
                </el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" :icon="MoreFilled" />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleEdit(item)">
                        <el-icon><Edit /></el-icon> 编辑
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleDelete(item)" class="text-red-500">
                        <el-icon><Delete /></el-icon> 删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredResourcesList.length === 0" class="text-center py-12">
          <el-icon class="text-6xl text-slate-300 mb-4"><FolderOpened /></el-icon>
          <div class="text-slate-400">暂无符合条件的资源</div>
        </div>

        <!-- 修复：真实分页组件 -->
        <div class="mt-6 flex justify-center" v-if="filteredResourcesList.length > 0">
          <el-pagination 
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            background 
            layout="total, prev, pager, next, sizes" 
            :total="filteredResourcesList.length"
            :page-sizes="[6, 12, 24]"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Upload, Plus, Search, Download, Document, User, ArrowDown, MoreFilled, Edit, Delete, FolderOpened } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

definePageMeta({ layout: 'admin' })

// ----------------------
// 状态管理
// ----------------------
const showUpload = ref(true)
const uploading = ref(false)
const activeTab = ref('all')
const isEditMode = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)

const toggleUpload = () => showUpload.value = !showUpload.value

// 表单数据
const form = ref({
  id: 0,
  category: '', 
  title: '', 
  description: '', 
  targetCountry: 'all', 
  targetCity: 'all', 
  files: [] as any[], // 真实绑定
  coverImage: [] as any[], // 真实绑定
  tags: [] as string[],
  accessLevel: 'all',
  isTop: false
})

// 筛选条件
const filters = ref({
  keyword: '', level: '', country: '', city: ''
})

// ----------------------
// 数据源 (Mock)
// ----------------------
const resourcesList = ref([
  {
    id: 1, category: 'training', title: '合伙人新手培训教程', description: '详细介绍平台操作流程、业务规则...',
    uploader: 'Admin', contact: '13800138000', targetCountry: 'all', targetCity: 'all', minLevel: 0,
    cover: '', files: [{ name: '培训教程.pdf' }], 
    tags: ['新手', '必读'], accessLevel: 'all',
    isTop: true, downloads: 156, uploadDate: '2026-01-20'
  },
  {
    id: 2, category: 'marketing', title: '上海区线下沙龙物料', description: '易拉宝、横幅设计源文件...',
    uploader: '张经理(上海)', contact: '13900001111', targetCountry: 'China', targetCity: '上海', minLevel: 0,
    cover: '', files: [{ name: '物料包.zip' }],
    tags: ['物料'], accessLevel: 'all',
    isTop: false, downloads: 45, uploadDate: '2026-01-28'
  },
  {
    id: 3, category: 'policy', title: '2026年度五星合伙人激励政策', description: '针对高级合伙人的额外返佣说明',
    uploader: '运营部', contact: '021-12345678', targetCountry: 'China', targetCity: 'all', minLevel: 5,
    cover: '', files: [{ name: '激励政策.docx' }],
    tags: ['政策', '高级'], accessLevel: 'senior',
    isTop: true, downloads: 88, uploadDate: '2026-01-15'
  }
])

// ----------------------
// 核心逻辑
// ----------------------

// 过滤逻辑
const filteredResourcesList = computed(() => {
  return resourcesList.value.filter(item => {
    // 类别筛选
    if (activeTab.value !== 'all' && item.category !== activeTab.value) return false
    
    // 关键字筛选
    if (filters.value.keyword) {
      const kw = filters.value.keyword.toLowerCase()
      if (!item.title.toLowerCase().includes(kw) && !item.description.toLowerCase().includes(kw)) return false
    }

    // 高级筛选 (Row 35)
    if (filters.value.country && item.targetCountry !== 'all' && item.targetCountry !== filters.value.country) return false
    if (filters.value.city && item.targetCity !== 'all' && item.targetCity !== filters.value.city) return false
    if (filters.value.level && item.minLevel > Number(filters.value.level)) return false

    return true
  })
})

// 分页切片 (修复)
const pagedResourcesList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredResourcesList.value.slice(start, start + pageSize.value)
})

// 样式映射
const getCategoryTag = (c: string) => ({ training: 'primary', marketing: 'success', policy: 'warning', template: 'info', manual: 'danger' }[c] || 'info')
const getCategoryLabel = (c: string) => ({ training: '培训', marketing: '营销', policy: '政策', template: '模板', manual: '手册' }[c] || '其他')

// ----------------------
// 交互处理
// ----------------------

// 搜索/筛选
const handleSearch = () => {
  currentPage.value = 1
  ElMessage.success('查询完成')
}

const handleResetFilter = () => {
  filters.value = { keyword: '', level: '', country: '', city: '' }
  currentPage.value = 1
  ElMessage.info('筛选已重置')
}

const handleTabChange = () => {
  currentPage.value = 1
  filters.value.keyword = '' // 可选：切换Tab时是否清空搜索
}

const handlePageChange = () => {
  window.scrollTo({ top: 300, behavior: 'smooth' })
}

const handleSizeChange = () => {
  currentPage.value = 1
}

// 上传/保存
const handleUpload = () => {
  if (!form.value.category || !form.value.title || !form.value.description) {
    return ElMessage.warning('请填写必填项')
  }
  
  uploading.value = true
  setTimeout(() => {
    if (isEditMode.value) {
      // 编辑逻辑
      const index = resourcesList.value.findIndex(r => r.id === form.value.id)
      if (index !== -1) {
        // 修复：保留原有的下载量和日期，合并新数据
        resourcesList.value[index] = { 
          ...resourcesList.value[index],
          ...form.value,
          cover: form.value.coverImage[0]?.url || '',
          files: form.value.files
        }
        ElMessage.success('资源更新成功')
      }
    } else {
      // 新增逻辑
      resourcesList.value.unshift({
        id: Date.now(),
        ...form.value,
        uploader: 'Admin',
        contact: '13800000000',
        minLevel: 0,
        cover: form.value.coverImage[0]?.url || '',
        files: form.value.files,
        downloads: 0,
        uploadDate: new Date().toISOString().split('T')[0]
      })
      ElMessage.success('资源发布成功')
    }
    
    uploading.value = false
    handleReset()
  }, 1000)
}

// 重置表单
const handleReset = () => {
  isEditMode.value = false
  form.value = {
    id: 0, category: '', title: '', description: '', 
    targetCountry: 'all', targetCity: 'all', 
    files: [], coverImage: [], tags: [], accessLevel: 'all', isTop: false
  }
}

const cancelEdit = () => {
  handleReset()
  ElMessage.info('已取消编辑')
}

// 编辑回显 (修复)
const handleEdit = (item: any) => {
  showUpload.value = true
  isEditMode.value = true
  
  // 深拷贝防止直接修改列表数据
  form.value = JSON.parse(JSON.stringify({
    ...item,
    files: item.files || [], // 确保有值
    coverImage: item.cover ? [{ url: item.cover }] : []
  }))
  
  window.scrollTo({ top: 0, behavior: 'smooth' })
  ElMessage.info('已进入编辑模式')
}

const handleDelete = (item: any) => {
  ElMessageBox.confirm(`确定删除资源 "${item.title}" 吗？`, '警告', { type: 'warning' })
    .then(() => {
      resourcesList.value = resourcesList.value.filter(r => r.id !== item.id)
      // 如果删除后当前页为空，且不是第一页，则前移一页
      if (pagedResourcesList.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
      }
      ElMessage.success('删除成功')
    })
}

const handleDownload = (item: any) => {
  item.downloads++
  ElMessage.success(`开始下载: ${item.title}`)
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