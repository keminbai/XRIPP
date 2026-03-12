package com.xripp.backend.util;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelExportUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public static <T> void write(HttpServletResponse response, String sheetName,
                                  Class<T> clazz, List<T> data) throws IOException {
        String fileName = sheetName + "_" + DATE_FMT.format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        EasyExcel.write(response.getOutputStream(), clazz)
                .sheet(sheetName)
                .doWrite(data);
    }

    public static String fmtDate(Date date) {
        if (date == null) return "";
        return DATE_FMT.format(date);
    }
}
