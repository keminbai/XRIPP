<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\activities.vue
  版本: V2.0 科学修复版 (2026-01-29)
  
  ✅ 核心修复:
  1. [业务审核] 补充审核中状态的通过/驳回按钮（仅总部可见）
  2. [双轨制] 业务页面审核 + 独立审核中心聚合
  3. [完整流程] 发布→审核→上线→显示申请→下架全闭环
  4. [权限控制] 合伙人发布自动进入审核，总部直接发布
  
  📋 修复依据: Gemini审查反馈 + 双轨制架构原则
  ========================================================================
-->
<template>
  <div class="space-y-6">
    
    <!-- 1. 顶部统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div 
        v-for="(stat, i) in stats" 
        :key="i" 
        class="bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow flex items-center justify-between"
      >
        <div>
          <div class="text-sm text-slate-500 mb-1">{{ stat.label }}</div>
          <div class="text-2xl font-bold text-slate-800">{{ stat.value }}</div>
        </div>
        <div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl" :class="stat.bgClass">
          <el-icon :class="stat.textClass"><component :is="stat.icon" /></el-icon>
        </div>
      </div>
    </div>

    <!-- 2. 主体管理区域 -->
    <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      
      <!-- 顶部操作栏 -->
      <div class="p-6 border-b border-slate-100">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h3 class="text-lg font-bold text-slate-800">活动管理</h3>
            <p class="text-xs text-slate-500 mt-1">发布和管理各类活动,包括亨嘉之会、公益行、出海考察等</p>
          </div>
          <el-button type="primary" color="#0f172a" @click="openPublishDialog">
            <el-icon class="mr-2"><Plus /></el-icon> 新建活动
          </el-button>
        </div>

        <!-- 筛选条件 -->
        <div class="flex gap-3">
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索标题/编号..." 
            prefix-icon="Search" 
            class="w-64" 
            clearable 
          />
          <el-select v-model="filters.status" placeholder="状态筛选" class="w-32" clearable>
            <el-option label="已发布" value="published" />
            <el-option label="审核中" value="auditing" />
            <el-option label="已驳回" value="rejected" />
            <el-option label="已下架" value="offline" />
          </el-select>
          <el-select v-model="filters.publisher" placeholder="发布者" class="w-32" clearable>
            <el-option label="总部" value="总部" />
            <el-option label="合伙人" value="合伙人" />
          </el-select>
          <el-button type="primary" plain @click="handleSearch">查询</el-button>
        </div>

        <!-- 合伙人提示 -->
        <el-alert 
          v-if="currentUserRole === 'partner'"
          title="合伙人发布须知" 
          type="warning" 
          :closable="false"
          class="mt-4"
        >
          <template #default>
            <div class="text-sm">
              您发布的活动将自动进入<strong>审核流程</strong>,经总部审核通过后才会展示在前台。
              <el-button link type="primary" size="small" @click="navigateTo('/admin/audit')">
                查看审核状态
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 活动列表表格 -->
      <div class="p-6">
        <el-table 
          :data="filteredActivityList" 
          style="width: 100%" 
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
        >
          <el-table-column prop="code" label="编号" width="130">
            <template #default="scope">
              <span class="font-mono text-xs text-slate-500">{{ scope.row.code }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="活动概览" min-width="280">
            <template #default="scope">
              <div class="flex gap-3 py-2">
                <div class="relative w-16 h-10 rounded overflow-hidden flex-shrink-0 bg-slate-100">
                  <img :src="scope.row.image" class="w-full h-full object-cover" />
                  <div v-if="scope.row.hasVideo" class="absolute inset-0 bg-black/30 flex items-center justify-center text-white">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-slate-800 text-sm line-clamp-1">{{ scope.row.title }}</div>
                  <div class="flex gap-2 mt-1">
                    <el-tag size="small" :type="scope.row.subTypeTag">{{ scope.row.subTypeLabel }}</el-tag>
                    <span class="text-xs text-slate-400 flex items-center" v-if="scope.row.cities && scope.row.cities.length">
                      <el-icon class="mr-0.5"><Location /></el-icon> {{ scope.row.cities.join(' / ') }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="前台位置" width="180">
            <template #default="scope">
              <div class="text-xs">
                <div class="font-medium text-slate-700">{{ scope.row.frontModuleLabel }}</div>
                <div class="text-slate-400 mt-0.5">{{ scope.row.frontPositionLabel }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="发布者" width="120">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.publisher === '总部' ? 'danger' : 'primary'" effect="dark">
                {{ scope.row.publisher }}
              </el-tag>
              <div class="text-[10px] text-slate-400 mt-0.5">{{ scope.row.publishDate }}</div>
            </template>
          </el-table-column>

          <el-table-column label="费用设置" width="150">
            <template #default="scope">
              <div v-if="scope.row.feeItemName && scope.row.feeItemName !== '免费'">
                <div class="font-bold text-slate-700 text-sm">{{ scope.row.feeItemName }}</div>
                <div class="text-xs text-orange-600">¥{{ scope.row.price }}</div>
                <el-tag 
                  v-if="scope.row.feeType && scope.row.feeType !== 'paid'" 
                  size="small" 
                  type="success" 
                  class="mt-1"
                >
                  {{ scope.row.feeType === 'member-free' ? '会员免费' : '会员优惠' }}
                </el-tag>
              </div>
              <div v-else class="text-green-600 font-bold">免费</div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100">
            <template #default="scope">
              <div class="flex items-center gap-1.5">
                <span class="w-1.5 h-1.5 rounded-full" :class="getStatusColor(scope.row.status)"></span>
                <span class="text-xs" :class="getStatusTextColor(scope.row.status)">
                  {{ scope.row.statusLabel }}
                </span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="报名数据" width="140">
            <template #default="scope">
              <div>
                <el-button link type="primary" size="small" @click="viewSignups(scope.row)">
                  {{ scope.row.signups }} / {{ scope.row.maxLimit || '∞' }}
                </el-button>
                <el-progress 
                  v-if="scope.row.maxLimit"
                  :percentage="Math.round((scope.row.signups / scope.row.maxLimit) * 100)" 
                  :stroke-width="4"
                  :show-text="false"
                  :color="scope.row.signups >= scope.row.maxLimit ? '#f56c6c' : '#67c23a'"
                  class="mt-1"
                />
                <div v-if="scope.row.signups >= scope.row.maxLimit" class="text-[10px] text-red-500 mt-0.5">
                  已满额
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- ✅ 核心修复：操作列补充审核按钮 -->
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              
              <!-- ✅ 审核中状态：显示审核按钮（仅总部） -->
              <div v-if="scope.row.status === 'auditing' && currentUserRole === 'admin'" class="flex gap-2">
                <el-button link type="success" size="small" @click="handleApprove(scope.row)">
                  <el-icon class="mr-1"><Check /></el-icon> 通过
                </el-button>
                <el-button link type="danger" size="small" @click="handleReject(scope.row)">
                  <el-icon class="mr-1"><Close /></el-icon> 驳回
                </el-button>
                <el-button link type="primary" size="small" @click="handleViewDetail(scope.row)">
                  <el-icon class="mr-1"><View /></el-icon> 详情
                </el-button>
              </div>

              <!-- 已驳回状态：显示驳回原因 + 重新编辑 -->
              <div v-else-if="scope.row.status === 'rejected'" class="flex gap-2">
                <el-popover placement="top" width="300" trigger="hover">
                  <template #reference>
                    <el-button link type="warning" size="small">
                      <el-icon class="mr-1"><Warning /></el-icon> 查看原因
                    </el-button>
                  </template>
                  <div class="text-sm">
                    <div class="font-bold text-red-600 mb-2">驳回原因：</div>
                    <div class="text-slate-700">{{ scope.row.rejectReason || '无' }}</div>
                  </div>
                </el-popover>
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                  <el-icon class="mr-1"><Edit /></el-icon> 重新编辑
                </el-button>
              </div>

              <!-- 常规操作按钮 -->
              <div v-else class="flex gap-2 flex-wrap">
                <el-button link type="primary" size="small" @click="handleEdit(scope.row)">
                  <el-icon class="mr-1"><Edit /></el-icon> 编辑
                </el-button>
                <el-button 
                  link 
                  type="success" 
                  size="small" 
                  @click="openDisplayApplyDialog(scope.row)" 
                  v-if="scope.row.status === 'published'"
                >
                  <el-icon class="mr-1"><Monitor /></el-icon> 申请显示
                </el-button>
                <el-button 
                  link 
                  :type="scope.row.status === 'published' ? 'danger' : 'warning'" 
                  size="small" 
                  @click="handleToggleStatus(scope.row)"
                  v-if="['published', 'offline'].includes(scope.row.status)"
                >
                  {{ scope.row.status === 'published' ? '下架' : '上架' }}
                </el-button>
              </div>

            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            v-model:current-page="currentPage"
            background
            layout="total, prev, pager, next"
            :total="totalItems"
            :page-size="pageSize"
            @current-change="loadActivities"
          />
        </div>
      </div>

    </div>

    <!-- 🌟 活动发布弹窗 -->
    <el-dialog 
      v-model="publishDialogVisible" 
      :title="isEdit ? '编辑活动' : '新建活动'" 
      width="950px" 
      top="5vh" 
      :close-on-click-modal="false"
      :destroy-on-close="true"
    >
      <el-form :model="form" label-width="120px" :rules="formRules" ref="formRef">
        
        <!-- A. 基础信息 -->
        <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
          <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-blue-500 pl-2">基础信息</h4>
          
          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="活动类型" required prop="subType">
              <el-select v-model="form.subType" class="w-full" placeholder="请选择">
                <el-option label="亨嘉之会" value="hengjia" />
                <el-option label="公益行" value="charity" />
                <el-option label="出海考察" value="inspection" />
                <el-option label="行业沙龙" value="salon" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="前台主模块" required prop="frontModule">
              <el-select 
                v-model="form.frontModule" 
                class="w-full" 
                placeholder="请选择" 
                @change="handleModuleChange"
              >
                <el-option label="首页轮播图" value="home-banner" />
                <el-option label="活动中心" value="activity-center" />
                <el-option label="资源拓展" value="resource-expansion" />
              </el-select>
            </el-form-item>
          </div>

          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="详细位置" required prop="frontPosition">
              <el-select 
                v-model="form.frontPosition" 
                class="w-full" 
                placeholder="请先选择前台模块"
                :disabled="!form.frontModule"
              >
                <template v-if="form.frontModule === 'home-banner'">
                  <el-option label="轮播图主位" value="banner-main" />
                  <el-option label="轮播图副位" value="banner-sub" />
                </template>
                <template v-else-if="form.frontModule === 'activity-center'">
                  <el-option label="顶部焦点位" value="top" />
                  <el-option label="列表推荐位" value="list" />
                </template>
                <template v-else>
                  <el-option label="侧边栏" value="sidebar" />
                  <el-option label="底部栏" value="footer" />
                </template>
              </el-select>
            </el-form-item>

            <el-form-item label="投放城市" required prop="cities">
              <el-select 
                v-model="form.cities" 
                multiple 
                collapse-tags 
                collapse-tags-tooltip
                placeholder="选择投放城市 (可多选)" 
                class="w-full"
                :max-collapse-tags="3"
              >
                <el-option label="全国通用" value="全国" />
                <el-option-group label="重点城市">
                  <el-option 
                    v-for="city in cityOptions" 
                    :key="city" 
                    :label="city" 
                    :value="city" 
                  />
                </el-option-group>
              </el-select>
              <div class="text-xs text-slate-400 mt-1">
                💡 选择"全国通用"后,将在所有城市展示
              </div>
            </el-form-item>
          </div>

          <el-form-item label="活动标题" required prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="请输入活动标题（30字以内）" 
              maxlength="30" 
              show-word-limit 
            />
          </el-form-item>
        </div>

        <!-- B. 媒体素材 -->
        <div class="bg-slate-50 p-5 rounded-lg mb-6 border border-slate-100">
          <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-orange-500 pl-2">媒体素材</h4>
          
          <el-form-item label="封面图片" required>
            <el-upload
              class="upload-demo"
              action="/api/common/upload"
              :limit="1"
              :on-success="handleImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              :file-list="imageFileList"
              list-type="picture-card"
              :auto-upload="true"
            >
              <el-icon><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip text-xs text-slate-500 ml-2">
                  建议尺寸 1920x650px, JPG/PNG格式, 不超过2MB
                </div>
              </template>
            </el-upload>
            
            <div v-if="form.coverImage" class="mt-3 relative w-48 h-28 rounded-lg overflow-hidden border border-slate-200">
              <img :src="form.coverImage" class="w-full h-full object-cover" />
              <div 
                class="absolute top-2 right-2 bg-red-500 text-white w-6 h-6 rounded-full flex items-center justify-center cursor-pointer hover:bg-red-600"
                @click="form.coverImage = ''; imageFileList = []"
              >
                <el-icon><Close /></el-icon>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item label="宣传视频">
            <el-upload
              class="upload-demo"
              action="/api/common/upload"
              :limit="1"
              :on-success="handleVideoSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeVideoUpload"
              :file-list="videoFileList"
              accept="video/mp4,video/quicktime"
              :auto-upload="true"
            >
              <el-button icon="VideoPlay" type="primary" plain>
                <el-icon class="mr-2"><VideoCamera /></el-icon> 上传视频 (MP4/MOV)
              </el-button>
              <template #tip>
                <div class="el-upload__tip text-xs text-slate-500">
                  建议时长1-3分钟, 大小不超过50MB
                </div>
              </template>
            </el-upload>
            
            <div v-if="form.videoUrl" class="mt-3">
              <video :src="form.videoUrl" controls class="w-full max-w-md h-48 rounded-lg border border-slate-200"></video>
              <el-button 
                type="danger" 
                size="small" 
                plain 
                class="mt-2"
                @click="form.videoUrl = ''; form.hasVideo = false; videoFileList = []"
              >
                删除视频
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="活动简介" required>
            <el-input 
              v-model="form.summary" 
              type="textarea" 
              :rows="4"
              placeholder="请输入活动核心内容介绍..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- ✅ 活动专属字段 - 议程 -->
          <div class="mt-4 pt-4 border-t border-slate-200">
            <el-form-item label="活动议程" required>
              <el-input 
                v-model="form.agenda" 
                type="textarea" 
                :rows="4" 
                placeholder="例如：&#10;09:00 签到&#10;09:30 开幕致辞&#10;10:00 主题演讲..."
              />
              <div class="text-xs text-slate-400 mt-1">
                详细列出活动流程,方便参会者了解
              </div>
            </el-form-item>
          </div>
        </div>

        <!-- C. 业务规则与收费 -->
        <div class="bg-slate-50 p-5 rounded-lg border border-slate-100">
          <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-green-500 pl-2">业务规则与收费</h4>
          
          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="活动时间">
              <el-date-picker 
                v-model="form.date" 
                type="datetime" 
                class="!w-full"
                placeholder="选择日期时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            
            <el-form-item label="名额限制">
              <el-input-number 
                v-model="form.maxLimit" 
                :min="0" 
                :max="9999"
                class="!w-full"
                placeholder="0表示不限制"
              />
              <div class="text-xs text-slate-400 mt-1">
                设为0表示不限制报名人数
              </div>
            </el-form-item>
          </div>

          <el-form-item label="关联收费项目" required>
            <el-select 
              v-model="form.feeItemId" 
              class="w-full" 
              placeholder="请选择后台配置的收费项目" 
              @change="handleFeeChange"
            >
              <el-option label="免费 (Free)" value="free" />
              <el-option 
                v-for="item in feeOptions" 
                :key="item.id" 
                :label="`${item.name} (¥${item.price})`" 
                :value="item.id" 
              />
            </el-select>
            <div class="text-xs text-slate-400 mt-1">
              * 金额由后台"业务管理系统-付费机制"统一配置
            </div>
          </el-form-item>

          <div v-if="form.feeItemId !== 'free'" class="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-100">
            <div class="flex items-center gap-2 mb-3">
              <el-icon class="text-blue-600"><Star /></el-icon>
              <span class="font-bold text-sm text-blue-800">会员优惠设置</span>
            </div>
            
            <el-form-item label="会员优惠方式">
              <el-radio-group v-model="form.feeType" @change="handleFeeTypeChange">
                <el-radio label="paid">统一收费(会员非会员同价)</el-radio>
                <el-radio label="member-discount">会员享受折扣价</el-radio>
                <el-radio label="member-free">会员完全免费</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-row :gutter="16" v-if="form.feeType !== 'paid'">
              <el-col :span="12">
                <el-form-item label="非会员价格">
                  <el-input 
                    :value="form.price" 
                    disabled
                    placeholder="自动从关联项目获取"
                  >
                    <template #prepend>¥</template>
                  </el-input>
                </el-form-item>
              </el-col>
              
              <el-col :span="12">
                <el-form-item :label="form.feeType === 'member-free' ? '会员价格' : '会员折扣价'">
                  <el-input 
                    v-model="form.memberPrice" 
                    :placeholder="form.feeType === 'member-free' ? '输入0表示免费' : '输入会员专享价'"
                    type="number"
                  >
                    <template #prepend>¥</template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <div class="text-xs text-blue-600 mt-2">
              💡 提示: 会员差异化定价可以提高会员购买积极性
            </div>
          </div>
        </div>

      </el-form>
      
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="submitLoading">
          {{ isEdit ? '保存修改' : (currentUserRole === 'admin' ? '创建并发布' : '提交审核') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 显示申请弹窗 -->
    <el-dialog 
      v-model="displayApplyDialogVisible" 
      title="申请首页显示" 
      width="600px"
    >
      <el-form :model="displayApplyForm" label-width="120px">
        <el-form-item label="活动标题">
          <el-input :value="currentDisplayApplyItem?.title" disabled />
        </el-form-item>
        
        <el-form-item label="显示区域" required>
          <el-select v-model="displayApplyForm.displayArea" class="w-full">
            <el-option label="首页轮播" value="home" />
            <el-option label="活动中心推荐" value="activity" />
            <el-option label="平台服务推荐" value="service" />
          </el-select>
        </el-form-item>

        <el-form-item label="显示时间" required>
          <el-date-picker
            v-model="displayApplyForm.displayTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            class="!w-full"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-alert 
          title="提示" 
          type="info" 
          :closable="false"
          class="mt-4"
        >
          <div class="text-xs">
            1. 显示申请提交后需要总部审核<br>
            2. 显示结束时间不能超过活动截止日期<br>
            3. 每个区域最多同时显示10个内容
          </div>
        </el-alert>
      </el-form>
      
      <template #footer>
        <el-button @click="displayApplyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDisplayApply" :loading="submitLoading">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 报名名单弹窗 -->
    <el-dialog v-model="signupDialogVisible" title="报名管理" width="900px">
      <div class="flex justify-between items-center mb-4 bg-blue-50 p-3 rounded-lg border border-blue-100">
         <div class="text-sm text-blue-800">
           <span class="font-bold text-lg mr-2">{{ currentSignupItem?.title }}</span>
           <el-tag size="small" type="primary">
             已报名: {{ currentSignupItem?.signups }} / {{ currentSignupItem?.maxLimit || '不限' }}
           </el-tag>
         </div>
         <el-button 
           type="success" 
           size="small" 
           plain 
           icon="Download" 
           :loading="exportLoading" 
           @click="exportSignupList"
         >
           导出 Excel 名单
         </el-button>
      </div>
      
      <el-table :data="signupList" height="400" border stripe>
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="company" label="公司名称" show-overflow-tooltip min-width="200" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="position" label="职位" width="100" />
        <el-table-column prop="time" label="报名时间" width="160" />
        <el-table-column prop="status" label="支付状态" width="100" align="center">
           <template #default="scope">
             <el-tag 
               size="small" 
               :type="scope.row.paid ? 'success' : 'danger'" 
               effect="dark"
             >
               {{ scope.row.paid ? '已支付' : '待支付' }}
             </el-tag>
           </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 详情查看弹窗 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="`${currentDetailItem?.title} - 活动详情`" 
      width="900px"
    >
      <div v-if="currentDetailItem" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动编号">{{ currentDetailItem.code }}</el-descriptions-item>
          <el-descriptions-item label="活动类型">
            <el-tag size="small" :type="currentDetailItem.subTypeTag">{{ currentDetailItem.subTypeLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布者">
            <el-tag size="small" :type="currentDetailItem.publisher === '总部' ? 'danger' : 'primary'">
              {{ currentDetailItem.publisher }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentDetailItem.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="活动标题" :span="2">{{ currentDetailItem.title }}</el-descriptions-item>
          <el-descriptions-item label="投放城市" :span="2">
            {{ currentDetailItem.cities.join(', ') }}
          </el-descriptions-item>
          <el-descriptions-item label="费用信息" :span="2">
            <div v-if="currentDetailItem.feeItemName !== '免费'">
              <span class="font-bold text-orange-600">¥{{ currentDetailItem.price }}</span>
              <el-tag size="small" type="success" class="ml-2" v-if="currentDetailItem.feeType !== 'paid'">
                {{ currentDetailItem.feeType === 'member-free' ? '会员免费' : '会员优惠' }}
              </el-tag>
            </div>
            <span v-else class="text-green-600 font-bold">免费活动</span>
          </el-descriptions-item>
        </el-descriptions>

        <div>
          <h4 class="text-sm font-bold mb-2">活动简介</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.summary || '无' }}
          </div>
        </div>

        <div v-if="currentDetailItem.agenda">
          <h4 class="text-sm font-bold mb-2">活动议程</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700 whitespace-pre-line">
            {{ currentDetailItem.agenda }}
          </div>
        </div>

        <div v-if="currentDetailItem.coverImage">
          <h4 class="text-sm font-bold mb-2">封面图片</h4>
          <img :src="currentDetailItem.coverImage" class="w-full max-w-md rounded-lg border border-slate-200" />
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { 
  Search, Plus, Calendar, Monitor, VideoPlay, Location, 
  Picture, Close, VideoCamera, User, Download, Star, Check,
  Edit, View, Warning
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps, UploadUserFile, FormInstance } from 'element-plus'
import { useGlobalConfig } from '~/composables/useGlobalConfig'
import { navigateTo } from '#app'
import { apiRequest, getLoginUser } from '@/utils/request'

definePageMeta({ layout: 'admin' })

const { cityOptions } = useGlobalConfig()

// 状态管理
const publishDialogVisible = ref(false)
const displayApplyDialogVisible = ref(false)
const signupDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)
const currentUserRole = ref<'admin' | 'partner'>(getLoginUser()?.role === 'partner' ? 'partner' : 'admin')
const apiLoading = ref(false)
const currentSignupItem = ref<any>(null)
const currentDisplayApplyItem = ref<any>(null)
const currentDetailItem = ref<any>(null)
const currentEditId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const filters = ref({ keyword: '', status: '', publisher: '' })

// 模拟后台收费项目配置
const feeOptions = [
  { id: 'FEE001', name: '活动标准票', price: 299 },
  { id: 'FEE002', name: '活动VIP票', price: 999 },
  { id: 'FEE005', name: '出海考察团费', price: 19800 }
]

const imageFileList = ref<UploadUserFile[]>([])
const videoFileList = ref<UploadUserFile[]>([])

// 表单数据结构
const form = ref({
  subType: '',
  title: '',
  cities: [] as string[],
  frontModule: '',
  frontPosition: '',
  coverImage: '',
  videoUrl: '',
  hasVideo: false,
  summary: '',
  agenda: '',
  date: '',
  maxLimit: 50,
  feeItemId: 'free',
  price: 0,
  feeItemName: '',
  feeType: 'paid',
  normalPrice: '',
  memberPrice: ''
})

const displayApplyForm = ref({
  displayArea: 'home',
  displayTime: [] as string[]
})

const formRules = {
  subType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  frontModule: [{ required: true, message: '请选择前台模块', trigger: 'change' }],
  frontPosition: [{ required: true, message: '请选择展示位置', trigger: 'change' }],
  cities: [{ required: true, message: '请选择投放城市', trigger: 'change' }],
  agenda: [{ required: true, message: '请输入活动议程', trigger: 'blur' }]
}

// 统计数据
const stats = computed(() => [
  { label: '累计发布活动', value: totalItems.value, icon: Calendar, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '本月新增', value: '—', icon: Plus, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '累计参会', value: allActivityList.value.reduce((sum, item) => sum + Number(item.signups || 0), 0), icon: User, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '待审核', value: allActivityList.value.filter(i => i.status === 'auditing').length, icon: Monitor, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
])

const fmtDate = (v: any) => {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v).slice(0, 10)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const mapAuditStatus = (raw: any) => {
  const n = Number(raw)
  if (n === 30) return { status: 'published', statusLabel: '已发布' }
  if (n === 40) return { status: 'rejected',  statusLabel: '已驳回' }
  if (n === 50) return { status: 'offline',   statusLabel: '已下架' }
  if (n === 0 || n === 10 || n === 20) return { status: 'auditing', statusLabel: '审核中' }
  return { status: 'draft', statusLabel: '草稿' }
}

const mapSubType = (raw?: string) => {
  const m: Record<string, { subType: string; subTypeLabel: string; subTypeTag: string }> = {
    hengjia:  { subType: 'hengjia',  subTypeLabel: '亨嘉之会', subTypeTag: 'success' },
    charity:  { subType: 'charity',  subTypeLabel: '公益行',   subTypeTag: 'primary' },
    inspection: { subType: 'inspection', subTypeLabel: '出海考察', subTypeTag: 'warning' },
    overseas: { subType: 'inspection', subTypeLabel: '出海考察', subTypeTag: 'warning' },
    salon:    { subType: 'salon',    subTypeLabel: '行业沙龙', subTypeTag: 'info'    },
    forum:    { subType: 'salon',    subTypeLabel: '行业沙龙', subTypeTag: 'info'    },
  }
  return m[raw || ''] ?? { subType: 'salon', subTypeLabel: '行业沙龙', subTypeTag: 'info' }
}

const mapFrontModuleLabel = (raw?: string) => {
  const m: Record<string, string> = {
    'home-banner': '首页轮播图',
    'activity-center': '活动中心',
    'resource-expansion': '资源拓展'
  }
  return m[String(raw || '')] || String(raw || '-')
}

const mapFrontPositionLabel = (module?: string, position?: string) => {
  const mm: Record<string, Record<string, string>> = {
    'home-banner': {
      'banner-main': '轮播图主位',
      'banner-sub': '轮播图副位'
    },
    'activity-center': {
      top: '顶部焦点位',
      list: '列表推荐位'
    },
    'resource-expansion': {
      sidebar: '侧边栏',
      footer: '底部栏'
    }
  }
  return mm[String(module || '')]?.[String(position || '')] || String(position || '-')
}

const toCityArray = (item: any) => {
  if (Array.isArray(item?.cities)) {
    return item.cities.map((city: any) => String(city || '').trim()).filter(Boolean)
  }
  if (typeof item?.cities_json === 'string') {
    try {
      const parsed = JSON.parse(item.cities_json)
      if (Array.isArray(parsed)) {
        return parsed.map((city: any) => String(city || '').trim()).filter(Boolean)
      }
    } catch {}
  }
  const single = String(item?.city_name ?? item?.cityName ?? '').trim()
  return single ? [single] : ['全国']
}

const mapActivityRow = (item: any = {}) => {
  const st = mapAuditStatus(item?.audit_status ?? item?.auditStatus)
  const tp = mapSubType(item?.sub_type ?? item?.subType)
  const cities = toCityArray(item)
  const frontModule = item?.front_module ?? item?.frontModule ?? ''
  const frontPosition = item?.front_position ?? item?.frontPosition ?? ''
  const rawIsFree = item?.is_free ?? item?.isFree
  const isFree = rawIsFree === true || rawIsFree === 'true' || rawIsFree === 1 || rawIsFree === '1'
  const feeType = String(item?.fee_type ?? item?.feeType ?? (isFree ? 'free' : 'paid'))
  return {
    id:             item?.id,
    ...tp,
    code:           item?.activity_no ?? item?.activityNo ?? `ACT-${item?.id ?? ''}`,
    title:          item?.title ?? '未命名活动',
    cities,
    ...st,
    publisher:      item?.partner_id ? '合伙人' : '总部',
    publishDate:    fmtDate(item?.created_at ?? item?.createdAt ?? item?.updated_at ?? item?.updatedAt),
    image:          item?.cover_image ?? item?.coverImage ?? item?.image_url ?? '',
    coverImage:     item?.cover_image ?? item?.coverImage ?? item?.image_url ?? '',
    hasVideo:       !!(item?.video_url ?? item?.videoUrl),
    videoUrl:       item?.video_url ?? item?.videoUrl ?? '',
    feeItemId:      item?.fee_item_id ?? item?.feeItemId ?? (isFree ? 'free' : 'FEE001'),
    feeItemName:    item?.fee_item_name ?? item?.feeItemName ?? (isFree ? '免费' : '活动票'),
    price:          Number(item?.fee ?? 0),
    feeType,
    memberPrice:    item?.member_price ?? item?.memberPrice ?? '',
    signups:        Number(item?.signup_count ?? item?.signupCount ?? item?.current_participants ?? item?.currentParticipants ?? 0),
    maxLimit:       Number(item?.max_limit ?? item?.maxLimit ?? 0),
    agenda:         item?.agenda ?? '',
    summary:        item?.description ?? item?.summary ?? '',
    date:           item?.start_time ?? item?.startTime ?? '',
    frontModule,
    frontModuleLabel: item?.front_module_label ?? item?.frontModuleLabel ?? mapFrontModuleLabel(frontModule),
    frontPosition,
    frontPositionLabel: item?.front_position_label ?? item?.frontPositionLabel ?? mapFrontPositionLabel(frontModule, frontPosition),
    rejectReason:   item?.change_reason ?? item?.changeReason ?? '',
    partnerId:      item?.partner_id ?? item?.partnerId ?? null,
    displayApplications: Array.isArray(item?.display_applications ?? item?.displayApplications)
      ? (item?.display_applications ?? item?.displayApplications)
      : [],
  }
}

const allActivityList = ref<any[]>([])

const resetForm = () => {
  currentEditId.value = null
  form.value = {
    subType: '',
    title: '',
    cities: [],
    frontModule: '',
    frontPosition: '',
    coverImage: '',
    videoUrl: '',
    hasVideo: false,
    summary: '',
    agenda: '',
    date: '',
    maxLimit: 50,
    feeItemId: 'free',
    price: 0,
    feeItemName: '免费',
    feeType: 'paid',
    normalPrice: '',
    memberPrice: ''
  }
  imageFileList.value = []
  videoFileList.value = []
}

const fillFormFromActivity = (item: any) => {
  currentEditId.value = Number(item?.id || 0) || null
  form.value = {
    subType: item?.subType || '',
    title: item?.title || '',
    cities: Array.isArray(item?.cities) ? item.cities : [],
    frontModule: item?.frontModule || '',
    frontPosition: item?.frontPosition || '',
    coverImage: item?.coverImage || item?.image || '',
    videoUrl: item?.videoUrl || '',
    hasVideo: item?.hasVideo || false,
    summary: item?.summary || '',
    agenda: item?.agenda || '',
    date: item?.date || '',
    maxLimit: item?.maxLimit ?? 50,
    feeItemId: item?.feeItemId || 'free',
    price: Number(item?.price || 0),
    feeItemName: item?.feeItemName || '免费',
    feeType: item?.feeType || 'paid',
    normalPrice: String(item?.price || ''),
    memberPrice: item?.memberPrice ?? item?.memberPrice ?? ''
  }
  imageFileList.value = form.value.coverImage
    ? [{ name: 'cover-image', url: form.value.coverImage } as UploadUserFile]
    : []
  videoFileList.value = form.value.videoUrl
    ? [{ name: 'promo-video', url: form.value.videoUrl } as UploadUserFile]
    : []
}

const loadActivityDetail = async (id: number | string) => {
  const res: any = await apiRequest(`/v3/partner/publishes/${id}`)
  return mapActivityRow(res?.data || {})
}

const buildActivityPayload = () => ({
  title: form.value.title.trim(),
  sub_type: form.value.subType,
  cities: form.value.cities,
  front_module: form.value.frontModule,
  front_position: form.value.frontPosition,
  cover_image: form.value.coverImage,
  video_url: form.value.videoUrl,
  summary: form.value.summary || '',
  agenda: form.value.agenda || '',
  start_time: form.value.date || null,
  max_limit: Number(form.value.maxLimit || 0),
  is_free: form.value.feeItemId === 'free',
  fee: form.value.feeItemId === 'free' ? 0 : Number(form.value.price || 0),
  fee_item_id: form.value.feeItemId,
  fee_item_name: form.value.feeItemName || (form.value.feeItemId === 'free' ? '免费' : ''),
  fee_type: form.value.feeItemId === 'free' ? 'free' : form.value.feeType,
  member_price: form.value.feeItemId === 'free'
    ? 0
    : (form.value.memberPrice === '' ? null : Number(form.value.memberPrice))
})

const loadActivities = async () => {
  apiLoading.value = true
  try {
    const query: Record<string, any> = { page: currentPage.value, page_size: pageSize.value }
    const statusMap: Record<string, number> = { published: 30, auditing: 0, rejected: 40, offline: 50 }
    if (filters.value.status && statusMap[filters.value.status] !== undefined) {
      query.audit_status = statusMap[filters.value.status]
    }
    if (filters.value.keyword) {
      query.keyword = filters.value.keyword
    }
    if (filters.value.publisher) {
      query.publisher = filters.value.publisher
    }
    const res: any = await apiRequest('/v3/partner/publishes', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allActivityList.value = items.map(mapActivityRow)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    allActivityList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取活动列表失败')
  } finally {
    apiLoading.value = false
  }
}

const signupList = ref<any[]>([])

// All filtering is now server-side (keyword, audit_status, publisher passed as API params)
const filteredActivityList = computed(() => allActivityList.value)

// 工具函数
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    'published': 'bg-green-500',
    'auditing': 'bg-orange-400',
    'rejected': 'bg-red-400',
    'offline': 'bg-slate-300'
  }
  return map[status] || 'bg-slate-300'
}

const getStatusTextColor = (status: string) => {
  const map: Record<string, string> = {
    'published': 'text-green-600',
    'auditing': 'text-orange-600',
    'rejected': 'text-red-600',
    'offline': 'text-slate-400'
  }
  return map[status] || 'text-slate-400'
}

// 上传处理
const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持 JPG/PNG 格式的图片')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleImageSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response.code === 200) {
    form.value.coverImage = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('上传失败: ' + response.message)
  }
}

const beforeVideoUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['video/mp4', 'video/quicktime']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持 MP4/MOV 格式的视频')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 50) {
    ElMessage.error('视频大小不能超过 50MB')
    return false
  }
  return true
}

const handleVideoSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response.code === 200) {
    form.value.videoUrl = response.data.url
    form.value.hasVideo = true
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error('上传失败: ' + response.message)
  }
}

