package com.iwei.oss.config;

import com.obs.services.ObsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "huawei.obs", name = "endpoint")
public class HuaweiObsConfig {

    @Value("${huawei.obs.endpoint}")
    private String endpoint;

    @Value("${huawei.obs.accessKeyId}")
    private String accessKeyId;

    @Value("${huawei.obs.secretAccessKey}")
    private String secretAccessKey;

    @Value("${huawei.obs.bucketName}")
    private String bucketName;

    @Bean
    public ObsClient obsClient() {
        return new ObsClient(accessKeyId, secretAccessKey, endpoint);
    }
}