package com.iwei.task.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/*
 * 新疆调度表实体类
 */
@Data
public class ScheduleXjVo implements Serializable {

    /*
     * 主键 id
     */
    private Integer id;

    /*
     * 源表 id：info 表 id
     */
    private Integer sourceId;

    /*
     * 第一个文档 id
     */
    private Integer docFId;

    /*
     * 第二个文档 id
     */
    private Integer docSId;

    /*
     * 状态
     */
    private Integer taskStatus;

    /*
     * 站线名称
     */
    private String stationLineName;

    /*
     * 重复标识
     */
    private Integer duplicateFlg;

    /*
     * 重复信息：例如 多头申报
     */
    private String duplicateMsg;

    /*
     * 错误信息
     */
    private String errorMsg;

    /*
     * 优化建议
     */
    private String advice;

    /*
     * 删除标识
     */
    private Integer delFlg;

    /*
     * 比对的组
     */
    private List<ScheduleXjVo> groups;
}
