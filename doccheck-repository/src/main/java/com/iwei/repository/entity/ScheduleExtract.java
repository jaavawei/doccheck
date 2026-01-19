package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

/**
 * 文档提取任务表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class ScheduleExtract {

    /** 主键ID */
    private Integer id;

    /** 来源表id */
    private Integer sourceId;

    /** 任务状态 */
    private Integer taskStatus;

    /** 任务来源 */
    private Integer taskSource;

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
    
    /** 重试次数 */
    private Integer retryCount = 0;
}
