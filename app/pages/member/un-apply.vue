<!-- 文件路径: D:\ipp-platform\app\pages\member\un-apply.vue -->
<template>
  <div class="space-y-6">

    <!-- 顶部说明 -->
    <div class="relative bg-gradient-to-r from-blue-900 to-indigo-900 rounded-2xl p-8 text-white shadow-lg">
      <div class="relative z-10">
        <h1 class="text-3xl font-bold mb-2">联合国供应商注册申请</h1>
        <p class="text-blue-200 text-sm max-w-2xl leading-relaxed">
          本页面用于提交联合国企业信息审核表。请确保所填信息真实、准确，并与企业官方文件保持一致。
        </p>
        <div class="mt-4 flex items-center gap-2 text-sm bg-white/10 w-fit px-3 py-1 rounded-full backdrop-blur-sm">
          <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
          当前状态：{{ statusText }}
        </div>
      </div>
      <div class="absolute right-0 top-0 h-full w-1/3 bg-blue-500/10 skew-x-12"></div>
    </div>

    <!-- 流程步骤 -->
    <div class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
      <el-steps :active="activeStep" align-center finish-status="success">
        <el-step title="填写申请资料" description="完成联合国供应商注册信息" />
        <el-step title="平台审核" description="进行合规性与资格预审" />
        <el-step title="注册完成" description="下载联合国注册证书" />
      </el-steps>
    </div>

    <!-- 表单区域 -->
    <div v-if="applicationStatus === 'none'" class="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
      <el-tabs
        v-model="activeTab"
        class="custom-tabs"
        :before-leave="handleTabChange"
      >

        <!-- ══════════ Tab 1: 公司基本信息 ══════════ -->
        <el-tab-pane label="第一步：公司基本信息" name="basic">
          <el-form :model="form" label-position="top" size="large">
            
            <el-alert type="warning" show-icon :closable="false" class="mb-6" title="重要提示"
              description="请确保使用企业官方核定的标准英文名称，包括大小写、空格及标点符号。与对外贸易单据保持严格一致。" />

            <!-- A. 公司名称 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">公司名称</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="中文名称 (Company Name in Chinese)" required>
                  <el-input v-model="form.companyNameZh" placeholder="营业执照上的公司全称" />
                </el-form-item>
                <el-form-item label="英文名称 (Company Name in English)" required>
                  <el-input v-model="form.companyNameEn" placeholder="e.g. Shanghai XXX Co., Ltd." />
                </el-form-item>
              </div>
            </div>

            <!-- B. 注册信息 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">注册信息</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="统一社会信用代码 (License Number)" required>
                  <el-input v-model="form.licenseNumber" placeholder="18位信用代码" maxlength="18" />
                </el-form-item>
                <el-form-item label="公司类型 (Company Type)" required>
                  <el-select v-model="form.companyType" placeholder="请选择（11选1）" class="w-full">
                    <el-option v-for="t in companyTypes" :key="t.value" :label="t.label" :value="t.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="成立日期 (Year Established)" required>
                  <el-date-picker
                    v-model="form.establishedDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="与营业执照一致（年-月-日）"
                    class="w-full"
                  />
                </el-form-item>
                <el-form-item label="员工数量 (Number of Employees)">
                  <el-input v-model="form.employees" type="number" placeholder="总人数" />
                </el-form-item>
              </div>
            </div>

            <!-- C. 联系人信息 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">联系人信息 (Contact Information)</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="联系人姓名 (Name)" required>
                  <el-input v-model="form.contactName" placeholder="Full Name" />
                </el-form-item>
                <el-form-item label="性别 (Gender)">
                  <el-select v-model="form.contactGender" placeholder="请选择" class="w-full">
                    <el-option label="男 (Male)" value="M" />
                    <el-option label="女 (Female)" value="F" />
                    <el-option label="不便透露 (Prefer not to say)" value="N/A" />
                  </el-select>
                </el-form-item>
                <el-form-item label="职位 (Title)">
                  <el-input v-model="form.contactTitle" placeholder="e.g. General Manager" />
                </el-form-item>
                <el-form-item label="电子邮箱 (Email)" required>
                  <el-input v-model="form.email" placeholder="contact@company.com" />
                </el-form-item>
                <el-form-item label="座机 (Telephone)">
                  <el-input v-model="form.telephone" placeholder="+86-21-12345678" />
                </el-form-item>
                <el-form-item label="传真 (Fax number)">
                  <el-input v-model="form.fax" placeholder="+86-21-12345679（选填）" />
                </el-form-item>
                <el-form-item label="手机 (Mobile)" required>
                  <el-input v-model="form.mobile" placeholder="+86-138****1234" />
                </el-form-item>
              </div>
              <el-form-item label="公司网址 (Website)">
                <el-input v-model="form.website" placeholder="https://www.company.com" />
              </el-form-item>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="公司地址 (Address)" required>
                  <el-input v-model="form.address" placeholder="请精确到门牌号" />
                </el-form-item>
                <el-form-item label="邮编 (Postcode)" required>
                  <el-input v-model="form.postcode" placeholder="如：200120" />
                </el-form-item>
              </div>
            </div>

            <div class="mt-8 text-right">
              <el-button type="primary" size="large" class="px-8" @click="activeTab = 'ownership'">
                下一步 <el-icon class="ml-2"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- ══════════ Tab 2: 所有权结构 ══════════ -->
        <el-tab-pane label="第二步：所有权结构" name="ownership">
          <el-form :model="form" label-position="top" size="large">

            <!-- A. 所有权类型（按PDF数字口径） -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">公司所有权类型 (COMPANY'S OWNERSHIP)</h4>
              <el-form-item label="请选择并填写相应数字（1-4）" required>
                <el-radio-group v-model="form.ownershipTypeCode">
                  <el-radio :label="1">1. 私有 (Privately-owned)</el-radio>
                  <el-radio :label="2">2. 上市公司 (Publicly-traded)</el-radio>
                  <el-radio :label="3">3. 企业集团的一部分 (Part of a business conglomerate)</el-radio>
                  <el-radio :label="4">4. 不适用 (Not applicable)</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>

            <!-- B. 私有企业补充（a/b） -->
            <div class="mb-6" v-if="form.ownershipTypeCode === 1">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">私有企业补充信息（中英文）</h4>

              <el-form-item label="是否存在母公司/子公司/附属公司？">
                <el-radio-group v-model="form.privateHasAffiliates">
                  <el-radio :label="0">否（无母子附属）</el-radio>
                  <el-radio :label="1">是（有母子附属）</el-radio>
                </el-radio-group>
              </el-form-item>

              <!-- 私有且无母子附属 -->
              <div v-if="form.privateHasAffiliates === 0" class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="拥有者（法人）姓名（中文）" required>
                  <el-input v-model="form.privateOwnerNameZh" placeholder="法人中文姓名" />
                </el-form-item>
                <el-form-item label="Owner Name (English)" required>
                  <el-input v-model="form.privateOwnerNameEn" placeholder="Owner full name in English" />
                </el-form-item>
              </div>

              <!-- 私有且有母子附属 -->
              <div v-else class="space-y-4">
                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">贵公司（Your Company）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）">
                      <el-input v-model="form.selfCompanyNameZh" />
                    </el-form-item>
                    <el-form-item label="Company Name (EN)">
                      <el-input v-model="form.selfCompanyNameEn" />
                    </el-form-item>
                    <el-form-item label="拥有者（法人）姓名">
                      <el-input v-model="form.selfOwnerName" />
                    </el-form-item>
                    <el-form-item label="CEO / General Manager">
                      <el-input v-model="form.selfCeoName" />
                    </el-form-item>
                    <el-form-item label="控股权人士（如适用）">
                      <el-input v-model="form.selfControllingPerson" />
                    </el-form-item>
                  </div>
                </div>

                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">母公司（Parent Company）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）">
                      <el-input v-model="form.parentCompanyNameZh" />
                    </el-form-item>
                    <el-form-item label="Company Name (EN)">
                      <el-input v-model="form.parentCompanyNameEn" />
                    </el-form-item>
                    <el-form-item label="拥有者（法人）姓名">
                      <el-input v-model="form.parentOwnerName" />
                    </el-form-item>
                    <el-form-item label="CEO / General Manager">
                      <el-input v-model="form.parentCeoName" />
                    </el-form-item>
                    <el-form-item label="控股权人士（如适用）">
                      <el-input v-model="form.parentControllingPerson" />
                    </el-form-item>
                  </div>
                </div>

                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">子公司/附属公司（Subsidiary/Affiliate）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）">
                      <el-input v-model="form.subCompanyNameZh" />
                    </el-form-item>
                    <el-form-item label="Company Name (EN)">
                      <el-input v-model="form.subCompanyNameEn" />
                    </el-form-item>
                    <el-form-item label="拥有者（法人）姓名">
                      <el-input v-model="form.subOwnerName" />
                    </el-form-item>
                    <el-form-item label="CEO / General Manager">
                      <el-input v-model="form.subCeoName" />
                    </el-form-item>
                    <el-form-item label="控股权人士（如适用）">
                      <el-input v-model="form.subControllingPerson" />
                    </el-form-item>
                  </div>
                </div>
              </div>
            </div>

            <!-- C. 集团企业补充 -->
            <div class="mb-6" v-if="form.ownershipTypeCode === 3">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">集团企业补充信息（中英文）</h4>
              <div class="text-xs text-slate-500 mb-4">
                请填写贵公司、母公司、子公司/附属公司的：公司名称、法人、CEO、控股权人士（如适用）。
              </div>

              <div class="space-y-4">
                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">贵公司（Your Company）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）"><el-input v-model="form.groupSelfCompanyNameZh" /></el-form-item>
                    <el-form-item label="Company Name (EN)"><el-input v-model="form.groupSelfCompanyNameEn" /></el-form-item>
                    <el-form-item label="拥有者（法人）姓名"><el-input v-model="form.groupSelfOwnerName" /></el-form-item>
                    <el-form-item label="CEO / General Manager"><el-input v-model="form.groupSelfCeoName" /></el-form-item>
                    <el-form-item label="控股权人士（如适用）"><el-input v-model="form.groupSelfControllingPerson" /></el-form-item>
                  </div>
                </div>

                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">母公司（Parent Company）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）"><el-input v-model="form.groupParentCompanyNameZh" /></el-form-item>
                    <el-form-item label="Company Name (EN)"><el-input v-model="form.groupParentCompanyNameEn" /></el-form-item>
                    <el-form-item label="拥有者（法人）姓名"><el-input v-model="form.groupParentOwnerName" /></el-form-item>
                    <el-form-item label="CEO / General Manager"><el-input v-model="form.groupParentCeoName" /></el-form-item>
                    <el-form-item label="控股权人士（如适用）"><el-input v-model="form.groupParentControllingPerson" /></el-form-item>
                  </div>
                </div>

                <div class="p-4 rounded-lg border border-slate-200 bg-slate-50">
                  <div class="font-semibold text-slate-700 mb-3">子公司/附属公司（Subsidiary/Affiliate）</div>
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <el-form-item label="公司名称（中）"><el-input v-model="form.groupSubCompanyNameZh" /></el-form-item>
                    <el-form-item label="Company Name (EN)"><el-input v-model="form.groupSubCompanyNameEn" /></el-form-item>
                    <el-form-item label="拥有者（法人）姓名"><el-input v-model="form.groupSubOwnerName" /></el-form-item>
                    <el-form-item label="CEO / General Manager"><el-input v-model="form.groupSubCeoName" /></el-form-item>
                    <el-form-item label="控股权人士（如适用）"><el-input v-model="form.groupSubControllingPerson" /></el-form-item>
                  </div>
                </div>
              </div>
            </div>

            <!-- D. 女性控股（按PDF 1/2/3） -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">女性控股 (Women ownership)</h4>
              <el-form-item label="请选择相应数字（1-3）" required>
                <el-radio-group v-model="form.womenOwnedCode">
                  <el-radio :label="1">1. 不适用 (Not applicable)</el-radio>
                  <el-radio :label="2">2. 至少51%由女性控制</el-radio>
                  <el-radio :label="3">3. 低于51%由女性控制</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>

            <!-- E. 残疾包容（按PDF 1/2） -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">残疾包容 (Disability inclusion)</h4>
              <el-form-item label="请选择相应数字（1-2）" required>
                <el-radio-group v-model="form.disabilityInclusionCode">
                  <el-radio :label="1">1. 不明确 (Not specified)</el-radio>
                  <el-radio :label="2">2. 符合，残疾包容型供应商</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>

            <div class="mt-8 flex justify-between">
              <el-button size="large" @click="activeTab = 'basic'">上一步</el-button>
              <el-button type="primary" size="large" class="px-8" @click="activeTab = 'export'">
                下一步 <el-icon class="ml-2"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- ══════════ Tab 3: 出口经验与证书 ══════════ -->
        <el-tab-pane label="第三步：出口经验与证书" name="export">
          <el-form :model="form" label-position="top" size="large">

            <!-- A. 出口经验 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">出口经验 (Export Experience)</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <el-form-item label="出口年数 (Years of Export)">
                  <el-input v-model="form.exportYears" type="number" placeholder="年" />
                </el-form-item>
                <el-form-item label="主要出口国家 (Main Export Countries)">
                  <el-input v-model="form.exportCountries" placeholder="e.g. USA, Germany, Japan" />
                </el-form-item>
              </div>
            </div>

            <!-- B. 相关证书 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">相关证书 (Certificates)</h4>
              <el-form-item label="请列出所持有的国际认证证书（如 ISO 9001, CE, FDA 等）">
                <el-input type="textarea" :rows="3" v-model="form.certificates" 
                  placeholder="每行一个证书，格式：证书名称 - 证书编号 - 有效期" />
              </el-form-item>
            </div>

            <!-- C. 主营产品/服务 -->
            <div class="mb-6">
              <h4 class="font-bold text-slate-800 mb-4 pb-2 border-b border-slate-200">主营产品/服务 (Main Products or Services)</h4>
              <el-form-item label="请详细描述您的主营产品或服务（英文）" required>
                <el-input type="textarea" :rows="5" v-model="form.mainProducts" 
                  placeholder="请尽量详细描述产品规格、应用场景、技术参数等..." />
              </el-form-item>
            </div>

            <div class="mt-8 flex justify-between">
              <el-button size="large" @click="activeTab = 'ownership'">上一步</el-button>
              <el-button type="primary" size="large" class="px-8" @click="activeTab = 'declaration'">
                下一步 <el-icon class="ml-2"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <!-- ══════════ 声明总结论（1/2/3） ══════════ -->
        <el-tab-pane label="第四步：合规声明" name="declaration">
          <el-form :model="form" label-position="top" size="large">
            <el-alert
              type="error"
              show-icon
              :closable="false"
              class="mb-6"
              title="必读：合规声明"
              description="请基于前述7项声明，选择最终结论（1/2/3）。该结论将用于审核判断。"
            />

            <div class="mb-6 p-4 rounded-lg border border-slate-200 bg-white">
              <div class="font-semibold text-slate-800 mb-2">7条声明原文（只读）</div>
              <ol class="list-decimal pl-5 space-y-1 text-xs text-slate-600 leading-6">
                <li>并非联合国系统和世界银行集团内任何组织禁止从事采购的公司或与之相关的公司或个人。</li>
                <li>未被联合国系统和世界银行集团内的任何组织列入黑名单。</li>
                <li>过去三年内，未被任何联合国会员国制裁。</li>
                <li>未宣布破产。</li>
                <li>未针对联合国实体的任何法律诉讼或与之发生纠纷。</li>
                <li>承诺不与联合国系统和世界银行集团内的任何组织进行违禁行为。</li>
                <li>关于上述任何陈述的情况变化，在被纳入联合国全球市场后，会立即通知。</li>
              </ol>
            </div>

            <div class="p-5 bg-slate-50 rounded-lg border border-slate-200">
              <div class="font-bold text-sm text-slate-900 mb-3">
                声明总选择（Declaration Summary）
              </div>
              <div class="text-xs text-slate-600 mb-3">
                1. 符合上述全部7项并承诺变更立即通知。<br />
                2. 不符合上述7项条件。<br />
                3. 不确定是否符合上述7项条件。
              </div>

              <el-radio-group v-model="form.declarationSummaryCode">
                <el-radio :label="1">1. 符合全部7项条件（Yes, fully compliant）</el-radio>
                <el-radio :label="2">2. 不符合7项条件（No, not compliant）</el-radio>
                <el-radio :label="3">3. 不确定（Not sure）</el-radio>
              </el-radio-group>
            </div>

            <div class="mt-8 p-6 bg-blue-50 rounded-lg border border-blue-200">
              <el-checkbox v-model="form.finalConfirm" class="text-sm">
                <span class="font-bold text-blue-900">
                  我确认以上所填信息真实、完整、准确，并愿意承担法律责任。
                </span>
              </el-checkbox>
            </div>

            <div class="mt-6 p-4 bg-slate-50 rounded-lg border border-slate-200">
              <el-form-item label="审核联系邮箱（可选）">
                <el-input
                  v-model="form.reviewEmail"
                  placeholder="不填则默认使用联系人邮箱"
                />
              </el-form-item>
            </div>

            <div class="mt-8 flex justify-between">
              <el-button size="large" @click="activeTab = 'export'">上一步</el-button>
              <el-button type="success" size="large" class="px-12" :loading="submitting" @click="submit">
                提交申请 <el-icon class="ml-2"><Check /></el-icon>
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- 状态页：审核中 / 审核通过 -->
    <div v-else class="bg-white p-16 rounded-2xl border border-slate-200 text-center shadow-sm">
      <div class="w-24 h-24 mx-auto rounded-full flex items-center justify-center mb-6"
        :class="applicationStatus === 'success' ? 'bg-green-50 text-green-500' : 'bg-blue-50 text-blue-500'">
        <el-icon class="text-5xl">
          <component :is="applicationStatus === 'success' ? CircleCheck : Timer" />
        </el-icon>
      </div>
      
      <h2 class="text-2xl font-bold text-slate-900 mb-2">
        {{ applicationStatus === 'pending' ? '资料已提交，正在审核中' : '审核通过' }}
      </h2>
      
      <p class="text-slate-500 mb-8 max-w-md mx-auto">
        {{ applicationStatus === 'pending' 
           ? '平台专员将在 1-3 个工作日内完成合规性预审。审核结果将通过短信通知您。' 
           : '恭喜！您的企业已成功注册为联合国供应商。请下载证书并妥善保管。' 
        }}
      </p>

      <div class="flex justify-center gap-4">
        <el-button plain size="large" @click="applicationStatus = 'none'; activeTab = 'basic'">返回修改 (演示用)</el-button>
        <el-button 
          v-if="applicationStatus === 'success'" 
          type="primary" 
          size="large" 
          class="px-8"
          @click="downloadCert"
        >
          <el-icon class="mr-2"><Download /></el-icon> 下载证书
        </el-button>
        <el-button v-else type="primary" plain size="large" @click="navigateTo('/member')">
          返回控制台
        </el-button>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Check, Timer, CircleCheck, Download } from '@element-plus/icons-vue'

