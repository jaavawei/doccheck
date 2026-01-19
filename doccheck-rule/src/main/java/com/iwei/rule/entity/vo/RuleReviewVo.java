package com.iwei.rule.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 审查规则表 Vo
 *
 * @auther: zhaokangwei
 */
@Data
public class RuleReviewVo {

    /** 自增主键id */
    private Integer id;

    /** 审查规则名称 */
    private String ruleName;

    /** 审查规则内容 */
    private String content;

    /** 智能体api */
    private String agentUrl;

    /** 智能体token */
    private String agentToken;

    /** 智能体id */
    private String agentId;

    /** 智能体版本 */
    private String agentVersion;

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