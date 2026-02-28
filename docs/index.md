# XRIPP 文档索引

本文件是 `docs/` 目录的统一入口，用于：
- 快速定位需求来源、设计约束、接口契约和执行计划
- 明确“当前权威文档”与“历史参考文档”
- 减少前后端联调时的路径错误和口径不一致

最后更新：2026-02-28（第九轮更新：全面系统审计 → 详见 AUDIT_Full_System_2026-02-28.md）

## 1. 当前权威文档（开发与联调优先参考）

- [API_Contract_v3.0.md](./API_Contract_v3.0.md)
- [ARCH_StateMachine_v1.md](./ARCH_StateMachine_v1.md)
- [ARCH_DataDictionary_v1.md](./ARCH_DataDictionary_v1.md)
- [Page_API_Mapping_v1.md](./Page_API_Mapping_v1.md)
- [Execution_Week1_Plan.md](./Execution_Week1_Plan.md)
- [Mock_Removal_Execution_Checklist.md](./Mock_Removal_Execution_Checklist.md)
- [Roadmap_Week2_Week4.md](./Roadmap_Week2_Week4.md)
- [DDL_Gap_Report_v1.md](./DDL_Gap_Report_v1.md)
- [DDL_Phase1_Final.sql](./DDL_Phase1_Final.sql)
- [DDL_Phase2_Migration.sql](./DDL_Phase2_Migration.sql)
- [API_Implementation_Plan_v1.md](./API_Implementation_Plan_v1.md)
- [AUDIT_Full_System_2026-02-28.md](./AUDIT_Full_System_2026-02-28.md)（全面系统审计报告）

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

1. 新功能开发、联调、验收：优先使用“当前权威文档”。
2. 历史需求文档仅用于追溯，不直接覆盖当前接口与状态机定义。
3. 若文档与代码冲突：先以代码实际行为为准，补充文档修订记录后再统一。
4. 路由权限基线：
   - `member` -> `/member`
   - `partner` -> `/admin/partner-publish`
   - `admin` -> `/admin`
5. 所有用户身份必须来自 `SecurityContextHolder`，禁止从请求参数透传 `user_id` 作为权限依据。
6. v3 统一响应字段：`code/message/data/request_id/timestamp`，不使用 `msg` 作为 v3 标准字段。
7. 会员等级统一枚举：`NORMAL | VIP | SVIP`。

## 4. 当前已知漂移（待收敛）

| 接口/功能 | 状态 | 责任人 | 预计完成时间 | 备注 |
|---|---|---|---|---|
| `app/pages/admin/tenders/reference.vue` | 部分实现 | - | - | 已切真实列表；“引用提交/抓取/删除”写接口未接入，当前只读 |
| `app/pages/admin/members/un-audit.vue` | 部分实现 | - | - | 已切真实审核主流程；证书上传接口未接入 |
| `app/pages/admin/audit.vue` | 部分实现 | - | - | 服务商、内容、采购需求三 Tab 均已接入真实 API（ad8ddbc）；需求审核已闭环 |
| `app/pages/admin/suppliers/list.vue` | 部分实现 | - | - | 已切真实列表；新增/编辑/资料上传写接口未接入，当前受控降级 |
| `app/pages/admin/suppliers/audit.vue` | 部分实现 | - | - | 主数据与审核流已切真实接口；证书上传/下载接口未接入，受控降级 |
| `app/pages/admin/suppliers/analysis.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247）；入驻申请口径，非运营主表 suppliers |
| `app/pages/admin/suppliers/list.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247）；写操作受控降级 |
| `app/pages/admin/content/activities.vue` | 部分实现 | - | - | 已切真实列表（/v3/partner/publishes）；partner 新建已接真实接口；状态切换/展示申请/导出受控降级 |
| `app/pages/admin/content/trainings.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/contents?content_type=training（1df2247）；发布/编辑受控降级 |
| `app/pages/admin/content/media.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/contents?content_type=media（1df2247）；发布/编辑受控降级 |
| `member/orders.vue` | ✅ 已完成 | - | 2026-02-28 | 已删除 mock 数组，接入 /v3/orders API |
| **系统级：7 张 Phase1 表无 Entity** | **阻塞** | - | - | partners/member_profile/partner_benefit_pool/benefit_grant_logs/audit_logs/sys_dict/state_transition_logs → 合伙人管理 + 权益分发全线阻塞 |
| **系统级：order_type 枚举不一致** | **风险** | - | - | DDL CHECK 是小写 (activity/service/tender/report/other)，代码写入 MEMBER_ORDER/tender_download 等 → INSERT 时 CHECK 违反 |
| **系统级：活动审核仅单级** | **偏差** | - | - | 需求要求双级 (一审+二审)，实际 PartnerPublishesV3Controller 为 0→30 单步 |
| **安全：SupplierOnboarding scope** | **致命** | - | - | V3Controller 用 userId 替代 partnerId（scope 违规）|
| **安全：Tenders PUT 绕过状态机** | **致命** | - | - | AdminTendersV3Controller PUT 可直接改 tender_status |
| **已修复：ActivitiesV3Controller** | ✅ | - | 2026-02-28 | audit_status 过滤值从 20 修正为 30 |
| **已修复：OrdersV3Controller** | ✅ | - | 2026-02-28 | 增加 user_id 隔离 + 字段映射 |

## 5. 功能状态矩阵（验收口径）

> DataScope 说明：`orders` 和 `tenders` 已加入 DataScope 白名单（超出 API_Specs_V2.0 原定范围，属代码先于文档的合理扩展，以代码为准）。

