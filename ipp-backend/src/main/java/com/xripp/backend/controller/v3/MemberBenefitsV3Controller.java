package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IActivityRegistrationService;
import com.xripp.backend.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/member/benefits")
@RequiredArgsConstructor
public class MemberBenefitsV3Controller {

    private final IActivityRegistrationService activityRegistrationService;
    private final IOrderService orderService;

    @GetMapping("/usage")
    public V3Response<Map<String, Object>> usage() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }
        if (!SecurityContextHolder.isMember()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        Map<String, Integer> usage = new LinkedHashMap<>();
        usage.put("tender_download", countOrderBizUsage(userId, "tender_download"));
        usage.put("sms_quota", countOrderBizUsage(userId, "sms_quota"));
        usage.put("report_download", countOrderBizUsage(userId, "report_download"));
        usage.put("supplier_quota", countOrderBizUsage(userId, "supplier_quota"));
        usage.put("activity_free", countFreeActivityUsage(userId));

        return V3Response.success(Map.of("usage", usage));
    }

    private int countOrderBizUsage(Long userId, String bizType) {
        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("biz_type", bizType);
        Long count = orderService.count(qw);
        return count == null ? 0 : count.intValue();
    }

    private int countFreeActivityUsage(Long userId) {
        QueryWrapper<ActivityRegistration> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.eq("amount", BigDecimal.ZERO);
        Long count = activityRegistrationService.count(qw);
        return count == null ? 0 : count.intValue();
    }
}
