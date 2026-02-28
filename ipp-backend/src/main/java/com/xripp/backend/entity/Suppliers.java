package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author XRIPP Team
 * @since 2026-02-09
 */
@Getter
@Setter
@TableName("suppliers")
public class Suppliers implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("supplier_no")
    private String supplierNo;

    @TableField("company_name")
    private String companyName;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("audit_status")
    private Byte auditStatus;

    @TableField("submit_time")
    private Date submitTime;

    @TableField("onboarding_status")
    private String onboardingStatus;

    @TableField("created_at")
    private Date createdAt;
}
