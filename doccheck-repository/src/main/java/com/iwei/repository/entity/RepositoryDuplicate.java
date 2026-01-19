package com.iwei.repository.entity;

import lombok.Data;
import java.util.Date;

/**
 * 查重库表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryDuplicate {
    /** 主键自增id */
    private Integer id;

    /** 提取规则id */
    private Integer ruleExtractId;

    /** 查重库名称 */
    private String repositoryDuplicateName;

    /** 数据来源：1-本地文档库   2-外部知识库 */
    private Integer dataSource;

    /** API URL (外部知识库) */
    private String apiUrl;

    /** Token (外部知识库) */
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
