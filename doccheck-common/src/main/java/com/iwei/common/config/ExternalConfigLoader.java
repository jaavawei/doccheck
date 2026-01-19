package com.iwei.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 外置配置加载工具类（复用Bootstrap提前加载的配置）
 */
@Slf4j
@Component
public class ExternalConfigLoader {
    // 是否启用外部配置，默认为true
    private boolean enabled = true;
    // 复用Bootstrap加载的配置对象
    private AppExternalConfig appConfig;

    /**
     * 初始化：复用Bootstrap提前加载的配置
     */
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("外部配置已禁用，跳过初始化");
            return;
        }
        // 复用Bootstrap加载的全局配置，避免重复解析文件
        this.appConfig = ExternalConfigBootstrap.getGlobalAppConfig();
        if (this.appConfig == null) {
            log.warn("外置配置未加载成功，appConfig为null（但不影响启动）");
        } else {
            log.info("ExternalConfigLoader初始化完成，配置已加载");
        }
    }

    /**
     * 获取解析后的全量配置（供业务使用）
     */
    public AppExternalConfig getAppConfig() {
        if (!enabled) {
            throw new RuntimeException("外部配置已禁用！");
        }
        if (appConfig == null) {
            throw new RuntimeException("外置配置未加载！");
        }
        return appConfig;
    }

    // ========== getter/setter ==========
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 配置对象映射（与app-config.yml结构一致，无改动）
     */
    @Data
    public static class AppExternalConfig {
        // Robocop 配置
        private JiangsuRobocopConfig jiangsu;
        // JNDI 数据源配置
        private DataSourceConfig datasource;
        // 日志配置
        private LoggingConfig logging;

        @Data
        public static class JiangsuRobocopConfig {
            private RobocopConfig robocop;

            @Data
            public static class RobocopConfig {
                private String key;
                private String secret;
                private String baseUrl;
            }
        }

        @Data
        public static class DataSourceConfig {
            private String jndiName;
        }

        @Data
        public static class LoggingConfig {
            private String path;
        }
    }
}