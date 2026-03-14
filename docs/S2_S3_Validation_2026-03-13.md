# S2/S3 验证记录 (2026-03-13)

## 2026-03-14 勘误 / 增补

本文档主体记录的是 2026-03-13 当晚的验证结果，保留历史原貌；但截至 2026-03-14，当前仓库与后续执行记录已有八点需要修正：

- 运行基线版本已应更新为 `2026-03-14-phase-ah`，不再以 `2026-03-13-phase-ae` 作为当前阶段标识。
- `admin/finance/pricing.vue` 与 `admin/finance/profit.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/business/packages.vue`、`admin/business/promotions.vue`、`admin/business/roles.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/system/settings.vue` 与 `admin/system/login-config.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/system/certificates.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/system/customer-service.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/system/notifications.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。
- `admin/system/logs.vue` 已在后续阶段完成真实化，不应再纳入“当前降级页面清单”。

按 2026-03-14 当前口径重算：

- 当前降级页面总数应为 **6 页**
- 其中 session-only 为 **4 页**
- 纯信息 / 说明页为 **2 页**
- `pricing.vue` / `profit.vue` / `business/*` / `system/settings|login-config|certificates|customer-service|notifications|logs` 当前状态应改为“真实后端已接入，待浏览器联调补留痕”

## 背景

按 S0→S1→S2→S3→S4 方法论，本文档记录 S2 冒烟测试和 S3 降级清单的正式结果。
此前验证结果仅存在于对话上下文，本文档将其落入仓库以供复核。

---

## S2 冒烟测试

### 执行时间
2026-03-13 22:19 (UTC+8)

### 前置条件
- 后端版本: `2026-03-13-phase-ae` (通过 `/api/v3/runtime-info` 确认)
- Schema preflight: 20/20 全部通过
- DDL Phase 1-18 全部已执行

### 测试方法
使用 curl 对三角色（admin/partner/member）+ 公共端点执行 HTTP 请求，验证返回状态码。

### Admin 角色 (10 端点)

| # | 端点 | HTTP | 说明 |
|---|---|---|---|
| 1 | `GET /v3/admin/members?page=1&page_size=10` | 200 | 返回 3 条会员 |
| 2 | `GET /v3/admin/tenders?page=1&page_size=10` | 200 | |
| 3 | `GET /v3/admin/contents?page=1&page_size=10` | 200 | 含 extra_json 列 |
| 4 | `GET /v3/admin/partners?page=1&page_size=10` | 200 | |
| 5 | `GET /v3/admin/demands?page=1&page_size=10` | 200 | |
| 6 | `GET /v3/admin/orders?page=1&page_size=10` | 200 | |
| 7 | `GET /v3/admin/overseas-points?page=1&page_size=10` | 200 | |
| 8 | `GET /v3/admin/supplier-onboarding?page=1&page_size=10` | 200 | |
| 9 | `GET /v3/partner/publishes?page=1&page_size=10` | 200 | admin 可访问 |
| 10 | `GET /v3/admin/member-verifications?page=1&page_size=10` | 200 | |

### Partner 角色 (1 端点)

| # | 端点 | HTTP | 说明 |
|---|---|---|---|
| 1 | `GET /v3/partner/publishes?page=1&page_size=10` | 200 | 返回 4 条活动 |

> 说明: Partner 角色的核心操作集中在 `/v3/partner/publishes`（活动发布管理），
> 标书/需求/供应商/订单由 admin 统一管理，Partner 不直接操作这些资源。

### Member 角色 (5 端点)

| # | 端点 | HTTP | 说明 |
|---|---|---|---|
| 1 | `GET /v3/member/profile` | 200 | |
| 2 | `GET /v3/member/favorites?page=1&page_size=10` | 200 | |
| 3 | `GET /v3/orders?page=1&page_size=100` | 200 | |
| 4 | `GET /v3/member/demands?page=1&page_size=10` | 200 | |
| 5 | `GET /v3/member/supplier-apply` | 200 | |

### Public 端点 (6 端点)

| # | 端点 | HTTP | 说明 |
|---|---|---|---|
| 1 | `GET /v3/tenders?page=1&page_size=10` | 200 | |
| 2 | `GET /v3/contents?page=1&page_size=10` | 200 | |
| 3 | `GET /v3/activities?page=1&page_size=10` | 200 | |
| 4 | `GET /v3/overseas-points?page=1&page_size=10` | 200 | |
| 5 | `GET /v3/suppliers?page=1&page_size=10` | 200 | |
| 6 | `GET /v3/runtime-info` | 200 | version=2026-03-13-phase-ae |

### S2 结论
**22/22 端点全部返回 200，零 500 错误。** S2 冒烟通过。

### S1 补充验证: 会员 CRUD 持久化闭环

| 步骤 | 操作 | 结果 |
|---|---|---|
| 1 | `POST /v3/admin/members` 创建会员 | 200, 返回 id=4 |
| 2 | `GET /v3/admin/members?page=1&page_size=10` 查列表 | 200, total=3, id=4 出现在列表中 |
| 3 | `GET /v3/admin/members/4` 查详情 | 200, 返回完整会员信息 |

**结论**: "新增会员保存后刷新消失"缺陷已闭环。

---

## S3 降级页面清单

### 统计
- 降级页面总数: **17 页**
- 其中 hybrid (部分真实): 1 页
- 100% session-only: 15 页
- 纯信息展示 (无需 API): 1 页

### Finance 模块 (2 页)

> 以下 Finance 降级结论仅代表 2026-03-13 当晚状态；截至 2026-03-14，`pricing.vue` / `profit.vue` 已不再属于当前降级页。

| 页面 | 降级类型 | Alert 文本 | 优先级 |
|---|---|---|---|
| `admin/finance/profit.vue` | Hybrid | 功能开发中 — 利润分成模块需要独立后端API...统计卡片中的收入数据来自真实订单API | P2 |
| `admin/finance/pricing.vue` | Session-only | 定价配置暂未对接后端API，修改仅在当前会话有效，刷新后将重置 | P2 |

> profit.vue 的统计卡片已接 `/v3/admin/orders/stats` 和 `/v3/admin/partners` 真实 API，
> 但分成配置/结算功能需要新建数据库表和后端 API。

### Business 模块 (3 页)

| 页面 | 降级类型 | Alert 文本 | 优先级 |
|---|---|---|---|
| `admin/business/packages.vue` | Session-only | 业务配置模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置 | P2 |
| `admin/business/promotions.vue` | Session-only | 同上 | P2 |
| `admin/business/roles.vue` | Session-only | 同上 | P2 |

### System 模块 (9 页)

| 页面 | 降级类型 | Alert 文本 | 优先级 |
|---|---|---|---|
| `admin/system/settings.vue` | Session-only | 系统管理模块暂未对接后端API，配置修改仅在当前会话有效，刷新后将重置 | P2 |
| `admin/system/notifications.vue` | Session-only | 同上 | P2 |
| `admin/system/login-config.vue` | Session-only | 同上 | P2 |
| `admin/system/certificates.vue` | Session-only | 同上 | P2 |
| `admin/system/logs.vue` | Session-only | 同上 | P2 |
| `admin/system/customer-service.vue` | Session-only | 同上 | P2 |
| `admin/system/permissions.vue` | Session-only | 同上 | P2 |
| `admin/system/backup.vue` | Session-only | 同上 | P2 |
| `admin/system/about.vue` | 纯信息 | 同上 | N/A |

### Overseas 模块 (3 页)

| 页面 | 降级类型 | Alert 文本 | 优先级 |
|---|---|---|---|
| `admin/overseas/analysis.vue` | Session-only | 功能开发中 — 海外服务模块暂无后端API，当前数据仅为界面演示，不会持久化保存 | P2 |
| `admin/overseas/services.vue` | Session-only | 同上 | P2 |
| `admin/overseas/reports.vue` | Session-only | 同上 | P2 |

### S3 结论

若仅按 2026-03-13 当晚状态，所有 17 个降级页面均已有 `<el-alert>` 明确告知用户当前限制。
这些页面属于**内部配置/运营管理**功能，不影响核心业务流程（标书、会员、活动、内容、订单）。
后续实现需要新建数据库表和后端 API，归入 P2 优先级。

---

## 总结

| 阶段 | 状态 | 证据 |
|---|---|---|
| S0 基线收敛 | ✅ 完成 | Schema 20/20, BUILD SUCCESS, DDL Phase 1-18 |
| S1 500 清零 | ✅ 完成 | 7/7 Runbook 端点 + 会员 CRUD 持久化闭环 |
| S2 冒烟 | ✅ 完成 | 22/22 端点全部 200 (本文档详细清单) |
| S3 降级清单 | ✅ 已盘点 | 17 页降级，全部有 alert 标注，全部 P2 |
| S4 UAT 交接 | 待执行 | 需准备测试数据和用户验收流程 |
