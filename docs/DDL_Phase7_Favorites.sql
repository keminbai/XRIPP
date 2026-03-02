-- =====================================================
-- Phase 7: User Favorites Table
-- Date: 2026-03-02
-- Strategy: idempotent (OBJECT_ID guard)
-- Purpose: Replace localStorage favorites with backend persistence
-- =====================================================

IF OBJECT_ID('dbo.user_favorites', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.user_favorites (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id     BIGINT NOT NULL,
        target_type NVARCHAR(30) NOT NULL,   -- 'tender', 'activity', 'supplier'
        target_id   BIGINT NOT NULL,
        created_at  DATETIME DEFAULT GETDATE(),

        CONSTRAINT UK_user_favorites UNIQUE (user_id, target_type, target_id)
    );

    CREATE INDEX IX_user_favorites_user ON dbo.user_favorites (user_id, created_at DESC);
    CREATE INDEX IX_user_favorites_target ON dbo.user_favorites (target_type, target_id);

    PRINT N'Phase 7: user_favorites table created';
END
ELSE
    PRINT N'Phase 7: user_favorites table already exists, skipped';
GO
