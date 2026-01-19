package com.iwei.common.enums;

import lombok.Getter;

/**
 * 解析定时任务来源
 *
 * @auther: zhaokangwei
 */
@Getter
public enum ScheduleTaskSourceEnum {

    REPOSITORY(0,"文档库"),
    TASK(1,"查重任务");

    public int code;

    public String desc;

    ScheduleTaskSourceEnum(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public static ScheduleTaskSourceEnum getByCode(int codeVal){
        for(ScheduleTaskSourceEnum scheduleTaskSourceEnum : ScheduleTaskSourceEnum.values()){
            if(scheduleTaskSourceEnum.code == codeVal){
                return scheduleTaskSourceEnum;
            }
        }
        return null;
    }
}
