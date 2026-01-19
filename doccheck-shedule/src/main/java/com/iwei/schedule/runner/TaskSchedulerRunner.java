package com.iwei.schedule.runner;

import com.iwei.schedule.scheduler.DuplicateTaskScheduler;
import com.iwei.schedule.scheduler.ExtractTaskScheduler;
import com.iwei.schedule.scheduler.ReviewTaskScheduler;
import com.iwei.schedule.scheduler.XjTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 定时任务启动器
 *
 * @auther: zhaokangwei
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "schedule.task.enabled", havingValue = "true", matchIfMissing = false)
public class TaskSchedulerRunner implements CommandLineRunner {

    private final ExtractTaskScheduler extractTaskScheduler;
    private final DuplicateTaskScheduler duplicateTaskScheduler;
    private final ReviewTaskScheduler reviewTaskScheduler;
    private final XjTaskScheduler xjTaskScheduler;

    public TaskSchedulerRunner(ExtractTaskScheduler extractTaskScheduler,
                               DuplicateTaskScheduler duplicateTaskScheduler,
                               ReviewTaskScheduler reviewTaskScheduler,
                               XjTaskScheduler xjTaskScheduler) {
        this.extractTaskScheduler = extractTaskScheduler;
        this.duplicateTaskScheduler = duplicateTaskScheduler;
        this.reviewTaskScheduler = reviewTaskScheduler;
        this.xjTaskScheduler = xjTaskScheduler;
    }

    /**
     * 项目启动完成后自动启动定时任务
     */
    @Override
    public void run(String... args) throws Exception {

        // 启动任务调度器
        extractTaskScheduler.start();
        log.info("文档解析定时任务已启动");
        duplicateTaskScheduler.start();
        log.info("文档查重定时任务已启动");
        reviewTaskScheduler.start();
        log.info("文档审核定时任务已启动");
        xjTaskScheduler.start();
        log.info("新疆定时任务已启动");

    }
}