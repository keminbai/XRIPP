/* =========================================================
   DDL Phase 22 — 系统通知模块核心模型
   目标：
   1) 为通知类型设置提供真实持久化来源
   2) 为通知模板管理提供独立结构化存储
   3) 为批量发送动作提供发送记录留痕
   4) 不把通知系统塞进 Phase 20 配置表
   幂等：所有 CREATE 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) 通知类型设置
     ========================================================= */
  IF OBJECT_ID('dbo.notification_type_settings', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.notification_type_settings (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      notification_type NVARCHAR(30) NOT NULL
        CHECK (notification_type IN ('payment', 'activity', 'urgent')),
      display_name NVARCHAR(100) NOT NULL,
      sms_enabled BIT NOT NULL CONSTRAINT DF_notification_type_settings_sms_enabled DEFAULT 1,
      in_app_enabled BIT NOT NULL CONSTRAINT DF_notification_type_settings_in_app_enabled DEFAULT 1,
      email_enabled BIT NOT NULL CONSTRAINT DF_notification_type_settings_email_enabled DEFAULT 0,
      description NVARCHAR(500) NULL,
      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_notification_type_settings_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_notification_type_settings_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE UNIQUE NONCLUSTERED INDEX UX_notification_type_settings_type
      ON dbo.notification_type_settings(notification_type);
  END

  /* =========================================================
     2) 通知模板
     ========================================================= */
  IF OBJECT_ID('dbo.notification_templates', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.notification_templates (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      template_name NVARCHAR(200) NOT NULL,
      notification_type NVARCHAR(30) NOT NULL
        CHECK (notification_type IN ('payment', 'activity', 'urgent')),
      template_content NVARCHAR(4000) NOT NULL,
      enabled BIT NOT NULL CONSTRAINT DF_notification_templates_enabled DEFAULT 1,
      sort_order INT NOT NULL CONSTRAINT DF_notification_templates_sort_order DEFAULT 0,
      created_by BIGINT NULL,
      updated_by BIGINT NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_notification_templates_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_notification_templates_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE NONCLUSTERED INDEX IX_notification_templates_type_sort
      ON dbo.notification_templates(notification_type, sort_order ASC, id ASC);
  END

  /* =========================================================
     3) 通知发送记录
     ========================================================= */
  IF OBJECT_ID('dbo.notification_send_logs', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.notification_send_logs (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      notification_type NVARCHAR(30) NOT NULL
        CHECK (notification_type IN ('payment', 'activity', 'urgent')),
      target_scope NVARCHAR(30) NOT NULL
        CHECK (target_scope IN ('all', 'member', 'supplier', 'partner', 'custom')),
      notification_title NVARCHAR(200) NOT NULL,
      notification_content NVARCHAR(4000) NOT NULL,
      send_status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_notification_send_logs_send_status DEFAULT 'accepted'
        CHECK (send_status IN ('accepted', 'processed', 'failed')),
      requested_by BIGINT NULL,
      requested_at DATETIME2 NOT NULL CONSTRAINT DF_notification_send_logs_requested_at DEFAULT SYSUTCDATETIME(),
      processed_at DATETIME2 NULL,
      result_message NVARCHAR(1000) NULL
    );

    CREATE NONCLUSTERED INDEX IX_notification_send_logs_requested
      ON dbo.notification_send_logs(requested_at DESC, id DESC);

    CREATE NONCLUSTERED INDEX IX_notification_send_logs_type_status
      ON dbo.notification_send_logs(notification_type, send_status, requested_at DESC);
  END

  COMMIT TRAN;
  PRINT 'Phase 22 — notification module core tables complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
