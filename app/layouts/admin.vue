<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\layouts\admin.vue
  版本: V6.2 最终完整版 (2026-02-01)
  
  ✅ 修复内容:
  1. [合伙人入口] 新增 "我的发布" 菜单，链接至 /admin/partner-publish
  2. [权限隔离] "信息发布系统" (CMS管理) 设为仅总部可见
  3. [LOGO更新] 所有 "IPP" 已更新为 "XRIPP"
  4. [逻辑完善] 确保合伙人登录后有清晰的操作路径
  5. [菜单优化] 根据角色动态显示菜单项
  6. [菜单补全] 补充3个系统综管菜单（客服/证书/登录）
  7. [命名优化] business模块菜单链接优化（permissions→roles, pricing→promotions）
  8. [文本优化] 菜单文本更清晰（系统权限管理、业务角色配置）
  ========================================================================
-->
<template>
  <div class="min-h-screen bg-[#f1f5f9] flex font-sans">
    
    <!-- 1. 左侧侧边栏 -->
    <aside class="w-64 bg-slate-900 text-white flex flex-col fixed h-full z-20 shadow-2xl transition-all duration-300">
      <!-- Logo -->
      <div class="h-16 flex items-center gap-3 px-6 border-b border-slate-800 bg-slate-900 cursor-pointer" @click="navigateTo('/admin')">
        <div class="w-8 h-8 bg-brand-600 rounded flex items-center justify-center font-bold text-white">X</div>
        <span class="font-bold text-lg tracking-wide">XRIPP 管理后台</span>
      </div>

      <!-- 角色展示 -->
      <div class="px-4 py-3 bg-slate-900 border-b border-slate-800">
        <div class="flex items-center justify-between">
          <span class="text-xs text-slate-400">当前视角</span>
          <el-tag size="small" :type="currentRole === 'admin' ? 'danger' : 'success'" effect="dark">
            {{ currentRole === 'admin' ? '总部管理员' : '城市合伙人' }}
          </el-tag>
        </div>
      </div>

      <!-- 菜单区域 -->
      <el-scrollbar class="flex-grow">
        <el-menu
          :default-active="activeMenu"
          class="border-none"
          background-color="#0f172a"
          text-color="#94a3b8"
          active-text-color="#ffffff"
          :unique-opened="true"
          router
        >
          <!-- ==================== 一级菜单 (公共) ==================== -->
          
          <!-- 1. 数据驾驶舱 -->
          <el-menu-item index="/admin">
            <el-icon><Monitor /></el-icon>
            <span>数据驾驶舱</span>
          </el-menu-item>

          <!-- ==================== 角色专属：合伙人 ==================== -->
          
          <!-- [新增] 合伙人专属发布入口 -->
          <el-menu-item index="/admin/partner-publish" v-if="currentRole === 'partner'">
            <el-icon><EditPen /></el-icon>
            <span>我的发布</span>
          </el-menu-item>

          <!-- ==================== 角色专属：总部管理员 ==================== -->

          <!-- 2. 业务审核中心 (总部独有) -->
          <el-menu-item index="/admin/audit" v-if="currentRole === 'admin'">
            <el-icon><Stamp /></el-icon>
            <span>业务审核中心</span>
            <el-badge :value="pendingAuditCount" type="danger" class="ml-auto" v-if="pendingAuditCount > 0" />
          </el-menu-item>

          <!-- ==================== 二级折叠菜单 ==================== -->

          <!-- 3. 会员管理系统 (公共) -->
          <el-sub-menu index="member-system">
            <template #title>
              <el-icon><User /></el-icon>
              <span>会员管理系统</span>
            </template>
            <el-menu-item index="/admin/members/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>会员统计分析</span>
            </el-menu-item>
            <el-menu-item index="/admin/members/list">
              <el-icon><List /></el-icon>
              <span>会员管理列表</span>
            </el-menu-item>
            <el-menu-item index="/admin/members/orders">
              <el-icon><ShoppingCart /></el-icon>
              <span>会员订单管理</span>
            </el-menu-item>
            <!-- 仅总部可见：联合国入库审核 -->
            <el-menu-item index="/admin/members/un-audit" v-if="currentRole === 'admin'">
              <el-icon><DocumentChecked /></el-icon>
              <span>联合国入库审核</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 4. 信息发布系统 (✅ 修复：仅总部可见，合伙人走"我的发布") -->
          <el-sub-menu index="content-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>信息发布系统</span>
            </template>
            <el-menu-item index="/admin/content/activities">
              <el-icon><Calendar /></el-icon>
              <span>活动管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/content/trainings">
              <el-icon><Reading /></el-icon>
              <span>培训管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/content/business">
              <el-icon><Connection /></el-icon>
              <span>商机对接</span>
            </el-menu-item>
            <el-menu-item index="/admin/content/media">
              <el-icon><Mic /></el-icon>
              <span>媒体发布</span>
            </el-menu-item>
            <el-menu-item index="/admin/content/display">
              <el-icon><Monitor /></el-icon>
              <span>广告与显示</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 5. 服务商系统 (公共) -->
          <el-sub-menu index="supplier-system">
            <template #title>
              <el-icon><OfficeBuilding /></el-icon>
              <span>服务商系统</span>
            </template>
            <el-menu-item index="/admin/suppliers/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计分析</span>
            </el-menu-item>
            <el-menu-item index="/admin/suppliers/list">
              <el-icon><List /></el-icon>
              <span>服务商名录库</span>
            </el-menu-item>
            <!-- 仅总部可见：入驻审核 -->
            <el-menu-item index="/admin/suppliers/audit" v-if="currentRole === 'admin'">
              <el-icon><Select /></el-icon>
              <span>入驻审核</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 6. 标书发布系统 (总部独有) -->
          <el-sub-menu index="tender-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Files /></el-icon>
              <span>标书发布系统</span>
            </template>
            <el-menu-item index="/admin/tenders/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/admin/tenders/publish">
              <el-icon><Plus /></el-icon>
              <span>发布新标书</span>
            </el-menu-item>
            <el-menu-item index="/admin/tenders/manage">
              <el-icon><List /></el-icon>
              <span>标书管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/tenders/orders">
              <el-icon><ShoppingCart /></el-icon>
              <span>销售订单</span>
            </el-menu-item>
            <el-menu-item index="/admin/tenders/reference">
              <el-icon><Link /></el-icon>
              <span>引用标讯</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 7. 出海发布系统 (总部独有) -->
          <el-sub-menu index="overseas-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span>出海发布系统</span>
            </template>
            <el-menu-item index="/admin/overseas/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/admin/overseas/points">
              <el-icon><Location /></el-icon>
              <span>服务网点设置</span>
            </el-menu-item>
            <el-menu-item index="/admin/overseas/services">
              <el-icon><Service /></el-icon>
              <span>出海信息发布</span>
            </el-menu-item>
            <el-menu-item index="/admin/overseas/reports">
              <el-icon><Document /></el-icon>
              <span>行业报告上传</span>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 8. 合伙人系统 (公共) --> 
          <el-sub-menu index="partner-system">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>合伙人系统</span>
            </template>
            <el-menu-item index="/admin/partners/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计分析</span>
            </el-menu-item>
            <!-- 仅总部可见：合伙人列表管理 -->
            <el-menu-item index="/admin/partners/list" v-if="currentRole === 'admin'">
              <el-icon><List /></el-icon>
              <span>合伙人管理</span>
            </el-menu-item>
            <!-- 合伙人可见：我的资源 -->
            <el-menu-item index="/admin/partners/resources">
              <el-icon><Box /></el-icon>
              <span>资源库</span>
            </el-menu-item>
            <!-- ✅ 新增：合伙人个人中心 -->
            <el-menu-item index="/admin/partners/center">
              <el-icon><UserFilled /></el-icon>
              <span>合伙人个人中心</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 9. 数据看板配置 (总部独有) -->
          <el-sub-menu index="dashboard-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><DataBoard /></el-icon>
              <span>数据看板配置</span>
            </template>
            <el-menu-item index="/admin/dashboard/procurement">
              <el-icon><Goods /></el-icon>
              <span>采购数据配置</span>
            </el-menu-item>
            <el-menu-item index="/admin/dashboard/member">
              <el-icon><UserFilled /></el-icon>
              <span>会员数据配置</span>
            </el-menu-item>
            <el-menu-item index="/admin/dashboard/network">
              <el-icon><Place /></el-icon>
              <span>服务网络配置</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 10. 业务管理系统 (总部独有) -->
          <el-sub-menu index="business-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Operation /></el-icon>
              <span>业务管理系统</span>
            </template>
            <el-menu-item index="/admin/business/roles">
              <el-icon><UserFilled /></el-icon>
              <span>业务角色配置</span>
            </el-menu-item>
            <el-menu-item index="/admin/business/packages">
              <el-icon><Box /></el-icon>
              <span>服务包策略</span>
            </el-menu-item>
            <el-menu-item index="/admin/business/promotions">
              <el-icon><Medal /></el-icon>
              <span>营销促销</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 11. 财务配置 (总部独有) -->
          <el-sub-menu index="finance-system" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>财务配置</span>
            </template>
            <el-menu-item index="/admin/finance/revenue">
              <el-icon><TrendCharts /></el-icon>
              <span>收入概览</span>
            </el-menu-item>
            <el-menu-item index="/admin/finance/pricing">
              <el-icon><PriceTag /></el-icon>
              <span>定价策略</span>
            </el-menu-item>
            <el-menu-item index="/admin/finance/refund">
              <el-icon><RefreshLeft /></el-icon>
              <span>退款管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/finance/profit">
              <el-icon><Coin /></el-icon>
              <span>利润分成</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 12. 系统综管 (总部独有) -->
          <el-sub-menu index="system-config" v-if="currentRole === 'admin'">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统综管</span>
            </template>
            <el-menu-item index="/admin/system/permissions">
              <el-icon><Key /></el-icon>
              <span>系统权限管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/notifications">
              <el-icon><Bell /></el-icon>
              <span>通知管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/customer-service">
              <el-icon><Headset /></el-icon>
              <span>客服系统</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/certificates">
              <el-icon><Medal /></el-icon>
              <span>证书管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/logs">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/settings">
              <el-icon><Tools /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/login-config">
              <el-icon><Lock /></el-icon>
              <span>登录配置</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/backup">
              <el-icon><FolderOpened /></el-icon>
              <span>数据备份</span>
            </el-menu-item>
            <el-menu-item index="/admin/system/about">
              <el-icon><InfoFilled /></el-icon>
              <span>关于系统</span>
            </el-menu-item>
          </el-sub-menu>

        </el-menu>
      </el-scrollbar>

      <!-- 底部用户信息 -->
      <div class="p-4 border-t border-slate-800 bg-slate-900">
        <div class="flex items-center gap-3">
          <img 
            src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" 
            class="w-8 h-8 rounded-full border border-slate-600" 
          />
          <div class="flex-col flex flex-grow overflow-hidden">
            <span class="text-xs font-bold text-white truncate">
              {{ currentUser?.username || (currentRole === 'admin' ? 'System Admin' : '上海李经理') }}
            </span>
            <span class="text-[10px] text-slate-500 truncate">
              {{ currentRole === 'admin' ? '最高权限' : 'SH001合伙人' }}
            </span>
          </div>
          <el-dropdown trigger="click" @command="handleCommand">
            <el-icon class="text-slate-400 hover:text-white cursor-pointer transition-colors"><More /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </aside>

    <!-- 2. 右侧主体内容 -->
    <main class="flex-grow ml-64 min-h-screen flex flex-col transition-all duration-300">
      <!-- 顶部导航栏 -->
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 sticky top-0 z-10 shadow-sm">
        <div class="flex items-center gap-4">
          <h2 class="text-lg font-bold text-slate-800">{{ pageTitle }}</h2>
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/" class="text-sm">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbs.length > 0">{{ breadcrumbs[0] }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbs.length > 1">{{ breadcrumbs[1] }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="flex items-center gap-4">
          <!-- 待审核提醒 -->
          <el-badge :value="pendingAuditCount" class="cursor-pointer" @click="navigateTo('/admin/audit')" v-if="currentRole === 'admin' && pendingAuditCount > 0">
            <el-button circle>
              <el-icon class="text-xl"><Bell /></el-icon>
            </el-button>
          </el-badge>
          <!-- 快捷操作 -->
          <el-button link @click="navigateTo('/')">
            <el-icon class="mr-1"><Link /></el-icon> 前往前台
          </el-button>
        </div>
      </header>
      
      <!-- 主内容区 -->
      <div class="p-6 flex-grow overflow-x-hidden">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { 
  Monitor, User, Files, Money, Setting, Bell, More, Stamp,
  OfficeBuilding, Promotion, Reading, Connection,
  Calendar, List, Plus, DataAnalysis, ShoppingCart, DocumentChecked,
  Mic, Select, Location, Service, Document, Box, DataBoard, Goods,
  UserFilled, Place, Operation, Key, TrendCharts, PriceTag, RefreshLeft,
  Coin, Tools, InfoFilled, Link,
  View, SwitchButton, EditPen, FolderOpened, Headset, Medal, Lock
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAuth, getLoginUser } from '~/utils/request'

const route = useRoute()
const currentUser = computed(() => getLoginUser())
const currentRole = computed(() => {
  const r = currentUser.value?.role
  if (r === 'partner') return 'partner'
  if (r === 'admin') return 'admin'
  return 'partner' // 或者按你希望的默认值
})
const pendingAuditCount = ref(12) // 待审核数量

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 页面标题映射
const pageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/admin': '数据驾驶舱',
    '/admin/partner-publish': '我的发布',
    '/admin/audit': '业务审核中心',
    '/admin/members/analysis': '会员统计分析',
    '/admin/members/list': '会员管理列表',
    '/admin/members/orders': '会员订单管理',
    '/admin/members/un-audit': '联合国入库审核',
    '/admin/content/activities': '活动管理',
    '/admin/content/trainings': '培训管理',
    '/admin/content/business': '商机对接',
    '/admin/content/media': '媒体发布',
    '/admin/content/display': '广告与显示',
    '/admin/suppliers/analysis': '服务商统计分析',
    '/admin/suppliers/list': '服务商名录库',
    '/admin/suppliers/audit': '入驻审核',
    '/admin/tenders/analysis': '标书数据统计',
    '/admin/tenders/publish': '发布新标书',
    '/admin/tenders/manage': '标书管理',
    '/admin/tenders/orders': '销售订单',
    '/admin/tenders/reference': '引用标讯',
    '/admin/overseas/analysis': '出海数据统计',
    '/admin/overseas/points': '服务网点设置',
    '/admin/overseas/services': '出海信息发布',
    '/admin/overseas/reports': '行业报告上传',
    '/admin/partners/analysis': '合伙人统计分析',
    '/admin/partners/list': '合伙人管理',
    '/admin/partners/resources': '资源库',
    '/admin/dashboard/procurement': '采购数据配置',
    '/admin/dashboard/member': '会员数据配置',
    '/admin/dashboard/network': '服务网络配置',
    '/admin/business/roles': '业务角色配置',
    '/admin/business/packages': '服务包策略',
    '/admin/business/promotions': '营销促销',
    '/admin/finance/revenue': '收入概览',
    '/admin/finance/pricing': '定价策略',
    '/admin/finance/refund': '退款管理',
    '/admin/finance/profit': '利润分成',
    '/admin/system/permissions': '系统权限管理',
    '/admin/system/notifications': '通知管理',
    '/admin/system/customer-service': '客服系统',
    '/admin/system/certificates': '证书管理',
    '/admin/system/logs': '操作日志',
    '/admin/system/settings': '系统设置',
    '/admin/system/login-config': '登录配置',
    '/admin/system/backup': '数据备份',
    '/admin/system/about': '关于系统'
  }
  return titleMap[route.path] || '管理后台'
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const path = route.path
  const crumbs: string[] = []
  
  if (path === '/admin/partner-publish') crumbs.push('合伙人发布')
  else if (path.includes('/members/')) crumbs.push('会员管理系统')
  else if (path.includes('/content/')) crumbs.push('信息发布系统')
  else if (path.includes('/suppliers/')) crumbs.push('服务商系统')
  else if (path.includes('/tenders/')) crumbs.push('标书发布系统')
  else if (path.includes('/overseas/')) crumbs.push('出海发布系统')
  else if (path.includes('/partners/')) crumbs.push('合伙人系统')
  else if (path.includes('/dashboard/')) crumbs.push('数据看板配置')
  else if (path.includes('/business/')) crumbs.push('业务管理系统')
  else if (path.includes('/finance/')) crumbs.push('财务配置')
  else if (path.includes('/system/')) crumbs.push('系统综管')
  
  if (crumbs.length > 0 && path !== '/admin') {
    crumbs.push(pageTitle.value)
  }
  
  return crumbs
})

