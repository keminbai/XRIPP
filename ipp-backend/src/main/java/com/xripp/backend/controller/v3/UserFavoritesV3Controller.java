package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Activity;
import com.xripp.backend.entity.Suppliers;
import com.xripp.backend.entity.TenderEntity;
import com.xripp.backend.entity.UserFavorite;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/member/favorites")
@RequiredArgsConstructor
public class UserFavoritesV3Controller {

    private final IUserFavoriteService favoriteService;
    private final ITenderService tenderService;
    private final IActivityService activityService;
    private final ISuppliersService suppliersService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "target_type", required = false) String targetType
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) return V3Response.error("AUTH_UNAUTHORIZED", "login required");

        QueryWrapper<UserFavorite> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (targetType != null && !targetType.isBlank()) {
            qw.eq("target_type", targetType.trim());
        }
        qw.orderByDesc("created_at");

        Page<UserFavorite> p = new Page<>(page, pageSize);
        Page<UserFavorite> result = favoriteService.page(p, qw);

        // Group by type to batch-load targets
        Map<String, List<UserFavorite>> grouped = result.getRecords().stream()
                .collect(Collectors.groupingBy(UserFavorite::getTargetType));

        Map<String, Map<Long, Map<String, Object>>> targetCache = new HashMap<>();
        targetCache.put("tender", loadTenders(grouped.getOrDefault("tender", List.of())));
        targetCache.put("activity", loadActivities(grouped.getOrDefault("activity", List.of())));
        targetCache.put("supplier", loadSuppliers(grouped.getOrDefault("supplier", List.of())));

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(fav -> toItem(fav, targetCache))
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/ids")
    public V3Response<Map<String, Object>> ids(
            @RequestParam(name = "target_type", defaultValue = "tender") String targetType
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) return V3Response.error("AUTH_UNAUTHORIZED", "login required");

        List<UserFavorite> favs = favoriteService.list(
                new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("target_type", targetType)
                        .select("target_id")
        );
        List<Long> ids = favs.stream().map(UserFavorite::getTargetId).collect(Collectors.toList());
        return V3Response.success(Map.of("ids", ids));
    }

    @PostMapping
    public V3Response<Map<String, Object>> add(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) return V3Response.error("AUTH_UNAUTHORIZED", "login required");

        String targetType = strVal(body, "target_type");
        Long targetId = longVal(body, "target_id");
        if (targetType.isBlank() || targetId == null) {
            return V3Response.error("VALIDATION_ERROR", "target_type and target_id required");
        }
        if (!Set.of("tender", "activity", "supplier").contains(targetType)) {
            return V3Response.error("VALIDATION_ERROR", "invalid target_type");
        }

        // Idempotent: check if already exists
        long exists = favoriteService.count(new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId));
        if (exists > 0) {
            return V3Response.success(Map.of("id", targetId, "already_exists", true));
        }

        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setTargetType(targetType);
        fav.setTargetId(targetId);
        fav.setCreatedAt(new Date());
        favoriteService.save(fav);

        return V3Response.success(Map.of("id", fav.getId()));
    }

    @DeleteMapping
    public V3Response<Void> remove(
            @RequestParam(name = "target_type") String targetType,
            @RequestParam(name = "target_id") Long targetId
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) return V3Response.error("AUTH_UNAUTHORIZED", "login required");

        favoriteService.remove(new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .eq("target_type", targetType)
                .eq("target_id", targetId));
        return V3Response.success(null);
    }

    @DeleteMapping("/{id}")
    public V3Response<Void> removeById(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) return V3Response.error("AUTH_UNAUTHORIZED", "login required");

        UserFavorite fav = favoriteService.getById(id);
        if (fav == null) return V3Response.error("RESOURCE_NOT_FOUND", "favorite not found");
        if (!userId.equals(fav.getUserId())) return V3Response.error("AUTH_FORBIDDEN", "forbidden");

        favoriteService.removeById(id);
        return V3Response.success(null);
    }

    // --- Batch loaders ---

    private Map<Long, Map<String, Object>> loadTenders(List<UserFavorite> favs) {
        if (favs.isEmpty()) return Map.of();
        Set<Long> ids = favs.stream().map(UserFavorite::getTargetId).collect(Collectors.toSet());
        List<TenderEntity> tenders = tenderService.listByIds(ids);
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (TenderEntity t : tenders) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", t.getTitle() != null ? t.getTitle() : "");
            m.put("description", t.getSummary() != null ? t.getSummary() : "");
            m.put("organization", t.getOrganizationName() != null ? t.getOrganizationName() : "");
            m.put("location", t.getCountry() != null ? t.getCountry() : "");
            m.put("deadline", fmtDate(t.getDeadlineAt()));
            m.put("amount", t.getPrice() != null ? "¥" + t.getPrice() : "");
            m.put("isExpired", t.getDeadlineAt() != null && t.getDeadlineAt().before(new Date()));
            result.put(t.getId(), m);
        }
        return result;
    }

    private Map<Long, Map<String, Object>> loadActivities(List<UserFavorite> favs) {
        if (favs.isEmpty()) return Map.of();
        Set<Long> ids = favs.stream().map(UserFavorite::getTargetId).collect(Collectors.toSet());
        List<Activity> activities = activityService.listByIds(ids);
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Activity a : activities) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", a.getTitle() != null ? a.getTitle() : "");
            m.put("description", a.getSummary() != null ? a.getSummary() : "");
            m.put("organization", "");
            m.put("location", a.getCityName() != null ? a.getCityName() : "");
            m.put("deadline", fmtDate(a.getStartTime()));
            m.put("amount", a.getIsFree() != null && a.getIsFree() ? "免费" : a.getFee() != null ? "¥" + a.getFee() : "");
            m.put("isExpired", a.getStartTime() != null && a.getStartTime().before(new Date()));
            result.put(a.getId(), m);
        }
        return result;
    }

    private Map<Long, Map<String, Object>> loadSuppliers(List<UserFavorite> favs) {
        if (favs.isEmpty()) return Map.of();
        Set<Long> ids = favs.stream().map(UserFavorite::getTargetId).collect(Collectors.toSet());
        List<Suppliers> suppliers = suppliersService.listByIds(ids);
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Suppliers s : suppliers) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", s.getCompanyName() != null ? s.getCompanyName() : "");
            m.put("description", "服务商");
            m.put("organization", "");
            m.put("location", "");
            m.put("deadline", null);
            m.put("amount", null);
            m.put("isExpired", false);
            result.put(s.getId(), m);
        }
        return result;
    }

    private Map<String, Object> toItem(UserFavorite fav, Map<String, Map<Long, Map<String, Object>>> cache) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", fav.getId());
        m.put("type", fav.getTargetType());
        m.put("targetId", fav.getTargetId());
        m.put("favoriteTime", fmtDateTime(fav.getCreatedAt()));

        Map<Long, Map<String, Object>> typeCache = cache.getOrDefault(fav.getTargetType(), Map.of());
        Map<String, Object> target = typeCache.getOrDefault(fav.getTargetId(), Map.of());
        m.put("title", target.getOrDefault("title", "已删除"));
        m.put("description", target.getOrDefault("description", ""));
        m.put("organization", target.getOrDefault("organization", ""));
        m.put("location", target.getOrDefault("location", ""));
        m.put("deadline", target.get("deadline"));
        m.put("amount", target.get("amount"));
        m.put("isExpired", target.getOrDefault("isExpired", false));
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private Long longVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        try { return Long.parseLong(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private String fmtDate(Date d) {
        if (d == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    private String fmtDateTime(Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }
}
