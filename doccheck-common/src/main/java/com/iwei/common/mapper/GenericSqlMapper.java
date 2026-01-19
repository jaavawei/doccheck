package com.iwei.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Map;

/**
 * 通用SQL执行Mapper接口
 * 使用参数化查询来提高安全性
 */
@Mapper
public interface GenericSqlMapper {

    /**
     * 执行查询SQL，返回Map列表
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return 查询结果列表
     */
    @SelectProvider(type = GenericSqlProvider.class, method = "buildSelect")
    List<Map<String, Object>> executeSelect(@Param("sql") String sql, @Param("params") Map<String, Object> params);

    /**
     * 执行插入SQL
     * @param sql SQL插入语句
     * @param params 插入参数
     * @return 影响的行数
     */
    @UpdateProvider(type = GenericSqlProvider.class, method = "buildInsert")
    int executeInsert(@Param("sql") String sql, @Param("params") Map<String, Object> params);

    /**
     * 执行更新SQL
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 影响的行数
     */
    @UpdateProvider(type = GenericSqlProvider.class, method = "buildUpdate")
    int executeUpdate(@Param("sql") String sql, @Param("params") Map<String, Object> params);

    /**
     * 执行删除SQL
     * @param sql SQL删除语句
     * @param params 删除参数
     * @return 影响的行数
     */
    @UpdateProvider(type = GenericSqlProvider.class, method = "buildDelete")
    int executeDelete(@Param("sql") String sql, @Param("params") Map<String, Object> params);
}