package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryDocStationLineMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档库与站线映射Mapper接口
 *
 * @author: zhaokangwei
 */
@Mapper
public interface RepositoryDocStationLineMappingMapper {

    /**
     * 根据ID查询映射记录
     * @param id 映射ID
     * @return 映射对象
     */
    RepositoryDocStationLineMapping selectById(@Param("id") Integer id);

    /**
     * 查询所有未删除的映射记录
     * @return 映射记录列表
     */
    List<RepositoryDocStationLineMapping> selectAll();

    /**
     * 插入映射记录
     * @param mapping 映射对象
     * @return 影响行数
     */
    int insert(RepositoryDocStationLineMapping mapping);

    /**
     * 根据ID更新映射记录
     * @param mapping 映射对象
     * @return 影响行数
     */
    int updateById(RepositoryDocStationLineMapping mapping);

    /**
     * 根据ID逻辑删除映射记录
     * @param id 映射ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 批量插入映射记录
     * @param repositoryDocStationLineMappingList 映射记录列表
     */
    void batchInsert(List<RepositoryDocStationLineMapping> repositoryDocStationLineMappingList);
}