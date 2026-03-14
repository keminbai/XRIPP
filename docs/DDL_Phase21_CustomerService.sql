/* =========================================================
   DDL Phase 21 — 客服系统核心模型
   目标：
   1) 为后台“留言记录”提供真实持久化来源
   2) 为会员端/后台端“客服工单”提供真实闭环
   3) 为工单截图/附件提供结构化文件存储
   4) 不把工单/留言强塞进 Phase 20 配置表
   幂等：所有 CREATE 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) 留言记录表
     ========================================================= */
  IF OBJECT_ID('dbo.customer_service_messages', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.customer_service_messages (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      user_id BIGINT NULL,
      member_level_snapshot NVARCHAR(20) NULL,
      name NVARCHAR(100) NOT NULL,
      phone NVARCHAR(50) NOT NULL,
      source_channel NVARCHAR(30) NOT NULL
        CONSTRAINT DF_customer_service_messages_source_channel DEFAULT 'member_portal'
        CHECK (source_channel IN ('member_portal', 'admin_manual', 'website', 'other')),
      content NVARCHAR(2000) NOT NULL,
      status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_customer_service_messages_status DEFAULT 'pending'
        CHECK (status IN ('pending', 'done')),
      handled_by BIGINT NULL,
      handled_at DATETIME2 NULL,
      handle_remark NVARCHAR(1000) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_messages_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_messages_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE NONCLUSTERED INDEX IX_customer_service_messages_status_created
      ON dbo.customer_service_messages(status, created_at DESC, id DESC);

    CREATE NONCLUSTERED INDEX IX_customer_service_messages_user_created
      ON dbo.customer_service_messages(user_id, created_at DESC, id DESC);
  END

  /* =========================================================
     2) 客服工单表
     ========================================================= */
  IF OBJECT_ID('dbo.customer_service_tickets', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.customer_service_tickets (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      ticket_no NVARCHAR(50) NOT NULL,
      user_id BIGINT NULL,
      partner_id BIGINT NULL,
      member_level_snapshot NVARCHAR(20) NULL,
      ticket_type NVARCHAR(30) NOT NULL
        CONSTRAINT DF_customer_service_tickets_ticket_type DEFAULT 'other'
        CHECK (ticket_type IN ('finance', 'account', 'tender', 'activity', 'other')),
      priority NVARCHAR(20) NOT NULL
        CONSTRAINT DF_customer_service_tickets_priority DEFAULT 'normal'
        CHECK (priority IN ('low', 'normal', 'high', 'urgent')),
      title NVARCHAR(200) NOT NULL,
      content NVARCHAR(4000) NOT NULL,
      contact_name NVARCHAR(100) NOT NULL,
      contact_phone NVARCHAR(50) NOT NULL,
      status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_customer_service_tickets_status DEFAULT 'open'
        CHECK (status IN ('open', 'processing', 'closed')),
      reply NVARCHAR(4000) NULL,
      source_channel NVARCHAR(30) NOT NULL
        CONSTRAINT DF_customer_service_tickets_source_channel DEFAULT 'member_portal'
        CHECK (source_channel IN ('member_portal', 'admin_manual', 'website', 'other')),
      created_by_admin BIT NOT NULL CONSTRAINT DF_customer_service_tickets_created_by_admin DEFAULT 0,
      last_replied_by BIGINT NULL,
      closed_at DATETIME2 NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_tickets_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_tickets_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE UNIQUE NONCLUSTERED INDEX UX_customer_service_tickets_ticket_no
      ON dbo.customer_service_tickets(ticket_no);

    CREATE NONCLUSTERED INDEX IX_customer_service_tickets_user_created
      ON dbo.customer_service_tickets(user_id, created_at DESC, id DESC);

    CREATE NONCLUSTERED INDEX IX_customer_service_tickets_status_priority
      ON dbo.customer_service_tickets(status, priority, updated_at DESC, id DESC);
  END

  /* =========================================================
     3) 工单附件表
     ========================================================= */
  IF OBJECT_ID('dbo.customer_service_ticket_files', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.customer_service_ticket_files (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      ticket_id BIGINT NOT NULL,
      file_name NVARCHAR(255) NOT NULL,
      file_url NVARCHAR(1000) NOT NULL,
      file_ext NVARCHAR(20) NULL,
      file_size BIGINT NULL,
      content_type NVARCHAR(100) NULL,
      sort_order INT NOT NULL CONSTRAINT DF_customer_service_ticket_files_sort_order DEFAULT 0,
      uploaded_by BIGINT NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_ticket_files_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_customer_service_ticket_files_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE NONCLUSTERED INDEX IX_customer_service_ticket_files_ticket
      ON dbo.customer_service_ticket_files(ticket_id, sort_order ASC, id ASC);
  END

  COMMIT TRAN;
  PRINT 'Phase 21 — customer service core tables complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
