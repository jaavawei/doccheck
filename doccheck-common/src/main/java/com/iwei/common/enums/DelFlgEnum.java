package com.iwei.common.enums;

import lombok.Getter;

/**
 * 删除状态枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum DelFlgEnum {

    UN_DELETED(0,"未删除"),
    DELETED(1,"已删除");

    public final int code;

    public final String desc;

    DelFlgEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static DelFlgEnum getByCode(int codeVal){
        for(DelFlgEnum resultCodeEnum : DelFlgEnum.values()){
            if(resultCodeEnum.code == codeVal){
                return resultCodeEnum;
            }
        }
        return null;
    }

}
