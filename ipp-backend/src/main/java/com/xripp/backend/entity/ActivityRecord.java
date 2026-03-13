package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("activity_records")
public class ActivityRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("activity_id")
    private Long activityId;

    @TableField("actual_participants")
    private Integer actualParticipants;

    @TableField("summary")
    private String summary;

    @TableField("completion_status")
    private String completionStatus;

    @TableField("recorded_by")
    private Long recordedBy;

    @TableField("completed_at")
    private Date completedAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
