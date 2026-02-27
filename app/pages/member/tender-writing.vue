<!-- 文件路径: app/pages/member/tender-writing.vue -->
<template>
  <div class="space-y-8">

    <!-- 顶部 Hero -->
    <div class="relative bg-gradient-to-r from-indigo-900 to-slate-900 rounded-2xl p-8 overflow-hidden text-white shadow-lg">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-indigo-500/10 skew-x-12"></div>
      <div class="absolute right-10 top-1/2 -translate-y-1/2 w-32 h-32 bg-purple-500/20 rounded-full blur-3xl"></div>

      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/30 border border-indigo-400/30 text-indigo-200 text-xs font-bold mb-4">
            <el-icon><Star /></el-icon> PREMIUM SERVICE
          </div>
          <h1 class="text-3xl font-bold mb-2">全程托管式标书定制服务</h1>
          <p class="text-indigo-100 text-sm max-w-xl leading-relaxed">
            10年团队精通国内外标书规则，重点深耕联合国采购体系，<strong>中标率提升40%+</strong>
            <br><span class="text-yellow-400">Domestic / International Tender Document Preparation</span>
          </p>
        </div>
        <div class="flex-shrink-0 bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/10">
          <el-icon class="text-4xl text-indigo-300"><Edit /></el-icon>
        </div>
      </div>
    </div>

    <!-- 服务类型 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

      <!-- ══════════ 国际标书（左） ══════════ -->
      <div class="bg-white rounded-2xl border-2 border-brand-200 shadow-lg overflow-hidden hover:shadow-2xl transition-all group relative">
        <div class="absolute -top-1 -right-1 bg-yellow-400 text-yellow-900 text-xs font-bold px-3 py-1 rounded-bl-lg shadow-md z-10">
          热门推荐
        </div>
        <div class="h-2 bg-gradient-to-r from-brand-500 to-brand-600"></div>
        <div class="p-8">
          <!-- 标题 -->
          <div class="flex items-center gap-3 mb-6">
            <div class="w-12 h-12 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center text-2xl flex-shrink-0">
              🌍
            </div>
            <div>
              <h3 class="text-xl font-bold text-slate-900">国际标书定制</h3>
              <p class="text-xs text-slate-500">International Tender Document Preparation</p>
            </div>
          </div>

          <div class="mb-6">
            <div class="inline-flex gap-2 mb-4 flex-wrap">
              <span class="px-3 py-1 bg-brand-50 text-brand-700 text-xs font-bold rounded-full border border-brand-100">货物类 Goods</span>
              <span class="px-3 py-1 bg-purple-50 text-purple-700 text-xs font-bold rounded-full border border-purple-100">服务类 Services</span>
            </div>

            <!-- 货物类标书 -->
            <div class="mb-5">
              <div class="font-bold text-sm text-slate-900 mb-3 flex items-center gap-2">
                <span class="w-6 h-6 bg-brand-100 text-brand-700 rounded-full flex items-center justify-center text-xs">1</span>
                货物类标书 (Goods Tender)
              </div>
              <div class="space-y-3 ml-8 text-xs text-slate-600">
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Company Information & Compliance Documents</strong><br>
                    公司基本信息与合规文件：企业资质、Supplier Sustainability Questionnaire、H&S 合规文件、UN Supplier Code of Conduct
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Technical Specifications & TOR</strong><br>
                    技术规格书与任务大纲：技术参数、性能验证、Schedule of Requirements、Technical Annexes
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Pricing & Commercial Proposal</strong><br>
                    报价与商务方案：BOQ Schedule、Financial Offer Form、Payment Terms、总价构成
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Logistics & Delivery Plan</strong><br>
                    物流方案与交付保障：运输方式、交付时效、仓储方案、风险应对
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Bid Response Documents</strong><br>
                    投标响应文件：Compliance Matrix、Returnable Bidding Forms、Response to Instructions to Bidders
                  </span>
                </div>
              </div>
            </div>

            <!-- 服务类标书 -->
            <div>
              <div class="font-bold text-sm text-slate-900 mb-3 flex items-center gap-2">
                <span class="w-6 h-6 bg-purple-100 text-purple-700 rounded-full flex items-center justify-center text-xs">2</span>
                服务类标书 (Services Tender)
              </div>
              <div class="space-y-3 ml-8 text-xs text-slate-600">
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Company Information & Qualification Proof</strong><br>
                    公司基本信息与资质证明：企业资质、Past Performance、H&S 资质、可持续性认证
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Implementation Methodology & TOR</strong><br>
                    实施方法论与任务大纲：服务流程、技术方案、问题应对机制、需求匹配说明
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Project Team Profile</strong><br>
                    项目团队组成：核心成员履历、资质证明、分工说明
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Service Quotation & Cost Breakdown</strong><br>
                    服务报价与费用明细：分项报价、附加费用说明、优惠方案、付款周期
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Bid Response & Contract Documents</strong><br>
                    投标响应与合同文件：Returnable Bidding Forms、Contract Form、履约保障方案
                  </span>
                </div>
                <div class="flex items-start gap-2">
                  <span class="flex-shrink-0">•</span>
                  <span>
                    <strong>Compliance & Risk Management</strong><br>
                    合规与风险管理：逐条符合性匹配、H&S 管理方案、可持续性承诺
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 报价 -->
          <div class="bg-gradient-to-br from-brand-50 to-purple-50 rounded-xl p-5 mb-6 border border-brand-100">
            <div class="text-xs text-slate-500 mb-2">参考报价</div>
            <div class="flex items-baseline gap-2 mb-1">
              <span class="text-3xl font-bold text-brand-600">¥8,000</span>
              <span class="text-sm text-slate-500">起</span>
            </div>
            <div class="text-xs text-slate-400 mt-2">
              交付时间：<strong>5-15个工作日</strong>（根据项目复杂度）
            </div>
          </div>

          <button class="w-full py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-lg transition-colors shadow-lg flex items-center justify-center gap-2">
            <el-icon><ChatDotRound /></el-icon>
            立即咨询
          </button>
        </div>
      </div>

      <!-- ══════════ 国内标书（右） ══════════ -->
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden hover:shadow-lg transition-all group">
        <div class="h-2 bg-gradient-to-r from-blue-500 to-blue-600"></div>
        <div class="p-8">
          <!-- 标题 -->
          <div class="flex items-center gap-3 mb-6">
            <div class="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center text-2xl flex-shrink-0">📄</div>
            <div>
              <h3 class="text-xl font-bold text-slate-900">中文标书定制</h3>
              <p class="text-xs text-slate-500">Domestic Tender Document Preparation</p>
            </div>
          </div>

          <!-- 服务清单 -->
          <div class="space-y-3 mb-8">
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <el-icon class="text-blue-500 flex-shrink-0"><CircleCheck /></el-icon>
              <span>政府采购项目投标文件</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <el-icon class="text-blue-500 flex-shrink-0"><CircleCheck /></el-icon>
              <span>企业招标响应文件</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <el-icon class="text-blue-500 flex-shrink-0"><CircleCheck /></el-icon>
              <span>技术方案与商务报价</span>
            </div>
            <div class="flex items-center gap-3 text-sm text-slate-600">
              <el-icon class="text-blue-500 flex-shrink-0"><CircleCheck /></el-icon>
              <span>投标文件排版装订</span>
            </div>
          </div>

          <!-- 报价 -->
          <div class="bg-slate-50 rounded-xl p-5 mb-6 border border-slate-100">
            <div class="text-xs text-slate-500 mb-2">参考报价</div>
            <div class="flex items-baseline gap-2 mb-1">
              <span class="text-3xl font-bold text-slate-900">¥3,000</span>
              <span class="text-sm text-slate-500">起</span>
            </div>
            <div class="text-xs text-slate-400 mt-2">
              交付时间：<strong>5-15个工作日</strong>（根据项目复杂度）
            </div>
          </div>

          <button class="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold rounded-lg transition-colors flex items-center justify-center gap-2">
            <el-icon><ChatDotRound /></el-icon>
            立即咨询
          </button>
        </div>
      </div>
    </div>

    <!-- 服务流程 -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-8 md:p-12">
      <h2 class="text-2xl font-bold text-slate-900 mb-2 text-center">服务流程</h2>
      <p class="text-sm text-slate-500 text-center mb-12">Service Process</p>

      <div class="relative max-w-6xl mx-auto">

        <!-- 桌面端横向连接线 -->
        <div class="hidden md:block absolute top-8 left-0 right-0 h-0.5 bg-gradient-to-r from-brand-200 via-blue-200 to-brand-200"></div>

        <div class="grid grid-cols-1 md:grid-cols-5 gap-12 md:gap-6 relative">

          <div
            v-for="(step, i) in steps"
            :key="i"
            class="relative text-center"
          >

            <!-- 移动端纵向连接线 -->
            <div
              v-if="i !== steps.length - 1"
              class="md:hidden absolute left-1/2 top-16 w-0.5 h-12 bg-slate-200 -translate-x-1/2"
            ></div>

            <!-- 圆形图标 -->
            <div
              class="relative z-10 w-16 h-16 mx-auto mb-4 rounded-full bg-white border-4 border-brand-100 shadow-md flex items-center justify-center text-2xl transition-transform duration-300 hover:scale-110"
            >
              {{ step.icon }}
            </div>

            <!-- 标题 -->
            <div class="font-bold text-sm text-slate-900 mb-1">
              {{ step.title }}
            </div>

            <!-- 描述 -->
            <div class="text-xs text-slate-500 leading-relaxed">
              {{ step.desc }}
            </div>

          </div>

        </div>
      </div>
    </div>


    <!-- 为什么选择我们 -->
    <div class="bg-gradient-to-br from-slate-50 to-blue-50 rounded-2xl p-8 md:p-12 border border-slate-200">
      <h2 class="text-2xl font-bold text-slate-900 mb-2 text-center">为什么选择我们</h2>
      <p class="text-sm text-slate-500 text-center mb-10">Why Choose Us</p>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-10 max-w-6xl mx-auto">

        <div class="text-center group">
          <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-white shadow-md flex items-center justify-center text-4xl transition-all duration-300 group-hover:scale-110 group-hover:shadow-xl">
            🎯
          </div>
          <h3 class="font-bold text-lg text-slate-900 mb-2">专业团队</h3>
          <p class="text-sm text-slate-600 leading-relaxed">
            10年+国际投标经验，熟悉FIDIC、世行、亚开行及联合国采购体系标准
          </p>
        </div>

        <div class="text-center group">
          <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-white shadow-md flex items-center justify-center text-4xl transition-all duration-300 group-hover:scale-110 group-hover:shadow-xl">
            📈
          </div>
          <h3 class="font-bold text-lg text-slate-900 mb-2">高中标率</h3>
          <p class="text-sm text-slate-600 leading-relaxed">
            累计服务800+项目，系统化方法论支持，平均中标率提升40%以上
          </p>
        </div>

        <div class="text-center group">
          <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-white shadow-md flex items-center justify-center text-4xl transition-all duration-300 group-hover:scale-110 group-hover:shadow-xl">
            🔒
          </div>
          <h3 class="font-bold text-lg text-slate-900 mb-2">严格保密</h3>
          <p class="text-sm text-slate-600 leading-relaxed">
            签署严格NDA协议，采用流程化权限管理，确保企业商业信息安全
          </p>
        </div>

      </div>
    </div>


    <!-- CTA区域 -->
    <div class="bg-gradient-to-r from-yellow-400 to-orange-400 rounded-2xl p-12 text-center shadow-lg">
      <h2 class="text-3xl font-bold mb-3 text-slate-900">
        准备好提升您的中标率了吗？
      </h2>
      <p class="text-lg text-slate-700 mb-10">
        立即联系我们的投标专家，获取免费咨询方案
      </p>

      <div class="flex flex-col sm:flex-row gap-4 justify-center">

        <button
          class="px-8 py-4 bg-white text-slate-900 font-bold rounded-lg hover:bg-slate-100 transition-all duration-300 shadow-lg flex items-center justify-center gap-2 hover:scale-105"
        >
          <el-icon><Phone /></el-icon>
          电话咨询：400-888-8888
        </button>

        <button
          class="px-8 py-4 bg-slate-900 hover:bg-slate-800 text-white font-bold rounded-lg transition-all duration-300 border-2 border-white/20 flex items-center justify-center gap-2 hover:scale-105"
        >
          <el-icon><ChatDotRound /></el-icon>
          在线咨询
        </button>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Edit, CircleCheck, Phone, ChatDotRound, Star } from '@element-plus/icons-vue'

definePageMeta({ layout: 'member' })

const steps = [
  { icon: '💬', title: '需求沟通', desc: '30分钟电话沟通' },
  { icon: '📋', title: '方案确认', desc: '1个工作日' },
  { icon: '✍️', title: '专业撰写', desc: '5-15个工作日' },
  { icon: '👀', title: '客户审阅', desc: '2-3个工作日' },
  { icon: '✅', title: '定稿交付', desc: 'Word可编辑版' }
]
</script>

<style scoped>
@media (max-width: 768px) {
  .grid.grid-cols-2 {
    gap: 1rem;
  }
}
</style>