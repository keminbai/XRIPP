<template>
  <div class="bg-white min-h-screen pb-12 font-sans">
    
    <!-- 1. 动态地图 Banner -->
    <div v-if="!hideMapBanner" class="relative h-[650px] bg-[#0b1120] overflow-hidden flex items-center justify-center text-center">
      <div class="absolute inset-0 z-0">
        <ClientOnly>
          <div v-if="mapLoaded" class="w-full h-full animate-fade-in">
            <AppChart :options="mapOptions" @click="handleMapClick" />
          </div>
        </ClientOnly>
      </div>
      <div class="absolute inset-0 bg-[radial-gradient(transparent_30%,#0b1120_100%)] pointer-events-none"></div>
      
      <!-- A. 左侧：动态统计数据 ✅ 修复：数据会随筛选变化 -->
      <div class="absolute top-1/2 left-8 -translate-y-1/2 z-20 flex flex-col gap-6 pointer-events-none">
        <div class="bg-white/5 backdrop-blur-md border border-white/10 p-5 rounded-2xl text-left shadow-2xl animate-slide-in-left pointer-events-auto hover:bg-white/10 transition-colors">
          <div class="text-brand-400 text-[10px] font-bold uppercase tracking-widest mb-1 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-brand-400 animate-pulse"></span> Coverage
          </div>
          <!-- ✅ 修复：显示筛选后的国家数 -->
          <div class="text-4xl font-bold text-white font-mono">{{ displayStats.countryCount }}</div>
          <div class="text-xs text-slate-400 mt-1">
            {{ activeCountry === '全部' ? '已开通服务国家' : activeCountry + ' 服务覆盖' }}
          </div>
        </div>
        <div class="bg-white/5 backdrop-blur-md border border-white/10 p-5 rounded-2xl text-left shadow-2xl animate-slide-in-left delay-100 pointer-events-auto hover:bg-white/10 transition-colors">
          <div class="text-yellow-400 text-[10px] font-bold uppercase tracking-widest mb-1 flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-yellow-400 animate-pulse"></span> Stations
          </div>
          <!-- ✅ 修复：显示筛选后的服务点数 -->
          <div class="text-4xl font-bold text-white font-mono">{{ displayStats.pointCount }}</div>
          <div class="text-xs text-slate-400 mt-1">
            {{ activeType === '全部' ? '全球服务网点' : activeType + ' 服务点' }}
          </div>
        </div>
      </div>

      <!-- B. 右侧：交互详情框（新增评分、响应时长） -->
      <transition name="slide-right">
        <div v-if="selectedPoint" class="absolute top-20 right-8 z-30 w-80 bg-[#0f172a]/90 backdrop-blur-xl border border-slate-700/50 rounded-2xl overflow-hidden shadow-2xl text-left ring-1 ring-white/10">

          <!-- 顶部：国家封面 -->
          <div class="h-36 relative group">
            <img :src="selectedPoint.image" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
            <div class="absolute inset-0 bg-gradient-to-t from-[#0f172a] via-[#0f172a]/20 to-transparent"></div>
            
            <div class="absolute bottom-4 left-5 w-full pr-10">
              <div class="text-2xl font-bold text-white flex items-center gap-2 mb-1">
                <img :src="`https://flagcdn.com/24x18/${getCountryCode(selectedPoint.country)}.png`" class="rounded-sm shadow-sm" />
                {{ selectedPoint.country }}
              </div>
              <div class="text-xs text-brand-300 font-medium tracking-wide flex items-center gap-1">
                <el-icon><Location /></el-icon> {{ selectedPoint.city }} 国际办事处
              </div>
            </div>
            
            <button class="absolute top-3 right-3 w-8 h-8 rounded-full bg-black/40 text-white/70 hover:bg-red-500 hover:text-white flex items-center justify-center transition-all backdrop-blur-sm" @click="selectedPoint = null">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <!-- 内容区 ✅ 新增：评分、响应时长、成功案例 -->
          <div class="p-6 pt-2 text-slate-300">
            <!-- 核心数据指标 -->
            <div class="grid grid-cols-2 gap-4 mb-5 pb-5 border-b border-white/10">
              <div>
                <div class="text-[10px] text-slate-500 uppercase tracking-wider mb-1">Manager</div>
                <div class="text-sm font-bold text-white flex items-center gap-1">
                  <div class="w-5 h-5 rounded-full bg-slate-700 flex items-center justify-center text-[10px]">👨‍💼</div>
                  {{ selectedPoint.manager }}
                </div>
              </div>
              <div>
                <div class="text-[10px] text-slate-500 uppercase tracking-wider mb-1">Rating</div>
                <div class="text-sm font-bold text-yellow-400 flex items-center gap-1">
                  ⭐ {{ selectedPoint.rating }} / 5.0
                </div>
              </div>
              <div>
                <div class="text-[10px] text-slate-500 uppercase tracking-wider mb-1">Response</div>
                <div class="text-sm font-bold text-green-400 flex items-center gap-1">
                  ⚡ {{ selectedPoint.responseTime }}
                </div>
              </div>
              <div>
                <div class="text-[10px] text-slate-500 uppercase tracking-wider mb-1">Success</div>
                <div class="text-sm font-bold text-blue-400 flex items-center gap-1">
                  ✓ {{ selectedPoint.successCases }}+ 案例
                </div>
              </div>
            </div>

            <!-- 服务描述 -->
            <div class="mb-5">
              <div class="text-xs font-bold text-white mb-2 flex items-center gap-2">
                <span class="w-1 h-4 bg-brand-500 rounded-full"></span> 核心服务能力
              </div>
              <div class="flex flex-wrap gap-2">
                <span v-for="s in selectedPoint.services" :key="s" class="text-[10px] px-2 py-1 rounded bg-slate-800 border border-slate-700 text-slate-300">
                  {{ s }}
                </span>
              </div>
              <p class="text-xs text-slate-400 mt-3 leading-relaxed line-clamp-3">
                {{ selectedPoint.desc }}
              </p>
            </div>

            <!-- 底部操作 -->
            <button class="w-full py-3 bg-gradient-to-r from-brand-600 to-blue-600 hover:from-brand-500 hover:to-blue-500 text-white rounded-xl text-sm font-bold shadow-lg shadow-brand-900/50 transition-all flex items-center justify-center gap-2 group" @click="openServiceDetail(selectedPoint)">
              <el-icon class="group-hover:animate-bounce"><ChatDotRound /></el-icon> 
              查看服务方案 & 咨询
            </button>
          </div>
        </div>
      </transition>

      <!-- B2. 右侧操作提示（未选中服务点时显示） -->
      <div
        v-if="!selectedPoint"
        class="absolute top-28 right-6 z-20 w-44 bg-white/5 backdrop-blur-md border border-white/10 p-4 rounded-2xl text-left shadow-2xl"
      >
        <div class="text-brand-400 text-[10px] font-bold uppercase tracking-widest mb-1">
          Guide
        </div>
        <div class="text-sm font-semibold text-white leading-snug">
          点击城市节点
        </div>
        <div class="text-xs text-slate-400 mt-1 leading-relaxed">
          查看当地服务点与联系信息
        </div>
      </div>

      <!-- C. 标题区 -->
      <div class="absolute top-16 left-0 right-0 z-10 pointer-events-none">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-400/30 text-blue-300 text-xs font-bold tracking-widest mb-4 uppercase backdrop-blur-sm">
          <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
          Global Service Network
        </div>
        <h1 class="text-4xl md:text-5xl font-bold text-white tracking-tight drop-shadow-2xl">
          全球战略服务网络
        </h1>
      </div>
    </div>

    <!-- 2. 快捷选择栏 ✅ 优化：新增大洲筛选 + 重置按钮 -->
    <div class="container mx-auto px-4 -mt-10 relative z-20">
      <div class="bg-white rounded-2xl shadow-xl border border-slate-100 p-6 flex flex-col gap-5">
        
        <!-- Filter 0: 大洲筛选 ✅ 新增 -->
        <div class="flex items-center gap-3 pb-5 border-b border-slate-100">
          <div class="text-xs font-bold text-slate-500 uppercase tracking-wider min-w-[60px]">大洲：</div>
          <div class="flex flex-wrap gap-3 flex-1">
            <button 
              v-for="continent in continents" 
              :key="continent"
              class="px-4 py-2 rounded-full text-xs font-bold transition-all hover:scale-105"
              :class="activeContinent === continent ? 'bg-purple-600 text-white shadow-md shadow-purple-500/30' : 'bg-slate-50 text-slate-600 hover:bg-purple-50 hover:text-purple-600'"
              @click="activeContinent = continent; activeCountry = '全部'"
            >
              {{ continent === '全部' ? '🌐 全部' : continent }}
            </button>
          </div>
          <!-- 重置按钮 -->
          <button 
            class="px-4 py-2 rounded-full text-xs font-bold bg-slate-100 text-slate-500 hover:bg-red-50 hover:text-red-600 transition-all"
            @click="resetFilters"
          >
            🔄 重置
          </button>
        </div>
        
        <!-- Filter 1: 服务类型 -->
        <div class="flex items-center gap-3 pb-5 border-b border-slate-100">
          <div class="text-xs font-bold text-slate-500 uppercase tracking-wider min-w-[60px]">服务：</div>
          <div class="flex flex-wrap gap-3 flex-1">
            <button 
              v-for="type in serviceTypes" 
              :key="type"
              class="px-5 py-2 rounded-full text-xs font-bold transition-all hover:scale-105"
              :class="activeType === type ? 'bg-brand-600 text-white shadow-md shadow-brand-500/30' : 'bg-slate-50 text-slate-600 hover:bg-brand-50 hover:text-brand-600'"
              @click="activeType = type"
            >
              {{ type }}
            </button>
          </div>
        </div>

        <!-- Filter 2: 服务国家 ✅ 优化：全部选项显示地球图标 -->
        <div class="relative group">
          <div class="absolute left-0 top-0 bottom-0 w-8 bg-gradient-to-r from-white to-transparent z-10 pointer-events-none"></div>
          <div class="absolute right-0 top-0 bottom-0 w-8 bg-gradient-to-l from-white to-transparent z-10 pointer-events-none"></div>
          
          <div class="overflow-x-auto no-scrollbar py-1">
            <div class="flex gap-3 min-w-max px-2">
              <button 
                v-for="country in displayCountries" 
                :key="country"
                class="px-5 py-2 rounded-lg text-xs font-medium border transition-colors whitespace-nowrap flex items-center gap-2"
                :class="activeCountry === country ? 'border-brand-500 text-brand-600 bg-brand-50' : 'border-slate-200 text-slate-500 hover:border-brand-300 hover:text-slate-700'"
                @click="activeCountry = country"
              >
                <!-- ✅ 修复：全部选项显示地球图标 -->
                <span v-if="country === '全部'" class="text-base">🌐</span>
                <img v-else :src="`https://flagcdn.com/20x15/${getCountryCode(country)}.png`" class="w-4 h-3 rounded-[1px] opacity-80" />
                {{ country }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 信息展示页面（列表）✅ 优化：显示评分和服务类型 -->
    <!-- 合作伙伴广告位 -->
    <div class="container mx-auto px-4 mt-8">
        <div
          class="relative w-full h-40 rounded-2xl border border-slate-200 bg-gradient-to-r from-brand-700 to-brand-600 px-6 md:px-10 text-white shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all flex items-center"
          @click="navigateTo('/supplier-directory')"
        >
          <div class="absolute right-3 top-3 px-2 py-0.5 text-[10px] rounded border border-white/20 bg-white/10">合作伙伴</div>
          <div class="max-w-2xl">
            <div class="text-xs text-blue-200 font-semibold mb-2">精选合作伙伴</div>
            <div class="text-lg md:text-2xl font-bold mb-1">服务商风采 · 合作伙伴广告位</div>
            <p class="text-xs text-slate-200">展示优质服务点与合作伙伴，助力企业出海与落地服务。</p>
          </div>
          <div class="ml-auto hidden md:block">
            <div class="bg-white text-slate-900 px-5 py-2 rounded-lg text-xs font-bold">了解合作</div>
          </div>
        </div>
    </div>

    <div class="container mx-auto px-4 mt-12 mb-20">
      <div class="flex justify-between items-end mb-6">
        <h2 class="text-2xl font-bold text-slate-900">推荐服务商</h2>
        <span class="text-sm text-slate-500">共筛选出 {{ filteredPoints.length }} 个优质服务点</span>
      </div>

      <!-- ✅ 新增：无结果提示 -->
      <div v-if="filteredPoints.length === 0" class="text-center py-20">
        <div class="text-6xl mb-4">🔍</div>
        <div class="text-xl font-bold text-slate-700 mb-2">暂无匹配结果</div>
        <div class="text-sm text-slate-500 mb-6">请尝试调整筛选条件</div>
        <button class="px-6 py-2 bg-brand-600 text-white rounded-lg hover:bg-brand-700 transition-colors" @click="resetFilters">
          重置筛选条件
        </button>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div 
          v-for="point in filteredPoints" 
          :key="point.id"
          :id="`card-${point.id}`"
          class="group relative bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-brand-200 transition-all duration-300 overflow-hidden cursor-pointer flex flex-col"
          @click="openServiceDetail(point)"
        >
          <!-- 宣传图 -->
          <div class="h-48 overflow-hidden relative flex-shrink-0">
            <img :src="point.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 to-transparent"></div>
            
            <!-- ✅ 新增：服务类型标签 -->
            <div class="absolute top-3 right-3 px-2 py-1 bg-brand-600/90 backdrop-blur-sm text-white text-[10px] font-bold rounded-full">
              {{ point.serviceType }}
            </div>
            
            <div class="absolute bottom-4 left-4 text-white">
              <div class="text-xs font-medium opacity-90 mb-1 flex items-center gap-1.5">
                <img :src="`https://flagcdn.com/20x15/${getCountryCode(point.country)}.png`" class="w-4 h-3 rounded-[1px]" />
                {{ point.country }}
              </div>
              <h3 class="text-xl font-bold tracking-wide">{{ point.city }}</h3>
              <!-- ✅ 新增：评分显示 -->
              <div class="text-xs text-yellow-400 mt-1">
                ⭐ {{ point.rating }} · {{ point.successCases }}+ 成功案例
              </div>
            </div>
          </div>
          
          <!-- 内容摘要 -->
          <div class="p-6 flex flex-col flex-grow">
            <div class="mb-4">
              <h4 class="font-bold text-slate-800 text-sm mb-2 line-clamp-1">{{ point.city }} 市场准入与合规咨询</h4>
              <p class="text-xs text-slate-500 leading-relaxed line-clamp-3">
                {{ point.desc }}
              </p>
            </div>
            
            <div class="mt-auto pt-4 border-t border-slate-50 flex items-center justify-between">
              <div class="flex items-center gap-2 text-xs text-slate-500">
                <el-icon><TrendCharts /></el-icon> {{ point.inquiryCount }} 人已咨询
              </div>
              <button class="px-4 py-1.5 rounded-full bg-slate-900 text-white text-xs font-bold hover:bg-brand-600 transition-colors flex items-center gap-1">
                立即咨询 <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 服务方案详情与咨询弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailData?.city + ' - 服务咨询'" width="900px" align-center class="custom-dialog">
      <div v-if="detailData" class="flex flex-col md:flex-row gap-8">
        <!-- 左侧信息 -->
        <div class="w-full md:w-1/3">
          <div class="aspect-[3/4] rounded-xl overflow-hidden shadow-lg border border-slate-200 relative mb-4">
            <img :src="detailData.image" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex flex-col justify-end p-4 text-white">
              <div class="font-bold text-lg">{{ detailData.country }}出海全案</div>
              <div class="text-xs text-slate-300 mt-1">⭐ {{ detailData.rating }} · {{ detailData.successCases }}+ 案例</div>
            </div>
          </div>
        </div>

        <!-- 右侧表单 -->
        <div class="w-full md:w-2/3 space-y-5">
          <div class="bg-blue-50 p-4 rounded-lg text-sm text-blue-800">
            <div class="font-bold mb-1">服务内容：</div>
            {{ detailData.desc }}
          </div>

          <el-form label-position="top">
            <el-form-item label="咨询诉求描述">
              <el-input type="textarea" :rows="3" placeholder="请简述您的需求..." />
            </el-form-item>
            
            <el-form-item label="项目资料上传 (选填)">
              <el-upload
                class="w-full"
                drag
                action="#"
                multiple
                :auto-upload="false"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  拖拽文件到此处或 <em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">支持 PDF/Word/Excel，单个文件不超过 10MB</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-form>

          <div class="pt-4 border-t border-slate-100 flex gap-4">
            <button class="flex-1 py-3 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl shadow-lg transition-all" @click="handleConsult">
              提交咨询并上传
            </button>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { onMounted, ref, computed, watch, nextTick } from 'vue'
import { ChatDotRound, ArrowRight, Location, Close, TrendCharts, Download, UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

useHead({ title: '海外服务 - XRIPP全球公共采购服务平台' })

const route = useRoute()
const hideMapBanner = computed(() => route.query.mode === 'delivery')

// ==================== 1. 真实服务点数据 (客户提供) ====================
const servicePoints = [
    // 🇨🇳 总部（新增）
  { 
    id: 'CN01', 
    city: '上海', 
    country: '中国', 
    lat: 31.23, 
    lng: 121.47, 
    manager: '总部运营中心', 
    services: ['标书翻译', '服务商注册', '法律咨询', '资源对接'], 
    continent: '亚洲', 
    serviceType: '跨境企服', 
    rating: 5.0, 
    responseTime: '即时', 
    successCases: 999 
  },

  // 🌏 亚洲
  { id: 'AS01', city: '新加坡', country: '新加坡', lat: 1.35, lng: 103.82, manager: 'Tan先生', services: ['金融牌照', '离岸架构', '税务筹划'], continent: '亚洲', serviceType: '跨境企服', rating: 4.9, responseTime: '2小时', successCases: 156 },
  { id: 'AS02', city: '金边', country: '柬埔寨', lat: 11.56, lng: 104.93, manager: 'Sok经理', services: ['特区入驻', '劳工政策'], continent: '亚洲', serviceType: '外贸服务', rating: 4.6, responseTime: '4小时', successCases: 89 },
  { id: 'AS03', city: '宿务', country: '菲律宾', lat: 10.31, lng: 123.89, manager: 'Santos先生', services: ['BPO外包', '物流服务'], continent: '亚洲', serviceType: '外贸服务', rating: 4.5, responseTime: '6小时', successCases: 72 },
  { id: 'AS04', city: '马尼拉', country: '菲律宾', lat: 14.60, lng: 120.98, manager: 'Cruz女士', services: ['制造业咨询', '港口对接'], continent: '亚洲', serviceType: '出海投资', rating: 4.7, responseTime: '3小时', successCases: 95 },
  { id: 'AS05', city: '曼谷', country: '泰国', lat: 13.75, lng: 100.50, manager: 'Sompong先生', services: ['BOI申请', '税务筹划'], continent: '亚洲', serviceType: '跨境企服', rating: 4.8, responseTime: '2小时', successCases: 142 },
  { id: 'AS06', city: '东京', country: '日本', lat: 35.68, lng: 139.65, manager: '佐藤先生', services: ['技术认证', '知识产权'], continent: '亚洲', serviceType: '知识产权', rating: 4.9, responseTime: '1小时', successCases: 203 },
  { id: 'AS07', city: '胡志明市', country: '越南', lat: 10.82, lng: 106.63, manager: '阮经理', services: ['建厂选址', '劳工招聘'], continent: '亚洲', serviceType: '出海投资', rating: 4.7, responseTime: '3小时', successCases: 118 },
  { id: 'AS08', city: '河内', country: '越南', lat: 21.03, lng: 105.85, manager: '黎经理', services: ['政府对接', '园区入驻'], continent: '亚洲', serviceType: '出海投资', rating: 4.6, responseTime: '4小时', successCases: 97 },
  { id: 'AS09', city: '阿布扎比', country: '阿联酋', lat: 24.47, lng: 54.37, manager: 'Ahmed先生', services: ['能源项目', '自贸区注册'], continent: '亚洲', serviceType: '出海投资', rating: 4.8, responseTime: '2小时', successCases: 134 },
  { id: 'AS10', city: '迪拜', country: '阿联酋', lat: 25.20, lng: 55.27, manager: 'Khalid先生', services: ['中东市场拓展', '展会对接'], continent: '亚洲', serviceType: '外贸服务', rating: 4.9, responseTime: '1小时', successCases: 178 },
  { id: 'AS11', city: '吉隆坡', country: '马来西亚', lat: 3.14, lng: 101.69, manager: '李经理', services: ['清关服务', '本地化支持'], continent: '亚洲', serviceType: '外贸服务', rating: 4.7, responseTime: '3小时', successCases: 126 },
  { id: 'AS12', city: '香港', country: '中国', lat: 22.32, lng: 114.17, manager: '陈经理', services: ['离岸公司', '跨境金融'], continent: '亚洲', serviceType: '跨境企服', rating: 4.9, responseTime: '1小时', successCases: 245 },
  { id: 'AS13', city: '德黑兰', country: '伊朗', lat: 35.69, lng: 51.42, manager: 'Reza先生', services: ['能源合作', '政策咨询'], continent: '亚洲', serviceType: '出海投资', rating: 4.3, responseTime: '8小时', successCases: 54 },
  { id: 'AS14', city: '雅加达', country: '印度尼西亚', lat: -6.21, lng: 106.85, manager: 'Budi先生', services: ['采矿许可', '环评服务'], continent: '亚洲', serviceType: '出海投资', rating: 4.5, responseTime: '5小时', successCases: 81 },

  // 🌍 欧洲
  { id: 'EU01', city: '柏林', country: '德国', lat: 52.52, lng: 13.41, manager: 'Schmidt先生', services: ['工业4.0认证', '欧盟准入'], continent: '欧洲', serviceType: '跨境企服', rating: 4.8, responseTime: '2小时', successCases: 167 },
  { id: 'EU02', city: '莫斯科', country: '俄罗斯', lat: 55.76, lng: 37.62, manager: 'Ivanov先生', services: ['关税咨询', '物流通道'], continent: '欧洲', serviceType: '外贸服务', rating: 4.4, responseTime: '6小时', successCases: 73 },
  { id: 'EU03', city: '巴黎', country: '法国', lat: 48.86, lng: 2.35, manager: 'Dubois女士', services: ['品牌注册', '展会对接'], continent: '欧洲', serviceType: '知识产权', rating: 4.7, responseTime: '3小时', successCases: 139 },
  { id: 'EU04', city: '爱丁堡', country: '英国', lat: 55.95, lng: -3.19, manager: 'Campbell先生', services: ['金融服务', '法律合规'], continent: '欧洲', serviceType: '跨境企服', rating: 4.6, responseTime: '4小时', successCases: 102 },
  { id: 'EU05', city: '曼彻斯特', country: '英国', lat: 53.48, lng: -2.24, manager: 'Brown女士', services: ['制造业对接', '供应链优化'], continent: '欧洲', serviceType: '出海投资', rating: 4.7, responseTime: '3小时', successCases: 115 },
  { id: 'EU06', city: '伦敦', country: '英国', lat: 51.51, lng: -0.13, manager: 'Wilson先生', services: ['欧洲法律合规', '投资并购'], continent: '欧洲', serviceType: '跨境企服', rating: 4.9, responseTime: '1小时', successCases: 198 },
  { id: 'EU07', city: '米兰', country: '意大利', lat: 45.46, lng: 9.19, manager: 'Rossi先生', services: ['时尚品牌', '设计产业'], continent: '欧洲', serviceType: '知识产权', rating: 4.6, responseTime: '4小时', successCases: 91 },
  { id: 'EU08', city: '佛罗伦萨', country: '意大利', lat: 43.77, lng: 11.26, manager: 'Bianchi女士', services: ['文化创意', '艺术品交易'], continent: '欧洲', serviceType: '其他服务', rating: 4.5, responseTime: '5小时', successCases: 68 },
  { id: 'EU09', city: '阿姆斯特丹', country: '荷兰', lat: 52.37, lng: 4.90, manager: 'Van Berg先生', services: ['物流枢纽', '税务筹划'], continent: '欧洲', serviceType: '外贸服务', rating: 4.8, responseTime: '2小时', successCases: 152 },
  { id: 'EU10', city: '里斯本', country: '葡萄牙', lat: 38.72, lng: -9.14, manager: 'Silva女士', services: ['旅游业投资', '房地产'], continent: '欧洲', serviceType: '出海投资', rating: 4.4, responseTime: '6小时', successCases: 76 },

  // 🌎 北美洲
  { id: 'NA01', city: '洛杉矶', country: '美国', lat: 34.05, lng: -118.24, manager: 'Johnson先生', services: ['北美市场准入', '港口物流'], continent: '北美洲', serviceType: '外贸服务', rating: 4.8, responseTime: '2小时', successCases: 164 },
  { id: 'NA02', city: '波士顿', country: '美国', lat: 42.36, lng: -71.06, manager: 'Williams女士', services: ['教育科技', '生物医药'], continent: '北美洲', serviceType: '出海投资', rating: 4.7, responseTime: '3小时', successCases: 128 },
  { id: 'NA03', city: '纽约', country: '美国', lat: 40.71, lng: -74.01, manager: 'Davis先生', services: ['金融服务', '联合国对接'], continent: '北美洲', serviceType: '跨境企服', rating: 4.9, responseTime: '1小时', successCases: 215 },
  { id: 'NA04', city: '华盛顿', country: '美国', lat: 38.91, lng: -77.04, manager: 'Miller女士', services: ['政府采购', '政策游说'], continent: '北美洲', serviceType: '报告下载', rating: 4.6, responseTime: '4小时', successCases: 103 },
  { id: 'NA05', city: '哈瓦那', country: '古巴', lat: 23.11, lng: -82.37, manager: 'Rodriguez先生', services: ['拉美市场', '文化交流'], continent: '北美洲', serviceType: '其他服务', rating: 4.2, responseTime: '8小时', successCases: 47 },
  { id: 'NA06', city: '多伦多', country: '加拿大', lat: 43.65, lng: -79.38, manager: 'Thompson先生', services: ['移民咨询', '资源开发'], continent: '北美洲', serviceType: '跨境企服', rating: 4.7, responseTime: '3小时', successCases: 137 },
  { id: 'NA07', city: '墨西哥城', country: '墨西哥', lat: 19.43, lng: -99.13, manager: 'Garcia先生', services: ['制造业外包', '矿产贸易'], continent: '北美洲', serviceType: '出海投资', rating: 4.5, responseTime: '5小时', successCases: 84 },

  // 🌍 非洲
  { id: 'AF01', city: '约翰内斯堡', country: '南非', lat: -26.20, lng: 28.05, manager: 'Van Der Merwe先生', services: ['矿业投资', '基建项目'], continent: '非洲', serviceType: '出海投资', rating: 4.5, responseTime: '5小时', successCases: 79 },
  { id: 'AF02', city: '马普托', country: '莫桑比克', lat: -25.97, lng: 32.57, manager: 'Macamo女士', services: ['港口开发', '能源合作'], continent: '非洲', serviceType: '出海投资', rating: 4.3, responseTime: '7小时', successCases: 56 },
  { id: 'AF03', city: '卡萨布兰卡', country: '摩洛哥', lat: 33.57, lng: -7.59, manager: 'El Fassi先生', services: ['非洲门户', '法语区对接'], continent: '非洲', serviceType: '外贸服务', rating: 4.4, responseTime: '6小时', successCases: 68 },
  { id: 'AF04', city: '达累斯萨拉姆', country: '坦桑尼亚', lat: -6.79, lng: 39.27, manager: 'Mwamba先生', services: ['农业项目', '交通基建'], continent: '非洲', serviceType: '出海投资', rating: 4.2, responseTime: '8小时', successCases: 51 },

  // 🌏 大洋洲
  { id: 'OC01', city: '悉尼', country: '澳大利亚', lat: -33.87, lng: 151.21, manager: 'Anderson女士', services: ['大洋洲项目支持', '矿产贸易'], continent: '大洋洲', serviceType: '出海投资', rating: 4.7, responseTime: '3小时', successCases: 129 },
  { id: 'OC02', city: '墨尔本', country: '澳大利亚', lat: -37.81, lng: 144.96, manager: 'Taylor先生', services: ['教育产业', '农业出口'], continent: '大洋洲', serviceType: '外贸服务', rating: 4.6, responseTime: '4小时', successCases: 96 },
  { id: 'OC03', city: '奥克兰', country: '新西兰', lat: -36.85, lng: 174.76, manager: 'Smith女士', services: ['乳制品', '旅游业'], continent: '大洋洲', serviceType: '外贸服务', rating: 4.5, responseTime: '5小时', successCases: 73 }
]

// ==================== 2. 数据处理与增强 ====================
// 补全所有新国家的国旗代码
const getCountryCode = (name: string) => {
  const map: Record<string, string> = {
    '新加坡': 'sg', '柬埔寨': 'kh', '菲律宾': 'ph', '泰国': 'th', '日本': 'jp',
    '越南': 'vn', '阿联酋': 'ae', '马来西亚': 'my', '中国': 'cn', '伊朗': 'ir',
    '印度尼西亚': 'id', '德国': 'de', '俄罗斯': 'ru', '法国': 'fr', '英国': 'gb',
    '意大利': 'it', '荷兰': 'nl', '葡萄牙': 'pt', '瑞士': 'ch', '美国': 'us',
    '古巴': 'cu', '加拿大': 'ca', '墨西哥': 'mx', '南非': 'za', '莫桑比克': 'mz',
    '摩洛哥': 'ma', '坦桑尼亚': 'tz', '肯尼亚': 'ke', '澳大利亚': 'au', '新西兰': 'nz'
  }
  return map[name] || 'cn'
}

// ECharts 地图名称映射
const nameMap = {
  'Singapore': '新加坡', 'Cambodia': '柬埔寨', 'Philippines': '菲律宾',
  'Thailand': '泰国', 'Japan': '日本', 'Vietnam': '越南',
  'United Arab Emirates': '阿联酋', 'Malaysia': '马来西亚',
  'China': '中国', 'Iran': '伊朗', 'Indonesia': '印度尼西亚',
  'Germany': '德国', 'Russia': '俄罗斯', 'France': '法国',
  'United Kingdom': '英国', 'Italy': '意大利', 'Netherlands': '荷兰',
  'Portugal': '葡萄牙', 'Switzerland': '瑞士',
  'United States of America': '美国', 'United States': '美国',
  'Cuba': '古巴', 'Canada': '加拿大', 'Mexico': '墨西哥',
  'South Africa': '南非', 'Mozambique': '莫桑比克',
  'Morocco': '摩洛哥', 'Tanzania': '坦桑尼亚', 'Kenya': '肯尼亚',
  'Australia': '澳大利亚', 'New Zealand': '新西兰'
}

// 数据增强：图片、描述、热度
const enhancedPoints = servicePoints.map(p => ({
  ...p,
  desc: `提供${p.country}本地化一站式服务，包括公司注册、税务合规、法律咨询等。我们的团队拥有10年以上${p.country}市场经验，已服务超过${p.successCases}家中国企业成功出海。`,
  mainService: p.services[0],
  inquiryCount: Math.floor(Math.random() * 300) + 200, 
  image: 
    // ✅ 新增：上海总部专属封面
    p.city === '上海' ? '/images/overseas/shanghai.jpeg' :
    p.city === '新加坡' ? 'https://images.unsplash.com/photo-1525625293386-3f8f99389edd?q=80&w=800' :
    p.city === '吉隆坡' ? 'https://images.unsplash.com/photo-1596422846543-75c6fc197f07?q=80&w=800' :
    p.city === '迪拜' ? 'https://images.unsplash.com/photo-1512453979798-5ea90b7cadc9?q=80&w=800' :
    p.city === '东京' ? 'https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?q=80&w=800' :
    p.city === '纽约' ? 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?q=80&w=800' :
    p.city === '伦敦' ? 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?q=80&w=800' :
    p.city === '悉尼' ? 'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=800' :
    p.city === '巴黎' ? 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?q=80&w=800' :
    p.continent === '亚洲' ? 'https://images.unsplash.com/photo-1555217851-6141535bd771?q=80&w=800' :
    p.continent === '欧洲' ? 'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?q=80&w=800' :
    p.continent === '北美洲' ? 'https://images.unsplash.com/photo-1485738422979-f5c462d49f74?q=80&w=800' :
    p.continent === '非洲' ? 'https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?q=80&w=800' :
    'https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?q=80&w=800'
}))

// ==================== 3. 状态与筛选逻辑 ====================
const mapLoaded = ref(false)
const mapOptions = ref<any>({})
let chartInstance: echarts.ECharts | null = null // 保存实例

const selectedPoint = ref(null)
const activeType = ref('全部')
const activeCountry = ref('全部')
const activeContinent = ref('全部')
const detailVisible = ref(false)
const detailData = ref(null)

const serviceTypes = ['全部', '报告下载', '外贸服务', '知识产权', '跨境企服', '出海投资', '其他服务']
const continents = ['全部', '亚洲', '欧洲', '北美洲', '非洲', '大洋洲']

// 动态计算下拉国家列表
const displayCountries = computed(() => {
  let points = enhancedPoints
  if (activeContinent.value !== '全部') {
    points = points.filter(p => p.continent === activeContinent.value)
  }
  return ['全部', ...new Set(points.map(p => p.country))]
})

// 核心筛选逻辑
const filteredPoints = computed(() => {
  return enhancedPoints.filter(p => {
    const matchType = activeType.value === '全部' || p.serviceType === activeType.value
    const matchCountry = activeCountry.value === '全部' || p.country === activeCountry.value
    const matchContinent = activeContinent.value === '全部' || p.continent === activeContinent.value
    return matchType && matchCountry && matchContinent
  })
})

// 动态统计数据
const displayStats = computed(() => {
  const countries = new Set(filteredPoints.value.map(p => p.country))
  return {
    countryCount: countries.size,
    pointCount: filteredPoints.value.length
  }
})

// ==================== 4. 交互逻辑 ====================
const handleMapClick = (params: any) => {
  let targetPoint = null
  if (params.componentType === 'series') {
    targetPoint = enhancedPoints.find(p => p.city === params.name)
  } else if (params.componentType === 'geo') {
    targetPoint = enhancedPoints.find(p => p.country === params.name)
  }
  selectedPoint.value = targetPoint || null
}

const openServiceDetail = (point: any) => {
  detailData.value = point
  detailVisible.value = true
}

const handleConsult = () => {
  ElMessage.success('已收到您的咨询请求，客服将稍后联系您')
  detailVisible.value = false
}

const resetFilters = () => {
  activeType.value = '全部'
  activeCountry.value = '全部'
  activeContinent.value = '全部'
}

// ✅ 核心修复：监听筛选变化，动态更新地图点位
watch(filteredPoints, (newPoints) => {
  if (!mapOptions.value.series) return
  
  // 更新地图数据
  const newMapData = newPoints.map(p => ({ name: p.city, value: [p.lng, p.lat] }))
  
  mapOptions.value = {
    ...mapOptions.value, // 保留 Geo 配置
    series: [
      {
        ...mapOptions.value.series[0],
        data: newMapData // 仅替换数据
      }
    ]
  }
})

// ==================== 5. 初始化 ====================
onMounted(async () => {
  try {
    const response = await fetch('/maps/world.json')
    if (!response.ok) throw new Error('Map file not found')
    const geoJson = await response.json()
    echarts.registerMap('world', geoJson)
    
    // 初始数据
    const mapData = enhancedPoints.map(p => ({ name: p.city, value: [p.lng, p.lat] }))
    
    mapOptions.value = {
      backgroundColor: 'transparent',
      tooltip: { show: false },
      geo: {
        map: 'world',
        roam: true,
        zoom: 1.25,
        top: 'middle',
        label: { show: false },
        nameMap: nameMap,
        itemStyle: { areaColor: '#1e293b', borderColor: '#475569', borderWidth: 1 },
        emphasis: {
          disabled: false,
          label: { show: true, color: '#fff' },
          itemStyle: { areaColor: '#38bdf8', shadowBlur: 10, shadowColor: 'rgba(56, 189, 248, 0.5)' }
        },
        select: {
          itemStyle: { areaColor: '#0284c7', shadowBlur: 0 },
          label: { show: true, color: '#fff' }
        },
        selectedMode: 'single'
      },
      series: [
        {
          type: 'effectScatter',
          coordinateSystem: 'geo',
          data: mapData,
          symbolSize: 12,
          itemStyle: { color: '#facc15', shadowBlur: 10, shadowColor: '#facc15' },
          rippleEffect: { scale: 4, brushType: 'stroke', color: '#facc15' },
          emphasis: {
            scale: true,
            itemStyle: { color: '#fff', borderColor: '#facc15', borderWidth: 2 },
            label: { show: true, formatter: '{b}', position: 'right', color: '#fff', backgroundColor: 'rgba(0,0,0,0.5)', padding: [4, 8], borderRadius: 4 }
          },
          zlevel: 1
        }
      ]
    }
    mapLoaded.value = true
  } catch (error) {
    console.error('地图加载失败:', error)
  }
})
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 1s ease-out; }
.animate-slide-in-left { animation: slideInLeft 0.5s ease-out; }
.slide-right-enter-active, .slide-right-leave-active { transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(50px); opacity: 0; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideInLeft { from { opacity: 0; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
:deep(.custom-dialog .el-dialog__body) { padding: 30px; }
:deep(.custom-dialog) { border-radius: 16px; overflow: hidden; }
</style>
