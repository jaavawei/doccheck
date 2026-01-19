package com.iwei.common.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: 4611200202
 * @Date: 2024/10/12
 * @Description:
 */
@Data
public class LicenseEntity {
    private List<String> macList;
    private Date date;
}
