package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.entity.PartnerProfitConfig;
import com.xripp.backend.entity.PartnerProfitSettlement;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IPartnerProfitConfigService;
import com.xripp.backend.service.IPartnerProfitSettlementService;
import com.xripp.backend.service.IPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/profit-sharing")
@RequiredArgsConstructor
public class AdminProfitSharingV3Controller {

    private static final Set<String> VALID_BUSINESS_LINES = Set.of("membership", "tender", "training");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IPartnerProfitConfigService configService;
    private final IPartnerProfitSettlementService settlementService;
    private final IPartnerService partnerService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping("/configs")
    public V3Response<List<Map<String, Object>>> configs() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        List<PartnerProfitConfig> configs = configService.list(new QueryWrapper<PartnerProfitConfig>()
                .orderByDesc("updated_at")
                .orderByDesc("id"));
        Map<Long, Partner> partnerMap = loadPartnerMap(configs.stream()
                .map(PartnerProfitConfig::getPartnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        List<Map<String, Object>> items = new ArrayList<>();
        for (PartnerProfitConfig config : configs) {
            items.add(toConfigItem(config, partnerMap.get(config.getPartnerId())));
        }
        return V3Response.success(items);
    }

    @Transactional
    @PostMapping("/configs")
    public V3Response<Map<String, Object>> createConfig(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Long partnerId = longVal(body, "partner_id", "partnerId");
        if (partnerId == null || partnerId <= 0) {
            return V3Response.error("VALIDATION_ERROR", "partner_id required");
        }
        Partner partner = partnerService.getById(partnerId);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }
        if (configService.count(new QueryWrapper<PartnerProfitConfig>().eq("partner_id", partnerId)) > 0) {
            return V3Response.error("VALIDATION_ERROR", "partner already configured");
        }

        BigDecimal percentage = decimalVal(body, "percentage");
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            return V3Response.error("VALIDATION_ERROR", "percentage must be between 0 and 100");
        }

