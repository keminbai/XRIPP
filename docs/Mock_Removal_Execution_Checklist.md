# Mock 清理执行清单（Week2）

最后更新：2026-02-26

## 1. 目标

将核心联调页面从“mock + API 并存”收敛为“API 单一事实源”，避免验收假通过。

## 2. 范围与优先级

### P0（先做，直接影响联调真实性）

1. `app/pages/admin/tenders/reference.vue`
2. `app/pages/admin/members/un-audit.vue`
3. `app/pages/services.vue`

### P1（随后做，影响后台审核一致性）

1. `app/pages/admin/components/ActivityAuditList.vue`
2. `app/pages/admin/components/SupplierAuditList.vue`
3. `app/pages/admin/components/DemandAuditList.vue`

## 3. 每页改造要求

### 3.1 reference.vue

- 删除本地列表主数据与业务 `setTimeout` 模拟提交流程。
- 列表、详情、动作全部走 `apiRequest`。
- 保留仅 UI 相关的 `URL.revokeObjectURL` 定时释放。
- 所有动作失败必须展示后端错误码文案（至少 message）。

### 3.2 un-audit.vue

- 删除本地兜底审核数据与模拟审批逻辑。
- 证书下载 TODO 改为真实接口调用；若接口未就绪，按钮显式禁用并标注“待后端”。
- 审核动作结果以接口回包为准，不允许本地强行改状态。

### 3.3 services.vue

- 关闭/移除 `useMockAuth`、`mockIsLoggedIn`、`mockMemberLevel` 的运行开关能力（可保留开发期注释，不进生产逻辑）。
- 活动与供应商展示以 API 数据为主，不再以 mock 数据兜底覆盖真实结果。
- 权益展示只依赖登录态中的 `member_level`。

### 3.4 审核组件（P1）

- 删除 `mockData` 主数据源，统一改为上层注入 API 数据。
- 组件内部仅保留展示与事件派发，不保留业务状态机。

## 4. 验收标准（必须同时满足）

1. 代码中不再出现用于主流程的 `mockData`、业务 `setTimeout` 模拟成功。
2. 断网或后端报错时，页面展示真实失败，不出现“本地成功”。
3. Network 面板可追踪所有关键动作请求（查询、审核、提交、撤销）。
4. 权限与路由遵循基线：
   - `member` -> `/member`
   - `partner` -> `/admin/partner-publish`
   - `admin` -> `/admin`

## 5. 回归测试建议

1. 账号矩阵：`admin` / `p1001` / `m1001` 分别执行关键路径。
2. 负向用例：越权访问、非法状态迁移、空参数提交。
3. 数据一致性：页面刷新后状态与接口查询一致，不依赖本地缓存伪造。
