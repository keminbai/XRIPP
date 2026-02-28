package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("partner_benefit_pool")
public class PartnerBenefitPool {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("benefit_type")
    private String benefitType;

    @TableField("total_amount")
    private Integer totalAmount;

    @TableField("used_amount")
    private Integer usedAmount;

    /** SQL Server 计算列 (total_amount - used_amount PERSISTED)，只读 */
    @TableField(value = "balance", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Integer balance;

    private Integer version;

    @TableField("updated_at")
    private Date updatedAt;
}
