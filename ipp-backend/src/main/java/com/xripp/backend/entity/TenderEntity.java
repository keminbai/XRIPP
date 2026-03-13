package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("tenders")
public class TenderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("tender_no")
    private String tenderNo;

    private String title;

    @TableField("organization_name")
    private String organizationName;

    private String country;

    @TableField("deadline_at")
    private Date deadlineAt;

    @TableField("tender_status")
    private String tenderStatus;

    @TableField("category")
    private String category;

    @TableField("summary")
    private String summary;

    @TableField("price")
    private BigDecimal price;

    @TableField("sales")
    private Integer sales;

    @TableField("is_top")
    private Boolean isTop;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("changed_at")
    private Date changedAt;

    @TableField("change_reason")
    private String changeReason;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("published_at")
    private Date publishedAt;
}
