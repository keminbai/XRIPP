# 系统配置页第一阶段真实化记录（2026-03-14）

## 1. 本轮目标

`admin/system/*` 不能整体粗暴接入通用配置底座。

本轮只处理最适合配置存储的两页：

- `app/pages/admin/system/settings.vue`
- `app/pages/admin/system/login-config.vue`

暂不处理：

- `notifications.vue`
- `customer-service.vue`
- `permissions.vue`
- `logs.vue`
- `backup.vue`
- `certificates.vue`（仅部分能力适合配置化）

## 2. 设计判断

适合本轮接入的原因：

- 两页本质都是“配置对象保存”
- 不依赖复杂业务关系
- 不需要独立列表、工单、日志或任务执行模型

不适合本轮强行配置化的页面，本质更接近：

- 通知中心
- 工单系统
- 审计日志
- 备份任务
- 权限/RBAC 中心

## 3. 实现方式

继续复用 `Phase 20` 通用配置底座：

- `admin_config_entries`
- `GET /v3/admin/configs/{namespace}`
- `POST /v3/admin/configs/{namespace}/batch`

复用前端公共封装：

- `app/composables/useAdminConfigNamespace.ts`

## 4. 命名空间设计

### 4.1 系统设置

页面：

- `app/pages/admin/system/settings.vue`

namespace：

- `system_settings`

keys：

- `basic_settings`
- `email_settings`
- `security_settings`

### 4.2 登录安全配置

页面：

- `app/pages/admin/system/login-config.vue`

namespace：

- `system_login_config`

keys：

- `basic_config`
- `security_config`
- `oauth_providers`

## 5. 本轮结果

- `settings.vue` 已实现真实加载 / 保存 / 刷新回读
- `login-config.vue` 已实现真实加载 / 保存 / 刷新回读
- 本轮无新增 DDL
- 当前仍需 Windows 浏览器环境补页面级联调留痕

## 6. 结论

`admin/system/*` 已明确分成两类：

1. 可以走配置底座的页面
2. 不能投机塞进配置表的页面

本轮先拿下第 1 类中的两页，为后续 `certificates.vue` 的部分配置化提供参照。
