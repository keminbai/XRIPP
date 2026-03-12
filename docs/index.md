# XRIPP 文档索引

本文件是 `docs/` 目录的统一入口，用于：
- 快速定位需求来源、设计约束、接口契约和执行计划
- 明确"当前权威文档"与"历史参考文档"
- 减少前后端联调时的路径错误和口径不一致

最后更新：2026-03-12（第十五轮：前端 Mock 清理 Batch 3-4 + P0 会员注册全栈 + 质量审计修复）

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
| 10 | [DDL_Seed_Data.sql](./DDL_Seed_Data.sql) | **测试账号 + 业务种子数据（幂等）** |

### 执行计划
- [Execution_Week1_Plan.md](./Execution_Week1_Plan.md)
- [Roadmap_Week2_Week4.md](./Roadmap_Week2_Week4.md)
- [Mock_Removal_Execution_Checklist.md](./Mock_Removal_Execution_Checklist.md)
- [API_Implementation_Plan_v1.md](./API_Implementation_Plan_v1.md)

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

## 5. 当前已知漂移（待收敛）

### 前端页面接入状态

| 页面 | 状态 | 说明 |
|---|---|---|
| `admin/tenders/reference.vue` | 部分实现 | 列表真实+服务端分页；写操作（引用提交/抓取/删除）未接入 |
| `admin/tenders/orders.vue` | ✅ 已接入 | 调用 /v3/admin/orders 真实数据+服务端分页 |
| `admin/members/orders.vue` | ✅ 已接入 | 列表/统计/创建/完成/取消全链路真实 API |
| `admin/members/un-audit.vue` | 部分实现 | 审核主流程真实；证书上传依赖文件上传（Phase D 已就绪） |
| `admin/members/list.vue` | ✅ 已接入 | admin + partner 均可访问（partner 按 partner_id 隔离） |
| `admin/audit.vue` | ✅ 已接入 | 三 Tab 均已接入真实 API；需求审核已闭环 |
| `admin/suppliers/list.vue` | ✅ 已接入 | 列表真实；admin + partner 均可访问（partner 隔离）；新增/编辑受控降级 |
| `admin/suppliers/audit.vue` | 部分实现 | 主审核流真实（admin + partner 可查看）；证书上传/下载受控降级 |
| `admin/suppliers/analysis.vue` | ✅ 已接入 | 入驻申请口径统计（admin + partner 隔离） |
| `admin/partner-publish.vue` | ✅ 已接入 | 合伙人发布管理（list/create/delete 真实） |
| `admin/content/activities.vue` | 部分实现 | 列表+新建+审核真实；下架/导出受控降级 |
| `admin/content/trainings.vue` | 部分实现 | 列表+状态流转+创建真实；编辑受控降级 |
| `admin/content/media.vue` | 部分实现 | 同培训 |
| `admin/content/display.vue` | 未实现 | 三 Tab 均为占位提示；需 OSS 依赖 |
| `admin/overseas/*.vue` | 降级标注 | 海外服务模块无后端 API，已添加"功能开发中"横幅 |
| `admin/system/*.vue` | 降级标注 | 系统管理模块，已添加"暂未对接后端"横幅 |
| `admin/business/*.vue` | 降级标注 | 业务配置模块，已添加"暂未对接后端"横幅 |
| `admin/finance/pricing.vue` | 降级标注 | 定价配置，已添加"暂未对接后端"横幅 |
| `admin/finance/profit.vue` | 部分实现 | 统计卡片接入真实 API；利润分成表仍为 mock |
| `admin/index.vue` | 未实现 | 仪表盘统计为静态数据（低优先级） |

### 系统级待办

| 事项 | 优先级 | 说明 |
|---|---|---|
| ~~种子数据执行~~ | ~~P0~~ | ✅ 已执行（2026-02-28 sqlcmd 确认） |
| ~~DDL 执行验证~~ | ~~P0~~ | ✅ Phase 1-5 + 种子数据已全量执行确认 |
| 活动审核仅单级 | P2 | 需求要求双级审核（一审+二审），需 operator/auditor 子角色 |
| 微信支付集成 | P2 | 需商户号审批，当前无支付回调 |
| SMS 短信服务 | P2 | 需第三方短信服务商接入 |
| 限流 / CORS 细化 | P3 | 当前无 rate limiting，CORS 使用默认配置 |
| 清理空 stub controller | P3 | SuppliersController.java / SysUserController.java 为空壳，可删除 |

## 6. 功能状态矩阵（验收口径）

> DataScope 说明：`orders` 和 `tenders` 已加入 DataScope 白名单（超出 API_Specs_V2.0 原定范围，属代码先于文档的合理扩展，以代码为准）。

| 模块 | 后端 API | 前端接入 | 验收状态 |
|---|---|---|---|
| 用户登录/注册 | ✅ BCrypt-only 认证 + 注册 API | ✅ 已接入（含注册） | ✅ 可验收（种子数据 + 自注册） |
| 标书发布/管理 | ✅ CRUD + 状态机 | ✅ 已接入 | ✅ 可验收 |
| 标书引用 | 读接口完成，写未实现 | 只读降级 | 仅读操作可验收 |
| 标书防重复扣费 | ✅ tender_download_logs + dedup | 不涉及前端 | ✅ 可验收 |
| 服务商名录 | ✅ 公共查询 | ✅ 已接入 | ✅ 可验收查询 |
| 服务商审核 | ✅ 入库审核流 | ✅ 主流程真实 | ✅ 可验收主流程 |
| 服务商统计 | ✅ 聚合统计 | ✅ 已接入 | ✅ 可验收 |
| 采购需求管理 | ✅ list + review | ✅ 已接入 | ✅ 可验收 |
| 商机对接管理 | ✅ 同采购需求 | ✅ 已接入 | ✅ 可验收 |
| 活动管理 | ✅ 列表/新建/审核 | ✅ 已接入 | ✅ 可验收主流程 |
| 内容管理（培训/媒体） | ✅ CRUD + 状态流转 | ✅ 已接入 | ✅ 可验收 |
| 展示位管理 | 无后端 | 占位降级 | 暂不可验收 |
| 订单管理 | ✅ CRUD + 状态流转 | ✅ 已接入 | ✅ 可验收 |
| 会员管理 | ✅ list + set-level | ✅ 已接入 | ✅ 可验收 |
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
| 微信支付 | 未实现 | 未接入 | 暂不可验收 |
| SMS 短信 | 未实现 | 未接入 | 暂不可验收 |

## 7. ⚠️ 当前风险提醒

| 风险 | 严重性 | 动作 |
|---|---|---|
| ~~DDL 执行状态未知~~ | ~~🔴~~ ✅ 已解决 | Phase 1-5 + 种子数据已通过 sqlcmd 执行确认（2026-02-28） |
| ~~无测试用户数据~~ | ~~🔴~~ ✅ 已解决 | 账号可用：admin/admin123, p1001/p1001, p2001/p1001, m1001/m1001, m2001/m1001 |
| 生产密钥未替换 | 🟡 上线前 | JWT secret 和 DB password 使用默认值，生产环境必须通过环境变量覆盖 |
| API_Contract_v3.0.md 漂移 | 🟡 文档 | 3 个模块端点已从 /review+/publish+/close 演进为统一 /transition，文档未同步 |

## 8. 后端 Controller 清单（27 个）

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
| InternalPaymentsV3Controller | /v3/internal/payments | 支付回调（预留） |

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
