package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.entity.StateTransitionLog;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOrderService;
import com.xripp.backend.service.IStateTransitionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/orders")
@RequiredArgsConstructor
public class OrdersV3Controller {

    private final IOrderService orderService;
    private final IStateTransitionLogService stateTransitionLogService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "order_type", required = false) String orderType,
            @RequestParam(name = "order_status", required = false) String orderStatus
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        // 非管理员只能查看自己的订单
        if (!SecurityContextHolder.isAdmin()) {
            qw.eq("user_id", userId);
        }
        if (orderType != null && !orderType.isBlank()) qw.eq("order_type", orderType);
        if (orderStatus != null && !orderStatus.isBlank()) qw.eq("order_status", orderStatus);
        qw.orderByDesc("created_at");

        Page<OrderEntity> p = new Page<>(page, pageSize);
        Page<OrderEntity> result = orderService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderEntity o : result.getRecords()) {
            items.add(toItem(o));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        OrderEntity order = orderService.getById(id);
        if (order == null) return V3Response.error("RESOURCE_NOT_FOUND", "order not found");

        // 非管理员只能查自己的订单
        if (!SecurityContextHolder.isAdmin() && !userId.equals(order.getUserId())) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        return V3Response.success(toItem(order));
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        String orderType = strVal(body, "order_type");
        if (!Set.of("activity", "service", "tender", "report", "other").contains(orderType)) {
            return V3Response.error("VALIDATION_ERROR", "order_type must be activity/service/tender/report/other");
        }

        String bizType = strVal(body, "biz_type");
        if (bizType.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "biz_type is required");
        }

        String ts8 = String.valueOf(System.currentTimeMillis());
        ts8 = ts8.substring(ts8.length() - 8);

        OrderEntity order = new OrderEntity();
        order.setOrderNo("ORD-" + userId + "-" + ts8);
        order.setUserId(userId);
        order.setPartnerId(SecurityContextHolder.getCurrentPartnerId());
        order.setOrderType(orderType);
        order.setOrderStatus("created");
        order.setBizType(bizType);

        // Optional: amount
        Object amtObj = body.get("amount");
        if (amtObj != null) {
            try {
                order.setAmount(new java.math.BigDecimal(amtObj.toString()));
            } catch (Exception e) {
                order.setAmount(java.math.BigDecimal.ZERO);
            }
        } else {
            order.setAmount(java.math.BigDecimal.ZERO);
        }

        // Optional: currency_code
        String currencyCode = strVal(body, "currency_code");
        order.setCurrencyCode(currencyCode.isBlank() ? "CNY" : currencyCode);

        // Optional: biz_id
        Object bizIdObj = body.get("biz_id");
        if (bizIdObj != null) {
            try {
                order.setBizId(Long.parseLong(bizIdObj.toString()));
            } catch (Exception ignored) {}
        }

        Date now = new Date();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderService.save(order);

        // Log state transition
        StateTransitionLog log = new StateTransitionLog();
        log.setTargetType("order");
        log.setTargetId(order.getId());
        log.setFromStatus(null);
        log.setToStatus("created");
        log.setAction("created");
        log.setChangedBy(userId);
        log.setChangedAt(now);
        stateTransitionLogService.save(log);

        return V3Response.success(toItem(order));
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private Map<String, Object> toItem(OrderEntity o) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo() == null ? "" : o.getOrderNo());
        m.put("orderType", o.getOrderType() == null ? "" : o.getOrderType());
        m.put("orderStatus", o.getOrderStatus() == null ? "" : o.getOrderStatus());
        m.put("amount", o.getAmount() == null ? "0.00" : o.getAmount().toPlainString());
        m.put("currencyCode", o.getCurrencyCode() == null ? "CNY" : o.getCurrencyCode());
        m.put("bizType", o.getBizType() == null ? "" : o.getBizType());
        m.put("bizId", o.getBizId());
        m.put("title", buildTitle(o));
        m.put("category", mapCategory(o.getOrderType()));
        m.put("status", mapStatus(o.getOrderStatus()));
        m.put("createTime", fmtDate(o.getCreatedAt()));
        m.put("updatedAt", fmtDate(o.getUpdatedAt()));
        return m;
    }

    private String buildTitle(OrderEntity o) {
        String type = o.getOrderType() == null ? "" : o.getOrderType();
        return switch (type) {
            case "service" -> "服务订单";
            case "tender" -> "标书购买订单";
            case "activity" -> "活动报名订单";
            case "report" -> "报告订单";
            default -> "订单";
        };
    }

    private String mapCategory(String orderType) {
        if (orderType == null) return "other";
        return switch (orderType) {
            case "activity", "service", "tender", "report", "other" -> orderType;
            default -> "other";
        };
    }

    private String mapStatus(String orderStatus) {
        if (orderStatus == null) return "pending";
        return switch (orderStatus) {
            case "PAID" -> "paid";
            case "UNPAID" -> "pending";
            case "REFUNDED" -> "cancelled";
            case "FAILED", "CLOSED" -> "cancelled";
            default -> "pending";
        };
    }

    private String fmtDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
