# 证书模板管理真实化记录（2026-03-14）

## 1. 背景

`app/pages/admin/system/certificates.vue` 原先属于 session-only 页面：

- 模板列表仅保存在前端内存
- 上传成功后刷新即丢
- 无法形成稳定的模板资产库

但该页的真实边界并不是“证书签发系统”，而是“证书模板管理”：

- 文件上传
- 模板元数据保存
- 模板预览 / 下载 / 删除

因此不需要新建专用业务表，也不应把它误判为完整证书引擎。

## 2. 架构判断

本页拆成两层：

1. 文件本体
   - 继续复用 `/api/common/upload`
   - 文件落地到现有 uploads 目录
2. 模板元数据
   - 复用 `Phase 20` 通用配置底座持久化

理由：

- 文件上传能力已在系统内稳定复用
- 当前页面只管理模板元数据，不涉及签发流水、编号规则、发证批次
- 不值得为“模板列表”单独起一张表

## 3. 命名空间设计

页面：

- `app/pages/admin/system/certificates.vue`

namespace：

- `system_certificates`

key：

- `template_list`

元数据字段：

- `id`
- `name`
- `type`
- `category`
- `sourceModule`
- `updateTime`
- `fileUrl`
- `fileName`
- `storedName`
- `fileExt`
- `fileSize`
- `contentType`

## 4. 前端实现

真实化内容：

- 页面初始化真实加载模板列表
- 上传模板后元数据真实落库
- 刷新页面后模板不丢失
- 支持图片 / PDF 预览
- 支持下载
- 删除后刷新不复活

继续复用：

- `app/composables/useAdminConfigNamespace.ts`

## 5. 本轮边界

本轮只完成：

- 证书模板管理

本轮未做：

- 证书签发
- 编号生成规则
- 批量发证
- 业务证书发放记录

这些能力若后续需要，应独立建模，不继续硬塞配置表。

## 6. 结论

`admin/system/certificates.vue` 已从“前端会话态模板页”收敛为：

- 文件上传走既有上传能力
- 模板元数据走后台通用配置底座

这页现在可以视为真实后台页面，而不是降级占位页。
