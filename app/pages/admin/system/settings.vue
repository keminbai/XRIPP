<template>
  <div class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        系统设置已接入真实 API，保存后会持久化到后台配置中心。
      </template>
    </el-alert>

    <div class="mb-6 mt-4 flex items-center justify-between">
      <div>
        <h3 class="text-lg font-bold text-slate-800">系统设置</h3>
        <p class="mt-1 text-xs text-slate-500">配置系统基础参数</p>
      </div>
      <el-button :loading="loading" @click="loadSettings">刷新配置</el-button>
    </div>

    <el-tabs v-model="activeTab" v-loading="loading">
      <el-tab-pane label="基础设置" name="basic">
        <el-form :model="basicSettings" label-width="150px" class="max-w-3xl">
          <el-form-item label="系统名称">
            <el-input v-model="basicSettings.systemName" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <div class="w-full space-y-3">
              <el-upload
                action="/api/common/upload"
                :show-file-list="false"
                accept=".png,.jpg,.jpeg,.svg,.webp"
                :on-success="handleLogoUploadSuccess"
                :on-error="handleLogoUploadError"
              >
                <el-button>
                  <el-icon class="mr-1"><Plus /></el-icon>
                  上传 Logo
                </el-button>
              </el-upload>
              <el-input v-model="basicSettings.logoUrl" placeholder="上传后回填图片 URL" />
            </div>
          </el-form-item>
          <el-form-item label="ICP备案号">
            <el-input v-model="basicSettings.icp" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveBasicSettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="邮件配置" name="email">
        <el-form :model="emailSettings" label-width="150px" class="max-w-3xl">
          <el-form-item label="SMTP服务器">
            <el-input v-model="emailSettings.smtp" />
          </el-form-item>
          <el-form-item label="SMTP端口">
            <el-input-number v-model="emailSettings.port" class="!w-full" />
          </el-form-item>
          <el-form-item label="发件邮箱">
            <el-input v-model="emailSettings.from" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveEmailSettings">保存设置</el-button>
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
            <el-button type="primary" :loading="saving" @click="saveSecuritySettings">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'

definePageMeta({ layout: 'admin' })

const NAMESPACE = 'system_settings'
const CONFIG_META = {
  basic_settings: { name: '系统基础设置', sortOrder: 10 },
  email_settings: { name: '系统邮件配置', sortOrder: 20 },
  security_settings: { name: '系统安全设置', sortOrder: 30 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

const activeTab = ref('basic')
const basicSettings = ref({ systemName: 'XRIPP管理后台', logoUrl: '', icp: '沪ICP备XXXXXXXX号' })
const emailSettings = ref({ smtp: 'smtp.example.com', port: 465, from: 'noreply@xripp.com' })
const securitySettings = ref({ minPasswordLength: 8, loginLock: true, maxFailCount: 5 })

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeBasicSettings = (value: any) => ({
  systemName: value?.systemName || 'XRIPP管理后台',
  logoUrl: value?.logoUrl || '',
  icp: value?.icp || '沪ICP备XXXXXXXX号'
})

const normalizeEmailSettings = (value: any) => ({
  smtp: value?.smtp || 'smtp.example.com',
  port: normalizeNumber(value?.port, 465),
  from: value?.from || 'noreply@xripp.com'
})

const normalizeSecuritySettings = (value: any) => ({
  minPasswordLength: normalizeNumber(value?.minPasswordLength, 8),
  loginLock: value?.loginLock !== false,
  maxFailCount: normalizeNumber(value?.maxFailCount, 5)
})

const loadSettings = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    basicSettings.value = normalizeBasicSettings(itemMap.basic_settings)
    emailSettings.value = normalizeEmailSettings(itemMap.email_settings)
    securitySettings.value = normalizeSecuritySettings(itemMap.security_settings)
  } catch (error: any) {
    ElMessage.error(error?.message || '系统设置加载失败')
  }
}

const persistSettings = async (key: keyof typeof CONFIG_META, value: unknown, successMessage: string) => {
  try {
    await saveNamespaceItems([{ key, value }])
    ElMessage.success(successMessage)
    await loadSettings()
  } catch (error: any) {
    ElMessage.error(error?.message || '系统设置保存失败')
  }
}

const handleLogoUploadSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response?.code === 200 && response?.data?.url) {
    basicSettings.value.logoUrl = response.data.url
    ElMessage.success('Logo 上传成功')
    return
  }
  ElMessage.error('Logo 上传失败')
}

const handleLogoUploadError: UploadProps['onError'] = () => {
  ElMessage.error('Logo 上传失败')
}

const saveBasicSettings = async () => {
  await persistSettings('basic_settings', basicSettings.value, '基础设置已保存')
}

const saveEmailSettings = async () => {
  await persistSettings('email_settings', emailSettings.value, '邮件配置已保存')
}

const saveSecuritySettings = async () => {
  await persistSettings('security_settings', securitySettings.value, '安全设置已保存')
}

onMounted(async () => {
  await loadSettings()
})
</script>
