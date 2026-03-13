# 服务商入驻闭环修复记录（2026-03-13）

## 1. 目标

本轮修复聚焦此前审查结论中的 3 个高危点：

1. 付费入驻可被绕过，未支付也能进入审核
2. 资质/附件未结构化持久化，关键材料只停留在前端或零散 `detail_json`
3. 管理端无法完整复查申请详情、附件、证书、支付状态

本阶段已经完成高危项 1、2、3 的主链路改造，并补齐了会员侧“我的服务商申请”状态追踪闭环。

---

## 2. 已完成内容

### 2.1 支付闭环

#### 数据模型

- `supplier_onboarding` 扩展字段：
  - `apply_type`
  - `fee_amount`
  - `payment_required`
  - `payment_status`
  - `payment_order_id`
  - `payment_verified_at`
  - `submitted_snapshot_json`
  - `last_payment_check_at`
- 新增 `payment_orders` 表，承载：
  - 业务类型与业务主键
  - 支付渠道
  - 二维码链接
  - 第三方流水号
  - 回调原文
  - 过期时间
  - 支付状态

#### 后端能力

- `POST /v3/member/supplier-apply/draft`
  - 保存申请草稿
  - 创建或更新 `orders`
  - 创建 `payment_orders`
  - 返回二维码、支付单号、支付状态
- `GET /v3/member/supplier-apply/{id}/payment`
  - 查询实时支付状态
- `POST /v3/member/supplier-apply/{id}/submit`
  - 仅在后端确认已支付后允许提交审核
- `InternalPaymentsV3Controller`
  - 支持将支付回调写入 `payment_orders`
  - 同步更新 `supplier_onboarding.payment_status`
  - 同步更新平台订单状态

#### 前端能力

- `member/supplier-apply.vue`
  - 第 4 步显示真实二维码
  - “刷新支付结果”从后端读取状态
  - 未支付时禁用“提交审核”

---

### 2.2 附件/资质完整持久化

#### 数据模型

- `supplier_onboarding` 扩展：
  - `attachments_completed`
  - `certificates_completed`
- 新增 `supplier_onboarding_files`
  - 支持 `cover_image/company_pdf/promo_image/business_license/bank_license/other`
- 新增 `supplier_onboarding_certificates`
  - 支持 `ISO9001/ISO14001/ISO45001/ISO13485/IATF16949/ISO22000/ISO27001/OTHER`
  - 保存 `has_certificate/cert_no/valid_from/valid_to/issuer_name/remarks`

#### 后端能力

- `/common/upload`
  - 返回 `url/fileName/storedName/fileExt/fileSize/contentType`
- `SupplierOnboardingPaymentService.createOrUpdateDraft()`
  - 接收 `attachments[]`
  - 接收 `certificates[]`
  - 服务端强校验必传附件是否齐全
  - 服务端强校验证书声明是否完整
  - 持久化到 `supplier_onboarding_files`
  - 持久化到 `supplier_onboarding_certificates`

#### 前端能力

- 会员侧已去掉“营业执照已上传/银行许可证已上传”的假开关
- 改为真实上传：
  - 营业执照
  - 开户银行许可证
  - 宣传封面
  - 公司介绍 PDF
  - 企业宣传图
- 草稿提交时发送结构化 `attachments` 和 `certificates`

---

### 2.3 管理端复查

#### 后端能力

- `GET /v3/admin/supplier-onboarding`
  - 返回列表所需的联系人、联系方式、主营业务、支付状态、资料完整度
- `GET /v3/admin/supplier-onboarding/{id}`
  - 返回：
    - `detail`
    - `detailJson`
    - `submittedSnapshot`
    - `submittedSnapshotJson`
    - `attachments`
    - `certificates`
    - `paymentOrder`
    - `paymentVerifiedAt`
    - `lastPaymentCheckAt`
    - `attachmentsCompleted`
    - `certificatesCompleted`
- 审核流转接口继续保持“未支付不允许进入后续审核”的后端守卫

#### 前端能力

- `admin/suppliers/audit.vue` 已重写为真实复查页
- 现在可以查看：
  - 企业基本信息
  - 联系人信息
  - 结构化附件列表
  - 文件预览/下载
  - 资质证书列表
  - 支付信息
  - 提交审核时冻结的快照
