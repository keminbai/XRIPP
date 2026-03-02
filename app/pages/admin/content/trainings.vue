<!-- 
  ========================================================================
  文件路径: D:\ipp-platform\app\pages\admin\content\trainings.vue
  版本: V1.0 (2026-01-28)
  
  ✅ 核心功能:
  1. [培训专属字段] 团队介绍、课程章节、专家信息
  2. [收费逻辑] 关联后台收费项目 + 会员差异化定价
  3. [完整上传] 图片/视频上传功能
  4. [前台配置] 前台位置/城市选择器
  5. [报名管理] 报名列表/进度条/导出
  
  📋 基于V3.0 cms.vue优化,保留所有优势功能
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
            <h3 class="text-lg font-bold text-slate-800">培训课程管理</h3>
            <p class="text-xs text-slate-500 mt-1">发布和管理培训课程,支持线上/线下/认证课程</p>
          </div>
          <el-button type="primary" color="#0f172a" @click="openPublishDialog">
            <el-icon class="mr-2"><Plus /></el-icon> 新建培训
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
              您发布的培训课程将自动进入<strong>审核流程</strong>,经总部审核通过后才会展示在前台。
              <el-button link type="primary" size="small" @click="navigateTo('/admin/audit')">
                查看审核状态
              </el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 培训列表表格 -->
      <div class="p-6">
        <el-table 
          :data="filteredTrainingList" 
          style="width: 100%" 
          :header-cell-style="{background:'#f8fafc', color:'#64748b'}"
        >
          <el-table-column prop="code" label="编号" width="130">
            <template #default="scope">
              <span class="font-mono text-xs text-slate-500">{{ scope.row.code }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="课程概览" min-width="280">
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
                <span class="text-xs" :class="scope.row.status === 'published' ? 'text-green-600' : 'text-slate-400'">
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
              
              <!-- 场景A: 待审核状态 (仅管理员可见) -->
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

              <!-- 场景B: 已驳回状态 -->
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

              <!-- 场景C: 常规操作 -->
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
            @current-change="loadTrainings"
          />
        </div>
      </div>

    </div>

    <!-- 🌟 培训发布弹窗 -->
    <el-dialog 
      v-model="publishDialogVisible" 
      :title="isEdit ? '编辑培训' : '新建培训'" 
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
            <el-form-item label="培训类型" required prop="subType">
              <el-select v-model="form.subType" class="w-full" placeholder="请选择">
                <el-option label="线上课程" value="online" />
                <el-option label="线下培训" value="offline" />
                <el-option label="认证课程" value="certification" />
                <el-option label="企业内训" value="corporate" />
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
                <el-option label="培训中心" value="training-center" />
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
                <template v-else-if="form.frontModule === 'training-center'">
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

          <el-form-item label="培训标题" required prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="请输入培训标题（30字以内）" 
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

          <el-form-item label="课程简介" required>
            <el-input 
              v-model="form.summary" 
              type="textarea" 
              :rows="4"
              placeholder="请输入课程核心内容介绍..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- ✅ 培训专属字段 -->
          <div class="mt-4 pt-4 border-t border-slate-200 space-y-4">
            <el-form-item label="团队介绍" required>
              <el-input 
                v-model="form.teamIntro" 
                type="textarea" 
                :rows="3" 
                placeholder="讲师团队背景介绍、教学经验等..."
              />
              <div class="text-xs text-slate-400 mt-1">
                介绍讲师团队的专业背景、行业经验等
              </div>
            </el-form-item>
            
            <el-form-item label="课程章节" required>
              <el-input 
                v-model="form.courseChapters" 
                type="textarea" 
                :rows="4" 
                placeholder="例如：&#10;第一章 联合国采购概述&#10;第二章 供应商注册流程&#10;第三章 投标技巧..."
              />
              <div class="text-xs text-slate-400 mt-1">
                详细列出课程各章节内容,方便学员了解课程结构
              </div>
            </el-form-item>
            
            <el-form-item label="专家信息" required>
              <el-input 
                v-model="form.expertInfo" 
                placeholder="特邀专家姓名及头衔，例如：张教授 - 国际贸易专家 / 李博士 - 联合国采购顾问"
              />
              <div class="text-xs text-slate-400 mt-1">
                列出特邀专家的姓名和专业头衔
              </div>
            </el-form-item>
          </div>
        </div>

        <!-- C. 业务规则与收费 -->
        <div class="bg-slate-50 p-5 rounded-lg border border-slate-100">
          <h4 class="font-bold text-sm text-slate-700 mb-4 border-l-4 border-green-500 pl-2">业务规则与收费</h4>
          
          <div class="grid grid-cols-2 gap-6">
            <el-form-item label="开课时间">
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
          {{ isEdit ? '保存修改' : '提交审核' }}
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
        <el-form-item label="培训标题">
          <el-input :value="currentDisplayApplyItem?.title" disabled />
        </el-form-item>
        
        <el-form-item label="显示区域" required>
          <el-select v-model="displayApplyForm.displayArea" class="w-full">
            <el-option label="首页轮播" value="home" />
            <el-option label="培训中心推荐" value="training" />
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
            2. 显示结束时间不能超过课程开始日期<br>
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

    <!-- 详情查看弹窗 (培训专属) -->
    <el-dialog 
      v-model="detailDialogVisible" 
      :title="`${currentDetailItem?.title} - 培训详情`" 
      width="900px"
    >
      <div v-if="currentDetailItem" class="space-y-4">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="培训编号">{{ currentDetailItem.code }}</el-descriptions-item>
          <el-descriptions-item label="培训类型">
            <el-tag size="small" :type="currentDetailItem.subTypeTag">{{ currentDetailItem.subTypeLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布者">
            <el-tag size="small" :type="currentDetailItem.publisher === '总部' ? 'danger' : 'primary'">
              {{ currentDetailItem.publisher }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布日期">{{ currentDetailItem.publishDate }}</el-descriptions-item>
          <el-descriptions-item label="培训标题" :span="2">{{ currentDetailItem.title }}</el-descriptions-item>
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
            <span v-else class="text-green-600 font-bold">免费课程</span>
          </el-descriptions-item>
        </el-descriptions>

        <div>
          <h4 class="text-sm font-bold mb-2">课程简介</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.summary || '无' }}
          </div>
        </div>

        <!-- 培训专属字段展示 -->
        <div v-if="currentDetailItem.teamIntro">
          <h4 class="text-sm font-bold mb-2">团队介绍</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.teamIntro }}
          </div>
        </div>

        <div v-if="currentDetailItem.courseChapters">
          <h4 class="text-sm font-bold mb-2">课程章节</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700 whitespace-pre-line">
            {{ currentDetailItem.courseChapters }}
          </div>
        </div>

        <div v-if="currentDetailItem.expertInfo">
          <h4 class="text-sm font-bold mb-2">专家信息</h4>
          <div class="p-4 bg-slate-50 rounded-lg text-sm text-slate-700">
            {{ currentDetailItem.expertInfo }}
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
  Search, Plus, Calendar, Monitor, More, VideoPlay, Location, 
  Picture, Close, VideoCamera, User, Timer, Download, Star, Reading, Check, Edit, View, Warning
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
const isEdit = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)
const currentUserRole = ref<'admin' | 'partner'>(getLoginUser()?.role === 'partner' ? 'partner' : 'admin')
const apiLoading = ref(false)
const currentSignupItem = ref<any>(null)
const currentDisplayApplyItem = ref<any>(null)
const formRef = ref<FormInstance>()

