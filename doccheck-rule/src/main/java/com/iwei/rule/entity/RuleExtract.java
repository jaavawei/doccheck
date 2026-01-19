package com.iwei.rule.entity;

import lombok.Data;
import java.util.Date;

/**
 * 提取规则表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RuleExtract {
    /** 主键自增id */
    private Integer id;

    /** 提取规则名称 */
    private String ruleName;

    /** 提取规则要素 */
    private String elements;

    /** 智能体提示词 */
    private String prompt;

    /** 智能体api */
    private String agentUrl;

    /** 智能体id */
    private String agentId;

    /** 智能体token */
    private String agentToken;
    
    /** 智能体版本 */
    private String agentVersion;

    /** Minio Bucket名 */
    private String bucket;

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