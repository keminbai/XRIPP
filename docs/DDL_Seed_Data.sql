-- =====================================================
-- XRIPP 种子数据（测试/演示环境）
-- 执行前提：DDL_Phase1_Final.sql + DDL_Phase2_Migration.sql
--           + DDL_Phase3_Partners_Extension.sql
--           + DDL_Phase4_ScheduledTasks.sql 均已执行
-- 执行顺序：本脚本最后执行
-- 策略：幂等（先 DELETE 再 INSERT，SET IDENTITY_INSERT ON）
-- =====================================================

SET NOCOUNT ON;
GO

-- =====================================================
-- 1. 合伙人组织（partners）
-- =====================================================
SET IDENTITY_INSERT dbo.partners ON;
IF NOT EXISTS (SELECT 1 FROM dbo.partners WHERE id = 1)
BEGIN
    INSERT INTO dbo.partners (id, partner_name, partner_no, province, city_code, city_name,
        contact_person, contact_phone, invitation_code, status, created_at, join_date, expire_date)
    VALUES (1, N'北京瑞普国际贸易有限公司', 'PTN-0000001', N'北京', '110000', N'北京市',
        N'张伟', '13800138001', 'XRIPP2026A', 1, GETDATE(),
        CAST('2026-01-01' AS DATE), CAST('2027-01-01' AS DATE));
END
IF NOT EXISTS (SELECT 1 FROM dbo.partners WHERE id = 2)
BEGIN
    INSERT INTO dbo.partners (id, partner_name, partner_no, province, city_code, city_name,
        contact_person, contact_phone, invitation_code, status, created_at, join_date, expire_date)
    VALUES (2, N'上海中采供应链管理有限公司', 'PTN-0000002', N'上海', '310000', N'上海市',
        N'李娜', '13900139002', 'XRIPP2026B', 1, GETDATE(),
        CAST('2026-02-01' AS DATE), CAST('2027-02-01' AS DATE));
END
SET IDENTITY_INSERT dbo.partners OFF;
GO

-- =====================================================
-- 2. 系统用户（sys_user）
--    密码说明：
--    admin  → 密码 admin123
--    p1001  → 密码 p1001
--    p2001  → 密码 p1001（与 p1001 同密码）
--    m1001  → 密码 m1001
--    m2001  → 密码 m1001（与 m1001 同密码）
-- =====================================================
SET IDENTITY_INSERT dbo.sys_user ON;
IF NOT EXISTS (SELECT 1 FROM dbo.sys_user WHERE username = 'admin')
BEGIN
    INSERT INTO dbo.sys_user (id, username, password_hash, phone, role, partner_id, status)
    VALUES (1, 'admin',
        '$2a$10$OZYX1DTgF6a9i1kcc8nppeBXsD773.lHSs/b.QyuoWnl1pZa3Y7tq',
        '13000000000', 'admin', NULL, 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.sys_user WHERE username = 'p1001')
BEGIN
    INSERT INTO dbo.sys_user (id, username, password_hash, phone, role, partner_id, status)
    VALUES (2, 'p1001',
        '$2a$10$6C7V7CHoZ70Sl3G1s.Npo.PJqWIvK1fdchSXJJA3Joofue2SBaHVO',
        '13800138001', 'partner', 1, 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.sys_user WHERE username = 'p2001')
BEGIN
    INSERT INTO dbo.sys_user (id, username, password_hash, phone, role, partner_id, status)
    VALUES (3, 'p2001',
        '$2a$10$6C7V7CHoZ70Sl3G1s.Npo.PJqWIvK1fdchSXJJA3Joofue2SBaHVO',
        '13900139002', 'partner', 2, 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.sys_user WHERE username = 'm1001')
BEGIN
    INSERT INTO dbo.sys_user (id, username, password_hash, phone, role, partner_id, status)
    VALUES (4, 'm1001',
        '$2a$10$TcCk8w3ANGbV6u9T4jXMLubu5ZVaZSuRnumap6XqRuvpl2v0eCsVi',
        '13700137001', 'member', 1, 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.sys_user WHERE username = 'm2001')
BEGIN
    INSERT INTO dbo.sys_user (id, username, password_hash, phone, role, partner_id, status)
    VALUES (5, 'm2001',
        '$2a$10$TcCk8w3ANGbV6u9T4jXMLubu5ZVaZSuRnumap6XqRuvpl2v0eCsVi',
        '13600136001', 'member', 2, 1);
END
SET IDENTITY_INSERT dbo.sys_user OFF;
GO

-- =====================================================
-- 3. 会员画像（member_profile）
-- =====================================================
SET IDENTITY_INSERT dbo.member_profile ON;
IF NOT EXISTS (SELECT 1 FROM dbo.member_profile WHERE user_id = 4)
BEGIN
    INSERT INTO dbo.member_profile (id, user_id, company_name, industry,
        contact_person, contact_phone, vip_level, vip_expire_time,
        invitation_code, member_level)
    VALUES (1, 4, N'深圳创新科技有限公司', N'电子信息',
        N'王强', '13700137001', 'VIP',
        DATEADD(YEAR, 1, GETDATE()),
        'INV-M1001', 'vip');
