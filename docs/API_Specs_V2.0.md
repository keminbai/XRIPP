# 📘 API_Specs_V2.0

## XRIPP 后端开发执行规范（Phase 1 · 冻结版）

> 适用范围：XRIPP 平台（总部 Admin / 合伙人 Partner / 会员 Member）
> 技术栈：Spring Boot 3.x + MyBatis Plus + SQL Server 2022
> 状态：**冻结执行版（不得随意更改）**

---

## 一、总体架构原则（必须遵守）

### 1. 系统分层职责

| 层级         | 职责               |
| ---------- | ---------------- |
| Controller | 参数校验、权限校验、请求转发   |
| Service    | **唯一允许承载业务规则的层** |
| Mapper     | 只做数据映射，不承载业务判断   |
| DB         | 数据完整性、最终一致性兜底    |

🚫 **严禁**在 Controller / Mapper 中编写业务规则
🚫 **严禁**绕过 Service 直接操作 Mapper

---

### 2. 角色模型（最终裁决）

系统角色 **只允许以下三种**：

| role    | 说明          |
| ------- | ----------- |
| admin   | 总部管理员 / 审核员 |
| partner | 合伙人         |
| member  | 前台会员        |

❌ 不存在 auditor、reviewer 等扩展角色
✅ 审核能力通过 **权限** 控制，不通过角色拆分

---

## 二、数据隔离（DataScope）执行规范【核心红线】

### 1. 总体原则

> **数据隔离必须在 SQL 生成层完成，而不是依赖 Service 层自觉**

因此：

* **必须**使用 MyBatis Plus 的 `DataPermissionHandler`
* **禁止**开发人员在 Service 中手写 `where partner_id = ?`

---

### 2. DataScopeInterceptor 启用规则

#### ✅ 生效条件

当且仅当满足以下条件时，自动注入数据隔离条件：

1. 当前用户角色为 `partner`
2. SQL 操作为 `SELECT`
3. 查询表属于 **白名单业务表**

---

### 3. 白名单表（Phase 1 · 冻结）

> 仅以下表允许使用 DataScopeInterceptor

```text
suppliers         -- 供应商
member_profile    -- 会员档案
activities        -- 活动
audit_logs        -- 审核日志（仅查询）
```

⚠️ audit_logs 仅允许 SELECT 查询，禁止通过任何接口修改或删除

---

### 4. ❌ 禁止使用 DataScope 的表（强一致性资源）

以下表 **严禁** 通过 DataScopeInterceptor 自动注入条件：

```text
partner_benefit_pool
benefit_grant_logs
sys_user
sys_dict
partners
sys_user
sys_dict
partners
```

📌 原因说明：

* 涉及余额 / 权益 / 事务锁
* 必须由 Service 层显式控制（`SELECT … FOR UPDATE` 或乐观锁）
* 禁止隐式 SQL 改写，防止死锁与超扣

---

### 5. DataScopeInterceptor 实现规范（必须满足）

实现必须符合以下约束：

1. **保留原始 where 条件**

   ```java
   new AndExpression(originalWhere, partnerCondition)
   ```
2. **支持表别名**

   * 如 `FROM suppliers s` → `s.partner_id`
3. **必须校验当前表名是否在白名单**
4. **admin 角色不注入任何条件**
5. **member 角色不走 DataScope（由接口级控制）**

🚫 不满足以上任一条，视为实现不合格

---

## 三、权益与并发控制（高危区）

### 1. 权益来源唯一性

* 所有权益余额 **只允许从 `partner_benefit_pool.balance` 读取**
* 禁止在以下位置冗余存储权益数值：

  * `member_profile`
  * `partners`
  * 任何业务表

---

### 2. 权益变更规则（强制）

所有权益变更必须满足：

1. 在 `@Transactional` 中执行
2. 必须：

   * 更新 `benefit_pool.total_amount` 或 `used_amount`
   * **同步写入 `benefit_grant_logs`**
3. 禁止：

   * 手动更新 `balance`（计算列）
   * 脱离事务写流水

---

### 3. 并发控制要求

