# Content Meta Closure 2026-03-13

## 本轮目标

收敛 `contents` 通用内容模型中“正文与元数据混存”的历史问题，避免媒体资讯、培训课程等页面把结构化 JSON 直接当正文渲染，同时补齐管理端真实编辑能力。

## 本轮完成

### 1. 数据模型

- 新增 DDL：[`DDL_Phase18_Contents_ExtraJson.sql`](./DDL_Phase18_Contents_ExtraJson.sql)
- 为 `contents` 表新增 `extra_json`
  - `body` 只承担正文文本
  - `extra_json` 承担扩展元数据，如分类、作者、来源、培训讲师/章节/报名上限/展示位等

### 2. 后端接口

- 扩展 [`/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminContentsV3Controller.java`](/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/AdminContentsV3Controller.java)
  - 新增 `PUT /v3/admin/contents/{id}`
  - `detail()` 返回解析后的 `body + extraJson`
  - 列表项补充 `extraJson / extra_json`
- 扩展 [`/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/ContentsV3Controller.java`](/mnt/d/ipp-platform/ipp-backend/src/main/java/com/xripp/backend/controller/v3/ContentsV3Controller.java)
  - 公共详情优先读取真实正文
  - 对历史“`body` 里塞整段 JSON”的旧数据做兼容回退解析，避免前台直接显示原始 JSON 字符串

### 3. 兼容策略

- 不直接强制迁移历史脏数据，先通过后端解析兼容保障线上可读
- 新写入统一走：
  - `body` -> 正文
  - `extra_json` -> 结构化扩展字段
- 这样既避免一次性大规模数据回填风险，也避免继续制造新的混存数据

### 4. 前端页面

- 更新 [`/mnt/d/ipp-platform/app/pages/admin/content/media.vue`](/mnt/d/ipp-platform/app/pages/admin/content/media.vue)
  - 列表/详情/新建/编辑统一接入 `/v3/admin/contents`
  - 真正支持编辑保存，不再停留在“接口未接入”提示
  - 正文写入 `body`，分类/作者/来源等写入 `extra_json`
- 更新 [`/mnt/d/ipp-platform/app/pages/admin/content/trainings.vue`](/mnt/d/ipp-platform/app/pages/admin/content/trainings.vue)
  - 编辑前先请求详情接口
  - 培训专属字段从 `extraJson` 回填并保存
  - 修复 `cities` 重复 key 问题

## 结果

- 媒体正文不再因为历史 JSON 混存而渲染异常
- 培训/媒体内容管理从“部分可读不可写”提升为“列表 + 详情 + 创建 + 编辑”真实闭环
- `contents` 模型的职责边界更清晰，为后续内容类型扩展提供稳定基础

## 验证

- 前端：`/tmp/ipp-platform-buildcheck` 下 `npm run build` 通过
- 后端：`/tmp/ipp-backend-buildcheck/ipp-backend` 下 `./mvnw -q -DskipTests compile` 通过

## 后续说明

- 培训页中原有“显示申请 / 报名导出”入口已在后续阶段收敛移除，因为当前系统并不存在对应的培训报名表与显示申请模型
- 合伙人侧统一发布入口 `admin/partner-publish.vue` 已在后续阶段收敛为真实活动发布闭环
