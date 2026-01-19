package com.iwei.app.config;

import com.iwei.common.config.ExternalConfigLoader;
import org.platform.robocop.client.RobocopClient;
import org.platform.robocop.client.RobocopClientFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 基于外置配置初始化 RobocopClient
 */
@Component
@ConditionalOnProperty(name = "external.config.enabled", havingValue = "true", matchIfMissing = true)
public class RobocopClientInitializer {
    @Resource
    private ExternalConfigLoader configLoader;

    private RobocopClient robocopClient;

    @PostConstruct
    public void initRobocopClient() {
        // 1. 从外置配置中获取 Robocop 配置
        ExternalConfigLoader.AppExternalConfig.JiangsuRobocopConfig.RobocopConfig robocopConfig =
                configLoader.getAppConfig().getJiangsu().getRobocop();

        // 2. 空值校验
        Assert.hasText(robocopConfig.getKey(), "外置配置 jiangsu.robocop.key 不能为空！");
        Assert.hasText(robocopConfig.getSecret(), "外置配置 jiangsu.robocop.secret 不能为空！");
        Assert.hasText(robocopConfig.getBaseUrl(), "外置配置 jiangsu.robocop.base-url 不能为空！");

        // 3. 初始化 Client
        this.robocopClient = RobocopClientFactory.getRobocopClient(
                robocopConfig.getKey(),
                robocopConfig.getSecret(),
                robocopConfig.getBaseUrl()
        );
    }

    // 注册为 Spring Bean，供业务类注入
    @Bean
    public RobocopClient jiangsuRobocopClient() {
        return this.robocopClient;
    }
}