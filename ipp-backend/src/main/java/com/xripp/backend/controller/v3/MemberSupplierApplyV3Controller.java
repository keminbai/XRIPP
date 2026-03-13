package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.PaymentOrder;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.entity.SupplierOnboardingCertificate;
import com.xripp.backend.entity.SupplierOnboardingFile;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.IPaymentOrderService;
import com.xripp.backend.service.ISupplierOnboardingCertificateService;
import com.xripp.backend.service.ISupplierOnboardingFileService;
import com.xripp.backend.service.ISupplierOnboardingService;
import com.xripp.backend.service.SupplierOnboardingPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 会员自助申请成为服务商
 * 区别于 SupplierOnboardingV3Controller（合伙人提交入库申请），
 * 本接口面向普通会员，不需要 partner 上下文。
 */
@RestController
@RequestMapping("/v3/member/supplier-apply")
@RequiredArgsConstructor
public class MemberSupplierApplyV3Controller {

    private final ISupplierOnboardingService service;
    private final ISupplierOnboardingFileService supplierOnboardingFileService;
    private final ISupplierOnboardingCertificateService supplierOnboardingCertificateService;
    private final IPaymentOrderService paymentOrderService;
    private final SupplierOnboardingPaymentService supplierOnboardingPaymentService;
    private final ObjectMapper objectMapper;

