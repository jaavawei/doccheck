package com.iwei.common.enums;

import lombok.Getter;

/**
 * 定时任务状态枚举类
 *
 * @author: zhaokangwei
 */
@Getter
public enum TaskStatusEnum {
    UNEXECUTED(0, "未执行"),
    EXECUTING(1, "执行中"),
    COMPLETED(2, "执行完成"),
    FAILED(3, "执行失败"),
    INVALID(4, "已失效");

    public final int code;

    public final String desc;

    TaskStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param codeVal 状态码
     * @return 对应的枚举实例，若未找到则返回null
     */
    public static TaskStatusEnum getByCode(int codeVal) {
        for (TaskStatusEnum statusEnum : TaskStatusEnum.values()) {
            if (statusEnum.code == codeVal) {
                return statusEnum;
            }
        }
        return null;
    }
}

