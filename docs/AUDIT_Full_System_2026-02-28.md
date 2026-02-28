# IPP 平台全面系统审计报告 (2026-02-28)

## 一、审计范围

| 维度 | 数量 | 完成度 |
|------|------|--------|
| 需求文档 | 5 份 (V5.0 + 4 份清单/导图) | 153 功能项 (前台 66 + 后台 87) |
| 架构文档 | 9 份 (API 合约、状态机、数据字典等) | 100% 阅读 |
| DDL 脚本 | 2 份 (Phase1 10 表 + Phase2 9 新表) | 100% 逐列比对 |
| 后端控制器 | 13 个 (41 个端点) | 100% 逐方法审查 |
| 后端实体 | 11 个 Java 类 | 100% 与 DDL 比对 |
| 前端页面 | 76 个 .vue 文件 | 100% 扫描 |

---

## 二、系统级问题（非表面 bug，是架构缺失）

### 问题 S1：7 张核心表无 Entity 类（阻塞整个业务中台）

以下 Phase 1 表在 DDL 中定义，但后端**完全没有对应的 Entity/Mapper/Service**：

| 表名 | 业务意义 | 被阻塞的功能 |
|------|---------|-------------|
| `partners` | 合伙人主表 | 合伙人管理、入驻、续费、数据隔离 |
| `member_profile` | 会员画像 | 等级升降、VIP 到期、配额管理、自动降级 |
| `partner_benefit_pool` | 合伙人权益池 | 标书下载配额分配、活动免费名额 |
| `benefit_grant_logs` | 权益分发记录 | 合伙人→会员权益分发审计 |
| `audit_logs` | 统一审核日志 | 全域审核操作追溯 |
| `sys_dict` | 数据字典 | 枚举集中管理 |
| `state_transition_logs` | 状态变迁日志 | ★ StateTransitionService 已直接写 SQL，但无 Entity |

**影响**：没有 Entity 层就无法通过 MyBatis Plus 读写这些表。所有与合伙人管理、会员等级、权益分发相关的需求（约占 V5.0 需求的 40%）被**彻底阻塞**。

### 问题 S2：order_type 枚举全线不一致（运行时崩溃风险）

| 来源 | 值 |
|------|---|
| DDL CHECK 约束 | `'activity','service','tender','report','other'`（小写） |
| 数据字典 | `MEMBER_ORDER, SERVICE_ORDER, TENDER_ORDER, ACTIVITY_ORDER`（大写） |
| TendersV3Controller | 写入 `biz_type='tender_download'`（不在 CHECK 中）|
| OrdersV3Controller | 映射 `MEMBER_ORDER → 'member'`（不在 CHECK 中）|

**影响**：INSERT 语句违反 CHECK 约束 → SQL Server 报错 → 订单创建/标书下载全线失败。

### 问题 S3：活动审核仅单级（需求要求双级）

| 需求 (V5.0) | 实际实现 |
|-------------|---------|
| 一审通过 → 二审通过 → 前台可见 | audit_status: 0 → 30 (直接通过) |
| audit_status 应经过 10(PENDING_L1) → 20(PENDING_L2) → 30(APPROVED) | PartnerPublishesV3Controller: 0 → 30 跳过 10/20 |

**影响**：审核流程与需求不符，无法支撑双角色审核。

### 问题 S4：4 个 Entity 缺少审计三字段

`OrderEntity`、`ActivityRegistration`、`MemberVerification`、`Demand` 均缺少：
- `changed_by` (操作人)
- `changed_at` (操作时间)
- `change_reason` (变更原因)

DDL 中这些列已定义。缺失意味着状态变更时无法记录操作人信息。

### 问题 S5：安全漏洞

| 严重度 | 问题 | 位置 |
|--------|------|------|
| **致命** | SupplierOnboardingV3Controller 用 userId 替代 partnerId | Lines 26, 67 |
| **致命** | AdminTendersV3Controller PUT 绕过状态机 | Lines 144-214 |
| **高** | AuthV3Controller 仍支持明文密码 | Lines 48-50 |
| **高** | AdminMemberVerificationV3Controller action 参数无空校验 | Line 69 |
| **中** | 无登录限流 | 全局 |

---

## 三、前端集成现状

| 分类 | 页面数 | 占比 |
|------|--------|------|
| ✅ 已接入 API | 6 | 7.9% |
| ⚠️ 部分接入 | 14 | 18.4% |
| ❌ 纯 Mock | 20 | 26.3% |
| 📋 静态/配置 | 36 | 47.4% |