// 命令处理
const handleCommand = (command: string) => {
  if (command === 'logout') {
    clearAuth()
    ElMessage.success('已退出登录')
    navigateTo('/login')
    return
  }

  // 禁用前端调试角色切换，避免绕过真实权限
  if (command === 'switch_admin' || command === 'switch_partner') {
    ElMessage.warning('已禁用调试角色切换，请使用真实账号登录')
    return
  }
}
</script>

<style scoped>
/* Element Plus 菜单样式覆盖 */
:deep(.el-menu) { border-right: none; }
:deep(.el-sub-menu__title), :deep(.el-menu-item) { height: 48px; line-height: 48px; transition: all 0.2s ease; }
:deep(.el-sub-menu__title:hover), :deep(.el-menu-item:hover) { background-color: #1e293b !important; color: #fff !important; }
:deep(.el-menu-item.is-active) { background-color: #0284c7 !important; color: #fff !important; font-weight: 600; border-right: 3px solid #38bdf8; }
:deep(.el-sub-menu .el-menu-item) { background-color: #0b1522 !important; padding-left: 56px !important; min-height: 44px; height: auto; line-height: 44px; }
:deep(.el-sub-menu .el-menu-item:hover) { background-color: #1e293b !important; }
:deep(.el-sub-menu .el-menu-item.is-active) { background-color: #0369a1 !important; border-right: 3px solid #0ea5e9; }
:deep(.el-menu-item .el-icon), :deep(.el-sub-menu__title .el-icon) { margin-right: 8px; font-size: 16px; vertical-align: middle; }
:deep(.el-badge__content) { border: none; font-size: 10px; height: 16px; line-height: 16px; padding: 0 5px; }
</style>