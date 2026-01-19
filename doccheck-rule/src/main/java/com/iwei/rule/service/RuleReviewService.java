package com.iwei.rule.service;

import com.iwei.common.entity.PageResult;
import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.entity.vo.RuleReviewVo;

/**
 * 审查规则表service
 *
 * @auther: zhaokangwei
 */
public interface RuleReviewService {

    /**
     * 根据id查询
     */
    RuleReview getById(Integer id);

    /**
     * 新增审查规则
     */
    void addRuleReview(RuleReviewVo ruleReviewVo);

    /**
     * 更新审查规则
     */
    void updateRuleReview(RuleReviewVo ruleReviewVo);

    /**
     * 删除审查规则
     */
    void deleteRuleReview(RuleReviewVo ruleReviewVo);

    /**
     * 分页查询审查规则列表
     */
    PageResult<RuleReviewVo> queryRuleReviewList(String ruleName, String operateUser, Integer pageNo, Integer pageSize);
}