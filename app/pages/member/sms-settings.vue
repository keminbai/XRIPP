<!-- 文件路径: app/pages/member/sms-settings.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 顶部 Hero：短信推送 -->
    <div class="relative bg-gradient-to-r from-cyan-900 to-slate-900 rounded-2xl p-8 overflow-hidden text-white shadow-lg">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-cyan-500/10 skew-x-12"></div>
      <div class="absolute right-10 top-1/2 -translate-y-1/2 w-32 h-32 bg-blue-500/20 rounded-full blur-3xl"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-500/30 border border-cyan-400/30 text-cyan-200 text-xs font-bold mb-4">
            <el-icon><Bell /></el-icon> INTELLIGENT ALERT
          </div>
          <h1 class="text-3xl font-bold mb-2">短信精准推送设置</h1>
          <p class="text-cyan-100 text-sm max-w-xl leading-relaxed">
            不错过任何商机。设置关键词，系统自动匹配并发送短信通知。
            <br>权益消耗：<span class="font-bold text-white">当前等级 {{ currentLevel }}（可用 {{ quotaTotal }} 次）</span>。
          </p>
        </div>
        <div class="flex-shrink-0 bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/10">
          <el-icon class="text-4xl text-cyan-300"><Message /></el-icon>
        </div>
      </div>
    </div>

    <!-- 额度显示卡 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 rounded-lg bg-green-50 text-green-600 flex items-center justify-center">
            <el-icon class="text-2xl"><CircleCheck /></el-icon>
          </div>
          <div>
            <div class="text-xs text-slate-500">剩余推送次数</div>
            <div class="text-2xl font-bold text-slate-900">{{ quotaRemaining }}</div>
          </div>
        </div>
        <div class="text-xs text-slate-400">总额度: {{ quotaTotal }} 次</div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
            <el-icon class="text-2xl"><Message /></el-icon>
          </div>
          <div>
            <div class="text-xs text-slate-500">本月已推送</div>
            <div class="text-2xl font-bold text-slate-900">{{ quotaUsed }}</div>
          </div>
        </div>
        <div class="text-xs text-slate-400">较上月 +12 次</div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 rounded-lg bg-purple-50 text-purple-600 flex items-center justify-center">
            <el-icon class="text-2xl"><Key /></el-icon>
          </div>
          <div>
            <div class="text-xs text-slate-500">已设置关键词</div>
            <div class="text-2xl font-bold text-slate-900">{{ keywords.length }}/5</div>
          </div>
        </div>
        <div class="text-xs text-slate-400">最多设置5个</div>
      </div>
    </div>

    <!-- 关键词设置 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-8 md:p-12">
      
      <div class="max-w-4xl mx-auto">
        <h3 class="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
          <span class="w-1 h-6 bg-brand-600 rounded-full"></span>
          关键词管理
        </h3>

        <!-- 已设置的关键词列表 -->
        <div v-if="keywords.length > 0" class="mb-8">
          <div class="flex flex-wrap gap-3">
            <div 
              v-for="(keyword, index) in keywords" 
              :key="index"
              class="group relative flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-lg"
            >
              <el-icon class="text-blue-500"><PriceTag /></el-icon>
              <span class="font-bold text-slate-900">{{ keyword }}</span>
              <button 
                class="ml-2 text-slate-400 hover:text-red-500 transition-colors"
                @click="handleRemoveKeyword(index)"
              >
                <el-icon><Close /></el-icon>
              </button>
            </div>
          </div>
        </div>

        <!-- 添加新关键词 -->
        <div v-if="keywords.length < 5" class="space-y-6">
          <el-form>
            <el-form-item label="添加新关键词">
              <div class="flex gap-3">
                <el-input 
                  v-model="newKeyword" 
                  placeholder="输入关键词（5字以内，如：拖拉机、医疗设备）"
                  maxlength="5"
                  show-word-limit
                  @keyup.enter="handleAddKeyword"
                  class="flex-grow"
                />
                <el-button 
                  type="primary" 
                  @click="handleAddKeyword"
                  :disabled="!newKeyword || newKeyword.length > 5"
                >
                  <el-icon><Plus /></el-icon> 添加
                </el-button>
              </div>
            </el-form-item>
          </el-form>

          <!-- 推荐关键词 -->
          <div>
            <div class="text-sm font-bold text-slate-700 mb-3">💡 热门推荐</div>
            <div class="flex flex-wrap gap-2">
              <button 
                v-for="recommend in recommendKeywords" 
                :key="recommend"
                class="px-3 py-1.5 text-sm text-slate-600 bg-slate-50 hover:bg-brand-50 hover:text-brand-600 border border-slate-200 hover:border-brand-200 rounded-lg transition-colors"
                @click="newKeyword = recommend"
              >
                {{ recommend }}
              </button>
            </div>
          </div>
        </div>

        <!-- 满额提示 -->
        <div v-else class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-start gap-3">
          <el-icon class="text-yellow-600 text-xl mt-0.5"><Warning /></el-icon>
          <div class="text-sm text-yellow-800">
            您已设置了5个关键词（上限）。如需添加新关键词，请先删除现有的。
          </div>
        </div>

        <!-- 接收手机号 -->
        <div class="mt-10 pt-8 border-t border-slate-200">
          <h3 class="text-lg font-bold text-slate-900 mb-4">接收手机号</h3>
          <el-form>
            <el-form-item>
              <div class="flex gap-3 items-center">
                <el-input 
                  v-model="phoneNumber" 
                  placeholder="请输入手机号"
                  class="w-64"
                  disabled
                >
                  <template #prefix><el-icon><Iphone /></el-icon></template>
                </el-input>
                <el-button link type="primary">修改</el-button>
              </div>
              <div class="text-xs text-slate-400 mt-2">
                默认为您的注册手机号，如需修改请联系客服
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- 推送规则说明 -->
        <div class="mt-8 bg-slate-50 rounded-xl p-6 border border-slate-200">
          <h4 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
            <el-icon class="text-brand-600"><InfoFilled /></el-icon>
            推送规则说明
          </h4>
          <ul class="space-y-2 text-sm text-slate-600">
            <li class="flex items-start gap-2">
              <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
              <span>系统每小时检测一次新发布的标书，匹配您的关键词后立即推送</span>
            </li>
            <li class="flex items-start gap-2">
              <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
              <span>推送内容包括：标书标题、采购方、截止日期、查看链接</span>
            </li>
            <li class="flex items-start gap-2">
              <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
              <span>每条标书仅推送一次，避免重复打扰</span>
            </li>
            <li class="flex items-start gap-2">
              <el-icon class="text-brand-500 mt-0.5"><Right /></el-icon>
              <span>额度用完后可联系客服购买增值包（¥100/50次）</span>
            </li>
          </ul>
        </div>

        <!-- 保存按钮 -->
        <div class="flex justify-between items-center mt-8 pt-6 border-t border-slate-200">
          <el-button @click="handleReset" class="px-8">
            <el-icon class="mr-2"><RefreshLeft /></el-icon> 重置
          </el-button>
          <el-button 
            type="primary"
            @click="handleSave"
            :loading="saving"
            class="px-12"
          >
            <el-icon class="mr-2"><CircleCheck /></el-icon> 保存设置
          </el-button>
        </div>

      </div>
    </div>

    <!-- 推送历史记录 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
      <h3 class="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
        <el-icon class="text-brand-600"><Clock /></el-icon>
        最近推送记录
      </h3>

      <div class="space-y-4">
        <div v-for="(record, i) in pushHistory" :key="i" class="flex items-start gap-4 p-4 bg-slate-50 rounded-lg border border-slate-100 hover:border-brand-200 transition-colors">
          <div class="flex-shrink-0 w-10 h-10 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center text-xs font-bold">
            {{ record.date.split('-')[2] }}日
          </div>
          <div class="flex-grow">
            <div class="font-bold text-slate-900 mb-1">{{ record.title }}</div>
            <div class="flex items-center gap-4 text-xs text-slate-500">
              <span>关键词: {{ record.keyword }}</span>
              <span>推送时间: {{ record.date }} {{ record.time }}</span>
            </div>
          </div>
          <el-button link type="primary" size="small" @click="navigateTo('/procurement/A001')">
            查看详情
          </el-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { 
  Bell, CircleCheck, Message, Key, PriceTag, Close, Plus, Warning,
  Iphone, InfoFilled, Right, RefreshLeft, Clock
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMemberBenefitModel } from '@/composables/useMemberBenefitModel'

