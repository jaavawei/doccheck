package com.iwei.question.entity;

import lombok.Data;
import java.util.Date;

@Data
public class QuestionLog {
    /**
     * 主键
     */
    private Integer id;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 回答内容
     */
    private String answer;
    
    /**
     * 智能体id
     */
    private Integer appId;
    
    /**
     * 用户上传文件
     */
    private String fileId;
    
    /**
     * 文件名
     */
    private String fileName;

    /**
     * 会话id
     */
    private String sessionId;

    /**
     * 唯一标识：确认是否为同一会话
     */
    private String uuid;

    /**
     * 用户选择的项目信息，用于展示历史数据
     */
    private String projectInfo;

    /**
     * 本次问答操作
     */
    private String qaAction;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 删除标志 0-正常 1-删除
     */
    private Integer delFlg;
}