* 扣减 / 消耗权益时：

  * 使用 **乐观锁 version** 或 `SELECT … FOR UPDATE`
* 扣减前必须校验：

  * `balance > 0`
* 数据库 `CHECK` 约束仅作为**最后一道防线**

---

## 四、审核流程规范（统一模型）

### 1. 状态流转规则（单向）

```text
DRAFT → PENDING → APPROVED
               → REJECTED
```

🚫 禁止反向流转
🚫 禁止跳级修改状态

---

### 2. 审核操作强制要求

* 所有审核操作：

  * **不得直接 update 业务表状态**
  * 必须通过统一 `AuditService`
* 每一次审核：

  * 必须在 `audit_logs` 中写一条记录
  * 包含：操作者、前后状态、备注

---

## 五、状态与字典（统一约束）

### 1. 状态值来源

* 所有状态值 **必须来源于 `sys_dict`**
* 前端禁止硬编码：

  * `'pending'`
  * `'approved'`
  * `'active'` 等字符串

---

### 2. 后端规范

* 使用常量类 / 枚举类映射 `sys_dict`
* 禁止魔法数字散落在代码中

---

## 六、接口与返回规范

### 1. 返回结构统一

```json
{
  "code": 0,
  "msg": "success",
  "data": {}
}
```

---

### 2. 异常处理

* 所有业务异常：

  * 使用自定义 Exception
  * 由全局异常处理器统一转换
* 禁止：

  * 在 Controller 中 try-catch 吞异常

---

## 七、开发红线（必须遵守）

🚫 禁止以下行为：

* 直接在 Controller / Mapper 写业务逻辑
* 绕过 Service 操作核心表
* 使用 DataScope 处理强一致性资源
* 在前端 / 后端重复维护权益数据
* 未写日志直接修改审核状态

---

## 八、执行声明（冻结条款）

> 自本文件生效起：

1. 数据库结构以 **Phase 1 已落库 DDL（即当前生产数据库）** 为唯一标准
2. 前端仅做接口对接，不得反推业务逻辑
3. 所有后端代码 Review 必须以本文件为检查基准

**本文件即为 Phase 1 的“技术宪法”。**

#### 附录 A：DataScopeInterceptor 合格实现标准（冻结条款）

##### A.1 必须满足的 5 个约束（缺一不可）

1️⃣ **必须保留原始 WHERE 条件**

```java
Expression result = originalWhere != null
    ? new AndExpression(originalWhere, partnerCondition)
    : partnerCondition;
```

🚫 严禁覆盖原条件
🚫 严禁 return 单一 EqualsTo

---

2️⃣ **必须支持表别名**

```java
String columnName = tableAlias == null || tableAlias.isEmpty()
    ? "partner_id"
    : tableAlias + ".partner_id";
```

---

3️⃣ **必须校验表名白名单**

```java
if (!TABLE_WHITELIST.contains(tableName.toLowerCase())) {
    return originalWhere;
}
```

---

4️⃣ **admin 角色必须完全跳过**

```java
if ("admin".equals(currentUser.getRole())) {
    return originalWhere;
}
```

---

5️⃣ **必须记录调试日志（仅 debug）**

```java
log.debug(
    "DataScope applied: table={}, partnerId={}",
    tableName, partnerId
);
```

---

##### A.2 明确禁止的不合格实现（示例）

```java
// ❌ 丢失原 where
return new EqualsTo(new Column("partner_id"), new LongValue(partnerId));

// ❌ Service 层手写 SQL
"SELECT * FROM suppliers WHERE partner_id = " + partnerId;

// ❌ 对 partner_benefit_pool 使用 DataScope
```

> **违反本附录，视为严重架构违规，不得合并代码。**

## 数据库冻结声明（Phase 1）

以下表结构已在 SQL Server 2022 实例中完成初始化，
并作为 Phase 1 唯一合法数据库事实来源：

- sys_user
- partners
- member_profile
- partner_benefit_pool
- benefit_grant_logs
- audit_logs
- suppliers
- activities
- demands
- sys_dict

任何代码、文档、口头方案，如与上述表结构不一致，
一律以**当前生产数据库中的实际表结构**为准。