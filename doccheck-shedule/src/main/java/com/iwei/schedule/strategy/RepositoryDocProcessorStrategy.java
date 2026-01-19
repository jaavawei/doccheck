package com.iwei.schedule.strategy;

import com.iwei.common.enums.ScheduleTaskSourceEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.mapper.RepositoryDocMapper;
import javax.annotation.Resource;

public class RepositoryDocProcessorStrategy implements ExtractTaskProcessorStrategy {
    @Resource
    private RepositoryDocMapper repositoryDocMapper;

    @Override
    public void updateSourceStatus (Integer sourceId, TaskStatusEnum status, String extractContent, String errorMsg) {
        RepositoryDoc repositoryDoc = new RepositoryDoc();
        repositoryDoc.setId(sourceId);
        repositoryDoc.setStatus(status.getCode());
        repositoryDoc.setExtractContent(extractContent);
        repositoryDoc.setErrorMsg(errorMsg);
        repositoryDocMapper.updateById(repositoryDoc);
    }

    @Override
    public Integer getRuleExtractId (Integer sourceId) {
        RepositoryDoc repositoryDoc = repositoryDocMapper.queryById(sourceId);
        return repositoryDoc.getRuleExtractId();
    }

    @Override
    public String getDocUrl(Integer sourceId) {
        RepositoryDoc repositoryDoc = repositoryDocMapper.queryById(sourceId);
        return repositoryDoc.getDocUrl();
    }

    @Override
    public ScheduleTaskSourceEnum getSourceType() {
        return ScheduleTaskSourceEnum.REPOSITORY;
    }
}

