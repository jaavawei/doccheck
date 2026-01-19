package com.iwei.app.config;

import com.iwei.common.config.ExternalConfigLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 基于外置配置初始化 JNDI 数据源
 */
@Configuration
@ConditionalOnProperty(name = "external.config.enabled", havingValue = "true", matchIfMissing = true)
public class JndiDataSourceConfig {
    @Resource
    private ExternalConfigLoader configLoader;

    @Bean
    public DataSource dataSource() {
        // 1. 从外置配置中获取 JNDI 名称
        String jndiName = configLoader.getAppConfig().getDatasource().getJndiName();
        // 2. 校验 JNDI 名称
        if (StringUtils.isBlank(jndiName)) {
            throw new RuntimeException("外置配置 datasource.jndi-name 不能为空！");
        }
        // 3. 从 Tomcat 中查找 JNDI 数据源
        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
        return dataSourceLookup.getDataSource(jndiName);
    }
}