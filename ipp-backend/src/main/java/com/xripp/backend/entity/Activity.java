package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("activities")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("start_time")
    private Date startTime;

    @TableField("is_free")
    private Boolean isFree;

    private BigDecimal fee;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("created_at")
    private Date createdAt;
}
