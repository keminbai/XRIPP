package com.xripp.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class OrderExportDTO {

    @ExcelProperty("订单编号")
    @ColumnWidth(22)
    private String orderNo;

    @ExcelProperty("订单类型")
    @ColumnWidth(15)
    private String orderType;

    @ExcelProperty("企业名称")
    @ColumnWidth(30)
    private String companyName;

    @ExcelProperty("联系人")
    @ColumnWidth(15)
    private String contactPerson;

    @ExcelProperty("联系电话")
    @ColumnWidth(18)
    private String contactPhone;

    @ExcelProperty("金额")
    @ColumnWidth(12)
    private String amount;

    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String orderStatus;

    @ExcelProperty("创建日期")
    @ColumnWidth(15)
    private String createdAt;
}
