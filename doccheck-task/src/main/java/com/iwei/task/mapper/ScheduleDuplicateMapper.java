package com.iwei.task.mapper;

import com.iwei.task.entity.ScheduleDuplicate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 查重任务定时任务表 mapper
 *
 * @auther: zhaokangwei
 */
public interface ScheduleDuplicateMapper {

    /**
     * 插入单条记录
     */
    int insert(ScheduleDuplicate record);

    /**
     * 批量插入
     */
    int batchInsert(List<ScheduleDuplicate> list);

    /**
     * 根据 docId 查询
     */
    List<ScheduleDuplicate> queryByDocId(Integer docId);

    /**
     * 根据任务状态查询
     */
    List<ScheduleDuplicate> queryByTaskStatus(Integer taskStatus);

    /**
     * 根据条件查询
     */
    List<ScheduleDuplicate> queryByCondition(ScheduleDuplicate record);

    /**
     * 获取待执行任务
     */
    List<ScheduleDuplicate> getPendingTasks(int batchSize);

    /**
     * 根据 id 更新
     */
    boolean update(ScheduleDuplicate scheduleduplicate);

    /**
     * 更新 docId 更新
     */
    void updateByDocId(ScheduleDuplicate scheduleDuplicate);

    /**
     * 根据 infoId 更新
     */
    void updateByInfoId(Integer infoId, ScheduleDuplicate scheduleDuplicate);

    /**
     * 根据 docId 列表重试
     */
    void retryByDocIdList(List<Integer> docIdList);
}
