package com.iwei.task.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * 查重任务信息表（任务总表）实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateInfo {
    /** 主键自增id */
    private Integer id;

    /** 查重任务名称 */
    private String taskDuplicateName;

    /** 查重库id */
    private Integer repositoryDuplicateId;

    /** 查重规则id */
    private Integer ruleDuplicateId;

    /** 查重类型/查重范围 */
    private Integer duplicateType;

    /** 结构类型：0-结构化数据  1-非结构化数据 */
    private Integer dataType;

    /** 任务状态： 0-未开始  1-执行中  2-已完成 */
    private Integer taskStatus;

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