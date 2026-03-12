package com.xripp.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("overseas_points")
public class OverseasPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("partner_id")
    private String partnerId;

    private String name;

    private String continent;

    private String country;

    private String city;

    private BigDecimal lat;

    private BigDecimal lng;

    private String manager;

    private String phone;

    private String email;

    private String address;

    @TableField("services_json")
    private String servicesJson;

    @TableField("service_type")
    private String serviceType;

    private String description;

    @TableField("cover_image")
    private String coverImage;

    private BigDecimal rating;

    @TableField("response_time")
    private String responseTime;

    @TableField("success_cases")
    private Integer successCases;

    /** active | inactive */
    private String status;

    @TableField("established_at")
    private Date establishedAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
