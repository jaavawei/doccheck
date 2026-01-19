package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

/**
 * 审查库-提取规则 映射表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryReviewExtractMapping {
    /** 主键自增id */
    private Integer id;

    /** 审查库id */
    private Integer repositoryReviewId;

    /** 提取规则id */
    private Integer ruleExtractId;

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