package com.iwei.task.service.impl;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.*;
import com.iwei.common.tool.*;
import com.iwei.oss.service.FileService;
import com.iwei.repository.convert.RepositoryDocConverter;
import com.iwei.repository.convert.RepositoryDuplicateConverter;
import com.iwei.repository.entity.*;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import com.iwei.repository.mapper.*;
import com.iwei.rule.convert.RuleDuplicateConverter;
import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.entity.vo.RuleDuplicateVo;
import com.iwei.rule.mapper.RuleDuplicateMapper;
import com.iwei.task.converter.TaskDuplicateDocConverter;
import com.iwei.task.converter.TaskDuplicateInfoConverter;
import com.iwei.task.entity.*;
import com.iwei.task.entity.vo.ScheduleXjVo;
import com.iwei.task.entity.vo.TaskDuplicateDocVo;
import com.iwei.task.entity.vo.TaskDuplicateInfoVo;
import com.iwei.task.mapper.*;
import com.iwei.task.service.TaskDuplicateInfoService;
import io.minio.messages.Rule;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.shadow.org.terracotta.offheapstore.paging.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.context.AnalysisContext;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 查重任务信息表（任务总表）service实现类
 *
 * @auther: zhaokangwei
 */
@Service
@Slf4j
public class TaskDuplicateInfoServiceImpl implements TaskDuplicateInfoService {

    @Resource
    private TaskDuplicateInfoMapper taskDuplicateInfoMapper;
    @Resource
    private TaskDuplicateDocMapper taskDuplicateDocMapper;
    @Resource
    private TaskDuplicateFileMapper taskDuplicateFileMapper;
    @Resource
    private TaskDuplicateSnapshotMapper taskDuplicateSnapshotMapper;
    @Resource
    private TaskDuplicateResultMapper taskDuplicateResultMapper;
    @Resource
    private RepositoryDocMapper repositoryDocMapper;
    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    private ScheduleExtractMapper scheduleExtractMapper;
    @Resource
    private ScheduleDuplicateMapper scheduleDuplicateMapper;
    @Resource
    private RuleDuplicateMapper ruleDuplicateMapper;
    @Resource
    private ScheduleXjMapper scheduleXjMapper;
    @Resource
    private ProjectStationLineMappingMapper projectStationLineMappingMapper;
    @Resource
    private StationLineMapper stationLineMapper;
    @Resource
    private FileService fileService;
    @Resource
    private DeviceMapper deviceMapper;


    /**
     * 分页查重查重任务列表
     */
    @Override
    public PageResult<TaskDuplicateInfoVo> queryTaskList(String taskName, Integer taskStatus, Integer dataType, Integer pageNo, Integer pageSize) {
        PageResult<TaskDuplicateInfoVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);


