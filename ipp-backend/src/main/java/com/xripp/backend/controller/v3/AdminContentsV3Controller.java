package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.ContentEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IContentService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/contents")
@RequiredArgsConstructor
public class AdminContentsV3Controller {

    private final IContentService contentService;
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "content_type", required = false) String contentType,
            @RequestParam(name = "publish_status", required = false) String publishStatus,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<ContentEntity> qw = new QueryWrapper<>();
        if (contentType != null && !contentType.isBlank()) {
            qw.eq("content_type", contentType.trim());
        }
        if (publishStatus != null && !publishStatus.isBlank()) {
            qw.eq("publish_status", publishStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw).or().like("summary", kw));
        }
        qw.orderByDesc("updated_at");

        Page<ContentEntity> p = new Page<>(page, pageSize);
        Page<ContentEntity> result = contentService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (ContentEntity c : result.getRecords()) {
            items.add(toItem(c));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        ContentEntity c = contentService.getById(id);
        if (c == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "content not found");
        }

        Map<String, Object> m = toItem(c);
        m.put("body", safe(c.getBody()));
        m.put("changeReason", safe(c.getChangeReason()));
        return V3Response.success(m);
    }

    @Transactional
    @PostMapping("/{id}/transition")
    public V3Response<Map<String, Object>> transition(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String toStatus = str(body.getOrDefault("to_status", ""));
        String reason = str(body.getOrDefault("reason", ""));
        if (toStatus.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "to_status required");
        }

        ContentEntity c = contentService.getById(id);
        if (c == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "content not found");
        }

        String fromStatus = c.getPublishStatus() == null ? "" : c.getPublishStatus().trim();
        if (fromStatus.isEmpty()) {
            return V3Response.error("STATE_INVALID_TRANSITION", "publish_status empty");
        }

        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION",
                    "invalid transition: " + fromStatus + " -> " + toStatus);
        }

        Date now = new Date();
        c.setPublishStatus(toStatus);
        c.setUpdatedAt(now);
        c.setChangedAt(now);
        c.setChangeReason(reason.isEmpty() ? null : reason);

        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId != null && userId > 0) {
            c.setChangedBy(userId);
        }

        contentService.updateById(c);

        stateTransitionService.log(
                "content", c.getId(),
                fromStatus, toStatus,
                toStatus,
                reason.isEmpty() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", c.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    @GetMapping("/stats")
    public V3Response<Map<String, Object>> stats(
            @RequestParam(name = "content_type", required = false) String contentType
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        for (String s : List.of("published", "pending_review", "approved", "rejected", "offline", "draft")) {
            QueryWrapper<ContentEntity> qw = new QueryWrapper<ContentEntity>().eq("publish_status", s);
            if (contentType != null && !contentType.isBlank()) {
                qw.eq("content_type", contentType.trim());
            }
            data.put(s, contentService.count(qw));
        }
        return V3Response.success(data);
    }

    private Map<String, Object> toItem(ContentEntity c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        // contentNo: derived from content_type prefix + id, consistent with frontend expectation
        String typePrefix = switch (safeOr(c.getContentType(), "other")) {
            case "training" -> "TRN";
            case "media" -> "MED";
            case "activity" -> "ACT";
            case "ad" -> "AD";
            default -> "CNT";
        };
        m.put("contentNo", typePrefix + "-" + String.format("%07d", c.getId()));
        m.put("title", safe(c.getTitle()));
        m.put("summary", safe(c.getSummary()));
        m.put("contentType", safeOr(c.getContentType(), "other"));
        // expose as "status" to minimise frontend field mapping changes
        m.put("status", safeOr(c.getPublishStatus(), "draft"));
        m.put("cityName", safe(c.getCityName()));
        m.put("isPaid", Boolean.TRUE.equals(c.getIsPaid()));
        m.put("fee", c.getFee() == null ? BigDecimal.ZERO : c.getFee());
        m.put("publishDate", fmtDate(c.getCreatedAt()));
        m.put("updatedAt", fmtDate(c.getUpdatedAt()));
        return m;
    }

    /**
     * State machine (matches DDL CHECK constraint on publish_status):
     * draft → pending_review
     * pending_review → published | rejected
     * approved → published
     * published → offline
     * offline → published
     */
    private boolean isAllowedTransition(String from, String to) {
        if (from.equals(to)) return false;
        return switch (from) {
            case "draft" -> "pending_review".equals(to);
            case "pending_review" -> "published".equals(to) || "rejected".equals(to) || "approved".equals(to);
            case "approved" -> "published".equals(to) || "rejected".equals(to);
            case "published" -> "offline".equals(to);
            case "offline" -> "published".equals(to);
            default -> false;
        };
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String str(String s) {
        return s == null ? "" : s.trim();
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
