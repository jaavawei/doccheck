package com.iwei.oss.service.impl;

import com.iwei.oss.service.FileService;
import com.iwei.oss.util.MinioUtil;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Minio 文件存储服务实现
 *
 * @auther: zhaokangwei
 */
@Slf4j
@ConditionalOnProperty(name = "storage.service.type", havingValue = "minio")
public class MinioFileServiceImpl implements FileService {

    @Resource
    private MinioUtil minioUtil;

    /**
     * minioUrl
     */
    @Value("${minio.url}")
    private String url;

    /**
     * 上传文件
     *
     * @param uploadFile 上传的文件
     * @param container 容器，对应桶
     * @param path 中间路径
     * @return 文件路径，需拼接文件原始文件名
     */
    @Override
    @SneakyThrows
    public String uploadFile(InputStream inputStream, String fileName, String container, String middlePath) {
        if(!minioUtil.bucketExists(container)){
            minioUtil.createBucket(container);
        }
        // objectName 为容器后的全部路径
        String objectName = null;
        if (middlePath != null) {
            objectName = middlePath + "/" + fileName;
        } else {
            objectName = fileName;
        }
        minioUtil.uploadFile(inputStream, container, objectName);
        return url + "/" + container + "/" + objectName;
    }

    /**
     * 下载文件
     *
     * @param url 文件路径
     */
    @Override
    @SneakyThrows
    public InputStream downloadFile(String url) {
        // 根据 文件url 解析出 container(bucket) 与 objectName
        String[] params = parseUrl(url);
        String bucket = params[0];
        String objectName = params[1];
        InputStream inputStream = minioUtil.downLoad(bucket, objectName);
        return inputStream;
    }

    /**
     * 删除文件
     *
     * @param url 文件路径
     */
    @Override
    @SneakyThrows
    public void deleteFile(String url) {
        // 根据 文件url 解析出 container(bucket) 与 objectName
        String[] params = parseUrl(url);
        String bucket = params[0];
        String objectName = params[1];
        minioUtil.deleteObject(bucket, objectName);
    }

    /**
     * 解析文件url
     */
    private String[] parseUrl(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        // 获取URL的路径部分
        String path = url.getPath();
        // 去除路径开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        // 分割路径，第一个部分是bucket，剩下的部分组合起来是objectName
        String[] pathSegments = path.split("/", 2);
        if (pathSegments.length < 1) {
            throw new MalformedURLException("URL无法正确解析");
        }
        if (pathSegments.length > 1) {
            String bucket = pathSegments[0];
            String objectName = pathSegments.length > 1 ? pathSegments[1] : "";
        }
        String bucket = pathSegments[0];
        String objectName = pathSegments.length > 1 ? pathSegments[1] : "";

        return new String[]{bucket, objectName};
    }
}
