package com.iwei.common.tool;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tika 文档解析工具类
 */
@Component
public class TikaUtil {

    private static final Tika tika = new Tika();

    static {
        tika.setMaxStringLength(Integer.MAX_VALUE);
    }
    /**
     * 提取文件中的文本内容（支持所有 Tika 兼容格式）
     * @param file 待解析的文件
     * @return 提取的文本字符串
     */
    public static String extractText(File file) throws Exception {
        return tika.parseToString(file);
    }

    /**
     * 提取输入流中的文本内容（适合网络文件或上传的文件）
     * @param inputStream 文件输入流
     * @param fileName 文件名（用于辅助格式检测）
     * @return 提取的文本字符串
     */
    public static String extractText(InputStream inputStream, String fileName) throws Exception {
        // 传入文件名帮助 Tika 更准确地识别格式
        Metadata metadata = new Metadata();
        // metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
        return tika.parseToString(inputStream, metadata);
    }

    /**
     * 解析文件的元数据（如作者、创建时间、格式等）
     * @param filePath 文件路径
     * @return 元数据键值对
     */
    public static Metadata extractMetadata(String filePath) throws Exception {
        Path path = new File(filePath).toPath();
        Metadata metadata = new Metadata();
        // metadata.set(Metadata.RESOLUTION_NAME_KEY, path.getFileName().toString());

        // 使用 AutoDetectParser 解析元数据
        try (InputStream inputStream = Files.newInputStream(path)) {
            ContentHandler handler = new BodyContentHandler(); // 忽略文本内容，只提取元数据
            new AutoDetectParser().parse(inputStream, handler, metadata, new ParseContext());
        }
        return metadata;
    }

    /**
     * 检测文件的真实格式（不受扩展名欺骗）
     * @param inputStream 待检测文件输入流
     * @return 格式类型（如 "application/pdf"、"application/msword"）
     */
    public static String detectFileType(InputStream inputStream) throws Exception {
        return tika.detect(inputStream);
    }

    /**
     * 判断输入流是否是Excel文档
     * @param inputStream 待检测的文件输入流
     * @return 如果输入流是Excel文档，返回 true；否则返回 false
     */
    public static boolean isExcelFile(InputStream inputStream) throws Exception {
        String mimeType = tika.detect(inputStream);
        return isExcelMimeType(mimeType);
    }

    /**
     * 判断MIME类型是否是Excel文档的MIME类型
     * @param mimeType MIME类型
     * @return 如果MIME类型是Excel文档的MIME类型，返回 true；否则返回 false
     */
    private static boolean isExcelMimeType(String mimeType) {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mimeType) ||
                "application/vnd.ms-excel".equals(mimeType);
    }
}
