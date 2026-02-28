package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.ActivityRegistration;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IActivityRegistrationService;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final IMemberProfileService memberProfileService;
    private final JdbcTemplate jdbcTemplate;

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

        // Build quota info for tender_download
        Map<String, Object> quota = buildTenderDownloadQuota(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("usage", usage);
        data.put("quota", quota);
        return V3Response.success(data);
    }

    private Map<String, Object> buildTenderDownloadQuota(Long userId) {
        Map<String, Object> quota = new LinkedHashMap<>();

        // Member level and base quota
        String memberLevel = "normal";
        int baseQuota = 0;
        QueryWrapper<MemberProfile> mpQw = new QueryWrapper<>();
        mpQw.eq("user_id", userId);
        MemberProfile mp = memberProfileService.getOne(mpQw);
        if (mp != null && mp.getMemberLevel() != null) {
            memberLevel = mp.getMemberLevel();
            baseQuota = switch (memberLevel) {
                case "vip" -> 50;
                case "svip" -> 200;
                default -> 0;
            };
        }

        // Granted extra
        Integer grantedExtra = jdbcTemplate.queryForObject(
                "SELECT ISNULL(SUM(amount), 0) FROM benefit_grant_logs " +
                "WHERE member_id = ? AND benefit_type = 'tender_download'",
                Integer.class, userId
        );
        int extra = grantedExtra == null ? 0 : grantedExtra;
        int totalQuota = baseQuota + extra;

        // Used this year
        Integer usedThisYear = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tender_download_logs " +
                "WHERE user_id = ? AND downloaded_at >= DATEFROMPARTS(YEAR(GETUTCDATE()), 1, 1)",
                Integer.class, userId
        );
        int used = usedThisYear == null ? 0 : usedThisYear;

        quota.put("memberLevel", memberLevel);
        quota.put("tenderDownloadBaseQuota", baseQuota);
        quota.put("tenderDownloadGrantedExtra", extra);
        quota.put("tenderDownloadTotalQuota", totalQuota);
        quota.put("tenderDownloadUsed", used);
        quota.put("tenderDownloadRemaining", Math.max(0, totalQuota - used));
        return quota;
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
