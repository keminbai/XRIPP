package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.BenefitGrantLog;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.entity.PartnerBenefitPool;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IBenefitGrantLogService;
import com.xripp.backend.service.IPartnerBenefitPoolService;
import com.xripp.backend.service.IPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v3/admin/partners")
@RequiredArgsConstructor
public class BenefitPoolV3Controller {

    private static final Set<String> VALID_BENEFIT_TYPES = Set.of(
            "tender_download", "activity_free", "report_download", "supplier_quota"
    );

    private final IPartnerBenefitPoolService benefitPoolService;
    private final IBenefitGrantLogService benefitGrantLogService;
    private final IPartnerService partnerService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/{partnerId}/benefit-pool")
    public V3Response<List<Map<String, Object>>> listPool(@PathVariable Long partnerId) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(partnerId);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        QueryWrapper<PartnerBenefitPool> qw = new QueryWrapper<>();
        qw.eq("partner_id", partnerId);
        List<PartnerBenefitPool> pools = benefitPoolService.list(qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (PartnerBenefitPool pool : pools) {
            items.add(toPoolItem(pool));
        }

        return V3Response.success(items);
    }

    @PostMapping("/{partnerId}/benefit-pool/topup")
    public V3Response<Map<String, Object>> topup(
            @PathVariable Long partnerId,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Partner partner = partnerService.getById(partnerId);
        if (partner == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "partner not found");
        }

        String benefitType = strVal(body, "benefit_type");
        if (!VALID_BENEFIT_TYPES.contains(benefitType)) {
            return V3Response.error("VALIDATION_ERROR", "benefit_type must be one of: " + VALID_BENEFIT_TYPES);
        }

        Integer amount = intVal(body, "amount");
        if (amount == null || amount <= 0) {
            return V3Response.error("VALIDATION_ERROR", "amount must be a positive integer");
        }

        // Find or create pool row
        QueryWrapper<PartnerBenefitPool> qw = new QueryWrapper<>();
        qw.eq("partner_id", partnerId).eq("benefit_type", benefitType);
        PartnerBenefitPool pool = benefitPoolService.getOne(qw);

        if (pool == null) {
            pool = new PartnerBenefitPool();
            pool.setPartnerId(partnerId);
            pool.setBenefitType(benefitType);
            pool.setTotalAmount(amount);
            pool.setUsedAmount(0);
            pool.setVersion(0);
            pool.setUpdatedAt(new Date());
            benefitPoolService.save(pool);
        } else {
            pool.setTotalAmount(pool.getTotalAmount() + amount);
            pool.setUpdatedAt(new Date());
            benefitPoolService.updateById(pool);
        }

