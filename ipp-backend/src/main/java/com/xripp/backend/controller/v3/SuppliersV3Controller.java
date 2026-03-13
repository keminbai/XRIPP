package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.entity.SupplierOnboardingFile;
import com.xripp.backend.service.ISupplierOnboardingFileService;
import com.xripp.backend.service.ISupplierOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 公开供应商名录接口（无需登录）。
 *
 * <p>数据来源：supplier_onboarding WHERE onboarding_status='active'
 * <p>对应前端：app/pages/supplier-directory.vue
 */
@RestController
@RequestMapping("/v3/suppliers")
@RequiredArgsConstructor
public class SuppliersV3Controller {

    private final ISupplierOnboardingService supplierOnboardingService;
    private final ISupplierOnboardingFileService supplierOnboardingFileService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(name = "service_type", required = false) String serviceType,
            @RequestParam(name = "apply_type", required = false) String applyType
    ) {
        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();
        qw.eq("onboarding_status", "active");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw)
                    .or().like("intro", kw)
                    .or().like("detail_json", kw)
                    .or().like("service_types_json", kw));
        }
        if (city != null && !city.isBlank()) {
            qw.like("city_name", city.trim());
        }
        if (serviceType != null && !serviceType.isBlank()) {
            qw.like("detail_json", "\"mainService\":\"" + serviceType.trim() + "\"");
        }
        if (applyType != null && !applyType.isBlank()) {
            qw.eq("apply_type", applyType.trim());
        }
        qw.orderByDesc("updated_at");

        Page<SupplierOnboarding> p = new Page<>(page, pageSize);
        Page<SupplierOnboarding> result = supplierOnboardingService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboarding so : result.getRecords()) {
            items.add(toItem(so));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        SupplierOnboarding so = supplierOnboardingService.getById(id);
        if (so == null || !"active".equals(so.getOnboardingStatus())) {
            return V3Response.error("RESOURCE_NOT_FOUND", "supplier not found");
        }
        Map<String, Object> m = toDetailItem(so);
        return V3Response.success(m);
    }

    private Map<String, Object> toItem(SupplierOnboarding so) {
        Map<String, Object> detail = parseJsonObject(so.getDetailJson());
        Map<String, Object> publicFiles = loadPublicFiles(so.getId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", so.getId());
        m.put("supplierId", so.getSupplierId());
        m.put("companyName", safe(so.getCompanyName()));
        m.put("cityName", safe(so.getCityName()));
        m.put("mainService", safe(strObj(detail.get("mainService"))));
        m.put("mainServiceLabel", mainServiceLabel(strObj(detail.get("mainService"))));
        m.put("applyType", safeOr(so.getApplyType(), "normal"));
        m.put("applyTypeLabel", applyTypeLabel(safeOr(so.getApplyType(), "normal")));
        m.put("intro", safe(so.getIntro()));
        m.put("serviceTypesJson", safe(so.getServiceTypesJson()));
        m.put("serviceTypes", parseJsonArray(so.getServiceTypesJson()));
        m.put("industryList", toStringList(detail.get("industryList")));
        m.put("isoList", toStringList(detail.get("isoList")));
        m.put("coverImageUrl", safe(strObj(publicFiles.get("cover_image"))));
        m.put("promoImageUrl", safe(strObj(publicFiles.get("promo_image"))));
        m.put("companyPdfUrl", safe(strObj(publicFiles.get("company_pdf"))));
        m.put("joinDate", formatDate(so.getCreatedAt()));
        m.put("verified", true);
        return m;
    }

    private Map<String, Object> toDetailItem(SupplierOnboarding so) {
        Map<String, Object> detail = parseJsonObject(so.getDetailJson());
        Map<String, Object> m = toItem(so);
        m.put("companyNameZh", safe(strObj(detail.get("companyNameZh"))));
        m.put("companyNameEn", safe(strObj(detail.get("companyNameEn"))));
        m.put("nature", safe(strObj(detail.get("nature"))));
        m.put("regCapital", safe(strObj(detail.get("regCapital"))));
        m.put("foundDate", safe(strObj(detail.get("foundDate"))));
        m.put("mainService", safe(strObj(detail.get("mainService"))));
        m.put("mainServiceLabel", mainServiceLabel(strObj(detail.get("mainService"))));
        m.put("intro", safe(so.getIntro()));
        return m;
    }

    private Map<String, Object> loadPublicFiles(Long onboardingId) {
        List<SupplierOnboardingFile> files = supplierOnboardingFileService.list(new QueryWrapper<SupplierOnboardingFile>()
                .eq("onboarding_id", onboardingId)
                .in("file_category", List.of("cover_image", "promo_image", "company_pdf"))
                .orderByAsc("sort_order")
                .orderByAsc("id"));
        Map<String, Object> result = new LinkedHashMap<>();
        for (SupplierOnboardingFile file : files) {
            result.put(file.getFileCategory(), safe(file.getFileUrl()));
        }
        return result;
    }

    private Map<String, Object> parseJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private List<String> parseJsonArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<ArrayList<String>>() {});
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private List<String> toStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<String> items = new ArrayList<>();
        for (Object item : list) {
            if (item != null && !String.valueOf(item).isBlank()) {
                items.add(String.valueOf(item).trim());
            }
        }
        return items;
    }

    private String applyTypeLabel(String applyType) {
        return switch (safeOr(applyType, "normal")) {
            case "strategic" -> "战略服务商";
            case "normal" -> "普通服务商";
            default -> safeOr(applyType, "-");
        };
    }

    private String mainServiceLabel(String mainService) {
        return switch (safeOr(mainService, "")) {
            case "manufacturing" -> "制造业";
            case "trade" -> "贸易类";
            case "service" -> "服务业";
            case "bulk" -> "大宗贸易";
            case "construction" -> "建筑业";
            case "tech" -> "科技型企业";
            case "other" -> "其他";
            default -> safeOr(mainService, "-");
        };
    }

    private String strObj(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