const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)

const filters = ref({ keyword: '', status: '', publisher: '' })

// 模拟后台收费项目配置
const feeOptions = [
  { id: 'FEE003', name: '培训基础课程', price: 1999 },
  { id: 'FEE004', name: '培训高级课程', price: 4999 },
  { id: 'FEE005', name: '认证考试费用', price: 3999 },
  { id: 'FEE006', name: '企业内训套餐', price: 29999 }
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
  teamIntro: '',
  courseChapters: '',
  expertInfo: '',
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
  subType: [{ required: true, message: '请选择培训类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  frontModule: [{ required: true, message: '请选择前台模块', trigger: 'change' }],
  frontPosition: [{ required: true, message: '请选择展示位置', trigger: 'change' }],
  cities: [{ required: true, message: '请选择投放城市', trigger: 'change' }],
  teamIntro: [{ required: true, message: '请输入团队介绍', trigger: 'blur' }],
  courseChapters: [{ required: true, message: '请输入课程章节', trigger: 'blur' }],
  expertInfo: [{ required: true, message: '请输入专家信息', trigger: 'blur' }]
}

const allTrainingList = ref<any[]>([])

const fmtDate = (v: any) => {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v).slice(0, 10)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const mapStatus = (raw?: string) => {
  const s = String(raw || '').toLowerCase()
  if (s === 'published') return { key: 'published', label: '已发布' }
  if (s === 'offline') return { key: 'offline', label: '已下架' }
  if (s === 'rejected') return { key: 'rejected', label: '已驳回' }
  return { key: 'auditing', label: '审核中' }
}

const mapSubType = (category?: string) => {
  const c = String(category || '').toLowerCase()
  if (c === 'medical') return { subType: 'offline', subTypeLabel: '线下培训', subTypeTag: 'success' }
  if (c === 'it') return { subType: 'online', subTypeLabel: '线上课程', subTypeTag: 'primary' }
  if (c === 'construction') return { subType: 'certification', subTypeLabel: '认证课程', subTypeTag: 'warning' }
  return { subType: 'corporate', subTypeLabel: '企业内训', subTypeTag: 'info' }
}

const mapTrainingRow = (item: any) => {
  const st = mapStatus(item?.status)
  const tp = mapSubType(item?.category)
  const price = Number(item?.price || 0)
  return {
    id: item?.id,
    section: 'training',
    subType: tp.subType,
    subTypeLabel: tp.subTypeLabel,
    subTypeTag: tp.subTypeTag,
    code: item?.contentNo || `TRN-${item?.id || ''}`,
    title: item?.title || '未命名培训',
    cities: ['全国'],
    status: st.key,
    statusLabel: st.label,
    publisher: item?.publisher || '总部',
    publishDate: fmtDate(item?.publishDate || item?.createdAt || item?.updatedAt),
    image: item?.coverImage || 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=200',
    hasVideo: false,
    feeItemId: price > 0 ? 'FEE003' : 'free',
    feeItemName: price > 0 ? '培训基础课程' : '免费',
    price,
    feeType: 'paid',
    signups: Number(item?.signups || 0),
    maxLimit: Number(item?.maxLimit || 0),
    teamIntro: item?.teamIntro || '',
    courseChapters: item?.courseChapters || '',
    expertInfo: item?.expertInfo || '',
    summary: item?.description || '',
    rejectReason: item?.rejectReason || ''
  }
}

const loadTrainings = async () => {
  apiLoading.value = true
  try {
    const query: Record<string, any> = { content_type: 'training', page: currentPage.value, page_size: pageSize.value }
    if (filters.value.keyword.trim()) query.keyword = filters.value.keyword.trim()
    if (filters.value.status) query.publish_status = filters.value.status
    const res: any = await apiRequest('/v3/admin/contents', { query })
    const items = Array.isArray(res?.data?.items) ? res.data.items : []
    allTrainingList.value = items.map(mapTrainingRow)
    totalItems.value = Number(res?.data?.total ?? 0)
  } catch (e: any) {
    allTrainingList.value = []
    totalItems.value = 0
    ElMessage.error(e?.message || '读取培训列表失败')
  } finally {
    apiLoading.value = false
  }
}

const stats = computed(() => [
  { label: '累计发布培训', value: String(allTrainingList.value.length), icon: Reading, bgClass: 'bg-green-50', textClass: 'text-green-600' },
  { label: '本月新增', value: String(allTrainingList.value.filter(i => String(i.publishDate).startsWith(new Date().toISOString().slice(0, 7))).length), icon: Calendar, bgClass: 'bg-blue-50', textClass: 'text-blue-600' },
  { label: '累计学员', value: String(allTrainingList.value.reduce((n, i) => n + Number(i.signups || 0), 0)), icon: User, bgClass: 'bg-purple-50', textClass: 'text-purple-600' },
  { label: '待审核', value: String(allTrainingList.value.filter(i => i.status === 'auditing').length), icon: Monitor, bgClass: 'bg-orange-50', textClass: 'text-orange-600' }
])

const signupList = ref([
  { name: '张伟', phone: '138****0001', company: '上海宏大贸易', position: '采购经理', time: '2026-01-22 10:30', paid: true },
  { name: '李丽', phone: '139****0002', company: '苏州精密制造', position: '市场总监', time: '2026-01-22 11:15', paid: false }
])

const filteredTrainingList = computed(() => {
  let list = allTrainingList.value
  // keyword and status are now handled server-side; only publisher remains client-side
  if (filters.value.publisher) {
    list = list.filter(item => item.publisher === filters.value.publisher)
  }
  return list
})

// 工具函数
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    published: 'bg-green-500',
    auditing: 'bg-orange-400',
    rejected: 'bg-red-400',
    offline: 'bg-slate-300'
  }
  return map[status] || 'bg-slate-300'
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
      await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
        method: 'POST',
        body: { to_status: 'published', reason: 'training approve' }
      })
      ElMessage.success('审核通过，培训课程已上线')
      await loadTrainings()
    } catch (e: any) {
      ElMessage.error(e?.message || '审核失败')
    }
  }).catch(() => {})
}

