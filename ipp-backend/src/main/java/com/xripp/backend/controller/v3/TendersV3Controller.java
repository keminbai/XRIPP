package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.TenderEntity;
import com.xripp.backend.service.ITenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/tenders")
@RequiredArgsConstructor
public class TendersV3Controller {

    private final ITenderService tenderService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String category
    ) {
        QueryWrapper<TenderEntity> qw = new QueryWrapper<>();
        qw.eq("tender_status", "published");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("title", kw)
                    .or().like("tender_no", kw)
                    .or().like("organization_name", kw));
        }
        if (country != null && !country.isBlank()) {
            qw.eq("country", country.trim());
        }
        if (category != null && !category.isBlank()) {
            qw.eq("category", category.trim());
        }
        qw.orderByDesc("is_top").orderByDesc("updated_at");

        Page<TenderEntity> p = new Page<>(page, pageSize);
        Page<TenderEntity> result = tenderService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (TenderEntity t : result.getRecords()) {
            items.add(toItem(t));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        TenderEntity t = tenderService.getById(id);
        if (t == null || !"published".equals(t.getTenderStatus())) {
            return V3Response.error("RESOURCE_NOT_FOUND", "tender not found");
        }

        Map<String, Object> m = toItem(t);
        m.put("description", t.getSummary() == null ? "" : t.getSummary());
        return V3Response.success(m);
    }

    private Map<String, Object> toItem(TenderEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("tenderNo", t.getTenderNo() == null ? "" : t.getTenderNo());
        m.put("title", t.getTitle() == null ? "" : t.getTitle());
        m.put("organization", t.getOrganizationName() == null ? "" : t.getOrganizationName());
        m.put("country", t.getCountry() == null ? "" : t.getCountry());
        String cat = (t.getCategory() == null || t.getCategory().isBlank()) ? "other" : t.getCategory();
        m.put("category", cat);
        m.put("categoryLabel", mapCategoryLabel(cat));
        m.put("price", t.getPrice() == null ? BigDecimal.ZERO : t.getPrice());
        m.put("isTop", Boolean.TRUE.equals(t.getIsTop()));
        m.put("publishDate", formatDate(t.getCreatedAt()));
        m.put("deadline", formatDateTime(t.getDeadlineAt()));
        return m;
    }

    private String mapCategoryLabel(String category) {
        return switch (category) {
            case "medical" -> "医疗器械";
            case "it" -> "IT设备";
            case "construction" -> "建筑工程";
            case "office" -> "办公用品";
            case "consulting" -> "咨询服务";
            default -> "其他";
        };
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private String formatDateTime(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
