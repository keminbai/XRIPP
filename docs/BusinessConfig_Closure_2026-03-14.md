# 业务配置页真实化记录（2026-03-14）

## 1. 背景

`app/pages/admin/business/*` 原先全部属于 session-only 页面：

- `packages.vue` 刷新即丢
- `promotions.vue` 新增/编辑/复制仅停留在前端会话
- `roles.vue` 保存配置与变更记录均未落库

在 `Phase 20` 已建立通用配置底座后，这三页不应再继续各自维护一套前端内存状态，因此本轮统一收敛到后台配置中心。

## 2. 设计原则

本轮不新建专用表，而是直接复用：

- `admin_config_entries`
- `GET /v3/admin/configs/{namespace}`
- `POST /v3/admin/configs/{namespace}/batch`
- `DELETE /v3/admin/configs/{namespace}/{key}`

理由：

- 三页本质都是后台运营配置，不需要复杂关系查询
- 复用 Phase 20 底座可显著降低后续 `system/*` 同类页面接入成本
- 避免为每个配置页再发明一张表

## 3. 前端实现

新增通用 composable：

- `app/composables/useAdminConfigNamespace.ts`

作用：

- 统一封装 namespace 配置读取
- 统一封装批量保存
- 统一封装单 key 删除

### 3.1 会员套餐配置

页面：

- `app/pages/admin/business/packages.vue`

namespace：

- `business_packages`

key：

- `packages`

真实化内容：

- 套餐权益配置真实加载
- 保存策略后真实落库
- 启用/停用后刷新仍保持
- 对比表改为基于真实配置即时计算

### 3.2 营销促销规则

页面：

- `app/pages/admin/business/promotions.vue`

namespace：

- `business_promotions`

key：

- `promotion_rules`

真实化内容：

- 新增规则弹窗
- 编辑、复制、删除、启停全部真实持久化
- 刷新后规则状态不丢失
- 统计卡片改为基于真实规则集合计算

### 3.3 业务角色权限配置

页面：

- `app/pages/admin/business/roles.vue`

namespace：

- `business_roles`

keys：

- `role_list`
- `change_log`

真实化内容：

- 角色权限配置真实加载
- 单条保存 / 批量保存真实落库
- 最近变更记录一并持久化

## 4. 数据模型

本轮无新增 DDL。

原因：

- `Phase 20` 的 `admin_config_entries` 已足够承接这三页
- 只需在运行库中确保 `docs/DDL_Phase20_AdminConfigs.sql` 已执行

## 5. 当前限制

- 当前为“后台配置真实持久化”，不是完整营销引擎 / RBAC 引擎
- `promotions.vue` 的使用次数等统计仍属于配置侧口径，不直接联动订单收益
- `roles.vue` 当前只是业务运营角色配置，不等同于系统级权限中心

## 6. 验证状态

截至本轮仓库留痕：

- 代码已落库
- 文档已更新
- WSL 侧尝试执行 `npm run build`

当前阻断：

- 构建仍受 `@oxc-parser/binding-linux-x64-gnu` 缺失影响
- 因此页面级最终验收仍需 Claude 在 Windows 浏览器环境补留痕

## 7. 结论

`admin/business/packages.vue`、`promotions.vue`、`roles.vue` 已从“前端会话态配置页”收敛为复用统一配置底座的真实后台页面。

后续可按同一路径继续推进：

- `admin/system/settings.vue`
- `admin/system/login-config.vue`
- `admin/system/certificates.vue`（部分能力）
