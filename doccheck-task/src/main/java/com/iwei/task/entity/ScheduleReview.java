package com.iwei.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * 审查任务调度表实体类
 *
 * @author: zhaokangwei
 */
@Data
public class ScheduleReview {

    /*
     * 主键id
     */
    private Integer id;

    /*
     * 审查任务id
     */
    private Integer reviewId;

    /*
     * 任务状态
     */
    private Integer taskStatus;

    /*
     * 错误信息
     */
    private String errorMsg;

    /*
     * 创建时间
     */
    private Date createdAt;

    /*
     * 创建人
     */
    private Integer createdBy;

    /*
     * 更新时间
     */
    private Date updatedAt;

    /*
     * 更新人
     */
    private Integer updatedBy;

    /*
     * 删除标识
     */
    private Integer delFlg;

}
