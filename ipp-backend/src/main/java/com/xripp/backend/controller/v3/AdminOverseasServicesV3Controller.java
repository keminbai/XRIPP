package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.OverseasService;
import com.xripp.backend.entity.OverseasServiceFile;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IOverseasServiceFileService;
import com.xripp.backend.service.IOverseasServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/admin/overseas/services")
@RequiredArgsConstructor
public class AdminOverseasServicesV3Controller {

    private final IOverseasServiceService overseasServiceService;
    private final IOverseasServiceFileService overseasServiceFileService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @RequestParam(name = "service_type", required = false) String serviceType,
            @RequestParam(required = false) String project,
            @RequestParam(required = false) String status
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<OverseasService> qw = new QueryWrapper<>();
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw)
                    .or().like("summary", kw)
                    .or().like("contact_person", kw)
                    .or().like("contact_email", kw));
        }
        if (!isBlank(country)) {
            qw.like("countries_json", country.trim());
        }
        if (!isBlank(serviceType)) {
            qw.eq("service_type", serviceType.trim());
        }
        if (!isBlank(project)) {
            qw.eq("project_code", project.trim());
        }
        if (!isBlank(status)) {
            qw.eq("status", status.trim());
        }
        qw.orderByDesc("updated_at").orderByDesc("id");

        Page<OverseasService> result = overseasServiceService.page(new Page<>(page, pageSize), qw);
        Map<Long, List<OverseasServiceFile>> fileMap = listFileMap(result.getRecords().stream()
                .map(OverseasService::getId)
                .filter(Objects::nonNull)
                .toList());

        List<Map<String, Object>> items = result.getRecords().stream()
                .map(item -> toListItem(item, fileMap.getOrDefault(item.getId(), List.of())))
                .collect(Collectors.toList());

        return V3Response.success(new V3PageData<>(items, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasService service = overseasServiceService.getById(id);
        if (service == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "service not found");
        }
        return V3Response.success(toDetailItem(service));
    }

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        ServicePayload payload = parsePayload(body, null);
        if (!payload.isValid()) {
            return V3Response.error("VALIDATION_ERROR", payload.validationMessage);
        }

        Date now = new Date();
        OverseasService service = new OverseasService();
        applyPayload(service, payload, true, now);
        overseasServiceService.save(service);
        replaceFiles(service.getId(), payload);
        return V3Response.success(toDetailItem(service));
    }

    @Transactional
    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasService service = overseasServiceService.getById(id);
        if (service == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "service not found");
        }

        ServicePayload payload = parsePayload(body, service);
        if (!payload.isValid()) {
            return V3Response.error("VALIDATION_ERROR", payload.validationMessage);
        }

        applyPayload(service, payload, false, new Date());
        overseasServiceService.updateById(service);
        replaceFiles(service.getId(), payload);
        return V3Response.success(toDetailItem(service));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public V3Response<Map<String, Object>> delete(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        OverseasService service = overseasServiceService.getById(id);
        if (service == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "service not found");
        }

        overseasServiceFileService.remove(new QueryWrapper<OverseasServiceFile>().eq("service_id", id));
        overseasServiceService.removeById(id);
        return V3Response.success(Map.of("id", id, "deleted", true));
    }

    private void applyPayload(OverseasService service, ServicePayload payload, boolean creating, Date now) {
        service.setServiceType(payload.serviceType);
        service.setProjectCode(payload.projectCode);
        service.setTitle(payload.title);
        service.setCountriesJson(toJson(payload.countries));
        service.setSummary(payload.summary);
        service.setContent(payload.content);
        service.setPriceType(payload.priceType);
        service.setPriceAmount(payload.priceAmount);
        service.setDurationDesc(payload.duration);
        service.setContactPerson(payload.contactPerson);
        service.setContactPhone(payload.contactPhone);
        service.setContactEmail(payload.contactEmail);
        service.setTagsJson(toJson(payload.tags));
        service.setStatus(payload.status);
        service.setViewsCount(payload.viewsCount);
        service.setInquiriesCount(payload.inquiriesCount);
        if ("published".equals(payload.status)) {
            if (service.getPublishedAt() == null) {
                service.setPublishedAt(now);
            }
        } else if (creating) {
            service.setPublishedAt(null);
        }
        if (creating) {
            service.setCreatedBy(SecurityContextHolder.getCurrentUserId());
            service.setCreatedAt(now);
        }
        service.setUpdatedBy(SecurityContextHolder.getCurrentUserId());
        service.setUpdatedAt(now);
    }

    private ServicePayload parsePayload(Map<String, Object> body, OverseasService current) {
        ServicePayload payload = new ServicePayload();
        payload.serviceType = firstNonBlank(
                str(body.get("service_type")),
                str(body.get("serviceType")),
                str(body.get("type")),
                current == null ? "" : safe(current.getServiceType())
        );
        payload.projectCode = firstNonBlank(
                str(body.get("project_code")),
                str(body.get("projectCode")),
                str(body.get("project")),
                current == null ? "" : safe(current.getProjectCode())
        );
        payload.title = firstNonBlank(str(body.get("title")), current == null ? "" : safe(current.getTitle()));
        payload.countries = firstNonEmptyList(
                parseStringList(body.get("countries")),
                parseStringList(body.get("countries_json")),
                current == null ? List.of() : parseJsonStringArray(current.getCountriesJson())
        );
        payload.summary = firstNonBlank(str(body.get("summary")), current == null ? "" : safe(current.getSummary()));
        payload.content = firstNonBlank(str(body.get("content")), current == null ? "" : safe(current.getContent()));
        payload.priceType = firstNonBlank(
                str(body.get("price_type")),
                str(body.get("priceType")),
                current == null ? "negotiable" : safeOr(current.getPriceType(), "negotiable")
        );
        payload.priceAmount = firstNonNullDecimal(
                dec(body.get("price_amount")),
                dec(body.get("price")),
                current == null ? null : current.getPriceAmount()
        );
        payload.duration = firstNonBlank(
                str(body.get("duration_desc")),
                str(body.get("duration")),
                current == null ? "" : safe(current.getDurationDesc())
        );
        payload.contactPerson = firstNonBlank(
                str(body.get("contact_person")),
                str(body.get("contactPerson")),
                current == null ? "" : safe(current.getContactPerson())
        );
        payload.contactPhone = firstNonBlank(
                str(body.get("contact_phone")),
                str(body.get("contactPhone")),
                current == null ? "" : safe(current.getContactPhone())
        );
        payload.contactEmail = firstNonBlank(
                str(body.get("contact_email")),
                str(body.get("contactEmail")),
                current == null ? "" : safe(current.getContactEmail())
        );
        payload.tags = firstNonEmptyList(
                parseStringList(body.get("tags")),
                parseStringList(body.get("tags_json")),
                current == null ? List.of() : parseJsonStringArray(current.getTagsJson())
        );
        payload.status = normalizeStatus(firstNonBlank(str(body.get("status")), current == null ? "published" : safeOr(current.getStatus(), "published")));
        payload.viewsCount = firstNonNullInt(
                intVal(body.get("views_count")),
                intVal(body.get("views")),
                current == null ? 0 : safeInt(current.getViewsCount())
        );
        payload.inquiriesCount = firstNonNullInt(
                intVal(body.get("inquiries_count")),
                intVal(body.get("inquiries")),
                current == null ? 0 : safeInt(current.getInquiriesCount())
        );
        payload.promoProvided = body.containsKey("promoImage") || body.containsKey("promo_image");
        payload.galleryProvided = body.containsKey("images") || body.containsKey("gallery_images");
        payload.introProvided = body.containsKey("introFiles") || body.containsKey("intro_files");
        payload.promoImages = normalizeFileItems(body.containsKey("promoImage") ? body.get("promoImage") : body.get("promo_image"));
        payload.galleryImages = normalizeFileItems(body.containsKey("images") ? body.get("images") : body.get("gallery_images"));
        payload.introFiles = normalizeFileItems(body.containsKey("introFiles") ? body.get("introFiles") : body.get("intro_files"));

        if (current != null) {
            List<OverseasServiceFile> currentFiles = listFiles(current.getId());
            if (!payload.promoProvided && payload.promoImages.isEmpty()) payload.promoImages = filterCategory(currentFiles, "promo_image");
            if (!payload.galleryProvided && payload.galleryImages.isEmpty()) payload.galleryImages = filterCategory(currentFiles, "gallery_image");
            if (!payload.introProvided && payload.introFiles.isEmpty()) payload.introFiles = filterCategory(currentFiles, "intro_file");
        }

        if (isBlank(payload.serviceType)
                || isBlank(payload.projectCode)
                || isBlank(payload.title)
                || payload.countries.isEmpty()
                || isBlank(payload.summary)
                || isBlank(payload.content)) {
            payload.validationMessage = "type/project/title/countries/summary/content required";
            return payload;
        }
        if (payload.promoImages.isEmpty()) {
            payload.validationMessage = "promo image required";
            return payload;
        }
        if ("fixed".equals(payload.priceType) && (payload.priceAmount == null || payload.priceAmount.compareTo(BigDecimal.ZERO) <= 0)) {
            payload.validationMessage = "fixed price must be greater than 0";
        }
        return payload;
    }

    private void replaceFiles(Long serviceId, ServicePayload payload) {
        overseasServiceFileService.remove(new QueryWrapper<OverseasServiceFile>().eq("service_id", serviceId));
        List<OverseasServiceFile> rows = new ArrayList<>();
        appendFiles(rows, serviceId, "promo_image", payload.promoImages);
        appendFiles(rows, serviceId, "gallery_image", payload.galleryImages);
        appendFiles(rows, serviceId, "intro_file", payload.introFiles);
        for (OverseasServiceFile row : rows) {
            overseasServiceFileService.save(row);
        }
    }

    private void appendFiles(List<OverseasServiceFile> rows, Long serviceId, String category, List<Map<String, Object>> items) {
        Date now = new Date();
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            String url = firstNonBlank(str(item.get("fileUrl")), str(item.get("url")));
            if (url.isEmpty()) {
                continue;
            }
            OverseasServiceFile row = new OverseasServiceFile();
            row.setServiceId(serviceId);
            row.setFileCategory(category);
            row.setFileName(firstNonBlank(str(item.get("fileName")), str(item.get("name")), "未命名文件"));
            row.setFileUrl(url);
            row.setFileExt(firstNonBlank(str(item.get("fileExt")), fileExtFromName(row.getFileName()), fileExtFromName(url)));
            row.setFileSize(longVal(item.get("fileSize")));
            row.setSortOrder(firstNonNullInt(intVal(item.get("sortOrder")), i));
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            rows.add(row);
        }
    }

    private Map<String, Object> toListItem(OverseasService service, List<OverseasServiceFile> files) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", service.getId());
        item.put("type", safe(service.getServiceType()));
        item.put("serviceType", safe(service.getServiceType()));
        item.put("project", safe(service.getProjectCode()));
        item.put("projectCode", safe(service.getProjectCode()));
        item.put("title", safe(service.getTitle()));
        item.put("countries", parseJsonStringArray(service.getCountriesJson()));
        item.put("summary", safe(service.getSummary()));
        item.put("content", safe(service.getContent()));
        item.put("priceType", safeOr(service.getPriceType(), "negotiable"));
        item.put("price", service.getPriceAmount());
        item.put("duration", safe(service.getDurationDesc()));
        item.put("contactPerson", safe(service.getContactPerson()));
        item.put("contactPhone", safe(service.getContactPhone()));
        item.put("contactEmail", safe(service.getContactEmail()));
        item.put("tags", parseJsonStringArray(service.getTagsJson()));
        item.put("status", safeOr(service.getStatus(), "draft"));
        item.put("views", safeInt(service.getViewsCount()));
        item.put("inquiries", safeInt(service.getInquiriesCount()));
        item.put("publishDate", fmtDate(service.getPublishedAt()));
        item.put("createdAt", fmtDateTime(service.getCreatedAt()));
        item.put("updatedAt", fmtDateTime(service.getUpdatedAt()));
        List<Map<String, Object>> promoImages = toFileItems(files.stream().filter(itemFile -> "promo_image".equals(itemFile.getFileCategory())).toList());
        item.put("promoImage", promoImages);
        item.put("promoImageUrl", promoImages.isEmpty() ? "" : safe(str(promoImages.get(0).get("url"))));
        return item;
    }

    private Map<String, Object> toDetailItem(OverseasService service) {
        List<OverseasServiceFile> files = listFiles(service.getId());
        Map<String, Object> item = new LinkedHashMap<>(toListItem(service, files));
        item.put("images", toFileItems(files.stream().filter(file -> "gallery_image".equals(file.getFileCategory())).toList()));
        item.put("introFiles", toFileItems(files.stream().filter(file -> "intro_file".equals(file.getFileCategory())).toList()));
        return item;
    }

    private Map<Long, List<OverseasServiceFile>> listFileMap(List<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            return Map.of();
        }
        List<OverseasServiceFile> files = overseasServiceFileService.list(
                new QueryWrapper<OverseasServiceFile>()
                        .in("service_id", serviceIds)
                        .orderByAsc("sort_order")
                        .orderByAsc("id")
        );
        return files.stream().collect(Collectors.groupingBy(OverseasServiceFile::getServiceId, LinkedHashMap::new, Collectors.toList()));
    }

    private List<OverseasServiceFile> listFiles(Long serviceId) {
        return overseasServiceFileService.list(
                new QueryWrapper<OverseasServiceFile>()
                        .eq("service_id", serviceId)
                        .orderByAsc("sort_order")
                        .orderByAsc("id")
        );
    }

    private List<Map<String, Object>> filterCategory(List<OverseasServiceFile> files, String category) {
        return toFileItems(files.stream().filter(item -> category.equals(item.getFileCategory())).toList());
    }

    private List<Map<String, Object>> toFileItems(List<OverseasServiceFile> files) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (OverseasServiceFile file : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", file.getId());
            item.put("name", safe(file.getFileName()));
            item.put("fileName", safe(file.getFileName()));
            item.put("url", safe(file.getFileUrl()));
            item.put("fileUrl", safe(file.getFileUrl()));
            item.put("fileExt", safe(file.getFileExt()));
            item.put("fileSize", file.getFileSize() == null ? 0L : file.getFileSize());
            item.put("sortOrder", safeInt(file.getSortOrder()));
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> normalizeFileItems(Object raw) {
        if (raw == null) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> converted = objectMapper.convertValue(raw, new TypeReference<List<Map<String, Object>>>() {});
            return converted == null ? new ArrayList<>() : converted.stream()
                    .filter(Objects::nonNull)
                    .map(item -> {
                        Map<String, Object> normalized = new LinkedHashMap<>();
                        normalized.put("fileName", firstNonBlank(str(item.get("fileName")), str(item.get("name"))));
                        normalized.put("url", firstNonBlank(str(item.get("fileUrl")), str(item.get("url"))));
                        normalized.put("fileExt", firstNonBlank(str(item.get("fileExt")), fileExtFromName(firstNonBlank(str(item.get("fileName")), str(item.get("name"))))));
                        normalized.put("fileSize", longVal(item.get("fileSize")));
                        normalized.put("sortOrder", intVal(item.get("sortOrder")));
                        return normalized;
                    })
                    .filter(item -> !isBlank(str(item.get("url"))))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException ignored) {
            return new ArrayList<>();
        }
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

    private String normalizeStatus(String status) {
        String normalized = safeOr(status, "draft").toLowerCase(Locale.ROOT);
        return List.of("draft", "published", "offline").contains(normalized) ? normalized : "draft";
    }

    private String fileExtFromName(String name) {
        String value = safe(name);
        int idx = value.lastIndexOf('.');
        if (idx < 0 || idx == value.length() - 1) {
            return "";
        }
        return value.substring(idx + 1).toLowerCase(Locale.ROOT);
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

    private BigDecimal dec(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
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

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
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

    private BigDecimal firstNonNullDecimal(BigDecimal... values) {
        return firstNonNull(values);
    }

    private Integer firstNonNullInt(Integer... values) {
        Integer value = firstNonNull(values);
        return value == null ? 0 : value;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private static class ServicePayload {
        private String serviceType = "";
        private String projectCode = "";
        private String title = "";
        private List<String> countries = new ArrayList<>();
        private String summary = "";
        private String content = "";
        private String priceType = "negotiable";
        private BigDecimal priceAmount;
        private String duration = "";
        private String contactPerson = "";
        private String contactPhone = "";
        private String contactEmail = "";
        private List<String> tags = new ArrayList<>();
        private String status = "draft";
        private Integer viewsCount = 0;
        private Integer inquiriesCount = 0;
        private List<Map<String, Object>> promoImages = new ArrayList<>();
        private List<Map<String, Object>> galleryImages = new ArrayList<>();
        private List<Map<String, Object>> introFiles = new ArrayList<>();
        private boolean promoProvided;
        private boolean galleryProvided;
        private boolean introProvided;
        private String validationMessage = "";

        private boolean isValid() {
            return validationMessage == null || validationMessage.isEmpty();
        }
    }
}
