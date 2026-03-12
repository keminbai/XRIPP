package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.dto.SupplierExportDTO;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.ISupplierOnboardingService;
import com.xripp.backend.service.StateTransitionService;
import com.xripp.backend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v3/admin/supplier-onboarding")
@RequiredArgsConstructor
public class AdminSupplierOnboardingV3Controller {

    private final ISupplierOnboardingService service;
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(name = "onboarding_status", required = false) String onboardingStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();

        // Partner data scope: only see supplier onboarding under own partner_id
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null) {
                return V3Response.error("AUTH_FORBIDDEN", "invalid partner context");
            }
            qw.eq("partner_id", partnerId);
        }

        if (onboardingStatus != null && !onboardingStatus.isBlank()) {
            qw.eq("onboarding_status", onboardingStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw).or().like("city_name", kw));
        }
        qw.orderByDesc("updated_at");

        Page<SupplierOnboarding> p = new Page<>(page, pageSize);
        Page<SupplierOnboarding> result = service.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboarding so : result.getRecords()) {
            items.add(toListItem(so));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        SupplierOnboarding so = service.getById(id);
        if (so == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "supplier onboarding not found");
        }

        // Partner scope check
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (!partnerId.equals(so.getPartnerId())) {
                return V3Response.error("AUTH_FORBIDDEN", "forbidden");
            }
        }

        Map<String, Object> m = toListItem(so);
        m.put("intro", safe(so.getIntro()));
        m.put("serviceTypesJson", safe(so.getServiceTypesJson()));
        m.put("changeReason", safe(so.getChangeReason()));
        m.put("submittedAt", fmtDate(so.getSubmittedAt()));
        m.put("reviewedAt", fmtDate(so.getReviewedAt()));
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

        SupplierOnboarding so = service.getById(id);
        if (so == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "supplier onboarding not found");
        }

        String fromStatus = so.getOnboardingStatus() == null ? "" : so.getOnboardingStatus().trim();
        if (fromStatus.isEmpty()) {
            return V3Response.error("STATE_INVALID_TRANSITION", "onboarding status empty");
        }

        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION",
                    "invalid transition: " + fromStatus + " -> " + toStatus);
        }

        Date now = new Date();
        so.setOnboardingStatus(toStatus);
        so.setUpdatedAt(now);
        so.setChangedAt(now);
        so.setChangeReason(reason.isEmpty() ? null : reason);

        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId != null && userId > 0) {
            so.setChangedBy(userId);
        }

        boolean isReviewAction = toStatus.equals("precheck_passed") || toStatus.equals("precheck_failed")
                || toStatus.equals("final_review_passed") || toStatus.equals("final_review_failed");
        if (isReviewAction) {
            so.setReviewedAt(now);
        }

        service.updateById(so);

        stateTransitionService.log(
                "supplier_onboarding", so.getId(),
                fromStatus, toStatus,
                toStatus,
                reason.isEmpty() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", so.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    @GetMapping("/stats")
    public V3Response<Map<String, Long>> stats() {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Map<String, Long> data = new LinkedHashMap<>();
        for (String s : List.of("submitted", "precheck_passed", "precheck_failed",
                "final_review_passed", "final_review_failed", "active", "inactive", "draft")) {
            QueryWrapper<SupplierOnboarding> sq = new QueryWrapper<SupplierOnboarding>()
                    .eq("onboarding_status", s);
            // Partner data scope
            if (SecurityContextHolder.isPartner()) {
                Long partnerId = SecurityContextHolder.getCurrentPartnerId();
                if (partnerId != null) {
                    sq.eq("partner_id", partnerId);
                }
            }
            data.put(s, service.count(sq));
        }
        return V3Response.success(data);
    }

    private Map<String, Object> toListItem(SupplierOnboarding so) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", so.getId());
        m.put("companyName", safe(so.getCompanyName()));
        m.put("cityName", safe(so.getCityName()));
        m.put("partnerId", so.getPartnerId());
        m.put("supplierId", so.getSupplierId());
        m.put("onboardingStatus", safeOr(so.getOnboardingStatus(), "draft"));
        m.put("onboardingStatusLabel", mapStatusLabel(safeOr(so.getOnboardingStatus(), "draft")));
        m.put("createdAt", fmtDate(so.getCreatedAt()));
        m.put("updatedAt", fmtDate(so.getUpdatedAt()));
        return m;
    }

    private String mapStatusLabel(String status) {
        return switch (status) {
            case "draft" -> "草稿";
            case "submitted" -> "已提交";
            case "precheck_passed" -> "预审通过";
            case "precheck_failed" -> "预审未通过";
            case "final_review_passed" -> "终审通过";
            case "final_review_failed" -> "终审未通过";
            case "active" -> "已激活";
            case "inactive" -> "已停用";
            default -> status;
        };
    }

    private boolean isAllowedTransition(String from, String to) {
        if (from.equals(to)) return false;
        return switch (from) {
            case "draft" -> "submitted".equals(to);
            case "submitted" -> "precheck_passed".equals(to) || "precheck_failed".equals(to);
            case "precheck_passed" -> "final_review_passed".equals(to) || "final_review_failed".equals(to);
            case "final_review_passed" -> "active".equals(to);
            case "active" -> "inactive".equals(to);
            case "inactive" -> "active".equals(to);
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

    private String fmtDate(java.util.Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(name = "onboarding_status", required = false) String onboardingStatus,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response
    ) throws Exception {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            response.setStatus(403);
            return;
        }

        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null) { response.setStatus(403); return; }
            qw.eq("partner_id", partnerId);
        }
        if (onboardingStatus != null && !onboardingStatus.isBlank()) {
            qw.eq("onboarding_status", onboardingStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw).or().like("city_name", kw));
        }
        qw.orderByDesc("updated_at");

        List<SupplierOnboarding> all = service.list(qw);
        List<SupplierExportDTO> rows = new ArrayList<>();
        for (SupplierOnboarding so : all) {
            SupplierExportDTO dto = new SupplierExportDTO();
            dto.setId(so.getId());
            dto.setCompanyName(so.getCompanyName());
            dto.setCityName(so.getCityName());
            dto.setOnboardingStatus(mapStatusLabel(safeOr(so.getOnboardingStatus(), "draft")));
            dto.setSubmittedAt(ExcelExportUtil.fmtDate(so.getSubmittedAt()));
            dto.setReviewedAt(ExcelExportUtil.fmtDate(so.getReviewedAt()));
            rows.add(dto);
        }
        ExcelExportUtil.write(response, "供应商入驻", SupplierExportDTO.class, rows);
    }
}