        TaskDuplicateInfo taskDuplicateInfo = new TaskDuplicateInfo();
        taskDuplicateInfo.setTaskDuplicateName(taskName);
        taskDuplicateInfo.setTaskStatus(taskStatus);
        taskDuplicateInfo.setDataType(dataType);
        taskDuplicateInfo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = taskDuplicateInfoMapper.countByCondition(taskDuplicateInfo);
        if(total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize(); // 偏移量
        List<TaskDuplicateInfo> list = taskDuplicateInfoMapper.queryPageByCondition(taskDuplicateInfo, pageResult.getPageSize(), offset);
        List<TaskDuplicateInfoVo> voList = TaskDuplicateInfoConverter.INSTANCE.convertListToVoList(list);

        for (TaskDuplicateInfoVo taskDuplicateInfoVo : voList) {
            RuleDuplicate ruleDuplicate = ruleDuplicateMapper.queryById(taskDuplicateInfoVo.getRuleDuplicateId());
            RepositoryDuplicate repositoryDuplicate = repositoryDuplicateMapper.queryById(taskDuplicateInfoVo.getRepositoryDuplicateId());
            if (ruleDuplicate == null || repositoryDuplicate == null) {
                taskDuplicateInfoVo.setTaskStatus(TaskStatusEnum.INVALID.getCode());
                continue;
            }
            String ruleDuplicateName = ruleDuplicate.getRuleName();
            String repositoryDuplicateName = repositoryDuplicate.getRepositoryDuplicateName();
            taskDuplicateInfoVo.setRuleDuplicateName(ruleDuplicateName);
            taskDuplicateInfoVo.setRepositoryDuplicateName(repositoryDuplicateName);
            if (taskDuplicateInfoVo.getTaskStatus() != TaskStatusEnum.COMPLETED.getCode() ||
                    taskDuplicateInfoVo.getTaskStatus() != TaskStatusEnum.FAILED.getCode()) {
                // 主任务的状态不为已完成或已失败，需要根据子任务状态进行更新
                Integer infoStatus = getInfoTaskStatus(taskDuplicateInfoVo);
                taskDuplicateInfoVo.setTaskStatus(infoStatus);
                // 更新数据表
                taskDuplicateInfo.setTaskStatus(infoStatus);
                taskDuplicateInfoMapper.updateById(taskDuplicateInfo);
            }
        }
        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

    /**
     * 查询主任务状态
     */
    private Integer getInfoTaskStatus(TaskDuplicateInfoVo taskDuplicateInfoVo) {

        if(taskDuplicateInfoVo.getDuplicateType() == 1) {
            // 新疆多文件查重任务，特殊处理
            List<Integer> statuses = scheduleXjMapper.queryDistinctTaskStatus(taskDuplicateInfoVo.getId());
            if(statuses.size() == 1) {
                return statuses.get(0);
            }
            if (statuses.size() == 2) {
                if(statuses.contains(TaskStatusEnum.EXECUTING.getCode()) || statuses.contains(TaskStatusEnum.UNEXECUTED.getCode())) {
                    return TaskStatusEnum.EXECUTING.getCode();
                }
                return TaskStatusEnum.FAILED.getCode();
            }
            return TaskStatusEnum.EXECUTING.getCode();
        }


        boolean allUnexecuted = true;
        boolean allCompleted = true;
        boolean hasExecuting = false;
        boolean hasFailed = false;

        // 查询所有子任务状态
        List<Integer> statusList = taskDuplicateDocMapper.queryDocStatusByInfoId(taskDuplicateInfoVo.getId());
        for (Integer status : statusList) {
            if (status == TaskStatusEnum.UNEXECUTED.getCode()) {
                allCompleted = false;
            } else if (status == TaskStatusEnum.EXECUTING.getCode()) {
                allUnexecuted = false;
                allCompleted = false;
                hasExecuting = true;
            } else if (status == TaskStatusEnum.COMPLETED.getCode()) {
                allUnexecuted = false;
            } else if (status == TaskStatusEnum.FAILED.getCode()) {
                allUnexecuted = false;
                allCompleted = false;
                hasFailed = true;
            }
        }

        if (allUnexecuted) {
            // 子任务全未执行，主任务状态为未执行
            return TaskStatusEnum.UNEXECUTED.getCode();
        } else if (allCompleted) {
            // 子任务全已完成，主任务状态为已完成
            return TaskStatusEnum.COMPLETED.getCode();
        } else if (hasExecuting) {
            // 否则若存在还在执行的子任务，主任务状态为执行中
            return TaskStatusEnum.EXECUTING.getCode();
        } else if (hasFailed) {
            // 否则若存在失败的子任务，主任务状态为失败
            return TaskStatusEnum.FAILED.getCode();
        } else {
            // 所有任务状态均为未执行和已完成
            return TaskStatusEnum.EXECUTING.getCode();
        }
}


    /**
     * 删除查重任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(TaskDuplicateInfoVo taskDuplicateInfoVo) {
        Integer id = taskDuplicateInfoVo.getId();

        // 逻辑删除主任务
        TaskDuplicateInfo taskDuplicateInfo = new TaskDuplicateInfo();
        taskDuplicateInfo.setId(id);
        taskDuplicateInfo.setDelFlg(DelFlgEnum.DELETED.getCode()); // 标记为删除
        taskDuplicateInfoMapper.updateById(taskDuplicateInfo);

        // 逻辑删除子任务
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        taskDuplicateDoc.setInfoId(id);
        taskDuplicateDoc.setDelFlg(DelFlgEnum.DELETED.getCode());
        taskDuplicateDocMapper.updateByInfoId(taskDuplicateDoc);

        // 删除定时任务
        ScheduleExtract scheduleExtract = new ScheduleExtract();
        scheduleExtract.setSourceId(id);
        scheduleExtract.setDelFlg(DelFlgEnum.DELETED.getCode());
        scheduleExtractMapper.updateBySourceId(scheduleExtract);
        ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
        scheduleDuplicate.setDelFlg(DelFlgEnum.DELETED.getCode());
        scheduleDuplicateMapper.updateByInfoId(id, scheduleDuplicate);
        ScheduleXj scheduleXj = new ScheduleXj();
        scheduleXj.setDelFlg(DelFlgEnum.DELETED.getCode());
        scheduleXjMapper.updateByInfoId(id, scheduleXj);

    }

    /**
     * 新增查重任务
     * 解析、提取与查重均交给定时任务处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTask(TaskDuplicateInfoVo taskDuplicateInfoVo) {
        TaskDuplicateInfo taskDuplicateInfo = TaskDuplicateInfoConverter.INSTANCE.convertVoToTaskDuplicateInfo(taskDuplicateInfoVo);
        taskDuplicateInfo.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateInfo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        // 确保查重任务名不重复
        List<String> taskNames = taskDuplicateInfoMapper.queryLikeTaskNames(taskDuplicateInfoVo.getTaskDuplicateName());
        String newName = FileNameUtil.generateUniqueFileName(taskDuplicateInfoVo.getTaskDuplicateName(), taskNames);
        taskDuplicateInfo.setTaskDuplicateName(newName);
        // 先插入主任务表中公共字段
        taskDuplicateInfoMapper.insert(taskDuplicateInfo);
        if (taskDuplicateInfoVo.getDuplicateType() == 1) {
            if (taskDuplicateInfoVo.getDuplicateRange() == MultiDuplicateEnum.STATION_LINE.getCode()) {
                addStationLineTask(taskDuplicateInfo);
            }
            if (taskDuplicateInfoVo.getDuplicateRange() == MultiDuplicateEnum.DEVICE.getCode()) {
                addDeviceTask(taskDuplicateInfo);
            }
            return;
        }
        List<RepositoryDoc> repositoryDocs = repositoryDuplicateMapper.queryDocByDuplicateId(taskDuplicateInfo.getRepositoryDuplicateId());
        // 创建查重规则快照和查重库快照并插入快照表
        List<Integer> repoDocIds = repositoryDocs.stream().map(RepositoryDoc::getId).collect(Collectors.toList());
        String snapshotRepo = JSON.toJSONString(repoDocIds);
        String snapshotRule = ruleDuplicateMapper.queryById(taskDuplicateInfo.getRuleDuplicateId()).getContent();
        TaskDuplicateSnapshot taskDuplicateSnapshot = new TaskDuplicateSnapshot();
        taskDuplicateSnapshot.setInfoId(taskDuplicateInfo.getId());
        taskDuplicateSnapshot.setSnapshotRule(snapshotRule); // 查出规则快照
        taskDuplicateSnapshot.setSnapshotRepo(snapshotRepo); // 查重库快照
        taskDuplicateSnapshot.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        taskDuplicateSnapshotMapper.insert(taskDuplicateSnapshot);

        // 根据数据类型解析并处理子任务
        List<TaskDuplicateDoc> taskDuplicateDocList;
        Integer dataType = taskDuplicateInfoVo.getDataType();
        taskDuplicateInfoVo.setId(taskDuplicateInfo.getId());
        if(dataType == DataTypeEnum.STRUCTURED.getCode()) {
            // 从一个Excel文件中提取子任务
            taskDuplicateDocList = parseStructuredDoc(taskDuplicateInfoVo);
        } else {
            // 解析多个文档并生成子任务
            taskDuplicateDocList = parseUnstructuredDocs(taskDuplicateInfoVo);
        }
        // 批量插入子任务表
        List<List<TaskDuplicateDoc>> taskDuplicateDocLists = BatchSplitUtil.splitList(taskDuplicateDocList);
        for (List<TaskDuplicateDoc> list : taskDuplicateDocLists) {
            taskDuplicateDocMapper.batchInsert(list);
        }

        if (dataType == DataTypeEnum.STRUCTURED.getCode()) {
            // 结构化文档无需提取，直接存入查重定时任务表，非结构化文档则等待解析提取任务结束后再存入
            List<ScheduleDuplicate> scheduleDuplicateList = new ArrayList<>();
            for (TaskDuplicateDoc taskDuplicateDoc : taskDuplicateDocList) {
                ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
                scheduleDuplicate.setDocId(taskDuplicateDoc.getId());
                scheduleDuplicate.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                scheduleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                scheduleDuplicateList.add(scheduleDuplicate);
            }
            List<List<ScheduleDuplicate>> scheduleDuplicateLists = BatchSplitUtil.splitList(scheduleDuplicateList);
            for (List<ScheduleDuplicate> list : scheduleDuplicateLists) {
                scheduleDuplicateMapper.batchInsert(list);
            }
        } else {
            // 批量插入提取定时任务表
            List<ScheduleExtract> scheduleExtractList = new ArrayList<>();
            for (TaskDuplicateDoc taskDuplicateDoc : taskDuplicateDocList) {
                ScheduleExtract scheduleExtract = new ScheduleExtract();
                scheduleExtract.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                scheduleExtract.setSourceId(taskDuplicateDoc.getId());
                // 通过任务来源区分是文档库提取任务还是定时任务提取任务
                scheduleExtract.setTaskSource(ScheduleTaskSourceEnum.TASK.getCode());
                scheduleExtract.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                scheduleExtractList.add(scheduleExtract);
            }
            List<List<ScheduleExtract>> scheduleExtractLists = BatchSplitUtil.splitList(scheduleExtractList);
            for (List<ScheduleExtract> list : scheduleExtractLists) {
                scheduleExtractMapper.batchInsert(list);
            }
        }
    }

    /**
     * 新增按站线查重任务
     */
    @Transactional(rollbackFor = Exception.class)
    private void addStationLineTask(TaskDuplicateInfo taskDuplicateInfo) {
        // 根据查重库id查询出所有站线名称 查重库-文档库-站线
        List<StationLine> stationLineList = stationLineMapper.queryByRepoDuplicateId(taskDuplicateInfo.getRepositoryDuplicateId());
        // 遍历每一个站线名称，将查重库里该站线名称下的所有文档两两查重
        List<ScheduleXj> scheduleXjList = new ArrayList<>();
        Set<TaskDuplicateDoc> taskDuplicateDocSet = new HashSet<>();
        for (StationLine stationLine : stationLineList) {
            // 根据查重库名称和站线名称查询出所有文档
            List<RepositoryDoc> docList = stationLineMapper.queryByStationIdAndRepoDuplicateId(stationLine.getId(), taskDuplicateInfo.getRepositoryDuplicateId());
            for (int i = 0; i < docList.size(); i++) {
                for (int j = i + 1; j < docList.size(); j++) {
                    ScheduleXj scheduleXj = new ScheduleXj();
                    RepositoryDoc docF = docList.get(i);
                    RepositoryDoc docS = docList.get(j);
                    scheduleXj.setDocFId(docF.getId());
                    scheduleXj.setDocSId(docS.getId());
                    scheduleXj.setStationLineName(stationLine.getStationLineName());
                    scheduleXj.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                    scheduleXj.setSourceId(taskDuplicateInfo.getId());
                    scheduleXj.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                    scheduleXjList.add(scheduleXj);

                    TaskDuplicateDoc taskDoc1 = doc2taskDoc(docF, taskDuplicateInfo.getId());
                    TaskDuplicateDoc taskDoc2 = doc2taskDoc(docS, taskDuplicateInfo.getId());
                    taskDuplicateDocSet.add(taskDoc1);
                    taskDuplicateDocSet.add(taskDoc2);
                }
            }
        }
        List<List<ScheduleXj>> scheduleXjListList = BatchSplitUtil.splitList(scheduleXjList);
        for (List<ScheduleXj> list : scheduleXjListList) {
            scheduleXjMapper.batchInsert(list);
        }
        List<TaskDuplicateDoc> taskDuplicateDocList = new ArrayList<>(taskDuplicateDocSet);
        // 在 task_duplicate_doc 表中插入所有 doc 记录，只有创建了查重对的 doc 才会被记录
        List<List<TaskDuplicateDoc>> taskDuplicateDocLists = BatchSplitUtil.splitList(taskDuplicateDocList);
        for (List<TaskDuplicateDoc> list : taskDuplicateDocLists) {
            taskDuplicateDocMapper.batchInsert(list);
        }
    }

    /**
     * 新增按设备查重任务
     */
    @Transactional(rollbackFor = Exception.class)
    private void addDeviceTask(TaskDuplicateInfo taskDuplicateInfo) {
        // 根据查重库id查询出所有设备名称 查重库-文档库-设备
        List<Device> deviceList = deviceMapper.queryByRepoDuplicateId(taskDuplicateInfo.getRepositoryDuplicateId());
        // 遍历每一个设备名称，将查重库里该设备名称下的所有文档两两查重
        List<ScheduleXj> scheduleXjList = new ArrayList<>();
        Set<TaskDuplicateDoc> taskDuplicateDocSet = new HashSet<>();
        for (Device device : deviceList) {
            // 根据查重库名称和设备名称查询出所有文档
            List<RepositoryDoc> docList = deviceMapper.queryByDeviceIdAndRepoDuplicateId(device.getId(), taskDuplicateInfo.getRepositoryDuplicateId());
            for (int i = 0; i < docList.size(); i++) {
                for (int j = i + 1; j < docList.size(); j++) {
                    ScheduleXj scheduleXj = new ScheduleXj();
                    RepositoryDoc docF = docList.get(i);
                    RepositoryDoc docS = docList.get(j);
                    scheduleXj.setDocFId(docF.getId());
                    scheduleXj.setDocSId(docS.getId());
                    scheduleXj.setStationLineName(device.getDeviceName());
                    scheduleXj.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                    scheduleXj.setSourceId(taskDuplicateInfo.getId());
                    scheduleXj.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                    scheduleXjList.add(scheduleXj);

                    TaskDuplicateDoc taskDoc1 = doc2taskDoc(docF, taskDuplicateInfo.getId());
                    TaskDuplicateDoc taskDoc2 = doc2taskDoc(docS, taskDuplicateInfo.getId());
                    taskDuplicateDocSet.add(taskDoc1);
                    taskDuplicateDocSet.add(taskDoc2);
                }
            }
        }
        List<List<ScheduleXj>> scheduleXjListList = BatchSplitUtil.splitList(scheduleXjList);
        for (List<ScheduleXj> list : scheduleXjListList) {
            scheduleXjMapper.batchInsert(list);
        }
        List<TaskDuplicateDoc> taskDuplicateDocList = new ArrayList<>(taskDuplicateDocSet);
        // 在 task_duplicate_doc 表中插入所有 doc 记录，只有创建了查重对的 doc 才会被记录
        List<List<TaskDuplicateDoc>> taskDuplicateDocLists = BatchSplitUtil.splitList(taskDuplicateDocList);
        for (List<TaskDuplicateDoc> list : taskDuplicateDocLists) {
            taskDuplicateDocMapper.batchInsert(list);
        }
    }

    private TaskDuplicateDoc doc2taskDoc(RepositoryDoc doc, Integer infoId) {
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        taskDuplicateDoc.setInfoId(infoId);
        taskDuplicateDoc.setDocName(doc.getDocName());
        taskDuplicateDoc.setRepoDocId(doc.getId());
        taskDuplicateDoc.setDuplicateFlg(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateDoc.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateDoc.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        return taskDuplicateDoc;
    }

    /**
     * 查询任务详情
     */
    @Override
    public TaskDuplicateInfoVo queryTaskDetail(Integer id, Integer pageNo, Integer pageSize, Integer duplicateFlg, String projectCode,
                                               String projectName, String implQrg, String planYear, String projectMsg, Integer duplicateStatus) {
        TaskDuplicateInfo taskDuplicateInfo = taskDuplicateInfoMapper.queryById(id);
        if(taskDuplicateInfo.getDuplicateType() == 1) {
            return getXjTaskDetail(taskDuplicateInfo, pageNo, pageSize, duplicateFlg, projectCode, projectName, implQrg, planYear, projectMsg, duplicateStatus);
        }
        TaskDuplicateInfoVo taskDuplicateInfoVo = new TaskDuplicateInfoVo();
        // 查询子任务数量
        int taskNum = taskDuplicateDocMapper.countByInfoId(id);
        // 查询所有子任务详情，并处理返回结果
        List<TaskDuplicateDoc> subtaskList = taskDuplicateDocMapper.queryByInfoId(id);
        List<TaskDuplicateDocVo> taskDuplicateDocVoList = new ArrayList<>();
        int toDoNum = 0, doingNum = 0, doneNum = 0, failNum = 0;
        for (TaskDuplicateDoc taskDuplicateDoc : subtaskList) {
            Integer taskStatus = taskDuplicateDoc.getTaskStatus();
            if (taskStatus == TaskStatusEnum.UNEXECUTED.getCode()) {
                toDoNum++;
            }
            if (taskStatus == TaskStatusEnum.EXECUTING.getCode()) {
                doingNum++;
            }
            if (taskStatus == TaskStatusEnum.COMPLETED.getCode()) {
                doneNum++;
            }
            if (taskStatus == TaskStatusEnum.FAILED.getCode()) {
                failNum++;
            }
            TaskDuplicateDocVo taskDuplicateDocVo = new TaskDuplicateDocVo();
            taskDuplicateDocVo.setId(taskDuplicateDoc.getId());
            taskDuplicateDocVo.setDocName(taskDuplicateDoc.getDocName());
            taskDuplicateDocVo.setTaskStatus(taskDuplicateDoc.getTaskStatus());
//            taskDuplicateDocVo.setExtractContent(taskDuplicateDoc.getExtractContent());
//            TaskDuplicateResult taskDuplicateResult = taskDuplicateResultMapper.queryById(taskDuplicateDoc.getResultId());
//            if (taskDuplicateResult != null) {
//                taskDuplicateDocVo.setDuplicateResult(taskDuplicateResult.getDuplicateResult());
//            }
            taskDuplicateDocVoList.add(taskDuplicateDocVo);
        }
        taskDuplicateInfoVo.setToDoNum(toDoNum);
        taskDuplicateInfoVo.setDoingNum(doingNum);
        taskDuplicateInfoVo.setDoneNum(doneNum);
        taskDuplicateInfoVo.setFailNum(failNum);
        taskDuplicateInfoVo.setTaskNum(taskNum);
        taskDuplicateInfoVo.setSubtasks(taskDuplicateDocVoList);
        return taskDuplicateInfoVo;
    }

    /**
     * 查询 xj 任务详情
     */
    TaskDuplicateInfoVo getXjTaskDetail(TaskDuplicateInfo taskDuplicateInfo, Integer pageNo, Integer pageSize, Integer duplicateFlg, String projectCode,
                                        String projectName, String implOrg, String planYear, String projectMsg, Integer duplicateStatus) {
        TaskDuplicateInfoVo taskDuplicateInfoVo = new TaskDuplicateInfoVo();
        taskDuplicateInfoVo.setId(taskDuplicateInfo.getId());

        // 获取总对比组数
        Integer totalGroupNum = scheduleXjMapper.countBySourceId(taskDuplicateInfo.getId());
        // 获取重复组数
        Integer duplicateGroupNum = scheduleXjMapper.countDuplicateBySourceId(taskDuplicateInfo.getId());
        Double duplicatePercent = (double)duplicateGroupNum / (double)totalGroupNum * 100;
        Map<String, Integer> duplicateMsgMap = new HashMap<>();

        // 重复具体情况
        Integer msgCount = scheduleXjMapper.countDuplicateMsgBySourceId(taskDuplicateInfo.getId(), "重复申报");
        duplicateMsgMap.put("重复申报", msgCount);
        msgCount= scheduleXjMapper.countDuplicateMsgBySourceId(taskDuplicateInfo.getId(), "多头申报");
        duplicateMsgMap.put("多头申报", msgCount);
        msgCount= scheduleXjMapper.countDuplicateMsgBySourceId(taskDuplicateInfo.getId(), "重复改造");
        duplicateMsgMap.put("重复改造", msgCount);

         // 各单位重复情况
         // List<Map<String, Double>> orgCondition = new ArrayList<>();
        Object orgCondition = scheduleXjMapper.queryOrgConditionBySourceId(taskDuplicateInfo.getId());
        log.info("orgConditionMap:{}", orgCondition);
        taskDuplicateInfoVo.setOrgCondition(orgCondition);
        taskDuplicateInfoVo.setTotalGroupNum(totalGroupNum);
        taskDuplicateInfoVo.setDuplicateGroupNum(duplicateGroupNum);
        taskDuplicateInfoVo.setDuplicatePercent(duplicatePercent);
        taskDuplicateInfoVo.setDuplicateMsgMap(duplicateMsgMap);

        pageSize = pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize;
        pageNo = pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo;
        Integer offset = (pageNo - 1) * pageSize;
        // 封装分页信息
        Integer total = repositoryDuplicateMapper.countByRepoDupId(taskDuplicateInfo.getId(), taskDuplicateInfo.getRepositoryDuplicateId(), duplicateFlg, projectCode, projectName, implOrg, planYear, projectMsg, duplicateStatus);
        Integer pageCount = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        taskDuplicateInfoVo.setTotal(total);
        taskDuplicateInfoVo.setPageCount(pageCount);
        // 分页查询出文档
        List<RepositoryDoc> docList = repositoryDuplicateMapper.queryByPageAndRepoDupId(taskDuplicateInfo.getId(), taskDuplicateInfo.getRepositoryDuplicateId(),
                pageSize, offset, duplicateFlg, projectCode, projectName, implOrg, planYear, projectMsg, duplicateStatus);
        List<RepositoryDocVo> docVoList = RepositoryDocConverter.INSTANCE.convertListToVoList(docList);
        for (RepositoryDocVo repositoryDocVo : docVoList) {
            TaskDuplicateDoc taskDuplicateDoc = taskDuplicateDocMapper.queryByInfoIdAndRepoDocId(taskDuplicateInfo.getId(), repositoryDocVo.getId());
            repositoryDocVo.setDuplicateStatus(taskDuplicateDoc.getTaskStatus());
            repositoryDocVo.setDuplicateFlg(taskDuplicateDoc.getDuplicateFlg());
        }

        // 专项重复情况
        Map<String, Integer> specialItemConditionMap = new HashMap<>();
        specialItemConditionMap.put("电网基建", 0);
        specialItemConditionMap.put("生产技改", 0);
        specialItemConditionMap.put("生产投入", 0);
        specialItemConditionMap.put("跨专项", 0);
        // 查询出所有对比组
        for (RepositoryDocVo repositoryDocVo : docVoList) {
            // 先封装一下主文件信息
            Integer mDocId = repositoryDocVo.getId();
            RepositoryDoc mRepositoryDoc = repositoryDocMapper.queryById(mDocId);
            Map<String, String> mExtractContentMap = JSON.parseObject(mRepositoryDoc.getExtractContent(), Map.class);
            List<ScheduleXj> scheduleXjs = scheduleXjMapper.queryByEitherDocIdAndSourceId(repositoryDocVo.getId(), taskDuplicateInfoVo.getId());
            for (ScheduleXj scheduleXj : scheduleXjs) {
                // 拿到被对比文档的 id
                Integer rDocId = scheduleXj.getDocFId().equals(repositoryDocVo.getId()) ? scheduleXj.getDocSId() : scheduleXj.getDocFId();
                RepositoryDoc rRepositoryDoc = repositoryDocMapper.queryById(rDocId);
                String rExtractContent = rRepositoryDoc.getExtractContent();
                Map<String, Object> rExtractContentMap = JSON.parseObject(rExtractContent, Map.class);

                // 专项重复情况
//                if (mExtractContentMap.get("专项名称") != null && !mExtractContentMap.get("专项名称").equals(rExtractContentMap.get("专项名称"))) {
//                    specialItemConditionMap.put("跨专项", specialItemConditionMap.get("跨专项") + 1);
//                }
//                if (mExtractContentMap.get("专项名称") != null && mExtractContentMap.get("专项名称").equals(rExtractContentMap.get("专项名称"))) {
//                    specialItemConditionMap.put(mExtractContentMap.get("专项名称"), specialItemConditionMap.get(mExtractContentMap.get("专项名称")) + 1);
//                }
                if (mRepositoryDoc.getProjectType() != null && mRepositoryDoc.getProjectType().equals(rRepositoryDoc.getProjectType())) {
                    specialItemConditionMap.put(ProjectTypeEnum.getByCode(mRepositoryDoc.getProjectType()).getName(), specialItemConditionMap.get(ProjectTypeEnum.getByCode(mRepositoryDoc.getProjectType()).getName()) + 1);
                }
                if (mRepositoryDoc.getProjectType() != null && !mRepositoryDoc.getProjectType().equals(rRepositoryDoc.getProjectType())) {
                    specialItemConditionMap.put("跨专项", specialItemConditionMap.get("跨专项") + 1);
                }

                implOrg = rRepositoryDoc.getImplOrg();
//                implOrg = String.valueOf(rExtractContentMap.get("实施单位名称"));
                scheduleXj.setImplOrg(implOrg);
                planYear = rRepositoryDoc.getPlanYear();
//                planYear = String.valueOf(rExtractContentMap.get("计划年度"));
                scheduleXj.setPlanYear(planYear);
                projectCode = rRepositoryDoc.getProjectCode();
//                projectCode = String.valueOf(rExtractContentMap.get("项目编码"));
                scheduleXj.setProjectCode(projectCode);
                projectName = rRepositoryDoc.getProjectName();
//                projectName = String.valueOf(rExtractContentMap.get("项目名称"));
                scheduleXj.setProjectName(projectName);
                projectMsg = rRepositoryDoc.getProjectMsg();
//                projectMsg = String.valueOf(rExtractContentMap.get("站线名称"));
                scheduleXj.setProjectMsg(projectMsg);

                Map<String, String> adviceMap = JSON.parseObject(scheduleXj.getAdvice(), Map.class);
                if (adviceMap != null) {
                    scheduleXj.setSuggestions(adviceMap.get("suggestions"));
                    scheduleXj.setAnalysis(adviceMap.get("analysis"));
                }

                Map<String, List<String>> tableMap = new HashMap<>();
                Map<String, String> contentA = JSON.parseObject(mRepositoryDoc.getExtractContent(), Map.class);
                Map<String, String> contentB = JSON.parseObject(rRepositoryDoc.getExtractContent(), Map.class);
                List<String> list1 = new ArrayList<>();
                list1.add(contentA.get("设备现状"));
                list1.add(contentB.get("设备现状"));
                tableMap.put("设备现状", list1);
                List<String> list2 = new ArrayList<>();
                list2.add(contentA.get("存在问题"));
                list2.add(contentB.get("存在问题"));
                tableMap.put("存在问题及实施必要性", list2);
                List<String> list3 = new ArrayList<>();
                list3.add(contentA.get("方案规模"));
                list3.add(contentB.get("方案规模"));
                tableMap.put("建设方案", list3);
                List<String> list4 = new ArrayList<>();
                list4.add(contentA.get("项目类型") == null ? "基建" : contentA.get("项目类型"));
                list4.add(contentB.get("项目类型") == null ? "基建" : contentB.get("项目类型"));
                tableMap.put("项目类型", list4);
                // scheduleXj.setTable(tableMap);
            }
            repositoryDocVo.setGroups(scheduleXjs);
        }
        taskDuplicateInfoVo.setRepositoryDocs(docVoList);

        List<Map<String, String>> specialItemConditionList = new ArrayList<>();
        // 将specialItemConditionMap中的内容转化格式存到specialItemConditionList中
        // 首先计算四项的总和
        int specialItemTotal = specialItemConditionMap.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : specialItemConditionMap.entrySet()) {
            Map<String, String> item = new HashMap<>();
            String name = entry.getKey();
            Integer value = entry.getValue();
            String repeatRate = specialItemTotal > 0 ? String.format("%.2f", (value * 100.0 / specialItemTotal)) + "%" : "0%";
            item.put("name", name);
            item.put("value", String.valueOf(value));
            item.put("repeatRate", repeatRate);
            specialItemConditionList.add(item);
        }
        taskDuplicateInfoVo.setSpecialItemConditionList(specialItemConditionList);

        return taskDuplicateInfoVo;
    }

    /**
     * 查询查重规则和查重库
     */
    @Override
    public Map<String, Object> queryDuplicateRuleAndRepository() {
        Map<String, Object> resultMap = new HashMap<>();
        List<RuleDuplicate> ruleDuplicateList = ruleDuplicateMapper.queryIdAndName();
        List<RepositoryDuplicate> repositoryDuplicateList = repositoryDuplicateMapper.queryIdAndName();
        List<RuleDuplicateVo> ruleDuplicateVoList = RuleDuplicateConverter.INSTANCE.convertListToVoList(ruleDuplicateList);
        List<RepositoryDuplicateVo> repositoryDuplicateVoList = RepositoryDuplicateConverter.INSTANCE.convertListToVoList(repositoryDuplicateList);
        resultMap.put("ruleDuplicate", ruleDuplicateVoList);
        resultMap.put("repositoryDuplicate", repositoryDuplicateVoList);
        return resultMap;
    }

    /**
     * 查询子任务详情
     */
    @Override
    public TaskDuplicateDocVo querySubtaskDetail(Integer id) {
        TaskDuplicateDoc taskDuplicateDoc = taskDuplicateDocMapper.queryById(id);
        TaskDuplicateDocVo taskDuplicateDocVo = new TaskDuplicateDocVo();
        taskDuplicateDocVo.setId(taskDuplicateDoc.getId());
        taskDuplicateDocVo.setTaskStatus(taskDuplicateDoc.getTaskStatus());
        taskDuplicateDocVo.setExtractContent(taskDuplicateDoc.getExtractContent());
        if (taskDuplicateDoc.getTaskStatus() == TaskStatusEnum.COMPLETED.getCode() || taskDuplicateDoc.getTaskStatus() == TaskStatusEnum.FAILED.getCode()) {
            // 任务状态为已完成时才返回查重报告
            taskDuplicateDocVo.setDuplicateResult(taskDuplicateDoc.getDuplicateResult());
        }

        return taskDuplicateDocVo;
    }

    /**
     * 保存子任务详情信息
     */
    @Override
    public void saveSubtask(TaskDuplicateDocVo taskDuplicateDocVo) {
        TaskDuplicateDoc taskDuplicateDoc = TaskDuplicateDocConverter.INSTANCE.convertVoToTaskDuplicateDoc(taskDuplicateDocVo);
        taskDuplicateDocMapper.updateById(taskDuplicateDoc);
        // 此处不需要重新发起查重请求，需要用户手动点
    }

    /**
     * 重新发起查重请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduplicate(TaskDuplicateDocVo taskDuplicateDocVo) {
        // 更新查重定时任务表的状态为未执行
        ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
        scheduleDuplicate.setDocId(taskDuplicateDocVo.getId());
        scheduleDuplicate.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        scheduleDuplicateMapper.updateByDocId(scheduleDuplicate);
        // 更新子任务表和子任务表的状态为未执行
        TaskDuplicateDoc taskDuplicateDoc = taskDuplicateDocMapper.queryById(taskDuplicateDocVo.getId());
        taskDuplicateDoc.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateDocMapper.updateById(taskDuplicateDoc);
        TaskDuplicateInfo taskDuplicateInfo = new TaskDuplicateInfo();
        taskDuplicateInfo.setId(taskDuplicateDoc.getInfoId());
        taskDuplicateInfo.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateInfoMapper.updateById(taskDuplicateInfo);
    }

    /**
     * 重新发起查重请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduplicateFailedTasks(TaskDuplicateInfoVo taskDuplicateInfoVo) {
        // 更新查重定时任务表的状态为未执行
        ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
        scheduleDuplicate.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        scheduleDuplicateMapper.updateByInfoId(taskDuplicateInfoVo.getId(), scheduleDuplicate);
        // 更新子任务表和子任务表的状态为未执行
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        taskDuplicateDoc.setInfoId(taskDuplicateInfoVo.getId());
        taskDuplicateDoc.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateDocMapper.updateByInfoId(taskDuplicateDoc);
        TaskDuplicateInfo taskDuplicateInfo = new TaskDuplicateInfo();
        taskDuplicateInfo.setId(taskDuplicateDoc.getInfoId());
        taskDuplicateInfo.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
        taskDuplicateInfoMapper.updateById(taskDuplicateInfo);
    }

    /*
     * 分页查询任务详情
     */
    @Override
    public PageResult<RepositoryDocVo> queryTaskDetailPage(Integer id, String projectName, Integer pageNo, Integer pageSize) {
        PageResult<RepositoryDocVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);
        // 查询满足条件的总记录数
        int total = scheduleXjMapper.countBySourceId(id);
        if(total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }
        TaskDuplicateInfo taskDuplicateInfo = taskDuplicateInfoMapper.queryById(id);
        Integer offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
        List<RepositoryDoc> docList = repositoryDuplicateMapper.queryByPageAndRepoDupIdAndCondition(taskDuplicateInfo.getRepositoryDuplicateId(), PageInfoEnum.PAGE_SIZE.getCode(), offset, projectName);
        List<RepositoryDocVo> docVoList = RepositoryDocConverter.INSTANCE.convertListToVoList(docList);
        // 查询出所有对比组
        for (RepositoryDocVo repositoryDocVo : docVoList) {
            // 先封装一下主文件信息
            Integer mDocId = repositoryDocVo.getId();
            ProjectStationLineMapping mProjectStationLineMapping = projectStationLineMappingMapper.queryByDocId(mDocId);
            repositoryDocVo.setProjectMsg(mProjectStationLineMapping.getProjectMsg());
            Integer mDupNum = scheduleXjMapper.countDuplicateByDocIdAndSourceId(mDocId, id);
            repositoryDocVo.setDuplicateFlg(mDupNum > 0 ? 1 : 0);
            List<ScheduleXj> scheduleXjs = scheduleXjMapper.queryByEitherDocIdAndSourceId(repositoryDocVo.getId(), id);
            for (ScheduleXj scheduleXj : scheduleXjs) {
                //拿到被对比文档的 id
                Integer rDocId = scheduleXj.getDocFId() == repositoryDocVo.getId() ? scheduleXj.getDocSId() : scheduleXj.getDocFId();
                ProjectStationLineMapping rProjectStationLineMapping = projectStationLineMappingMapper.queryByDocId(rDocId);
                RepositoryDoc rRepositoryDoc = repositoryDocMapper.queryById(rDocId);
                // 封装被对比文档的信息
                scheduleXj.setImplOrg(rProjectStationLineMapping.getImplOrg());
                scheduleXj.setPlanYear(rProjectStationLineMapping.getPlanYear());
                scheduleXj.setProjectCode(rProjectStationLineMapping.getProjectCode());
                scheduleXj.setProjectName(rProjectStationLineMapping.getProjectName());
                scheduleXj.setProjectMsg(rProjectStationLineMapping.getProjectMsg());

                Map<String, String> adviceMap = JSON.parseObject(scheduleXj.getAdvice(), Map.class);
                scheduleXj.setSuggestions(adviceMap.get("suggestions"));
                scheduleXj.setAnalysis(adviceMap.get("analysis"));

                Map<String, List<String>> tableMap = new HashMap<>();
                Map<String, String> contentA = JSON.parseObject(repositoryDocVo.getExtractContent(), Map.class);
                Map<String, String> contentB = JSON.parseObject(rRepositoryDoc.getExtractContent(), Map.class);
                List<String> list1 = new ArrayList<>();
                list1.add(contentA.get("设备现状"));
                list1.add(contentB.get("设备现状"));
                tableMap.put("设备现状", list1);
                List<String> list2 = new ArrayList<>();
                list2.add(contentA.get("存在问题"));
                list2.add(contentB.get("存在问题"));
                tableMap.put("存在问题及实施必要性", list2);
                List<String> list3 = new ArrayList<>();
                list3.add(contentA.get("方案规模"));
                list3.add(contentB.get("方案规模"));
                tableMap.put("建设方案", list3);
                List<String> list4 = new ArrayList<>();
                list4.add(contentA.get("专项类型"));
                list4.add(contentB.get("专项类型"));
                tableMap.put("项目类型", list4);
                // scheduleXj.setTable(tableMap);
            }
            repositoryDocVo.setGroups(scheduleXjs);
        }
        pageResult.setRecords(docVoList);
        pageResult.setTotal(total);
        return pageResult;
    }

