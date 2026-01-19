package com.iwei.common.enums;

import lombok.Getter;

/**
 * 删除状态枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum AttributeEnum {

    SESSION_CREATE(1,"创建session相关配置"),
    SESSION_DELETE(2,"删除session相关配置");

    public final int code;

    public final String desc;

    AttributeEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static AttributeEnum getByCode(int codeVal){
        for(AttributeEnum attributeEnum : AttributeEnum.values()){
            if(attributeEnum.code == codeVal){
                return attributeEnum;
            }
        }
        return null;
    }

}
