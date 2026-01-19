package com.iwei.task.entity;

import lombok.Data;

@Data
public class ProjectStationLineMapping {
    private Integer id;
    private String projectName;
    private String projectCode;
    private String stationLineName;
    private String stationLineCode;
    private String implOrg;
    private String planYear;
    private String projectMsg;
    private Integer delFlg;
}
