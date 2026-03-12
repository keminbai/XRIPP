<template>
  <div class="bg-white p-6 rounded-xl border border-slate-200">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <div class="text-center py-8">
      <div class="w-20 h-20 bg-brand-600 rounded-xl mx-auto mb-4 flex items-center justify-center text-white text-3xl font-bold">
        X
      </div>
      <h2 class="text-2xl font-bold text-slate-800 mb-2">XRIPP 管理后台系统</h2>
      <p class="text-slate-500 mb-8">International Public Procurement Platform</p>

      <div class="max-w-2xl mx-auto space-y-6">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统版本">V4.0 Final</el-descriptions-item>
          <el-descriptions-item label="发布日期">2026-01-29</el-descriptions-item>
          <el-descriptions-item label="技术栈">Nuxt 3 + Vue 3 + Element Plus</el-descriptions-item>
          <el-descriptions-item label="数据库">SQL Server 2022</el-descriptions-item>
          <el-descriptions-item label="开发团队">XRIPP技术部</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">tech@xripp-platform.com</el-descriptions-item>
        </el-descriptions>

        <el-card shadow="never" class="border border-slate-200">
          <template #header>
            <span class="font-bold">功能模块</span>
          </template>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>✅ 会员管理系统</div>
            <div>✅ 信息发布系统</div>
            <div>✅ 服务商系统</div>
            <div>✅ 标书发布系统</div>
            <div>✅ 出海发布系统</div>
            <div>✅ 合伙人系统</div>
            <div>✅ 财务配置</div>
            <div>✅ 数据看板配置</div>
            <div>✅ 系统综管</div>
            <div>✅ 业务审核中心</div>
          </div>
        </el-card>

        <div class="text-xs text-slate-400">
          © 2026 XRIPP Platform. All rights reserved.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: 'admin' })
</script>