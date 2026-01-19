package com.iwei.task.mapper;

import com.iwei.task.entity.TaskDuplicateDoc;

import java.util.List;

/**
 * 查重任务文档表（子任务表）mapper
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateDocMapper {

    /**
     * 根据id查询
     */
    TaskDuplicateDoc queryById(Integer id);


    int insert(TaskDuplicateDoc taskDuplicateDoc);

    /**
     * 根据id更新
     */
    int updateById(TaskDuplicateDoc taskDuplicateDoc);


    /**
     * 根据主任务 id 更新
     */
    void updateByInfoId(TaskDuplicateDoc taskDuplicateDoc);

    /**
     * 批量插入子任务
     */
    void batchInsert(List<TaskDuplicateDoc> docList);

    /**
     * 根据 infoId 计数
     */
    int countByInfoId(Integer infoId);

    /**
     * 根据 InfoId 查询
     */
    List<TaskDuplicateDoc> queryByInfoId(Integer id);

    /**
     * 查询提取规则id
     */
    Integer queryRepositoryDuplicateId(Integer id);

    /**
     * 根据 infoId 查询子任务状态
     */
    List<Integer> queryDocStatusByInfoId(Integer infoId);

    /**
     * 根据 infoId 和 repoDocId 查询子任务
     */
    TaskDuplicateDoc queryByInfoIdAndRepoDocId(Integer infoId, Integer repoDocId);

    /**
     * 根据条件查询
     */
    List<TaskDuplicateDoc> queryByCondition();
}

