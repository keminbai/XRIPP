package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("member_verifications")
public class MemberVerification {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("company_name")
    private String companyName;

    @TableField("contact_name")
    private String contactName;

    private String phone;
    private String industry;

    @TableField("verification_status")
    private String verificationStatus;

    @TableField("submitted_at")
    private Date submittedAt;

    @TableField("reviewed_at")
    private Date reviewedAt;

    @TableField("change_reason")
    private String changeReason;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
