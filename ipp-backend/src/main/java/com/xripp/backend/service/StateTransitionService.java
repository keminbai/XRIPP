package com.xripp.backend.service;

import com.xripp.backend.security.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 统一状态流转审计日志服务。
 *
 * <p>所有业务状态变更（订单、会员认证、供应商入库、内容发布等）
 * 必须在 @Transactional 范围内调用 {@link #log}，确保日志与业务变更原子提交。
 *
 * <p>写入失败时仅告警，不抛出异常，不影响主流程。
 * 线上若出现持续写入失败，应检查 state_transition_logs 表是否存在（Phase 2 DDL 是否已执行）。
 *
 * <p>目标表：{@code dbo.state_transition_logs}（见 DDL_Phase2_Migration.sql）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StateTransitionService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 记录一次状态流转。
     *
     * @param targetType 业务对象类型：order / member_verification / tender /
     *                   supplier_onboarding / activity_registration / content
     * @param targetId   业务对象主键
     * @param fromStatus 流转前状态（首次提交时传 null）
     * @param toStatus   流转后状态（不可为空）
     * @param action     触发动作：submit / approve / reject / pay / close /
     *                   complete / refund / publish / offline
     * @param reason     变更原因（驳回时必填，其他场景可为 null）
     */
    public void log(String targetType, Long targetId,
                    String fromStatus, String toStatus,
                    String action, String reason) {

        Long changedBy = SecurityContextHolder.getCurrentUserId();

        try {
            jdbcTemplate.update(
                    "INSERT INTO state_transition_logs " +
                    "(target_type, target_id, from_status, to_status, action, changed_by, change_reason) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    targetType, targetId, fromStatus, toStatus, action, changedBy, reason
            );
        } catch (Exception e) {
            // 日志写入失败不阻断主流程，但必须留下告警供排查
            log.error("[StateTransition] 写入失败 type={} id={} {}->{} err={}",
                    targetType, targetId, fromStatus, toStatus, e.getMessage());
        }
    }
}
