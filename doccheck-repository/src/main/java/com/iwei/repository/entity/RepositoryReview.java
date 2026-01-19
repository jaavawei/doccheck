package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

/**
 * 审查库表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryReview {
    /** 主键自增id */
    private Integer id;

    /** 审查库名称 */
    private String repositoryReviewName;

    /** 数据来源：1-本地文档库   2-外部知识库 */
    private Integer dataSource;

    /** 项目名称 */
    private String projectName;

    /** 项目类型 */
    private Integer projectType;

    /** 项目年份 */
    private Integer projectYear;

    /** 外部资源库apiUrl */
    private String apiUrl;

    /** 外部资源库apiToken */
    private String apiToken;

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