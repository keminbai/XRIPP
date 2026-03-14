# Customer Service 闭环记录（2026-03-14）

## 1. 本轮目标

将以下两页从 mock / session-only 状态改为真实业务闭环：

- `app/pages/member/feedback.vue`
- `app/pages/admin/system/customer-service.vue`

同时补齐后端独立业务模型，明确该模块**不进入 Phase 20 配置底座**，而是采用独立表 + 独立 API 的方式实现。

## 2. 架构判断

`customer-service` 的真实边界不是“配置页”，而是“留言 + 工单 + 附件 + 后台处理”的业务模块。

因此本轮采用：

- 独立 DDL：`DDL_Phase21_CustomerService.sql`
- 独立表：
  - `customer_service_messages`
  - `customer_service_tickets`
  - `customer_service_ticket_files`
- 独立控制器：
  - `MemberCustomerServiceV3Controller`
  - `AdminCustomerServiceV3Controller`

未采用：

- 将工单/留言塞进 `admin_config_entries`
- 将附件继续藏在前端会话态或零散 JSON 中

## 3. 后端完成项

### 3.1 DDL 与启动护栏

- 新增 `docs/DDL_Phase21_CustomerService.sql`
- `DatabaseSchemaPreflightRunner` 新增 Phase 21 表/列检查
- 未执行 DDL 时，后端启动会 fail-fast，而不是运行期再出现 500

### 3.2 业务实体

- `CustomerServiceMessage`
- `CustomerServiceTicket`
- `CustomerServiceTicketFile`

### 3.3 会员端 API

- `GET /v3/member/customer-service/tickets`
- `GET /v3/member/customer-service/tickets/{id}`
- `POST /v3/member/customer-service/tickets`
- `POST /v3/member/customer-service/messages`

### 3.4 管理端 API

- `GET /v3/admin/customer-service/overview`
- `GET /v3/admin/customer-service/messages`
- `POST /v3/admin/customer-service/messages/{id}/handle`
- `GET /v3/admin/customer-service/tickets`
- `GET /v3/admin/customer-service/tickets/{id}`
- `POST /v3/admin/customer-service/tickets`
- `PUT /v3/admin/customer-service/tickets/{id}`
- `DELETE /v3/admin/customer-service/tickets/{id}`

## 4. 前端完成项

### 4.1 会员端 `member/feedback.vue`

- 工单列表改为真实 API 加载
- 新建工单改为真实提交
- 工单详情改为真实读取
- 上传截图继续走 `/api/common/upload`
- 上传回执中的 `url/fileName/fileExt/fileSize/contentType` 会随工单一起持久化
- 新增“快速留言”入口，真实写入后台留言记录

### 4.2 后台 `admin/system/customer-service.vue`

- 去除“当前会话有效”的降级提示
- 统计卡改为真实读取 `/overview`
- 留言记录改为真实查询与处理
- 工单列表改为真实查询
- 工单处理弹窗改为真实详情编辑
- 工单详情支持会员资料查看
- 附件支持真实预览/下载
- 删除操作改为真实 `DELETE`

## 5. 权限与边界

- 会员端只允许基于 `SecurityContextHolder.getCurrentUserId()` 操作本人数据
- 不接受前端透传 `user_id` 作为会员端权限依据
- 管理端当前为 `admin only`
- 后台手工新建工单允许 `user_id` 为空，避免模型被“必须绑定会员”做死

## 6. 当前状态

`admin/system/customer-service.vue` 已不再属于当前降级页。

### 2026-03-14 增补

以下统计为 customer-service 完成当时的阶段估算，保留历史原貌；
但后续 `notifications/logs/permissions/overseas/*` 已继续真实化，因此下面的“剩余 8 页”不再适用于当前仓库。

按当前口径重算：

- 业务型 `session-only`：**0 页**
- 说明页：**2 页**
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

截至 2026-03-14 当前口径，剩余降级页应收缩为：

- session-only：7 页
  - `admin/system/notifications.vue`
  - `admin/system/permissions.vue`
  - `admin/system/logs.vue`
  - `admin/system/backup.vue`
  - `admin/overseas/analysis.vue`
  - `admin/overseas/services.vue`
  - `admin/overseas/reports.vue`
- pure info：1 页
  - `admin/system/about.vue`

合计：**8 页**

## 7. 仍待补强项

- 未运行本地 `mvn compile` / `mvn test`
  - 原因：本轮按协作约定，命令执行需单独批准；当前为代码与静态审查收口
- 仍需 Windows 运行环境按 runbook 补一轮：
  - DDL Phase 21 执行
  - 后端重启
  - 页面操作序列验证
  - 必要时补浏览器截图留痕

## 8. 结论

本轮实现后，`customer-service` 已从“展示型假页面”提升为“真实业务闭环”：

- 会员可提工单、看工单、留留言
- 后台可查留言、处理留言、查工单、处理工单、删工单
- 附件已结构化持久化
- 模块边界与数据模型已独立成立，不依赖配置表投机落地
