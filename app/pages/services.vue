<!-- 文件路径: D:\ipp-platform\app\pages\services.vue -->
<template>
  <div class="bg-white min-h-screen pb-20 font-sans relative">
    
    <!-- 登录态信息（仅真实态） -->
    <div class="fixed bottom-6 left-6 z-[9999] group">
      <button class="w-10 h-10 bg-slate-800 text-white rounded-full shadow-lg flex items-center justify-center opacity-50 hover:opacity-100 transition-all hover:scale-110" title="演示控制台">
        <el-icon><Setting /></el-icon>
      </button>
      
      <div class="absolute bottom-12 left-0 w-56 bg-white p-4 rounded-xl shadow-2xl border border-slate-200 transform scale-0 group-hover:scale-100 origin-bottom-left transition-all duration-200">
        <div class="text-xs font-bold text-slate-800 mb-3 border-b border-slate-100 pb-2">
          登录态信息
        </div>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <span class="text-xs text-slate-500">当前状态</span>
            <el-tag size="small" :type="isLoggedIn ? 'success' : 'info'">
              {{ isLoggedIn ? '会员' : '游客' }}
            </el-tag>
          </div>
          <div class="flex items-center justify-between" v-if="isLoggedIn">
            <span class="text-xs text-slate-500">会员等级</span>
            <el-tag size="small" type="warning">{{ memberLevel }}</el-tag>
          </div>
        </div>
        <div class="mt-3 text-[10px] text-slate-400 bg-slate-50 p-2 rounded">
          * 本页已关闭前端身份模拟开关，统一使用真实登录态与真实会员等级。
        </div>
      </div>
    </div>

    <!-- 1. 头部 Banner -->
    <div class="relative h-[500px] bg-slate-900 overflow-hidden flex flex-col items-center justify-center text-center group">
      <div class="absolute inset-0 overflow-hidden">
        <img src="/images/services/services.jpg" class="w-full h-full object-cover animate-slow-zoom" />
        <div class="absolute inset-0 bg-gradient-to-b from-blue-900/40 via-blue-900/20 to-slate-900/90 mix-blend-multiply"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-transparent to-transparent"></div>
      </div>
      
      <div class="absolute top-8 right-4 z-20">
         <el-popover placement="bottom" :width="350" trigger="click">
            <template #reference>
              <div class="flex items-center gap-2 cursor-pointer text-white/80 hover:text-white bg-black/20 backdrop-blur-md px-3 py-1.5 rounded-full border border-white/10">
                <el-icon><Location /></el-icon>
                <span>{{ selectedCity || '上海' }}</span>
                <el-icon class="text-xs"><ArrowDown /></el-icon>
              </div>
            </template>
            <div class="mb-3 px-1">
              <el-input v-model="citySearchQuery" placeholder="输入城市名搜索..." size="small" prefix-icon="Search" clearable />
            </div>
            <div class="grid grid-cols-3 gap-2 max-h-60 overflow-y-auto">
              <div v-for="city in filteredCities" :key="city" class="text-center p-2 rounded cursor-pointer text-sm transition-colors" :class="selectedCity === city ? 'bg-brand-50 text-brand-600 font-bold' : 'hover:bg-slate-50 hover:text-slate-900 text-slate-600'" @click="handleCitySelect(city)">
                {{ city }}
              </div>
            </div>
         </el-popover>
      </div>

      <div class="relative z-10 container mx-auto px-4 mt-16">
        <div class="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/20 border border-white/40 text-white text-xs font-bold tracking-widest mb-8 backdrop-blur-md shadow-lg">
          <span class="w-2 h-2 rounded-full bg-white animate-pulse"></span> PLATFORM SERVICES
        </div>
        <h1 class="text-5xl md:text-7xl font-bold text-white mb-8 tracking-tight drop-shadow-2xl">企业全周期赋能</h1>
        <p class="text-white text-xl max-w-3xl mx-auto font-light leading-relaxed drop-shadow-lg">
          连接 <span class="font-bold border-b-2 border-white/50">国际公共采购</span> 与 <span class="font-bold border-b-2 border-white/50">中国制造</span>
        </p>
      </div>
    </div>

    <!-- 滚动通知条 -->
    <div class="bg-brand-50 border-b border-brand-100 overflow-hidden h-10 flex items-center relative z-20">
      <div class="container mx-auto px-4 flex items-center">
        <div class="flex-shrink-0 flex items-center gap-2 text-brand-700 text-xs font-bold mr-6">
          <el-icon><Bell /></el-icon> 最新活动
        </div>
        <div class="flex-grow overflow-hidden relative h-6">
           <div class="animate-marquee whitespace-nowrap absolute top-0 text-xs text-slate-600 leading-6">
             <span class="mr-12">📢 [享嘉之会] 2026全球公共采购高峰论坛正在报名中...</span>
             <span class="mr-12">📢 [出海考察] 越南&马来西亚制造业考察团剩余名额 3 位...</span>
             <span class="mr-12">📢 [行业沙龙] 医疗器械出海合规认证解析(线上直播)...</span>
           </div>
        </div>
      </div>
    </div>

    <!-- 平台介绍 -->
    <div class="container mx-auto px-4 mt-12 mb-16">
      <div class="bg-white rounded-2xl shadow-xl border border-slate-100 p-8 md:p-12">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div>
            <h2 class="text-2xl font-bold text-slate-900 mb-6 flex items-center gap-3">
              <span class="w-1.5 h-8 bg-brand-600 rounded-full"></span> 平台介绍
            </h2>
            <p class="text-slate-600 leading-relaxed mb-8 text-justify">
              XRIPP Global 是国内首个专注于国际公共采购的综合服务平台。我们依托上海市中小企业协会,深度整合<strong>联合国、世界银行</strong>资源,通过遍布全球的 47 个服务中心,为中国企业提供"家门口"的国际化服务。
            </p>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div v-for="stat in platformStats" :key="stat.label" class="text-center p-4 bg-slate-50 rounded-xl border border-slate-100 hover:bg-white hover:shadow-md transition-all">
                <div class="text-2xl md:text-3xl font-bold text-brand-600 mb-1">{{ stat.value }}</div>
                <div class="text-xs text-slate-500 font-medium">{{ stat.label }}</div>
              </div>
            </div>
          </div>
          
          <div class="relative h-full min-h-[300px] rounded-2xl overflow-hidden shadow-lg group cursor-pointer" @click="handleCitySelect('上海')">
            <img src="https://images.unsplash.com/photo-1519501025264-65ba15a82390?q=80&w=1000" class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
            <div class="absolute inset-0 bg-blue-900/70 flex flex-col items-center justify-center text-white p-8 text-center">
              <el-icon class="text-5xl mb-4"><MapLocation /></el-icon>
              <h3 class="text-2xl font-bold mb-2">城市合伙人网络</h3>
              <p class="text-blue-100 text-sm mb-6">全国已开通 35 个城市服务中心</p>
              <button class="px-6 py-2.5 bg-white text-blue-900 font-bold rounded-lg text-sm hover:bg-blue-50 transition-colors shadow-lg">
                查找身边服务点
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ✅ Tabs 主内容区 (服务商库调到第1位) -->
    <div class="bg-slate-50 py-16 md:py-20">
      <div class="container mx-auto px-4">
        <el-tabs v-model="activeTab" type="card" class="custom-tabs">
          
          <!-- ✅ 1. 服务商库 (调到第一位) -->
          <el-tab-pane label="服务商库" name="supplier">
            <div class="text-center mb-12">
              <span class="text-brand-600 font-bold tracking-wider text-sm uppercase">Supplier Directory</span>
              <h2 class="text-3xl font-bold text-slate-900 mt-2">平台服务商名录库</h2>
              <p class="text-slate-500 mt-2">按城市、服务项目筛选优质合作伙伴</p>
            </div>

            <!-- 筛选区域 -->
            <div class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm mb-8">
              <div class="flex items-center gap-3 mb-6">
                <div class="w-12 h-12 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center text-2xl">
                  <el-icon><Search /></el-icon>
                </div>
                <div>
                  <div class="font-bold text-slate-900 text-lg">搜索服务商</div>
                  <div class="text-xs text-slate-400">按城市、服务项目筛选优质服务商</div>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <el-select v-model="supplierFilters.province" placeholder="选择省份" clearable size="large">
                  <el-option v-for="p in provinces" :key="p" :label="p" :value="p" />
                </el-select>
                <el-input v-model="supplierFilters.keyword" placeholder="输入关键词(如:地板)" clearable size="large">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="supplierFilters.serviceType" placeholder="服务项目" clearable size="large">
                  <el-option label="标书定制服务" value="bid_writing" />
                  <el-option label="出海咨询" value="export_consulting" />
                  <el-option label="认证服务" value="certification" />
                  <el-option label="物流报关" value="logistics" />
                </el-select>
                <el-button type="primary" size="large" class="w-full" @click="searchSupplier">
                  <el-icon><Search /></el-icon> 查询服务商
                </el-button>
              </div>
            </div>

            <!-- ✅ 默认展示:最新服务商(2排×5个,使用首页风格卡片) -->
            <div v-if="!hasSearched" class="mb-8">
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-slate-900">最新入驻服务商</h3>
                <span class="text-sm text-slate-500">共 {{ latestSuppliers.length }} 家认证服务商</span>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
                <div 
                  v-for="supplier in latestSuppliers" 
                  :key="supplier.id" 
                  class="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-brand-200 hover:-translate-y-2 transition-all duration-500 cursor-pointer h-full relative overflow-hidden flex flex-col"
                  @click="viewSupplierDetail(supplier)"
                >
                  <!-- 封面图 -->
                  <div class="h-32 bg-slate-200 relative overflow-hidden">
                    <img :src="supplier.image" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
                    <div class="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-60"></div>
                  </div>
                  
                  <!-- 悬浮 Logo -->
                  <div class="relative -mt-10 px-4 mb-2">
                    <div class="w-16 h-16 rounded-2xl bg-white shadow-lg flex items-center justify-center text-2xl font-bold text-brand-700 border-2 border-white group-hover:border-brand-50 transition-colors z-10 relative">
                      {{ supplier.icon }}
                    </div>
                  </div>
                  
                  <div class="p-4 pt-2 flex flex-col flex-grow">
                    <div class="mb-3">
                      <h4 class="font-bold text-base text-slate-900 mb-1 line-clamp-1 group-hover:text-brand-600 transition-colors" :title="supplier.name">
                        {{ supplier.name }}
                      </h4>
                      <p class="text-xs text-slate-400 font-medium flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-slate-300 group-hover:bg-brand-400 transition-colors"></span>
                        {{ supplier.category }}
                      </p>
                    </div>
                    
                    <div class="mt-auto pt-3 border-t border-slate-50 flex justify-between items-center">
                      <div class="text-xs text-slate-500 flex items-center gap-1">
                        <el-icon><Location /></el-icon> {{ supplier.city }}
                      </div>
                      <el-icon class="text-slate-300 group-hover:text-brand-600 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- ✅ 搜索结果区域(使用相同卡片样式) -->
            <div v-else-if="supplierList.length > 0">
              <div class="flex justify-between items-center mb-6">
                <div class="text-sm text-slate-500">共找到 <span class="font-bold text-brand-600">{{ supplierList.length }}</span> 家服务商</div>
                <el-button text @click="clearSearch">清空结果</el-button>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
                <div 
                  v-for="supplier in supplierList" 
                  :key="supplier.id" 
                  class="group bg-white rounded-2xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-brand-200 hover:-translate-y-2 transition-all duration-500 cursor-pointer h-full relative overflow-hidden flex flex-col"
                  @click="viewSupplierDetail(supplier)"
                >
                  <!-- 封面图 -->
                  <div class="h-32 bg-slate-200 relative overflow-hidden">
                    <img :src="supplier.image" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
                    <div class="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-60"></div>
                  </div>
                  
                  <!-- 悬浮 Logo -->
                  <div class="relative -mt-10 px-4 mb-2">
                    <div class="w-16 h-16 rounded-2xl bg-white shadow-lg flex items-center justify-center text-2xl font-bold text-brand-700 border-2 border-white group-hover:border-brand-50 transition-colors z-10 relative">
                      {{ supplier.icon }}
                    </div>
                  </div>
                  
                  <div class="p-4 pt-2 flex flex-col flex-grow">
                    <div class="mb-3">
                      <h4 class="font-bold text-base text-slate-900 mb-1 line-clamp-1 group-hover:text-brand-600 transition-colors" :title="supplier.name">
                        {{ supplier.name }}
                      </h4>
                      <p class="text-xs text-slate-400 font-medium flex items-center gap-1">
                        <span class="w-1.5 h-1.5 rounded-full bg-slate-300 group-hover:bg-brand-400 transition-colors"></span>
                        {{ supplier.category }}
                      </p>
                    </div>
                    
                    <div class="mt-auto pt-3 border-t border-slate-50 flex justify-between items-center">
                      <div class="text-xs text-slate-500 flex items-center gap-1">
                        <el-icon><Location /></el-icon> {{ supplier.city }}
                      </div>
                      <el-icon class="text-slate-300 group-hover:text-brand-600 group-hover:translate-x-1 transition-all"><ArrowRight /></el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 未找到结果 -->
            <div v-else class="text-center py-12 text-slate-400">
              <el-icon class="text-4xl mb-2"><Search /></el-icon>
              <div class="text-sm">未找到符合条件的服务商,请调整筛选条件</div>
            </div>
          </el-tab-pane>

          <!-- 2. 活动中心 -->
          <el-tab-pane label="活动中心" name="activity">
            <!-- 活动中心内容保持不变 -->
            <div class="text-center mb-12">
              <span class="text-brand-600 font-bold tracking-wider text-sm uppercase">Activity Center</span>
              <h2 class="text-3xl font-bold text-slate-900 mt-2">活动中心</h2>
              <p class="text-slate-500 mt-2">聚焦企业出海,打造多元化交流平台</p>
            </div>

            <el-carousel
              :interval="4500"
              height="420px"
              indicator-position="outside"
              class="mb-16"
            >
              <el-carousel-item v-for="act in activityTypes" :key="act.title">
                <div
                  class="group cursor-pointer bg-white rounded-2xl shadow-sm hover:shadow-2xl transition-all duration-500 overflow-hidden border border-slate-100 hover:border-blue-200 h-full"
                  @click="openActivityDetail(act)"
                >
                  <div class="relative h-full overflow-hidden bg-gradient-to-br from-slate-50 to-slate-100">
                    <img :src="act.image" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700" />
                    <div class="absolute inset-0 bg-gradient-to-t from-black/75 via-black/35 to-transparent"></div>

                    <div class="absolute top-4 right-4 px-3 py-1 rounded-full bg-white/90 backdrop-blur-sm text-xs font-bold" :class="act.colorClass">
                      {{ act.tag }}
                    </div>
                    <div v-if="act.isPaid" class="absolute top-4 left-4 px-2 py-1 rounded bg-orange-500 text-white text-xs font-bold shadow-md">
                      ¥{{ act.price }}起
                    </div>
                    <div v-else class="absolute top-4 left-4 px-2 py-1 rounded bg-green-500 text-white text-xs font-bold shadow-md">
                      免费
                    </div>

                    <div class="absolute bottom-0 left-0 right-0 p-6 text-white">
                      <h3 class="text-2xl font-bold mb-2">{{ act.title }}</h3>
                      <p class="text-sm text-blue-100 mb-3">{{ act.subTitle }}</p>
                      <p class="text-sm text-slate-100 leading-relaxed mb-4 max-w-3xl">{{ act.desc }}</p>
                      <div class="flex items-center justify-between">
                        <span class="text-sm font-bold flex items-center gap-1" :class="act.btnColor">
                          {{ act.btnText }} <el-icon><ArrowRight /></el-icon>
                        </span>
                        <span class="text-xs text-slate-200">{{ act.freq }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>

            <div class="bg-white rounded-2xl p-8 shadow-sm border border-slate-200">
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-xl font-bold text-slate-900">近期精选活动</h3>
                <button class="text-sm text-brand-600 hover:text-brand-700 font-medium flex items-center gap-1">
                  查看全部 <el-icon><ArrowRight /></el-icon>
                </button>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div v-for="activity in recentActivities" :key="activity.id" class="group cursor-pointer border border-slate-100 rounded-xl overflow-hidden hover:shadow-lg hover:border-brand-200 transition-all flex flex-col" @click="openActivityDetail(activity)">
                  <div class="aspect-video relative overflow-hidden bg-slate-100">
                    <img :src="activity.image" :alt="activity.title" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                    <div class="absolute top-3 left-3 px-2 py-1 rounded bg-white/95 backdrop-blur-sm text-xs font-bold text-slate-800">{{ activity.type }}</div>
                    <div v-if="activity.isPaid" class="absolute bottom-3 right-3 px-2 py-1 rounded bg-orange-500 text-white text-xs font-bold shadow-sm">¥{{ activity.price }}</div>
                    <div v-else class="absolute bottom-3 right-3 px-2 py-1 rounded bg-green-500 text-white text-xs font-bold shadow-sm">免费</div>
                  </div>
                  <div class="p-4 flex flex-col flex-grow">
                    <div class="flex gap-2 text-xs text-brand-600 mb-2">
                      <span>📅 {{ activity.date }}</span>
                      <span>📍 {{ activity.location }}</span>
                    </div>
                    <h4 class="font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-2 mb-2">{{ activity.title }}</h4>
                    <div class="mt-auto pt-3 border-t border-slate-50 flex justify-between items-center">
                      <button class="text-xs text-slate-400 hover:text-brand-600 flex items-center gap-1" @click.stop="handleSubscribe(activity)">
                        <el-icon><Bell /></el-icon> 订阅
                      </button>
                      <el-button type="primary" size="small" round>立即报名</el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 3. 培训中心 -->
          <el-tab-pane label="培训中心" name="training">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
              <div>
                <span class="text-brand-600 font-bold tracking-wider text-sm uppercase">Training Center</span>
                <h2 class="text-3xl font-bold text-slate-900 mt-2 mb-4">专业培训体系</h2>
                <p class="text-slate-500 mb-8">从理论到实战,全方位提升企业国际竞争力</p>
                
                <div class="space-y-6">
                  <div v-for="(train, i) in trainingCourses" :key="i" class="flex gap-4 p-4 bg-slate-50 rounded-xl hover:bg-white hover:shadow-md transition-all border border-slate-100 cursor-pointer" @click="openActivityDetail(train)">
                    <div class="flex-shrink-0 w-12 h-12 rounded-full bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center font-bold text-lg shadow-lg">{{ i+1 }}</div>
                    <div>
                      <h4 class="font-bold text-lg text-slate-800 mb-1 flex items-center gap-2">
                        {{ train.title }}
                        <span v-if="train.isPaid" class="text-xs text-orange-600 bg-orange-50 border border-orange-200 px-2 py-0.5 rounded">收费</span>
                        <span v-else class="text-xs text-green-600 bg-green-50 border border-green-200 px-2 py-0.5 rounded">免费</span>
                      </h4>
                      <p class="text-sm text-slate-500">{{ train.desc }}</p>
                    </div>
                  </div>
                </div>
                
                <div class="flex gap-4 mt-8">
                  <button
                    class="px-8 py-3 bg-brand-600 text-white font-bold rounded-lg shadow-lg hover:bg-brand-700 hover:shadow-xl transition-all flex items-center gap-2"
                    @click="openActivityDetail({
                      title:'获取课程表',
                      type:'培训服务',
                      isPaid:false,
                      desc:'查看近期课程安排与开班节奏',
                      longDesc:'该入口提供近期培训课程总览，包括课程主题、适用人群、授课形式与开班时间。建议先查看课程表，再根据企业阶段选择对应训练模块。'
                    })"
                  >
                    <el-icon><Calendar /></el-icon> 查看课程表
                  </button>

                  <button
                    class="px-8 py-3 bg-white border-2 border-brand-600 text-brand-600 font-bold rounded-lg hover:bg-brand-50 transition-all flex items-center gap-2"
                    @click="openActivityDetail({
                      title:'观看回放',
                      type:'培训服务',
                      isPaid:true,
                      price:199,
                      desc:'精选课程回放，支持随时学习',
                      longDesc:'回放内容覆盖联合国采购规则解读、申报材料撰写与案例复盘。购买后可在有效期内反复观看，适合团队内部统一培训与重点模块复习。'
                    })"
                  >
                    <el-icon><VideoCamera /></el-icon> 观看回放
                  </button>
                </div>
              </div>
              
              <div class="relative">
                <el-carousel :interval="5000" height="420px" indicator-position="outside" class="rounded-2xl overflow-hidden shadow-2xl">
                  <el-carousel-item v-for="(train, i) in trainingCourses" :key="train.title">
                    <div class="relative h-full">
                      <img
                        :src="trainingImages[i % trainingImages.length]"
                        class="w-full h-full object-cover"
                        alt="Training"
                      />
                      <div class="absolute inset-0 bg-gradient-to-t from-black/70 via-black/30 to-transparent"></div>
                      <div class="absolute bottom-0 left-0 right-0 p-6 text-white">
                        <div class="text-xl font-bold mb-1">{{ train.title }}</div>
                        <div class="text-sm text-slate-200 mb-2">{{ train.desc }}</div>
                        <div class="text-xs">
                          <span v-if="train.isPaid">收费课程：¥{{ train.price }}</span>
                          <span v-else>免费课程</span>
                        </div>
                      </div>
                    </div>
                  </el-carousel-item>
                </el-carousel>

                <div class="absolute bottom-4 right-4 bg-white/95 backdrop-blur-sm px-4 py-2 rounded-lg shadow-lg z-10">
                  <div class="text-xs text-slate-500">累计培训人次</div>
                  <div class="text-2xl font-bold text-brand-600">8,600+</div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 4. 资源拓展 -->
          <el-tab-pane label="资源拓展" name="resource">
            <div class="text-center mb-12">
              <span class="text-brand-600 font-bold tracking-wider text-sm uppercase">Resource Network</span>
              <h2 class="text-3xl font-bold text-slate-900 mt-2">资源拓展生态</h2>
              <p class="text-slate-500 mt-2">整合优质资源,为企业发展赋能</p>
            </div>

            <!-- 三卡一行，卡片不联动拉伸 -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6 items-start">
              <div
                v-for="resource in resources"
                :key="resource.title"
                class="group bg-white rounded-2xl border-2 border-slate-200 p-6 hover:border-brand-300 hover:shadow-xl transition-all duration-300 min-h-[220px] flex flex-col justify-between"
                :class="{ 'border-brand-400 shadow-lg': expertExpanded && resource.title === '专家智库' }"
              >
                <div>
                  <div class="flex items-start gap-4 mb-4">
                    <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-slate-50 to-slate-100 flex items-center justify-center text-3xl flex-shrink-0 group-hover:scale-110 transition-transform">
                      {{ resource.icon }}
                    </div>
                    <div class="flex-1">
                      <h3 class="text-lg font-bold text-slate-900 mb-1 group-hover:text-brand-600 transition-colors">
                        {{ resource.title }}
                      </h3>
                      <p class="text-sm text-slate-500 leading-relaxed">{{ resource.desc }}</p>
                    </div>
                  </div>
                </div>

                <div class="flex items-center justify-between pt-2">
                  <el-button
                    v-if="resource.title !== '专家智库'"
                    :type="resource.comingSoon ? 'info' : (resource.title === '项目对接' ? 'warning' : 'primary')"
                    size="default"
                    :disabled="resource.comingSoon"
                    :plain="resource.title !== '项目对接'"
                    @click.stop="handleResourceClick(resource)"
                  >
                    <el-icon class="mr-1"><ArrowRight /></el-icon>
                    {{
                      resource.comingSoon
                        ? '即将上线'
                        : (resource.title === '项目对接' ? '发起对接' : resource.cta)
                    }}
                  </el-button>

                  <!-- 专家智库：主次按钮分层，避免重复 -->
                  <template v-else>
                    <el-button type="primary" size="default" @click.stop="navigateTo('/experts')">
                      <el-icon class="mr-1"><ArrowRight /></el-icon>
                      进入专家库
                    </el-button>

                    <button
                      class="text-sm font-medium transition-colors"
                      :class="expertExpanded ? 'text-brand-600' : 'text-slate-500 hover:text-brand-600'"
                      @click.stop="toggleExpertExpanded"
                    >
                      {{ expertExpanded ? '收起分类 ▲' : '选择专家委 ▼' }}
                    </button>
                  </template>
                </div>
              </div>
            </div>

            <!-- 独立展开区：不再撑开其他卡片 -->
            <transition
              enter-active-class="transition-all duration-300 ease-out"
              enter-from-class="opacity-0 -translate-y-2"
              enter-to-class="opacity-100 translate-y-0"
              leave-active-class="transition-all duration-200 ease-in"
              leave-from-class="opacity-100 translate-y-0"
              leave-to-class="opacity-0 -translate-y-2"
            >
              <div v-if="expertExpanded" class="bg-white rounded-2xl border border-brand-200 shadow-lg p-6 mb-12">
                <div class="text-sm font-bold text-slate-700 mb-4 flex items-center gap-2">
                  <span class="w-1.5 h-4 bg-brand-500 rounded"></span>
                  请选择专家委员会
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                  <button
                    v-for="cat in expertCategories"
                    :key="cat.value"
                    class="px-4 py-3 bg-gradient-to-r from-white to-slate-50 border border-slate-200 text-slate-700 rounded-lg hover:from-blue-50 hover:to-blue-100 hover:border-blue-300 hover:text-brand-700 hover:shadow-sm font-medium text-sm transition-all duration-200 flex items-center justify-center gap-2"
                    @click="navigateTo(`/experts?category=${cat.value}`)"
                  >
                    <span>{{ cat.icon }}</span>
                    <span>{{ cat.label }}</span>
                  </button>
                </div>
              </div>
            </transition>
          </el-tab-pane>

          <!-- 5. 媒体中心 -->
          <el-tab-pane label="媒体中心" name="media">
            <div class="flex justify-between items-end mb-10">
              <div>
                <span class="text-brand-600 font-bold tracking-wider text-sm uppercase">Media Center</span>
                <h2 class="text-3xl font-bold text-slate-900 mt-2">媒体中心</h2>
              </div>
              <div class="flex items-center gap-3">
                <button
                  class="px-4 py-2 rounded-lg border border-slate-200 text-slate-600 text-sm font-bold hover:bg-slate-50"
                  @click="navigateTo('/member/feedback')"
                >
                  咨询客服
                </button>
                <button
                  class="text-brand-600 font-bold text-sm hover:underline flex items-center gap-1"
                  @click="navigateTo('/media/1')"
                >
                  查看更多资讯 <el-icon><ArrowRight /></el-icon>
                </button>
              </div>
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
              <div
                v-for="news in newsList"
                :key="news.id"
                class="group cursor-pointer"
                @click="navigateTo(`/media/${news.id}`)"
              >
                <div class="aspect-video rounded-xl overflow-hidden mb-4 relative shadow-md">
                  <img :src="news.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" alt="News" />
                  <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                  <div class="absolute bottom-4 left-4 right-4 text-white opacity-0 group-hover:opacity-100 transition-opacity">
                    <div class="text-sm font-bold">查看详情 →</div>
                  </div>
                </div>
                <div class="flex gap-2 items-center text-xs text-slate-400 mb-2">
                  <span class="px-2 py-1 bg-slate-100 border border-slate-200 rounded text-slate-600 font-medium">{{ news.tag }}</span>
                  <span>{{ news.date }}</span>
                  <span class="ml-auto flex items-center gap-1"><el-icon><View /></el-icon> {{ news.views }}</span>
                </div>
                <h3 class="font-bold text-lg text-slate-900 mb-2 group-hover:text-brand-600 transition-colors line-clamp-2">{{ news.title }}</h3>
                <p class="text-sm text-slate-500 line-clamp-2">{{ news.excerpt }}</p>
              </div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>

    <!-- 所有弹窗保持不变 -->
    <!-- 弹窗:活动报名 -->
    <el-dialog v-model="signupDialogVisible" :title="dialogTitle" width="650px" align-center :close-on-click-modal="false">
      <div v-if="!isLoggedIn" class="text-center py-8">
        <div class="w-20 h-20 bg-gradient-to-br from-orange-50 to-orange-100 rounded-full flex items-center justify-center mx-auto mb-4 border-4 border-orange-200">
          <el-icon class="text-4xl text-orange-600"><Lock /></el-icon>
        </div>
        <h3 class="text-xl font-bold text-slate-900 mb-3">请先登录会员账号</h3>
        <p class="text-slate-500 text-sm mb-2 max-w-md mx-auto">为了提供更好的服务并确保您的权益,报名前需要先登录或注册会员账号。</p>
        <p class="text-xs text-slate-400 mb-6">登录后将自动返回本页面继续报名</p>
        <div class="flex justify-center gap-4">
          <el-button @click="signupDialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="goToLogin" size="large" class="px-8">立即登录 / 注册</el-button>
        </div>
      </div>

      <div v-else>
        <div v-if="currentActivity.isPaid" class="bg-gradient-to-r from-orange-50 to-amber-50 p-4 rounded-lg mb-6 border border-orange-200 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-xl shadow-sm">💰</div>
            <div>
              <div class="font-bold text-orange-900 text-sm">收费活动</div>
              <div class="text-xs text-orange-700">报名费:<span class="text-lg font-bold">¥{{ currentActivity.price }}</span> / 人</div>
            </div>
          </div>
          <div class="text-right">
            <el-tag type="success" size="small" effect="dark" v-if="canFreePaidActivity"><el-icon><Trophy /></el-icon> SVIP权益可抵扣</el-tag>
            <el-tag type="warning" size="small" v-else><el-icon><CreditCard /></el-icon> 需在线支付</el-tag>
          </div>
        </div>

        <div v-else class="bg-gradient-to-r from-green-50 to-emerald-50 p-4 rounded-lg mb-6 border border-green-200 flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-green-100 text-green-600 flex items-center justify-center text-xl shadow-sm">🎉</div>
          <div><div class="font-bold text-green-900 text-sm">免费活动</div><div class="text-xs text-green-700">无需支付,填写信息即可报名</div></div>
        </div>

        <el-form :model="signupForm" label-position="top" size="large">
          <div class="bg-blue-50 p-4 rounded-lg mb-6 border border-blue-100 flex items-start gap-3">
            <el-icon class="text-blue-600 text-lg mt-0.5"><InfoFilled /></el-icon>
            <div class="text-sm text-blue-800"><div class="font-bold mb-1">温馨提示:</div>请准确填写公司及职务信息,以便我们为您制作参会证件及安排商务对接。</div>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="公司主体全称 (Company Name)" required><el-input v-model="signupForm.company" placeholder="请输入营业执照上的全称" /></el-form-item>
            <el-form-item label="职务 (Job Title)" required><el-input v-model="signupForm.jobTitle" placeholder="如: CEO / 采购经理" /></el-form-item>
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="参会人姓名" required><el-input v-model="signupForm.contact" placeholder="请输入真实姓名" /></el-form-item>
            <el-form-item label="联系电话" required><el-input v-model="signupForm.phone" placeholder="用于接收参会短信" maxlength="11" /></el-form-item>
          </div>
          <el-form-item>
            <el-checkbox v-model="signupForm.subscribe"><span class="text-sm">订阅本活动提醒(活动开始前24小时发送短信通知)</span></el-checkbox>
          </el-form-item>
        </el-form>
        
        <div class="flex gap-3 justify-end mt-6 pt-4 border-t border-slate-100 items-center">
          <div class="mr-auto text-xs text-slate-400" v-if="currentActivity.isPaid && !canFreePaidActivity"><el-icon><InfoFilled /></el-icon> 点击提交后将跳转至支付页面</div>
          <el-button @click="signupDialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="submitSignup" size="large" :loading="signupSubmitting" :color="currentActivity.isPaid && !canFreePaidActivity ? '#ea580c' : '#0284c7'" class="px-8">
            <template v-if="currentActivity.isPaid && !canFreePaidActivity"><el-icon><CreditCard /></el-icon> 去支付 ¥{{ currentActivity.price }}</template>
            <template v-else><el-icon><Check /></el-icon> 确认报名</template>
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 支付二维码弹窗 -->
    <el-dialog v-model="payDialogVisible" title="收银台" width="420px" align-center :close-on-click-modal="false">
      <div class="text-center">
        <div class="mb-4">
          <div class="text-sm text-slate-500 mb-2">请使用微信/支付宝扫码支付</div>
          <div class="text-xs text-slate-400">支付成功后将自动完成报名</div>
        </div>
        <div class="w-56 h-56 bg-gradient-to-br from-slate-100 to-slate-200 mx-auto mb-4 flex items-center justify-center border-2 border-dashed border-slate-300 rounded-xl">
          <div class="text-center">
            <el-icon class="text-5xl text-slate-400 mb-2"><Picture /></el-icon>
            <div class="text-xs text-slate-400">模拟支付二维码</div>
          </div>
        </div>
        <div class="flex items-center justify-center gap-2 mb-6">
          <span class="text-slate-500 text-sm">订单金额:</span>
          <span class="text-3xl font-bold text-orange-600">¥{{ currentActivity.price }}</span>
        </div>
        <el-button type="success" class="w-full" size="large" :loading="paySubmitting" @click="finishPayment"><el-icon><Check /></el-icon> 模拟支付成功</el-button>
      </div>
    </el-dialog>

    <!-- 城市服务点弹窗 -->
    <el-dialog v-model="cityDialogVisible" :title="cityDetail.name + '服务中心'" width="500px" align-center>
      <div class="space-y-4">
        <div class="flex items-center gap-4 p-4 bg-slate-50 rounded-lg border border-slate-100">
          <div class="w-12 h-12 rounded-full bg-brand-100 flex items-center justify-center text-2xl">🏢</div>
          <div><div class="font-bold text-slate-900 text-lg">{{ cityDetail.manager }}</div><div class="text-xs text-slate-500">区域负责人</div></div>
          <el-tag size="small" type="success" class="ml-auto">已认证</el-tag>
        </div>
        <div class="space-y-3 text-sm text-slate-600 px-2">
          <div class="flex"><span class="w-20 text-slate-400">服务地址:</span><span class="flex-1">{{ cityDetail.address }}</span></div>
          <div class="flex"><span class="w-20 text-slate-400">咨询热线:</span><span class="flex-1 font-mono font-bold text-brand-600">{{ cityDetail.phone }}</span></div>
        </div>
      </div>
      <template #footer><el-button type="primary" class="w-full" @click="cityDialogVisible = false" size="large">立即联系</el-button></template>
    </el-dialog>

    <!-- 服务商详情弹窗 -->
    <el-dialog v-model="supplierDetailVisible" title="服务商基本信息" width="700px" align-center>
      <div class="space-y-4">
        <div class="flex items-center gap-4 p-5 bg-slate-50 rounded-lg border border-slate-100">
          <div class="w-16 h-16 rounded-xl bg-white shadow-sm flex items-center justify-center text-3xl">{{ currentSupplier.icon }}</div>
          <div class="flex-1"><div class="font-bold text-xl text-slate-900">{{ currentSupplier.name }}</div><div class="text-xs text-slate-400 mt-1">入驻: {{ currentSupplier.joinDate }}</div></div>
          <el-tag type="success">已认证</el-tag>
        </div>
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-1">企业性质</div><div class="font-bold text-slate-900">{{ currentSupplier.type }}</div></div>
          <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-1">所在城市</div><div class="font-bold text-slate-900">{{ currentSupplier.city }}</div></div>
          <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-1">主营业务</div><div class="font-bold text-slate-900">{{ currentSupplier.category }}</div></div>
          <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-1">联系电话</div><div class="font-bold text-brand-600 font-mono">{{ currentSupplier.phone }}</div></div>
        </div>
        <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-2 text-sm">服务项目</div><div class="flex flex-wrap gap-2"><el-tag v-for="service in currentSupplier.services" :key="service" size="small">{{ service }}</el-tag></div></div>
        <div class="p-4 bg-slate-50 rounded-lg"><div class="text-slate-400 mb-2 text-sm">企业简介</div><div class="text-slate-600 text-sm leading-relaxed">{{ currentSupplier.intro }}</div></div>
      </div>
      <template #footer>
        <div class="flex gap-3">
          <el-button @click="supplierDetailVisible = false" class="flex-1" size="large">关闭</el-button>
          <el-button type="primary" @click="downloadSupplierPDF(currentSupplier)" class="flex-1" size="large"><el-icon><Download /></el-icon> 下载企业介绍PDF</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="activityDetailVisible"
      :title="activityDetail.title || '活动详情'"
      width="760px"
      align-center
    >
      <div class="space-y-4">
        <div class="rounded-xl overflow-hidden border border-slate-200">
          <img
            v-if="activityDetail.image"
            :src="activityDetail.image"
            :alt="activityDetail.title"
            class="w-full h-56 object-cover"
          />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div class="bg-slate-50 rounded-lg p-3 border border-slate-200">
            <div class="text-slate-500">类型</div>
            <div class="font-semibold text-slate-900">{{ activityDetail.type || activityDetail.tag || '培训/活动' }}</div>
          </div>
          <div class="bg-slate-50 rounded-lg p-3 border border-slate-200">
            <div class="text-slate-500">时间/频次</div>
            <div class="font-semibold text-slate-900">{{ activityDetail.date || activityDetail.freq || '待发布' }}</div>
          </div>
          <div class="bg-slate-50 rounded-lg p-3 border border-slate-200">
            <div class="text-slate-500">地点</div>
            <div class="font-semibold text-slate-900">{{ activityDetail.location || '线上/线下待定' }}</div>
          </div>
          <div class="bg-slate-50 rounded-lg p-3 border border-slate-200">
            <div class="text-slate-500">费用</div>
            <div class="font-semibold text-slate-900">
              <span v-if="activityDetail.isPaid">¥{{ activityDetail.price || 0 }}</span>
              <span v-else>免费</span>
            </div>
          </div>
        </div>

        <div class="bg-white border border-slate-200 rounded-lg p-4">
          <div class="text-slate-500 text-sm mb-2">详情介绍</div>
          <div class="text-slate-800 leading-7 text-sm whitespace-pre-line">
            {{ activityDetail.longDesc || activityDetail.desc || '该活动/培训聚焦国际公共采购实操，覆盖政策解读、案例拆解与报名辅导。' }}
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="activityDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goSignupFromDetail">立即报名</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Location, ArrowDown, ArrowRight, ArrowLeft, Right, Search, MapLocation, 
  View, HomeFilled, Bell, InfoFilled, Download, Picture, Setting,
  Lock, Trophy, CreditCard, Check, Calendar, VideoCamera
} from '@element-plus/icons-vue'
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLoginUser, apiRequest } from '@/utils/request'

