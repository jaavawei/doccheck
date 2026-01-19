package com.iwei.schedule.processor;

import com.iwei.task.entity.ScheduleDuplicate;
import com.iwei.task.entity.ScheduleXj;

/**
 * 新疆任务处理器适配器
 *
 * @author:zhaokangwei
 */
public interface XjTaskProcessorAdaptor {
    /**
     * 处理任务
     */
    public void processTask(ScheduleXj task);
}