- 审核动作现在基于 `supplier-onboarding` 状态机流转：
  - `submitted -> precheck_passed / precheck_failed`
  - `precheck_passed -> final_review_passed / final_review_failed`
  - `final_review_passed -> active`

### 2.4 会员侧状态闭环

#### 后端能力

- `GET /v3/member/supplier-apply`
  - 返回当前会员的申请列表
  - 包含：
    - `onboardingStatus/onboardingStatusLabel`
    - `paymentStatus/paymentVerifiedAt`
    - `contactName/contactPhone/mainService`
    - `attachmentsCompleted/certificatesCompleted`
- `GET /v3/member/supplier-apply/{id}`
  - 返回详情页所需全部数据：
    - `detail`
    - `detailJson`
    - `submittedSnapshot`
    - `submittedSnapshotJson`
    - `attachments`
    - `certificates`
    - `paymentOrder`
    - `paymentRequired`
    - `lastPaymentCheckAt`

#### 前端能力

- `member.vue`
  - 会员左侧菜单新增“我的服务商申请”
- `member/index.vue`
  - 常用工具新增“我的服务商申请”直达入口
- `member/supplier-applications.vue`
  - 支持查看申请列表、状态筛选、详情弹窗、附件预览/下载、证书表、提交快照
  - 支持查看支付二维码并刷新支付状态
  - 支持在已支付后直接提交审核，避免用户离开申请页后无法继续流程
- `member/supplier-apply.vue`
  - 提交成功后跳转到 `/member/supplier-applications`，而不是回到 `/member`

### 2.5 失败态修改后重提闭环

#### 业务规则

- `precheck_failed`、`final_review_failed` 不再要求用户新建一张申请单
- 被退回申请允许回到原单继续修改，重新进入 `draft`
- 已支付或免支付的被退回申请：
  - 保留原支付结果
  - 不重复生成新的有效支付要求
  - 允许修改资料后直接重新提交审核
- 为避免“已支付后免费切换套餐”漏洞：
  - 已支付/免支付的被退回申请暂不允许变更 `apply_type`

#### 后端能力

- `SupplierOnboardingPaymentService`
  - `findEditableDraft()` 已纳入：
    - `draft`
    - `pending_payment`
    - `precheck_failed`
    - `final_review_failed`
  - 保存失败态申请时：
    - 已支付单复用原 `payment_order`
    - `payment_status` 保持原值
    - `onboarding_status` 回到 `draft`
    - 清空旧的 `submittedAt/reviewedAt/changeReason`
  - 草稿保存新增显式 `application_id` 定向更新
    - 优先更新前端指定的申请单
    - 避免历史脏数据或多条可编辑记录并存时误改“最新那一单”
  - 支付回调新增强校验
    - 只接受 `biz_type=supplier_onboarding` 的支付单
    - 只接受当前申请正在使用的那张 `payment_order`
    - 已 `closed/refunded` 等非活跃历史支付单不允许再通过回调“复活”
  - `submitAfterPayment()` 新增后端完整性守卫
    - 附件不完整或资质不完整时，即使前端绕过也不允许提交审核
- `AdminSupplierOnboardingV3Controller`
  - 状态机允许：
    - `precheck_failed -> draft`
    - `final_review_failed -> draft`

#### 前端能力

- `member/supplier-applications.vue`
  - 失败态申请新增“修改后重提”入口
- `member/supplier-apply.vue`
  - 支持根据 `application_id` 回填原申请资料、附件、证书

### 2.6 管理端真实筛选 / 导出对齐

#### 标书管理

##### 后端能力

- `AdminTendersV3Controller`
  - 列表接口 `GET /v3/admin/tenders`
  - 导出接口 `GET /v3/admin/tenders/export`
  - 统一支持真实筛选参数：
    - `tender_status`
    - `keyword`
    - `organization`
    - `category`
    - `created_from`
    - `created_to`

##### 前端能力

- `admin/tenders/manage.vue`
  - 查询与导出统一透传：
    - 采购组织
    - 标书类别
    - 发布日期范围
    - 关键词
    - 当前 Tab 状态
  - 达成“列表怎么筛，导出就怎么导”

#### 会员管理

##### 后端能力

- `AdminMembersV3Controller`
  - 列表接口 `GET /v3/admin/members`
  - 导出接口 `GET /v3/admin/members/export`
  - 统一支持真实筛选参数：
    - `member_level`
    - `keyword`
    - `industry`
    - `invitation_code`
    - `created_from`
    - `created_to`

