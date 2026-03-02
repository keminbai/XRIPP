package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Demand;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IDemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/member/demands")
@RequiredArgsConstructor
public class MemberDemandsV3Controller {

    private final IDemandService demandService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        QueryWrapper<Demand> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.orderByDesc("created_at");

        Page<Demand> p = new Page<>(page, pageSize);
        Page<Demand> result = demandService.page(p, qw);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        if (title.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "title is required");
        }

        Demand d = new Demand();
        d.setTitle(title);
        d.setUserId(userId);
        d.setOrgType(strVal(body, "orgType"));
        d.setOrgName(strVal(body, "orgName"));
        d.setCategory(strVal(body, "category"));
        d.setIndustry(strVal(body, "industry"));
        d.setSummary(strVal(body, "summary"));
        d.setEnableSms(Boolean.TRUE.equals(body.get("enableSms")));
        d.setAuditStatus(0);
        d.setCreatedAt(new Date());

        // parse dates
        d.setPublishDate(parseDate(body, "publishDate"));
        d.setDeadline(parseDate(body, "deadline"));

        demandService.save(d);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", d.getId());
        result.put("demandNo", "DEM-" + String.format("%07d", d.getId()));
        return V3Response.success(result);
    }

    @DeleteMapping("/{id}")
    public V3Response<Void> delete(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        Demand d = demandService.getById(id);
        if (d == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "demand not found");
        }
        if (!userId.equals(d.getUserId())) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }
        if (d.getAuditStatus() != null && d.getAuditStatus() != 0) {
            return V3Response.error("STATE_INVALID_TRANSITION", "only pending demands can be deleted");
        }

        demandService.removeById(id);
        return V3Response.success(null);
    }

    private Map<String, Object> toItem(Demand d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("demandNo", "DEM-" + String.format("%07d", d.getId()));
        m.put("title", d.getTitle() != null ? d.getTitle() : "");
        m.put("orgType", d.getOrgType() != null ? d.getOrgType() : "");
        m.put("orgName", d.getOrgName() != null ? d.getOrgName() : "");
        m.put("publishDate", fmtDate(d.getPublishDate() != null ? d.getPublishDate() : d.getCreatedAt()));
        m.put("deadline", fmtDate(d.getDeadline()));
        m.put("category", d.getCategory() != null ? d.getCategory() : "");
        m.put("industry", d.getIndustry() != null ? d.getIndustry() : "");
        m.put("summary", d.getSummary() != null ? d.getSummary() : "");
        m.put("status", mapStatusLabel(d.getAuditStatus()));
        m.put("date", fmtDate(d.getCreatedAt()));
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? String.valueOf(v).trim() : null;
    }

    private Date parseDate(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(v).substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private String mapStatusLabel(Integer s) {
        if (s == null || s == 0) return "审核中";
        if (s == 30) return "已发布";
        if (s == 40) return "已驳回";
        return "审核中";
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
