package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("contents")
public class ContentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** activity | media | ad | training */
    @TableField("content_type")
    private String contentType;

    private String summary;

    private String body;

    /** draft | pending_review | approved | published | rejected | offline */
    @TableField("publish_status")
    private String publishStatus;

    @TableField("cover_image")
    private String coverImage;

    @TableField("city_name")
    private String cityName;

    @TableField("is_paid")
    private Boolean isPaid;

    private BigDecimal fee;

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
}
