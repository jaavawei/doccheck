package com.iwei.task.service;

import com.iwei.common.entity.PageResult;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.task.entity.ScheduleXj;
import com.iwei.task.entity.TaskDuplicateInfo;
import com.iwei.task.entity.vo.TaskDuplicateDocVo;
import com.iwei.task.entity.vo.TaskDuplicateInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 查重任务信息表（任务总表）service
 *
 * @auther: zhaokangwei
 */
public interface TaskDuplicateInfoService {

    /**
     * 查询查重任务列表
     */
    PageResult<TaskDuplicateInfoVo> queryTaskList(String taskName, Integer taskStatus, Integer dataType, Integer pageNo, Integer pageSize);

    /**
     * 逻辑删除查重任务
     */
    void deleteTask(TaskDuplicateInfoVo taskDuplicateInfoVo);

    /**
     * 新增查重任务
     */
    void addTask(TaskDuplicateInfoVo taskDuplicateInfoVo);

    /**
     * 查询任务详情
     */
    TaskDuplicateInfoVo queryTaskDetail(Integer id, Integer pageNo, Integer pageSize, Integer duplicateFlg, String projectCode,
                                        String projectName, String implOrg, String planYear, String projectMsg, Integer duplicateStatus);

    /**
     * 查询查重规则和查重库
     */
    Map<String, Object> queryDuplicateRuleAndRepository();

    /**
     * 查询子任务详情
     */
    TaskDuplicateDocVo querySubtaskDetail(Integer id);

    /**
     * 保存子任务
     */
    void saveSubtask(TaskDuplicateDocVo taskDuplicateDocVo);

    /**
     * 重新发起查重请求
     */
    void reduplicate(TaskDuplicateDocVo taskDuplicateDocVo);

    /**
     * 重新发起所有失败子任务
     */
    void reduplicateFailedTasks(TaskDuplicateInfoVo taskDuplicateInfoVo);

    /*
     * 分页查询任务详情
     */
    PageResult<RepositoryDocVo> queryTaskDetailPage(Integer id, String projectName, Integer pageNo, Integer pageSize);

    /**
     * 查询对比组查重任务详情
     */
    ScheduleXj queryGroupDetail(Integer id);

    /**
     * 导出查重结果
     */
    void exportDuplicateResult(Integer infoId, Integer range, Integer exportType);

}

