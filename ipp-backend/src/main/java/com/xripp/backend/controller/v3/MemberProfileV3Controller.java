package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IMemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/member/profile")
@RequiredArgsConstructor
public class MemberProfileV3Controller {

    private final IMemberProfileService memberProfileService;

    @GetMapping
    public V3Response<Map<String, Object>> get() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        MemberProfile profile = getByUserId(userId);
        if (profile == null) {
            // Return empty shell if no profile exists
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("id", null);
            empty.put("userId", userId);
            empty.put("companyName", "");
            empty.put("industry", "");
            empty.put("contactPerson", "");
            empty.put("contactPhone", "");
            empty.put("memberLevel", "normal");
            return V3Response.success(empty);
        }

        return V3Response.success(toItem(profile));
    }

    @PutMapping
    public V3Response<Map<String, Object>> update(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        MemberProfile profile = getByUserId(userId);
        if (profile == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "profile not found, please contact admin");
        }

        if (body.containsKey("company_name")) {
            profile.setCompanyName(strVal(body, "company_name"));
        }
        if (body.containsKey("industry")) {
            profile.setIndustry(strVal(body, "industry"));
        }
        if (body.containsKey("contact_person")) {
            profile.setContactPerson(strVal(body, "contact_person"));
        }
        if (body.containsKey("contact_phone")) {
            profile.setContactPhone(strVal(body, "contact_phone"));
        }

        memberProfileService.updateById(profile);
        return V3Response.success(toItem(profile));
    }

    private MemberProfile getByUserId(Long userId) {
        QueryWrapper<MemberProfile> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        return memberProfileService.getOne(qw);
    }

    private Map<String, Object> toItem(MemberProfile p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("userId", p.getUserId());
        m.put("companyName", p.getCompanyName() == null ? "" : p.getCompanyName());
        m.put("industry", p.getIndustry() == null ? "" : p.getIndustry());
        m.put("contactPerson", p.getContactPerson() == null ? "" : p.getContactPerson());
        m.put("contactPhone", p.getContactPhone() == null ? "" : p.getContactPhone());
        m.put("memberLevel", p.getMemberLevel() == null ? "normal" : p.getMemberLevel());
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }
}
