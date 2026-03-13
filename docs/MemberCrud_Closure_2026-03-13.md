# 会员管理真实 CRUD 闭环修复记录（2026-03-13）

## 1. 问题背景

用户实测发现：后台“新增会员”弹窗可以填写并点击保存，但刷新后数据消失，说明页面并没有形成真实写库闭环。

这不是单点 UI 小问题，而是典型的“前端假 CRUD + 后端写接口缺失”问题：

- `app/pages/admin/members/list.vue`
  - 之前新增、编辑、删除、启停主要是本地数组操作
  - 页面表现像“保存成功”，但刷新后自然丢失
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminMembersV3Controller.java`
  - 原先只有 `list/detail/set-level/export`
  - 没有真正的会员创建、更新、删除、启停接口

## 2. 本轮修复目标

把会员管理从“可演示页面”修成“真实业务闭环”：

1. 后端补齐真实写接口
2. 前端列表页完全改为调用真实 API
3. 会员创建逻辑与现有账号体系一致，不再制造孤立脏数据
4. 补留痕文档，明确根因和验证结果

## 3. 后端修复

文件：
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminMembersV3Controller.java`

新增/补齐接口：

- `POST /v3/admin/members`
  - 管理员新增会员
  - 同时创建 `sys_user` 与 `member_profile`
- `PUT /v3/admin/members/{id}`
  - 管理员编辑会员
  - 同步更新账号信息与会员档案
- `POST /v3/admin/members/{id}/transition`
  - 启用/禁用会员账号
- `DELETE /v3/admin/members/{id}`
  - 删除无业务历史的会员
  - 如存在订单、需求、认证、入驻、福利、收藏等业务数据，则禁止删除，只允许禁用

关键实现原则：

- 新增会员不是只插 `member_profile`，而是创建成完整账号：
  - `sys_user.username = 联系电话`
  - `sys_user.role = member`
  - 初始密码默认 `123456`，也允许管理员手输
- 邀请码不再只做字符串落库，而是解析为 `partner_id`
- 邀请码校验与注册逻辑对齐：
  - 不存在则拒绝
  - 对应合伙人已停用也拒绝
- 会员等级继续以数据库真实枚举保存：
  - `normal | vip | svip`

## 4. 前端修复

文件：
- `app/pages/admin/members/list.vue`

本轮完成：

- 新增会员改为调用 `POST /v3/admin/members`
- 编辑会员改为调用 `PUT /v3/admin/members/{id}`
- 启停账号改为调用 `POST /v3/admin/members/{id}/transition`
- 删除账号改为调用 `DELETE /v3/admin/members/{id}`
- 详情改为调用 `GET /v3/admin/members/{id}`
- 新增时增加“初始密码”字段说明，默认账号为联系电话
- 去掉数据库并不存在的伪字段输入
- 增加表格加载态与保存提交态，避免重复点击造成误判
- 分页完全依赖后端返回，不再前端伪分页

## 5. 为什么这次修复是“从根上改”

这次不是在页面上补一句“保存成功后请刷新”，也不是把数据先塞浏览器缓存，而是把会员管理拉回真实数据模型：

- 页面操作全部落到真实 API
- API 落到真实账号表和会员档案表
- 删除动作受业务关联约束保护
- 邀请码和合伙人关系进入统一口径

也就是说，“刷新后消失”这类问题的根因已经被去掉，而不是被掩盖。

## 6. 验证结果

本轮已完成本地构建级验证：

- 后端：
  - `/tmp/ipp-backend-buildcheck/ipp-backend`
  - `./mvnw -q -DskipTests compile` 已通过
- 前端：
  - `/tmp/ipp-platform-buildcheck`
  - `npm run build` 已通过

说明：

- 当前改动已达到“代码可编译、前后端链路存在”的状态
- 但是否已经在你正在使用的运行环境里生效，还取决于运行中的前后端服务是否已重启/重新部署

## 7. 本轮未做的事

本轮只修会员 CRUD 主闭环，没有顺手扩散到其他模块：

- 没新增数据库 DDL
  - 因为会员 CRUD 这次不需要加表或改表
- 没改会员认证审核页
- 没改其他后台管理页的潜在伪操作

## 8. 关联文件

- `app/pages/admin/members/list.vue`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminMembersV3Controller.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AuthV3Controller.java`
