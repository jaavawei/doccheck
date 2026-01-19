package com.iwei.common.mapper;

import com.iwei.common.entity.AttrTable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性配置表 Mapper 接口
 *
 */
@Mapper
public interface AttrTableMapper {

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return AttrTable实体
     */
    AttrTable queryById(Integer id);

    /**
     * 查询所有记录
     * @return AttrTable实体列表
     */
    List<AttrTable> queryAll();

    /**
     * 插入记录
     * @param attrTable 实体对象
     * @return 影响行数
     */
    int insert(AttrTable attrTable);

    /**
     * 批量插入记录
     * @param attrTables 实体对象列表
     * @return 影响行数
     */
    int batchInsert(List<AttrTable> attrTables);

    /**
     * 根据ID更新记录
     * @param attrTable 实体对象
     * @return 影响行数
     */
    int updateById(AttrTable attrTable);

    /**
     * 根据ID删除记录
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Integer id);
}