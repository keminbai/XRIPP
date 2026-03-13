# 合伙人利润分成闭环修复记录（2026-03-13）

## 1. 背景

`app/pages/admin/finance/profit.vue` 原先属于 hybrid 页面：
- 顶部统计卡片部分接入真实接口
- 分成配置、分成明细、月度结算仍是前端会话态/mock
- 刷新丢失，无法形成可追溯的财务闭环

本阶段目标是将该页收敛为“真实配置 + 真实统计 + 真实结算记录”的后台能力。

## 2. 数据模型

新增 DDL：[`DDL_Phase19_ProfitSharing.sql`](./DDL_Phase19_ProfitSharing.sql)

新增表：
- `partner_profit_configs`
  - 每个合伙人一条分成配置
  - 核心字段：`partner_id`、`percentage`、`business_lines_json`、`enabled`
  - 审计字段：`changed_by`、`changed_at`、`change_reason`
- `partner_profit_settlements`
  - 按合伙人 + 月份落库结算结果
  - 核心字段：`settlement_month`、`revenue_amount`、`profit_amount`、`order_count`
  - 审计字段：`settled_by`、`settled_at`、`note`

## 3. 后端实现

新增实体 / Mapper / Service：
- `PartnerProfitConfig`
- `PartnerProfitSettlement`
- `PartnerProfitConfigMapper`
- `PartnerProfitSettlementMapper`
- `IPartnerProfitConfigService`
- `IPartnerProfitSettlementService`

新增控制器：
- `AdminProfitSharingV3Controller`

新增真实接口：
- `GET /v3/admin/profit-sharing/configs`
- `POST /v3/admin/profit-sharing/configs`
- `PUT /v3/admin/profit-sharing/configs/{id}`
- `DELETE /v3/admin/profit-sharing/configs/{id}`
- `GET /v3/admin/profit-sharing/stats`
- `GET /v3/admin/profit-sharing/records`
- `POST /v3/admin/profit-sharing/settlements`

计算口径：
- 只统计 `orders` 中状态为 `paid / in_service / completed` 的订单
- 合伙人归属优先取 `orders.partner_id`
- 若订单未直接写 `partner_id`，回退到 `sys_user.partner_id`
- 仅统计已启用配置且业务线命中的订单
- 业务线识别基于 `order_type + biz_type`

## 4. 前端实现

页面：`app/pages/admin/finance/profit.vue`

本轮收敛内容：
- 分成配置改为真实 CRUD
- 分成统计改为真实后端聚合
- 分成明细改为真实订单投影
- 月度结算改为真实写库
- 顶部统计卡片纠正为按 `/v3/admin/orders/stats` 真实返回结构读取

## 5. 启动护栏

`DatabaseSchemaPreflightRunner` 已纳入 Phase 19 检查：
- `partner_profit_configs`
- `partner_profit_settlements`

含义：
- 若未先执行 `DDL_Phase19_ProfitSharing.sql` 就重启后端，应用会 fail-fast
- 这是有意设计，用于防止“页面已宣称真实化，但数据库缺表后运行期 500”

## 6. 当前限制

- 目前为“平台内部分成结算台账”，不是正式财务总账系统
- 结算明细由订单实时投影得出，不单独生成逐笔分录表
- 业务线暂只支持 `membership / tender / training`

## 7. 联调顺序

1. 执行 `docs/DDL_Phase19_ProfitSharing.sql`
2. 重启后端，使 Phase 19 preflight 生效
3. 验证以下接口：
   - `GET /v3/admin/profit-sharing/configs`
   - `POST /v3/admin/profit-sharing/configs`
   - `GET /v3/admin/profit-sharing/stats`
   - `GET /v3/admin/profit-sharing/records`
   - `POST /v3/admin/profit-sharing/settlements`
4. 进入 `admin/finance/profit.vue` 进行页面联调

## 8. 结论

`admin/finance/profit.vue` 已从“前端假配置 + 假结算”收敛为真实后端模块。
后续若要继续扩展，可在此基础上追加撤销结算、重算、分账流水等能力。
