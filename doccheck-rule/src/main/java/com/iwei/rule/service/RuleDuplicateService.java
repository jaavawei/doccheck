package com.iwei.rule.service;

import com.iwei.common.entity.PageResult;
import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.entity.vo.RuleDuplicateVo;
import com.iwei.rule.entity.vo.RuleDuplicateVo;

import java.util.List;

/**
 * 查重规则表service
 *
 * @auther: zhaokangwei
 */
public interface RuleDuplicateService {
    
    /**
     * 添加查重规则
     */
    int add(RuleDuplicateVo ruleDuplicateVo);

    /**
     * 更新查重规则
     */
    boolean update(RuleDuplicateVo ruleDuplicateVo);

    /**
     * 逻辑删除查重规则
     */
    boolean remove(RuleDuplicateVo ruleDuplicateVo);

    /**
     * 分页查询查重规则列表
     */
    PageResult<RuleDuplicateVo> queryRuleList(String ruleName, String operateUser, Integer pageNo, Integer pageSize);

}
