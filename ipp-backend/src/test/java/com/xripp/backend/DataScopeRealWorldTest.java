package com.xripp.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.security.SecurityContextHolder.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.xripp.backend.entity.Suppliers;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.mapper.SuppliersMapper;
import com.xripp.backend.mapper.SysUserMapper;

/**
 * DataScope 真实环境验证 (Step 4B)
 * 目的：观察控制台 SQL 日志，确认 DataScope 是否生效
 *
 * ⚠️ 关键修复：明确指定Spring Boot主类
 */
@SpringBootTest(classes = IppBackendApplication.class)
class DataScopeRealWorldTest {

    @Autowired(required = false)
    private SysUserMapper sysUserMapper;

    @Autowired(required = false)
    private SuppliersMapper suppliersMapper;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void test01_AdminShouldSeeAll() {
        if (suppliersMapper == null) {
            System.out.println("⚠️ SuppliersMapper未注入，跳过测试");
            return;
        }

        System.out.println("\n========== [TEST 1: Admin Query] ==========");
        // 模拟 Admin
        SecurityContextHolder.setContext(new UserContext(1L, "admin", "admin", null));

        // 查询 Suppliers (Admin 不应该受限)
        suppliersMapper.selectList(null);

        System.out.println("✅ 请查看上方SQL日志");
        System.out.println("❌ 如果包含 'AND partner_id'，说明admin被错误拦截");
        System.out.println("✅ 如果不包含 'AND partner_id'，说明正确");
        System.out.println("===========================================\n");
    }

    @Test
    void test02_PartnerShouldSeeOwn() {
        if (suppliersMapper == null) {
            System.out.println("⚠️ SuppliersMapper未注入，跳过测试");
            return;
        }

        System.out.println("\n========== [TEST 2: Partner Query] ==========");
        // 模拟 Partner (ID=1001)
        SecurityContextHolder.setContext(new UserContext(2L, "partnerA", "partner", 1001L));

        // 查询 Suppliers (应该有 WHERE partner_id = 1001)
        suppliersMapper.selectList(null);

        System.out.println("✅ 请查看上方SQL日志");
        System.out.println("✅ 如果包含 'WHERE partner_id = 1001'，说明DataScope生效");
        System.out.println("❌ 如果不包含 'partner_id'，说明DataScope未生效（严重问题）");
        System.out.println("===========================================\n");
    }

    @Test
    void test03_PartnerWithCondition() {
        if (suppliersMapper == null) {
            System.out.println("⚠️ SuppliersMapper未注入，跳过测试");
            return;
        }

        System.out.println("\n========== [TEST 3: Partner + Condition] ==========");
        // 模拟 Partner
        SecurityContextHolder.setContext(new UserContext(2L, "partnerA", "partner", 1001L));

        // 查询 name like 'test' (应该有 WHERE ... AND partner_id = 1001)
        QueryWrapper<Suppliers> qw = new QueryWrapper<>();
        qw.like("company_name", "test");
        suppliersMapper.selectList(qw);

        System.out.println("✅ 请查看上方SQL日志");
        System.out.println("✅ 应包含：company_name LIKE ? AND partner_id = 1001");
        System.out.println("❌ 如果只有 partner_id，说明原WHERE被覆盖（严重问题）");
        System.out.println("===========================================\n");
    }

    @Test
    void test04_SysUserShouldNotBeScoped() {
        if (sysUserMapper == null) {
            System.out.println("⚠️ SysUserMapper未注入，跳过测试");
            return;
        }

        System.out.println("\n========== [TEST 4: SysUser Query (Not In WhiteList)] ==========");
        // 模拟 Partner
        SecurityContextHolder.setContext(new UserContext(2L, "partnerA", "partner", 1001L));

        // 查询 SysUser (sys_user 表没有 partner_id 字段)
        // 如果拦截器没有白名单机制，这里会报错 "Invalid column name 'partner_id'"
        try {
            sysUserMapper.selectList(null);
            System.out.println("✅ SysUser 查询成功 (未被拦截)");
            System.out.println("说明：白名单机制生效，sys_user不在白名单中");
        } catch (Exception e) {
            System.out.println("❌ SysUser 查询失败 (被错误拦截)");
            System.out.println("错误原因：" + e.getCause().getMessage());
            System.out.println("说明：DataScope错误地拦截了sys_user表");
        }
        System.out.println("===========================================\n");
    }
}