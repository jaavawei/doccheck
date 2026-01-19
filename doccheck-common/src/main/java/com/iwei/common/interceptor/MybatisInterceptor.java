package com.iwei.common.interceptor;

import com.iwei.common.enums.DelFlgEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.ibatis.mapping.SqlCommandType.INSERT;
import static org.apache.ibatis.mapping.SqlCommandType.UPDATE;


/**
 * Mybatis拦截器：填充createBy,createAt,updateAt,updateBy,delFlg等公共字段
 *
 * @auther: zhaokangwei
 */
@Component
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MybatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];

        if (parameter == null) {
            return invocation.proceed();
        }

        Integer loginId = 0;

        if (sqlCommandType == INSERT || sqlCommandType == UPDATE) {
            handleParameter(parameter, loginId, sqlCommandType);
        }
        return invocation.proceed();
    }

    /**
     * 处理参数填充
     */
    private void handleParameter(Object param, Integer loginId, SqlCommandType cmdType) {
        if (param instanceof Map) {
            handleMap((Map<?, ?>) param, loginId, cmdType);
        } else if (param instanceof Collection) {
            handleCollection((Collection<?>) param, loginId, cmdType);
        } else {
            processSingleEntity(param, loginId, cmdType);
        }
    }

    /**
     * 处理map的参数填充
     */
    private void handleMap(Map<?, ?> paramMap, Integer loginId, SqlCommandType cmdType) {
        paramMap.values().forEach(value -> {
            if (value instanceof Collection) {
                handleCollection((Collection<?>) value, loginId, cmdType);
            } else {
                processSingleEntity(value, loginId, cmdType);
            }
        });
    }

    /**
     * 处理参数集合的参数填充
     */
    private void handleCollection(Collection<?> collection, Integer loginId, SqlCommandType cmdType) {
        collection.forEach(item -> processSingleEntity(item, loginId, cmdType));
    }

    /**
     * 处理单个参数对象的参数填充
     */
    private void processSingleEntity(Object entity, Integer loginId, SqlCommandType cmdType) {
        if (isJavaClass(entity.getClass())) return;

        if (cmdType == INSERT) {
            populateInsertFields(entity, loginId);
        } else if (cmdType == UPDATE) {
            populateUpdateFields(entity, loginId);
        }
    }

    /**
     * 填入插入sql字段
     */
    private void populateInsertFields(Object entity, Integer loginId) {
        setFieldIfNull(entity, "delFlg", DelFlgEnum.UN_DELETED.getCode());
        setFieldIfNull(entity, "createdBy", loginId);
        setFieldIfNull(entity, "createdAt", new Date());
        setFieldIfNull(entity, "updatedBy", loginId);
        setFieldIfNull(entity, "updatedAt", new Date());
    }

    /**
     * 填充更新sql字段
     */
    private void populateUpdateFields(Object entity, Integer loginId) {
        setFieldIfNull(entity, "updatedBy", loginId);
        setFieldIfNull(entity, "updatedAt", new Date(System.currentTimeMillis()));
    }

    /**
     * 如果目标字段为空的话填充
     */
    private void setFieldIfNull(Object obj, String fieldName, Object value) {
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field == null) return;

            field.setAccessible(true);
            // 仅当字段当前值为空时设置
            if (field.get(obj) == null) {
                field.set(obj, value);
            }
            field.setAccessible(false);
        } catch (Exception e) {
            log.debug("Field set failed: {}", fieldName, e);
        }
    }

    /**
     * 查询目标field
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && !isJavaClass(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 判断是否为java类
     */
    private boolean isJavaClass(Class<?> clazz) {
        return clazz != null && clazz.getClassLoader() == null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}
