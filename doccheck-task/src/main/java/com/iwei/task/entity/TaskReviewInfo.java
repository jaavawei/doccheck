package com.iwei.task.entity;

import lombok.Data;
import java.util.Date;

/**
 * 审查任务信息表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskReviewInfo {
    /** 主键自增id */
    private Integer id;

    /** 审查任务名称 */
    private String taskReviewName;

    /** 审查规则id */
    private Integer ruleReviewId;

    /** 审查库id */
    private Integer repositoryReviewId;

    /** 任务状态： 0-未开始  1-执行中  2-已完成 */
    private Integer taskStatus;

    /** 项目名称 */
    private String projectName;

    /** 项目类型 */
    private Integer projectType;

    /** 项目年份 */
    private Integer projectYear;

    /** 审查结果 */
    private String reviewResult;

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