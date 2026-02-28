package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_dict")
public class SysDict {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("dict_type")
    private String dictType;

    @TableField("dict_key")
    private String dictKey;

    @TableField("dict_value")
    private String dictValue;

    @TableField("sort_order")
    private Integer sortOrder;

    private Boolean status;

    @TableField("created_at")
    private Date createdAt;
}