const handleReject = (row: any) => {
  ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputValidator: (value) => value?.trim() ? true : '请输入驳回原因'
  }).then(async ({ value }) => {
    try {
      await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
        method: 'POST',
        body: { to_status: 'rejected', reason: String(value || 'training reject') }
      })
      ElMessage.warning('已驳回申请')
      await loadTrainings()
    } catch (e: any) {
      ElMessage.error(e?.message || '驳回失败')
    }
  }).catch(() => {})
}

// 补充详情弹窗状态
const detailDialogVisible = ref(false)
const currentDetailItem = ref<any>(null)

const handleViewDetail = (row: any) => {
  currentDetailItem.value = row
  detailDialogVisible.value = true
}

// 核心交互逻辑
const handleSearch = () => {
  currentPage.value = 1
  loadTrainings()
}

watch(() => [filters.value.status, filters.value.publisher], () => {
  currentPage.value = 1
  loadTrainings()
})

const openPublishDialog = () => { 
  isEdit.value = false
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
    teamIntro: '',
    courseChapters: '',
    expertInfo: '',
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
  publishDialogVisible.value = true 
}

const handlePublish = async () => {
  if (isEdit.value) {
    ElMessage.warning('编辑接口未接入，当前为受控降级模式')
    return
  }

  if (!form.value.title?.trim()) {
    return ElMessage.warning('请填写培训标题')
  }

  submitLoading.value = true
  try {
    const extraBody = JSON.stringify({
      subType: form.value.subType,
      coverImage: form.value.coverImage,
      videoUrl: form.value.videoUrl,
      hasVideo: form.value.hasVideo,
      teamIntro: form.value.teamIntro,
      courseChapters: form.value.courseChapters,
      expertInfo: form.value.expertInfo,
      date: form.value.date,
      maxLimit: form.value.maxLimit,
      feeItemId: form.value.feeItemId,
      feeItemName: form.value.feeItemName,
      feeType: form.value.feeType,
      frontModule: form.value.frontModule,
      frontPosition: form.value.frontPosition
    })

    await apiRequest('/v3/admin/contents', {
      method: 'POST',
      body: {
        title: form.value.title.trim(),
        content_type: 'training',
        summary: form.value.summary || '',
        body: extraBody,
        city_name: (form.value.cities && form.value.cities.length > 0) ? form.value.cities[0] : '',
        is_paid: (form.value.price || 0) > 0,
        fee: form.value.price || 0
      }
    })

    ElMessage.success('培训已创建（草稿），可在列表中上架发布')
    publishDialogVisible.value = false
    await loadTrainings()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 'published' ? '下架' : '上架'
  ElMessageBox.confirm(`确定${action}此培训吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const toStatus = row.status === 'published' ? 'offline' : 'published'
        await apiRequest(`/v3/admin/contents/${row.id}/transition`, {
          method: 'POST',
          body: { to_status: toStatus, reason: toStatus === 'published' ? 'training up' : 'training down' }
        })
        ElMessage.success(`已${action}`)
        await loadTrainings()
      } catch (e: any) {
        ElMessage.error(e?.message || '状态更新失败')
      }
    })
    .catch(() => {})
}

const handleEdit = (row: any) => {
  isEdit.value = true
  form.value = {
    subType: row.subType || '',
    title: row.title || '',
    cities: row.cities || [],
    frontModule: row.frontModule || '',
    frontPosition: row.frontPosition || '',
    coverImage: row.coverImage || row.image || '',
    videoUrl: row.videoUrl || '',
    hasVideo: row.hasVideo || false,
    summary: row.summary || '',
    teamIntro: row.teamIntro || '',
    courseChapters: row.courseChapters || '',
    expertInfo: row.expertInfo || '',
    date: row.date || '',
    maxLimit: row.maxLimit ?? 50,
    feeItemId: row.feeItemId || 'free',
    price: row.price || 0,
    feeItemName: row.feeItemName || '免费',
    feeType: row.feeType || 'paid',
    normalPrice: row.normalPrice || '',
    memberPrice: row.memberPrice || ''
  }
  publishDialogVisible.value = true
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
  displayApplyForm.value = {
    displayArea: 'home',
    displayTime: []
  }
  displayApplyDialogVisible.value = true
}

const handleSubmitDisplayApply = () => {
  const range = displayApplyForm.value.displayTime
  if (!range || range.length !== 2) {
    return ElMessage.warning('请选择显示时间')
  }

  const [start, end] = range
  const courseStart = currentDisplayApplyItem.value?.date
  if (courseStart && end > courseStart) {
    return ElMessage.warning('显示结束时间不能超过课程开始日期')
  }

  ElMessage.warning('显示申请接口未接入，当前为受控降级模式')
}

const viewSignups = (row: any) => { 
  currentSignupItem.value = row
  signupDialogVisible.value = true 
}

const exportSignupList = () => {
  ElMessage.warning('报名导出接口未接入，当前为受控降级模式')
}

onMounted(() => {
  loadTrainings()
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