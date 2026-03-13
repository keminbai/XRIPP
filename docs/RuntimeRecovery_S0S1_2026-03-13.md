# Runtime Recovery S0/S1（2026-03-13）

## 1. 目标

把当前系统从“代码仓库看起来已闭环，但真实运行环境仍连续 500”收敛到“运行实例、数据库 schema、仓库代码三者一致”。

这不是页面优化阶段，而是运行基线恢复阶段。

## 2. 已确认的事实

### 2.1 当前 500 不是同一类问题

来自 `/mnt/d/ipp-platform/ipp-backend/logs/xripp-backend.log` 的直接证据：

- 2026-03-13 20:33 左右
  - `POST /v3/admin/members`
  - 报错：`Request method 'POST' is not supported`
  - 结论：当前运行中的后端实例不是最新代码，至少没有加载 `AdminMembersV3Controller` 的 `@PostMapping`
- 2026-03-13 20:36 左右
  - `GET /v3/admin/contents?page=1&page_size=100`
  - 报错：`invalid column 'extra_json'`
  - 结论：当前运行库缺少 `docs/DDL_Phase18_Contents_ExtraJson.sql`
- 2026-03-13 20:37 左右
  - `GET /v3/partner/publishes?page=1&page_size=10`
  - 报错：`invalid object name 'activity_records'`
  - 结论：当前运行库缺少 `docs/DDL_Phase17_ActivityRecords.sql`

### 2.2 另外两条重要推断

- `GET /v3/admin/members` 已能正常查询 `member_profile.member_level`
  - 说明 `docs/DDL_Phase2_Migration.sql` 至少在当前运行库里已部分生效
- `GET /v3/partner/publishes` 已能查询 `activities.activity_no/front_module/front_position/...`
  - 说明 `docs/DDL_Phase14_Activities_Closure.sql` 大概率已在当前运行库存在

因此，当前最直接的 schema 漂移集中在：

- `Phase 17`
- `Phase 18`

同时存在一个独立问题：

- 运行中的后端进程不是当前仓库代码版本

## 3. 根因判断

当前系统不是“控制器写坏了”，而是两个系统级问题叠加：

1. 运行实例漂移
   - Windows 侧正在提供服务的后端不是当前仓库代码，导致新接口不存在
2. 数据库 schema 漂移
   - 当前运行库没有补齐后续 DDL，导致代码一访问新增表/列就 500

这类问题不能靠删除字段、回退实体、注释查询来“暂时修通”。
那会把已经完成的闭环重新做假。

## 4. 必须坚持的修复原则

- 不移除 `extra_json`
- 不移除 `activity_records`
- 不回退会员 CRUD 到前端假保存
- 不以“先能打开页面”为目标做表面绕过
- 先统一运行实例与 schema，再继续页面层优化

## 5. 恢复顺序

### 步骤 A：统一后端运行版本

目标：

- 停止当前旧后端实例
- 用当前仓库代码重新构建并启动后端

原因：

- 只有当前仓库代码才包含：
  - `AdminMembersV3Controller` 的真实写接口
  - `DatabaseSchemaPreflightRunner`

预期结果：

- 如果数据库仍缺表/缺列，后端应在启动期直接 fail fast
- 不再等用户点页面才看到 500
- 可通过 `GET /api/v3/runtime-info` 或响应头确认当前实例身份：
  - `X-XRIPP-Runtime-Version`
  - `X-XRIPP-Schema-Preflight`

### 步骤 B：执行缺失 DDL

至少补齐：

- `docs/DDL_Phase17_ActivityRecords.sql`
- `docs/DDL_Phase18_Contents_ExtraJson.sql`

建议同时复核：

- `docs/DDL_Phase2_Migration.sql`
- `docs/DDL_Phase14_Activities_Closure.sql`
- `docs/DDL_Phase15_ActivityDisplayApplications.sql`

原因：

- 当前预检器也覆盖了这些依赖
- 即使今天尚未触发对应页面，启动期也应该保证它们齐全

### 步骤 C：启动后端并观察预检

启动成功标准：

- 启动日志出现 `[SchemaPreflight] passed`
- 不再出现 `Database schema preflight failed`
- `GET /api/v3/runtime-info` 返回当前仓库约定版本号

### 步骤 D：做最小冒烟

按以下顺序验证：

1. `GET /api/v3/admin/members?page=1&page_size=10`
2. `POST /api/v3/admin/members`
3. `GET /api/v3/admin/contents?page=1&page_size=100`
4. `GET /api/v3/partner/publishes?page=1&page_size=10`
5. `GET /api/v3/admin/contents?content_type=training&page=1&page_size=10`
6. `GET /api/v3/admin/contents?content_type=media&page=1&page_size=10`
7. `GET /api/v3/admin/contents?content_type=carousel&page=1&page_size=20`

验收标准：

- 无 500
- 新增/编辑后刷新仍在
- 相关列表字段能正常返回

## 6. 当前阶段结论

现在不适合让同事做“全面实测”。

更合理的阶段目标是：

- 先完成 S0：运行实例 + schema 收敛
- 再完成 S1：系统性 500 清零
- 然后才进入三角色主链路实测

## 7. 配套脚本

可先执行：

- `docs/SQL_Runtime_Schema_Check_2026-03-13.sql`

用途：

- 在 SQL Server 中只读核验关键表/列是否存在
- 帮助快速确认当前库与代码是否一致
