/*
  DDL_Phase2_Migration.sql
  Target: SQL Server 2022
  Strategy: additive + backward-compatible
*/

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     0) 通用状态流转日志（P0）
     ========================================================= */
  IF OBJECT_ID('dbo.state_transition_logs', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.state_transition_logs (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      target_type NVARCHAR(50) NOT NULL,
      target_id BIGINT NOT NULL,
      from_status NVARCHAR(50) NULL,
      to_status NVARCHAR(50) NOT NULL,
      action NVARCHAR(50) NULL,
      changed_by BIGINT NULL,
      change_reason NVARCHAR(500) NULL,
      changed_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_state_transition_target ON dbo.state_transition_logs(target_type, target_id, changed_at DESC);
  END

  /* =========================================================
     1) 会员认证 member_verifications（P0）
     ========================================================= */
  IF OBJECT_ID('dbo.member_verifications', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.member_verifications (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      user_id BIGINT NOT NULL,
      company_name NVARCHAR(200) NOT NULL,
      contact_name NVARCHAR(50) NOT NULL,
      phone NVARCHAR(20) NOT NULL,
      industry NVARCHAR(50) NULL,
      documents_json NVARCHAR(MAX) NULL,

      verification_status NVARCHAR(30) NOT NULL DEFAULT 'draft'
        CHECK (verification_status IN ('draft','submitted','under_review','approved','rejected','withdrawn')),

      submitted_at DATETIME2 NULL,
      reviewed_at DATETIME2 NULL,

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_member_verif_user ON dbo.member_verifications(user_id, created_at DESC);
    CREATE INDEX IX_member_verif_status ON dbo.member_verifications(verification_status, updated_at DESC);
  END

  /* =========================================================
     2) 供应商入库 supplier_onboarding（P0）
     ========================================================= */
  IF OBJECT_ID('dbo.supplier_onboarding', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.supplier_onboarding (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      supplier_id BIGINT NULL,
      partner_id BIGINT NULL,
      company_name NVARCHAR(200) NOT NULL,
      city_name NVARCHAR(50) NULL,
      service_types_json NVARCHAR(MAX) NULL,
      intro NVARCHAR(1000) NULL,

      onboarding_status NVARCHAR(30) NOT NULL DEFAULT 'draft'
        CHECK (onboarding_status IN (
          'draft','submitted',
          'precheck_passed','precheck_failed',
          'final_review_passed','final_review_failed',
          'active','inactive'
        )),

      submitted_at DATETIME2 NULL,
      reviewed_at DATETIME2 NULL,

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_supplier_onboarding_status ON dbo.supplier_onboarding(onboarding_status, updated_at DESC);
    CREATE INDEX IX_supplier_onboarding_partner ON dbo.supplier_onboarding(partner_id, created_at DESC);
  END

  /* =========================================================
     3) 活动报名 activity_registrations（P0）
     ========================================================= */
  IF OBJECT_ID('dbo.activity_registrations', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.activity_registrations (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      activity_id BIGINT NOT NULL,
      user_id BIGINT NOT NULL,
      company_name NVARCHAR(200) NULL,
      contact_name NVARCHAR(50) NULL,
      phone NVARCHAR(20) NULL,

      registration_status NVARCHAR(30) NOT NULL
        CHECK (registration_status IN ('pending_payment','paid','confirmed','cancelled','refunded')),

      amount DECIMAL(10,2) NOT NULL DEFAULT 0,
      paid_at DATETIME2 NULL,

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

      CONSTRAINT UQ_activity_reg UNIQUE(activity_id, user_id)
    );

    CREATE INDEX IX_activity_reg_user ON dbo.activity_registrations(user_id, created_at DESC);
    CREATE INDEX IX_activity_reg_status ON dbo.activity_registrations(registration_status, updated_at DESC);
  END

  /* =========================================================
     4) 订单 orders（P0）
     ========================================================= */
  IF OBJECT_ID('dbo.orders', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.orders (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      order_no NVARCHAR(50) NOT NULL UNIQUE,
      user_id BIGINT NOT NULL,
      partner_id BIGINT NULL,

      order_type NVARCHAR(30) NOT NULL
        CHECK (order_type IN ('activity','service','tender','report','other')),

      order_status NVARCHAR(30) NOT NULL
        CHECK (order_status IN ('created','awaiting_payment','paid','in_service','completed','closed','refunding','refunded')),

      amount DECIMAL(12,2) NOT NULL DEFAULT 0,
      currency_code NVARCHAR(10) NOT NULL DEFAULT 'CNY',

      biz_type NVARCHAR(50) NULL,
      biz_id BIGINT NULL,

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_orders_user_time ON dbo.orders(user_id, created_at DESC);
    CREATE INDEX IX_orders_status_time ON dbo.orders(order_status, updated_at DESC);
  END

  /* =========================================================
     5) 内容发布 contents（P1）
     ========================================================= */
  IF OBJECT_ID('dbo.contents', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.contents (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      title NVARCHAR(200) NOT NULL,
      content_type NVARCHAR(30) NOT NULL
        CHECK (content_type IN ('activity','media','ad','training')),
      summary NVARCHAR(500) NULL,
      body NVARCHAR(MAX) NULL,

      publish_status NVARCHAR(30) NOT NULL DEFAULT 'draft'
        CHECK (publish_status IN ('draft','pending_review','approved','published','rejected','offline')),

      city_name NVARCHAR(50) NULL,
      is_paid BIT NOT NULL DEFAULT 0,
      fee DECIMAL(10,2) NOT NULL DEFAULT 0,

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_by BIGINT NULL,
      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_contents_type_status ON dbo.contents(content_type, publish_status, updated_at DESC);
  END

  /* =========================================================
     6) 标书 tenders（P1）
     ========================================================= */
  IF OBJECT_ID('dbo.tenders', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.tenders (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      tender_no NVARCHAR(50) NOT NULL UNIQUE,
      title NVARCHAR(200) NOT NULL,
      organization_name NVARCHAR(200) NULL,
      country NVARCHAR(50) NULL,
      deadline_at DATETIME2 NULL,

      tender_status NVARCHAR(30) NOT NULL DEFAULT 'draft'
        CHECK (tender_status IN ('draft','published','closed','archived')),

      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,

      created_by BIGINT NULL,
      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_tenders_status_time ON dbo.tenders(tender_status, updated_at DESC);
  END

  /* =========================================================
     7) 兼容改造：member_profile 会员等级统一（P0）
     ========================================================= */
  IF COL_LENGTH('dbo.member_profile', 'member_level') IS NULL
  BEGIN
    ALTER TABLE dbo.member_profile ADD member_level NVARCHAR(20) NULL;
  END

  UPDATE dbo.member_profile
  SET member_level = CASE UPPER(ISNULL(vip_level, 'FREE'))
    WHEN 'SVIP' THEN 'svip'
    WHEN 'VIP' THEN 'vip'
    ELSE 'normal'
  END
  WHERE member_level IS NULL;

  IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_member_profile_member_level')
  BEGIN
    ALTER TABLE dbo.member_profile DROP CONSTRAINT CK_member_profile_member_level;
  END
  ALTER TABLE dbo.member_profile
    ADD CONSTRAINT CK_member_profile_member_level CHECK (member_level IN ('normal','vip','svip'));

  /* =========================================================
     8) 兼容改造：suppliers 增加新状态列（P0）
     ========================================================= */
  IF COL_LENGTH('dbo.suppliers', 'onboarding_status') IS NULL
  BEGIN
    ALTER TABLE dbo.suppliers ADD onboarding_status NVARCHAR(30) NULL;
  END

  UPDATE dbo.suppliers
  SET onboarding_status = CASE audit_status
    WHEN 0 THEN 'draft'
    WHEN 10 THEN 'submitted'
    WHEN 20 THEN 'precheck_passed'
    WHEN 30 THEN 'active'
    WHEN 40 THEN 'final_review_failed'
    WHEN 50 THEN 'inactive'
    ELSE 'draft'
  END
  WHERE onboarding_status IS NULL;

  IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_suppliers_onboarding_status')
  BEGIN
    ALTER TABLE dbo.suppliers DROP CONSTRAINT CK_suppliers_onboarding_status;
  END
  ALTER TABLE dbo.suppliers
    ADD CONSTRAINT CK_suppliers_onboarding_status CHECK (onboarding_status IN (
      'draft','submitted','precheck_passed','precheck_failed',
      'final_review_passed','final_review_failed','active','inactive'
    ));

  /* =========================================================
     9) 标书下载日志 tender_download_logs（P0 · 防重复扣减核心）
     =========================================================
     业务规则：VIP/SVIP 会员重复下载同一标书不扣减次数。
     实现方式：INSERT OR IGNORE（后端 DuplicateKeyException 视为"已下载过"）。
     ⚠️ 禁止对该表做 DELETE，日志只增不减。
     ========================================================= */
  IF OBJECT_ID('dbo.tender_download_logs', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.tender_download_logs (
      id           BIGINT IDENTITY(1,1) PRIMARY KEY,
      user_id      BIGINT NOT NULL,
      tender_id    BIGINT NOT NULL,
      downloaded_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

      CONSTRAINT UQ_tender_download UNIQUE (user_id, tender_id)
    );

    CREATE INDEX IX_tender_dl_user ON dbo.tender_download_logs(user_id, downloaded_at DESC);
  END

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
