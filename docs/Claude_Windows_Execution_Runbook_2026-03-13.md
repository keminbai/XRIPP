# Claude Windows Execution Runbook（2026-03-13）

## 1. 目的

本手册给 Windows 环境下的 Claude Code 使用。

当前阻断不在业务代码本身，而在：

- Windows 侧后端运行实例不是当前仓库最新版本
- SQL Server `XRIPP_CORE` 缺少部分后续 DDL

因此，Claude 在 Windows 侧的职责应限定为：

1. 核验当前运行库 schema
2. 执行缺失 DDL
3. 重启后端到当前仓库版本
4. 做最小接口验证

不要在这一步继续做页面层“美化”或“空壳页降级”。

## 2. 已确认的直接根因

来自后端日志 `/mnt/d/ipp-platform/ipp-backend/logs/xripp-backend.log`：

- `POST /api/v3/admin/members`
  - 返回 `Request method 'POST' is not supported`
  - 结论：当前运行中的后端不是最新代码
- `GET /api/v3/admin/contents?page=1&page_size=100`
  - 报 `invalid column 'extra_json'`
  - 结论：缺少 `docs/DDL_Phase18_Contents_ExtraJson.sql`
- `GET /api/v3/partner/publishes?page=1&page_size=10`
  - 报 `invalid object name 'activity_records'`
  - 结论：缺少 `docs/DDL_Phase17_ActivityRecords.sql`

## 3. 先执行的 SQL 自检

优先执行：

- `docs/SQL_Runtime_Schema_Check_2026-03-13.sql`

目标：

- 快速确认 `Phase 2 / 14 / 15 / 17 / 18` 是否齐全

如果工具链允许，推荐：

```powershell
sqlcmd -S localhost,1433 -U sa -P "<your-password>" -d XRIPP_CORE -i "D:\ipp-platform\docs\SQL_Runtime_Schema_Check_2026-03-13.sql"
```

如果没有 `sqlcmd`，就用 SSMS 连接 `XRIPP_CORE` 手动执行。

## 4. 必须补齐的 DDL

至少执行：

1. `docs/DDL_Phase17_ActivityRecords.sql`
2. `docs/DDL_Phase18_Contents_ExtraJson.sql`

建议顺带复核并按需执行：

1. `docs/DDL_Phase2_Migration.sql`
2. `docs/DDL_Phase14_Activities_Closure.sql`
3. `docs/DDL_Phase15_ActivityDisplayApplications.sql`

如果使用 `sqlcmd`，可按单文件逐个执行，不要自行改写字段名来“临时绕过”。

## 5. 后端重启目标

目标不是“让 8080 有服务”，而是“让 8080 跑当前仓库版本”。

当前仓库已经新增：

- 启动期 schema 预检：`DatabaseSchemaPreflightRunner`
- 运行信息接口：`GET /api/v3/runtime-info`
- 响应头：
  - `X-XRIPP-Runtime-Version`
  - `X-XRIPP-Schema-Preflight`

当前仓库约定运行版本号：

- `2026-03-13-phase-ae`

如果 Windows 侧使用 Maven Wrapper，可在后端目录执行类似：

```powershell
cd D:\ipp-platform\ipp-backend
.\mvnw.cmd -DskipTests spring-boot:run
```

如果是打包后运行 jar，也可以，但前提是产物必须来自当前仓库代码。

## 6. 重启后必须做的核验

### 6.1 先看运行实例身份

浏览器或 curl 访问：

```text
GET http://localhost:8080/api/v3/runtime-info
```

必须确认：

- `runtimeVersion = 2026-03-13-phase-ae`
- `schemaPreflightEnabled = true`

同时在任意接口响应头里确认：

- `X-XRIPP-Runtime-Version: 2026-03-13-phase-ae`
- `X-XRIPP-Schema-Preflight: true`

### 6.2 再做最小接口验证

按顺序验证：

1. `GET /api/v3/admin/members?page=1&page_size=10`
2. `POST /api/v3/admin/members`
3. `GET /api/v3/admin/contents?page=1&page_size=100`
4. `GET /api/v3/partner/publishes?page=1&page_size=10`
5. `GET /api/v3/admin/contents?content_type=training&page=1&page_size=10`
6. `GET /api/v3/admin/contents?content_type=media&page=1&page_size=10`
7. `GET /api/v3/admin/contents?content_type=carousel&page=1&page_size=20`

验收标准：

- 无 500
- `POST /api/v3/admin/members` 不再报 method not supported
- `admin/contents` 不再报 `extra_json` 缺失
- `partner/publishes` 不再报 `activity_records` 缺失

## 7. 禁止事项

- 不删除 `extra_json`
- 不删除 `activity_records`
- 不回退 `AdminMembersV3Controller` 的真实 CRUD
- 不用“注释查询字段”方式掩盖缺失 DDL
- 不在这一步继续做首页静态卡片或空壳页面优化

## 8. 完成后回传信息

请 Claude 回传这 4 项：

1. SQL 自检结果
2. 实际执行了哪些 DDL
3. `/api/v3/runtime-info` 返回值
4. 7 个最小接口验证结果
