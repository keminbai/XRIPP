# Claude Windows 执行手册：Phase 24 出海发布系统联调（2026-03-14）

## 1. 目标

验证 Phase 24 出海发布系统真实化是否在 Windows 运行环境中真正闭环：

- DDL 已执行
- 后端可启动
- 新接口可用
- 前端页面可真实增删改查
- 海外模块权限可见

## 2. 本次必须执行的 DDL

按顺序确认：

1. `docs/DDL_Phase23_SystemPermissions_RBAC.sql`
2. `docs/DDL_Phase24_Overseas_Realization.sql`

说明：

- Phase 24 依赖 Phase 23 的权限表
- 若 Phase 23 已执行过，只需确认相关表存在即可

## 3. 后端启动前检查

检查项：

1. `/api/v3/runtime-info` 可返回当前实例信息
2. 启动日志中 `SchemaPreflight` 无报错
3. 数据库中存在以下表：
   - `overseas_services`
   - `overseas_service_files`
   - `overseas_reports`

## 4. 后端接口冒烟

以 admin token 执行。

### 4.1 services

1. `GET /api/v3/admin/overseas/services?page=1&page_size=10&status=published`
   - 预期：200，返回标准分页结构
2. `POST /api/v3/admin/overseas/services`
   - 预期：200，返回新建详情
3. `GET /api/v3/admin/overseas/services/{id}`
   - 预期：200，返回完整字段 + `promoImage/images/introFiles`
4. `PUT /api/v3/admin/overseas/services/{id}`
   - 预期：200，修改后可回读
5. `PUT /api/v3/admin/overseas/services/{id}` body=`{ "status": "offline" }`
   - 预期：200，状态改为 `offline`
6. `DELETE /api/v3/admin/overseas/services/{id}`
   - 预期：200，返回 `deleted=true`

### 4.2 reports

1. `GET /api/v3/admin/overseas/reports?page=1&page_size=10`
   - 预期：200，返回标准分页结构
2. `POST /api/v3/admin/overseas/reports`
   - 预期：200，返回新建详情
3. `GET /api/v3/admin/overseas/reports/{id}`
   - 预期：200，返回封面、文件、关键词、收费、访问级别
4. `PUT /api/v3/admin/overseas/reports/{id}`
   - 预期：200，修改后可回读
5. `POST /api/v3/admin/overseas/reports/{id}/download`
   - 预期：200，`downloads` 自增
6. `POST /api/v3/admin/overseas/reports/{id}/expire`
   - 预期：200，`expireDate` 更新
7. `DELETE /api/v3/admin/overseas/reports/{id}`
   - 预期：200，返回 `deleted=true`

### 4.3 analysis

1. `GET /api/v3/admin/overseas/analysis?range=12`
   - 预期：200
   - 返回应包含：
     - `stats`
     - `summary`
     - `networkDistribution`
     - `serviceTypes`
     - `industryDistribution`
     - `trend`
     - `updatedAt`

## 5. 前端页面联调

登录 admin 后验证：

1. `/admin/overseas/services`
   - 可加载列表
   - 可上传宣传图
   - 可上传服务图片
   - 可上传介绍文件
   - 新增后刷新仍存在
   - 编辑后刷新仍保留
   - 下架/重新发布生效
   - 删除后列表消失
2. `/admin/overseas/reports`
   - 可上传封面
   - 可上传报告文件
   - 新增后刷新仍存在
   - 编辑后刷新仍保留
   - 设置截止日期后刷新仍保留
   - 点击下载后下载量增长
3. `/admin/overseas/analysis`
   - 卡片、网点分布、服务类型、趋势、行业分布都能加载
   - 切换 `3/6/12` 月趋势无 500

## 6. 权限验证

验证点：

1. super-admin/admin fallback 账号可见海外模块菜单
2. 绑定了 `CONTENT_PUBLISHER` 的 admin 账号可见 `overseas` 菜单并可进入页面
3. 绑定了 `CONTENT_AUDITOR` 的 admin 账号
   - 应可进入页面
   - 但写操作应按权限受限

## 7. 建议回传格式

请 Claude Windows 侧回传以下结论：

1. DDL：PASS / FAIL
2. Backend compile/start：PASS / FAIL
3. services APIs：PASS / FAIL
4. reports APIs：PASS / FAIL
5. analysis API：PASS / FAIL
6. Frontend pages：PASS / FAIL
7. Permission visibility：PASS / FAIL
8. 若 FAIL，请给出：
   - 绝对路径
   - 接口或页面
   - 实际错误
   - 初步判断

## 8. 当前已知非代码型限制

WSL 侧未完成验证的原因：

- 无 `java` / `JAVA_HOME`
- `npm run build` 卡在 `@oxc-parser/binding-linux-x64-gnu` 缺失

所以本阶段最终真正确认，以 Windows 侧结果为准。