END
IF NOT EXISTS (SELECT 1 FROM dbo.member_profile WHERE user_id = 5)
BEGIN
    INSERT INTO dbo.member_profile (id, user_id, company_name, industry,
        contact_person, contact_phone, vip_level, vip_expire_time,
        invitation_code, member_level)
    VALUES (2, 5, N'广州永盛贸易有限公司', N'国际贸易',
        N'陈静', '13600136001', 'FREE',
        NULL,
        'INV-M2001', 'normal');
END
SET IDENTITY_INSERT dbo.member_profile OFF;
GO

-- =====================================================
-- 4. 合伙人权益池（partner_benefit_pool）
-- =====================================================
SET IDENTITY_INSERT dbo.partner_benefit_pool ON;
IF NOT EXISTS (SELECT 1 FROM dbo.partner_benefit_pool WHERE partner_id = 1 AND benefit_type = N'标书下载')
BEGIN
    INSERT INTO dbo.partner_benefit_pool (id, partner_id, benefit_type, total_quota, used_quota)
    VALUES (1, 1, N'标书下载', 100, 5);
END
IF NOT EXISTS (SELECT 1 FROM dbo.partner_benefit_pool WHERE partner_id = 1 AND benefit_type = N'培训名额')
BEGIN
    INSERT INTO dbo.partner_benefit_pool (id, partner_id, benefit_type, total_quota, used_quota)
    VALUES (2, 1, N'培训名额', 50, 2);
END
IF NOT EXISTS (SELECT 1 FROM dbo.partner_benefit_pool WHERE partner_id = 2 AND benefit_type = N'标书下载')
BEGIN
    INSERT INTO dbo.partner_benefit_pool (id, partner_id, benefit_type, total_quota, used_quota)
    VALUES (3, 2, N'标书下载', 80, 0);
END
SET IDENTITY_INSERT dbo.partner_benefit_pool OFF;
GO

-- =====================================================
-- 5. 服务商（suppliers）
-- =====================================================
SET IDENTITY_INSERT dbo.suppliers ON;
IF NOT EXISTS (SELECT 1 FROM dbo.suppliers WHERE id = 1)
BEGIN
    INSERT INTO dbo.suppliers (id, company_name, service_type, contact_person,
        contact_phone, city_name, audit_status, created_at)
    VALUES (1, N'环球法律事务所', N'法律咨询', N'刘明',
        '13500135001', N'北京市', 30, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.suppliers WHERE id = 2)
BEGIN
    INSERT INTO dbo.suppliers (id, company_name, service_type, contact_person,
        contact_phone, city_name, audit_status, created_at)
    VALUES (2, N'中信物流国际有限公司', N'物流服务', N'赵磊',
        '13400134001', N'上海市', 30, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.suppliers WHERE id = 3)
BEGIN
    INSERT INTO dbo.suppliers (id, company_name, service_type, contact_person,
        contact_phone, city_name, audit_status, created_at)
    VALUES (3, N'华夏翻译有限公司', N'翻译服务', N'孙芳',
        '13300133001', N'广州市', 0, GETDATE());
END
SET IDENTITY_INSERT dbo.suppliers OFF;
GO

-- =====================================================
-- 6. 活动（activities）
-- =====================================================
SET IDENTITY_INSERT dbo.activities ON;
IF NOT EXISTS (SELECT 1 FROM dbo.activities WHERE id = 1)
BEGIN
    INSERT INTO dbo.activities (id, title, partner_id, is_free, fee,
        start_time, audit_status, created_at)
    VALUES (1, N'2026年中小企业国际采购对接会', 1, 1, 0,
        DATEADD(DAY, 30, GETDATE()), 30, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.activities WHERE id = 2)
BEGIN
    INSERT INTO dbo.activities (id, title, partner_id, is_free, fee,
        start_time, audit_status, created_at)
    VALUES (2, N'联合国采购供应商认证培训', 1, 0, 299.00,
        DATEADD(DAY, 45, GETDATE()), 30, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.activities WHERE id = 3)
BEGIN
    INSERT INTO dbo.activities (id, title, partner_id, is_free, fee,
        start_time, audit_status, created_at)
    VALUES (3, N'跨境电商政策解读沙龙', 2, 1, 0,
        DATEADD(DAY, 15, GETDATE()), 0, GETDATE());
END
SET IDENTITY_INSERT dbo.activities OFF;
GO

-- =====================================================
-- 7. 采购需求（demands）
-- =====================================================
SET IDENTITY_INSERT dbo.demands ON;
IF NOT EXISTS (SELECT 1 FROM dbo.demands WHERE id = 1)
BEGIN
    INSERT INTO dbo.demands (id, title, user_id, audit_status, created_at)
    VALUES (1, N'联合国儿童基金会办公设备采购需求', 4, 30, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.demands WHERE id = 2)
BEGIN
    INSERT INTO dbo.demands (id, title, user_id, audit_status, created_at)
    VALUES (2, N'世界卫生组织医疗物资采购需求', 4, 0, GETDATE());
