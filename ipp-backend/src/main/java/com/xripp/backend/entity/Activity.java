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

    @TableField("activity_no")
    private String activityNo;

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

    /** 宣传视频 URL */
    @TableField("video_url")
    private String videoUrl;

    /** 活动简介 */
    private String summary;

    /** 前台主模块 */
    @TableField("front_module")
    private String frontModule;

    /** 前台详细位置 */
    @TableField("front_position")
    private String frontPosition;

    /** 投放城市 JSON 数组 */
    @TableField("cities_json")
    private String citiesJson;

    /** 活动议程 */
    @TableField("agenda")
    private String agenda;

    /** 名额限制，0 表示不限 */
    @TableField("max_limit")
    private Integer maxLimit;

    /** 当前报名人数缓存 */
    @TableField("current_participants")
    private Integer currentParticipants;

    /** 收费项目 ID */
    @TableField("fee_item_id")
    private String feeItemId;

    /** 收费项目名称 */
    @TableField("fee_item_name")
    private String feeItemName;

    /** 收费模式：free/paid/member-discount/member-free */
    @TableField("fee_type")
    private String feeType;

    /** 会员价 */
    @TableField("member_price")
    private BigDecimal memberPrice;

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
