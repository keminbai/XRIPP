# 深度回归验证记录（2026-03-14）

## 1. 背景

在 40/40 GET 冒烟全部通过后，进行更深层的 CRUD + 状态转换 + 多角色隔离验证，确保系统不仅"能读"，还"能写、能转换、能隔离"。

## 2. 验证结论

全部关键路径验证通过，发现并修复了 1 个真实生产 bug。

### 2.1 CRUD 写入验证

| 操作 | 结果 | 备注 |
|---|---|---|
| Content CREATE (activity) | PASS | 需要 `content_type` 字段（非 `type`） |
| Tender CREATE | PASS | 需要 `organization` 字段（非 `organization_name`） |
| Content PUBLISH (draft→published) | PASS | 修复 audit_logs CHECK 约束后通过 |
| Content OFFLINE (published→offline) | PASS | 同上 |
| Notification template CREATE | PASS | type 仅支持 `payment/activity/urgent` |

### 2.2 多角色隔离验证

| 角色 | 调用 Admin API | 结果 |
|---|---|---|
| admin | 所有 admin 端点 | PASS（200） |
| partner (p1001) | admin 端点 | AUTH_FORBIDDEN ✓ |
| member (m1001) | admin 端点 | AUTH_FORBIDDEN ✓ |

### 2.3 Member 端 API 验证

| 端点 | 结果 | 数据 |
|---|---|---|
| `/v3/member/benefits/usage` | 200 OK | 配额/用量数据正确 |
| `/v3/member/customer-service/tickets` | 200 OK | 工单列表正常 |

## 3. 修复的 Bug

### audit_logs CHECK 约束过窄（DDL Phase 25）

- **现象**: Content 发布（draft→published）返回 500
- **根因**: `audit_logs.action` 列的 CHECK 约束仅允许 `submit/approve/reject/offline`，但 AdminContentsV3Controller 的 transition 接口会将 `to_status`（如 `published`）写入 `action` 列
- **修复**: 扩展 CHECK 约束，新增 `published/created/draft/update/delete`
- **DDL**: `docs/DDL_Phase25_AuditLogs_CheckFix.sql`
- **状态**: 已在数据库执行，DDL 脚本已留档

## 4. 澄清的"假问题"

| 疑似问题 | 真实情况 |
|---|---|
| Member benefits 500 | URL 测错：正确路径是 `/v3/member/benefits/usage`（有 `/usage` 子路径） |
| Member customer-service 500 | 同上：正确路径是 `/v3/member/customer-service/tickets` |
| Content DELETE 500 | 非 bug：前端通过 transition 接口做下线（`to_status:'offline', reason:'delete'`），不用 HTTP DELETE |

## 5. 科学判断

截至本次深度回归，系统验证层次已从"GET 冒烟"提升到"CRUD + 状态转换 + 角色隔离"：

1. **读**: 40/40 GET 端点全部通过
2. **写**: Content/Tender/Notification 创建全部通过
3. **状态流转**: 发布/下线全链路通过（修复 bug 后）
4. **权限隔离**: 三种角色隔离验证通过
5. **Member 端**: 核心 Member API 真实可用

当前系统已确认可进入真实业务 UAT 实测阶段。
