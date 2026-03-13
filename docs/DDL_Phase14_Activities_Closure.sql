/* =========================================================
   Phase 14: activities 内容管理闭环扩展（后台活动管理落库/编辑/上下架）
   执行时间：在 Phase 13 之后
   目标：
     1) 补齐活动运营配置字段，避免只存 title/fee/start_time
     2) 支持后台编辑回显、前台投放位置、城市投放、媒体素材、议程、会员价
     3) 支持驳回原因/上下架等状态操作持久化
   注意：分两个 batch，因为 SQL Server 在同一事务内
         ALTER TABLE ADD 的新列对 CREATE INDEX 不可见
   ========================================================= */

-- Batch 1: 新增列
BEGIN TRY
  BEGIN TRAN;

  IF COL_LENGTH('dbo.activities', 'activity_no') IS NULL
    ALTER TABLE dbo.activities ADD activity_no NVARCHAR(30) NULL;

  IF COL_LENGTH('dbo.activities', 'front_module') IS NULL
    ALTER TABLE dbo.activities ADD front_module NVARCHAR(50) NULL;

  IF COL_LENGTH('dbo.activities', 'front_position') IS NULL
    ALTER TABLE dbo.activities ADD front_position NVARCHAR(50) NULL;

  IF COL_LENGTH('dbo.activities', 'cities_json') IS NULL
    ALTER TABLE dbo.activities ADD cities_json NVARCHAR(1000) NULL;

  IF COL_LENGTH('dbo.activities', 'video_url') IS NULL
    ALTER TABLE dbo.activities ADD video_url NVARCHAR(500) NULL;

  IF COL_LENGTH('dbo.activities', 'agenda') IS NULL
    ALTER TABLE dbo.activities ADD agenda NVARCHAR(MAX) NULL;

  IF COL_LENGTH('dbo.activities', 'max_limit') IS NULL
    ALTER TABLE dbo.activities ADD max_limit INT NOT NULL CONSTRAINT DF_activities_max_limit DEFAULT 0;

  IF COL_LENGTH('dbo.activities', 'current_participants') IS NULL
    ALTER TABLE dbo.activities ADD current_participants INT NOT NULL CONSTRAINT DF_activities_current_participants DEFAULT 0;

  IF COL_LENGTH('dbo.activities', 'fee_item_id') IS NULL
    ALTER TABLE dbo.activities ADD fee_item_id NVARCHAR(50) NULL;

  IF COL_LENGTH('dbo.activities', 'fee_item_name') IS NULL
    ALTER TABLE dbo.activities ADD fee_item_name NVARCHAR(100) NULL;

  IF COL_LENGTH('dbo.activities', 'fee_type') IS NULL
    ALTER TABLE dbo.activities ADD fee_type NVARCHAR(30) NULL;

  IF COL_LENGTH('dbo.activities', 'member_price') IS NULL
    ALTER TABLE dbo.activities ADD member_price DECIMAL(10,2) NULL;

  IF COL_LENGTH('dbo.activities', 'changed_by') IS NULL
    ALTER TABLE dbo.activities ADD changed_by BIGINT NULL;

  IF COL_LENGTH('dbo.activities', 'changed_at') IS NULL
    ALTER TABLE dbo.activities ADD changed_at DATETIME2 NULL;

  IF COL_LENGTH('dbo.activities', 'change_reason') IS NULL
    ALTER TABLE dbo.activities ADD change_reason NVARCHAR(1000) NULL;

  COMMIT TRAN;
  PRINT 'Phase 14 Batch 1 — activities columns added';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO

-- Batch 2: 建索引（新列已提交，此 batch 可见）
SET QUOTED_IDENTIFIER ON;
BEGIN TRY
  BEGIN TRAN;

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_activities_activity_no'
      AND object_id = OBJECT_ID('dbo.activities')
  )
    CREATE UNIQUE NONCLUSTERED INDEX UX_activities_activity_no
      ON dbo.activities(activity_no)
      WHERE activity_no IS NOT NULL;

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_activities_partner_audit'
      AND object_id = OBJECT_ID('dbo.activities')
  )
    CREATE NONCLUSTERED INDEX IX_activities_partner_audit
      ON dbo.activities(partner_id, audit_status, created_at DESC);

  COMMIT TRAN;
  PRINT 'Phase 14 Batch 2 — activities indexes created';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