**关键发现**：
- `useMockData.ts` composable 仍提供 30 条 mock 标书 + 活动数据
- `useServiceProviders.ts` 仍提供 10 条 mock 供应商
- 20 个页面使用 `setTimeout` 模拟异步

---

## 四、需求覆盖率分析

### 核心业务流（V5.0 定义 → 当前状态）

| 业务流 | 需求 | 后端 | 前端 | 差距 |
|--------|------|------|------|------|
| 会员注册/登录 | ✅ 已定义 | ✅ JWT 登录 | ✅ login.vue | - |
| 会员等级升降 | ✅ FREE/VIP/SVIP + 配额 | ❌ 无 MemberProfile Entity | ❌ Mock | **全缺** |
| VIP 自动降级 | ✅ 到期降 FREE + 清零 | ❌ 无定时任务 | ❌ | **全缺** |
| 合伙人权益分发 | ✅ pool→member 原子事务 | ❌ 无 Entity/API | ❌ | **全缺** |
| 合伙人续费 | ✅ 30 天提醒 | ❌ 无定时任务 | ❌ | **全缺** |
| 标书 CRUD | ✅ 发布/管理/搜索 | ✅ Admin + Public | ⚠️ 部分 | PUT 绕过状态机 |
| 标书下载扣额 | ✅ 反重复扣除 | ✅ 去重 OK | ⚠️ | 无配额扣减逻辑 |
| 活动发布 | ✅ 双级审核 | ⚠️ 单级审核 | ⚠️ | 审核层级不足 |
| 活动报名 | ✅ 免费/付费 | ✅ 注册端点 | ⚠️ | 无配额检查 |
| 供应商入库 | ✅ 两阶段审核 | ✅ 完整 | ✅ | scope bug |
| 内容发布 | ✅ 创建→审核→上线 | ✅ 完整 | ⚠️ | 前端部分接入 |
| 订单管理 | ✅ 双轨 (会员+服务) | ✅ CRUD + 状态机 | ✅ member/orders | enum 不一致 |
| 海外服务 | ✅ 地图+咨询+报告 | ❌ 无 | ❌ Mock | **全缺** |
| SMS 精准推送 | ✅ 5 关键词 | ❌ 无 | ❌ Mock | **全缺** |
| 微信支付 | ✅ 扫码支付 | ❌ 无 | ❌ | **全缺** |
| 子账号 | ✅ SVIP 5个 | ❌ 无 | ❌ Mock | **全缺** |
| 数据大屏 | ✅ 全屏 1920×1080 | ❌ 无 | ❌ Mock | **全缺** |

**估算覆盖率**：
- 后端 API：~35%（41 端点 / 估计需 120+ 端点）
- 前端接入：~25%（20 页面有真实调用 / 76 总页面）

---

## 五、全局执行计划

### 原则
1. **自底向上**：先补 Entity 层 → 再建 Service/API → 最后接前端
2. **不做表面修补**：修 order_type 枚举是改 DDL CHECK，不是在代码里 try-catch
3. **按业务价值排序**：合伙人权益系统 > 海外服务 > 数据大屏

### Phase A：基础设施修复（预计 2-3 天）

#### A1. 创建 7 个缺失 Entity + Mapper + Service
```
partners → Partner.java + PartnerMapper + IPartnerService
member_profile → MemberProfile.java + ...
partner_benefit_pool → PartnerBenefitPool.java + ...
benefit_grant_logs → BenefitGrantLog.java + ...
audit_logs → AuditLog.java + ...
sys_dict → SysDict.java + ...
state_transition_logs → StateTransitionLog.java + ...
```

#### A2. 补全 4 个 Entity 的审计字段
```
OrderEntity         += changedBy, changedAt, changeReason
ActivityRegistration += changedBy, changedAt, changeReason
MemberVerification  += documentsJson, changedBy, changedAt
Suppliers           += onboardingStatus
```

#### A3. 修复 order_type 枚举一致性
- 方案：统一为小写 (`activity`, `service`, `tender`, `report`, `other`)
- 修改：OrdersV3Controller.mapCategory()、TendersV3Controller.download() 中的写入值
- 或：修改 DDL CHECK 约束扩展为同时接受大小写（兼容性更好）

#### A4. 修复 4 个安全漏洞
- SupplierOnboardingV3Controller: userId → partnerId
- AdminTendersV3Controller: 删除 PUT 端点或加入状态机校验
- AuthV3Controller: 移除明文密码兼容
- AdminMemberVerificationV3Controller: 加 action 空值校验

### Phase B：核心业务 API 构建（预计 5-7 天）

#### B1. 合伙人管理模块
- `GET/POST /v3/admin/partners` 列表 + 创建
- `GET/PUT /v3/admin/partners/{id}` 详情 + 编辑
- `POST /v3/admin/partners/{id}/renew` 续费

