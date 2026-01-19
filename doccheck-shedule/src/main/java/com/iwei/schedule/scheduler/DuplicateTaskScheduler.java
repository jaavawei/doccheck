package com.iwei.schedule.scheduler;

import com.iwei.schedule.config.ScheduleTaskConfig;
import com.iwei.schedule.processor.DuplicateTaskProcessor;

import com.iwei.task.entity.ScheduleDuplicate;
import com.iwei.task.mapper.ScheduleDuplicateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 查重定时任务调度器
 *
 * @auther: zhaokangwei
 */
@Component
@Slf4j
public class DuplicateTaskScheduler {

    // 移除字段上的@Value注解，改为构造函数参数注入
    private final int SCAN_INTERVAL_SECONDS; // 空闲时扫描间隔（秒）
    private final int SCHEDULE_TASK_BATCH_SIZE; // 每次处理的任务数量
    private final int MAX_RETRY_COUNT; // 最大重试次数
    private int THREAD_POOL_SIZE; // 线程池大小
    private static final String SCHEDULER_NAME = "duplicate"; // 调度器名称

    private final ScheduleDuplicateMapper scheduleDuplicateMapper;
    private final DuplicateTaskProcessor duplicateTaskProcessor;
    private final ScheduledExecutorService scheduler;
    private ThreadPoolExecutor taskExecutor; // 任务执行线程池
    private volatile boolean isRunning; // 控制任务调度的开关
    private ScheduleTaskConfig scheduleTaskConfig;

    public DuplicateTaskScheduler(
            @Value("${schedule.task.scan-interval-seconds}") int scanIntervalSeconds,
            @Value("${schedule.task.batch-size}") int scheduleTaskBatchSize,
            @Value("${schedule.task.max-retry-count}") int maxRetryCount,
            @Value("${schedule.task.thread-pool-size}") int threadPoolSize,
            ScheduleDuplicateMapper scheduleDuplicateMapper,
            DuplicateTaskProcessor duplicateTaskProcessor,
            ScheduleTaskConfig scheduleTaskConfig) {
        // 赋值配置参数（此时已拿到配置文件中的正确值）
        this.SCAN_INTERVAL_SECONDS = scanIntervalSeconds;
        this.SCHEDULE_TASK_BATCH_SIZE = scheduleTaskBatchSize;
        this.MAX_RETRY_COUNT = maxRetryCount;
        this.THREAD_POOL_SIZE = threadPoolSize;

        // 初始化依赖Bean
        this.scheduleTaskConfig = scheduleTaskConfig;
        this.scheduleDuplicateMapper = scheduleDuplicateMapper;
        this.duplicateTaskProcessor = duplicateTaskProcessor;

        // 创建调度线程
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.taskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.isRunning = false;
    }

    /**
     * 启动任务调度器
     */
    public void start() {
        if (isRunning) {
            log.info("任务调度器已经在运行中");
            return;
        }

        isRunning = true;
        log.info("启动任务调度器，空闲时扫描间隔：" + SCAN_INTERVAL_SECONDS + "秒");

        // 提交初始任务
        scheduler.submit(this::processLoop);
    }

    /**
     * 停止任务调度器
     */
    public void stop() {
        isRunning = false;
        log.info("停止任务调度器");
        scheduler.shutdown();
        taskExecutor.shutdown(); // 关闭任务执行线程池
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            taskExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 应用关闭时的清理工作
     */
    @PreDestroy
    public void cleanup() {
        stop();
    }
    
    /**
     * 动态调整线程池大小
     * @param newSize 新的线程池大小
     */
    public void resizeThreadPool(int newSize) {
        if (newSize <= 0) {
            log.warn("线程池大小必须大于0，当前值: {}", newSize);
            return;
        }
        
        log.info("调整线程池大小从 {} 到 {}", THREAD_POOL_SIZE, newSize);
        THREAD_POOL_SIZE = newSize;
        taskExecutor.setCorePoolSize(newSize);
        taskExecutor.setMaximumPoolSize(newSize);
        log.info("线程池大小调整完成: {}", newSize);
    }

    /**
     * 获取当前线程池大小
     * @return 当前线程池大小
     */
    public int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    /**
     * 任务处理主循环
     * 核心逻辑：处理任务 -> 检查是否需要继续 -> 延迟指定时间后再次执行
     */
    private void processLoop() {
        while (isRunning) {
            try {
                // 处理一批任务
                processBatchTasks();

                // 如果仍在运行状态，则等待指定间隔后再次执行
                if (isRunning) {
                    TimeUnit.SECONDS.sleep(SCAN_INTERVAL_SECONDS);
                }
            } catch (InterruptedException e) {
                // 中断异常，退出循环
                Thread.currentThread().interrupt();
                log.info("任务调度线程被中断");
                break;
            } catch (Exception e) {
                log.error("任务处理循环发生错误:{}", e.getMessage(), e);

                // 发生错误时也等待一段时间再重试，避免无限快速循环
                try {
                    if (isRunning) {
                        TimeUnit.SECONDS.sleep(SCAN_INTERVAL_SECONDS);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("文档任务处理循环已退出");
    }

    /**
     * 处理一批任务（修改为多线程处理）
     */
    private void processBatchTasks() {
        // 1. 获取待执行的任务
        List<ScheduleDuplicate> pendingTasks = scheduleDuplicateMapper.getPendingTasks(SCHEDULE_TASK_BATCH_SIZE);

        // 检查并更新线程池大小
        int currentEnumSize = scheduleTaskConfig.getThreadPoolSize();
        if (currentEnumSize != THREAD_POOL_SIZE) {
            resizeThreadPool(currentEnumSize);
        }

        // 2. 并行处理任务
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        for (ScheduleDuplicate task : pendingTasks) {
            // 如果调度器已停止，则不再处理新任务
            if (!isRunning) {
                break;
            }
            
            // 提交任务到线程池执行
            java.util.concurrent.Future<?> future = taskExecutor.submit(() -> {
                try {
                    duplicateTaskProcessor.processTask(task);
                } catch (Exception e) {
                    log.error("处理任务{}时发生错误: {}", task.getId(), e.getMessage(), e);
                }
            });
            futures.add(future);
        }
        
        // 等待所有任务完成
        for (java.util.concurrent.Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("等待任务完成时发生错误: {}", e.getMessage(), e);
            }
        }
    }
}