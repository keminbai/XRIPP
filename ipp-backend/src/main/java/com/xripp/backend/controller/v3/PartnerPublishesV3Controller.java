package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.AuditLog;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IAuditLogService;
import com.xripp.backend.service.IActivityService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/partner/publishes")
@RequiredArgsConstructor
public class PartnerPublishesV3Controller {

    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_APPROVED = 30;
    private static final int AUDIT_REJECTED = 40;

    private final IActivityService activityService;
    private final StateTransitionService stateTransitionService;
    private final IAuditLogService auditLogService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "audit_status", required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String publisher
    ) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<Activity> qw = new QueryWrapper<>();
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null || partnerId <= 0) {
                return V3Response.error("AUTH_FORBIDDEN", "invalid partner");
            }
            qw.eq("partner_id", partnerId);
        }
        if (auditStatus != null) {
            qw.eq("audit_status", auditStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.like("title", keyword.trim());
        }
        if ("总部".equals(publisher)) {
            qw.and(w -> w.isNull("partner_id").or().eq("partner_id", 0));
        } else if ("合伙人".equals(publisher)) {
            qw.isNotNull("partner_id").gt("partner_id", 0);
        }
        qw.orderByDesc("created_at");

        Page<Activity> p = new Page<>(page, pageSize);
        Page<Activity> result = activityService.page(p, qw);

        return V3Response.success(new V3PageData<>(
                result.getRecords().stream().map(this::toPublishItem).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        ));
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        if (title.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "title required");
        }

        Long partnerId = SecurityContextHolder.getCurrentPartnerId();
        if (SecurityContextHolder.isPartner() && (partnerId == null || partnerId <= 0)) {
            return V3Response.error("AUTH_FORBIDDEN", "invalid partner");
        }

        try {
            Activity a = new Activity();
            a.setTitle(title);
            a.setPartnerId(partnerId);
            a.setCreatedAt(new Date());
            a.setAuditStatus(AUDIT_PENDING);
            a.setIsFree(parseIsFree(body));
            a.setFee(parseFee(body));
            a.setStartTime(parseStartTime(body.get("activity_time")));
            if (a.getStartTime() == null) {
                a.setStartTime(new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000));
            }

            boolean ok = activityService.save(a);
            if (!ok || a.getId() == null) {
                return V3Response.error("INTERNAL_ERROR", "save publish failed");
            }

            return V3Response.success(Map.of(
                    "id", a.getId(),
                    "audit_status", a.getAuditStatus()
            ));
        } catch (Exception e) {
            return V3Response.error("INTERNAL_ERROR", "create publish failed: " + e.getMessage());
        }
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

        Activity a = activityService.getById(id);
        if (a == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }

        Integer curr = a.getAuditStatus();
        if (curr == null || curr != AUDIT_PENDING) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending publish can be reviewed");
        }

        int newStatus = "approve".equals(action) ? AUDIT_APPROVED : AUDIT_REJECTED;
        a.setAuditStatus(newStatus);
        a.setUpdatedAt(new Date());
        activityService.updateById(a);

        stateTransitionService.log(
                "activity", a.getId(),
                String.valueOf(curr), String.valueOf(newStatus),
                action, reason.isBlank() ? null : reason
        );

        AuditLog auditLog = new AuditLog();
        auditLog.setTargetType("activity");
        auditLog.setTargetId(a.getId());
        auditLog.setOperatorId(com.xripp.backend.security.SecurityContextHolder.getCurrentUserId());
        auditLog.setAction(action);
        auditLog.setPrevStatus((byte) curr.intValue());
        auditLog.setCurrStatus((byte) newStatus);
        auditLog.setComment(reason.isBlank() ? null : reason);
        auditLog.setCreatedAt(new Date());
        auditLogService.save(auditLog);

        return V3Response.success(null);
    }

    @DeleteMapping("/{id}")
    public V3Response<Void> withdraw(@PathVariable Long id) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity a = activityService.getById(id);
        if (a == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }

        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null || !partnerId.equals(a.getPartnerId())) {
                return V3Response.error("AUTH_FORBIDDEN", "forbidden");
            }
        }

        if (a.getAuditStatus() != null && a.getAuditStatus() != AUDIT_PENDING) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending publish can be withdrawn");
        }

        activityService.removeById(id);
        return V3Response.success(null);
    }

    private Map<String, Object> toPublishItem(Activity a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("section", "activity");
        m.put("submit_time", a.getCreatedAt());
        m.put("audit_status", a.getAuditStatus());
        m.put("status", mapStatus(a.getAuditStatus()));
        m.put("status_label", mapStatusLabel(a.getAuditStatus()));
        m.put("is_free", a.getIsFree());
        m.put("fee", a.getFee() == null ? BigDecimal.ZERO : a.getFee());
        return m;
    }

    private String mapStatus(Integer auditStatus) {
        if (auditStatus == null) return "pending";
        if (auditStatus == AUDIT_APPROVED) return "approved";
        if (auditStatus == AUDIT_REJECTED) return "rejected";
        return "pending";
    }

    private String mapStatusLabel(Integer auditStatus) {
        if (auditStatus == null) return "待审核";
        if (auditStatus == AUDIT_APPROVED) return "已通过";
        if (auditStatus == AUDIT_REJECTED) return "已驳回";
        return "待审核";
    }

    private Boolean parseIsFree(Map<String, Object> body) {
        Object feeType = body.get("fee_type");
        if (feeType != null) {
            return !"paid".equalsIgnoreCase(String.valueOf(feeType));
        }
        Object isFree = body.get("is_free");
        if (isFree != null) {
            return Boolean.parseBoolean(String.valueOf(isFree));
        }
        return Boolean.TRUE;
    }

    private BigDecimal parseFee(Map<String, Object> body) {
        try {
            Object fee = body.get("fee");
            if (fee == null) return BigDecimal.ZERO;
            return new BigDecimal(String.valueOf(fee));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private Date parseStartTime(Object v) {
        if (v == null) return null;
        if (v instanceof Date d) return d;
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return null;
        try {
            LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored2) {
                try {
                    return Date.from(Instant.parse(s));
                } catch (Exception ignored3) {
                    try {
                        return Date.from(OffsetDateTime.parse(s).toInstant());
                    } catch (Exception ignored4) {
                        return null;
                    }
                }
            }
        }
    }
}
