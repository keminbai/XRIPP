<!-- 文件路径: app/pages/member/supplier-apply.vue -->
<template>
  <div class="space-y-6">

    <!-- 顶部 Hero -->
    <div class="relative bg-gradient-to-r from-blue-900 to-slate-900 rounded-2xl p-8 overflow-hidden text-white shadow-lg">
      <div class="absolute right-0 top-0 h-full w-1/3 bg-blue-500/10 skew-x-12"></div>
      <div class="absolute right-20 top-1/2 -translate-y-1/2 w-40 h-40 bg-cyan-400/20 rounded-full blur-3xl"></div>
      <div class="relative z-10 flex flex-col md:flex-row items-center justify-between gap-6">
        <div>
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/30 border border-blue-400/30 text-blue-200 text-xs font-bold mb-4">
            <el-icon><Trophy /></el-icon> 核心业务认证
          </div>
          <h1 class="text-3xl font-bold mb-2">成为平台业务服务商</h1>
          <p class="text-blue-100 text-sm max-w-xl leading-relaxed">
            加入XRIPP全球服务商网络，获得项目推荐与品牌曝光。
            <br><span class="text-yellow-400 font-bold">普通服务商</span>（名录展示）/
            <span class="text-yellow-400 font-bold">战略服务商</span>（首页推广+商机推送）
          </p>
        </div>
        <div class="flex-shrink-0 bg-white/10 backdrop-blur-md p-4 rounded-xl border border-white/10">
          <el-icon class="text-4xl text-blue-300"><OfficeBuilding /></el-icon>
        </div>
      </div>
    </div>

    <!-- 多步骤表单容器 -->
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">

      <!-- 步骤条 -->
      <div class="bg-gradient-to-r from-slate-50 to-white px-8 py-8 border-b border-slate-100">
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="入驻方案" :icon="Shop" description="选择套餐" />
          <el-step title="企业画像" :icon="Postcard" description="完善详情" />
          <el-step title="资质上传" :icon="UploadFilled" description="提交资质" />
          <el-step title="确认支付" :icon="Wallet" description="激活权益" />
        </el-steps>
      </div>

      <!-- 表单区域 -->
      <div class="p-8 md:p-12">

        <!-- ══════════ 步骤1：入驻方案 ══════════ -->
        <div v-show="currentStep === 0" class="max-w-3xl mx-auto space-y-8 animate-fade-in">

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <el-form-item label="服务城市 (City)" required class="mb-0">
              <el-select v-model="formData.city" placeholder="请选择服务城市" class="w-full" size="large">
                <el-option v-for="city in cityOptions" :key="city" :label="city" :value="city" />
              </el-select>
            </el-form-item>
            <el-form-item label="主营业务 (Main Service)" required class="mb-0">
              <el-select v-model="formData.mainService" placeholder="请选择主营业务" class="w-full" size="large">
                <el-option v-for="item in industryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </div>

          <!-- 方案选择卡片 -->
          <div>
            <div class="text-sm font-bold text-slate-700 mb-4">入库类型 (Membership Type) <span class="text-red-500">*</span></div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">

              <!-- 普通服务商 -->
              <div
                class="p-6 border-2 rounded-2xl cursor-pointer transition-all hover:shadow-lg"
                :class="formData.type === 'normal' ? 'border-blue-500 bg-blue-50 shadow-md' : 'border-slate-200 hover:border-blue-300'"
                @click="formData.type = 'normal'"
              >
                <div class="flex items-start justify-between mb-4">
                  <div class="flex-1">
                    <div class="font-bold text-slate-900 text-xl mb-1">{{ supplierPricing.normal.label }}</div>
                    <div class="text-xs text-slate-500 leading-relaxed">{{ supplierPricing.normal.description }}</div>
                  </div>
                  <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 ml-3 transition-all mt-0.5"
                    :class="formData.type === 'normal' ? 'border-blue-500 bg-blue-500' : 'border-slate-300'">
                    <div v-if="formData.type === 'normal'" class="w-2 h-2 rounded-full bg-white"></div>
                  </div>
                </div>
                <div class="text-3xl font-bold text-slate-700 mb-3">
                  ¥{{ supplierPricing.normal.price.toLocaleString() }}
                  <span class="text-sm font-normal text-slate-400">/ {{ supplierPricing.normal.period }}</span>
                </div>
                <div class="space-y-1.5">
                  <div v-for="f in supplierPricing.normal.features" :key="f" class="flex items-center gap-2 text-sm text-slate-600">
                    <span class="w-4 h-4 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs">✓</span>
                    {{ f }}
                  </div>
                </div>
              </div>

              <!-- 战略服务商 -->
              <div
                class="p-6 border-2 rounded-2xl cursor-pointer transition-all hover:shadow-lg relative"
                :class="formData.type === 'strategic' ? 'border-amber-500 bg-amber-50 shadow-md' : 'border-slate-200 hover:border-amber-300'"
                @click="formData.type = 'strategic'"
              >
                <div class="absolute -top-3 left-6">
                  <span class="bg-gradient-to-r from-amber-500 to-orange-500 text-white text-xs font-bold px-3 py-1 rounded-full shadow-md">⭐ 推荐</span>
                </div>
                <div class="flex items-start justify-between mb-4 mt-1">
                  <div class="flex-1">
                    <div class="font-bold text-slate-900 text-xl mb-1">{{ supplierPricing.strategic.label }}</div>
                    <div class="text-xs text-slate-500 leading-relaxed">{{ supplierPricing.strategic.description }}</div>
                  </div>
                  <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 ml-3 transition-all mt-0.5"
                    :class="formData.type === 'strategic' ? 'border-amber-500 bg-amber-500' : 'border-slate-300'">
                    <div v-if="formData.type === 'strategic'" class="w-2 h-2 rounded-full bg-white"></div>
                  </div>
                </div>
                <div class="text-3xl font-bold text-amber-600 mb-3">
                  ¥{{ supplierPricing.strategic.price.toLocaleString() }}
                  <span class="text-sm font-normal text-amber-400">/ {{ supplierPricing.strategic.period }}</span>
                </div>
                <div class="space-y-1.5">
                  <div v-for="f in supplierPricing.strategic.features" :key="f" class="flex items-center gap-2 text-sm text-slate-600">
                    <span class="w-4 h-4 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center text-xs">✓</span>
                    {{ f }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ══════════ 步骤2：企业画像 ══════════ -->
        <div v-show="currentStep === 1" class="max-w-4xl mx-auto space-y-6 animate-fade-in">

          <!-- A. 企业基本信息（必填） -->
          <div class="bg-slate-50 p-6 rounded-xl border border-slate-200">
            <h4 class="font-bold text-slate-800 mb-5 pb-2 border-b border-slate-200 flex items-center gap-2">
              <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">A</span>
              企业基本信息（必填）
            </h4>

            <el-form :model="formData.detail" label-width="140px" size="default">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8">
                <el-form-item label="企业名称（中文）" required>
                  <el-input v-model="formData.detail.companyNameZh" placeholder="请输入营业执照全称" />
                </el-form-item>

                <el-form-item label="企业名称（英文）">
                  <el-input v-model="formData.detail.companyNameEn" placeholder="Company Name in English (Optional)" />
                </el-form-item>

                <el-form-item label="统一社会信用代码" required>
                  <el-input v-model="formData.detail.creditCode" maxlength="18" placeholder="18位统一社会信用代码" />
                </el-form-item>

                <el-form-item label="企业类型" required>
                  <el-select v-model="formData.detail.nature" placeholder="请选择企业类型" class="w-full">
                    <el-option label="国营" value="state" />
                    <el-option label="民营" value="private" />
                    <el-option label="外资" value="foreign" />
                    <el-option label="合资" value="joint" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>

                <el-form-item label="注册资本（万元）" required>
                  <el-input v-model="formData.detail.regCapital" placeholder="请输入注册资本">
                    <template #append>万元</template>
                  </el-input>
                </el-form-item>

                <el-form-item label="成立日期" required>
                  <el-date-picker
                    v-model="formData.detail.foundDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="选择成立日期"
                    class="w-full"
                  />
                </el-form-item>
              </div>

              <el-form-item label="注册地址" required>
                <el-input v-model="formData.detail.address" placeholder="请输入注册地址" />
              </el-form-item>
            </el-form>
          </div>

          <!-- B. 联系人信息（必填） -->
          <div class="bg-slate-50 p-6 rounded-xl border border-slate-200">
            <h4 class="font-bold text-slate-800 mb-5 pb-2 border-b border-slate-200 flex items-center gap-2">
              <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">B</span>
              联系人信息（必填）
            </h4>

            <el-form :model="formData.detail" label-width="140px" size="default">
              <div class="mb-4">
                <div class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">总经理</div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8">
                  <el-form-item label="姓名" required>
                    <el-input v-model="formData.detail.gmName" placeholder="总经理姓名" />
                  </el-form-item>
                  <el-form-item label="手机号" required>
                    <el-input v-model="formData.detail.gmPhone" placeholder="138****1234" />
                  </el-form-item>
                </div>
              </div>

              <div class="pt-4 border-t border-slate-200">
                <div class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">业务对接人</div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-x-6">
                  <el-form-item label="姓名" required>
                    <el-input v-model="formData.detail.contactName" placeholder="联系人姓名" />
                  </el-form-item>
                  <el-form-item label="职务">
                    <el-input v-model="formData.detail.contactTitle" placeholder="如：采购经理" />
                  </el-form-item>
                  <el-form-item label="手机" required>
                    <el-input v-model="formData.detail.contactPhone" placeholder="138****1234" />
                  </el-form-item>
                </div>
              </div>
            </el-form>
          </div>

          <!-- C. 企业资质信息（必填） -->
          <div class="bg-slate-50 p-6 rounded-xl border border-slate-200">
            <h4 class="font-bold text-slate-800 mb-5 pb-2 border-b border-slate-200 flex items-center gap-2">
              <span class="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-xs font-bold">C</span>
              企业资质信息（必填）
            </h4>

            <el-form :model="formData.detail" label-width="220px" size="default">
              <el-form-item label="营业执照（已上传）">
                <el-switch v-model="formData.detail.businessLicenseUploaded" />
              </el-form-item>

              <el-form-item label="开户银行许可证（已上传）">
                <el-switch v-model="formData.detail.bankLicenseUploaded" />
              </el-form-item>

              <el-form-item label="ISO 9001 质量认证">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso9001Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso9001Has" v-model="formData.detail.iso9001No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="ISO 14001 环境管理体系">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso14001Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso14001Has" v-model="formData.detail.iso14001No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="ISO 45001 职业健康安全">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso45001Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso45001Has" v-model="formData.detail.iso45001No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="医疗器械：ISO 13485">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso13485Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso13485Has" v-model="formData.detail.iso13485No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="汽车行业：IATF 16949">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iatf16949Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iatf16949Has" v-model="formData.detail.iatf16949No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="食品行业：ISO 22000">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso22000Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso22000Has" v-model="formData.detail.iso22000No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="信息安全：ISO 27001">
                <div class="w-full flex gap-3 items-center">
                  <el-radio-group v-model="formData.detail.iso27001Has">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.iso27001Has" v-model="formData.detail.iso27001No" placeholder="证书号" class="max-w-xs" />
                </div>
              </el-form-item>

              <el-form-item label="其他资质">
                <div class="w-full grid grid-cols-1 md:grid-cols-3 gap-3">
                  <el-input v-model="formData.detail.otherCertName" placeholder="资质名称" />
                  <el-radio-group v-model="formData.detail.otherCertHas">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <el-input v-if="formData.detail.otherCertHas" v-model="formData.detail.otherCertNo" placeholder="证书号" />
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- ══════════ 步骤3：资质上传 ══════════ -->
        <div v-show="currentStep === 2" class="max-w-3xl mx-auto space-y-6 animate-fade-in">

          <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-start gap-3">
            <el-icon class="text-yellow-600 text-xl mt-0.5"><Warning /></el-icon>
            <div class="text-sm text-yellow-800">
              <p class="font-bold mb-1">上传要求：</p>
              <p class="text-xs">宣传封面：800×600px，JPG/PNG，不超过2MB；公司介绍：<strong>PDF格式</strong>，不超过10MB；宣传图：PNG/JPG，不超过5MB/张</p>
            </div>
          </div>

          <!-- ISO认证勾选 -->
          <div class="bg-white p-6 rounded-xl border border-slate-200">
            <div class="text-sm font-bold text-slate-700 mb-4">ISO 认证体系（可多选）</div>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
              <el-checkbox
                v-for="iso in isoOptions"
                :key="iso.value"
                v-model="formData.isoList"
                :label="iso.value"
                class="!mr-0"
              >
                <span class="text-sm">{{ iso.label }}</span>
              </el-checkbox>
            </div>
          </div>

          <!-- 主营行业 -->
          <div class="bg-white p-6 rounded-xl border border-slate-200">
            <div class="text-sm font-bold text-slate-700 mb-4">主营行业分类（可多选）</div>
            <el-checkbox-group v-model="formData.industryList">
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <el-checkbox
                  v-for="ind in mainIndustryOptions"
                  :key="ind.value"
                  :label="ind.value"
                  class="!mr-0"
                >
                  <span class="text-sm">{{ ind.label }}</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <el-input
              v-if="formData.industryList.includes('OTHER')"
              v-model="formData.otherIndustry"
              class="mt-3"
              maxlength="50"
              show-word-limit
              placeholder="请填写其他行业"
            />            
          </div>

          <!-- 主营产品描述 -->
          <div class="bg-white p-6 rounded-xl border border-slate-200">
            <div class="text-sm font-bold text-slate-700 mb-3">主营产品/服务描述</div>
            <el-input
              v-model="formData.productDesc"
              type="textarea"
              :rows="4"
              :maxlength="200"
              show-word-limit
              placeholder="请简要描述企业主营产品或服务，200字以内..."
            />
          </div>

          <!-- 文件上传区 -->
          <div class="space-y-5">
            <!-- 宣传封面 -->
            <div class="bg-white p-6 rounded-xl border border-slate-200">
              <label class="block text-sm font-bold text-slate-700 mb-3">
                1. 宣传封面 <span class="text-red-500">*</span>
                <span class="text-xs text-slate-400 font-normal ml-2">（800×600px，JPG/PNG）</span>
              </label>
              <el-upload
                drag :limit="1" accept="image/*" :auto-upload="false"
                :on-change="handleCoverChange" :file-list="coverFileList"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Picture /></el-icon>
                <div class="el-upload__text text-sm">拖拽或点击上传封面图</div>
              </el-upload>
            </div>

            <!-- 公司介绍PDF -->
            <div class="bg-white p-6 rounded-xl border border-slate-200">
              <label class="block text-sm font-bold text-slate-700 mb-3">
                2. 公司介绍 PDF <span class="text-red-500">*</span>
                <span class="text-xs text-slate-400 font-normal ml-2">（仅限PDF格式）</span>
              </label>
              <el-upload
                drag :limit="1" accept="application/pdf" :auto-upload="false"
                :on-change="handlePdfChange" :file-list="pdfFileList"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Document /></el-icon>
                <div class="el-upload__text text-sm">只能上传 PDF 文件</div>
              </el-upload>
            </div>

            <!-- 宣传图（可选） -->
            <div class="bg-white p-6 rounded-xl border border-slate-200">
              <label class="block text-sm font-bold text-slate-700 mb-3">
                3. 企业宣传图 <span class="text-red-500">*</span>
                <span class="text-xs text-slate-400 font-normal ml-2">（仅1张，JPG/PNG，不超过5MB）</span>
              </label>
              <el-upload
                drag
                :limit="1"
                accept="image/*"
                :auto-upload="false"
                :on-change="handlePromoImgChange"
                :file-list="promoImgList"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Picture /></el-icon>
                <div class="el-upload__text text-sm">拖拽或点击上传企业宣传图</div>
              </el-upload>
            </div>
          </div>
        </div>

        <!-- ══════════ 步骤4：确认支付 ══════════ -->
        <div v-show="currentStep === 3" class="max-w-2xl mx-auto animate-fade-in">
          <div class="bg-slate-50 rounded-2xl p-8 border border-slate-200">
            <div class="text-center mb-6">
              <div class="text-sm text-slate-500 mb-1">应付总额</div>
              <div class="text-4xl font-bold" :class="formData.type === 'strategic' ? 'text-amber-600' : 'text-blue-600'">
                ¥{{ formData.type === 'strategic'
                  ? supplierPricing.strategic.price.toLocaleString()
                  : supplierPricing.normal.price.toLocaleString() }}
              </div>
              <div class="text-xs text-slate-400 mt-1">
                {{ formData.type === 'strategic' ? supplierPricing.strategic.period : supplierPricing.normal.period }}
              </div>
            </div>

            <!-- 订单信息 -->
            <div class="bg-white rounded-xl p-5 border border-slate-100 mb-6 space-y-3 text-sm">
              <div class="flex justify-between">
                <span class="text-slate-500">入驻类型</span>
                <span class="font-bold">{{ formData.type === 'strategic' ? supplierPricing.strategic.label : supplierPricing.normal.label }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">服务城市</span>
                <span>{{ formData.city || '未选择' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">主营业务</span>
                <span>{{ industryOptions.find(i => i.value === formData.mainService)?.label || '未选择' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-500">业务对接人</span>
                <span>{{ formData.detail.contactName || '未填写' }}</span>
              </div>
              <div v-if="formData.isoList.length" class="flex justify-between">
                <span class="text-slate-500">ISO认证</span>
                <span>{{ formData.isoList.join('、') }}</span>
              </div>
            </div>

            <!-- 支付二维码 -->
            <div class="flex justify-center">
              <div class="w-40 h-40 bg-white border-2 border-slate-200 p-3 rounded-xl shadow-sm">
                <div class="w-full h-full bg-slate-800 flex items-center justify-center text-white text-xs font-mono rounded">
                  微信支付二维码
                </div>
              </div>
            </div>
            <p class="text-xs text-slate-400 mt-4 text-center">请使用微信扫码支付，支付成功后点击下方按钮提交审核</p>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-between items-center mt-10 pt-8 border-t border-slate-200">
          <el-button v-if="currentStep > 0" size="large" @click="currentStep--">
            <el-icon class="mr-1"><ArrowLeft /></el-icon> 上一步
          </el-button>
          <div v-else></div>

          <el-button v-if="currentStep < 3" type="primary" size="large" @click="handleNext" class="px-8">
            下一步 <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>

          <el-button v-else type="success" size="large" @click="handleFinish" class="px-12" :loading="submitting">
            <el-icon class="mr-1"><Check /></el-icon> 已完成支付，提交审核
          </el-button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Shop, Document, Wallet, Picture, Warning,
  ArrowRight, ArrowLeft, Check, Postcard, UploadFilled,
  Trophy, OfficeBuilding
} from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useGlobalConfig } from '~/composables/useGlobalConfig'

const { supplierPricing, cityOptions, industryOptions } = useGlobalConfig()

definePageMeta({ layout: 'member' })

const currentStep = ref(0)
const submitting = ref(false)
const coverFileList = ref([])
const pdfFileList = ref([])
const promoImgList = ref([])

// ISO选项
const isoOptions = [
  { label: 'ISO 9001', value: 'ISO9001' },
  { label: 'ISO 14001', value: 'ISO14001' },
  { label: 'ISO 45001', value: 'ISO45001' },
  { label: 'ISO 13485', value: 'ISO13485' },
  { label: 'ISO 16949', value: 'ISO16949' },
  { label: 'ISO 22000', value: 'ISO22000' },
  { label: 'ISO 27001', value: 'ISO27001' },
]

// 主营行业选项
const mainIndustryOptions = [
  { label: '信息技术 (IT)', value: 'IT' },
  { label: '医疗设备 (MD)', value: 'MD' },
  { label: '化工产品 (CN)', value: 'CN' },
  { label: '物流运输 (LG)', value: 'LG' },
  { label: '建筑工程 (CE)', value: 'CE' },
  { label: '农业食品 (AG)', value: 'AG' },
  { label: '环保能源 (EV)', value: 'EV' },
  { label: '其他', value: 'OTHER' }
]

const formData = ref({
  city: '',
  mainService: '',
  type: 'normal',
  cover: null as any,
  pdf: null as any,
  promoImgs: [] as any[],
  isoList: [] as string[],
  industryList: [] as string[],
  productDesc: '',
  otherIndustry: '',
  detail: {
    companyNameZh: '',
    companyNameEn: '',
    creditCode: '',
    nature: '',
    regCapital: '',
    foundDate: '',
    address: '',
    gmName: '',
    gmPhone: '',
    contactName: '',
    contactTitle: '',
    contactPhone: '',
    contactEmail: '',

    businessLicenseUploaded: false,
    bankLicenseUploaded: false,

    iso9001Has: null as boolean | null,
    iso9001No: '',
    iso14001Has: null as boolean | null,
    iso14001No: '',
    iso45001Has: null as boolean | null,
    iso45001No: '',
    iso13485Has: null as boolean | null,
    iso13485No: '',
    iatf16949Has: null as boolean | null,
    iatf16949No: '',
    iso22000Has: null as boolean | null,
    iso22000No: '',
    iso27001Has: null as boolean | null,
    iso27001No: '',
    otherCertName: '',
    otherCertHas: null as boolean | null,
    otherCertNo: ''
  }
})

const handleCoverChange = (file: any) => {
  formData.value.cover = file.raw
  coverFileList.value = [file]
}

const handlePdfChange = (file: any) => {
  formData.value.pdf = file.raw
  pdfFileList.value = [file]
}

const handlePromoImgChange = (_file: any, fileList: any[]) => {
  promoImgList.value = fileList
  formData.value.promoImgs = fileList.map(f => f.raw)
}

const handleNext = () => {
  if (currentStep.value === 0) {
    if (!formData.value.city) return ElMessage.warning('请选择服务城市')
    if (!formData.value.mainService) return ElMessage.warning('请选择主营业务')
  }

  if (currentStep.value === 1) {
    const d = formData.value.detail
    if (!d.companyNameZh) return ElMessage.warning('请填写企业名称（中文）')
    if (!d.creditCode) return ElMessage.warning('请填写统一社会信用代码')
    if (!d.nature) return ElMessage.warning('请选择企业类型')
    if (!d.regCapital) return ElMessage.warning('请填写注册资本')
    if (!d.foundDate) return ElMessage.warning('请选择成立日期')
    if (!d.address) return ElMessage.warning('请填写注册地址')
    if (!d.gmName || !d.gmPhone) return ElMessage.warning('请填写总经理姓名和手机号')
    if (!d.contactName || !d.contactPhone) return ElMessage.warning('请填写业务对接人姓名和手机')
    if (!d.businessLicenseUploaded) return ElMessage.warning('请确认营业执照已上传')
    if (!d.bankLicenseUploaded) return ElMessage.warning('请确认开户银行许可证已上传')
  }

  if (currentStep.value === 2) {
    if (!formData.value.industryList.length) return ElMessage.warning('请至少选择一个主营行业')
    if (formData.value.industryList.includes('OTHER') && !formData.value.otherIndustry.trim()) {
      return ElMessage.warning('请选择“其他”时，请填写具体行业')
    }
    if (!formData.value.productDesc) return ElMessage.warning('请填写主营产品/服务描述')
    if (!formData.value.cover) return ElMessage.warning('请上传宣传封面')
    if (!formData.value.pdf) return ElMessage.warning('请上传公司介绍PDF')
    if (!formData.value.promoImgs.length) return ElMessage.warning('请上传企业宣传图')
  }

  currentStep.value++
}

const handleFinish = () => {
  submitting.value = true
  setTimeout(() => {
    submitting.value = false
    ElMessage.success('提交成功！请等待后台审核（1-3个工作日）')
    navigateTo('/member/orders')
  }, 2000)
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
:deep(.el-upload-dragger) { padding: 30px 20px; }
:deep(.el-step__head.is-process) { color: #0f172a; border-color: #0f172a; }
:deep(.el-step__title.is-process) { color: #0f172a; font-weight: 800; }
:deep(.el-step__head.is-success) { color: #10b981; border-color: #10b981; }
:deep(.el-step__title.is-success) { color: #10b981; }
:deep(.el-checkbox) { display: flex; align-items: center; }
</style>