    @Override
    public ScheduleXj queryGroupDetail(Integer id) {
        ScheduleXj scheduleXj = scheduleXjMapper.selectById(id);
        Map<String, String> adviceMap = JSON.parseObject(scheduleXj.getAdvice(), Map.class);
        if (adviceMap != null) {
            scheduleXj.setSuggestions(adviceMap.get("suggestions"));
            scheduleXj.setAnalysis(adviceMap.get("analysis"));
        }
        Integer docFid = scheduleXj.getDocFId();
        Integer docSid = scheduleXj.getDocSId();
        RepositoryDoc repositoryDocA = repositoryDocMapper.queryById(docFid);
        RepositoryDoc repositoryDocB = repositoryDocMapper.queryById(docSid);
        Map<String, List<String>> tableMap = new HashMap<>();
        Map<String, String> contentA = JSON.parseObject(repositoryDocA.getExtractContent(), Map.class);
        Map<String, String> contentB = JSON.parseObject(repositoryDocB.getExtractContent(), Map.class);

        // 配合前端样式
        List<Map<String, String>> tableData = new ArrayList<>();
        Map<String, String> map1 = new HashMap<>();
        map1.put("header", "设备现状");
        map1.put("project1", contentA.get("设备现状"));
        map1.put("project2", contentB.get("设备现状"));
        tableData.add(map1);
        Map<String, String> map2 = new HashMap<>();
        map2.put("header", "存在问题及实施必要性");
        map2.put("project1", contentA.get("存在问题"));
        map2.put("project2", contentB.get("存在问题"));
        tableData.add(map2);
        Map<String, String> map3 = new HashMap<>();
        map3.put("header", "建设方案");
        map3.put("project1", contentA.get("方案规模"));
        map3.put("project2", contentB.get("方案规模"));
        tableData.add(map3);
        Map<String, String> map4 = new HashMap<>();
        map4.put("header", "项目类型");
        map4.put("project1", contentA.get("专项类型"));
        map4.put("project2", contentB.get("专项类型"));
        tableData.add(map4);
        scheduleXj.setTable(tableData);
        List<Map<String, String>> projects = new ArrayList<>();
        Map<String, String> pmap1 = new HashMap<>();
        pmap1.put("prop", "project1");
        pmap1.put("label", "项目1");
        projects.add(pmap1);
        Map<String, String> pmap2 = new HashMap<>();
        pmap2.put("prop", "project2");
        pmap2.put("label", "项目2");
        projects.add(pmap2);
        scheduleXj.setProjects(projects);

        return scheduleXj;
    }

