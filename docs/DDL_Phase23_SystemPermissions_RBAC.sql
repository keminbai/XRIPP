/* =========================================================
   DDL Phase 23 — 系统权限中心最小 RBAC 底座
   目标：
   1) 为后台系统权限中心建立独立数据模型，不能继续停留在 session-only 页面
   2) 在不破坏现有 admin/partner/member 三层角色基线的前提下，增加后台子权限档案
   3) 为“城市合伙人 / 发布管理员 / 审核管理员”登录模式提供真实持久化来源
   4) 为后续菜单显隐、接口鉴权、双级审核子角色演进提供统一底座
   幂等：所有 CREATE / ALTER / INSERT 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) 权限档案主表
     ========================================================= */
  IF OBJECT_ID('dbo.sys_permission_profiles', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.sys_permission_profiles (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      code NVARCHAR(50) NOT NULL,
      name NVARCHAR(100) NOT NULL,
      base_role NVARCHAR(20) NOT NULL
        CHECK (base_role IN ('admin', 'partner')),
      status BIT NOT NULL CONSTRAINT DF_sys_permission_profiles_status DEFAULT 1,
      is_system BIT NOT NULL CONSTRAINT DF_sys_permission_profiles_is_system DEFAULT 0,
      is_super_admin BIT NOT NULL CONSTRAINT DF_sys_permission_profiles_is_super_admin DEFAULT 0,
      description NVARCHAR(500) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_sys_permission_profiles_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_sys_permission_profiles_updated_at DEFAULT SYSUTCDATETIME()
    );

    CREATE UNIQUE NONCLUSTERED INDEX UX_sys_permission_profiles_code
      ON dbo.sys_permission_profiles(code);

    CREATE NONCLUSTERED INDEX IX_sys_permission_profiles_base_role_status
      ON dbo.sys_permission_profiles(base_role, status, id);
  END

  /* =========================================================
     2) 权限授权明细
     ========================================================= */
  IF OBJECT_ID('dbo.sys_permission_grants', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.sys_permission_grants (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      profile_id BIGINT NOT NULL,
      module_code NVARCHAR(80) NOT NULL,
      module_name NVARCHAR(100) NOT NULL,
      can_view BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_view DEFAULT 0,
      can_create BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_create DEFAULT 0,
      can_edit BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_edit DEFAULT 0,
      can_delete BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_delete DEFAULT 0,
      can_review BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_review DEFAULT 0,
      can_export BIT NOT NULL CONSTRAINT DF_sys_permission_grants_can_export DEFAULT 0,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_sys_permission_grants_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_sys_permission_grants_updated_at DEFAULT SYSUTCDATETIME()
    );

    ALTER TABLE dbo.sys_permission_grants
      ADD CONSTRAINT FK_sys_permission_grants_profile
      FOREIGN KEY (profile_id) REFERENCES dbo.sys_permission_profiles(id);

    CREATE UNIQUE NONCLUSTERED INDEX UX_sys_permission_grants_profile_module
      ON dbo.sys_permission_grants(profile_id, module_code);

    CREATE NONCLUSTERED INDEX IX_sys_permission_grants_module
      ON dbo.sys_permission_grants(module_code, profile_id);
  END

  /* =========================================================
     3) 登录模式
     ========================================================= */
  IF OBJECT_ID('dbo.sys_login_modes', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.sys_login_modes (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      mode_code NVARCHAR(50) NOT NULL,
      name NVARCHAR(100) NOT NULL,
      base_role NVARCHAR(20) NOT NULL
        CHECK (base_role IN ('admin', 'partner')),
      enabled BIT NOT NULL CONSTRAINT DF_sys_login_modes_enabled DEFAULT 1,
      default_profile_id BIGINT NULL,
      sort_order INT NOT NULL CONSTRAINT DF_sys_login_modes_sort_order DEFAULT 0,
      description NVARCHAR(500) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_sys_login_modes_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_sys_login_modes_updated_at DEFAULT SYSUTCDATETIME()
    );

    ALTER TABLE dbo.sys_login_modes
      ADD CONSTRAINT FK_sys_login_modes_default_profile
      FOREIGN KEY (default_profile_id) REFERENCES dbo.sys_permission_profiles(id);

    CREATE UNIQUE NONCLUSTERED INDEX UX_sys_login_modes_mode_code
      ON dbo.sys_login_modes(mode_code);

    CREATE NONCLUSTERED INDEX IX_sys_login_modes_sort
      ON dbo.sys_login_modes(sort_order ASC, id ASC);
  END

  /* =========================================================
     4) sys_user 绑定权限档案
     ========================================================= */
  IF COL_LENGTH('dbo.sys_user', 'permission_profile_id') IS NULL
  BEGIN
    ALTER TABLE dbo.sys_user
      ADD permission_profile_id BIGINT NULL;
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_sys_user_permission_profile'
      AND parent_object_id = OBJECT_ID('dbo.sys_user')
  )
  BEGIN
    ALTER TABLE dbo.sys_user
      ADD CONSTRAINT FK_sys_user_permission_profile
      FOREIGN KEY (permission_profile_id) REFERENCES dbo.sys_permission_profiles(id);
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_sys_user_permission_profile_id'
      AND object_id = OBJECT_ID('dbo.sys_user')
  )
  BEGIN
    CREATE NONCLUSTERED INDEX IX_sys_user_permission_profile_id
      ON dbo.sys_user(permission_profile_id);
  END

  /* =========================================================
     5) 默认权限档案种子
     ========================================================= */
  IF NOT EXISTS (SELECT 1 FROM dbo.sys_permission_profiles WHERE code = 'SUPER_ADMIN')
  BEGIN
    INSERT INTO dbo.sys_permission_profiles (
      code, name, base_role, status, is_system, is_super_admin, description
    ) VALUES (
      'SUPER_ADMIN', N'总部超级管理员', 'admin', 1, 1, 1, N'保留现有 admin 全量权限基线；未绑定具体档案的历史 admin 账号仍按全量权限兼容。'
    );
  END

  IF NOT EXISTS (SELECT 1 FROM dbo.sys_permission_profiles WHERE code = 'PARTNER_STANDARD')
  BEGIN
    INSERT INTO dbo.sys_permission_profiles (
      code, name, base_role, status, is_system, is_super_admin, description
    ) VALUES (
      'PARTNER_STANDARD', N'城市合伙人标准档案', 'partner', 1, 1, 0, N'用于城市合伙人登录模式的默认权限档案；当前主要承接合伙人发布与本人数据查看边界。'
    );
  END

  IF NOT EXISTS (SELECT 1 FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_PUBLISHER')
  BEGIN
    INSERT INTO dbo.sys_permission_profiles (
      code, name, base_role, status, is_system, is_super_admin, description
    ) VALUES (
      'CONTENT_PUBLISHER', N'发布管理员', 'admin', 1, 1, 0, N'面向内容发布、编辑、提交流程，不默认拥有系统设置与权限管理能力。'
    );
  END

  IF NOT EXISTS (SELECT 1 FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_AUDITOR')
  BEGIN
    INSERT INTO dbo.sys_permission_profiles (
      code, name, base_role, status, is_system, is_super_admin, description
    ) VALUES (
      'CONTENT_AUDITOR', N'审核管理员', 'admin', 1, 1, 0, N'面向业务审核中心与复查流程，不默认拥有系统配置修改能力。'
    );
  END

  DECLARE @partnerProfileId BIGINT = (
    SELECT TOP 1 id FROM dbo.sys_permission_profiles WHERE code = 'PARTNER_STANDARD'
  );
  DECLARE @publisherProfileId BIGINT = (
    SELECT TOP 1 id FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_PUBLISHER'
  );
  DECLARE @auditorProfileId BIGINT = (
    SELECT TOP 1 id FROM dbo.sys_permission_profiles WHERE code = 'CONTENT_AUDITOR'
  );

  /* =========================================================
     6) 默认授权明细种子
     ========================================================= */
  IF @partnerProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @partnerProfileId AND module_code = 'partner_publish'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @partnerProfileId, 'partner_publish', N'合伙人发布', 1, 1, 1, 0, 0, 0
    );
  END

  IF @partnerProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @partnerProfileId AND module_code = 'member_management'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @partnerProfileId, 'member_management', N'会员管理', 1, 0, 0, 0, 0, 1
    );
  END

  IF @partnerProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @partnerProfileId AND module_code = 'supplier_directory'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @partnerProfileId, 'supplier_directory', N'服务商名录', 1, 0, 0, 0, 0, 1
    );
  END

  IF @publisherProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @publisherProfileId AND module_code = 'content_management'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @publisherProfileId, 'content_management', N'信息发布管理', 1, 1, 1, 0, 0, 1
    );
  END

  IF @publisherProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @publisherProfileId AND module_code = 'activity_display'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @publisherProfileId, 'activity_display', N'活动显示管理', 1, 1, 1, 0, 0, 0
    );
  END

  IF @auditorProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @auditorProfileId AND module_code = 'audit_center'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @auditorProfileId, 'audit_center', N'业务审核中心', 1, 0, 0, 0, 1, 1
    );
  END

  IF @auditorProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @auditorProfileId AND module_code = 'supplier_onboarding_review'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @auditorProfileId, 'supplier_onboarding_review', N'服务商入驻审核', 1, 0, 0, 0, 1, 1
    );
  END

  IF @auditorProfileId IS NOT NULL
     AND NOT EXISTS (
       SELECT 1 FROM dbo.sys_permission_grants
       WHERE profile_id = @auditorProfileId AND module_code = 'content_management'
     )
  BEGIN
    INSERT INTO dbo.sys_permission_grants (
      profile_id, module_code, module_name, can_view, can_create, can_edit, can_delete, can_review, can_export
    ) VALUES (
      @auditorProfileId, 'content_management', N'信息发布管理', 1, 0, 0, 0, 1, 1
    );
  END

  /* =========================================================
     7) 默认登录模式种子
     ========================================================= */
  IF NOT EXISTS (SELECT 1 FROM dbo.sys_login_modes WHERE mode_code = 'partner_mode')
  BEGIN
    INSERT INTO dbo.sys_login_modes (
      mode_code, name, base_role, enabled, default_profile_id, sort_order, description
    ) VALUES (
      'partner_mode', N'城市合伙人', 'partner', 1, @partnerProfileId, 10, N'面向城市合伙人使用，默认绑定合伙人标准档案。'
    );
  END

  IF NOT EXISTS (SELECT 1 FROM dbo.sys_login_modes WHERE mode_code = 'publisher_mode')
  BEGIN
    INSERT INTO dbo.sys_login_modes (
      mode_code, name, base_role, enabled, default_profile_id, sort_order, description
    ) VALUES (
      'publisher_mode', N'发布管理员', 'admin', 1, @publisherProfileId, 20, N'面向发布运营人员使用，默认绑定发布管理员权限档案。'
    );
  END

  IF NOT EXISTS (SELECT 1 FROM dbo.sys_login_modes WHERE mode_code = 'auditor_mode')
  BEGIN
    INSERT INTO dbo.sys_login_modes (
      mode_code, name, base_role, enabled, default_profile_id, sort_order, description
    ) VALUES (
      'auditor_mode', N'审核管理员', 'admin', 1, @auditorProfileId, 30, N'面向审核与复查人员使用，默认绑定审核管理员权限档案。'
    );
  END

  COMMIT TRAN;
  PRINT 'Phase 23 — system permissions RBAC foundation complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
