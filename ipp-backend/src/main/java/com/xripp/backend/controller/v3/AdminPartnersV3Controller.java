package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IPartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/partners")
@RequiredArgsConstructor
public class AdminPartnersV3Controller {

    private final IPartnerService partnerService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<Partner> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("partner_name", kw)
                    .or().like("partner_no", kw)
                    .or().like("contact_person", kw));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");

        Page<Partner> p = new Page<>(page, pageSize);
        Page<Partner> result = partnerService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Partner partner : result.getRecords()) {
            items.add(toItem(partner));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String partnerName = strVal(body, "partner_name");
        String contactPerson = strVal(body, "contact_person");
        String contactPhone = strVal(body, "contact_phone");

        if (partnerName.isBlank() || contactPerson.isBlank() || contactPhone.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "partner_name, contact_person, contact_phone are required");
        }

        String ts8 = String.valueOf(System.currentTimeMillis()).substring(5);
        String hex4 = String.format("%04x", new Random().nextInt(0xFFFF));

        Partner partner = new Partner();
        partner.setPartnerName(partnerName);
        partner.setPartnerNo("PTR-" + ts8);
        partner.setInvitationCode("INV-" + ts8.substring(ts8.length() - 6) + hex4);
        partner.setContactPerson(contactPerson);
        partner.setContactPhone(contactPhone);
        partner.setProvince(strVal(body, "province"));
        partner.setCityCode(strVal(body, "city_code"));
        partner.setCityName(strVal(body, "city_name"));
        partner.setStatus(true);

        Date now = new Date();
        partner.setCreatedAt(now);
        partner.setUpdatedAt(now);

        partnerService.save(partner);

        return V3Response.success(toItem(partner));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(id);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }
        return V3Response.success(toItem(partner));
    }

    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(id);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        if (body.containsKey("partner_name")) partner.setPartnerName(strVal(body, "partner_name"));
        if (body.containsKey("contact_person")) partner.setContactPerson(strVal(body, "contact_person"));
        if (body.containsKey("contact_phone")) partner.setContactPhone(strVal(body, "contact_phone"));
        if (body.containsKey("province")) partner.setProvince(strVal(body, "province"));
        if (body.containsKey("city_code")) partner.setCityCode(strVal(body, "city_code"));
        if (body.containsKey("city_name")) partner.setCityName(strVal(body, "city_name"));
        partner.setUpdatedAt(new Date());

        partnerService.updateById(partner);
        return V3Response.success(toItem(partner));
    }

    @PostMapping("/{id}/toggle-status")
    public V3Response<Map<String, Object>> toggleStatus(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(id);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        partner.setStatus(!Boolean.TRUE.equals(partner.getStatus()));
        partner.setUpdatedAt(new Date());
        partnerService.updateById(partner);

        return V3Response.success(toItem(partner));
    }

    @PostMapping("/{id}/renew")
    public V3Response<Map<String, Object>> renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(id);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        String expireDateStr = strVal(body, "expire_date");
        if (expireDateStr.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "expire_date is required (yyyy-MM-dd)");
        }

        LocalDate expireDate;
        try {
            expireDate = LocalDate.parse(expireDateStr);
        } catch (Exception e) {
            return V3Response.error("VALIDATION_ERROR", "expire_date must be yyyy-MM-dd format");
        }

        if (partner.getJoinDate() == null) {
            partner.setJoinDate(LocalDate.now());
        }
        partner.setExpireDate(expireDate);
        partner.setUpdatedAt(new Date());
        partnerService.updateById(partner);

        return V3Response.success(toItem(partner));
    }

    private Map<String, Object> toItem(Partner p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("partnerName", p.getPartnerName());
        m.put("partnerNo", p.getPartnerNo());
        m.put("province", p.getProvince());
        m.put("cityCode", p.getCityCode());
        m.put("cityName", p.getCityName());
        m.put("contactPerson", p.getContactPerson());
        m.put("contactPhone", p.getContactPhone());
        m.put("invitationCode", p.getInvitationCode());
        m.put("status", Boolean.TRUE.equals(p.getStatus()));
        m.put("joinDate", p.getJoinDate() != null ? p.getJoinDate().toString() : null);
        m.put("expireDate", p.getExpireDate() != null ? p.getExpireDate().toString() : null);
        m.put("createdAt", fmtDate(p.getCreatedAt()));
        m.put("updatedAt", fmtDate(p.getUpdatedAt()));
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