        List<String> businessLines = normalizeBusinessLines(body);
        if (businessLines.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "business_lines required");
        }

        Date now = new Date();
        PartnerProfitConfig config = new PartnerProfitConfig();
        config.setPartnerId(partnerId);
        config.setPercentage(percentage.setScale(2, RoundingMode.HALF_UP));
        config.setBusinessLinesJson(writeJson(businessLines));
        config.setEnabled(boolVal(body, "enabled", true));
        config.setChangeReason(strVal(body, "change_reason", "changeReason"));
        config.setChangedBy(SecurityContextHolder.getCurrentUserId());
        config.setChangedAt(now);
        config.setCreatedAt(now);
        config.setUpdatedAt(now);
        configService.save(config);

        return V3Response.success(toConfigItem(config, partner));
    }

    @Transactional
    @PutMapping("/configs/{id}")
    public V3Response<Map<String, Object>> updateConfig(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        PartnerProfitConfig config = configService.getById(id);
        if (config == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "profit config not found");
        }

        Long partnerId = body.containsKey("partner_id") || body.containsKey("partnerId")
                ? longVal(body, "partner_id", "partnerId")
                : config.getPartnerId();
        if (partnerId == null || partnerId <= 0) {
            return V3Response.error("VALIDATION_ERROR", "partner_id required");
        }
        Partner partner = partnerService.getById(partnerId);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }
        long duplicateCount = configService.count(new QueryWrapper<PartnerProfitConfig>()
                .eq("partner_id", partnerId)
                .ne("id", id));
        if (duplicateCount > 0) {
            return V3Response.error("VALIDATION_ERROR", "partner already configured");
        }

        BigDecimal percentage = body.containsKey("percentage")
                ? decimalVal(body, "percentage")
                : config.getPercentage();
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            return V3Response.error("VALIDATION_ERROR", "percentage must be between 0 and 100");
        }

        List<String> businessLines = body.containsKey("business_lines") || body.containsKey("businessLines")
                ? normalizeBusinessLines(body)
                : parseBusinessLines(config.getBusinessLinesJson());
        if (businessLines.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "business_lines required");
        }

        config.setPartnerId(partnerId);
        config.setPercentage(percentage.setScale(2, RoundingMode.HALF_UP));
        config.setBusinessLinesJson(writeJson(businessLines));
        if (body.containsKey("enabled")) {
            config.setEnabled(boolVal(body, "enabled", config.getEnabled() == null || config.getEnabled()));
        }
        config.setChangeReason(strVal(body, "change_reason", "changeReason"));
        config.setChangedBy(SecurityContextHolder.getCurrentUserId());
        config.setChangedAt(new Date());
        config.setUpdatedAt(new Date());
        configService.updateById(config);

        return V3Response.success(toConfigItem(config, partner));
    }

    @DeleteMapping("/configs/{id}")
    public V3Response<Map<String, Object>> deleteConfig(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        PartnerProfitConfig config = configService.getById(id);
        if (config == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "profit config not found");
        }
        configService.removeById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("deleted", true);
        return V3Response.success(result);
    }

    @GetMapping("/stats")
    public V3Response<List<Map<String, Object>>> stats(
            @RequestParam(name = "month", required = false) String month
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        YearMonth targetMonth = parseYearMonth(month, YearMonth.now());
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate nextMonthStart = targetMonth.plusMonths(1).atDay(1);

        List<PartnerProfitConfig> configs = configService.list(new QueryWrapper<PartnerProfitConfig>()
                .orderByDesc("updated_at")
                .orderByDesc("id"));
        Map<Long, Partner> partnerMap = loadPartnerMap(configs.stream()
                .map(PartnerProfitConfig::getPartnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, ResolvedConfig> resolved = resolveConfigs(configs, partnerMap);

        List<ProfitRecord> monthRecords = queryProfitRecords(resolved, monthStart, nextMonthStart, null);
        List<ProfitRecord> totalRecords = queryProfitRecords(resolved, LocalDate.of(2020, 1, 1), nextMonthStart, null);
        Map<Long, PartnerProfitSettlement> settlements = loadSettlementMap(targetMonth);

        List<Map<String, Object>> items = new ArrayList<>();
        for (PartnerProfitConfig config : configs) {
            ResolvedConfig resolvedConfig = resolved.get(config.getPartnerId());
            if (resolvedConfig == null) {
                continue;
            }
            BigDecimal monthRevenue = sumAmount(monthRecords, config.getPartnerId(), ProfitRecord::orderAmount);
            BigDecimal monthProfit = sumAmount(monthRecords, config.getPartnerId(), ProfitRecord::profitAmount);
            BigDecimal totalProfit = sumAmount(totalRecords, config.getPartnerId(), ProfitRecord::profitAmount);
            PartnerProfitSettlement settlement = settlements.get(config.getPartnerId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("partnerId", config.getPartnerId());
            item.put("city", resolvedConfig.cityName());
            item.put("partnerName", resolvedConfig.partnerName());
            item.put("percentage", safeDecimal(config.getPercentage()));
            item.put("monthRevenue", monthRevenue);
            item.put("monthProfit", monthProfit);
            item.put("totalProfit", totalProfit);
            item.put("settled", settlement != null && "settled".equalsIgnoreCase(safe(settlement.getSettlementStatus())));
            item.put("enabled", Boolean.TRUE.equals(config.getEnabled()));
            item.put("settlementMonth", targetMonth.format(MONTH_FORMATTER));
            item.put("settledAt", settlement == null ? "" : fmtDateTime(settlement.getSettledAt()));
            item.put("orderCount", countRecords(monthRecords, config.getPartnerId()));
            items.add(item);
        }
        return V3Response.success(items);
    }

    @GetMapping("/records")
    public V3Response<List<Map<String, Object>>> records(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "month_from", required = false) String monthFrom,
            @RequestParam(name = "month_to", required = false) String monthTo
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        YearMonth defaultEnd = YearMonth.now();
        YearMonth from = parseYearMonth(monthFrom, defaultEnd.minusMonths(11));
        YearMonth to = parseYearMonth(monthTo, defaultEnd);
        if (to.isBefore(from)) {
            return V3Response.error("VALIDATION_ERROR", "month_to must be >= month_from");
        }

        List<PartnerProfitConfig> configs = configService.list(new QueryWrapper<PartnerProfitConfig>()
                .orderByDesc("updated_at")
                .orderByDesc("id"));
        Map<Long, Partner> partnerMap = loadPartnerMap(configs.stream()
                .map(PartnerProfitConfig::getPartnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, ResolvedConfig> resolved = resolveConfigs(configs, partnerMap);

        List<ProfitRecord> records = queryProfitRecords(resolved, from.atDay(1), to.plusMonths(1).atDay(1), blankToNull(city));
        Map<String, PartnerProfitSettlement> settlements = loadSettlementKeyMap(from, to);

        List<Map<String, Object>> items = new ArrayList<>();
        for (ProfitRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", record.date());
            item.put("city", record.city());
            item.put("partnerId", record.partnerId());
            item.put("partnerName", record.partnerName());
            item.put("orderNo", record.orderNo());
            item.put("businessLine", record.businessLine());
            item.put("orderAmount", record.orderAmount());
            item.put("percentage", record.percentage());
            item.put("profitAmount", record.profitAmount());
            item.put("settled", settlements.containsKey(record.partnerId() + "|" + record.month()));
            item.put("month", record.month());
            items.add(item);
        }
        return V3Response.success(items);
    }

    @Transactional
    @PostMapping("/settlements")
    public V3Response<Map<String, Object>> settle(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Long partnerId = longVal(body, "partner_id", "partnerId");
        if (partnerId == null || partnerId <= 0) {
            return V3Response.error("VALIDATION_ERROR", "partner_id required");
        }

        PartnerProfitConfig config = configService.getOne(new QueryWrapper<PartnerProfitConfig>().eq("partner_id", partnerId));
        if (config == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "profit config not found");
        }

        Partner partner = partnerService.getById(partnerId);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        YearMonth month = parseYearMonth(strVal(body, "settlement_month", "settlementMonth"), YearMonth.now());
        LocalDate monthStart = month.atDay(1);
        LocalDate nextMonthStart = month.plusMonths(1).atDay(1);
        Map<Long, ResolvedConfig> resolved = resolveConfigs(List.of(config), Map.of(partnerId, partner));
        List<ProfitRecord> monthRecords = queryProfitRecords(resolved, monthStart, nextMonthStart, null);
        List<ProfitRecord> partnerRecords = monthRecords.stream()
                .filter(record -> Objects.equals(record.partnerId(), partnerId))
                .toList();
        if (partnerRecords.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "no profit records found for settlement month");
        }

        BigDecimal revenueAmount = partnerRecords.stream()
                .map(ProfitRecord::orderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = partnerRecords.stream()
                .map(ProfitRecord::profitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        QueryWrapper<PartnerProfitSettlement> qw = new QueryWrapper<>();
        qw.eq("partner_id", partnerId).eq("settlement_month", monthStart);
        PartnerProfitSettlement settlement = settlementService.getOne(qw, false);

        Date now = new Date();
        if (settlement == null) {
            settlement = new PartnerProfitSettlement();
            settlement.setPartnerId(partnerId);
            settlement.setSettlementMonth(monthStart);
            settlement.setCreatedAt(now);
        }
        settlement.setRevenueAmount(revenueAmount);
        settlement.setProfitAmount(profitAmount);
        settlement.setOrderCount(partnerRecords.size());
        settlement.setSettlementStatus("settled");
        settlement.setSettledAt(now);
        settlement.setSettledBy(SecurityContextHolder.getCurrentUserId());
        settlement.setNote(strVal(body, "note"));
        settlement.setUpdatedAt(now);

        if (settlement.getId() == null) {
            settlementService.save(settlement);
        } else {
            settlementService.updateById(settlement);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("partnerId", partnerId);
        result.put("partnerName", safe(partner.getPartnerName()));
        result.put("settlementMonth", month.format(MONTH_FORMATTER));
        result.put("revenueAmount", revenueAmount);
        result.put("profitAmount", profitAmount);
        result.put("orderCount", partnerRecords.size());
        result.put("settled", true);
        result.put("settledAt", fmtDateTime(now));
        return V3Response.success(result);
    }

    private Map<Long, Partner> loadPartnerMap(Set<Long> partnerIds) {
        if (partnerIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return partnerService.listByIds(partnerIds).stream()
                .collect(Collectors.toMap(Partner::getId, partner -> partner, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, ResolvedConfig> resolveConfigs(List<PartnerProfitConfig> configs, Map<Long, Partner> partnerMap) {
        Map<Long, ResolvedConfig> resolved = new LinkedHashMap<>();
        for (PartnerProfitConfig config : configs) {
            if (config.getPartnerId() == null) {
                continue;
            }
            Partner partner = partnerMap.get(config.getPartnerId());
            resolved.put(config.getPartnerId(), new ResolvedConfig(
                    config.getPartnerId(),
                    partner == null ? "" : safe(partner.getPartnerName()),
                    partner == null ? "" : safe(partner.getCityName()),
                    safeDecimal(config.getPercentage()),
                    parseBusinessLines(config.getBusinessLinesJson()),
                    Boolean.TRUE.equals(config.getEnabled())
            ));
        }
        return resolved;
    }

    private List<ProfitRecord> queryProfitRecords(
            Map<Long, ResolvedConfig> configs,
            LocalDate fromDate,
            LocalDate toExclusive,
            String city
    ) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT
                  o.order_no,
                  o.amount,
                  o.order_type,
                  o.biz_type,
                  o.created_at,
                  COALESCE(NULLIF(o.partner_id, 0), su.partner_id) AS resolved_partner_id,
                  p.partner_name,
                  p.city_name
                FROM orders o
                LEFT JOIN sys_user su ON su.id = o.user_id
                LEFT JOIN partners p ON p.id = COALESCE(NULLIF(o.partner_id, 0), su.partner_id)
                WHERE o.created_at >= ?
                  AND o.created_at < ?
                  AND o.order_status IN ('paid', 'in_service', 'completed')
                  AND COALESCE(NULLIF(o.partner_id, 0), su.partner_id) IS NOT NULL
                ORDER BY o.created_at DESC, o.id DESC
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                sql,
                Timestamp.valueOf(fromDate.atStartOfDay()),
                Timestamp.valueOf(toExclusive.atStartOfDay())
        );

        List<ProfitRecord> records = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long partnerId = numberToLong(row.get("resolved_partner_id"));
            if (partnerId == null) {
                continue;
            }

            ResolvedConfig config = configs.get(partnerId);
            if (config == null || !config.enabled()) {
                continue;
            }

            String cityName = safe(row.get("city_name"));
            if (city != null && !city.equals(cityName)) {
                continue;
            }

            String businessLine = deriveBusinessLine(safe(row.get("order_type")), safe(row.get("biz_type")));
            if (businessLine == null || !config.businessLines().contains(businessLine)) {
                continue;
            }

            BigDecimal amount = numberToBigDecimal(row.get("amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal profitAmount = amount.multiply(config.percentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            Date createdAt = dateVal(row.get("created_at"));
            String month = createdAt == null
                    ? ""
                    : YearMonth.from(createdAt.toInstant().atZone(ZoneId.systemDefault())).format(MONTH_FORMATTER);

            records.add(new ProfitRecord(
                    partnerId,
                    config.partnerName(),
                    cityName,
                    safe(row.get("order_no")),
                    businessLine,
                    safeDecimal(config.percentage()),
                    amount,
                    profitAmount,
                    fmtDate(createdAt),
                    month
            ));
        }
        return records;
    }

    private Map<Long, PartnerProfitSettlement> loadSettlementMap(YearMonth month) {
        List<PartnerProfitSettlement> rows = settlementService.list(new QueryWrapper<PartnerProfitSettlement>()
                .eq("settlement_month", month.atDay(1)));
        return rows.stream().collect(Collectors.toMap(
                PartnerProfitSettlement::getPartnerId,
                row -> row,
                (left, right) -> right,
                LinkedHashMap::new
        ));
    }

    private Map<String, PartnerProfitSettlement> loadSettlementKeyMap(YearMonth from, YearMonth to) {
        List<PartnerProfitSettlement> rows = settlementService.list(new QueryWrapper<PartnerProfitSettlement>()
                .ge("settlement_month", from.atDay(1))
                .le("settlement_month", to.atEndOfMonth()));
        return rows.stream().collect(Collectors.toMap(
                row -> row.getPartnerId() + "|" + YearMonth.from(row.getSettlementMonth()).format(MONTH_FORMATTER),
                row -> row,
                (left, right) -> right,
                LinkedHashMap::new
        ));
    }

    private Map<String, Object> toConfigItem(PartnerProfitConfig config, Partner partner) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", config.getId());
        item.put("partnerId", config.getPartnerId());
        item.put("partnerName", partner == null ? "" : safe(partner.getPartnerName()));
        item.put("city", partner == null ? "" : safe(partner.getCityName()));
        item.put("percentage", safeDecimal(config.getPercentage()));
        item.put("businessLines", parseBusinessLines(config.getBusinessLinesJson()));
        item.put("enabled", Boolean.TRUE.equals(config.getEnabled()));
        item.put("updatedAt", fmtDateTime(config.getUpdatedAt()));
        item.put("createdAt", fmtDateTime(config.getCreatedAt()));
        return item;
    }

    private List<String> normalizeBusinessLines(Map<String, Object> body) {
        Object raw = body.containsKey("business_lines") ? body.get("business_lines") : body.get("businessLines");
        List<String> values = new ArrayList<>();
        if (raw instanceof Collection<?> collection) {
            for (Object item : collection) {
                String value = item == null ? "" : item.toString().trim();
                if (VALID_BUSINESS_LINES.contains(value) && !values.contains(value)) {
                    values.add(value);
                }
            }
        } else if (raw instanceof String text && !text.isBlank()) {
            for (String part : text.split(",")) {
                String value = part.trim();
                if (VALID_BUSINESS_LINES.contains(value) && !values.contains(value)) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private List<String> parseBusinessLines(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> values = objectMapper.readValue(rawJson, new TypeReference<ArrayList<String>>() {});
            return values.stream()
                    .map(value -> value == null ? "" : value.trim())
                    .filter(VALID_BUSINESS_LINES::contains)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "[]";
        }
    }

    private BigDecimal sumAmount(List<ProfitRecord> records, Long partnerId, java.util.function.Function<ProfitRecord, BigDecimal> getter) {
        return records.stream()
                .filter(record -> Objects.equals(record.partnerId(), partnerId))
                .map(getter)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countRecords(List<ProfitRecord> records, Long partnerId) {
        return (int) records.stream()
                .filter(record -> Objects.equals(record.partnerId(), partnerId))
                .count();
    }

    private YearMonth parseYearMonth(String raw, YearMonth fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return YearMonth.parse(raw.trim(), MONTH_FORMATTER);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String deriveBusinessLine(String orderType, String bizType) {
        String merged = (orderType + " " + bizType).toLowerCase(Locale.ROOT);
        if (merged.contains("membership")) {
            return "membership";
        }
        if (merged.contains("tender")) {
            return "tender";
        }
        if (merged.contains("training")) {
            return "training";
        }
        return null;
    }

    private Long longVal(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value == null) {
                continue;
            }
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal decimalVal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean boolVal(Map<String, Object> body, String key, boolean fallback) {
        Object value = body.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Long numberToLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal numberToBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    private Date dateVal(Object value) {
        if (value instanceof Date date) {
            return date;
        }
        return null;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String strVal(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value != null) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String fmtDate(Date date) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' ');
    }

    private record ResolvedConfig(
            Long partnerId,
            String partnerName,
            String cityName,
            BigDecimal percentage,
            List<String> businessLines,
            boolean enabled
    ) {}

    private record ProfitRecord(
            Long partnerId,
            String partnerName,
            String city,
            String orderNo,
            String businessLine,
            BigDecimal percentage,
            BigDecimal orderAmount,
            BigDecimal profitAmount,
            String date,
            String month
    ) {}
}
