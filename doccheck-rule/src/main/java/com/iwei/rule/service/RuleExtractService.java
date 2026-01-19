package com.iwei.rule.service;

import com.iwei.common.entity.PageResult;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.entity.vo.RuleExtractVo;
import java.util.List;

/**
 * 提取规则表service
 *
 * @auther: zhaokangwei
 */
public interface RuleExtractService {

    /**
     * 添加提取规则
     */
    int add(RuleExtractVo ruleExtractVo);

    /**
     * 更新提取规则
     */
    boolean update(RuleExtractVo ruleExtractVo);

    /**
     * 逻辑删除提取规则
     */
    boolean remove(RuleExtractVo ruleExtractVo);

    /**
     * 分页查询提取规则列表
     */
    PageResult<RuleExtractVo> queryRuleList(String ruleName, String operateUser, Integer pageNo, Integer pageSize);


    /**
     * 查询全部提取规则
     */
    List<RuleExtractVo> queryAllRuleIdAndName();
}

