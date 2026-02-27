package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v3/orders")
@RequiredArgsConstructor
public class OrdersV3Controller {

    private final IOrderService orderService;

    @GetMapping
    public V3Response<V3PageData<OrderEntity>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "order_type", required = false) String orderType,
            @RequestParam(name = "order_status", required = false) String orderStatus
    ) {
        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        if (orderType != null && !orderType.isBlank()) qw.eq("order_type", orderType);
        if (orderStatus != null && !orderStatus.isBlank()) qw.eq("order_status", orderStatus);
        qw.orderByDesc("created_at");

        Page<OrderEntity> p = new Page<>(page, pageSize);
        Page<OrderEntity> result = orderService.page(p, qw);

        return V3Response.success(new V3PageData<>(
                result.getRecords(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<OrderEntity> detail(@PathVariable Long id) {
        OrderEntity order = orderService.getById(id);
        if (order == null) return V3Response.error("RESOURCE_NOT_FOUND", "order not found");
        return V3Response.success(order);
    }
}
