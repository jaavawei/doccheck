package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import com.iwei.rule.entity.RuleExtract;

import java.util.List;

/**
 * 查重库表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryDuplicateMapper {
    /**
     * 根据id查询
     */
    RepositoryDuplicate queryById(Integer id);

    List<RepositoryDuplicate> selectList(RepositoryDuplicate repositoryDuplicate);

    int insert(RepositoryDuplicate repositoryDuplicate);

    /**
     * 根据id更新
     */
    int updateById(RepositoryDuplicate repositoryDuplicate);

    int deleteById(Integer id);

    /**
     * 根据条件分页查询
     */
    List<RepositoryDuplicate> queryPageByCondition(RepositoryDuplicateVo repositoryDuplicateVo, Integer pageSize, Integer offset);

    /**
     * 根据条件计数
     */
    int countByCondition(RepositoryDuplicateVo repositoryDuplicateVo);

    /**
     * 根据查重库id查询关联文档
     */
    List<RepositoryDoc> queryDocByDuplicateId(Integer repositoryDuplicateId);

    /**
     * 查询id与名称
     */
    List<RepositoryDuplicate> queryIdAndName();

    /**
     * 根据查重库id查询提取规则
     */
    List<RuleExtract> queryRuleByDuplicateId(Integer duplicateId);

    /*
     * 分页查询文档库中文档
     */
    List<RepositoryDoc> queryByPageAndRepoDupId(Integer infoId, Integer repositoryDuplicateId, Integer pageSize, Integer offset, Integer duplicateFlg, String projectCode, String projectName, String implOrg, String planYear, String projectMsg, Integer duplicateStatus);

    /**
     * 根据查重库id和条件分页查询文档
     */
    List<RepositoryDoc> queryByPageAndRepoDupIdAndCondition(Integer repositoryDuplicateId, Integer pageSize, Integer offset, String projectName);

    Integer countByRepoDupId(Integer infoId, Integer repositoryDuplicateId, Integer duplicateFlg, String projectCode, String projectName,
                             String implOrg, String planYear, String projectMsg, Integer duplicateStatus);
}

