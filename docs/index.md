# XRIPP 文档索引

本文件是 `docs/` 目录的统一入口，用于：
- 快速定位需求来源、设计约束、接口契约和执行计划
- 明确"当前权威文档"与"历史参考文档"
- 减少前后端联调时的路径错误和口径不一致

最后更新：2026-03-14（第五十二轮：出海发布系统真实化）

## 1. 当前权威文档（开发与联调优先参考）

### 架构与契约
- [API_Contract_v3.0.md](./API_Contract_v3.0.md) — V3 API 响应格式
- [ARCH_StateMachine_v1.md](./ARCH_StateMachine_v1.md) — 状态机定义
- [ARCH_DataDictionary_v1.md](./ARCH_DataDictionary_v1.md) — 数据字典（枚举值）
- [Page_API_Mapping_v1.md](./Page_API_Mapping_v1.md) — 页面-API 映射
- [AUDIT_Full_System_2026-02-28.md](./AUDIT_Full_System_2026-02-28.md) — 全面系统审计报告

### DDL 脚本（按顺序执行）
| 序号 | 文件 | 说明 |
|---|---|---|
| 1 | [DDL_Phase1_Pure.sql](./DDL_Phase1_Pure.sql) | 10 张核心表 + sys_dict 种子（可执行版；DDL_Phase1_Final.sql 为 Markdown 参考） |
| 2 | [DDL_Phase2_Migration.sql](./DDL_Phase2_Migration.sql) | 7 张业务扩展表 + 列扩展（幂等） |
| 3 | [DDL_Phase3_Partners_Extension.sql](./DDL_Phase3_Partners_Extension.sql) | partners 生命周期字段（幂等） |
| 4 | [DDL_Phase4_ScheduledTasks.sql](./DDL_Phase4_ScheduledTasks.sql) | partners.renewal_reminder_sent |
| 5 | [DDL_Phase5_Suppliers_Extension.sql](./DDL_Phase5_Suppliers_Extension.sql) | suppliers 扩展列（service_type/contact等） |
| 6 | [DDL_Phase6_Demands_Extension.sql](./DDL_Phase6_Demands_Extension.sql) | demands 表字段扩展（8 列，幂等） |
| 7 | [DDL_Phase7_Favorites.sql](./DDL_Phase7_Favorites.sql) | user_favorites 收藏表（幂等） |
| 8 | [DDL_Phase8_Contents_Carousel.sql](./DDL_Phase8_Contents_Carousel.sql) | contents 表 cover_image + carousel 类型扩展 |
| 9 | [DDL_Phase9_Overseas_Points.sql](./DDL_Phase9_Overseas_Points.sql) | overseas_points 表 + 39 条种子数据 |
| 10 | [DDL_Phase10_SupplierOnboarding_Extension.sql](./DDL_Phase10_SupplierOnboarding_Extension.sql) | supplier_onboarding 增加 user_id + detail_json |
| 11 | [DDL_Phase11_SupplierOnboarding_Payment.sql](./DDL_Phase11_SupplierOnboarding_Payment.sql) | supplier_onboarding 支付闭环扩展 + `payment_orders` |
| 12 | [DDL_Phase12_SupplierOnboarding_FilesAndCertificates.sql](./DDL_Phase12_SupplierOnboarding_FilesAndCertificates.sql) | 附件/证书结构化表 + 完整性标记 |
| 13 | [DDL_Seed_Data.sql](./DDL_Seed_Data.sql) | **测试账号 + 业务种子数据（幂等）** |
| 14 | [DDL_Phase14_Activities_Closure.sql](./DDL_Phase14_Activities_Closure.sql) | activities 运营字段扩展 + 编辑/上下架闭环 |
| 15 | [DDL_Phase15_ActivityDisplayApplications.sql](./DDL_Phase15_ActivityDisplayApplications.sql) | 活动显示申请表 + 审核/上下线闭环 |
| 16 | [DDL_Phase16_Tenders_PublishedAt.sql](./DDL_Phase16_Tenders_PublishedAt.sql) | tenders `published_at` 补列 + 回填 + 发布时间索引 |
| 17 | [DDL_Phase17_ActivityRecords.sql](./DDL_Phase17_ActivityRecords.sql) | 活动记录表 + 现场照片结构化存储 |
| 18 | [DDL_Phase18_Contents_ExtraJson.sql](./DDL_Phase18_Contents_ExtraJson.sql) | contents `extra_json` 扩展字段，正文/元数据分离 |
| 19 | [DDL_Phase19_ProfitSharing.sql](./DDL_Phase19_ProfitSharing.sql) | 合伙人利润分成配置 + 月度结算表 |
| 20 | [DDL_Phase20_AdminConfigs.sql](./DDL_Phase20_AdminConfigs.sql) | 后台通用配置表，首批承接定价配置 |
| 21 | [DDL_Phase21_CustomerService.sql](./DDL_Phase21_CustomerService.sql) | 客服系统核心表：留言 / 工单 / 工单附件 |
| 22 | [DDL_Phase22_Notifications.sql](./DDL_Phase22_Notifications.sql) | 通知中心核心表：类型设置 / 模板 / 发送记录 |
| 23 | [DDL_Phase23_SystemPermissions_RBAC.sql](./DDL_Phase23_SystemPermissions_RBAC.sql) | 系统权限中心最小 RBAC 底座：权限档案 / 模块授权 / 登录模式 / `sys_user.permission_profile_id` |
| 24 | [DDL_Phase24_Overseas_Realization.sql](./DDL_Phase24_Overseas_Realization.sql) | 出海发布系统真实化：`overseas_services` / `overseas_service_files` / `overseas_reports` + `overseas_management` 权限扩展 |

