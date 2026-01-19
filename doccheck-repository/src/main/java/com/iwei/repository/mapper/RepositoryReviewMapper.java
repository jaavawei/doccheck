package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.RepositoryReview;
import com.iwei.repository.entity.vo.RepositoryReviewVo;
import com.iwei.rule.entity.RuleExtract;

import java.util.List;

/**
 * 审查库表mapper
 *
 * @auther: zhaokangwei
 */
public interface RepositoryReviewMapper {
    /**
     * 根据id查询
     */
    RepositoryReview queryById(Integer id);

    List<RepositoryReview> selectList(RepositoryReview repositoryReview);

    int insert(RepositoryReview repositoryReview);

    /**
     * 根据id更新
     */
    int updateById(RepositoryReview repositoryReview);

    int deleteById(Integer id);

    /**
     * 根据条件分页查询
     */
    List<RepositoryReview> queryPageByCondition(RepositoryReviewVo repositoryReviewVo, Integer pageSize, Integer offset);

    /**
     * 根据条件计数
     */
    int countByCondition(RepositoryReviewVo repositoryReviewVo);

    /**
     * 根据审查库id查询关联文档
     */
    List<RepositoryDoc> queryDocByReviewId(Integer repositoryReviewId);

    /**
     * 查询id与名称
     */
    List<RepositoryReview> queryIdAndName();

    /**
     * 根据审查库id查询提取规则
     */
    List<RuleExtract> queryRuleByReviewId(Integer reviewId);
}