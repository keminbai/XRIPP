package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.ActivityDisplayApplication;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IActivityDisplayApplicationService;
import com.xripp.backend.service.IActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/activity-display-applications")
@RequiredArgsConstructor
public class AdminActivityDisplayApplicationsV3Controller {

    private final IActivityDisplayApplicationService applicationService;
    private final IActivityService activityService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "apply_status", required = false) String applyStatus,
            @RequestParam(name = "display_area", required = false) String displayArea,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<ActivityDisplayApplication> qw = new QueryWrapper<>();
        if (applyStatus != null && !applyStatus.isBlank()) {
            qw.eq("apply_status", applyStatus.trim());
        }
        if (displayArea != null && !displayArea.isBlank()) {
            qw.eq("display_area", displayArea.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            List<Long> matchedActivityIds = activityService.list(new QueryWrapper<Activity>()
                            .select("id")
                            .and(w -> w.like("title", keyword.trim())
                                    .or().like("activity_no", keyword.trim())
                                    .or().like("city_name", keyword.trim())))
                    .stream()
                    .map(Activity::getId)
                    .filter(Objects::nonNull)
                    .toList();
            if (matchedActivityIds.isEmpty()) {
                return V3Response.success(new V3PageData<>(List.of(), page, pageSize, 0));
            }
            qw.in("activity_id", matchedActivityIds);
        }
        qw.orderByDesc("created_at");

        Page<ActivityDisplayApplication> p = new Page<>(page, pageSize);
        Page<ActivityDisplayApplication> result = applicationService.page(p, qw);

        Map<Long, Activity> activityMap = loadActivityMap(result.getRecords().stream()
                .map(ActivityDisplayApplication::getActivityId)
                .filter(Objects::nonNull)
                .toList());

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(app -> toItem(app, activityMap.get(app.getActivityId())))
                .toList();

        return V3Response.success(new V3PageData<>(items, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @Transactional
    @PostMapping("/{id}/transition")
    public V3Response<Map<String, Object>> transition(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        ActivityDisplayApplication application = applicationService.getById(id);
        if (application == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "display application not found");
        }

        String toStatus = str(body.get("to_status"));
        if (toStatus.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "to_status required");
        }
        String fromStatus = str(application.getApplyStatus());
        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "invalid display application transition");
        }

        if ("approved".equals(toStatus) && !"approved".equals(fromStatus)) {
            long activeApprovedCount = applicationService.count(new QueryWrapper<ActivityDisplayApplication>()
                    .eq("display_area", application.getDisplayArea())
                    .eq("apply_status", "approved")
                    .ge("display_end_at", new Date())
                    .ne("id", application.getId()));
            if (activeApprovedCount >= 10) {
                return V3Response.error("VALIDATION_ERROR", "display area already has 10 approved applications");
            }
        }

        Date now = new Date();
        application.setApplyStatus(toStatus);
        application.setUpdatedAt(now);
        application.setReviewedAt(now);
        application.setReviewedBy(SecurityContextHolder.getCurrentUserId());
        application.setChangeReason(str(body.get("reason")));
        if (body.containsKey("sort_order")) {
            application.setSortOrder(parseInt(body.get("sort_order"), application.getSortOrder() == null ? 0 : application.getSortOrder()));
        }
        applicationService.updateById(application);

        return V3Response.success(Map.of(
                "id", application.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    private Map<Long, Activity> loadActivityMap(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return new HashMap<>();
        }
        return activityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, Function.identity(), (a, b) -> a));
    }

    private Map<String, Object> toItem(ActivityDisplayApplication application, Activity activity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", application.getId());
        item.put("activity_id", application.getActivityId());
        item.put("activityId", application.getActivityId());
        item.put("display_area", safe(application.getDisplayArea()));
        item.put("displayArea", safe(application.getDisplayArea()));
        item.put("display_area_label", displayAreaLabel(application.getDisplayArea()));
        item.put("apply_status", safe(application.getApplyStatus()));
        item.put("applyStatus", safe(application.getApplyStatus()));
        item.put("apply_status_label", statusLabel(application.getApplyStatus()));
        item.put("sort_order", application.getSortOrder() == null ? 0 : application.getSortOrder());
        item.put("display_start_at", application.getDisplayStartAt());
        item.put("displayStartAt", fmtDateTime(application.getDisplayStartAt()));
        item.put("display_end_at", application.getDisplayEndAt());
        item.put("displayEndAt", fmtDateTime(application.getDisplayEndAt()));
        item.put("change_reason", safe(application.getChangeReason()));
        item.put("changeReason", safe(application.getChangeReason()));
        item.put("created_at", application.getCreatedAt());
        item.put("createdAt", fmtDateTime(application.getCreatedAt()));
        item.put("reviewed_at", application.getReviewedAt());
        item.put("reviewedAt", fmtDateTime(application.getReviewedAt()));
        if (activity != null) {
            item.put("title", safe(activity.getTitle()));
            item.put("activity_no", safe(activity.getActivityNo()));
            item.put("city_name", safe(activity.getCityName()));
            item.put("cover_image", safe(activity.getImageUrl()));
            item.put("summary", safe(activity.getSummary()));
            item.put("publisher", activity.getPartnerId() == null || activity.getPartnerId() <= 0 ? "总部" : "合伙人");
        } else {
            item.put("title", "");
            item.put("activity_no", "");
            item.put("city_name", "");
            item.put("cover_image", "");
            item.put("summary", "");
            item.put("publisher", "");
        }
        return item;
    }

    private boolean isAllowedTransition(String fromStatus, String toStatus) {
        return switch (safe(fromStatus)) {
            case "pending_review" -> Set.of("approved", "rejected").contains(toStatus);
            case "approved" -> Set.of("offline").contains(toStatus);
            case "rejected", "offline" -> Set.of("approved").contains(toStatus);
            default -> false;
        };
    }

    private String displayAreaLabel(String area) {
        return switch (safe(area)) {
            case "home" -> "首页推荐";
            case "activity" -> "活动中心推荐";
            case "service" -> "平台服务推荐";
            default -> safe(area);
        };
    }

    private String statusLabel(String status) {
        return switch (safe(status)) {
            case "pending_review" -> "待审核";
            case "approved" -> "已通过";
            case "rejected" -> "已驳回";
            case "offline" -> "已下线";
            default -> safe(status);
        };
    }

    private String fmtDateTime(Date value) {
        if (value == null) {
            return "";
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int parseInt(Object value, int dft) {
        try {
            if (value == null || String.valueOf(value).isBlank()) {
                return dft;
            }
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return dft;
        }
    }
}
