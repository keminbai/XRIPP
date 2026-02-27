# API Contract v3.0

## 1. 版本与原则
- Base URL: `/api/v3`
- 所有响应遵循统一结构
- 前后端状态字段、枚举、命名必须与：
  - `ARCH_DataDictionary_v1.md`
  - `ARCH_StateMachine_v1.md`
  完全一致

## 2. 统一响应结构

### Success
```json
{
  "code": "OK",
  "message": "success",
  "data": {},
  "request_id": "uuid",
  "timestamp": "2026-02-14T10:00:00Z"
}
```

### Error
```json
{
  "code": "STATE_INVALID_TRANSITION",
  "message": "invalid status transition",
  "details": {},
  "request_id": "uuid",
  "timestamp": "2026-02-14T10:00:00Z"
}
```

## 3. 通用规范
- 认证：`Authorization: Bearer <token>`
- 分页参数：
  - `page` (>=1)
  - `page_size` (1~100)
- 列表响应：
```json
{
  "items": [],
  "page": 1,
  "page_size": 20,
  "total": 0
}
```
- 排序参数：
  - `sort_by`
  - `sort_order` (`asc|desc`)

## 4. 模块：会员认证 Member Verification

### 4.1 提交认证
- `POST /member-verifications`
- body:
```json
{
  "company_name": "string",
  "contact_name": "string",
  "phone": "string",
  "industry": "string",
  "documents": ["file_id_1"]
}
```
- 返回：`verification_status=submitted`

### 4.2 获取我的认证
- `GET /member-verifications/me`

### 4.3 撤回认证
- `POST /member-verifications/{id}/withdraw`
- guard: 仅 `submitted` 可撤回

### 4.4 审核列表（后台）
- `GET /admin/member-verifications`
- query: `verification_status`, `keyword`, `page`, `page_size`

### 4.5 审核动作（后台）
- `POST /admin/member-verifications/{id}/review`
- body:
```json
{
  "action": "approve|reject",
  "reason": "string"
}
```

## 5. 模块：供应商入库 Supplier Onboarding

### 5.1 提交入库申请
- `POST /supplier-onboarding`
- 返回：`onboarding_status=submitted`

### 5.2 我的申请详情
- `GET /supplier-onboarding/me`

### 5.3 审核列表（后台）
- `GET /admin/supplier-onboarding`

### 5.4 审核动作（后台）
- `POST /admin/supplier-onboarding/{id}/review`
- body:
```json
{
  "stage": "precheck|final",
  "action": "pass|fail",
  "reason": "string"
}
```

### 5.5 启停供应商（后台）
- `POST /admin/suppliers/{id}/toggle-active`
- body:
```json
{
  "active": true
}
```

## 6. 模块：活动与报名 Activity

### 6.1 活动列表
- `GET /activities`
- query: `type`, `city`, `is_paid`, `page`, `page_size`

### 6.2 活动详情
- `GET /activities/{id}`

### 6.3 活动报名
- `POST /activities/{id}/registrations`
- 返回：
  - 免费活动：`registration_status=confirmed`
  - 付费活动：`registration_status=pending_payment`

### 6.4 支付回调（内部）
- `POST /internal/payments/callback`
- 结果驱动：
  - `pending_payment -> paid -> confirmed`

## 7. 模块：订单 Orders

### 7.1 创建订单
- `POST /orders`

### 7.2 订单列表
- `GET /orders`
- query: `order_type`, `order_status`, `page`, `page_size`

### 7.3 订单详情
- `GET /orders/{id}`

### 7.4 订单状态流转（后台）
- `POST /admin/orders/{id}/transition`
- body:
```json
{
  "to_status": "in_service",
  "reason": "string"
}
```

## 8. 模块：内容发布 Content Publish

### 8.1 创建内容
- `POST /contents`
- `content_type`: `activity|media|ad|training`

### 8.2 内容列表
- `GET /contents`

### 8.3 提交审核
- `POST /contents/{id}/submit-review`

### 8.4 审核动作（后台）
- `POST /admin/contents/{id}/review`
- body:
```json
{
  "action": "approve|reject",
  "reason": "string"
}
```

### 8.5 发布/下线（后台）
- `POST /admin/contents/{id}/publish`
- `POST /admin/contents/{id}/offline`

## 9. 模块：标书 Tender

### 9.1 创建标书
- `POST /tenders`

### 9.2 标书列表
- `GET /tenders`

### 9.3 发布标书
- `POST /admin/tenders/{id}/publish`

### 9.4 关闭标书
- `POST /admin/tenders/{id}/close`

## 10. 错误码（首批）
- `OK`
- `AUTH_UNAUTHORIZED`
- `AUTH_FORBIDDEN`
- `VALIDATION_ERROR`
- `RESOURCE_NOT_FOUND`
- `STATE_INVALID_TRANSITION`
- `STATE_GUARD_FAILED`
- `STATE_TERMINAL_LOCKED`
- `INTERNAL_ERROR`

## 11. 兼容策略
- v2 -> v3 过渡期保留 `/api/v2`
- 新功能只进 v3
- 前端新页面优先调用 v3
- 旧字段兼容映射在网关层处理（不在前端做脏映射）
