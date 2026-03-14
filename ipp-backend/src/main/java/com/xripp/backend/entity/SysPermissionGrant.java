package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_permission_grants")
public class SysPermissionGrant {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("profile_id")
    private Long profileId;

    @TableField("module_code")
    private String moduleCode;

    @TableField("module_name")
    private String moduleName;

    @TableField("can_view")
    private Boolean canView;

    @TableField("can_create")
    private Boolean canCreate;

    @TableField("can_edit")
    private Boolean canEdit;

    @TableField("can_delete")
    private Boolean canDelete;

    @TableField("can_review")
    private Boolean canReview;

    @TableField("can_export")
    private Boolean canExport;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
