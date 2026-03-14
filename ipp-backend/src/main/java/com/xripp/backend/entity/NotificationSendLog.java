package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("notification_send_logs")
public class NotificationSendLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("notification_type")
    private String notificationType;

    @TableField("target_scope")
    private String targetScope;

    @TableField("notification_title")
    private String notificationTitle;

    @TableField("notification_content")
    private String notificationContent;

    @TableField("send_status")
    private String sendStatus;

    @TableField("requested_by")
    private Long requestedBy;

    @TableField("requested_at")
    private Date requestedAt;

    @TableField("processed_at")
    private Date processedAt;

    @TableField("result_message")
    private String resultMessage;
}
