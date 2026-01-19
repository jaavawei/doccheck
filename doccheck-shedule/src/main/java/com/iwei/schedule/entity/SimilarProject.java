package com.iwei.schedule.entity;

import lombok.Data;

/**
 * 查重结果类
 *
 * @author: zhaokangwei
 */
@Data
public class SimilarProject {

    /** 项目id */
    private String projectId;

    /** 项目名称 */
    private String projectName;

    /** 提取内容 */
    private String extractContent;

    /** 总分 */
    private String totalScore;

    /** 相似维度描述 */
    private String similarityDescription;

}

