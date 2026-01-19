package com.iwei.common.tool;

import com.iwei.common.config.AgentConfig;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能体工具类
 */
@Component
@Slf4j
public class AgentUtil {
    
    @Resource
    private AgentConfig agentConfig;
    
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3000, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(3000, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public String createSession(String agentId, String agentVersion) {
        if ("facade".equals(agentConfig.getAgentType())) {
            return createSessionFacade(agentId, agentVersion);
        } else if ("bailian".equals(agentConfig.getAgentType())) {
            return createSessionBailian(agentId, agentVersion);
        } else {
            return "default_session_id_" + System.currentTimeMillis();
        }
    }

    public void deleteSession(String sessionId, String agentId, String agentVersion) {
        if ("facade".equals(agentConfig.getAgentType())) {
            deleteSessionFacade(sessionId, agentId, agentVersion);
        } else if ("bailian".equals(agentConfig.getAgentType())){
            deleteSessionBailian(sessionId, agentId, agentVersion);
        }
    }

    public String uploadFile(String sessionId, String agentId, String agentVersion, String agentToken, File file) {
        if ("facade".equals(agentConfig.getAgentType())) {
            return uploadFileFacade(sessionId, agentId, agentVersion, agentToken, file);
        } else if ("bailian".equals(agentConfig.getAgentType())) {
            return uploadFileBailian(sessionId, agentId, agentVersion, agentToken, file);
        } else {
            return "default_file_id_" + System.currentTimeMillis();
        }
    }

    /**
     * 上传文件 (人工智能门户)
     */
    private String uploadFileFacade(String sessionId, String agentId, String agentVersion, String agentToken, File file) {

        String uploadUrl = agentConfig.getUploadFileUrl();

        // 创建 multipart 表单请求体
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("agentId", agentId != null ? agentId : "")
                .addFormDataPart("file", file.getName(), 
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .addFormDataPart("agentVersion", agentVersion != null ? agentVersion : "")
                .addFormDataPart("sessionId", sessionId != null ? sessionId : "");

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(multipartBuilder.build())
                .addHeader("Authorization", agentToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("文件上传失败，状态码：" + response.code() + "，信息：" + 
                        (response.body() != null ? response.body().string() : "无响应内容"));
                throw new RuntimeException("文件上传失败，状态码：" + response.code() + "，信息：" +
                        (response.body() != null ? response.body().string() : "无响应内容"));
            }
            
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Object data = responseMap.get("data");
            
            if (data != null) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                String fileId = String.valueOf(dataMap.get("fileId"));
                log.info("文件上传成功，返回ID: {}", fileId);
                return fileId;
            } else {
                throw new RuntimeException("文件上传失败，未找到 data 字段");
            }
            
        } catch (Exception e) {
            log.error("文件上传时发生异常", e);
            return "000";
        }
    }

