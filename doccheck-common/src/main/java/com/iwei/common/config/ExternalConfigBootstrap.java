package com.iwei.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 外置配置引导类：在Log4j2初始化前加载日志路径（核心解决时机问题）
 */
@Slf4j
public class ExternalConfigBootstrap implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    // 外置配置文件名称
    private static final String CONFIG_FILE_NAME = "app-config.yml";
    // 全局复用的外置配置对象（避免重复解析文件）
    private static ExternalConfigLoader.AppExternalConfig GLOBAL_APP_CONFIG;

    /**
     * 核心：ApplicationEnvironmentPreparedEvent 触发时机早于Log4j2初始化
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // 即使event为null也要执行配置加载
        loadExternalConfig();
    }

    /**
     * 加载外部配置的核心逻辑
     */
    private void loadExternalConfig() {
        // 1. 禁止重复加载
        if (GLOBAL_APP_CONFIG != null) {
            return;
        }

        try {
            // 2. 获取Tomcat根目录（优先系统属性，兜底默认路径）
            String tomcatRootDir = System.getProperty("customConfigTomcatRootDir");
            if (StringUtils.isBlank(tomcatRootDir)) {
                tomcatRootDir = "D:/work/installation/tomcat/apache-tomcat-9.0.93";
                log.warn("未配置customConfigTomcatRootDir系统属性，使用兜底路径：{}", tomcatRootDir);
            }

            // 3. 拼接配置文件完整路径
            String configFilePath = tomcatRootDir + File.separator + "conf" + File.separator +
                    "custom-conf" + File.separator + "doccheck" + File.separator + CONFIG_FILE_NAME;
            File configFile = new File(configFilePath);

            // 4. 配置文件不存在：打印警告，使用默认日志路径
            if (!configFile.exists()) {
                log.warn("外置配置文件不存在，将使用默认日志路径！路径：{}", configFilePath);
                setDefaultLoggingPath();
                return;
            }

            // 5. 读取并解析YAML配置
            Yaml yaml = new Yaml(new Constructor(ExternalConfigLoader.AppExternalConfig.class));
            try (InputStream inputStream = new FileInputStream(configFile)) {
                GLOBAL_APP_CONFIG = yaml.load(inputStream);
                log.info("外置配置文件加载成功：{}", configFilePath);

                // 6. 设置日志路径系统属性（核心：此时Log4j2尚未初始化）
                setLoggingPathSystemProperty();
            }

        } catch (Exception e) {
            // 容错：加载失败不终止启动，仅打印错误，使用默认日志路径
            log.error("加载外置配置失败，将使用默认配置！", e);
            setDefaultLoggingPath();
        }
    }

    /**
     * 设置默认日志路径
     */
    private void setDefaultLoggingPath() {
        System.setProperty("logging.path", "./logs");
        log.info("已设置默认日志路径：./logs");
    }

    /**
     * 设置日志路径系统属性（含空值兜底、路径校验）
     */
    private void setLoggingPathSystemProperty() {
        if (GLOBAL_APP_CONFIG == null || GLOBAL_APP_CONFIG.getLogging() == null) {
            log.warn("外置配置中无logging节点，使用默认日志路径：./logs");
            System.setProperty("logging.path", "./logs");
            return;
        }

        // 7. 空字符串判断+兜底
        String logPath = GLOBAL_APP_CONFIG.getLogging().getPath();
        if (StringUtils.isBlank(logPath)) {
            logPath = "./logs";
            log.warn("外置配置中logging.path为空，使用默认路径：{}", logPath);
        }

        // 8. 尝试创建日志目录（避免路径不存在导致Log4j2报错）
        File logDir = new File(logPath);
        if (!logDir.exists() && !logDir.mkdirs()) {
            log.error("日志路径创建失败，降级使用默认路径！失败路径：{}", logPath);
            logPath = "./logs";
        }

        // 9. 最终设置系统属性
        System.setProperty("logging.path", logPath);
        log.info("日志路径系统属性已设置：{}", logPath);
    }

    /**
     * 获取全局配置对象（供其他组件复用）
     */
    public static ExternalConfigLoader.AppExternalConfig getGlobalAppConfig() {
        return GLOBAL_APP_CONFIG;
    }

    /**
     * 保证监听器优先执行（Order值越小，执行越早）
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级执行
    }
}