package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@TableName("supplier_onboarding_certificates")
public class SupplierOnboardingCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("onboarding_id")
    private Long onboardingId;

    @TableField("cert_type")
    private String certType;

    @TableField("cert_name")
    private String certName;

    @TableField("has_certificate")
    private Boolean hasCertificate;

    @TableField("cert_no")
    private String certNo;

    @TableField("valid_from")
    private LocalDate validFrom;

    @TableField("valid_to")
    private LocalDate validTo;

    @TableField("issuer_name")
    private String issuerName;

    private String remarks;

    @TableField("attachment_file_id")
    private Long attachmentFileId;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
