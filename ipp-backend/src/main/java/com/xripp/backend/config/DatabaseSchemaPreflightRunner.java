package com.xripp.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaPreflightRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${xripp.schema-preflight.enabled:true}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.warn("[SchemaPreflight] disabled by config");
            return;
        }

        Map<String, List<String>> missingByScript = new LinkedHashMap<>();
        checkColumn(missingByScript, "docs/DDL_Phase2_Migration.sql", "member_profile", "member_level");

        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "activity_no");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "front_module");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "front_position");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "cities_json");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "video_url");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "agenda");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "max_limit");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "current_participants");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "fee_item_id");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "fee_item_name");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "fee_type");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "member_price");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "changed_by");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "changed_at");
        checkColumn(missingByScript, "docs/DDL_Phase14_Activities_Closure.sql", "activities", "change_reason");

        checkTable(missingByScript, "docs/DDL_Phase15_ActivityDisplayApplications.sql", "activity_display_applications");
        checkTable(missingByScript, "docs/DDL_Phase17_ActivityRecords.sql", "activity_records");
        checkTable(missingByScript, "docs/DDL_Phase17_ActivityRecords.sql", "activity_record_photos");
        checkColumn(missingByScript, "docs/DDL_Phase18_Contents_ExtraJson.sql", "contents", "extra_json");

        if (missingByScript.isEmpty()) {
            log.info("[SchemaPreflight] passed");
            return;
        }

        StringBuilder message = new StringBuilder("Database schema preflight failed. Missing schema items:\n");
        missingByScript.forEach((script, items) -> {
            message.append("- run ").append(script).append('\n');
            for (String item : items) {
                message.append("  - ").append(item).append('\n');
            }
        });
        message.append("Current code requires these phases before serving requests.");

        throw new IllegalStateException(message.toString());
    }

    private void checkTable(Map<String, List<String>> missingByScript, String script, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys.tables WHERE name = ? AND schema_id = SCHEMA_ID('dbo')",
                Integer.class,
                tableName
        );
        if (count == null || count <= 0) {
            addMissing(missingByScript, script, "missing table dbo." + tableName);
        }
    }

    private void checkColumn(Map<String, List<String>> missingByScript, String script, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) " +
                        "FROM sys.columns c " +
                        "JOIN sys.tables t ON c.object_id = t.object_id " +
                        "WHERE t.name = ? AND c.name = ? AND t.schema_id = SCHEMA_ID('dbo')",
                Integer.class,
                tableName,
                columnName
        );
        if (count == null || count <= 0) {
            addMissing(missingByScript, script, "missing column dbo." + tableName + "." + columnName);
        }
    }

    private void addMissing(Map<String, List<String>> missingByScript, String script, String item) {
        missingByScript.computeIfAbsent(script, key -> new ArrayList<>()).add(item);
    }
}