definePageMeta({ layout: 'member' })

const applicationStatus = ref<'none' | 'pending' | 'success'>('none')
const activeTab = ref('basic')
const submitting = ref(false)

const activeStep = computed(() => {
  if (applicationStatus.value === 'none') return 0
  if (applicationStatus.value === 'pending') return 1
  if (applicationStatus.value === 'success') return 2
  return 0
})

const statusText = computed(() => ({
  none: '未提交',
  pending: '审核中',
  success: '审核通过'
}[applicationStatus.value]))

// 11类公司类型（完全对应PDF）
const companyTypes = [
  { value: 1, label: '1. Authorized agent (授权代理人)' },
  { value: 2, label: '2. Consulting company (咨询公司)' },
  { value: 3, label: '3. Manufacturer (生产商)' },
  { value: 4, label: '4. Trader (贸易商)' },
  { value: 5, label: '5. Software support (软件支持)' },
  { value: 6, label: '6. Services (服务)' },
  { value: 7, label: '7. Design (设计)' },
  { value: 8, label: '8. Publishing (出版社)' },
  { value: 9, label: '9. Research (研究)' },
  { value: 10, label: '10. Audit / Accounting (审计)' },
  { value: 11, label: '11. NGO (非盈利组织)' }
]

const form = ref({
  // Tab 1: 基本信息
  companyNameZh: '',
  companyNameEn: '',
  licenseNumber: '',
  companyType: null as number | null,
  establishedDate: null as string | null,
  employees: '',
  contactName: '',
  contactGender: '',
  contactTitle: '',
  email: '',
  telephone: '',
  mobile: '',
  website: '',
  fax: '',
  address: '',
  postcode: '',
  reviewEmail: '',
  
  // Tab 2: 所有权结构
  ownershipTypeCode: null as number | null, // 1-4
  privateHasAffiliates: 0, // 0无，1有

  // 私有（无/有附属）补充
  privateOwnerNameZh: '',
  privateOwnerNameEn: '',

  selfCompanyNameZh: '',
  selfCompanyNameEn: '',
  selfOwnerName: '',
  selfCeoName: '',
  selfControllingPerson: '',

  parentCompanyNameZh: '',
  parentCompanyNameEn: '',
  parentOwnerName: '',
  parentCeoName: '',
  parentControllingPerson: '',

  subCompanyNameZh: '',
  subCompanyNameEn: '',
  subOwnerName: '',
  subCeoName: '',
  subControllingPerson: '',

  // 集团补充
  groupSelfCompanyNameZh: '',
  groupSelfCompanyNameEn: '',
  groupSelfOwnerName: '',
  groupSelfCeoName: '',
  groupSelfControllingPerson: '',

  groupParentCompanyNameZh: '',
  groupParentCompanyNameEn: '',
  groupParentOwnerName: '',
  groupParentCeoName: '',
  groupParentControllingPerson: '',

  groupSubCompanyNameZh: '',
  groupSubCompanyNameEn: '',
  groupSubOwnerName: '',
  groupSubCeoName: '',
  groupSubControllingPerson: '',

  // 女性控股 & 残疾包容（按PDF数字）
  womenOwnedCode: null as number | null, // 1/2/3
  disabilityInclusionCode: null as number | null, // 1/2
  
  // Tab 3: 出口经验
  exportYears: '',
  exportCountries: '',
  certificates: '',
  mainProducts: '',
  
  // Tab 4: 7项声明
  declarationSummaryCode: null as number | null, // 1/2/3
  finalConfirm: false
})

