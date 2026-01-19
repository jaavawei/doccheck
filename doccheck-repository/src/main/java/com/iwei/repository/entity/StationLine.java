package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

/**
 * 站线实体类
 *
 * @author: zhaokangwei
 */
@Data
public class StationLine {

    /** 自增主键id */
    private Integer id;

    /** 站线名称 */
    private String stationLineName;

    /** 站线编码 */
    private String stationLineCode;

    /** 创建时间 */
    private Date createdAt;

    /** 创建人 */
    private Integer createdBy;

    /** 更新时间 */
    private Date updatedAt;

    /** 更新人 */
    private Integer updatedBy;

    /** 删除标识 */
    private Integer delFlg;
}