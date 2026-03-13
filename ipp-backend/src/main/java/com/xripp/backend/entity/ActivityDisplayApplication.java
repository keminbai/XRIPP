package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("activity_display_applications")
public class ActivityDisplayApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("activity_id")
    private Long activityId;

    @TableField("display_area")
    private String displayArea;

    @TableField("display_start_at")
    private Date displayStartAt;

    @TableField("display_end_at")
    private Date displayEndAt;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("apply_status")
    private String applyStatus;

    @TableField("applied_by")
    private Long appliedBy;

    @TableField("reviewed_by")
    private Long reviewedBy;

    @TableField("reviewed_at")
    private Date reviewedAt;

    @TableField("change_reason")
    private String changeReason;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
