package com.iwei.task.mapper;

import com.iwei.task.entity.TaskDuplicateResult;

import java.util.List;

/**
 * 查重任务结果表mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateResultMapper {

    /**
     * 根据id查询
     */
    TaskDuplicateResult queryById(Integer resultId);

    /**
     * 插入
     */
    void insert(TaskDuplicateResult taskDuplicateResult);
}

