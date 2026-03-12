-- ============================================================
-- Phase 8: Contents table extension for carousel support
-- Adds cover_image column + expands content_type CHECK constraint
-- Execute in SSMS after Phase 7
-- ============================================================

-- 1. Add cover_image column
IF COL_LENGTH('dbo.contents', 'cover_image') IS NULL
BEGIN
    ALTER TABLE dbo.contents ADD cover_image NVARCHAR(500) NULL;
    PRINT 'Added cover_image column to contents';
END
GO

-- 2. Drop existing CHECK constraint on content_type and recreate with 'carousel'
DECLARE @ckName NVARCHAR(200);
SELECT @ckName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c ON cc.parent_object_id = c.object_id AND cc.parent_column_id = c.column_id
WHERE cc.parent_object_id = OBJECT_ID('dbo.contents')
  AND c.name = 'content_type';

IF @ckName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE dbo.contents DROP CONSTRAINT [' + @ckName + ']');
    PRINT 'Dropped old content_type CHECK constraint: ' + @ckName;
END
GO

ALTER TABLE dbo.contents ADD CONSTRAINT CK_contents_content_type
    CHECK (content_type IN ('activity','media','ad','training','carousel'));
PRINT 'Added new content_type CHECK constraint with carousel';
GO

-- 3. Seed: insert one default carousel banner
IF NOT EXISTS (SELECT 1 FROM dbo.contents WHERE content_type = 'carousel')
BEGIN
    INSERT INTO dbo.contents (title, content_type, summary, cover_image, publish_status, created_at, updated_at)
    VALUES (
        N'XRIPP 全球公共采购',
        'carousel',
        N'助力中国企业走向世界',
        'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072',
        'published',
        SYSUTCDATETIME(),
        SYSUTCDATETIME()
    );
    PRINT 'Inserted seed carousel banner';
END
GO
