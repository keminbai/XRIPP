# 系统权限中心 Phase 2 前端联动记录（2026-03-14）

## 1. 本轮目标

在 Phase 23 最小 RBAC 后端底座完成后，将前端权限中心和后台菜单/路由真正接上这套权限快照。

本轮解决的问题不是“页面能保存”这么浅，而是：

- 权限页真实展示和编辑后台档案
- 受限管理员不再默认落到会报错的 `/admin` 仪表盘
- 菜单显隐与路由放行基于同一份权限快照

## 2. 实现内容

### 2.1 前端权限快照层

新增：

- `app/composables/useAdminPermissionSnapshot.ts`

职责：

- 拉取 `GET /v3/admin/permissions/current`
- 维护当前管理员权限快照 state
- 统一判断模块权限
- 统一判断后台路由是否可访问
- 统一计算受限管理员默认首页

### 2.2 权限中心页面真实化

重写：

- `app/pages/admin/system/permissions.vue`

已接入真实 API：

- `GET /v3/admin/permissions/current`
- `GET /v3/admin/permissions/login-modes`
- `POST /v3/admin/permissions/login-modes`
- `GET /v3/admin/permissions/profiles`
- `POST /v3/admin/permissions/profiles`
- `PUT /v3/admin/permissions/profiles/{id}`
- `POST /v3/admin/permissions/users/{userId}/profile`

页面能力：

- 查看当前会话生效的权限快照
- 编辑登录模式与默认档案
- 新增/编辑权限档案
- 配置模块级权限
- 绑定/解绑账号权限档案

### 2.3 后台菜单联动

更新：

- `app/layouts/admin.vue`

关键收敛：

- 受限管理员菜单不再按“只要 role=admin 就全显”
- 菜单显隐改为基于权限快照判断
- `/admin` 不再强行作为所有管理员首页
- 对没有 dashboard 全量能力的管理员，自动切到其首个可用工作入口

这一步很重要，因为旧版 `/admin` 仪表盘会触发多组跨模块 API，受限管理员即使进入页面，也会出现部分接口 403/异常，不是真正可用的首页。

### 2.4 路由校验联动

更新：

- `app/middleware/admin-auth.global.ts`

当前逻辑：

- `partner` 仍按原有 partner/admin 隔离基线处理
- `admin` 进入后台路由时，先拉当前权限快照
- 无权访问当前页面则重定向到首个可访问入口

## 3. 关键设计判断

### 3.1 不让受限管理员停在 `/admin`

这不是体验优化，而是权限一致性修复。

原因：

- `/admin` 页面会请求多个跨模块统计接口
- 受限管理员本来就不应天然拥有这些能力
- 如果只隐藏菜单但仍把用户送到 `/admin`，系统仍会出现“看得见首页 / 实际接口报错”的假闭环

### 3.2 菜单与路由不能各写一套判断

因此本轮新增统一前端权限快照层，由它统一提供：

- 菜单显隐
- 路由可达性
- fallback 首页计算

避免后续再出现：

- 菜单隐藏了，但地址栏还能进
- 中间件拦住了，但 layout 还显示菜单
- 权限页保存了，但前端联动没生效

## 4. 当前状态口径

### 2026-03-14 增补

本节记录的是 Phase 23/Phase 24 之前的阶段口径，保留原貌；
但截至 2026-03-14 当前仓库状态，以下判断已过时：

- `admin/overseas/analysis.vue`
- `admin/overseas/services.vue`
- `admin/overseas/reports.vue`

上述三页已在后续 Phase 24 完成真实化，不再属于 `session-only` 页面。

按当前口径重算：

- 业务型 `session-only`：**0 页**
- 说明页：**2 页**
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

本轮之后：

- `admin/system/permissions.vue` 已完成前端真实接入
- 系统权限中心已具备“后端底座 + 前端接入 + 菜单/路由联动”
- 剩余 `session-only` 页面应收敛为：
  - `admin/overseas/analysis.vue`
  - `admin/overseas/services.vue`
  - `admin/overseas/reports.vue`

即：

- `session-only`：**3 页**
- `pure info / ops guidance`：**2 页**
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

合计剩余总量：**5 页**

## 5. 仍保留的现实边界

本轮没有假装解决以下问题：

- 当前 JWT 权限档案切换需要重新登录才会写入新 token
- `audit.vue` 中“一审/二审视图”仍是运营视图切换，不等于真正 operator/auditor 双角色体系
- 海外服务三页仍未建立独立业务数据后端
  - 上述判断截至当前已失效：Phase 24 已补齐独立数据模型与接口

## 6. 结论

本轮后，`permissions` 已从“前端假页”升级为：

- 有真实 RBAC 数据模型
- 有真实后端接口
- 有真实前端配置页
- 有真实菜单/路由联动

这意味着系统级权限中心已经进入可验证状态，不再只是文档概念或会话态演示。
