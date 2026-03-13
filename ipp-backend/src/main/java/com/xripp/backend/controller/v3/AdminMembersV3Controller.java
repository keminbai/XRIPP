package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.dto.MemberExportDTO;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IPartnerService;
import com.xripp.backend.service.ISysUserService;
import com.xripp.backend.service.StateTransitionService;
import com.xripp.backend.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/members")
@RequiredArgsConstructor
public class AdminMembersV3Controller {

    private final IMemberProfileService memberProfileService;
    private final ISysUserService sysUserService;
    private final IPartnerService partnerService;
    private final StateTransitionService stateTransitionService;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public V3Response<V3PageData<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "member_level", required = false) String memberLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String industry,
            @RequestParam(name = "invitation_code", required = false) String invitationCode,
            @RequestParam(name = "created_from", required = false) String createdFrom,
            @RequestParam(name = "created_to", required = false) String createdTo
    ) {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        QueryWrapper<MemberProfile> qw = new QueryWrapper<>();
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null) {
                return V3Response.error("AUTH_FORBIDDEN", "invalid partner context");
            }
            qw.inSql("user_id", "SELECT id FROM sys_user WHERE partner_id = " + partnerId);
        }

        applyMemberFilters(qw, memberLevel, keyword, industry, invitationCode, createdFrom, createdTo);
        qw.orderByDesc("created_at");

        Page<MemberProfile> p = new Page<>(page, pageSize);
        Page<MemberProfile> result = memberProfileService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (MemberProfile mp : result.getRecords()) {
            items.add(toItem(mp, loadUser(mp.getUserId())));
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
        if (!canAccessMember(mp)) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        return V3Response.success(toItem(mp, loadUser(mp.getUserId())));
    }

    @Transactional
    @PostMapping
    public V3Response<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String companyName = strVal(body, "company_name", "companyName");
        String contactPerson = strVal(body, "contact_person", "contactPerson");
        String contactPhone = strVal(body, "contact_phone", "contactPhone");
        String email = strVal(body, "email");
        String industry = strVal(body, "industry");
        String invitationCode = strVal(body, "invitation_code", "invitationCode");
        String memberLevel = normalizeMemberLevel(strVal(body, "member_level", "memberLevel"));
        String password = strVal(body, "password");

        if (companyName.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "company_name required");
        }
        if (contactPerson.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "contact_person required");
        }
        if (contactPhone.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "contact_phone required");
        }

        if (sysUserService.findByUsername(contactPhone) != null) {
            return V3Response.error("VALIDATION_ERROR", "contact_phone already exists");
        }

        Partner partner = findPartnerByInvitationCode(invitationCode);
        if (partner == null && !invitationCode.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "invitation_code invalid");
        }
        if (partner != null && partner.getStatus() != null && !partner.getStatus()) {
            return V3Response.error("VALIDATION_ERROR", "invitation_code disabled");
        }
        Long partnerId = partner == null ? null : partner.getId();

        Date now = new Date();
        SysUser user = new SysUser();
        user.setUsername(contactPhone);
        user.setPasswordHash(passwordEncoder.encode(password.isBlank() ? "123456" : password));
        user.setPhone(contactPhone);
        user.setEmail(email.isBlank() ? null : email);
        user.setRole("member");
        user.setPartnerId(partnerId);
        user.setStatus((byte) 1);
        user.setCreatedAt(now);

        try {
            sysUserService.save(user);
        } catch (DataIntegrityViolationException e) {
            return V3Response.error("VALIDATION_ERROR", "contact_phone already exists");
        }

        MemberProfile mp = new MemberProfile();
        mp.setUserId(user.getId());
        mp.setCompanyName(companyName);
        mp.setIndustry(industry.isBlank() ? null : industry);
        mp.setContactPerson(contactPerson);
        mp.setContactPhone(contactPhone);
        mp.setInvitationCode(invitationCode.isBlank() ? null : invitationCode);
        mp.setMemberLevel(memberLevel);
        mp.setVipLevel(toVipLevel(memberLevel));
        mp.setVipExpireTime(defaultExpireTime(memberLevel, null));
        mp.setCreatedAt(now);
        memberProfileService.save(mp);

        stateTransitionService.log(
                "member_profile",
                mp.getId(),
                null,
                memberLevel,
                "create",
                "admin_create_member"
        );

        return V3Response.success(toItem(mp, user));
    }

    @Transactional
    @PutMapping("/{id}")
    public V3Response<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        MemberProfile mp = memberProfileService.getById(id);
        if (mp == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member profile not found");
        }

        SysUser user = loadUser(mp.getUserId());
        if (user == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "sys_user not found");
        }

        String companyName = strVal(body, "company_name", "companyName");
        String contactPerson = strVal(body, "contact_person", "contactPerson");
        String contactPhone = strVal(body, "contact_phone", "contactPhone");
        String email = strVal(body, "email");
        String industry = strVal(body, "industry");
        String invitationCode = strVal(body, "invitation_code", "invitationCode");
        String memberLevel = normalizeMemberLevel(strVal(body, "member_level", "memberLevel"));

        if (companyName.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "company_name required");
        }
        if (contactPerson.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "contact_person required");
        }
        if (contactPhone.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "contact_phone required");
        }

        SysUser existing = sysUserService.findByUsername(contactPhone);
        if (existing != null && !existing.getId().equals(user.getId())) {
            return V3Response.error("VALIDATION_ERROR", "contact_phone already exists");
        }

        Partner partner = findPartnerByInvitationCode(invitationCode);
        if (partner == null && !invitationCode.isBlank()) {
            return V3Response.error("VALIDATION_ERROR", "invitation_code invalid");
        }
        if (partner != null && partner.getStatus() != null && !partner.getStatus()) {
            return V3Response.error("VALIDATION_ERROR", "invitation_code disabled");
        }
        Long partnerId = partner == null ? null : partner.getId();

        String oldLevel = normalizeMemberLevel(mp.getMemberLevel());
        mp.setCompanyName(companyName);
        mp.setIndustry(industry.isBlank() ? null : industry);
        mp.setContactPerson(contactPerson);
        mp.setContactPhone(contactPhone);
        mp.setInvitationCode(invitationCode.isBlank() ? null : invitationCode);
        mp.setMemberLevel(memberLevel);
        mp.setVipLevel(toVipLevel(memberLevel));
        mp.setVipExpireTime(defaultExpireTime(memberLevel, mp.getVipExpireTime()));
        memberProfileService.updateById(mp);

        user.setUsername(contactPhone);
        user.setPhone(contactPhone);
        user.setEmail(email.isBlank() ? null : email);
        user.setPartnerId(partnerId);
        sysUserService.updateById(user);

        stateTransitionService.log(
                "member_profile",
                mp.getId(),
                oldLevel,
                memberLevel,
                "update",
                "admin_edit_member"
        );

        return V3Response.success(toItem(mp, user));
    }

    @Transactional
    @PostMapping("/{id}/transition")
    public V3Response<Map<String, Object>> transition(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        MemberProfile mp = memberProfileService.getById(id);
        if (mp == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member profile not found");
        }
        SysUser user = loadUser(mp.getUserId());
        if (user == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "sys_user not found");
        }

        String toStatus = strVal(body, "to_status", "toStatus");
        if (!Set.of("active", "disabled").contains(toStatus)) {
            return V3Response.error("VALIDATION_ERROR", "to_status must be active|disabled");
        }

        String fromStatus = user.getStatus() != null && user.getStatus() == 1 ? "active" : "disabled";
        user.setStatus((byte) ("active".equals(toStatus) ? 1 : 0));
        sysUserService.updateById(user);

        stateTransitionService.log(
                "member_account",
                user.getId(),
                fromStatus,
                toStatus,
                toStatus,
                strVal(body, "reason")
        );

        return V3Response.success(toItem(mp, user));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public V3Response<Void> delete(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        MemberProfile mp = memberProfileService.getById(id);
        if (mp == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "member profile not found");
        }

        Long userId = mp.getUserId();
        if (userId != null && hasRelatedBusinessData(userId)) {
            return V3Response.error("STATE_INVALID_TRANSITION", "member has related business data, disable instead of delete");
        }
        stateTransitionService.log(
                "member_profile",
                mp.getId(),
                normalizeMemberLevel(mp.getMemberLevel()),
                "deleted",
                "delete",
                "admin_delete_member"
        );

        memberProfileService.removeById(id);
        if (userId != null) {
            sysUserService.removeById(userId);
        }
        return V3Response.success(null);
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

        String newLevel = normalizeMemberLevel(strVal(body, "member_level"));
        String oldLevel = normalizeMemberLevel(mp.getMemberLevel());
        mp.setMemberLevel(newLevel);
        mp.setVipLevel(toVipLevel(newLevel));

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
        } else {
            mp.setVipExpireTime(defaultExpireTime(newLevel, mp.getVipExpireTime()));
        }

        memberProfileService.updateById(mp);

        stateTransitionService.log(
                "member_profile",
                id,
                oldLevel,
                newLevel,
                "set_level",
                strVal(body, "reason").isBlank() ? null : strVal(body, "reason")
        );

        return V3Response.success(toItem(mp, loadUser(mp.getUserId())));
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(name = "member_level", required = false) String memberLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String industry,
            @RequestParam(name = "invitation_code", required = false) String invitationCode,
            @RequestParam(name = "created_from", required = false) String createdFrom,
            @RequestParam(name = "created_to", required = false) String createdTo,
            HttpServletResponse response
    ) throws Exception {
        if (!SecurityContextHolder.isAdmin() && !SecurityContextHolder.isPartner()) {
            response.setStatus(403);
            return;
        }

        QueryWrapper<MemberProfile> qw = new QueryWrapper<>();
        if (SecurityContextHolder.isPartner()) {
            Long partnerId = SecurityContextHolder.getCurrentPartnerId();
            if (partnerId == null) {
                response.setStatus(403);
                return;
            }
            qw.inSql("user_id", "SELECT id FROM sys_user WHERE partner_id = " + partnerId);
        }

        applyMemberFilters(qw, memberLevel, keyword, industry, invitationCode, createdFrom, createdTo);
        qw.orderByDesc("created_at");

        List<MemberProfile> all = memberProfileService.list(qw);
        List<MemberExportDTO> rows = new ArrayList<>();
        for (MemberProfile mp : all) {
            MemberExportDTO dto = new MemberExportDTO();
            dto.setId(mp.getId());
            dto.setCompanyName(mp.getCompanyName());
            dto.setContactPerson(mp.getContactPerson());
            dto.setContactPhone(mp.getContactPhone());
            dto.setIndustry(mp.getIndustry());
            dto.setMemberLevel(normalizeMemberLevel(mp.getMemberLevel()));
            dto.setInvitationCode(mp.getInvitationCode());
            dto.setCreatedAt(ExcelExportUtil.fmtDate(mp.getCreatedAt()));
            rows.add(dto);
        }
        ExcelExportUtil.write(response, "会员列表", MemberExportDTO.class, rows);
    }

    private Map<String, Object> toItem(MemberProfile mp, SysUser user) {
        String level = normalizeMemberLevel(mp.getMemberLevel());
        String accountStatus = "active";
        if (user != null && user.getStatus() != null && user.getStatus() != 1) {
            accountStatus = "disabled";
        } else if (mp.getVipExpireTime() != null && mp.getVipExpireTime().before(new Date())) {
            accountStatus = "expired";
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mp.getId());
        m.put("userId", mp.getUserId());
        m.put("username", user == null ? "" : safe(user.getUsername()));
        m.put("email", user == null ? "" : safe(user.getEmail()));
        m.put("partnerId", user == null ? null : user.getPartnerId());
        m.put("companyName", safe(mp.getCompanyName()));
        m.put("industry", safe(mp.getIndustry()));
        m.put("contactPerson", safe(mp.getContactPerson()));
        m.put("contactPhone", safe(mp.getContactPhone()));
        m.put("memberLevel", level);
        m.put("level", level);
        m.put("vipExpireTime", fmtDate(mp.getVipExpireTime()));
        m.put("invitationCode", safe(mp.getInvitationCode()));
        m.put("createdAt", fmtDate(mp.getCreatedAt()));
        m.put("status", accountStatus);
        m.put("statusLabel", mapStatusLabel(accountStatus));
        return m;
    }

    private boolean canAccessMember(MemberProfile mp) {
        if (SecurityContextHolder.isAdmin()) {
            return true;
        }
        if (!SecurityContextHolder.isPartner() || mp.getUserId() == null) {
            return false;
        }
        Long partnerId = SecurityContextHolder.getCurrentPartnerId();
        SysUser memberUser = sysUserService.getById(mp.getUserId());
        return memberUser != null && partnerId != null && partnerId.equals(memberUser.getPartnerId());
    }

    private SysUser loadUser(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return sysUserService.getById(userId);
    }

    private Partner findPartnerByInvitationCode(String invitationCode) {
        if (invitationCode == null || invitationCode.isBlank()) {
            return null;
        }
        return partnerService.getOne(new QueryWrapper<Partner>().eq("invitation_code", invitationCode.trim()));
    }

    private String normalizeMemberLevel(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if ("svip".equals(value)) return "svip";
        if ("vip".equals(value)) return "vip";
        return "normal";
    }

    private String toVipLevel(String memberLevel) {
        return switch (normalizeMemberLevel(memberLevel)) {
            case "svip" -> "SVIP";
            case "vip" -> "VIP";
            default -> "FREE";
        };
    }

    private Date defaultExpireTime(String memberLevel, Date existing) {
        String level = normalizeMemberLevel(memberLevel);
        if ("normal".equals(level)) {
            return null;
        }
        if (existing != null) {
            return existing;
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        return cal.getTime();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String mapStatusLabel(String status) {
        return switch (status) {
            case "disabled" -> "已禁用";
            case "expired" -> "已过期";
            default -> "正常";
        };
    }

    private String strVal(Map<String, Object> body, String... keys) {
        if (body == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            Object value = body.get(key);
            if (value != null && !String.valueOf(value).trim().isBlank()) {
                return String.valueOf(value).trim();
            }
        }
        return "";
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    private void applyMemberFilters(
            QueryWrapper<MemberProfile> qw,
            String memberLevel,
            String keyword,
            String industry,
            String invitationCode,
            String createdFrom,
            String createdTo
    ) {
        if (memberLevel != null && !memberLevel.isBlank()) {
            qw.eq("member_level", memberLevel.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("company_name", kw)
                    .or().like("contact_person", kw)
                    .or().like("contact_phone", kw));
        }
        if (industry != null && !industry.isBlank()) {
            qw.eq("industry", industry.trim());
        }
        if (invitationCode != null && !invitationCode.isBlank()) {
            qw.like("invitation_code", invitationCode.trim());
        }

        Date createdFromDate = parseDateStart(createdFrom);
        if (createdFromDate != null) {
            qw.ge("created_at", createdFromDate);
        }
        Date createdToDate = parseDateEndExclusive(createdTo);
        if (createdToDate != null) {
            qw.lt("created_at", createdToDate);
        }
    }

    private Date parseDateStart(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(raw.trim());
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Date parseDateEndExclusive(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(raw.trim()).plusDays(1);
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean hasRelatedBusinessData(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        String[] queries = new String[]{
                "SELECT COUNT(1) FROM orders WHERE user_id = ?",
                "SELECT COUNT(1) FROM demands WHERE user_id = ?",
                "SELECT COUNT(1) FROM member_verifications WHERE user_id = ?",
                "SELECT COUNT(1) FROM activity_registrations WHERE user_id = ?",
                "SELECT COUNT(1) FROM user_favorites WHERE user_id = ?",
                "SELECT COUNT(1) FROM supplier_onboarding WHERE user_id = ?",
                "SELECT COUNT(1) FROM benefit_grant_logs WHERE member_id = ?",
                "SELECT COUNT(1) FROM tender_download_logs WHERE user_id = ?"
        };
        for (String sql : queries) {
            try {
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
                if (count != null && count > 0) {
                    return true;
                }
            } catch (Exception ignored) {
                // Some environments may not have all tables yet; ignore missing-table checks.
            }
        }
        return false;
    }
}
