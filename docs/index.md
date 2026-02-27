# XRIPP 文档索引

本文件是 `docs/` 目录的统一入口，用于：
- 快速定位需求来源、设计约束、接口契约和执行计划
- 明确“当前权威文档”与“历史参考文档”
- 减少前后端联调时的路径错误和口径不一致

最后更新：2026-02-27（Claude Code 第四轮更新：contents 后端落地、supplier_onboarding 前端修正、标书 DDL 漂移补录、tender_download_logs 防重复扣减实现）

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
| `app/pages/admin/audit.vue` | 部分实现 | - | - | 服务商与内容审核已接入；采购需求审核写接口未接入（显式提示） |
| `app/pages/admin/suppliers/list.vue` | 部分实现 | - | - | 已切真实列表；新增/编辑/资料上传写接口未接入，当前受控降级 |
| `app/pages/admin/suppliers/audit.vue` | 部分实现 | - | - | 主数据与审核流已切真实接口；证书上传/下载接口未接入，受控降级 |
| `app/pages/admin/suppliers/analysis.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247）；入驻申请口径，非运营主表 suppliers |
| `app/pages/admin/suppliers/list.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247）；写操作受控降级 |
| `app/pages/admin/content/activities.vue` | 部分实现 | - | - | 已切真实列表（/v3/partner/publishes）；partner 新建已接真实接口；状态切换/展示申请/导出受控降级 |
| `app/pages/admin/content/trainings.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/contents?content_type=training（1df2247）；发布/编辑受控降级 |
| `app/pages/admin/content/media.vue` | 部分实现 | - | - | ✅ 已切换 /v3/admin/contents?content_type=media（1df2247）；发布/编辑受控降级 |

## 5. 功能状态矩阵（验收口径）

> DataScope 说明：`orders` 和 `tenders` 已加入 DataScope 白名单（超出 API_Specs_V2.0 原定范围，属代码先于文档的合理扩展，以代码为准）。

| 模块 | 客户需求确认 | 前端状态 | 后端状态 | 当前上线状态 |
|---|---|---|---|---|
| 标书发布/管理 | 已确认 | 已接真实 API | 已可用（主链路） | 可联调验收 |
| 标书引用 | 已确认 | 保留入口，受控降级只读 | 写接口未完成 | 暂不可验收写操作 |
| 服务商名录 | 已确认 | 列表真实、写操作受控降级 | 写接口未完成 | 仅可验收查询/导出 |
| 服务商审核 | 已确认 | 主页面与子页均已接真实审核流，证书能力受控降级 | 部分可用 | 可验收审核主流程 |
| 服务商统计 | 已确认 | 已改真实聚合统计（审核申请口径） | 部分可用 | 可验收统计展示，口径需注明 |
| 采购需求审核 | 已确认 | 入口保留+显式待接入 | 写接口未完成 | 暂不可验收 |
| 活动管理 | 已确认 | 已切真实列表；partner 新建已接；审核/下架/导出受控降级 | 列表/新建部分可用，缺 admin 管理接口 | 可验收 partner 发布流程 |
| 培训内容管理 | 已确认 | 已切真实列表（临时借用 tenders 口径）；状态流转已接 | 无专用 /v3/contents 接口，用 tenders 临时代替 | 暂不可正式验收，待 /v3/contents 实现 |
| 媒体内容管理 | 已确认 | 同培训，已切真实列表，受控降级 | 同上 | 同上 |
| 订单管理 | 已确认 | 列表已接真实 API | 已可用（列表+状态流转） | 可联调验收 |
| 会员认证审核 | 已确认 | 主流程已接真实 API；证书上传受控降级 | 部分可用 | 可验收主审核流程 |
| 标书防重复扣费 | 已确认 | 不涉及前端 | ✅ DDL 已补建（tender_download_logs），TenderDownloadLogService + POST /v3/tenders/{id}/download 已实现（含 OrderEntity 写入） | ⚠️ 代码完整，DDL 需在目标 DB 执行 DDL_Phase2_Migration.sql |
| 状态流转审计 | 已确认 | 不涉及前端 | ✅ StateTransitionService 已注入 5 个 Controller（order/tender/member_verification/content/supplier_onboarding） | ⚠️ 代码完整，DDL 需在目标 DB 执行 |
| 供应商入库审核 | 已确认 | ✅ 已切换 /v3/admin/supplier-onboarding（1df2247） | ✅ 后端完整（Entity/Mapper/Service/Controller）（0280d7d） | ⚠️ 可联调验收，DDL 需先执行 |
| 内容管理（培训/媒体） | 已确认 | ✅ 已切换 /v3/admin/contents（1df2247） | ✅ 后端完整（Entity/Mapper/Service/AdminContentsV3Controller）（1df2247） | ⚠️ 可联调验收，DDL 需先执行 |

## 6. AI 工具使用提示（Cursor / Continue.dev）

- 优先参考“当前权威文档”生成代码或核实逻辑。
- 涉及历史需求比对时，再引用 `docs/requirements`。
- 权限相关问题必须遵守第 3 节路由权限基线与 `SecurityContextHolder` 约束。
- 回答建议格式：先列符合/冲突点，再给修改方案（必要时附最小 diff）。

## 7. 维护建议

- 每次新增/删除文档后，同步更新本索引。
- 文档命名统一使用 UTF-8 编码与稳定路径，避免中英文混排重命名导致失链。
- 每周联调结束后，在本文件“当前已知漂移”区增删实际状态。
- 每周同步更新“功能状态矩阵”，避免“误删功能”或“假完成”判断。


