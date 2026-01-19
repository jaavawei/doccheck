package com.iwei.task.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.common.enums.DataTypeEnum;
import com.iwei.common.enums.ExportTypeEnum;
import com.iwei.common.tool.TikaUtil;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.task.entity.ScheduleXj;
import com.iwei.task.entity.vo.TaskDuplicateDocVo;
import com.iwei.task.entity.vo.TaskDuplicateInfoVo;
import com.iwei.task.service.TaskDuplicateInfoService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 查重任务 Controller
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/task/duplicate")
@Slf4j
public class TaskDuplicateController {

    @Resource
    private TaskDuplicateInfoService taskDuplicateInfoService;

    /**
     * 查询查重任务列表
     */
    @GetMapping("/queryTaskList")
    public Result<PageResult<TaskDuplicateInfoVo>> queryTaskList(@RequestParam(required = false) String taskName,
                                                              @RequestParam(required = false) Integer taskStatus,
                                                              @RequestParam(required = false) Integer dataType,
                                                              @RequestParam(required = false) Integer pageNo,
                                                              @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<TaskDuplicateInfoVo> pageResult = taskDuplicateInfoService.queryTaskList(taskName, taskStatus, dataType, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("TaskDuplicateController.queryTaskList.error:{}", e.getMessage(), e);
            return Result.fail("查询查重任务列表失败");
        }
    }

