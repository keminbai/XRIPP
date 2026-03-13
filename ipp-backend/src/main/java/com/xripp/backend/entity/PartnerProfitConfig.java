package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("partner_profit_configs")
public class PartnerProfitConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_id")
    private Long partnerId;

    private BigDecimal percentage;

    @TableField("business_lines_json")
    private String businessLinesJson;

    private Boolean enabled;

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
