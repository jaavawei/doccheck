package com.iwei.task.mapper;

import com.iwei.task.entity.ScheduleReview;

import java.util.List;

/**
 * 审查任务调度mapper
 *
 * @author: zhaokangwei
 */
public interface ScheduleReviewMapper {

    /**
     * 新增审查任务
     */
    void insert(ScheduleReview scheduleReview);

    /**
     * 根据审查任务 id 查询任务状态
     */
    Integer queryReviewStatusByReviewId(Integer reviewId);

    /**
     * 批量插入审查任务
     */
    void batchInsert(List<ScheduleReview> scheduleReviewList);

    /**
     * 根据 id 更新审查任务
     */
    boolean update(ScheduleReview scheduleReview);

    /**
     * 根据审查任务 id 更新审查任务
     */
    void updateByReviewId(ScheduleReview scheduleReview);

    /**
     * 获取待执行任务
     */
    List<ScheduleReview> getPendingTasks(int batchSize);

    /**
     * 根据 id 查询任务
     */
    ScheduleReview queryById(Integer id);
}