##### 前端能力

- `admin/members/list.vue`
  - 查询与导出统一透传：
    - 会员等级
    - 公司/联系人关键词
    - 邀请码
    - 所属行业
    - 注册时间范围
  - 移除数据库并不存在的假筛选：
    - 省份
    - 城市
    - 状态
    - 国际会员
  - 移除基于单页结果再做客户端伪过滤的旧逻辑，避免出现“页面看似筛选，实际只过滤当前页”的错误体验
  - 列表展示收敛为真实字段，不再继续强化虚构的地区/状态信息

### 2.7 公共大屏去假数据化

#### 后端能力

- `DashboardV3Controller`
  - `/v3/dashboard/stats` 在原有 totals/category/country/memberTrend 基础上新增真实统计：
    - `organizationCount`
    - `monthlyTenderCount`
    - `industryCount`
    - `svipCount / vipCount / normalCount`
    - `tendersByOrganization`
    - `memberLevels`
    - `memberCities`
    - `provinceHeat`
    - `domesticNetwork`
    - `overseasNetwork`
- 统计口径说明：
  - 会员服务城市分布：通过 `member_profile -> sys_user.partner_id -> partners.city_name` 聚合
  - 国内服务网络：读取 `partners.status=1`
  - 国际服务网络：读取 `overseas_points.status='active'`

#### 前端能力

- `dashboard.vue`
  - 删除 `localStorage('ipp-dashboard-config')` 对公共大屏的本地覆盖入口，避免历史假配置污染实时数据
  - Page 1：
    - “联合国采购数据” 改为真实“平台采购数据”
    - 采购机构滚动榜与机构分布图改为读取真实 `tendersByOrganization`
    - 中国热力图改为读取真实 `provinceHeat`
  - Page 2：
    - 会员顶部摘要改为真实 `SVIP/VIP/本月新增/行业覆盖`
    - 饼图改为真实会员等级占比
    - 城市排名与地图改为真实会员服务城市分布
  - Page 3：
    - 国内服务网络改为真实 `partners`
    - 国际服务网络改为真实 `overseas_points`
    - 世界小图改为真实海外点位坐标散点，不再依赖伪国家热力

#### 后台说明

- `admin/dashboard/network.vue`
  - 明确标记为“功能规划中”
  - 当前仅保留为未来配置原型
  - 不再伪装成已接后端、可影响公共大屏的真实配置页

### 2.8 首页公开入口去假数据化

#### 前端能力

- `app/pages/index.vue`
  - “最新采购商机”改为真实 `tenders` 切片展示
  - 删除 `TMP-${Math.random()}` 生成的假补位标书
  - “查看全部”按钮显示真实标书总量
  - 首页统计卡片第 4 项改为真实 `organizationCount`
- “平台服务商风采”改为读取 `/v3/suppliers?page=1&page_size=10`
  - 展示真实企业名称、主营服务、服务商级别、公开封面/宣传图
  - 当公开服务商为空时，显示诚实空态文案，不再渲染静态假企业卡片

#### 业务意义

- 首页入口区是用户最先看到的公共页面，继续保留伪造的标书补位和企业风采，会直接损害整站可信度
- 这轮处理后，首页已从“真实接口 + 假补位素材”的混合态，收敛为以真实公开数据为主的展示页

### 2.9 回调分流修正

- `InternalPaymentsV3Controller`
  - 内部支付回调优先识别活动报名 `registration_id`
  - 仅在未携带 `registration_id` 时，才按供应商入驻支付单回调处理
  - 避免未来其他支付业务带 `order_no` 时被错误分流到供应商支付逻辑

### 2.10 服务页平台数据去假化

#### 前端能力

- `app/pages/services.vue`
  - 平台介绍区四张统计卡片改为真实统计：
    - `memberCount`
    - `organizationCount`
    - `countryCount`
    - `domesticNetwork + overseasNetwork`
  - 顶部滚动通知条改为展示真实活动标题，不再使用静态报名文案
  - “47 个服务中心”“35 个城市服务中心”改为根据真实服务点位动态展示
  - 服务商筛选从静态省份映射改为真实城市筛选，候选取自 `domesticNetwork.cityPoints`
  - 城市服务点弹窗改为展示真实城市点位数量与最近接入点位，不再展示虚构负责人/热线/地址
  - 服务商详情弹窗移除假联系电话，改为显示真实服务商级别、主营业务和公开 PDF 状态
  - 活动报名在登录态下不再自动回填假公司/联系人/手机号，改为空表单由用户自行填写

