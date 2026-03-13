package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.ActivityDisplayApplication;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.service.IActivityDisplayApplicationService;
import com.xripp.backend.service.IActivityRegistrationService;
import com.xripp.backend.service.IActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/activities")
@RequiredArgsConstructor
public class ActivitiesV3Controller {

    private final IActivityService activityService;
    private final IActivityDisplayApplicationService activityDisplayApplicationService;
    private final IActivityRegistrationService activityRegistrationService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "is_paid", required = false) Boolean isPaid,
            @RequestParam(name = "display_area", required = false) String displayArea
    ) {
        if (displayArea != null && !displayArea.isBlank()) {
            return listByDisplayArea(page, pageSize, isPaid, displayArea.trim());
        }

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
        String typeCode = act.getActivityType() == null ? "" : act.getActivityType().trim();
        String location = act.getCityName() == null ? "" : act.getCityName();
        String image = act.getImageUrl() == null || act.getImageUrl().isBlank()
                ? "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800"
                : act.getImageUrl();
        m.put("id", act.getId());
        m.put("title", act.getTitle() == null ? "" : act.getTitle());
        m.put("type", activityTypeLabel(typeCode));
        m.put("subType", typeCode);
        m.put("date", formatDate(act.getStartTime()));
        m.put("startTime", formatDateTime(act.getStartTime()));
        m.put("location", location);
        m.put("cityName", location);
        m.put("image", image);
        m.put("coverImage", image);
        boolean isFree = Boolean.TRUE.equals(act.getIsFree());
        BigDecimal fee = act.getFee();
        long signupCount = countSignups(act.getId());
        m.put("isPaid", !isFree);
        m.put("isFree", isFree);
        m.put("price", fee == null ? 0 : fee.intValue());
        m.put("fee", fee == null ? BigDecimal.ZERO : fee);
        m.put("summary", act.getSummary() == null ? "" : act.getSummary());
        m.put("description", act.getSummary() == null ? "" : act.getSummary());
        m.put("agenda", act.getAgenda() == null ? "" : act.getAgenda());
        m.put("videoUrl", act.getVideoUrl() == null ? "" : act.getVideoUrl());
        m.put("hasVideo", act.getVideoUrl() != null && !act.getVideoUrl().isBlank());
        m.put("maxLimit", act.getMaxLimit() == null ? 0 : act.getMaxLimit());
        m.put("signupCount", signupCount > 0 ? signupCount : (act.getCurrentParticipants() == null ? 0 : act.getCurrentParticipants()));
        m.put("auditStatus", act.getAuditStatus());
        return m;
    }

    private V3Response<V3PageData<Map<String, Object>>> listByDisplayArea(
            long page,
            long pageSize,
            Boolean isPaid,
            String displayArea
    ) {
        if (!Set.of("home", "activity", "service").contains(displayArea)) {
            return V3Response.error("VALIDATION_ERROR", "display_area must be home|activity|service");
        }

        Date now = new Date();
        QueryWrapper<ActivityDisplayApplication> qw = new QueryWrapper<ActivityDisplayApplication>()
                .eq("display_area", displayArea)
                .eq("apply_status", "approved")
                .le("display_start_at", now)
                .ge("display_end_at", now)
                .orderByAsc("sort_order")
                .orderByDesc("created_at");
        Page<ActivityDisplayApplication> p = new Page<>(page, pageSize);
        Page<ActivityDisplayApplication> appPage = activityDisplayApplicationService.page(p, qw);

        List<Long> activityIds = appPage.getRecords().stream()
                .map(ActivityDisplayApplication::getActivityId)
                .filter(Objects::nonNull)
                .toList();
        if (activityIds.isEmpty()) {
            return V3Response.success(new V3PageData<>(List.of(), appPage.getCurrent(), appPage.getSize(), appPage.getTotal()));
        }

        Map<Long, Activity> activityMap = activityService.listByIds(activityIds).stream()
                .filter(act -> Objects.equals(act.getAuditStatus(), 30))
                .filter(act -> isPaid == null || (Boolean.TRUE.equals(act.getIsFree()) != isPaid))
                .collect(Collectors.toMap(Activity::getId, act -> act, (a, b) -> a));

        List<Map<String, Object>> items = new ArrayList<>();
        for (ActivityDisplayApplication app : appPage.getRecords()) {
            Activity activity = activityMap.get(app.getActivityId());
            if (activity == null) {
                continue;
            }
            Map<String, Object> item = toItem(activity);
            item.put("displayArea", displayArea);
            item.put("displayStartAt", formatDateTime(app.getDisplayStartAt()));
            item.put("displayEndAt", formatDateTime(app.getDisplayEndAt()));
            item.put("sortOrder", app.getSortOrder() == null ? 0 : app.getSortOrder());
            items.add(item);
        }

        return V3Response.success(new V3PageData<>(items, appPage.getCurrent(), appPage.getSize(), appPage.getTotal()));
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String formatDateTime(Date date) {
        if (date == null) return "";
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String activityTypeLabel(String code) {
        return switch (code) {
            case "hengjia" -> "亨嘉之会";
            case "charity" -> "公益行";
            case "inspection" -> "出海考察";
            case "salon" -> "行业沙龙";
            default -> "活动";
        };
    }

    private long countSignups(Long activityId) {
        if (activityId == null || activityId <= 0) {
            return 0;
        }
        return activityRegistrationService.count(new QueryWrapper<ActivityRegistration>().eq("activity_id", activityId));
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
        act.setCurrentParticipants((act.getCurrentParticipants() == null ? 0 : act.getCurrentParticipants()) + 1);
        act.setUpdatedAt(new Date());
        activityService.updateById(act);

        Map<String, Object> data = new HashMap<>();
        data.put("registration_id", reg.getId());
        data.put("registration_status", status);
        data.put("amount", reg.getAmount());

        return V3Response.success(data);
    }
}
