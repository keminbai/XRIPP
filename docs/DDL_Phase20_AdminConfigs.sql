/* =========================================================
   Phase 20: 后台通用配置存储
   目标：
     1) 为 admin 侧配置型页面提供统一持久化底座
     2) 首批承接 finance/pricing 页面真实化
     3) 为 business/*、system/* 后续接入复用同一套配置表
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  IF OBJECT_ID('dbo.admin_config_entries', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.admin_config_entries (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      config_namespace NVARCHAR(50) NOT NULL,
      config_key NVARCHAR(100) NOT NULL,
      config_name NVARCHAR(100) NULL,
      description NVARCHAR(500) NULL,
      config_value_json NVARCHAR(MAX) NULL,
      sort_order INT NOT NULL CONSTRAINT DF_admin_config_entries_sort_order DEFAULT 0,
      enabled BIT NOT NULL CONSTRAINT DF_admin_config_entries_enabled DEFAULT 1,
      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_admin_config_entries_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_admin_config_entries_updated_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_admin_config_entries_namespace_key'
      AND object_id = OBJECT_ID('dbo.admin_config_entries')
  )
    CREATE UNIQUE NONCLUSTERED INDEX UX_admin_config_entries_namespace_key
      ON dbo.admin_config_entries(config_namespace, config_key);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_admin_config_entries_namespace_sort'
      AND object_id = OBJECT_ID('dbo.admin_config_entries')
  )
    CREATE NONCLUSTERED INDEX IX_admin_config_entries_namespace_sort
      ON dbo.admin_config_entries(config_namespace, sort_order ASC, id ASC);

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
