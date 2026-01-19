package com.iwei.common.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Json 工具类
 *
 * @author:zhaokangwei
 */
public class JsonUtil {

    /**
     * 导出 json
     */
    public static void exportJson(HttpServletResponse response, String fileName, List<String> list) {
        OutputStream outputStream = null;
        try {
            // 将 List<Map<String, String>> 转化成 JSON 数组字符串
            JSONArray jsonArray = new JSONArray(list);
            String jsonStr = JSON.toJSONString(jsonArray);

            // 设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".json");

            // 获取输出流
            outputStream = response.getOutputStream();

            // 写入 JSON 数组字符串
            outputStream.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 处理 json：提取合规 json 字符串，并转义特殊字符
     */
    public static String processJson(String str) {
        // 查找第一个 { 或 [
        int start = -1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{' || c == '[') {
                start = i;
                break;
            }
        }

        // 查找最后一个 } 或 ]
        int end = -1;
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (c == '}' || c == ']') {
                end = i;
                break;
            }
        }

        // 如果没有找到合规的 JSON 字符串，返回空字符串
        if (start == -1 || end == -1 || start >= end) {
            return "";
        }

        // 提取中间的内容
        String result = str.substring(start, end + 1);
        result = fixJsonString(result);

        return result;
    }

    /**
     * 修复非标准JSON字符串中的双引号问题
     *
     * @param jsonString 需要修复的非标准JSON字符串
     * @return 修复后的标准JSON字符串
     */
    public static String fixJsonString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }

        // 去除首尾空格
        jsonString = jsonString.trim();

        // 使用正则表达式方法修复（更简单有效）
        return fixJsonWithRegex(jsonString);
    }

    /**
     * 使用正则表达式修复JSON字符串
     *
     * @param jsonString 需要修复的JSON字符串
     * @return 修复后的JSON字符串
     */
    public static String fixJsonWithRegex(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return jsonString;
        }

        // 手动实现修复逻辑
        StringBuilder result = new StringBuilder();
        boolean inString = false;  // 是否在字符串内部
        boolean escaped = false;   // 是否刚遇到转义字符\

        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);

            if (escaped) {
                // 如果刚遇到转义字符，直接添加当前字符
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                // 遇到转义字符
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                // 遇到双引号
                if (!inString) {
                    // 字符串开始
                    inString = true;
                    result.append(c);
                } else {
                    // 字符串结束，但需要判断是否真的是结束
                    // 检查后面是否是:（表示这是键）或者, } ]（表示这是值的结束）
                    boolean isEnd = false;
                    for (int j = i + 1; j < jsonString.length(); j++) {
                        char nextChar = jsonString.charAt(j);
                        if (nextChar == ' ' || nextChar == '\t' || nextChar == '\n' || nextChar == '\r') {
                            continue; // 跳过空白字符
                        }
                        if (nextChar == ':' || nextChar == ',' || nextChar == '}' || nextChar == ']') {
                            isEnd = true;
                        }
                        break;
                    }

                    if (isEnd) {
                        // 真正的字符串结束
                        inString = false;
                        result.append(c);
                    } else {
                        // 不是真正的结束，需要转义
                        result.append("\\\"");
                    }
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
