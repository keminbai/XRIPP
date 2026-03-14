package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("overseas_reports")
public class OverseasReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String country;

    @TableField("industry_code")
    private String industryCode;

    @TableField("report_type")
    private String reportType;

    @TableField("report_year")
    private String reportYear;

    @TableField("publish_date")
    private Date publishDate;

    @TableField("expire_date")
    private Date expireDate;

    private String summary;

    @TableField("cover_image_url")
    private String coverImageUrl;

    @TableField("cover_image_name")
    private String coverImageName;

    @TableField("cover_image_ext")
    private String coverImageExt;

    @TableField("cover_image_size")
    private Long coverImageSize;

    @TableField("report_file_url")
    private String reportFileUrl;

    @TableField("report_file_name")
    private String reportFileName;

    @TableField("report_file_ext")
    private String reportFileExt;

    @TableField("report_file_size")
    private Long reportFileSize;

    @TableField("keywords_json")
    private String keywordsJson;

    @TableField("source_name")
    private String sourceName;

    @TableField("fee_level")
    private String feeLevel;

    @TableField("access_level")
    private String accessLevel;

    @TableField("is_recommended")
    private Boolean isRecommended;

    @TableField("downloads_count")
    private Integer downloadsCount;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
