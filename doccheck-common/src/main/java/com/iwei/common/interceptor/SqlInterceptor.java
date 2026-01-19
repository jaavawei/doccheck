package com.iwei.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
})
@Component
@ConditionalOnProperty(name = "sql.log.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class SqlInterceptor implements Interceptor {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取StatementHandler对象（MyBatis处理SQL的核心类）
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        // 获取BoundSql（包含SQL和参数信息）
        BoundSql boundSql = handler.getBoundSql();
        // 原始SQL（带?占位符）
        String sql = boundSql.getSql();
        // 参数对象（可能是单个参数、Map、实体类等）
        Object parameterObject = boundSql.getParameterObject();
        // 参数映射列表（记录每个?对应的参数名称和类型）
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // MyBatis配置（用于获取类型处理器）
        Configuration configuration = getConfiguration(handler);
        // 类型处理器注册表（处理不同类型参数的格式化）
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        // 替换SQL中的?为实际参数值
        String actualSql = replacePlaceholder(sql, parameterMappings, parameterObject, typeHandlerRegistry);

        // 打印带实际参数的SQL
        log.info("SQL: " + actualSql);

        return invocation.proceed();
    }

    /**
     * 替换SQL中的?占位符为实际参数值
     */
    private String replacePlaceholder(String sql, List<ParameterMapping> parameterMappings,
                                      Object parameterObject, TypeHandlerRegistry typeHandlerRegistry) {
        if (parameterMappings.isEmpty() || parameterObject == null) {
            return sql; // 无参数时直接返回原始SQL
        }

        StringBuilder sqlBuilder = new StringBuilder(sql);
        // 从后往前替换（避免替换后索引偏移）
        for (int i = parameterMappings.size() - 1; i >= 0; i--) {
            ParameterMapping mapping = parameterMappings.get(i);
            String propertyName = mapping.getProperty(); // 参数名称（如实体类的字段名）
            // 获取参数值（通过MyBatis的MetaObject反射获取，支持Map、实体类等）
            Object value = getParameterValue(parameterObject, propertyName);
            // 格式化参数值（如字符串加单引号、日期转字符串等）
            String valueStr = formatValue(value, typeHandlerRegistry, mapping.getJavaType());
            // 查找当前?的位置并替换
            int questionMarkIndex = sqlBuilder.lastIndexOf("?");
            if (questionMarkIndex != -1) {
                sqlBuilder.replace(questionMarkIndex, questionMarkIndex + 1, valueStr);
            }
        }
        return sqlBuilder.toString();
    }

    /**
     * 获取参数值（支持实体类、Map、单个基本类型参数）
     */
    private Object getParameterValue(Object parameterObject, String propertyName) {
        // 处理参数为基本数据类型的情况
        if (parameterObject instanceof Number || parameterObject instanceof Boolean || parameterObject instanceof Character || parameterObject instanceof String) {
            // 当参数是基本类型且属性名为"id"时，直接返回参数值
            if ("id".equals(propertyName)) {
                return parameterObject;
            }
            // 对于其他属性名，返回null或者抛出异常前给个默认值
            return parameterObject;
        }
        
        MetaObject metaObject = org.apache.ibatis.reflection.SystemMetaObject.forObject(parameterObject);
        return metaObject.getValue(propertyName);
    }

    /**
     * 格式化参数值（处理字符串、日期、数字等类型）
     */
    private String formatValue(Object value, TypeHandlerRegistry typeHandlerRegistry, Class<?> javaType) {
        if (value == null) {
            return "NULL";
        }
        // 字符串类型：加单引号，转义内部的单引号
        if (String.class.equals(javaType) || CharSequence.class.isAssignableFrom(javaType)) {
            String strValue = value.toString().replace("'", "''"); // 转义单引号
            return "'" + strValue + "'";
        }
        // 日期类型：格式化为字符串
        if (Date.class.isAssignableFrom(javaType)) {
            return "'" + DATE_FORMAT.format(value) + "'";
        }
        // 其他类型（数字、布尔等）：直接转字符串
        return value.toString();
    }

    /**
     * 从StatementHandler中获取MyBatis配置
     */
    private Configuration getConfiguration(StatementHandler handler) {
        MetaObject metaObject = org.apache.ibatis.reflection.SystemMetaObject.forObject(handler);
        return (Configuration) metaObject.getValue("delegate.configuration");
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可接收配置参数（如日期格式等）
    }
}