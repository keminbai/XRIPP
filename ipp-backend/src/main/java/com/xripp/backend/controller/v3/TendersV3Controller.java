package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.entity.TenderDownloadLog;
import com.xripp.backend.entity.TenderEntity;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IOrderService;
import com.xripp.backend.service.ITenderDownloadLogService;
import com.xripp.backend.service.ITenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v3/tenders")
@RequiredArgsConstructor
public class TendersV3Controller {

    private final ITenderService tenderService;
    private final ITenderDownloadLogService downloadLogService;
    private final IOrderService orderService;
    private final IMemberProfileService memberProfileService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String category
    ) {
        QueryWrapper<TenderEntity> qw = new QueryWrapper<>();
        qw.eq("tender_status", "published");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw)
                    .or().like("tender_no", kw)
                    .or().like("organization_name", kw));
        }
        if (country != null && !country.isBlank()) {
            qw.eq("country", country.trim());
        }
        if (category != null && !category.isBlank()) {
            qw.eq("category", category.trim());
        }
        qw.orderByDesc("is_top").orderByDesc("updated_at");

        Page<TenderEntity> p = new Page<>(page, pageSize);
        Page<TenderEntity> result = tenderService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (TenderEntity t : result.getRecords()) {
            items.add(toItem(t));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        TenderEntity t = tenderService.getById(id);
        if (t == null || !"published".equals(t.getTenderStatus())) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        Map<String, Object> m = toItem(t);
        m.put("description", t.getSummary() == null ? "" : t.getSummary());
        return V3Response.success(m);
    }

    /**
     * 标书下载（防重复扣减）。
     *
     * <p>业务规则：
     * <ul>
     *   <li>首次下载：写入 tender_download_logs + 创建 order 记录（biz_type=tender_download）</li>
     *   <li>重复下载：跳过扣减，直接返回标书信息（is_duplicate=true）</li>
     * </ul>
     *
     * <p>需要登录，不限角色（member/partner/admin 均可触发下载记录）。
     */
    @Transactional
    @PostMapping("/{id}/download")
    public V3Response<Map<String, Object>> download(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        TenderEntity t = tenderService.getById(id);
        if (t == null || !"published".equals(t.getTenderStatus())) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        // Quota check: skip for admin and duplicate downloads
        if (!SecurityContextHolder.isAdmin()) {
            V3Response<Map<String, Object>> quotaError = checkDownloadQuota(userId, t.getId());
            if (quotaError != null) return quotaError;
        }

        boolean isFirstDownload = downloadLogService.tryLog(userId, t.getId());

        if (isFirstDownload) {
            // 首次下载：创建 order 记录（用于 /v3/member/benefits/usage 统计）
            OrderEntity order = new OrderEntity();
            order.setOrderNo(genOrderNo(userId, t.getId()));
            order.setUserId(userId);
            order.setOrderType("service");
            order.setOrderStatus("completed");
            order.setAmount(BigDecimal.ZERO);   // 权益扣减，金额为 0
            order.setCurrencyCode("CNY");
            order.setBizType("tender_download");
            order.setBizId(t.getId());
            Date now = new Date();
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            orderService.save(order);
            log.info("[TenderDownload] 首次下载 userId={} tenderId={} orderId={}", userId, t.getId(), order.getId());
        } else {
            log.debug("[TenderDownload] 重复下载 userId={} tenderId={}", userId, t.getId());
        }

        Map<String, Object> m = toItem(t);
        m.put("description", t.getSummary() == null ? "" : t.getSummary());
        m.put("isDuplicate", !isFirstDownload);
        return V3Response.success(m);
    }

    /**
     * Check download quota. Returns null if OK, or error response if exceeded.
     * Skips check if the user already downloaded this specific tender (duplicate).
     */
    private V3Response<Map<String, Object>> checkDownloadQuota(Long userId, Long tenderId) {
        // Skip check if already downloaded (duplicate)
        QueryWrapper<TenderDownloadLog> dlQw = new QueryWrapper<>();
        dlQw.eq("user_id", userId).eq("tender_id", tenderId);
        if (downloadLogService.count(dlQw) > 0) {
            return null; // already downloaded, allow re-download
        }

        // Determine base quota from member_level
        int baseQuota = 0;
        QueryWrapper<MemberProfile> mpQw = new QueryWrapper<>();
        mpQw.eq("user_id", userId);
        MemberProfile mp = memberProfileService.getOne(mpQw);
        if (mp != null && mp.getMemberLevel() != null) {
            baseQuota = switch (mp.getMemberLevel()) {
                case "vip" -> 50;
                case "svip" -> 200;
                default -> 0;
            };
        }

        // Granted extra from benefit_grant_logs
        Integer grantedExtra = jdbcTemplate.queryForObject(
                "SELECT ISNULL(SUM(amount), 0) FROM benefit_grant_logs " +
                "WHERE member_id = ? AND benefit_type = 'tender_download'",
                Integer.class, userId
        );
        int totalQuota = baseQuota + (grantedExtra == null ? 0 : grantedExtra);

        // Count downloads this year
        Integer usedThisYear = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tender_download_logs " +
                "WHERE user_id = ? AND downloaded_at >= DATEFROMPARTS(YEAR(GETUTCDATE()), 1, 1)",
                Integer.class, userId
        );
        int used = usedThisYear == null ? 0 : usedThisYear;

        if (used >= totalQuota) {
            return V3Response.error("QUOTA_EXCEEDED",
                    "download quota exceeded: used=" + used + ", total=" + totalQuota);
        }

        return null;
    }

    /** 生成下载 order 编号：DL-{userId}-{tenderId}-{时间戳尾8位} */
    private String genOrderNo(Long userId, Long tenderId) {
        String ts = String.valueOf(System.currentTimeMillis());
        return "DL-" + userId + "-" + tenderId + "-" + ts.substring(ts.length() - 8);
    }

    private Map<String, Object> toItem(TenderEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("tenderNo", t.getTenderNo() == null ? "" : t.getTenderNo());
        m.put("title", t.getTitle() == null ? "" : t.getTitle());
        m.put("organization", t.getOrganizationName() == null ? "" : t.getOrganizationName());
        m.put("country", t.getCountry() == null ? "" : t.getCountry());
        String cat = (t.getCategory() == null || t.getCategory().isBlank()) ? "other" : t.getCategory();
        m.put("category", cat);
        m.put("categoryLabel", mapCategoryLabel(cat));
        m.put("price", t.getPrice() == null ? BigDecimal.ZERO : t.getPrice());
        m.put("isTop", Boolean.TRUE.equals(t.getIsTop()));
        m.put("publishDate", formatDate(t.getCreatedAt()));
        m.put("deadline", formatDateTime(t.getDeadlineAt()));
        return m;
    }

    private String mapCategoryLabel(String category) {
        return switch (category) {
            case "medical" -> "医疗器械";
            case "it" -> "IT设备";
            case "construction" -> "建筑工程";
            case "office" -> "办公用品";
            case "consulting" -> "咨询服务";
            default -> "其他";
        };
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String formatDateTime(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