    /**
     * 新增查重任务
     */
    @PostMapping(value = "/addTask", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Boolean> addTask(@ModelAttribute TaskDuplicateInfoVo taskDuplicateInfoVo) {
        try {
            Preconditions.checkArgument(!(taskDuplicateInfoVo.getRepositoryDuplicateId() == null), "查重库id不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(taskDuplicateInfoVo.getTaskDuplicateName())), "查重任务名称不能为空");
            Preconditions.checkArgument(!(taskDuplicateInfoVo.getRuleDuplicateId() == null), "查重规则id不能为空");
            Preconditions.checkArgument(!(taskDuplicateInfoVo.getDuplicateType() == null), "查重类型不能为空");
            if(taskDuplicateInfoVo.getDuplicateType() == 0) {
                Preconditions.checkArgument(!(taskDuplicateInfoVo.getDataType() == null), "数据类型不能为空");
                Preconditions.checkArgument(!(taskDuplicateInfoVo.getFiles() == null || taskDuplicateInfoVo.getFiles().size() < 1), "至少上传一个文件");
                if(taskDuplicateInfoVo.getDataType() == DataTypeEnum.STRUCTURED.getCode()) {
                    Preconditions.checkArgument(!(taskDuplicateInfoVo.getFiles().size() > 1), "结构化数据只能上传一个文件");
                    MultipartFile multipartFile = taskDuplicateInfoVo.getFiles().get(0);
                    Preconditions.checkArgument(TikaUtil.isExcelFile(multipartFile.getInputStream()),
                            "上传文件类型错误，请上传结构化数据");
                }
            } else {
                // Preconditions.checkArgument(!(taskDuplicateInfoVo.getDuplicateRange() == null), 查重范围不能为空");
            }
            taskDuplicateInfoService.addTask(taskDuplicateInfoVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskDuplicateController.addTask.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除查重任务
     */
    @DeleteMapping("/deleteTask")
    public Result<Boolean> deleteTask(@RequestBody TaskDuplicateInfoVo taskDuplicateInfoVo) {
        try {
            log.info("TaskDuplicateController.deleteTask.taskDuplicateInfo:{}", JSON.toJSONString(taskDuplicateInfoVo));
            Preconditions.checkArgument(!(taskDuplicateInfoVo.getId() == null), "任务id不能为空");
            taskDuplicateInfoService.deleteTask(taskDuplicateInfoVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskDuplicateController.deleteTask.error:{}", e.getMessage(), e);
            return Result.fail("删除查重任务失败");
        }
    }

    /**
     * 查询查重任务详情
     */
    @GetMapping("/queryTaskDetail")
    public Result<TaskDuplicateInfoVo> queryTaskDetail(@RequestParam(required = true) Integer id,
                                                       @RequestParam(required = false) Integer pageNo,
                                                       @RequestParam(required = false) Integer pageSize,
                                                       @RequestParam(required = false) Integer duplicateFlg,
                                                       @RequestParam(required = false) String projectCode,
                                                       @RequestParam(required = false) String projectName,
                                                       @RequestParam(required = false) String implOrg,
                                                       @RequestParam(required = false) String planYear,
                                                       @RequestParam (required = false) String projectMsg,
                                                       @RequestParam (required = false) Integer duplicateStatus) {
        try {
            TaskDuplicateInfoVo taskDuplicateInfoVo = taskDuplicateInfoService.queryTaskDetail(id, pageNo, pageSize,
                    duplicateFlg, projectCode, projectName, implOrg, planYear, projectMsg, duplicateStatus);
            return Result.ok(taskDuplicateInfoVo);
        } catch (Exception e) {
            log.error("TaskDuplicateController.queryTaskDetail.error:{}", e.getMessage(), e);
            return Result.fail("查询查重任务详情失败");
        }
    }

    @GetMapping("/queryGroupDetail")
    public Result<ScheduleXj> queryGroupDetail(@RequestParam Integer id) {
        try {
            ScheduleXj scheduleXj = taskDuplicateInfoService.queryGroupDetail(id);
            return Result.ok(scheduleXj);
        } catch (Exception e) {
            log.error("TaskDuplicateController.queryGroupDetail.error:{}", e.getMessage(), e);
            return Result.fail("查询对比组查重任务详情失败");
        }
    }
    /**
     * 查询查重任务详情
     */
    @GetMapping("/queryTaskDetailPage")
    public Result<PageResult<RepositoryDocVo>> queryTaskDetailPage(@RequestParam Integer id,
                                                           @RequestParam(required = false) String projectName,
                                                           @RequestParam(required = false) Integer taskStatus,
                                                           @RequestParam(required = false) Integer pageNo,
                                                           @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RepositoryDocVo> repositoryDocVoPageResult = taskDuplicateInfoService.queryTaskDetailPage(id, projectName, pageNo, pageSize);
            return Result.ok(repositoryDocVoPageResult);
        } catch (Exception e) {
            log.error("TaskDuplicateController.queryTaskDetailPage.error:{}", e.getMessage(), e);
            return Result.fail("查询查重任务详情失败");
        }
    }

    /**
     * 查询子任务详情
     */
    @GetMapping("/querySubtaskDetail")
    public Result<TaskDuplicateDocVo> querySubtaskDetail(@RequestParam Integer id) {
        try {
            TaskDuplicateDocVo taskDuplicateDocVo = taskDuplicateInfoService.querySubtaskDetail(id);
            return  Result.ok(taskDuplicateDocVo);
        } catch (Exception e) {
            log.error("TaskDuplicateController.querySubtaskDetail.error:{}", e.getMessage(), e);
            return Result.fail("查询子任务详情失败");
        }
    }

    /*
     * 保存子任务提取内容
     */
    @PostMapping("/saveSubtask")
    public Result<Boolean> saveSubtask(@RequestBody TaskDuplicateDocVo taskDuplicateDocVo) {
        try {
            taskDuplicateInfoService.saveSubtask(taskDuplicateDocVo);
            return  Result.ok(true);
        } catch (Exception e) {
            log.error("TaskDuplicateController.saveSubtask.error:{}", e.getMessage(), e);
            return Result.fail("保存子任务提取内容失败");
        }
    }

    /**
     * 重新发起查重请求
     */
    @PutMapping("/reduplicate")
    public Result<Boolean> reduplicate(@RequestBody TaskDuplicateDocVo taskDuplicateDocVo) {
        try {
            taskDuplicateInfoService.reduplicate(taskDuplicateDocVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskDuplicateController.reduplicate.error:{}", e.getMessage(), e);
            return Result.fail("查询发起查重失败");
        }
    }

    /**
     * 重新发起查重请求 (所有失败子任务)
     */
    @PutMapping("/reduplicateFailedTasks")
    public Result<Boolean> reduplicateFailedTasks(@RequestBody TaskDuplicateInfoVo taskDuplicateInfoVo) {
        try {
            taskDuplicateInfoService.reduplicateFailedTasks(taskDuplicateInfoVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskDuplicateController.reduplicate.error:{}", e.getMessage(), e);
            return Result.fail("查询发起查重失败");
        }
    }

    /**
     * 查询查重规则和查重库
     */
    @GetMapping("/queryDuplicateRuleAndRepository")
    public Result<Map<String, Object>> queryDuplicateRuleAndRepository() {
        try {
            Map<String, Object> resultMap = taskDuplicateInfoService.queryDuplicateRuleAndRepository();
            return Result.ok(resultMap);
        } catch (Exception e) {
            log.error("TaskDuplicateController.queryTaskDetail.error:{}", e.getMessage(), e);
            return Result.fail("查询查重规则和查重库失败");
        }
    }

    /**
     * 导出查重结果
     */
    @GetMapping("/exportDuplicateResult")
    public void exportDuplicateResult(@RequestParam Integer infoId, 
                                      @RequestParam Integer range, 
                                      @RequestParam Integer exportType) {
        try {
            Preconditions.checkArgument(!(infoId == null), "任务id不能为空");
            Preconditions.checkArgument(!(range == null), "导出范围不能为空");
            Preconditions.checkArgument(!(exportType == null), "导出类型不能为空");
            taskDuplicateInfoService.exportDuplicateResult(infoId, range, exportType);
        } catch (Exception e) {
            log.error("TaskDuplicateController.exportDuplicateResult.error: {}", e.getMessage(), e);
            return;
        }
    }

}
