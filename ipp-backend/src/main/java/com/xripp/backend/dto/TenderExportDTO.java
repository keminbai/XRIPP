package com.xripp.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class TenderExportDTO {

    @ExcelProperty("标书编号")
    @ColumnWidth(18)
    private String tenderNo;

    @ExcelProperty("标题")
    @ColumnWidth(40)
    private String title;

    @ExcelProperty("发布机构")
    @ColumnWidth(25)
    private String organizationName;

    @ExcelProperty("国家")
    @ColumnWidth(15)
    private String country;

    @ExcelProperty("类目")
    @ColumnWidth(15)
    private String category;

    @ExcelProperty("价格")
    @ColumnWidth(12)
    private String price;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String tenderStatus;

    @ExcelProperty("截止日期")
    @ColumnWidth(20)
    private String deadlineAt;

    @ExcelProperty("发布日期")
    @ColumnWidth(15)
    private String createdAt;
}
