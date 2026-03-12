package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.dto.TenderExportDTO;
import com.xripp.backend.entity.TenderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.ITenderService;
import com.xripp.backend.service.StateTransitionService;
import com.xripp.backend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/tenders")
@RequiredArgsConstructor
public class AdminTendersV3Controller {

    private final ITenderService tenderService;
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "tender_status", required = false) String tenderStatus,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<TenderEntity> qw = new QueryWrapper<>();
        if (tenderStatus != null && !tenderStatus.isBlank()) {
            qw.eq("tender_status", tenderStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw).or().like("tender_no", kw));
        }
        qw.orderByDesc("updated_at");

        Page<TenderEntity> p = new Page<>(page, pageSize);
        Page<TenderEntity> result = tenderService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (TenderEntity t : result.getRecords()) {
            items.add(toListItem(t));
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

        TenderEntity t = tenderService.getById(id);
        if (t == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("tenderNo", t.getTenderNo());
        m.put("title", t.getTitle());
        m.put("description", safe(t.getSummary()));
        m.put("organization", safe(t.getOrganizationName()));
        m.put("category", safeOr(t.getCategory(), "other"));
        m.put("categoryLabel", mapCategoryLabel(safeOr(t.getCategory(), "other")));
        m.put("price", t.getPrice() == null ? BigDecimal.ZERO : t.getPrice());
        m.put("sales", t.getSales() == null ? 0 : t.getSales());
        m.put("publishDate", formatDate(t.getCreatedAt()));
        m.put("deadline", formatDateTime(t.getDeadlineAt()));
        m.put("status", safeOr(t.getTenderStatus(), "draft"));
        m.put("isTop", Boolean.TRUE.equals(t.getIsTop()));
        return V3Response.success(m);
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String title = str(body.get("title"));
        String organization = str(body.get("organization"));
        if (title.isBlank() || organization.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "title/organization required");
        }

        String category = strOr(body.get("category"), "other");
        String tenderStatus = strOr(body.get("tender_status"), "draft");
        if (!Set.of("draft", "published", "archived").contains(tenderStatus)) {
            return V3Response.error("VALIDATION_ERROR", "invalid tender_status");
        }

        TenderEntity t = new TenderEntity();
        String tenderNo = str(body.get("tender_no"));
        t.setTenderNo(tenderNo.isBlank() ? genTenderNo(organization, category) : tenderNo);

        t.setTitle(title);
        t.setOrganizationName(organization);
        t.setCategory(category);
        t.setSummary(str(body.get("description")));
        t.setPrice(parsePrice(body.get("price")));
        t.setTenderStatus(tenderStatus);
        t.setSales(0);
        t.setIsTop(Boolean.TRUE.equals(body.get("is_top")));
        t.setDeadlineAt(parseDate(body.get("deadline")));

        Date now = new Date();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        t.setChangedAt(now);

        Long userId = SecurityContextHolder.getCurrentUserId();
        t.setCreatedBy(userId);
        t.setChangedBy(userId);

        tenderService.save(t);

        return V3Response.success(Map.of(
                "id", t.getId(),
                "tenderNo", t.getTenderNo(),
                "status", t.getTenderStatus()
        ));
    }

    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        TenderEntity t = tenderService.getById(id);
        if (t == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        if (body.containsKey("title")) {
            String title = str(body.get("title"));
            if (title.isBlank()) {
                return V3Response.error("VALIDATION_ERROR", "title required");
            }
            t.setTitle(title);
        }
        if (body.containsKey("organization")) {
            String organization = str(body.get("organization"));
            if (organization.isBlank()) {
                return V3Response.error("VALIDATION_ERROR", "organization required");
            }
            t.setOrganizationName(organization);
        }
        if (body.containsKey("category")) {
            t.setCategory(strOr(body.get("category"), "other"));
        }
        if (body.containsKey("description")) {
            t.setSummary(str(body.get("description")));
        }
        if (body.containsKey("price")) {
            t.setPrice(parsePrice(body.get("price")));
        }
        if (body.containsKey("is_top")) {
            t.setIsTop(Boolean.TRUE.equals(body.get("is_top")));
        }
        if (body.containsKey("deadline")) {
            t.setDeadlineAt(parseDate(body.get("deadline")));
        }
        if (body.containsKey("tender_no")) {
            String tenderNo = str(body.get("tender_no"));
            if (!tenderNo.isBlank()) {
                t.setTenderNo(tenderNo);
            }
        }
        if (body.containsKey("tender_status")) {
            String status = str(body.get("tender_status"));
            if (!Set.of("draft", "published", "archived").contains(status)) {
                return V3Response.error("VALIDATION_ERROR", "invalid tender_status");
            }
            String currentStatus = safeOr(t.getTenderStatus(), "draft");
            if (!currentStatus.equals(status) && !isAllowedTransition(currentStatus, status)) {
                return V3Response.error("STATE_INVALID_TRANSITION",
                        "cannot transition from " + currentStatus + " to " + status);
            }
            t.setTenderStatus(status);
        }

        t.setUpdatedAt(new Date());
        t.setChangedAt(new Date());
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId != null) {
            t.setChangedBy(userId);
        }

        tenderService.updateById(t);

        return V3Response.success(Map.of(
                "id", t.getId(),
                "status", safeOr(t.getTenderStatus(), "draft")
        ));
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

        String toStatus = String.valueOf(body.getOrDefault("to_status", "")).trim();
        String reason = String.valueOf(body.getOrDefault("reason", "")).trim();
        if (toStatus.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "to_status required");
        }

        TenderEntity tender = tenderService.getById(id);
        if (tender == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        String fromStatus = tender.getTenderStatus() == null ? "" : tender.getTenderStatus().trim();
        if (fromStatus.isEmpty()) {
            return V3Response.error("STATE_INVALID_TRANSITION", "tender status empty");
        }

        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "invalid status transition");
        }

        tender.setTenderStatus(toStatus);
        tender.setUpdatedAt(new Date());
        tender.setChangedAt(new Date());
        tender.setChangeReason(reason);

        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId != null && userId > 0) {
            tender.setChangedBy(userId);
        }

        tenderService.updateById(tender);

        stateTransitionService.log(
                "tender", tender.getId(),
                fromStatus, toStatus,
                toStatus,
                reason.isEmpty() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", tender.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    @GetMapping("/stats")
    public V3Response<Map<String, Long>> stats() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Map<String, Long> data = new HashMap<>();
        data.put("published", tenderService.count(new QueryWrapper<TenderEntity>().eq("tender_status", "published")));
        data.put("draft", tenderService.count(new QueryWrapper<TenderEntity>().eq("tender_status", "draft")));
        data.put("archived", tenderService.count(new QueryWrapper<TenderEntity>().eq("tender_status", "archived")));
        return V3Response.success(data);
    }

    private Map<String, Object> toListItem(TenderEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("tenderNo", t.getTenderNo());
        m.put("title", t.getTitle());
        m.put("description", safe(t.getSummary()));
        m.put("organization", safeOr(t.getOrganizationName(), "-"));

        String category = safeOr(t.getCategory(), "other");
        m.put("category", category);
        m.put("categoryLabel", mapCategoryLabel(category));

        m.put("price", t.getPrice() == null ? BigDecimal.ZERO : t.getPrice());
        m.put("sales", t.getSales() == null ? 0 : t.getSales());
        m.put("publishDate", formatDate(t.getCreatedAt()));
        m.put("status", safeOr(t.getTenderStatus(), "draft"));
        m.put("isTop", Boolean.TRUE.equals(t.getIsTop()));
        return m;
    }

    private String mapCategoryLabel(String category) {
        return switch (category) {
            case "medical" -> "医疗器械";
            case "it" -> "IT设备";
            case "construction" -> "建筑工程";
            case "office" -> "办公用品";
            case "consulting" -> "咨询服务";
            default -> "未分类";
        };
    }

    private boolean isAllowedTransition(String from, String to) {
        if (from.equals(to)) return false;
        return switch (from) {
            case "draft" -> "published".equals(to) || "archived".equals(to);
            case "published" -> "archived".equals(to);
            case "archived" -> false;
            default -> false;
        };
    }

    private String genTenderNo(String organization, String category) {
        String orgCode = (organization == null || organization.isBlank()) ? "ORG" : organization.trim();
        String prefix = switch (category == null ? "" : category.trim()) {
            case "medical" -> "A";
            case "it" -> "B";
            case "construction" -> "C";
            case "office" -> "D";
            case "consulting" -> "E";
            default -> "X";
        };
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String rand = String.format("%03d", new Random().nextInt(1000));
        return orgCode + "-" + prefix + date + "-" + rand;
    }

    private BigDecimal parsePrice(Object raw) {
        if (raw == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(String.valueOf(raw));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Date parseDate(Object raw) {
        if (raw == null) return null;
        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) return null;

        try {
            if (s.endsWith("Z") || s.contains("+")) {
                return Date.from(OffsetDateTime.parse(s).toInstant());
            }
        } catch (Exception ignored) {}

        try {
            return Date.from(Instant.parse(s));
        } catch (Exception ignored) {}

        List<DateTimeFormatter> patterns = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter f : patterns) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(s, f);
                return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private String strOr(Object o, String dft) {
        String s = str(o);
        return s.isBlank() ? dft : s;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(name = "tender_status", required = false) String tenderStatus,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response
    ) throws Exception {
        if (!SecurityContextHolder.isAdmin()) {
            response.setStatus(403);
            return;
        }

        QueryWrapper<TenderEntity> qw = new QueryWrapper<>();
        if (tenderStatus != null && !tenderStatus.isBlank()) {
            qw.eq("tender_status", tenderStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw).or().like("tender_no", kw));
        }
        qw.orderByDesc("updated_at");

        List<TenderEntity> all = tenderService.list(qw);
        List<TenderExportDTO> rows = new ArrayList<>();
        for (TenderEntity t : all) {
            TenderExportDTO dto = new TenderExportDTO();
            dto.setTenderNo(t.getTenderNo());
            dto.setTitle(t.getTitle());
            dto.setOrganizationName(t.getOrganizationName());
            dto.setCountry(t.getCountry());
            dto.setCategory(mapCategoryLabel(safeOr(t.getCategory(), "other")));
            dto.setPrice(t.getPrice() == null ? "0" : t.getPrice().toPlainString());
            dto.setTenderStatus(t.getTenderStatus());
            dto.setDeadlineAt(formatDateTime(t.getDeadlineAt()));
            dto.setCreatedAt(formatDate(t.getCreatedAt()));
            rows.add(dto);
        }
        ExcelExportUtil.write(response, "标书列表", TenderExportDTO.class, rows);
    }

    private String formatDateTime(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
