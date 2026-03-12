package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OverseasPoint;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOverseasPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/overseas-points")
@RequiredArgsConstructor
public class AdminOverseasPointsV3Controller {

    private final IOverseasPointService pointService;

    // ─── LIST (paginated) ───────────────────────────────────────
    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String continent,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<OverseasPoint> qw = new QueryWrapper<>();
        if (continent != null && !continent.isBlank()) qw.eq("continent", continent.trim());
        if (country != null && !country.isBlank()) qw.eq("country", country.trim());
        if (status != null && !status.isBlank()) qw.eq("status", status.trim());
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("name", kw).or().like("city", kw).or().like("country", kw).or().like("manager", kw));
        }
        qw.orderByDesc("id");

        Page<OverseasPoint> p = new Page<>(page, pageSize);
        Page<OverseasPoint> result = pointService.page(p, qw);

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    // ─── STATS ──────────────────────────────────────────────────
    @GetMapping("/stats")
    public V3Response<Map<String, Object>> stats() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }
        long total = pointService.count();
        long active = pointService.count(new QueryWrapper<OverseasPoint>().eq("status", "active"));
        long inactive = pointService.count(new QueryWrapper<OverseasPoint>().eq("status", "inactive"));

        // distinct countries
        QueryWrapper<OverseasPoint> cqw = new QueryWrapper<>();
        cqw.select("DISTINCT country");
        long countries = pointService.list(cqw).stream().map(OverseasPoint::getCountry).distinct().count();

        // distinct continents
        QueryWrapper<OverseasPoint> ctqw = new QueryWrapper<>();
        ctqw.select("DISTINCT continent");
        long continents = pointService.list(ctqw).stream().map(OverseasPoint::getContinent).distinct().count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("active", active);
        data.put("inactive", inactive);
        data.put("countries", countries);
        data.put("continents", continents);
        return V3Response.success(data);
    }

    // ─── CREATE ─────────────────────────────────────────────────
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        String name = strVal(body, "name");
        String continent = strVal(body, "continent");
        String country = strVal(body, "country");
        String city = strVal(body, "city");
        if (name.isEmpty() || continent.isEmpty() || country.isEmpty() || city.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "name, continent, country, city are required");
        }

        BigDecimal lat = decVal(body, "lat");
        BigDecimal lng = decVal(body, "lng");
        if (lat == null || lng == null) {
            return V3Response.error("VALIDATION_ERROR", "lat and lng are required");
        }

        OverseasPoint pt = new OverseasPoint();
        pt.setName(name);
        pt.setContinent(continent);
        pt.setCountry(country);
        pt.setCity(city);
        pt.setLat(lat);
        pt.setLng(lng);
        pt.setManager(strVal(body, "manager"));
        pt.setPhone(strVal(body, "phone"));
        pt.setEmail(strVal(body, "email"));
        pt.setAddress(strVal(body, "address"));
        pt.setServicesJson(strVal(body, "services_json"));
        pt.setServiceType(strVal(body, "service_type"));
        pt.setDescription(strVal(body, "description"));
        pt.setCoverImage(strVal(body, "cover_image"));
        pt.setRating(decVal(body, "rating") != null ? decVal(body, "rating") : new BigDecimal("5.0"));
        pt.setResponseTime(strVal(body, "response_time").isEmpty() ? "2小时" : strVal(body, "response_time"));
        pt.setSuccessCases(intVal(body, "success_cases"));
        pt.setStatus(strVal(body, "status").isEmpty() ? "active" : strVal(body, "status"));
        pt.setPartnerId(strVal(body, "partner_id"));
        pt.setCreatedAt(new Date());
        pt.setUpdatedAt(new Date());

        pointService.save(pt);

        return V3Response.success(Map.of("id", pt.getId()));
    }

    // ─── UPDATE ─────────────────────────────────────────────────
    @PutMapping("/{id}")
    public V3Response<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        OverseasPoint pt = pointService.getById(id);
        if (pt == null) return V3Response.error("RESOURCE_NOT_FOUND", "point not found");

        if (body.containsKey("name"))           pt.setName(strVal(body, "name"));
        if (body.containsKey("continent"))      pt.setContinent(strVal(body, "continent"));
        if (body.containsKey("country"))        pt.setCountry(strVal(body, "country"));
        if (body.containsKey("city"))           pt.setCity(strVal(body, "city"));
        if (body.containsKey("lat"))            pt.setLat(decVal(body, "lat"));
        if (body.containsKey("lng"))            pt.setLng(decVal(body, "lng"));
        if (body.containsKey("manager"))        pt.setManager(strVal(body, "manager"));
        if (body.containsKey("phone"))          pt.setPhone(strVal(body, "phone"));
        if (body.containsKey("email"))          pt.setEmail(strVal(body, "email"));
        if (body.containsKey("address"))        pt.setAddress(strVal(body, "address"));
        if (body.containsKey("services_json"))  pt.setServicesJson(strVal(body, "services_json"));
        if (body.containsKey("service_type"))   pt.setServiceType(strVal(body, "service_type"));
        if (body.containsKey("description"))    pt.setDescription(strVal(body, "description"));
        if (body.containsKey("cover_image"))    pt.setCoverImage(strVal(body, "cover_image"));
        if (body.containsKey("rating"))         pt.setRating(decVal(body, "rating"));
        if (body.containsKey("response_time"))  pt.setResponseTime(strVal(body, "response_time"));
        if (body.containsKey("success_cases"))  pt.setSuccessCases(intVal(body, "success_cases"));
        if (body.containsKey("status"))         pt.setStatus(strVal(body, "status"));
        if (body.containsKey("partner_id"))     pt.setPartnerId(strVal(body, "partner_id"));
        pt.setUpdatedAt(new Date());

        pointService.updateById(pt);
        return V3Response.success(null);
    }

    // ─── DELETE ──────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public V3Response<Void> delete(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        OverseasPoint pt = pointService.getById(id);
        if (pt == null) return V3Response.error("RESOURCE_NOT_FOUND", "point not found");

        pointService.removeById(id);
        return V3Response.success(null);
    }

    // ─── Helpers ─────────────────────────────────────────────────
    private Map<String, Object> toItem(OverseasPoint p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("partnerId", safe(p.getPartnerId()));
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
        m.put("status", safe(p.getStatus()));
        m.put("createdAt", fmtDate(p.getCreatedAt()));
        m.put("updatedAt", fmtDate(p.getUpdatedAt()));
        return m;
    }

    private String safe(String s) { return s == null ? "" : s; }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private BigDecimal decVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        try { return new BigDecimal(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private Integer intVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return 0;
        try { return Integer.parseInt(v.toString().trim()); } catch (Exception e) { return 0; }
    }

    private String fmtDate(Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }
}
