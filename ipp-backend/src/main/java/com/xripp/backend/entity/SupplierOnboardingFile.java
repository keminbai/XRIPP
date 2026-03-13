package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("supplier_onboarding_files")
public class SupplierOnboardingFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("onboarding_id")
    private Long onboardingId;

    @TableField("file_category")
    private String fileCategory;

    @TableField("file_name")
    private String fileName;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_ext")
    private String fileExt;

    @TableField("file_size")
    private Long fileSize;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("uploaded_by")
    private Long uploadedBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
