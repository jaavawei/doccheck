package com.iwei.schedule.entity;

import lombok.Data;

/**
 * 查重项目信息类
 *
 * @author: zhaokangwei
 */
@Data
public class ProjectQuery {

    /** 项目id */
    private String projectId;

    /** 项目名称 */
    private String projectName;

    /** 建设内容 */
    private String constructionContent;

}