useHead({ title: '平台服务 - XRIPP全球公共采购服务平台' })

type MemberLevel = 'NORMAL' | 'VIP' | 'SVIP'

const normalizeLevel = (raw: unknown): MemberLevel => {
  const v = String(raw || '').toUpperCase()
  if (v === 'SVIP') return 'SVIP'
  if (v === 'VIP') return 'VIP'
  return 'NORMAL'
}

const authUser = ref<any>(getLoginUser())
const isLoggedIn = computed(() => !!authUser.value)
const memberLevel = computed<MemberLevel>(() => normalizeLevel(authUser.value?.member_level))

const canFreePaidActivity = computed(() =>
  isLoggedIn.value && memberLevel.value === 'SVIP'
)

if (import.meta.client) {
  window.addEventListener('storage', () => {
    authUser.value = getLoginUser()
  })
}

const activeTab = ref('supplier') // ✅ 默认为服务商库
const expertExpanded = ref(false)

const expertCategories = [
  { value: '生命科学', label: '生命科学专家委', icon: '🧬' },
  { value: '生态环境', label: '生态环境专家委', icon: '🌱' },
  { value: '医疗健康', label: '医疗健康专家委', icon: '⚕️' },
  { value: '智慧城市', label: '智慧城市专家委', icon: '🏙️' },
  { value: '信息技术', label: '信息技术专家委', icon: '💻' },
  { value: '机器人制造', label: '机器人制造专家委', icon: '🤖' }
]

