package com.iwei.schedule.strategy;

import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.ScheduleTaskSourceEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.mapper.RepositoryDuplicateMapper;
import com.iwei.task.entity.ScheduleDuplicate;
import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.mapper.ScheduleDuplicateMapper;
import com.iwei.task.mapper.TaskDuplicateDocMapper;
import com.iwei.task.mapper.TaskDuplicateFileMapper;
import javax.annotation.Resource;

public class TaskDuplicateProcessorStrategy implements ExtractTaskProcessorStrategy {
    @Resource
    private TaskDuplicateDocMapper taskDuplicateDocMapper;
    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    private TaskDuplicateFileMapper taskDuplicateFileMapper;
    @Resource
    private ScheduleDuplicateMapper scheduleDuplicateMapper;

    @Override
    public void updateSourceStatus(Integer sourceId, TaskStatusEnum status, String extractContent, String errorMsg) {
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        taskDuplicateDoc.setId(sourceId);
        taskDuplicateDoc.setTaskStatus(status.getCode());
        taskDuplicateDoc.setExtractContent(extractContent);
        taskDuplicateDoc.setErrorMsg(errorMsg);
        taskDuplicateDocMapper.updateById(taskDuplicateDoc);

        if (status == TaskStatusEnum.COMPLETED) {
            ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
            scheduleDuplicate.setDocId(sourceId);
            scheduleDuplicate.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
            scheduleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            scheduleDuplicateMapper.insert(scheduleDuplicate);
        }
    }

    @Override
    public Integer getRuleExtractId(Integer sourceId) {
        Integer duplicateId = taskDuplicateDocMapper.queryRepositoryDuplicateId(sourceId);
        RepositoryDuplicate repositoryDuplicate = repositoryDuplicateMapper.queryById(duplicateId);
        return repositoryDuplicate.getRuleExtractId();
    }

    @Override
    public String getDocUrl(Integer sourceId) {
        return taskDuplicateFileMapper.queryUrlByDocId(sourceId);
    }

    @Override
    public ScheduleTaskSourceEnum getSourceType() {
        return ScheduleTaskSourceEnum.TASK;
    }
}
