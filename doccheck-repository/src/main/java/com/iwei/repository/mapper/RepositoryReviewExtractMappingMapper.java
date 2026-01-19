package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryReviewExtractMapping;

import java.util.List;

/**
 * 审查库-提取规则 映射表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryReviewExtractMappingMapper {
    RepositoryReviewExtractMapping selectById(Integer id);

    List<RepositoryReviewExtractMapping> selectList(RepositoryReviewExtractMapping mapping);

    int insert(RepositoryReviewExtractMapping mapping);

    int updateById(RepositoryReviewExtractMapping mapping);

    int deleteById(Integer id);

    /**
     * 批量插入
     */
    void batchInsert(List<RepositoryReviewExtractMapping> ruleMappingList);

    void batchUpdateByReviewId(List<RepositoryReviewExtractMapping> ruleMappingList);

    /**
     * 根据审查库id更新
     */
    void updateByReviewId(RepositoryReviewExtractMapping extractMapping);
}