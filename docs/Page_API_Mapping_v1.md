# Page API Mapping v1

## 1. 前台页面映射

| 页面 | 路径 | 当前状态 | 目标接口(v3) | 方法 | 备注 |
|---|---|---|---|---|---|
| 平台服务-活动列表 | `/services` | mock为主 | `/api/v3/activities` | GET | 先切只读 |
| 平台服务-活动详情 | `/services`(弹窗) | mock为主 | `/api/v3/activities/{id}` | GET | 与列表同批 |
| 平台服务-活动报名 | `/services`(报名弹窗) | 前端模拟提交 | `/api/v3/activities/{id}/registrations` | POST | Batch B |
| 平台服务-服务商库 | `/services` | useServiceProviders 本地数据 | `/api/v3/suppliers`(建议新增) | GET | 支持筛选分页 |
| 服务商详情 | `/services`(详情弹窗) | 本地数据 | `/api/v3/suppliers/{id}`(建议新增) | GET | PDF下载后续 |
| 媒体中心列表 | `/services` | 本地 newsList | `/api/v3/contents?content_type=media` | GET | Batch C读 |
| 媒体详情 | `/media/{id}` | 待核 | `/api/v3/contents/{id}`(建议新增读详情) | GET | 可提前补 |

## 2. 会员中心页面映射

| 页面 | 路径 | 当前状态 | 目标接口(v3) | 方法 | 备注 |
|---|---|---|---|---|---|
| 联采会员申请提交 | `/member/un-apply` | 本地模拟 | `/api/v3/member-verifications` | POST | Batch B |
| 我的认证状态 | `/member/*` | 待统一 | `/api/v3/member-verifications/me` | GET | 与提交同迭代 |
| 供应商入驻草稿/支付单创建 | `/member/supplier-apply` | ✅ 已接入 | `/api/v3/member/supplier-apply/draft` | POST | 保存资料并生成支付单 |
| 供应商入驻支付后提交审核 | `/member/supplier-apply` | ✅ 已接入 | `/api/v3/member/supplier-apply/{id}/submit` | POST | 仅已支付后可提交 |
| 我的服务商申请列表 | `/member/supplier-applications` | ✅ 已接入 | `/api/v3/member/supplier-apply` | GET | 会员侧状态追踪入口 |
| 我的服务商申请详情 | `/member/supplier-applications` | ✅ 已接入 | `/api/v3/member/supplier-apply/{id}` | GET | 返回 detail/attachments/certificates/paymentOrder |
| 我的服务商申请支付状态 | `/member/supplier-applications` | ✅ 已接入 | `/api/v3/member/supplier-apply/{id}/payment` | GET | 支付二维码与支付状态刷新 |
| 我的订单列表 | `/member/orders` | 待核 | `/api/v3/orders` | GET | Batch A |
| 我的订单详情 | `/member/orders/{id}` | 待核 | `/api/v3/orders/{id}` | GET | Batch A |

## 3. 后台页面映射

| 页面 | 路径 | 当前状态 | 目标接口(v3) | 方法 | 备注 |
|---|---|---|---|---|---|
| 会员待审核列表 | `/admin/members/un-audit` | mock列表 | `/api/v3/admin/member-verifications` | GET | Batch A 首批 |
| 会员审核动作 | `/admin/members/un-audit` | 本地改状态 | `/api/v3/admin/member-verifications/{id}/review` | POST | Batch B |
| 供应商审核列表 | `/admin/suppliers/audit` | ✅ 已接入 | `/api/v3/admin/supplier-onboarding` | GET | 列表含联系人/支付/完整性 |
| 供应商审核详情 | `/admin/suppliers/audit` | ✅ 已接入 | `/api/v3/admin/supplier-onboarding/{id}` | GET | 返回 detail/attachments/certificates/paymentOrder |
| 供应商审核动作 | `/admin/suppliers/audit` | ✅ 已接入 | `/api/v3/admin/supplier-onboarding/{id}/transition` | POST | 基于状态机流转，不再使用 review 占位接口 |
| 订单管理列表 | `/admin/tenders/orders` | 待核 | `/api/v3/orders` | GET | 先读后写 |
| 订单状态流转 | `/admin/tenders/orders` | 后端已落地（待联调） | `/api/v3/admin/orders/{id}/transition` | POST | Batch B |
| 标书管理列表 | `/admin/tenders/manage` | 待核 | `/api/v3/tenders` | GET | Batch C |
| 标书发布/关闭 | `/admin/tenders/manage` | 待核 | `/api/v3/admin/tenders/{id}/publish|close` | POST | Batch C |
| 内容发布管理 | `/admin/*content*` | 待核 | `/api/v3/contents*` | GET/POST | Batch C |

## 4. 字段映射规则（前端必须遵循）
- 不在页面层做旧字段转换。
- 页面只识别：
  - `member_level`: `normal|vip|svip`
  - `*_status`: 按 `ARCH_StateMachine_v1.md`
- 兼容映射在 BFF/网关层处理。

## 5. 开发任务拆分（可直接派工）

### FE-1（前台只读切换）
- `/services` 活动列表/详情接 v3
- 错误处理统一 `code/message`

### FE-2（前台提交流程）
- 活动报名 POST
- 会员认证提交 POST

### FE-3（后台审核）
- `un-audit.vue` 列表 GET + 审核 POST
- 供应商审核同模式复用组件

### BE-1（P0读接口）
- activities/member-verifications/supplier-onboarding/orders 查询接口

### BE-2（P0写接口）
- 认证提交与审核、供应商提交与审核、报名、订单流转

### QA-1（联调回归）
- 状态机正反例
- 权限矩阵
- v2兼容回归

## 6. 完成定义（DoD）
- 页面不再依赖本地mock主数据
- 状态展示与按钮显隐由后端状态驱动
- 关键路径（申请/审核/报名/订单）全链路打通
