package com.iwei.rule.mapper;

import com.iwei.rule.entity.RuleExtract;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 提取规则表mapper
 *
 * @auther: zhaokangwei
 */
public interface RuleExtractMapper {

    /**
     * 新增提取规则
     */
    int insert(RuleExtract ruleExtract);

    /**
     * 根据id更新
     */
    int updateById(RuleExtract ruleExtract);


    /**
     * 根据条件查询数量
     */
    int countByCondition(RuleExtract ruleExtract);

    /**
     * 根据条件分页查询
     */
    List<RuleExtract> queryPageByCondition(@Param("ruleExtract") RuleExtract ruleExtract,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    /**
     * 根据 id 查询
     */
    RuleExtract queryById(Integer ruleId);

    /**
     * 查询全部提取规则名称和id
     */
    List<RuleExtract> queryAllRuleIdAndName();
}
