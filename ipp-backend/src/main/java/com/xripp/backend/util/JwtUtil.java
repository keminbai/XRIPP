package com.xripp.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 *
 * @author XRIPP Team
 * @since 2026-02-08
 *
 * 功能：
 * 1. 生成Token
 * 2. 解析Token
 * 3. 验证Token有效性
 * 4. 从Token中提取用户信息
 *
 * Token格式：
 * Header.Payload.Signature
 *
 * Payload包含：
 * - userId: 用户ID
 * - username: 用户名
 * - role: 角色（admin/partner/member）
 * - partnerId: 合伙人ID（如果是合伙人或会员）
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token有效期（秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Token前缀
     */
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    // ========================================================================
    // 生成Token
    // ========================================================================

    /**
     * 生成Token（完整参数）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @param partnerId 合伙人ID
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String role, Long partnerId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 只有合伙人和会员才有partnerId
        if (partnerId != null) {
            claims.put("partnerId", partnerId);
        }

        return createToken(claims);
    }

    /**
     * 生成Token（简化版：无partnerId）
     */
    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, null);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    // ========================================================================
    // 解析Token
    // ========================================================================

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            // 移除Token前缀（如果有）
            if (token.startsWith(tokenPrefix)) {
                token = token.substring(tokenPrefix.length()).trim();
            }

            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("role", String.class);
    }

    /**
     * 从Token中获取合伙人ID
     */
    public Long getPartnerIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }

        Object partnerIdObj = claims.get("partnerId");
        if (partnerIdObj == null) {
            return null;
        }

        // 处理Integer转Long的情况
        if (partnerIdObj instanceof Integer) {
            return ((Integer) partnerIdObj).longValue();
        }

        return claims.get("partnerId", Long.class);
    }

    // ========================================================================
    // 验证Token
    // ========================================================================

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }

            // 检查是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否即将过期（1小时内）
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }

            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();

            // 小于1小时视为即将过期
            return diff < 3600000;
        } catch (Exception e) {
            return true;
        }
    }

    // ========================================================================
    // 私有方法
    // ========================================================================

    /**
     * 获取密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 刷新Token（生成新的Token，保持原有信息）
     */
    public String refreshToken(String oldToken) {
        Claims claims = getClaimsFromToken(oldToken);
        if (claims == null) {
            return null;
        }

        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        Long partnerId = claims.get("partnerId", Long.class);

        return generateToken(userId, username, role, partnerId);
    }
}