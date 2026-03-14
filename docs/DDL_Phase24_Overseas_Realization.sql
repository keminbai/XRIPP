/* =========================================================
   DDL Phase 24 — 出海发布系统真实化
   目标：
   1) 将 admin/overseas/services、reports、analysis 从 session-only 改为真实后端驱动
   2) services 的图片/文件不再停留在前端内存数组，必须结构化持久化
   3) reports 的封面、报告文件、关键词、收费与权限口径必须可查询、可编辑、可统计
   4) analysis 不建假统计表，统一基于 overseas_points + overseas_services + overseas_reports 聚合
   5) 补齐 RBAC 的 overseas_management 模块，避免海外模块继续只能 super-admin 访问
   幂等：所有 CREATE / ALTER / INSERT 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) 出海服务主表
     ========================================================= */
  IF OBJECT_ID('dbo.overseas_services', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.overseas_services (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      service_type NVARCHAR(20) NOT NULL
        CHECK (service_type IN ('trade', 'ip', 'cross', 'invest', 'other')),
      project_code NVARCHAR(50) NOT NULL,
      title NVARCHAR(150) NOT NULL,
      countries_json NVARCHAR(MAX) NOT NULL CONSTRAINT DF_overseas_services_countries_json DEFAULT N'[]',
      summary NVARCHAR(500) NOT NULL,
      content NVARCHAR(MAX) NOT NULL,
      price_type NVARCHAR(20) NOT NULL
        CONSTRAINT DF_overseas_services_price_type DEFAULT 'negotiable'
        CHECK (price_type IN ('free', 'fixed', 'negotiable')),
      price_amount DECIMAL(18,2) NULL,
      duration_desc NVARCHAR(100) NULL,
      contact_person NVARCHAR(100) NULL,
      contact_phone NVARCHAR(50) NULL,
      contact_email NVARCHAR(200) NULL,
      tags_json NVARCHAR(MAX) NOT NULL CONSTRAINT DF_overseas_services_tags_json DEFAULT N'[]',
      status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_overseas_services_status DEFAULT 'draft'
        CHECK (status IN ('draft', 'published', 'offline')),
      views_count INT NOT NULL CONSTRAINT DF_overseas_services_views_count DEFAULT 0,
      inquiries_count INT NOT NULL CONSTRAINT DF_overseas_services_inquiries_count DEFAULT 0,
      published_at DATETIME2 NULL,
      created_by BIGINT NULL,
      updated_by BIGINT NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_services_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_services_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE NONCLUSTERED INDEX IX_overseas_services_status_type
      ON dbo.overseas_services(status, service_type, id DESC);

    CREATE NONCLUSTERED INDEX IX_overseas_services_project
      ON dbo.overseas_services(project_code, id DESC);

    CREATE NONCLUSTERED INDEX IX_overseas_services_published_at
      ON dbo.overseas_services(published_at DESC, id DESC);
  END

  /* =========================================================
     2) 出海服务附件表
     ========================================================= */
  IF OBJECT_ID('dbo.overseas_service_files', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.overseas_service_files (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      service_id BIGINT NOT NULL,
      file_category NVARCHAR(20) NOT NULL
        CHECK (file_category IN ('promo_image', 'gallery_image', 'intro_file')),
      file_name NVARCHAR(255) NOT NULL,
      file_url NVARCHAR(1000) NOT NULL,
      file_ext NVARCHAR(20) NULL,
      file_size BIGINT NULL,
      sort_order INT NOT NULL CONSTRAINT DF_overseas_service_files_sort_order DEFAULT 0,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_service_files_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_service_files_updated_at DEFAULT SYSUTCDATETIME()
    );

    ALTER TABLE dbo.overseas_service_files
      ADD CONSTRAINT FK_overseas_service_files_service
      FOREIGN KEY (service_id) REFERENCES dbo.overseas_services(id);

    CREATE NONCLUSTERED INDEX IX_overseas_service_files_service
      ON dbo.overseas_service_files(service_id, file_category, sort_order, id);
  END

  /* =========================================================
     3) 出海报告主表
     ========================================================= */
  IF OBJECT_ID('dbo.overseas_reports', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.overseas_reports (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      title NVARCHAR(200) NOT NULL,
      country NVARCHAR(100) NOT NULL,
      industry_code NVARCHAR(50) NOT NULL,
      report_type NVARCHAR(50) NOT NULL,
      report_year NVARCHAR(4) NULL,
      publish_date DATE NOT NULL,
      expire_date DATE NULL,
      summary NVARCHAR(2000) NOT NULL,
      cover_image_url NVARCHAR(1000) NULL,
      cover_image_name NVARCHAR(255) NULL,
      cover_image_ext NVARCHAR(20) NULL,
      cover_image_size BIGINT NULL,
      report_file_url NVARCHAR(1000) NOT NULL,
      report_file_name NVARCHAR(255) NOT NULL,
      report_file_ext NVARCHAR(20) NULL,
      report_file_size BIGINT NULL,
      keywords_json NVARCHAR(MAX) NOT NULL CONSTRAINT DF_overseas_reports_keywords_json DEFAULT N'[]',
      source_name NVARCHAR(200) NULL,
      fee_level NVARCHAR(20) NOT NULL
        CONSTRAINT DF_overseas_reports_fee_level DEFAULT 'free'
        CHECK (fee_level IN ('free', 'fee_1', 'fee_2')),
      access_level NVARCHAR(20) NOT NULL
        CONSTRAINT DF_overseas_reports_access_level DEFAULT 'public'
        CHECK (access_level IN ('public', 'vip', 'svip')),
      is_recommended BIT NOT NULL CONSTRAINT DF_overseas_reports_is_recommended DEFAULT 0,
      downloads_count INT NOT NULL CONSTRAINT DF_overseas_reports_downloads_count DEFAULT 0,
      created_by BIGINT NULL,
      updated_by BIGINT NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_reports_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_overseas_reports_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE NONCLUSTERED INDEX IX_overseas_reports_publish_date
      ON dbo.overseas_reports(publish_date DESC, id DESC);

    CREATE NONCLUSTERED INDEX IX_overseas_reports_country_industry
      ON dbo.overseas_reports(country, industry_code, report_type, id DESC);

    CREATE NONCLUSTERED INDEX IX_overseas_reports_expire_date
      ON dbo.overseas_reports(expire_date, id DESC);
  END

  /* =========================================================
     4) RBAC 扩展：海外模块权限
     ========================================================= */
  IF OBJECT_ID('dbo.sys_permission_profiles', 'U') IS NOT NULL
     AND OBJECT_ID('dbo.sys_permission_grants', 'U') IS NOT NULL
  BEGIN
    DECLARE @publisherProfileId BIGINT = (
      SELECT TOP 1 id FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_PUBLISHER'
    );
    DECLARE @auditorProfileId BIGINT = (
      SELECT TOP 1 id FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_AUDITOR'
    );

    IF @publisherProfileId IS NOT NULL
       AND NOT EXISTS (
         SELECT 1 FROM dbo.sys_permission_grants
         WHERE profile_id = @publisherProfileId AND module_code = 'overseas_management'
       )
    BEGIN
      INSERT INTO dbo.sys_permission_grants (
        profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
      ) VALUES (
        @publisherProfileId, 'overseas_management', N'出海发布系统', 1, 1, 1, 1, 0, 1
      );
    END

    IF @auditorProfileId IS NOT NULL
       AND NOT EXISTS (
         SELECT 1 FROM dbo.sys_permission_grants
         WHERE profile_id = @auditorProfileId AND module_code = 'overseas_management'
       )
    BEGIN
      INSERT INTO dbo.sys_permission_grants (
        profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
      ) VALUES (
        @auditorProfileId, 'overseas_management', N'出海发布系统', 1, 0, 0, 0, 0, 1
      );
    END
  END

  COMMIT TRAN;
  PRINT 'Phase 24 — overseas services/reports realization complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
