package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.ISupplierOnboardingService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v3/supplier-onboarding")
@RequiredArgsConstructor
public class SupplierOnboardingV3Controller {

    private final ISupplierOnboardingService service;
    private final StateTransitionService stateTransitionService;

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Long partnerId = SecurityContextHolder.getCurrentUserId();
        if (partnerId == null || partnerId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        String companyName = str(body.get("company_name"));
        if (companyName.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "company_name required");
        }

        SupplierOnboarding so = new SupplierOnboarding();
        so.setPartnerId(partnerId);
        so.setCompanyName(companyName);
        so.setCityName(str(body.get("city_name")));
        so.setServiceTypesJson(str(body.get("service_types_json")));
        so.setIntro(str(body.get("intro")));
        so.setOnboardingStatus("submitted");

        Date now = new Date();
        so.setSubmittedAt(now);
        so.setCreatedAt(now);
        so.setUpdatedAt(now);
        so.setChangedAt(now);
        so.setChangedBy(partnerId);

        service.save(so);

        stateTransitionService.log(
                "supplier_onboarding", so.getId(),
                null, "submitted",
                "submit", null
        );

        return V3Response.success(Map.of(
                "id", so.getId(),
                "onboarding_status", so.getOnboardingStatus()
        ));
    }

    @GetMapping
    public V3Response<List<Map<String, Object>>> myList() {
        Long partnerId = SecurityContextHolder.getCurrentUserId();
        if (partnerId == null || partnerId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        List<SupplierOnboarding> list = service.list(
                new QueryWrapper<SupplierOnboarding>()
                        .eq("partner_id", partnerId)
                        .orderByDesc("created_at")
        );

        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboarding so : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", so.getId());
            m.put("companyName", safe(so.getCompanyName()));
            m.put("onboardingStatus", safeOr(so.getOnboardingStatus(), "draft"));
            m.put("submittedAt", fmtDate(so.getSubmittedAt()));
            m.put("reviewedAt", fmtDate(so.getReviewedAt()));
            m.put("changeReason", safe(so.getChangeReason()));
            items.add(m);
        }

        return V3Response.success(items);
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
