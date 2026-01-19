package com.iwei.task.entity.vo;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.task.entity.TaskDuplicateDoc;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 查重任务信息表（任务总表）实体类
 *
 * @auther: zhaokangwei
 */
@Data
public class TaskDuplicateInfoVo {

    /** 主键自增id */
    private Integer id;

    /** 查重任务名称 */
    private String taskDuplicateName;

    /** 查重库id */
    private Integer repositoryDuplicateId;

    /** 查重规则id */
    private Integer ruleDuplicateId;

    /** 查重类型/查重范围 */
    private Integer duplicateType;

    /** 查重范围 */
    private Integer duplicateRange;

    /** 结构类型：0-结构化数据  1-非结构化数据 */
    private Integer dataType;

    /** 任务状态： 0-未开始  1-执行中  2-已完成 */
    private Integer taskStatus;

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

    /** 待查重文件 */
    private List<MultipartFile> files;

    /** 子任务总数量 */
    private Integer taskNum;

    /** 待执行子任务数量 */
    private Integer toDoNum;

    /** 正在执行子任务数量 */
    private Integer doingNum;

    /** 已完成子任务数量 */
    private Integer doneNum;

    /** 失败子任务数量 */
    private Integer failNum;

    /** 子任务列表 */
    private List<TaskDuplicateDocVo> subtasks;

    /** 查重库名称 */
    private String repositoryDuplicateName;

    /** 查重规则名称 */
    private String ruleDuplicateName;

    // 下面为新疆任务所需字段
    private Integer totalGroupNum;
    private Integer duplicateGroupNum;
    private Double duplicatePercent;
    private Map<String,Integer> duplicateMsgMap;
    private List<RepositoryDocVo> repositoryDocs;
    private Object orgCondition;
    private Integer total;
    private Integer pageCount;
    private List<Map<String, String>> specialItemConditionList;
}