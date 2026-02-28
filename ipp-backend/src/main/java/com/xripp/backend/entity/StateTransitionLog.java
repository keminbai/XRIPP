package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("state_transition_logs")
public class StateTransitionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    private String action;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("change_reason")
    private String changeReason;

    @TableField("changed_at")
    private Date changedAt;
}
