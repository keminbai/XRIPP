package com.xripp.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class MemberExportDTO {

    @ExcelProperty("会员ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("企业名称")
    @ColumnWidth(30)
    private String companyName;

    @ExcelProperty("联系人")
    @ColumnWidth(15)
    private String contactPerson;

    @ExcelProperty("联系电话")
    @ColumnWidth(18)
    private String contactPhone;

    @ExcelProperty("行业")
    @ColumnWidth(15)
    private String industry;

    @ExcelProperty("会员等级")
    @ColumnWidth(12)
    private String memberLevel;

    @ExcelProperty("邀请码")
    @ColumnWidth(15)
    private String invitationCode;

    @ExcelProperty("注册日期")
    @ColumnWidth(15)
    private String createdAt;
}
