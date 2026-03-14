package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_permission_profiles")
public class SysPermissionProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("code")
    private String code;

    @TableField("name")
    private String name;

    @TableField("base_role")
    private String baseRole;

    @TableField("status")
    private Boolean status;

    @TableField("is_system")
    private Boolean isSystem;

    @TableField("is_super_admin")
    private Boolean isSuperAdmin;

    @TableField("description")
    private String description;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
