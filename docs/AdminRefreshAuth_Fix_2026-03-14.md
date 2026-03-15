# Admin 刷新登录态修复记录（2026-03-14）

## 1. 现象

Windows 实测发现：

- `admin` 登录后进入后台页面正常
- 会员新增、活动新增与编辑都可成功
- 但刷新 `/admin/members/list`、`/admin/content/activities` 时，会直接回到登录页

同时一度伴随 Nuxt middleware 上下文异常。

## 2. 根因拆解

本问题实际由两层因素叠加造成：

1. `admin-auth.global.ts` 在 middleware 中使用 `.then/.catch + navigateTo()`，刷新进入 SSR 时可能丢失 Nuxt 上下文
2. 登录态读取链路过度依赖 `xripp_user` cookie；一旦 SSR 首屏未稳定解出该对象，就会把已有 token 的 admin 误判为未完成登录态恢复

## 3. 修复内容

### 3.1 middleware 改为 async/await

`app/middleware/admin-auth.global.ts`

- 不再在 Promise 回调里调用 `navigateTo()`
- 改为标准 `async/await` 守卫

### 3.2 登录用户恢复链路增强

`app/utils/request.ts`

- `xripp_user` 改为标准对象 cookie 写入，不再双重 JSON 字符串化
- `getLoginUser()` 兼容两种历史格式：
  - 字符串化 JSON
  - 对象 cookie
- 若 `xripp_user` 不可用，则回退解析 JWT payload，恢复最小用户信息：
  - `id`
  - `username`
  - `role`
  - `partner_id`
  - `permission_profile_id`

## 4. 预期结果

修复后应满足：

- `admin` 登录后刷新后台页，不再直接回到登录页
- SSR 首屏鉴权可在只有 token 的情况下恢复 admin 身份
- 旧格式 cookie 不需要强制清理即可兼容

## 5. 建议复测

- `admin` 登录后打开 `/admin/members/list`，刷新
- `admin` 登录后打开 `/admin/content/activities`，刷新
- 检查是否仍跳回 `/login`
