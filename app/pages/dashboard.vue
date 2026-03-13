<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\dashboard.vue
  版本: V5.1 (2026-02-06 地图与布局修复版)
  
  ✅ 修复:
  1. [关键] 地图切换后消失 — 改用 v-show 替代 v-if，避免DOM销毁导致图表丢失
  2. [关键] 图表重绘机制 — watch activeTab 时不再 dispose/recreate，改为 resize 已有图表
  3. [布局] Page2 会员面板溢出 — 调整flex权重和内部间距
  4. [稳定] 首次加载时分页渲染所有图表，切换只做 show/hide + resize
  ========================================================================
-->
<template>
  <div class="dashboard-root">
    <div class="bg-effects"><div class="glow glow-1"></div><div class="glow glow-2"></div></div>

    <!-- ═══ Header (80px) ═══ -->
    <header class="header-bar">
      <div class="header-bg-fallback"></div>
      <div class="scan-line"></div>
      <div class="header-content">
        <div class="header-left">
          <button class="back-btn" @click="navigateTo('/admin')">
            <el-icon class="back-icon"><Back /></el-icon><span>返回管理后台</span>
          </button>
        </div>
        <div class="header-center">
          <div class="title-frame">
            <span class="title-wing left"></span>
            <h1 class="main-title">XRIPP国际公共采购服务平台</h1>
            <span class="title-wing right"></span>
          </div>

          <div class="tab-bar">
            <button v-for="(tab, i) in tabLabels" :key="i" class="tab-btn" :class="{ active: activeTab === i }" @click="manualSwitch(i)">
              <span class="tab-text">{{ tab }}</span>
              <div v-if="activeTab === i && isAutoPlay" class="tab-progress" :style="{ width: progressPct + '%' }"></div>
            </button>
          </div>
        </div>
        <div class="header-right">
          <div class="time-display">{{ currentTime }}</div>
          <div class="date-display">{{ currentDate }}</div>
        </div>
      </div>
    </header>

    <!-- ═══ Main — 关键改动：用 v-show 替代 v-if，避免 DOM 销毁 ═══ -->
    <main class="main-area">

      <!-- ══════════ PAGE 1: 公共采购态势 ══════════ -->
      <div v-show="activeTab===0" class="page-grid">
        <aside class="col-left">
          <div class="panel flex-2">
            <div class="panel-header"><el-icon><Reading /></el-icon> 公共采购项目概述</div>
            <div class="panel-body pad text-body">
              <p>国际公共采购是指国际组织、各国政府及非政府组织依法在国际市场采购货物、工程和服务。平台汇集联合国及其专门机构、世界银行及各国政府采购信息，为中国企业提供国际采购准入服务，助力中国企业走向世界。</p>
            </div>
          </div>
          <div class="panel flex-25">
            <div class="panel-header"><el-icon><DataLine /></el-icon> 平台采购数据</div>
            <div class="panel-body pad un-data-body">
              <div class="un-grid-3">
                <div class="un-card">
                  <div class="un-label">已发布标书</div>
                  <div class="un-value grad-blue">{{ cfg.platformData.totalPublished }}</div>
                </div>
                <div class="un-card">
                  <div class="un-label">采购机构数</div>
                  <div class="un-value grad-yellow">{{ cfg.platformData.organizationCount }}</div>
                </div>
                <div class="un-card">
                  <div class="un-label">本月新增标书</div>
                  <div class="un-value grad-green">{{ cfg.platformData.monthlyPublished }}</div>
                </div>
              </div>
              <div class="un-meta">
                <span class="un-label">覆盖国家</span>
                <span class="un-value grad-blue">{{ cfg.platformData.countryCount }} 个</span>
              </div>
            </div>
          </div>
          <div class="panel flex-25">
            <div class="panel-header"><el-icon><Connection /></el-icon> 采购机构滚动榜</div>
            <div class="panel-body scroll-box">
              <div class="scroll-mask-t"></div><div class="scroll-mask-b"></div>
              <div class="scroll-track anim-scroll">
                <div v-for="(o,i) in dblOrg" :key="'o'+i" class="scroll-row">
                  <span class="scroll-name">{{ o.name }}</span><span class="scroll-tag">{{ o.code }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="panel flex-3">
            <div class="panel-header"><el-icon><PieChart /></el-icon> 采购机构分布</div>
            <div class="panel-body chart-wrap"><div ref="c_p1_pie" class="chart-el"></div></div>
          </div>
        </aside>

        <section class="col-center">
          <div class="panel panel-map">
            <div class="map-overlay-title"><h2>国内服务覆盖热力分布</h2></div>
            <div ref="c_p1_map" class="chart-el"></div>

            <!-- 第1页国际小图（左下角） -->
            <div class="mini-world-overlay p1-mini">
              <div class="mini-world-title">国际采购热区</div>
              <div ref="c_p1_worldMini" class="mini-world-chart"></div>
              <div v-if="!maps.world" class="mini-world-mask">国际地图加载中...</div>
            </div>

            <div v-if="!maps.china" class="map-loading"><el-icon class="spin"><Loading /></el-icon><span>正在加载地理数据...</span></div>
            <div class="map-stats">
              <div class="ms">
                <div class="ms-v yellow">{{ cfg.countryCount }}</div>
                <div class="ms-l">覆盖国家</div>
              </div>
              <div class="ms">
                <div class="ms-v blue">{{ cfg.memberCount.toLocaleString() }}</div>
                <div class="ms-l">入驻会员</div>
              </div>
              <div class="ms">
                <div class="ms-v green">{{ cfg.tenderCount.toLocaleString() }}</div>
                <div class="ms-l">已发布标书</div>
              </div>
            </div>
          </div>
        </section>

        <aside class="col-right">
          <div class="panel flex-25">
            <div class="panel-header"><el-icon><Odometer /></el-icon> 公共采购数据概览</div>
            <div class="panel-body pad"><div class="kpi-grid">
              <div class="kpi"><div class="kpi-l">招标信息总量</div><div class="kpi-v">{{ cfg.tenderCount.toLocaleString() }}</div></div>
              <div class="kpi"><div class="kpi-l">注册企业会员</div><div class="kpi-v">{{ cfg.memberCount.toLocaleString() }}</div></div>
              <div class="kpi"><div class="kpi-l">覆盖目标国家</div><div class="kpi-v">{{ cfg.countryCount }}</div></div>
              <div class="kpi"><div class="kpi-l">本月新增标书</div><div class="kpi-v">{{ cfg.platformData.monthlyPublished }}</div></div>
            </div></div>
          </div>
          <div class="panel flex-35">
            <div class="panel-header"><el-icon><TrendCharts /></el-icon> {{ cfg.year }} 招标类目排名</div>
            <div class="panel-body chart-wrap"><div ref="c_p1_bar1" class="chart-el"></div></div>
          </div>
          <div class="panel flex-35">
            <div class="panel-header"><el-icon><Flag /></el-icon> 受惠国采购数量排名</div>
            <div class="panel-body chart-wrap"><div ref="c_p1_bar2" class="chart-el"></div></div>
          </div>
        </aside>
      </div>

      <!-- ══════════ PAGE 2: 会员生态 ══════════ -->
      <div v-show="activeTab===1" class="page-grid">
        <aside class="col-left">
          <!-- 修复：flex权重从2.5调整为3，给会员面板更多空间 -->
          <div class="panel flex-3">
            <div class="panel-header"><el-icon><User /></el-icon> 会员入驻情况</div>
            <div class="panel-body pad mem-panel">
              <!-- 核心数字行：紧凑横排 -->
              <div class="mem-top">
                <div class="mem-avatar"><el-icon><UserFilled /></el-icon></div>
                <div class="mem-top-info">
                  <div class="mem-big">{{ cfg.memberCount.toLocaleString() }}</div>
                  <div class="mem-sub">入驻会员总数</div>
                </div>
              </div>
              <!-- 四项数据：单行四列，避免高度溢出 -->
              <div class="mem-strip">
                <div class="mem-cell"><span class="mc-v">{{ cfg.svipCount.toLocaleString() }}</span><span class="mc-l">SVIP会员</span></div>
                <div class="mem-cell"><span class="mc-v">{{ cfg.vipCount.toLocaleString() }}</span><span class="mc-l">VIP会员</span></div>
                <div class="mem-cell"><span class="mc-v green">+{{ cfg.monthlyNewMembers }}</span><span class="mc-l">本月新增</span></div>
                <div class="mem-cell"><span class="mc-v">{{ cfg.industryCount }}</span><span class="mc-l">覆盖行业</span></div>
              </div>
            </div>
          </div>
          <!-- 调整：折线图flex从3.5降为3 -->
          <div class="panel flex-3">
            <div class="panel-header"><el-icon><DataLine /></el-icon> 月度新增会员趋势</div>
            <div class="panel-body chart-wrap"><div ref="c_p2_line" class="chart-el"></div></div>
          </div>
          <div class="panel flex-4">
            <div class="panel-header"><el-icon><PieChart /></el-icon> 会员等级占比</div>
            <div class="panel-body chart-wrap"><div ref="c_p2_pie" class="chart-el"></div></div>
          </div>
        </aside>

        <section class="col-center">
          <div class="panel panel-map">
            <div class="map-overlay-title"><h2>会员服务城市分布</h2></div>
            <div ref="c_p2_map" class="chart-el"></div>
            <div v-if="!maps.china" class="map-loading"><el-icon class="spin"><Loading /></el-icon><span>正在加载...</span></div>
          </div>
        </section>

        <aside class="col-right">
          <div class="panel flex-4">
            <div class="panel-header"><el-icon><Trophy /></el-icon> 会员服务城市排名</div>
            <div class="panel-body chart-wrap"><div ref="c_p2_bar" class="chart-el"></div></div>
          </div>
          <div class="panel flex-35">
            <div class="panel-header"><el-icon><List /></el-icon> 最新入驻企业</div>
            <div class="panel-body scroll-box">
              <div class="scroll-mask-t"></div><div class="scroll-mask-b"></div>
              <div class="scroll-track anim-scroll-slow">
                <div v-for="(c,i) in dblComp" :key="'c'+i" class="scroll-row">
                  <span class="dot-g"></span><span class="scroll-name">{{ c }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="panel flex-2">
            <div class="panel-header"><el-icon><InfoFilled /></el-icon> 会员情况概述</div>
            <div class="panel-body pad text-body">
              <p>至今XRIPP已发展了{{ cfg.memberCount.toLocaleString() }}家会员单位，当前会员服务已覆盖 {{ cfg.cityRank.length }} 个重点城市，并形成了 {{ cfg.industryCount }} 个行业方向的服务能力。</p>
            </div>
          </div>
        </aside>
      </div>

      <!-- ══════════ PAGE 3: 服务网络 ══════════ -->
      <div v-show="activeTab===2" class="page-grid-footer">
        <div class="page-grid-body">
          <aside class="col-left">
            <div class="panel flex-2">
              <div class="panel-header"><el-icon><HomeFilled /></el-icon> 国内服务点概况</div>
              <div class="panel-body pad"><div class="trio">
                <div class="trio-i"><div class="trio-n">{{ cfg.dp.count }}</div><div class="trio-t">服务点</div></div>
                <div class="trio-i"><div class="trio-n">{{ cfg.dp.provinces }}</div><div class="trio-t">覆盖省份</div></div>
                <div class="trio-i"><div class="trio-n">{{ cfg.dp.cities }}</div><div class="trio-t">服务城市</div></div>
              </div></div>
            </div>
            <div class="panel flex-4">
              <div class="panel-header"><el-icon><Histogram /></el-icon> 省份服务点排名</div>
              <div class="panel-body chart-wrap"><div ref="c_p3_bL" class="chart-el"></div></div>
            </div>
            <div class="panel flex-4">
              <div class="panel-header"><el-icon><LocationInformation /></el-icon> 国内服务点列表</div>
              <div class="panel-body scroll-box">
                <div class="scroll-mask-t"></div><div class="scroll-mask-b"></div>
                <div class="scroll-track anim-scroll">
                  <div v-for="(p,i) in dblDp" :key="'dp'+i" class="scroll-row">
                    <el-icon class="ic-blue"><Location /></el-icon>
                    <div class="scroll-col"><span class="scroll-name">{{ p.name }}</span><span class="scroll-sub">{{ p.location }}</span></div>
                  </div>
                </div>
              </div>
            </div>
          </aside>

          <section class="col-center">
            <div class="panel panel-map">
              <div class="map-overlay-title"><h2>中国服务网络分布</h2></div>
              <div ref="c_p3_map" class="chart-el"></div>

              <!-- 左下角国际分布子图 -->
              <div class="mini-world-overlay">
                <div class="mini-world-title">国际服务点分布</div>
                <div ref="c_p3_worldMini" class="mini-world-chart"></div>
                <div v-if="!maps.world" class="mini-world-mask">国际地图加载中...</div>
              </div>

              <div v-if="!maps.china" class="map-loading"><el-icon class="spin"><Loading /></el-icon><span>正在加载...</span></div>
            </div>
          </section>

          <aside class="col-right">
            <div class="panel flex-2">
              <div class="panel-header"><el-icon><Promotion /></el-icon> 国际服务点概况</div>
              <div class="panel-body pad"><div class="trio">
                <div class="trio-i"><div class="trio-n">{{ cfg.ip.count }}</div><div class="trio-t">服务点</div></div>
                <div class="trio-i"><div class="trio-n">{{ cfg.ip.countries }}</div><div class="trio-t">覆盖国家</div></div>
                <div class="trio-i"><div class="trio-n">{{ cfg.ip.cities }}</div><div class="trio-t">服务城市</div></div>
              </div></div>
            </div>
            <div class="panel flex-4">
              <div class="panel-header"><el-icon><Histogram /></el-icon> 服务点国家排名</div>
              <div class="panel-body chart-wrap"><div ref="c_p3_bR" class="chart-el"></div></div>
            </div>
            <div class="panel flex-4">
              <div class="panel-header"><el-icon><LocationInformation /></el-icon> 国际服务点列表</div>
              <div class="panel-body scroll-box">
                <div class="scroll-mask-t"></div><div class="scroll-mask-b"></div>
                <div class="scroll-track anim-scroll">
                  <div v-for="(p,i) in dblIp" :key="'ip'+i" class="scroll-row">
                    <el-icon class="ic-green"><Position /></el-icon>
                    <div class="scroll-col"><span class="scroll-name">{{ p.name }}</span><span class="scroll-sub">{{ p.location }}</span></div>
                  </div>
                </div>
              </div>
            </div>
          </aside>
        </div>

        <div class="footer-partner">
          <div class="fp-icon"><el-icon><UserFilled /></el-icon></div>
          <div class="fp-body">
            <h3 class="fp-title">城市合伙人服务体系</h3>
            <p class="fp-desc">
              <span class="hl">服务职责：</span>负责本地企业入驻审核、资质初验、线下培训组织及日常咨询服务，作为XRIPP平台在各城市的授权服务代表。
              <span class="hl" style="margin-left:16px">服务范围：</span>覆盖所在城市及周边区域，构建"总部-合伙人-企业"三级服务网络，打通国际公共采购"最后100米"。
            </p>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useNow, useDateFormat } from '@vueuse/core'
import {
  Back, Reading, DataLine, Connection, PieChart, Odometer, TrendCharts, Flag,
  User, UserFilled, Trophy, List, InfoFilled, HomeFilled, Histogram,
  LocationInformation, Location, Promotion, Position, Loading
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

definePageMeta({ layout: false })

const now = useNow()
const currentTime = useDateFormat(now, 'HH:mm:ss')
const currentDate = useDateFormat(now, 'YYYY-MM-DD dddd')

// ═══ Tab & AutoPlay ═══
const tabLabels = ['公共采购项目', '会员分布信息', '国内/国际服务点分布']
const activeTab = ref(0)
const isAutoPlay = ref(true)
const countdown = ref(20)
let autoTimer: any = null
let resumeTimer: any = null

const progressPct = computed(() => ((20 - countdown.value) / 20) * 100)

function manualSwitch(i: number) {
  activeTab.value = i
  countdown.value = 20
  isAutoPlay.value = false
  if (resumeTimer) clearTimeout(resumeTimer)
  resumeTimer = setTimeout(() => { isAutoPlay.value = true }, 60000)
}

function startAuto() {
  if (autoTimer) clearInterval(autoTimer)
  autoTimer = setInterval(() => {
    if (!isAutoPlay.value) return
    countdown.value--
    if (countdown.value <= 0) {
      activeTab.value = (activeTab.value + 1) % 3
      countdown.value = 20
    }
  }, 1000)
}

// ═══ Category Mapping ═══
const categoryMap: Record<string, string> = {
  medical: '医疗设备', it: 'IT通讯', construction: '基建工程',
  office: '办公设备', consulting: '咨询服务', other: '其他'
}

// ═══ Config Data ═══
const { stats: dashboardStats, loaded: statsLoaded } = usePlatformStats()
const cfg = ref({
  year: new Date().getFullYear().toString(), countryCount: 0, memberCount: 0, monthlyNewMembers: 0,
  vipCount: 0, svipCount: 0, industryCount: 0,
  tenderCount: 0,
  platformData: { totalPublished: '0', organizationCount: '0', monthlyPublished: '0', countryCount: '0' },
  dp: { count: 0, provinces: 0, cities: 0 },
  ip: { count: 0, countries: 0, cities: 0 },
  orgList: [] as { name: string; code: string }[],
  orgPieData: [] as { name: string; value: number }[],
  topCat: [] as { name: string; value: number }[],
  topCtry: [] as { name: string; value: number }[],
  memberLevelData: [] as { name: string; value: number }[],
  compTypes: [] as { name: string; value: number }[],
  trend: [] as { m: string; v: number }[],
  cityRank: [] as { name: string; value: number }[],
  compNames: [] as string[],
  provRank: [] as { name: string; value: number }[],
  ctryRank: [] as { name: string; value: number }[],
  dpList: [] as { name: string; location: string }[],
  ipList: [] as { name: string; location: string }[],
  worldData: [] as { name: string; value: number }[],
  projectHeatData: [] as { name: string; value: number }[],
  spCities: [] as { name: string; co: [number, number]; v: number }[],
  intlCities: [] as { name: string; co: [number, number]; v: number }[]
})

const chinaCityCoordMap: Record<string, [number, number]> = {
  北京: [116.40, 39.90],
  上海: [121.47, 31.23],
  深圳: [114.05, 22.54],
  广州: [113.27, 23.13],
  杭州: [120.15, 30.28],
  苏州: [120.59, 31.30],
  成都: [104.06, 30.67],
  武汉: [114.30, 30.59],
  南京: [118.78, 32.04],
  西安: [108.93, 34.27],
  天津: [117.20, 39.13],
  绵阳: [104.74, 31.46],
  青岛: [120.38, 36.07],
  宁波: [121.55, 29.87],
  郑州: [113.62, 34.75],
  重庆: [106.55, 29.56],
  长沙: [112.93, 28.23],
  厦门: [118.10, 24.46],
  福州: [119.30, 26.08],
  合肥: [117.27, 31.86]
}

const normalizeCityName = (name: string) => String(name || '').trim().replace(/市$/, '')

const toDomesticScatter = (items: Array<{ name: string; value: number }> = []) => {
  return items
    .map((item) => {
      const normalized = normalizeCityName(item.name)
      const co = chinaCityCoordMap[normalized]
      if (!co) return null
      return { name: normalized, co, v: Number(item.value || 0) }
    })
    .filter((item): item is { name: string; co: [number, number]; v: number } => Boolean(item))
}

const toOverseasScatter = (items: Array<{ city: string; lat: number; lng: number; value: number }> = []) => {
  return items
    .filter(item => Number.isFinite(Number(item.lat)) && Number.isFinite(Number(item.lng)))
    .map(item => ({
      name: item.city || '未命名',
      co: [Number(item.lng), Number(item.lat)] as [number, number],
      v: Number(item.value || 0)
    }))
}

// ═══ Doubled lists ═══
const dblOrg = computed(() => [...cfg.value.orgList, ...cfg.value.orgList])
const dblComp = computed(() => [...cfg.value.compNames, ...cfg.value.compNames])
const dblDp = computed(() => [...cfg.value.dpList, ...cfg.value.dpList])
const dblIp = computed(() => [...cfg.value.ipList, ...cfg.value.ipList])

// ═══ Maps ═══
const maps = ref({ world: false, china: false })
async function loadMaps() {
  try { const r = await fetch('/maps/world.json'); if(r.ok){echarts.registerMap('world',await r.json());maps.value.world=true} } catch {}
  try { const r = await fetch('/maps/china.json'); if(r.ok){echarts.registerMap('china',await r.json());maps.value.china=true} } catch {}
}

// ═══ Chart Management ═══
// 关键改动：用 v-show 后，所有三个页面的 DOM 始终存在
// 所以图表只需要 init 一次，切换时只做 resize
const inst: Record<string, echarts.ECharts> = {}
const chartsInited = ref(false)

const c_p1_pie = ref<HTMLElement>(); const c_p1_map = ref<HTMLElement>(); const c_p1_worldMini = ref<HTMLElement>()
const c_p1_bar1 = ref<HTMLElement>(); const c_p1_bar2 = ref<HTMLElement>()
const c_p2_line = ref<HTMLElement>(); const c_p2_pie = ref<HTMLElement>()
const c_p2_map = ref<HTMLElement>(); const c_p2_bar = ref<HTMLElement>()
const c_p3_bL = ref<HTMLElement>(); const c_p3_map = ref<HTMLElement>(); const c_p3_bR = ref<HTMLElement>(); const c_p3_worldMini = ref<HTMLElement>()

function mk(el: HTMLElement|undefined, k: string): echarts.ECharts|null {
  if (!el) return null
  // 如果已经创建过，直接返回（不重复创建）
  if (inst[k]) { inst[k].resize(); return inst[k] }
  const c = echarts.init(el)
  inst[k] = c
  return c
}

function resizeAll() {
  Object.values(inst).forEach(c => c?.resize())
}

function disposeAll() {
  Object.keys(inst).forEach(k => { inst[k]?.dispose(); delete inst[k] })
  chartsInited.value = false
}

// ═══ Bar factory ═══
const ts = { color: '#cbd5e1', fontSize: 11 }
function hBar(d:{name:string,value:number}[], c1:string, c2:string, left=70) {
  return {
    grid:{top:8,bottom:16,left,right:36}, tooltip:{trigger:'axis' as const},
    xAxis:{type:'value' as const,show:false},
    yAxis:{type:'category' as const,data:d.map(x=>x.name).reverse(),axisLabel:ts,axisLine:{show:false},axisTick:{show:false}},
    series:[{type:'bar' as const,data:d.map(x=>x.value).reverse(),barWidth:14,
      itemStyle:{borderRadius:[0,10,10,0],color:new echarts.graphic.LinearGradient(0,0,1,0,[{offset:0,color:c1},{offset:1,color:c2}])},
      label:{show:true,position:'right' as const,color:'#fff',fontSize:11,fontWeight:'bold' as const}}]
  }
}

// ═══ 一次性初始化所有三个页面的图表 ═══
async function initAllCharts() {
  await nextTick()
  // v-show 下三个页面的 DOM 都存在，但非活动页面的容器可能高度为0
  // 解决方案：给一个短暂延时让浏览器完成首次布局计算
  await new Promise(r => setTimeout(r, 200))
  
  renderP1()
  renderP2()
  renderP3()
  chartsInited.value = true
  
  // 再延时一次做 resize，确保 v-show:none 的页面在首次显示时尺寸正确
  // （v-show 隐藏的元素有 display:none，ECharts init 时宽高为0）
  // 所以我们需要在 tab 切换时再 resize
}

function renderP1() {
  const orgPieData = cfg.value.orgPieData.slice(0, 6)
  mk(c_p1_pie.value, 'p1pie')?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    legend: { bottom: 0, textStyle: { color: '#cbd5e1', fontSize: 10 }, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '42%'],
      itemStyle: { borderRadius: 5, borderColor: '#060c19', borderWidth: 2 },
      label: { show: true, color: '#fff', formatter: '{d}%', fontSize: 10 },
      data: orgPieData.length
        ? orgPieData.map((item, index) => ({
            value: item.value,
            name: item.name,
            itemStyle: { color: ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#06b6d4', '#ec4899'][index % 6] }
          }))
        : [{ value: 1, name: '暂无数据', itemStyle: { color: '#334155' } }]
    }]
  })

  mk(c_p1_bar1.value, 'p1b1')?.setOption(hBar(cfg.value.topCat, '#fbbf24', '#d97706', 80))
  mk(c_p1_bar2.value, 'p1b2')?.setOption(hBar(cfg.value.topCtry, '#38bdf8', '#0284c7', 60))

  if (maps.value.china) {
    const mapMax = Math.max(...cfg.value.projectHeatData.map(item => Number(item.value || 0)), 10)
    mk(c_p1_map.value, 'p1map')?.setOption({
      tooltip: {
        trigger: 'item',
        formatter: (p: any) => `${p.name}<br/>服务覆盖数: ${p.value || 0}`
      },
      visualMap: {
        show: true,
        right: 10,
        bottom: 72,
        min: 0,
        max: mapMax,
        text: ['高', '低'],
        textStyle: { color: '#fff', fontSize: 10 },
        inRange: { color: ['#0f172a', '#1d4ed8', '#38bdf8'] },
        calculable: true
      },
      geo: {
        map: 'china',
        roam: true,
        zoom: 1.2,
        itemStyle: { areaColor: '#0f172a', borderColor: '#334155', borderWidth: 0.8 },
        emphasis: { itemStyle: { areaColor: '#1e40af' }, label: { show: true, color: '#fff' } },
        label: { show: true, color: 'rgba(255,255,255,0.65)', fontSize: 9 }
      },
      series: [{
        name: '服务覆盖',
        type: 'map',
        geoIndex: 0,
        label: {
          show: true,
          color: '#fff',
          fontSize: 10,
          formatter: (p: any) => (p.value ? `${p.name}\n${p.value}` : p.name)
        },
        data: cfg.value.projectHeatData
      }]
    }, true)
  }

  const worldMini = mk(c_p1_worldMini.value, 'p1worldmini')
  if (worldMini && maps.value.world) {
    worldMini.setOption({
      animation: false,
      tooltip: { trigger: 'item', formatter: (p: any) => `${p.name}<br/>服务点: ${p.value?.[2] ?? 0}` },
      geo: {
        map: 'world',
        roam: false,
        zoom: 1.08,
        center: [16, 18],
        itemStyle: { areaColor: '#0f1f3a', borderColor: '#334155', borderWidth: 0.4 },
        emphasis: { itemStyle: { areaColor: '#1e3a8a' } }
      },
      series: [{
        name: '国际服务点',
        type: 'effectScatter',
        geoIndex: 0,
        coordinateSystem: 'geo',
        data: cfg.value.intlCities.map(c => ({ name: c.name, value: [...c.co, c.v] })),
        symbolSize: (v: any) => Math.max(6, Math.min(12, 6 + v[2] * 2)),
        itemStyle: { color: '#60a5fa', shadowBlur: 8, shadowColor: '#60a5fa' },
        rippleEffect: { scale: 2.2, brushType: 'stroke' },
        label: { show: false }
      }]
    }, true)
  }
}

