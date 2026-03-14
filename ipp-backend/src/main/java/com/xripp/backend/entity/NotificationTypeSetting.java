package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("notification_type_settings")
public class NotificationTypeSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("notification_type")
    private String notificationType;

    @TableField("display_name")
    private String displayName;

    @TableField("sms_enabled")
    private Boolean smsEnabled;

    @TableField("in_app_enabled")
    private Boolean inAppEnabled;

    @TableField("email_enabled")
    private Boolean emailEnabled;

    @TableField("description")
    private String description;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("changed_at")
    private Date changedAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
