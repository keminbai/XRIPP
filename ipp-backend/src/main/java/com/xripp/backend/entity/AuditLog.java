package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("audit_logs")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("operator_id")
    private Long operatorId;

    private String action;

    @TableField("prev_status")
    private Byte prevStatus;

    @TableField("curr_status")
    private Byte currStatus;

    private String comment;

    @TableField("created_at")
    private Date createdAt;
}
