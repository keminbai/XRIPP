# API Implementation Plan v1

## 1. 目标
在不影响现网页面的前提下，从 `/api/v2` 平滑迁移到 `/api/v3`。

## 2. 总体策略
- 双轨并行：保留 v2，新增 v3。
- 先“读接口”后“写接口”。
- 先后台列表/详情，再前台提交流程。
- 网关层做字段兼容映射，前端不做脏转换。

## 3. 实施批次

### Batch A（P0，本周）
1. `GET /api/v3/activities`
2. `GET /api/v3/activities/{id}`
3. `GET /api/v3/admin/member-verifications`
4. `GET /api/v3/admin/supplier-onboarding`
5. `GET /api/v3/orders`
6. `GET /api/v3/orders/{id}`

验收：
- 前后台可只切“读取”到 v3，不改提交流程。
- 列表分页、筛选、排序正常。

### Batch B（P0，下一周）
1. `POST /api/v3/member-verifications`
2. `POST /api/v3/member-verifications/{id}/withdraw`
3. `POST /api/v3/admin/member-verifications/{id}/review`
4. `POST /api/v3/supplier-onboarding`
5. `POST /api/v3/admin/supplier-onboarding/{id}/review`
6. `POST /api/v3/activities/{id}/registrations`
7. `POST /api/v3/admin/orders/{id}/transition`

验收：
- 状态迁移全部受 `ARCH_StateMachine_v1.md` 约束。
- 非法迁移返回 `STATE_INVALID_TRANSITION`。

### Batch C（P1）
1. `POST /api/v3/contents`
2. `POST /api/v3/contents/{id}/submit-review`
3. `POST /api/v3/admin/contents/{id}/review`
4. `POST /api/v3/admin/contents/{id}/publish`
5. `POST /api/v3/admin/contents/{id}/offline`
6. `POST /api/v3/tenders`
7. `POST /api/v3/admin/tenders/{id}/publish`
8. `POST /api/v3/admin/tenders/{id}/close`

## 4. 兼容映射（网关层）
- `vip_level` -> `member_level`
  - `FREE` -> `normal`
  - `VIP` -> `vip`
  - `SVIP` -> `svip`
- `audit_status`(0/10/20/30/40/50) -> 新状态枚举（按模块映射）
- 响应统一包装：
  - v2 非统一格式 -> v3 `{code,message,data,request_id,timestamp}`

## 5. 前端切换顺序（建议）
1. 后台审核列表页先切 v3（只读）
2. 前台活动列表/详情切 v3（只读）
3. 再切提交动作（报名、申请、审核）
4. 最后下线 v2 页面调用

## 6. 测试清单（最小）
- 状态机迁移测试：每个状态至少1条正向+1条逆向非法用例
- 权限测试：member/partner/operator/auditor/admin 各1条
- 幂等测试：重复提交、重复回调
- 分页筛选测试：边界页、空结果、大页码
- 兼容测试：旧页面调用 v2 不受影响

## 7. 上线闸门（Go/No-Go）
- DDL迁移已执行并回归通过
- v3 P0接口联调通过率 >= 95%
- 关键页面无阻塞缺陷（P0=0）
- 回滚预案已演练

## 8. 回滚方案
- 应用回滚：网关路由切回 v2
- 数据回滚：执行预备回滚SQL（按表分批）
- 功能降级：关闭 v3 写接口，仅保留读接口
