# 系统权限中心 Phase 1 底座记录（2026-03-14）

## 1. 本轮目标

将 `admin/system/permissions.vue` 从“纯前端会话态假页面”推进到真正可接入的系统级 RBAC 底座阶段。

本轮先处理后端基础层，不直接把旧页面包装成“会持久化的假权限页”。

## 2. 设计判断

当前系统真实生效的权限模型只有三层：

- `admin`
- `partner`
- `member`

这由以下代码共同决定：

- `SecurityContextHolder`
- `JwtAuthenticationFilter`
- `admin-auth.global.ts`
- `admin.vue`

因此旧版 `permissions.vue` 里“城市合伙人 / 发布管理员 / 审核管理员 / 模块授权 / 权限模板”这些概念，在本轮开始前都不是系统真实权限对象。

继续把它接到某个配置表里，只会产生“持久化的假页面”，不满足系统级修复目标。

## 3. 本轮实现

### 3.1 新增 DDL Phase 23

文件：

- `docs/DDL_Phase23_SystemPermissions_RBAC.sql`

新增结构：

- `sys_permission_profiles`
  - 权限档案主表
- `sys_permission_grants`
  - 档案-模块授权明细
- `sys_login_modes`
  - 登录模式定义
- `sys_user.permission_profile_id`
  - 用户绑定的权限档案

### 3.2 兼容原则

本轮不推翻现有主角色基线：

- `admin`
- `partner`
- `member`

兼容策略：

- 历史 `admin` 账号若未绑定 `permission_profile_id`，仍按“全量后台权限”兼容
- `partner` 现有数据隔离逻辑继续由 `partner_id + DataScope` 决定
- `member` 不纳入本轮后台权限中心

也就是说：

- 本轮是“在 `admin/partner/member` 之上补后台子权限档案”
- 不是“推倒重做整套登录体系”

### 3.3 默认种子

默认权限档案：

- `SUPER_ADMIN`
- `PARTNER_STANDARD`
- `CONTENT_PUBLISHER`
- `CONTENT_AUDITOR`

默认登录模式：

- `partner_mode`
- `publisher_mode`
- `auditor_mode`

### 3.4 后端接口

新增：

- `GET /v3/admin/permissions/current`
- `GET /v3/admin/permissions/login-modes`
- `POST /v3/admin/permissions/login-modes`
- `GET /v3/admin/permissions/profiles`
- `POST /v3/admin/permissions/profiles`
- `PUT /v3/admin/permissions/profiles/{id}`
- `POST /v3/admin/permissions/users/{userId}/profile`

### 3.5 请求链路补强

本轮已打通：

- `sys_user.permission_profile_id` 实体映射
- JWT 载荷新增 `permissionProfileId`
- `SecurityContextHolder` 增加当前权限档案上下文
- 后台权限守卫骨架：
  - 通过 `/api/v3/admin/**` 路径映射模块代码
  - 先对权限中心与已纳入映射的系统模块生效
  - 未映射接口默认不拦截，避免一次性误伤整站

## 4. 当前边界

本轮完成的是“系统权限中心后端底座”，不是全部前端闭环。

因此当前真实状态应精确表述为：

- `permissions` 已有独立 RBAC 数据模型和接口底座
- `permissions.vue` 旧页面本身仍未完成前端接入
- 菜单显隐和页面交互联动将在下一阶段处理

## 5. 为什么不走配置表

`business/roles.vue` 可以复用 `Phase 20 admin_config_entries`，因为它本质是业务运营配置。

但 `permissions.vue` 不行，因为它会直接影响：

- 后台菜单显隐
- 接口访问放行/拒绝
- 审核角色边界
- 登录模式与账号绑定关系

这类系统能力必须有独立结构化模型，不能继续塞进配置 JSON。

## 6. 下一阶段

下一阶段处理前端与联动：

- 重写 `permissions.vue` 为真实 API 驱动页面
- `admin.vue` 基于当前权限快照控制菜单显示
- `admin-auth.global.ts` 增加权限快照校验
- `audit.vue` 去掉“假一审 / 假二审切换”，改为基于真实权限展示

## 7. 结论

本轮之后：

- `permissions` 已不再只是“待讨论概念”
- 系统已经具备最小 RBAC 的数据模型、接口和请求链路骨架
- 旧假页面后续可以建立在真实权限底座上继续收敛，而不是重复制造新假壳
