package com.iwei.common.enums;

import lombok.Getter;

/**
 * 数据来源枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum DataSourceEnum {
    INTERNAL(0,"内部文档库"),
    EXTERNAL(1,"外部资源库");

    public int code;

    public String desc;

    DataSourceEnum(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static DataSourceEnum getByCode(int codeVal){
        for(DataSourceEnum dataSourceEnum : DataSourceEnum.values()){
            if(dataSourceEnum.code == codeVal){
                return dataSourceEnum;
            }
        }
        return null;
    }
}
