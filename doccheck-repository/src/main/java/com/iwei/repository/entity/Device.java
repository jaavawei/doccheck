package com.iwei.repository.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Device {

    /** 自增主键id */
    private Integer id;

    /** 设备名称 */
    private String deviceName;

    /** 设备编码 */
    private String deviceCode;

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