const resources = [
  { icon: '🏢', title: '行业专委会', desc: '连接行业协会、分会、分中心', cta: '查看名录', path: '/committee-list', comingSoon: true },
  { icon: '👨‍🏫', title: '专家智库', desc: '协会内专家一对一咨询指导服务', cta: '预约专家', path: '/experts', comingSoon: false },
  { icon: '🤝', title: '项目对接', desc: 'C对C、C对G项目撮合服务', cta: '发布需求', path: '/member/publish-demand', comingSoon: false }
]

const activityTypes = [
  {
    title: '公益行活动',
    subTitle: '免费科普',
    tag: '免费',
    image: 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800&q=80',
    desc: '助力中小企业扫盲,提供入门级培训与政策解读',
    longDesc:
      '面向初次接触国际公共采购的企业,系统讲解联合国及国际采购规则、常见准入门槛与投标流程。课程包含政策解读、案例拆解与实操答疑,帮助企业快速建立从“看得懂”到“能上手”的基础能力。',
    btnText: '立即报名',
    btnColor: 'text-green-600',
    colorClass: 'text-green-600',
    freq: '全年免费',
    isPaid: false,
    price: 0
  },
  {
    title: '享嘉之会',
    subTitle: '高端沙龙',
    tag: '高端',
    image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80',
    desc: '连接行业领袖与采购官,深度探讨国际公共采购趋势与机遇',
    longDesc:
      '围绕公共采购热点议题组织高端闭门交流,邀请行业专家、采购负责人及企业代表共同研判趋势。通过主题分享与圆桌互动,帮助企业识别高价值赛道、优化市场进入策略并建立合作网络。',
    btnText: '查看排期',
    btnColor: 'text-blue-600',
    colorClass: 'text-blue-600',
    freq: '12场/年',
    isPaid: true,
    price: 299
  },
  {
    title: '出海考察',
    subTitle: '实地探访',
    tag: '实地',
    image: 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&q=80',
    desc: '实地探访海外市场,对接当地政府与产业园区资源',
    longDesc:
      '组织企业赴重点目标国开展实地考察,覆盖园区参访、政策沟通、服务商对接与项目机会交流。通过“前期调研+现场走访+会后对接”闭环,提升企业跨境落地效率与合作成功率。',
    btnText: '近期行程',
    btnColor: 'text-purple-600',
    colorClass: 'text-purple-600',
    freq: '每季度',
    isPaid: true,
    price: 19800
  },
  {
    title: '行业沙龙',
    subTitle: '深度交流',
    tag: '行业',
    image: 'https://images.unsplash.com/photo-1591115765373-5207764f72e7?w=800&q=80',
    desc: '分享中标案例与实战经验,解决企业实际痛点',
    longDesc:
      '聚焦不同行业的典型投标场景,由实战团队分享中标案例、失标复盘与应对策略。通过小规模深度讨论,帮助企业在资格准备、技术响应、商务报价与风险控制等关键环节形成可执行方案。',
    btnText: '参与讨论',
    btnColor: 'text-orange-600',
    colorClass: 'text-orange-600',
    freq: '每月2场',
    isPaid: true,
    price: 199
  }
]

