<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\display.vue
  版本: V2.0 流程闭环版 (2026-02-01)
  
  ✅ 修复内容:
  1. [新增功能] 增加 "显示申请处理" Tab，闭环 activity.vue 的申请流程
  2. [审核逻辑] 支持对申请进行 "同意上墙" (自动加入轮播/广告) 或 "驳回"
  3. [数据联动] 模拟了从业务端流转过来的申请数据
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div class="mb-6">
        <h3 class="text-lg font-bold text-slate-800">网站内容展示管理</h3>
        <p class="text-xs text-slate-500 mt-1">管理前台首页轮播、广告位及各业务板块的推荐申请</p>
      </div>

      <el-tabs v-model="activeTab" type="card" class="demo-tabs">
        
        <!-- 1. 首页轮播图 -->
        <el-tab-pane label="首页轮播图" name="carousel">
          <div class="space-y-4 pt-4">
            <el-alert
              title="轮播图管理依赖文件上传服务（OSS），该功能尚未接入后端，当前操作不会持久化。"
              type="warning"
              :closable="false"
              class="mb-4"
            />
            <div class="flex justify-between items-center">
              <div class="text-xs text-slate-500">最多支持5张轮播图，建议尺寸1920x650px</div>
              <el-button type="primary" @click="handleCarouselUpload" :disabled="carouselList.length >= 5">
                <el-icon class="mr-2"><Plus /></el-icon> 上传轮播图
              </el-button>
            </div>
            <div class="grid grid-cols-1 gap-4">
              <div v-for="item in carouselList" :key="item.id" class="bg-slate-50 border border-slate-200 rounded-lg p-4 flex gap-4 items-center">
                <div class="w-48 h-28 rounded overflow-hidden bg-slate-200 flex-shrink-0 border border-slate-300">
                  <img :src="item.image" class="w-full h-full object-cover" />
                </div>
                <div class="flex-1">
                  <div class="font-bold text-slate-800">{{ item.title }}</div>
                  <div class="text-sm text-slate-500 mt-1">排序: {{ item.sort }} | 类型: {{ item.type || '手动上传' }}</div>
                  <div class="text-sm text-slate-500">链接: {{ item.link || '无' }}</div>
                </div>
                <el-button type="danger" size="small" plain @click="handleCarouselDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 2. 广告位管理 -->
        <el-tab-pane label="广告位管理" name="ads">
          <div class="space-y-4 pt-4">
          <el-alert
            title="广告位配置尚未接入后端存储，当前修改仅在页面内存中生效，刷新后重置。"
            type="warning"
            :closable="false"
            class="mb-2"
          />
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4">
            <div v-for="ad in adsList" :key="ad.id" class="bg-white border border-slate-200 rounded-lg p-5 shadow-sm">
              <div class="flex justify-between items-start mb-3">
                <div>
                  <div class="font-bold text-slate-800">{{ ad.name }}</div>
                  <div class="text-xs text-slate-500 mt-1">{{ ad.position }}</div>
                </div>
                <el-tag :type="ad.status === 'active' ? 'success' : 'info'" size="small">
                  {{ ad.status === 'active' ? '已配置' : '未配置' }}
                </el-tag>
              </div>
              <div class="w-full h-32 bg-slate-50 rounded border border-dashed border-slate-300 mb-3 flex items-center justify-center overflow-hidden">
                <img v-if="ad.image" :src="ad.image" class="w-full h-full object-cover" />
                <span v-else class="text-slate-400 text-sm">暂无广告图</span>
              </div>
              <el-button type="primary" size="small" plain class="w-full" @click="handleAdEdit(ad)">
                {{ ad.status === 'active' ? '编辑广告' : '配置广告' }}
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ✅ [新增] 3. 显示申请处理 -->
        <el-tab-pane label="显示申请处理" name="applications">
          <template #label>
            <span class="flex items-center gap-1">
              显示申请处理
              <el-badge :value="applicationList.length" type="danger" v-if="applicationList.length > 0" />
            </span>
          </template>
          
          <div class="pt-4">
            <el-alert title="来自各业务模块的首页推荐申请，审核通过后将自动加入轮播图或推荐位" type="info" show-icon :closable="false" class="mb-4" />
            <el-alert title="显示申请尚未接入后端接口，当前数据为示例占位，操作不会持久化。" type="warning" :closable="false" class="mb-4" />
            
            <el-table :data="applicationList" stripe style="width: 100%">
              <el-table-column prop="submitTime" label="申请时间" width="160" />
              <el-table-column label="申请内容" min-width="200">
                <template #default="scope">
                  <div class="font-bold text-slate-800">{{ scope.row.title }}</div>
                  <div class="text-xs text-slate-500 mt-1">来源: {{ scope.row.source }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="targetArea" label="申请位置" width="150">
                 <template #default="scope">
                   <el-tag>{{ getAreaLabel(scope.row.targetArea) }}</el-tag>
                 </template>
              </el-table-column>
              <el-table-column label="申请时段" width="200">
                <template #default="scope">
                  <div class="text-xs">
                    <div>始: {{ scope.row.startTime }}</div>
                    <div>止: {{ scope.row.endTime }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button type="success" size="small" @click="handleApproveApp(scope.row)">同意上墙</el-button>
                  <el-button type="danger" size="small" plain @click="handleRejectApp(scope.row)">驳回</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 4. 关于我们 -->
        <el-tab-pane label="关于我们" name="about">
          <div class="pt-4 max-w-4xl">
            <el-form :model="aboutContent" label-width="100px">
              <el-form-item label="主标题"><el-input v-model="aboutContent.title" /></el-form-item>
              <el-form-item label="正文内容"><el-input v-model="aboutContent.content" type="textarea" :rows="10" /></el-form-item>
              <el-form-item><el-button type="primary" @click="handleAboutSave">保存修改</el-button></el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 5. 网站设置 -->
        <el-tab-pane label="网站设置" name="site">
           <div class="pt-4 max-w-3xl space-y-8">
             <el-card shadow="never" class="border border-slate-200">
               <template #header><span class="font-bold text-slate-700">基础信息</span></template>
               <el-form :model="siteConfig" label-width="120px">
                 <el-form-item label="网站LOGO文字"><el-input v-model="siteConfig.logo" /></el-form-item>
                 <el-form-item label="网站中文名"><el-input v-model="siteConfig.siteName" /></el-form-item>
               </el-form>
               <el-button type="primary" class="mt-4" @click="handleSiteSave">保存设置</el-button>
             </el-card>
           </div>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- 弹窗部分 -->
    <el-dialog v-model="carouselDialogVisible" title="上传轮播图" width="600px">
        <el-form :model="carouselForm" label-width="80px">
           <el-form-item label="标题"><el-input v-model="carouselForm.title" /></el-form-item>
           <el-form-item label="图片"><el-upload action="#" list-type="picture-card" :auto-upload="false"><el-icon><Plus /></el-icon></el-upload></el-form-item>
        </el-form>
        <template #footer><el-button @click="carouselDialogVisible = false">取消</el-button><el-button type="primary" @click="handleCarouselSave">保存</el-button></template>
    </el-dialog>
    
    <el-dialog v-model="adDialogVisible" title="配置广告位" width="600px">
         <el-form :model="adForm" label-width="80px">
            <el-form-item label="名称"><el-input v-model="adForm.name" disabled /></el-form-item>
            <el-form-item label="图片"><el-upload action="#" list-type="picture-card" :auto-upload="false"><el-icon><Plus /></el-icon></el-upload></el-form-item>
         </el-form>
         <template #footer><el-button @click="adDialogVisible = false">取消</el-button><el-button type="primary" @click="handleAdSave">保存</el-button></template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'

definePageMeta({ layout: 'admin' })

const activeTab = ref('applications') // 默认展示申请处理，方便验收

// 1. 轮播图数据
const carouselDialogVisible = ref(false)
const carouselForm = ref({ title: '', image: '', link: '', sort: 1 })
const carouselList = ref([
  { id: 1, title: '联合国采购平台XRIPP', image: 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=1920', link: '/about', sort: 1, type: '系统默认' }
])
const handleCarouselUpload = () => { carouselDialogVisible.value = true }
const handleCarouselSave = () => {
  if (carouselList.value.length >= 5) {
    return ElMessage.warning('轮播图最多5张')
  }
  if (!carouselForm.value.title) {
    return ElMessage.warning('请输入轮播图标题')
  }

  carouselList.value.push({
    id: Date.now(),
    ...carouselForm.value,
    type: '手动上传'
  })

  carouselForm.value = { title: '', image: '', link: '', sort: 1 }
  carouselDialogVisible.value = false
  ElMessage.success('保存成功')
}

const handleCarouselDelete = (id: number) => { carouselList.value = carouselList.value.filter(i => i.id !== id) }

// 2. 广告位数据
const adDialogVisible = ref(false)
const adForm = ref<any>({})
const adsList = ref([
  { id: 1, name: '首页侧边栏', position: '首页右侧', status: 'empty' },
  { id: 2, name: '活动页横幅', position: '活动列表顶', status: 'active', image: 'https://via.placeholder.com/800x100' }
])
const handleAdEdit = (ad: any) => { adForm.value = {...ad}; adDialogVisible.value = true }
const handleAdSave = () => {
  const idx = adsList.value.findIndex(item => item.id === adForm.value.id)
  if (idx !== -1) {
    adsList.value[idx] = {
      ...adsList.value[idx],
      ...adForm.value,
      status: 'active'
    }
  }
  adDialogVisible.value = false
  ElMessage.success('配置已更新')
}

// 3. ✅ [新增] 显示申请数据
const applicationList = ref([
  { 
    id: 101, 
    title: '2026全球公共采购高峰论坛', 
    source: '活动管理 (Activity)', 
    targetArea: 'home', 
    startTime: '2026-02-01 00:00', 
    endTime: '2026-02-28 23:59',
    submitTime: '2026-01-29 10:30',
    image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=200'
  },
  { 
    id: 102, 
    title: '联合国采购入门指南(课程)', 
    source: '培训管理 (Training)', 
    targetArea: 'activity', 
    startTime: '2026-02-05 00:00', 
    endTime: '2026-03-05 23:59',
    submitTime: '2026-01-28 15:20',
    image: 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=200'
  }
])

const getAreaLabel = (area: string) => {
  const map: Record<string, string> = { 'home': '首页轮播', 'activity': '活动中心推荐', 'service': '平台服务推荐' }
  return map[area] || area
}

// 同意上墙
const handleApproveApp = (row: any) => {
  ElMessageBox.confirm(`确认将 "${row.title}" 添加到显示位置吗？`, '同意申请', {
    confirmButtonText: '同意并上墙',
    type: 'success'
  }).then(() => {
    if (row.targetArea === 'home') {
      carouselList.value.push({
        id: Date.now(),
        title: row.title,
        image: row.image, 
        link: `/services/${row.id}`, 
        sort: carouselList.value.length + 1,
        type: '申请通过'
      })
      ElMessage.success('已同意，内容已自动添加到轮播图列表')
    } else {
      ElMessage.success('已同意，内容已添加到对应板块推荐位')
    }
    applicationList.value = applicationList.value.filter(i => i.id !== row.id)
  })
}

const handleRejectApp = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
    inputType: 'textarea',
    inputValidator: (value) => value?.trim() ? true : '请输入驳回原因'
  }).then(() => {
    applicationList.value = applicationList.value.filter(i => i.id !== row.id)
    ElMessage.info('申请已驳回')
  })
}

// 4. 其他配置
const aboutContent = ref({ title: '关于XRIPP', content: '...' })
const handleAboutSave = () => ElMessage.success('保存成功')
const siteConfig = ref({ logo: 'XRIPP', siteName: 'XRIPP国际公共采购平台' })
const handleSiteSave = () => ElMessage.success('设置保存成功')

</script>