| 模块 | 客户需求确认 | 前端状态 | 后端状态 | 当前上线状态 |
|---|---|---|---|---|
| 标书发布/管理 | 已确认 | 已接真实 API | 已可用（主链路） | 可联调验收 |
| 标书引用 | 已确认 | 保留入口，受控降级只读 | 写接口未完成 | 暂不可验收写操作 |
| 服务商名录 | 已确认 | 列表真实、写操作受控降级 | 写接口未完成 | 仅可验收查询/导出 |
| 服务商审核 | 已确认 | 主页面与子页均已接真实审核流，证书能力受控降级 | 部分可用 | 可验收审核主流程 |
| 服务商统计 | 已确认 | 已改真实聚合统计（审核申请口径） | 部分可用 | 可验收统计展示，口径需注明 |
| 采购需求审核 | 已确认 | ✅ 已接入 DemandAuditList + /v3/admin/demands list/review（ad8ddbc） | ✅ GET list + POST review 已实现（0d8df57） | 可联调验收审核主流程 |
| 商机对接管理 | 已确认 | ✅ business.vue 已接入 /v3/admin/demands（0d8df57） | ✅ GET list + POST review 已实现 | 可联调验收 |
| 展示位管理 | 已确认 | display.vue 三 Tab 已添加待接入 alert；写操作受控降级 | 无对应 DDL/接口（OSS 依赖未就绪） | 暂不可验收写操作 |
| 活动管理 | 已确认 | ✅ 列表+partner新建+审核 approve/reject 均已接真实 API（8470688） | ✅ 列表/新建/审核完整；下架/导出受控降级 | 可联调验收主流程 |
| 培训内容管理 | 已确认 | ✅ 已切 /v3/admin/contents?content_type=training；状态流转已接；发布/编辑受控降级 | ✅ /v3/admin/contents 已实现；缺 POST create endpoint（Week2 DoD 阻塞） | 可验收查询/状态切换，创建待实现 |
| 媒体内容管理 | 已确认 | ✅ 已切 /v3/admin/contents?content_type=media；受控降级 | 同培训 | 同培训 |
| 订单管理 | 已确认 | ✅ 已接 /v3/admin/orders（8470688 修正） | ✅ GET list + POST transition 已实现 | 可联调验收 |
| 服务商前台展示 | 已确认 | ✅ services.vue 供应商区块已接 /v3/suppliers（f0e818f） | ✅ SuppliersV3Controller 已实现 | 可联调验收 |
| 会员认证审核 | 已确认 | 主流程已接真实 API；证书上传受控降级 | 部分可用 | 可验收主审核流程 |
| 标书防重复扣费 | 已确认 | 不涉及前端 | ✅ DDL 已补建（tender_download_logs），TenderDownloadLogService + POST /v3/tenders/{id}/download 已实现（含 OrderEntity 写入） | ⚠️ 代码完整，DDL 需在目标 DB 执行 DDL_Phase2_Migration.sql |
| 状态流转审计 | 已确认 | 不涉及前端 | ✅ StateTransitionService 已注入 5 个 Controller（order/tender/member_verification/content/supplier_onboarding） | ⚠️ 代码完整，DDL 需在目标 DB 执行 |
| 供应商入库审核 | 已确认 | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247） | ✅ 后端完整（Entity/Mapper/Service/Controller）（0280d7d） | ⚠️ 可联调验收，DDL 需先执行 |
| 内容管理（培训/媒体） | 已确认 | ✅ 已切换 /v3/admin/contents（1df2247） | ✅ 后端完整（Entity/Mapper/Service/AdminContentsV3Controller）（1df2247） | ⚠️ 可联调验收，DDL 需先执行 |

## 6. ⚠️ 致命风险提醒（优先处理）

| 风险 | 说明 | 动作 |
|---|---|---|
| DDL_Phase2_Migration.sql 执行状态未知 | 9 张 Phase 2 表若未在目标 DB 执行，后端 100% 运行时崩溃 | 在目标 DB 执行 `docs/DDL_Phase2_Migration.sql`（幂等，可安全重复执行）|
| ~~contents POST create 缺失~~ | ~~`POST /v3/admin/contents` 未实现~~ | ✅ 已实现 (978cf74) |
| 7 张 Phase1 表无 Entity | partners/member_profile 等核心表无 Entity/Mapper/Service → 合伙人管理 + 权益分发 + 会员画像全线阻塞 | 创建 7 组 Entity + Mapper + IService（Phase A1 优先执行）|
| order_type 枚举全线不一致 | DDL CHECK 允许小写、代码写大写 → INSERT 崩溃 | 统一枚举值 或 修改 DDL CHECK（Phase A3）|
| 安全漏洞 ×4 | SupplierOnboarding scope 违规 + Tenders PUT 绕过状态机 + 明文密码 + action 空校验 | Phase A4 优先修复 |
| DDL_Gap_Report_v1.md 为空模板 | 占位符未填写 | 以 DDL_Phase2_Migration.sql + AUDIT_Full_System_2026-02-28.md 为权威参考 |

## 7. AI 工具使用提示（Cursor / Continue.dev）

- 优先参考“当前权威文档”生成代码或核实逻辑。
- 涉及历史需求比对时，再引用 `docs/requirements`。
- 权限相关问题必须遵守第 3 节路由权限基线与 `SecurityContextHolder` 约束。
- 回答建议格式：先列符合/冲突点，再给修改方案（必要时附最小 diff）。

## 8. 维护建议

- 每次新增/删除文档后，同步更新本索引。
- 文档命名统一使用 UTF-8 编码与稳定路径，避免中英文混排重命名导致失链。
- 每周联调结束后，在本文件“当前已知漂移”区增删实际状态。
- 每周同步更新“功能状态矩阵”，避免“误删功能”或“假完成”判断。


