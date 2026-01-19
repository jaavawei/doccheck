package com.iwei.schedule.processor;

import com.iwei.task.entity.ScheduleReview;

/**
 * 审查任务处理器适配器
 *
 * @author:zhaokangwei
 */
public interface ReviewTaskProcessorAdaptor {

    /*
     * 处理任务
     */
    public void processTask(ScheduleReview task);
}
