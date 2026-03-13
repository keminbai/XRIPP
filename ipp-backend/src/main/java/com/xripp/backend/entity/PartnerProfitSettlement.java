package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@TableName("partner_profit_settlements")
public class PartnerProfitSettlement {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("settlement_month")
    private LocalDate settlementMonth;

    @TableField("revenue_amount")
    private BigDecimal revenueAmount;

    @TableField("profit_amount")
    private BigDecimal profitAmount;

    @TableField("order_count")
    private Integer orderCount;

    @TableField("settlement_status")
    private String settlementStatus;

    @TableField("settled_at")
    private Date settledAt;

    @TableField("settled_by")
    private Long settledBy;

    private String note;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
