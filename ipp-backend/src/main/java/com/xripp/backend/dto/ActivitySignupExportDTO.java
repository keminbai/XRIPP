package com.xripp.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ActivitySignupExportDTO {

    @ExcelProperty("报名ID")
    private Long id;

    @ExcelProperty("企业名称")
    private String companyName;

    @ExcelProperty("联系人")
    private String contactName;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("报名状态")
    private String registrationStatus;

    @ExcelProperty("支付状态")
    private String paymentStatus;

    @ExcelProperty("报名时间")
    private String createdAt;

    @ExcelProperty("支付时间")
    private String paidAt;
}
