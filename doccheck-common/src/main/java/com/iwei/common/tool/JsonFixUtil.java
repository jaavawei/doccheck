package com.iwei.common.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON修复工具类
 * 用于处理非标准JSON字符串中双引号未正确转义的问题
 */
public class JsonFixUtil {
    
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
     * 解析非标准JSON字符串
     * 
     * @param jsonString 需要解析的非标准JSON字符串
     * @return 解析后的标准JSON字符串
     * @throws RuntimeException 如果无法解析JSON字符串
     */
    public static String parseNonStandardJson(String jsonString) {
        try {
            // 首先尝试直接解析（这里简化处理，实际项目中可以使用Jackson或Gson等库进行验证）
            // 如果是实际项目，建议使用JSON库进行验证
            return jsonString;
        } catch (Exception e) {
            // 如果直接解析失败，尝试修复后再解析
            try {
                String fixedJson = fixJsonString(jsonString);
                System.out.println("修复后的JSON字符串: " + fixedJson);
                // 这里可以再次尝试用JSON库解析fixedJson
                return fixedJson;
            } catch (Exception fixError) {
                throw new RuntimeException("无法解析JSON字符串: " + jsonString + " 错误信息: " + fixError.getMessage(), fixError);
            }
        }
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
    
    // 测试方法
    public static void main(String[] args) {
        // 测试用例
        String nonStandardJson = "{\"des\"asdxa\": \"i am not a \"cat\" !!\"}";
        System.out.println("原始JSON: " + nonStandardJson);
        
        String fixedJson = fixJsonString(nonStandardJson);
        System.out.println("修复后JSON: " + fixedJson);
        
        // 使用正则表达式方法
        String fixedJsonRegex = fixJsonWithRegex(nonStandardJson);
        System.out.println("正则修复后JSON: " + fixedJsonRegex);
    }
}