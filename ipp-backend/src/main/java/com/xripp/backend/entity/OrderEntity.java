package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class OrderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("order_type")
    private String orderType;

    @TableField("order_status")
    private String orderStatus;

    private BigDecimal amount;

    @TableField("currency_code")
    private String currencyCode;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

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