const handleUploadError: UploadProps['onError'] = (error) => {
  ElMessage.error('上传失败,请重试')
  console.error('Upload error:', error)
}

const handleApprove = (row: any) => {
  ElMessageBox.confirm(`确认批准发布 "${row.title}" 吗？`, '审核通过', {
    confirmButtonText: '批准上线',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await apiRequest(`/v3/partner/publishes/${row.id}/review`, {
        method: 'POST',
        body: { action: 'approve', reason: '' }
      })
      ElMessage.success('审核通过，活动已上线')
      await loadActivities()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '审核失败')
    }
  })
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '例如：活动议程不够详细，请补充具体议题和嘉宾信息...',
    inputValidator: (value) => {
      if (!value || value.trim() === '') {
        return '请输入驳回原因'
      }
      return true
    }
  }).then(async ({ value }) => {
    try {
      await apiRequest(`/v3/partner/publishes/${row.id}/review`, {
        method: 'POST',
        body: { action: 'reject', reason: value }
      })
      ElMessage.warning('已驳回申请')
      await loadActivities()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '驳回失败')
    }
  })
}

// 核心交互逻辑
const handleSearch = () => {
  currentPage.value = 1
  loadActivities()
}

watch(() => [filters.value.status, filters.value.publisher], () => {
  currentPage.value = 1
  loadActivities()
})

