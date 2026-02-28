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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/activities")
@RequiredArgsConstructor
public class ActivitiesV3Controller {

    private final IActivityService activityService;
    private final IActivityRegistrationService activityRegistrationService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "is_paid", required = false) Boolean isPaid
    ) {
        QueryWrapper<Activity> qw = new QueryWrapper<>();
        // 只返回审核通过（audit_status=30 APPROVED）的活动
        qw.eq("audit_status", 30);
        if (isPaid != null) {
            qw.eq("is_free", !isPaid);
        }
        qw.orderByDesc("start_time");

        Page<Activity> p = new Page<>(page, pageSize);
        Page<Activity> result = activityService.page(p, qw);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        Activity act = activityService.getById(id);
        if (act == null) return V3Response.error("RESOURCE_NOT_FOUND", "activity not found");
        Map<String, Object> m = toItem(act);
        m.put("description", act.getSummary() == null ? "" : act.getSummary());
        return V3Response.success(m);
    }

    private Map<String, Object> toItem(Activity act) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", act.getId());
        m.put("title", act.getTitle() == null ? "" : act.getTitle());
        m.put("type", act.getActivityType() == null || act.getActivityType().isBlank()
                ? "活动" : act.getActivityType());
        m.put("date", formatDate(act.getStartTime()));
        m.put("location", act.getCityName() == null ? "" : act.getCityName());
        m.put("image", act.getImageUrl() == null || act.getImageUrl().isBlank()
                ? "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800"
                : act.getImageUrl());
        boolean isFree = Boolean.TRUE.equals(act.getIsFree());
        m.put("isPaid", !isFree);
        BigDecimal fee = act.getFee();
        m.put("price", fee == null ? 0 : fee.intValue());
        m.put("auditStatus", act.getAuditStatus());
        return m;
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
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
