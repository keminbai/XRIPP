package com.xripp.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xripp.backend.entity.OrderEntity;
import com.xripp.backend.entity.PaymentOrder;
import com.xripp.backend.entity.SupplierOnboarding;
import com.xripp.backend.entity.SupplierOnboardingCertificate;
import com.xripp.backend.entity.SupplierOnboardingFile;
import com.xripp.backend.exception.BusinessException;
import com.xripp.backend.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierOnboardingPaymentService {

    private static final String BIZ_TYPE = "supplier_onboarding";
    private static final String PAY_CHANNEL = "wechat_qr";

    private final ISupplierOnboardingService supplierOnboardingService;
    private final IOrderService orderService;
    private final IPaymentOrderService paymentOrderService;
    private final ISupplierOnboardingFileService supplierOnboardingFileService;
    private final ISupplierOnboardingCertificateService supplierOnboardingCertificateService;
    private final StateTransitionService stateTransitionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> createOrUpdateDraft(Long userId, Map<String, Object> body) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("请先登录");
        }

        String companyName = str(body.get("company_name"));
        String city = str(body.get("city"));
        String applyType = normalizeApplyType(body.get("apply_type"), body.get("type"));
        Long requestedApplicationId = longVal(body.get("application_id"));
        String serviceTypesJson = str(body.get("service_types_json"));
        String intro = str(body.get("intro"));
        String detailJson = str(body.get("detail_json"));
        List<Map<String, Object>> attachments = listOfMap(body.get("attachments"));
        List<Map<String, Object>> certificates = listOfMap(body.get("certificates"));

        if (companyName.isBlank()) {
            throw new BusinessException("company_name required");
        }
        if (city.isBlank()) {
            throw new BusinessException("city required");
        }
        if (applyType.isBlank()) {
            throw new BusinessException("apply_type required");
        }
        validateAttachments(attachments);
        validateCertificates(certificates);

        SupplierOnboarding blocking = findBlockingApplication(userId);
        if (blocking != null) {
            throw new BusinessException("您已有在途服务商申请，完成支付或审核结束后再发起新的申请");
        }

        SupplierOnboarding onboarding = resolveEditableApplication(userId, requestedApplicationId);
        String fromStatus = onboarding == null ? null : safeOr(onboarding.getOnboardingStatus(), "draft");
        Date now = new Date();
        BigDecimal feeAmount = resolveFee(applyType);
        boolean paymentAlreadySatisfied = onboarding != null && isPaymentSatisfied(onboarding.getPaymentStatus());

        if (onboarding == null) {
            onboarding = new SupplierOnboarding();
            onboarding.setUserId(userId);
            onboarding.setPartnerId(null);
            onboarding.setCreatedAt(now);
        } else if (paymentAlreadySatisfied && onboarding.getApplyType() != null
                && !safeOr(onboarding.getApplyType(), "normal").equals(applyType)) {
            throw new BusinessException("已支付且被驳回的申请仅允许修改资料，不允许变更入驻方案");
        }

        onboarding.setCompanyName(companyName);
        onboarding.setCityName(city);
        onboarding.setApplyType(applyType);
        onboarding.setFeeAmount(feeAmount);
        onboarding.setPaymentRequired(feeAmount.compareTo(BigDecimal.ZERO) > 0);
        onboarding.setServiceTypesJson(serviceTypesJson);
        onboarding.setIntro(intro);
        onboarding.setDetailJson(detailJson);
        onboarding.setPaymentStatus(resolveDraftPaymentStatus(onboarding, feeAmount, paymentAlreadySatisfied));
        onboarding.setOnboardingStatus(resolveDraftOnboardingStatus(feeAmount, paymentAlreadySatisfied));
        onboarding.setUpdatedAt(now);
        onboarding.setChangedAt(now);
        onboarding.setChangedBy(userId);
        onboarding.setSubmittedAt(null);
        onboarding.setReviewedAt(null);
        if (!paymentAlreadySatisfied) {
            onboarding.setPaymentOrderId(null);
            onboarding.setPaymentVerifiedAt(null);
            onboarding.setLastPaymentCheckAt(null);
        }

        if (onboarding.getId() == null) {
            supplierOnboardingService.save(onboarding);
        } else {
            supplierOnboardingService.updateById(onboarding);
        }

        replaceAttachments(onboarding.getId(), userId, attachments, now);
        replaceCertificates(onboarding.getId(), certificates, now);
        onboarding.setAttachmentsCompleted(Boolean.TRUE);
        onboarding.setCertificatesCompleted(Boolean.TRUE);

        OrderEntity order = upsertBusinessOrder(onboarding, userId, feeAmount, now, paymentAlreadySatisfied);
        PaymentOrder paymentOrder = paymentAlreadySatisfied
                ? getRequiredPaymentOrder(onboarding)
                : createFreshPaymentOrder(onboarding, userId, feeAmount, now);

        onboarding.setPaymentOrderId(paymentOrder.getId());
        onboarding.setUpdatedAt(now);
        supplierOnboardingService.updateById(onboarding);

        if (!safeOr(fromStatus, "").equals(onboarding.getOnboardingStatus())) {
            stateTransitionService.log(
                    "supplier_onboarding",
                    onboarding.getId(),
                    fromStatus,
                    onboarding.getOnboardingStatus(),
                    paymentAlreadySatisfied ? "revise_rejected_application" : "create_payment",
                    null
            );
        }

        Map<String, Object> data = buildPaymentInfo(onboarding, paymentOrder);
        data.put("orderStatus", order.getOrderStatus());
        data.put("message", paymentAlreadySatisfied
                ? "资料草稿已保存，当前申请已支付，无需重复缴费，可确认后重新提交审核"
                : "资料草稿已保存，请完成支付后再提交审核");
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentInfo(Long userId, Long applicationId) {
        SupplierOnboarding onboarding = getOwnedApplication(userId, applicationId);
        PaymentOrder paymentOrder = getRequiredPaymentOrder(onboarding);
        return buildPaymentInfo(onboarding, paymentOrder);
    }

    @Transactional
    public Map<String, Object> submitAfterPayment(Long userId, Long applicationId) {
        SupplierOnboarding onboarding = getOwnedApplication(userId, applicationId);

        if (!Boolean.TRUE.equals(onboarding.getAttachmentsCompleted()) || !Boolean.TRUE.equals(onboarding.getCertificatesCompleted())) {
            throw new BusinessException("请先补齐附件与资质资料，再提交审核");
        }

        if (Boolean.TRUE.equals(onboarding.getPaymentRequired())) {
            if (!"paid".equalsIgnoreCase(safeOr(onboarding.getPaymentStatus(), "unpaid"))) {
                throw new BusinessException("请先完成支付，再提交审核");
            }
            PaymentOrder paymentOrder = getRequiredPaymentOrder(onboarding);
            if (!"paid".equalsIgnoreCase(safeOr(paymentOrder.getPayStatus(), "created"))) {
                throw new BusinessException("支付状态未确认，请稍后刷新后重试");
            }
        }

        String fromStatus = safeOr(onboarding.getOnboardingStatus(), "draft");
        if (!List.of("draft", "pending_payment").contains(fromStatus)) {
            throw new BusinessException("当前状态不允许再次提交审核");
        }

        Date now = new Date();
        onboarding.setOnboardingStatus("submitted");
        onboarding.setSubmittedAt(now);
        onboarding.setPaymentVerifiedAt(onboarding.getPaymentVerifiedAt() == null ? now : onboarding.getPaymentVerifiedAt());
        onboarding.setSubmittedSnapshotJson(buildSubmittedSnapshot(onboarding));
        onboarding.setUpdatedAt(now);
        onboarding.setChangedAt(now);
        onboarding.setChangedBy(userId);
        onboarding.setReviewedAt(null);
        onboarding.setChangeReason(null);
        supplierOnboardingService.updateById(onboarding);

        OrderEntity order = getOrderByBizId(onboarding.getId());
        if (order != null && "paid".equals(order.getOrderStatus())) {
            order.setOrderStatus("in_service");
            order.setUpdatedAt(now);
            order.setChangedAt(now);
            order.setChangedBy(userId);
            order.setChangeReason("supplier_onboarding submitted");
            orderService.updateById(order);
        }

        stateTransitionService.log(
                "supplier_onboarding",
                onboarding.getId(),
                fromStatus,
                "submitted",
                "submit_after_payment",
                null
        );

        return Map.of(
                "id", onboarding.getId(),
                "onboarding_status", onboarding.getOnboardingStatus(),
                "payment_status", safeOr(onboarding.getPaymentStatus(), "unpaid")
        );
    }

    @Transactional
    public Map<String, Object> markSupplierOnboardingPaid(Map<String, Object> body) {
        String orderNo = str(body.get("order_no"));
        Long paymentOrderId = longVal(body.get("payment_order_id"));
        String payStatus = str(body.get("pay_status"));

        if (!"success".equalsIgnoreCase(payStatus)) {
            throw new BusinessException("pay_status must be success");
        }

        PaymentOrder paymentOrder = findPaymentOrder(orderNo, paymentOrderId);
        if (paymentOrder == null) {
            throw new BusinessException("payment order not found");
        }
        if (!BIZ_TYPE.equalsIgnoreCase(safeOr(paymentOrder.getBizType(), ""))) {
            throw new BusinessException("payment order biz_type mismatch");
        }

        Date now = new Date();
        String currentPayStatus = safeOr(paymentOrder.getPayStatus(), "created").toLowerCase(Locale.ROOT);
        if ("paid".equals(currentPayStatus)) {
            paymentOrder.setCallbackPayloadJson(writeJson(body));
            paymentOrder.setUpdatedAt(now);
            paymentOrderService.updateById(paymentOrder);
        } else {
            if (!List.of("created", "pending").contains(currentPayStatus)) {
                throw new BusinessException("payment order status does not allow payment callback");
            }
            paymentOrder.setPayStatus("paid");
            paymentOrder.setProviderTradeNo(str(body.get("provider_trade_no")));
            paymentOrder.setCallbackPayloadJson(writeJson(body));
            paymentOrder.setPaidAt(now);
            paymentOrder.setUpdatedAt(now);
            paymentOrderService.updateById(paymentOrder);
        }

        SupplierOnboarding onboarding = supplierOnboardingService.getById(paymentOrder.getBizId());
        if (onboarding == null) {
            throw new BusinessException("supplier onboarding not found");
        }
        if (onboarding.getPaymentOrderId() == null || !paymentOrder.getId().equals(onboarding.getPaymentOrderId())) {
            throw new BusinessException("payment order is not the active payment order for this application");
        }

        onboarding.setPaymentStatus("paid");
        onboarding.setPaymentOrderId(paymentOrder.getId());
        onboarding.setPaymentVerifiedAt(now);
        onboarding.setLastPaymentCheckAt(now);
        if (safeOr(onboarding.getOnboardingStatus(), "draft").equals("draft")) {
            onboarding.setOnboardingStatus("pending_payment");
        }
        onboarding.setUpdatedAt(now);
        supplierOnboardingService.updateById(onboarding);

        OrderEntity order = getOrderByBizId(onboarding.getId());
        if (order != null && !"paid".equals(order.getOrderStatus())) {
            String fromStatus = safeOr(order.getOrderStatus(), "awaiting_payment");
            order.setOrderStatus("paid");
            order.setUpdatedAt(now);
            order.setChangedAt(now);
            order.setChangeReason("payment callback");
            orderService.updateById(order);

            stateTransitionService.log(
                    "order",
                    order.getId(),
                    fromStatus,
                    "paid",
                    "payment_callback",
                    null
            );
        }

        return Map.of(
                "payment_order_id", paymentOrder.getId(),
                "payment_status", "paid",
                "application_id", onboarding.getId(),
                "onboarding_payment_status", onboarding.getPaymentStatus()
        );
    }

    private SupplierOnboarding getOwnedApplication(Long userId, Long applicationId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("请先登录");
        }
        SupplierOnboarding onboarding = supplierOnboardingService.getById(applicationId);
        if (onboarding == null) {
            throw new BusinessException("supplier onboarding not found");
        }
        if (!userId.equals(onboarding.getUserId())) {
            throw new BusinessException("forbidden");
        }
        return onboarding;
    }

    private void replaceAttachments(Long onboardingId, Long userId, List<Map<String, Object>> attachments, Date now) {
        supplierOnboardingFileService.remove(new QueryWrapper<SupplierOnboardingFile>()
                .eq("onboarding_id", onboardingId));

        List<SupplierOnboardingFile> entities = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            Map<String, Object> item = attachments.get(i);
            SupplierOnboardingFile file = new SupplierOnboardingFile();
            file.setOnboardingId(onboardingId);
            file.setFileCategory(normalizeFileCategory(item.get("file_category")));
            file.setFileName(str(item.get("file_name")));
            file.setFileUrl(str(item.get("file_url")));
            file.setFileExt(resolveFileExt(item));
            file.setFileSize(longVal(item.get("file_size")));
            file.setSortOrder(intVal(item.get("sort_order"), i));
            file.setUploadedBy(userId);
            file.setCreatedAt(now);
            file.setUpdatedAt(now);
            entities.add(file);
        }
        supplierOnboardingFileService.saveBatch(entities);
    }

    private void replaceCertificates(Long onboardingId, List<Map<String, Object>> certificates, Date now) {
        supplierOnboardingCertificateService.remove(new QueryWrapper<SupplierOnboardingCertificate>()
                .eq("onboarding_id", onboardingId));

        List<SupplierOnboardingCertificate> entities = new ArrayList<>();
        for (Map<String, Object> item : certificates) {
            SupplierOnboardingCertificate cert = new SupplierOnboardingCertificate();
            String certType = normalizeCertType(item.get("cert_type"));
            cert.setOnboardingId(onboardingId);
            cert.setCertType(certType);
            cert.setCertName(resolveCertName(certType, str(item.get("cert_name"))));
            cert.setHasCertificate(boolVal(item.get("has_certificate")));
            cert.setCertNo(str(item.get("cert_no")));
            cert.setValidFrom(localDateVal(item.get("valid_from")));
            cert.setValidTo(localDateVal(item.get("valid_to")));
            cert.setIssuerName(str(item.get("issuer_name")));
            cert.setRemarks(str(item.get("remarks")));
            cert.setAttachmentFileId(longVal(item.get("attachment_file_id")));
            cert.setCreatedAt(now);
            cert.setUpdatedAt(now);
            entities.add(cert);
        }
        supplierOnboardingCertificateService.saveBatch(entities);
    }

    private void validateAttachments(List<Map<String, Object>> attachments) {
        if (attachments.isEmpty()) {
            throw new BusinessException("请上传完整附件材料");
        }

        Set<String> required = Set.of("cover_image", "company_pdf", "promo_image", "business_license", "bank_license");
        Set<String> seen = new HashSet<>();

        for (Map<String, Object> item : attachments) {
            String category = normalizeFileCategory(item.get("file_category"));
            if (category.isBlank()) {
                throw new BusinessException("存在不支持的附件类型");
            }
            if (str(item.get("file_url")).isBlank()) {
                throw new BusinessException("附件缺少 file_url");
            }
            if (str(item.get("file_name")).isBlank()) {
                throw new BusinessException("附件缺少 file_name");
            }
            if (!"promo_image".equals(category) && !seen.add(category)) {
                throw new BusinessException("附件类型重复: " + category);
            }
            seen.add(category);
        }

        for (String category : required) {
            if (!seen.contains(category)) {
                throw new BusinessException("缺少必传附件: " + category);
            }
        }
    }

    private void validateCertificates(List<Map<String, Object>> certificates) {
        if (certificates.isEmpty()) {
            throw new BusinessException("请完整填写资质信息");
        }

        Set<String> required = Set.of("ISO9001", "ISO14001", "ISO45001", "ISO13485", "IATF16949", "ISO22000", "ISO27001");
        Set<String> seen = new HashSet<>();

        for (Map<String, Object> item : certificates) {
            String certType = normalizeCertType(item.get("cert_type"));
            Boolean hasCertificate = boolVal(item.get("has_certificate"));
            String certName = str(item.get("cert_name"));
            String certNo = str(item.get("cert_no"));

            if (certType.isBlank()) {
                throw new BusinessException("存在不支持的资质类型");
            }
            if (!seen.add(certType)) {
                throw new BusinessException("资质类型重复: " + certType);
            }
            if (hasCertificate == null) {
                throw new BusinessException("请明确填写资质是否具备: " + certType);
            }
            if ("OTHER".equals(certType) && certName.isBlank()) {
                throw new BusinessException("其他资质必须填写名称");
            }
            if (hasCertificate) {
                if (certNo.isBlank()) {
                    throw new BusinessException("请填写证书编号: " + resolveCertName(certType, certName));
                }
                if (localDateVal(item.get("valid_to")) == null) {
                    throw new BusinessException("请填写证书有效期: " + resolveCertName(certType, certName));
                }
            }
        }

        for (String certType : required) {
            if (!seen.contains(certType)) {
                throw new BusinessException("缺少资质声明: " + certType);
            }
        }
    }

    private SupplierOnboarding findBlockingApplication(Long userId) {
        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .in("onboarding_status", List.of("submitted", "precheck_passed", "final_review_passed", "active"))
                .orderByDesc("created_at")
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        return supplierOnboardingService.getOne(qw, false);
    }

    private SupplierOnboarding resolveEditableApplication(Long userId, Long applicationId) {
        if (applicationId != null && applicationId > 0) {
            SupplierOnboarding onboarding = supplierOnboardingService.getById(applicationId);
            if (onboarding == null || !userId.equals(onboarding.getUserId())) {
                throw new BusinessException("supplier onboarding not found");
            }
            String status = safeOr(onboarding.getOnboardingStatus(), "draft");
            if (!List.of("draft", "pending_payment", "precheck_failed", "final_review_failed").contains(status)) {
                throw new BusinessException("当前申请状态不允许继续修改");
            }
            return onboarding;
        }
        return findEditableDraft(userId);
    }

    private SupplierOnboarding findEditableDraft(Long userId) {
        QueryWrapper<SupplierOnboarding> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .in("onboarding_status", List.of("draft", "pending_payment", "precheck_failed", "final_review_failed"))
                .orderByDesc("created_at")
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        return supplierOnboardingService.getOne(qw, false);
    }

    private OrderEntity upsertBusinessOrder(SupplierOnboarding onboarding, Long userId, BigDecimal feeAmount, Date now,
                                            boolean paymentAlreadySatisfied) {
        OrderEntity order = getOrderByBizId(onboarding.getId());
        if (order == null) {
            order = new OrderEntity();
            order.setOrderNo("ORD-SUP-" + shortNo());
            order.setUserId(userId);
            order.setPartnerId(null);
            order.setOrderType("service");
            order.setBizType(BIZ_TYPE);
            order.setBizId(onboarding.getId());
            order.setCreatedAt(now);
        }

        order.setAmount(feeAmount);
        order.setCurrencyCode("CNY");
        if (paymentAlreadySatisfied) {
            String currentStatus = safeOr(order.getOrderStatus(), "");
            order.setOrderStatus(currentStatus.isBlank() ? "paid" : currentStatus);
        } else {
            order.setOrderStatus(feeAmount.compareTo(BigDecimal.ZERO) > 0 ? "awaiting_payment" : "paid");
        }
        order.setUpdatedAt(now);
        order.setChangedAt(now);
        order.setChangedBy(userId);
        order.setChangeReason(paymentAlreadySatisfied
                ? "supplier_onboarding rejected application revised"
                : "supplier_onboarding draft saved");

        if (order.getId() == null) {
            orderService.save(order);
        } else {
            orderService.updateById(order);
        }
        return order;
    }

    private PaymentOrder createFreshPaymentOrder(SupplierOnboarding onboarding, Long userId, BigDecimal feeAmount, Date now) {
        if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setOrderNo("PAY-SUP-" + shortNo());
            paymentOrder.setBizType(BIZ_TYPE);
            paymentOrder.setBizId(onboarding.getId());
            paymentOrder.setUserId(userId);
            paymentOrder.setPayChannel(PAY_CHANNEL);
            paymentOrder.setAmount(feeAmount);
            paymentOrder.setCurrencyCode("CNY");
            paymentOrder.setPayStatus("paid");
            paymentOrder.setPaidAt(now);
            paymentOrder.setCreatedAt(now);
            paymentOrder.setUpdatedAt(now);
            paymentOrderService.save(paymentOrder);
            return paymentOrder;
        }

        List<PaymentOrder> existing = paymentOrderService.list(new QueryWrapper<PaymentOrder>()
                .eq("biz_type", BIZ_TYPE)
                .eq("biz_id", onboarding.getId())
                .in("pay_status", List.of("created", "pending", "failed")));
        for (PaymentOrder item : existing) {
            item.setPayStatus("closed");
            item.setUpdatedAt(now);
            paymentOrderService.updateById(item);
        }

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setOrderNo("PAY-SUP-" + shortNo());
        paymentOrder.setBizType(BIZ_TYPE);
        paymentOrder.setBizId(onboarding.getId());
        paymentOrder.setUserId(userId);
        paymentOrder.setPayChannel(PAY_CHANNEL);
        paymentOrder.setAmount(feeAmount);
        paymentOrder.setCurrencyCode("CNY");
        paymentOrder.setPayStatus("pending");
        paymentOrder.setPaymentUrl("https://pay.xripp.local/wechat/checkout?order_no=" + paymentOrder.getOrderNo());
        paymentOrder.setQrCodeBase64(QrCodeUtil.toDataUrl(paymentOrder.getPaymentUrl(), 320, 320));
        paymentOrder.setExpiredAt(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)));
        paymentOrder.setCreatedAt(now);
        paymentOrder.setUpdatedAt(now);
        paymentOrderService.save(paymentOrder);
        return paymentOrder;
    }

    private String resolveDraftPaymentStatus(SupplierOnboarding onboarding, BigDecimal feeAmount, boolean paymentAlreadySatisfied) {
        if (paymentAlreadySatisfied) {
            return safeOr(onboarding.getPaymentStatus(), "paid");
        }
        return feeAmount.compareTo(BigDecimal.ZERO) > 0 ? "pending" : "waived";
    }

    private String resolveDraftOnboardingStatus(BigDecimal feeAmount, boolean paymentAlreadySatisfied) {
        if (paymentAlreadySatisfied) {
            return "draft";
        }
        return feeAmount.compareTo(BigDecimal.ZERO) > 0 ? "pending_payment" : "draft";
    }

    private boolean isPaymentSatisfied(String paymentStatus) {
        String normalized = safeOr(paymentStatus, "unpaid").toLowerCase(Locale.ROOT);
        return "paid".equals(normalized) || "waived".equals(normalized);
    }

    private PaymentOrder getRequiredPaymentOrder(SupplierOnboarding onboarding) {
        if (onboarding.getPaymentOrderId() == null) {
            throw new BusinessException("payment order not found");
        }
        PaymentOrder paymentOrder = paymentOrderService.getById(onboarding.getPaymentOrderId());
        if (paymentOrder == null) {
            throw new BusinessException("payment order not found");
        }
        return paymentOrder;
    }

    private PaymentOrder findPaymentOrder(String orderNo, Long paymentOrderId) {
        if (paymentOrderId != null && paymentOrderId > 0) {
            return paymentOrderService.getById(paymentOrderId);
        }
        if (!orderNo.isBlank()) {
            return paymentOrderService.getOne(new QueryWrapper<PaymentOrder>()
                    .eq("order_no", orderNo)
                    .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"), false);
        }
        return null;
    }

    private OrderEntity getOrderByBizId(Long bizId) {
        QueryWrapper<OrderEntity> qw = new QueryWrapper<>();
        qw.eq("biz_type", BIZ_TYPE)
                .eq("biz_id", bizId)
                .orderByDesc("created_at")
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        return orderService.getOne(qw, false);
    }

    private Map<String, Object> buildPaymentInfo(SupplierOnboarding onboarding, PaymentOrder paymentOrder) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("applicationId", onboarding.getId());
        data.put("companyName", safe(onboarding.getCompanyName()));
        data.put("applyType", safeOr(onboarding.getApplyType(), "normal"));
        data.put("feeAmount", onboarding.getFeeAmount() == null ? BigDecimal.ZERO : onboarding.getFeeAmount().setScale(2, RoundingMode.HALF_UP));
        data.put("onboardingStatus", safeOr(onboarding.getOnboardingStatus(), "draft"));
        data.put("paymentRequired", Boolean.TRUE.equals(onboarding.getPaymentRequired()));
        data.put("paymentStatus", safeOr(onboarding.getPaymentStatus(), "unpaid"));
        data.put("paymentOrderId", paymentOrder.getId());
        data.put("paymentOrderNo", safe(paymentOrder.getOrderNo()));
        data.put("payChannel", safeOr(paymentOrder.getPayChannel(), PAY_CHANNEL));
        data.put("payStatus", safeOr(paymentOrder.getPayStatus(), "created"));
        data.put("paymentUrl", safe(paymentOrder.getPaymentUrl()));
        data.put("qrCodeBase64", safe(paymentOrder.getQrCodeBase64()));
        data.put("providerTradeNo", safe(paymentOrder.getProviderTradeNo()));
        data.put("expiredAt", fmt(paymentOrder.getExpiredAt()));
        data.put("paidAt", fmt(paymentOrder.getPaidAt()));
        return data;
    }

    private String buildSubmittedSnapshot(SupplierOnboarding onboarding) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("company_name", onboarding.getCompanyName());
        snapshot.put("city", onboarding.getCityName());
        snapshot.put("apply_type", onboarding.getApplyType());
        snapshot.put("fee_amount", onboarding.getFeeAmount());
        snapshot.put("service_types_json", onboarding.getServiceTypesJson());
        snapshot.put("intro", onboarding.getIntro());
        snapshot.put("detail_json", onboarding.getDetailJson());
        snapshot.put("payment_status", onboarding.getPaymentStatus());
        snapshot.put("payment_order_id", onboarding.getPaymentOrderId());
        snapshot.put("payment_verified_at", fmt(onboarding.getPaymentVerifiedAt()));
        snapshot.put("attachments_completed", Boolean.TRUE.equals(onboarding.getAttachmentsCompleted()));
        snapshot.put("certificates_completed", Boolean.TRUE.equals(onboarding.getCertificatesCompleted()));
        snapshot.put("attachments", buildAttachmentSnapshot(onboarding.getId()));
        snapshot.put("certificates", buildCertificateSnapshot(onboarding.getId()));
        return writeJson(snapshot);
    }

    private List<Map<String, Object>> buildAttachmentSnapshot(Long onboardingId) {
        List<SupplierOnboardingFile> files = supplierOnboardingFileService.list(new QueryWrapper<SupplierOnboardingFile>()
                .eq("onboarding_id", onboardingId)
                .orderByAsc("sort_order")
                .orderByAsc("id"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboardingFile file : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", file.getId());
            item.put("file_category", safe(file.getFileCategory()));
            item.put("file_name", safe(file.getFileName()));
            item.put("file_url", safe(file.getFileUrl()));
            item.put("file_ext", safe(file.getFileExt()));
            item.put("file_size", file.getFileSize());
            item.put("sort_order", file.getSortOrder());
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> buildCertificateSnapshot(Long onboardingId) {
        List<SupplierOnboardingCertificate> certificates = supplierOnboardingCertificateService.list(new QueryWrapper<SupplierOnboardingCertificate>()
                .eq("onboarding_id", onboardingId)
                .orderByAsc("id"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SupplierOnboardingCertificate cert : certificates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", cert.getId());
            item.put("cert_type", safe(cert.getCertType()));
            item.put("cert_name", safe(cert.getCertName()));
            item.put("has_certificate", Boolean.TRUE.equals(cert.getHasCertificate()));
            item.put("cert_no", safe(cert.getCertNo()));
            item.put("valid_from", cert.getValidFrom() == null ? "" : cert.getValidFrom().toString());
            item.put("valid_to", cert.getValidTo() == null ? "" : cert.getValidTo().toString());
            item.put("issuer_name", safe(cert.getIssuerName()));
            item.put("remarks", safe(cert.getRemarks()));
            item.put("attachment_file_id", cert.getAttachmentFileId());
            items.add(item);
        }
        return items;
    }

    private BigDecimal resolveFee(String applyType) {
        return switch (applyType) {
            case "normal" -> new BigDecimal("5888.00");
            case "strategic" -> new BigDecimal("28888.00");
            default -> throw new BusinessException("invalid apply_type");
        };
    }

    private String normalizeApplyType(Object applyType, Object type) {
        String v = str(applyType);
        if (v.isBlank()) {
            v = str(type);
        }
        v = v.toLowerCase();
        if ("normal".equals(v) || "strategic".equals(v)) {
            return v;
        }
        return "";
    }

    private String normalizeFileCategory(Object value) {
        String v = str(value).toLowerCase();
        return switch (v) {
            case "cover_image", "company_pdf", "promo_image", "business_license", "bank_license", "other" -> v;
            default -> "";
        };
    }

    private String normalizeCertType(Object value) {
        String v = str(value).toUpperCase();
        return switch (v) {
            case "ISO9001", "ISO14001", "ISO45001", "ISO13485", "IATF16949", "ISO22000", "ISO27001", "OTHER" -> v;
            default -> "";
        };
    }

    private String resolveCertName(String certType, String certName) {
        if (!certName.isBlank()) {
            return certName;
        }
        return switch (certType) {
            case "ISO9001" -> "ISO 9001 质量认证";
            case "ISO14001" -> "ISO 14001 环境管理体系";
            case "ISO45001" -> "ISO 45001 职业健康安全";
            case "ISO13485" -> "ISO 13485 医疗器械";
            case "IATF16949" -> "IATF 16949 汽车行业";
            case "ISO22000" -> "ISO 22000 食品安全";
            case "ISO27001" -> "ISO 27001 信息安全";
            case "OTHER" -> "其他资质";
            default -> certType;
        };
    }

    private String resolveFileExt(Map<String, Object> item) {
        String ext = str(item.get("file_ext")).toLowerCase();
        if (!ext.isBlank()) {
            return ext;
        }
        String fileName = str(item.get("file_name"));
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        String fileUrl = str(item.get("file_url"));
        int urlDotIndex = fileUrl.lastIndexOf('.');
        if (urlDotIndex >= 0 && urlDotIndex < fileUrl.length() - 1) {
            return fileUrl.substring(urlDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private List<Map<String, Object>> listOfMap(Object raw) {
        if (raw == null) {
            return List.of();
        }
        try {
            return objectMapper.convertValue(raw, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException e) {
            throw new BusinessException("申请资料格式错误");
        }
    }

    private String writeJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    private String shortNo() {
        String ts = String.valueOf(System.currentTimeMillis());
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6)
                .toUpperCase();
        return ts.substring(Math.max(0, ts.length() - 8)) + suffix;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOr(String s, String dft) {
        return (s == null || s.isBlank()) ? dft : s;
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private Long longVal(Object o) {
        if (o == null) return null;
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer intVal(Object o, int dft) {
        if (o == null) return dft;
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception ignored) {
            return dft;
        }
    }

    private Boolean boolVal(Object o) {
        if (o == null) return null;
        if (o instanceof Boolean bool) return bool;
        String value = String.valueOf(o).trim().toLowerCase();
        if (value.isEmpty()) return null;
        if (Set.of("true", "1", "yes", "y").contains(value)) return Boolean.TRUE;
        if (Set.of("false", "0", "no", "n").contains(value)) return Boolean.FALSE;
        return null;
    }

    private LocalDate localDateVal(Object o) {
        String value = str(o);
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式错误: " + value);
        }
    }

    private String fmt(Date date) {
        if (date == null) return "";
        return Instant.ofEpochMilli(date.getTime()).toString();
    }
}
