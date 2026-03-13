/* =========================================================
   DDL Phase 11 — supplier_onboarding 支付闭环增强
   目标：
   1) 将“资料草稿 / 待支付 / 已支付待审核”明确分层
   2) 新增 payment_orders 承载二维码、回调、第三方流水号
   3) 为附件/复查链路预留 submitted_snapshot_json
   幂等：所有 ALTER / CREATE 都带存在性判断
   ========================================================= */

SET XACT_ABORT ON;
BEGIN TRY
  BEGIN TRAN;

  /* =========================================================
     1) supplier_onboarding 支付字段扩展
     ========================================================= */
  IF COL_LENGTH('dbo.supplier_onboarding', 'apply_type') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD apply_type NVARCHAR(20) NULL;

  IF COL_LENGTH('dbo.supplier_onboarding', 'fee_amount') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD fee_amount DECIMAL(12,2) NOT NULL CONSTRAINT DF_supplier_onboarding_fee_amount DEFAULT 0;

  IF COL_LENGTH('dbo.supplier_onboarding', 'payment_required') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD payment_required BIT NOT NULL CONSTRAINT DF_supplier_onboarding_payment_required DEFAULT 0;

  IF COL_LENGTH('dbo.supplier_onboarding', 'payment_status') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD payment_status NVARCHAR(20) NOT NULL
      CONSTRAINT DF_supplier_onboarding_payment_status DEFAULT 'unpaid'
      CONSTRAINT CK_supplier_onboarding_payment_status CHECK (payment_status IN ('unpaid','pending','paid','waived','refunded','closed'));

  IF COL_LENGTH('dbo.supplier_onboarding', 'payment_order_id') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD payment_order_id BIGINT NULL;

  IF COL_LENGTH('dbo.supplier_onboarding', 'payment_verified_at') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD payment_verified_at DATETIME2 NULL;

  IF COL_LENGTH('dbo.supplier_onboarding', 'submitted_snapshot_json') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD submitted_snapshot_json NVARCHAR(MAX) NULL;

  IF COL_LENGTH('dbo.supplier_onboarding', 'last_payment_check_at') IS NULL
    ALTER TABLE dbo.supplier_onboarding ADD last_payment_check_at DATETIME2 NULL;

  IF NOT EXISTS (
      SELECT 1 FROM sys.indexes
      WHERE name = 'IX_supplier_onboarding_payment_status'
        AND object_id = OBJECT_ID('dbo.supplier_onboarding')
  )
    CREATE INDEX IX_supplier_onboarding_payment_status
      ON dbo.supplier_onboarding(payment_status, onboarding_status, updated_at DESC);

  /* =========================================================
     2) payment_orders 支付执行单
     ========================================================= */
  IF OBJECT_ID('dbo.payment_orders', 'U') IS NULL
  BEGIN
    CREATE TABLE dbo.payment_orders (
      id BIGINT IDENTITY(1,1) PRIMARY KEY,
      order_no NVARCHAR(50) NOT NULL UNIQUE,
      biz_type NVARCHAR(50) NOT NULL,
      biz_id BIGINT NOT NULL,
      user_id BIGINT NOT NULL,
      pay_channel NVARCHAR(30) NOT NULL,
      amount DECIMAL(12,2) NOT NULL DEFAULT 0,
      currency_code NVARCHAR(10) NOT NULL DEFAULT 'CNY',

      pay_status NVARCHAR(20) NOT NULL DEFAULT 'created'
        CHECK (pay_status IN ('created','pending','paid','closed','failed','refunded')),

      provider_trade_no NVARCHAR(100) NULL,
      payment_url NVARCHAR(1000) NULL,
      qr_code_base64 NVARCHAR(MAX) NULL,
      callback_payload_json NVARCHAR(MAX) NULL,

      paid_at DATETIME2 NULL,
      expired_at DATETIME2 NULL,
      created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
      updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_payment_orders_biz ON dbo.payment_orders(biz_type, biz_id, created_at DESC);
    CREATE INDEX IX_payment_orders_user_status ON dbo.payment_orders(user_id, pay_status, updated_at DESC);
    CREATE INDEX IX_payment_orders_provider_trade_no ON dbo.payment_orders(provider_trade_no);
  END

  COMMIT TRAN;
  PRINT 'Phase 11 — supplier_onboarding payment enhancement complete';
END TRY
BEGIN CATCH
  IF @@TRANCOUNT > 0 ROLLBACK TRAN;
  THROW;
END CATCH;
GO
