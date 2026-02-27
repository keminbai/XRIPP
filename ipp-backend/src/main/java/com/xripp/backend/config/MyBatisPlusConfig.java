package com.xripp.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.xripp.backend.interceptor.PartnerDataScopeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 *
 * @author XRIPP Team
 * @since 2026-02-08
 *
 * 配置内容：
 * 1. DataScope拦截器（数据隔离）
 * 2. 分页插件（SQL Server）
 * 3. Mapper扫描路径
 *
 * ⚠️ 插件执行顺序很重要：
 * 1. 先执行DataPermission（改写WHERE条件）
 * 2. 再执行Pagination（添加分页）
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisPlusConfig {

    private final PartnerDataScopeHandler partnerDataScopeHandler;
    private final DataScopeProperties dataScopeProperties;

    /**
     * MyBatis Plus 拦截器
     *
     * ⚠️ 注意插件顺序：
     * 1. DataPermissionInterceptor（数据权限）
     * 2. PaginationInnerInterceptor（分页）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // ========================================================================
        // 1. 数据权限拦截器（DataScope）
        // ========================================================================

        if (dataScopeProperties.getEnabled()) {
            DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor();
            dataPermissionInterceptor.setDataPermissionHandler(partnerDataScopeHandler);
            interceptor.addInnerInterceptor(dataPermissionInterceptor);

            log.info("DataScope拦截器已启用，白名单表数量: {}",
                    dataScopeProperties.getWhiteList().size());
            log.info("白名单表: {}", dataScopeProperties.getWhiteList());
        } else {
            log.warn("DataScope拦截器已禁用（xripp.data-scope.enabled=false）");
        }

        // ========================================================================
        // 2. 分页插件（SQL Server 2022）
        // ========================================================================

        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.SQL_SERVER);

        // 单页分页条数限制（防止恶意查询）
        paginationInterceptor.setMaxLimit(500L);

        // 溢出总页数后是否进行处理
        paginationInterceptor.setOverflow(false);

        interceptor.addInnerInterceptor(paginationInterceptor);

        log.info("分页插件已启用（SQL Server 2022），单页最大条数: 500");

        return interceptor;
    }

    /**
     * 配置元数据对象处理器
     *
     * 功能：
     * 1. 自动填充创建时间、更新时间
     * 2. 自动填充创建人、更新人
     */
    // 注释：Phase 1暂不启用，后续需要时再开启
    // @Bean
    // public MetaObjectHandler metaObjectHandler() {
    //     return new CustomMetaObjectHandler();
    // }
}