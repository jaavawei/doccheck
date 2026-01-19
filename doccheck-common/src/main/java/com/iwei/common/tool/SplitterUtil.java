package com.iwei.common.tool;

import com.iwei.common.entity.HeadingNode;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 文档分割工具：将文档按结构解析为树状
 * 仅能匹配数字开头的标题 即：1，1.1，1.1.1
 *
 * @author:zhaokangwei
 */
public class SplitterUtil {
    
    private String toc = ""; // 目录作为实例变量

    /*
     * 将文档解析为树状数据结构，返回文档根节点
     */
    public HeadingNode splitByHeading(String text) {
        text = text.replace("#", "");
        toc = "";

        String[] lines = text.split("\n");
        List<HeadingNode> rootNodes = new ArrayList<>();
        Stack<HeadingNode> stack = new Stack<>();

        // 用于存储第一个标题之前的内容
        StringBuilder preambleContent = new StringBuilder();
        boolean firstHeadingFound = false;

        for (String line : lines) {
            line = line.trim();
            // 空行，跳过
            if (line.isEmpty()) {
                continue;
            }

            // 检查是否为标题行（数字开头）
            int level = getHeadingLevel(line);
            // 目录行，处理为内容行
            if (line.contains("........")) {
                line = line.replaceAll(" ", "");
                toc = toc + line + "\n";
                // level = 0;
                continue;
            }
            if (level > 0) {

                firstHeadingFound = true;
                HeadingNode node = new HeadingNode(line);

                // 根据层级关系构建树结构
                while (!stack.isEmpty() && getHeadingLevel(stack.peek().getTitle()) >= level) {
                    stack.pop();
                }

                if (stack.isEmpty()) {
                    // 一级标题，添加到根节点
                    rootNodes.add(node);
                } else {
                    // 子标题，添加到父节点
                    stack.peek().getChildren().add(node);
                }

                stack.push(node);
            } else {
                // 普通内容行
                if (firstHeadingFound && !stack.isEmpty()) {
                    // 已经有标题了，内容属于当前标题
                    HeadingNode currentNode = stack.peek();
                    if (currentNode.getContent().isEmpty()) {
                        currentNode.setContent(line);
                    } else {
                        currentNode.setContent(currentNode.getContent() + "\n" + line);
                    }
                } else if (!firstHeadingFound) {
                    // 还没有标题，内容属于前言部分
                    if (preambleContent.length() == 0) {
                        preambleContent.append(line);
                    } else {
                        preambleContent.append("\n").append(line);
                    }
                }
            }
        }

        // 创建一个虚拟的根节点，包含所有一级标题
        HeadingNode rootNode = new HeadingNode("--");
        // 将前言内容设置为根节点的内容
        if (preambleContent.length() > 0) {
            rootNode.setContent(preambleContent.toString());
        }
        rootNode.getChildren().addAll(rootNodes);

        return rootNode;
    }

    /*
     * 获取标题级别（根据数字后的点数判断）
     */
    private int getHeadingLevel(String line) {

        if(line.length() > 20 || line.length() <= 2) {
            // 字数大于 20 是正文，字数小于 3 是页码
            // 目前最短的标题为"1 概述"或"1.概述"，长度为4
            return 0;
        }

        // 判断目录中是否包含该标题
        line = line.replaceAll(" ", "");
        if (!toc.contains(line)) {
            // 目录中没有该标题的文本，是正文
            return 0;
        }

        // 匹配数字开头的标题（如 1 标题, 1.1 标题, 1.1.1 标题等）
        Pattern pattern = Pattern.compile("^(\\d+(\\.\\d+)*)");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String number = matcher.group(1);
            // 计算点的个数加 1 作为级别
            int dots = number.length() - number.replace(".", "").length();
            if (dots > 0) {
                // 二级及以下标题
                return dots + 1;
            }

            int firstNumber = Integer.parseInt(number);
            if (firstNumber < 20) {
                // 只有数字小于20时才被判定为一级标题
                return 1;
            }

        }

