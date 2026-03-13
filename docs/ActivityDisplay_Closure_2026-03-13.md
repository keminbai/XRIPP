# Activity Display Closure（2026-03-13）

## 1. 本阶段目标

补齐活动模块中“显示申请”这一条运营闭环，解决此前存在的 3 个问题：

1. 活动页里的“申请显示”只是前端按钮，没有独立数据模型承载申请生命周期。
2. 后台 `display.vue` 无法审核活动显示申请，页面与需求文档中的“活动显示申请列表”不一致。
3. 首页 / 服务页无法按“已审核通过且在有效期内”的活动推荐位读取真实数据。

## 2. 需求依据

- `docs/requirements/早期规划后台管理功能清单.md`
  - 显示管理 -> 活动显示申请列表
- `docs/requirements/早期规划后台管理思维导图备注.md`
  - 支持首页显示 / 公共采购显示 / 平台服务显示
  - 每个区域最多设置 10 个活动
  - 显示时间不能超过活动截止 / 开始时间

## 3. 数据模型与 DDL

新增：

- `docs/DDL_Phase15_ActivityDisplayApplications.sql`

新表：

- `activity_display_applications`
  - `activity_id`
  - `display_area`
  - `display_start_at`
  - `display_end_at`
  - `sort_order`
  - `apply_status`
  - `applied_by`
  - `reviewed_by`
  - `reviewed_at`
  - `change_reason`
  - `created_at`
  - `updated_at`

设计判断：

- 不把显示申请塞回 `activities` 表或 `detail_json`
- 原因是显示申请有自己的审核态与上下线态，生命周期独立于活动审核态

## 4. 后端改动

### 4.1 新增显示申请实体与服务

- `ipp-backend/src/main/java/com/xripp/backend/entity/ActivityDisplayApplication.java`
- `ipp-backend/src/main/java/com/xripp/backend/mapper/ActivityDisplayApplicationMapper.java`
- `ipp-backend/src/main/java/com/xripp/backend/service/IActivityDisplayApplicationService.java`
- `ipp-backend/src/main/java/com/xripp/backend/service/impl/ActivityDisplayApplicationServiceImpl.java`

### 4.2 合伙人 / 总部活动管理接口补齐显示申请写入口

- `PartnerPublishesV3Controller`
  - 新增 `POST /v3/partner/publishes/{id}/display-apply`
  - 约束：
    - 仅已审核通过活动可申请显示
    - `display_area` 仅允许 `home | activity | service`
    - 显示结束时间不得超过活动开始时间
  - 活动详情接口新增 `display_applications`

### 4.3 后台显示管理补齐审核接口

- `AdminActivityDisplayApplicationsV3Controller`
  - 新增 `GET /v3/admin/activity-display-applications`
  - 新增 `POST /v3/admin/activity-display-applications/{id}/transition`
  - 状态流转：
    - `pending_review -> approved/rejected`
    - `approved -> offline`
    - `rejected/offline -> approved`
  - 审批守卫：
    - 同一区域同时有效的已通过申请最多 10 条

### 4.4 公共活动接口支持显示位读取

- `ActivitiesV3Controller`
  - `GET /v3/activities` 新增 `display_area`
  - 当传入 `display_area=home|activity|service` 时：
    - 只返回对应区域已审核通过且在有效期内的显示申请
    - 再映射为真实活动卡片数据

### 4.5 本轮补充修正

- `AdminActivityDisplayApplicationsV3Controller`
  - 把 `keyword` 检索从“分页后内存过滤”改为真正进 SQL 的 `activity_id` 过滤
  - 避免后台列表出现“当前页条数变少但 total 仍不变”的伪分页问题

## 5. 前端改动

### 5.1 活动管理页

- `app/pages/admin/content/activities.vue`
  - “申请显示”弹窗改为真实提交
  - 编辑活动时会带出历史 `display_applications`
  - 再次申请时默认回填最近一次申请记录

### 5.2 后台显示管理页

- `app/pages/admin/content/display.vue`
  - 新增 Tab：“活动显示申请”
  - 对接真实列表接口
  - 支持通过 / 驳回 / 下线 / 重新启用

### 5.3 公共页面

- `app/pages/index.vue`
  - 首页活动优先读取 `/v3/activities?...&display_area=home`
  - 无结果时回退到普通活动列表
- `app/pages/services.vue`
  - 活动中心优先读取 `/v3/activities?...&display_area=activity`
  - 无结果时回退到普通活动列表

## 6. 验收口径

验收通过后，应满足：

1. 合伙人可为已审核通过活动提交显示申请。
2. 总部可在显示管理页看到独立申请列表并完成审核。
3. 首页 / 服务页可读取已通过且处于有效期内的推荐活动。
4. 同一显示区域不会同时超过 10 个有效显示项。

## 7. 验证

本阶段代码完成后，已完成以下验证：

- `/tmp/ipp-backend-buildcheck` 下：
  - `./mvnw -q -DskipTests compile` 通过
- `/tmp/ipp-platform-buildcheck` 下：
  - `npm run build` 通过

说明：

- 当前未执行数据库实库 DDL，仅已补齐 `Phase15` 脚本
- 现阶段仍保留一个可接受的技术债：活动详情页返回 `display_applications` 采用逐条装配，列表场景存在 N+1 风险，后续若运营量上升可再做批量化优化
