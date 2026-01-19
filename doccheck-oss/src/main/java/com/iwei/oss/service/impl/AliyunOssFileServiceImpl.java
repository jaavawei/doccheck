package com.iwei.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.OSSErrorCode;
import com.aliyun.oss.model.OSSObject;
import com.iwei.oss.service.FileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * aliyun oss
 *
 * @auther: zhaokangwei
 */
@Slf4j
@ConditionalOnProperty(name = "storage.service.type", havingValue = "aliyun")
public class AliyunOssFileServiceImpl implements FileService {

    @Resource
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Override
    @SneakyThrows
    public String uploadFile(InputStream inputStream, String fileName, String container, String middlePath) {
        String bucket = getBucketName(container);
        String objectName = buildObjectName(middlePath, fileName);
        
        try {
            ossClient.putObject(bucket, objectName, inputStream);
            return generateFileUrl(bucket, objectName);
        } catch (OSSException oe) {
            log.error("OSS上传文件失败，错误代码：{}，错误信息：{}", oe.getErrorCode(), oe.getMessage());
            throw new RuntimeException("文件上传失败：" + oe.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public InputStream downloadFile(String url) {
        try {
            String[] parts = parseUrl(url);
            String bucket = parts[0];
            String objectName = parts[1];
            
            OSSObject ossObject = ossClient.getObject(bucket, objectName);
            return ossObject.getObjectContent();
        } catch (OSSException oe) {
            if (oe.getErrorCode().equals(OSSErrorCode.NO_SUCH_KEY)) {
                log.error("文件不存在，URL：{}", url);
                throw new RuntimeException("文件不存在");
            } else {
                log.error("OSS下载文件失败，错误代码：{}，错误信息：{}", oe.getErrorCode(), oe.getMessage());
                throw new RuntimeException("文件下载失败：" + oe.getMessage());
            }
        }
    }

    @Override
    @SneakyThrows
    public void deleteFile(String url) {
        try {
            String[] parts = parseUrl(url);
            String bucket = parts[0];
            String objectName = parts[1];
            
            ossClient.deleteObject(bucket, objectName);
        } catch (OSSException oe) {
            log.error("OSS删除文件失败，错误代码：{}，错误信息：{}", oe.getErrorCode(), oe.getMessage());
            throw new RuntimeException("文件删除失败：" + oe.getMessage());
        }
    }

    /**
     * 根据容器名称获取实际的bucket名称
     * @param container 容器名称
     * @return 实际bucket名称
     */
    private String getBucketName(String container) {
        // 如果传入了容器名称，则使用容器名称，否则使用默认bucket
        return (container != null && !container.isEmpty()) ? container : bucketName;
    }

    /**
     * 构建对象名称
     * @param middlePath 中间路径
     * @param fileName 文件名
     * @return 对象名称
     */
    private String buildObjectName(String middlePath, String fileName) {
        if (middlePath != null && !middlePath.isEmpty()) {
            // 确保路径不以/开头
            if (middlePath.startsWith("/")) {
                middlePath = middlePath.substring(1);
            }
            // 确保路径以/结尾
            if (!middlePath.endsWith("/")) {
                middlePath = middlePath + "/";
            }
            return middlePath + fileName;
        } else {
            return fileName;
        }
    }

    /**
     * 生成文件访问URL
     * @param bucket bucket名称
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    private String generateFileUrl(String bucket, String objectName) {
        // 从endpoint中提取域名部分，例如从"oss-cn-hangzhou.aliyuncs.com"提取"aliyuncs.com"
        String domain = endpoint;
        if (endpoint.startsWith("http://")) {
            domain = endpoint.substring(7);
        } else if (endpoint.startsWith("https://")) {
            domain = endpoint.substring(8);
        }
        return String.format("https://%s.%s/%s", bucket, domain, objectName);
    }

    /**
     * 解析URL获取bucket和对象名称
     * @param url 文件URL
     * @return 包含bucket和对象名称的数组
     */
    private String[] parseUrl(String url) throws java.net.MalformedURLException {
        URL urlObj = new URL(url);
        String path = urlObj.getPath();
        
        // 移除路径开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        // 根据阿里云OSS的URL格式解析bucket和对象名称
        // 例如：https://bucket-name.oss-cn-hangzhou.aliyuncs.com/object-name
        String host = urlObj.getHost();
        String bucket = host.split("\\.")[0];
        String objectName = path;
        
        return new String[]{bucket, objectName};
    }
}