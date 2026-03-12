package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.ContentEntity;
import com.xripp.backend.service.IContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Public read-only contents API — returns only published content.
 * Used by portal pages: homepage carousel, media articles, news list.
 */
@RestController
@RequestMapping("/v3/contents")
@RequiredArgsConstructor
public class ContentsV3Controller {

    private final IContentService contentService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "content_type", required = false) String contentType,
            @RequestParam(required = false) String keyword
    ) {
        QueryWrapper<ContentEntity> qw = new QueryWrapper<>();
        // Public API only returns published content
        qw.eq("publish_status", "published");

        if (contentType != null && !contentType.isBlank()) {
            qw.eq("content_type", contentType.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw).or().like("summary", kw));
        }
        qw.orderByDesc("created_at");

        Page<ContentEntity> p = new Page<>(page, pageSize);
        Page<ContentEntity> result = contentService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (ContentEntity c : result.getRecords()) {
            items.add(toPublicItem(c));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        ContentEntity c = contentService.getById(id);
        if (c == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "content not found");
        }
        // Only return published content via public API
        if (!"published".equals(c.getPublishStatus())) {
            return V3Response.error("RESOURCE_NOT_FOUND", "content not found");
        }

        Map<String, Object> m = toPublicItem(c);
        m.put("body", c.getBody() == null ? "" : c.getBody());
        return V3Response.success(m);
    }

    private Map<String, Object> toPublicItem(ContentEntity c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("title", safe(c.getTitle()));
        m.put("summary", safe(c.getSummary()));
        m.put("contentType", safeOr(c.getContentType(), "other"));
        m.put("cityName", safe(c.getCityName()));
        m.put("isPaid", Boolean.TRUE.equals(c.getIsPaid()));
        m.put("fee", c.getFee() == null ? java.math.BigDecimal.ZERO : c.getFee());
        m.put("publishDate", fmtDate(c.getCreatedAt()));
        m.put("updatedAt", fmtDate(c.getUpdatedAt()));
        return m;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String fmtDate(Date date) {
        if (date == null) return "";
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FMT);
    }
}
