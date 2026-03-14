# Logs 闭环记录（2026-03-14）

## 1. 本轮目标

将 `app/pages/admin/system/logs.vue` 从 session-only mock 页改为真实后台日志查询页。

## 2. 架构判断

`logs` 不需要再新建第三套日志表。

当前系统已经有两类真实日志来源：

- `audit_logs`
- `state_transition_logs`

因此最合理的做法不是重复建模，而是：

- 新增统一聚合查询接口
- 将两类现有真实日志统一投射到后台日志页

## 3. 后端完成项

### 3.1 新增接口

- `GET /v3/admin/logs`

### 3.2 聚合来源

- `audit_logs`
  - 审核操作类日志
- `state_transition_logs`
  - 状态流转类日志

### 3.3 支持筛选

- `keyword`
- `source`
  - `audit`
  - `transition`
- `date_from`
- `date_to`
- `page`
- `page_size`

### 3.4 返回字段

- `time`
- `user`
- `source`
- `sourceLabel`
- `action`
- `module`
- `target`
- `comment`

## 4. 前端完成项

`admin/system/logs.vue` 已改为真实日志查询页：

- 去除“当前会话有效”的假提示
- 查询真实 `/v3/admin/logs`
- 支持关键字 / 来源 / 日期范围筛选
- 支持分页
- 支持导出当前查询页 CSV
- 去掉没有真实来源支撑的 `IP地址` 字段

## 5. 页面语义

本轮日志页的真实边界是：

- 后台操作审计查询
- 状态流转查询

本轮未包含：

- 应用运行时错误日志
- Web 访问日志
- 慢查询日志
- 系统级运维日志

这些能力如果后续要进入后台，应单独界定来源和权限边界。

## 6. 当前降级页口径（本轮完成后）

### 2026-03-14 增补

本节记录的是 `logs.vue` 完成当时的剩余页估算，保留历史原貌；
但后续 `permissions` 与 `overseas/*` 已继续真实化，因此这里的“剩余 6 页”已过时。

当前真实口径应为：

- 业务型 `session-only`：**0 页**
- 说明页：**2 页**
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

建议当前剩余页面按真实口径收敛为：

- session-only：4 页
  - `admin/system/permissions.vue`
  - `admin/overseas/analysis.vue`
  - `admin/overseas/services.vue`
  - `admin/overseas/reports.vue`
- pure info / ops guidance：2 页
  - `admin/system/about.vue`
  - `admin/system/backup.vue`

合计：**6 页**

## 7. 结论

本轮后：

- `logs.vue` 已不再属于降级页
- 日志页已建立在现有真实审计基础上，而非重复造表
- 当前剩余真正待真实化重点进一步收敛到：
  - `permissions`
  - `overseas/*`
