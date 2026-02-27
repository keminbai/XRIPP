package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
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
@RequestMapping("/v3/admin/member-verifications")
@RequiredArgsConstructor
public class AdminMemberVerificationV3Controller {

    private final IMemberVerificationService service;
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<MemberVerification>> list(
            @RequestParam(name = "verification_status", required = false) String verificationStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        if (!com.xripp.backend.security.SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<MemberVerification> qw = new QueryWrapper<>();

        if (verificationStatus != null && !verificationStatus.isBlank()) {
            qw.eq("verification_status", verificationStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("company_name", keyword)
                    .or().like("contact_name", keyword)
                    .or().like("phone", keyword));
        }
        qw.orderByDesc("created_at");

        Page<MemberVerification> p = new Page<>(page, pageSize);
        Page<MemberVerification> result = service.page(p, qw);

        return V3Response.success(new V3PageData<>(
                result.getRecords(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        ));
    }

    @Transactional
    @PostMapping("/{id}/review")
    public V3Response<Void> review(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        if (!com.xripp.backend.security.SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String action = body.get("action"); // approve|reject
        String reason = body.getOrDefault("reason", "");

        MemberVerification mv = service.getById(id);
        if (mv == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member verification not found");
        }

        String curr = mv.getVerificationStatus();
        if (!"submitted".equals(curr) && !"under_review".equals(curr)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "invalid status transition");
        }

        if ("approve".equals(action)) {
            mv.setVerificationStatus("approved");
        } else if ("reject".equals(action)) {
            mv.setVerificationStatus("rejected");
            mv.setChangeReason(reason);
        } else {
            return V3Response.error("VALIDATION_ERROR", "action must be approve|reject");
        }

        String newStatus = mv.getVerificationStatus(); // approve 已改为 approved，reject 已改为 rejected
        mv.setReviewedAt(new Date());
        mv.setUpdatedAt(new Date());
        service.updateById(mv);

        stateTransitionService.log(
                "member_verification", mv.getId(),
                curr, newStatus,
                action,                              // "approve" 或 "reject"
                reason.isBlank() ? null : reason
        );

        return V3Response.success(null);
    }
}
