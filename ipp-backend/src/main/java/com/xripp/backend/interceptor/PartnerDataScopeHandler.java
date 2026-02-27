package com.xripp.backend.interceptor;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.xripp.backend.config.DataScopeProperties;
import com.xripp.backend.security.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import org.springframework.stereotype.Component;

/**
 * Partner 数据隔离拦截器（MyBatis Plus 3.5.5 稳定实现）
 *
 * ⚠️ Phase 1 目标：
 * - 保证可编译
 * - 保证逻辑正确
 * - 不使用 Table 级 API（避免版本不兼容）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartnerDataScopeHandler implements DataPermissionHandler {

    private final DataScopeProperties dataScopeProperties;

    /**
     * MyBatis Plus 3.5.5 必须实现的方法
     */
    @Override
    public Expression getSqlSegment(
            Expression where,
            String mappedStatementId
    ) {

        // 1. 总开关
        if (!Boolean.TRUE.equals(dataScopeProperties.getEnabled())) {
            return where;
        }

        // 0.5 Mapper 白名单控制（避免对系统表误拦截）
        if (!dataScopeProperties.isMapperInWhiteList(mappedStatementId)) {
            return where;
        }

        // 2. 当前用户
        SecurityContextHolder.UserContext user =
                SecurityContextHolder.getContext();

        if (user == null) {
            return where;
        }

        // 3. admin 永久绕过
        if ("admin".equals(user.getRole())) {
            return where;
        }

        // 4. 仅 partner 生效
        if (!"partner".equals(user.getRole())) {
            return where;
        }

        Long partnerId = user.getPartnerId();
        if (partnerId == null) {
            log.warn("DataScope: partner 角色但 partnerId 为空，跳过");
            return where;
        }

        // ⚠️ Phase 1 说明：
        // 在 3.5.5 版本下，这里无法可靠获取 Table / Alias
        // 所以不做表名判断，白名单控制交由 Mapper 级规范
        // Phase 2 再升级到 Table API

        // 5. 构造 partner_id = ?
        Column partnerIdColumn = new Column("partner_id");

        EqualsTo condition = new EqualsTo();
        condition.setLeftExpression(partnerIdColumn);
        condition.setRightExpression(new LongValue(partnerId));

        // 6. 保留原 WHERE
        Expression finalWhere =
                (where != null)
                        ? new AndExpression(where, condition)
                        : condition;

        log.debug(
                "DataScope 生效: mapper={}, partnerId={}",
                mappedStatementId,
                partnerId
        );

        return finalWhere;
    }
}
