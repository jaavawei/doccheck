package com.iwei.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * 任务审查规则映射实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskReviewRuleMapping {
    /** 主键自增id */
    private Integer id;

    /** 审查任务id */
    private Integer taskReviewId;

    /** 审查规则id */
    private Integer ruleReviewId;

    /** 合规标识 */
    private Integer compliantFlg;

    /** 不合规问题信息 */
    private String questionMsg;

    /** 不合规建议 */
    private String advice;

    /** 创建人 */
    private Integer createdBy;

    /** 创建时间 */
    private Date createdAt;

    /** 更新人 */
    private Integer updatedBy;

    /** 更新时间 */
    private Date updatedAt;

    /** 删除标识 */
    private Integer delFlg;
}