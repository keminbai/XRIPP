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

    <el-alert
      v-if="showRetryAlert"
      type="warning"
      show-icon
      :closable="false"
      class="rounded-2xl"
    >
      <template #title>
        正在修改申请编号 SO{{ String(retryApplicationId).padStart(6, '0') }} 的被退回申请。
        {{ retryReason ? `处理说明：${retryReason}` : '请根据处理说明修订资料后重新提交。' }}
        {{ retryLockedApplyType ? ' 该申请已支付，本次仅允许修改资料，不再重复缴费，且暂不允许变更入驻方案。' : '' }}
      </template>
    </el-alert>

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
                :class="[
                  formData.type === 'normal' ? 'border-blue-500 bg-blue-50 shadow-md' : 'border-slate-200 hover:border-blue-300',
                  retryLockedApplyType ? 'opacity-70 cursor-not-allowed hover:shadow-none' : ''
                ]"
                @click="selectApplyType('normal')"
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
                :class="[
                  formData.type === 'strategic' ? 'border-amber-500 bg-amber-50 shadow-md' : 'border-slate-200 hover:border-amber-300',
                  retryLockedApplyType ? 'opacity-70 cursor-not-allowed hover:shadow-none' : ''
                ]"
                @click="selectApplyType('strategic')"
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

            <el-form label-width="220px" size="default">
              <el-form-item label="营业执照" required>
                <div class="w-full space-y-3">
                  <el-upload
                    drag
                    :limit="1"
                    accept=".pdf,.jpg,.jpeg,.png,.webp"
                    :show-file-list="true"
                    :file-list="businessLicenseFileList"
                    :http-request="uploadBusinessLicense"
                    :before-upload="beforeBusinessLicenseUpload"
                    :on-remove="handleBusinessLicenseRemove"
                  >
                    <el-icon class="el-icon--upload text-4xl text-slate-400"><UploadFilled /></el-icon>
                    <div class="el-upload__text text-sm">上传营业执照，支持 PDF/JPG/PNG</div>
                  </el-upload>
                  <div v-if="attachments.business_license" class="text-xs text-slate-500">
                    已上传：<a :href="attachments.business_license.url" target="_blank" class="text-blue-600 hover:underline">{{ attachments.business_license.fileName }}</a>
                    <span v-if="attachments.business_license.fileSize"> · {{ formatFileSize(attachments.business_license.fileSize) }}</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="开户银行许可证" required>
                <div class="w-full space-y-3">
                  <el-upload
                    drag
                    :limit="1"
                    accept=".pdf,.jpg,.jpeg,.png,.webp"
                    :show-file-list="true"
                    :file-list="bankLicenseFileList"
                    :http-request="uploadBankLicense"
                    :before-upload="beforeBankLicenseUpload"
                    :on-remove="handleBankLicenseRemove"
                  >
                    <el-icon class="el-icon--upload text-4xl text-slate-400"><UploadFilled /></el-icon>
                    <div class="el-upload__text text-sm">上传开户银行许可证，支持 PDF/JPG/PNG</div>
                  </el-upload>
                  <div v-if="attachments.bank_license" class="text-xs text-slate-500">
                    已上传：<a :href="attachments.bank_license.url" target="_blank" class="text-blue-600 hover:underline">{{ attachments.bank_license.fileName }}</a>
                    <span v-if="attachments.bank_license.fileSize"> · {{ formatFileSize(attachments.bank_license.fileSize) }}</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item
                v-for="cert in standardCertificateDefs"
                :key="cert.type"
                :label="cert.label"
                required
              >
                <div class="w-full space-y-3">
                  <el-radio-group v-model="certificateDrafts[cert.type].hasCertificate">
                    <el-radio :label="true">有</el-radio>
                    <el-radio :label="false">无</el-radio>
                  </el-radio-group>
                  <div v-if="certificateDrafts[cert.type].hasCertificate" class="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <el-input v-model="certificateDrafts[cert.type].certNo" placeholder="证书编号" />
                    <el-date-picker
                      v-model="certificateDrafts[cert.type].validTo"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="有效期至"
                      class="w-full"
                    />
                    <el-input v-model="certificateDrafts[cert.type].issuerName" placeholder="发证机构（选填）" />
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="其他资质">
                <div class="w-full space-y-3">
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <el-input v-model="certificateDrafts.OTHER.customName" placeholder="资质名称" />
                    <el-radio-group v-model="certificateDrafts.OTHER.hasCertificate">
                      <el-radio :label="true">有</el-radio>
                      <el-radio :label="false">无</el-radio>
                    </el-radio-group>
                  </div>
                  <div v-if="certificateDrafts.OTHER.hasCertificate" class="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <el-input v-model="certificateDrafts.OTHER.certNo" placeholder="证书编号" />
                    <el-date-picker
                      v-model="certificateDrafts.OTHER.validTo"
                      type="date"
                      value-format="YYYY-MM-DD"
                      placeholder="有效期至"
                      class="w-full"
                    />
                    <el-input v-model="certificateDrafts.OTHER.issuerName" placeholder="发证机构（选填）" />
                  </div>
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
                drag
                :limit="1"
                accept="image/*"
                :show-file-list="true"
                :file-list="coverFileList"
                :http-request="uploadCoverFile"
                :before-upload="beforeCoverUpload"
                :on-remove="handleCoverRemove"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Picture /></el-icon>
                <div class="el-upload__text text-sm">拖拽或点击上传封面图</div>
              </el-upload>
              <div v-if="attachments.cover_image" class="mt-3 text-xs text-slate-500">
                已上传：<a :href="attachments.cover_image.url" target="_blank" class="text-blue-600 hover:underline">{{ attachments.cover_image.fileName }}</a>
                <span v-if="attachments.cover_image.fileSize"> · {{ formatFileSize(attachments.cover_image.fileSize) }}</span>
              </div>
            </div>

            <!-- 公司介绍PDF -->
            <div class="bg-white p-6 rounded-xl border border-slate-200">
              <label class="block text-sm font-bold text-slate-700 mb-3">
                2. 公司介绍 PDF <span class="text-red-500">*</span>
                <span class="text-xs text-slate-400 font-normal ml-2">（仅限PDF格式）</span>
              </label>
              <el-upload
                drag
                :limit="1"
                accept="application/pdf,.pdf"
                :show-file-list="true"
                :file-list="pdfFileList"
                :http-request="uploadCompanyPdf"
                :before-upload="beforePdfUpload"
                :on-remove="handlePdfRemove"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Document /></el-icon>
                <div class="el-upload__text text-sm">只能上传 PDF 文件</div>
              </el-upload>
              <div v-if="attachments.company_pdf" class="mt-3 text-xs text-slate-500">
                已上传：<a :href="attachments.company_pdf.url" target="_blank" class="text-blue-600 hover:underline">{{ attachments.company_pdf.fileName }}</a>
                <span v-if="attachments.company_pdf.fileSize"> · {{ formatFileSize(attachments.company_pdf.fileSize) }}</span>
              </div>
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
                :show-file-list="true"
                :file-list="promoImgList"
                :http-request="uploadPromoImage"
                :before-upload="beforePromoUpload"
                :on-remove="handlePromoRemove"
              >
                <el-icon class="el-icon--upload text-4xl text-slate-400"><Picture /></el-icon>
                <div class="el-upload__text text-sm">拖拽或点击上传企业宣传图</div>
              </el-upload>
              <div v-if="attachments.promo_image" class="mt-3 text-xs text-slate-500">
                已上传：<a :href="attachments.promo_image.url" target="_blank" class="text-blue-600 hover:underline">{{ attachments.promo_image.fileName }}</a>
                <span v-if="attachments.promo_image.fileSize"> · {{ formatFileSize(attachments.promo_image.fileSize) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- ══════════ 步骤4：确认支付 ══════════ -->
        <div v-show="currentStep === 3" class="max-w-2xl mx-auto animate-fade-in">
          <div class="bg-slate-50 rounded-2xl p-8 border border-slate-200">
            <div class="text-center mb-6">
              <div class="text-sm text-slate-500 mb-1">应付总额</div>
              <div class="text-4xl font-bold" :class="formData.type === 'strategic' ? 'text-amber-600' : 'text-blue-600'">
                ¥{{ currentAmountText }}
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
              <div class="flex justify-between">
                <span class="text-slate-500">支付单号</span>
                <span class="font-mono text-xs">{{ paymentInfo?.paymentOrderNo || '生成中' }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-slate-500">支付状态</span>
                <el-tag :type="paymentTagType" size="small">{{ paymentStatusText }}</el-tag>
              </div>
              <div v-if="formData.isoList.length" class="flex justify-between">
                <span class="text-slate-500">ISO认证</span>
                <span>{{ formData.isoList.join('、') }}</span>
              </div>
            </div>

            <!-- 支付二维码 -->
            <div v-loading="paymentLoading" class="flex justify-center">
              <div class="w-52 h-52 bg-white border-2 border-slate-200 p-3 rounded-xl shadow-sm flex items-center justify-center">
                <img
                  v-if="paymentInfo?.qrCodeBase64"
                  :src="paymentInfo.qrCodeBase64"
                  alt="微信支付二维码"
                  class="w-full h-full object-contain rounded"
                />
                <div v-else class="w-full h-full bg-slate-100 flex items-center justify-center text-slate-400 text-xs rounded">
                  二维码生成中
                </div>
              </div>
            </div>

            <div class="mt-4 text-center text-xs text-slate-500 space-y-1">
              <p>请使用微信扫码支付。页面不会自行判定支付成功，必须以后端支付状态为准。</p>
              <p v-if="paymentInfo?.expiredAt">二维码有效期至：{{ formatInstant(paymentInfo.expiredAt) }}</p>
              <p v-if="paymentInfo?.paidAt" class="text-green-600">支付确认时间：{{ formatInstant(paymentInfo.paidAt) }}</p>
            </div>

            <div class="mt-6 flex justify-center gap-3">
              <el-button :loading="paymentChecking" @click="refreshPaymentStatus">
                刷新支付结果
              </el-button>
              <el-button
                v-if="paymentInfo?.paymentUrl"
                type="primary"
                plain
                @click="copyPaymentLink"
              >
                复制支付链接
              </el-button>
            </div>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-between items-center mt-10 pt-8 border-t border-slate-200">
          <el-button v-if="currentStep > 0" size="large" @click="currentStep--">
            <el-icon class="mr-1"><ArrowLeft /></el-icon> 上一步
          </el-button>
          <div v-else></div>

          <el-button v-if="currentStep < 3" type="primary" size="large" @click="handleNext" class="px-8" :loading="stepSubmitting">
            下一步 <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>

          <el-button
            v-else
            type="success"
            size="large"
            @click="handleFinish"
            class="px-12"
            :loading="submitting"
            :disabled="!canSubmitAfterPayment"
          >
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
import type { UploadRequestOptions, UploadUserFile } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useGlobalConfig } from '~/composables/useGlobalConfig'
import { apiFetchRaw, apiRequest } from '@/utils/request'

const { supplierPricing, cityOptions, industryOptions } = useGlobalConfig()

definePageMeta({ layout: 'member' })

const currentStep = ref(0)
const submitting = ref(false)
const stepSubmitting = ref(false)
const paymentLoading = ref(false)
const paymentChecking = ref(false)
const coverFileList = ref<UploadUserFile[]>([])
const pdfFileList = ref<UploadUserFile[]>([])
const promoImgList = ref<UploadUserFile[]>([])
const businessLicenseFileList = ref<UploadUserFile[]>([])
const bankLicenseFileList = ref<UploadUserFile[]>([])
const applicationId = ref<number | null>(null)
const route = useRoute()

type SupplierApplyPaymentInfo = {
  applicationId: number
  companyName: string
  applyType: 'normal' | 'strategic' | string
  feeAmount: number | string
  onboardingStatus: string
  paymentRequired: boolean
  paymentStatus: string
  paymentOrderId: number | null
  paymentOrderNo: string
  payChannel: string
  payStatus: string
  paymentUrl: string
  qrCodeBase64: string
  providerTradeNo?: string
  expiredAt?: string
  paidAt?: string
}

type SupplierApplyDetailResponse = {
  id: number
  companyName: string
  cityName: string
  mainService?: string
  serviceTypes?: unknown[]
  onboardingStatus: string
  paymentStatus: string
  applyType: string
  feeAmount: number | string
  paymentOrderId?: number | null
  paymentRequired: boolean
  changeReason: string
  intro?: string
  detail?: Record<string, any>
  attachments: Array<{
    id: number
    fileCategory: string
    fileName: string
    fileUrl: string
    fileExt?: string
    fileSize?: number
  }>
  certificates: Array<{
    id: number
    certType: string
    certName: string
    hasCertificate: boolean
    certNo: string
    validFrom: string
    validTo: string
    issuerName: string
    remarks: string
  }>
  paymentOrder?: {
    id?: number
    orderNo?: string
    payChannel?: string
    payStatus?: string
    paymentUrl?: string
    providerTradeNo?: string
    expiredAt?: string
    paidAt?: string
  }
}

const paymentInfo = ref<SupplierApplyPaymentInfo | null>(null)
const loadedApplication = ref<SupplierApplyDetailResponse | null>(null)

type AttachmentCategory =
  | 'cover_image'
  | 'company_pdf'
  | 'promo_image'
  | 'business_license'
  | 'bank_license'

type UploadedFileMeta = {
  url: string
  fileName: string
  storedName?: string
  fileExt?: string
  fileSize?: number
  contentType?: string
}

type UploadApiResponse = {
  code: number | string
  message: string
  data?: Partial<UploadedFileMeta> | null
}

type CertificateDraft = {
  hasCertificate: boolean | null
  certNo: string
  validFrom: string
  validTo: string
  issuerName: string
  remarks: string
  customName?: string
}

// ISO选项
const isoOptions = [
  { label: 'ISO 9001', value: 'ISO9001' },
  { label: 'ISO 14001', value: 'ISO14001' },
  { label: 'ISO 45001', value: 'ISO45001' },
  { label: 'ISO 13485', value: 'ISO13485' },
  { label: 'IATF 16949', value: 'IATF16949' },
  { label: 'ISO 22000', value: 'ISO22000' },
  { label: 'ISO 27001', value: 'ISO27001' },
]

const standardCertificateDefs = [
  { type: 'ISO9001', label: 'ISO 9001 质量认证' },
  { type: 'ISO14001', label: 'ISO 14001 环境管理体系' },
  { type: 'ISO45001', label: 'ISO 45001 职业健康安全' },
  { type: 'ISO13485', label: '医疗器械：ISO 13485' },
  { type: 'IATF16949', label: '汽车行业：IATF 16949' },
  { type: 'ISO22000', label: '食品行业：ISO 22000' },
  { type: 'ISO27001', label: '信息安全：ISO 27001' }
] as const

type StandardCertificateType = typeof standardCertificateDefs[number]['type']
type ManagedCertificateType = StandardCertificateType | 'OTHER'

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

const createCertificateDraft = (): CertificateDraft => ({
  hasCertificate: null,
  certNo: '',
  validFrom: '',
  validTo: '',
  issuerName: '',
  remarks: '',
  customName: ''
})

const certificateDrafts = reactive<Record<ManagedCertificateType, CertificateDraft>>({
  ISO9001: createCertificateDraft(),
  ISO14001: createCertificateDraft(),
  ISO45001: createCertificateDraft(),
  ISO13485: createCertificateDraft(),
  IATF16949: createCertificateDraft(),
  ISO22000: createCertificateDraft(),
  ISO27001: createCertificateDraft(),
  OTHER: createCertificateDraft()
})

const attachments = reactive<Record<AttachmentCategory, UploadedFileMeta | null>>({
  cover_image: null,
  company_pdf: null,
  promo_image: null,
  business_license: null,
  bank_license: null
})

const formData = ref({
  city: '',
  mainService: '',
  type: 'normal',
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
    contactEmail: ''
  }
})

const retryApplicationId = computed<number | null>(() => {
  const raw = Array.isArray(route.query.application_id) ? route.query.application_id[0] : route.query.application_id
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
})

const retryMode = computed(() => {
  const raw = Array.isArray(route.query.mode) ? route.query.mode[0] : route.query.mode
  return String(raw || '').toLowerCase() === 'retry'
})

const currentAmountText = computed(() => {
  const fee = Number(paymentInfo.value?.feeAmount)
  if (Number.isFinite(fee) && fee > 0) {
    return fee.toLocaleString()
  }
  const price = formData.value.type === 'strategic'
    ? supplierPricing.strategic.price
    : supplierPricing.normal.price
  return price.toLocaleString()
})

const paymentStatusText = computed(() => {
  const status = String(paymentInfo.value?.payStatus || paymentInfo.value?.paymentStatus || '').toLowerCase()
  if (status === 'paid') return '已支付'
  if (status === 'waived') return '免支付'
  if (status === 'pending') return '待支付'
  if (status === 'closed') return '已关闭'
  if (status === 'failed') return '支付失败'
  return '待支付'
})

const paymentTagType = computed(() => {
  const status = String(paymentInfo.value?.payStatus || paymentInfo.value?.paymentStatus || '').toLowerCase()
  if (status === 'paid' || status === 'waived') return 'success'
  if (status === 'closed' || status === 'failed') return 'danger'
  return 'warning'
})

const canSubmitAfterPayment = computed(() => {
  const status = String(paymentInfo.value?.payStatus || paymentInfo.value?.paymentStatus || '').toLowerCase()
  return !!applicationId.value && ['paid', 'waived'].includes(status)
})

const retryReason = computed(() => loadedApplication.value?.changeReason || '')

const retryLockedApplyType = computed(() => {
  const application = loadedApplication.value
  if (!application) return false
  return ['precheck_failed', 'final_review_failed'].includes(String(application.onboardingStatus || ''))
    && ['paid', 'waived'].includes(String(application.paymentStatus || '').toLowerCase())
})

const showRetryAlert = computed(() => {
  return retryMode.value
    && !!retryApplicationId.value
    && ['precheck_failed', 'final_review_failed'].includes(String(loadedApplication.value?.onboardingStatus || ''))
})

const normalizeUploadMeta = (raw: Partial<UploadedFileMeta> | null | undefined, fallbackName: string): UploadedFileMeta => {
  const fileSize = Number(raw?.fileSize)
  return {
    url: String(raw?.url || ''),
    fileName: String(raw?.fileName || fallbackName || '未命名文件'),
    storedName: raw?.storedName ? String(raw.storedName) : '',
    fileExt: raw?.fileExt ? String(raw.fileExt).toLowerCase() : '',
    fileSize: Number.isFinite(fileSize) ? fileSize : undefined,
    contentType: raw?.contentType ? String(raw.contentType) : ''
  }
}

const toUploadUserFile = (file: UploadedFileMeta): UploadUserFile => ({
  name: file.fileName,
  url: file.url,
  status: 'success'
})

const isPaymentSatisfied = (status?: string) => ['paid', 'waived'].includes(String(status || '').toLowerCase())

const selectApplyType = (type: 'normal' | 'strategic') => {
  if (retryLockedApplyType.value && formData.value.type !== type) {
    ElMessage.warning('该申请已支付，当前仅允许修改资料，暂不允许变更入驻方案')
    return
  }
  formData.value.type = type
}

const formatFileSize = (size?: number) => {
  if (!size || size <= 0) return ''
  if (size >= 1024 * 1024) return `${(size / 1024 / 1024).toFixed(2)} MB`
  if (size >= 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${size} B`
}

const validateUpload = (rawFile: File, allowedExts: string[], maxMb: number, label: string) => {
  const ext = rawFile.name.split('.').pop()?.toLowerCase() || ''
  if (!allowedExts.includes(ext)) {
    ElMessage.warning(`${label}仅支持 ${allowedExts.join('/').toUpperCase()} 格式`)
    return false
  }
  if (rawFile.size > maxMb * 1024 * 1024) {
    ElMessage.warning(`${label}大小不能超过 ${maxMb}MB`)
    return false
  }
  return true
}

const beforeCoverUpload = (rawFile: File) => validateUpload(rawFile, ['jpg', 'jpeg', 'png', 'webp'], 2, '宣传封面')
const beforePdfUpload = (rawFile: File) => validateUpload(rawFile, ['pdf'], 10, '公司介绍 PDF')
const beforePromoUpload = (rawFile: File) => validateUpload(rawFile, ['jpg', 'jpeg', 'png', 'webp'], 5, '企业宣传图')
const beforeBusinessLicenseUpload = (rawFile: File) => validateUpload(rawFile, ['pdf', 'jpg', 'jpeg', 'png', 'webp'], 10, '营业执照')
const beforeBankLicenseUpload = (rawFile: File) => validateUpload(rawFile, ['pdf', 'jpg', 'jpeg', 'png', 'webp'], 10, '开户银行许可证')

const uploadAttachment = async (
  category: AttachmentCategory,
  options: UploadRequestOptions,
  fileListRef: typeof coverFileList
) => {
  const form = new FormData()
  form.append('file', options.file)
  try {
    const response = await apiFetchRaw<UploadApiResponse>('/common/upload', {
      method: 'POST',
      body: form
    })
    if (Number(response?.code) !== 200 || !response?.data?.url) {
      throw new Error(response?.message || '上传失败')
    }
    const uploaded = normalizeUploadMeta(response.data, options.file.name)
    attachments[category] = uploaded
    fileListRef.value = [toUploadUserFile(uploaded)]
    options.onSuccess?.(response as any)
    ElMessage.success('上传成功')
  } catch (error: any) {
    options.onError?.(error)
    ElMessage.error(error?.message || '上传失败')
  }
}

const clearAttachment = (category: AttachmentCategory, fileListRef: typeof coverFileList) => {
  attachments[category] = null
  fileListRef.value = []
}

const uploadCoverFile = (options: UploadRequestOptions) => uploadAttachment('cover_image', options, coverFileList)
const uploadCompanyPdf = (options: UploadRequestOptions) => uploadAttachment('company_pdf', options, pdfFileList)
const uploadPromoImage = (options: UploadRequestOptions) => uploadAttachment('promo_image', options, promoImgList)
const uploadBusinessLicense = (options: UploadRequestOptions) => uploadAttachment('business_license', options, businessLicenseFileList)
const uploadBankLicense = (options: UploadRequestOptions) => uploadAttachment('bank_license', options, bankLicenseFileList)

const handleCoverRemove = () => clearAttachment('cover_image', coverFileList)
const handlePdfRemove = () => clearAttachment('company_pdf', pdfFileList)
const handlePromoRemove = () => clearAttachment('promo_image', promoImgList)
const handleBusinessLicenseRemove = () => clearAttachment('business_license', businessLicenseFileList)
const handleBankLicenseRemove = () => clearAttachment('bank_license', bankLicenseFileList)

const resetCertificateDrafts = () => {
  (Object.keys(certificateDrafts) as ManagedCertificateType[]).forEach((key) => {
    Object.assign(certificateDrafts[key], createCertificateDraft())
  })
}

const resetAttachmentState = () => {
  clearAttachment('cover_image', coverFileList)
  clearAttachment('company_pdf', pdfFileList)
  clearAttachment('promo_image', promoImgList)
  clearAttachment('business_license', businessLicenseFileList)
  clearAttachment('bank_license', bankLicenseFileList)
}

const setAttachmentFromDetail = (
  category: AttachmentCategory,
  raw: { fileUrl?: string; fileName?: string; fileExt?: string; fileSize?: number } | undefined
) => {
  if (!raw?.fileUrl) return
  const normalized = normalizeUploadMeta({
    url: raw.fileUrl,
    fileName: raw.fileName,
    fileExt: raw.fileExt,
    fileSize: raw.fileSize
  }, raw.fileName || '')
  attachments[category] = normalized

  const userFile = [toUploadUserFile(normalized)]
  if (category === 'cover_image') coverFileList.value = userFile
  if (category === 'company_pdf') pdfFileList.value = userFile
  if (category === 'promo_image') promoImgList.value = userFile
  if (category === 'business_license') businessLicenseFileList.value = userFile
  if (category === 'bank_license') bankLicenseFileList.value = userFile
}

const buildCertificatesPayload = () => {
  const items = standardCertificateDefs.map(cert => ({
    cert_type: cert.type,
    cert_name: cert.label,
    has_certificate: certificateDrafts[cert.type].hasCertificate,
    cert_no: certificateDrafts[cert.type].certNo.trim(),
    valid_from: certificateDrafts[cert.type].validFrom || '',
    valid_to: certificateDrafts[cert.type].validTo || '',
    issuer_name: certificateDrafts[cert.type].issuerName.trim(),
    remarks: certificateDrafts[cert.type].remarks.trim()
  }))

  const other = certificateDrafts.OTHER
  const otherTouched = !!(
    other.customName?.trim()
    || other.hasCertificate !== null
    || other.certNo.trim()
    || other.validFrom
    || other.validTo
    || other.issuerName.trim()
    || other.remarks.trim()
  )

  if (otherTouched) {
    items.push({
      cert_type: 'OTHER',
      cert_name: other.customName?.trim() || '',
      has_certificate: other.hasCertificate,
      cert_no: other.certNo.trim(),
      valid_from: other.validFrom || '',
      valid_to: other.validTo || '',
      issuer_name: other.issuerName.trim(),
      remarks: other.remarks.trim()
    })
  }

  return items
}

const buildAttachmentsPayload = () => {
  const categories: AttachmentCategory[] = [
    'cover_image',
    'company_pdf',
    'promo_image',
    'business_license',
    'bank_license'
  ]

  return categories.flatMap((category, index) => {
    const file = attachments[category]
    if (!file) return []
    return [{
      file_category: category,
      file_name: file.fileName,
      file_url: file.url,
      file_ext: file.fileExt || '',
      file_size: file.fileSize ?? 0,
      sort_order: index
    }]
  })
}

const validateCertificateDrafts = () => {
  for (const cert of standardCertificateDefs) {
    const draft = certificateDrafts[cert.type]
    if (draft.hasCertificate === null) {
      return `请明确填写“${cert.label}”是否具备`
    }
    if (draft.hasCertificate) {
      if (!draft.certNo.trim()) return `请填写“${cert.label}”证书编号`
      if (!draft.validTo) return `请填写“${cert.label}”有效期`
    }
  }

  const other = certificateDrafts.OTHER
  const otherTouched = !!(
    other.customName?.trim()
    || other.hasCertificate !== null
    || other.certNo.trim()
    || other.validFrom
    || other.validTo
    || other.issuerName.trim()
    || other.remarks.trim()
  )
  if (otherTouched) {
    if (!other.customName?.trim()) return '请填写其他资质名称'
    if (other.hasCertificate === null) return '请明确填写“其他资质”是否具备'
    if (other.hasCertificate) {
      if (!other.certNo.trim()) return '请填写其他资质证书编号'
      if (!other.validTo) return '请填写其他资质有效期'
    }
  }

  return ''
}

const buildDraftPayload = () => {
  const f = formData.value
  const d = f.detail
  const industries = f.industryList.includes('OTHER') && f.otherIndustry.trim()
    ? [...f.industryList.filter(i => i !== 'OTHER'), f.otherIndustry.trim()]
    : f.industryList
  const attachmentsPayload = buildAttachmentsPayload()
  const certificatesPayload = buildCertificatesPayload()

  return {
    application_id: applicationId.value,
    company_name: d.companyNameZh,
    city: f.city,
    apply_type: f.type,
    type: f.type,
    service_types_json: JSON.stringify(industries),
    intro: f.productDesc,
    attachments: attachmentsPayload,
    certificates: certificatesPayload,
    detail_json: JSON.stringify({
      companyNameZh: d.companyNameZh,
      companyNameEn: d.companyNameEn,
      creditCode: d.creditCode,
      nature: d.nature,
      regCapital: d.regCapital,
      foundDate: d.foundDate,
      address: d.address,
      gmName: d.gmName,
      gmPhone: d.gmPhone,
      contactName: d.contactName,
      contactTitle: d.contactTitle,
      contactPhone: d.contactPhone,
      contactEmail: d.contactEmail,
      mainService: f.mainService,
      applyType: f.type,
      isoList: f.isoList,
      industryList: industries,
      productDesc: f.productDesc,
      attachmentSummary: attachmentsPayload.map(item => ({
        fileCategory: item.file_category,
        fileName: item.file_name,
        fileUrl: item.file_url
      })),
      certificateSummary: certificatesPayload
    })
  }
}

const applyApplicationDetail = (detail: SupplierApplyDetailResponse) => {
  const payload = detail.detail || {}
  const knownIndustryValues = new Set(mainIndustryOptions.filter(item => item.value !== 'OTHER').map(item => item.value))
  const rawIndustryList = Array.isArray(payload.industryList) ? payload.industryList : detail.serviceTypes
  const detailIndustries = Array.isArray(rawIndustryList)
    ? rawIndustryList.map(item => String(item || '').trim()).filter(Boolean)
    : []
  const knownIndustries = detailIndustries.filter(item => knownIndustryValues.has(item))
  const customIndustries = detailIndustries.filter(item => !knownIndustryValues.has(item) && item !== 'OTHER')
  const isoList = Array.isArray(payload.isoList)
    ? payload.isoList.map(item => String(item || '').trim()).filter(Boolean)
    : []

  formData.value.city = String(detail.cityName || '')
  formData.value.mainService = String(payload.mainService || detail.mainService || '')
  formData.value.type = detail.applyType === 'strategic' ? 'strategic' : 'normal'
  formData.value.isoList = isoList
  formData.value.industryList = customIndustries.length ? [...knownIndustries, 'OTHER'] : knownIndustries
  formData.value.otherIndustry = customIndustries.join(' / ')
  formData.value.productDesc = String(payload.productDesc || detail.intro || '')
  formData.value.detail = {
    companyNameZh: String(payload.companyNameZh || detail.companyName || ''),
    companyNameEn: String(payload.companyNameEn || ''),
    creditCode: String(payload.creditCode || ''),
    nature: String(payload.nature || ''),
    regCapital: String(payload.regCapital || ''),
    foundDate: String(payload.foundDate || ''),
    address: String(payload.address || ''),
    gmName: String(payload.gmName || ''),
    gmPhone: String(payload.gmPhone || ''),
    contactName: String(payload.contactName || ''),
    contactTitle: String(payload.contactTitle || ''),
    contactPhone: String(payload.contactPhone || ''),
    contactEmail: String(payload.contactEmail || '')
  }

  resetAttachmentState()
  ;(detail.attachments || []).forEach((file) => {
    const category = String(file.fileCategory || '') as AttachmentCategory
    if (['cover_image', 'company_pdf', 'promo_image', 'business_license', 'bank_license'].includes(category)) {
      setAttachmentFromDetail(category, file)
    }
  })

  resetCertificateDrafts()
  ;(detail.certificates || []).forEach((certificate) => {
    const certType = String(certificate.certType || '') as ManagedCertificateType
    if (certType !== 'OTHER' && !(certType in certificateDrafts)) {
      return
    }
    const draft = certificateDrafts[certType]
    if (!draft) return
    draft.hasCertificate = certificate.hasCertificate
    draft.certNo = String(certificate.certNo || '')
    draft.validFrom = String(certificate.validFrom || '')
    draft.validTo = String(certificate.validTo || '')
    draft.issuerName = String(certificate.issuerName || '')
    draft.remarks = String(certificate.remarks || '')
    if (certType === 'OTHER') {
      draft.customName = String(certificate.certName || '')
    }
  })

  applicationId.value = detail.id
  paymentInfo.value = {
    applicationId: detail.id,
    companyName: detail.companyName,
    applyType: detail.applyType,
    feeAmount: detail.feeAmount,
    onboardingStatus: detail.onboardingStatus,
    paymentRequired: detail.paymentRequired,
    paymentStatus: detail.paymentStatus,
    paymentOrderId: detail.paymentOrder?.id ?? detail.paymentOrderId ?? null,
    paymentOrderNo: String(detail.paymentOrder?.orderNo || ''),
    payChannel: String(detail.paymentOrder?.payChannel || ''),
    payStatus: String(detail.paymentOrder?.payStatus || detail.paymentStatus || ''),
    paymentUrl: String(detail.paymentOrder?.paymentUrl || ''),
    qrCodeBase64: '',
    providerTradeNo: String(detail.paymentOrder?.providerTradeNo || ''),
    expiredAt: detail.paymentOrder?.expiredAt,
    paidAt: detail.paymentOrder?.paidAt
  }
}

const loadEditableApplication = async (id: number) => {
  const res = await apiRequest<SupplierApplyDetailResponse>(`/v3/member/supplier-apply/${id}`)
  const detail = res.data
  const status = String(detail?.onboardingStatus || '')
  const editableStatuses = ['draft', 'pending_payment', 'precheck_failed', 'final_review_failed']
  if (!detail || !editableStatuses.includes(status)) {
    throw new Error('当前申请状态不支持继续修改')
  }
  loadedApplication.value = detail
  applyApplicationDetail(detail)

  if (retryMode.value && !['precheck_failed', 'final_review_failed'].includes(status)) {
    ElMessage.info('当前申请不是被退回状态，已按草稿继续编辑处理')
  } else if (retryMode.value && isPaymentSatisfied(detail.paymentStatus)) {
    ElMessage.info('已加载被退回申请资料，原支付结果保留，无需重复缴费')
  } else if (retryMode.value) {
    ElMessage.info('已加载被退回申请资料，请修订后重新提交')
  }
}

const createDraftAndPreparePayment = async () => {
  paymentLoading.value = true
  try {
    const body = buildDraftPayload()
    const res: any = await apiRequest('/v3/member/supplier-apply/draft', { method: 'POST', body })
    paymentInfo.value = (res?.data || null) as SupplierApplyPaymentInfo | null
    applicationId.value = Number(res?.data?.applicationId || 0) || null
  } finally {
    paymentLoading.value = false
  }
}

const refreshPaymentStatus = async () => {
  if (!applicationId.value) return
  paymentChecking.value = true
  try {
    const res: any = await apiRequest(`/v3/member/supplier-apply/${applicationId.value}/payment`)
    paymentInfo.value = (res?.data || null) as SupplierApplyPaymentInfo | null
    if (isPaymentSatisfied(String(res?.data?.payStatus || res?.data?.paymentStatus || ''))) {
      ElMessage.success('支付已确认，可以提交审核')
    } else {
      ElMessage.info('支付尚未确认，请稍后重试')
    }
  } finally {
    paymentChecking.value = false
  }
}

const copyPaymentLink = async () => {
  const url = paymentInfo.value?.paymentUrl
  if (!url) return ElMessage.warning('暂无可复制的支付链接')
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('支付链接已复制')
  } catch {
    ElMessage.error('复制失败，请手动扫码支付')
  }
}

const formatInstant = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

const handleNext = async () => {
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
    if (!attachments.business_license) return ElMessage.warning('请上传营业执照')
    if (!attachments.bank_license) return ElMessage.warning('请上传开户银行许可证')
    const certificateError = validateCertificateDrafts()
    if (certificateError) return ElMessage.warning(certificateError)
  }

  if (currentStep.value === 2) {
    if (!formData.value.industryList.length) return ElMessage.warning('请至少选择一个主营行业')
    if (formData.value.industryList.includes('OTHER') && !formData.value.otherIndustry.trim()) {
      return ElMessage.warning('请选择“其他”时，请填写具体行业')
    }
    if (!formData.value.productDesc) return ElMessage.warning('请填写主营产品/服务描述')
    if (!attachments.cover_image) return ElMessage.warning('请上传宣传封面')
    if (!attachments.company_pdf) return ElMessage.warning('请上传公司介绍PDF')
    if (!attachments.promo_image) return ElMessage.warning('请上传企业宣传图')

    stepSubmitting.value = true
    try {
      await createDraftAndPreparePayment()
    } catch (e: any) {
      ElMessage.error(e?.message || '支付单生成失败')
      return
    } finally {
      stepSubmitting.value = false
    }
  }

  currentStep.value++
}

const handleFinish = async () => {
  if (!applicationId.value) {
    return ElMessage.warning('请先生成支付单')
  }
  if (!canSubmitAfterPayment.value) {
    return ElMessage.warning('支付尚未确认，暂不能提交审核')
  }

  submitting.value = true
  try {
    await apiRequest(`/v3/member/supplier-apply/${applicationId.value}/submit`, { method: 'POST' })
    ElMessage.success('提交成功！请等待后台审核（1-3个工作日）')
    navigateTo('/member/supplier-applications')
  } catch (e: any) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!retryApplicationId.value) return
  try {
    await loadEditableApplication(retryApplicationId.value)
  } catch (e: any) {
    ElMessage.error(e?.message || '读取原申请资料失败')
    navigateTo('/member/supplier-applications')
  }
})
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
