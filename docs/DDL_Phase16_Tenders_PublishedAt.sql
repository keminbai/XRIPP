/* =========================================================
   Phase 16: 标书 published_at 发布时间口径收敛
   目标：
     1) 为 tenders 增加正式的 published_at 列
     2) 回填历史已发布标书的发布时间
     3) 为按发布时间筛选/统计提供索引基础
   注意：分三个 batch，因为 SQL Server 同一事务内
         ALTER TABLE ADD 的新列对后续 DML/DDL 不可见
   ========================================================= */

-- Batch 1: 新增列
IF COL_LENGTH('dbo.tenders', 'published_at') IS NULL
  ALTER TABLE dbo.tenders ADD published_at DATETIME2 NULL;
PRINT 'Phase 16 Batch 1 — published_at column added';
GO

-- Batch 2: 回填（列已提交可见）
UPDATE dbo.tenders
   SET published_at = COALESCE(changed_at, created_at)
 WHERE tender_status = 'published'
   AND published_at IS NULL;
PRINT 'Phase 16 Batch 2 — backfill done';
GO

-- Batch 3: 索引（filtered index 需要 QUOTED_IDENTIFIER）
SET QUOTED_IDENTIFIER ON;
IF NOT EXISTS (
  SELECT 1
    FROM sys.indexes
   WHERE name = 'IX_tenders_status_published_at'
     AND object_id = OBJECT_ID('dbo.tenders')
)
  CREATE NONCLUSTERED INDEX IX_tenders_status_published_at
    ON dbo.tenders(tender_status, published_at DESC, id DESC);
PRINT 'Phase 16 Batch 3 — index created';
GO
