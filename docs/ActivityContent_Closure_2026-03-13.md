# 活动内容管理写闭环记录（2026-03-13）

## 1. 背景

后台 [`app/pages/admin/content/activities.vue`](../app/pages/admin/content/activities.vue) 已有完整运营表单，但后端 [`PartnerPublishesV3Controller`](../ipp-backend/src/main/java/com/xripp/backend/controller/v3/PartnerPublishesV3Controller.java) 之前只保存 `title / fee / start_time` 等极少字段，导致：

- 活动类型、前台投放位置、投放城市、封面图、视频、议程、名额、会员价无法真实落库
- 编辑弹窗只能用列表粗数据回填，刷新后信息丢失
- 上下架按钮是受控降级提示，不是业务能力
- `audit_status=0` 被前端错误映射为“草稿”，运营侧状态感知失真

## 2. 本轮修复范围

### 2.1 数据模型

新增迁移脚本：[`DDL_Phase14_Activities_Closure.sql`](./DDL_Phase14_Activities_Closure.sql)

扩展 `activities` 表：

- `activity_no`
- `front_module`
- `front_position`
- `cities_json`
- `video_url`
- `agenda`
- `max_limit`
- `current_participants`
- `fee_item_id`
- `fee_item_name`
- `fee_type`
- `member_price`
- `changed_by`
- `changed_at`
- `change_reason`

同时新增：

- `UX_activities_activity_no`
- `IX_activities_partner_audit`

### 2.2 后端接口

更新 [`PartnerPublishesV3Controller`](../ipp-backend/src/main/java/com/xripp/backend/controller/v3/PartnerPublishesV3Controller.java)：

- `GET /v3/partner/publishes`
  - 列表返回完整活动运营字段
  - 增加报名人数聚合
- `GET /v3/partner/publishes/{id}`
  - 供编辑/详情回显使用
- `POST /v3/partner/publishes`
  - 支持完整创建
  - `partner` 创建后进入审核中
  - `admin` 创建后直接发布
- `PUT /v3/partner/publishes/{id}`
  - 支持完整编辑
  - `partner` 编辑后自动重新进入审核
- `POST /v3/partner/publishes/{id}/transition`
  - 支持 `published -> offline`
  - 支持 `offline -> published`

同步扩展 [`Activity.java`](../ipp-backend/src/main/java/com/xripp/backend/entity/Activity.java) 与公共接口 [`ActivitiesV3Controller`](../ipp-backend/src/main/java/com/xripp/backend/controller/v3/ActivitiesV3Controller.java)。

### 2.3 前端页面

更新 [`activities.vue`](../app/pages/admin/content/activities.vue)：

- 新建/编辑统一走真实接口
- 编辑前先拉详情，避免列表字段不足
- 上下架按钮接入真实 transition 接口
- `audit_status=0` 纠正为“审核中”
- `inspection/salon` 类型映射纠正
- 列表补齐前台模块/位置标签、投放城市、会员价等字段回填

## 3. 关键状态策略

### 3.1 审核状态

- `0` -> `auditing`
- `30` -> `published`
- `40` -> `rejected`
- `50` -> `offline`

### 3.2 编辑策略

- `partner` 编辑任何已存在活动后，重新进入审核，防止“编辑绕过审核”
- `admin` 编辑保留当前发布能力，作为运营主控入口

### 3.3 报名人数

- 活动列表/详情返回实时报名计数
- 公共报名成功后同步回写 `current_participants`

## 4. 已知未覆盖项

本轮聚焦“活动内容管理写闭环”，以下能力仍未实现：

- 首页展示位申请（`display apply`）
- 报名名单导出 Excel
- 活动审核双级流（当前仍是单级）

这些属于下一轮功能扩展，不影响本轮“创建/编辑/详情回显/上下架/公共展示字段”闭环验收。
