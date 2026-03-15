# Partner 后台路由守卫修复记录（2026-03-14）

## 1. 背景

Windows 环境实测发现：

- `partner` 账号登录成功
- 允许访问的后台页对应 API 已返回 `OK`
- 但访问 `/admin/partner-publish`、`/admin/members/analysis` 等页面时，前端报 `Infinite redirect in navigation guard`

这说明问题不在后端权限，而在前端路由守卫首屏导航逻辑。

## 2. 根因

`admin-auth.global.ts` 已能从 token 解析出 `role=partner`，但 `useAdminPermissionSnapshot.ts` 中的 `canAccessAdminRoute()` 又重新调用 `getLoginUser()` 读取角色。

在首屏导航或 SSR/CSR 切换瞬间，如果 `xripp_user` 尚未被该读取链路取到：

- middleware 看到的是 `partner`
- composable 看到的是空角色

结果是：

- 原本允许的 partner 页面被误判为禁止
- middleware 重定向到 `/admin/partner-publish`
- 目标页再次被误判为禁止
- 最终形成无限重定向

## 3. 修复内容

本次修复做了两件事：

1. `canAccessAdminRoute()` / `resolveAdminFallbackRoute()` 增加显式 `role` 参数，允许调用方传入当前已解析出的角色
2. `admin-auth.global.ts` 与 `app/layouts/admin.vue` 都改为显式传入当前角色，不再依赖函数内部二次读取用户态

## 4. 修复结果预期

修复后应满足：

- partner 访问 allowlist 页面不再出现 `Infinite redirect in navigation guard`
- partner 访问 `/admin` 时应跳转到 `/admin/partner-publish`
- partner 访问非 allowlist 页面应被稳定拦回 `/admin/partner-publish`

## 5. 后续验证

建议由 Windows 环境继续复测以下页面：

- 允许访问：`/admin/partner-publish`、`/admin/members/analysis`、`/admin/members/list`、`/admin/suppliers/analysis`、`/admin/suppliers/list`
- 必须拦回：`/admin`、`/admin/partners/analysis`、`/admin/partners/resources`、`/admin/system/*`、`/admin/content/*`
