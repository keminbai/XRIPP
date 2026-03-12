package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OverseasPoint;
import com.xripp.backend.service.IOverseasPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Public (no auth) endpoint for overseas service points.
 * Returns only active points for the portal map & cards.
 */
@RestController
@RequestMapping("/v3/overseas-points")
@RequiredArgsConstructor
public class OverseasPointsV3Controller {

    private final IOverseasPointService pointService;

    @GetMapping
    public V3Response<List<Map<String, Object>>> list(
            @RequestParam(required = false) String continent,
            @RequestParam(required = false) String country,
            @RequestParam(name = "service_type", required = false) String serviceType
    ) {
        QueryWrapper<OverseasPoint> qw = new QueryWrapper<>();
        qw.eq("status", "active");
        if (continent != null && !continent.isBlank()) {
            qw.eq("continent", continent.trim());
        }
        if (country != null && !country.isBlank()) {
            qw.eq("country", country.trim());
        }
        if (serviceType != null && !serviceType.isBlank()) {
            qw.eq("service_type", serviceType.trim());
        }
        qw.orderByDesc("success_cases");

        List<OverseasPoint> points = pointService.list(qw);
        List<Map<String, Object>> items = points.stream()
                .map(this::toPublicItem)
                .collect(Collectors.toList());

        return V3Response.success(items);
    }

    private Map<String, Object> toPublicItem(OverseasPoint p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", safe(p.getName()));
        m.put("continent", safe(p.getContinent()));
        m.put("country", safe(p.getCountry()));
        m.put("city", safe(p.getCity()));
        m.put("lat", p.getLat());
        m.put("lng", p.getLng());
        m.put("manager", safe(p.getManager()));
        m.put("phone", safe(p.getPhone()));
        m.put("email", safe(p.getEmail()));
        m.put("address", safe(p.getAddress()));
        m.put("servicesJson", safe(p.getServicesJson()));
        m.put("serviceType", safe(p.getServiceType()));
        m.put("description", safe(p.getDescription()));
        m.put("coverImage", safe(p.getCoverImage()));
        m.put("rating", p.getRating());
        m.put("responseTime", safe(p.getResponseTime()));
        m.put("successCases", p.getSuccessCases());
        return m;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
