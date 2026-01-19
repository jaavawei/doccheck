package com.iwei.task.service.impl;

import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.CompliantFlgEnum;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.common.tool.BatchSplitUtil;
import com.iwei.repository.entity.RepositoryReview;
import com.iwei.repository.mapper.RepositoryReviewMapper;
import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.mapper.RuleReviewMapper;
import com.iwei.task.converter.TaskReviewInfoConverter;
import com.iwei.task.entity.ScheduleReview;
import com.iwei.task.entity.TaskReviewInfo;
import com.iwei.task.entity.TaskReviewRuleMapping;
import com.iwei.task.entity.vo.TaskReviewInfoVo;
import com.iwei.task.entity.vo.UncompliantItemVo;
import com.iwei.task.mapper.ScheduleReviewMapper;
import com.iwei.task.mapper.TaskReviewInfoMapper;
import com.iwei.task.mapper.TaskReviewRuleMappingMapper;
import com.iwei.task.service.TaskReviewInfoService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审查任务信息表service实现类
 *
 * @auther: zhaokangwei
 */
@Slf4j
@Service
public class TaskReviewInfoServiceImpl implements TaskReviewInfoService {

    @Resource
    private TaskReviewInfoMapper taskReviewInfoMapper;
    @Resource
    private TaskReviewRuleMappingMapper taskReviewRuleMappingMapper;
    @Resource
    private RepositoryReviewMapper repositoryReviewMapper;
    @Resource
    private RuleReviewMapper ruleReviewMapper;
    @Resource
    private ScheduleReviewMapper scheduleReviewMapper;

    @Override
    public TaskReviewInfo getById(Integer id) {
        return taskReviewInfoMapper.queryById(id);
    }

    /**
     * 新增审查任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTaskReview(TaskReviewInfoVo taskReviewInfoVo) {
        TaskReviewInfo taskReviewInfo = TaskReviewInfoConverter.INSTANCE.convertToEntity(taskReviewInfoVo);
        taskReviewInfo.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskReviewInfo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        taskReviewInfo.setRuleReviewId(taskReviewInfoVo.getRuleReviewIds().get(0));
        taskReviewInfoMapper.insert(taskReviewInfo);
        
        // 插入 审查任务-审查规则 映射表
        if (taskReviewInfoVo.getRuleReviewIds() != null && !taskReviewInfoVo.getRuleReviewIds().isEmpty()) {
            List<TaskReviewRuleMapping> ruleMappingList = new ArrayList<>();
            for (Integer ruleReviewId : taskReviewInfoVo.getRuleReviewIds()) {
                TaskReviewRuleMapping taskReviewRuleMapping = new TaskReviewRuleMapping();
                taskReviewRuleMapping.setRuleReviewId(ruleReviewId);
                taskReviewRuleMapping.setTaskReviewId(taskReviewInfo.getId());
                taskReviewRuleMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                ruleMappingList.add(taskReviewRuleMapping);
            }
            List<List<TaskReviewRuleMapping>> ruleMappingLists = BatchSplitUtil.splitList(ruleMappingList);
            for (List<TaskReviewRuleMapping> list : ruleMappingLists) {
                taskReviewRuleMappingMapper.batchInsert(list);
            }
        }

        // 插入定时任务表
        ScheduleReview scheduleReview = new ScheduleReview();
        scheduleReview.setReviewId(taskReviewInfo.getId());
        scheduleReview.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        scheduleReview.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        scheduleReviewMapper.insert(scheduleReview);
    }

    /**
     * 逻辑删除审查任务
     */
    @Override
    public void deleteTaskReview(TaskReviewInfoVo taskReviewInfoVo) {
        TaskReviewInfo taskReviewInfo = new TaskReviewInfo();
        taskReviewInfo.setId(taskReviewInfoVo.getId());
        taskReviewInfo.setDelFlg(DelFlgEnum.DELETED.getCode());
        taskReviewInfoMapper.updateById(taskReviewInfo);

        // 逻辑删除 审查任务-审查规则 映射表
        TaskReviewRuleMapping taskReviewRuleMapping = new TaskReviewRuleMapping();
        taskReviewRuleMapping.setTaskReviewId(taskReviewInfo.getId());
        taskReviewRuleMapping.setDelFlg(DelFlgEnum.DELETED.getCode());
        taskReviewRuleMappingMapper.updateByReviewId(taskReviewRuleMapping);

        // 逻辑删除定时任务表
        ScheduleReview scheduleReview = new ScheduleReview();
        scheduleReview.setReviewId(taskReviewInfo.getId());
        scheduleReview.setDelFlg(DelFlgEnum.DELETED.getCode());
        scheduleReviewMapper.updateByReviewId(scheduleReview);
    }

