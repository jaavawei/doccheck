package com.iwei.repository.mapper;

import java.util.List;
import com.iwei.repository.entity.RepositoryDuplicateExtractMapping;

/**
 * 查重库-提取规则 映射表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryDuplicateExtractMappingMapper {
    RepositoryDuplicateExtractMapping selectById(Integer id);

    List<RepositoryDuplicateExtractMapping> selectList(RepositoryDuplicateExtractMapping mapping);

    int insert(RepositoryDuplicateExtractMapping mapping);

    int updateById(RepositoryDuplicateExtractMapping mapping);

    int deleteById(Integer id);

    /**
     * 批量插入
     */
    void batchInsert(List<RepositoryDuplicateExtractMapping> ruleMappingList);
}
