package com.iwei.oss.config;

import com.iwei.oss.service.FileService;
import com.iwei.oss.service.impl.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${storage.service.type}")
    private String storageType;

    @Bean
    public FileService fileService() {
        switch (storageType.toLowerCase()) {
            case "fastdfs":
                return new MinioFileServiceImpl();
            case "minio":
                return new MinioFileServiceImpl();
            case "aliyunoss":
                return new AliyunOssFileServiceImpl();
            case "rustfs":
                return new RustFsFileServiceImpl();
            case "huaweiobs":
                return new HuaweiObsFileServiceImpl();
            case "jiangsu":
                return new JiangsuFileServiceImpl();
            default:
                throw new IllegalArgumentException("未找到对应的文件存储服务: " + storageType);
        }
    }
}