package com.iwei.schedule.config;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 调度任务配置类 - 合并文档最大长度和线程池大小配置
 */
@Slf4j
@Component
@Data
public class ScheduleTaskConfig {
    
    // 从配置文件中读取默认值
    @Value("${schedule.task.max-doc-length}")
    private int defaultMaxLength;
    
    @Value("${schedule.task.thread-pool-size}")
    private int defaultThreadPoolSize;

    @Value("${schedule.task.extract.type}")
    private String defaultExtractType;

    @Value("${schedule.task.enabled}")
    private boolean defaultEnable;
    
    @Value("${schedule.task.max-retry-count}")
    private int defaultMaxRetryCount;
    
    // 用于存储运行时修改的配置值
    private volatile int maxLength;
    private volatile int threadPoolSize;
    private volatile boolean enable;
    private volatile String extractType;
    private volatile int maxRetryCount;
    
    @PostConstruct
    public void init() {
        this.maxLength = defaultMaxLength;
        this.threadPoolSize = defaultThreadPoolSize;
        this.extractType = defaultExtractType;
        this.enable = defaultEnable;
        this.maxRetryCount = defaultMaxRetryCount;
        log.info("ScheduleTaskConfig被初始化: maxLength={}, threadPoolSize={}, enable={}, extractType: {}, maxRetryCount: {}",
                defaultMaxLength, defaultThreadPoolSize, defaultEnable, defaultExtractType, defaultMaxRetryCount);
    }
    
//    /**
//     * 设置文档最大长度
//     * @param maxLength 最大长度
//     */
//    public void setMaxLength(int maxLength) {
//        this.maxLength = maxLength;
//        log.info("DocMaxLength被更新为: {}", maxLength);
//    }
//
//    /**
//     * 设置线程池大小
//     * @param threadPoolSize 线程池大小
//     */
//    public void setThreadPoolSize(int threadPoolSize) {
//        this.threadPoolSize = threadPoolSize;
//        log.info("ThreadPoolSize被更新为: {}", threadPoolSize);
//    }
//
//    /**
//     * 启用/禁用调度任务
//     * @param enable 是否启用
//     */
//    public void setEnable(boolean enable) {
//        this.enable = enable;
//        log.info("ScheduleTaskEnable被更新为: {}", enable);
//    }

}