-- =====================================================
-- Phase 6: Demands Table Field Extension
-- Date: 2026-03-02
-- Strategy: idempotent (COL_LENGTH guard)
-- Purpose: Fix frontend-backend field gap
--   Frontend submits 11 fields but demands table only had 5.
--   8 business fields were silently lost.
-- =====================================================

-- org_type: 'enterprise' | 'ngo'
IF COL_LENGTH('dbo.demands','org_type') IS NULL
    ALTER TABLE dbo.demands ADD org_type NVARCHAR(20) NULL;
GO

-- org_name: organization/company name
IF COL_LENGTH('dbo.demands','org_name') IS NULL
    ALTER TABLE dbo.demands ADD org_name NVARCHAR(200) NULL;
GO

-- publish_date: user-specified publish date
IF COL_LENGTH('dbo.demands','publish_date') IS NULL
    ALTER TABLE dbo.demands ADD publish_date DATE NULL;
GO

-- deadline: submission deadline
IF COL_LENGTH('dbo.demands','deadline') IS NULL
    ALTER TABLE dbo.demands ADD deadline DATE NULL;
GO

-- category: procurement method (itb|rfq|rfp)
IF COL_LENGTH('dbo.demands','category') IS NULL
    ALTER TABLE dbo.demands ADD category NVARCHAR(50) NULL;
GO

-- industry: sector classification
IF COL_LENGTH('dbo.demands','industry') IS NULL
    ALTER TABLE dbo.demands ADD industry NVARCHAR(50) NULL;
GO

-- summary: requirement description (max 1000 chars)
IF COL_LENGTH('dbo.demands','summary') IS NULL
    ALTER TABLE dbo.demands ADD summary NVARCHAR(1000) NULL;
GO

-- enable_sms: whether to push matching suppliers via SMS
IF COL_LENGTH('dbo.demands','enable_sms') IS NULL
    ALTER TABLE dbo.demands ADD enable_sms BIT DEFAULT 0;
GO

PRINT N'Phase 6: demands extension complete';
GO