END
IF NOT EXISTS (SELECT 1 FROM dbo.demands WHERE id = 3)
BEGIN
    INSERT INTO dbo.demands (id, title, user_id, audit_status, created_at)
    VALUES (3, N'亚洲开发银行IT服务外包需求', 5, 0, GETDATE());
END
SET IDENTITY_INSERT dbo.demands OFF;
GO

-- =====================================================
-- 8. 标书（tenders）
-- =====================================================
SET IDENTITY_INSERT dbo.tenders ON;
IF NOT EXISTS (SELECT 1 FROM dbo.tenders WHERE id = 1)
BEGIN
    INSERT INTO dbo.tenders (id, title, org_name, tender_status,
        deadline, created_at, category, summary, price, sales)
    VALUES (1, N'UNICEF 办公家具采购招标', N'联合国儿童基金会', 'published',
        DATEADD(DAY, 60, GETDATE()), GETDATE(),
        N'办公设备', N'采购办公桌椅、文件柜等办公家具', 0, 0);
END
IF NOT EXISTS (SELECT 1 FROM dbo.tenders WHERE id = 2)
BEGIN
    INSERT INTO dbo.tenders (id, title, org_name, tender_status,
        deadline, created_at, category, summary, price, sales)
    VALUES (2, N'WHO 医疗设备维护服务招标', N'世界卫生组织', 'published',
        DATEADD(DAY, 45, GETDATE()), GETDATE(),
        N'医疗健康', N'为WHO亚太区域办事处提供医疗设备维护服务', 99.00, 3);
END
IF NOT EXISTS (SELECT 1 FROM dbo.tenders WHERE id = 3)
BEGIN
    INSERT INTO dbo.tenders (id, title, org_name, tender_status,
        deadline, created_at, category, summary, price, sales)
    VALUES (3, N'ADB 信息系统开发招标', N'亚洲开发银行', 'draft',
        DATEADD(DAY, 90, GETDATE()), GETDATE(),
        N'信息技术', N'开发亚洲开发银行内部项目管理信息系统', 199.00, 0);
END
SET IDENTITY_INSERT dbo.tenders OFF;
GO

-- =====================================================
-- 9. 内容（contents）
-- =====================================================
SET IDENTITY_INSERT dbo.contents ON;
IF NOT EXISTS (SELECT 1 FROM dbo.contents WHERE id = 1)
BEGIN
    INSERT INTO dbo.contents (id, title, content_type, summary, body,
        publish_status, is_paid, fee, created_at, updated_at, created_by)
    VALUES (1, N'国际公共采购入门指南', 'training',
        N'帮助中小企业了解国际公共采购的基本流程和要求',
        N'<p>国际公共采购是指联合国、世界银行等国际组织的采购活动...</p>',
        'published', 0, 0, GETDATE(), GETDATE(), 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.contents WHERE id = 2)
BEGIN
    INSERT INTO dbo.contents (id, title, content_type, summary, body,
        publish_status, is_paid, fee, created_at, updated_at, created_by)
    VALUES (2, N'联合国供应商注册实操视频', 'media',
        N'手把手教你完成联合国供应商注册流程',
        N'<p>视频内容：UNGM注册步骤详解...</p>',
        'published', 1, 49.00, GETDATE(), GETDATE(), 1);
END
IF NOT EXISTS (SELECT 1 FROM dbo.contents WHERE id = 3)
BEGIN
    INSERT INTO dbo.contents (id, title, content_type, summary, body,
        publish_status, is_paid, fee, created_at, updated_at, created_by)
    VALUES (3, N'2026年采购政策变化解读', 'training',
        N'解读最新国际公共采购政策调整',
        N'<p>2026年主要变化包括...</p>',
        'pending_review', 0, 0, GETDATE(), GETDATE(), 1);
END
SET IDENTITY_INSERT dbo.contents OFF;
GO

-- =====================================================
-- 验证查询
-- =====================================================
SELECT 'sys_user' AS [table], COUNT(*) AS [rows] FROM dbo.sys_user
UNION ALL SELECT 'partners', COUNT(*) FROM dbo.partners
UNION ALL SELECT 'member_profile', COUNT(*) FROM dbo.member_profile
UNION ALL SELECT 'partner_benefit_pool', COUNT(*) FROM dbo.partner_benefit_pool
UNION ALL SELECT 'suppliers', COUNT(*) FROM dbo.suppliers
UNION ALL SELECT 'activities', COUNT(*) FROM dbo.activities
UNION ALL SELECT 'demands', COUNT(*) FROM dbo.demands
UNION ALL SELECT 'tenders', COUNT(*) FROM dbo.tenders
UNION ALL SELECT 'contents', COUNT(*) FROM dbo.contents;
GO

PRINT N'✅ 种子数据插入完成';
PRINT N'测试账号：';
PRINT N'  admin  / admin123  → 管理员';
PRINT N'  p1001  / p1001     → 合伙人（北京瑞普）';
PRINT N'  p2001  / p1001     → 合伙人（上海中采）';
PRINT N'  m1001  / m1001     → 会员VIP（深圳创新科技）';
PRINT N'  m2001  / m1001     → 会员普通（广州永盛贸易）';
GO
