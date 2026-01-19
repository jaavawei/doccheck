package com.iwei.task.mapper;

import com.iwei.task.entity.TaskReviewRuleMapping;

import java.util.List;

/**
 * 任务审查规则映射mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskReviewRuleMappingMapper {
    TaskReviewRuleMapping selectById(Integer id);

    List<TaskReviewRuleMapping> selectList(TaskReviewRuleMapping mapping);

    int insert(TaskReviewRuleMapping mapping);

    int updateById(TaskReviewRuleMapping mapping);

    int deleteById(Integer id);

    /**
     * 批量插入
     */
    void batchInsert(List<TaskReviewRuleMapping> ruleMappingList);

    /**
     * 根据审查任务id修改
     */
    void updateByReviewId(TaskReviewRuleMapping taskReviewRuleMapping);

    /**
     * 根据条件计数
     */
    Integer countByCondition(TaskReviewRuleMapping taskReviewRuleMapping);

    /**
     * 根据条件查询
     */
    List<TaskReviewRuleMapping> queryByCondition(TaskReviewRuleMapping taskReviewRuleMapping);

    /**
     * 根据任务id查询未通过项
     */
    List<TaskReviewRuleMapping> queryUncompliantItemByReviewId(Integer id);
}