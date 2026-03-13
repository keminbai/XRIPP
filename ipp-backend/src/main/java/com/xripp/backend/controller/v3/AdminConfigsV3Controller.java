package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.AdminConfigEntry;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IAdminConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/v3/admin/configs")
@RequiredArgsConstructor
public class AdminConfigsV3Controller {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private final IAdminConfigEntryService adminConfigEntryService;
    private final ObjectMapper objectMapper;

    @GetMapping("/{namespace}")
    public V3Response<Map<String, Object>> listByNamespace(@PathVariable String namespace) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        if (!isValidKey(namespace, 50)) {
            return V3Response.error("VALIDATION_ERROR", "invalid namespace");
        }

        List<AdminConfigEntry> rows = adminConfigEntryService.list(new QueryWrapper<AdminConfigEntry>()
                .eq("config_namespace", namespace)
                .orderByAsc("sort_order")
                .orderByAsc("id"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("namespace", namespace);
        result.put("items", rows.stream().map(this::toItem).toList());
        return V3Response.success(result);
    }

    @Transactional
    @PostMapping("/{namespace}/batch")
    public V3Response<Map<String, Object>> batchUpsert(
            @PathVariable String namespace,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        if (!isValidKey(namespace, 50)) {
            return V3Response.error("VALIDATION_ERROR", "invalid namespace");
        }

        Object rawItems = body.get("items");
        if (!(rawItems instanceof List<?> items) || items.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "items required");
        }

        Date now = new Date();
        Long currentUserId = SecurityContextHolder.getCurrentUserId();

        for (Object rawItem : items) {
            if (!(rawItem instanceof Map<?, ?> rawMap)) {
                return V3Response.error("VALIDATION_ERROR", "invalid item payload");
            }

            Map<String, Object> item = new LinkedHashMap<>();
            rawMap.forEach((key, value) -> item.put(String.valueOf(key), value));

            String configKey = strVal(item, "key");
            if (!isValidKey(configKey, 100)) {
                return V3Response.error("VALIDATION_ERROR", "invalid key");
            }

            QueryWrapper<AdminConfigEntry> qw = new QueryWrapper<>();
            qw.eq("config_namespace", namespace).eq("config_key", configKey);
            AdminConfigEntry entry = adminConfigEntryService.getOne(qw, false);
            if (entry == null) {
                entry = new AdminConfigEntry();
                entry.setConfigNamespace(namespace);
                entry.setConfigKey(configKey);
                entry.setCreatedAt(now);
            }

            entry.setConfigName(strVal(item, "name"));
            entry.setDescription(strVal(item, "description"));
            entry.setConfigValueJson(writeJson(item.get("value")));
            entry.setSortOrder(intVal(item.get("sort_order"), intVal(item.get("sortOrder"), 0)));
            entry.setEnabled(boolVal(item.get("enabled"), true));
            entry.setChangedBy(currentUserId);
            entry.setChangedAt(now);
            entry.setUpdatedAt(now);

            if (entry.getId() == null) {
                adminConfigEntryService.save(entry);
            } else {
                adminConfigEntryService.updateById(entry);
            }
        }

        return listByNamespace(namespace);
    }

    @Transactional
    @DeleteMapping("/{namespace}/{key}")
    public V3Response<Map<String, Object>> deleteOne(
            @PathVariable String namespace,
            @PathVariable String key
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        if (!isValidKey(namespace, 50) || !isValidKey(key, 100)) {
            return V3Response.error("VALIDATION_ERROR", "invalid namespace or key");
        }

        QueryWrapper<AdminConfigEntry> qw = new QueryWrapper<>();
        qw.eq("config_namespace", namespace).eq("config_key", key);
        AdminConfigEntry entry = adminConfigEntryService.getOne(qw, false);
        if (entry == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "config entry not found");
        }

        adminConfigEntryService.removeById(entry.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("namespace", namespace);
        result.put("key", key);
        result.put("deleted", true);
        return V3Response.success(result);
    }

    private Map<String, Object> toItem(AdminConfigEntry entry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entry.getId());
        item.put("namespace", safe(entry.getConfigNamespace()));
        item.put("key", safe(entry.getConfigKey()));
        item.put("name", safe(entry.getConfigName()));
        item.put("description", safe(entry.getDescription()));
        item.put("enabled", Boolean.TRUE.equals(entry.getEnabled()));
        item.put("sortOrder", entry.getSortOrder() == null ? 0 : entry.getSortOrder());
        item.put("value", readJson(entry.getConfigValueJson()));
        item.put("updatedAt", fmtDateTime(entry.getUpdatedAt()));
        return item;
    }

    private Object readJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(rawJson, Object.class);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? new ArrayList<>() : value);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private boolean isValidKey(String value, int maxLength) {
        return value != null && value.length() <= maxLength && KEY_PATTERN.matcher(value).matches();
    }

    private String strVal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value).trim();
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

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' ');
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
