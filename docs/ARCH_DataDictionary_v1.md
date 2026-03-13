# ARCH Data Dictionary v1

## 1. Canonical Enums

### 1.1 UserRole
- `admin` 总部管理员/审核员
- `partner` 合伙人
- `member` 前台会员

### 1.2 MemberLevel
- `NORMAL`
- `VIP`
- `SVIP`

### 1.3 AuditStatus (统一审核状态码，沿用 Phase1)
- `0` DRAFT（草稿）
- `10` PENDING_L1（待一审）
- `20` PENDING_L2（待二审）
- `30` APPROVED（通过）
- `40` REJECTED（驳回）
- `50` OFFLINE（下架/关闭）

### 1.4 PaymentStatus
- `UNPAID`
- `PAID`
- `REFUND_PENDING`
- `REFUNDED`
- `FAILED`
- `CLOSED`

> 说明：上述为早期通用支付语义。当前服务商入驻链路已落地的运行值采用小写业务状态：
- `pending`
- `paid`
- `waived`
- `failed`
- `closed`

### 1.4.1 SupplierApplyType
- `normal`
- `strategic`

### 1.4.2 SupplierOnboardingPaymentStatus
- `unpaid`
- `pending`
- `paid`
- `waived`
- `failed`
- `closed`

### 1.4.3 SupplierOnboardingFileCategory
- `cover_image`
- `company_pdf`
- `promo_image`
- `business_license`
- `bank_license`
- `other`

### 1.4.4 SupplierOnboardingCertificateType
- `ISO9001`
- `ISO14001`
- `ISO45001`
- `ISO13485`
- `IATF16949`
- `ISO22000`
- `ISO27001`
- `OTHER`

### 1.5 OrderType
- `MEMBER_ORDER` 会员订单
- `SERVICE_ORDER` 服务订单
- `TENDER_ORDER` 标书订单
- `ACTIVITY_ORDER` 活动报名订单

### 1.6 ContentType
- `ACTIVITY`
- `TRAINING`
- `MEDIA`
- `AD`

---

## 2. Canonical Field Names (API)

### 2.1 公司与联系人
- `companyNameZh`
- `companyNameEn`
- `licenseNumber`
- `companyType`
- `establishedDate`
- `employeeCount`
- `contactName`
- `contactGender`
- `contactTitle`
- `contactEmail`
- `mobile`
- `telephone`
- `fax`
- `website`
- `address`
- `postcode`
- `reviewEmail`

### 2.1.1 服务商入驻扩展字段
- `applyType`
- `serviceTypes`
- `productDesc`
- `attachments`
- `certificates`
- `paymentStatus`
- `paymentOrderId`
- `paymentVerifiedAt`
- `attachmentsCompleted`
- `certificatesCompleted`
- `submittedSnapshot`

### 2.2 UN 申请扩展
- `ownershipTypeCode`
- `privateHasAffiliates`
- `womenOwnedCode`
- `disabilityInclusionCode`
- `declarationSummaryCode`
- `exportYears`
- `exportCountries`
- `certificates`
- `mainProducts`

---

## 3. 兼容映射（旧字段 -> 新字段）

- `companyNameCn` -> `companyNameZh`
- `employees` -> `employeeCount`
- `contactPosition` -> `contactTitle`
- `contactPhone` -> `mobile`
- `contactEmail` -> `contactEmail`（保留）
- `yearEstablished` -> `establishedDate`
- `ownership` -> `ownershipTypeCode`
- `womenOwnership` -> `womenOwnedCode`
- `disabilityInclusion` -> `disabilityInclusionCode`
- `declaration` -> `declarationSummaryCode`
- `exportExperience` -> `exportYears`
- `mainExportCountries` -> `exportCountries`

---

## 4. 约束

- 前后台只允许使用本文件枚举值，不允许新增同义值。
- 前端展示文案可中文化，但提交/接口值必须使用 canonical 值。
- 新增字段必须先更新本文件，再改 DDL/API/前端。