### 执行计划
- [Execution_Week1_Plan.md](./Execution_Week1_Plan.md)
- [Roadmap_Week2_Week4.md](./Roadmap_Week2_Week4.md)
- [Mock_Removal_Execution_Checklist.md](./Mock_Removal_Execution_Checklist.md)
- [API_Implementation_Plan_v1.md](./API_Implementation_Plan_v1.md)
- [SupplierOnboarding_Closure_2026-03-13.md](./SupplierOnboarding_Closure_2026-03-13.md) — 服务商入驻闭环修复阶段记录
- [ActivityContent_Closure_2026-03-13.md](./ActivityContent_Closure_2026-03-13.md) — 活动内容管理写闭环阶段记录
- [ActivitySignup_Closure_2026-03-13.md](./ActivitySignup_Closure_2026-03-13.md) — 活动报名管理/导出闭环阶段记录
- [ActivityDisplay_Closure_2026-03-13.md](./ActivityDisplay_Closure_2026-03-13.md) — 活动显示申请/审核闭环阶段记录
- [ActivityRecord_Closure_2026-03-13.md](./ActivityRecord_Closure_2026-03-13.md) — 活动记录闭环阶段记录
- [ContentMeta_Closure_2026-03-13.md](./ContentMeta_Closure_2026-03-13.md) — contents 正文/元数据分离 + 媒体/培训编辑闭环
- [PartnerPublish_Closure_2026-03-13.md](./PartnerPublish_Closure_2026-03-13.md) — 合伙人活动发布入口真实编辑/上传闭环
- [TrainingPage_Truthfulness_2026-03-13.md](./TrainingPage_Truthfulness_2026-03-13.md) — 培训页去伪能力、回到真实内容管理边界
- [MemberCrud_Closure_2026-03-13.md](./MemberCrud_Closure_2026-03-13.md) — 会员管理真实 CRUD 闭环修复记录
- [RuntimeRecovery_S0S1_2026-03-13.md](./RuntimeRecovery_S0S1_2026-03-13.md) — 运行实例漂移 + schema 漂移恢复方案
- [Claude_Windows_Execution_Runbook_2026-03-13.md](./Claude_Windows_Execution_Runbook_2026-03-13.md) — Claude Windows 环境恢复执行手册
- [SQL_Runtime_Schema_Check_2026-03-13.sql](./SQL_Runtime_Schema_Check_2026-03-13.sql) — 关键表/列只读自检脚本
- [TenderPublishDate_Closure_2026-03-13.md](./TenderPublishDate_Closure_2026-03-13.md) — 标书发布时间口径/DDL 留痕修复记录
- [ProfitSharing_Closure_2026-03-13.md](./ProfitSharing_Closure_2026-03-13.md) — 合伙人利润分成配置/统计/结算真实闭环
- [PricingConfig_Closure_2026-03-13.md](./PricingConfig_Closure_2026-03-13.md) — 后台通用配置底座 + 定价配置真实闭环
- [BusinessConfig_Closure_2026-03-14.md](./BusinessConfig_Closure_2026-03-14.md) — 业务配置三页复用通用配置底座完成真实化
- [SystemConfig_Stage1_Closure_2026-03-14.md](./SystemConfig_Stage1_Closure_2026-03-14.md) — system 配置型页面第一阶段真实化（settings/login-config）
- [SystemCertificates_Closure_2026-03-14.md](./SystemCertificates_Closure_2026-03-14.md) — 证书模板管理真实化（上传走文件服务，元数据走配置底座）
- [CustomerService_Closure_2026-03-14.md](./CustomerService_Closure_2026-03-14.md) — 客服系统真实化（留言/工单/附件/后台处理闭环）
- [Notifications_Closure_2026-03-14.md](./Notifications_Closure_2026-03-14.md) — 通知中心真实化（通知类型/模板/发送记录闭环）
- [Logs_Closure_2026-03-14.md](./Logs_Closure_2026-03-14.md) — 操作日志真实化（audit_logs + state_transition_logs 聚合查询）
- [SystemPermissions_Phase1_Closure_2026-03-14.md](./SystemPermissions_Phase1_Closure_2026-03-14.md) — 系统权限中心 Phase 1：最小 RBAC 数据底座 + 接口 + 请求链路骨架
- [SystemPermissions_Phase2_Closure_2026-03-14.md](./SystemPermissions_Phase2_Closure_2026-03-14.md) — 系统权限中心 Phase 2：前端权限页接入 + 菜单/路由联动
- [OverseasPublishing_Closure_2026-03-14.md](./OverseasPublishing_Closure_2026-03-14.md) — 出海发布系统真实化：services / reports / analysis 三页去 session-only
- [UAT_Alpha_TestPlan_2026-03-13.md](./UAT_Alpha_TestPlan_2026-03-13.md) — 第一轮 Alpha UAT 测试范围与前置检查表
- [UAT_Alpha_Checklist_2026-03-13.md](./UAT_Alpha_Checklist_2026-03-13.md) — 同事执行版测试清单
- [TestEnv_Preflight_2026-03-13.md](./TestEnv_Preflight_2026-03-13.md) — 测试环境快速检查表
- [Bug_Template_2026-03-13.md](./Bug_Template_2026-03-13.md) — 缺陷登记模板

## 2. 历史需求与参考文档（用于追溯，不直接覆盖权威契约）

目录：[`docs/requirements`](./requirements)

- [早期规划V5.0_需求文档.md](./requirements/早期规划V5.0_需求文档.md)
- [早期规划前台管理功能清单.md](./requirements/早期规划前台管理功能清单.md)
- [早期规划后台管理功能清单.md](./requirements/早期规划后台管理功能清单.md)
- [前后台功能串联分析.txt](./requirements/前后台功能串联分析.txt)
- [早期规划前台管理思维导图备注.md](./requirements/早期规划前台管理思维导图备注.md)
- [早期规划后台管理思维导图备注.md](./requirements/早期规划后台管理思维导图备注.md)
- [早期规划思维导图_前台页面.pdf](./requirements/早期规划思维导图_前台页面.pdf)
- [早期规划思维导图_后台管理.pdf](./requirements/早期规划思维导图_后台管理.pdf)
- [联合国企业信息审核表.pdf](./requirements/联合国企业信息审核表.pdf)
- [API_Specs_V2.0.md](./API_Specs_V2.0.md)（历史技术规范参考，非 v3 接口权威契约）

## 3. 文档使用规则（强制）

1. 新功能开发、联调、验收：优先使用"当前权威文档"。
2. 历史需求文档仅用于追溯，不直接覆盖当前接口与状态机定义。
3. 若文档与代码冲突：先以代码实际行为为准，补充文档修订记录后再统一。
4. 路由权限基线：
   - `member` -> `/member`
   - `partner` -> `/admin/partner-publish`、`/admin/members`（只读本partner下会员）、`/admin/suppliers`（只读本partner下服务商）
   - `admin` -> `/admin`（全量数据）
5. 所有用户身份必须来自 `SecurityContextHolder`，禁止从请求参数透传 `user_id` 作为权限依据。
6. v3 统一响应字段：`code/message/data/request_id/timestamp`，不使用 `msg` 作为 v3 标准字段。
7. 会员等级统一枚举：`NORMAL | VIP | SVIP`。

## 4. 开发阶段完成记录

### Phase A — 实体补全 + 安全修复（2026-02-28 ✅）
- ✅ 7 张 Phase1 表 Entity/Mapper/Service 全部创建（21 文件）
- ✅ 4 个 Entity 补全 DDL 审计字段（OrderEntity/ActivityRegistration/MemberVerification/Suppliers）
- ✅ order_type 枚举统一为小写（匹配 DDL CHECK）
- ✅ 安全漏洞×4 全部修复：SupplierOnboarding scope 违规、Tenders PUT 绕过状态机、明文密码降级、action 空校验

### Phase B — 后端 API 扩展（2026-02-28 ✅）
- ✅ AdminPartnersV3Controller、BenefitPoolV3Controller、MemberProfileV3Controller 新建
- ✅ OrdersV3Controller 完善（全 CRUD + user_id 隔离）
- ✅ TendersV3Controller 增强（下载追踪）
- ✅ AdminDemandsV3Controller、AdminContentsV3Controller 创建 + POST create
- ✅ PartnerPublishesV3Controller review 端点

### Phase C — 前端 API 接入 + Mock 清理（2026-02-28 ✅）
- ✅ 删除 useMockData.ts、useServiceProviders.ts 等 mock 文件
- ✅ member/index、member/orders、member/settings 接入真实 API
- ✅ admin/members/list、admin/partners/list 接入真实 API
- ✅ services.vue、index.vue 首页接入真实 API
- ✅ audit.vue 三 Tab 均接入真实 API