const apiActivities = ref<any[]>([])

const mapApiActivityToCard = (a: any) => ({
  id: a.id,
  type: '活动',
  date: a.startTime ? String(a.startTime).slice(0, 10) : '待定',
  location: '待定',
  title: a.title || '未命名活动',
  image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80',
  isPaid: !(a.isFree ?? true),
  price: Number(a.fee || 0)
})

const loadActivities = async () => {
  try {
    const res: any = await $fetch('/api/v3/activities', {
      query: { page: 1, page_size: 6 }
    })
    const items = res?.data?.items || []
    apiActivities.value = items.map(mapApiActivityToCard)
  } catch (e) {
    apiActivities.value = []
  }
}

const recentActivities = computed(() => apiActivities.value)

onMounted(() => {
  loadActivities()
  loadLatestSuppliers()
})

const trainingCourses = [
  {
    title: '出海系列培训',
    desc: '从注册到投标的全流程实操课程',
    longDesc:
      '围绕企业出海公共采购的完整链路设计课程,覆盖账号注册、资质准备、投标流程、关键节点风控与材料提交规范。通过案例拆解与实操演练,帮助企业建立可复制的项目执行方法。',
    isPaid: true,
    price: 2999
  },
  {
    title: '联采项目青年全球胜任力培养计划',
    desc: 'ISO、CE、FDA 等国际资质认证辅导',
    longDesc:
      '面向青年骨干与业务负责人,系统讲解国际资质认证路径与合规要点,包含认证标准解读、材料准备方法、审核常见问题及整改思路。目标是提升团队国际项目胜任力与持续交付能力。',
    isPaid: true,
    price: 1500
  },
  {
    title: '国际公共采购（联合国篇）',
    desc: '政策解读与申报材料撰写指导',
    longDesc:
      '聚焦联合国采购体系,深入讲解政策口径、供应商准入规则、评审关注点与申报文本结构。通过“政策解读+模板讲解+实战答疑”模式,提升企业在联合国项目中的申报质量与通过率。',
    isPaid: true,
    price: 5000
  }
]

