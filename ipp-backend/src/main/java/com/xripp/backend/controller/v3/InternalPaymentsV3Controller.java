package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.service.IActivityRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentsV3Controller {

    private final IActivityRegistrationService activityRegistrationService;

    @PostMapping("/callback")
    public V3Response<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        Long registrationId = body.get("registration_id") == null
                ? null
                : Long.valueOf(String.valueOf(body.get("registration_id")));
        String payStatus = String.valueOf(body.getOrDefault("pay_status", ""));

        if (registrationId == null || registrationId <= 0) {
            return V3Response.error("VALIDATION_ERROR", "registration_id required");
        }
        if (!"success".equals(payStatus)) {
            return V3Response.error("VALIDATION_ERROR", "pay_status must be success");
        }

        ActivityRegistration reg = activityRegistrationService.getById(registrationId);
        if (reg == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "registration not found");
        }

        // 与当前数据库 CHECK 约束保持一致
        if (!"pending_payment".equals(reg.getRegistrationStatus())) {
            return V3Response.error("STATE_INVALID_TRANSITION", "registration is not pending_payment");
        }

        reg.setRegistrationStatus("confirmed");
        reg.setPaidAt(new Date());
        reg.setUpdatedAt(new Date());
        activityRegistrationService.updateById(reg);

        Map<String, Object> data = new HashMap<>();
        data.put("registration_id", reg.getId());
        data.put("registration_status", reg.getRegistrationStatus());

        return V3Response.success(data);
    }
}
