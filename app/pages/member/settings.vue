<!-- 文件路径: D:\ipp-platform\app\pages\member\settings.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 1. 个人资料卡 (核心业务信息补全) -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
      <h3 class="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
        <span class="w-1 h-5 bg-brand-600 rounded-full"></span> 
        基本信息 <span class="text-xs text-red-500 font-normal ml-2">* 请完善企业信息以激活会员权益</span>
      </h3>
      
      <div class="flex flex-col md:flex-row gap-8">
        <!-- 头像上传 (保留原有的好看样式) -->
        <div class="flex-shrink-0 text-center">
          <div class="w-24 h-24 rounded-full border-4 border-slate-50 overflow-hidden mb-3 mx-auto relative group cursor-pointer shadow-sm">
            <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <span class="text-white text-xs font-bold">更换</span>
            </div>
          </div>
          <p class="text-xs text-slate-400">支持 JPG/PNG</p>
        </div>

        <!-- 表单区域 (根据务注深度升级) -->
        <div class="flex-grow max-w-3xl">
          <el-form :model="form" label-position="top" size="large">
            
            <!-- 第一行：姓名/电话 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <el-form-item label="联系人姓名 (Contact Name)" required>
                <el-input v-model="form.name" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="联系电话 (Phone)" required>
                <el-input v-model="form.phone" disabled placeholder="138****5678">
                  <template #append><span class="text-green-600">已验证</span></template>
                </el-input>
              </el-form-item>
            </div>
            
            <!-- 第二行：公司名称 -->
            <el-form-item label="公司全称 (Company Name)" required>
              <el-input v-model="form.company" placeholder="请输入公司全称 (需与营业执照一致)" />
            </el-form-item>
            
            <!-- 第三行：行业/城市 (务注核心字段) -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <el-form-item label="行业类别 (Industry)" required>
                <el-select v-model="form.industry" placeholder="请选择主营行业" class="w-full">
                  <el-option label="1. 制造业" value="manufacturing" />
                  <el-option label="2. 贸易类" value="trade" />
                  <el-option label="3. 服务业" value="service" />
                  <el-option label="4. 大宗贸易" value="bulk" />
                  <el-option label="5. 建筑业" value="construction" />
                  <el-option label="6. 科技型企业" value="tech" />
                  <el-option label="7. 其他类别" value="other" />
                </el-select>
              </el-form-item>

              <el-form-item label="所在城市 (City)" required>
                <el-select v-model="form.city" placeholder="请选择城市" class="w-full">
                  <el-option label="四川 - 绵阳" value="mianyang" />
                  <el-option label="上海" value="shanghai" />
                  <el-option label="北京" value="beijing" />
                  <el-option label="深圳" value="shenzhen" />
                  <el-option label="成都" value="chengdu" />
                </el-select>
              </el-form-item>
            </div>

            <!-- 第四行：邀请码 (数据隔离核心) -->
            <el-form-item label="合伙人邀请码 (Invitation Code)">
              <el-input v-model="form.inviteCode" placeholder="请输入合伙人提供的邀请码 (选填)">
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
              <div class="text-xs text-slate-400 mt-1">填写邀请码将自动绑定至合伙人名下，并获得额外权益支持</div>
            </el-form-item>

            <div class="pt-4">
              <el-button type="primary" size="large" class="px-8 !rounded-lg" @click="handleSave">保存并更新权益</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 2. 账号安全 (保留原有的功能) -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
      <h3 class="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
        <span class="w-1 h-5 bg-orange-500 rounded-full"></span> 账号安全
      </h3>
      
      <div class="space-y-6">
        <div class="flex items-center justify-between py-4 border-b border-slate-100">
          <div>
            <div class="font-bold text-slate-800">登录密码</div>
            <div class="text-xs text-slate-400 mt-1">建议定期修改密码以保护账号安全</div>
          </div>
          <el-button link type="primary">修改</el-button>
        </div>
        
        <div class="flex items-center justify-between py-4 border-b border-slate-100">
          <div>
            <div class="font-bold text-slate-800">微信绑定</div>
            <div class="text-xs text-slate-400 mt-1">已绑定微信号：XRIPP_User_007</div>
          </div>
          <el-button link type="danger">解绑</el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Key } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest } from '@/utils/request'

definePageMeta({ layout: 'member' })

const form = ref({
  name: '',
  phone: '',
  company: '',
  industry: '',
  city: '',
  inviteCode: ''
})

const loading = ref(false)

async function loadProfile() {
  try {
    const res = await apiRequest<any>('/v3/member/profile')
    const p = res.data || {}
    form.value.name = p.contactPerson || ''
    form.value.company = p.companyName || ''
    form.value.phone = p.contactPhone || ''
    form.value.industry = p.industry || ''
    form.value.city = p.city || ''
    form.value.inviteCode = p.invitationCode || ''
  } catch {
    // keep empty form
  }
}

onMounted(loadProfile)

const handleSave = async () => {
  if (!form.value.name || !form.value.company || !form.value.industry) {
    return ElMessage.warning('请补全所有带 * 的必填信息')
  }
  loading.value = true
  try {
    await apiRequest('/v3/member/profile', {
      method: 'PUT',
      body: {
        contact_person: form.value.name,
        company_name: form.value.company,
        industry: form.value.industry,
        contact_phone: form.value.phone
      }
    })
    ElMessage.success('资料保存成功！权益已更新')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>