const openPublishDialog = () => {
  isEdit.value = false
  resetForm()
  publishDialogVisible.value = true 
}

const handlePublish = () => {
  if (!formRef.value) return

  formRef.value.validate(async (valid) => {
    if (!valid) return ElMessage.warning('请填写必填项')

    submitLoading.value = true
    try {
      const body = buildActivityPayload()
      const res: any = isEdit.value && currentEditId.value
        ? await apiRequest(`/v3/partner/publishes/${currentEditId.value}`, { method: 'PUT', body })
        : await apiRequest('/v3/partner/publishes', { method: 'POST', body })

      const saved = mapActivityRow(res?.data || {})
      if (isEdit.value) {
        if (currentUserRole.value === 'partner' && saved.status === 'auditing') {
          ElMessage.success('修改已保存，并重新进入审核')
        } else {
          ElMessage.success('活动修改已保存')
        }
      } else if (currentUserRole.value === 'partner') {
        ElMessage.success('活动已提交审核')
      } else {
        ElMessage.success('活动已创建并发布')
      }
      publishDialogVisible.value = false
      await loadActivities()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '保存失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleEdit = async (row: any) => {
  try {
    const detail = await loadActivityDetail(row.id)
    isEdit.value = true
    fillFormFromActivity(detail)
    publishDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '读取活动详情失败')
  }
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 'published' ? '下架' : '上架'
  ElMessageBox.confirm(`确定${action}活动 "${row.title}" 吗？`, '状态切换', {
    confirmButtonText: `确认${action}`,
    cancelButtonText: '取消',
    type: row.status === 'published' ? 'warning' : 'success'
  }).then(async () => {
    try {
      const toStatus = row.status === 'published' ? 'offline' : 'published'
      await apiRequest(`/v3/partner/publishes/${row.id}/transition`, {
        method: 'POST',
        body: { to_status: toStatus, reason: `${currentUserRole.value}_${toStatus}` }
      })
      ElMessage.success(`活动已${action}`)
      await loadActivities()
    } catch (e: any) {
      ElMessage.error(e?.data?.message || e?.message || '状态切换失败')
    }
  }).catch(() => {})
}

const handleViewDetail = async (row: any) => {
  try {
    currentDetailItem.value = await loadActivityDetail(row.id)
    detailDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '读取活动详情失败')
  }
}

const handleModuleChange = () => {
  form.value.frontPosition = ''
}

const handleFeeChange = (val: any) => {
  if (val === 'free') {
    form.value.price = 0
    form.value.feeItemName = '免费'
    form.value.feeType = 'paid'
  } else {
    const item = feeOptions.find(f => f.id === val)
    if (item) {
      form.value.price = item.price
      form.value.feeItemName = item.name
      form.value.normalPrice = String(item.price)
    }
  }
}

const handleFeeTypeChange = (value: string) => {
  if (value === 'member-free') {
    form.value.memberPrice = '0'
  } else if (value === 'paid') {
    form.value.memberPrice = ''
  }
}

const openDisplayApplyDialog = (row: any) => {
  currentDisplayApplyItem.value = row
  const latest = Array.isArray(row?.displayApplications) ? row.displayApplications[0] : null
  displayApplyForm.value = {
    displayArea: latest?.displayArea || latest?.display_area || 'home',
    displayTime: latest?.displayStartAt && latest?.displayEndAt
      ? [latest.displayStartAt, latest.displayEndAt]
      : []
  }
  displayApplyDialogVisible.value = true
}

const handleSubmitDisplayApply = async () => {
  const range = displayApplyForm.value.displayTime
  if (!range || range.length !== 2) {
    return ElMessage.warning('请选择显示时间')
  }

  const [start, end] = range
  const activityStart = currentDisplayApplyItem.value?.date
  if (activityStart && end > activityStart) {
    return ElMessage.warning('显示结束时间不能超过活动开始时间')
  }

  submitLoading.value = true
  try {
    await apiRequest(`/v3/partner/publishes/${currentDisplayApplyItem.value.id}/display-apply`, {
      method: 'POST',
      body: {
        display_area: displayApplyForm.value.displayArea,
        display_time: range
      }
    })
    ElMessage.success('显示申请已提交，等待后台审核')
    displayApplyDialogVisible.value = false
    await loadActivities()
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '显示申请提交失败')
  } finally {
    submitLoading.value = false
  }
}

