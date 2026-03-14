package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.AdminPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionsV3Controller {

    private final AdminPermissionService adminPermissionService;

    @GetMapping("/current")
    public V3Response<Map<String, Object>> current() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        return V3Response.success(adminPermissionService.getCurrentSnapshot());
    }

    @GetMapping("/login-modes")
    public V3Response<List<Map<String, Object>>> loginModes() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        return V3Response.success(adminPermissionService.listLoginModes());
    }

    @PostMapping("/login-modes")
    public V3Response<List<Map<String, Object>>> saveLoginModes(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Object rawItems = body.get("items");
        if (!(rawItems instanceof List<?> items) || items.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "items required");
        }

        try {
            List<Map<String, Object>> normalizedItems = items.stream()
                    .filter(Map.class::isInstance)
                    .map(Map.class::cast)
                    .map(this::castToStringObjectMap)
                    .toList();
            if (normalizedItems.isEmpty()) {
                return V3Response.error("VALIDATION_ERROR", "items required");
            }
            return V3Response.success(adminPermissionService.saveLoginModes(normalizedItems));
        } catch (IllegalArgumentException e) {
            return V3Response.error("VALIDATION_ERROR", e.getMessage());
        }
    }

    @GetMapping("/profiles")
    public V3Response<List<Map<String, Object>>> profiles() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        return V3Response.success(adminPermissionService.listProfiles());
    }

    @PostMapping("/profiles")
    public V3Response<Map<String, Object>> createProfile(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        try {
            return V3Response.success(adminPermissionService.createProfile(body));
        } catch (IllegalArgumentException e) {
            return V3Response.error("VALIDATION_ERROR", e.getMessage());
        }
    }

    @PutMapping("/profiles/{id}")
    public V3Response<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }
        try {
            return V3Response.success(adminPermissionService.updateProfile(id, body));
        } catch (IllegalArgumentException e) {
            return V3Response.error("VALIDATION_ERROR", e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/profile")
    public V3Response<Map<String, Object>> assignProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        try {
            Long profileId = extractProfileId(body);
            Map<String, Object> result = new LinkedHashMap<>(adminPermissionService.assignProfileToUser(userId, profileId));
            return V3Response.success(result);
        } catch (IllegalArgumentException e) {
            return V3Response.error("VALIDATION_ERROR", e.getMessage());
        }
    }

    private Long extractProfileId(Map<String, Object> body) {
        if (body.containsKey("permission_profile_id")) {
            return parseNullableLong(body.get("permission_profile_id"), "permission_profile_id");
        }
        if (body.containsKey("permissionProfileId")) {
            return parseNullableLong(body.get("permissionProfileId"), "permissionProfileId");
        }
        return null;
    }

    private Long parseNullableLong(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        try {
            String raw = String.valueOf(value).trim();
            if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) {
                return null;
            }
            return Long.parseLong(raw);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("invalid " + fieldName);
        }
    }

    private Map<String, Object> castToStringObjectMap(Map<?, ?> source) {
        Map<String, Object> item = new LinkedHashMap<>();
        source.forEach((key, value) -> item.put(String.valueOf(key), value));
        return item;
    }
}
