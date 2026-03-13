package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.dto.ActivitySignupExportDTO;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.ActivityDisplayApplication;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.entity.AuditLog;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IActivityDisplayApplicationService;
import com.xripp.backend.service.IActivityRegistrationService;
import com.xripp.backend.service.IAuditLogService;
import com.xripp.backend.service.IActivityService;
import com.xripp.backend.service.StateTransitionService;
import com.xripp.backend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/v3/partner/publishes")
@RequiredArgsConstructor
public class PartnerPublishesV3Controller {

    private static final int AUDIT_PENDING = 0;
    private static final int AUDIT_APPROVED = 30;
    private static final int AUDIT_REJECTED = 40;
    private static final int AUDIT_OFFLINE = 50;

    private final IActivityService activityService;
    private final IActivityDisplayApplicationService activityDisplayApplicationService;
    private final IActivityRegistrationService activityRegistrationService;
    private final StateTransitionService stateTransitionService;
    private final IAuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "audit_status", required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String publisher
    ) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<Activity> qw = new QueryWrapper<>();
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null || partnerId <= 0) {
                return V3Response.error("AUTH_FORBIDDEN", "invalid partner");
            }
            qw.eq("partner_id", partnerId);
        }
        if (auditStatus != null) {
            qw.eq("audit_status", auditStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw)
                    .or().like("activity_no", kw)
                    .or().like("summary", kw));
        }
        if ("总部".equals(publisher)) {
            qw.and(w -> w.isNull("partner_id").or().eq("partner_id", 0));
        } else if ("合伙人".equals(publisher)) {
            qw.isNotNull("partner_id").gt("partner_id", 0);
        }
        qw.orderByDesc("created_at");

        Page<Activity> p = new Page<>(page, pageSize);
        Page<Activity> result = activityService.page(p, qw);
        Map<Long, Long> signupCounts = loadSignupCounts(
                result.getRecords().stream()
                        .map(Activity::getId)
                        .filter(Objects::nonNull)
                        .toList()
        );

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(item -> toPublishItem(item, signupCounts.getOrDefault(item.getId(), 0L)))
                .toList();

        return V3Response.success(new V3PageData<>(
                items,
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Map<String, Object> item = toPublishItem(activity, loadSignupCounts(List.of(id)).getOrDefault(id, 0L));
        item.put("display_applications", loadDisplayApplications(id));
        return V3Response.success(item);
    }

    @GetMapping("/{id}/registrations")
    public V3Response<Map<String, Object>> registrations(@PathVariable Long id) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        List<ActivityRegistration> registrations = activityRegistrationService.list(
                new QueryWrapper<ActivityRegistration>()
                        .eq("activity_id", id)
                        .orderByDesc("created_at")
                        .orderByDesc("id")
        );

        List<Map<String, Object>> items = registrations.stream()
                .map(this::toRegistrationItem)
                .toList();

        return V3Response.success(Map.of(
                "activity_id", id,
                "items", items,
                "total", items.size()
        ));
    }

    @GetMapping("/{id}/registrations/export")
    public void exportRegistrations(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws Exception {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            response.setStatus(403);
            return;
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            response.setStatus(404);
            return;
        }
        if (!canAccessActivity(activity)) {
            response.setStatus(403);
            return;
        }

        List<ActivityRegistration> registrations = activityRegistrationService.list(
                new QueryWrapper<ActivityRegistration>()
                        .eq("activity_id", id)
                        .orderByDesc("created_at")
                        .orderByDesc("id")
        );

        List<ActivitySignupExportDTO> rows = new ArrayList<>();
        for (ActivityRegistration registration : registrations) {
            ActivitySignupExportDTO dto = new ActivitySignupExportDTO();
            dto.setId(registration.getId());
            dto.setCompanyName(safeOr(registration.getCompanyName(), "-"));
            dto.setContactName(safeOr(registration.getContactName(), "-"));
            dto.setPhone(safeOr(registration.getPhone(), "-"));
            dto.setRegistrationStatus(registrationStatusLabel(registration.getRegistrationStatus()));
            dto.setPaymentStatus(Boolean.TRUE.equals(isPaidRegistration(registration)) ? "已支付" : "待支付");
            dto.setCreatedAt(fmtDateTime(registration.getCreatedAt()));
            dto.setPaidAt(fmtDateTime(registration.getPaidAt()));
            rows.add(dto);
        }

        ExcelExportUtil.write(response, safeOr(activity.getTitle(), "活动报名名单"), ActivitySignupExportDTO.class, rows);
    }

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String title = requiredText(body.get("title"));
        if (title == null) {
            return V3Response.error("VALIDATION_ERROR", "title required");
        }

        Long partnerId = SecurityContextHolder.getCurrentPartnerId();
        if (SecurityContextHolder.isPartner() && (partnerId == null || partnerId <= 0)) {
            return V3Response.error("AUTH_FORBIDDEN", "invalid partner");
        }

        Activity activity = new Activity();
        Date now = new Date();
        activity.setPartnerId(SecurityContextHolder.isPartner() ? partnerId : null);
        activity.setAuditStatus(SecurityContextHolder.isAdmin() ? AUDIT_APPROVED : AUDIT_PENDING);
        activity.setCurrentParticipants(0);
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        activity.setChangedAt(now);
        activity.setChangedBy(SecurityContextHolder.getCurrentUserId());
        activity.setChangeReason(SecurityContextHolder.isAdmin() ? "admin_create" : "submit_for_review");

        applyActivityBody(activity, body);

        if (!activityService.save(activity) || activity.getId() == null) {
            return V3Response.error("INTERNAL_ERROR", "save publish failed");
        }

        ensureActivityNo(activity);
        activityService.updateById(activity);

        return V3Response.success(toPublishItem(activity, 0L));
    }

    @Transactional
    @PostMapping("/{id}/display-apply")
    public V3Response<Map<String, Object>> applyDisplay(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }
        if (!Objects.equals(activity.getAuditStatus(), AUDIT_APPROVED)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only published activity can apply display");
        }

        String displayArea = str(firstNonNull(body.get("display_area"), body.get("displayArea")));
        if (!Set.of("home", "activity", "service").contains(displayArea)) {
            return V3Response.error("VALIDATION_ERROR", "display_area must be home|activity|service");
        }

        List<?> range = body.get("display_time") instanceof List<?> list
                ? list
                : (body.get("displayTime") instanceof List<?> list ? list : List.of());
        if (range.size() != 2) {
            return V3Response.error("VALIDATION_ERROR", "display_time must contain [start, end]");
        }

        Date startAt = parseStartTime(range.get(0));
        Date endAt = parseStartTime(range.get(1));
        if (startAt == null || endAt == null || !startAt.before(endAt)) {
            return V3Response.error("VALIDATION_ERROR", "invalid display time range");
        }
        if (activity.getStartTime() != null && endAt.after(activity.getStartTime())) {
            return V3Response.error("VALIDATION_ERROR", "display end time cannot exceed activity start time");
        }

        ActivityDisplayApplication application = activityDisplayApplicationService.page(
                new Page<>(1, 1),
                new QueryWrapper<ActivityDisplayApplication>()
                        .eq("activity_id", id)
                        .eq("display_area", displayArea)
                        .orderByDesc("id")
        ).getRecords().stream().findFirst().orElse(null);
        Date now = new Date();
        if (application == null) {
            application = new ActivityDisplayApplication();
            application.setActivityId(id);
            application.setDisplayArea(displayArea);
            application.setAppliedBy(SecurityContextHolder.getCurrentUserId());
            application.setCreatedAt(now);
        }
        application.setDisplayStartAt(startAt);
        application.setDisplayEndAt(endAt);
        application.setSortOrder(parseInteger(firstNonNull(body.get("sort_order"), body.get("sortOrder")), 0));
        application.setApplyStatus("pending_review");
        application.setChangeReason(str(body.get("reason")));
        application.setUpdatedAt(now);
        application.setReviewedAt(null);
        application.setReviewedBy(null);

        if (application.getId() == null) {
            activityDisplayApplicationService.save(application);
        } else {
            activityDisplayApplicationService.updateById(application);
        }

        return V3Response.success(Map.of(
                "id", application.getId(),
                "activity_id", application.getActivityId(),
                "display_area", application.getDisplayArea(),
                "apply_status", application.getApplyStatus()
        ));
    }

    @Transactional
    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String title = requiredText(body.get("title"));
        if (title == null) {
            return V3Response.error("VALIDATION_ERROR", "title required");
        }

        String fromStatus = mapStatus(activity.getAuditStatus());
        applyActivityBody(activity, body);
        Date now = new Date();
        activity.setUpdatedAt(now);
        activity.setChangedAt(now);
        activity.setChangedBy(SecurityContextHolder.getCurrentUserId());
        if (SecurityContextHolder.isPartner()) {
            activity.setAuditStatus(AUDIT_PENDING);
            activity.setChangeReason("partner_edit_resubmit");
        } else {
            if (activity.getAuditStatus() == null) {
                activity.setAuditStatus(AUDIT_APPROVED);
            }
            activity.setChangeReason("admin_edit");
        }

        if (!activityService.updateById(activity)) {
            return V3Response.error("INTERNAL_ERROR", "update publish failed");
        }

        String toStatus = mapStatus(activity.getAuditStatus());
        stateTransitionService.log(
                "activity", activity.getId(),
                fromStatus, toStatus,
                "edit",
                activity.getChangeReason()
        );

        return V3Response.success(toPublishItem(
                activity,
                loadSignupCounts(List.of(activity.getId())).getOrDefault(activity.getId(), 0L)
        ));
    }

    @Transactional
    @PostMapping("/{id}/review")
    public V3Response<Void> review(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String action = str(body.getOrDefault("action", ""));
        String reason = str(body.getOrDefault("reason", ""));

        if (!"approve".equals(action) && !"reject".equals(action)) {
            return V3Response.error("VALIDATION_ERROR", "action must be approve|reject");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }

        Integer current = activity.getAuditStatus();
        if (current == null || current != AUDIT_PENDING) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending publish can be reviewed");
        }

        int newStatus = "approve".equals(action) ? AUDIT_APPROVED : AUDIT_REJECTED;
        Date now = new Date();
        activity.setAuditStatus(newStatus);
        activity.setUpdatedAt(now);
        activity.setChangedAt(now);
        activity.setChangedBy(SecurityContextHolder.getCurrentUserId());
        activity.setChangeReason(reason.isBlank() ? null : reason);
        activityService.updateById(activity);

        stateTransitionService.log(
                "activity", activity.getId(),
                mapStatus(current), mapStatus(newStatus),
                action, reason.isBlank() ? null : reason
        );

        AuditLog auditLog = new AuditLog();
        auditLog.setTargetType("activity");
        auditLog.setTargetId(activity.getId());
        auditLog.setOperatorId(SecurityContextHolder.getCurrentUserId());
        auditLog.setAction(action);
        auditLog.setPrevStatus((byte) current.intValue());
        auditLog.setCurrStatus((byte) newStatus);
        auditLog.setComment(reason.isBlank() ? null : reason);
        auditLog.setCreatedAt(now);
        auditLogService.save(auditLog);

        return V3Response.success(null);
    }

    @Transactional
    @PostMapping("/{id}/transition")
    public V3Response<Map<String, Object>> transition(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String toStatus = str(body.getOrDefault("to_status", ""));
        String reason = str(body.getOrDefault("reason", ""));
        if (toStatus.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "to_status required");
        }

        String fromStatus = mapStatus(activity.getAuditStatus());
        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "invalid status transition");
        }

        Date now = new Date();
        activity.setAuditStatus("published".equals(toStatus) ? AUDIT_APPROVED : AUDIT_OFFLINE);
        activity.setUpdatedAt(now);
        activity.setChangedAt(now);
        activity.setChangedBy(SecurityContextHolder.getCurrentUserId());
        activity.setChangeReason(reason.isBlank() ? null : reason);
        activityService.updateById(activity);

        stateTransitionService.log(
                "activity", activity.getId(),
                fromStatus, toStatus,
                toStatus,
                reason.isBlank() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", activity.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    @DeleteMapping("/{id}")
    public V3Response<Void> withdraw(@PathVariable Long id) {
        if (!SecurityContextHolder.isPartner() && !SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Activity activity = activityService.getById(id);
        if (activity == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "publish not found");
        }
        if (!canAccessActivity(activity)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }
        if (activity.getAuditStatus() == null || activity.getAuditStatus() != AUDIT_PENDING) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending publish can be withdrawn");
        }

        activityService.removeById(id);
        return V3Response.success(null);
    }

    private boolean canAccessActivity(Activity activity) {
        if (SecurityContextHolder.isAdmin()) {
            return true;
        }
        Long partnerId = SecurityContextHolder.getCurrentPartnerId();
        return partnerId != null && partnerId > 0 && partnerId.equals(activity.getPartnerId());
    }

    private void applyActivityBody(Activity activity, Map<String, Object> body) {
        activity.setTitle(str(body.get("title")));
        activity.setActivityType(normalizeActivityType(str(firstNonNull(
                body.get("sub_type"),
                body.get("subType"),
                body.get("activity_type"),
                body.get("activityType")
        ))));
        activity.setFrontModule(str(firstNonNull(body.get("front_module"), body.get("frontModule"))));
        activity.setFrontPosition(str(firstNonNull(body.get("front_position"), body.get("frontPosition"))));
        activity.setImageUrl(str(firstNonNull(body.get("cover_image"), body.get("coverImage"), body.get("image_url"))));
        activity.setVideoUrl(str(firstNonNull(body.get("video_url"), body.get("videoUrl"))));
        activity.setSummary(str(firstNonNull(body.get("summary"), body.get("description"))));
        activity.setAgenda(str(body.get("agenda")));
        activity.setStartTime(parseStartTime(firstNonNull(
                body.get("start_time"),
                body.get("startTime"),
                body.get("activity_time"),
                body.get("date")
        )));
        activity.setIsFree(parseIsFree(body));
        activity.setFee(parseFee(body));
        activity.setMaxLimit(parseInteger(firstNonNull(body.get("max_limit"), body.get("maxLimit")), 0));
        activity.setFeeItemId(str(firstNonNull(body.get("fee_item_id"), body.get("feeItemId"))));
        activity.setFeeItemName(str(firstNonNull(body.get("fee_item_name"), body.get("feeItemName"))));
        activity.setFeeType(normalizeFeeType(str(firstNonNull(body.get("fee_type"), body.get("feeType"))), activity.getIsFree()));
        activity.setMemberPrice(parseNullableBigDecimal(firstNonNull(body.get("member_price"), body.get("memberPrice"))));
        if (activity.getIsFree() == null) {
            activity.setIsFree(Boolean.TRUE);
        }
        if (activity.getFee() == null) {
            activity.setFee(BigDecimal.ZERO);
        }
        if (Boolean.TRUE.equals(activity.getIsFree())) {
            activity.setFee(BigDecimal.ZERO);
            activity.setFeeType("free");
            if (activity.getMemberPrice() == null) {
                activity.setMemberPrice(BigDecimal.ZERO);
            }
            if (activity.getFeeItemName() == null || activity.getFeeItemName().isBlank()) {
                activity.setFeeItemName("免费");
            }
        }
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }

        List<String> cities = parseCityList(firstNonNull(body.get("cities"), body.get("cities_json"), body.get("citiesJson")));
        if (cities.isEmpty()) {
            String cityName = str(firstNonNull(body.get("city_name"), body.get("cityName")));
            if (!cityName.isBlank()) {
                cities = List.of(cityName);
            }
        }
        if (cities.isEmpty()) {
            cities = List.of("全国");
        }
        activity.setCitiesJson(writeJson(cities));
        activity.setCityName(cities.contains("全国") ? "全国" : cities.get(0));
    }

    private Map<String, Object> toPublishItem(Activity activity, long signupCount) {
        List<String> cities = parseCityList(activity.getCitiesJson());
        if (cities.isEmpty() && activity.getCityName() != null && !activity.getCityName().isBlank()) {
            cities = List.of(activity.getCityName());
        }
        if (cities.isEmpty()) {
            cities = List.of("全国");
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", activity.getId());
        item.put("activity_no", safeOr(activity.getActivityNo(), buildFallbackActivityNo(activity.getId())));
        item.put("activityNo", safeOr(activity.getActivityNo(), buildFallbackActivityNo(activity.getId())));
        item.put("title", safe(activity.getTitle()));
        item.put("sub_type", safe(activity.getActivityType()));
        item.put("subType", safe(activity.getActivityType()));
        item.put("sub_type_label", activityTypeLabel(activity.getActivityType()));
        item.put("activity_type", safe(activity.getActivityType()));
        item.put("front_module", safe(activity.getFrontModule()));
        item.put("frontModule", safe(activity.getFrontModule()));
        item.put("front_module_label", frontModuleLabel(activity.getFrontModule()));
        item.put("frontPosition", safe(activity.getFrontPosition()));
        item.put("front_position", safe(activity.getFrontPosition()));
        item.put("front_position_label", frontPositionLabel(activity.getFrontModule(), activity.getFrontPosition()));
        item.put("cities", cities);
        item.put("cities_json", safe(activity.getCitiesJson()));
        item.put("city_name", safe(activity.getCityName()));
        item.put("cityName", safe(activity.getCityName()));
        item.put("cover_image", safe(activity.getImageUrl()));
        item.put("coverImage", safe(activity.getImageUrl()));
        item.put("image_url", safe(activity.getImageUrl()));
        item.put("video_url", safe(activity.getVideoUrl()));
        item.put("videoUrl", safe(activity.getVideoUrl()));
        item.put("has_video", activity.getVideoUrl() != null && !activity.getVideoUrl().isBlank());
        item.put("summary", safe(activity.getSummary()));
        item.put("description", safe(activity.getSummary()));
        item.put("agenda", safe(activity.getAgenda()));
        item.put("start_time", activity.getStartTime());
        item.put("startTime", activity.getStartTime());
        item.put("submit_time", activity.getCreatedAt());
        item.put("created_at", activity.getCreatedAt());
        item.put("createdAt", activity.getCreatedAt());
        item.put("updated_at", activity.getUpdatedAt());
        item.put("updatedAt", activity.getUpdatedAt());
        item.put("partner_id", activity.getPartnerId());
        item.put("partnerId", activity.getPartnerId());
        item.put("audit_status", activity.getAuditStatus());
        item.put("auditStatus", activity.getAuditStatus());
        item.put("status", mapStatus(activity.getAuditStatus()));
        item.put("status_label", mapStatusLabel(activity.getAuditStatus()));
        item.put("statusLabel", mapStatusLabel(activity.getAuditStatus()));
        item.put("is_free", Boolean.TRUE.equals(activity.getIsFree()));
        item.put("isFree", Boolean.TRUE.equals(activity.getIsFree()));
        item.put("fee", activity.getFee() == null ? BigDecimal.ZERO : activity.getFee());
        item.put("fee_item_id", safe(activity.getFeeItemId()));
        item.put("feeItemId", safe(activity.getFeeItemId()));
        item.put("fee_item_name", safe(activity.getFeeItemName()));
        item.put("feeItemName", safe(activity.getFeeItemName()));
        item.put("fee_type", safeOr(activity.getFeeType(), Boolean.TRUE.equals(activity.getIsFree()) ? "free" : "paid"));
        item.put("feeType", safeOr(activity.getFeeType(), Boolean.TRUE.equals(activity.getIsFree()) ? "free" : "paid"));
        item.put("member_price", activity.getMemberPrice() == null ? BigDecimal.ZERO : activity.getMemberPrice());
        item.put("memberPrice", activity.getMemberPrice() == null ? BigDecimal.ZERO : activity.getMemberPrice());
        item.put("max_limit", activity.getMaxLimit() == null ? 0 : activity.getMaxLimit());
        item.put("maxLimit", activity.getMaxLimit() == null ? 0 : activity.getMaxLimit());
        item.put("current_participants", signupCount > 0 ? signupCount : safeInt(activity.getCurrentParticipants()));
        item.put("currentParticipants", signupCount > 0 ? signupCount : safeInt(activity.getCurrentParticipants()));
        item.put("signup_count", signupCount > 0 ? signupCount : safeInt(activity.getCurrentParticipants()));
        item.put("signupCount", signupCount > 0 ? signupCount : safeInt(activity.getCurrentParticipants()));
        item.put("change_reason", safe(activity.getChangeReason()));
        item.put("changeReason", safe(activity.getChangeReason()));
        item.put("changed_at", activity.getChangedAt());
        item.put("changedAt", activity.getChangedAt());
        item.put("display_applications", loadDisplayApplications(activity.getId()));
        return item;
    }

    private List<Map<String, Object>> loadDisplayApplications(Long activityId) {
        if (activityId == null || activityId <= 0) {
            return List.of();
        }
        return activityDisplayApplicationService.list(new QueryWrapper<ActivityDisplayApplication>()
                        .eq("activity_id", activityId)
                        .orderByDesc("created_at")
                        .orderByDesc("id"))
                .stream()
                .map(application -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", application.getId());
                    item.put("display_area", safe(application.getDisplayArea()));
                    item.put("displayArea", safe(application.getDisplayArea()));
                    item.put("display_area_label", displayAreaLabel(application.getDisplayArea()));
                    item.put("apply_status", safe(application.getApplyStatus()));
                    item.put("applyStatus", safe(application.getApplyStatus()));
                    item.put("apply_status_label", displayStatusLabel(application.getApplyStatus()));
                    item.put("display_start_at", application.getDisplayStartAt());
                    item.put("displayStartAt", application.getDisplayStartAt());
                    item.put("display_end_at", application.getDisplayEndAt());
                    item.put("displayEndAt", application.getDisplayEndAt());
                    item.put("sort_order", application.getSortOrder() == null ? 0 : application.getSortOrder());
                    item.put("change_reason", safe(application.getChangeReason()));
                    item.put("changeReason", safe(application.getChangeReason()));
                    return item;
                })
                .toList();
    }

    private Map<String, Object> toRegistrationItem(ActivityRegistration registration) {
        Map<String, Object> item = new LinkedHashMap<>();
        boolean paid = isPaidRegistration(registration);
        item.put("id", registration.getId());
        item.put("company_name", safe(registration.getCompanyName()));
        item.put("companyName", safe(registration.getCompanyName()));
        item.put("contact_name", safe(registration.getContactName()));
        item.put("contactName", safe(registration.getContactName()));
        item.put("phone", safe(registration.getPhone()));
        item.put("position", "-");
        item.put("registration_status", safe(registration.getRegistrationStatus()));
        item.put("registrationStatus", safe(registration.getRegistrationStatus()));
        item.put("registration_status_label", registrationStatusLabel(registration.getRegistrationStatus()));
        item.put("registrationStatusLabel", registrationStatusLabel(registration.getRegistrationStatus()));
        item.put("paid", paid);
        item.put("amount", registration.getAmount() == null ? BigDecimal.ZERO : registration.getAmount());
        item.put("created_at", registration.getCreatedAt());
        item.put("createdAt", registration.getCreatedAt());
        item.put("paid_at", registration.getPaidAt());
        item.put("paidAt", registration.getPaidAt());
        return item;
    }

    private Map<Long, Long> loadSignupCounts(List<Long> activityIds) {
        Map<Long, Long> counts = new HashMap<>();
        if (activityIds == null || activityIds.isEmpty()) {
            return counts;
        }

        List<ActivityRegistration> registrations = activityRegistrationService.list(
                new QueryWrapper<ActivityRegistration>()
                        .select("activity_id")
                        .in("activity_id", activityIds)
        );
        for (ActivityRegistration registration : registrations) {
            if (registration.getActivityId() == null) {
                continue;
            }
            counts.merge(registration.getActivityId(), 1L, Long::sum);
        }
        return counts;
    }

    private boolean isAllowedTransition(String fromStatus, String toStatus) {
        return ("published".equals(fromStatus) && "offline".equals(toStatus))
                || ("offline".equals(fromStatus) && "published".equals(toStatus));
    }

    private void ensureActivityNo(Activity activity) {
        if (activity.getActivityNo() != null && !activity.getActivityNo().isBlank()) {
            return;
        }
        activity.setActivityNo(buildFallbackActivityNo(activity.getId()));
    }

    private String buildFallbackActivityNo(Long id) {
        if (id == null || id <= 0) {
            return "";
        }
        return String.format("ACT-%06d", id);
    }

    private String mapStatus(Integer auditStatus) {
        if (auditStatus == null) return "draft";
        if (auditStatus == AUDIT_APPROVED) return "published";
        if (auditStatus == AUDIT_REJECTED) return "rejected";
        if (auditStatus == AUDIT_OFFLINE) return "offline";
        if (auditStatus == AUDIT_PENDING) return "auditing";
        return "draft";
    }

    private String mapStatusLabel(Integer auditStatus) {
        if (auditStatus == null) return "草稿";
        if (auditStatus == AUDIT_APPROVED) return "已发布";
        if (auditStatus == AUDIT_REJECTED) return "已驳回";
        if (auditStatus == AUDIT_OFFLINE) return "已下架";
        if (auditStatus == AUDIT_PENDING) return "审核中";
        return "草稿";
    }

    private String activityTypeLabel(String activityType) {
        return switch (safeOr(activityType, "")) {
            case "hengjia" -> "亨嘉之会";
            case "charity" -> "公益行";
            case "inspection" -> "出海考察";
            case "salon" -> "行业沙龙";
            default -> safeOr(activityType, "活动");
        };
    }

    private String frontModuleLabel(String frontModule) {
        return switch (safeOr(frontModule, "")) {
            case "home-banner" -> "首页轮播图";
            case "activity-center" -> "活动中心";
            case "resource-expansion" -> "资源拓展";
            default -> safeOr(frontModule, "-");
        };
    }

    private String frontPositionLabel(String frontModule, String frontPosition) {
        if ("home-banner".equals(frontModule)) {
            return switch (safeOr(frontPosition, "")) {
                case "banner-main" -> "轮播图主位";
                case "banner-sub" -> "轮播图副位";
                default -> safeOr(frontPosition, "-");
            };
        }
        if ("activity-center".equals(frontModule)) {
            return switch (safeOr(frontPosition, "")) {
                case "top" -> "顶部焦点位";
                case "list" -> "列表推荐位";
                default -> safeOr(frontPosition, "-");
            };
        }
        return switch (safeOr(frontPosition, "")) {
            case "sidebar" -> "侧边栏";
            case "footer" -> "底部栏";
            default -> safeOr(frontPosition, "-");
        };
    }

    private boolean isPaidRegistration(ActivityRegistration registration) {
        return registration != null
                && ("confirmed".equalsIgnoreCase(safe(registration.getRegistrationStatus()))
                || registration.getPaidAt() != null);
    }

    private String registrationStatusLabel(String status) {
        return switch (safeOr(status, "")) {
            case "confirmed" -> "报名成功";
            case "pending_payment" -> "待支付";
            case "cancelled" -> "已取消";
            default -> safeOr(status, "-");
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

    private String displayStatusLabel(String status) {
        return switch (safe(status)) {
            case "pending_review" -> "待审核";
            case "approved" -> "已通过";
            case "rejected" -> "已驳回";
            case "offline" -> "已下线";
            default -> safe(status);
        };
    }

    private String normalizeActivityType(String raw) {
        return switch (safeOr(raw, "")) {
            case "overseas" -> "inspection";
            case "forum" -> "salon";
            default -> safeOr(raw, "");
        };
    }

    private String normalizeFeeType(String raw, Boolean isFree) {
        if (Boolean.TRUE.equals(isFree)) {
            return "free";
        }
        return switch (safeOr(raw, "")) {
            case "member-discount", "member-free" -> raw;
            default -> "paid";
        };
    }

    private Boolean parseIsFree(Map<String, Object> body) {
        Object feeType = firstNonNull(body.get("fee_type"), body.get("feeType"));
        if (feeType != null) {
            String feeTypeText = String.valueOf(feeType).trim();
            if (!feeTypeText.isEmpty()) {
                return "free".equalsIgnoreCase(feeTypeText);
            }
        }
        Object isFree = firstNonNull(body.get("is_free"), body.get("isFree"));
        if (isFree != null) {
            return Boolean.parseBoolean(String.valueOf(isFree));
        }
        return Boolean.TRUE;
    }

    private BigDecimal parseFee(Map<String, Object> body) {
        BigDecimal value = parseNullableBigDecimal(body.get("fee"));
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal parseNullableBigDecimal(Object value) {
        try {
            if (value == null || String.valueOf(value).isBlank()) {
                return null;
            }
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer parseInteger(Object value, int defaultValue) {
        try {
            if (value == null || String.valueOf(value).isBlank()) {
                return defaultValue;
            }
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private Date parseStartTime(Object value) {
        if (value == null) return null;
        if (value instanceof Date date) return date;
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        try {
            LocalDateTime ldt = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            } catch (Exception ignored2) {
                try {
                    return Date.from(Instant.parse(text));
                } catch (Exception ignored3) {
                    try {
                        return Date.from(OffsetDateTime.parse(text).toInstant());
                    } catch (Exception ignored4) {
                        return null;
                    }
                }
            }
        }
    }

    private List<String> parseCityList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                String text = str(item);
                if (!text.isBlank()) {
                    result.add(text);
                }
            }
            return result;
        }

        String raw = str(value);
        if (raw.isBlank()) {
            return new ArrayList<>();
        }

        if (raw.startsWith("[")) {
            try {
                return objectMapper.readValue(raw, new TypeReference<ArrayList<String>>() {});
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
        }

        return new ArrayList<>(Arrays.stream(raw.split("[,，]"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList());
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String requiredText(Object value) {
        String text = str(value);
        return text.isBlank() ? null : text;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeOr(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String fmtDateTime(Date value) {
        if (value == null) {
            return "";
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