        return 0; // 非标题行
    }

    /*
     * 根据标题名查找第一个匹配节点，并返回节点下内容
     */
    public String findHeadingByName(HeadingNode root, String targetTitle) {
        // 查找节点及其路径
        PathResult result = findHeadingWithPathHelper(root, targetTitle, new ArrayList<>());
        if (result != null) {
            return result.path + "\n" + result.node.toString();
        } else {
            return ""; // 查找不到时返回空字符串而不是抛出异常
        }
    }

    /*
     * 根据标题名查找所有匹配节点，并返回节点下内容
     */
    public List<String> findHeadingsByName(HeadingNode root, String targetTitle) {
        List<PathResult> results = new ArrayList<>();
        findHeadingsByNameHelper(root, targetTitle, results, new ArrayList<String>());
        List<String> ss = results.stream().map(r -> r.path + "\n" + r.node.toString()).collect(Collectors.toList());
        if (ss == null || ss.isEmpty()) {
            return new ArrayList<>(); // 查找不到时返回空列表而不是抛出异常
        }
        return ss;
    }

    /*
     * 根据标题名和等级查找第一个匹配节点，并返回节点下内容
     */
    public String findHeadingByNameAndLevel(HeadingNode root, String targetTitle, int targetLevel) {
        // 查找节点及其路径
        PathResult result = findHeadingByNameAndLevelWithPathHelper(root, targetTitle, targetLevel, new ArrayList<>());
        if (result != null) {
            return result.path + "\n" + result.node.toString();
        } else {
            return ""; // 查找不到时返回空字符串而不是抛出异常
        }
    }

    /*
     * 根据标题名和等级查找所有匹配的节点，并返回节点下内容
     */
    public List<String> findHeadingsByNameAndLevel(HeadingNode root, String targetTitle, int targetLevel) {
        List<PathResult> result = new ArrayList<>();
        findHeadingsByNameAndLevelWithPathHelper(root, targetTitle, targetLevel, result, new ArrayList<String>());
        List<String> ss = result.stream().map(r -> r.path + "\n" + r.node.toString()).collect(Collectors.toList());
        if (ss == null || ss.isEmpty()) {
            return new ArrayList<>(); // 查找不到时返回空列表而不是抛出异常
        }
        return ss;
    }

    /*
     * 用于存储节点和路径信息的辅助类
     */
    private static class PathResult {
        HeadingNode node;
        String path;
        
        PathResult(HeadingNode node, String path) {
            this.node = node;
            this.path = path;
        }
    }

    /*
     * 辅助函数：递归查找匹配的标题及其路径
     */
    private PathResult findHeadingWithPathHelper(HeadingNode node, String targetTitle, List<String> path) {
        // 添加当前节点到路径
        List<String> currentPath = new ArrayList<>(path);
        currentPath.add(node.getTitle());

        // 提取当前节点标题中不带数字和空格的部分
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());
        if (cleanTitle.contains(targetTitle)) {
            // 构建路径字符串
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                if (i > 0) {
                    pathStr.append("\n");
                }
                pathStr.append(currentPath.get(i));
            }
            return new PathResult(node, pathStr.toString());
        }

        // 递归查找子节点
        for (Object child : node.getChildren()) {
            if (child instanceof HeadingNode) {
                PathResult result = findHeadingWithPathHelper((HeadingNode) child, targetTitle, currentPath);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /*
     * 辅助函数：递归查找匹配的标题
     */
    private HeadingNode findHeadingByNameHelper(HeadingNode node, String targetTitle) {
        // 提取当前节点标题中不带数字和空格的部分
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());
        if (cleanTitle.contains(targetTitle)) {
            return node;
        }

        // 递归查找子节点
        for (Object child : node.getChildren()) {
            if (child instanceof HeadingNode) {
                HeadingNode found = findHeadingByNameHelper((HeadingNode) child, targetTitle);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /*
     * 辅助函数：递归查找所有匹配的标题及其路径
     */
    private void findHeadingsByNameHelper(HeadingNode node, String targetTitle, List<PathResult> result, List<String> path) {
        // 添加当前节点到路径
        List<String> currentPath = new ArrayList<>(path);
        currentPath.add(node.getTitle());

        // 提示标志，表示是否在此节点找到了匹配
        boolean foundInCurrentNode = false;

        // 提取当前节点标题中不带数字和空格的部分
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());
        if (cleanTitle.contains(targetTitle)) {
            // 构建路径字符串
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                if (i > 0) {
                    pathStr.append("\n");
                }
                pathStr.append(currentPath.get(i));
            }
            result.add(new PathResult(node, pathStr.toString()));
            foundInCurrentNode = true;
        }

        // 递归查找子节点（只有在当前节点未匹配时才继续查找子节点）
        if (!foundInCurrentNode) {
            for (Object child : node.getChildren()) {
                if (child instanceof HeadingNode) {
                    findHeadingsByNameHelper((HeadingNode) child, targetTitle, result, currentPath);
                }
            }
        }
    }

    /*
     * 辅助函数：递归查找指定等级和标题名的节点
     */
    private HeadingNode findHeadingByNameAndLevelHelper(HeadingNode node, String targetTitle, int targetLevel) {
        // 检查当前节点是否符合要求
        int currentLevel = getHeadingLevel(node.getTitle());
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());

        if (currentLevel == targetLevel && cleanTitle.contains(targetTitle)) {
            return node;
        }

        // 递归查找子节点
        for (Object child : node.getChildren()) {
            if (child instanceof HeadingNode) {
                HeadingNode found = findHeadingByNameAndLevelHelper((HeadingNode) child, targetTitle, targetLevel);
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /*
     * 辅助函数：递归查找指定等级和标题名的节点及其路径
     */
    private PathResult findHeadingByNameAndLevelWithPathHelper(HeadingNode node, String targetTitle, int targetLevel, List<String> path) {
        // 添加当前节点到路径
        List<String> currentPath = new ArrayList<>(path);
        currentPath.add(node.getTitle());

        // 检查当前节点是否符合要求
        int currentLevel = getHeadingLevel(node.getTitle());
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());

        if (currentLevel == targetLevel && cleanTitle.contains(targetTitle)) {
            // 构建路径字符串
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                if (i > 0) {
                    pathStr.append("\n");
                }
                pathStr.append(currentPath.get(i));
            }
            return new PathResult(node, pathStr.toString());
        }

        // 递归查找子节点
        for (Object child : node.getChildren()) {
            if (child instanceof HeadingNode) {
                PathResult result = findHeadingByNameAndLevelWithPathHelper((HeadingNode) child, targetTitle, targetLevel, currentPath);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /*
     * 辅助函数：递归查找指定等级和标题名的节点及其路径
     */
    private void findHeadingsByNameAndLevelWithPathHelper(HeadingNode node, String targetTitle, int targetLevel, List<PathResult> result, List<String> path) {
        // 添加当前节点到路径
        List<String> currentPath = new ArrayList<>(path);
        currentPath.add(node.getTitle());

        // 提示标志，表示是否在此节点找到了匹配
        boolean foundInCurrentNode = false;

        // 检查当前节点是否符合要求
        int currentLevel = getHeadingLevel(node.getTitle());
        String cleanTitle = extractTitleWithoutNumber(node.getTitle());

        if (currentLevel == targetLevel && cleanTitle.contains(targetTitle)) {
            // 构建路径字符串
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < currentPath.size(); i++) {
                if (i > 0) {
                    pathStr.append("\n");
                }
                pathStr.append(currentPath.get(i));
            }
            result.add(new PathResult(node, pathStr.toString()));
            foundInCurrentNode = true;
        }

        // 递归查找子节点（只有在当前节点未匹配时才继续查找子节点）
        if (!foundInCurrentNode) {
            for (Object child : node.getChildren()) {
                if (child instanceof HeadingNode) {
                    findHeadingsByNameAndLevelWithPathHelper((HeadingNode) child, targetTitle, targetLevel, result, currentPath);
                }
            }
        }
    }

    /*
     * 提取标题中不带数字和空格的部分
     */
    private static String extractTitleWithoutNumber(String fullTitle) {

        Pattern pattern = Pattern.compile("^((?:\\d+(?:\\.\\d+)*)|[一二三四五六七八九十]+)[\\.、\\s]*(.*)$");
        Matcher matcher = pattern.matcher(fullTitle);

        if (matcher.find()) {
            String cleanTitle = matcher.group(2).trim();
            // 若提取后为空（如标题仅为“一”“1”），返回原标题
            return cleanTitle.isEmpty() ? fullTitle : cleanTitle;
        }

        return fullTitle; // 不匹配则返回原标题
    }

}