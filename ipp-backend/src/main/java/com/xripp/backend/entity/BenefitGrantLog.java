package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("benefit_grant_logs")
public class BenefitGrantLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("member_id")
    private Long memberId;

    @TableField("benefit_type")
    private String benefitType;

    private Integer amount;

    @TableField("operator_id")
    private Long operatorId;

    private String remark;

    @TableField("created_at")
    private Date createdAt;
}
