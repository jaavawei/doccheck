package com.iwei.task.mapper;

import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.entity.TaskDuplicateInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查重任务信息表（任务总表）mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateInfoMapper {

    /**
     * 根据id查询
     */
    TaskDuplicateInfo queryById(Integer id);

    /**
     * 插入新纪录
     */
    int insert(TaskDuplicateInfo taskDuplicateInfo);

    /**
     * 根据id更新
     */
    int updateById(TaskDuplicateInfo taskDuplicateInfo);


    /**
     * 根据条件计数
     */
    int countByCondition(TaskDuplicateInfo taskDuplicateInfo);

    /**
     * 根据条件分页查询
     */
    List<TaskDuplicateInfo> queryPageByCondition(
            @Param("taskDuplicateInfo") TaskDuplicateInfo taskDuplicateInfo,
            @Param("pageSize") Integer pageSize,
            @Param("offset") Integer offset
    );

    /*
     * 查询 like 任务名
     */
    List<String> queryLikeTaskNames(String taskDuplicateName);
}
