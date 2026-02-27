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

    /** 活动类型标签（亨嘉之会 / 公益行 / 出海考察 / 行业沙龙 / 其他）*/
    @TableField("activity_type")
    private String activityType;

    /** 活动城市/地点展示名称 */
    @TableField("city_name")
    private String cityName;

    /** 封面图 URL */
    @TableField("image_url")
    private String imageUrl;

    /** 活动简介 */
    private String summary;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
