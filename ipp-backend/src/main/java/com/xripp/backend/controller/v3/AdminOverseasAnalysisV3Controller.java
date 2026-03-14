package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OverseasPoint;
import com.xripp.backend.entity.OverseasReport;
import com.xripp.backend.entity.OverseasService;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOverseasPointService;
import com.xripp.backend.service.IOverseasReportService;
import com.xripp.backend.service.IOverseasServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/overseas/analysis")
@RequiredArgsConstructor
public class AdminOverseasAnalysisV3Controller {

    private final IOverseasPointService overseasPointService;
    private final IOverseasServiceService overseasServiceService;
    private final IOverseasReportService overseasReportService;
    @GetMapping
    public V3Response<Map<String, Object>> overview(
            @RequestParam(defaultValue = "12") int range
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        int normalizedRange = List.of(3, 6, 12).contains(range) ? range : 12;
        List<OverseasPoint> points = overseasPointService.list(
                new QueryWrapper<OverseasPoint>()
                        .eq("status", "active")
                        .orderByDesc("id")
        );
        List<OverseasService> services = overseasServiceService.list(
                new QueryWrapper<OverseasService>()
                        .ne("status", "draft")
                        .orderByAsc("published_at")
                        .orderByAsc("created_at")
                        .orderByAsc("id")
        );
        List<OverseasReport> reports = overseasReportService.list(
                new QueryWrapper<OverseasReport>()
                        .orderByDesc("publish_date")
                        .orderByDesc("id")
        );

        Map<String, Integer> pointCounter = new LinkedHashMap<>();
        for (OverseasPoint point : points) {
            String country = safe(point.getCountry());
            if (!country.isEmpty()) {
                pointCounter.merge(country, 1, Integer::sum);
            }
        }

        Map<String, Integer> serviceTypeCounter = new LinkedHashMap<>();
        for (OverseasService service : services) {
            String type = safe(service.getServiceType());
            if (!type.isEmpty()) {
                serviceTypeCounter.merge(type, 1, Integer::sum);
            }
        }

        Map<String, Integer> industryCounter = new LinkedHashMap<>();
        for (OverseasReport report : reports) {
            String industry = safe(report.getIndustryCode());
            if (!industry.isEmpty()) {
                industryCounter.merge(industry, 1, Integer::sum);
            }
        }

        Set<String> companyKeys = new LinkedHashSet<>();
        for (OverseasService service : services) {
            String key = firstNonBlank(
                    safe(service.getContactEmail()).toLowerCase(Locale.ROOT),
                    safe(service.getContactPhone()),
                    safe(service.getTitle()),
                    service.getId() == null ? "" : String.valueOf(service.getId())
            );
            if (!key.isEmpty()) {
                companyKeys.add(key);
            }
        }

        List<Map<String, Object>> trend = buildTrend(services, normalizedRange);
        int monthlyNew = trend.isEmpty() ? 0 : intVal(trend.get(trend.size() - 1).get("newCount"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", List.of(
                statItem("服务网点", points.size(), "当前运营网点"),
                statItem("服务企业", companyKeys.size(), "累计服务企业"),
                statItem("覆盖国家", pointCounter.keySet().size(), "重点市场覆盖"),
                statItem("行业报告", reports.size(), "报告库累计上传")
        ));
        result.put("summary", Map.of(
                "totalPoints", points.size(),
                "totalCompanies", companyKeys.size(),
                "coveredCountries", pointCounter.keySet().size(),
                "totalReports", reports.size(),
                "monthlyNew", monthlyNew
        ));
        result.put("networkDistribution", pointCounter.entrySet().stream()
                .map(entry -> mapOf("country", entry.getKey(), "count", entry.getValue()))
                .toList());
        result.put("serviceTypes", serviceTypeCounter.entrySet().stream()
                .map(entry -> mapOf("type", entry.getKey(), "count", entry.getValue()))
                .toList());
        result.put("industryDistribution", toIndustryDistribution(industryCounter));
        result.put("trend", trend);
        result.put("pointCountries", pointCounter.keySet());
        result.put("updatedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        return V3Response.success(result);
    }

    private List<Map<String, Object>> buildTrend(List<OverseasService> services, int range) {
        ZoneId zoneId = ZoneId.systemDefault();
        YearMonth now = YearMonth.now(zoneId);
        YearMonth firstMonth = now.minusMonths(range - 1L);
        Map<YearMonth, Integer> monthlyNewCounter = new LinkedHashMap<>();
        for (int i = range - 1; i >= 0; i--) {
            monthlyNewCounter.put(now.minusMonths(i), 0);
        }

        int baseline = 0;
        for (OverseasService service : services) {
            Date sourceDate = service.getPublishedAt() != null ? service.getPublishedAt() : service.getCreatedAt();
            if (sourceDate == null) {
                continue;
            }
            YearMonth month = YearMonth.from(sourceDate.toInstant().atZone(zoneId));
            if (monthlyNewCounter.containsKey(month)) {
                monthlyNewCounter.put(month, monthlyNewCounter.get(month) + 1);
            } else if (month.isBefore(firstMonth)) {
                baseline++;
            }
        }

        int cumulative = baseline;
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<YearMonth, Integer> entry : monthlyNewCounter.entrySet()) {
            cumulative += entry.getValue();
            items.add(mapOf(
                    "month", entry.getKey().toString(),
                    "newCount", entry.getValue(),
                    "total", cumulative
            ));
        }
        return items;
    }

    private List<Map<String, Object>> toIndustryDistribution(Map<String, Integer> counter) {
        int total = counter.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            return new ArrayList<>();
        }
        return counter.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(entry -> {
                    double percentage = ((double) entry.getValue() / total) * 100.0d;
                    return mapOf(
                            "industry", entry.getKey(),
                            "count", entry.getValue(),
                            "percentage", Double.parseDouble(String.format(Locale.US, "%.1f", percentage))
                    );
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> statItem(String label, int value, String subLabel) {
        return mapOf("label", label, "value", value, "subLabel", subLabel);
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private int intVal(Object value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