    /**
     * 解析结构化文件（Excel）提取子任务
     */
    @SneakyThrows
    private List<TaskDuplicateDoc> parseStructuredDoc(TaskDuplicateInfoVo taskDuplicateInfoVo) {
        // 只有一个文件
        MultipartFile file = taskDuplicateInfoVo.getFiles().get(0);
        // 解析 excel 文件
        ExcelParserListener listener = new ExcelParserListener();
        EasyExcel.read(file.getInputStream(), listener).sheet().headRowNumber(0).doRead();
        List<Map<String, String>> taskList = listener.getTaskList();
        if (taskList.isEmpty()) {
            throw new RuntimeException("请检查结构化文档内容是否为空或格式是否正确");
        }

        // 添加记录到文件上传表
        TaskDuplicateFile taskDuplicateFile = new TaskDuplicateFile();
        String docUrl = fileService.uploadFile(file.getInputStream(), file.getOriginalFilename(), "duplicate", String.valueOf(taskDuplicateInfoVo.getId()));
        taskDuplicateFile.setInfoId(taskDuplicateInfoVo.getId());
        taskDuplicateFile.setTaskDocNum(taskList.size());
        taskDuplicateFile.setFileStatus(TaskStatusEnum.UNEXECUTED.getCode()); // 任务状态为未开始
        taskDuplicateFile.setDocUrl(docUrl);
        taskDuplicateFile.setFileType(TikaUtil.detectFileType(file.getInputStream()));
        taskDuplicateFile.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        taskDuplicateFileMapper.insert(taskDuplicateFile);

        // 封装子任务list 和 查重任务list
        List<TaskDuplicateDoc> taskDuplicateDocList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            Map<String, String> task = taskList.get(i);
            TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
            taskDuplicateDoc.setInfoId(taskDuplicateInfoVo.getId());
            taskDuplicateDoc.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
            taskDuplicateDoc.setDocName(task.get("taskName"));
            taskDuplicateDoc.setExtractContent(task.get("extractContent"));
            taskDuplicateDoc.setFileId(taskDuplicateFile.getId());
            taskDuplicateDoc.setRowIndex(i + 1);
            taskDuplicateDoc.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            taskDuplicateDocList.add(taskDuplicateDoc);
        }
        return taskDuplicateDocList;
    }

    /**
     * 自定义监听器，处理Excel解析逻辑
     */
    private static class ExcelParserListener implements ReadListener<Map<Integer, Object>> {
        private List<String> extractNames;
        private final List<Map<String, String>> resultList = new ArrayList<>();
        private int rowNum = 0;

        @Override
        public void invoke(Map<Integer, Object> rowData, AnalysisContext context) {
            // log.info(JSON.toJSONString(rowData));
            rowNum++;

            if (rowNum == 1) {
                // 第一行为提取内容名
                extractNames = new ArrayList<>();
                for (int i = 1; ; i++) { // 循环获取列索引1、2、3...的值
                    Object value = rowData.get(i);
                    if (value == null) break; // 没有更多列时退出
                    extractNames.add(String.valueOf(value));
                }
                return;
            }

            if (rowNum >= 2) {
                // 从第二行开始处理，列索引0为taskName
                Object taskNameObj = rowData.get(0);
                if (taskNameObj == null) {
                    // 任务名为空，跳过此行
                    return;
                }
                String taskName = String.valueOf(taskNameObj);
                Map<String, Object> taskMap = new HashMap<>();

                // 列索引1开始对应extractNames中的内容
                for (int i = 1; i <= extractNames.size(); i++) {
                    String extractName = extractNames.get(i - 1); // i-1对应extractNames的索引
                    Object value = rowData.get(i);
                    taskMap.put(extractName, value);
                }
                Map<String, String> map = new HashMap<>();
                map.put("taskName", taskName);
                map.put("extractContent", JSON.toJSONString(taskMap));
                resultList.add(map);
            }
        }

        public List<Map<String, String>> getTaskList() { return resultList; }
        @Override
        public boolean hasNext(AnalysisContext context) { return true; }
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    /**
     * 解析多个文档生成子任务
     */
    @SneakyThrows
    private List<TaskDuplicateDoc> parseUnstructuredDocs(TaskDuplicateInfoVo taskDuplicateInfoVo) {
        List<TaskDuplicateDoc> taskDuplicateDocList = new ArrayList<>();
        List<MultipartFile> files = taskDuplicateInfoVo.getFiles();
        // 非结构化文档会上传多个文件，进行分别处理
        List<TaskDuplicateFile> taskDuplicateFileList = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            // 同一个任务中文件不能重名
            String fileName = FileNameUtil.generateUniqueFileName(file.getOriginalFilename(), fileNames);
            fileNames.add(fileName);
            // 每个文档对应上传文件表中的一条记录
            TaskDuplicateFile taskDuplicateFile = new TaskDuplicateFile();
            String docUrl = fileService.uploadFile(file.getInputStream(), fileName, "duplicate", null);
            taskDuplicateFile.setInfoId(taskDuplicateInfoVo.getId());
            taskDuplicateFile.setTaskDocNum(1); // 每个非结构化文档对应一个子任务
            taskDuplicateFile.setFileName(fileName);
            taskDuplicateFile.setFileStatus(TaskStatusEnum.UNEXECUTED.getCode()); // 任务状态为未开始
            taskDuplicateFile.setDocUrl(docUrl);
            taskDuplicateFile.setFileType(TikaUtil.detectFileType(file.getInputStream()));
            taskDuplicateFile.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            taskDuplicateFileList.add(taskDuplicateFile);
        }
        List<List<TaskDuplicateFile>> taskDuplicateFileLists = BatchSplitUtil.splitList(taskDuplicateFileList);
        for (List<TaskDuplicateFile> list : taskDuplicateFileLists) {
            taskDuplicateFileMapper.batchInsert(list);
        }
        for (TaskDuplicateFile taskDuplicateFile : taskDuplicateFileList) {
            // 每个文件对应一个子任务
            TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
            taskDuplicateDoc.setDocName(taskDuplicateFile.getFileName());
            taskDuplicateDoc.setInfoId(taskDuplicateInfoVo.getId());
            taskDuplicateDoc.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
            taskDuplicateDoc.setFileId(taskDuplicateFile.getId());
            taskDuplicateDoc.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            taskDuplicateDocList.add(taskDuplicateDoc);
        }
        return taskDuplicateDocList;
    }

    /**
     * 导出查重结果
     */
    @Override
    @SneakyThrows
    public void exportDuplicateResult(Integer infoId, Integer range, Integer exportType) {
        // 查询子任务列表
        List<TaskDuplicateDoc> taskDuplicateDocList = taskDuplicateDocMapper.queryByInfoId(infoId);
        
        // 根据范围过滤数据
        List<TaskDuplicateDoc> filteredList;
        if (range != null && range == 1) { // 只导出重复的
            filteredList = taskDuplicateDocList.stream()
                    .filter(doc -> doc.getDuplicateMsg() != null)
                    .collect(Collectors.toList());
        } else { // 全量导出
            filteredList = taskDuplicateDocList;
        }
        
        if (exportType == ExportTypeEnum.EXCEL.getCode()) {
            exportDuplicateResultExcel(filteredList);
        } else if (exportType == ExportTypeEnum.JSON.getCode()) {
            exportDuplicateResultJson(filteredList);
        }
    }

    /**
     * 导出查重结果为 Excel
     */
    private void exportDuplicateResultExcel(List<TaskDuplicateDoc> taskDuplicateDocList) throws IOException {
        // 定义表头
        List<String> headers = Arrays.asList("文档名称", "查重结果", "重复信息");
        List<List<String>> excelHead = headers.stream()
                .map(header -> Collections.singletonList(header))
                .collect(Collectors.toList());
        
        // 封装数据列表
        List<List<String>> dataList = new ArrayList<>();
        for (TaskDuplicateDoc doc : taskDuplicateDocList) {
            List<String> rowData = new ArrayList<>();
            rowData.add(doc.getDocName() != null ? doc.getDocName() : "");
            rowData.add(doc.getDuplicateResult() != null ? doc.getDuplicateResult() : "");
            rowData.add(doc.getDuplicateMsg() != null ? doc.getDuplicateMsg() : "");
            dataList.add(rowData);
        }
        
        ExcelUtil.exportExcel(
            SpringContextUtil.getHttpServletResponse(), 
            "查重结果数据", 
            excelHead, 
            dataList
        );
    }

    /**
     * 导出查重结果为 JSON
     */
    private void exportDuplicateResultJson(List<TaskDuplicateDoc> taskDuplicateDocList) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (TaskDuplicateDoc doc : taskDuplicateDocList) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("文档名称", doc.getDocName());
            dataMap.put("查重结果", doc.getDuplicateResult());
            dataMap.put("重复信息", doc.getDuplicateMsg());
            dataList.add(dataMap);
        }
        List<String> jsonList = dataList.stream()
                .map(JSON::toJSONString)
                .collect(Collectors.toList());
        JsonUtil.exportJson(
            SpringContextUtil.getHttpServletResponse(), 
            "查重结果数据", 
            jsonList
        );
    }

}
