/* =========================================================
   Phase 17: 活动记录闭环
   目标：
     1) 为活动管理补齐线下活动后的记录台账
     2) 独立保存实际参与人数、活动总结、完成状态
     3) 结构化保存现场照片元数据，支持后台预览/下载
   ========================================================= */

SET QUOTED_IDENTIFIER ON;
BEGIN TRY
  BEGIN TRAN;

  IF OBJECT_ID('dbo.activity_records', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.activity_records (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      activity_id BIGINT NOT NULL,
      actual_participants INT NOT NULL CONSTRAINT DF_activity_records_actual_participants DEFAULT 0,
      summary NVARCHAR(MAX) NULL,
      completion_status NVARCHAR(30) NOT NULL CONSTRAINT DF_activity_records_completion_status DEFAULT 'draft',
      recorded_by BIGINT NULL,
      completed_at DATETIME2 NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_activity_records_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_activity_records_updated_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF OBJECT_ID('dbo.activity_record_photos', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.activity_record_photos (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      activity_record_id BIGINT NOT NULL,
      file_name NVARCHAR(255) NULL,
      file_url NVARCHAR(500) NOT NULL,
      stored_name NVARCHAR(255) NULL,
      file_ext NVARCHAR(20) NULL,
      file_size BIGINT NULL,
      content_type NVARCHAR(100) NULL,
      sort_order INT NOT NULL CONSTRAINT DF_activity_record_photos_sort_order DEFAULT 0,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_activity_record_photos_created_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_activity_records_activity'
      AND object_id = OBJECT_ID('dbo.activity_records')
  )
    CREATE UNIQUE NONCLUSTERED INDEX UX_activity_records_activity
      ON dbo.activity_records(activity_id);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_activity_records_status_updated'
      AND object_id = OBJECT_ID('dbo.activity_records')
  )
    CREATE NONCLUSTERED INDEX IX_activity_records_status_updated
      ON dbo.activity_records(completion_status, updated_at DESC, id DESC);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_activity_record_photos_record_sort'
      AND object_id = OBJECT_ID('dbo.activity_record_photos')
  )
    CREATE NONCLUSTERED INDEX IX_activity_record_photos_record_sort
      ON dbo.activity_record_photos(activity_record_id, sort_order ASC, id ASC);

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
