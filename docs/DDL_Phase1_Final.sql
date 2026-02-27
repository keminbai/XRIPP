---

# 🧱 XRIPP CORE – Phase 1 最终冻结版 DDL

**适用数据库：SQL Server 2022**
**定位：定制项目 · 非产品化 · 稳态优先**

---

## 一、设计裁决（写在最前，给未来的人看）

```text
【核心裁决】
1. 合伙人权益池 ≠ 会员权益
   → 只落 partner_benefit_pool
   → member 权益 Phase 2 再引入

2. 组织是主体，用户是附属
   → sys_user.partner_id → partners.id
   → partners 不反绑 user_id

3. 审核是横切能力
   → audit_logs 统一承载
   → 业务表只存 audit_status

4. 本阶段不落：
   ❌ profit_sharing
   ❌ member_benefit_wallet
   ❌ 完整 orders / tenders
```

---

## 二、建库

```sql
CREATE DATABASE XRIPP_CORE;
GO
USE XRIPP_CORE;
GO
```

---

## 三、系统字典（**必须第一张表**）

```sql
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
GO
```

### 初始化审核状态

```sql
INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
('audit_status','0','草稿',0),
('audit_status','10','待一审',10),
('audit_status','20','待二审',20),
('audit_status','30','已通过',30),
('audit_status','40','已驳回',40),
('audit_status','50','已下架',50);
GO
```

---

## 四、系统用户（**数据隔离锚点**）

```sql
CREATE TABLE sys_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    username NVARCHAR(50) NOT NULL UNIQUE,
    password_hash NVARCHAR(128) NOT NULL,
    phone NVARCHAR(20),
    email NVARCHAR(100),

    role NVARCHAR(20) NOT NULL
        CHECK (role IN ('admin','partner','member')),

    -- 🔑 数据隔离锚点
    -- admin   → NULL
    -- partner → 自己的 partners.id
    -- member  → 所属 partners.id
    partner_id BIGINT NULL,

    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),

    INDEX IX_sys_user_role (role),
    INDEX IX_sys_user_partner (partner_id)
);
GO
```

---

## 五、合伙人主体（**业务组织**）

```sql
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
GO
```

---

## 六、会员企业档案（**无权益字段**）

```sql
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
GO
```

---

## 七、合伙人权益池（⚠️ 强一致核心）

```sql
CREATE TABLE partner_benefit_pool (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    partner_id BIGINT NOT NULL,

    benefit_type NVARCHAR(50) NOT NULL
        CHECK (benefit_type IN (
            'tender_download',
            'activity_free',
            'report_download',
            'supplier_quota'
        )),

    total_amount INT NOT NULL DEFAULT 0,
    used_amount  INT NOT NULL DEFAULT 0,

    balance AS (total_amount - used_amount) PERSISTED,

    version INT NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT GETDATE(),

    CONSTRAINT UK_partner_benefit UNIQUE (partner_id, benefit_type),
    CHECK (total_amount >= used_amount),

    FOREIGN KEY (partner_id) REFERENCES partners(id)
);
GO
```

> ⚠️ **硬性规则（写进后端规范）**
> 所有扣减必须：
>
> ```sql
> SELECT ... WITH (UPDLOCK, ROWLOCK)
> ```

---

## 八、权益发放流水（不可删）

```sql
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
GO
```

---

## 九、统一审核日志（横切）

```sql
CREATE TABLE audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    target_type NVARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,

    operator_id BIGINT NOT NULL,
    action NVARCHAR(20) NOT NULL
        CHECK (action IN ('submit','approve','reject','offline')),

    prev_status TINYINT,
    curr_status TINYINT,
    comment NVARCHAR(500),

    created_at DATETIME DEFAULT GETDATE(),

    INDEX IX_audit_target (target_type, target_id)
);
GO
```

---

## 十、业务表示例（Phase 1 必要）

### 1️⃣ 供应商

```sql
CREATE TABLE suppliers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    supplier_no NVARCHAR(30) NOT NULL UNIQUE,

    company_name NVARCHAR(200) NOT NULL,
    partner_id BIGINT NOT NULL,

    audit_status TINYINT DEFAULT 0
        CHECK (audit_status IN (0,10,20,30,40,50)),

    submit_time DATETIME,
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (partner_id) REFERENCES partners(id),
    INDEX IX_supplier_partner (partner_id)
);
GO
```

### 2️⃣ 活动（简化）

```sql
CREATE TABLE activities (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    title NVARCHAR(200) NOT NULL,
    start_time DATETIME,
    is_free BIT DEFAULT 1,
    fee DECIMAL(10,2) DEFAULT 0,

    partner_id BIGINT NULL,
    audit_status TINYINT DEFAULT 0
        CHECK (audit_status IN (0,10,20,30,40,50)),

    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (partner_id) REFERENCES partners(id)
);
GO
```

### 3️⃣ 采购需求

```sql
CREATE TABLE demands (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,

    title NVARCHAR(200) NOT NULL,
    user_id BIGINT NOT NULL,

    audit_status TINYINT DEFAULT 0
        CHECK (audit_status IN (0,10,20,30,40,50)),

    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
GO
```

---

# ✅ Phase 1 最终结果

```text
✔ 表数量：10 张
✔ 无角色膨胀
✔ 无权益冗余
✔ DataScope 可实现
✔ 半年内不需要改结构
```

---

## 🔒 明确延后（不要被任何 AI 诱导）

```
❌ member_benefit_wallet
❌ tenders
❌ profit_sharing
❌ 复杂 orders
```
