package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("customer_service_tickets")
public class CustomerServiceTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("ticket_no")
    private String ticketNo;

    @TableField("user_id")
    private Long userId;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("member_level_snapshot")
    private String memberLevelSnapshot;

    @TableField("ticket_type")
    private String ticketType;

    @TableField("priority")
    private String priority;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("status")
    private String status;

    @TableField("reply")
    private String reply;

    @TableField("source_channel")
    private String sourceChannel;

    @TableField("created_by_admin")
    private Boolean createdByAdmin;

    @TableField("last_replied_by")
    private Long lastRepliedBy;

    @TableField("closed_at")
    private Date closedAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
