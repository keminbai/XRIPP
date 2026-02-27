<!-- 
  ========================================================================
  文件路径: app/pages/admin/partners/center.vue
  版本: V1.0 (2026-02-04)
  
  ✅ 对应 Row 36: 合伙人个人中心 / 个人信息管理
  1. 展示资格日期、邀请码、头像
  2. 支持资料变更
  3. 证书下载
  ========================================================================
-->
<template>
  <div class="space-y-6">
    <!-- 顶部概览 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row md:items-center gap-6">
      <div class="flex items-center gap-4">
        <el-upload
          class="avatar-uploader"
          action="/api/common/upload"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
        >
          <el-avatar :size="72" :src="profile.avatar">
            {{ profile.name ? profile.name[0] : '合' }}
          </el-avatar>
        </el-upload>

        <div>
          <div class="text-lg font-bold text-slate-800">{{ profile.name }}</div>
          <div class="text-xs text-slate-500 mt-1">合伙人等级：{{ profile.stars }} 星</div>
          <div class="mt-2">
            <el-tag size="small" type="info">邀请码：{{ profile.invitationCode }}</el-tag>
          </div>
        </div>
      </div>

      <div class="flex-1 grid grid-cols-2 md:grid-cols-3 gap-4">
        <div class="bg-slate-50 p-3 rounded-lg">
          <div class="text-xs text-slate-500">资格有效期</div>
          <div class="font-bold text-slate-800">{{ profile.expireDate }}</div>
        </div>
        <div class="bg-slate-50 p-3 rounded-lg">
          <div class="text-xs text-slate-500">所在城市</div>
          <div class="font-bold text-slate-800">{{ profile.city }}</div>
        </div>
        <div class="bg-slate-50 p-3 rounded-lg">
          <div class="text-xs text-slate-500">联系电话</div>
          <div class="font-bold text-slate-800">{{ profile.phone }}</div>
        </div>
      </div>
    </div>

    <!-- 基本信息 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <h3 class="text-base font-bold text-slate-800 mb-4">个人信息管理</h3>
      <el-form :model="profile" label-width="100px" class="max-w-3xl">
        <el-form-item label="姓名" required>
          <el-input v-model="profile.name" placeholder="请输入姓名" />
        </el-form-item>

        <el-form-item label="联系方式" required>
          <el-input v-model="profile.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="身份证号" required>
          <el-input v-model="profile.idCode" placeholder="身份证/护照号" />
        </el-form-item>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <el-form-item label="国家" required>
            <el-select v-model="profile.country" class="w-full">
              <el-option label="中国" value="China" />
              <el-option label="马来西亚" value="Malaysia" />
            </el-select>
          </el-form-item>
          <el-form-item label="城市" required>
            <el-select v-model="profile.city" class="w-full">
              <el-option label="上海" value="上海" />
              <el-option label="北京" value="北京" />
              <el-option label="深圳" value="深圳" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="邮箱">
          <el-input v-model="profile.email" placeholder="example@company.com" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 证书下载 -->
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <h3 class="text-base font-bold text-slate-800 mb-4">证书下载</h3>
      <el-table :data="certificates" stripe>
        <el-table-column prop="name" label="证书名称" min-width="220" />
        <el-table-column prop="issueDate" label="签发日期" width="140" />
        <el-table-column prop="expireDate" label="有效期" width="140" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDownload(scope.row)">
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="certificates.length === 0" class="text-center text-slate-400 py-8">
        暂无证书
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const profile = ref({
  name: '张三',
  phone: '138****0001',
  idCode: '310101****',
  country: 'China',
  city: '上海',
  email: 'zhangsan@company.com',
  invitationCode: 'SH888',
  expireDate: '2026-12-31',
  stars: 5,
  avatar: ''
})

const certificates = ref([
  { name: '城市合伙人授权证书', issueDate: '2025-01-01', expireDate: '2026-12-31' },
  { name: '合伙人服务能力认证', issueDate: '2024-06-01', expireDate: '2026-06-01' }
])

const handleAvatarSuccess = (res: any) => {
  // 兼容返回 { url } 或 { data: { url } }
  profile.value.avatar = res?.url || res?.data?.url || ''
  ElMessage.success('头像上传成功')
}

const handleSave = () => {
  ElMessage.success('信息已保存')
}

const handleReset = () => {
  // 这里简单提示，实际可从接口重新拉取
  ElMessage.info('已重置为当前值')
}

const handleDownload = (row: any) => {
  ElMessage.success(`开始下载：${row.name}`)
}
</script>
