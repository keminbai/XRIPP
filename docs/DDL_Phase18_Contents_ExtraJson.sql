/* =========================================================
   Phase 18: contents 元数据分离
   目标：
     1) 为 contents 增加 extra_json，避免 body 同时承担正文与元数据
     2) 支撑媒体/培训等内容管理页的结构化扩展字段
     3) 与历史数据兼容：旧数据仍可从 body JSON 回退解析
   ========================================================= */

BEGIN TRY
  BEGIN TRAN;

  IF COL_LENGTH('dbo.contents', 'extra_json') IS NULL
    ALTER TABLE dbo.contents ADD extra_json NVARCHAR(MAX) NULL;

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
