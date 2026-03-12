<!-- 文件路径: D:\ipp-platform\app\pages\admin\system\login-config.vue -->
<template>
  <div class="space-y-6">
    <el-alert type="info" :closable="true" show-icon>
      <template #title>
        系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置。
      </template>
    </el-alert>
    <div class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm max-w-3xl">
      <h3 class="text-lg font-bold text-slate-800 mb-6">登录安全配置</h3>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础配置" name="basic" />
        <el-tab-pane label="安全策略" name="security" />
        <el-tab-pane label="第三方登录" name="oauth" />
      </el-tabs>

      <!-- 基础配置 -->
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
            <el-button type="primary" @click="handleSaveBasic">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 安全策略 -->
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
            <div class="mt-2 text-xs text-slate-500" v-if="securityConfig.enableLockout">
              失败
              <el-input-number v-model="securityConfig.lockoutAttempts" :min="3" :max="10" size="small" class="mx-2 w-24" />
              次后锁定
              <el-input-number v-model="securityConfig.lockoutDuration" :min="5" :max="60" size="small" class="mx-2 w-24" />
              分钟
            </div>
          </el-form-item>

          <el-form-item label="强制定期修改密码">
            <el-switch v-model="securityConfig.forcePasswordChange" />
            <div class="mt-2 text-xs text-slate-500" v-if="securityConfig.forcePasswordChange">
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
            <el-button type="primary" @click="handleSaveSecurity">保存配置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第三方登录 -->
      <div v-if="activeTab === 'oauth'" class="mt-6">
        <el-alert title="第三方登录配置需要先在对应平台申请AppID与密钥" type="info" :closable="false" class="mb-4" />
        <div class="space-y-4">
          <el-card shadow="never" v-for="provider in oauthProviders" :key="provider.name">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded flex items-center justify-center" :class="provider.bgClass">
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

        <el-button type="primary" class="mt-6" @click="handleSaveOAuth">保存第三方登录配置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

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

const oauthProviders = ref([
  { name: 'wechat', label: '微信登录', icon: '🟢', desc: '使用微信扫码登录', bgClass: 'bg-green-50', enabled: false, appId: '', appSecret: '' },
  { name: 'alipay', label: '支付宝登录', icon: '🔵', desc: '使用支付宝账号登录', bgClass: 'bg-blue-50', enabled: false, appId: '', appSecret: '' },
  { name: 'dingtalk', label: '钉钉登录', icon: '📌', desc: '使用钉钉账号登录', bgClass: 'bg-blue-50', enabled: false, appId: '', appSecret: '' }
])

const handleSaveBasic = () => ElMessage.success('基础配置已保存')
const handleSaveSecurity = () => ElMessage.success('安全策略已保存')
const handleSaveOAuth = () => ElMessage.success('第三方登录配置已保存')
</script>