const viewSignups = (row: any) => { 
  currentSignupItem.value = row
  signupList.value = []
  signupDialogVisible.value = true
  apiRequest(`/v3/partner/publishes/${row.id}/registrations`).then((res: any) => {
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    signupList.value = items.map((item: any) => ({
      id: item?.id,
      name: item?.contact_name ?? item?.contactName ?? '-',
      company: item?.company_name ?? item?.companyName ?? '-',
      phone: item?.phone ?? '-',
      position: item?.position ?? '-',
      time: fmtDate(item?.created_at ?? item?.createdAt),
      paid: Boolean(item?.paid),
      statusLabel: item?.registration_status_label ?? item?.registrationStatusLabel ?? ''
    }))
  }).catch((e: any) => {
    signupList.value = []
    ElMessage.error(e?.data?.message || e?.message || '读取报名名单失败')
  })
}

const exportSignupList = async () => {
  if (!currentSignupItem.value?.id) {
    return ElMessage.warning('无有效活动ID')
  }
  exportLoading.value = true
  try {
    const { downloadExcel } = await import('@/utils/downloadExcel')
    await downloadExcel(
      `/v3/partner/publishes/${currentSignupItem.value.id}/registrations/export`,
      `${currentSignupItem.value.title || '活动'}_报名名单_${new Date().toISOString().slice(0, 10)}.xlsx`
    )
    ElMessage.success('报名名单导出成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '报名名单导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
:deep(.el-upload-dragger) {
  padding: 20px;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