const submit = async () => {
  // 基础校验
  if (!form.value.companyNameZh || !form.value.companyNameEn || !form.value.licenseNumber) {
    return ElMessage.warning('请完整填写公司基本信息')
  }
  if (!form.value.mainProducts) {
    return ElMessage.warning('请描述主营产品或服务')
  }
  if (!form.value.email || !form.value.mobile) {
    return ElMessage.warning('请填写联系人邮箱和手机')
  }
  if (!form.value.address || !form.value.postcode) {
    return ElMessage.warning('请填写公司地址和邮编')
  }
  
  // 7项声明校验（必须全部符合）
  if (form.value.declarationSummaryCode === null) {
    return ElMessage.warning('请选择声明总结论（1/2/3）')
  }

  if (form.value.declarationSummaryCode !== 1) {
    if (form.value.declarationSummaryCode === 2) {
      return ElMessage.error('当前声明为“不符合7项条件”，无法提交联合国注册申请')
    }
    if (form.value.declarationSummaryCode === 3) {
      return ElMessage.warning('当前声明为“不确定”，请确认后再提交')
    }
  }

  if (!form.value.finalConfirm) {
    return ElMessage.warning('请勾选最终确认')
  }

  submitting.value = true
  try {
    const payload = {
      company_name: form.value.companyNameZh || form.value.companyNameEn,
      contact_name: form.value.contactName,
      phone: form.value.mobile,
      industry: form.value.mainProducts || '',
      documents: [] as string[]
    }

    await $fetch('/api/v3/member-verifications', {
      method: 'POST',
      body: payload
    })

    applicationStatus.value = 'pending'
    ElMessage.success('申请提交成功，已进入审核队列')
  } catch (e: any) {
    ElMessage.error(e?.data?.message || e?.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleTabChange = () => true

const downloadCert = () => {
  ElMessage.success('正在下载联合国供应商证书...')
}
</script>

<style scoped>
:deep(.el-tabs__item) {
  font-size: 15px;
  height: 50px;
  line-height: 50px;
  font-weight: 600;
}
:deep(.el-alert__title) {
  font-weight: bold;
  font-size: 14px;
}
:deep(.custom-tabs .el-tabs__nav-wrap) {
  background: #f8fafc;
  padding: 0.5rem;
  border-radius: 0.75rem;
}
</style>