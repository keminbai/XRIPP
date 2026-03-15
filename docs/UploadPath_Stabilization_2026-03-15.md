# 上传路径稳定化修复记录（2026-03-15）

## 1. 现象

活动管理页上传封面图时：

- 浏览器能看到 `el-upload` 的本地预览
- 但后台弹出上传失败提示
- 保存后再次打开记录，图片未持久化
- Windows 环境报错文本末尾出现“系统找不到指定的路径”

## 2. 根因

问题分为两层：

1. 活动页前端一度存在上传成功回包适配过窄的问题，导致“预览有了，但表单字段未回填”
2. 后端上传底座仍依赖相对路径 `./uploads` 与 `MultipartFile.transferTo()`，在 Windows 运行目录漂移或临时文件处理不稳定时，更容易触发路径类异常

## 3. 本次修复

### 3.1 上传目录统一收敛

新增：

- `ipp-backend/src/main/java/com/xripp/backend/config/UploadStorageProperties.java`

职责：

- 统一解析上传根目录
- 支持配置项 `xripp.upload.base-dir`
- 启动时将相对路径解析为绝对标准路径

### 3.2 静态资源映射与上传写入复用同一目录

修改：

- `ipp-backend/src/main/java/com/xripp/backend/config/WebMvcConfig.java`
- `ipp-backend/src/main/java/com/xripp/backend/controller/v3/CommonV3Controller.java`

效果：

- `/uploads/**` 与实际写盘目录不再各自维护
- 避免“保存到 A，访问映射到 B”的漂移

### 3.3 文件写入方式更稳

`CommonV3Controller` 中：

- 从 `MultipartFile.transferTo()` 改为 `Files.copy(inputStream, targetPath, REPLACE_EXISTING)`

这样在 Windows 环境下更稳，更容易规避临时文件/路径类异常。

### 3.4 配置项补齐

`application.yml`：

- 新增 `xripp.upload.base-dir: ${XRIPP_UPLOAD_DIR:./uploads}`

允许后续在 Windows 环境直接指定绝对目录。

### 3.5 前端相对上传地址统一走 `/api/uploads/**`

新增：

- `app/utils/file-url.ts`

并替换多处页面中的本地 `resolveFileUrl()`：

- `admin/content/activities.vue`
- `admin/suppliers/audit.vue`
- `admin/system/certificates.vue`
- `member/supplier-applications.vue`
- `supplier-directory.vue`

效果：

- 后端返回 `/uploads/...` 时，前端统一解析为 `/api/uploads/...`
- 避免开发环境下命中 `localhost:3000/uploads/...` 造成 404

## 4. 预期结果

修复后应满足：

- 活动管理上传封面图不再出现“系统找不到指定的路径”
- 上传成功后提示正常
- 保存后再次打开，图片仍存在
- `/uploads/**` 与真实磁盘文件一一对应

## 5. 建议复测

- `admin/content/activities.vue` 上传封面图
- 保存活动后重新打开
- 确认图片持久化
