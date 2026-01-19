package com.iwei.schedule.strategy;

import com.iwei.common.enums.ScheduleTaskSourceEnum;
import com.iwei.common.enums.TaskStatusEnum;

public interface ExtractTaskProcessorStrategy {
    void updateSourceStatus(Integer sourceId, TaskStatusEnum status, String extractContent, String errorMsg);
    Integer getRuleExtractId(Integer sourceId);
    String getDocUrl(Integer sourceId);
    ScheduleTaskSourceEnum getSourceType();
}

