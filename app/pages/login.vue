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
            {{ loginRole === 'member' ? '欢迎回来，会员' : '管理后台登录' }}
          </h2>
          <p class="text-slate-500">
            {{ loginRole === 'member' ? '请选择您习惯的方式登录' : '请输入管理账号和密码' }}
          </p>
        </div>

        <div v-if="loginRole === 'member'" class="space-y-6 animate-fade-in-right">
          <div class="flex border-b border-slate-200 mb-6">
            <button
              class="pb-3 px-4 text-sm font-bold transition-all border-b-2"
              :class="memberTab === 'phone' ? 'text-brand-600 border-brand-600' : 'text-slate-400 border-transparent hover:text-slate-600'"
              @click="memberTab = 'phone'"
            >
              手机验证码
            </button>
            <button
              class="pb-3 px-4 text-sm font-bold transition-all border-b-2"
              :class="memberTab === 'wechat' ? 'text-brand-600 border-brand-600' : 'text-slate-400 border-transparent hover:text-slate-600'"
              @click="memberTab = 'wechat'"
            >
              微信扫码
            </button>
          </div>

          <div v-if="memberTab === 'phone'" class="space-y-5">
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">手机号</label>
              <el-input v-model="memberForm.phone" size="large" placeholder="请输入手机号" class="custom-input h-12">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </div>
            <div class="space-y-2">
              <label class="text-sm font-medium text-slate-700">验证码</label>
              <div class="flex gap-4">
                <el-input v-model="memberForm.code" size="large" placeholder="6位验证码" class="custom-input h-12 flex-grow" />
                <button class="px-6 h-12 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-lg transition-colors whitespace-nowrap">
                  获取验证码
                </button>
              </div>
            </div>
            <button
              class="w-full py-4 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-lg transition-all text-base mt-4"
              @click="handleMemberLogin"
            >
              立即登录 / 注册
            </button>
          </div>

          <div v-else class="py-4 text-center">
            <div class="w-48 h-48 mx-auto bg-slate-50 border-2 border-slate-200 rounded-2xl flex items-center justify-center mb-4">
              <el-icon class="text-6xl text-slate-300"><ChatDotRound /></el-icon>
            </div>
            <p class="text-sm text-slate-500">请使用微信扫一扫登录</p>
          </div>
        </div>

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
            :disabled="adminLoading"
            @click="handleAdminLogin"
          >
            {{ adminLoading ? '登录中...' : '安全登录' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Lock, Iphone, ChatDotRound, Switch, Warning } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginByPassword } from '~/utils/request'

definePageMeta({ layout: false })

const route = useRoute()
const loginRole = ref<'member' | 'admin'>('member')
const memberTab = ref<'phone' | 'wechat'>('phone')

const memberForm = ref({
  phone: '',
  code: ''
})

const adminForm = ref({
  username: '',
  password: ''
})

const adminLoading = ref(false)

const toggleRole = () => {
  loginRole.value = loginRole.value === 'member' ? 'admin' : 'member'
}

const handleMemberLogin = async () => {
  if (!memberForm.value.phone || !memberForm.value.code) {
    ElMessage.warning('请输入会员账号和密码')
    return
  }

  adminLoading.value = true
  try {
    const res = await loginByPassword(memberForm.value.phone, memberForm.value.code)
    const role = String(res.data.user.role || '').toLowerCase()

    if (role !== 'member') {
      ElMessage.error('该账号不是会员账号，请使用管理员入口登录')
      return
    }

    ElMessage.success(`会员登录成功：${res.data.user.username}`)
    await navigateTo('/member', { replace: true })
  } catch (e: any) {
    ElMessage.error(e?.message || '会员登录失败')
  } finally {
    adminLoading.value = false
  }
}

const handleAdminLogin = async () => {
  if (!adminForm.value.username || !adminForm.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  adminLoading.value = true
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
    adminLoading.value = false
  }
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