function renderP2() {
  mk(c_p2_line.value, 'p2line')?.setOption({
    grid: { top: 24, bottom: 24, left: 40, right: 16 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: cfg.value.trend.map(d => d.m), axisLabel: ts, axisLine: { lineStyle: { color: '#334155' } } },
    yAxis: { type: 'value', axisLabel: ts, splitLine: { lineStyle: { color: '#1e293b', type: 'dashed' } } },
    series: [{
      type: 'line',
      data: cfg.value.trend.map(d => d.v),
      smooth: true,
      symbolSize: 6,
      itemStyle: { color: '#34d399' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(52,211,153,0.4)' },
          { offset: 1, color: 'rgba(52,211,153,0)' }
        ])
      }
    }]
  })

  const tc = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#64748b']
  mk(c_p2_pie.value, 'p2pie')?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}家 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#cbd5e1', fontSize: 10 }, itemWidth: 8, itemHeight: 8 },
    series: [{
      type: 'pie',
      radius: ['42%', '65%'],
      center: ['50%', '40%'],
      itemStyle: { borderRadius: 4, borderColor: '#060c19', borderWidth: 2 },
      label: { show: true, color: '#fff', formatter: '{d}%', fontSize: 10 },
      data: (cfg.value.memberLevelData.length ? cfg.value.memberLevelData : [{ name: '暂无数据', value: 1 }])
        .map((d, i) => ({ value: d.value, name: d.name, itemStyle: { color: tc[i % tc.length] } }))
    }]
  })

  mk(c_p2_bar.value, 'p2bar')?.setOption({
    grid: { top: 8, bottom: 16, left: 50, right: 36 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', show: false },
    yAxis: {
      type: 'category',
      data: cfg.value.cityRank.map(d => d.name).reverse(),
      axisLabel: { ...ts, fontSize: 10 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [{
      type: 'bar',
      data: cfg.value.cityRank.map(d => d.value).reverse(),
      barWidth: 12,
      itemStyle: { borderRadius: [0, 8, 8, 0], color: '#818cf8' },
      label: { show: true, position: 'right', color: '#fff', fontSize: 10 }
    }]
  })

  const map = mk(c_p2_map.value, 'p2map')
  if (map && maps.value.china) {
    map.setOption({
      tooltip: { 
        trigger: 'item',
        formatter: (p: any) => {
          if (p.seriesType === 'scatter') {
            return `${p.name}<br/>入驻会员: ${p.value[2]}家`
          }
          return p.name
        }
      },
      // ✅ 去掉 visualMap（不再需要热力映射）
      geo: {
        map: 'china',
        roam: true,
        zoom: 1.2,
        // ✅ 统一深色底图（不再有颜色渐变）
        itemStyle: { 
          areaColor: '#1e293b',  // 统一深灰色
          borderColor: '#475569', 
          borderWidth: 1 
        },
        emphasis: { 
          itemStyle: { areaColor: '#334155' },  // hover时稍亮
          label: { show: true, color: '#fff' } 
        },
        label: { 
          show: true, 
          color: 'rgba(255,255,255,0.4)',  // 省份名半透明
          fontSize: 9 
        }
      },
      series: [
        // ✅ 核心：改用散点图，气泡大小代表会员数量
        {
          type: 'scatter',
          coordinateSystem: 'geo',
          name: '会员分布',
          data: cfg.value.spCities.map(c => ({
            name: c.name,
            value: [...c.co, c.v],
            itemStyle: {
              color: c.v > 20 ? '#f59e0b' :
                     c.v > 10 ? '#fbbf24' :
                     c.v > 5 ? '#facc15' :
                     '#fde047'
            }
          })),
          symbolSize: (val: any) => {
            const memberCount = val[2]
            return Math.max(12, Math.min(40, 10 + memberCount * 2))
          },
          label: { 
            show: true,
            formatter: '{b}',
            position: 'top', 
            color: '#fff', 
            fontSize: 10,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(251, 191, 36, 0.5)'
          }
        }
      ]
    }, true)
  }
}


function renderP3() {
  mk(c_p3_bL.value, 'p3bL')?.setOption(hBar(cfg.value.provRank, '#34d399', '#059669', 50))
  mk(c_p3_bR.value, 'p3bR')?.setOption(hBar(cfg.value.ctryRank, '#f472b6', '#db2777', 80))

  const map = mk(c_p3_map.value, 'p3map')
  if (!map || !maps.value.china) return

  const series: any[] = [
    {
      type: 'effectScatter',
      coordinateSystem: 'geo',
      name: '国内服务点',
      data: cfg.value.spCities.map(c => ({ name: c.name, value: [...c.co, c.v] })),
      symbolSize: (v: any) => Math.max(8, Math.min(18, v[2] / 60)),
      itemStyle: { color: '#facc15', shadowBlur: 10, shadowColor: '#facc15' },
      label: { show: true, formatter: '{b}', position: 'right', color: '#fff', fontSize: 9 },
      rippleEffect: { scale: 3, brushType: 'stroke' }
    }
  ]

  map.setOption({
    tooltip: { trigger: 'item', formatter: (p: any) => p.seriesType === 'effectScatter' ? `${p.seriesName}<br/>${p.name}` : p.name },
    legend: { bottom: 10, left: 'center', data: ['国内服务点'], textStyle: { color: '#cbd5e1', fontSize: 11 } },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      itemStyle: { areaColor: '#1e293b', borderColor: '#475569', borderWidth: 0.5 },
      emphasis: { itemStyle: { areaColor: '#0f172a' }, label: { show: true, color: '#fff' } }
    },
    series
  })

  const worldMini = mk(c_p3_worldMini.value, 'p3worldmini')
  if (worldMini && maps.value.world) {
    worldMini.setOption({
      animation: false,
      tooltip: {
        trigger: 'item',
        formatter: (p: any) => `${p.name}<br/>服务点: ${p.value?.[2] ?? ''}`
      },
      geo: {
        map: 'world',
        roam: false,
        zoom: 1.08,
        center: [15, 15],
        itemStyle: { areaColor: '#0f1f3a', borderColor: '#334155', borderWidth: 0.4 },
        emphasis: { itemStyle: { areaColor: '#1e3a8a' } }
      },
      series: [{
        type: 'effectScatter',
        coordinateSystem: 'geo',
        name: '国际服务点',
        data: cfg.value.intlCities.map(c => ({ name: c.name, value: [...c.co, c.v] })),
        symbolSize: (v: any) => Math.max(6, Math.min(12, 6 + v[2] * 2)),
        itemStyle: { color: '#ef4444', shadowBlur: 8, shadowColor: '#ef4444' },
        rippleEffect: { scale: 2.2, brushType: 'stroke' },
        label: { show: false }
      }]
    }, true)
  }
}

// ═══ 关键：Tab切换时，对当前可见页面的图表做 resize ═══
// v-show 隐藏的元素 display:none，ECharts 在 display:none 时 init 得到 0x0 尺寸
// 切换为可见后，必须 resize 让图表重新计算尺寸
watch(activeTab, async () => {
  await nextTick()
  // 短延时确保 display 已从 none 变为可见
  await new Promise(r => setTimeout(r, 50))
  resizeAll()
})

// ═══ API Data → cfg + chart redraw ═══
watch(statsLoaded, (ready) => {
  if (!ready) return
  const d = dashboardStats.value

  const t = d.totals
  cfg.value.tenderCount = Number(t.tenderCount || 0)
  cfg.value.memberCount = Number(t.memberCount || 0)
  cfg.value.countryCount = Number(t.countryCount || 0)
  cfg.value.monthlyNewMembers = Number(t.monthlyNewMembers || 0)
  cfg.value.svipCount = Number(t.svipCount || 0)
  cfg.value.vipCount = Number(t.vipCount || 0)
  cfg.value.industryCount = Number(t.industryCount || 0)
  cfg.value.platformData = {
    totalPublished: String(t.tenderCount || 0),
    organizationCount: String(t.organizationCount || 0),
    monthlyPublished: String(t.monthlyTenderCount || 0),
    countryCount: String(t.countryCount || 0)
  }

  cfg.value.topCat = (d.tendersByCategory || []).map(item => ({
    name: categoryMap[item.name] || item.name,
    value: Number(item.value || 0)
  }))
  cfg.value.topCtry = (d.tendersByCountry || []).map(item => ({
    name: item.name, value: Number(item.value || 0)
  }))
  cfg.value.orgList = (d.tendersByOrganization || []).map(item => ({
    name: item.name,
    code: `TOP${item.value}`
  }))
  cfg.value.orgPieData = (d.tendersByOrganization || []).slice(0, 6).map(item => ({
    name: item.name,
    value: Number(item.value || 0)
  }))
  cfg.value.trend = (d.memberTrend || []).map(item => ({
    m: item.month.replace(/^\d{4}-/, '').replace(/^0/, '') + '月',
    v: Number(item.count || 0)
  }))
  cfg.value.compTypes = (d.membersByIndustry || []).map(item => ({
    name: item.name,
    value: Number(item.value || 0)
  }))
  cfg.value.memberLevelData = (d.memberLevels || []).map(item => ({
    name: item.name,
    value: Number(item.value || 0)
  }))
  cfg.value.cityRank = (d.memberCities || []).map(item => ({
    name: normalizeCityName(item.name),
    value: Number(item.value || 0)
  }))
  cfg.value.compNames = d.recentCompanies || []
  cfg.value.projectHeatData = (d.provinceHeat || []).map(item => ({
    name: item.name,
    value: Number(item.value || 0)
  }))

  const domestic = d.domesticNetwork
  if (domestic) {
    cfg.value.dp = {
      count: Number(domestic.count || 0),
      provinces: Number(domestic.provinces || 0),
      cities: Number(domestic.cities || 0)
    }
    cfg.value.provRank = (domestic.provinceRank || []).map(item => ({
      name: item.name,
      value: Number(item.value || 0)
    }))
    cfg.value.dpList = (domestic.list || []).map(item => ({
      name: item.name,
      location: item.location || '-'
    }))
    cfg.value.spCities = toDomesticScatter(domestic.cityPoints || [])
  }

  const overseas = d.overseasNetwork
  if (overseas) {
    cfg.value.ip = {
      count: Number(overseas.count || 0),
      countries: Number(overseas.countries || 0),
      cities: Number(overseas.cities || 0)
    }
    cfg.value.ctryRank = (overseas.countryRank || []).map(item => ({
      name: item.name,
      value: Number(item.value || 0)
    }))
    cfg.value.ipList = (overseas.list || []).map(item => ({
      name: item.name,
      location: item.location || '-'
    }))
    cfg.value.intlCities = toOverseasScatter(overseas.points || [])
    cfg.value.worldData = (overseas.countryRank || []).map(item => ({
      name: item.name,
      value: Number(item.value || 0)
    }))
  }

  if (chartsInited.value) {
    renderP1()
    renderP2()
    renderP3()
    resizeAll()
  }
})

// ═══ Lifecycle ═══
function onResize() { resizeAll() }

onMounted(async () => {
  await loadMaps()
  await initAllCharts()
  startAuto()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (autoTimer) clearInterval(autoTimer)
  if (resumeTimer) clearTimeout(resumeTimer)
  disposeAll()
  window.removeEventListener('resize', onResize)
})
</script>
<style scoped>
/* ═══ Root ═══ */
.dashboard-root {
  width: 100vw;
  height: 100vh;
  max-height: 100vh;
  background: #060c19;
  color: #fff;
  font-family: 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* ═══ Background ═══ */
.bg-effects { position: absolute; inset: 0; pointer-events: none; z-index: 0; }
.glow { position: absolute; border-radius: 50%; filter: blur(120px); }
.glow-1 { top: -10%; left: 20%; width: 30%; height: 30%; background: rgba(37, 99, 235, .08); animation: pulse-s 6s ease-in-out infinite; }
.glow-2 { bottom: -10%; right: 20%; width: 30%; height: 30%; background: rgba(99, 102, 241, .08); animation: pulse-s 6s ease-in-out infinite 2s; }

/* ═══ Header ═══ */
.header-bar {
  height: 80px;
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  border-bottom: 1px solid rgba(56, 189, 248, .35);
  background:
    radial-gradient(circle at 50% -120%, rgba(56, 189, 248, .35), transparent 55%),
    linear-gradient(180deg, rgba(8, 27, 68, .96) 0%, rgba(5, 12, 28, .94) 100%);
  overflow: hidden;
}
.header-bar::before,
.header-bar::after {
  content: '';
  position: absolute;
  top: 8px;
  width: 250px;
  height: 24px;
  border-top: 2px solid rgba(56, 189, 248, .75);
  border-bottom: 1px solid rgba(56, 189, 248, .35);
  background: linear-gradient(90deg, rgba(56,189,248,.08), rgba(56,189,248,.22), rgba(56,189,248,.08));
  box-shadow: 0 0 18px rgba(56, 189, 248, .25);
  opacity: .95;
}
.header-bar::before { left: 18px; clip-path: polygon(0 0, 90% 0, 100% 100%, 0 100%); }
.header-bar::after  { right: 18px; clip-path: polygon(10% 0, 100% 0, 100% 100%, 0 100%); }

/* 扫描线 */
.header-bar .scan-line {
  position: absolute;
  top: 0;
  left: -30%;
  width: 30%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(125, 211, 252, .14), transparent);
  filter: blur(1px);
  animation: scan-move 4s linear infinite;
  pointer-events: none;
  z-index: 1;
}

.header-bar::before,
.header-bar::after {
  z-index: 0;
  opacity: .42;
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 2;
}

.header-bg-fallback {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, #0f172a, rgba(6, 12, 25, .9));
  z-index: -1;
}

.header-content { display: flex; align-items: center; justify-content: space-between; height: 100%; padding: 0 32px; }
.header-left { width: 280px; }
.header-right { width: 280px; text-align: right; }
.header-center { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 6px;
  border: 1px solid rgba(56, 189, 248, .3);
  background: rgba(30, 58, 138, .2);
  color: #93c5fd;
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
}
.back-btn:hover { background: rgba(56, 189, 248, .2); }
.back-icon { transition: transform .2s; }
.back-btn:hover .back-icon { transform: translateX(-3px); }

/* 新增：标题边框风格 */
.title-frame {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 4px 22px 6px;
  border: 1px solid rgba(56, 189, 248, .28);
  border-radius: 6px;
  background: rgba(8, 27, 68, .32);
  box-shadow: inset 0 0 10px rgba(56, 189, 248, .12);
  z-index: 3;
}

/* 标题角点 */
.title-frame::before,
.title-frame::after {
  content: '';
  position: absolute;
  top: -1px;
  width: 22px;
  height: 22px;
  border-top: 2px solid rgba(125, 211, 252, .35);
  z-index: 1;
}
.title-frame::before {
  left: -1px;
  border-left: 2px solid #7dd3fc;
  border-top-left-radius: 4px;
}
.title-frame::after {
  right: -1px;
  border-right: 2px solid #7dd3fc;
  border-top-right-radius: 4px;
}

.title-wing {
  width: 60px;
  height: 12px;
  background: linear-gradient(90deg, transparent, rgba(186,230,253,.28), transparent);
  z-index: 1;
}
.title-wing.right { transform: scaleX(-1); }

.main-title {
  margin: 0;
  font-size: 27px;
  font-weight: 800;
  letter-spacing: .16em;
  color: #eef8ff;
  text-shadow:
    0 0 8px rgba(147, 197, 253, .55),
    0 0 16px rgba(56, 189, 248, .35),
    0 2px 10px rgba(0, 0, 0, .45);
}

@keyframes scan-move {
  0% { transform: translateX(0); opacity: 0; }
  15% { opacity: 1; }
  85% { opacity: 1; }
  100% { transform: translateX(460%); opacity: 0; }
}

.time-display { font-size: 22px; font-weight: 700; font-family: 'Courier New', monospace; color: #e0f2fe; }
.date-display { font-size: 13px; color: #60a5fa; margin-top: 2px; }

/* Tab */
.tab-bar { display: flex; gap: 8px; margin-top: 0; transform: translateY(-4px); }
.tab-btn {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  padding: 4px 24px;
  border-radius: 20px;
  font-size: 13px;
  border: 1px solid rgba(56, 189, 248, .25);
  background: rgba(30, 58, 138, .15);
  color: #60a5fa;
  transition: all .3s;
}
.tab-btn.active { background: #2563eb; border-color: #60a5fa; color: #fff; box-shadow: 0 0 16px rgba(37, 99, 235, .5); }
.tab-btn:hover:not(.active) { color: #93c5fd; background: rgba(30, 58, 138, .3); }
.tab-text { position: relative; z-index: 1; }
.tab-progress { position: absolute; bottom: 0; left: 0; height: 2px; background: #facc15; transition: width 1s linear; }

/* ═══ Main ═══ */
.main-area { flex: 1; padding: 12px 16px; z-index: 10; position: relative; overflow: hidden; }

/* ═══ Grid ═══ */
.page-grid { width: 100%; height: 100%; display: grid; grid-template-columns: 3fr 6fr 3fr; gap: 12px; }
.page-grid-footer { width: 100%; height: 100%; display: flex; flex-direction: column; gap: 10px; }
.page-grid-body { flex: 1; min-height: 0; display: grid; grid-template-columns: 3fr 6fr 3fr; gap: 12px; }
.col-left, .col-right { display: flex; flex-direction: column; gap: 10px; min-height: 0; }
.col-center { display: flex; flex-direction: column; gap: 10px; min-height: 0; }

/* ═══ Panel ═══ */
.panel {
  background: rgba(13, 26, 48, .7);
  border: 1px solid rgba(56, 189, 248, .15);
  border-radius: 10px;
  position: relative;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, .3);
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 10px;
  height: 10px;
  border-top: 2px solid #38bdf8;
  border-left: 2px solid #38bdf8;
  border-top-left-radius: 6px;
  z-index: 1;
}
.panel::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-bottom: 2px solid #38bdf8;
  border-right: 2px solid #38bdf8;
  border-bottom-right-radius: 6px;
  z-index: 1;
}
.panel-map { flex: 1; border: 2px solid rgba(56, 189, 248, .25); }

.panel-header {
  height: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px;
  background: linear-gradient(90deg, rgba(56, 189, 248, .1), transparent);
  border-bottom: 1px solid rgba(56, 189, 248, .1);
  color: #e0f2fe;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: .5px;
}

.panel-body { flex: 1; min-height: 0; position: relative; }
.panel-body.pad { padding: 12px 14px; }

/* Flex weights */
.flex-2 { flex: 2; }
.flex-25 { flex: 2.5; }
.flex-3 { flex: 3; }
.flex-35 { flex: 3.5; }
.flex-4 { flex: 4; }

/* ═══ Chart ═══ */
.chart-wrap { padding: 4px; }
.chart-el { width: 100%; height: 100%; }

/* ═══ Text ═══ */
.text-body { font-size: 12px; color: rgba(191, 219, 254, .86); line-height: 1.6; display: flex; align-items: flex-start; text-align: justify; }


/* ═══ UN Data ═══ */
.un-data-body { display: flex; flex-direction: column; gap: 8px; }

.un-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.un-card {
  background: rgba(30, 41, 59, .45);
  border: 1px solid rgba(56, 189, 248, .12);
  border-radius: 8px;
  padding: 8px 6px;
  text-align: center;
}

.un-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid rgba(56, 189, 248, .12);
  padding-top: 6px;
}

.un-label { font-size: 13px; color: #93c5fd; }
.un-value { font-size: 20px; font-weight: 700; font-family: 'Courier New', monospace; }
.grad-blue { background: linear-gradient(to right, #fff, #93c5fd); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.grad-yellow { background: linear-gradient(to right, #fef3c7, #f59e0b); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.grad-green { background: linear-gradient(to right, #bbf7d0, #22c55e); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }

/* ═══ KPI ═══ */
.kpi-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 9px; height: 100%; align-content: center; }
.kpi { background: rgba(30, 41, 59, .5); border: 1px solid rgba(255, 255, 255, .05); border-radius: 8px; padding: 9px 11px; text-align: center; }
.kpi-l { font-size: 10px; color: #94a3b8; }
.kpi-v { font-size: 16px; font-weight: 700; margin-top: 3px; color: #fff; }
.kpi-v.green { color: #34d399; }

/* ═══ Member Panel (修复：紧凑布局) ═══ */
.mem-panel { display: flex; flex-direction: column; justify-content: space-evenly; gap: 6px; padding: 10px 14px !important; }
.mem-top {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: rgba(59, 130, 246, .08);
  border-radius: 8px;
  border: 1px solid rgba(59, 130, 246, .15);
}
.mem-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  flex-shrink: 0;
  background: rgba(59, 130, 246, .15);
  border: 1px solid rgba(59, 130, 246, .3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #60a5fa;
}
.mem-top-info { flex: 1; overflow: hidden; }
.mem-big { font-size: 28px; font-weight: 800; font-family: 'Impact', 'Courier New', monospace; color: #fff; letter-spacing: 1px; line-height: 1; white-space: nowrap; }
.mem-sub { font-size: 11px; color: #60a5fa; margin-top: 4px; }

.mem-strip { display: flex; gap: 6px; }
.mem-cell {
  flex: 1;
  background: rgba(30, 41, 59, .5);
  border: 1px solid rgba(56, 189, 248, .1);
  border-radius: 6px;
  padding: 6px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.mc-v { font-size: 15px; font-weight: 700; color: #fff; line-height: 1.2; }
.mc-v.green { color: #34d399; }
.mc-l { font-size: 9px; color: #94a3b8; white-space: nowrap; margin-top: 2px; }

/* ═══ Stat Trio ═══ */
.trio { display: flex; gap: 8px; height: 100%; align-items: center; }
.trio-i { flex: 1; display: flex; flex-direction: column; align-items: center; background: rgba(59, 130, 246, .08); border-radius: 8px; padding: 10px 4px; }
.trio-n { font-size: 24px; font-weight: 800; color: #fff; }
.trio-t { font-size: 11px; color: #60a5fa; margin-top: 2px; }

.mini-world-overlay {
  position: absolute;
  left: 16px;
  bottom: 16px;
  width: 280px;
  height: 170px;
  z-index: 12;
  border: 1px solid rgba(56, 189, 248, .28);
  border-radius: 10px;
  background: rgba(6, 12, 25, .78);
  backdrop-filter: blur(6px);
  overflow: hidden;
}

.mini-world-overlay.p1-mini {
  width: 242px;
  height: 145px;
  left: 14px;
  bottom: 105px;
}

.mini-world-title {
  position: absolute;
  left: 10px;
  top: 8px;
  z-index: 2;
  font-size: 11px;
  color: #93c5fd;
  letter-spacing: .04em;
}

.mini-world-chart {
  width: 100%;
  height: 100%;
}

.mini-world-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #60a5fa;
  background: rgba(2, 6, 23, .45);
}

/* ═══ Map overlays ═══ */
.map-overlay-title { position: absolute; top: 12px; left: 0; right: 0; text-align: center; z-index: 10; pointer-events: none; }
.map-overlay-title h2 { font-size: 20px; font-weight: 700; color: #e0f2fe; letter-spacing: .15em; text-shadow: 0 2px 8px rgba(0, 0, 0, .8); }
.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: rgba(0, 0, 0, .5);
  z-index: 20;
  color: #60a5fa;
  font-size: 14px;
}
.map-stats {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 28px;
  background: rgba(6, 12, 25, .78);
  border-radius: 14px;
  padding: 10px 30px;
  border: 1px solid rgba(56, 189, 248, .28);
  backdrop-filter: blur(8px);
}
.ms { text-align: center; min-width: 120px; }
.ms-v {
  font-size: 30px;
  line-height: 1.05;
  font-weight: 900;
  font-family: 'Impact', 'Courier New', monospace;
  letter-spacing: 1.2px;
}
.ms-v.yellow { color: #facc15; text-shadow: 0 0 12px rgba(250, 204, 21, .4); }
.ms-v.blue { color: #60a5fa; text-shadow: 0 0 12px rgba(96, 165, 250, .4); }
.ms-v.green { color: #34d399; text-shadow: 0 0 12px rgba(52, 211, 153, .4); }
.ms-l {
  font-size: 12px;
  color: #b6c6da;
  margin-top: 6px;
  letter-spacing: .06em;
}

/* ═══ Scroll Lists ═══ */
.scroll-box { overflow: hidden; position: relative; }
.scroll-mask-t, .scroll-mask-b { position: absolute; left: 0; right: 0; height: 12px; z-index: 5; pointer-events: none; }
.scroll-mask-t { top: 0; background: linear-gradient(to bottom, rgba(13, 26, 48, 1), transparent); }
.scroll-mask-b { bottom: 0; background: linear-gradient(to top, rgba(13, 26, 48, 1), transparent); }
.scroll-track { position: absolute; width: 100%; }
.scroll-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-bottom: 1px solid rgba(56, 189, 248, .08);
  font-size: 13px;
  color: #93c5fd;
  transition: background .2s;
}
.scroll-row:hover { background: rgba(56, 189, 248, .06); }
.scroll-name { flex: 1; }
.scroll-tag { background: rgba(56, 189, 248, .15); padding: 2px 8px; border-radius: 4px; font-size: 10px; color: #bae6fd; white-space: nowrap; }
.scroll-col { display: flex; flex-direction: column; }
.scroll-sub { font-size: 10px; color: rgba(148, 163, 184, .7); margin-top: 1px; }
.dot-g { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; background: #34d399; }
.ic-blue { color: #3b82f6; flex-shrink: 0; }
.ic-green { color: #22c55e; flex-shrink: 0; }

/* ═══ Footer Partner ═══ */
.footer-partner {
  height: 88px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 20px;
  background: linear-gradient(90deg, rgba(30, 58, 138, .3), transparent);
  border: 1px solid rgba(56, 189, 248, .2);
  border-radius: 10px;
}
.fp-icon {
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  background: rgba(37, 99, 235, .15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, .3);
}
.fp-title { font-size: 16px; font-weight: 700; color: #facc15; margin-bottom: 4px; }
.fp-desc { font-size: 13px; color: rgba(191, 219, 254, .8); line-height: 1.6; }
.fp-desc .hl { color: #60a5fa; font-weight: 700; }

/* ═══ Transition — 不再使用，因为改用 v-show ═══ */
.page-fade-enter-active, .page-fade-leave-active { transition: opacity .3s ease; }
.page-fade-enter-from, .page-fade-leave-to { opacity: 0; }

/* ═══ Animations ═══ */
@keyframes scrollUp { 0% { transform: translateY(0); } 100% { transform: translateY(-50%); } }
.anim-scroll { animation: scrollUp 30s linear infinite; }
.anim-scroll-slow { animation: scrollUp 50s linear infinite; }
@keyframes pulse-s { 0%, 100% { opacity: .08; } 50% { opacity: .2; } }
.spin { animation: spin-k 1s linear infinite; }
@keyframes spin-k { from { transform: rotate(0); } to { transform: rotate(360deg); } }
::-webkit-scrollbar { width: 0; height: 0; }

/* 响应式补充 */
@media (max-width: 1440px) {
  .main-title { font-size: 22px; letter-spacing: .1em; }
  .map-stats { gap: 16px; padding: 8px 18px; }
  .ms-v { font-size: 24px; }
  .ms-l { font-size: 11px; margin-top: 4px; }
  .mini-world-overlay.p1-mini {
    width: 200px;
    height: 120px;
    left: 12px;
    bottom: 96px;
  }
}
</style>
