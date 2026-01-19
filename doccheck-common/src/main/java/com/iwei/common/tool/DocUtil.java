package com.iwei.common.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class DocUtil {

    @Value("${libreoffice.path:}")
    private String libreOfficePath;


    /**
     * 获取文件的扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（含小数点，如“.docx”），无扩展名则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    /**
     * 核心接口：将文档输入流转换为PDF输出流
     *
     * @param docInputStream 文档输入流
     * @param pdfOutputStream PDF输出流
     * @param suffix 文档扩展名（含小数点，如“.docx”）
     * @throws Exception 转换过程中发生异常
     */
    public void convertToPDF(InputStream docInputStream, OutputStream pdfOutputStream, String suffix) throws Exception {
        // 直接使用LibreOffice转换文档
        convertWithLibreOffice(docInputStream, pdfOutputStream, suffix);
    }

    /**
     * 使用LibreOffice命令行工具转换文档
     * 注：LibreOffice不直接支持流操作，通过临时文件中转实现
     *
     * @param docInputStream 文档输入流
     * @param pdfOutputStream PDF输出流
     * @param suffix 文档扩展名
     * @throws Exception 转换异常
     */
    private void convertWithLibreOffice(InputStream docInputStream, OutputStream pdfOutputStream, String suffix) throws Exception {
        // 创建临时文件（源文件和目标PDF文件）
        File sourceFile = File.createTempFile("temp_doc_", suffix);
        File targetFile = new File(sourceFile.getAbsolutePath().replace(suffix, ".pdf"));

        try {
            // 1. 将输入流写入临时源文件
            try (FileOutputStream tempDocOut = new FileOutputStream(sourceFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = docInputStream.read(buffer)) != -1) {
                    tempDocOut.write(buffer, 0, bytesRead);
                }
            }

            // 2. 构建LibreOffice转换命令
            List<String> command = new ArrayList<>();
            command.add(libreOfficePath);
            command.add("--headless");
            command.add("--convert-to");
            command.add("pdf");
            command.add("--outdir");
            command.add(sourceFile.getParent());
            command.add(sourceFile.getAbsolutePath());

            // 3. 执行转换命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // 4. 转换成功则将PDF写入输出流
            if (exitCode == 0 && targetFile.exists()) {
                try (FileInputStream tempDocxIn = new FileInputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = tempDocxIn.read(buffer)) != -1) {
                        pdfOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                throw new Exception("LibreOffice转换失败，退出码: " + exitCode);
            }
        } finally {
            // 清理临时文件
            if (sourceFile.exists()) {
                sourceFile.delete();
            }
            if (targetFile.exists()) {
                targetFile.delete();
            }
        }
    }


    /**
     * 根据纯文本生成 txt 文件
     * 
     * @param text 纯文本内容
     * @return 生成的 txt 文件
     * @throws Exception 文件操作异常
     */
    public File createTxtFile(String text) throws Exception {
        File txtFile = File.createTempFile("generated_text_", ".txt");
        
        try (FileOutputStream fileOutputStream = new FileOutputStream(txtFile)) {
            if (text != null) {
                fileOutputStream.write(text.getBytes("UTF-8"));
            }
            fileOutputStream.flush();
        }
        
        return txtFile;
    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件对象
     */
    public void delete(File file) {
        if (file != null) {
            // 1. 先判断文件是否存在，避免无效删除
            if (file.exists()) {
                // 2. 执行删除并判断结果，失败时记录日志
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    log.info("临时txt文件删除成功，文件路径：{}", file.getAbsolutePath());
                } else {
                    log.error("临时txt文件删除失败，文件路径：{}", file.getAbsolutePath());
                }
            } else {
                log.warn("临时txt文件不存在，无需删除，文件路径：{}", file.getAbsolutePath());
            }
        } else {
            log.warn("待删除的txt文件对象为null，跳过删除");
        }
    }
}