# Notifications 闭环记录（2026-03-14）

## 1. 本轮目标

将 `app/pages/admin/system/notifications.vue` 从 session-only mock 页改为真实后台通知中心。

实现范围：

- 通知类型设置
- 通知模板管理
- 批量发送动作记录
- 发送记录查询

## 2. 架构判断

`notifications` 不是简单配置页，也不应直接塞进 `Phase 20`。

原因：

- 既有“通知类型设置”这类稳定配置
- 也有“通知模板”这类独立业务对象
- 还有“发送记录”这类操作留痕

因此本轮采用独立业务模型：

- `notification_type_settings`
- `notification_templates`
- `notification_send_logs`

## 3. 关键边界

本轮明确区分两件事：

1. 通知中心真实化
- 设置、模板、发送记录全部真实落库

2. 第三方网关未接通
- 当前 `POST /v3/admin/notifications/send` 的语义是：
  - 接受发送请求
  - 写入发送记录
  - 标记处理结果
- 当前不是：
  - 真实短信平台
  - 真实邮件平台
  - 真实站内信投递中心

这避免了页面“看起来能发消息，实际上什么都没发”的误导。

## 4. 后端完成项

### 4.1 DDL 与 preflight

- 新增 `docs/DDL_Phase22_Notifications.sql`
- `DatabaseSchemaPreflightRunner` 新增 Phase 22 表/列检查

### 4.2 实体与服务

- `NotificationTypeSetting`
- `NotificationTemplate`
- `NotificationSendLog`

### 4.3 管理端 API

- `GET /v3/admin/notifications/type-settings`
- `POST /v3/admin/notifications/type-settings`
- `GET /v3/admin/notifications/templates`
- `POST /v3/admin/notifications/templates`
- `PUT /v3/admin/notifications/templates/{id}`
- `DELETE /v3/admin/notifications/templates/{id}`
- `GET /v3/admin/notifications/send-logs`
- `POST /v3/admin/notifications/send`

## 5. 前端完成项

`admin/system/notifications.vue` 已改为真实页面：

- 通知类型设置改为真实加载/保存
- 通知模板改为真实新增/编辑/删除
- 批量推送改为真实写入发送记录
- 发送记录改为真实查询
- 页面提示已明确说明：当前记录发送请求，不代表第三方网关已接通

## 6. 相关页面口径修正

### 6.1 `about.vue`

- 移除错误的“未接后端 API”提示
- 保持为纯信息页

### 6.2 `backup.vue`

- 移除假备份列表、假恢复、假删除按钮
- 改为运维/DBA 备份治理说明页
- 避免继续在应用后台伪造高风险运维能力

## 7. 当前降级页口径（本轮完成后）

建议当前剩余页面按真实口径收敛为：

- session-only：5 页
  - `admin/system/permissions.vue`
  - `admin/system/logs.vue`
  - `admin/overseas/analysis.vue`
  - `admin/overseas/services.vue`
  - `admin/overseas/reports.vue`
- pure info / ops guidance：2 页
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

合计：**7 页**

## 8. 结论

本轮后：

- `notifications.vue` 已不再属于降级页
- `about.vue` 与 `backup.vue` 的页面语义已被校正
- 剩余待真实化重点应收敛到：
  - `permissions`
  - `logs`
  - `overseas/*`
