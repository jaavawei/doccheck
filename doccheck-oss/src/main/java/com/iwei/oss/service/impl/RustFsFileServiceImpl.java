package com.iwei.oss.service.impl;

import com.iwei.oss.config.RustFsConfig;
import com.iwei.oss.service.FileService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@ConditionalOnProperty(name = "storage.service.type", havingValue = "rustfs")
public class RustFsFileServiceImpl implements FileService {

    @Resource
    private RustFsConfig rustFsConfig;

    @Override
    @SneakyThrows
    public String uploadFile(InputStream inputStream, String fileName, String container, String middlePath) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String objectName = buildObjectName(middlePath, fileName);
            String bucket = container != null && !container.isEmpty() ? container : "default";
            
            // 构建上传URL
            String url = String.format("%s/upload?bucket=%s&object=%s", 
                    rustFsConfig.getServerUrl(), bucket, objectName);
            
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + rustFsConfig.getAccessToken());
            
            HttpEntity entity = new InputStreamEntity(inputStream);
            httpPost.setEntity(entity);
            
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return String.format("%s/file/%s/%s", rustFsConfig.getServerUrl(), bucket, objectName);
                } else {
                    log.error("RustFS上传文件失败，状态码：{}", statusCode);
                    throw new RuntimeException("文件上传失败，状态码：" + statusCode);
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    @Override
    @SneakyThrows
    public InputStream downloadFile(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Authorization", "Bearer " + rustFsConfig.getAccessToken());
            
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // 读取响应内容到字节数组
                    HttpEntity entity = response.getEntity();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    entity.writeTo(buffer);
                    return new ByteArrayInputStream(buffer.toByteArray());
                } else if (statusCode == 404) {
                    log.error("文件不存在，URL：{}", url);
                    throw new RuntimeException("文件不存在");
                } else {
                    log.error("RustFS下载文件失败，状态码：{}", statusCode);
                    throw new RuntimeException("文件下载失败，状态码：" + statusCode);
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    @Override
    @SneakyThrows
    public void deleteFile(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpDelete httpDelete = new HttpDelete(url);
            httpDelete.setHeader("Authorization", "Bearer " + rustFsConfig.getAccessToken());
            
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.error("RustFS删除文件失败，状态码：{}", statusCode);
                    throw new RuntimeException("文件删除失败，状态码：" + statusCode);
                }
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
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
}