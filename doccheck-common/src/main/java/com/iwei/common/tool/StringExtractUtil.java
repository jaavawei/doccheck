package com.iwei.common.tool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串提取工具
 *
 * @author: zhaokangwei
 */
@Slf4j
public class StringExtractUtil {

    /**
     * 使用字符串定位和截取提取多个部分的内容
     *
     * @param source       源字符串
     * @param startKeyword 起始关键词
     * @param endKeyword   结束关键词
     * @return 包含多个部分的列表
     */
    public static List<String> extractAllBetween(String source, String startKeyword, String endKeyword) {


        List<String> results = new ArrayList<>();
        int start = 0;

        while (start < source.length()) {
            int startIdx = source.indexOf(startKeyword, start);
            if (startIdx == -1) {
                break;
            }

            startIdx += startKeyword.length();
            int endIdx = source.indexOf(endKeyword, startIdx);
            if (endIdx == -1) {
                break;
            }

            String match = source.substring(startIdx, endIdx).trim();
            results.add(match);

            start = endIdx + endKeyword.length();
        }

        return results;
    }

    /**
     * 提取最后一个形如 ".3集成需求" 之前的所有内容，其中 "3" 也可能是其他数字
     *
     * @param source 源字符串
     * @return 提取的内容
     */
    public static String extractBeforeLastIntegrationRequirement(String source) {

        // 去除源字符串中的所有空格
        source = source.replaceAll("\\s+", "");
        String start = "\\.[0-9]+可行性分析";
        String end = "\\.[0-9]+集成需求";
        Pattern pattern = Pattern.compile(end);
        Matcher matcher = pattern.matcher(source);

        int lastEndIdx = -1;
        while (matcher.find()) {
            lastEndIdx = matcher.end();
        }

        if (lastEndIdx == -1) {
            // 没有找到匹配的模式，返回空字符串
            return "";
        }

        // 提取最后一个匹配模式之前的内容
        return source.substring(0, lastEndIdx).trim();
    }

    public static List<String> extractBetweenStartAndEnd(String source, String start) {

        log.info("length: " + source.length());
        // 去除源字符串中的所有空格
        source = source.replaceAll(" ", "");

        String startPatternStr = "\\d+" + start;
        String endPatternStr = "\\d+建设内容";
        Pattern startPattern = Pattern.compile(startPatternStr);
        Pattern endPattern = Pattern.compile(endPatternStr);

        List<String> results = new ArrayList<>();
        Matcher startMatcher = startPattern.matcher(source);
        Matcher endMatcher = endPattern.matcher(source);

        int startIdx = 0;
        while (startMatcher.find(startIdx)) {
            int startStartIdx = startMatcher.start();
            int startEndIdx = startMatcher.end();
            if (endMatcher.find(startEndIdx)) {
                int endStartIdx = endMatcher.start();
                String match = source.substring(startStartIdx - 30, endStartIdx).trim();
                results.add(match);
                startIdx = endMatcher.end();
            } else {
                break;
            }
        }

        return results;
    }

    /*
     * 提取建设内容
     */
    public static List<String> extractJianSheNeiRong(String source) {
        // 去除源字符串中的所有空格
        source = source.replaceAll("\\s+", "");

        String startPatternStr = "\\d+\\.\\d+\\.\\d+建设内容";
        String endPatternStr = "\\d+\\.\\d+\\.\\d+";
        Pattern startPattern = Pattern.compile(startPatternStr);
        Pattern endPattern = Pattern.compile(endPatternStr);

        List<String> results = new ArrayList<>();
        Matcher startMatcher = startPattern.matcher(source);
        Matcher endMatcher = endPattern.matcher(source);

        int startIdx = 0;
        while (startMatcher.find(startIdx)) {
            int startStartIdx = startMatcher.start();
            int startEndIdx = startMatcher.end();
            if (endMatcher.find(startEndIdx)) {
                int endStartIdx = endMatcher.start();
                String match = source.substring(startStartIdx, endStartIdx).trim();
                results.add(match);
                startIdx = endMatcher.end();
            } else {
                break;
            }
        }

        return results;
    }



}