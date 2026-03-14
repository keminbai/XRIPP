package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.security.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/v3/admin/logs")
@RequiredArgsConstructor
public class AdminLogsV3Controller {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(name = "date_from", required = false) String dateFrom,
            @RequestParam(name = "date_to", required = false) String dateTo,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        long safePage = Math.max(page, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), 200);
        int offset = (int) ((safePage - 1) * safePageSize);
        String normalizedSource = normalizeSource(source);

        List<String> unionParts = new ArrayList<>();
        List<Object> queryParams = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();

        if (normalizedSource.isEmpty() || "audit".equals(normalizedSource)) {
            StringBuilder auditSql = new StringBuilder();
            auditSql.append("SELECT ")
                    .append("'audit' AS source, ")
                    .append("al.id AS log_id, ")
                    .append("al.created_at AS log_time, ")
                    .append("COALESCE(su.username, N'系统') AS operator_name, ")
                    .append("al.action AS action_name, ")
                    .append("al.target_type AS target_type, ")
                    .append("al.target_id AS target_id, ")
                    .append("al.comment AS log_comment ")
                    .append("FROM audit_logs al ")
                    .append("LEFT JOIN sys_user su ON su.id = al.operator_id ")
                    .append("WHERE 1 = 1 ");
            appendCommonFilters(
                    auditSql,
                    queryParams,
                    countParams,
                    "su.username",
                    "al.action",
                    "al.target_type",
                    "al.target_id",
                    "al.comment",
                    "al.created_at",
                    keyword,
                    dateFrom,
                    dateTo
            );
            unionParts.add(auditSql.toString());
        }

        if (normalizedSource.isEmpty() || "transition".equals(normalizedSource)) {
            StringBuilder transitionSql = new StringBuilder();
            transitionSql.append("SELECT ")
                    .append("'transition' AS source, ")
                    .append("stl.id AS log_id, ")
                    .append("stl.changed_at AS log_time, ")
                    .append("COALESCE(su.username, N'系统') AS operator_name, ")
                    .append("COALESCE(NULLIF(stl.action, ''), stl.to_status) AS action_name, ")
                    .append("stl.target_type AS target_type, ")
                    .append("stl.target_id AS target_id, ")
                    .append("stl.change_reason AS log_comment ")
                    .append("FROM state_transition_logs stl ")
                    .append("LEFT JOIN sys_user su ON su.id = stl.changed_by ")
                    .append("WHERE 1 = 1 ");
            appendCommonFilters(
                    transitionSql,
                    queryParams,
                    countParams,
                    "su.username",
                    "COALESCE(NULLIF(stl.action, ''), stl.to_status)",
                    "stl.target_type",
                    "stl.target_id",
                    "stl.change_reason",
                    "stl.changed_at",
                    keyword,
                    dateFrom,
                    dateTo
            );
            unionParts.add(transitionSql.toString());
        }

        if (unionParts.isEmpty()) {
            return V3Response.success(new V3PageData<>(new ArrayList<>(), safePage, safePageSize, 0));
        }

        String unionSql = String.join(" UNION ALL ", unionParts);

        String dataSql = "SELECT * FROM (" + unionSql + ") logs " +
                "ORDER BY log_time DESC, log_id DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        queryParams.add(offset);
        queryParams.add((int) safePageSize);

        String countSql = "SELECT COUNT(1) FROM (" + unionSql + ") logs";

        List<Map<String, Object>> items = jdbcTemplate.query(dataSql, queryParams.toArray(), (rs, rowNum) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            String sourceValue = safe(rs.getString("source"));
            String targetType = safe(rs.getString("target_type"));
            long targetId = rs.getLong("target_id");
            item.put("id", rs.getLong("log_id"));
            item.put("source", sourceValue);
            item.put("sourceLabel", mapSourceLabel(sourceValue));
            item.put("time", fmtDateTime(rs.getTimestamp("log_time")));
            item.put("user", safe(rs.getString("operator_name")));
            item.put("action", safe(rs.getString("action_name")));
            item.put("module", mapModuleLabel(targetType));
            item.put("target", buildTargetLabel(targetType, targetId));
            item.put("comment", safe(rs.getString("log_comment")));
            return item;
        });

        Long total = jdbcTemplate.queryForObject(countSql, countParams.toArray(), Long.class);
        return V3Response.success(new V3PageData<>(items, safePage, safePageSize, total == null ? 0 : total));
    }

    private void appendCommonFilters(
            StringBuilder sql,
            List<Object> queryParams,
            List<Object> countParams,
            String operatorColumn,
            String actionColumn,
            String targetTypeColumn,
            String targetIdColumn,
            String commentColumn,
            String timeColumn,
            String keyword,
            String dateFrom,
            String dateTo
    ) {
        if (keyword != null && !keyword.isBlank()) {
            String likeValue = "%" + keyword.trim() + "%";
            sql.append(" AND (")
                    .append(operatorColumn).append(" LIKE ? ")
                    .append("OR ").append(actionColumn).append(" LIKE ? ")
                    .append("OR ").append(targetTypeColumn).append(" LIKE ? ")
                    .append("OR CAST(").append(targetIdColumn).append(" AS NVARCHAR(50)) LIKE ? ")
                    .append("OR COALESCE(").append(commentColumn).append(", '') LIKE ?")
                    .append(") ");
            for (int i = 0; i < 5; i++) {
                queryParams.add(likeValue);
                countParams.add(likeValue);
            }
        }

        LocalDate from = parseDate(dateFrom);
        if (from != null) {
            Timestamp fromTs = Timestamp.valueOf(from.atStartOfDay());
            sql.append(" AND ").append(timeColumn).append(" >= ? ");
            queryParams.add(fromTs);
            countParams.add(fromTs);
        }

        LocalDate to = parseDate(dateTo);
        if (to != null) {
            Timestamp toTs = Timestamp.valueOf(to.plusDays(1).atStartOfDay());
            sql.append(" AND ").append(timeColumn).append(" < ? ");
            queryParams.add(toTs);
            countParams.add(toTs);
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim().substring(0, 10), DATE_FORMATTER);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeSource(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        return ("audit".equals(value) || "transition".equals(value)) ? value : "";
    }

    private String mapSourceLabel(String source) {
        return switch (safe(source)) {
            case "audit" -> "审核日志";
            case "transition" -> "状态流转";
            default -> "系统日志";
        };
    }

    private String mapModuleLabel(String targetType) {
        return switch (safe(targetType)) {
            case "activity" -> "活动管理";
            case "content" -> "内容管理";
            case "demand" -> "需求管理";
            case "member_verification" -> "会员认证审核";
            case "member" -> "会员管理";
            case "order" -> "订单管理";
            case "supplier_onboarding" -> "服务商入驻审核";
            default -> targetType;
        };
    }

    private String buildTargetLabel(String targetType, long targetId) {
        String prefix = safe(targetType);
        if (prefix.trim().isEmpty()) {
            return String.valueOf(targetId);
        }
        return prefix + "#" + targetId;
    }

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
