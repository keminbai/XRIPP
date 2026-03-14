<template>
  <div class="space-y-6">
    <el-alert type="success" :closable="false" show-icon>
      <template #title>
        登录安全配置已接入真实 API，保存后会持久化到后台配置中心。
      </template>
    </el-alert>

    <div class="max-w-3xl rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="mb-6 flex items-center justify-between">
        <h3 class="text-lg font-bold text-slate-800">登录安全配置</h3>
        <el-button :loading="loading" @click="loadLoginConfig">刷新配置</el-button>
      </div>

      <el-tabs v-model="activeTab" v-loading="loading">
        <el-tab-pane label="基础配置" name="basic" />
        <el-tab-pane label="安全策略" name="security" />
        <el-tab-pane label="第三方登录" name="oauth" />
      </el-tabs>

      <div v-if="activeTab === 'basic'" class="mt-6">
        <el-form :model="basicConfig" label-width="150px">
          <el-form-item label="允许合伙人登录">
            <el-switch v-model="basicConfig.allowPartnerLogin" />
            <span class="ml-2 text-xs text-slate-500">关闭后合伙人无法登录后台</span>
          </el-form-item>

          <el-form-item label="允许服务商登录">
            <el-switch v-model="basicConfig.allowSupplierLogin" />
          </el-form-item>

          <el-form-item label="登录验证码">
            <el-switch v-model="basicConfig.requireCaptcha" />
            <span class="ml-2 text-xs text-slate-500">启用后所有登录需输入验证码</span>
          </el-form-item>

          <el-form-item label="Session有效期">
            <el-input-number v-model="basicConfig.sessionTimeout" :min="1" :max="168" />
            <span class="ml-2 text-sm text-slate-500">小时</span>
          </el-form-item>

          <el-form-item label="记住我功能">
            <el-switch v-model="basicConfig.allowRememberMe" />
            <span class="ml-2 text-xs text-slate-500">允许用户7天内免登录</span>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSaveBasic">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="activeTab === 'security'" class="mt-6">
        <el-form :model="securityConfig" label-width="150px">
          <el-form-item label="密码最小长度">
            <el-input-number v-model="securityConfig.minPasswordLength" :min="6" :max="20" />
            <span class="ml-2 text-sm text-slate-500">字符</span>
          </el-form-item>

          <el-form-item label="密码复杂度要求">
            <el-checkbox-group v-model="securityConfig.passwordRequirements">
              <el-checkbox label="uppercase">包含大写字母</el-checkbox>
              <el-checkbox label="lowercase">包含小写字母</el-checkbox>
              <el-checkbox label="number">包含数字</el-checkbox>
              <el-checkbox label="special">包含特殊字符</el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <el-form-item label="登录失败锁定">
            <el-switch v-model="securityConfig.enableLockout" />
            <div v-if="securityConfig.enableLockout" class="mt-2 text-xs text-slate-500">
              失败
              <el-input-number v-model="securityConfig.lockoutAttempts" :min="3" :max="10" size="small" class="mx-2 w-24" />
              次后锁定
              <el-input-number v-model="securityConfig.lockoutDuration" :min="5" :max="60" size="small" class="mx-2 w-24" />
              分钟
            </div>
          </el-form-item>

          <el-form-item label="强制定期修改密码">
            <el-switch v-model="securityConfig.forcePasswordChange" />
            <div v-if="securityConfig.forcePasswordChange" class="mt-2 text-xs text-slate-500">
              每
              <el-input-number v-model="securityConfig.passwordChangeDays" :min="30" :max="365" size="small" class="mx-2 w-24" />
              天强制修改一次
            </div>
          </el-form-item>

          <el-form-item label="IP白名单">
            <el-switch v-model="securityConfig.enableIPWhitelist" />
            <el-input
              v-if="securityConfig.enableIPWhitelist"
              v-model="securityConfig.ipWhitelist"
              type="textarea"
              :rows="3"
              placeholder="每行一个IP，例如：192.168.1.100"
              class="mt-2"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSaveSecurity">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="activeTab === 'oauth'" class="mt-6">
        <el-alert title="第三方登录配置需要先在对应平台申请AppID与密钥" type="info" :closable="false" class="mb-4" />
        <div class="space-y-4">
          <el-card shadow="never" v-for="provider in oauthProviders" :key="provider.name">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="flex h-10 w-10 items-center justify-center rounded" :class="provider.bgClass">
                  <span class="text-xl">{{ provider.icon }}</span>
                </div>
                <div>
                  <div class="font-bold">{{ provider.label }}</div>
                  <div class="text-xs text-slate-500">{{ provider.desc }}</div>
                </div>
              </div>
              <el-switch v-model="provider.enabled" />
            </div>
            <div v-if="provider.enabled" class="mt-4 grid grid-cols-2 gap-4">
              <el-input v-model="provider.appId" placeholder="App ID" size="small" />
              <el-input v-model="provider.appSecret" placeholder="App Secret" size="small" type="password" show-password />
            </div>
          </el-card>
        </div>

        <el-button type="primary" class="mt-6" :loading="saving" @click="handleSaveOAuth">保存第三方登录配置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminConfigNamespace } from '@/composables/useAdminConfigNamespace'

definePageMeta({ layout: 'admin' })

type OAuthProvider = {
  name: string
  label: string
  icon: string
  desc: string
  bgClass: string
  enabled: boolean
  appId: string
  appSecret: string
}

