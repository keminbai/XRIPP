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
        totals.put("organizationCount", queryInt(
                "SELECT COUNT(DISTINCT organization_name) FROM tenders " +
                "WHERE tender_status = 'published' AND organization_name IS NOT NULL AND organization_name <> ''"));
        totals.put("monthlyTenderCount", queryInt(
                "SELECT COUNT(*) FROM tenders " +
                "WHERE tender_status = 'published' AND created_at >= DATEADD(MONTH, DATEDIFF(MONTH, 0, GETUTCDATE()), 0)"));
        totals.put("industryCount", queryInt(
                "SELECT COUNT(DISTINCT industry) FROM member_profile WHERE industry IS NOT NULL AND industry <> ''"));
        totals.put("svipCount", queryInt(
                "SELECT COUNT(*) FROM member_profile WHERE UPPER(ISNULL(member_level, 'NORMAL')) = 'SVIP'"));
        totals.put("vipCount", queryInt(
                "SELECT COUNT(*) FROM member_profile WHERE UPPER(ISNULL(member_level, 'NORMAL')) = 'VIP'"));
        totals.put("normalCount", queryInt(
                "SELECT COUNT(*) FROM member_profile WHERE UPPER(ISNULL(member_level, 'NORMAL')) = 'NORMAL'"));
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

        // 4. Tenders by organization TOP 10
        List<Map<String, Object>> tendersByOrganization = jdbcTemplate.queryForList(
                "SELECT TOP 10 organization_name AS name, COUNT(*) AS value FROM tenders " +
                "WHERE tender_status = 'published' AND organization_name IS NOT NULL AND organization_name <> '' " +
                "GROUP BY organization_name ORDER BY COUNT(*) DESC");
        result.put("tendersByOrganization", tendersByOrganization);

        // 5. Member trend (last 6 months)
        List<Map<String, Object>> memberTrend = jdbcTemplate.queryForList(
                "SELECT CONVERT(VARCHAR(7), created_at, 120) AS month, COUNT(*) AS count " +
                "FROM member_profile " +
                "WHERE created_at >= DATEADD(MONTH, -6, GETUTCDATE()) " +
                "GROUP BY CONVERT(VARCHAR(7), created_at, 120) " +
                "ORDER BY month");
        result.put("memberTrend", memberTrend);

        // 6. Members by industry
        List<Map<String, Object>> membersByIndustry = jdbcTemplate.queryForList(
                "SELECT industry AS name, COUNT(*) AS value FROM member_profile " +
                "WHERE industry IS NOT NULL AND industry <> '' " +
                "GROUP BY industry ORDER BY COUNT(*) DESC");
        result.put("membersByIndustry", membersByIndustry);

        // 7. Member levels
        List<Map<String, Object>> memberLevels = jdbcTemplate.queryForList(
                "SELECT UPPER(ISNULL(member_level, 'NORMAL')) AS name, COUNT(*) AS value " +
                "FROM member_profile GROUP BY UPPER(ISNULL(member_level, 'NORMAL')) " +
                "ORDER BY COUNT(*) DESC");
        result.put("memberLevels", memberLevels);

        // 8. Recent companies (last 15)
        List<String> recentCompanies = jdbcTemplate.queryForList(
                "SELECT TOP 15 company_name FROM member_profile " +
                "WHERE company_name IS NOT NULL AND company_name <> '' " +
                "ORDER BY created_at DESC",
                String.class);
        result.put("recentCompanies", recentCompanies);

        // 9. Member cities (proxy via partner service city)
        List<Map<String, Object>> memberCities = jdbcTemplate.queryForList(
                "SELECT TOP 12 p.city_name AS name, COUNT(*) AS value " +
                "FROM member_profile mp " +
                "JOIN sys_user su ON su.id = mp.user_id " +
                "JOIN partners p ON p.id = su.partner_id " +
                "WHERE p.city_name IS NOT NULL AND p.city_name <> '' " +
                "GROUP BY p.city_name ORDER BY COUNT(*) DESC");
        result.put("memberCities", memberCities);

        // 10. Province heat (active partners)
        List<Map<String, Object>> provinceHeat = jdbcTemplate.queryForList(
                "SELECT province AS name, COUNT(*) AS value FROM partners " +
                "WHERE status = 1 AND province IS NOT NULL AND province <> '' " +
                "GROUP BY province ORDER BY COUNT(*) DESC");
        result.put("provinceHeat", provinceHeat);

        // 11. Domestic network (active partners)
        Map<String, Object> domesticNetwork = new LinkedHashMap<>();
        domesticNetwork.put("count", queryInt("SELECT COUNT(*) FROM partners WHERE status = 1"));
        domesticNetwork.put("provinces", queryInt(
                "SELECT COUNT(DISTINCT province) FROM partners WHERE status = 1 AND province IS NOT NULL AND province <> ''"));
        domesticNetwork.put("cities", queryInt(
                "SELECT COUNT(DISTINCT city_name) FROM partners WHERE status = 1 AND city_name IS NOT NULL AND city_name <> ''"));
        domesticNetwork.put("provinceRank", jdbcTemplate.queryForList(
                "SELECT TOP 10 province AS name, COUNT(*) AS value FROM partners " +
                "WHERE status = 1 AND province IS NOT NULL AND province <> '' " +
                "GROUP BY province ORDER BY COUNT(*) DESC"));
        domesticNetwork.put("cityPoints", jdbcTemplate.queryForList(
                "SELECT TOP 20 city_name AS name, COUNT(*) AS value FROM partners " +
                "WHERE status = 1 AND city_name IS NOT NULL AND city_name <> '' " +
                "GROUP BY city_name ORDER BY COUNT(*) DESC"));
        domesticNetwork.put("list", jdbcTemplate.queryForList(
                "SELECT TOP 15 partner_name AS name, city_name AS location FROM partners " +
                "WHERE status = 1 ORDER BY created_at DESC, id DESC"));
        result.put("domesticNetwork", domesticNetwork);

        // 12. Overseas network (active overseas points)
        Map<String, Object> overseasNetwork = new LinkedHashMap<>();
        overseasNetwork.put("count", queryInt("SELECT COUNT(*) FROM overseas_points WHERE status = 'active'"));
        overseasNetwork.put("countries", queryInt(
                "SELECT COUNT(DISTINCT country) FROM overseas_points " +
                "WHERE status = 'active' AND country IS NOT NULL AND country <> ''"));
        overseasNetwork.put("cities", queryInt(
                "SELECT COUNT(DISTINCT city) FROM overseas_points " +
                "WHERE status = 'active' AND city IS NOT NULL AND city <> ''"));
        overseasNetwork.put("countryRank", jdbcTemplate.queryForList(
                "SELECT TOP 10 country AS name, COUNT(*) AS value FROM overseas_points " +
                "WHERE status = 'active' AND country IS NOT NULL AND country <> '' " +
                "GROUP BY country ORDER BY COUNT(*) DESC"));
        overseasNetwork.put("points", jdbcTemplate.queryForList(
                "SELECT TOP 50 name, country, city, lat, lng, ISNULL(success_cases, 1) AS value " +
                "FROM overseas_points " +
                "WHERE status = 'active' AND lat IS NOT NULL AND lng IS NOT NULL " +
                "ORDER BY ISNULL(success_cases, 0) DESC, id DESC"));
        overseasNetwork.put("list", jdbcTemplate.queryForList(
                "SELECT TOP 15 name, " +
                "CONCAT(country, CASE WHEN city IS NOT NULL AND city <> '' THEN ' · ' + city ELSE '' END) AS location " +
                "FROM overseas_points WHERE status = 'active' ORDER BY ISNULL(success_cases, 0) DESC, id DESC"));
        result.put("overseasNetwork", overseasNetwork);

        return V3Response.success(result);
    }

    private int queryInt(String sql) {
        Integer val = jdbcTemplate.queryForObject(sql, Integer.class);
        return val == null ? 0 : val;
    }
}
