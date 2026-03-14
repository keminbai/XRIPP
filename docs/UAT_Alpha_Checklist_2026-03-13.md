# UAT Alpha Checklist（2026-03-13）

## 2026-03-14 口径补充

- `admin/finance/pricing.vue` 已真实化，当前仍暂列排除项，只是因为页面联调留痕未补齐
- `admin/finance/profit.vue` 的配置/结算操作已真实化，当前仍暂列排除项，只是因为页面联调与补测留痕未补齐
- `admin/business/packages.vue` / `promotions.vue` / `roles.vue` 已真实化，当前仍暂列排除项，只是因为页面联调留痕未补齐
- `admin/system/certificates.vue` 已真实化，当前仍暂列排除项，只是因为页面联调留痕未补齐
- partner 后台只测 5 个已开放页面：`partner-publish`、`members/analysis`、`members/list`、`suppliers/analysis`、`suppliers/list`
- `admin/partners/resources.vue`、`admin/partners/center.vue` 当前不对 partner 暴露，也不纳入本轮结果统计

## 1. 测试前确认

- 测试库已执行 Phase 6 ~ Phase 16 DDL
- 前后端已指向同一测试环境
- 已准备 3 类账号：`admin` / `partner` / `member`
- 上传目录 `/uploads/**` 可访问

## 2. 必测主流程

### 会员侧

- 注册、登录、退出登录
- 标书列表、详情、下载
- 标书收藏 / 取消收藏
- 服务商入驻：
  - 草稿保存
  - 上传附件 / 证书
  - 支付二维码
  - 支付状态刷新
  - 提交审核
  - 我的申请列表 / 详情
  - 失败后修改重提
- 活动报名：
  - 免费活动
  - 付费活动
  - 支付后状态变化

### 后台

- 标书管理：
  - 列表筛选
  - 发布时间筛选
  - 新建 / 编辑 / 状态流转
  - 导出
- 会员管理：
  - 列表 / 筛选 / 导出 / 等级调整
- 服务商审核：
  - 列表
  - 详情
  - 附件查看
  - 证书查看
  - 支付复核
  - 状态流转
- 活动管理：
  - 列表 / 新建 / 编辑 / 审核 / 上下架
  - 报名管理 / 导出
  - 显示申请提交
- 显示管理：
  - 活动显示申请列表
  - 通过 / 驳回 / 下线 / 重新启用
- 订单管理：
  - 列表 / 状态 / 统计

### 公共页

- 首页统计与推荐内容
- 服务页活动与服务商
- 供应商名录
- 媒体详情
- 公共大屏

## 3. 本轮排除项

- `admin/overseas/*`
- `admin/system/*`
- `admin/business/*`（已真实化，当前不作为 Alpha 必测项）
- `admin/system/certificates.vue`（已真实化，当前不作为 Alpha 必测项）
- `admin/finance/pricing.vue`（已真实化，待浏览器联调补留痕后纳入）
- `admin/finance/profit.vue` 的配置/结算操作（已真实化，待浏览器联调与补测后纳入）
- `admin/content/trainings.vue` 中并不存在的培训报名 / 独立显示申请能力
- `admin/partners/resources.vue`
- `admin/partners/center.vue`
- `experts.vue`
- 正式商户微信支付
- 正式短信服务

## 4. 结果记录建议

- 通过：主流程走通，数据正确落库
- 失败：记录账号、步骤、接口、报错、截图
- 阻塞：先停测并通知开发处理
