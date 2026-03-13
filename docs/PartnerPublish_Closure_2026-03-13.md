# Partner Publish Closure 2026-03-13

## 本轮目标

把 [`/mnt/d/ipp-platform/app/pages/admin/partner-publish.vue`](/mnt/d/ipp-platform/app/pages/admin/partner-publish.vue) 从“列表/创建半真实、编辑占位、状态口径错误、上传不落库”的旧页面，收敛为真实可用的合伙人活动发布入口。

## 本轮完成

### 1. 页面职责收敛

- 明确本页只处理“合伙人活动发布”
- 去掉“培训/商机也可通过此页发布”的误导性表述
- 页面文案、筛选条件、表单字段统一对齐现有真实后端能力：`/v3/partner/publishes`

### 2. 状态口径修复

- 前端状态从旧的 `pending/approved/rejected` 收敛为真实口径：
  - `auditing`
  - `published`
  - `rejected`
  - `offline`
- 统计卡片、筛选器、列表状态文案同步修正
- 不再把已发布误显示成“已通过”、把审核中误显示成“待审核”

### 3. 真实编辑闭环

- 查看详情改为请求 `GET /v3/partner/publishes/{id}`
- 编辑改为先拉详情回填，再调用 `PUT /v3/partner/publishes/{id}`
- 已发布/已下架活动也允许“编辑重提”，保存后重新进入审核
- 仅审核中活动保留“撤回”按钮，和后端状态机保持一致

### 4. 真实文件上传

- 封面图上传接入 `/api/common/upload`
- 宣传视频上传接入 `/api/common/upload`
- 上传成功后持久化回填 `cover_image / video_url`
- 不再只是把本地 `File` 暂存在表单里，避免提交时丢失

## 结果

- 合伙人发布入口不再停留在“能看不能改”的假闭环
- 前端页面与 `PartnerPublishesV3Controller` 的真实能力达成一致
- 后续联调与实测时，这一页可以直接作为合伙人活动发布主入口使用

## 验证

- `/tmp/ipp-platform-buildcheck` 下执行：
  - `NUXT_TELEMETRY_DISABLED=1 NUXT_TELEMETRY_CONSENT=0 npm run build`
- 构建通过

## 仍未覆盖

- 本页当前不承载培训/商机内容发布；如后续需要合伙人侧统一多内容发布入口，应新增独立后端能力，而不是继续复用活动接口伪装实现
