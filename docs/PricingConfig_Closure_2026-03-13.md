# 定价配置真实闭环记录（2026-03-13）

## 1. 背景

`app/pages/admin/finance/pricing.vue` 原先为 session-only 页面：
- 会员费、标书、培训、服务四类定价全部驻留前端内存
- 保存后刷新即丢失
- 无统一后端配置底座，后续 `business/*`、`system/*` 也无法复用

本阶段目标不是只“救活 pricing 一页”，而是先建立后台通用配置存储层，并以 pricing 作为首个真实接入页。

## 2. 数据模型

新增 DDL：[`DDL_Phase20_AdminConfigs.sql`](./DDL_Phase20_AdminConfigs.sql)

新增表：
- `admin_config_entries`

核心字段：
- `config_namespace`
- `config_key`
- `config_name`
- `config_value_json`
- `sort_order`
- `enabled`

审计字段：
- `changed_by`
- `changed_at`
- `created_at`
- `updated_at`

约束与索引：
- `UX_admin_config_entries_namespace_key`
- `IX_admin_config_entries_namespace_sort`

## 3. 后端实现

新增实体 / Mapper / Service：
- `AdminConfigEntry`
- `AdminConfigEntryMapper`
- `IAdminConfigEntryService`

新增控制器：
- `AdminConfigsV3Controller`

首批接口：
- `GET /v3/admin/configs/{namespace}`
- `POST /v3/admin/configs/{namespace}/batch`
- `DELETE /v3/admin/configs/{namespace}/{key}`

设计说明：
- 后端不为每个配置页单独建表
- 统一按 `namespace + key + json value` 存储
- pricing 首批使用 `namespace=pricing`

pricing 目前落库的 key：
- `membership_tiers`
- `tender_pricing`
- `training_pricing`
- `service_pricing`

## 4. 前端实现

页面：`app/pages/admin/finance/pricing.vue`

本轮变化：
- 页面初始化时真实加载 `/v3/admin/configs/pricing`
- 新增/编辑/删除四类定价后，立即回写后台
- 提示文案从“会话内有效”改为“真实持久化”
- 保留原有 UI 结构，降低回归风险

## 5. 启动护栏

`DatabaseSchemaPreflightRunner` 已纳入 Phase 20 检查：
- `admin_config_entries`

含义：
- 若未执行 `DDL_Phase20_AdminConfigs.sql` 就重启后端，将 fail-fast
- 避免 pricing 页面再次退化为运行期 500

## 6. 架构收益

这轮不是一次性页面修补，而是给后续配置型页面建立统一底座：
- `admin/business/*`
- `admin/system/*`
- 其他运营配置页

后续新页只需复用：
- `namespace`
- `key`
- `json value`

即可接入真实持久化，无需重复发明表结构。

## 7. 联调顺序

1. 执行 `docs/DDL_Phase20_AdminConfigs.sql`
2. 重启后端，使 Phase 20 preflight 生效
3. 验证：
   - `GET /v3/admin/configs/pricing`
   - `POST /v3/admin/configs/pricing/batch`
4. 进入 `admin/finance/pricing.vue`
5. 实测：
   - 编辑会员费档位并保存
   - 删除一个测试项后刷新确认仍生效
   - 切换培训状态后刷新确认持久化

## 8. 结论

`admin/finance/pricing.vue` 已从“刷新即丢的前端会话页”收敛为真实后台配置页。
同时，项目新增了可复用的后台配置存储底座，可承接后续 P2 配置型页面真实化。
