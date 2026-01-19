package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryDuplicateDocMapping;
import java.util.List;

/**
 * 查重库-文档库 映射表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryDuplicateDocMappingMapper {
    RepositoryDuplicateDocMapping selectById(Integer id);

    List<RepositoryDuplicateDocMapping> selectList(RepositoryDuplicateDocMapping mapping);

    int insert(RepositoryDuplicateDocMapping mapping);

    int updateById(RepositoryDuplicateDocMapping mapping);

    int deleteById(Integer id);

    /**
     * 批量插入
     */
    void batchInsert(List<RepositoryDuplicateDocMapping> docMappingList);

    /**
     * 根据 查重库id 更新
     */
    void updateByDuplicateId(RepositoryDuplicateDocMapping docMapping);
}

