package com.iwei.common.config;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 会话配置类
 */
@Slf4j
@Component
@Data
public class AgentConfig {

    
    // 用于存储运行时修改的配置值
    private volatile String createUrl;
    private volatile String deleteUrl;
    private volatile String createToken;
    private volatile String deleteToken;
    private volatile String uploadFileUrl;
    private volatile String agentType;
    private volatile long sessionExpireTime = 7 * 24 * 60 * 60 * 1000L; // 默认7天
    
    @Value("${schedule.task.session.create-url:}")
    private String configCreateUrl;
    
    @Value("${schedule.task.session.delete-url:}")
    private String configDeleteUrl;
    
    @Value("${schedule.task.session.create-token:}")
    private String configCreateToken;
    
    @Value("${schedule.task.session.delete-token:}")
    private String configDeleteToken;

    @Value("${schedule.task.upload-file-url:}")
    private String configUploadFileUrl;

    @Value("${schedule.task.agent.type:}")
    private String configAgentType;
    
    @Value("${schedule.task.session.expire-time:604800000}")
    private long configSessionExpireTime;
    
    @PostConstruct
    public void init() {
        // 从配置文件中读取配置
        this.createUrl = configCreateUrl;
        this.deleteUrl = configDeleteUrl;
        this.createToken = configCreateToken;
        this.deleteToken = configDeleteToken;
        this.uploadFileUrl = configUploadFileUrl;
        this.agentType = configAgentType;
        this.sessionExpireTime = configSessionExpireTime;
        log.info("SessionConfig进行了初始化: createUrl={}, deleteUrl={}, createToken={}，deleteToken={}, uploadFileUrl={}, agentType={}",
                this.createUrl, this.deleteUrl, this.createToken, this.deleteToken, this.uploadFileUrl, this.agentType);
    }

}