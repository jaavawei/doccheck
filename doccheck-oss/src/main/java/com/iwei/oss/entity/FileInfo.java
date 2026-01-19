package com.iwei.oss.entity;

import lombok.Data;

/**
 * 文件类
 *
 * @auther: zhaokangwei
 */
@Data
public class FileInfo {

    private String fileName;

    private Boolean directoryFlag;

    private String etag;
}
