-- =====================================================
-- Phase 4: Scheduled Tasks DDL Migration
-- Date: 2026-02-28
-- Strategy: idempotent (COL_LENGTH guard)
-- =====================================================

-- Add renewal_reminder_sent to partners
-- Tracks whether a 30-day expiry reminder has been sent
IF COL_LENGTH('dbo.partners','renewal_reminder_sent') IS NULL
    ALTER TABLE dbo.partners ADD renewal_reminder_sent BIT DEFAULT 0;
GO

PRINT N'Phase 4: scheduled tasks migration complete';
GO
