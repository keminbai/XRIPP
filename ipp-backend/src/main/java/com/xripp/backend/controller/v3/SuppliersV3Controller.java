package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.SupplierOnboarding;
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

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city
    ) {
        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();
        qw.eq("onboarding_status", "active");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw).or().like("intro", kw));
        }
        if (city != null && !city.isBlank()) {
            qw.like("city_name", city.trim());
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
        Map<String, Object> m = toItem(so);
        m.put("intro", safe(so.getIntro()));
        m.put("serviceTypesJson", safe(so.getServiceTypesJson()));
        return V3Response.success(m);
    }

    private Map<String, Object> toItem(SupplierOnboarding so) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", so.getId());
        m.put("supplierId", so.getSupplierId());
        m.put("companyName", safe(so.getCompanyName()));
        m.put("cityName", safe(so.getCityName()));
        m.put("serviceTypesJson", safe(so.getServiceTypesJson()));
        m.put("joinDate", formatDate(so.getCreatedAt()));
        m.put("verified", true);
        return m;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
