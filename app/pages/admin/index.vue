<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\index.vue
  版本: V2.1 正确修复版 (2026-02-01)
  
  ✅ 修复内容:
  1. [保持不变] 数据驾驶舱: /dashboard (原文件正确，不修改)
  2. [路由修复] 发布新标书: /admin/tenders → /admin/tenders/publish
  3. [路由修复] 财务管理: /admin/finance → /admin/finance/revenue
  4. [路由优化] 查看全部会员: /admin/members → /admin/members/list
  ========================================================================
-->
<template>
  <div class="space-y-8">
    
    <!-- 1. 数据驾驶舱启动入口 -->
    <div class="relative bg-gradient-to-r from-[#0f172a] to-[#1e293b] rounded-xl p-8 text-white shadow-xl overflow-hidden group cursor-pointer" @click="navigateTo('/dashboard')">
      <!-- 动态背景装饰 -->
      <div class="absolute right-0 top-0 h-full w-1/2 bg-brand-500/10 skew-x-12 transform origin-bottom-right transition-transform duration-700 group-hover:scale-110"></div>
      <div class="absolute right-20 top-1/2 -translate-y-1/2 w-32 h-32 bg-blue-500/20 rounded-full blur-3xl group-hover:bg-blue-400/30 transition-colors"></div>
      
      <div class="relative z-10 flex justify-between items-center">
        <div>
          <div class="flex items-center gap-3 mb-2">
            <div class="p-2 bg-blue-500/20 rounded-lg border border-blue-400/30 backdrop-blur-sm">
              <el-icon class="text-2xl text-blue-300 animate-pulse"><DataBoard /></el-icon>
            </div>
            <h2 class="text-2xl font-bold tracking-wide">XRIPP 全球公共采购数据驾驶舱</h2>
          </div>
          <p class="text-blue-200/80 text-sm max-w-2xl pl-1">
            实时监控全球采购动态、会员入驻分布及合伙人业绩大屏。支持双地图自动切换与多维数据钻取。
          </p>
        </div>
        
        <el-button type="primary" size="large" round class="!bg-blue-500 !border-none hover:!bg-blue-400 shadow-lg shadow-blue-500/30 font-bold px-8 py-6 text-base transition-transform group-hover:scale-105">
          立即启动 <el-icon class="ml-2"><Monitor /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 2. 核心数据卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="(stat, i) in stats" :key="i" class="bg-white p-6 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-all hover:-translate-y-1 duration-300 cursor-pointer group">
        <div class="flex justify-between items-start mb-4">
          <div class="p-3 rounded-lg transition-colors" :class="stat.bgClass + ' group-hover:scale-110'">
            <el-icon class="text-xl transition-transform" :class="stat.textClass"><component :is="stat.icon" /></el-icon>
          </div>
          <span class="text-xs font-bold px-2 py-1 rounded bg-slate-50 text-slate-500 group-hover:bg-brand-50 group-hover:text-brand-600 transition-colors">{{ stat.period }}</span>
        </div>
        <div class="text-3xl font-bold text-slate-900 mb-1 group-hover:text-brand-600 transition-colors">{{ stat.value }}</div>
        <div class="text-xs text-slate-500">{{ stat.label }}</div>
      </div>
    </div>
    <div class="h-px bg-slate-100"></div>

    <!-- 3. 数据看板模块 -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-base font-bold text-slate-800">数据看板入口</h3>
        <p class="text-xs text-slate-500 mt-1">进入数据录入与发布模块，统一维护数据口径</p>
      </div>
    </div>
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 公共采购数据 -->
      <div
        class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full"
        @click="navigateTo('/admin/tenders/publish')"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-blue-600"><Document /></el-icon>
            <h3 class="text-base font-bold text-slate-800">公共采购数据</h3>
          </div>
          <el-button type="primary" size="small" plain @click.stop="navigateTo('/admin/tenders/publish')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">联合国采购数据、采购主体分布、采购类目排名</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-blue-600"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 会员数据整理 -->
      <div
        class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full"
        @click="navigateTo('/admin/members/list')"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-green-600"><User /></el-icon>
            <h3 class="text-base font-bold text-slate-800">会员数据整理</h3>
          </div>
          <el-button type="primary" size="small" plain @click.stop="navigateTo('/admin/members/list')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">会员入驻情况、新会员增量、企业类型占比</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-green-600"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 国内/国际服务网点 -->
      <div
        class="bg-white rounded-xl border border-slate-200 shadow-sm p-6 hover:shadow-md transition-all group cursor-pointer h-full"
        @click="navigateTo('/admin/overseas/points')"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <el-icon class="text-purple-600"><Location /></el-icon>
            <h3 class="text-base font-bold text-slate-800">国内/国际服务网点</h3>
          </div>
          <el-button type="warning" size="small" plain @click.stop="navigateTo('/admin/overseas/points')">录入/发布</el-button>
        </div>
        <p class="text-xs text-slate-500 leading-relaxed">服务点城市设置，自动匹配服务点总数与省份数量</p>
        <div class="mt-4 flex items-center justify-between text-xs text-slate-400">
          <span>查看详情</span>
          <el-icon class="text-slate-300 group-hover:text-purple-600"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
    <div class="h-px bg-slate-100"></div>

    <!-- 4. 快捷操作区 -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-base font-bold text-slate-800">快捷操作</h3>
        <p class="text-xs text-slate-500 mt-1">日常高频任务快速入口</p>
      </div>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      
      <!-- 标书发布 -->
      <div 
        class="bg-gradient-to-br from-brand-600 to-brand-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group"
        @click="navigateTo('/admin/tenders/publish')" 
      >
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><CirclePlus /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">发布新标书</h3>
            <p class="text-xs text-brand-100">快速发布采购信息</p>
          </div>
        </div>
        <el-icon class="ml-auto"><ArrowRight /></el-icon>
      </div>

      <!-- 业务审核 -->
      <div 
        class="bg-gradient-to-br from-purple-600 to-purple-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group"
        @click="navigateTo('/admin/audit')"
      >
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><UserFilled /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">业务审核</h3>
            <p class="text-xs text-purple-100">处理待审核申请</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <span class="bg-white/20 px-2 py-0.5 rounded text-xs animate-pulse">12 待处理</span>
          <el-icon class="ml-auto"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 财务管理 -->
      <div 
        class="bg-gradient-to-br from-green-600 to-green-700 text-white p-6 rounded-xl shadow-lg hover:shadow-2xl transition-all cursor-pointer group"
        @click="navigateTo('/admin/finance/revenue')"
      >
        <div class="flex items-center gap-4 mb-4">
          <div class="w-12 h-12 bg-white/20 rounded-lg flex items-center justify-center backdrop-blur-sm group-hover:scale-110 transition-transform">
            <el-icon class="text-2xl"><Wallet /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-lg">财务管理</h3>
            <p class="text-xs text-green-100">查看收益与定价策略</p>
          </div>
        </div>
        <el-icon class="ml-auto"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 5. 待处理任务表格 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div class="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
        <div>
          <h3 class="font-bold text-slate-800 mb-1">待处理会员申请</h3>
          <p class="text-xs text-slate-500">共 {{ tableData.length }} 条待审核</p>
        </div>
        <el-button type="primary" link @click="navigateTo('/admin/members/list')">查看全部 →</el-button>
      </div>
      
      <el-table :data="tableData" style="width: 100%" :header-cell-style="{background:'#f8fafc', color:'#64748b', fontWeight: 600}">
        <el-table-column prop="date" label="申请日期" width="150" />
        <el-table-column prop="name" label="企业名称">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded bg-slate-100 flex items-center justify-center text-slate-600 font-bold text-xs">
                {{ scope.row.name.substring(0, 2) }}
              </div>
              <span class="font-medium">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="level" label="申请等级" width="100">
          <template #default="scope">
            <span class="px-2.5 py-1 rounded-full text-xs font-bold" 
              :class="scope.row.level === 'SVIP' ? 'bg-orange-50 text-orange-600 border border-orange-200' : 'bg-blue-50 text-blue-600 border border-blue-200'">
              {{ scope.row.level }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleApprove(scope.row)">
              <el-icon><CircleCheck /></el-icon> 通过
            </el-button>
            <el-button link type="danger" size="small" @click="handleReject(scope.row)">
              <el-icon><CircleClose /></el-icon> 驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 6. 最近活动 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm p-6">
      <h3 class="font-bold text-slate-800 mb-6 flex items-center gap-2">
        <el-icon class="text-brand-500"><Clock /></el-icon> 最近活动记录
      </h3>
      <div class="space-y-4">
        <div v-for="(log, i) in activityLogs" :key="i" class="flex items-start gap-4 p-4 rounded-lg hover:bg-slate-50 transition-colors">
          <div class="w-2 h-2 rounded-full mt-2" :class="log.type === 'success' ? 'bg-green-500' : log.type === 'warning' ? 'bg-orange-500' : 'bg-blue-500'"></div>
          <div class="flex-grow">
            <p class="text-sm text-slate-700 mb-1">{{ log.content }}</p>
            <span class="text-xs text-slate-400">{{ log.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  User, Document, Money, TrendCharts, CirclePlus, UserFilled, Wallet, Location,
  ArrowRight, CircleCheck, CircleClose, Clock, DataBoard, Monitor
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

definePageMeta({ layout: 'admin' })

const stats = [
  { label: '本月新增会员', value: '24', icon: User, period: '月增', bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '标书下载量', value: '1,203', icon: Document, period: '周增', bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '成交金额 (万)', value: '450.5', icon: Money, period: '实时', bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '转化率', value: '12.8%', icon: TrendCharts, period: '平均', bgClass: 'bg-orange-50', textClass: 'text-orange-600' },
]

const tableData = [
  { date: '2026-01-13', name: '宁波高新科技股份有限公司', contact: '王总', phone: '138****5678', level: 'SVIP' },
  { date: '2026-01-12', name: '苏州精密制造厂', contact: '陈经理', phone: '139****1234', level: 'VIP' },
  { date: '2026-01-12', name: '杭州进出口贸易公司', contact: '刘总', phone: '136****8899', level: 'SVIP' },
  { date: '2026-01-11', name: '无锡光伏新能源', contact: '赵工', phone: '137****4455', level: 'VIP' },
]

const activityLogs = [
  { content: '李经理 通过了 "宁波高新科技" 的VIP会员申请', time: '2分钟前', type: 'success' },
  { content: '新标书 "A0002345" 发布成功，已推送给23位会员', time: '15分钟前', type: 'info' },
  { content: '财务提醒：本月佣金 ¥3,500 待结算', time: '1小时前', type: 'warning' },
  { content: '系统升级通知：今晚22:00进行维护，预计30分钟', time: '3小时前', type: 'info' },
]

const handleApprove = (row: any) => {
  ElMessage.success(`已通过 ${row.name} 的申请`)
}

const handleReject = (row: any) => {
  ElMessage.warning(`已驳回 ${row.name} 的申请`)
}
</script>
