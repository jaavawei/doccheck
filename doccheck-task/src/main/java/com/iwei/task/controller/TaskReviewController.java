package com.iwei.task.controller;

import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.task.entity.vo.TaskReviewInfoVo;
import com.iwei.task.service.TaskReviewInfoService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 审查任务 controller
 *
 * @auther: zhaokangwei
 */
@Slf4j
@RestController
@RequestMapping("/task/review")
public class TaskReviewController {

    @Resource
    private TaskReviewInfoService taskReviewInfoService;

    /**
     * 新增审查任务
     */
    @PostMapping("/addTaskReview")
    public Result<Boolean> addTaskReview(@RequestBody TaskReviewInfoVo taskReviewInfoVo) {
        try {
            log.info("taskReviewInfoVo:{}", taskReviewInfoVo);
            Preconditions.checkArgument(StringUtils.isNotBlank(taskReviewInfoVo.getTaskReviewName()), "审查任务名称不能为空");
            Preconditions.checkArgument(taskReviewInfoVo.getRepositoryReviewId() != null, "审查库 id 不能为空");
            Preconditions.checkArgument(!(taskReviewInfoVo.getRuleReviewIds() == null && taskReviewInfoVo.getRuleReviewIds().size() < 1), "至少选择一个审查规则");
            taskReviewInfoService.addTaskReview(taskReviewInfoVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.addTaskReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }


    /**
     * 删除审查任务
     */
    @DeleteMapping("/deleteTaskReview")
    public Result<Boolean> deleteTaskReview(@RequestBody TaskReviewInfoVo taskReviewInfoVo) {
        try {
            log.info("taskReviewInfoVo:{}", taskReviewInfoVo);
            Preconditions.checkArgument(taskReviewInfoVo.getId() != null, "审查任务 id 不能为空");
            taskReviewInfoService.deleteTaskReview(taskReviewInfoVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.deleteTaskReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询审查任务列表
     */
    @GetMapping("/queryTaskReviewList")
    public Result<PageResult<TaskReviewInfoVo>> queryTaskReviewList(@RequestParam(required = false) String taskReviewName,
                                                                    @RequestParam(required = false) Integer taskStatus,
                                                                    @RequestParam(required = false) String projectName,
                                                                    @RequestParam(required = false) Integer pageNo,
                                                                    @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<TaskReviewInfoVo> pageResult = taskReviewInfoService.queryTaskReviewList(taskReviewName, taskStatus, projectName, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("TaskReviewController.queryTaskReviewList.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询审查规则和审查库
     */
    @GetMapping("/queryReviewRuleAndRepository")
    public Result<Map<String, Object>> queryReviewRuleAndRepository() {
        try {
            Map<String, Object> result = taskReviewInfoService.queryReviewRuleAndRepository();
            return Result.ok(result);
        } catch (Exception e) {
            log.error("TaskReviewController.queryReviewRuleAndRepository.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询审查任务详情
     */
    @GetMapping("/queryReviewTaskDetail")
    public Result<TaskReviewInfoVo> queryReviewTaskDetail(@RequestParam Integer id) {
        try {
            Preconditions.checkArgument(id != null, "审查任务 id 不能为空");
            TaskReviewInfoVo result = taskReviewInfoService.queryReviewTaskDetail(id);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("TaskReviewController.queryReviewTaskDetail.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
}