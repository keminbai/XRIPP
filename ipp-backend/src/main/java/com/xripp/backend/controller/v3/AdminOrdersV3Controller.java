package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOrderService;
import com.xripp.backend.service.StateTransitionService;
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
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "order_status", required = false) String orderStatus,
            @RequestParam(name = "order_type", required = false) String orderType,
            @RequestParam(required = false) String keyword
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
        qw.orderByDesc("created_at");

        Page<OrderEntity> p = new Page<>(page, pageSize);
        Page<OrderEntity> result = orderService.page(p, qw);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    private Map<String, Object> toItem(OrderEntity o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo() != null ? o.getOrderNo() : "ORD-" + o.getId());
        m.put("userId", o.getUserId());
        m.put("partnerId", o.getPartnerId());
        m.put("orderType", o.getOrderType() != null ? o.getOrderType() : "");
        m.put("orderStatus", o.getOrderStatus() != null ? o.getOrderStatus() : "");
        m.put("amount", o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO);
        m.put("currencyCode", o.getCurrencyCode() != null ? o.getCurrencyCode() : "CNY");
        m.put("bizType", o.getBizType() != null ? o.getBizType() : "");
        m.put("bizId", o.getBizId());
        m.put("createdAt", fmtDate(o.getCreatedAt()));
        m.put("updatedAt", fmtDate(o.getUpdatedAt()));
        return m;
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
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
        orderService.updateById(order);

        stateTransitionService.log(
                "order", order.getId(),
                fromStatus, toStatus,
                toStatus,   // action 与目标状态同名，语义清晰
                reason.isEmpty() ? null : reason
        );

        return V3Response.success(Map.of(
                "id", order.getId(),
                "from_status", fromStatus,
                "to_status", toStatus
        ));
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
}
