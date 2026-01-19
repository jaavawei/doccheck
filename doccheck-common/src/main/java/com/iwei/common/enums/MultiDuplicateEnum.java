package com.iwei.common.enums;

import lombok.Getter;

/**
 * 删除状态枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum MultiDuplicateEnum {

    STATION_LINE(0,"按站线查重"),
    DEVICE(1,"按设备查重");


    public final int code;

    public final String desc;

    MultiDuplicateEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static MultiDuplicateEnum getByCode(int codeVal){
        for(MultiDuplicateEnum multiDuplicateEnum : MultiDuplicateEnum.values()){
            if(multiDuplicateEnum.code == codeVal){
                return multiDuplicateEnum;
            }
        }
        return null;
    }

}
