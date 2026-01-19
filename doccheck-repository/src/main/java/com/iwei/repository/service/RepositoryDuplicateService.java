package com.iwei.repository.service;

import com.iwei.common.entity.PageResult;
import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import com.iwei.rule.entity.vo.RuleExtractVo;

import java.util.List;

/**
 * 查重库表service
 *
 * @auther: zhaokangwei
 */
public interface RepositoryDuplicateService {

    /**
     * 根据id查询
     */
    RepositoryDuplicate getById(Integer id);

    /**
     * 新增查重库
     */
    void addDuplicate(RepositoryDuplicateVo repositoryDuplicateVo);

    /**
     * 更新查重库
     */
    void updateDuplicate(RepositoryDuplicateVo repositoryDuplicateVo);

    /**
     * 删除查重库
     */
    void deleteDuplicate(RepositoryDuplicateVo repositoryDuplicateVo);

    /**
     * 分页查询查重库列表
     */
    PageResult<RepositoryDuplicateVo> queryDuplicateList(String duplicateRepositoryName, Integer dataSource, Integer pageNo, Integer pageSize);
}
