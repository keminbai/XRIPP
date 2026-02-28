package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("member_profile")
public class MemberProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("company_name")
    private String companyName;

    private String industry;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("vip_level")
    private String vipLevel;

    @TableField("vip_expire_time")
    private Date vipExpireTime;

    @TableField("invitation_code")
    private String invitationCode;

    @TableField("created_at")
    private Date createdAt;

    /** Phase 2 新增：统一会员等级 (normal/vip/svip) */
    @TableField("member_level")
    private String memberLevel;
}
