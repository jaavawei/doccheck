package com.iwei.common.tool;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.metadata.WriteWorkbook;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Excel 工具类
 *
 * @auther: zhaokangwei
 */
@Slf4j
@Component
public class ExcelUtil {

    /**
     * 每个 sheet 存放的数据行数
     */
    private Integer sheetDataRows = 10000;

    /*
     * 导出 Excel，单个提取规则，单个sheet
     */
    public static void exportExcel(HttpServletResponse response, String fileName, List<List<String>> excelHead, List<List<String>> dataList) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            WriteWorkbook writeWorkbook = new WriteWorkbook();
            writeWorkbook.setOutputStream(outputStream);
            writeWorkbook.setExcelType(ExcelTypeEnum.XLSX);
            ExcelWriter writer = new ExcelWriter(writeWorkbook);
            WriteTable table = new WriteTable();
            table.setHead(excelHead); // 设置表头
            WriteSheet sheet = new WriteSheet();
            sheet.setSheetNo(1);
            sheet.setSheetName(fileName);
            writer.write(dataList, sheet, table);
            response.setHeader("Content-Disposition", "attachment;filename="
                    + new String((fileName).getBytes("gb2312"),
                    "ISO-8859-1") + ".xlsx");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            writer.finish();
            outputStream.flush();
        } catch (Exception e) {
            log.error("ExcelUtil.exportExcel.error:{}", e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("ExcelUtil.exportExcel.close.error:{}", e.getMessage(), e);
                }
            }
        }
    }

    /*
     * 导出 excel，多个提取规则对应多个 sheet
     */
    public static void exportExcel(HttpServletResponse response, String fileName, Map<String, Map<String, List<List<String>>>> sheetData) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            WriteWorkbook writeWorkbook = new WriteWorkbook();
            writeWorkbook.setOutputStream(outputStream);
            writeWorkbook.setExcelType(ExcelTypeEnum.XLSX);
            ExcelWriter writer = new ExcelWriter(writeWorkbook);

            int sheetNo = 1;
            // sheetData 中每个 map 为一个 sheet 的数据，map 中存放表头和数据, sheet 名为提取规则名
            for (Map.Entry<String, Map<String, List<List<String>>>> entry : sheetData.entrySet()) {
                String sheetName = entry.getKey();
                Map<String, List<List<String>>> sheetContent = entry.getValue();

                List<List<String>> excelHead = sheetContent.get("head");
                List<List<String>> dataList = sheetContent.get("data");

                WriteTable table = new WriteTable();
                table.setHead(excelHead); // 设置表头

                WriteSheet sheet = new WriteSheet();
                sheet.setSheetNo(sheetNo);
                sheet.setSheetName(sheetName);
                writer.write(dataList, sheet, table);
                sheetNo++;

            }
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            writer.finish();
            outputStream.flush();
        } catch (Exception e) {
            log.error("ExcelUtil.exportExcelWithMultipleSheets.error:{}", e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("ExcelUtil.exportExcelWithMultipleSheets.close.error:{}", e.getMessage(), e);
                }
            }
        }
    }
}

