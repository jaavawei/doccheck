package com.iwei.task.service;

import com.iwei.common.entity.PageResult;
import com.iwei.task.entity.TaskReviewInfo;
import com.iwei.task.entity.vo.TaskReviewInfoVo;

import java.util.Map;

/**
 * 审查任务信息表service
 *
 * @auther: zhaokangwei
 */
public interface TaskReviewInfoService {

    /**
     * 根据id查询
     */
    TaskReviewInfo getById(Integer id);

    /**
     * 新增审查任务
     */
    void addTaskReview(TaskReviewInfoVo taskReviewInfoVo);

    /**
     * 删除审查任务
     */
    void deleteTaskReview(TaskReviewInfoVo taskReviewInfoVo);

    /**
     * 分页查询审查任务列表
     */
    PageResult<TaskReviewInfoVo> queryTaskReviewList(String taskReviewName, Integer taskStatus, String projectName, Integer pageNo, Integer pageSize);

    /**
     * 查询审查规则和审查库
     */
    Map<String, Object> queryReviewRuleAndRepository();

    /**
     * 查询审查任务详情
     */
    TaskReviewInfoVo queryReviewTaskDetail(Integer id);
}