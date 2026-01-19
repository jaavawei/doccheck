package com.iwei.task.mapper;

import com.iwei.task.entity.TaskDuplicateFile;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 查重任务文件表 mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateFileMapper {
    /**
     * 插入单条记录
     */
    int insert(TaskDuplicateFile record);

    /**
     * 批量插入
     */
    int batchInsert(List<TaskDuplicateFile> list);

    /**
     * 根据id查询
     */
    TaskDuplicateFile queryById(Integer fileId);

    /**
     * 根据docId查询url
     */
    String queryUrlByDocId(Integer docId);
}
