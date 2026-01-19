package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

/**
 * 文档库与设备映射实体类
 *
 * @author: zhaokangwei
 */
@Data
public class RepositoryDocDeviceMapping {

    /** 自增主键id */
    private Integer id;

    /** 设备id */
    private Integer deviceId;

    /** 文档库id */
    private Integer docId;

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