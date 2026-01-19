package com.iwei.schedule.processor;

import com.iwei.task.entity.ScheduleDuplicate;

/**
 * 文档提取任务处理适配器
 *
 * @auther: zhaokangwei
 */
public interface DuplicateTaskProcessorAdaptor {


    /**
     * 处理任务
     */
    public void processTask(ScheduleDuplicate task);
}
