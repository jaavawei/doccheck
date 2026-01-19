package com.iwei.common.enums;

import lombok.Data;
import lombok.Getter;

/**
 * 结构化数据导出类型枚举类
 *
 * @author:zhaokangwei
 */
@Getter
public enum ExportTypeEnum {

    EXCEL(0, "excel"),
    JSON(1, "json");

    private final Integer code;
    private final String desc;

    ExportTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ExportTypeEnum getByCode(int codeVal){
        for(ExportTypeEnum exportTypeEnum : ExportTypeEnum.values()){
            if(exportTypeEnum.code == codeVal){
                return exportTypeEnum;
            }
        }
        return null;
    }
}
