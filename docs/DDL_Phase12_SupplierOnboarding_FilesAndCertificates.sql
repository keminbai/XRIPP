/* =========================================================
   DDL Phase 12 — supplier_onboarding 附件与证书结构化
   目标：
   1) 文件不再只留在前端 fileList 或零散 JSON 中
   2) 证书编号/有效期/是否具备可结构化复查
   3) 为管理端详情、导出、下载、筛选打基础
   幂等：所有 CREATE / ALTER 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) supplier_onboarding 主表补审核完成标记
     ========================================================= */
  IF COL_LENGTH('dbo.supplier_onboarding', 'attachments_completed') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD attachments_completed BIT NOT NULL
      CONSTRAINT DF_supplier_onboarding_attachments_completed DEFAULT 0;

  IF COL_LENGTH('dbo.supplier_onboarding', 'certificates_completed') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD certificates_completed BIT NOT NULL
      CONSTRAINT DF_supplier_onboarding_certificates_completed DEFAULT 0;

  /* =========================================================
     2) 文件表：附件与材料
     ========================================================= */
  IF OBJECT_ID('dbo.supplier_onboarding_files', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.supplier_onboarding_files (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      onboarding_id BIGINT NOT NULL,
      file_category NVARCHAR(30) NOT NULL
        CHECK (file_category IN (
          'cover_image',
          'company_pdf',
          'promo_image',
          'business_license',
          'bank_license',
          'other'
        )),
      file_name NVARCHAR(255) NOT NULL,
      file_url NVARCHAR(1000) NOT NULL,
      file_ext NVARCHAR(20) NULL,
      file_size BIGINT NULL,
      sort_order INT NOT NULL DEFAULT 0,
      uploaded_by BIGINT NULL,
      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_supplier_onboarding_files_onboarding
      ON dbo.supplier_onboarding_files(onboarding_id, file_category, sort_order, created_at DESC);
  END

  /* =========================================================
     3) 证书表：资质结构化信息
     ========================================================= */
  IF OBJECT_ID('dbo.supplier_onboarding_certificates', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.supplier_onboarding_certificates (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      onboarding_id BIGINT NOT NULL,
      cert_type NVARCHAR(30) NOT NULL
        CHECK (cert_type IN (
          'ISO9001',
          'ISO14001',
          'ISO45001',
          'ISO13485',
          'IATF16949',
          'ISO22000',
          'ISO27001',
          'OTHER'
        )),
      cert_name NVARCHAR(100) NOT NULL,
      has_certificate BIT NOT NULL DEFAULT 0,
      cert_no NVARCHAR(100) NULL,
      valid_from DATE NULL,
      valid_to DATE NULL,
      issuer_name NVARCHAR(200) NULL,
      remarks NVARCHAR(500) NULL,
      attachment_file_id BIGINT NULL,
      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_supplier_onboarding_certificates_onboarding
      ON dbo.supplier_onboarding_certificates(onboarding_id, cert_type, created_at DESC);
  END

  COMMIT TRAN;
  PRINT 'Phase 12 — supplier_onboarding files and certificates complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
