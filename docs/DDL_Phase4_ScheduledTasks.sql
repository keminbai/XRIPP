-- =====================================================
-- Phase 4: Scheduled Tasks DDL Migration
-- Date: 2026-02-28
-- =====================================================

-- Add renewal_reminder_sent to partners
-- Tracks whether a 30-day expiry reminder has been sent
ALTER TABLE partners ADD renewal_reminder_sent BIT DEFAULT 0;
GO
