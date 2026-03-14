# 出海发布系统真实化完成记录（2026-03-14）

## 1. 目标与结论

本阶段完成 `admin/overseas/analysis.vue`、`admin/overseas/services.vue`、`admin/overseas/reports.vue` 的真实化收口，结束此前“页面可操作但刷新即丢失”的 session-only 状态。

本次结论：

- `services` 已具备真实 CRUD、文件上传、附件持久化、状态切换能力
- `reports` 已具备真实 CRUD、封面/文件上传、下载计数、到期时间设置能力
- `analysis` 已改为真实聚合，不再依赖前端 mock 数组
- 海外模块已补进 RBAC 权限口径，不再默认只有 super-admin 可见

## 2. 数据模型

新增 DDL：

- [DDL_Phase24_Overseas_Realization.sql](./DDL_Phase24_Overseas_Realization.sql)

新增表：

- `overseas_services`
  - 服务主数据：类型、项目、标题、国家、摘要、正文、价格、联系人、状态、浏览/咨询统计、发布时间
- `overseas_service_files`
  - 服务附件明细：宣传图、服务图片、介绍文件
- `overseas_reports`
  - 报告主数据：国家、行业、类型、年份、发布时间、摘要、封面、报告文件、关键词、来源、收费级别、访问级别、推荐、下载量、到期日

设计原则：

- 不再用前端 fileList 或临时 JSON 充当真实数据源
- `analysis` 不建独立统计表，统一从真实业务表聚合
- 海外模块文件采用“主表 + 明细表”结构，保证后台复查与后续公共端复用

## 3. 后端改动

新增实体/Mapper/Service：

- `OverseasService`
- `OverseasServiceFile`
- `OverseasReport`

新增控制器：

- `GET/POST/PUT/DELETE /v3/admin/overseas/services`
- `GET /v3/admin/overseas/services/{id}`
- `GET/POST/PUT/DELETE /v3/admin/overseas/reports`
- `GET /v3/admin/overseas/reports/{id}`
- `POST /v3/admin/overseas/reports/{id}/download`
- `POST /v3/admin/overseas/reports/{id}/expire`
- `GET /v3/admin/overseas/analysis`

补充工程接入：

- `DatabaseSchemaPreflightRunner` 新增 Phase 24 表/列检查
- `AdminPermissionGuard` 新增 `/api/v3/admin/overseas*` 权限守卫
- `AdminPermissionGuard` 增加 `/download` -> `export` 动作映射

## 4. 前端改动

新增公共口径文件：

- `app/composables/useOverseasAdminCatalog.ts`

重写页面：

- `app/pages/admin/overseas/services.vue`
  - 真实列表查询、分页、编辑、删除、上下架
  - 宣传图/服务图片/介绍文件上传走 `/api/common/upload`
  - 文件元数据回写到真实后端
- `app/pages/admin/overseas/reports.vue`
  - 真实列表查询、分页、编辑、删除
  - 封面与报告文件上传持久化
  - 下载动作写回下载计数
  - 到期日期设置走真实接口
- `app/pages/admin/overseas/analysis.vue`
  - 卡片、柱图、饼图、趋势图、行业分布表全部来自真实接口

权限前端接入：

- `useAdminPermissionSnapshot.ts`
  - 新增 `overseas_management` 模块目录
  - 新增 `/admin/overseas` 路由规则
  - 新增海外模块 fallback 路由候选

## 5. 真实口径说明

`analysis` 当前统计口径：

- 服务网点：`overseas_points.status = active`
- 服务企业：`overseas_services` 中非草稿记录，按 `contact_email -> contact_phone -> title -> id` 去重
- 覆盖国家：活跃网点国家去重
- 行业报告：`overseas_reports` 总数
- 趋势：基于服务发布时间/月度新增，累计值保留窗口前历史基数

## 6. 权限与访问边界

Phase 24 同时补入 `overseas_management` 权限模块：

- `CONTENT_PUBLISHER` 默认拥有 `view/create/edit/delete/export`
- `CONTENT_AUDITOR` 默认拥有 `view/export`

这样做的原因：

- 避免海外模块继续落在“未配置路由规则 -> 仅 super-admin 可见”的隐性状态
- 保持 Phase 23 RBAC 口径一致，不做特例穿透

## 7. 验证情况

本地已完成：

- 代码级人工复核
- Nuxt 构建尝试

本地受环境限制未完成：

- `mvn compile` / `mvn test`
  - WSL 当前无 `java`，且 `JAVA_HOME` 未配置
- `npm run build`
  - 失败原因仍为既有环境问题：缺失 `@oxc-parser/binding-linux-x64-gnu`
  - 当前没有证据表明失败由本阶段代码语法引起

建议后续由 Windows 侧继续验证：

1. 执行 `docs/DDL_Phase24_Overseas_Realization.sql`
2. 启动后端验证新接口
3. 启动前端验证三页 CRUD/上传/下载/统计

## 8. 当前项目口径更新

截至本阶段：

- `overseas/*` 3 个 session-only 页面已全部清零
- 当前剩余“未接业务后端”的页面应仅剩 2 页：
  - `admin/system/backup.vue`
  - `admin/system/about.vue`

其中：

- 这 2 页都属于说明页，不应再统计为 session-only 业务页
- 当前业务型 session-only 页面数应为 0
