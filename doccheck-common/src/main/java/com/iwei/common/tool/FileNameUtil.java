package com.iwei.common.tool;

import java.util.List;

/**
 * 文件名处理工具类
 * 用于处理文件上传时的文件名冲突问题
 *
 * @author:zhaokangwei
 */
public class FileNameUtil {

    /**
     * 生成唯一的文件名
     * 当文件名冲突时，自动在文件名后添加数字序号以区分
     *
     * @param currentName   当前文件名
     * @param existingNames 已存在的文件名列表（可能包含已重命名的文件）
     * @return 唯一的文件名
     */
    public static String generateUniqueFileName(String currentName, List<String> existingNames) {
        // 如果当前文件名不存在于列表中，直接返回
        if (!existingNames.contains(currentName)) {
            return currentName;
        }

        // 分离文件名和扩展名
        String baseName;
        String extension;

        int lastDotIndex = currentName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            // 没有扩展名的情况
            baseName = currentName;
            extension = "";
        } else {
            baseName = currentName.substring(0, lastDotIndex);
            extension = currentName.substring(lastDotIndex);
        }

        // 查找是否已经是重命名格式（如 "filename (1).txt"）
        int counter = 1;
        String newName;

        // 如果原始文件名已经是重命名格式，提取计数器并继续递增
        if (baseName.matches(".* \\(\\d+\\)$")) {
            int lastParenIndex = baseName.lastIndexOf(" (");
            if (lastParenIndex > 0) {
                try {
                    String counterStr = baseName.substring(lastParenIndex + 2, baseName.length() - 1);
                    int existingCounter = Integer.parseInt(counterStr);
                    counter = existingCounter + 1;
                    baseName = baseName.substring(0, lastParenIndex);
                } catch (NumberFormatException e) {
                    // 解析失败，使用默认处理方式
                }
            }
        }

        // 循环查找未使用的文件名
        do {
            newName = baseName + " (" + counter + ")" + extension;
            counter++;
        } while (existingNames.contains(newName));

        return newName;
    }

    /**
     * 处理文件名，使其满足Amazon S3的规范。
     *
     * @param originalFileName 原始文件名
     * @return 处理后的文件名
     */
    public static String processFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return originalFileName;
        }

        // 保留文件扩展名
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileExtension = originalFileName.substring(dotIndex);
            originalFileName = originalFileName.substring(0, dotIndex);
        }

        // 去除 Amazon S3不支持的字符
        String cleanedFileName = originalFileName.replaceAll("[\"*'<>?\\\\:|（）]", "_");

        // 重新拼接文件名和扩展名
        return cleanedFileName + fileExtension;
    }

}