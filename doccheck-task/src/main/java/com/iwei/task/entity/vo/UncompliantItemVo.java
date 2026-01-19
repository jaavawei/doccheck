package com.iwei.task.entity.vo;

import lombok.Data;

/**
 * 不合规项 Vo
 *
 * @auther: zhaokangwei
 */
@Data
public class UncompliantItemVo {

    /** 审查规则名称 */
    private String ruleReviewName;

    /** 不合规内容 */
    private String questionMsg;

    /** 建议 */
    private String advice;

}