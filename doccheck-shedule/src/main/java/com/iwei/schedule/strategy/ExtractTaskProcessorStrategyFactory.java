package com.iwei.schedule.strategy;

import com.iwei.common.enums.ScheduleTaskSourceEnum;
import javax.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

public class ExtractTaskProcessorStrategyFactory implements InitializingBean {

    @Resource
    private List<ExtractTaskProcessorStrategy> taskProcessorStrategies;

    private Map<ScheduleTaskSourceEnum, ExtractTaskProcessorStrategy> taskProcessorStrategyMap;

    public ExtractTaskProcessorStrategy getTaskProcessorStrategy(ScheduleTaskSourceEnum sourceType) {
        return taskProcessorStrategyMap.get(sourceType);
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        for(ExtractTaskProcessorStrategy taskProcessorStrategy : taskProcessorStrategies) {
            taskProcessorStrategyMap.put(taskProcessorStrategy.getSourceType(), taskProcessorStrategy);
        }
    }
}
