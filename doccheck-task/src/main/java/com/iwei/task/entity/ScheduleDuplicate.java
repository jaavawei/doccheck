package com.iwei.task.entity;

import lombok.Data;
import java.util.Date;

/**
 * 查重任务定时任务表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class ScheduleDuplicate {
    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 子任务id，关联具体的子任务
     */
    private Integer docId;

    /**
     * 任务执行状态：0-未执行，1-执行中，2-执行完成，3-执行失败
     */
    private Integer taskStatus;

    /**
     * 更新时间，自动更新
     */
    private Date updatedAt;

    /**
     * 更新人
     */
    private Integer updatedBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人
     */
    private Integer createdBy;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer delFlg;
}
