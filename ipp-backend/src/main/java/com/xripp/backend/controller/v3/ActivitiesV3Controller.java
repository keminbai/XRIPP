package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.service.IActivityRegistrationService;
import com.xripp.backend.service.IActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/activities")
@RequiredArgsConstructor
public class ActivitiesV3Controller {

    private final IActivityService activityService;
    private final IActivityRegistrationService activityRegistrationService;

    @GetMapping
    public V3Response<V3PageData<Activity>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "is_paid", required = false) Boolean isPaid
    ) {
        QueryWrapper<Activity> qw = new QueryWrapper<>();
        if (isPaid != null) {
            qw.eq("is_free", !isPaid);
        }
        qw.orderByDesc("created_at");

        Page<Activity> p = new Page<>(page, pageSize);
        Page<Activity> result = activityService.page(p, qw);

        return V3Response.success(new V3PageData<>(
                result.getRecords(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Activity> detail(@PathVariable Long id) {
        Activity act = activityService.getById(id);
        if (act == null) return V3Response.error("RESOURCE_NOT_FOUND", "activity not found");
        return V3Response.success(act);
    }

    @PostMapping("/{id}/registrations")
    public V3Response<Map<String, Object>> register(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Activity act = activityService.getById(id);
        if (act == null) return V3Response.error("RESOURCE_NOT_FOUND", "activity not found");

        Long userId = com.xripp.backend.security.SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        // 与当前数据库 CHECK 约束保持一致
        String status = Boolean.TRUE.equals(act.getIsFree()) ? "confirmed" : "pending_payment";

        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(id);
        reg.setUserId(userId);
        reg.setCompanyName(String.valueOf(body.getOrDefault("company_name", "")));
        reg.setContactName(String.valueOf(body.getOrDefault("contact_name", "")));
        reg.setPhone(String.valueOf(body.getOrDefault("phone", "")));
        reg.setRegistrationStatus(status);
        reg.setAmount(act.getFee() == null ? BigDecimal.ZERO : act.getFee());
        reg.setCreatedAt(new Date());
        reg.setUpdatedAt(new Date());

        activityRegistrationService.save(reg);

        Map<String, Object> data = new HashMap<>();
        data.put("registration_id", reg.getId());
        data.put("registration_status", status);
        data.put("amount", reg.getAmount());

        return V3Response.success(data);
    }
}
