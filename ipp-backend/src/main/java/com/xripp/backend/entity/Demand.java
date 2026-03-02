package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("demands")
public class Demand {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("user_id")
    private Long userId;

    @TableField("org_type")
    private String orgType;

    @TableField("org_name")
    private String orgName;

    @TableField("publish_date")
    private Date publishDate;

    private Date deadline;

    private String category;

    private String industry;

    private String summary;

    @TableField("enable_sms")
    private Boolean enableSms;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("created_at")
    private Date createdAt;
}
