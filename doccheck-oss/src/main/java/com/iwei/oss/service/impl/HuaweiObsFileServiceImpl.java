package com.iwei.oss.service.impl;

import com.iwei.oss.config.HuaweiObsProperties;
import com.iwei.oss.service.FileService;
import com.obs.services.ObsClient;
import com.obs.services.model.GetObjectRequest;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.DeleteObjectRequest;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import javax.annotation.PostConstruct;
import java.io.InputStream;

@Slf4j
@ConditionalOnProperty(name = "storage.service.type", havingValue = "huaweiobs")
public class HuaweiObsFileServiceImpl implements FileService {

    @Resource
    private HuaweiObsProperties obsProperties;

    private ObsClient obsClient;
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            this.bucketName = obsProperties.getBucketName();
            this.obsClient = new ObsClient(
                obsProperties.getAccessKeyId(),
                obsProperties.getSecretAccessKey(),
                obsProperties.getEndpoint()
            );
            
            log.info("华为云OBS初始化成功, endpoint: {}", obsProperties.getEndpoint());
        } catch (Exception e) {
            log.error("初始化华为云OBS失败", e);
            throw new RuntimeException("华为云OBS初始化失败", e);
        }
    }

    @Override
    @SneakyThrows
    public String uploadFile(InputStream inputStream, String fileName, String container, String middlePath) {
        try {
            String objectKey = buildObjectKey(container, middlePath, fileName);
            
            PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream);
            obsClient.putObject(request);
            
            // 生成文件访问URL
            return generateFileUrl(bucketName, objectKey);
        } catch (Exception e) {
            log.error("华为OBS上传文件失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public InputStream downloadFile(String url) {
        try {
            String objectKey = parseObjectKeyFromUrl(url);
            
            GetObjectRequest request = new GetObjectRequest(bucketName, objectKey);
            ObsObject obsObject = obsClient.getObject(request);
            return obsObject.getObjectContent();
        } catch (Exception e) {
            log.error("华为OBS下载文件失败", e);
            throw new RuntimeException("文件下载失败：" + e.getMessage());
        }
    }

    @Override
    @SneakyThrows
    public void deleteFile(String url) {
        try {
            String objectKey = parseObjectKeyFromUrl(url);
            
            DeleteObjectRequest request = new DeleteObjectRequest(bucketName, objectKey);
            obsClient.deleteObject(request);
        } catch (Exception e) {
            log.error("华为OBS删除文件失败", e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 构建对象键（在桶中的路径）
     * @param container 容器名称（作为路径前缀）
     * @param middlePath 中间路径
     * @param fileName 文件名
     * @return 对象键
     */
    private String buildObjectKey(String container, String middlePath, String fileName) {
        StringBuilder keyBuilder = new StringBuilder();
        
        // 添加container作为路径前缀
        if (container != null && !container.isEmpty()) {
            keyBuilder.append(container);
            if (!container.endsWith("/")) {
                keyBuilder.append("/");
            }
        }
        
        // 添加中间路径
        if (middlePath != null && !middlePath.isEmpty()) {
            if (middlePath.startsWith("/")) {
                middlePath = middlePath.substring(1);
            }
            if (!middlePath.isEmpty()) {
                keyBuilder.append(middlePath);
                if (!middlePath.endsWith("/")) {
                    keyBuilder.append("/");
                }
            }
        }
        
        // 添加文件名
        keyBuilder.append(fileName);
        return keyBuilder.toString();
    }

    /**
     * 从URL中解析对象键
     * @param url 文件URL
     * @return 对象键
     */
    private String parseObjectKeyFromUrl(String url) throws java.net.MalformedURLException {
        java.net.URL urlObj = new java.net.URL(url);
        String path = urlObj.getPath();
        
        // 移除路径开头的斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        return path;
    }

    /**
     * 生成文件访问URL
     * @param bucketName 桶名称
     * @param objectKey 对象键
     * @return 文件访问URL
     */
    private String generateFileUrl(String bucketName, String objectKey) {
        // 根据华为云OBS的URL格式生成访问链接
        return String.format("https://%s.%s/%s", bucketName, obsProperties.getEndpoint(), objectKey);
    }
}