const trainingImages = [
  'https://images.unsplash.com/photo-1544531586-fde5298cdd40?q=80&w=1200&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1517048676732-d65bc937f952?q=80&w=1200&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1552664730-d307ca884978?q=80&w=1200&auto=format&fit=crop'
]

const cities = ['上海', '北京', '深圳', '广州', '杭州', '成都', '武汉', '南京', '苏州', '宁波', '天津', '重庆', '西安', '郑州', '长沙']
const provinces = ['上海市', '北京市', '广东省', '浙江省', '江苏省', '四川省', '湖北省', '陕西省', '河南省', '湖南省']
const platformStats = [{ value: '4,600+', label: '服务企业' }, { value: '26', label: '联合国机构' }, { value: '193', label: '覆盖国家' }, { value: '99.8%', label: '注册成功率' }]
const newsList = [
  { id: 1, tag: '成功案例', date: '2025-01-15', views: '1.2k', title: 'XRIPP平台助力上海企业中标联合国$2.5M医疗设备项目', excerpt: '经过3个月的精心准备,上海某医疗器械企业成功中标...', image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800&q=80' },
  { id: 2, tag: '政策解读', date: '2025-01-10', views: '856', title: '2025年联合国采购政策重大调整解读', excerpt: '联合国采购部门近日发布最新政策指南,对供应商资质...', image: 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800&q=80' },
  { id: 3, tag: '战略合作', date: '2025-01-05', views: '643', title: 'XRIPP与马来西亚能源部签署战略合作协议', excerpt: 'XRIPP Global与马来西亚能源部达成战略合作,共同推动...', image: 'https://images.unsplash.com/photo-1600880292203-757bb62b4baf?w=800&auto=format&fit=crop' }
]

const selectedCity = ref('上海')
const citySearchQuery = ref('')
const cityDialogVisible = ref(false)
const cityDetail = ref({ name: '', manager: '', phone: '', address: '' })

const filteredCities = computed(() => {
  if (!citySearchQuery.value) return cities
  return cities.filter(city => city.includes(citySearchQuery.value))
})

const signupDialogVisible = ref(false)
const signupSubmitting = ref(false)
const paySubmitting = ref(false)
const payDialogVisible = ref(false)
const currentActivity = ref<any>({})
const currentRegistrationId = ref<number | null>(null)
const activityDetailVisible = ref(false)
const activityDetail = ref<any>({})

const openActivityDetail = (act: any) => {
  activityDetail.value = { ...act }
  activityDetailVisible.value = true
}

const goSignupFromDetail = () => {
  activityDetailVisible.value = false
  handleActivityClick(activityDetail.value)
}

const signupForm = ref({ company: '', contact: '', jobTitle: '', phone: '', subscribe: false })

const dialogTitle = computed(() => !isLoggedIn.value ? '需要登录' : '立即报名:' + (currentActivity.value.title || ''))

const handleActivityClick = (act: any) => {
  currentActivity.value = act
  if (!isLoggedIn.value) {
    signupDialogVisible.value = true
  } else {
    signupForm.value = {
      company: '上海宏大进出口贸易有限公司',
      contact: '张伟',
      jobTitle: '采购总监',
      phone: '13800138000',
      subscribe: false
    }
    signupDialogVisible.value = true
  }
}

const goToLogin = () => {
  signupDialogVisible.value = false
  navigateTo('/login?redirect=/services')
}

const submitSignup = async () => {
  if (signupSubmitting.value) return

  if (!signupForm.value.company || !signupForm.value.contact || !signupForm.value.jobTitle || !signupForm.value.phone) {
    return ElMessage.warning('请填写所有必填项')
  }

  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(signupForm.value.phone)) {
    return ElMessage.warning('请输入正确的11位手机号')
  }

  if (!currentActivity.value?.id) {
    return ElMessage.warning('当前活动缺少ID，暂无法报名')
  }

  try {
    signupSubmitting.value = true

    const res: any = await $fetch(`/api/v3/activities/${currentActivity.value.id}/registrations`, {
      method: 'POST',
      body: {
        company_name: signupForm.value.company,
        contact_name: signupForm.value.contact,
        phone: signupForm.value.phone
      }
    })

    currentRegistrationId.value = Number(res?.data?.registration_id || 0)
    const status = res?.data?.registration_status

    if (status === 'pending_payment') {
      signupDialogVisible.value = false
      payDialogVisible.value = true
    } else {
      finishSignup()
    }
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '报名提交失败')
  } finally {
    signupSubmitting.value = false
  }
}

