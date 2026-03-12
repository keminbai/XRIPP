package com.xripp.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class SupplierExportDTO {

    @ExcelProperty("申请ID")
    @ColumnWidth(10)
    private Long id;

    @ExcelProperty("企业名称")
    @ColumnWidth(30)
    private String companyName;

    @ExcelProperty("所在城市")
    @ColumnWidth(15)
    private String cityName;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String onboardingStatus;

    @ExcelProperty("提交日期")
    @ColumnWidth(15)
    private String submittedAt;

    @ExcelProperty("审核日期")
    @ColumnWidth(15)
    private String reviewedAt;
}