    /**
     * 查询审查任务列表
     */
    @Override
    public PageResult<TaskReviewInfoVo> queryTaskReviewList(String taskReviewName, Integer taskStatus, String projectName, Integer pageNo, Integer pageSize) {
        PageResult<TaskReviewInfoVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        TaskReviewInfo taskReviewInfo = new TaskReviewInfo();
        taskReviewInfo.setTaskReviewName(taskReviewName);
        taskReviewInfo.setTaskStatus(taskStatus);
        taskReviewInfo.setProjectName(projectName);
        taskReviewInfo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = taskReviewInfoMapper.countByCondition(taskReviewInfo);
        if (total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<TaskReviewInfo> list = taskReviewInfoMapper.queryPageByCondition(taskReviewInfo, pageSize, offset);
        List<TaskReviewInfoVo> voList = TaskReviewInfoConverter.INSTANCE.convertToVoList(list);
        for (TaskReviewInfoVo taskReviewInfoVo : voList) {
            String repoName = repositoryReviewMapper.queryById(taskReviewInfoVo.getRepositoryReviewId()).getRepositoryReviewName();
            String ruleName = ruleReviewMapper.queryById(taskReviewInfoVo.getRuleReviewId()).getRuleName();
            List<String> ruleNames = new ArrayList<>();
            ruleNames.add(ruleName);
            taskReviewInfoVo.setRepositoryReviewName(repoName);
            taskReviewInfoVo.setRuleReviewNames(ruleNames);
        }

        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

    /**
     * 查询审查库和审查规则
     */
    @Override
    public Map<String, Object> queryReviewRuleAndRepository() {
        Map<String, Object> result = new HashMap<>();
        List<RuleReview> ruleReviews = ruleReviewMapper.queryIdAndName();
        List<RepositoryReview> repositoryReviews = repositoryReviewMapper.queryIdAndName();
        result.put("ruleReviews", ruleReviews);
        result.put("repositoryReviews", repositoryReviews);
        return result;
    }

    /**
     * 查询审查任务详情
     */
    @Override
    public TaskReviewInfoVo queryReviewTaskDetail(Integer id) {
        TaskReviewInfo taskReviewInfo = taskReviewInfoMapper.queryById(id);
        TaskReviewInfoVo taskReviewInfoVo = new TaskReviewInfoVo();
        taskReviewInfoVo.setId(id);
        taskReviewInfoVo.setReviewResult(taskReviewInfo.getReviewResult());
//        TaskReviewInfoVo taskReviewInfoVo = new TaskReviewInfoVo();
//
//        TaskReviewRuleMapping taskReviewRuleMapping = new TaskReviewRuleMapping();
//        // 审查项数量
//        taskReviewRuleMapping.setTaskReviewId(id);
//        Integer itemCount = taskReviewRuleMappingMapper.countByCondition(taskReviewRuleMapping);
//        // 不合规项数量
//        taskReviewRuleMapping.setCompliantFlg(CompliantFlgEnum.UN_COMPLIANT.getCode());
//        Integer uncompliantCount = taskReviewRuleMappingMapper.countByCondition(taskReviewRuleMapping);
//        // 合规项数量
//        taskReviewRuleMapping.setCompliantFlg(CompliantFlgEnum.COMPLIANT.getCode());
//        Integer compliantCount = taskReviewRuleMappingMapper.countByCondition(taskReviewRuleMapping);
//        // 合规率
//        Double complianceRate = itemCount > 0 ? (double) compliantCount / itemCount * 100 : 0.0;
//
//        // 不合规项信息
//        List<TaskReviewRuleMapping> TaskReviewRuleMappingList = taskReviewRuleMappingMapper.queryUncompliantItemByReviewId(id);
//        List<UncompliantItemVo> uncompliantItems = new ArrayList<>();
//        for (TaskReviewRuleMapping mapping : TaskReviewRuleMappingList) {
//            UncompliantItemVo uncompliantItemVo = new UncompliantItemVo();
//            uncompliantItemVo.setRuleReviewName(ruleReviewMapper.queryById(mapping.getRuleReviewId()).getRuleName());
//            uncompliantItemVo.setQuestionMsg(mapping.getQuestionMsg());
//            uncompliantItemVo.setAdvice(mapping.getAdvice());
//            uncompliantItems.add(uncompliantItemVo);
//         }
//
//        // 封装数据
//        taskReviewInfoVo.setId(id);
//        taskReviewInfoVo.setTaskStatus(taskReviewInfoMapper.queryById(id).getTaskStatus());
//        taskReviewInfoVo.setItemCount(itemCount);
//        taskReviewInfoVo.setComplianceRate(complianceRate + "%");
//        taskReviewInfoVo.setUncompliantCount(uncompliantCount);
//        taskReviewInfoVo.setUncompliantItems(uncompliantItems);

        return taskReviewInfoVo;
    }

}