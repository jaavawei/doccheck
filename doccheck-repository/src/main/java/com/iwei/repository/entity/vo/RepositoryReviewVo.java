package com.iwei.repository.entity.vo;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.rule.entity.RuleExtract;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 审查库表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryReviewVo {
    /** 主键自增id */
    private Integer id;

    /** 审查库名称 */
    private String repositoryReviewName;

    /** 数据来源：0-内部文档库  1-外部知识库 */
    private Integer dataSource;

    /** 项目名称 */
    private String projectName;

    /** 项目类型 */
    private Integer projectType;

    /** 项目年份 */
    private Integer projectYear;

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

    /** 审查库对应的文档库id */
    private List<Integer> repositoryDocIds;

    /** 审查库关联的提取规则id */
    private List<Integer> ruleExtractIds;

    /** 审查库关联的文档库 */
    private List<RepositoryDoc> repositoryDocs;

    /** 审查库关联的提取规则 */
    private List<RuleExtract> ruleExtracts;

    /** 审查库关联的提取规则名称 */
    private List<String> ruleExtractNames;

    /** 审查库关联的文档项目名称 */
    private List<String> projectNames;
}