#### B2. 会员画像模块
- `GET /v3/member/profile` 我的画像
- `PUT /v3/member/profile` 修改信息
- `GET /v3/admin/members` 会员列表（含 member_level、vip_expire_time）
- `POST /v3/admin/members/{id}/set-level` 设置等级

#### B3. 权益分发模块
- `GET /v3/admin/partners/{id}/benefit-pool` 查看权益池
- `POST /v3/admin/partners/{id}/benefit-pool/grant` 分发权益
- `GET /v3/admin/benefit-grant-logs` 分发记录

#### B4. 标书下载配额
- `GET /v3/member/benefits/usage` 已存在，需扩展返回下载配额
- `POST /v3/tenders/{id}/download` 增加配额检查 + 扣减逻辑

#### B5. 订单创建端点
- `POST /v3/orders` 创建订单（连接支付前置）

### Phase C：前端接入（预计 5-7 天）

#### C1. 消除 Mock Composables
- 删除/重构 `useMockData.ts` → 改为调用 `/v3/tenders` + `/v3/activities`
- 删除/重构 `useServiceProviders.ts` → 改为调用 `/v3/suppliers`

#### C2. 会员中心页面接入
- `member/orders.vue` ✅ 已接入
- `member/index.vue` → `/v3/member/profile` + `/v3/member/benefits/usage`
- `member/settings.vue` → `/v3/member/profile` PUT
- `member/favorites.vue` → 需后端收藏 API
- `member/certificates.vue` → 需后端证书 API
- `member/un-apply.vue` → `/v3/member-verifications`
- `member/supplier-apply.vue` → `/v3/supplier-onboarding`

#### C3. 管理后台页面接入
- `admin/partners/*` → `/v3/admin/partners`
- `admin/members/list.vue` → `/v3/admin/members`
- `admin/members/analysis.vue` → `/v3/admin/members/stats`
- `admin/tenders/publish.vue` → `POST /v3/admin/tenders`
- `admin/tenders/manage.vue` → `GET/PUT /v3/admin/tenders`
- `admin/content/display.vue` → 依赖 OSS，暂 placeholder

### Phase D：高级功能（预计 5-8 天）

#### D1. 活动双级审核
- 修改 PartnerPublishesV3Controller review 逻辑
- 状态流：0(DRAFT) → 10(PENDING_L1) → 20(PENDING_L2) → 30(APPROVED)

#### D2. 定时任务
- 会员续费提醒（到期前 7 天）
- 会员自动降级（到期日 FREE + 清零）
- 合伙人续费提醒（到期前 30 天）

#### D3. 文件上传
- OSS/MinIO 集成
- 统一 `POST /v3/files/upload` 端点
- 轮播图、证书、标书原文上传

#### D4. 数据大屏 API
- `GET /v3/admin/dashboard/overview` 汇总统计
- `GET /v3/admin/dashboard/trends` 趋势数据

---

## 六、优先级路线图

```
Week 1 ──── Phase A (基础修复)
  ├── A1 创建 7 Entity + Mapper + Service
  ├── A2 补全 4 Entity 审计字段
  ├── A3 修复 order_type 枚举
  └── A4 修复 4 安全漏洞

Week 2-3 ── Phase B (核心业务 API)
  ├── B1 合伙人管理
  ├── B2 会员画像
  ├── B3 权益分发
  ├── B4 标书下载配额
  └── B5 订单创建

Week 3-4 ── Phase C (前端接入)
  ├── C1 消除 Mock Composables
  ├── C2 会员中心页面
  └── C3 管理后台页面

Week 4-5 ── Phase D (高级功能)
  ├── D1 双级审核
  ├── D2 定时任务
  ├── D3 文件上传
  └── D4 数据大屏
```

---

## 七、docs/index.md 漂移更新建议

以下漂移条目已过时，建议更新：

| 原记录 | 当前实际状态 |
|--------|-------------|
| POST /v3/admin/contents 缺失 | ✅ 已实现 (978cf74) |
| reference.vue 只读 | ✅ 已接入 /v3/admin/tenders 列表 |
| suppliers/analysis.vue 错误 API | ✅ 已修正 (1df2247) |

应新增漂移：
- order_type DDL CHECK 与代码枚举值不一致
- 7 张 Phase 1 表无 Entity（系统级阻塞）
- 活动审核仅单级（需求要求双级）
- ActivitiesV3Controller audit_status=30 已修复 (2026-02-28)
- OrdersV3Controller 已加 user_id 隔离 (2026-02-28)
