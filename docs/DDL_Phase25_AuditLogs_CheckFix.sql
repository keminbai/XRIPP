-- =============================================================
-- DDL Phase 25: audit_logs CHECK 约束扩展
-- 日期: 2026-03-14
-- 背景: AdminContentsV3Controller 的 transition 接口会将
--       to_status (如 published/draft) 写入 audit_logs.action。
--       原约束 CK__audit_log__actio__5CD6CB2B 仅允许
--       submit/approve/reject/offline，导致内容发布操作报 500。
-- =============================================================

-- 先安全删除旧约束（幂等：不存在也不报错）
IF EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.audit_logs')
      AND name LIKE 'CK%audit_log%actio%'
)
BEGIN
    DECLARE @constraintName NVARCHAR(256);
    SELECT TOP 1 @constraintName = name
    FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.audit_logs')
      AND name LIKE 'CK%audit_log%actio%';
    EXEC('ALTER TABLE dbo.audit_logs DROP CONSTRAINT [' + @constraintName + ']');
END
GO

-- 重建约束，覆盖所有实际写入的 action 值
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.audit_logs')
      AND name = 'CK_audit_logs_action'
)
BEGIN
    ALTER TABLE dbo.audit_logs
    ADD CONSTRAINT CK_audit_logs_action
    CHECK ([action] IN (
        'submit', 'approve', 'reject', 'offline',
        'published', 'created', 'draft', 'update', 'delete'
    ));
END
GO
