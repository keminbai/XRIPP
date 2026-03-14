package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.NotificationSendLog;
import com.xripp.backend.entity.NotificationTemplate;
import com.xripp.backend.entity.NotificationTypeSetting;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.INotificationSendLogService;
import com.xripp.backend.service.INotificationTemplateService;
import com.xripp.backend.service.INotificationTypeSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationsV3Controller {

    private static final Set<String> VALID_TYPES = Set.of("payment", "activity", "urgent");
    private static final Set<String> VALID_TARGETS = Set.of("all", "member", "supplier", "partner", "custom");
    private final INotificationTypeSettingService notificationTypeSettingService;
    private final INotificationTemplateService notificationTemplateService;
    private final INotificationSendLogService notificationSendLogService;

    @GetMapping("/type-settings")
    public V3Response<List<Map<String, Object>>> typeSettings() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        ensureDefaultTypeSettings();
        List<NotificationTypeSetting> rows = notificationTypeSettingService.list(
                new QueryWrapper<NotificationTypeSetting>().orderByAsc("id")
        );

        List<Map<String, Object>> items = new ArrayList<>();
        for (NotificationTypeSetting row : rows) {
            items.add(toTypeSettingItem(row));
        }
        return V3Response.success(items);
    }

    @Transactional
    @PostMapping("/type-settings")
    public V3Response<List<Map<String, Object>>> saveTypeSettings(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Object rawItems = body.get("items");
        if (!(rawItems instanceof List<?> items) || items.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "items required");
        }

        Date now = new Date();
        Long currentUserId = SecurityContextHolder.getCurrentUserId();

        for (Object raw : items) {
            if (!(raw instanceof Map<?, ?> rawMap)) {
                return V3Response.error("VALIDATION_ERROR", "invalid type setting payload");
            }
            Map<String, Object> item = new LinkedHashMap<>();
            rawMap.forEach((key, value) -> item.put(String.valueOf(key), value));

            String notificationType = normalizeType(firstNonBlank(
                    str(item.get("notification_type")),
                    str(item.get("notificationType")),
                    str(item.get("type"))
            ));
            if (notificationType.isEmpty()) {
                return V3Response.error("VALIDATION_ERROR", "invalid notification type");
            }

            NotificationTypeSetting setting = notificationTypeSettingService.getOne(
                    new QueryWrapper<NotificationTypeSetting>().eq("notification_type", notificationType),
                    false
            );
            if (setting == null) {
                setting = new NotificationTypeSetting();
                setting.setNotificationType(notificationType);
                setting.setCreatedAt(now);
            }

            setting.setDisplayName(firstNonBlank(str(item.get("display_name")), str(item.get("displayName")), defaultDisplayName(notificationType)));
            setting.setSmsEnabled(boolVal(item.get("sms"), boolVal(item.get("sms_enabled"), true)));
            setting.setInAppEnabled(boolVal(item.get("inApp"), boolVal(item.get("in_app_enabled"), true)));
            setting.setEmailEnabled(boolVal(item.get("email"), boolVal(item.get("email_enabled"), false)));
            setting.setDescription(firstNonBlank(str(item.get("desc")), str(item.get("description")), defaultDescription(notificationType)));
            setting.setChangedBy(currentUserId);
            setting.setChangedAt(now);
            setting.setUpdatedAt(now);

            if (setting.getId() == null) {
                notificationTypeSettingService.save(setting);
            } else {
                notificationTypeSettingService.updateById(setting);
            }
        }

        return typeSettings();
    }

    @GetMapping("/templates")
    public V3Response<List<Map<String, Object>>> templates() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        List<NotificationTemplate> rows = notificationTemplateService.list(
                new QueryWrapper<NotificationTemplate>()
                        .orderByAsc("sort_order")
                        .orderByAsc("id")
        );
        List<Map<String, Object>> items = new ArrayList<>();
        for (NotificationTemplate row : rows) {
            items.add(toTemplateItem(row));
        }
        return V3Response.success(items);
    }

    @Transactional
    @PostMapping("/templates")
    public V3Response<Map<String, Object>> createTemplate(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String templateName = firstNonBlank(str(body.get("template_name")), str(body.get("templateName")), str(body.get("name")));
        String notificationType = normalizeType(firstNonBlank(str(body.get("notification_type")), str(body.get("notificationType")), str(body.get("type"))));
        String templateContent = firstNonBlank(str(body.get("template_content")), str(body.get("templateContent")), str(body.get("content")));
        if (templateName.isEmpty() || notificationType.isEmpty() || templateContent.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "template_name/notification_type/template_content required");
        }

        Date now = new Date();
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName(templateName);
        template.setNotificationType(notificationType);
        template.setTemplateContent(templateContent);
        template.setEnabled(boolVal(body.get("enabled"), true));
        template.setSortOrder(intVal(body.get("sort_order"), intVal(body.get("sortOrder"), 0)));
        template.setCreatedBy(SecurityContextHolder.getCurrentUserId());
        template.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        notificationTemplateService.save(template);

        return V3Response.success(toTemplateItem(template));
    }

    @Transactional
    @PutMapping("/templates/{id}")
    public V3Response<Map<String, Object>> updateTemplate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        NotificationTemplate template = notificationTemplateService.getById(id);
        if (template == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "template not found");
        }

        String templateName = firstNonBlank(
                str(body.get("template_name")),
                str(body.get("templateName")),
                str(body.get("name")),
                safe(template.getTemplateName())
        );
        String notificationType = normalizeType(firstNonBlank(
                str(body.get("notification_type")),
                str(body.get("notificationType")),
                str(body.get("type")),
                safe(template.getNotificationType())
        ));
        String templateContent = firstNonBlank(
                str(body.get("template_content")),
                str(body.get("templateContent")),
                str(body.get("content")),
                safe(template.getTemplateContent())
        );
        if (templateName.isEmpty() || notificationType.isEmpty() || templateContent.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "template_name/notification_type/template_content required");
        }

        template.setTemplateName(templateName);
        template.setNotificationType(notificationType);
        template.setTemplateContent(templateContent);
        if (body.containsKey("enabled")) {
            template.setEnabled(boolVal(body.get("enabled"), true));
        }
        if (body.containsKey("sort_order") || body.containsKey("sortOrder")) {
            template.setSortOrder(intVal(body.get("sort_order"), intVal(body.get("sortOrder"), 0)));
        }
        template.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        template.setUpdatedAt(new Date());
        notificationTemplateService.updateById(template);

        return V3Response.success(toTemplateItem(template));
    }

    @Transactional
    @DeleteMapping("/templates/{id}")
    public V3Response<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        NotificationTemplate template = notificationTemplateService.getById(id);
        if (template == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "template not found");
        }
        notificationTemplateService.removeById(id);
        return V3Response.success(Map.of("id", id, "deleted", true));
    }

    @GetMapping("/send-logs")
    public V3Response<List<Map<String, Object>>> sendLogs() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        List<NotificationSendLog> rows = notificationSendLogService.list(
                new QueryWrapper<NotificationSendLog>()
                        .orderByDesc("requested_at")
                        .orderByDesc("id")
        );
        List<Map<String, Object>> items = new ArrayList<>();
        for (NotificationSendLog row : rows) {
            items.add(toSendLogItem(row));
        }
        return V3Response.success(items);
    }

    @Transactional
    @PostMapping("/send")
    public V3Response<Map<String, Object>> send(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String notificationType = normalizeType(firstNonBlank(str(body.get("notification_type")), str(body.get("notificationType")), str(body.get("type"))));
        String targetScope = normalizeTarget(firstNonBlank(str(body.get("target_scope")), str(body.get("targetScope")), str(body.get("target"))));
        String title = firstNonBlank(str(body.get("notification_title")), str(body.get("notificationTitle")), str(body.get("title")));
        String content = firstNonBlank(str(body.get("notification_content")), str(body.get("notificationContent")), str(body.get("content")));
        if (notificationType.isEmpty() || targetScope.isEmpty() || title.isEmpty() || content.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "notification_type/target_scope/title/content required");
        }

        Date now = new Date();
        NotificationSendLog log = new NotificationSendLog();
        log.setNotificationType(notificationType);
        log.setTargetScope(targetScope);
        log.setNotificationTitle(title);
        log.setNotificationContent(content);
        log.setSendStatus("processed");
        log.setRequestedBy(SecurityContextHolder.getCurrentUserId());
        log.setRequestedAt(now);
        log.setProcessedAt(now);
        log.setResultMessage("Notification request accepted and recorded; external provider integration is not enabled yet.");
        notificationSendLogService.save(log);

        return V3Response.success(toSendLogItem(log));
    }

    private void ensureDefaultTypeSettings() {
        if (notificationTypeSettingService.count() > 0) {
            return;
        }
        Date now = new Date();
        List<NotificationTypeSetting> defaults = List.of(
                buildDefaultTypeSetting("payment", true, true, false, now),
                buildDefaultTypeSetting("activity", true, true, true, now),
                buildDefaultTypeSetting("urgent", true, true, true, now)
        );
        for (NotificationTypeSetting item : defaults) {
            notificationTypeSettingService.save(item);
        }
    }

    private NotificationTypeSetting buildDefaultTypeSetting(
            String type,
            boolean smsEnabled,
            boolean inAppEnabled,
            boolean emailEnabled,
            Date now
    ) {
        NotificationTypeSetting setting = new NotificationTypeSetting();
        setting.setNotificationType(type);
        setting.setDisplayName(defaultDisplayName(type));
        setting.setSmsEnabled(smsEnabled);
        setting.setInAppEnabled(inAppEnabled);
        setting.setEmailEnabled(emailEnabled);
        setting.setDescription(defaultDescription(type));
        setting.setCreatedAt(now);
        setting.setUpdatedAt(now);
        return setting;
    }

    private Map<String, Object> toTypeSettingItem(NotificationTypeSetting row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("type", safe(row.getNotificationType()));
        item.put("displayName", safe(row.getDisplayName()));
        item.put("sms", Boolean.TRUE.equals(row.getSmsEnabled()));
        item.put("inApp", Boolean.TRUE.equals(row.getInAppEnabled()));
        item.put("email", Boolean.TRUE.equals(row.getEmailEnabled()));
        item.put("desc", safe(row.getDescription()));
        item.put("changedAt", fmtDateTime(row.getChangedAt()));
        item.put("updatedAt", fmtDateTime(row.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toTemplateItem(NotificationTemplate row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("name", safe(row.getTemplateName()));
        item.put("type", safe(row.getNotificationType()));
        item.put("content", safe(row.getTemplateContent()));
        item.put("enabled", Boolean.TRUE.equals(row.getEnabled()));
        item.put("sortOrder", row.getSortOrder() == null ? 0 : row.getSortOrder());
        item.put("updatedAt", fmtDateTime(row.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toSendLogItem(NotificationSendLog row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("date", fmtDateTime(row.getRequestedAt()));
        item.put("type", safe(row.getNotificationType()));
        item.put("title", safe(row.getNotificationTitle()));
        item.put("target", safe(row.getTargetScope()));
        item.put("status", safe(row.getSendStatus()));
        item.put("statusLabel", mapSendStatusLabel(row.getSendStatus()));
        item.put("content", safe(row.getNotificationContent()));
        item.put("resultMessage", safe(row.getResultMessage()));
        item.put("processedAt", fmtDateTime(row.getProcessedAt()));
        return item;
    }

    private String normalizeType(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.ROOT);
        return VALID_TYPES.contains(normalized) ? normalized : "";
    }

    private String normalizeTarget(String value) {
        String normalized = safe(value).trim().toLowerCase(Locale.ROOT);
        return VALID_TARGETS.contains(normalized) ? normalized : "";
    }

    private String defaultDisplayName(String type) {
        return switch (type) {
            case "payment" -> "付费通知";
            case "activity" -> "活动通知";
            case "urgent" -> "应急重点通知";
            default -> type;
        };
    }

    private String defaultDescription(String type) {
        return switch (type) {
            case "payment" -> "会员费、标书、培训等支付通知";
            case "activity" -> "活动报名、推广通知";
            case "urgent" -> "紧急事项快速触达";
            default -> "";
        };
    }

    private String mapSendStatusLabel(String status) {
        return switch (safe(status)) {
            case "processed" -> "已处理";
            case "failed" -> "失败";
            default -> "已接收";
        };
    }

    private int intVal(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean boolVal(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
