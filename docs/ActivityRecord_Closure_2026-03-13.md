# Activity Record Closure 2026-03-13

## 本轮目标

补齐“活动发布 -> 审核 -> 报名 -> 显示申请 -> 线下举办后活动记录”的最后一段闭环，满足早期需求中“实际参与人数、现场照片、活动总结、标记已完成、导出参会企业名单”的要求。

## 本轮完成

### 1. 数据模型

- 新增 DDL：[`DDL_Phase17_ActivityRecords.sql`](./DDL_Phase17_ActivityRecords.sql)
- 新增表：
  - `activity_records`
    - 一条活动对应一条记录
    - 持久化 `activity_id / actual_participants / summary / completion_status / recorded_by / completed_at`
  - `activity_record_photos`
    - 结构化保存现场照片元数据
    - 持久化 `file_name / file_url / stored_name / file_ext / file_size / content_type / sort_order`

### 2. 后端接口

- 扩展 [`/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/PartnerPublishesV3Controller.java`](/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/PartnerPublishesV3Controller.java)
  - `GET /v3/partner/publishes/{id}/record`
  - `PUT /v3/partner/publishes/{id}/record`
  - 列表/详情返回 `activity_record` 摘要
  - 详情返回完整 `activity_record + photos`
- 新增后端持久层：
  - `ActivityRecord / ActivityRecordPhoto` Entity
  - 对应 Mapper / Service / ServiceImpl

### 3. 业务规则

- 仅 `published / offline` 活动允许维护活动记录
- “保存并标记完成”时强校验：
  - `actual_participants > 0`
  - `summary` 非空
  - 至少 1 张现场照片
- 活动记录状态独立于 `activities.audit_status`，避免把“活动是否发布”和“活动是否已完成复盘”混为一层状态

### 4. 管理端页面

- 扩展 [`/mnt/d/ipp-platform/app/pages/admin/content/activities.vue`](/mnt/d/ipp-platform/app/pages/admin/content/activities.vue)
  - 列表显示活动记录状态
  - 新增“活动记录 / 查看记录”操作
  - 新增活动记录弹窗：
    - 实际参与人数
    - 现场照片上传
    - 活动总结
    - 保存草稿 / 保存并标记完成
  - 详情弹窗显示活动记录、照片预览、下载入口
  - 记录弹窗可直接导出参会名单（复用现有报名导出接口）

## 结果

- 活动管理从“发布运营”扩展为“活动全生命周期管理”
- 线下活动结束后不再只能口头补充，后台可形成正式复盘记录
- 现场照片不再只保存在页面临时态，已具备可重放、可预览、可下载的结构化持久化

## 未执行项

- 本轮未运行 `npm run build` / `mvn compile`
- 原因：当前用户已明确将慢测与实测交给同事，本轮优先持续开发与文档闭环
