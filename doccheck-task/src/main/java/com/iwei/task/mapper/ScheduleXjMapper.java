package com.iwei.task.mapper;

import com.iwei.task.entity.ScheduleXj;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
 * 新疆任务调度
 *
 * @author:zhaokangwei
 */
public interface ScheduleXjMapper {
    
    ScheduleXj selectById(Integer id);
    
    List<ScheduleXj> selectAll();
    
    int insert(ScheduleXj scheduleXj);

    int update(ScheduleXj scheduleXj);

    int deleteById(Integer id);

    List<ScheduleXj> getPendingTasks(int batchSize);

    List<Integer> queryDistinctTaskStatus(Integer sourceId);

    Integer countBySourceId(Integer id);

    Integer countDuplicateBySourceId(Integer id);

    Integer countDuplicateMsgBySourceId(Integer sourceId, String duplicateMsg);

    List<ScheduleXj> queryByEitherDocIdAndSourceId(Integer docId, Integer sourceId);

    Integer countDuplicateByDocIdAndSourceId(Integer docId, Integer sourceId);

    List<Object> queryOrgConditionBySourceId(Integer id);

    void batchInsert(List<ScheduleXj> scheduleXjList);

    Integer countByDocId(Integer id);

    Integer countByDocIdAndStatus(Integer docId, int status);

    List<ScheduleXj> queryAll();

    void updateByInfoId(Integer infoId, ScheduleXj scheduleXj);
}
