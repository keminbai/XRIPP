# ARCH_StateMachine_v1

## 1. 目标
统一平台核心业务状态机，避免前台、后台、数据库、接口状态不一致。

## 2. 通用约定
- 所有状态字段统一用 `snake_case`。
- 所有状态变更必须记录：`changed_by`、`changed_at`、`change_reason`。
- 终态不可逆，除非走“人工复核/重开”专用动作。

## 3. 状态机：会员认证（member_verification）
字段：`verification_status`

### 状态集合
- `draft`：草稿
- `submitted`：已提交待审
- `under_review`：审核中
- `approved`：审核通过
- `rejected`：审核拒绝
- `withdrawn`：用户撤回

### 允许迁移
- `draft -> submitted`
- `submitted -> under_review`
- `under_review -> approved`
- `under_review -> rejected`
- `submitted -> withdrawn`
- `rejected -> draft`

### 终态
- `approved`
- `withdrawn`

## 4. 状态机：供应商入库（supplier_onboarding）
字段：`onboarding_status`

### 状态集合
- `draft`
- `pending_payment`
- `submitted`
- `precheck_passed`
- `precheck_failed`
- `final_review_passed`
- `final_review_failed`
- `active`
- `inactive`

### 允许迁移
- `draft -> pending_payment`
- `draft -> submitted`（仅免支付场景）
- `pending_payment -> submitted`（支付确认后）
- `pending_payment -> draft`（重新编辑资料或关闭支付单）
- `submitted -> precheck_passed`
- `submitted -> precheck_failed`
- `precheck_passed -> final_review_passed`
- `precheck_passed -> final_review_failed`
- `final_review_passed -> active`
- `active -> inactive`
- `inactive -> active`
- `precheck_failed -> draft`
- `final_review_failed -> draft`

### 前置守卫
- 若 `payment_required=true`，则 `payment_status=paid|waived` 前不得进入 `submitted`
- 审核侧（预审/终审/激活）必须再次校验支付已完成
- 附件与资质完整性不足时，不应允许进入正式审核流

### 终态
- 无绝对终态（可运营启停）

### 支付执行单（payment_orders）
字段：`pay_status`

#### 状态集合
- `created`
- `pending`
- `paid`
- `closed`
- `failed`
- `refunded`

#### 允许迁移
- `created -> pending`
- `pending -> paid`
- `pending -> closed`
- `pending -> failed`
- `paid -> refunded`

#### 说明
- 一笔供应商入驻申请可关联多笔历史支付单，但同一时刻只允许 1 笔有效待支付单
- 支付二维码必须一单一码

## 5. 状态机：活动报名（activity_registration）
字段：`registration_status`

### 状态集合
- `pending_payment`
- `paid`
- `confirmed`
- `cancelled`
- `refunded`

### 允许迁移
- `pending_payment -> paid`
- `paid -> confirmed`
- `pending_payment -> cancelled`
- `paid -> refunded`
- `confirmed -> refunded`

### 终态
- `cancelled`
- `refunded`

## 6. 状态机：订单（order）
字段：`order_status`

### 状态集合
- `created`
- `awaiting_payment`
- `paid`
- `in_service`
- `completed`
- `closed`
- `refunding`
- `refunded`

### 允许迁移
- `created -> awaiting_payment`
- `awaiting_payment -> paid`
- `awaiting_payment -> closed`
- `paid -> in_service`
- `in_service -> completed`
- `paid -> refunding`
- `in_service -> refunding`
- `refunding -> refunded`
- `completed -> closed`

### 终态
- `closed`
- `refunded`

## 7. 状态机：内容发布（content_publish）
字段：`publish_status`

### 状态集合
- `draft`
- `pending_review`
- `approved`
- `published`
- `rejected`
- `offline`

### 允许迁移
- `draft -> pending_review`
- `pending_review -> approved`
- `pending_review -> rejected`
- `approved -> published`
- `published -> offline`
- `rejected -> draft`
- `offline -> published`

### 终态
- 无绝对终态（可反复上下线）

## 8. 状态机：标书发布（tender_publish）
字段：`tender_status`

### 状态集合
- `draft`
- `published`
- `closed`
- `archived`

### 允许迁移
- `draft -> published`
- `published -> closed`
- `closed -> archived`

### 终态
- `archived`

## 9. 权限与动作约束（最小版）
- `member`：可提交、撤回、查看本人记录。
- `partner`：可提交业务数据，不可终审。
- `operator`：可初审、驳回、补录说明。
- `auditor`：可终审通过/拒绝。
- `admin`：可执行重开、强制关闭、状态修复。

## 10. 错误码建议
- `STATE_INVALID_TRANSITION`：非法状态迁移
- `STATE_GUARD_FAILED`：前置条件不满足
- `STATE_TERMINAL_LOCKED`：终态锁定不可变更
