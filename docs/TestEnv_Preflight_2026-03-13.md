# Test Env Preflight（2026-03-13）

## 当前状态说明（2026-03-13 晚间复核）

本检查表原本是“准备开测前的 1 分钟检查”。
但根据最新真实运行日志，当前环境仍存在运行实例漂移 + schema 漂移，因此应先完成运行恢复，再按本表逐项确认。

当前已确认阻断：

- 运行中的后端实例未加载最新会员 CRUD 接口
- `contents.extra_json` 缺失
- `activity_records` / `activity_record_photos` 缺失

恢复入口：

- `docs/RuntimeRecovery_S0S1_2026-03-13.md`
- `docs/SQL_Runtime_Schema_Check_2026-03-13.sql`

## 上线前 1 分钟检查

- 数据库已执行：
  - Phase 2
  - Phase 6 ~ Phase 18
- 关键表存在：
  - `payment_orders`
  - `supplier_onboarding_files`
  - `supplier_onboarding_certificates`
  - `activity_display_applications`
  - `activity_records`
  - `activity_record_photos`
  - `member_profile.member_level`
  - `contents.extra_json`
  - `tenders.published_at`
- 测试账号可登录：
  - `admin`
  - `partner`
  - `member`
- 上传文件可访问
- 首页、服务页、后台首页能正常打开
- 后端接口无 500 连续报错
- 前端控制台无阻塞级报错

## 发现异常时优先检查

- 测试库 DDL 是否漏执行
- 前后端是否连到同一套环境
- 上传目录映射是否正常
- 角色账号是否带正确 partner_id / user_id
- 若后端启动即报 `Database schema preflight failed`
  - 按错误提示执行对应 `docs/DDL_Phase*.sql`
- 若 `POST /api/v3/admin/members` 返回 `Request method 'POST' is not supported`
  - 说明运行中的后端不是当前仓库版本，需要先重启到最新代码
