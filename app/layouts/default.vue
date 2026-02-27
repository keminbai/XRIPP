<template>
  <div class="min-h-screen flex flex-col bg-slate-50 font-sans relative">
    
    <!-- 1. 顶栏 (Top Bar) -->
    <div class="bg-slate-900 text-slate-300 text-xs py-2">
      <div class="container mx-auto px-4 flex justify-between items-center">
        <div class="flex gap-4">
          <!-- ✅ 品牌升级: IPP -> XRIPP -->
          <span>XRIPP国际公共采购服务平台</span>
          <span class="hidden sm:inline">服务热线: 400-888-8888</span>
        </div>
        <div class="flex gap-4 items-center cursor-pointer hover:text-white transition-colors">
          <span>English / 中文</span>
        </div>
      </div>
    </div>

    <!-- 2. 导航栏 (Header) -->
    <header class="bg-white shadow-sm sticky top-0 z-50">
      <div class="container mx-auto px-4">
        <div class="flex justify-between items-center h-20">
                    
          <!-- Logo ✅ 品牌升级 -->
          <NuxtLink to="/" class="flex items-center gap-3 group">
            <img 
              src="~/assets/images/logo.png" 
              alt="XRIPP Global" 
              class="h-10 w-auto object-contain group-hover:scale-105 transition-transform"
            />
            <div class="flex flex-col">
              <span class="font-bold text-xl text-slate-800 leading-tight">XRIPP Global</span>
              <span class="text-xs text-slate-500">国际公共采购一站式服务平台</span>
            </div>
          </NuxtLink>

          <!-- 桌面端导航菜单 -->
          <nav class="hidden lg:flex gap-8 items-center">
            <!-- 1. 首页 -->
            <NuxtLink to="/" class="nav-link group" active-class="active">
              首页
              <span class="nav-indicator"></span>
            </NuxtLink>
            <!-- 2. 公共采购 -->
            <NuxtLink to="/procurement" class="nav-link group" active-class="active">
              公共采购
              <span class="nav-indicator"></span>
            </NuxtLink>
            <!-- 3. 平台服务 -->
            <NuxtLink to="/services" class="nav-link group" active-class="active">
              平台服务
              <span class="nav-indicator"></span>
            </NuxtLink>
            <!-- 4. 出海服务 (✅ 更名: 原海外服务) -->
            <NuxtLink to="/overseas" class="nav-link group" active-class="active">
              出海服务
              <span class="nav-indicator"></span>
            </NuxtLink>
            <!-- 5. 会员中心 -->
            <NuxtLink to="/member" class="nav-link group" active-class="active">
              会员中心
              <span class="nav-indicator"></span>
            </NuxtLink>
            <!-- 6. 关于我们 -->
            <NuxtLink to="/about" class="nav-link group" active-class="active">
              关于我们
              <span class="nav-indicator"></span>
            </NuxtLink>
          </nav>

          <!-- 右侧操作区 -->
          <div class="flex items-center gap-4">
            
            <!-- 演示开关 -->
            <div class="hidden md:block">
              <el-switch 
                v-model="isLoggedIn" 
                inline-prompt 
                active-text="已登录" 
                inactive-text="游客"
                style="--el-switch-on-color: #13ce66; --el-switch-off-color: #909399"
              />
            </div>

            <!-- 情况A：未登录 -->
            <div v-if="!isLoggedIn" class="hidden md:block">
              <el-button type="primary" round class="bg-brand-600 hover:bg-brand-700 border-none shadow-md" @click="navigateTo('/login')">
                登录 / 注册
              </el-button>
            </div>

            <!-- 情况B：已登录 -->
            <div v-else class="hidden md:flex items-center gap-3">
              <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-gradient-to-r from-yellow-50 to-orange-50 border border-yellow-200">
                <el-icon class="text-yellow-600"><Trophy /></el-icon>
                <span class="text-xs font-bold text-yellow-700">SVIP会员</span>
              </div>
              
              <div class="text-right hidden xl:block">
                <div class="text-[10px] text-slate-400 leading-tight">距离到期</div>
                <div class="text-xs font-bold text-slate-800">182天</div>
              </div>
              
              <el-dropdown trigger="click">
                <div class="w-10 h-10 rounded-full border-2 border-slate-100 shadow-sm overflow-hidden cursor-pointer hover:border-brand-200 transition-all hover:scale-105">
                  <img src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" class="w-full h-full object-cover" />
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="navigateTo('/member')"><el-icon><User /></el-icon> 会员中心</el-dropdown-item>
                    <el-dropdown-item @click="navigateTo('/member/orders')"><el-icon><Document /></el-icon> 我的订单</el-dropdown-item>
                    <el-dropdown-item @click="navigateTo('/member/favorites')"><el-icon><Star /></el-icon> 我的收藏</el-dropdown-item>
                    <el-dropdown-item @click="navigateTo('/member/settings')"><el-icon><Setting /></el-icon> 账号设置</el-dropdown-item>
                    <el-dropdown-item divided @click="isLoggedIn = false"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 移动端菜单按钮 -->
            <div class="lg:hidden">
              <el-button link class="text-slate-600 text-2xl">
                <el-icon><Menu /></el-icon>
              </el-button>
            </div>
          </div>

        </div>
      </div>
    </header>

    <!-- 3. 全局右侧悬浮工具栏 (50%透明 + 蓝色字体) -->
    <div class="fixed right-6 bottom-28 z-40 flex flex-col gap-3">
      
      <!-- 容器：bg-white/50 -->
      <div class="glassmorphism-box rounded-2xl p-1.5 flex flex-col gap-1.5 w-16 shadow-2xl">
        
        <!-- 公众号 -->
        <el-popover placement="left" :width="200" trigger="hover">
          <template #reference>
            <div class="tool-item group">
              <el-icon class="text-2xl mb-1 text-blue-600 transform group-hover:scale-110 transition-transform"><ChatDotRound /></el-icon>
              <span class="text-[10px] font-bold text-blue-700">公众号</span>
            </div>
          </template>
          <div class="text-center p-3">
            <div class="w-32 h-32 bg-slate-100 mx-auto mb-3 flex items-center justify-center text-xs text-slate-400 rounded-lg">二维码</div>
            <p class="text-xs font-bold text-slate-700">关注官方公众号</p>
            <p class="text-xs text-slate-400 mt-1">获取最新采购资讯</p>
          </div>
        </el-popover>

        <div class="h-px bg-blue-200/30 w-full"></div>

        <!-- 小程序 -->
        <el-popover placement="left" :width="200" trigger="hover">
          <template #reference>
            <div class="tool-item group">
              <el-icon class="text-2xl mb-1 text-blue-600 transform group-hover:scale-110 transition-transform"><Iphone /></el-icon>
              <span class="text-[10px] font-bold text-blue-700">小程序</span>
            </div>
          </template>
          <div class="text-center p-3">
            <div class="w-32 h-32 bg-slate-100 mx-auto mb-3 flex items-center justify-center text-xs text-slate-400 rounded-lg">二维码</div>
            <p class="text-xs font-bold text-slate-700">扫码使用小程序</p>
            <p class="text-xs text-slate-400 mt-1">随时随地查看商机</p>
          </div>
        </el-popover>

        <div class="h-px bg-blue-200/30 w-full"></div>

        <!-- 在线客服 -->
        <el-tooltip content="联系人工客服" placement="left">
          <div class="tool-item group">
            <el-icon class="text-2xl mb-1 text-blue-600 transform group-hover:scale-110 transition-transform"><Headset /></el-icon>
            <span class="text-[10px] font-bold text-blue-700">客服</span>
          </div>
        </el-tooltip>
      </div>

      <!-- 回到顶部 -->
      <transition name="el-fade-in">
        <div 
          v-show="y > 300" 
          class="glassmorphism-box w-16 h-16 rounded-full shadow-2xl flex flex-col items-center justify-center text-slate-600 hover:text-brand-600 hover:-translate-y-1 transition-all cursor-pointer mt-2" 
          @click="scrollToTop"
        >
          <el-icon class="text-xl"><ArrowUpBold /></el-icon>
          <span class="text-[10px] font-medium mt-0.5 text-blue-700">顶部</span>
        </div>
      </transition>
    </div>

    <!-- 页面主体内容 -->
    <main class="flex-grow">
      <slot />
    </main>

    <!-- 4. 页脚 (✅ 增强合规版: 更新主办单位) -->
    <footer class="bg-slate-900 text-slate-400 pt-16 pb-8">
        <div class="container mx-auto px-4">
            
            <!-- 主要内容区 -->
            <div class="grid grid-cols-1 md:grid-cols-5 gap-12 mb-12 text-center md:text-left">
                
                <!-- 品牌介绍 -->
                <div class="md:col-span-2">
                    <div class="flex items-center gap-3 mb-6 text-white justify-center md:justify-start">
                        <img 
                            src="~/assets/images/Xlogo.png" 
                            alt="国际公共采购服务平台" 
                            class="h-10 w-auto object-contain"
                        />
                        <span class="font-bold text-lg">国际公共采购服务平台</span>
                    </div>
                    <p class="text-sm leading-relaxed mb-6">
                        连接中国企业与国际公共采购商机。<br>
                        通往联合国及国际政府招标市场的专业门户。
                    </p>
                    
                    <!-- 二维码区域 -->
                    <div class="flex gap-4 justify-center md:justify-start">
                        <div class="text-center">
                            <div class="w-24 h-24 bg-slate-800 rounded-lg mb-2 flex items-center justify-center text-xs text-slate-500 border border-slate-700">
                                公众号<br>二维码
                            </div>
                            <div class="text-xs text-slate-500">关注公众号</div>
                        </div>
                        <div class="text-center">
                            <div class="w-24 h-24 bg-slate-800 rounded-lg mb-2 flex items-center justify-center text-xs text-slate-500 border border-slate-700">
                                小程序<br>二维码
                            </div>
                            <div class="text-xs text-slate-500">扫码使用</div>
                        </div>
                    </div>
                </div>
                
                <!-- 平台服务 -->
                <div>
                    <h4 class="text-white font-bold mb-6">平台服务</h4>
                    <ul class="space-y-3 text-sm">
                        <li><NuxtLink to="/procurement" class="hover:text-white transition-colors">公共采购信息</NuxtLink></li>
                        <li><NuxtLink to="/services" class="hover:text-white transition-colors">联合国供应商注册</NuxtLink></li>
                        <li><NuxtLink to="/overseas" class="hover:text-white transition-colors">出海实地考察</NuxtLink></li>
                        <li><NuxtLink to="/supplier-directory" class="hover:text-white transition-colors">服务商名录</NuxtLink></li>
                    </ul>
                </div>
                
                <!-- 会员中心 -->
                <div>
                    <h4 class="text-white font-bold mb-6">会员中心</h4>
                    <ul class="space-y-3 text-sm">
                        <li><NuxtLink to="/member" class="hover:text-white transition-colors">我的工作台</NuxtLink></li>
                        <li><NuxtLink to="/member/orders" class="hover:text-white transition-colors">我的订单</NuxtLink></li>
                        <li><NuxtLink to="/member/favorites" class="hover:text-white transition-colors">我的收藏</NuxtLink></li>
                        <li><NuxtLink to="/member/settings" class="hover:text-white transition-colors">账号设置</NuxtLink></li>
                    </ul>
                </div>
                
                <!-- 联系我们 -->
                <div>
                    <h4 class="text-white font-bold mb-6">联系我们</h4>
                    <ul class="space-y-3 text-sm">
                        <li class="flex items-start gap-2">
                            <el-icon class="mt-0.5"><Location /></el-icon>
                            <span>上海市黄浦区北京东路668号科技京城东楼18F</span>
                        </li>
                        <li class="flex items-center gap-2">
                            <el-icon><Phone /></el-icon>
                            <span>400-888-8888</span>
                        </li>
                        <li class="flex items-center gap-2">
                            <el-icon><Message /></el-icon>
                            <span>postmaster@xripp.com</span>
                        </li>
                    </ul>
                </div>
            </div>
            
            <!-- 法律信息区 -->
            <div class="border-t border-slate-800 pt-8 space-y-4">
                
                <!-- 机构信息 (✅ 关键更新) -->
                <div class="text-xs text-slate-500 text-center space-y-2">
                    <div class="flex flex-wrap justify-center gap-x-8 gap-y-2">
                        <span><strong class="text-slate-400">主办单位：</strong>国际公共采购服务平台</span>
                        <span><strong class="text-slate-400">运维单位：</strong>上海杏睿信息科技有限公司</span>
                    </div>
                    <div class="flex flex-wrap justify-center gap-x-6 gap-y-2">
                        <span>沪ICP备2024XXXXXX号-1</span>
                        <a href="#" class="hover:text-white transition-colors">沪公网安备 31XXXXXXXXXX号</a>
                        <span>增值电信业务经营许可证：沪B2-XXXXXXXX</span>
                    </div>
                </div>
                
                <!-- 免责声明 -->
                <div class="text-xs text-slate-600 text-center max-w-4xl mx-auto leading-relaxed bg-slate-800/30 p-4 rounded-lg">
                    <p>
                        <strong>声明：</strong>本平台投放的联合国及国际组织采购项目标书均为<strong class="text-slate-400">免费公开信息</strong>，平台仅收取相关服务费用。
                        致力于帮助中小企业利用中文浏览快速了解国际公共采购商机，促进中国企业参与全球市场竞争。
                    </p>
                </div>
                
                <!-- 版权信息 -->
                <div class="text-xs text-center text-slate-500 pt-4">
                    <p>&copy; 2026 XRIPP国际公共采购服务平台. 保留所有权利.</p>
                </div>
            </div>
        </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { 
  User, SwitchButton, Document, Star, Setting,
  ChatDotRound, Headset, ArrowUpBold, Menu, Iphone, Trophy,
  Location, Phone, Message
} from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useWindowScroll } from '@vueuse/core'

const { y } = useWindowScroll()
const isLoggedIn = ref(false)

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
/* 核心修改：50%透明度 */
.glassmorphism-box {
  background: rgba(255, 255, 255, 0.5); 
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

/* 导航链接基础样式 */
.nav-link {
  @apply text-sm font-medium text-slate-600 hover:text-brand-600 transition-colors relative py-2;
}

/* 选中状态：蓝色加粗 */
.nav-link.active {
  @apply text-brand-600 font-bold;
}

/* 底部蓝条指示器 */
.nav-indicator {
  @apply absolute bottom-0 left-0 w-full h-0.5 bg-brand-600 hidden rounded-t-sm;
}
/* 仅在 active 状态下显示蓝条 */
.nav-link.active .nav-indicator {
  @apply block;
}

.tool-item {
  @apply flex flex-col items-center justify-center py-3 rounded-lg cursor-pointer transition-all;
}
.tool-item:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.05);
}

:deep(.el-button--primary) {
  background-color: #0284c7;
  border-color: #0284c7;
}
:deep(.el-button--primary:hover) {
  background-color: #0369a1;
  border-color: #0369a1;
}
</style>