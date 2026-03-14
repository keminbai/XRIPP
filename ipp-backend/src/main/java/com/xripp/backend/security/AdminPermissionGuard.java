package com.xripp.backend.security;

import com.xripp.backend.service.AdminPermissionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminPermissionGuard {

    private final AdminPermissionService adminPermissionService;
    private final List<PermissionRule> rules = buildRules();

    public PermissionRequirement resolve(String requestPath, String httpMethod) {
        if (requestPath == null || httpMethod == null) {
            return null;
        }
        if ("/api/v3/admin/permissions/current".equals(requestPath)) {
            return null;
        }
        for (PermissionRule rule : rules) {
            if (requestPath.startsWith(rule.getPathPrefix())) {
                PermissionRequirement requirement = new PermissionRequirement();
                requirement.setModuleCode(rule.getModuleCode());
                requirement.setAction(resolveAction(httpMethod, requestPath));
                return requirement;
            }
        }
        return null;
    }

    public boolean isAllowed(PermissionRequirement requirement) {
        if (requirement == null) {
            return true;
        }
        return adminPermissionService.hasAdminPermission(
                requirement.getModuleCode(),
                requirement.getAction()
        );
    }

    private String resolveAction(String httpMethod, String requestPath) {
        String method = httpMethod.trim().toUpperCase();
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return "view";
        }
        if ("DELETE".equals(method)) {
            return "delete";
        }
        if (requestPath.endsWith("/download") || requestPath.endsWith("/export")) {
            return "export";
        }
        if (requestPath.endsWith("/transition")
                || requestPath.endsWith("/review")
                || requestPath.endsWith("/submit")
                || requestPath.endsWith("/send")) {
            return "review";
        }
        if ("POST".equals(method)) {
            return "create";
        }
        if ("PUT".equals(method) || "PATCH".equals(method)) {
            return "edit";
        }
        return "view";
    }

    private List<PermissionRule> buildRules() {
        List<PermissionRule> items = new ArrayList<>();
        items.add(rule("/api/v3/admin/permissions", "system_permissions"));
        items.add(rule("/api/v3/admin/notifications", "system_notifications"));
        items.add(rule("/api/v3/admin/customer-service", "system_customer_service"));
        items.add(rule("/api/v3/admin/logs", "system_logs"));
        items.add(rule("/api/v3/admin/configs", "system_config_center"));
        items.add(rule("/api/v3/admin/audit", "audit_center"));
        items.add(rule("/api/v3/admin/contents", "content_management"));
        items.add(rule("/api/v3/admin/supplier-onboarding", "supplier_onboarding_review"));
        items.add(rule("/api/v3/admin/members", "member_management"));
        items.add(rule("/api/v3/admin/orders", "member_orders"));
        items.add(rule("/api/v3/admin/overseas", "overseas_management"));
        items.add(rule("/api/v3/admin/overseas-points", "overseas_management"));
        return items;
    }

    private PermissionRule rule(String pathPrefix, String moduleCode) {
        PermissionRule item = new PermissionRule();
        item.setPathPrefix(pathPrefix);
        item.setModuleCode(moduleCode);
        return item;
    }

    @Data
    private static class PermissionRule {
        private String pathPrefix;
        private String moduleCode;
    }

    @Data
    public static class PermissionRequirement {
        private String moduleCode;
        private String action;
    }
}
