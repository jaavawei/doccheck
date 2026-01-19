package com.iwei.task.mapper;

import com.iwei.task.entity.TaskDuplicateSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查重任务快照表 mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateSnapshotMapper {
    /**
     * 插入单条记录
     */
    int insert(TaskDuplicateSnapshot record);

    /**
     * 批量插入
     */
    int batchInsert(List<TaskDuplicateSnapshot> list);

    /**
     * 根据infoId查询
     */
    TaskDuplicateSnapshot queryByInfoId(Integer infoId);
}