const finishPayment = async () => {
  if (paySubmitting.value) return

  if (!currentRegistrationId.value) {
    return ElMessage.warning('缺少报名记录ID，无法确认支付')
  }

  try {
    paySubmitting.value = true

    await $fetch('/api/v3/internal/payments/callback', {
      method: 'POST',
      body: {
        registration_id: currentRegistrationId.value,
        pay_status: 'success'
      }
    })

    payDialogVisible.value = false
    ElMessage.success({ message: '支付成功!正在为您完成报名...', duration: 2000 })
    await loadActivities()
    setTimeout(() => finishSignup(), 300)
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '支付回写失败，请稍后重试')
  } finally {
    paySubmitting.value = false
  }
}

const finishSignup = () => {
  signupDialogVisible.value = false
  ElMessage.success({ message: `报名成功!${signupForm.value.subscribe ? '已订阅短信提醒' : ''}`, duration: 3000 })
}

const handleSubscribe = (act: any) => {
  if (!isLoggedIn.value) return ElMessage.warning('请先登录后订阅')
  ElMessage.success(`已订阅 "${act.title}",活动开始前将短信提醒您`)
}

const handleCitySelect = (city: string) => {
  selectedCity.value = city
  cityDetail.value = { name: city, manager: '李经理', phone: '138-0000-8888', address: `${city}市高新技术产业园区服务中心305室` }
  cityDialogVisible.value = true
}

