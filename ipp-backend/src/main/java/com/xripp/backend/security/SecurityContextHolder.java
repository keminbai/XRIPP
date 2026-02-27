package com.xripp.backend.security;

import lombok.Data;

/**
 * 用户上下文（线程安全）
 *
 * @author XRIPP Team
 * @since 2026-02-08
 *
 * 功能：
 * 1. 存储当前请求的用户信息
 * 2. 使用ThreadLocal保证线程安全
 * 3. 在请求结束后自动清理
 *
 * 使用场景：
 * - DataScope拦截器获取当前用户角色和partnerId
 * - Service层获取当前操作人信息
 * - 审计日志记录操作人
 *
 * ⚠️ 重要约定:
 * 1. 由JWT过滤器在每次请求时设置
 * 2. 由过滤器在请求结束后清理
 * 3. 不要在异步线程中使用（ThreadLocal特性）
 */
public class SecurityContextHolder {

    /**
     * 用户信息存储（ThreadLocal）
     */
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     */
    public static void setContext(UserContext userContext) {
        CONTEXT_HOLDER.set(userContext);
    }

    /**
     * 获取当前用户信息
     */
    public static UserContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前用户信息
     * ⚠️ 必须在finally块中调用，防止内存泄漏
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole() {
        UserContext context = getContext();
        return context != null ? context.getRole() : null;
    }

    /**
     * 获取当前用户的partnerId
     */
    public static Long getCurrentPartnerId() {
        UserContext context = getContext();
        return context != null ? context.getPartnerId() : null;
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "admin".equals(role);
    }

    /**
     * 判断当前用户是否为合伙人
     */
    public static boolean isPartner() {
        String role = getCurrentRole();
        return "partner".equals(role);
    }

    /**
     * 判断当前用户是否为会员
     */
    public static boolean isMember() {
        String role = getCurrentRole();
        return "member".equals(role);
    }

    // ========================================================================
    // 用户上下文数据类
    // ========================================================================

    /**
     * 用户上下文信息
     */
    @Data
    public static class UserContext {

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 角色（admin/partner/member）
         */
        private String role;

        /**
         * 合伙人ID
         * - admin: null
         * - partner: 自己的partners.id
         * - member: 所属partners.id
         */
        private Long partnerId;

        /**
         * 构造方法
         */
        public UserContext(Long userId, String username, String role, Long partnerId) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.partnerId = partnerId;
        }

        /**
         * 无参构造（必须保留）
         */
        public UserContext() {
        }
    }
}