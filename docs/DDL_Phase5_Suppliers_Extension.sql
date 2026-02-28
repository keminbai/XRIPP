-- =====================================================
-- Phase 5: Suppliers Table Extension
-- Adds columns that Entity references but DDL never created
-- Strategy: idempotent (COL_LENGTH guard)
-- =====================================================

USE XRIPP_CORE;
GO

IF COL_LENGTH('dbo.suppliers','service_type') IS NULL
    ALTER TABLE dbo.suppliers ADD service_type NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.suppliers','contact_person') IS NULL
    ALTER TABLE dbo.suppliers ADD contact_person NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.suppliers','contact_phone') IS NULL
    ALTER TABLE dbo.suppliers ADD contact_phone NVARCHAR(20) NULL;
IF COL_LENGTH('dbo.suppliers','city_name') IS NULL
    ALTER TABLE dbo.suppliers ADD city_name NVARCHAR(50) NULL;
IF COL_LENGTH('dbo.suppliers','updated_at') IS NULL
    ALTER TABLE dbo.suppliers ADD updated_at DATETIME NULL;
GO

PRINT N'Phase 5: suppliers extension complete';
GO
