package com.iwei.task.mapper;

import com.iwei.rule.entity.RuleReview;
import com.iwei.task.entity.TaskReviewInfo;
import com.iwei.task.entity.vo.TaskReviewInfoVo;

import java.util.List;

/**
 * 审查任务信息表mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskReviewInfoMapper {
    /**
     * 根据id查询
     */
    TaskReviewInfo queryById(Integer id);

    /**
     * 根据条件查询列表
     */
    List<TaskReviewInfo> selectList(TaskReviewInfo taskReviewInfo);

    /**
     * 插入
     */
    int insert(TaskReviewInfo taskReviewInfo);

    /**
     * 根据id更新
     */
    int updateById(TaskReviewInfo taskReviewInfo);

    /**
     * 根据id删除
     */
    int deleteById(Integer id);

    /**
     * 根据条件分页查询
     */
    List<TaskReviewInfo> queryPageByCondition(TaskReviewInfo taskReviewInfo, Integer pageSize, Integer offset);

    /**
     * 根据条件计数
     */
    int countByCondition(TaskReviewInfo taskReviewInfo);

    /*
     * 根据审查库id查询审查规则
     */
    List<RuleReview> queryRuleByReviewId(Integer taskReviewId);
}