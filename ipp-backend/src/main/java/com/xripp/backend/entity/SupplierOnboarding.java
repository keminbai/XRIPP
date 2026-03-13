package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("supplier_onboarding")
public class SupplierOnboarding {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("supplier_id")
    private Long supplierId;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("user_id")
    private Long userId;

    @TableField("detail_json")
    private String detailJson;

    @TableField("apply_type")
    private String applyType;

    @TableField("fee_amount")
    private BigDecimal feeAmount;

    @TableField("payment_required")
    private Boolean paymentRequired;

    @TableField("payment_status")
    private String paymentStatus;

    @TableField("payment_order_id")
    private Long paymentOrderId;

    @TableField("payment_verified_at")
    private Date paymentVerifiedAt;

    @TableField("submitted_snapshot_json")
    private String submittedSnapshotJson;

    @TableField("last_payment_check_at")
    private Date lastPaymentCheckAt;

    @TableField("attachments_completed")
    private Boolean attachmentsCompleted;

    @TableField("certificates_completed")
    private Boolean certificatesCompleted;

    @TableField("company_name")
    private String companyName;

    @TableField("city_name")
    private String cityName;

    @TableField("service_types_json")
    private String serviceTypesJson;

    @TableField("intro")
    private String intro;

    @TableField("onboarding_status")
    private String onboardingStatus;

    @TableField("submitted_at")
    private Date submittedAt;

    @TableField("reviewed_at")
    private Date reviewedAt;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("changed_at")
    private Date changedAt;

    @TableField("change_reason")
    private String changeReason;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
