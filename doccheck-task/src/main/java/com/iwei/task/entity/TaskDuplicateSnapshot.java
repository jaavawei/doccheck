package com.iwei.task.entity;

import lombok.Data;
import java.util.Date;

/**
 * 查重任务快照实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateSnapshot {
    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 主任务表id，关联主任务信息
     */
    private Integer infoId;

    /**
     * 查重库中文档id快照，存储任务执行时的文档ID状态
     */
    private String snapshotRepo;

    /**
     * 查重规则内容快照，存储任务执行时的规则内容
     */
    private String snapshotRule;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人
     */
    private Integer createdBy;

    /**
     * 更新时间，自动更新
     */
    private Date updatedAt;

    /**
     * 更新人
     */
    private Integer updatedBy;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer delFlg;
}