    /**
     * 兼容入口：改为“保存草稿并生成支付单”
     */
    @PostMapping
    public V3Response<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        return createDraft(body);
    }

    @PostMapping("/draft")
    public V3Response<Map<String, Object>> createDraft(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_REQUIRED", "please login");
        }
        return V3Response.success(supplierOnboardingPaymentService.createOrUpdateDraft(userId, body));
    }

    @GetMapping("/{id}/payment")
    public V3Response<Map<String, Object>> paymentInfo(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_REQUIRED", "please login");
        }
        return V3Response.success(supplierOnboardingPaymentService.getPaymentInfo(userId, id));
    }

    @PostMapping("/{id}/submit")
    public V3Response<Map<String, Object>> submitAfterPayment(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_REQUIRED", "please login");
        }
        return V3Response.success(supplierOnboardingPaymentService.submitAfterPayment(userId, id));
    }

    @GetMapping("/{id}")
    public V3Response<Map<String, Object>> applicationDetail(@PathVariable Long id) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_REQUIRED", "please login");
        }

        SupplierOnboarding so = service.getById(id);
        if (so == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "supplier onboarding not found");
        }
        if (!userId.equals(so.getUserId())) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        return V3Response.success(toDetailItem(so));
    }

    /**
     * 查询我的入驻申请状态
     */
    @GetMapping
    public V3Response<List<Map<String, Object>>> myApplications() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null) {
            return V3Response.error("AUTH_REQUIRED", "please login");
        }

        List<SupplierOnboarding> list = service.list(
                new QueryWrapper<SupplierOnboarding>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
        );

        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboarding so : list) {
            items.add(toListItem(so));
        }
        return V3Response.success(items);
    }

    private Map<String, Object> toListItem(SupplierOnboarding so) {
        Map<String, Object> detail = parseJsonObject(so.getDetailJson());
        List<Object> serviceTypes = parseJsonArray(so.getServiceTypesJson());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", so.getId());
        m.put("companyName", safe(so.getCompanyName()));
        m.put("cityName", safe(so.getCityName()));
        m.put("onboardingStatus", safeOr(so.getOnboardingStatus(), "draft"));
        m.put("onboardingStatusLabel", mapStatusLabel(safeOr(so.getOnboardingStatus(), "draft")));
        m.put("paymentStatus", safeOr(so.getPaymentStatus(), "unpaid"));
        m.put("applyType", safeOr(so.getApplyType(), "normal"));
        m.put("feeAmount", so.getFeeAmount());
        m.put("paymentOrderId", so.getPaymentOrderId());
        m.put("paymentVerifiedAt", fmtDate(so.getPaymentVerifiedAt()));
        m.put("attachmentsCompleted", Boolean.TRUE.equals(so.getAttachmentsCompleted()));
        m.put("certificatesCompleted", Boolean.TRUE.equals(so.getCertificatesCompleted()));
        m.put("contactName", safe(strObj(detail.get("contactName"))));
        m.put("contactPhone", safe(strObj(detail.get("contactPhone"))));
        m.put("mainService", safe(strObj(detail.get("mainService"))));
        m.put("serviceTypes", serviceTypes);
        m.put("submittedAt", fmtDate(so.getSubmittedAt()));
        m.put("reviewedAt", fmtDate(so.getReviewedAt()));
        m.put("changeReason", safe(so.getChangeReason()));
        m.put("createdAt", fmtDate(so.getCreatedAt()));
        m.put("updatedAt", fmtDate(so.getUpdatedAt()));
        return m;
    }

    private Map<String, Object> toDetailItem(SupplierOnboarding so) {
        Map<String, Object> m = toListItem(so);
        m.put("intro", safe(so.getIntro()));
        m.put("serviceTypesJson", safe(so.getServiceTypesJson()));
        m.put("detailJson", safe(so.getDetailJson()));
        m.put("detail", parseJsonObject(so.getDetailJson()));
        m.put("submittedSnapshotJson", safe(so.getSubmittedSnapshotJson()));
        m.put("submittedSnapshot", parseJsonObject(so.getSubmittedSnapshotJson()));
        m.put("paymentRequired", Boolean.TRUE.equals(so.getPaymentRequired()));
        m.put("lastPaymentCheckAt", fmtDate(so.getLastPaymentCheckAt()));
        m.put("attachments", buildAttachmentList(so.getId()));
        m.put("certificates", buildCertificateList(so.getId()));
        m.put("paymentOrder", buildPaymentOrder(so.getPaymentOrderId()));
        return m;
    }

    private List<Map<String, Object>> buildAttachmentList(Long onboardingId) {
        List<SupplierOnboardingFile> files = supplierOnboardingFileService.list(new QueryWrapper<SupplierOnboardingFile>()
                .eq("onboarding_id", onboardingId)
                .orderByAsc("sort_order")
                .orderByAsc("id"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboardingFile file : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", file.getId());
            item.put("fileCategory", safe(file.getFileCategory()));
            item.put("fileName", safe(file.getFileName()));
            item.put("fileUrl", safe(file.getFileUrl()));
            item.put("fileExt", safe(file.getFileExt()));
            item.put("fileSize", file.getFileSize());
            item.put("sortOrder", file.getSortOrder());
            item.put("createdAt", fmtDate(file.getCreatedAt()));
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> buildCertificateList(Long onboardingId) {
        List<SupplierOnboardingCertificate> certificates = supplierOnboardingCertificateService.list(new QueryWrapper<SupplierOnboardingCertificate>()
                .eq("onboarding_id", onboardingId)
                .orderByAsc("id"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboardingCertificate cert : certificates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cert.getId());
            item.put("certType", safe(cert.getCertType()));
            item.put("certName", safe(cert.getCertName()));
            item.put("hasCertificate", Boolean.TRUE.equals(cert.getHasCertificate()));
            item.put("certNo", safe(cert.getCertNo()));
            item.put("validFrom", cert.getValidFrom() == null ? "" : cert.getValidFrom().toString());
            item.put("validTo", cert.getValidTo() == null ? "" : cert.getValidTo().toString());
            item.put("issuerName", safe(cert.getIssuerName()));
            item.put("remarks", safe(cert.getRemarks()));
            items.add(item);
        }
        return items;
    }

    private Map<String, Object> buildPaymentOrder(Long paymentOrderId) {
        if (paymentOrderId == null) {
            return new LinkedHashMap<>();
        }
        PaymentOrder paymentOrder = paymentOrderService.getById(paymentOrderId);
        if (paymentOrder == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", paymentOrder.getId());
        m.put("orderNo", safe(paymentOrder.getOrderNo()));
        m.put("payChannel", safe(paymentOrder.getPayChannel()));
        m.put("amount", paymentOrder.getAmount());
        m.put("currencyCode", safe(paymentOrder.getCurrencyCode()));
        m.put("payStatus", safe(paymentOrder.getPayStatus()));
        m.put("providerTradeNo", safe(paymentOrder.getProviderTradeNo()));
        m.put("paymentUrl", safe(paymentOrder.getPaymentUrl()));
        m.put("paidAt", fmtDate(paymentOrder.getPaidAt()));
        m.put("expiredAt", fmtDate(paymentOrder.getExpiredAt()));
        return m;
    }

    private Map<String, Object> parseJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private List<Object> parseJsonArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<ArrayList<Object>>() {});
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private String mapStatusLabel(String status) {
        return switch (status) {
            case "draft" -> "草稿";
            case "pending_payment" -> "待支付";
            case "submitted" -> "已提交";
            case "precheck_passed" -> "预审通过";
            case "precheck_failed" -> "预审未通过";
            case "final_review_passed" -> "终审通过";
            case "final_review_failed" -> "终审未通过";
            case "active" -> "已激活";
            case "inactive" -> "已停用";
            default -> status;
        };
    }

    private String strObj(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String fmtDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
