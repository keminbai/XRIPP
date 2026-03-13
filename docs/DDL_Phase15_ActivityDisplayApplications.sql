BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     Phase 15: 活动显示申请闭环
     目标：
       1) 支持活动提交首页/活动中心/平台服务展示申请
       2) 支持后台显示管理审核、上下线、排序
       3) 支持公共活动接口按展示区域读取已批准且在有效期内的活动
     ========================================================= */

  IF OBJECT_ID('dbo.activity_display_applications', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.activity_display_applications (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      activity_id BIGINT NOT NULL,
      display_area NVARCHAR(30) NOT NULL,
      display_start_at DATETIME2 NOT NULL,
      display_end_at DATETIME2 NOT NULL,
      sort_order INT NOT NULL CONSTRAINT DF_activity_display_sort_order DEFAULT 0,
      apply_status NVARCHAR(30) NOT NULL CONSTRAINT DF_activity_display_apply_status DEFAULT 'pending_review',
      applied_by BIGINT NULL,
      reviewed_by BIGINT NULL,
      reviewed_at DATETIME2 NULL,
      change_reason NVARCHAR(1000) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_activity_display_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_activity_display_updated_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_activity_display_area_status_time'
      AND object_id = OBJECT_ID('dbo.activity_display_applications')
  )
    CREATE NONCLUSTERED INDEX IX_activity_display_area_status_time
      ON dbo.activity_display_applications(display_area, apply_status, display_start_at, display_end_at);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_activity_display_activity'
      AND object_id = OBJECT_ID('dbo.activity_display_applications')
  )
    CREATE NONCLUSTERED INDEX IX_activity_display_activity
      ON dbo.activity_display_applications(activity_id, created_at DESC);

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
