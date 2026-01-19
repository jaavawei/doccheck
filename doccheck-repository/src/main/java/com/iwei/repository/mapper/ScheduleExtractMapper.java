package com.iwei.repository.mapper;

import com.iwei.repository.entity.ScheduleExtract;

import java.util.List;

/**
 * 解析任务调度mapper
 *
 * @auther: zhaokangwei
 */
public interface ScheduleExtractMapper {

    /**
     * 获取待执行任务
     */
    List<ScheduleExtract> getPendingTasks(int batchSize);


    /**
     * 新增任务
     */
    void insert(ScheduleExtract scheduleExtract);

    /**
     * 根据文档id查询任务状态
     */
    Integer queryTaskStatusBySourceId(Integer repositoryDocId);

    /**
     * 批量插入
     */
    void batchInsert(List<ScheduleExtract> scheduleExtractList);

    /**
     * 根据id更新
     */
    boolean update(ScheduleExtract scheduleExtract);

    /**
     * 根据来源id更新
     */
    void updateBySourceId(ScheduleExtract scheduleExtract);
}
