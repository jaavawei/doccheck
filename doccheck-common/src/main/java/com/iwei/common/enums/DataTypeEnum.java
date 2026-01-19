package com.iwei.common.enums;

import lombok.Getter;

/**
 * 查重文档数据类型枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum DataTypeEnum {

    STRUCTURED(0,"结构化"),
    UNSTRUCTURED(1,"非结构化");

    public final int code;

    public final String desc;

    DataTypeEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static DataTypeEnum getByCode(int codeVal){
        for(DataTypeEnum dataTypeEnum : DataTypeEnum.values()){
            if(dataTypeEnum.code == codeVal){
                return dataTypeEnum;
            }
        }
        return null;
    }

}