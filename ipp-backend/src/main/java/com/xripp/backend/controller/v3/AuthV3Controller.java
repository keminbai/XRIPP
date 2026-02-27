package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.service.ISysUserService;
import com.xripp.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v3/auth")
@RequiredArgsConstructor
public class AuthV3Controller {

    private final ISysUserService sysUserService;
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
        boolean ok = stored.startsWith("$2")
                ? passwordEncoder.matches(password, stored)
                : password.equals(stored); // 兼容历史明文，后续建议全部迁移 BCrypt

        if (!ok) {
            return V3Response.error("AUTH_UNAUTHORIZED", "invalid credentials");
        }

        String role = (user.getRole() == null || user.getRole().isBlank())
                ? "member"
                : user.getRole().trim().toLowerCase();

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role, user.getPartnerId());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("role", role);
        userData.put("partner_id", user.getPartnerId());
        userData.put("status", user.getStatus());
        userData.put("member_level", resolveMemberLevel(user.getId(), role));

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
