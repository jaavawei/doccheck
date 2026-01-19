package com.iwei.oss.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;


/**
 * 文件存储service接口
 *
 * @auther: zhaokangwei
 */

public interface FileService {

    /**
     * 上传文件
     */
    String uploadFile(InputStream inputStream, String fileName, String container, String middlePath);

    /**
     * 下载文件
     */
    InputStream downloadFile(String url);

    /**
     * 删除文件
     */
    void deleteFile(String url);

}
