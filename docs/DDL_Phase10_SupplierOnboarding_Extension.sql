/* ============================================================
   DDL Phase 10 — supplier_onboarding 会员自助入驻扩展
   目标：支持会员（非合伙人）直接申请成为服务商
   幂等：所有操作含 IF NOT EXISTS 判断
   ============================================================ */

-- 1) user_id：会员自助申请时标识提交人
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'supplier_onboarding' AND COLUMN_NAME = 'user_id'
)
BEGIN
    ALTER TABLE dbo.supplier_onboarding ADD user_id BIGINT NULL;
    CREATE INDEX IX_supplier_onboarding_user ON dbo.supplier_onboarding(user_id, created_at DESC);
END
GO

-- 2) detail_json：存储丰富的企业信息（中英文名、信用代码、ISO认证、联系人等）
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'supplier_onboarding' AND COLUMN_NAME = 'detail_json'
)
BEGIN
    ALTER TABLE dbo.supplier_onboarding ADD detail_json NVARCHAR(MAX) NULL;
END
GO

PRINT 'Phase 10 — supplier_onboarding extension complete';
