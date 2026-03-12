package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.dto.OrderExportDTO;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IOrderService;
import com.xripp.backend.service.StateTransitionService;
import com.xripp.backend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/orders")
@RequiredArgsConstructor
public class AdminOrdersV3Controller {

    private static final Set<String> TERMINAL_STATUSES = Set.of("closed", "refunded");

    private final IOrderService orderService;
    private final IMemberProfileService memberProfileService;
    private final StateTransitionService stateTransitionService;

    // --- Label mappings ---

    private static final Map<String, String> ORDER_TYPE_LABELS = Map.of(
            "membership", "会员购买",
            "tender", "标书购买",
            "activity", "活动报名",
            "service", "投标服务",
            "consulting", "出海咨询",
            "delivery", "交付服务",
            "ghostwriting", "标书代写",
            "report", "报告订单",
            "other", "其他"
    );

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "created", "待支付",
            "awaiting_payment", "待支付",
            "paid", "进行中",
            "in_service", "进行中",
            "completed", "已完成",
            "refunding", "进行中",
            "refunded", "已取消",
            "closed", "已取消"
    );

    private static final Set<String> FRONTEND_ORDER_TYPES = Set.of(
            "membership", "tender", "activity"
    );

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "order_status", required = false) String orderStatus,
            @RequestParam(name = "order_type", required = false) String orderType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        if (orderStatus != null && !orderStatus.isBlank()) {
            qw.eq("order_status", orderStatus.trim());
        }
        if (orderType != null && !orderType.isBlank()) {
            qw.eq("order_type", orderType.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("order_no", kw).or().like("biz_type", kw));
        }
        if ("frontend".equals(source)) {
            qw.in("order_type", FRONTEND_ORDER_TYPES);
        } else if ("backend".equals(source)) {
            qw.notIn("order_type", FRONTEND_ORDER_TYPES);
        }
        qw.orderByDesc("created_at");

        Page<OrderEntity> p = new Page<>(page, pageSize);
        Page<OrderEntity> result = orderService.page(p, qw);

        // Batch load member profiles for all user_ids in this page
        Set<Long> userIds = result.getRecords().stream()
                .map(OrderEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, MemberProfile> profileMap = loadProfiles(userIds);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(o -> toItem(o, profileMap.get(o.getUserId())))
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/stats")
    public V3Response<Map<String, Object>> stats() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        qw.select("order_status", "COUNT(*) AS cnt", "ISNULL(SUM(amount),0) AS total_amount");
        qw.groupBy("order_status");
        List<Map<String, Object>> rows = orderService.listMaps(qw);

        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        long awaitingCount = 0; BigDecimal awaitingAmount = BigDecimal.ZERO;
        long inProgressCount = 0; BigDecimal inProgressAmount = BigDecimal.ZERO;
        long completedCount = 0; BigDecimal completedAmount = BigDecimal.ZERO;
        long cancelledCount = 0; BigDecimal cancelledAmount = BigDecimal.ZERO;

        for (Map<String, Object> row : rows) {
            String status = String.valueOf(row.getOrDefault("order_status", ""));
            long cnt = ((Number) row.getOrDefault("cnt", 0)).longValue();
            BigDecimal amt = row.get("total_amount") instanceof BigDecimal bd ? bd
                    : new BigDecimal(String.valueOf(row.getOrDefault("total_amount", "0")));

            totalCount += cnt;
            totalAmount = totalAmount.add(amt);

            String label = STATUS_LABELS.getOrDefault(status, "待支付");
            switch (label) {
                case "待支付" -> { awaitingCount += cnt; awaitingAmount = awaitingAmount.add(amt); }
                case "进行中" -> { inProgressCount += cnt; inProgressAmount = inProgressAmount.add(amt); }
                case "已完成" -> { completedCount += cnt; completedAmount = completedAmount.add(amt); }
                case "已取消" -> { cancelledCount += cnt; cancelledAmount = cancelledAmount.add(amt); }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCount", totalCount);
        data.put("totalAmount", totalAmount);
        data.put("awaitingCount", awaitingCount);
        data.put("awaitingAmount", awaitingAmount);
        data.put("inProgressCount", inProgressCount);
        data.put("inProgressAmount", inProgressAmount);
        data.put("completedCount", completedCount);
        data.put("completedAmount", completedAmount);
        data.put("cancelledCount", cancelledCount);
        data.put("cancelledAmount", cancelledAmount);
        return V3Response.success(data);
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String orderType = strVal(body, "order_type");
        if (orderType.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "order_type required");
        }

        Object amtObj = body.get("amount");
        BigDecimal amount = BigDecimal.ZERO;
        if (amtObj != null) {
            try { amount = new BigDecimal(String.valueOf(amtObj)); } catch (Exception ignored) {}
        }

        // user_id: admin can specify target user
        Long targetUserId = null;
        Object uidObj = body.get("user_id");
        if (uidObj != null) {
            try { targetUserId = Long.parseLong(String.valueOf(uidObj)); } catch (Exception ignored) {}
        }
        if (targetUserId == null) {
            targetUserId = SecurityContextHolder.getCurrentUserId();
        }

        String ts8 = String.valueOf(System.currentTimeMillis());
        ts8 = ts8.substring(ts8.length() - 8);

        OrderEntity order = new OrderEntity();
        order.setOrderNo("SRV-" + ts8);
        order.setUserId(targetUserId);
        order.setOrderType(orderType);
        order.setOrderStatus("in_service");
        order.setAmount(amount);
        order.setCurrencyCode("CNY");
        order.setBizType(strVal(body, "biz_type").isBlank() ? orderType : strVal(body, "biz_type"));
        order.setChangeReason(strVal(body, "remark"));

        Date now = new Date();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setChangedBy(SecurityContextHolder.getCurrentUserId());
        order.setChangedAt(now);
        orderService.save(order);

        stateTransitionService.log("order", order.getId(), null, "in_service", "admin_create",
                strVal(body, "remark").isBlank() ? null : strVal(body, "remark"));

        MemberProfile mp = null;
        if (targetUserId != null) {
            mp = memberProfileService.getOne(new QueryWrapper<MemberProfile>().eq("user_id", targetUserId).last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        }
        return V3Response.success(toItem(order, mp));
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

        String toStatus = String.valueOf(body.getOrDefault("to_status", "")).trim();
        String reason = String.valueOf(body.getOrDefault("reason", "")).trim();
        if (toStatus.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "to_status required");
        }

        OrderEntity order = orderService.getById(id);
        if (order == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "order not found");
        }

        String fromStatus = order.getOrderStatus() == null ? "" : order.getOrderStatus().trim();
        if (fromStatus.isEmpty()) {
            return V3Response.error("STATE_INVALID_TRANSITION", "order status empty");
        }

        if (TERMINAL_STATUSES.contains(fromStatus) && !fromStatus.equals(toStatus)) {
            return V3Response.error("STATE_TERMINAL_LOCKED", "terminal status locked");
        }

        if (!isAllowedTransition(fromStatus, toStatus)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "invalid status transition");
        }

        order.setOrderStatus(toStatus);
        order.setUpdatedAt(new Date());
        order.setChangedBy(SecurityContextHolder.getCurrentUserId());
        order.setChangedAt(new Date());
        order.setChangeReason(reason.isEmpty() ? null : reason);
        orderService.updateById(order);

        stateTransitionService.log(
                "order", order.getId(),
                fromStatus, toStatus,
                toStatus, reason.isEmpty() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", order.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
    }

    // --- Helpers ---

    private Map<Long, MemberProfile> loadProfiles(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        List<MemberProfile> profiles = memberProfileService.list(
                new QueryWrapper<MemberProfile>().in("user_id", userIds)
        );
        return profiles.stream().collect(Collectors.toMap(
                MemberProfile::getUserId, mp -> mp, (a, b) -> a
        ));
    }

    private Map<String, Object> toItem(OrderEntity o, MemberProfile mp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo() != null ? o.getOrderNo() : "ORD-" + o.getId());
        m.put("userId", o.getUserId());
        m.put("partnerId", o.getPartnerId());

        String rawType = o.getOrderType() != null ? o.getOrderType() : "";
        m.put("orderType", ORDER_TYPE_LABELS.getOrDefault(rawType, rawType));

        String rawStatus = o.getOrderStatus() != null ? o.getOrderStatus() : "";
        m.put("status", STATUS_LABELS.getOrDefault(rawStatus, "待支付"));
        m.put("orderStatus", rawStatus);

        m.put("source", FRONTEND_ORDER_TYPES.contains(rawType) ? "frontend" : "backend");
        m.put("orderTitle", buildTitle(rawType, o.getBizType()));

        m.put("amount", o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO);
        m.put("currencyCode", o.getCurrencyCode() != null ? o.getCurrencyCode() : "CNY");
        m.put("bizType", o.getBizType() != null ? o.getBizType() : "");
        m.put("bizId", o.getBizId());
        m.put("remark", o.getChangeReason() != null ? o.getChangeReason() : "");

        // Enriched from member_profile
        m.put("companyName", mp != null && mp.getCompanyName() != null ? mp.getCompanyName() : "-");
        m.put("contactPerson", mp != null && mp.getContactPerson() != null ? mp.getContactPerson() : "-");
        m.put("contactPhone", mp != null && mp.getContactPhone() != null ? mp.getContactPhone() : "-");
        m.put("industry", mp != null && mp.getIndustry() != null ? mp.getIndustry() : "");

        m.put("createTime", fmtDate(o.getCreatedAt()));
        m.put("updatedAt", fmtDate(o.getUpdatedAt()));
        return m;
    }

    private String buildTitle(String orderType, String bizType) {
        String label = ORDER_TYPE_LABELS.getOrDefault(orderType, "");
        if (!label.isEmpty()) return label;
        if (bizType != null && !bizType.isBlank()) return bizType;
        return "订单";
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private boolean isAllowedTransition(String from, String to) {
        if (from.equals(to)) return false;
        return switch (from) {
            case "created" -> "awaiting_payment".equals(to);
            case "awaiting_payment" -> "paid".equals(to) || "closed".equals(to);
            case "paid" -> "in_service".equals(to) || "refunding".equals(to);
            case "in_service" -> "completed".equals(to) || "refunding".equals(to);
            case "refunding" -> "refunded".equals(to);
            case "completed" -> "closed".equals(to);
            default -> false;
        };
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(name = "order_status", required = false) String orderStatus,
            @RequestParam(name = "order_type", required = false) String orderType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            HttpServletResponse response
    ) throws Exception {
        if (!SecurityContextHolder.isAdmin()) {
            response.setStatus(403);
            return;
        }

        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        if (orderStatus != null && !orderStatus.isBlank()) {
            qw.eq("order_status", orderStatus.trim());
        }
        if (orderType != null && !orderType.isBlank()) {
            qw.eq("order_type", orderType.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("order_no", kw).or().like("biz_type", kw));
        }
        if ("frontend".equals(source)) {
            qw.in("order_type", FRONTEND_ORDER_TYPES);
        } else if ("backend".equals(source)) {
            qw.notIn("order_type", FRONTEND_ORDER_TYPES);
        }
        qw.orderByDesc("created_at");

        List<OrderEntity> all = orderService.list(qw);

        Set<Long> userIds = all.stream()
                .map(OrderEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, MemberProfile> profileMap = loadProfiles(userIds);

        List<OrderExportDTO> rows = new ArrayList<>();
        for (OrderEntity o : all) {
            MemberProfile mp = profileMap.get(o.getUserId());
            OrderExportDTO dto = new OrderExportDTO();
            dto.setOrderNo(o.getOrderNo() != null ? o.getOrderNo() : "ORD-" + o.getId());
            dto.setOrderType(ORDER_TYPE_LABELS.getOrDefault(
                    o.getOrderType() != null ? o.getOrderType() : "", o.getOrderType()));
            dto.setCompanyName(mp != null && mp.getCompanyName() != null ? mp.getCompanyName() : "-");
            dto.setContactPerson(mp != null && mp.getContactPerson() != null ? mp.getContactPerson() : "-");
            dto.setContactPhone(mp != null && mp.getContactPhone() != null ? mp.getContactPhone() : "-");
            dto.setAmount(o.getAmount() != null ? o.getAmount().toPlainString() : "0");
            dto.setOrderStatus(STATUS_LABELS.getOrDefault(
                    o.getOrderStatus() != null ? o.getOrderStatus() : "", "待支付"));
            dto.setCreatedAt(fmtDate(o.getCreatedAt()));
            rows.add(dto);
        }
        ExcelExportUtil.write(response, "订单列表", OrderExportDTO.class, rows);
    }
}
