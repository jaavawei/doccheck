package com.iwei.task.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 查重任务文档表（子任务表）实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateDocVo {
    /** 主键自增id */
    private Integer id;

    /** 主任务id（查重任务信息表） */
    private Integer infoId;

    /** 子任务名 */
    private String docName;

    /** 上传文档id */
    private Integer fileId;

    /** excel行号（结构化文档） */
    private Integer rowIndex;

    /** 子任务状态： 0-未开始 1-执行中 2-已完成 3-失败 */
    private Integer taskStatus;

    /** 提取内容 */
    private String extractContent;

    /** 查重结果id（最相似） */
    private Integer resultId;

    /** 错误信息 */
    private String errorMsg;

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

    /** 文档库id（新疆） */
    private Integer repoDocId;

    /** 重复信息：重复改造、多头申报等 */
    private String duplicateMsg;

    /** 重复标识（新疆） */
    private Integer duplicateFlg;

    /** 查重结果 */
    private String duplicateResult;

    private String duplicateRuleName;

    private String duplicateRepositoryName;
}