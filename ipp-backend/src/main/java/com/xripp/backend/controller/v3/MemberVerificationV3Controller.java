package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.MemberVerification;
import com.xripp.backend.service.IMemberVerificationService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/v3/member-verifications")
@RequiredArgsConstructor
public class MemberVerificationV3Controller {

    private final IMemberVerificationService service;
    private final StateTransitionService stateTransitionService;

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String companyName = String.valueOf(body.getOrDefault("company_name", "")).trim();
        String contactName = String.valueOf(body.getOrDefault("contact_name", "")).trim();
        String phone = String.valueOf(body.getOrDefault("phone", "")).trim();
        String industry = String.valueOf(body.getOrDefault("industry", "")).trim();

        if (companyName.isEmpty() || contactName.isEmpty() || phone.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "company_name/contact_name/phone required");
        }

        MemberVerification mv = new MemberVerification();
        Long currentUserId = com.xripp.backend.security.SecurityContextHolder.getCurrentUserId();
        if (currentUserId == null || currentUserId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }
        mv.setUserId(currentUserId);
        mv.setCompanyName(companyName);
        mv.setContactName(contactName);
        mv.setPhone(phone);
        mv.setIndustry(industry);
        mv.setVerificationStatus("submitted");
        mv.setSubmittedAt(new Date());
        mv.setCreatedAt(new Date());
        mv.setUpdatedAt(new Date());

        service.save(mv);

        stateTransitionService.log(
                "member_verification", mv.getId(),
                null, "submitted",
                "submit", null
        );

        return V3Response.success(Map.of(
                "id", mv.getId(),
                "verification_status", mv.getVerificationStatus()
        ));
    }
}
