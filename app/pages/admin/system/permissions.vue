<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\system\permissions.vue
  版本: V1.1 (2026-02-06)
  
  ✅ 核心功能:
  1. [登录模式] 城市合伙人/发布管理员/审核管理员三种模式
  2. [权限模板] 预设角色权限模板
  3. [模块授权] 按模块分配权限
  4. [角色授权] 将模板分配到角色
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <!-- 登录模式 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4">
        <h3 class="text-lg font-bold text-slate-800">登录模式与角色</h3>
        <p class="text-xs text-slate-500 mt-1">定义后台登录模式与默认权限模板</p>
      </div>

      <el-table :data="loginModes" border stripe>
        <el-table-column prop="mode" label="登录模式" width="180" />
        <el-table-column prop="desc" label="说明" />
        <el-table-column label="启用" width="120" align="center">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" />
          </template>
        </el-table-column>
        <el-table-column label="默认模板" width="180">
          <template #default="scope">
            <el-select v-model="scope.row.defaultTemplate" class="w-full" size="small">
              <el-option v-for="tpl in templates" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-button type="primary" @click="handleSaveModes">保存设置</el-button>
      </div>
    </div>

    <!-- 权限模板 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h3 class="text-lg font-bold text-slate-800">权限模板</h3>
          <p class="text-xs text-slate-500 mt-1">预设角色权限模板，便于快速授权</p>
        </div>
        <el-button type="primary" @click="openTemplateDialog()">
          <el-icon class="mr-2"><Plus /></el-icon> 新增模板
        </el-button>
      </div>

      <el-table :data="templates" border stripe>
        <el-table-column prop="name" label="模板名称" width="200" />
        <el-table-column prop="desc" label="说明" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" @click="openTemplateDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="removeTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 模块授权 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200">
      <div class="mb-4">
        <h3 class="text-lg font-bold text-slate-800">模块授权</h3>
        <p class="text-xs text-slate-500 mt-1">按模块分配权限（可复用模板）</p>
      </div>

      <div class="flex gap-4 mb-4">
        <el-select v-model="activeTemplateId" placeholder="选择模板" class="w-64">
          <el-option v-for="tpl in templates" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
        </el-select>
        <el-button type="primary" @click="applyTemplate">应用模板</el-button>
        <el-button @click="resetModulePermissions">重置</el-button>
      </div>

      <el-table :data="modulePermissions" border stripe>
        <el-table-column prop="module" label="模块" width="200" />
        <el-table-column label="查看" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.view" /></template>
        </el-table-column>
        <el-table-column label="新增" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.create" /></template>
        </el-table-column>
        <el-table-column label="编辑" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.edit" /></template>
        </el-table-column>
        <el-table-column label="删除" width="120" align="center">
          <template #default="scope"><el-switch v-model="scope.row.delete" /></template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-button type="primary" @click="saveModulePermissions">保存授权</el-button>
      </div>
    </div>

    <!-- 模板弹窗 -->
    <el-dialog v-model="templateDialogVisible" :title="templateForm.id ? '编辑模板' : '新增模板'" width="600px">
      <el-form :model="templateForm" label-width="120px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="templateForm.desc" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

definePageMeta({ layout: 'admin' })

const loginModes = ref([
  { mode: '城市合伙人', desc: '面向城市合伙人使用', enabled: true, defaultTemplate: 1 },
  { mode: '发布管理员', desc: '负责内容发布与管理', enabled: true, defaultTemplate: 2 },
  { mode: '审核管理员', desc: '负责内容审核与审批', enabled: true, defaultTemplate: 3 }
])

const templates = ref([
  { id: 1, name: '合伙人模板', desc: '基础查看与发布权限' },
  { id: 2, name: '发布模板', desc: '发布与编辑为主' },
  { id: 3, name: '审核模板', desc: '审核与审批为主' }
])

const modulePermissions = ref([
  { module: '服务发布', view: true, create: true, edit: true, delete: false },
  { module: '活动管理', view: true, create: true, edit: true, delete: false },
  { module: '广告管理', view: true, create: false, edit: false, delete: false },
  { module: '会员管理', view: true, create: false, edit: true, delete: false },
  { module: '财务管理', view: true, create: false, edit: false, delete: false }
])

const activeTemplateId = ref(1)

const templateDialogVisible = ref(false)
const templateForm = ref({ id: 0, name: '', desc: '' })

const handleSaveModes = () => ElMessage.success('登录模式已保存')

const openTemplateDialog = (tpl?: any) => {
  templateForm.value = tpl ? { ...tpl } : { id: 0, name: '', desc: '' }
  templateDialogVisible.value = true
}

const saveTemplate = () => {
  if (!templateForm.value.name) return ElMessage.warning('请填写模板名称')
  if (templateForm.value.id) {
    const idx = templates.value.findIndex(t => t.id === templateForm.value.id)
    if (idx !== -1) templates.value[idx] = { ...templateForm.value }
  } else {
    templateForm.value.id = Date.now()
    templates.value.push({ ...templateForm.value })
  }
  templateDialogVisible.value = false
  ElMessage.success('模板已保存')
}

const removeTemplate = (tpl: any) => {
  ElMessageBox.confirm(`确认删除模板 "${tpl.name}" 吗？`, '提示', { type: 'warning' }).then(() => {
    templates.value = templates.value.filter(t => t.id !== tpl.id)
    ElMessage.success('已删除')
  })
}

const applyTemplate = () => {
  ElMessage.success('模板已应用到模块权限（示例）')
}

const resetModulePermissions = () => {
  modulePermissions.value = modulePermissions.value.map(m => ({ ...m, view: false, create: false, edit: false, delete: false }))
  ElMessage.warning('权限已清空')
}

const saveModulePermissions = () => ElMessage.success('模块权限已保存')
</script>
