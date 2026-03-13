# Training Page Truthfulness 2026-03-13

## 本轮目标

收敛 [`/mnt/d/ipp-platform/app/pages/admin/content/trainings.vue`](/mnt/d/ipp-platform/app/pages/admin/content/trainings.vue) 中残留的“假业务能力”，避免页面继续展示不存在的培训报名、名单导出、独立显示申请入口。

## 问题判断

经核对当前仓库代码与需求落地情况：

- 现有后端只有活动报名模型 `activity_registrations`
- 现有显示申请模型只覆盖活动：`activity_display_applications`
- `training` 当前属于 `contents` 内容管理范畴，并无独立培训报名表、支付链路、显示申请审核流

因此，继续在培训页保留“报名管理 / 导出 / 申请显示”按钮，只会制造新的假闭环。

## 本轮完成

### 1. 页面能力边界收敛

- 保留真实存在的能力：
  - 列表
  - 详情
  - 新建
  - 编辑
  - 审核
  - 上下架
  - 前台位置配置
- 移除不存在能力的 UI：
  - 培训报名管理弹窗
  - 培训名单导出按钮
  - 培训独立显示申请弹窗

### 2. 展示语义修复

- 顶部新增“能力边界说明”提示
- “报名数据”列改为“开课与名额”
- 统计卡片从“累计学员”改为“已发布”
- 详情弹窗补充真实的：
  - 前台位置
  - 开课时间
  - 名额限制

## 结果

- 培训页不再误导测试人员去验证不存在的培训报名/导出链路
- 页面语义与真实后端模型一致，减少无效缺陷和错误期待
- 内容管理页职责更清晰：培训当前是内容发布，不是活动报名系统

## 验证

- `/tmp/ipp-platform-buildcheck` 下执行：
  - `NUXT_TELEMETRY_DISABLED=1 NUXT_TELEMETRY_CONSENT=0 npm run build`
- 构建通过
