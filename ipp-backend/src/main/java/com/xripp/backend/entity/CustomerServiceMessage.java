package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("customer_service_messages")
public class CustomerServiceMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("member_level_snapshot")
    private String memberLevelSnapshot;

    @TableField("name")
    private String name;

    @TableField("phone")
    private String phone;

    @TableField("source_channel")
    private String sourceChannel;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("handled_by")
    private Long handledBy;

    @TableField("handled_at")
    private Date handledAt;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