const toggleExpertExpanded = () => {
  expertExpanded.value = !expertExpanded.value
}

const handleResourceClick = (res: any) => {
  if (res.comingSoon) {
    ElMessage.info({ message: '该功能即将上线,敬请期待!', duration: 2000 })
    return
  }

  expertExpanded.value = false
  if (res.path) navigateTo(res.path)
}

// ✅ 服务商相关数据
const supplierFilters = ref({ province: '', keyword: '', serviceType: '' })
const supplierList = ref<any[]>([])
const hasSearched = ref(false)
const supplierDetailVisible = ref(false)
const currentSupplier = ref<any>({})

const suppliersLoading = ref(false)
const latestSuppliers = ref<any[]>([])

// 将 API 返回的 supplier_onboarding 记录归一化为模板字段
const mapSupplierItem = (item: any) => {
  let services: string[] = []
  try { services = JSON.parse(item.serviceTypesJson || '[]') } catch { services = [] }
  return {
    id: item.id,
    name: item.companyName || '未命名企业',
    city: item.cityName || '-',
    category: services[0] || '综合服务',
    services,
    joinDate: item.joinDate || '',
    verified: true,
    icon: '🏢',
    type: '服务商',
    phone: '-',
    intro: item.intro || ''
  }
}

const loadLatestSuppliers = async () => {
  suppliersLoading.value = true
  try {
    const res: any = await apiRequest('/v3/suppliers?page=1&page_size=10')
    latestSuppliers.value = (res?.data?.items || []).map(mapSupplierItem)
  } catch {
    latestSuppliers.value = []
  } finally {
    suppliersLoading.value = false
  }
}

