/* =========================================================
   Phase 19: 合伙人利润分成配置与月度结算
   目标：
     1) 为 admin/finance/profit.vue 提供真实持久化配置表
     2) 支撑按合伙人、业务线、月份计算分成统计与明细
     3) 记录月度结算状态，避免前端本地假结算
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  IF OBJECT_ID('dbo.partner_profit_configs', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.partner_profit_configs (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      partner_id BIGINT NOT NULL,
      percentage DECIMAL(5,2) NOT NULL,
      business_lines_json NVARCHAR(MAX) NULL,
      enabled BIT NOT NULL CONSTRAINT DF_partner_profit_configs_enabled DEFAULT 1,
      changed_by BIGINT NULL,
      changed_at DATETIME2 NULL,
      change_reason NVARCHAR(500) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_partner_profit_configs_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_partner_profit_configs_updated_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF OBJECT_ID('dbo.partner_profit_settlements', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.partner_profit_settlements (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      partner_id BIGINT NOT NULL,
      settlement_month DATE NOT NULL,
      revenue_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_partner_profit_settlements_revenue DEFAULT 0,
      profit_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_partner_profit_settlements_profit DEFAULT 0,
      order_count INT NOT NULL CONSTRAINT DF_partner_profit_settlements_order_count DEFAULT 0,
      settlement_status NVARCHAR(20) NOT NULL CONSTRAINT DF_partner_profit_settlements_status DEFAULT 'settled',
      settled_at DATETIME2 NULL,
      settled_by BIGINT NULL,
      note NVARCHAR(500) NULL,
      created_at DATETIME2 NOT NULL CONSTRAINT DF_partner_profit_settlements_created_at DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL CONSTRAINT DF_partner_profit_settlements_updated_at DEFAULT SYSUTCDATETIME()
    );
  END

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_partner_profit_configs_partner'
      AND object_id = OBJECT_ID('dbo.partner_profit_configs')
  )
    CREATE UNIQUE NONCLUSTERED INDEX UX_partner_profit_configs_partner
      ON dbo.partner_profit_configs(partner_id);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_partner_profit_configs_enabled_updated'
      AND object_id = OBJECT_ID('dbo.partner_profit_configs')
  )
    CREATE NONCLUSTERED INDEX IX_partner_profit_configs_enabled_updated
      ON dbo.partner_profit_configs(enabled, updated_at DESC, id DESC);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_partner_profit_settlements_partner_month'
      AND object_id = OBJECT_ID('dbo.partner_profit_settlements')
  )
    CREATE UNIQUE NONCLUSTERED INDEX UX_partner_profit_settlements_partner_month
      ON dbo.partner_profit_settlements(partner_id, settlement_month);

  IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_partner_profit_settlements_status_month'
      AND object_id = OBJECT_ID('dbo.partner_profit_settlements')
  )
    CREATE NONCLUSTERED INDEX IX_partner_profit_settlements_status_month
      ON dbo.partner_profit_settlements(settlement_status, settlement_month DESC, partner_id);

  COMMIT TRAN;
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
