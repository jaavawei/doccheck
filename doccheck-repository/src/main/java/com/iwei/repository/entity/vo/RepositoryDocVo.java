package com.iwei.repository.entity.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 文档库表实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class RepositoryDocVo {
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

    /** 文档全部内容（解析得出） */
    private String docContent;

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

    /** 上传文件 */
    private List<MultipartFile> files;

    /** 提取规则内容 */
    private String ruleExtractContent;

    /** 提取规则名称 */
    private String ruleExtractName;

    /** 文档流 */
    private InputStream inputStream;

    /** 与 kkview 拼接后的预览 url */
    private String previewUrl;

    /** 多个文档id，用于批量操作 */
    private List<Integer> ids;

    /** 多个提取规则id */
    private List<Integer> ruleExtractIds;

    /** 多个项目名称 */
    private List<String> projectNames;

    /** 存在该文档的对比组（新疆） */
    private Object groups;

}