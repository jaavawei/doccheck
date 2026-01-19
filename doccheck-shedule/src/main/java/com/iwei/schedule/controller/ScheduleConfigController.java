package com.iwei.schedule.controller;

import com.iwei.common.entity.Result;
import com.iwei.schedule.config.ScheduleTaskConfig;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/schedule/config")
public class ScheduleConfigController {
    
    @Resource
    private ScheduleTaskConfig scheduleTaskConfig;

    
    /**
     * 获取文档最大长度
     * @return 文档最大长度
     */
    @GetMapping("/docMaxLength")
    public Result<Integer> getDocMaxLength() {
        try {
            return Result.ok(scheduleTaskConfig.getMaxLength());
        } catch (Exception e) {
            log.error("获取文档最大长度失败", e);
            return Result.fail("获取文档最大长度失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文档最大长度
     * @param maxLength 最大长度
     * @return 是否更新成功
     */
    @PostMapping("/docMaxLength")
    public Result<Boolean> updateDocMaxLength(@RequestBody int maxLength) {
        try {
            scheduleTaskConfig.setMaxLength(maxLength);
            log.info("DocMaxLength被更新为: {}", maxLength);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新文档最大长度失败，value: {}", maxLength, e);
            return Result.fail("更新文档最大长度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取线程池大小
     * @return 线程池大小
     */
    @GetMapping("/threadPoolSize")
    public Result<Integer> getThreadPoolSize() {
        try {
            return Result.ok(scheduleTaskConfig.getThreadPoolSize());
        } catch (Exception e) {
            log.error("获取线程池大小失败", e);
            return Result.fail("获取线程池大小失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新线程池大小
     * @param poolSize 线程池大小
     * @return 是否更新成功
     */
    @PostMapping("/threadPoolSize")
    public Result<Boolean> updateThreadPoolSize(@RequestBody int poolSize) {
        try {
            scheduleTaskConfig.setThreadPoolSize(poolSize);
            log.info("ThreadPoolSize被更新为: {}", poolSize);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新线程池大小失败，value: {}", poolSize, e);
            return Result.fail("更新线程池大小失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用 定时任务
     */
    @PostMapping("/enable")
    public Result<Boolean> enable() {
        try {
            scheduleTaskConfig.setEnable(!scheduleTaskConfig.isEnable());
            log.info("ScheduleTaskEnable被更新为: {}", scheduleTaskConfig.isEnable());
            return Result.ok(scheduleTaskConfig.isEnable());
        } catch (Exception e) {
            log.error("启用/禁用 定时任务失败", e);
            return Result.fail("启用/禁用 定时任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新提取任务类型
     */
    @PostMapping("/extractType")
    public Result<Boolean> updateExtractType(@RequestBody String extractType) {
        try {
            scheduleTaskConfig.setExtractType(extractType);
            log.info("ExtractType被更新为: {}", extractType);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新提取任务类型失败，value: {}", extractType, e);
            return Result.fail("更新提取任务类型失败: " + e.getMessage());
        }
    }
}