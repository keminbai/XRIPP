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
3. `docs/DDL_Phase20_AdminConfigs.sql`（若当前要联调 `admin/finance/pricing.vue`）

建议顺带复核并按需执行：

1. `docs/DDL_Phase2_Migration.sql`
2. `docs/DDL_Phase14_Activities_Closure.sql`
3. `docs/DDL_Phase15_ActivityDisplayApplications.sql`
4. `docs/DDL_Phase19_ProfitSharing.sql`（若当前要联调 `admin/finance/profit.vue`）

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

- `2026-03-14-phase-ah`

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

- `runtimeVersion = 2026-03-14-phase-ah`
- `schemaPreflightEnabled = true`

同时在任意接口响应头里确认：

- `X-XRIPP-Runtime-Version: 2026-03-14-phase-ah`
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

## 9. 利润分成模块补充验证

若利润分成模块已执行 `docs/DDL_Phase19_ProfitSharing.sql` 并完成重启，还需要补齐以下 3 个接口的实测。

### 9.1 补测目标

此前已完成的仅是核心冒烟：
- `GET /v3/admin/profit-sharing/configs`
- `POST /v3/admin/profit-sharing/configs`
- `GET /v3/admin/profit-sharing/stats`
- `GET /v3/admin/profit-sharing/records`

仍需补测：
1. `PUT /v3/admin/profit-sharing/configs/{id}`
2. `DELETE /v3/admin/profit-sharing/configs/{id}`
3. `POST /v3/admin/profit-sharing/settlements`

### 9.2 建议验证顺序

1. 登录获取 admin token
2. `GET /configs` 找到现有配置 id 和 partnerId
3. 用该记录做一次 `PUT`，例如修改：
   - `percentage`
   - `business_lines`
   - `enabled`
4. 再次 `GET /configs`，确认修改结果已持久化
5. 选择一条测试配置做 `DELETE`
6. 再次 `GET /configs`，确认该记录已消失
7. 选择一条仍然存在的配置，调用 `POST /settlements`
8. 再次 `GET /stats`，确认该合伙人在对应月份出现已结算状态

### 9.3 验收标准

- `PUT` 返回 200，且修改值可重新查询到
- `DELETE` 返回 200，且删除后不再出现在配置列表
- `POST /settlements` 返回 200，且 `GET /stats` 中对应月份 `settled=true`
- 全流程无 500

### 9.4 回传要求

请额外回传：
- 参与测试的配置 id / partnerId
- `PUT` 前后关键字段差异
- `DELETE` 后的列表确认结果
- `POST /settlements` 的返回体与对应 `GET /stats` 结果

## 10. 定价与利润分成页面联调补充

本节是 2026-03-14 新增要求。目的不是重复做 API 冒烟，而是补齐浏览器页面级闭环留痕。

### 10.1 `admin/finance/pricing.vue`

页面目标：

- 确认四类配置均能真实加载
- 页面保存后刷新不丢失
- 删除后刷新仍保持删除结果

建议验证顺序：

1. 打开 `http://localhost:3000/admin/finance/pricing`
2. 会员费定价：
   - 新增一个测试档位
   - 刷新页面，确认仍存在
3. 标书定价：
   - 编辑一条现有记录价格
   - 刷新页面，确认价格已持久化
4. 培训定价：
   - 切换一条记录状态
   - 刷新页面，确认状态保持
5. 服务定价：
   - 新增一条测试记录后删除
   - 刷新页面，确认已删除
6. 如条件允许，再用接口复核：
   - `GET /api/v3/admin/configs/pricing`

验收标准：

- 页面无 500 / 无保存假成功
- 每次保存后刷新仍能读回
- 删除后不会“刷新复活”

### 10.2 `admin/finance/profit.vue`

页面目标：

- 分成配置 CRUD 可在页面完整走通
- 统计与明细可正常加载
- 结算动作行为符合业务预期

建议验证顺序：

1. 打开 `http://localhost:3000/admin/finance/profit`
2. 分成配置 Tab：
   - 新增一条测试配置
   - 编辑该配置的比例或业务线
   - 刷新页面，确认修改保留
   - 删除测试配置
3. 分成统计 Tab：
   - 点击刷新，确认列表正常加载
   - 如存在可结算记录，执行一次结算
4. 分成明细 Tab：
   - 选择城市或月份查询
   - 确认明细列表正常返回

关键说明：

- 若 `POST /settlements` 因当月无可结算记录返回 `VALIDATION_ERROR` / “no profit records found for settlement month”，这属于正确的业务拒绝，不应算页面缺陷
- 真正的缺陷是：
  - 页面 500
  - 配置保存后刷新丢失
  - 删除后仍残留
  - 有记录却无法结算

回传要求：

1. `pricing.vue` 四类操作分别是否通过
2. `profit.vue` 配置新增 / 编辑 / 删除是否通过
3. 统计页是否正常加载
4. 结算动作结果：
   - 成功结算，或
   - 正确业务拒绝（无记录）
5. 页面级截图或关键现象摘要

### 10.3 `admin/business/*`

当前应验证 3 页：

- `admin/business/packages.vue`
- `admin/business/promotions.vue`
- `admin/business/roles.vue`

验证目标：

- 页面保存后刷新不丢失
- 不再出现“当前会话有效，刷新重置”的旧行为

建议验证顺序：

1. `packages.vue`
   - 调整一个套餐权益值并保存
   - 刷新页面，确认值仍在
   - 切换启停状态并刷新确认
2. `promotions.vue`
   - 新增一条规则
   - 编辑该规则
   - 复制该规则
   - 删除测试规则
   - 每一步至少刷新一次确认
3. `roles.vue`
   - 修改一个角色的数据范围或权限项
   - 点击单条保存
   - 刷新页面确认保留
   - 再执行一次“批量保存所有配置”
   - 刷新页面确认变更记录表也有留痕

回传要求：

- 3 页分别是否通过
- 是否存在“保存成功但刷新丢失”
- `roles.vue` 变更记录是否同步持久化

### 10.4 `admin/system/settings.vue` / `login-config.vue`

当前只验证本轮已真实化的两页：

- `admin/system/settings.vue`
- `admin/system/login-config.vue`

注意：

- `notifications.vue` / `customer-service.vue` / `permissions.vue` / `logs.vue` / `backup.vue` 仍不在本轮真实化范围

建议验证顺序：

1. `settings.vue`
   - 修改基础设置并保存
   - 修改邮件配置并保存
   - 修改安全设置并保存
   - 刷新页面，确认三类配置都能回读
2. `login-config.vue`
   - 修改基础配置并保存
   - 修改安全策略并保存
   - 修改一个第三方登录 provider 开关或 AppId 并保存
   - 刷新页面，确认三类配置都能回读

回传要求：

- 两页分别是否通过
- 是否存在某个 Tab 能保存、另一个 Tab 刷新丢失
- 页面是否出现 500 或保存假成功
