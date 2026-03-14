package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("overseas_services")
public class OverseasService {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("service_type")
    private String serviceType;

    @TableField("project_code")
    private String projectCode;

    private String title;

    @TableField("countries_json")
    private String countriesJson;

    private String summary;

    private String content;

    @TableField("price_type")
    private String priceType;

    @TableField("price_amount")
    private BigDecimal priceAmount;

    @TableField("duration_desc")
    private String durationDesc;

    @TableField("contact_person")
    private String contactPerson;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("contact_email")
    private String contactEmail;

    @TableField("tags_json")
    private String tagsJson;

    private String status;

    @TableField("views_count")
    private Integer viewsCount;

    @TableField("inquiries_count")
    private Integer inquiriesCount;

    @TableField("published_at")
    private Date publishedAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
