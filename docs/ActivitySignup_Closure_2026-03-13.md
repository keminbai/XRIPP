# 活动报名管理/导出闭环记录（2026-03-13）

## 1. 背景

在活动内容管理写闭环完成后，后台 [`activities.vue`](../app/pages/admin/content/activities.vue) 仍然保留两处明显的“假能力”：

- 报名管理弹窗使用静态假名单
- “导出 Excel 名单”只是受控降级提示

这与需求文档中“活动列表与报名管理，显示当前已报名人数，支持导出报名企业”的要求不一致。

## 2. 本轮修复范围

### 2.1 后端接口

更新 [`PartnerPublishesV3Controller`](../ipp-backend/src/main/java/com/xripp/backend/controller/v3/PartnerPublishesV3Controller.java)：

- `GET /v3/partner/publishes/{id}/registrations`
  - 返回指定活动的真实报名记录
  - 权限规则：`admin` 可看全量，`partner` 仅可看自己名下活动
- `GET /v3/partner/publishes/{id}/registrations/export`
  - 导出活动报名名单 Excel
  - 复用统一 `ExcelExportUtil`

返回字段包括：

- 企业名称
- 联系人
- 联系电话
- 报名状态
- 支付状态
- 报名时间
- 支付时间

### 2.2 导出 DTO

新增 [`ActivitySignupExportDTO`](../ipp-backend/src/main/java/com/xripp/backend/dto/ActivitySignupExportDTO.java)，避免直接把实体对象暴露给 Excel 导出层。

### 2.3 前端页面

更新 [`activities.vue`](../app/pages/admin/content/activities.vue)：

- 点击“报名管理”时调用真实报名列表接口
- 弹窗表格展示真实报名记录
- “导出 Excel 名单”接入真实下载
- 删除原静态假名单依赖

## 3. 设计取舍

本轮没有先做“显示申请”而是先做“报名管理/导出”，原因是：

- `activity_registrations` 已是现成真实业务表
- 导出能力可复用项目既有 Excel 体系，落地成本低且收益高
- “显示申请”仍缺专门的数据模型和审核流，先做容易再次形成半闭环

## 4. 未覆盖项

活动模块当前仍有两项待后续阶段处理：

- 首页/活动中心显示申请
- 活动记录（实际参与人数、现场照片、总结录入）

本轮仅聚焦“报名管理/导出”这一条最成熟的真实业务链路。
