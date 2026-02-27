package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOrderService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/v3/admin/orders")
@RequiredArgsConstructor
public class AdminOrdersV3Controller {

    private static final Set<String> TERMINAL_STATUSES = Set.of("closed", "refunded");

    private final IOrderService orderService;
    private final StateTransitionService stateTransitionService;

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
