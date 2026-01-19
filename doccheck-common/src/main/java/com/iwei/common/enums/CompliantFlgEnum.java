package com.iwei.common.enums;

import lombok.Getter;

/**
 * 删除状态枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum CompliantFlgEnum {

    COMPLIANT(0,"合规"),
    UN_COMPLIANT(1,"不合规");

    public final int code;

    public final String desc;

    CompliantFlgEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static CompliantFlgEnum getByCode(int codeVal){
        for(CompliantFlgEnum delFlgEnum : CompliantFlgEnum.values()){
            if(delFlgEnum.code == codeVal){
                return delFlgEnum;
            }
        }
        return null;
    }

}
