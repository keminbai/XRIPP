package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v3/dashboard")
@RequiredArgsConstructor
public class DashboardV3Controller {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public V3Response<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Totals
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("tenderCount", queryInt("SELECT COUNT(*) FROM tenders WHERE tender_status = 'published'"));
        totals.put("memberCount", queryInt("SELECT COUNT(*) FROM member_profile"));
        totals.put("countryCount", queryInt("SELECT COUNT(DISTINCT country) FROM tenders WHERE tender_status = 'published' AND country IS NOT NULL AND country <> ''"));
        totals.put("monthlyNewMembers", queryInt(
                "SELECT COUNT(*) FROM member_profile WHERE created_at >= DATEADD(MONTH, DATEDIFF(MONTH, 0, GETUTCDATE()), 0)"));
        result.put("totals", totals);

        // 2. Tenders by category
        List<Map<String, Object>> tendersByCategory = jdbcTemplate.queryForList(
                "SELECT category AS name, COUNT(*) AS value FROM tenders " +
                "WHERE tender_status = 'published' AND category IS NOT NULL AND category <> '' " +
                "GROUP BY category ORDER BY COUNT(*) DESC");
        result.put("tendersByCategory", tendersByCategory);

        // 3. Tenders by country TOP 5
        List<Map<String, Object>> tendersByCountry = jdbcTemplate.queryForList(
                "SELECT TOP 5 country AS name, COUNT(*) AS value FROM tenders " +
                "WHERE tender_status = 'published' AND country IS NOT NULL AND country <> '' " +
                "GROUP BY country ORDER BY COUNT(*) DESC");
        result.put("tendersByCountry", tendersByCountry);

        // 4. Member trend (last 6 months)
        List<Map<String, Object>> memberTrend = jdbcTemplate.queryForList(
                "SELECT CONVERT(VARCHAR(7), created_at, 120) AS month, COUNT(*) AS count " +
                "FROM member_profile " +
                "WHERE created_at >= DATEADD(MONTH, -6, GETUTCDATE()) " +
                "GROUP BY CONVERT(VARCHAR(7), created_at, 120) " +
                "ORDER BY month");
        result.put("memberTrend", memberTrend);

        // 5. Members by industry
        List<Map<String, Object>> membersByIndustry = jdbcTemplate.queryForList(
                "SELECT industry AS name, COUNT(*) AS value FROM member_profile " +
                "WHERE industry IS NOT NULL AND industry <> '' " +
                "GROUP BY industry ORDER BY COUNT(*) DESC");
        result.put("membersByIndustry", membersByIndustry);

        // 6. Recent companies (last 15)
        List<String> recentCompanies = jdbcTemplate.queryForList(
                "SELECT TOP 15 company_name FROM member_profile " +
                "WHERE company_name IS NOT NULL AND company_name <> '' " +
                "ORDER BY created_at DESC",
                String.class);
        result.put("recentCompanies", recentCompanies);

        return V3Response.success(result);
    }

    private int queryInt(String sql) {
        Integer val = jdbcTemplate.queryForObject(sql, Integer.class);
        return val == null ? 0 : val;
    }
}
