package com.iwei.task.entity.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 审查任务信息表 Vo
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskReviewInfoVo {

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

    /** 审查结果 */
    private String reviewResult;

    /** 项目类型 */
    private Integer projectType;

    /** 项目年份 */
    private Integer projectYear;

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

    /** 审查规则id列表 */
    private List<Integer> ruleReviewIds;

    /** 审查库名称 */
    private String repositoryReviewName;

    /** 审查规则名称 */
    private List<String> ruleReviewNames;

    /** 审查项总数量 */
    private Integer itemCount;

    /** 合规审查项数量 */
    private Integer compliantCount;

    /** 不合规审查项数量 */
    private Integer uncompliantCount;

    /** 合规率 */
    private String complianceRate;

    /** 不合规项列表 */
    private List<UncompliantItemVo> uncompliantItems;

}