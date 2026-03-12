-- ============================================================
-- DDL Phase 9: Overseas Service Points
-- Date: 2026-03-12
-- Idempotent: safe to re-run
-- ============================================================

IF OBJECT_ID('dbo.overseas_points', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.overseas_points (
        id              BIGINT IDENTITY(1,1) PRIMARY KEY,
        partner_id      NVARCHAR(50)    NULL,
        name            NVARCHAR(100)   NOT NULL,
        continent       NVARCHAR(50)    NOT NULL,
        country         NVARCHAR(50)    NOT NULL,
        city            NVARCHAR(50)    NOT NULL,
        lat             DECIMAL(10,4)   NOT NULL,
        lng             DECIMAL(10,4)   NOT NULL,
        manager         NVARCHAR(50)    NULL,
        phone           NVARCHAR(50)    NULL,
        email           NVARCHAR(100)   NULL,
        address         NVARCHAR(200)   NULL,
        services_json   NVARCHAR(MAX)   NULL,
        service_type    NVARCHAR(50)    NULL,
        description     NVARCHAR(1000)  NULL,
        cover_image     NVARCHAR(500)   NULL,
        rating          DECIMAL(2,1)    DEFAULT 5.0,
        response_time   NVARCHAR(20)    DEFAULT N'2小时',
        success_cases   INT             DEFAULT 0,
        status          NVARCHAR(20)    DEFAULT 'active',
        established_at  DATE            NULL,
        created_at      DATETIME2       DEFAULT SYSUTCDATETIME(),
        updated_at      DATETIME2       DEFAULT SYSUTCDATETIME()
    );
    PRINT 'Created table overseas_points';
END
ELSE
    PRINT 'Table overseas_points already exists — skipped';
GO

-- ============================================================
-- Seed data: migrated from app/pages/overseas.vue servicePoints
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM dbo.overseas_points)
BEGIN
    SET IDENTITY_INSERT dbo.overseas_points ON;

    INSERT INTO dbo.overseas_points (id, name, continent, country, city, lat, lng, manager, services_json, service_type, rating, response_time, success_cases, status) VALUES
    -- 总部
    (1,  N'上海总部运营中心',     N'亚洲',   N'中国',       N'上海',         31.2300, 121.4700, N'总部运营中心',     N'["标书翻译","服务商注册","法律咨询","资源对接"]',        N'跨境企服', 5.0, N'即时',   999, 'active'),
    -- 亚洲
    (2,  N'新加坡服务中心',       N'亚洲',   N'新加坡',     N'新加坡',       1.3500,  103.8200, N'Tan先生',          N'["金融牌照","离岸架构","税务筹划"]',                      N'跨境企服', 4.9, N'2小时',  156, 'active'),
    (3,  N'金边服务中心',         N'亚洲',   N'柬埔寨',     N'金边',         11.5600, 104.9300, N'Sok经理',          N'["特区入驻","劳工政策"]',                                N'外贸服务', 4.6, N'4小时',  89,  'active'),
    (4,  N'宿务服务中心',         N'亚洲',   N'菲律宾',     N'宿务',         10.3100, 123.8900, N'Santos先生',       N'["BPO外包","物流服务"]',                                 N'外贸服务', 4.5, N'6小时',  72,  'active'),
    (5,  N'马尼拉服务中心',       N'亚洲',   N'菲律宾',     N'马尼拉',       14.6000, 120.9800, N'Cruz女士',         N'["制造业咨询","港口对接"]',                               N'出海投资', 4.7, N'3小时',  95,  'active'),
    (6,  N'曼谷服务中心',         N'亚洲',   N'泰国',       N'曼谷',         13.7500, 100.5000, N'Sompong先生',      N'["BOI申请","税务筹划"]',                                 N'跨境企服', 4.8, N'2小时',  142, 'active'),
    (7,  N'东京服务中心',         N'亚洲',   N'日本',       N'东京',         35.6800, 139.6500, N'佐藤先生',         N'["技术认证","知识产权"]',                                 N'知识产权', 4.9, N'1小时',  203, 'active'),
    (8,  N'胡志明市服务中心',     N'亚洲',   N'越南',       N'胡志明市',     10.8200, 106.6300, N'阮经理',           N'["建厂选址","劳工招聘"]',                                 N'出海投资', 4.7, N'3小时',  118, 'active'),
    (9,  N'河内服务中心',         N'亚洲',   N'越南',       N'河内',         21.0300, 105.8500, N'黎经理',           N'["政府对接","园区入驻"]',                                 N'出海投资', 4.6, N'4小时',  97,  'active'),
    (10, N'阿布扎比服务中心',     N'亚洲',   N'阿联酋',     N'阿布扎比',     24.4700, 54.3700,  N'Ahmed先生',        N'["能源项目","自贸区注册"]',                               N'出海投资', 4.8, N'2小时',  134, 'active'),
    (11, N'迪拜服务中心',         N'亚洲',   N'阿联酋',     N'迪拜',         25.2000, 55.2700,  N'Khalid先生',       N'["中东市场拓展","展会对接"]',                             N'外贸服务', 4.9, N'1小时',  178, 'active'),
    (12, N'吉隆坡服务中心',       N'亚洲',   N'马来西亚',   N'吉隆坡',       3.1400,  101.6900, N'李经理',           N'["清关服务","本地化支持"]',                               N'外贸服务', 4.7, N'3小时',  126, 'active'),
    (13, N'香港服务中心',         N'亚洲',   N'中国',       N'香港',         22.3200, 114.1700, N'陈经理',           N'["离岸公司","跨境金融"]',                                 N'跨境企服', 4.9, N'1小时',  245, 'active'),
    (14, N'德黑兰服务中心',       N'亚洲',   N'伊朗',       N'德黑兰',       35.6900, 51.4200,  N'Reza先生',         N'["能源合作","政策咨询"]',                                 N'出海投资', 4.3, N'8小时',  54,  'active'),
    (15, N'雅加达服务中心',       N'亚洲',   N'印度尼西亚', N'雅加达',       -6.2100, 106.8500, N'Budi先生',         N'["采矿许可","环评服务"]',                                 N'出海投资', 4.5, N'5小时',  81,  'active'),
    -- 欧洲
    (16, N'柏林服务中心',         N'欧洲',   N'德国',       N'柏林',         52.5200, 13.4100,  N'Schmidt先生',      N'["工业4.0认证","欧盟准入"]',                              N'跨境企服', 4.8, N'2小时',  167, 'active'),
    (17, N'莫斯科服务中心',       N'欧洲',   N'俄罗斯',     N'莫斯科',       55.7600, 37.6200,  N'Ivanov先生',       N'["关税咨询","物流通道"]',                                 N'外贸服务', 4.4, N'6小时',  73,  'active'),
    (18, N'巴黎服务中心',         N'欧洲',   N'法国',       N'巴黎',         48.8600, 2.3500,   N'Dubois女士',       N'["品牌注册","展会对接"]',                                 N'知识产权', 4.7, N'3小时',  139, 'active'),
    (19, N'爱丁堡服务中心',       N'欧洲',   N'英国',       N'爱丁堡',       55.9500, -3.1900,  N'Campbell先生',     N'["金融服务","法律合规"]',                                 N'跨境企服', 4.6, N'4小时',  102, 'active'),
    (20, N'曼彻斯特服务中心',     N'欧洲',   N'英国',       N'曼彻斯特',     53.4800, -2.2400,  N'Brown女士',        N'["制造业对接","供应链优化"]',                             N'出海投资', 4.7, N'3小时',  115, 'active'),
    (21, N'伦敦服务中心',         N'欧洲',   N'英国',       N'伦敦',         51.5100, -0.1300,  N'Wilson先生',       N'["欧洲法律合规","投资并购"]',                             N'跨境企服', 4.9, N'1小时',  198, 'active'),
    (22, N'米兰服务中心',         N'欧洲',   N'意大利',     N'米兰',         45.4600, 9.1900,   N'Rossi先生',        N'["时尚品牌","设计产业"]',                                 N'知识产权', 4.6, N'4小时',  91,  'active'),
    (23, N'佛罗伦萨服务中心',     N'欧洲',   N'意大利',     N'佛罗伦萨',     43.7700, 11.2600,  N'Bianchi女士',      N'["文化创意","艺术品交易"]',                               N'其他服务', 4.5, N'5小时',  68,  'active'),
    (24, N'阿姆斯特丹服务中心',   N'欧洲',   N'荷兰',       N'阿姆斯特丹',   52.3700, 4.9000,   N'Van Berg先生',     N'["物流枢纽","税务筹划"]',                                 N'外贸服务', 4.8, N'2小时',  152, 'active'),
    (25, N'里斯本服务中心',       N'欧洲',   N'葡萄牙',     N'里斯本',       38.7200, -9.1400,  N'Silva女士',        N'["旅游业投资","房地产"]',                                 N'出海投资', 4.4, N'6小时',  76,  'active'),
    -- 北美洲
    (26, N'洛杉矶服务中心',       N'北美洲', N'美国',       N'洛杉矶',       34.0500, -118.2400,N'Johnson先生',      N'["北美市场准入","港口物流"]',                             N'外贸服务', 4.8, N'2小时',  164, 'active'),
    (27, N'波士顿服务中心',       N'北美洲', N'美国',       N'波士顿',       42.3600, -71.0600, N'Williams女士',     N'["教育科技","生物医药"]',                                 N'出海投资', 4.7, N'3小时',  128, 'active'),
    (28, N'纽约服务中心',         N'北美洲', N'美国',       N'纽约',         40.7100, -74.0100, N'Davis先生',        N'["金融服务","联合国对接"]',                               N'跨境企服', 4.9, N'1小时',  215, 'active'),
    (29, N'华盛顿服务中心',       N'北美洲', N'美国',       N'华盛顿',       38.9100, -77.0400, N'Miller女士',       N'["政府采购","政策游说"]',                                 N'报告下载', 4.6, N'4小时',  103, 'active'),
    (30, N'哈瓦那服务中心',       N'北美洲', N'古巴',       N'哈瓦那',       23.1100, -82.3700, N'Rodriguez先生',    N'["拉美市场","文化交流"]',                                 N'其他服务', 4.2, N'8小时',  47,  'active'),
    (31, N'多伦多服务中心',       N'北美洲', N'加拿大',     N'多伦多',       43.6500, -79.3800, N'Thompson先生',     N'["移民咨询","资源开发"]',                                 N'跨境企服', 4.7, N'3小时',  137, 'active'),
    (32, N'墨西哥城服务中心',     N'北美洲', N'墨西哥',     N'墨西哥城',     19.4300, -99.1300, N'Garcia先生',       N'["制造业外包","矿产贸易"]',                               N'出海投资', 4.5, N'5小时',  84,  'active'),
    -- 非洲
    (33, N'约翰内斯堡服务中心',   N'非洲',   N'南非',       N'约翰内斯堡',   -26.2000, 28.0500, N'Van Der Merwe先生',N'["矿业投资","基建项目"]',                                 N'出海投资', 4.5, N'5小时',  79,  'active'),
    (34, N'马普托服务中心',       N'非洲',   N'莫桑比克',   N'马普托',       -25.9700, 32.5700, N'Macamo女士',       N'["港口开发","能源合作"]',                                 N'出海投资', 4.3, N'7小时',  56,  'active'),
    (35, N'卡萨布兰卡服务中心',   N'非洲',   N'摩洛哥',     N'卡萨布兰卡',   33.5700, -7.5900,  N'El Fassi先生',     N'["非洲门户","法语区对接"]',                               N'外贸服务', 4.4, N'6小时',  68,  'active'),
    (36, N'达累斯萨拉姆服务中心', N'非洲',   N'坦桑尼亚',   N'达累斯萨拉姆', -6.7900,  39.2700, N'Mwamba先生',       N'["农业项目","交通基建"]',                                 N'出海投资', 4.2, N'8小时',  51,  'active'),
    -- 大洋洲
    (37, N'悉尼服务中心',         N'大洋洲', N'澳大利亚',   N'悉尼',         -33.8700, 151.2100,N'Anderson女士',     N'["大洋洲项目支持","矿产贸易"]',                           N'出海投资', 4.7, N'3小时',  129, 'active'),
    (38, N'墨尔本服务中心',       N'大洋洲', N'澳大利亚',   N'墨尔本',       -37.8100, 144.9600,N'Taylor先生',       N'["教育产业","农业出口"]',                                 N'外贸服务', 4.6, N'4小时',  96,  'active'),
    (39, N'奥克兰服务中心',       N'大洋洲', N'新西兰',     N'奥克兰',       -36.8500, 174.7600,N'Smith女士',        N'["乳制品","旅游业"]',                                     N'外贸服务', 4.5, N'5小时',  73,  'active');

    SET IDENTITY_INSERT dbo.overseas_points OFF;
    PRINT 'Inserted 39 seed overseas points';
END
ELSE
    PRINT 'overseas_points already has data — seed skipped';
GO
