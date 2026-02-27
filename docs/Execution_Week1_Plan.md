# Execution Week1 Plan（收敛版）

最后更新：2026-02-26

## 1. Week1 目标（P0 主链路）

打通以下主链路并可联调：
- 会员认证：提交 -> 审核
- 活动：列表/详情 -> 报名
- 订单：列表/详情
- 权限：member / partner / admin 路由与后端鉴权一致

同时保证：
- `v2` 路由不被破坏
- 新增接口统一响应结构：`code/message/data/request_id/timestamp`

---

## 2. 当前代码现状快照（以代码为准）

### 2.1 已落地

- `POST /api/v3/auth/login`
- `GET /api/v3/activities`
- `GET /api/v3/activities/{id}`
- `POST /api/v3/activities/{id}/registrations`
- `GET /api/v3/orders`
- `GET /api/v3/orders/{id}`
- `POST /api/v3/internal/payments/callback`
- `POST /api/v3/member-verifications`
- `GET /api/v3/admin/member-verifications`
- `POST /api/v3/admin/member-verifications/{id}/review`
- `GET /api/v3/admin/tenders`
- `GET /api/v3/admin/tenders/stats`
- `GET /api/v3/member/benefits/usage`
- `GET /api/v3/partner/publishes`
- `POST /api/v3/partner/publishes`
- `DELETE /api/v3/partner/publishes/{id}`

### 2.2 部分落地（前端仍有 mock 残留）

- `app/pages/admin/partner-publish.vue`：主流程已接 API，图片上传与编辑回填未完成
- `app/pages/admin/tenders/reference.vue`：仍有本地列表与 `setTimeout` 模拟流程
- `app/pages/admin/members/un-audit.vue`：API 与本地兜底并存，证书下载仍有 TODO
- `app/pages/member/index.vue`、`app/pages/member/sms-settings.vue`：部分展示数据仍为静态样例

### 2.3 已联调验收（代码已落地并通过）

- `POST /api/v3/admin/orders/{id}/transition`

说明：
- 已完成正向与负向联调：
  - 正向：`awaiting_payment -> paid -> in_service` 成功
  - 负向：`in_service -> paid` 返回 `STATE_INVALID_TRANSITION`
  - 终态锁：`in_service -> completed -> closed` 后，`closed -> paid` 返回 `STATE_TERMINAL_LOCKED`

---

## 3. Week1 任务状态（收敛后）

### Day 1（架构冻结）
- 状态：完成
- 输出：数据字典、状态机、接口契约、DDL 缺口文档

### Day 2（数据库迁移）
- 状态：完成（按现有环境）
- 输出：`DDL_Phase2_Migration.sql` 已可执行，关键表已补齐

### Day 3（读接口联调）
- 状态：完成
- 说明：核心读链路已可联调；个别页面仍有 mock UI 数据

### Day 4（写接口联调）
- 状态：完成
- 已完成：会员认证提交/审核、活动报名、合伙人发布主流程、订单状态流转后端实现与联调回归

### Day 5（回归与收口）
- 状态：进行中
- 当前重点：去 mock、统一页面行为与接口状态机

---

## 4. 风险与阻塞（当前）

1. 前端个别页面“真实 API + 本地模拟”并存，容易造成假通过
2. 媒体上传/证书下载链路未完全落库，影响验收完整性

---

## 5. 下一步执行顺序（建议）

1. 去除 `reference.vue` 与 `un-audit.vue` 的关键 mock（前端）
2. 补图片上传、证书下载的真实接口闭环（前后端）
3. 出一版“Week1 验收清单 + 回归脚本”

---

## 6. 责任与维护

- 本文档用于“周计划 + 现状快照”双用途
- 每次联调后必须更新：
  - 已落地接口
  - 未落地接口
  - 页面 mock 清理进度
