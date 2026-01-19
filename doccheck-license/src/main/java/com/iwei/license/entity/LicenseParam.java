package com.iwei.license.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author: 4611200202
 * @Date: 2024/10/11
 * @Description:
 */
@Data
public class LicenseParam {

    private List<String> ipList;

    private String key;

    private String date;

}
