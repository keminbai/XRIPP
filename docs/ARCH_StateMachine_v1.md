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
- `submitted`
- `precheck_passed`
- `precheck_failed`
- `final_review_passed`
- `final_review_failed`
- `active`
- `inactive`

### 允许迁移
- `draft -> submitted`
- `submitted -> precheck_passed`
- `submitted -> precheck_failed`
- `precheck_passed -> final_review_passed`
- `precheck_passed -> final_review_failed`
- `final_review_passed -> active`
- `active -> inactive`
- `inactive -> active`
- `precheck_failed -> draft`
- `final_review_failed -> draft`

### 终态
- 无绝对终态（可运营启停）

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
