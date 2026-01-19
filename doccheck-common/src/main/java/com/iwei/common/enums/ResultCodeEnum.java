package com.iwei.common.enums;

import lombok.Getter;

/**
 * 返回代码枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum ResultCodeEnum {

    LICENSE_AUTH_FAILD(13,"授权未通过"),

    SUCCESS(200,"成功"),
    FAIL(500,"失败");

    public int code;

    public String desc;

    ResultCodeEnum(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static ResultCodeEnum getByCode(int codeVal){
        for(ResultCodeEnum resultCodeEnum : ResultCodeEnum.values()){
            if(resultCodeEnum.code == codeVal){
                return resultCodeEnum;
            }
        }
        return null;
    }

}
