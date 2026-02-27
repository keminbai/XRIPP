<!-- 文件路径: app/pages/member/feedback.vue -->
<template>
  <div class="space-y-6">
    
    <!-- 顶部：客服概览 -->
    <div class="bg-gradient-to-r from-teal-800 to-emerald-900 rounded-2xl p-8 text-white shadow-lg flex flex-col md:flex-row items-center justify-between gap-6">
      <div>
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 text-teal-100 text-xs font-bold mb-4">
          <el-icon><Headset /></el-icon> SERVICE CENTER
        </div>
        <h1 class="text-3xl font-bold mb-2">客户服务与工单系统</h1>
        <p class="text-teal-100 text-sm max-w-xl leading-relaxed">
          遇到问题？提交工单，我们的专业客服团队将在 2 小时内响应。
          <br>VIP/SVIP 会员享有优先处理通道。
        </p>
      </div>
      <div class="flex gap-4">
        <div class="text-center bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10">
          <div class="text-2xl font-bold font-mono">400-888-8888</div>
          <div class="text-xs text-teal-200 mt-1">24小时热线</div>
        </div>
        <div class="text-center bg-white/10 backdrop-blur-sm p-4 rounded-xl border border-white/10">
          <div class="text-2xl font-bold font-mono">service@xripp.com</div>
          <div class="text-xs text-teal-200 mt-1">官方邮箱</div>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <!-- 左侧：工单列表 -->
      <div class="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <div class="flex justify-between items-center mb-6">
          <h3 class="font-bold text-slate-900 text-lg flex items-center gap-2">
            <span class="w-1 h-5 bg-teal-600 rounded-full"></span> 我的工单
          </h3>
          <el-button type="primary" @click="dialogVisible = true">
            <el-icon class="mr-1"><EditPen /></el-icon> 提交新工单
          </el-button>
        </div>

        <el-table :data="tickets" style="width: 100%" :header-cell-style="{background:'#f8fafc'}">
          <el-table-column prop="id" label="工单编号" width="140" font-family="mono" />
          <el-table-column prop="title" label="问题标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="100">
            <template #default="scope">
              <el-tag size="small" effect="plain" :type="getTypeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <span class="flex items-center gap-1.5 text-xs font-bold" :class="getStatusColor(scope.row.status)">
                <span class="w-1.5 h-1.5 rounded-full" :class="getStatusBg(scope.row.status)"></span>
                {{ scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="160" text-align="right" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default>
              <el-button link type="primary" size="small">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 空状态 -->
        <div v-if="tickets.length === 0" class="text-center py-12 text-slate-400">
          <el-icon class="text-4xl mb-2"><Document /></el-icon>
          <p>暂无工单记录</p>
        </div>
      </div>

      <!-- 右侧：常见问题 -->
      <div class="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
        <h3 class="font-bold text-slate-900 text-lg mb-6 flex items-center gap-2">
          <el-icon class="text-teal-600"><QuestionFilled /></el-icon> 常见问题 (FAQ)
        </h3>
        
        <div class="space-y-4">
          <div v-for="(faq, i) in faqs" :key="i" class="p-4 bg-slate-50 rounded-xl border border-slate-100 hover:bg-teal-50 hover:border-teal-100 transition-colors cursor-pointer group">
            <div class="text-sm font-bold text-slate-800 mb-2 group-hover:text-teal-700">{{ faq.q }}</div>
            <div class="text-xs text-slate-500 leading-relaxed">{{ faq.a }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提交工单弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交新工单" width="600px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="问题分类" required>
            <el-select v-model="form.type" placeholder="请选择" class="w-full">
              <el-option label="财务/发票" value="财务" />
              <el-option label="账号/登录" value="账号" />
              <el-option label="标书下载问题" value="标书" />
              <el-option label="活动报名" value="活动" />
              <el-option label="其他建议" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="form.priority" placeholder="请选择" class="w-full">
              <el-option label="一般" value="normal" />
              <el-option label="紧急 (影响业务)" value="high" />
              <el-option label="非常紧急 (无法使用)" value="urgent" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="工单标题" required>
          <el-input v-model="form.title" placeholder="请简要描述遇到的问题" />
        </el-form-item>

        <el-form-item label="详细描述" required>
          <el-input 
            v-model="form.desc" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述问题发生的场景、时间以及您的诉求..." 
          />
        </el-form-item>

        <el-form-item label="截图上传">
          <el-upload action="#" list-type="picture-card" :auto-upload="false" :limit="3">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交工单</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Headset, EditPen, Document, Plus, QuestionFilled 
} from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'member' })

const dialogVisible = ref(false)
const submitting = ref(false)

const form = ref({
  type: '',
  priority: 'normal',
  title: '',
  desc: ''
})

const tickets = ref([
  { id: 'TK260118001', title: 'SVIP会员发票申请开具', type: '财务', status: '处理中', updateTime: '2026-01-18 14:20' },
  { id: 'TK260115009', title: '标书文件下载速度过慢', type: '标书', status: '已解决', updateTime: '2026-01-15 09:10' },
  { id: 'TK260112033', title: '子账号无法登录提示密码错误', type: '账号', status: '已关闭', updateTime: '2026-01-12 16:45' },
])

const faqs = [
  { q: '如何申请发票？', a: '在"订单管理"中找到已支付订单，点击"申请发票"，填写抬头信息即可，电子发票将在24小时内发送至邮箱。' },
  { q: '标书下载次数用完了怎么办？', a: '您可以联系您的专属合伙人申请额外额度，或者直接在线购买标书增值包。' },
  { q: '子账号有什么权限？', a: '子账号默认拥有与主账号相同的标书查看权限，但无法进行支付和账号设置操作。' },
  { q: '如何修改企业认证信息？', a: '请前往"账号设置" -> "企业资料"，修改后需等待后台人工审核，约1个工作日。' },
]

const getTypeTag = (type: string) => {
  const map: Record<string, string> = { '财务': 'success', '标书': 'primary', '账号': 'warning', '活动': 'danger' }
  return map[type] || 'info'
}

const getStatusColor = (status: string) => {
  if (status === '处理中') return 'text-blue-600'
  if (status === '已解决') return 'text-green-600'
  return 'text-slate-400'
}

const getStatusBg = (status: string) => {
  if (status === '处理中') return 'bg-blue-600'
  if (status === '已解决') return 'bg-green-600'
  return 'bg-slate-400'
}

const handleSubmit = () => {
  if (!form.value.title || !form.value.type) {
    return ElMessage.warning('请补全必要信息')
  }
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    dialogVisible.value = false
    ElMessage.success('工单提交成功！编号：TK' + Date.now().toString().slice(-8))
    // 模拟插入
    tickets.value.unshift({
      id: 'TK' + Date.now().toString().slice(-8),
      title: form.value.title,
      type: form.value.type,
      status: '待处理',
      updateTime: '刚刚'
    })
    form.value = { type: '', priority: 'normal', title: '', desc: '' }
  }, 1000)
}
</script>