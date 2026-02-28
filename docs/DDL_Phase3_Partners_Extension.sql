/*
  DDL_Phase3_Partners_Extension.sql
  Target: SQL Server 2022
  Strategy: additive, idempotent (COL_LENGTH guard)
  Purpose: Add partner lifecycle fields for renewal / expiry management
*/

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* updated_at: 审计时间戳 */
  IF COL_LENGTH('dbo.partners', 'updated_at') IS NULL
  BEGIN
    ALTER TABLE dbo.partners ADD updated_at DATETIME2 NULL;
    -- 回填已有数据
    UPDATE dbo.partners SET updated_at = created_at WHERE updated_at IS NULL;
  END

  /* join_date: 合伙人加入日期 */
  IF COL_LENGTH('dbo.partners', 'join_date') IS NULL
    ALTER TABLE dbo.partners ADD join_date DATE NULL;

  /* expire_date: 合伙人到期日期 */
  IF COL_LENGTH('dbo.partners', 'expire_date') IS NULL
    ALTER TABLE dbo.partners ADD expire_date DATE NULL;

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