    /**
     * 创建 session (人工智能门户)
     */
    private String createSessionFacade(String agentId, String agentVersion) {
        String createUrl = agentConfig.getCreateUrl();
        String createToken = agentConfig.getCreateToken();
        
        if (createUrl == null || createUrl.isEmpty()) {
            log.warn("Session创建URL未配置，返回默认sessionId");
            return "default_session_id_" + System.currentTimeMillis();
        }
        
        Map<String, Object> bodyMap = new ConcurrentHashMap<>();
        bodyMap.put("agentId", agentId == null ? "" : agentId);
        bodyMap.put("agentVersion", agentVersion == null ? "" : agentVersion);
        String bodyJson = JSON.toJSONString(bodyMap);

        Request request = new Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", createToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("创建Session失败，状态码：" + response.code() + "，信息：" + 
                        (response.body() != null ? response.body().string() : "无响应内容"));
                return "default_session_id_" + System.currentTimeMillis();
            }
            
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Map<String, Object> dataMap = JSON.parseObject(String.valueOf(responseMap.get("data")), Map.class);
            String sessionId = String.valueOf(dataMap.get("sessionId"));
            
            log.info("成功创建sessionId: {}", sessionId);
            return sessionId;
            
        } catch (Exception e) {
            log.error("请求创建session时发生异常", e);
            return "default_session_id_" + System.currentTimeMillis();
        }
    }
    
    /**
     * 删除 session (人工智能门户)
     */
    private void deleteSessionFacade(String sessionId, String agentId, String agentVersion) {
        if (sessionId == null || sessionId.startsWith("default_session_id_")) {
            // 不删除默认 sessionId
            return;
        }
        
        String deleteUrl = agentConfig.getDeleteUrl();
        String deleteToken = agentConfig.getDeleteToken();
        
        if (deleteUrl == null || deleteUrl.isEmpty()) {
            log.warn("Session删除URL未配置，无法删除sessionId: {}", sessionId);
            return;
        }

        Map<String, Object> bodyMap = new ConcurrentHashMap<>();
        bodyMap.put("agentId", agentId == null ? "" : agentId);
        bodyMap.put("agentVersion", agentVersion == null ? "" : agentVersion);
        String bodyJson = JSON.toJSONString(bodyMap);

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("Authorization", deleteToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                log.info("成功删除会话: {}", sessionId);
            } else {
                log.error("删除会话失败: {}, 响应码: {}, 响应内容: {}", 
                        sessionId, response.code(), 
                        response.body() != null ? response.body().string() : "无响应内容");
            }
        } catch (Exception e) {
            log.error("调用删除会话API时发生异常: sessionId={}", sessionId, e);
        }
    }

    /**
     * 创建 session (百炼)
     */
    private String createSessionBailian(String agentId, String agentVersion) {
        String createUrl = agentConfig.getCreateUrl();
        String createToken = agentConfig.getCreateToken();

        if (createUrl == null || createUrl.isEmpty()) {
            log.warn("Session创建URL未配置，返回默认sessionId");
            return "default_session_id_" + System.currentTimeMillis();
        }

        Map<String, Object> bodyMap = new ConcurrentHashMap<>();
        bodyMap.put("agentCode", agentId == null ? "" : agentId);
        bodyMap.put("agentVersion", agentVersion == null ? "" : agentVersion);
        String bodyJson = JSON.toJSONString(bodyMap);

        Request request = new Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", createToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("创建 Session 失败，状态码：" + response.code() + "，信息：" +
                        (response.body() != null ? response.body().string() : "无响应内容"));
                return "default_session_id_" + System.currentTimeMillis();
            }

            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Map<String, Object> dataMap = JSON.parseObject(String.valueOf(responseMap.get("data")), Map.class);
            String sessionId = String.valueOf(dataMap.get("uniqueCode"));

            log.info("成功创建 sessionId: {}", sessionId);
            return sessionId;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("请求创建 session 时发生异常", e);
            return "default_session_id_" + System.currentTimeMillis();
        }
    }

    /**
     * 删除 session (百炼)
     */
    private void deleteSessionBailian(String sessionId, String agentId, String agentVersion) {
        if (sessionId == null || sessionId.startsWith("default_session_id_")) {
            // 不删除默认 sessionId
            return;
        }

        String deleteUrl = agentConfig.getDeleteUrl();
        String deleteToken = agentConfig.getDeleteToken();

        if (deleteUrl == null || deleteUrl.isEmpty()) {
            log.warn("Session删除URL未配置，无法删除sessionId: {}", sessionId);
            return;
        }

        // 构建请求体
        Map<String, Object> bodyMap = new ConcurrentHashMap<>();
        bodyMap.put("agentCode", agentId == null ? "" : agentId);
        bodyMap.put("agentVersion", agentVersion == null ? "" : agentVersion);
        bodyMap.put("sessionId", sessionId);
        String bodyJson = JSON.toJSONString(bodyMap);

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("Authorization", deleteToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                log.info("成功删除会话: {}", sessionId);
            } else {
                log.error("删除会话失败: {}, 响应码: {}, 响应内容: {}",
                        sessionId, response.code(),
                        response.body() != null ? response.body().string() : "无响应内容");
            }
        } catch (Exception e) {
            log.error("调用删除会话API时发生异常: sessionId={}", sessionId, e);
        }
    }

    /**
     * 上传文件 (百炼)
     */
    private String uploadFileBailian(String sessionId, String agentId, String agentVersion, String agentToken, File file) {
        String uploadUrl = agentConfig.getUploadFileUrl();
        
        if (uploadUrl == null || uploadUrl.isEmpty()) {
            log.warn("文件上传URL未配置");
            return "default_file_id_" + System.currentTimeMillis();
        }

        // 创建multipart表单请求体
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("agentCode", agentId != null ? agentId : "")
                .addFormDataPart("file", file.getName(), 
                        RequestBody.create(file, MediaType.parse("application/octet-stream")))
                .addFormDataPart("agentVersion", agentVersion != null ? agentVersion : "")
                .addFormDataPart("sessionId", sessionId != null ? sessionId : "");

        Request.Builder requestBuilder = new Request.Builder()
                .url(uploadUrl)
                .post(multipartBuilder.build());

        if (agentToken != null && !agentToken.isEmpty()) {
            requestBuilder.addHeader("Authorization", agentToken);
        }

        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("文件上传失败，状态码：" + response.code() + "，信息：" + 
                        (response.body() != null ? response.body().string() : "无响应内容"));
                return null;
            }
            
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Object data = responseMap.get("data");
            
            if (data != null) {
                // 根据返回的数据结构提取文件ID或其他标识符
                if (data instanceof String) {
                    log.info("文件上传成功，返回ID: {}", data);
                    return (String) data;
                } else if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    String fileId = String.valueOf(dataMap.get("fileId"));
                    log.info("文件上传成功，返回ID: {}", fileId);
                    return fileId;
                } else {
                    log.info("文件上传成功，返回数据: {}", data);
                    return String.valueOf(data);
                }
            } else {
                log.warn("文件上传响应中未找到data字段: {}", responseJson);
                return null;
            }
            
        } catch (Exception e) {
            log.error("文件上传时发生异常", e);
            return null;
        }
    }

}