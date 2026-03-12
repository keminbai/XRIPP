<template>
  <div class="bg-white p-6 rounded-xl border border-slate-200">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <div class="mb-6">
      <h3 class="text-lg font-bold text-slate-800">系统设置</h3>
      <p class="text-xs text-slate-500 mt-1">配置系统基础参数</p>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="基础设置" name="basic">
        <el-form :model="basicSettings" label-width="150px" class="max-w-3xl">
          <el-form-item label="系统名称">
            <el-input v-model="basicSettings.systemName" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <el-upload action="/api/upload" list-type="picture-card" :limit="1">
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
          <el-form-item label="ICP备案号">
            <el-input v-model="basicSettings.icp" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="邮件配置" name="email">
        <el-form :model="emailSettings" label-width="150px" class="max-w-3xl">
          <el-form-item label="SMTP服务器">
            <el-input v-model="emailSettings.smtp" />
          </el-form-item>
          <el-form-item label="SMTP端口">
            <el-input-number v-model="emailSettings.port" />
          </el-form-item>
          <el-form-item label="发件邮箱">
            <el-input v-model="emailSettings.from" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="安全设置" name="security">
        <el-form :model="securitySettings" label-width="150px" class="max-w-3xl">
          <el-form-item label="密码最小长度">
            <el-input-number v-model="securitySettings.minPasswordLength" :min="6" :max="20" />
          </el-form-item>
          <el-form-item label="登录失败锁定">
            <el-switch v-model="securitySettings.loginLock" />
          </el-form-item>
          <el-form-item label="最大失败次数">
            <el-input-number v-model="securitySettings.maxFailCount" :min="3" :max="10" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

definePageMeta({ layout: 'admin' })

const activeTab = ref('basic')
const basicSettings = ref({ systemName: 'XRIPP管理后台', icp: '沪ICP备XXXXXXXX号' })
const emailSettings = ref({ smtp: 'smtp.example.com', port: 465, from: 'noreply@xripp.com' })
const securitySettings = ref({ minPasswordLength: 8, loginLock: true, maxFailCount: 5 })

const handleSave = () => ElMessage.success('设置已保存')
</script>