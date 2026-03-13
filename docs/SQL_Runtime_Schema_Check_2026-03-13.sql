SET NOCOUNT ON;

DECLARE @checks TABLE (
  script_name NVARCHAR(200) NOT NULL,
  item_name NVARCHAR(200) NOT NULL,
  is_ok BIT NOT NULL
);

INSERT INTO @checks (script_name, item_name, is_ok)
SELECT 'docs/DDL_Phase2_Migration.sql', 'dbo.member_profile.member_level',
       CASE WHEN COL_LENGTH('dbo.member_profile', 'member_level') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.activity_no',
       CASE WHEN COL_LENGTH('dbo.activities', 'activity_no') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.front_module',
       CASE WHEN COL_LENGTH('dbo.activities', 'front_module') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.front_position',
       CASE WHEN COL_LENGTH('dbo.activities', 'front_position') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.cities_json',
       CASE WHEN COL_LENGTH('dbo.activities', 'cities_json') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.video_url',
       CASE WHEN COL_LENGTH('dbo.activities', 'video_url') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.agenda',
       CASE WHEN COL_LENGTH('dbo.activities', 'agenda') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.max_limit',
       CASE WHEN COL_LENGTH('dbo.activities', 'max_limit') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.current_participants',
       CASE WHEN COL_LENGTH('dbo.activities', 'current_participants') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.fee_item_id',
       CASE WHEN COL_LENGTH('dbo.activities', 'fee_item_id') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.fee_item_name',
       CASE WHEN COL_LENGTH('dbo.activities', 'fee_item_name') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.fee_type',
       CASE WHEN COL_LENGTH('dbo.activities', 'fee_type') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.member_price',
       CASE WHEN COL_LENGTH('dbo.activities', 'member_price') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.changed_by',
       CASE WHEN COL_LENGTH('dbo.activities', 'changed_by') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.changed_at',
       CASE WHEN COL_LENGTH('dbo.activities', 'changed_at') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase14_Activities_Closure.sql', 'dbo.activities.change_reason',
       CASE WHEN COL_LENGTH('dbo.activities', 'change_reason') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase15_ActivityDisplayApplications.sql', 'dbo.activity_display_applications',
       CASE WHEN OBJECT_ID('dbo.activity_display_applications', 'U') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase17_ActivityRecords.sql', 'dbo.activity_records',
       CASE WHEN OBJECT_ID('dbo.activity_records', 'U') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase17_ActivityRecords.sql', 'dbo.activity_record_photos',
       CASE WHEN OBJECT_ID('dbo.activity_record_photos', 'U') IS NULL THEN 0 ELSE 1 END
UNION ALL
SELECT 'docs/DDL_Phase18_Contents_ExtraJson.sql', 'dbo.contents.extra_json',
       CASE WHEN COL_LENGTH('dbo.contents', 'extra_json') IS NULL THEN 0 ELSE 1 END;

SELECT
  script_name AS script,
  item_name AS item,
  CASE WHEN is_ok = 1 THEN 'OK' ELSE 'MISSING' END AS status
FROM @checks
ORDER BY script_name, item_name;

SELECT
  COUNT(*) AS total_checks,
  SUM(CASE WHEN is_ok = 1 THEN 1 ELSE 0 END) AS passed_checks,
  SUM(CASE WHEN is_ok = 0 THEN 1 ELSE 0 END) AS missing_checks
FROM @checks;

IF EXISTS (SELECT 1 FROM @checks WHERE is_ok = 0)
BEGIN
  RAISERROR('Runtime schema baseline is incomplete. Run the missing docs/DDL_Phase*.sql scripts before UAT.', 16, 1);
END
