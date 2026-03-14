package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IPartnerService;
import com.xripp.backend.service.ISysUserService;
import com.xripp.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/auth")
@RequiredArgsConstructor
public class AuthV3Controller {

    private final ISysUserService sysUserService;
    private final IMemberProfileService memberProfileService;
    private final IPartnerService partnerService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${jwt.expiration}")
    private Long expirationSeconds;

    @PostMapping("/login")
    public V3Response<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "username/password required");
        }

        SysUser user = sysUserService.findByUsername(username);
        if (user == null) {
            return V3Response.error("AUTH_UNAUTHORIZED", "invalid credentials");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            return V3Response.error("AUTH_FORBIDDEN", "account disabled");
        }

        String stored = user.getPasswordHash() == null ? "" : user.getPasswordHash();
        boolean ok = passwordEncoder.matches(password, stored);

        if (!ok) {
            return V3Response.error("AUTH_UNAUTHORIZED", "invalid credentials");
        }

        String role = (user.getRole() == null || user.getRole().isBlank())
                ? "member"
                : user.getRole().trim().toLowerCase();

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                role,
                user.getPartnerId(),
                user.getPermissionProfileId()
        );

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("role", role);
        userData.put("partner_id", user.getPartnerId());
        userData.put("permission_profile_id", user.getPermissionProfileId());
        userData.put("status", user.getStatus());
        userData.put("member_level", resolveMemberLevel(user.getId(), role));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("token_type", "Bearer");
        data.put("expires_in", expirationSeconds);
        data.put("user", userData);

        return V3Response.success(data);
    }

    @Transactional
    @PostMapping("/register")
    public V3Response<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String phone = (body.getOrDefault("phone", "")).trim();
        String password = (body.getOrDefault("password", "")).trim();
        String smsCode = (body.getOrDefault("sms_code", "")).trim();
        String companyName = (body.getOrDefault("company_name", "")).trim();
        String invitationCode = (body.getOrDefault("invitation_code", "")).trim();

        // 1. 入参校验
        if (phone.isEmpty() || password.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "手机号和密码为必填项");
        }
        if (phone.length() < 6 || phone.length() > 20) {
            return V3Response.error("VALIDATION_ERROR", "手机号格式不正确");
        }
        if (password.length() < 6) {
            return V3Response.error("VALIDATION_ERROR", "密码长度不能少于6位");
        }
        if (companyName.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "公司名称为必填项");
        }

        // 2. 验证码校验（开发期硬编码 123456 放行）
        if (smsCode.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "验证码为必填项");
        }
        if (!"123456".equals(smsCode)) {
            return V3Response.error("VALIDATION_ERROR", "验证码错误");
        }

        // 3. 手机号唯一性校验（phone 即 username）
        SysUser existing = sysUserService.findByUsername(phone);
        if (existing != null) {
            return V3Response.error("VALIDATION_ERROR", "该手机号已注册");
        }

        // 4. 邀请码 → partner_id 绑定
        Long partnerId = null;
        if (!invitationCode.isEmpty()) {
            Partner partner = partnerService.getOne(
                    new QueryWrapper<Partner>().eq("invitation_code", invitationCode)
            );
            if (partner == null) {
                return V3Response.error("VALIDATION_ERROR", "邀请码无效");
            }
            if (partner.getStatus() != null && !partner.getStatus()) {
                return V3Response.error("VALIDATION_ERROR", "该合伙人已停用");
            }
            partnerId = partner.getId();
        }

        // 5. 创建 sys_user（捕获并发注册导致的唯一键冲突）
        SysUser user = new SysUser();
        user.setUsername(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole("member");
        user.setPartnerId(partnerId);
        user.setStatus((byte) 1);
        user.setCreatedAt(new Date());
        try {
            sysUserService.save(user);
        } catch (DataIntegrityViolationException e) {
            return V3Response.error("VALIDATION_ERROR", "该手机号已注册");
        }

        // 6. 初始化 member_profile（company_name NOT NULL）
        MemberProfile profile = new MemberProfile();
        profile.setUserId(user.getId());
        profile.setCompanyName(companyName);
        profile.setContactPhone(phone);
        profile.setMemberLevel("normal");
        profile.setVipLevel("FREE");
        profile.setCreatedAt(new Date());
        if (!invitationCode.isEmpty()) {
            profile.setInvitationCode(invitationCode);
        }
        memberProfileService.save(profile);

        // 7. 注册即登录 — 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "member", partnerId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("role", "member");
        userData.put("partner_id", partnerId);
        userData.put("permission_profile_id", null);
        userData.put("status", 1);
        userData.put("member_level", "NORMAL");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("token_type", "Bearer");
        data.put("expires_in", expirationSeconds);
        data.put("user", userData);

        return V3Response.success(data);
    }

    private String resolveMemberLevel(Long userId, String role) {
        if (!"member".equals(role) || userId == null) {
            return null;
        }

        try {
            // 你库里已确认有 member_profile.member_level
            String sql = "SELECT TOP 1 member_level FROM dbo.member_profile WHERE user_id = ?";
            String level = jdbcTemplate.query(
                    sql,
                    ps -> ps.setLong(1, userId),
                    rs -> rs.next() ? rs.getString(1) : null
            );

            if (level == null || level.isBlank()) {
                return "NORMAL";
            }

            String normalized = level.trim().toUpperCase();
            return ("VIP".equals(normalized) || "SVIP".equals(normalized)) ? normalized : "NORMAL";
        } catch (Exception ignored) {
            return "NORMAL";
        }
    }
}