const NAMESPACE = 'system_login_config'
const CONFIG_META = {
  basic_config: { name: '登录基础配置', sortOrder: 10 },
  security_config: { name: '登录安全策略', sortOrder: 20 },
  oauth_providers: { name: '第三方登录配置', sortOrder: 30 }
} as const

const { loading, saving, loadNamespaceItems, saveNamespaceItems } = useAdminConfigNamespace(
  NAMESPACE,
  CONFIG_META
)

const activeTab = ref('basic')

const basicConfig = ref({
  allowPartnerLogin: true,
  allowSupplierLogin: true,
  requireCaptcha: true,
  sessionTimeout: 24,
  allowRememberMe: true
})

const securityConfig = ref({
  minPasswordLength: 8,
  passwordRequirements: ['lowercase', 'number'],
  enableLockout: true,
  lockoutAttempts: 5,
  lockoutDuration: 30,
  forcePasswordChange: false,
  passwordChangeDays: 90,
  enableIPWhitelist: false,
  ipWhitelist: ''
})

const oauthProviders = ref<OAuthProvider[]>([
  { name: 'wechat', label: '微信登录', icon: 'W', desc: '使用微信扫码登录', bgClass: 'bg-green-50', enabled: false, appId: '', appSecret: '' },
  { name: 'alipay', label: '支付宝登录', icon: 'A', desc: '使用支付宝账号登录', bgClass: 'bg-blue-50', enabled: false, appId: '', appSecret: '' },
  { name: 'dingtalk', label: '钉钉登录', icon: 'D', desc: '使用钉钉账号登录', bgClass: 'bg-sky-50', enabled: false, appId: '', appSecret: '' }
])

const normalizeNumber = (value: unknown, fallback = 0) => {
  const num = Number(value ?? fallback)
  return Number.isFinite(num) ? num : fallback
}

const normalizeBasicConfig = (value: any) => ({
  allowPartnerLogin: value?.allowPartnerLogin !== false,
  allowSupplierLogin: value?.allowSupplierLogin !== false,
  requireCaptcha: value?.requireCaptcha !== false,
  sessionTimeout: normalizeNumber(value?.sessionTimeout, 24),
  allowRememberMe: value?.allowRememberMe !== false
})

const normalizeSecurityConfig = (value: any) => ({
  minPasswordLength: normalizeNumber(value?.minPasswordLength, 8),
  passwordRequirements: Array.isArray(value?.passwordRequirements) ? value.passwordRequirements.map((item: any) => String(item)) : ['lowercase', 'number'],
  enableLockout: value?.enableLockout !== false,
  lockoutAttempts: normalizeNumber(value?.lockoutAttempts, 5),
  lockoutDuration: normalizeNumber(value?.lockoutDuration, 30),
  forcePasswordChange: Boolean(value?.forcePasswordChange),
  passwordChangeDays: normalizeNumber(value?.passwordChangeDays, 90),
  enableIPWhitelist: Boolean(value?.enableIPWhitelist),
  ipWhitelist: value?.ipWhitelist || ''
})

const normalizeOAuthProviders = (value: any): OAuthProvider[] => {
  if (!Array.isArray(value) || value.length === 0) {
    return [
      { name: 'wechat', label: '微信登录', icon: 'W', desc: '使用微信扫码登录', bgClass: 'bg-green-50', enabled: false, appId: '', appSecret: '' },
      { name: 'alipay', label: '支付宝登录', icon: 'A', desc: '使用支付宝账号登录', bgClass: 'bg-blue-50', enabled: false, appId: '', appSecret: '' },
      { name: 'dingtalk', label: '钉钉登录', icon: 'D', desc: '使用钉钉账号登录', bgClass: 'bg-sky-50', enabled: false, appId: '', appSecret: '' }
    ]
  }
  return value.map((item: any) => ({
    name: item?.name || '',
    label: item?.label || '',
    icon: item?.icon || '',
    desc: item?.desc || '',
    bgClass: item?.bgClass || 'bg-slate-50',
    enabled: Boolean(item?.enabled),
    appId: item?.appId || '',
    appSecret: item?.appSecret || ''
  }))
}

const loadLoginConfig = async () => {
  try {
    const itemMap = await loadNamespaceItems()
    basicConfig.value = normalizeBasicConfig(itemMap.basic_config)
    securityConfig.value = normalizeSecurityConfig(itemMap.security_config)
    oauthProviders.value = normalizeOAuthProviders(itemMap.oauth_providers)
  } catch (error: any) {
    ElMessage.error(error?.message || '登录配置加载失败')
  }
}

const persistConfig = async (key: keyof typeof CONFIG_META, value: unknown, successMessage: string) => {
  try {
    await saveNamespaceItems([{ key, value }])
    ElMessage.success(successMessage)
    await loadLoginConfig()
  } catch (error: any) {
    ElMessage.error(error?.message || '登录配置保存失败')
  }
}

const handleSaveBasic = async () => {
  await persistConfig('basic_config', basicConfig.value, '基础配置已保存')
}

const handleSaveSecurity = async () => {
  await persistConfig('security_config', securityConfig.value, '安全策略已保存')
}

const handleSaveOAuth = async () => {
  await persistConfig('oauth_providers', oauthProviders.value, '第三方登录配置已保存')
}

onMounted(async () => {
  await loadLoginConfig()
})
</script>
