package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("activity_registrations")
public class ActivityRegistration {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("activity_id")
    private Long activityId;

    @TableField("user_id")
    private Long userId;

    @TableField("company_name")
    private String companyName;

    @TableField("contact_name")
    private String contactName;

    private String phone;

    @TableField("registration_status")
    private String registrationStatus;

    private BigDecimal amount;

    @TableField("paid_at")
    private Date paidAt;

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
