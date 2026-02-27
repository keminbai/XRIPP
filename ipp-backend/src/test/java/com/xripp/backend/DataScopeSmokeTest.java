package com.xripp.backend;

import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.security.SecurityContextHolder.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * DataScope 核心烟囱测试（Smoke Test）
 *
 * 目的：
 * 1. 验证 Spring Boot 测试上下文能正常启动
 * 2. 验证 SecurityContextHolder ThreadLocal 行为可控
 * 3. 为 PartnerDataScopeHandler 的运行提供最小可信保障
 *
 * ⚠️ 注意：
 * - 这是 Phase 1 测试
 * - 不连数据库
 * - 不校验 SQL 结果
 * - 只验证"链路可运行、不炸"
 */
@SpringBootTest(classes = IppBackendApplication.class)  // ← ✅ 修正为正确的类引用
class DataScopeSmokeTest {

    @AfterEach
    void clearContext() {
        // 防止 ThreadLocal 污染其他测试
        SecurityContextHolder.clearContext();
    }

    @Test
    void admin_user_should_be_marked_as_admin() {
        // 模拟 admin 用户
        SecurityContextHolder.setContext(
                new UserContext(
                        1L,
                        "admin",
                        "admin",
                        null
                )
        );

        assert SecurityContextHolder.isAdmin();
    }

    @Test
    void partner_user_should_have_partner_id() {
        // 模拟 partner 用户
        SecurityContextHolder.setContext(
                new UserContext(
                        2L,
                        "partner_user",
                        "partner",
                        1001L
                )
        );

        assert SecurityContextHolder.isPartner();
        assert SecurityContextHolder.getCurrentPartnerId() == 1001L;
    }
}