package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_login_modes")
public class SysLoginMode {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("mode_code")
    private String modeCode;

    @TableField("name")
    private String name;

    @TableField("base_role")
    private String baseRole;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("default_profile_id")
    private Long defaultProfileId;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
