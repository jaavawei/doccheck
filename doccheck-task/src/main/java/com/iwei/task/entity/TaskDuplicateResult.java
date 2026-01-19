package com.iwei.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * 查重任务结果表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateResult {
    /** 主键自增id */
    private Integer id;

    /** 子任务id */
    private Integer taskDocId;

    /** 查重库中文档id */
    private Integer repositoryDocId;

    /** 查重结果（markdown格式） */
    private String duplicateResult;

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
