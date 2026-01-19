package com.iwei.common.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 属性配置表实体类
 *
 */
@Data
public class AttrTable implements Serializable {
    /** 主键 */
    private Integer id;

    /** 配置项名称 */
    private String name;

    /** 属性1 */
    private String attribute1;

    /** 属性2 */
    private String attribute2;

    /** 属性3 */
    private String attribute3;

    /** 属性4 */
    private String attribute4;

    /** 属性5 */
    private String attribute5;

    /** 属性6 */
    private String attribute6;

    /** 属性7 */
    private String attribute7;
    
    private static final long serialVersionUID = 1L;
}