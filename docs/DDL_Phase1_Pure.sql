-- =====================================================
-- Phase 1: Core Tables (extracted from DDL_Phase1_Final.sql)
-- Idempotent: uses IF NOT EXISTS guards
-- =====================================================

-- 确保使用正确数据库
USE XRIPP_CORE;
GO

-- 1. sys_dict
IF OBJECT_ID('dbo.sys_dict', 'U') IS NULL
BEGIN
    CREATE TABLE sys_dict (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        dict_type NVARCHAR(50) NOT NULL,
        dict_key  NVARCHAR(50) NOT NULL,
        dict_value NVARCHAR(200) NOT NULL,
        sort_order INT DEFAULT 0,
        status BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT UK_sys_dict UNIQUE (dict_type, dict_key)
    );
    PRINT N'Created: sys_dict';
END
ELSE PRINT N'Exists: sys_dict';
GO

-- sys_dict seed data
IF NOT EXISTS (SELECT 1 FROM dbo.sys_dict WHERE dict_type = 'audit_status' AND dict_key = '0')
BEGIN
    INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
    ('audit_status','0',N'草稿',0),
    ('audit_status','10',N'待一审',10),
    ('audit_status','20',N'待二审',20),
    ('audit_status','30',N'已通过',30),
    ('audit_status','40',N'已驳回',40),
    ('audit_status','50',N'已下架',50);
    PRINT N'Seeded: sys_dict audit_status';
END
GO

-- 2. sys_user
IF OBJECT_ID('dbo.sys_user', 'U') IS NULL
BEGIN
    CREATE TABLE sys_user (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password_hash NVARCHAR(128) NOT NULL,
        phone NVARCHAR(20),
        email NVARCHAR(100),
        role NVARCHAR(20) NOT NULL
            CHECK (role IN ('admin','partner','member')),
        partner_id BIGINT NULL,
        status TINYINT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE(),
        INDEX IX_sys_user_role (role),
        INDEX IX_sys_user_partner (partner_id)
    );
    PRINT N'Created: sys_user';
END
ELSE PRINT N'Exists: sys_user';
GO

-- 3. partners
IF OBJECT_ID('dbo.partners', 'U') IS NULL
BEGIN
    CREATE TABLE partners (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        partner_name NVARCHAR(100) NOT NULL,
        partner_no NVARCHAR(20) NOT NULL UNIQUE,
        province NVARCHAR(20),
        city_code NVARCHAR(20),
        city_name NVARCHAR(50),
        contact_person NVARCHAR(50),
        contact_phone NVARCHAR(20),
        invitation_code NVARCHAR(20) NOT NULL UNIQUE,
        status BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE(),
        INDEX IX_partner_city (city_code)
    );
    PRINT N'Created: partners';
END
ELSE PRINT N'Exists: partners';
GO

-- 4. member_profile
IF OBJECT_ID('dbo.member_profile', 'U') IS NULL
BEGIN
    CREATE TABLE member_profile (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        company_name NVARCHAR(200) NOT NULL,
        industry NVARCHAR(50),
        contact_person NVARCHAR(50),
        contact_phone NVARCHAR(20),
        vip_level NVARCHAR(10) DEFAULT 'FREE',
        vip_expire_time DATETIME,
        invitation_code NVARCHAR(20),
        created_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
    );
    PRINT N'Created: member_profile';
END
ELSE PRINT N'Exists: member_profile';
GO

-- 5. partner_benefit_pool
IF OBJECT_ID('dbo.partner_benefit_pool', 'U') IS NULL
BEGIN
    CREATE TABLE partner_benefit_pool (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        partner_id BIGINT NOT NULL,
        benefit_type NVARCHAR(50) NOT NULL,
        total_amount INT NOT NULL DEFAULT 0,
        used_amount INT NOT NULL DEFAULT 0,
        version INT NOT NULL DEFAULT 0,
        updated_at DATETIME DEFAULT GETDATE(),
        CONSTRAINT UK_partner_benefit UNIQUE (partner_id, benefit_type),
        FOREIGN KEY (partner_id) REFERENCES partners(id)
    );
    PRINT N'Created: partner_benefit_pool';
END
ELSE PRINT N'Exists: partner_benefit_pool';
GO

-- 6. benefit_grant_logs
IF OBJECT_ID('dbo.benefit_grant_logs', 'U') IS NULL
BEGIN
    CREATE TABLE benefit_grant_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        partner_id BIGINT NOT NULL,
        member_id BIGINT NOT NULL,
        benefit_type NVARCHAR(50) NOT NULL,
        amount INT NOT NULL,
        operator_id BIGINT NOT NULL,
        remark NVARCHAR(200),
        created_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (partner_id) REFERENCES partners(id),
        FOREIGN KEY (member_id) REFERENCES sys_user(id),
        INDEX IX_benefit_partner_time (partner_id, created_at DESC)
    );
    PRINT N'Created: benefit_grant_logs';
END
ELSE PRINT N'Exists: benefit_grant_logs';
GO

-- 7. audit_logs
IF OBJECT_ID('dbo.audit_logs', 'U') IS NULL
BEGIN
    CREATE TABLE audit_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        target_type NVARCHAR(30) NOT NULL,
        target_id BIGINT NOT NULL,
        operator_id BIGINT NOT NULL,
        action NVARCHAR(20) NOT NULL,
        prev_status TINYINT,
        curr_status TINYINT,
        comment NVARCHAR(500),
        created_at DATETIME DEFAULT GETDATE(),
        INDEX IX_audit_target (target_type, target_id)
    );
    PRINT N'Created: audit_logs';
END
ELSE PRINT N'Exists: audit_logs';
GO

-- 8. suppliers
IF OBJECT_ID('dbo.suppliers', 'U') IS NULL
BEGIN
    CREATE TABLE suppliers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        supplier_no NVARCHAR(30),
        company_name NVARCHAR(200) NOT NULL,
        service_type NVARCHAR(50),
        contact_person NVARCHAR(50),
        contact_phone NVARCHAR(20),
        city_name NVARCHAR(50),
        partner_id BIGINT NULL,
        audit_status TINYINT DEFAULT 0,
        submit_time DATETIME,
        created_at DATETIME DEFAULT GETDATE(),
        INDEX IX_supplier_partner (partner_id)
    );
    PRINT N'Created: suppliers';
END
ELSE PRINT N'Exists: suppliers';
GO

-- 9. activities
IF OBJECT_ID('dbo.activities', 'U') IS NULL
BEGIN
    CREATE TABLE activities (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(200) NOT NULL,
        start_time DATETIME,
        is_free BIT DEFAULT 1,
        fee DECIMAL(10,2) DEFAULT 0,
        partner_id BIGINT NULL,
        audit_status TINYINT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE()
    );
    PRINT N'Created: activities';
END
ELSE PRINT N'Exists: activities';
GO

-- 10. demands
IF OBJECT_ID('dbo.demands', 'U') IS NULL
BEGIN
    CREATE TABLE demands (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(200) NOT NULL,
        user_id BIGINT NOT NULL,
        audit_status TINYINT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES sys_user(id)
    );
    PRINT N'Created: demands';
END
ELSE PRINT N'Exists: demands';
GO

PRINT N'========== Phase 1 complete ==========';
GO