        // Re-read to get computed balance column
        pool = benefitPoolService.getById(pool.getId());
        return V3Response.success(toPoolItem(pool));
    }

    @Transactional
    @PostMapping("/{partnerId}/benefit-pool/grant")
    public V3Response<Map<String, Object>> grant(
            @PathVariable Long partnerId,
            @RequestBody Map<String, Object> body
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String benefitType = strVal(body, "benefit_type");
        if (!VALID_BENEFIT_TYPES.contains(benefitType)) {
            return V3Response.error("VALIDATION_ERROR", "benefit_type must be one of: " + VALID_BENEFIT_TYPES);
        }

        Integer amount = intVal(body, "amount");
        if (amount == null || amount <= 0) {
            return V3Response.error("VALIDATION_ERROR", "amount must be a positive integer");
        }

        Long memberId = longVal(body, "member_id");
        if (memberId == null || memberId <= 0) {
            return V3Response.error("VALIDATION_ERROR", "member_id is required (sys_user.id)");
        }

        int balanceAfter = -1;

        if (partnerId == 0) {
            // Admin platform grant: no pool deduction, unlimited
            balanceAfter = -1;
            log.info("[BenefitGrant] admin platform grant (no pool deduction) memberId={} type={} amount={}",
                    memberId, benefitType, amount);
        } else {
            // Partner pool grant: pessimistic lock + balance check + CAS deduction
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, total_amount, used_amount, version " +
                    "FROM partner_benefit_pool WITH (UPDLOCK, ROWLOCK) " +
                    "WHERE partner_id = ? AND benefit_type = ?",
                    partnerId, benefitType
            );

            if (rows.isEmpty()) {
                return V3Response.error("RESOURCE_NOT_FOUND", "no benefit pool found for this partner and type");
            }

            Map<String, Object> poolRow = rows.get(0);
            Long poolId = ((Number) poolRow.get("id")).longValue();
            int totalAmount = ((Number) poolRow.get("total_amount")).intValue();
            int usedAmount = ((Number) poolRow.get("used_amount")).intValue();
            int version = ((Number) poolRow.get("version")).intValue();
            int balance = totalAmount - usedAmount;

            if (balance < amount) {
                return V3Response.error("INSUFFICIENT_BALANCE",
                        "insufficient balance: available=" + balance + ", requested=" + amount);
            }

            int affected = jdbcTemplate.update(
                    "UPDATE partner_benefit_pool " +
                    "SET used_amount = used_amount + ?, version = version + 1, updated_at = GETUTCDATE() " +
                    "WHERE id = ? AND version = ? AND (total_amount - used_amount) >= ?",
                    amount, poolId, version, amount
            );

            if (affected != 1) {
                return V3Response.error("CONCURRENCY_ERROR", "concurrent modification detected, please retry");
            }

            balanceAfter = balance - amount;
        }

        // Insert grant log
        BenefitGrantLog grantLog = new BenefitGrantLog();
        grantLog.setPartnerId(partnerId);
        grantLog.setMemberId(memberId);
        grantLog.setBenefitType(benefitType);
        grantLog.setAmount(amount);
        grantLog.setOperatorId(SecurityContextHolder.getCurrentUserId());
        grantLog.setRemark(strVal(body, "remark"));
        grantLog.setCreatedAt(new Date());
        benefitGrantLogService.save(grantLog);

        log.info("[BenefitGrant] partnerId={} memberId={} type={} amount={} grantId={}",
                partnerId, memberId, benefitType, amount, grantLog.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("grantLogId", grantLog.getId());
        result.put("benefitType", benefitType);
        result.put("grantedAmount", amount);
        result.put("poolBalanceAfter", balanceAfter);
        return V3Response.success(result);
    }

    @GetMapping("/benefit-grant-logs")
    public V3Response<V3PageData<Map<String, Object>>> grantLogs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(name = "partner_id", required = false) Long partnerId,
            @RequestParam(name = "benefit_type", required = false) String benefitType
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<BenefitGrantLog> qw = new QueryWrapper<>();
        if (partnerId != null) qw.eq("partner_id", partnerId);
        if (benefitType != null && !benefitType.isBlank()) qw.eq("benefit_type", benefitType);
        qw.orderByDesc("created_at");

        Page<BenefitGrantLog> p = new Page<>(page, pageSize);
        Page<BenefitGrantLog> result = benefitGrantLogService.page(p, qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (BenefitGrantLog gl : result.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", gl.getId());
            m.put("partnerId", gl.getPartnerId());
            m.put("memberId", gl.getMemberId());
            m.put("benefitType", gl.getBenefitType());
            m.put("amount", gl.getAmount());
            m.put("operatorId", gl.getOperatorId());
            m.put("remark", gl.getRemark() == null ? "" : gl.getRemark());
            m.put("createdAt", fmtDate(gl.getCreatedAt()));
            items.add(m);
        }

        return V3Response.success(new V3PageData<>(
                items, result.getCurrent(), result.getSize(), result.getTotal()
        ));
    }

    private Map<String, Object> toPoolItem(PartnerBenefitPool pool) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pool.getId());
        m.put("partnerId", pool.getPartnerId());
        m.put("benefitType", pool.getBenefitType());
        m.put("totalAmount", pool.getTotalAmount());
        m.put("usedAmount", pool.getUsedAmount());
        m.put("balance", pool.getBalance());
        m.put("version", pool.getVersion());
        m.put("updatedAt", fmtDate(pool.getUpdatedAt()));
        return m;
    }

    private String strVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    private Integer intVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private Long longVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}
