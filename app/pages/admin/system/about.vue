<template>
  <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
    <div class="text-center py-8">
      <div class="w-20 h-20 bg-brand-600 rounded-xl mx-auto mb-4 flex items-center justify-center text-white text-3xl font-bold">
        X
      </div>
      <h2 class="text-2xl font-bold text-slate-800 mb-2">XRIPP 管理后台系统</h2>
      <p class="text-slate-500 mb-8">International Public Procurement Platform</p>

      <div class="max-w-3xl mx-auto space-y-6">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="运行版本">
            <span class="font-semibold text-slate-800">{{ runtimeInfo.runtimeVersion || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="应用名称">{{ runtimeInfo.application || 'xripp-backend' }}</el-descriptions-item>
          <el-descriptions-item label="前端技术栈">Nuxt 4 + Vue 3 + Element Plus</el-descriptions-item>
          <el-descriptions-item label="后端技术栈">Spring Boot 3 + MyBatis-Plus</el-descriptions-item>
          <el-descriptions-item label="数据库">SQL Server 2022</el-descriptions-item>
          <el-descriptions-item label="Schema 预检">{{ runtimeInfo.schemaPreflightEnabled ? '已启用' : '已关闭' }}</el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ runtimeInfo.startedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前环境">
            {{ runtimeInfo.activeProfiles?.length ? runtimeInfo.activeProfiles.join(', ') : 'default' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-card shadow="never" class="border border-slate-200 text-left">
          <template #header>
            <span class="font-bold">当前模块状态</span>
          </template>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm text-slate-700">
            <div>会员管理系统：真实后端已接入</div>
            <div>信息发布系统：真实后端已接入</div>
            <div>服务商系统：真实后端已接入</div>
            <div>标书发布系统：真实后端已接入</div>
            <div>出海发布系统：真实后端已接入</div>
            <div>合伙人系统：真实后端已接入</div>
            <div>财务配置：真实后端已接入</div>
            <div>数据看板配置：真实后端已接入</div>
            <div>系统综管：除说明页外已接入真实后端</div>
            <div>业务审核中心：真实后端已接入</div>
          </div>
        </el-card>

        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            本页为系统说明页。运行版本与环境信息来自 `/api/v3/runtime-info`，其余长期说明以 `docs/index.md` 为准。
          </template>
        </el-alert>

        <div class="text-xs text-slate-400">
          © 2026 XRIPP Platform. All rights reserved.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive } from 'vue'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const runtimeInfo = reactive({
  application: '',
  runtimeVersion: '',
  schemaPreflightEnabled: false,
  startedAt: '',
  activeProfiles: [] as string[]
})

const loadRuntimeInfo = async () => {
  try {
    const res = await apiRequest<any>('/v3/runtime-info')
    runtimeInfo.application = String(res.data?.application || '')
    runtimeInfo.runtimeVersion = String(res.data?.runtimeVersion || '')
    runtimeInfo.schemaPreflightEnabled = Boolean(res.data?.schemaPreflightEnabled)
    runtimeInfo.startedAt = String(res.data?.startedAt || '')
    runtimeInfo.activeProfiles = Array.isArray(res.data?.activeProfiles)
      ? res.data.activeProfiles.map((item: any) => String(item))
      : []
  } catch (error: any) {
    ElMessage.error(error?.message || '运行信息加载失败')
  }
}

onMounted(loadRuntimeInfo)
</script>
