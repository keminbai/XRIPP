# DDL Gap Report v1
对比基线：
- `docs/DDL_Phase1_Final.sql`
- `docs/ARCH_DataDictionary_v1.md`
- `docs/ARCH_StateMachine_v1.md`
- `docs/API_Contract_v3.0.md`

## 1. 结论摘要
- Phase1 已覆盖：
- Phase1 缺失核心域：
- 必须先改（P0）：
- 可后置（P1/P2）：

## 2. 表级差异（Table-level）
| 领域 | 目标表 | DDL现状 | 处理动作 | 优先级 |
|---|---|---|---|---|
| 会员认证 | member_verifications | 已有/缺失 | keep/alter/add | P0 |
| 供应商入库 | supplier_onboarding | 已有/缺失 | keep/alter/add | P0 |
| 活动报名 | activity_registrations | 已有/缺失 | keep/alter/add | P0 |
| 订单 | orders | 已有/缺失 | keep/alter/add | P0 |
| 内容发布 | contents | 已有/缺失 | keep/alter/add | P1 |
| 标书 | tenders | 已有/缺失 | keep/alter/add | P1 |
| 状态流转日志 | state_transition_logs | 已有/缺失 | keep/alter/add | P0 |

## 3. 字段级差异（Column-level）
> 每个目标表都按“缺字段/类型不一致/命名不一致/约束缺失”列出

### 3.1 member_verifications
| 字段 | 目标定义 | DDL现状 | 问题类型 | 处理建议 |
|---|---|---|---|---|
| verification_status | enum(snake_case) |  | 缺失/枚举不一致 | ALTER |
| changed_by | bigint |  | 缺失 | ADD |
| changed_at | datetime |  | 缺失 | ADD |
| change_reason | varchar |  | 缺失 | ADD |

### 3.2 supplier_onboarding
（同上）

### 3.3 orders
（同上）

### 3.4 activity_registrations
（同上）

### 3.5 contents
（同上）

### 3.6 tenders
（同上）

## 4. 枚举统一差异（Enum）
| 枚举 | 目标值 | 现状值 | 风险 | 处理 |
|---|---|---|---|---|
| MemberLevel | normal/vip/svip | NORMAL/VIP/SVIP 或 free/vip/svip | 前后端不一致 | 网关兼容+库内统一 |
| verification_status | draft/submitted/... |  | 状态机断裂 | 统一 |
| order_status | created/... |  | 流转异常 | 统一 |

## 5. 索引与唯一约束差异
| 表 | 建议索引 | 现状 | 动作 |
|---|---|---|---|
| orders | (user_id, created_at) |  | ADD |
| activity_registrations | unique(activity_id, user_id) |  | ADD |
| supplier_onboarding | (status, updated_at) |  | ADD |

## 6. 审计与软删除
- 是否所有核心表都有：`created_at`, `updated_at`, `deleted_at`（如采用软删）
- 是否有操作者追踪：`created_by`, `updated_by`
- 是否有状态流转日志表：`state_transition_logs`

## 7. 迁移计划（执行批次）
### P0（必须先做）
- 状态字段统一
- 缺失核心表补齐：orders / activity_registrations / state_transition_logs
- 审计三字段补齐：changed_by/changed_at/change_reason

### P1
- contents / tenders 完整化
- 索引优化

### P2
- 历史字段清理、兼容层下线

## 8. 风险与回滚
- 风险：
- 回滚SQL：
- 数据备份策略：
