package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@TableName("partners")
public class Partner {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_name")
    private String partnerName;

    @TableField("partner_no")
    private String partnerNo;

    private String province;

    @TableField("city_code")
    private String cityCode;

    @TableField("city_name")
    private String cityName;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("invitation_code")
    private String invitationCode;

    private Boolean status;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("join_date")
    private LocalDate joinDate;

    @TableField("expire_date")
    private LocalDate expireDate;

    @TableField("renewal_reminder_sent")
    private Boolean renewalReminderSent;
}
