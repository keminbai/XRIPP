package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.ISysUserService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/members")
@RequiredArgsConstructor
public class AdminMembersV3Controller {

    private final IMemberProfileService memberProfileService;
    private final ISysUserService sysUserService;
    private final StateTransitionService stateTransitionService;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "member_level", required = false) String memberLevel,
            @RequestParam(required = false) String keyword
    ) {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<MemberProfile> qw = new QueryWrapper<>();

        // Partner data scope: only see members whose sys_user.partner_id matches
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null) {
                return V3Response.error("AUTH_FORBIDDEN", "invalid partner context");
            }
            qw.inSql("user_id", "SELECT id FROM sys_user WHERE partner_id = " + partnerId);
        }

        if (memberLevel != null && !memberLevel.isBlank()) {
            qw.eq("member_level", memberLevel.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw)
                    .or().like("contact_person", kw)
                    .or().like("contact_phone", kw));
        }
        qw.orderByDesc("created_at");

        Page<MemberProfile> p = new Page<>(page, pageSize);
        Page<MemberProfile> result = memberProfileService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (MemberProfile mp : result.getRecords()) {
            items.add(toItem(mp, null));
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> detail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        MemberProfile mp = memberProfileService.getById(id);
        if (mp == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member profile not found");
        }

        // Partner scope check: verify the member belongs to this partner
        if (SecurityContextHolder.isPartner() && mp.getUserId() != null) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            SysUser memberUser = sysUserService.getById(mp.getUserId());
            if (memberUser == null || !partnerId.equals(memberUser.getPartnerId())) {
                return V3Response.error("AUTH_FORBIDDEN", "forbidden");
            }
        }

        // Enrich with sys_user username
        String username = null;
        if (mp.getUserId() != null) {
            SysUser user = sysUserService.getById(mp.getUserId());
            if (user != null) {
                username = user.getUsername();
            }
        }

        return V3Response.success(toItem(mp, username));
    }

    @PostMapping("/{id}/set-level")
    public V3Response<Map<String, Object>> setLevel(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        MemberProfile mp = memberProfileService.getById(id);
        if (mp == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member profile not found");
        }

        String newLevel = strVal(body, "member_level");
        if (!Set.of("normal", "vip", "svip").contains(newLevel)) {
            return V3Response.error("VALIDATION_ERROR", "member_level must be normal/vip/svip");
        }

        String oldLevel = mp.getMemberLevel() == null ? "normal" : mp.getMemberLevel();

        // Update vip_expire_time if provided
        String expireStr = strVal(body, "vip_expire_time");
        if (!expireStr.isBlank()) {
            try {
                Date expireTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(expireStr);
                mp.setVipExpireTime(expireTime);
            } catch (Exception e) {
                try {
                    Date expireTime = new SimpleDateFormat("yyyy-MM-dd").parse(expireStr);
                    mp.setVipExpireTime(expireTime);
                } catch (Exception e2) {
                    return V3Response.error("VALIDATION_ERROR", "vip_expire_time must be yyyy-MM-dd or yyyy-MM-dd HH:mm:ss");
                }
            }
        }

        mp.setMemberLevel(newLevel);
        memberProfileService.updateById(mp);

        // Log state transition (unified via StateTransitionService)
        stateTransitionService.log(
                "member_profile", id,
                oldLevel, newLevel,
                "set_level",
                strVal(body, "reason").isBlank() ? null : strVal(body, "reason")
        );

        return V3Response.success(toItem(mp, null));
    }

    private Map<String, Object> toItem(MemberProfile mp, String username) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mp.getId());
        m.put("userId", mp.getUserId());
        if (username != null) {
            m.put("username", username);
        }
        m.put("companyName", mp.getCompanyName() == null ? "" : mp.getCompanyName());
        m.put("industry", mp.getIndustry() == null ? "" : mp.getIndustry());
        m.put("contactPerson", mp.getContactPerson() == null ? "" : mp.getContactPerson());
        m.put("contactPhone", mp.getContactPhone() == null ? "" : mp.getContactPhone());
        m.put("memberLevel", mp.getMemberLevel() == null ? "normal" : mp.getMemberLevel());
        m.put("vipExpireTime", fmtDate(mp.getVipExpireTime()));
        m.put("invitationCode", mp.getInvitationCode() == null ? "" : mp.getInvitationCode());
        m.put("createdAt", fmtDate(mp.getCreatedAt()));
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