definePageMeta({ layout: 'member' })

const { currentLevel, rightsByType, usageByType, remainingByType } = useMemberBenefitModel()

const quotaTotal = computed(() => rightsByType.value.sms_quota || 0)
const quotaUsed = computed(() => usageByType.value.sms_quota || 0)
const quotaRemaining = computed(() => remainingByType.value.sms_quota || 0)

const keywords = ref(['医疗设备', '光伏能源', '教育平板'])
const newKeyword = ref('')
const phoneNumber = ref('138****5678')
const saving = ref(false)

const recommendKeywords = ['拖拉机', '冷链物流', '净水设备', '建筑材料', '办公家具']

const pushHistory = [
  {
    date: '2026-01-15',
    time: '10:30',
    title: '肯尼亚教育部 - 教育平板采购项目',
    keyword: '教育平板'
  },
  {
    date: '2026-01-14',
    time: '14:15',
    title: '马来西亚能源部 - 光伏发电站二期',
    keyword: '光伏能源'
  },
  {
    date: '2026-01-13',
    time: '09:20',
    title: '苏丹卫生部 - 医疗设备紧急采购',
    keyword: '医疗设备'
  }
]

const handleAddKeyword = () => {
  if (!newKeyword.value) {
    ElMessage.warning('请输入关键词')
    return
  }
  if (newKeyword.value.length > 5) {
    ElMessage.warning('关键词不能超过5个字')
    return
  }
  if (keywords.value.includes(newKeyword.value)) {
    ElMessage.warning('该关键词已存在')
    return
  }
  if (keywords.value.length >= 5) {
    ElMessage.warning('最多只能设置5个关键词')
    return
  }

  keywords.value.push(newKeyword.value)
  newKeyword.value = ''
  ElMessage.success('关键词添加成功')
}

const handleRemoveKeyword = (index) => {
  ElMessageBox.confirm(
    `确认删除关键词"${keywords.value[index]}"吗？`,
    '提示',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    keywords.value.splice(index, 1)
    ElMessage.success('已删除')
  })
}

const handleReset = () => {
  ElMessageBox.confirm('确认重置所有关键词吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    keywords.value = []
    ElMessage.success('已重置')
  })
}

const handleSave = () => {
  if (keywords.value.length === 0) {
    ElMessage.warning('请至少设置1个关键词')
    return
  }

  saving.value = true
  setTimeout(() => {
    saving.value = false
    ElMessage.success('设置已保存！系统将开始为您推送匹配的标书')
  }, 1000)
}
</script>