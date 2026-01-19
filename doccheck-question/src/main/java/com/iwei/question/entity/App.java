package com.iwei.question.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * app 实体类
 *
 * @auther: zhaokangwei
 */
@Data
@Component
public class App {

    /**
     * id
     */
    private Integer id;

    /**
     * app 名称
     */
    private String appName;

    /**
     * agent 访问地址
     */
    private String agentUrl;

    /**
     * agent 唯一标识
     */
    private String agentId;

    /**
     * agent 版本
     */
    private String agentVersion;

    /**
     * agent 密钥
     */
    private String agentToken;

    /**
     * 删除标识
     */
    private Integer delFlg;
}
