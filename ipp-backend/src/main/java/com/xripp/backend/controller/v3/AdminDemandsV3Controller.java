package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.AuditLog;
import com.xripp.backend.entity.Demand;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IAuditLogService;
import com.xripp.backend.service.IDemandService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/demands")
@RequiredArgsConstructor
public class AdminDemandsV3Controller {

    private static final int AUDIT_PENDING   = 0;
    private static final int AUDIT_PUBLISHED = 30;
    private static final int AUDIT_REJECTED  = 40;

    private final IDemandService demandService;
    private final StateTransitionService stateTransitionService;
    private final IAuditLogService auditLogService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "audit_status", required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<Demand> qw = new QueryWrapper<>();
        if (auditStatus != null) {
            qw.eq("audit_status", auditStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword.trim());
        }
        qw.orderByDesc("created_at");

        Page<Demand> p = new Page<>(page, pageSize);
        Page<Demand> result = demandService.page(p, qw);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @Transactional
    @PostMapping("/{id}/review")
    public V3Response<Void> review(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String action = String.valueOf(body.getOrDefault("action", "")).trim();
        String reason = String.valueOf(body.getOrDefault("reason", "")).trim();

        if (!"approve".equals(action) && !"reject".equals(action)) {
            return V3Response.error("VALIDATION_ERROR", "action must be approve|reject");
        }

        Demand demand = demandService.getById(id);
        if (demand == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "demand not found");
        }

        Integer curr = demand.getAuditStatus();
        if (curr == null || curr != AUDIT_PENDING) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending demand can be reviewed");
        }

        int newStatus = "approve".equals(action) ? AUDIT_PUBLISHED : AUDIT_REJECTED;
        demand.setAuditStatus(newStatus);
        demandService.updateById(demand);

        stateTransitionService.log(
                "demand", demand.getId(),
                String.valueOf(curr), String.valueOf(newStatus),
                action, reason.isBlank() ? null : reason
        );

        AuditLog auditLog = new AuditLog();
        auditLog.setTargetType("demand");
        auditLog.setTargetId(demand.getId());
        auditLog.setOperatorId(SecurityContextHolder.getCurrentUserId());
        auditLog.setAction(action);
        auditLog.setPrevStatus((byte) curr.intValue());
        auditLog.setCurrStatus((byte) newStatus);
        auditLog.setComment(reason.isBlank() ? null : reason);
        auditLog.setCreatedAt(new Date());
        auditLogService.save(auditLog);

        return V3Response.success(null);
    }

    private Map<String, Object> toItem(Demand d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("demandNo", "DEM-" + String.format("%07d", d.getId()));
        m.put("title", d.getTitle() != null ? d.getTitle() : "");
        m.put("userId", d.getUserId());
        m.put("auditStatus", d.getAuditStatus() != null ? d.getAuditStatus() : AUDIT_PENDING);
        m.put("status", mapStatus(d.getAuditStatus()));
        m.put("statusLabel", mapStatusLabel(d.getAuditStatus()));
        m.put("publishDate", fmtDate(d.getCreatedAt()));
        // Fields not yet in schema — provide safe defaults
        m.put("description", "");
        m.put("demandType", "-");
        m.put("industry", "-");
        m.put("companyName", "用户#" + d.getUserId());
        m.put("contactPerson", "-");
        m.put("contactPhone", "-");
        m.put("matchCount", 0);
        m.put("rejectReason", "");
        return m;
    }

    private String mapStatus(Integer s) {
        if (s == null || s == AUDIT_PENDING) return "pending";
        if (s == AUDIT_PUBLISHED) return "published";
        if (s == AUDIT_REJECTED) return "rejected";
        return "pending";
    }

    private String mapStatusLabel(Integer s) {
        if (s == null || s == AUDIT_PENDING) return "待审核";
        if (s == AUDIT_PUBLISHED) return "已发布";
        if (s == AUDIT_REJECTED) return "已驳回";
        return "待审核";
    }

    private String fmtDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
