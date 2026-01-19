package com.iwei.oss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storage.service.type", havingValue = "rustfs")
public class RustFsConfig {

    @Value("${rustfs.server.url}")
    private String serverUrl;

    @Value("${rustfs.access.token}")
    private String accessToken;

    public String getServerUrl() {
        return serverUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }
}