### Phase D — 文件上传 + 定时任务 + 审计强化（2026-02-28 ✅）
- ✅ JWT Cookie 降级：JwtAuthenticationFilter 支持 xripp_token Cookie（el-upload 适配）
- ✅ 文件上传：CommonV3Controller POST /common/upload + 扩展名/大小校验 + UUID 存储
- ✅ 静态资源：WebMvcConfig /uploads/** 映射 + SecurityConfig permitAll
- ✅ Multipart 配置：50MB 文件 / 60MB 请求
- ✅ @EnableScheduling + SystemScheduledTasks（合伙人续期提醒 02:00 / 会员自动降级 04:00）
- ✅ 审计强化：4 个 review 端点写入 audit_logs
- ✅ AdminMembersV3Controller 统一使用 StateTransitionService.log()
- ✅ 内容发布 different-actor 规则（approved→published 不允许同一人操作）

### Phase E — Partner 数据隔离 + DDL 全量执行（2026-02-28 ✅）
- ✅ AdminMembersV3Controller 开放 partner 角色（list/detail 按 sys_user.partner_id 隔离）
- ✅ AdminSupplierOnboardingV3Controller 开放 partner 角色（list/detail/stats 按 partner_id 隔离）
- ✅ AdminMemberVerificationV3Controller 开放 partner 角色（list 按 sys_user.partner_id 隔离）
- ✅ 写操作（set-level/transition/review）保持 admin-only
- ✅ DDL Phase 1-5 + 种子数据已通过 sqlcmd 全量执行确认
- ✅ BCrypt 密码修复（原 plaintext "123456" 全部替换为 BCrypt hash）

### Phase F — P0 安全/数据/分页修复（2026-03-02 ✅）
- ✅ CSRF 修复：Cookie 回退限定为仅 /common/upload 端点（原全局回退有跨站请求伪造风险）
- ✅ Demand 字段补齐：DDL Phase 6 增加 8 列 + Entity 更新 + AdminDemandsV3Controller 去除占位值
- ✅ MemberDemandsV3Controller 新建：POST /v3/member/demands + GET + DELETE（前端 publish-demand.vue 接入）
- ✅ 标书详情页（procurement/[id].vue）接入 GET /v3/tenders/{id}，支持 SSR（useAsyncData）
- ✅ 假分页系统性修复：9 个页面从 page=1&page_size=200+slice() 改为真实服务端分页
  - admin/tenders: manage, orders, reference
  - admin/suppliers: list, audit
  - admin/members: un-audit
  - admin/content: activities, trainings
  - supplier-directory.vue

### Phase G — P1 订单/收藏/活动搜索修正（2026-03-02 ✅）
- ✅ activities.vue 搜索修正：keyword/publisher 传后端，移除客户端过滤，statusMap 修正（{1,0,2,3}→{30,0,40,50}）
- ✅ PartnerPublishesV3Controller 增加 keyword/publisher 查询参数（LIKE title + partner_id 过滤）
- ✅ AdminOrdersV3Controller 增强：member_profile JOIN（companyName/contactPerson/contactPhone/industry）
- ✅ AdminOrdersV3Controller 新增：GET /stats（按状态聚合统计）+ POST /（管理员创建订单）
- ✅ admin/members/orders.vue 接入真实 API（列表/统计/创建/完成/取消全链路）
- ✅ DDL Phase 7：user_favorites 表 + 唯一约束 + 索引
- ✅ UserFavoritesV3Controller 新建：GET list + GET /ids + POST add + DELETE remove（支持 tender/activity/supplier 三类）
- ✅ member/favorites.vue 从硬编码 mock 改为 API 接入
- ✅ procurement/index.vue + [id].vue 从 localStorage 改为后端 API 收藏
- ✅ DDL Phase 6 执行确认（demands 8 列已在数据库中存在）

### Phase H — 前端 Mock 清理 Batch 3-4 + 降级标注（2026-03-12 ✅）
- ✅ `services.vue`：hardcoded newsList 改为 `/v3/contents?content_type=media` API 加载
- ✅ `media/[id].vue`：3 篇静态文章改为 `/v3/contents/{id}` API + loading/error 状态
- ✅ `experts.vue`：移除假专家数据，标注"专家智库功能即将上线"
- ✅ `admin/finance/profit.vue`：统计卡片接入 `/v3/admin/orders/stats` + `/v3/admin/partners`
- ✅ 后端新增 ContentsV3Controller（`/v3/contents` 公共读取，仅返回 published 内容）
- ✅ SecurityConfig 添加 `/v3/contents/**` 到 permitAll
- ✅ 18 个暂无后端的 admin 页面添加"功能开发中"降级标注横幅：
  - `admin/overseas/*`（4 页）、`admin/system/*`（9 页）、`admin/business/*`（3 页）、`admin/finance/pricing.vue`（1 页）

### Phase I — P0 会员注册全栈 + 质量审计（2026-03-12 ✅）
- ✅ 后端：`POST /v3/auth/register` — @Transactional 原子操作（7 步流程）
  - SMS 验证码校验（开发期 123456）、手机号唯一性、invitation_code→partner_id 绑定
  - BCrypt 密码哈希、sys_user + member_profile 同步创建、JWT 即时签发
  - TOCTOU 并发注册保护（DataIntegrityViolationException 捕获）
- ✅ 前端：`login.vue` 会员区域重构为 Login/Register 双 Tab
  - 注册表单：手机号 + 验证码（60s 倒计时） + 密码 + 公司名称（必填） + 邀请码（选填）
  - 注册成功自动登录并跳转 `/member`
  - 支持 `?mode=register` URL 参数直接打开注册 Tab
- ✅ 质量审计修复：
  - ContentsV3Controller：SimpleDateFormat→DateTimeFormatter（线程安全）+ safeOr() 空值一致性
  - login.vue：SMS timer onBeforeUnmount 清理（内存泄漏）+ 三表单独立 submitting ref
  - SecurityConfig：`/v3/auth/register` 加入 permitAll

### Phase J — 标书分析脱 Mock + 供应商入驻全栈（2026-03-12 ✅）
- ✅ `admin/tenders/analysis.vue`：移除 `generateMockData()`（240 条随机记录），改为 `GET /v3/admin/tenders/analysis`
  - 后端新增 `/analysis` 端点：返回 published 标书的 organization/category/publishDate 轻量记录
  - 前端组织筛选器改为动态（从真实数据提取去重），同比/环比从真实数据计算（原 yoy 硬编码）
  - 导出报表从空操作改为真实 CSV 导出
- ✅ `member/supplier-apply.vue`：移除 `setTimeout` 假提交，改为 `POST /v3/member/supplier-apply`
  - DDL Phase 10：supplier_onboarding 增加 user_id + detail_json 列
  - 后端 MemberSupplierApplyV3Controller：会员自助入驻（防重复提交 + 状态审计）
  - 前端 30+ 表单字段映射为 company_name + city + service_types_json + detail_json（结构化 JSON）

### Phase K — 服务商入驻闭环修复（2026-03-13 ✅）
- ✅ 支付闭环：
  - DDL Phase 11：`supplier_onboarding` 增加支付字段，新增 `payment_orders`
  - 后端：草稿保存即创建平台订单 + 支付单 + 二维码
  - 后端：支付回调写入 `payment_orders`，未支付不允许提交审核
  - 前端：`member/supplier-apply.vue` 第 4 步改为真实二维码 + 刷新支付状态
- ✅ 附件/资质结构化：
  - DDL Phase 12：新增 `supplier_onboarding_files`、`supplier_onboarding_certificates`
  - 上传接口返回文件名/大小/扩展名等元数据
  - 会员端真实上传营业执照、开户许可证、封面、PDF、宣传图
  - 草稿提交落库结构化附件与证书，不再只依赖 `detail_json`
- ✅ 管理端复查：
  - `AdminSupplierOnboardingV3Controller` 列表/详情返回联系人、主业务、附件、证书、支付单、提交快照
  - `admin/suppliers/audit.vue` 重写为真实复查页，支持附件预览/下载、证书查看、支付复核、状态流转
- ⚠️ 环境验证未完全通过：
  - `npm run build` 受缺失 `@oxc-parser/binding-linux-x64-gnu` 影响失败
  - `./mvnw -q test` 受 `JAVA_HOME` 未正确设置影响未执行

### Phase L — 会员侧服务商申请状态闭环（2026-03-13 ✅）
- ✅ 新增 `member/supplier-applications.vue`
  - 列表查看本人全部服务商申请
  - 详情查看 `detail/detailJson/submittedSnapshot/attachments/certificates/paymentOrder`
  - 时间轴展示支付、提交、审核、激活进度
- ✅ 会员中心补入口
  - `member.vue` 左侧菜单增加“我的服务商申请”
  - `member/index.vue` 常用工具增加直达入口
- ✅ 会员端支付后补齐提交审核动作
  - 列表页、详情弹窗、支付弹窗均可在已支付后直接调用 `POST /v3/member/supplier-apply/{id}/submit`
  - 避免用户支付完成后仍卡在 `pending_payment`
- ✅ `MemberSupplierApplyV3Controller`
  - `GET /v3/member/supplier-apply`
  - `GET /v3/member/supplier-apply/{id}`
  - 返回联系人、主业务、完整性标记、附件、证书、支付单、提交快照

### Phase M — WSL 验证环境收敛（2026-03-13 ✅）
- ✅ 前端验证结论澄清
  - `/mnt/d` 挂载盘下 `npm ci` 会触发 `EPERM chmod`
  - 当前系统 Node `18.19.1` 不满足 Nuxt 4 / Vite 7 最低要求
  - 使用用户态 Node `20.19.0` + `/tmp` 镜像目录后，`npm ci` 与 `npm run build` 均通过
- ✅ 后端验证结论澄清
  - 使用本地 JDK 17 + 显式 truststore 后，`./mvnw -q test` 已可执行
  - 当前失败不再是 `JAVA_HOME`，而是测试所依赖的 SQL Server `localhost:1433` 不可达

### Phase N — 服务商入驻失败态修改重提（2026-03-13 ✅）
- ✅ 原单重提策略落地
  - `precheck_failed`、`final_review_failed` 回到原申请修改，不再要求新建申请
  - 后端允许失败态重新进入 `draft`
- ✅ 已支付失败单复用原支付结果
  - 已支付/免支付申请修改资料后不再重复缴费
  - 为避免免费切换套餐，已支付失败单暂锁定 `apply_type`
- ✅ 会员端表单回填
  - `member/supplier-applications.vue` 新增“修改后重提”

### Phase O — 活动内容管理写闭环（2026-03-13 ✅）
- ✅ DDL Phase 14：`activities` 增加活动编号、投放位置、城市 JSON、视频、议程、名额、会员价、变更原因等运营字段
- ✅ `PartnerPublishesV3Controller`
  - 新增 `GET /v3/partner/publishes/{id}` 详情接口
  - 新增 `PUT /v3/partner/publishes/{id}` 编辑接口
  - 新增 `POST /v3/partner/publishes/{id}/transition` 上下架接口
  - 创建/编辑均完整落库活动表单字段，不再只写 `title/fee/start_time`
- ✅ `Activity.java` 实体同步扩展
- ✅ `admin/content/activities.vue`
  - 编辑改为真实详情回填
  - 新建/编辑改为真实 POST/PUT
  - 上下架改为真实 transition
  - 审核中状态与字段映射纠偏（`audit_status=0` 视为审核中）
- ✅ `ActivitiesV3Controller`
  - 公共活动接口补充 `startTime/isFree/summary/agenda/maxLimit/signupCount` 等真实字段
  - 报名成功后回写 `current_participants`

### Phase P — 管理端真实筛选与导出对齐（2026-03-13 ✅）
- ✅ 标书管理做到“所筛即所导”
  - `AdminTendersV3Controller` 列表/导出统一支持 `organization/category/published_from/published_to`
  - `admin/tenders/manage.vue` 查询与导出统一透传采购组织、标书类别、发布日期范围
- ✅ 会员管理收敛到真实字段
  - `AdminMembersV3Controller` 列表/导出统一支持 `industry/invitation_code/created_from/created_to`
  - `admin/members/list.vue` 移除数据库并不存在的省份/城市/状态/国际会员假筛选
  - 移除客户端单页伪过滤，改为完全依赖后端筛选与分页结果
- ✅ 本轮验证
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过

### Phase Q — 公共大屏去假数据化（2026-03-13 ✅）
- ✅ `DashboardV3Controller` 扩展真实统计契约
  - 新增采购机构、会员等级、会员服务城市、国内外服务网络等聚合数据
  - 国内网络读取 `partners.status=1`
  - 国际网络读取 `overseas_points.status='active'`
- ✅ `dashboard.vue` 改为真实数据驱动
  - 移除 `localStorage('ipp-dashboard-config')` 对公共大屏的污染入口
  - Page 1 平台采购数据、采购机构榜单/分布、中国热力图改为真实统计
  - Page 2 会员等级占比、会员服务城市排名/地图改为真实统计
  - Page 3 国内/国际服务网络改为真实 `partners` / `overseas_points`
- ✅ `admin/dashboard/network.vue` 诚实化
  - 明确标记为规划页，不再伪装成已接后端的配置入口
- ✅ 本轮验证
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过

### Phase R — 公开供应商名录真实投射（2026-03-13 ✅）
- ✅ `/v3/suppliers` 公开接口增强
  - 继续只读取 `active` 服务商
  - 新增 `service_type`、`apply_type`、`keyword`、`city` 后端筛选
  - 返回真实 `applyType/mainService/industryList/isoList/intro`
  - 返回公开附件 `cover_image/promo_image/company_pdf`
  - 不公开营业执照、银行许可证等审核附件
- ✅ `supplier-directory.vue` 去假数据
  - 客户端伪筛选移除，改为真实服务端筛选
  - 详情弹窗改为真实公开资料，不再展示假联系人/邮箱/厂房占位
  - PDF 下载接入真实 `companyPdfUrl`
- ✅ 本阶段专项验证已完成
  - `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过

### Phase S — 首页公开入口去假数据化（2026-03-13 ✅）
- ✅ `app/pages/index.vue` 首页核心数据改为真实投射
  - “最新采购商机”列表改为真实 `tenders` 前 10 条切片，移除 `TMP-${Math.random()}` 补位假数据
  - “查看全部”按钮显示真实标书总量，不再使用静态文案
  - 统计卡片第 4 项改为真实 `organizationCount`
- ✅ 平台服务商风采改为真实服务商数据
  - 使用 `/v3/suppliers?page=1&page_size=10` 加载真实公开服务商
  - 展示真实企业名称、主营服务、服务商级别、公开图片
  - 无数据时显示诚实的空态文案，不再展示静态企业卡片
- ✅ 本阶段专项验证已完成
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - 保留 chunk size warning，仅为构建体积提示，不影响本轮功能正确性

### Phase T — 服务页平台数据去假化（2026-03-13 ✅）
- ✅ `app/pages/services.vue` 平台大数改为真实统计
  - 平台介绍区从硬编码 `4,600+ / 26 / 193 / 99.8%` 改为真实 `memberCount / organizationCount / countryCount / 服务点位`
  - 服务点位数量复用 `/v3/dashboard/stats` 的 `domesticNetwork` 与 `overseasNetwork`
- ✅ 服务页顶部通知与点位文案改为真实/诚实展示
  - 滚动通知条改为读取真实活动标题，不再展示静态报名文案
  - “47 个服务中心”“35 个城市服务中心”改为真实动态点位数量
- ✅ 服务页服务商/城市交互继续收口
  - 服务商筛选从静态“省份映射城市”改为真实城市筛选，候选来自 `domesticNetwork.cityPoints`
  - 城市服务点弹窗移除虚构负责人/热线/地址，改为真实点位统计和最近接入点位
  - 服务商详情弹窗移除假联系电话，改为展示真实服务商级别、主营业务、公开 PDF 状态
  - 活动报名登录后不再自动填入假公司/联系人/手机号
- ✅ 本阶段专项验证已完成
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - 保留 chunk size warning，仅为构建体积提示，不影响本轮功能正确性

### Phase U — 后台服务商名录字段纠偏（2026-03-13 ✅）
- ✅ `app/pages/admin/suppliers/list.vue` 列表字段映射拉正
  - 联系人从 `cityName` 改回真实 `contactName`
  - 联系电话从错误的 `onboardingStatusLabel` 改回真实 `contactPhone`
  - 行业/主营业务改为真实 `mainService/serviceTypes`
- ✅ 状态筛选口径拉正
  - 移除笼统的“审核未通过”假聚合
  - 改为明确区分 `precheck_failed` 与 `final_review_failed`
- ✅ 详情弹窗改读真实详情接口
  - `handleView()` 改为请求 `/v3/admin/supplier-onboarding/{id}`
  - 可读取真实驳回原因、提交时间、审核时间等字段
- ✅ 本阶段专项验证已完成
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - 保留 chunk size warning，仅为构建体积提示，不影响本轮功能正确性

### Phase V — 活动报名管理闭环（2026-03-13 ✅）
- ✅ 后端补齐活动报名管理接口
  - `PartnerPublishesV3Controller` 新增 `GET /v3/partner/publishes/{id}/registrations`
  - `PartnerPublishesV3Controller` 新增 `GET /v3/partner/publishes/{id}/registrations/export`
  - 管理端/合伙人均按活动归属读取报名企业、联系人、电话、报名状态、支付状态、报名时间、支付时间
- ✅ 后端导出复用现有 Excel 体系
  - 新增 `ActivitySignupExportDTO`
  - 复用 `ExcelExportUtil` 生成报名名单 Excel，无需新增表结构
- ✅ `admin/content/activities.vue` 报名弹窗改为真实闭环
  - 点击“报名管理”时读取真实报名记录
  - “导出 Excel 名单”改为真实下载后端导出文件
  - 不再保留静态假名单
- ✅ 本阶段验证
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过

### Phase W — 活动显示申请闭环（2026-03-13 ✅）
- ✅ 新增独立显示申请模型
  - DDL Phase 15：新增 `activity_display_applications`
  - 显示申请具备独立状态流：`pending_review/approved/rejected/offline`
- ✅ `PartnerPublishesV3Controller` 补齐活动显示申请写入口
  - 新增 `POST /v3/partner/publishes/{id}/display-apply`
  - 活动详情返回 `display_applications`
  - 校验显示区域、显示时间范围、结束时间不得超过活动开始时间
- ✅ `AdminActivityDisplayApplicationsV3Controller` 新建
  - 新增后台显示申请列表接口
  - 新增通过/驳回/下线/重新启用流转接口
  - 同一区域有效显示项最多 10 条
- ✅ 公共活动接口支持按显示区读取真实推荐
  - `GET /v3/activities` 新增 `display_area=home|activity|service`
  - 首页与服务页优先读取对应显示区活动，无数据再回退普通活动列表
- ✅ 本轮补充修正
  - 后台活动显示申请列表的 `keyword` 检索改为真正参与 SQL 过滤，避免伪分页
- ✅ 本阶段验证
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - `/tmp/ipp-backend-buildcheck` 下 `./mvnw -q -DskipTests compile` 通过

### Phase X — 标书发布时间口径收敛（2026-03-13 ✅）
- ✅ 为 `tenders.published_at` 补正式迁移留痕
  - 新增 `DDL_Phase16_Tenders_PublishedAt.sql`
  - 补列、回填历史已发布数据、增加状态+发布时间索引
- ✅ 管理端标书列表/导出统一切到发布时间口径
  - `AdminTendersV3Controller` 列表与导出新增 `published_from/published_to`
  - 继续兼容旧参数 `created_from/created_to`
  - 真正筛选字段从 `created_at` 收敛为 `published_at`
- ✅ 前端标书管理页同步改为发布时间参数
  - `admin/tenders/manage.vue` 查询与导出改发 `published_from/published_to`
  - 兼容旧查询串，避免历史链接立即失效
- ✅ 本轮修正结论
  - `publishDate` 问题不再只是“显示值修复”，而是展示、筛选、DDL 三处口径一致

### Phase Y — 内容元数据分离与媒体/培训编辑闭环（2026-03-13 ✅）
- ✅ `contents` 模型职责收敛
  - 新增 `DDL_Phase18_Contents_ExtraJson.sql`
  - `body` 与 `extra_json` 分离，避免正文与元数据混存
- ✅ `AdminContentsV3Controller`
  - 新增 `PUT /v3/admin/contents/{id}`
  - 列表/详情返回解析后的 `extraJson`
  - 兼容历史 `body` JSON 数据回退解析
- ✅ `ContentsV3Controller`
  - 公共媒体详情优先渲染真实正文
  - 旧数据不再把原始 JSON 暴露给前台页面
- ✅ `admin/content/media.vue` 与 `admin/content/trainings.vue`
  - 编辑改为真实详情加载与真实保存
  - 结构化扩展字段统一写入 `extra_json`
  - 不再保留“写接口未接入”的受控降级
- ✅ 本阶段验证
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
  - `/tmp/ipp-backend-buildcheck/ipp-backend` 下 `./mvnw -q -DskipTests compile` 通过

### Phase Z — 合伙人活动发布入口收敛（2026-03-13 ✅）
- ✅ `admin/partner-publish.vue` 收敛为真实“合伙人活动发布入口”
  - 去掉培训/商机伪能力表述，避免前端误导
  - 状态口径改为 `auditing/published/rejected/offline`
- ✅ 真实编辑闭环
  - 查看详情改读 `GET /v3/partner/publishes/{id}`
  - 编辑改为详情回填 + `PUT /v3/partner/publishes/{id}`
  - 已发布/已下架活动支持“编辑重提”
- ✅ 真实上传闭环
  - 封面图/宣传视频接入 `/api/common/upload`
  - 上传结果真实写入 `cover_image / video_url`
- ✅ 本阶段验证
  - `/tmp/ipp-platform-buildcheck` 下 `NUXT_TELEMETRY_DISABLED=1 NUXT_TELEMETRY_CONSENT=0 npm run build` 通过

### Phase AA — 培训页去伪能力收敛（2026-03-13 ✅）
- ✅ `admin/content/trainings.vue` 去掉不存在的培训报名/名单导出/独立显示申请入口
- ✅ 页面语义回到真实内容管理能力
  - “报名数据”改为“开课与名额”
  - “累计学员”改为“已发布”
  - 新增能力边界提示，明确培训当前不属于活动报名系统
- ✅ 详情弹窗补充真实投放信息
  - 前台位置
  - 开课时间
  - 名额限制
- ✅ 本阶段验证
  - `/tmp/ipp-platform-buildcheck` 下 `NUXT_TELEMETRY_DISABLED=1 NUXT_TELEMETRY_CONSENT=0 npm run build` 通过

### Phase AB — 会员管理真实 CRUD 闭环（2026-03-13 ✅）
- ✅ 根因修复
  - 后台“新增会员保存后刷新消失”的根因确认为前端假 CRUD + 后端缺少写接口
- ✅ 后端补齐真实写接口
  - `AdminMembersV3Controller` 新增 `create/update/transition/delete`
  - 新增会员时同步创建 `sys_user + member_profile`
  - 邀请码解析为 `partner_id`，并拒绝已停用合伙人邀请码
  - 删除前校验业务关联数据，避免误删有历史记录账号
- ✅ 前端改为真实 API 驱动
  - `admin/members/list.vue` 的新增、编辑、删除、启停、详情全部接真实接口
  - 增加表格 loading 和保存 submitting
  - 分页完全依赖后端，不再保留本地伪保存逻辑
- ✅ 本阶段验证
  - `/tmp/ipp-backend-buildcheck/ipp-backend` 下 `./mvnw -q -DskipTests compile` 通过
  - `/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过

### Phase AC — 数据库 Schema 启动预检（2026-03-13 ✅）
- ✅ 新增启动期预检
  - 后端启动时校验当前数据库是否具备运行所需关键表/列
  - 不再等用户点页面才看到一串 500
- ✅ 当前预检覆盖关键迁移
  - `DDL_Phase2_Migration.sql`
  - `DDL_Phase14_Activities_Closure.sql`
  - `DDL_Phase15_ActivityDisplayApplications.sql`
  - `DDL_Phase17_ActivityRecords.sql`
  - `DDL_Phase18_Contents_ExtraJson.sql`
- ✅ 缺失时直接 fail fast
  - 启动日志会明确指出缺哪张表/哪一列，以及应执行哪个 `docs/DDL_Phase*.sql`
- ✅ 测试前检查文档同步
  - `docs/TestEnv_Preflight_2026-03-13.md` 已补到 Phase 18 和新预检口径

### Phase AD — 运行基线复核与恢复方案（2026-03-13 ✅）
- ✅ 真实运行日志复核
  - 确认当前环境的高频 500 不是随机控制器错误，而是“运行实例漂移 + 数据库 schema 漂移”
- ✅ 已钉死的直接根因
  - `POST /v3/admin/members` 返回 `Request method 'POST' is not supported`
  - `GET /v3/admin/contents` 命中缺列 `contents.extra_json`
  - `GET /v3/partner/publishes` 命中缺表 `activity_records`
- ✅ 形成恢复工单
  - 新增 `docs/RuntimeRecovery_S0S1_2026-03-13.md`
  - 新增 `docs/SQL_Runtime_Schema_Check_2026-03-13.sql`
- ✅ 修正文档口径
  - `UAT_Alpha_TestPlan_2026-03-13.md` 不再把当前环境描述为“可直接启动 Alpha UAT”
  - `TestEnv_Preflight_2026-03-13.md` 增补运行恢复入口

### Phase AE — 运行版本识别护栏（2026-03-13 ✅）
- ✅ 新增运行信息接口
  - `GET /api/v3/runtime-info`
  - 返回当前运行实例的版本标识、预检开关、启动时间、激活 profile
- ✅ 所有响应附带版本头
  - `X-XRIPP-Runtime-Version`
  - `X-XRIPP-Schema-Preflight`
- ✅ 目的
  - 避免再次出现“仓库代码已更新，但 Windows 侧仍在跑旧实例”却难以识别的问题

### Phase AF — Windows 协作执行手册（2026-03-13 ✅）
- ✅ 新增 `docs/Claude_Windows_Execution_Runbook_2026-03-13.md`
- ✅ 明确 Claude 在 Windows 侧的职责边界
  - 只做 schema 自检、DDL 执行、后端重启、最小接口验证
- ✅ 明确回传口径
  - SQL 自检结果
  - 实际执行 DDL
  - `/api/v3/runtime-info`
  - 7 个关键接口验证结果

### Phase AG — 合伙人利润分成真实闭环（2026-03-13 ✅）
- ✅ DDL Phase 19
  - 新增 `partner_profit_configs`
  - 新增 `partner_profit_settlements`
- ✅ 后端真实化
  - 新增 `AdminProfitSharingV3Controller`
  - 提供配置 CRUD、月度统计、订单明细、结算写库接口
  - 分成归属优先取 `orders.partner_id`，缺失时回退 `sys_user.partner_id`
- ✅ 前端真实化
  - `admin/finance/profit.vue` 改为真实配置/统计/明细/结算页面
  - 顶部统计卡片按 `/v3/admin/orders/stats` 真实契约读取
- ✅ 启动护栏
  - `DatabaseSchemaPreflightRunner` 新增 Phase 19 两张表检查
  - 未执行 DDL 时后端启动 fail-fast，避免运行期再报 500

### Phase AH — 后台配置底座与定价真实化（2026-03-13 ✅）
- ✅ DDL Phase 20
  - 新增 `admin_config_entries`
- ✅ 后端通用配置层
  - 新增 `AdminConfigsV3Controller`
  - 支持 `GET /v3/admin/configs/{namespace}`
  - 支持 `POST /v3/admin/configs/{namespace}/batch`
  - 支持 `DELETE /v3/admin/configs/{namespace}/{key}`
- ✅ `pricing.vue` 真实化
  - 会员费、标书、培训、服务四类定价改为真实加载/保存
  - 页面不再停留在会话态
- ✅ 架构收益
  - 为 `admin/business/*`、`admin/system/*` 后续真实化提供统一配置存储底座
- ✅ 启动护栏
  - `DatabaseSchemaPreflightRunner` 新增 Phase 20 表检查

### Phase AI — Business 配置页真实化（2026-03-14 ✅）
- ✅ 复用 Phase 20 通用配置底座
  - 新增 `app/composables/useAdminConfigNamespace.ts`
- ✅ `admin/business/packages.vue`
  - 套餐权益真实加载/保存/启停
- ✅ `admin/business/promotions.vue`
  - 规则新增/编辑/复制/删除/启停真实持久化
- ✅ `admin/business/roles.vue`
  - 角色权限配置 + 变更记录真实持久化
- ✅ 无新增 DDL
  - 直接复用 `admin_config_entries`
- ⚠️ 页面级浏览器验收待补
  - 需 Claude 在 Windows 环境补最终联调留痕

### Phase AJ — System 配置页第一阶段真实化（2026-03-14 ✅）
- ✅ `admin/system/settings.vue`
  - 基础设置 / 邮件配置 / 安全设置真实持久化
- ✅ `admin/system/login-config.vue`
  - 基础配置 / 安全策略 / 第三方登录真实持久化
- ✅ 架构边界明确
  - `notifications` / `customer-service` / `permissions` / `logs` / `backup` 不强行塞进配置表
- ✅ 无新增 DDL
  - 继续复用 `admin_config_entries`
- ⚠️ 页面级浏览器验收待补
  - 需 Claude 在 Windows 环境补最终联调留痕

### Phase AK — 证书模板管理真实化（2026-03-14 ✅）
- ✅ `admin/system/certificates.vue`
  - 模板列表真实加载 / 保存 / 删除 / 刷新回读
- ✅ 文件与元数据分层
  - 文件本体继续走 `/api/common/upload`
  - 模板元数据复用 `admin_config_entries`
- ✅ 无新增 DDL
  - namespace: `system_certificates`
- ⚠️ 页面级浏览器验收待补
  - 需 Claude 在 Windows 环境补最终联调留痕

### Phase AL — 客服系统真实化（2026-03-14 ✅）
- ✅ DDL Phase 21
  - 新增 `customer_service_messages`
  - 新增 `customer_service_tickets`
  - 新增 `customer_service_ticket_files`
- ✅ 后端真实化
  - 新增 `MemberCustomerServiceV3Controller`
  - 新增 `AdminCustomerServiceV3Controller`
  - 会员端支持工单列表 / 详情 / 创建、快速留言
  - 后台支持统计卡 / 留言处理 / 工单查询 / 工单详情 / 工单处理 / 删除
- ✅ 前端真实化
  - `member/feedback.vue` 改为真实工单与留言入口
  - `admin/system/customer-service.vue` 改为真实客服处理台
- ✅ 启动护栏
  - `DatabaseSchemaPreflightRunner` 新增 Phase 21 表/列检查
- ⚠️ 运行验证待补
  - 需 Claude 在 Windows 环境执行 Phase 21 DDL、重启后端并按 runbook 补页面操作序列验证

### Phase AM — 通知中心真实化（2026-03-14 ✅）
- ✅ DDL Phase 22
  - 新增 `notification_type_settings`
  - 新增 `notification_templates`
  - 新增 `notification_send_logs`
- ✅ 后端真实化
  - 新增 `AdminNotificationsV3Controller`
  - 支持通知类型设置、模板 CRUD、发送记录查询、发送请求留痕
- ✅ 前端真实化
  - `admin/system/notifications.vue` 改为真实通知中心
- ✅ 页面口径修正
  - `admin/system/about.vue` 去除错误的“未接后端”提示
  - `admin/system/backup.vue` 改为运维备份治理说明页，不再伪造备份/恢复能力
- ⚠️ 运行验证待补
  - 需 Claude 在 Windows 环境执行 Phase 22 DDL、重启后端并补页面操作序列验证

### Phase AN — 操作日志真实化（2026-03-14 ✅）
- ✅ 无新增 DDL
  - 直接复用现有 `audit_logs` 与 `state_transition_logs`
- ✅ 后端真实化
  - 新增 `AdminLogsV3Controller`
  - 提供统一聚合查询接口 `GET /v3/admin/logs`
- ✅ 前端真实化
  - `admin/system/logs.vue` 改为真实日志查询页
  - 支持关键字 / 来源 / 日期范围筛选、分页、当前页 CSV 导出
- ✅ 架构收益
  - 避免重复创建第三套操作日志表
  - 后台日志页建立在现有真实审计能力之上
- ⚠️ 运行验证待补
  - 需 Claude 在 Windows 环境补页面操作序列验证

## 5. 当前已知漂移（待收敛）

### 前端页面接入状态

| 页面 | 状态 | 说明 |
|---|---|---|
| `admin/tenders/reference.vue` | 部分实现 | 列表真实+服务端分页；写操作（引用提交/抓取/删除）未接入 |
| `admin/tenders/orders.vue` | ✅ 已接入 | 调用 /v3/admin/orders 真实数据+服务端分页 |
| `admin/tenders/analysis.vue` | ✅ 已接入 | 移除 generateMockData()，调用 /v3/admin/tenders/analysis 真实统计 |
| `member/supplier-apply.vue` | ✅ 已接入 | 支付闭环 + 真实上传 + 结构化附件/证书持久化 |
| `member/supplier-applications.vue` | ✅ 已接入 | 我的服务商申请列表/详情/支付状态刷新/提交审核 |
| `admin/members/orders.vue` | ✅ 已接入 | 列表/统计/创建/完成/取消全链路真实 API |
| `admin/members/un-audit.vue` | 部分实现 | 审核主流程真实；证书上传依赖文件上传（Phase D 已就绪） |
| `admin/members/list.vue` | ✅ 已接入 | admin + partner 均可访问（partner 按 partner_id 隔离） |
| `admin/audit.vue` | ✅ 已接入 | 三 Tab 均已接入真实 API；需求审核已闭环 |
| `admin/suppliers/list.vue` | ✅ 已接入 | 列表真实；admin + partner 均可访问（partner 隔离）；新增/编辑受控降级 |
| `admin/suppliers/audit.vue` | ✅ 已接入 | 真实复查页：详情/附件/证书/支付/提交快照/状态流转 |
| `admin/suppliers/analysis.vue` | ✅ 已接入 | 入驻申请口径统计（admin + partner 隔离） |
| `admin/partner-publish.vue` | ✅ 已接入 | 合伙人活动发布真实闭环：列表/详情/创建/编辑重提/撤回/文件上传 |
| `admin/content/activities.vue` | ✅ 已接入 | 列表/详情/新建/编辑/审核/上下架/报名管理/显示申请均已接入真实 API |
| `admin/content/trainings.vue` | ✅ 已接入 | 列表/详情/新建/编辑/状态流转真实；已移除不存在的培训报名/显示申请假入口 |
| `admin/content/media.vue` | ✅ 已接入 | 列表/详情/新建/编辑/状态流转真实 |
| `admin/content/display.vue` | ✅ 已接入 | 轮播/广告真实内容管理 + 活动显示申请真实审核 |
| `admin/overseas/*.vue` | 降级标注 | 海外服务模块无后端 API，已添加"功能开发中"横幅 |
| `admin/system/settings.vue` | ✅ 已接入 | 基础设置 / 邮件配置 / 安全设置真实加载与保存；复用 Phase 20 配置底座 |
| `admin/system/login-config.vue` | ✅ 已接入 | 登录基础配置 / 安全策略 / OAuth 配置真实加载与保存；复用 Phase 20 配置底座 |
| `admin/system/notifications.vue` | ✅ 已接入 | 通知类型设置 / 模板管理 / 发送记录均已接入真实 API；发送动作当前为真实留痕而非第三方网关投递；依赖 Phase 22 DDL |
| `admin/system/customer-service.vue` | ✅ 已接入 | 留言查询/处理 + 工单列表/详情/处理/删除 + 附件查看均已接入真实 API；依赖 Phase 21 DDL |
| `admin/system/permissions.vue` | ✅ 已接入 | 系统权限中心已完成真实 RBAC 接入：权限快照、登录模式、权限档案、账号绑定以及菜单/路由联动均已落地 |
| `admin/system/logs.vue` | ✅ 已接入 | 统一聚合 audit_logs + state_transition_logs 形成真实日志查询页；支持筛选/分页/导出 |
| `admin/system/backup.vue` | 说明页 | 运维/DBA 备份治理说明页，不承担真实备份/恢复入口 |
| `admin/system/certificates.vue` | ✅ 已接入 | 模板上传走文件服务，模板元数据真实持久化；复用 Phase 20 配置底座 |
| `admin/system/about.vue` | 纯信息 | 静态信息页，无需后端配置 |
| `admin/business/packages.vue` | ✅ 已接入 | 会员套餐权益真实加载/保存/启停；复用 Phase 20 配置底座 |
| `admin/business/promotions.vue` | ✅ 已接入 | 促销规则新增/编辑/复制/删除/启停真实持久化；复用 Phase 20 配置底座 |
| `admin/business/roles.vue` | ✅ 已接入 | 业务角色权限配置 + 变更记录真实持久化；复用 Phase 20 配置底座 |
| `admin/finance/pricing.vue` | ✅ 已接入 | 四类定价真实加载/保存；依赖 Phase 20 DDL；浏览器联调待补最终留痕 |
| `admin/finance/profit.vue` | ✅ 已接入 | 分成配置/统计/明细/结算均已接入真实 API；依赖 Phase 19 DDL；浏览器联调待补最终留痕 |
| `admin/index.vue` | ✅ 已接入 | 仪表盘统计卡片、最近会员、最近内容均已接真实 API |

### 系统级待办

| 事项 | 优先级 | 说明 |
|---|---|---|
| ~~种子数据执行~~ | ~~P0~~ | ✅ 已执行（2026-02-28 sqlcmd 确认） |
| ~~DDL 执行验证~~ | ~~P0~~ | ✅ Phase 1-5 + 种子数据已全量执行确认 |
| 活动审核仅单级 | P2 | 需求要求双级审核（一审+二审），需 operator/auditor 子角色 |
| 微信支付正式商户集成 | P2 | 当前已具备平台内支付闭环与回调入口，但仍非正式商户接入 |
| SMS 短信服务 | P2 | 需第三方短信服务商接入 |
| 限流 / CORS 细化 | P3 | 当前无 rate limiting，CORS 使用默认配置 |
| 清理空 stub controller | P3 | SuppliersController.java / SysUserController.java 为空壳，可删除 |

## 6. 功能状态矩阵（验收口径）

> DataScope 说明：`orders` 和 `tenders` 已加入 DataScope 白名单（超出 API_Specs_V2.0 原定范围，属代码先于文档的合理扩展，以代码为准）。

| 模块 | 后端 API | 前端接入 | 验收状态 |
|---|---|---|---|
| 用户登录/注册 | ✅ BCrypt-only 认证 + 注册 API | ✅ 已接入（含注册） | ✅ 可验收（种子数据 + 自注册） |
| 标书发布/管理 | ✅ CRUD + 状态机 | ✅ 已接入 | ✅ 可验收 |
| 标书分析统计 | ✅ /analysis 端点 | ✅ 已接入（图表+导出） | ✅ 可验收 |
| 标书引用 | 读接口完成，写未实现 | 只读降级 | 仅读操作可验收 |
| 标书防重复扣费 | ✅ tender_download_logs + dedup | 不涉及前端 | ✅ 可验收 |
| 服务商名录 | ✅ 公共查询 | ✅ 已接入 | ✅ 可验收查询 |
| 服务商审核 | ✅ 入库审核流 | ✅ 主流程真实 + 复查详情完整 | ✅ 可验收主流程 |
| 服务商统计 | ✅ 聚合统计 | ✅ 已接入 | ✅ 可验收 |
| 采购需求管理 | ✅ list + review | ✅ 已接入 | ✅ 可验收 |
| 商机对接管理 | ✅ 同采购需求 | ✅ 已接入 | ✅ 可验收 |
| 活动管理 | ✅ 列表/详情/新建/编辑/审核/上下架/显示申请 | ✅ 已接入 | ✅ 可验收主流程 |
| 内容管理（培训/媒体） | ✅ CRUD + 状态流转 | ✅ 已接入 | ✅ 可验收 |
| 展示位管理 | ✅ 活动显示申请 + 内容展示管理 | ✅ 已接入 | ✅ 可验收主流程 |
| 订单管理 | ✅ CRUD + 状态流转 | ✅ 已接入 | ✅ 可验收 |
| 利润分成管理 | ✅ 配置/统计/明细/结算 API | ✅ 已接入 | ✅ 可验收（需先执行 Phase 19 DDL） |
| 定价配置管理 | ✅ 通用配置 API + pricing 命名空间 | ✅ 已接入 | ✅ 可验收（需先执行 Phase 20 DDL） |
| 客服系统 | ✅ 留言/工单/附件/后台处理 API | ✅ 已接入 | ✅ 可验收（需先执行 Phase 21 DDL） |
| 通知中心 | ✅ 类型设置/模板/发送记录 API | ✅ 已接入 | ✅ 可验收（需先执行 Phase 22 DDL） |
| 操作日志 | ✅ 聚合查询 audit_logs + state_transition_logs | ✅ 已接入 | ✅ 可验收（无需新增 DDL） |
| 会员管理 | ✅ list/detail/create/update/delete/transition/set-level | ✅ 已接入 | ✅ 可验收 |
| 会员认证审核 | ✅ review 流程 | ✅ 主流程真实 | ✅ 可验收主流程 |
| 会员中心/画像 | ✅ profile + benefits | ✅ 已接入 | ✅ 可验收 |
| 合伙人管理 | ✅ list + detail | ✅ 已接入 | ✅ 可验收 |
| 会员收藏 | ✅ UserFavoritesV3Controller | ✅ 已接入 | ✅ 可验收（三类收藏） |
| 权益池管理 | ✅ BenefitPoolV3Controller | 待接入 | 后端可验收 |
| 文件上传 | ✅ POST /common/upload | 14 页面引用 | ✅ 可验收（Cookie 认证） |
| 定时任务 | ✅ 续期提醒 + 自动降级 | 不涉及前端 | ✅ 可验收（查看日志） |
| 状态流转审计 | ✅ StateTransitionService 全覆盖 | 不涉及前端 | ✅ 可验收 |
| 审核日志 | ✅ audit_logs 4 端点写入 | 不涉及前端 | ✅ 可验收 |
| 海外服务 | 未实现 | 未接入 | 暂不可验收 |
| 微信支付 | ✅ 服务商入驻已具备平台内支付闭环与回调入口 | ✅ 已接入（服务商入驻） | ✅ 可验收入驻主流程（正式商户待接） |
| SMS 短信 | 未实现 | 未接入 | 暂不可验收 |

## 7. ⚠️ 当前风险提醒

| 风险 | 严重性 | 动作 |
|---|---|---|
| ~~DDL 执行状态未知~~ | ~~🔴~~ ✅ 已解决 | Phase 1-5 + 种子数据已通过 sqlcmd 执行确认（2026-02-28） |
| ~~无测试用户数据~~ | ~~🔴~~ ✅ 已解决 | 账号可用：admin/admin123, p1001/p1001, p2001/p1001, m1001/m1001, m2001/m1001 |
| 生产密钥未替换 | 🟡 上线前 | JWT secret 和 DB password 使用默认值，生产环境必须通过环境变量覆盖 |
| API_Contract_v3.0.md 漂移 | 🟡 文档 | 3 个模块端点已从 /review+/publish+/close 演进为统一 /transition，文档未同步 |

## 8. 后端 Controller 清单（35 个）

| Controller | 路径前缀 | 说明 |
|---|---|---|
| AuthV3Controller | /v3/auth | 登录 + 注册认证 |
| CommonV3Controller | /common | 文件上传 |
| TendersV3Controller | /v3/tenders | 标书公共查询 + 下载 |
| SuppliersV3Controller | /v3/suppliers | 服务商公共查询 |
| ActivitiesV3Controller | /v3/activities | 活动公共查询 |
| OrdersV3Controller | /v3/orders | 会员订单 |
| MemberProfileV3Controller | /v3/member/profile | 会员画像 |
| MemberBenefitsV3Controller | /v3/member/benefits | 会员权益 |
| MemberDemandsV3Controller | /v3/member/demands | 会员采购需求（CRUD） |
| MemberVerificationV3Controller | /v3/member/verifications | 会员认证 |
| SupplierOnboardingV3Controller | /v3/supplier-onboarding | 供应商入驻 |
| PartnerPublishesV3Controller | /v3/partner/publishes | 合伙人发布 |
| BenefitPoolV3Controller | /v3/admin/benefit-pool | 权益池管理 |
| AdminMembersV3Controller | /v3/admin/members | 会员管理 |
| AdminPartnersV3Controller | /v3/admin/partners | 合伙人管理 |
| AdminTendersV3Controller | /v3/admin/tenders | 标书管理 |
| AdminOrdersV3Controller | /v3/admin/orders | 订单管理 |
| AdminDemandsV3Controller | /v3/admin/demands | 需求管理 |
| AdminContentsV3Controller | /v3/admin/contents | 内容管理 |
| AdminMemberVerificationV3Controller | /v3/admin/member-verifications | 会员认证审核 |
| AdminSupplierOnboardingV3Controller | /v3/admin/supplier-onboarding | 供应商入库审核 |
| UserFavoritesV3Controller | /v3/member/favorites | 会员收藏（CRUD + ids） |
| ContentsV3Controller | /v3/contents | 内容公共查询（仅 published） |
| DashboardV3Controller | /v3/dashboard | 数据大屏统计（公共，无需认证） |
| OverseasPointsV3Controller | /v3/overseas-points | 海外网点公共查询（公共，无需认证） |
| AdminOverseasPointsV3Controller | /v3/admin/overseas-points | 海外网点管理（CRUD + stats） |
| MemberSupplierApplyV3Controller | /v3/member/supplier-apply | 会员自助服务商入驻申请 |
| MemberCustomerServiceV3Controller | /v3/member/customer-service | 会员客服工单/留言 |
| InternalPaymentsV3Controller | /v3/internal/payments | 支付回调（预留） |
| AdminActivityDisplayApplicationsV3Controller | /v3/admin/activity-display-applications | 活动显示申请审核/上下线 |
| AdminProfitSharingV3Controller | /v3/admin/profit-sharing | 合伙人利润分成配置/统计/明细/结算 |
| AdminConfigsV3Controller | /v3/admin/configs | 后台通用配置存储（首批承接 pricing） |
| AdminCustomerServiceV3Controller | /v3/admin/customer-service | 客服统计/留言处理/工单处理 |
| AdminNotificationsV3Controller | /v3/admin/notifications | 通知类型设置/模板/发送记录 |
| AdminLogsV3Controller | /v3/admin/logs | 操作日志聚合查询 |

## 9. AI 工具使用提示（Cursor / Claude Code）

- 优先参考"当前权威文档"生成代码或核实逻辑。
- 涉及历史需求比对时，再引用 `docs/requirements`。
- 权限相关问题必须遵守第 3 节路由权限基线与 `SecurityContextHolder` 约束。
- 回答建议格式：先列符合/冲突点，再给修改方案（必要时附最小 diff）。

## 10. 维护建议

- 每次新增/删除文档后，同步更新本索引。
- 文档命名统一使用 UTF-8 编码与稳定路径，避免中英文混排重命名导致失链。
- 每周联调结束后，在本文件"当前已知漂移"区增删实际状态。
- 每周同步更新"功能状态矩阵"，避免"误删功能"或"假完成"判断。
