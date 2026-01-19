package com.iwei.task.entity;

import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * 查重任务子任务表（任务明细表）实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateDoc {
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

    /** 子任务状态： 0-未开始 1-执行中 2-已完成 */
    private Integer taskStatus;

    /** 提取内容 */
    private String extractContent;

    /** 查重结果id */
    private Integer resultId;

    /** 查重报告 */
    private String duplicateResult;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDuplicateDoc that = (TaskDuplicateDoc) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(infoId, that.infoId) &&
                Objects.equals(docName, that.docName) &&
                Objects.equals(fileId, that.fileId) &&
                Objects.equals(rowIndex, that.rowIndex) &&
                Objects.equals(taskStatus, that.taskStatus) &&
                Objects.equals(extractContent, that.extractContent) &&
                Objects.equals(resultId, that.resultId) &&
                Objects.equals(duplicateResult, that.duplicateResult) &&
                Objects.equals(errorMsg, that.errorMsg) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(delFlg, that.delFlg) &&
                Objects.equals(repoDocId, that.repoDocId) &&
                Objects.equals(duplicateFlg, that.duplicateFlg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, infoId, docName, fileId, rowIndex, taskStatus, extractContent, resultId, 
                duplicateResult, errorMsg, createdBy, createdAt, updatedBy, updatedAt, delFlg, repoDocId, duplicateFlg);
    }
}