<template>
  <div class="min-h-screen flex bg-white font-sans">
    <div class="hidden lg:flex lg:w-5/12 bg-slate-900 relative overflow-hidden flex-col justify-between p-12">
      <div class="absolute inset-0 z-0 overflow-hidden">
        <img
          src="https://images.unsplash.com/photo-1497215728101-856f4ea42174?q=80&w=2070&auto=format&fit=crop"
          alt="Real Business World"
          class="w-full h-full object-cover animate-breathe-zoom"
        />
        <div class="absolute inset-0 bg-black/20"></div>
        <div class="absolute inset-0 bg-gradient-to-b from-slate-900/60 via-transparent to-slate-900/90"></div>
      </div>

      <div class="relative z-10 flex items-center gap-3">
        <div class="w-10 h-10 bg-slate-900/50 backdrop-blur-md border border-white/20 rounded-lg flex items-center justify-center text-white font-bold text-xl shadow-lg">
          X
        </div>
        <span class="text-white font-bold text-xl tracking-wide drop-shadow-md">XRIPP Global</span>
      </div>

      <div class="relative z-10 my-auto">
        <h1 class="text-5xl font-bold text-white mb-6 leading-tight drop-shadow-[0_2px_10px_rgba(0,0,0,0.8)] tracking-tight">
          连接全球<br />
          <span class="text-blue-200">公共采购</span> 商机
        </h1>
        <p class="text-white text-lg font-light leading-relaxed max-w-md drop-shadow-md border-l-4 border-blue-500 pl-6">
          加入国际采购服务网络，与全球伙伴共享项目信息与服务机会。
        </p>
      </div>
    </div>

    <div class="w-full lg:w-7/12 flex items-center justify-center p-8 bg-white relative">
      <div class="absolute top-8 right-8">
        <button
          class="flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-brand-600 transition-colors bg-slate-50 px-4 py-2 rounded-full border border-slate-200 hover:border-brand-200 hover:bg-brand-50"
          @click="toggleRole"
        >
          <el-icon><Switch /></el-icon>
          {{ loginRole === 'member' ? '切换为管理员登录' : '切换为会员登录' }}
        </button>
      </div>

      <div class="w-full max-w-md space-y-8 transition-all duration-300">
        <div class="text-center lg:text-left">
          <div
            class="inline-block px-3 py-1 rounded-full text-xs font-bold mb-3"
            :class="loginRole === 'member' ? 'bg-blue-50 text-blue-600' : 'bg-orange-50 text-orange-600'"
          >
            {{ loginRole === 'member' ? 'MEMBER ACCESS' : 'ADMIN ACCESS' }}
          </div>
          <h2 class="text-3xl font-bold text-slate-900 mb-2">
            {{ loginRole === 'member'
              ? (memberMode === 'login' ? '欢迎回来，会员' : '注册新会员')
              : '管理后台登录' }}
          </h2>
          <p class="text-slate-500">
            {{ loginRole === 'member'
              ? (memberMode === 'login' ? '请输入手机号和密码登录' : '填写信息完成注册，即刻开始')
              : '请输入管理账号和密码' }}
          </p>
        </div>

        <!-- ========== 会员区域 ========== -->
        <div v-if="loginRole === 'member'" class="space-y-6 animate-fade-in-right">
          <!-- 登录/注册 Tab -->
          <div class="flex border-b border-slate-200 mb-6">
            <button
              class="pb-3 px-4 text-sm font-bold transition-all border-b-2"
              :class="memberMode === 'login' ? 'text-brand-600 border-brand-600' : 'text-slate-400 border-transparent hover:text-slate-600'"
              @click="memberMode = 'login'"
            >
              登录
            </button>
            <button
              class="pb-3 px-4 text-sm font-bold transition-all border-b-2"
              :class="memberMode === 'register' ? 'text-brand-600 border-brand-600' : 'text-slate-400 border-transparent hover:text-slate-600'"
              @click="memberMode = 'register'"
            >
              注册
            </button>
          </div>

          <!-- 登录表单 -->
          <div v-if="memberMode === 'login'" class="space-y-5">
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">手机号</label>
              <el-input v-model="loginForm.phone" size="large" placeholder="请输入手机号" class="custom-input h-12" @keyup.enter="handleMemberLogin">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">密码</label>
              <el-input v-model="loginForm.password" size="large" type="password" show-password placeholder="请输入密码" class="custom-input h-12" @keyup.enter="handleMemberLogin">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </div>
            <button
              class="w-full py-4 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-lg transition-all text-base mt-4 disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="memberSubmitting"
              @click="handleMemberLogin"
            >
              {{ memberSubmitting ? '登录中...' : '立即登录' }}
            </button>
            <div class="text-center text-sm text-slate-500">
              还没有账号？
              <button class="text-brand-600 font-bold hover:underline" @click="memberMode = 'register'">立即注册</button>
            </div>
          </div>

          <!-- 注册表单 -->
          <div v-else class="space-y-4">
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">手机号 <span class="text-red-500">*</span></label>
              <el-input v-model="registerForm.phone" size="large" placeholder="手机号即为登录账号" class="custom-input h-12">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">验证码 <span class="text-red-500">*</span></label>
              <div class="flex gap-3">
                <el-input v-model="registerForm.smsCode" size="large" placeholder="6位验证码" class="custom-input h-12 flex-grow" />
                <button
                  class="px-5 h-12 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-lg transition-colors whitespace-nowrap text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="smsCountdown > 0"
                  @click="handleSendSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重试` : '获取验证码' }}
                </button>
              </div>
              <div class="text-xs text-slate-400">测试环境验证码：123456</div>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">设置密码 <span class="text-red-500">*</span></label>
              <el-input v-model="registerForm.password" size="large" type="password" show-password placeholder="至少6位" class="custom-input h-12">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">公司名称 <span class="text-red-500">*</span></label>
              <el-input v-model="registerForm.companyName" size="large" placeholder="请输入您的公司全称" class="custom-input h-12">
                <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
              </el-input>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">合伙人邀请码 <span class="text-slate-400 font-normal">(选填)</span></label>
              <el-input v-model="registerForm.invitationCode" size="large" placeholder="如有合伙人邀请码请填写" class="custom-input h-12">
                <template #prefix><el-icon><Ticket /></el-icon></template>
              </el-input>
            </div>
            <button
              class="w-full py-4 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-lg transition-all text-base mt-2 disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="registerSubmitting"
              @click="handleRegister"
            >
              {{ registerSubmitting ? '注册中...' : '注册并登录' }}
            </button>
            <div class="text-center text-sm text-slate-500">
              已有账号？
              <button class="text-brand-600 font-bold hover:underline" @click="memberMode = 'login'">返回登录</button>
            </div>
          </div>
        </div>

        <!-- ========== 管理员区域 ========== -->
        <div v-else class="space-y-6 animate-fade-in-right">
          <div class="p-4 bg-orange-50 border border-orange-100 rounded-xl text-orange-800 text-sm flex gap-3 items-start mb-6">
            <el-icon class="mt-0.5 text-lg"><Warning /></el-icon>
            <div>
              <div class="font-bold">安全提示</div>
              <div>此入口仅限合伙人及平台管理员访问，您的操作将被系统记录。</div>
            </div>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-700">管理账号</label>
            <el-input
              v-model="adminForm.username"
              size="large"
              placeholder="请输入管理员账号"
              class="custom-input h-12"
              @keyup.enter="handleAdminLogin"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-700">登录密码</label>
            <el-input
              v-model="adminForm.password"
              size="large"
              type="password"
              show-password
              placeholder="请输入密码"
              class="custom-input h-12"
              @keyup.enter="handleAdminLogin"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </div>

          <button
            class="w-full py-4 bg-slate-900 hover:bg-slate-800 text-white font-bold rounded-xl shadow-lg transition-all text-base mt-4 disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="adminSubmitting"
            @click="handleAdminLogin"
          >
            {{ adminSubmitting ? '登录中...' : '安全登录' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Lock, Iphone, Switch, Warning, OfficeBuilding, Ticket } from '@element-plus/icons-vue'
import { ref, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginByPassword, setAuth, apiRequest } from '~/utils/request'

definePageMeta({ layout: false })

const route = useRoute()
const loginRole = ref<'member' | 'admin'>('member')
const memberMode = ref<'login' | 'register'>('login')
const memberSubmitting = ref(false)
const registerSubmitting = ref(false)
const adminSubmitting = ref(false)
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

onBeforeUnmount(() => {
  if (smsTimer) { clearInterval(smsTimer); smsTimer = null }
})

// 登录表单
const loginForm = ref({ phone: '', password: '' })

// 注册表单
const registerForm = ref({
  phone: '',
  smsCode: '',
  password: '',
  companyName: '',
  invitationCode: ''
})

// 管理员表单
const adminForm = ref({ username: '', password: '' })

const toggleRole = () => {
  loginRole.value = loginRole.value === 'member' ? 'admin' : 'member'
}

// ====== 会员登录 ======
const handleMemberLogin = async () => {
  if (!loginForm.value.phone || !loginForm.value.password) {
    return ElMessage.warning('请输入手机号和密码')
  }

  memberSubmitting.value = true
  try {
    const res = await loginByPassword(loginForm.value.phone, loginForm.value.password)
    const role = String(res.data.user.role || '').toLowerCase()

    if (role !== 'member') {
      ElMessage.error('该账号不是会员账号，请使用管理员入口登录')
      return
    }

    ElMessage.success(`会员登录成功：${res.data.user.username}`)
    await navigateTo('/member', { replace: true })
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    memberSubmitting.value = false
  }
}

// ====== 会员注册 ======
const handleRegister = async () => {
  const { phone, smsCode, password, companyName } = registerForm.value

  if (!phone) return ElMessage.warning('请输入手机号')
  if (!smsCode) return ElMessage.warning('请输入验证码')
  if (!password) return ElMessage.warning('请设置密码')
  if (password.length < 6) return ElMessage.warning('密码长度不能少于6位')
  if (!companyName) return ElMessage.warning('请输入公司名称')

  registerSubmitting.value = true
  try {
    const res: any = await apiRequest('/v3/auth/register', {
      method: 'POST',
      body: {
        phone: registerForm.value.phone,
        password: registerForm.value.password,
        sms_code: registerForm.value.smsCode,
        company_name: registerForm.value.companyName,
        invitation_code: registerForm.value.invitationCode || ''
      }
    })

    if (!res.data?.token || !res.data?.user) {
      throw new Error('注册响应异常')
    }

    // 注册即登录：保存 token + user
    setAuth(res.data.token, res.data.user)
    ElMessage.success('注册成功，欢迎加入 XRIPP！')
    await navigateTo('/member', { replace: true })
  } catch (e: any) {
    ElMessage.error(e?.message || '注册失败，请稍后重试')
  } finally {
    registerSubmitting.value = false
  }
}

// ====== 发送验证码（开发期模拟） ======
const handleSendSms = () => {
  if (!registerForm.value.phone) {
    return ElMessage.warning('请先输入手机号')
  }
  if (registerForm.value.phone.length < 6) {
    return ElMessage.warning('请输入正确的手机号')
  }

  ElMessage.success('验证码已发送（测试环境请输入 123456）')
  smsCountdown.value = 60
  if (smsTimer) clearInterval(smsTimer)
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      if (smsTimer) { clearInterval(smsTimer); smsTimer = null }
    }
  }, 1000)
}

