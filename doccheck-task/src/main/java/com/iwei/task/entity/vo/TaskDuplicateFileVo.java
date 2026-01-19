package com.iwei.task.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 查重任务文件表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateFileVo {
    /**
     * 主键自增id
     */
    private Integer id;

    /**
     * 主任务表id，关联主任务信息
     */
    private Integer infoId;

    /**
     * 文件类型（如：doc、pdf、txt等）
     */
    private String fileType;

    /**
     * 文件存储路径
     */
    private String docUrl;

    /**
     * 文档提取状态：0-未处理，1-处理中，2-处理完成，3-处理失败
     */
    private Integer fileStatus;

    /**
     * 生成子任务数
     */
    private Integer taskDocNum;

    /**
     * 解析错误信息，处理失败时存储错误详情
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新时间，自动更新
     */
    private Date updatedAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer delFlg;
}
