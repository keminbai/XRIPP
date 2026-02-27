package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