#### 业务意义

- `services.vue` 是公共流量入口之一，平台规模与活动动态如果继续使用硬编码数字，会与前面已去假的首页/大屏形成新的口径冲突
- 本轮处理后，服务页最显眼的规模型数字已经与公共大屏统一到同一组后端统计来源
- 同时，服务页里几处“看似能用、实际靠前端静态拼接”的交互也已收口，避免用户把演示数据误认为真实服务网络与真实客户资料

### 2.11 后台服务商名录字段纠偏

#### 前端能力

- `app/pages/admin/suppliers/list.vue`
  - 列表项联系人从错误的 `cityName` 改为真实 `contactName`
  - 列表项联系电话从错误的 `onboardingStatusLabel` 改为真实 `contactPhone`
  - 行业/主营业务展示改为读取真实 `mainService/serviceTypes`
  - 状态筛选从笼统的“审核未通过”改为明确区分：
    - `precheck_failed`
    - `final_review_failed`
  - 详情弹窗改为调用 `GET /v3/admin/supplier-onboarding/{id}`，读取真实驳回原因、提交时间、审核时间

#### 业务意义

- 该页属于后台运营常用入口，错误字段映射会直接误导审核与运营判断
- 这轮处理后，后台服务商名录页至少不会再把城市名当联系人、把状态标签当手机号展示

---

## 3. 本轮涉及的关键文件

### DDL

- `docs/DDL_Phase11_SupplierOnboarding_Payment.sql`
- `docs/DDL_Phase12_SupplierOnboarding_FilesAndCertificates.sql`

### 后端

- `ipp-backend/src/main/java/com/xripp/backend/service/SupplierOnboardingPaymentService.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/CommonV3Controller.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/MemberSupplierApplyV3Controller.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/InternalPaymentsV3Controller.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminSupplierOnboardingV3Controller.java`

### 前端

- `app/pages/dashboard.vue`
- `app/composables/usePlatformStats.ts`
- `app/pages/admin/dashboard/network.vue`
- `app/pages/index.vue`
- `app/pages/services.vue`
- `app/pages/admin/suppliers/list.vue`
- `app/pages/member/supplier-apply.vue`
- `app/pages/member/supplier-applications.vue`
- `app/layouts/member.vue`
- `app/pages/member/index.vue`
- `app/pages/admin/suppliers/audit.vue`
- `app/pages/admin/audit.vue`

---

## 4. 验证结果

### 已执行

- 前端原地验证（`/mnt/d/ipp-platform`）
  - `npm ci` 失败
  - 原因 1：`/mnt/d` 挂载盘不支持 npm 安装阶段的 `chmod`
  - 原因 2：当前 WSL 自带 Node 为 `18.19.1`，不满足 Nuxt 4 / Vite 7 所需的 `20.19+`

- 前端隔离验证（Linux 原生目录 + Node 20.19.0）
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm ci`
    - `npm run build`
  - 结果：通过
  - 结论：当前前端代码可构建，之前失败主要来自 WSL 节点版本与 `/mnt/d` 文件权限语义

- 本轮“真实筛选 / 真实导出对齐”补丁复验
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm run build`
  - 结果：通过

- 本轮“公共大屏去假数据化”补丁复验
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm run build`
  - 结果：通过

- 本轮“首页去假数据化”补丁复验
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm run build`
  - 结果：通过
  - 备注：存在 chunk size warning，但属于构建体积提示，不影响本轮功能正确性