// ====== 管理员登录 ======
const handleAdminLogin = async () => {
  if (!adminForm.value.username || !adminForm.value.password) {
    return ElMessage.warning('请输入账号和密码')
  }

  adminSubmitting.value = true
  try {
    const res = await loginByPassword(adminForm.value.username, adminForm.value.password)
    ElMessage.success(`登录成功：${res.data.user.username}`)

    const role = String(res.data.user.role || '').toLowerCase()

    const redirectQuery = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const safeAdminRedirect =
      redirectQuery.startsWith('/admin') && !redirectQuery.startsWith('//')
        ? redirectQuery
        : '/admin'

    let target = '/login'
    if (role === 'admin') target = safeAdminRedirect
    else if (role === 'partner') target = '/admin/partner-publish'
    else if (role === 'member') target = '/member'
    else {
      ElMessage.error('账号角色无效，请联系管理员')
      return
    }

    await navigateTo(target, { replace: true })
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    adminSubmitting.value = false
  }
}

// 如果 URL 带 mode=register，自动切换到注册
if (route.query.mode === 'register') {
  memberMode.value = 'register'
}
</script>

<style scoped>
:deep(.custom-input .el-input__wrapper) {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #0284c7;
  box-shadow: 0 0 0 3px rgba(2, 132, 199, 0.1);
}

@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}
.animate-fade-in-right {
  animation: fadeInRight 0.3s ease-out;
}

@keyframes breathe-zoom {
  0% { transform: scale(1); }
  100% { transform: scale(1.15); }
}
.animate-breathe-zoom {
  animation: breathe-zoom 15s ease-in-out infinite alternate;
}
</style>
