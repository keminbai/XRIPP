package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OverseasReport;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOverseasReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/overseas/reports")
@RequiredArgsConstructor
public class AdminOverseasReportsV3Controller {

    private final IOverseasReportService overseasReportService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String type,
            @RequestParam(name = "fee_level", required = false) String feeLevel,
            @RequestParam(name = "publish_start", required = false) String publishStart,
            @RequestParam(name = "publish_end", required = false) String publishEnd
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<OverseasReport> qw = new QueryWrapper<>();
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw)
                    .or().like("summary", kw)
                    .or().like("source_name", kw)
                    .or().like("keywords_json", kw));
        }
        if (!isBlank(country)) {
            qw.eq("country", country.trim());
        }
        if (!isBlank(industry)) {
            qw.eq("industry_code", industry.trim());
        }
        if (!isBlank(type)) {
            qw.eq("report_type", type.trim());
        }
        if (!isBlank(feeLevel)) {
            qw.eq("fee_level", feeLevel.trim());
        }
        Date publishStartDate = parseDate(publishStart);
        Date publishEndDate = parseDate(publishEnd);
        if (publishStartDate != null) {
            qw.ge("publish_date", publishStartDate);
        }
        if (publishEndDate != null) {
            qw.le("publish_date", publishEndDate);
        }
        qw.orderByDesc("publish_date").orderByDesc("id");

        Page<OverseasReport> result = overseasReportService.page(new Page<>(page, pageSize), qw);
        List<Map<String, Object>> items = result.getRecords().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        return V3Response.success(new V3PageData<>(items, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasReport row = overseasReportService.getById(id);
        if (row == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "report not found");
        }
        return V3Response.success(toItem(row));
    }

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        ReportPayload payload = parsePayload(body, null);
        if (!payload.isValid()) {
            return V3Response.error("VALIDATION_ERROR", payload.validationMessage);
        }

        OverseasReport row = new OverseasReport();
        applyPayload(row, payload, true);
        overseasReportService.save(row);
        return V3Response.success(toItem(row));
    }

    @Transactional
    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasReport row = overseasReportService.getById(id);
        if (row == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "report not found");
        }

        ReportPayload payload = parsePayload(body, row);
        if (!payload.isValid()) {
            return V3Response.error("VALIDATION_ERROR", payload.validationMessage);
        }

        applyPayload(row, payload, false);
        overseasReportService.updateById(row);
        return V3Response.success(toItem(row));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public V3Response<Map<String, Object>> delete(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasReport row = overseasReportService.getById(id);
        if (row == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "report not found");
        }
        overseasReportService.removeById(id);
        return V3Response.success(Map.of("id", id, "deleted", true));
    }

    @Transactional
    @PostMapping("/{id}/download")
    public V3Response<Map<String, Object>> recordDownload(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasReport row = overseasReportService.getById(id);
        if (row == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "report not found");
        }
        row.setDownloadsCount(safeInt(row.getDownloadsCount()) + 1);
        row.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        row.setUpdatedAt(new Date());
        overseasReportService.updateById(row);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", row.getId());
        result.put("fileUrl", safe(row.getReportFileUrl()));
        result.put("fileName", safe(row.getReportFileName()));
        result.put("downloads", safeInt(row.getDownloadsCount()));
        return V3Response.success(result);
    }

    @Transactional
    @PostMapping("/{id}/expire")
    public V3Response<Map<String, Object>> setExpire(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasReport row = overseasReportService.getById(id);
        if (row == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "report not found");
        }

        Date expireDate = firstNonNull(
                parseDate(str(body.get("expire_date"))),
                parseDate(str(body.get("expireDate")))
        );
        row.setExpireDate(expireDate);
        row.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        row.setUpdatedAt(new Date());
        overseasReportService.updateById(row);
        return V3Response.success(toItem(row));
    }

    private void applyPayload(OverseasReport row, ReportPayload payload, boolean creating) {
        Date now = new Date();
        row.setTitle(payload.title);
        row.setCountry(payload.country);
        row.setIndustryCode(payload.industryCode);
        row.setReportType(payload.reportType);
        row.setReportYear(payload.reportYear);
        row.setPublishDate(payload.publishDate);
        row.setExpireDate(payload.expireDate);
        row.setSummary(payload.summary);
        row.setCoverImageUrl(payload.coverImageUrl);
        row.setCoverImageName(payload.coverImageName);
        row.setCoverImageExt(payload.coverImageExt);
        row.setCoverImageSize(payload.coverImageSize);
        row.setReportFileUrl(payload.reportFileUrl);
        row.setReportFileName(payload.reportFileName);
        row.setReportFileExt(payload.reportFileExt);
        row.setReportFileSize(payload.reportFileSize);
        row.setKeywordsJson(toJson(payload.keywords));
        row.setSourceName(payload.sourceName);
        row.setFeeLevel(payload.feeLevel);
        row.setAccessLevel(payload.accessLevel);
        row.setIsRecommended(payload.isRecommended);
        if (creating) {
            row.setDownloadsCount(0);
            row.setCreatedBy(SecurityContextHolder.getCurrentUserId());
            row.setCreatedAt(now);
        }
        row.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        row.setUpdatedAt(now);
    }

    private ReportPayload parsePayload(Map<String, Object> body, OverseasReport current) {
        ReportPayload payload = new ReportPayload();
        payload.title = firstNonBlank(str(body.get("title")), current == null ? "" : safe(current.getTitle()));
        payload.country = firstNonBlank(str(body.get("country")), current == null ? "" : safe(current.getCountry()));
        payload.industryCode = firstNonBlank(
                str(body.get("industry_code")),
                str(body.get("industryCode")),
                str(body.get("industry")),
                current == null ? "" : safe(current.getIndustryCode())
        );
        payload.reportType = firstNonBlank(
                str(body.get("report_type")),
                str(body.get("reportType")),
                str(body.get("type")),
                current == null ? "" : safe(current.getReportType())
        );
        payload.reportYear = firstNonBlank(
                str(body.get("report_year")),
                str(body.get("reportYear")),
                str(body.get("year")),
                current == null ? "" : safe(current.getReportYear())
        );
        payload.publishDate = firstNonNull(
                parseDate(str(body.get("publish_date"))),
                parseDate(str(body.get("publishDate"))),
                current == null ? null : current.getPublishDate()
        );
        payload.expireDate = firstNonNull(
                parseDate(str(body.get("expire_date"))),
                parseDate(str(body.get("expireDate"))),
                current == null ? null : current.getExpireDate()
        );
        payload.summary = firstNonBlank(str(body.get("summary")), current == null ? "" : safe(current.getSummary()));

        boolean coverProvided = body.containsKey("cover") || body.containsKey("coverImageMeta") || body.containsKey("cover_image");
        boolean fileProvided = body.containsKey("reportFile") || body.containsKey("report_file") || body.containsKey("file");
        Map<String, Object> coverMeta = firstNonNullMap(
                normalizeFileMap(body.get("cover")),
                normalizeFileMap(body.get("coverImageMeta")),
                normalizeFileMap(body.get("cover_image"))
        );
        Map<String, Object> fileMeta = firstNonNullMap(
                normalizeFileMap(body.get("reportFile")),
                normalizeFileMap(body.get("report_file")),
                normalizeFileMap(body.get("file"))
        );
        if (current != null && !coverProvided && coverMeta.isEmpty() && !isBlank(current.getCoverImageUrl())) {
            coverMeta = existingFileMeta(current.getCoverImageUrl(), current.getCoverImageName(), current.getCoverImageExt(), current.getCoverImageSize());
        }
        if (current != null && !fileProvided && fileMeta.isEmpty() && !isBlank(current.getReportFileUrl())) {
            fileMeta = existingFileMeta(current.getReportFileUrl(), current.getReportFileName(), current.getReportFileExt(), current.getReportFileSize());
        }

        payload.coverImageUrl = firstNonBlank(str(coverMeta.get("fileUrl")), str(coverMeta.get("url")), current == null ? "" : safe(current.getCoverImageUrl()));
        payload.coverImageName = firstNonBlank(str(coverMeta.get("fileName")), str(coverMeta.get("name")), current == null ? "" : safe(current.getCoverImageName()));
        payload.coverImageExt = firstNonBlank(str(coverMeta.get("fileExt")), fileExtFromName(payload.coverImageName), fileExtFromName(payload.coverImageUrl), current == null ? "" : safe(current.getCoverImageExt()));
        payload.coverImageSize = firstNonNull(longVal(coverMeta.get("fileSize")), current == null ? null : current.getCoverImageSize());

        payload.reportFileUrl = firstNonBlank(str(fileMeta.get("fileUrl")), str(fileMeta.get("url")), current == null ? "" : safe(current.getReportFileUrl()));
        payload.reportFileName = firstNonBlank(str(fileMeta.get("fileName")), str(fileMeta.get("name")), current == null ? "" : safe(current.getReportFileName()));
        payload.reportFileExt = firstNonBlank(str(fileMeta.get("fileExt")), fileExtFromName(payload.reportFileName), fileExtFromName(payload.reportFileUrl), current == null ? "" : safe(current.getReportFileExt()));
        payload.reportFileSize = firstNonNull(longVal(fileMeta.get("fileSize")), current == null ? null : current.getReportFileSize());

        payload.keywords = firstNonEmptyList(
                parseStringList(body.get("keywords")),
                current == null ? List.of() : parseJsonStringArray(current.getKeywordsJson())
        );
        payload.sourceName = firstNonBlank(
                str(body.get("source_name")),
                str(body.get("sourceName")),
                str(body.get("source")),
                current == null ? "" : safe(current.getSourceName())
        );
        payload.feeLevel = normalizeFeeLevel(firstNonBlank(
                str(body.get("fee_level")),
                str(body.get("feeLevel")),
                current == null ? "free" : safeOr(current.getFeeLevel(), "free")
        ));
        payload.accessLevel = normalizeAccessLevel(firstNonBlank(
                str(body.get("access_level")),
                str(body.get("accessLevel")),
                current == null ? "public" : safeOr(current.getAccessLevel(), "public")
        ));
        payload.isRecommended = firstNonNull(boolVal(body.get("is_recommended")), boolVal(body.get("isRecommended")), current == null ? Boolean.FALSE : Boolean.TRUE.equals(current.getIsRecommended()));

        if (isBlank(payload.title)
                || isBlank(payload.country)
                || isBlank(payload.industryCode)
                || isBlank(payload.reportType)
                || payload.publishDate == null
                || isBlank(payload.summary)
                || isBlank(payload.reportFileUrl)) {
            payload.validationMessage = "title/country/industry/type/publish_date/summary/report_file required";
        }
        return payload;
    }

    private Map<String, Object> toItem(OverseasReport row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("title", safe(row.getTitle()));
        item.put("country", safe(row.getCountry()));
        item.put("industry", safe(row.getIndustryCode()));
        item.put("industryCode", safe(row.getIndustryCode()));
        item.put("type", safe(row.getReportType()));
        item.put("reportType", safe(row.getReportType()));
        item.put("year", safe(row.getReportYear()));
        item.put("publishDate", fmtDate(row.getPublishDate()));
        item.put("expireDate", fmtDate(row.getExpireDate()));
        item.put("summary", safe(row.getSummary()));
        item.put("coverImage", safe(row.getCoverImageUrl()));
        item.put("coverImageName", safe(row.getCoverImageName()));
        item.put("coverImageExt", safe(row.getCoverImageExt()));
        item.put("coverImageSize", row.getCoverImageSize() == null ? 0L : row.getCoverImageSize());
        item.put("fileUrl", safe(row.getReportFileUrl()));
        item.put("reportFileUrl", safe(row.getReportFileUrl()));
        item.put("fileName", safe(row.getReportFileName()));
        item.put("reportFileName", safe(row.getReportFileName()));
        item.put("fileExt", safe(row.getReportFileExt()));
        item.put("reportFileExt", safe(row.getReportFileExt()));
        item.put("fileSize", row.getReportFileSize() == null ? 0L : row.getReportFileSize());
        item.put("reportFileSize", row.getReportFileSize() == null ? 0L : row.getReportFileSize());
        item.put("keywords", parseJsonStringArray(row.getKeywordsJson()));
        item.put("source", safe(row.getSourceName()));
        item.put("feeLevel", safeOr(row.getFeeLevel(), "free"));
        item.put("accessLevel", safeOr(row.getAccessLevel(), "public"));
        item.put("isRecommended", Boolean.TRUE.equals(row.getIsRecommended()));
        item.put("downloads", safeInt(row.getDownloadsCount()));
        item.put("uploadDate", fmtDate(row.getCreatedAt()));
        item.put("createdAt", fmtDateTime(row.getCreatedAt()));
        item.put("updatedAt", fmtDateTime(row.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> normalizeFileMap(Object raw) {
        if (raw == null) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> converted = objectMapper.convertValue(raw, new TypeReference<Map<String, Object>>() {});
            return converted == null ? new LinkedHashMap<>() : converted;
        } catch (IllegalArgumentException ignored) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> existingFileMeta(String url, String name, String ext, Long size) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("fileUrl", safe(url));
        item.put("fileName", safe(name));
        item.put("fileExt", safe(ext));
        item.put("fileSize", size == null ? 0L : size);
        return item;
    }

    private List<String> parseStringList(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) {
                return List.of();
            }
            if (trimmed.startsWith("[")) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
                } catch (Exception ignored) {
                    return List.of();
                }
            }
            return Arrays.stream(trimmed.split("[,，]"))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .distinct()
                    .toList();
        }
        try {
            List<String> items = objectMapper.convertValue(raw, new TypeReference<List<String>>() {});
            return items == null ? List.of() : items.stream()
                    .map(item -> item == null ? "" : item.trim())
                    .filter(item -> !item.isEmpty())
                    .distinct()
                    .toList();
        } catch (IllegalArgumentException ignored) {
            return List.of();
        }
    }

    private List<String> parseJsonStringArray(String json) {
        if (isBlank(json)) {
            return List.of();
        }
        try {
            List<String> items = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return items == null ? List.of() : items.stream()
                    .map(item -> item == null ? "" : item.trim())
                    .filter(item -> !item.isEmpty())
                    .toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private String normalizeFeeLevel(String value) {
        String normalized = safeOr(value, "free").toLowerCase(Locale.ROOT);
        return List.of("free", "fee_1", "fee_2").contains(normalized) ? normalized : "free";
    }

    private String normalizeAccessLevel(String value) {
        String normalized = safeOr(value, "public").toLowerCase(Locale.ROOT);
        return List.of("public", "vip", "svip").contains(normalized) ? normalized : "public";
    }

    private String fileExtFromName(String name) {
        String value = safe(name);
        int idx = value.lastIndexOf('.');
        if (idx < 0 || idx == value.length() - 1) {
            return "";
        }
        return value.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private Date parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        List<String> patterns = List.of("yyyy-MM-dd", "yyyy-MM", "yyyy");
        for (String pattern : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern);
                fmt.setLenient(false);
                return fmt.parse(value.trim());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String fmtDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeOr(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Integer intVal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Long longVal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Boolean boolVal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String raw = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            return null;
        }
        return List.of("1", "true", "yes", "y", "on").contains(raw);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @SafeVarargs
    private final <T> List<T> firstNonEmptyList(List<T>... values) {
        for (List<T> value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return new ArrayList<>();
    }

    private Map<String, Object> firstNonNullMap(Map<String, Object>... values) {
        for (Map<String, Object> value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return new LinkedHashMap<>();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static class ReportPayload {
        private String title = "";
        private String country = "";
        private String industryCode = "";
        private String reportType = "";
        private String reportYear = "";
        private Date publishDate;
        private Date expireDate;
        private String summary = "";
        private String coverImageUrl = "";
        private String coverImageName = "";
        private String coverImageExt = "";
        private Long coverImageSize;
        private String reportFileUrl = "";
        private String reportFileName = "";
        private String reportFileExt = "";
        private Long reportFileSize;
        private List<String> keywords = new ArrayList<>();
        private String sourceName = "";
        private String feeLevel = "free";
        private String accessLevel = "public";
        private Boolean isRecommended = Boolean.FALSE;
        private String validationMessage = "";

        private boolean isValid() {
            return validationMessage == null || validationMessage.isEmpty();
        }
    }
}
