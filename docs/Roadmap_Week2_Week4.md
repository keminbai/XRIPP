# Roadmap Week2-Week4

## 总目标
在 Week1 P0 打通基础上，完成：
1. 内容发布系统（contents）闭环  
2. 标书系统（tenders）闭环  
3. 供应商系统深水区（入库、名录、证书、审核）  
4. 观测性、性能与上线治理

---

## Week2：内容与标书闭环周（P1主攻）

### 目标
- 完成 `contents` 与 `tenders` 全链路（前台展示 + 后台发布审核）

### 后端任务
1. Contents
   - `POST /api/v3/contents`
   - `GET /api/v3/contents`
   - `POST /api/v3/contents/{id}/submit-review`
   - `POST /api/v3/admin/contents/{id}/review`
   - `POST /api/v3/admin/contents/{id}/publish`
   - `POST /api/v3/admin/contents/{id}/offline`
2. Tenders
   - `POST /api/v3/tenders`
   - `GET /api/v3/tenders`
   - `POST /api/v3/admin/tenders/{id}/publish`
   - `POST /api/v3/admin/tenders/{id}/close`
3. 补充：
   - 操作审计统一写入 `state_transition_logs`

### 前端任务
1. 后台内容发布页接 v3
2. 后台标书管理页接 v3
3. 前台媒体中心改读 `/contents?content_type=media`
4. 公共采购/标书列表逐步切 v3（先只读）

### QA重点
- 发布状态机完整性（draft -> pending_review -> approved -> published/offline）
- 标书状态机完整性（draft -> published -> closed -> archived）

### Week2 DoD
- 内容和标书均可“创建-审核-发布(或关闭)-回显”
- 无 P0 缺陷

---

## Week3：供应商系统与业务联动周（P1/P2）

### 目标
- 从“可审核”升级到“可运营”
- 打通供应商名录、详情、启停、证书下载/展示

### 后端任务
1. 新增/完善接口（建议）
   - `GET /api/v3/suppliers`
   - `GET /api/v3/suppliers/{id}`
   - `POST /api/v3/admin/suppliers/{id}/toggle-active`
2. 入库审核与 suppliers 主表联动
   - `supplier_onboarding.final_review_passed -> suppliers.active`
3. 证书与附件
   - 统一文件元数据表（若未建）
   - 供应商证书关联关系
4. 报表预聚合（可选）
   - 供应商按城市/服务类型统计

### 前端任务
1. `/services` 服务商库从本地数据切到 v3
2. 服务商详情弹窗改真实接口
3. 后台供应商审核/管理页联动真实状态
4. 下载按钮接真实文件地址（先鉴权后下载）

### QA重点
- 名录筛选准确性（城市/关键词/服务项）
- 启停后前台可见性正确
- 附件权限校验

### Week3 DoD
- 服务商链路脱离 mock
- 入库->审核->上架->前台可见全链路完成

---

## Week4：稳定性与上线治理周（P2）

### 目标
- 从“能用”提升到“可持续运营”
- 准备灰度与正式上线

### 后端任务
1. 性能优化
   - 慢查询治理、索引补强、分页上限保护
2. 观测性
   - request_id 全链路
   - 关键接口耗时/错误率监控
3. 安全与治理
   - 限流策略（登录、提交、审核动作）
   - 审计日志抽样巡检
4. 兼容收口
   - v2 调用量统计
   - 制定 v2 下线计划

### 前端任务
1. 全站错误处理统一（按 `code`）
2. 空态/异常态/重试机制
3. 关键页面埋点（提交成功率、转化漏斗）

### QA重点
- 回归测试全量执行
- 压测与稳定性测试
- 灰度验证（小流量）

### Week4 DoD
- 达到上线闸门指标
- 形成《上线检查清单》与《回滚预案演练记录》

---

## 跨周治理项（Week2~Week4 持续执行）

### 1. 字段与状态一致性红线
- 禁止新增“口头字段”
- 所有状态变更必须写日志

### 2. 变更评审机制
- DDL变更必须附回滚脚本
- API变更必须更新契约文档

### 3. 缺陷分级SLA
- P0：24小时闭环
- P1：72小时闭环
- P2：排期处理

### 4. 发布节奏
- 每周一次小版本
- 双周一次稳定版本

---

## 里程碑

- M1（Week1末）  
  P0链路打通（认证/活动/订单）

- M2（Week2末）  
  内容与标书闭环

- M3（Week3末）  
  供应商体系脱离mock并可运营

- M4（Week4末）  
  可灰度、可观测、可回滚，达到上线条件
