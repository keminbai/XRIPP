package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("admin_config_entries")
public class AdminConfigEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("config_namespace")
    private String configNamespace;

    @TableField("config_key")
    private String configKey;

    @TableField("config_name")
    private String configName;

    private String description;

    @TableField("config_value_json")
    private String configValueJson;

    @TableField("sort_order")
    private Integer sortOrder;

    private Boolean enabled;

    @TableField("changed_by")
    private Long changedBy;

    @TableField("changed_at")
    private Date changedAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