// 省份 → 城市关键词映射（与 API city 参数对应）
const provinceCityMap: Record<string, string> = {
  '上海市': '上海', '北京市': '北京', '广东省': '深圳',
  '河南省': '郑州', '浙江省': '杭州', '江苏省': '苏州',
  '四川省': '成都', '湖北省': '武汉'
}

// 服务类型 → 关键词映射（用于 API keyword 搜索）
const serviceTypeKwMap: Record<string, string> = {
  'bid_writing': '标书', 'export_consulting': '咨询',
  'certification': '认证', 'logistics': '物流'
}

const searchSupplier = async () => {
  hasSearched.value = true
  suppliersLoading.value = true
  try {
    const params: Record<string, string> = { page: '1', page_size: '50' }

    if (supplierFilters.value.province) {
      const city = provinceCityMap[supplierFilters.value.province]
      if (city) params.city = city
    }

    let kw = supplierFilters.value.keyword.trim()
    if (!kw && supplierFilters.value.serviceType) {
      kw = serviceTypeKwMap[supplierFilters.value.serviceType] || ''
    }
    if (kw) params.keyword = kw

    const qs = new URLSearchParams(params).toString()
    const res: any = await apiRequest(`/v3/suppliers?${qs}`)
    const items = (res?.data?.items || []).map(mapSupplierItem)
    supplierList.value = items

    if (items.length > 0) ElMessage.success(`找到 ${items.length} 家符合条件的服务商`)
    else ElMessage.info('未找到符合条件的服务商，请调整筛选条件')
  } catch (e: any) {
    ElMessage.error(e?.message || '查询失败')
    supplierList.value = []
  } finally {
    suppliersLoading.value = false
  }
}

const clearSearch = () => {
  hasSearched.value = false
  supplierList.value = []
  supplierFilters.value = { province: '', keyword: '', serviceType: '' }
}

const viewSupplierDetail = (supplier: any) => {
  currentSupplier.value = supplier
  supplierDetailVisible.value = true
}

const downloadSupplierPDF = (supplier: any) => {
  ElMessage.success({ message: `正在下载 "${supplier.name}" 的企业介绍PDF...`, duration: 2000 })
}
</script>

<style scoped>
@keyframes slow-zoom { 0% { transform: scale(1); } 50% { transform: scale(1.1); } 100% { transform: scale(1); } }
.animate-slow-zoom { animation: slow-zoom 20s ease-in-out infinite; }
@keyframes marquee { 0% { transform: translateX(100%); } 100% { transform: translateX(-100%); } }
.animate-marquee { animation: marquee 30s linear infinite; }

:deep(.custom-tabs .el-tabs__header) {
  margin-bottom: 2rem;
  border-bottom: none !important;
}
:deep(.custom-tabs .el-tabs__item) {
  font-size: 1.125rem;
  font-weight: 600;
  padding: 0 2rem !important;
  height: 3.5rem !important;
  line-height: 3.5rem !important;
  border-radius: 0.75rem 0.75rem 0 0 !important;
  background: #f8fafc;
  color: #64748b;
  transition: all 0.3s;
  margin-right: 0.375rem;
}
:deep(.custom-tabs .el-tabs__item.is-active) {
  background: white !important;
  color: #2563eb !important;
  border-bottom: 3px solid #2563eb !important;
  box-shadow: 0 6px 16px -4px rgba(37,99,235,0.15);
  transform: translateY(-2px);
}
:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  height: 0 !important;
}
</style>
