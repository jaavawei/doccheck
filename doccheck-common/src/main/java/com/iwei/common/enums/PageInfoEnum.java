package com.iwei.common.enums;

import lombok.Getter;

/**
 * 分页信息枚举类
 *
 * @auther: zhaokangwei
 */
@Getter
public enum PageInfoEnum {
    PAGE_NO(1, "页码"),
    PAGE_SIZE(10, "页面大小");

    public final int code;

    public final String des;

    PageInfoEnum(int code, String des) {
        this.code = code;
        this.des = des;
    }
}
