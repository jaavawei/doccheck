package com.iwei.common.tool;

import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * JSON数据导出基础类
 *
 * @author:zhaokangwei
 */
@Slf4j
public abstract class BaseJsonExport<T> {

    /**
     * 导出Map列表到JSON文件
     *
     * @param fileName 文件名
     * @param dataList Map数据列表
     */
    protected void exportJsonFromMap(String fileName, List<Map<String, Object>> dataList) {
        HttpServletResponse response = SpringContextUtil.getHttpServletResponse();
        doExport(response, fileName, dataList);
    }

    /**
     * 导出JSON字符串到JSON文件
     *
     * @param fileName 文件名
     * @param jsonStringList JSON字符串列表
     */
    protected void exportJson(String fileName, List<String> jsonStringList) {
        HttpServletResponse response = SpringContextUtil.getHttpServletResponse();
        doExport(response, fileName, jsonStringList);
    }

    /**
     * 导出Map数据到JSON文件
     */
    private void doExport(HttpServletResponse response, String fileName, List<?> dataList) {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment;filename="
                    + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + ".json");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

            JSONArray jsonArray = new JSONArray();
            if (!dataList.isEmpty()) {
                if (dataList.get(0) instanceof Map) {
                    // 处理Map类型数据
                    for (Object item : dataList) {
                        JSONObject jsonObject = new JSONObject((Map<String, Object>) item);
                        jsonArray.add(jsonObject);
                    }
                } else if (dataList.get(0) instanceof String) {
                    // 处理JSON字符串类型数据
                    for (Object item : dataList) {
                        JSONObject jsonObject = JSON.parseObject((String) item);
                        jsonArray.add(jsonObject);
                    }
                } else {
                    // 如果是其他类型，尝试直接转换
                    for (Object item : dataList) {
                        jsonArray.add(item);
                    }
                }
            }

            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(JSON.toJSONString(jsonArray, true)); // 格式化输出
            writer.flush();
        } catch (Exception e) {
            log.error("BaseJsonExport.exportJson.error:{}", e.getMessage(), e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    log.error("BaseJsonExport.exportJson.close.error:{}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 分批导出大量数据到JSON文件
     *
     * @param fileName 文件名
     * @param queryCondition 查询条件
     */
    protected void exportJsonBatch(String fileName, Map<String, Object> queryCondition) {
        HttpServletResponse response = SpringContextUtil.getHttpServletResponse();
        // 根据条件查询总记录数
        Long totalCount = dataTotalCount(queryCondition);
        // 每次导出的数据量
        Long exportDataRows = eachTimesExportTotalCount();
        
        try {
            OutputStream outputStream = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment;filename="
                    + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + ".json");
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write("[\n");
            
            // 计算分页信息
            long totalPage = totalCount % exportDataRows == 0 ? (totalCount / exportDataRows) : (totalCount / exportDataRows + 1);
            boolean first = true;
            
            for (long page = 1; page <= totalPage; page++) {
                List<T> dataList = queryDataList(queryCondition, page, exportDataRows);
                
                for (T data : dataList) {
                    if (!first) {
                        writer.write(",\n");
                    }
                    writer.write(JSON.toJSONString(data, true).replaceAll("(?m)^", "  "));
                    first = false;
                }
            }
            
            writer.write("\n]");
            writer.flush();
            writer.close();
            
        } catch (Exception e) {
            log.error("BaseJsonExport.exportJsonBatch.error:{}", e.getMessage(), e);
        }
    }

    /**
     * 计算导出数据的总数
     */
    protected abstract Long dataTotalCount(Map<String, Object> conditions);

    /**
     * 每次导出的数据总量
     */
    protected abstract Long eachTimesExportTotalCount();

    /**
     * 查询分页数据
     */
    protected abstract List<T> queryDataList(Map<String, Object> queryCondition, Long pageNo, Long pageSize);
}