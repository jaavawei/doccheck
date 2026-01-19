package com.iwei.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "huawei.obs")
public class HuaweiObsProperties {
    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
}