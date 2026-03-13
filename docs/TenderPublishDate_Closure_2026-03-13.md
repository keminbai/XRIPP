# Tender Publish Date Closure（2026-03-13）

## 1. 背景

在对 `临时讨论/讨论和执行记录.txt` 的复审中，发现标书 `publishDate` 问题只修到了“显示值”，还没有完全修到“筛选口径”和“数据库迁移留痕”：

1. 代码已经开始依赖 `tenders.published_at`
2. 但仓库中没有正式 DDL 记录该列的增加与回填
3. 管理端标书列表与导出仍使用 `created_at` 做日期范围筛选

这会导致：

- 新环境按仓库 DDL 重建数据库时缺列
- “发布日期筛选”名义上正确，实际口径还是创建时间

## 2. 本轮修复目标

1. 给 `published_at` 补正式版本化 DDL
2. 将管理端标书列表/导出日期筛选收敛到 `published_at`
3. 对前端与后端参数做兼容，避免旧链接立刻失效

## 3. DDL 变更

新增：

- `docs/DDL_Phase16_Tenders_PublishedAt.sql`

内容包括：

- 若不存在则新增 `tenders.published_at`
- 对历史 `tender_status='published'` 的记录用 `created_at` 回填
- 新增 `IX_tenders_status_published_at`

## 4. 代码变更

### 4.1 后端

- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminTendersV3Controller.java`
  - 列表接口新增 `published_from` / `published_to`
  - 继续兼容旧参数 `created_from` / `created_to`
  - 真正筛选字段切换为 `published_at`
  - 导出接口同样统一到 `published_at`

### 4.2 前端

- `app/pages/admin/tenders/manage.vue`
  - 查询与导出改发 `published_from` / `published_to`
  - 仍兼容从旧查询串读取 `created_from` / `created_to`

## 5. 结果

修复后，标书管理的“发布日期”做到三处一致：

1. 展示值使用 `published_at`
2. 筛选值使用 `published_at`
3. 数据库迁移有正式 DDL 留痕

## 6. 风险说明

- 当前未执行实库 DDL，本轮只补齐了仓库中的正式迁移脚本
- 若线上数据库尚未补列，部署前必须先执行 `DDL_Phase16_Tenders_PublishedAt.sql`
