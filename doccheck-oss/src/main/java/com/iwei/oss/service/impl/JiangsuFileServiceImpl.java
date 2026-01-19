package com.iwei.oss.service.impl;

import com.iwei.oss.service.FileService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.platform.robocop.client.FileParam;
import org.platform.robocop.client.RobocopClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.*;

/**
 * 江苏文件服务实现类
 */
@Slf4j
@ConditionalOnProperty(name = "storage.service.type", havingValue = "jiangsu")
public class JiangsuFileServiceImpl implements FileService {

    @Resource
    private RobocopClient robocopClient;

    @Override
    @SneakyThrows
    public String uploadFile(InputStream inputStream, String fileName, String container, String middlePath) {
        File tempFile = inputStreamToTempFile(inputStream, fileName);
        FileParam fileParam = new FileParam(tempFile);
        String uuid = robocopClient.uploadFile(fileParam);
        return uuid;
    }


    @Override
    @SneakyThrows
    public InputStream downloadFile(String url) {
        // 这里的 url 实际上是 uuid
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        robocopClient.getFileContent(url, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    @SneakyThrows
    public void deleteFile(String url) {
        // 这里的 url 实际上是 uuid
        robocopClient.deleteFile(url);
    }

    /**
     * InputStream 转 File
     */
    private static File inputStreamToTempFile(InputStream inputStream, String fileName) throws IOException {
        String suffix = fileName.contains(".") ?
                fileName.substring(fileName.lastIndexOf(".")) : ".tmp";
        String prefix = fileName.contains(".") ?
                fileName.substring(0, fileName.lastIndexOf(".")) : "temp_file";

        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } finally {
            inputStream.close();
        }
        return tempFile;
    }
}