- 本轮“服务页平台数据去假化”补丁复验
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm run build`
  - 结果：通过
  - 备注：存在 chunk size warning，但属于构建体积提示，不影响本轮功能正确性

- 本轮“后台服务商名录字段纠偏”补丁复验
  - 在 `/tmp/ipp-platform-buildcheck` 执行：
    - `npm run build`
  - 结果：通过
  - 备注：存在 chunk size warning，但属于构建体积提示，不影响本轮功能正确性

- 后端隔离验证（Linux 原生目录 + 本地解包 JDK 17）
  - 在 `/tmp/ipp-backend-buildcheck` 执行：
    - `./mvnw -q test`
  - 结果：测试已真正启动并进入 Spring / MyBatis / Hikari / SQL Server 连接阶段
  - 当前失败点：
    - `DataScopeRealWorldTest`
    - `localhost:1433` SQL Server 拒绝连接
  - 结论：`JAVA_HOME` 问题已绕过，当前阻断转为“测试依赖数据库服务未启动/不可达”

- 本轮“真实筛选 / 真实导出对齐”补丁复验
  - 在 `/tmp/ipp-backend-buildcheck` 执行：
    - `./mvnw -q -DskipTests compile`
  - 结果：通过

- 本轮“公共大屏去假数据化”补丁复验
  - 在 `/tmp/ipp-backend-buildcheck` 执行：
    - `./mvnw -q -DskipTests compile`
  - 结果：通过

### 当前结论

- 这轮改动的业务链路已经完成主干接通
- 前端构建已在正确 Linux/Node 环境下验证通过
- 本次“失败态修改后重提”补丁已单独复跑前端构建，通过
- 后端 Java 编译已单独复跑，通过
- 本次“真实筛选 / 真实导出对齐”补丁已单独复跑前端构建与后端编译，通过
- 本次“公共大屏去假数据化”补丁已单独复跑前端构建与后端编译，通过
- 本次“首页去假数据化”补丁已单独复跑前端构建，通过
- 本次“服务页平台数据去假化”补丁已单独复跑前端构建，通过
- 本次“后台服务商名录字段纠偏”补丁已单独复跑前端构建，通过
- 后端测试已可执行，但仍依赖本地 SQL Server（`localhost:1433`）可用后才能完成全量验证

---

## 5. 仍待继续的事项

### P1

- 管理端列表/统计进一步利用附件与证书结构化数据
  - 例如按资料完整度筛选
  - 按证书类型筛选

### P1.5 已补充：公开名录数据投射

#### 后端能力

- `SuppliersV3Controller`
  - 公开名录列表继续只读取 `supplier_onboarding.onboarding_status='active'`
  - 新增后端筛选参数：
    - `keyword`
    - `city`
    - `service_type`
    - `apply_type`
  - 列表/详情返回真实公开字段：
    - `applyType/applyTypeLabel`
    - `mainService/mainServiceLabel`
    - `industryList`
    - `isoList`
    - `intro`
    - `coverImageUrl`
    - `promoImageUrl`
    - `companyPdfUrl`
  - 公开附件严格限定为：
    - `cover_image`
    - `promo_image`
    - `company_pdf`
  - 不向公开名录暴露营业执照、银行许可证等审核附件

#### 前端能力

- `supplier-directory.vue`
  - 去除客户端伪筛选与假字段
  - 直接使用 `/v3/suppliers` 的真实分页和筛选结果
  - 新增真实筛选：
    - 城市
    - 服务项目
    - 服务商级别（普通/战略）
  - 列表支持展示真实封面图、主营业务、战略级标识、真实简介
  - 详情弹窗改为展示真实公开资料，不再展示假联系人/邮箱/厂房等占位信息
  - “下载介绍PDF” 已接入真实 `companyPdfUrl`

#### 验证

- `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过
- `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过

### P2

- 继续盘点首页剩余静态宣传位
  - 当前广告位、友情链接等仍属静态运营内容
  - 这类内容不等于“伪业务数据”，但后续仍建议明确是否要接 CMS 或后台配置

- 继续盘点 `services.vue` 剩余静态内容
  - 当前培训课程、城市筛选候选项、资源卡片仍多为运营配置
  - 它们不等于伪业务数据，但后续仍建议明确是否接 CMS / 字典 / 后台配置

- 接真实微信支付商户能力
  - 当前二维码和回调链路已具备平台内闭环
  - 但仍是内部模拟支付地址，不是正式第三方商户接入

- 修复重复提交边界
  - 当前已阻挡 `submitted/precheck_passed/final_review_passed/active`
  - 仍建议结合实际业务明确是否允许某些失败态重提

---

## 6. 下一步建议

下一阶段建议按以下顺序继续：

1. 继续盘点高可见公共页，优先清理仍会被误认为“实时业务数据”的静态大数、榜单、企业卡片
2. 在 WSL 中固定前端验证基线
   - Node `20.19+`
   - 避免直接在 `/mnt/d` 挂载盘执行 `npm ci`
3. 启动或接通本地 SQL Server 测试实例（`localhost:1433`）
4. 在数据库可达后重新执行 `./mvnw -q test`
