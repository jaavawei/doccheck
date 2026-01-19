package com.iwei.rule.mapper;

import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.entity.vo.RuleReviewVo;

import java.util.List;

/**
 * 审查规则表mapper
 *
 * @auther: zhaokangwei
 */
public interface RuleReviewMapper {
    /**
     * 根据id查询
     */
    RuleReview queryById(Integer id);

    /**
     * 根据条件查询列表
     */
    List<RuleReview> selectList(RuleReview ruleReview);

    /**
     * 插入
     */
    int insert(RuleReview ruleReview);

    /**
     * 根据id更新
     */
    int updateById(RuleReview ruleReview);

    /**
     * 根据id删除
     */
    int deleteById(Integer id);

    /**
     * 根据条件分页查询
     */
    List<RuleReview> queryPageByCondition(RuleReviewVo ruleReviewVo, Integer pageSize, Integer offset);

    /**
     * 根据条件计数
     */
    int countByCondition(RuleReviewVo ruleReviewVo);

    /**
     * 根据审查任务id查询关联的审查规则
     */
    List<RuleReview> queryByTaskReviewId(Integer taskReviewId);

    /**
     * 查询审查规则 id 和 name
     */
    List<RuleReview> queryIdAndName();
}