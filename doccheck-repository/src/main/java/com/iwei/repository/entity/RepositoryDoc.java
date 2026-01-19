package com.iwei.repository.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * 文档库表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryDoc {
    /** 主键自增id */
    private Integer id;

    /** 提取规则id */
    private Integer ruleExtractId;

    /** 文档名称 */
    private String docName;

    /** 项目名称 */
    private String projectName;

    /** 项目类型 */
    private Integer projectType;

    /** 提取状态：0-未开始  1-执行中 2-已完成 */
    private Integer status;

    /** 文档存储路径 */
    private String docUrl;

    /** 文档提取错误信息 */
    private String errorMsg;

    /** 文档提取内容（根据提取规则提取） */
    private String extractContent;

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

    /** xj */
    private Integer duplicateFlg; // 重复标识
    private String projectCode; // 项目编码
    private String implOrg; // 实施单位
    private String planYear; // 计划年度
    private String projectMsg; // 项目关键信息（站线名称）
    private Integer duplicateStatus; // 查重任务状态：-1-没有任务 0-等待中 1-执行中 2-已完成

}