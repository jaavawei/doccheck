package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryReviewDocMapping;

import java.util.List;

/**
 * 审查库-文档库 映射表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryReviewDocMappingMapper {
    RepositoryReviewDocMapping selectById(Integer id);

    List<RepositoryReviewDocMapping> selectList(RepositoryReviewDocMapping mapping);

    int insert(RepositoryReviewDocMapping mapping);

    int updateById(RepositoryReviewDocMapping mapping);

    int deleteById(Integer id);

    /**
     * 批量插入
     */
    void batchInsert(List<RepositoryReviewDocMapping> docMappingList);

    void batchUpdateByReviewId(List<RepositoryReviewDocMapping> docMappingList);

    /**
     * 根据审查库id更新
     */
    void updateByReviewId(RepositoryReviewDocMapping docMapping);
}