package com.iwei.schedule.processor;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.config.AgentConfig;
import com.iwei.common.enums.DataSourceEnum;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.common.tool.JsonUtil;
import com.iwei.common.tool.AgentUtil;
import com.iwei.oss.service.FileService;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.mapper.RepositoryDocMapper;
import com.iwei.repository.mapper.RepositoryDuplicateMapper;
import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.mapper.RuleDuplicateMapper;
import com.iwei.schedule.config.ScheduleTaskConfig;
import com.iwei.schedule.entity.ProjectQuery;
import com.iwei.schedule.entity.SimilarProject;
import com.iwei.schedule.generator.ReportGenerator;
import com.iwei.task.entity.ScheduleDuplicate;
import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.entity.TaskDuplicateInfo;
import com.iwei.task.entity.TaskDuplicateResult;
import com.iwei.task.entity.TaskDuplicateSnapshot;
import com.iwei.task.mapper.ScheduleDuplicateMapper;
import com.iwei.task.mapper.TaskDuplicateDocMapper;
import com.iwei.task.mapper.TaskDuplicateInfoMapper;
import com.iwei.task.mapper.TaskDuplicateResultMapper;
import com.iwei.task.mapper.TaskDuplicateSnapshotMapper;
import javax.annotation.Resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DuplicateTaskProcessor implements DuplicateTaskProcessorAdaptor {
    @Resource
    private ScheduleDuplicateMapper scheduleDuplicateMapper;
    @Resource
    private TaskDuplicateInfoMapper taskDuplicateInfoMapper;
    @Resource
    private TaskDuplicateDocMapper taskDuplicateDocMapper;
    @Resource
    private TaskDuplicateSnapshotMapper taskDuplicateSnapshotMapper;
    @Resource
    private TaskDuplicateResultMapper taskDuplicateResultMapper;
    @Resource
    private RepositoryDocMapper repositoryDocMapper;
    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    private RuleDuplicateMapper ruleDuplicateMapper;
    @Resource
    private FileService fileService;
    
    @Resource
    private AgentUtil agentUtil;

    @Resource
    private AgentConfig agentConfig;
    
    @Resource
    private ScheduleTaskConfig scheduleTaskConfig;
    


    /**
     * 处理单个任务
     */
    @Override
    public void processTask(ScheduleDuplicate task) {
        log.info("开始处理任务：" + task.getId());

        // 更新任务状态为处理中
        ScheduleDuplicate scheduleduplicate = new ScheduleDuplicate();
        scheduleduplicate.setId(task.getId());
        scheduleduplicate.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        boolean statusUpdated = scheduleDuplicateMapper.update(scheduleduplicate);
        // 多线程竞争时使用乐观锁
        if (!statusUpdated) {
            log.info("任务{}状态已被其他线程修改，跳过处理", task.getId());
            return;
        }
        // 同步修改子任务表的状态为处理中，主任务表的状态等到查询时再修改
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        taskDuplicateDoc.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        taskDuplicateDoc.setId(task.getDocId());
        taskDuplicateDocMapper.updateById(taskDuplicateDoc);

        try {
            TaskDuplicateDoc doc = taskDuplicateDocMapper.queryById(task.getDocId());
            String extractContent = doc.getExtractContent(); // 获取被查重文档的提取内容
            TaskDuplicateInfo info = taskDuplicateInfoMapper.queryById(doc.getInfoId());
            RuleDuplicate ruleDuplicate = ruleDuplicateMapper.queryById(info.getRuleDuplicateId());
            String prompt = ruleDuplicate.getContent();
            String url = ruleDuplicate.getAgentUrl();
            String agentId = ruleDuplicate.getAgentId();
            String agentVersion = ruleDuplicate.getAgentVersion();
            String token = ruleDuplicate.getAgentToken();
            RepositoryDuplicate repositoryDuplicate = repositoryDuplicateMapper.queryById(info.getRepositoryDuplicateId());
            if (repositoryDuplicate.getDataSource() == DataSourceEnum.EXTERNAL.getCode()) {
                // 外部知识库
                String result = duplicate(extractContent, null, null, url, agentId, agentVersion, token);
                if (result != null) {
                    Map<String, String> resultMap = JSON.parseObject(result, Map.class);
                    String report = resultMap.get("text");
                    String msg = resultMap.get("type");
                    // 更新查重子任务表
                    taskDuplicateDoc.setDuplicateResult(report);
                    if (! StringUtils.isBlank(msg)) {
                        taskDuplicateDoc.setDuplicateFlg(1);
                        taskDuplicateDoc.setDuplicateMsg(msg);
                    } else {
                        taskDuplicateDoc.setDuplicateFlg(0);
                    }
                } else {
                    // 获取查重结果失败
                    log.error("解析智能体返回结果失败：{}", result);
                    taskDuplicateDoc.setDuplicateResult("解析智能体返回结果失败：" + result);
                    taskDuplicateDoc.setErrorMsg("解析智能体返回结果失败");
                }

                // 更新任务状态为已完成
                scheduleduplicate.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
                scheduleDuplicateMapper.update(scheduleduplicate);
                taskDuplicateDoc.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
                taskDuplicateDocMapper.updateById(taskDuplicateDoc);
                return;
            }
            TaskDuplicateSnapshot snapshot = taskDuplicateSnapshotMapper.queryByInfoId(doc.getInfoId());
            // 拿到查重库快照中的文档id和查重规则快照
            String snapshotRepo = snapshot.getSnapshotRepo();
            List<Integer> repoDocIdList = JSON.parseArray(snapshotRepo, Integer.class);

            // 将被查重文档与查重库中所有文档进行比对
            TaskDuplicateResult taskDuplicateResult = new TaskDuplicateResult();
            taskDuplicateResult.setTaskDocId(task.getDocId());
            Integer resultId = 0;
            BigDecimal percent = BigDecimal.ZERO;
            ProjectQuery projectQuery = new ProjectQuery();
            projectQuery.setProjectName(doc.getDocName());
            projectQuery.setProjectId(doc.getId().toString());
            Map<String, String> extracttextMap  = JSON.parseObject(extractContent, Map.class);
            projectQuery.setConstructionContent(extracttextMap.get("建设内容"));
            List<SimilarProject> similarProjectList = new ArrayList<>();
            for (Integer repoDocId : repoDocIdList) {
                RepositoryDoc historyDoc = repositoryDocMapper.queryById(repoDocId);
                String historyContent = historyDoc.getExtractContent();
                String result = duplicate(extractContent, historyContent, prompt, url, agentId, agentVersion, token);
                result = JsonUtil.processJson(result);
                Map<String, Object> result1 = JSON.parseObject(result);
                String resultString = (String) result1.get("result");
                Map<String, Object> resultMap = JSON.parseObject(resultString);

                // 对查重结果进行封装
                SimilarProject similarProject = new SimilarProject();
                similarProject.setProjectId(historyDoc.getId().toString());
                similarProject.setProjectName(historyDoc.getDocName());
                similarProject.setExtractContent(historyContent);
                similarProject.setTotalScore(String.valueOf(resultMap.get("总分")));
                similarProject.setSimilarityDescription(String.valueOf(resultMap.get("相似维度描述")));
                similarProjectList.add(similarProject);

                // 记录相似度最高的结果
                if (percent.compareTo((BigDecimal) resultMap.get("总分")) > 0 || percent.equals(BigDecimal.ZERO)) {
                    percent = (BigDecimal) resultMap.get("总分");
                    resultId = taskDuplicateResult.getId();
                }

                // 将本次查重结果记录在查重结果表中
                taskDuplicateResult.setDuplicateResult(result);
                taskDuplicateResult.setRepositoryDocId(repoDocId);
                taskDuplicateResult.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                taskDuplicateResultMapper.insert(taskDuplicateResult);
            }
            // 根据查重结果生成 查重报告（```
            String reportMd = ReportGenerator.generateReportMd(projectQuery, similarProjectList);

            // 更新任务状态为已完成
            scheduleduplicate.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            scheduleDuplicateMapper.update(scheduleduplicate);
            // 更新查重子任务表
            taskDuplicateDoc.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            // 更新 doc 表中的 resultId 与 duplicateResult
            taskDuplicateDoc.setResultId(resultId);
            taskDuplicateDoc.setDuplicateResult(reportMd);
            taskDuplicateDocMapper.updateById(taskDuplicateDoc);
            log.info("执行任务结束！");
        } catch (Exception e) {
            log.error("任务{}处理失败：{}", task.getId(), e.getMessage(), e);
            // 更新任务状态为失败
            scheduleduplicate.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            scheduleDuplicateMapper.update(scheduleduplicate);
            taskDuplicateDoc.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            taskDuplicateDoc.setDuplicateResult(e.getMessage());
            // taskDuplicateDoc.setErrorMsg(e.getMessage()); // 会超长
            taskDuplicateDocMapper.updateById(taskDuplicateDoc);
        }
    }

    /**
     * 外部知识库查重
     */
    private String duplicateExternal(String extractContent, String repoUrl, String repoToken) {
        return "这是外部知识库查重结果";
    }


    /**
     * 智能体查重
     */
    @SneakyThrows
    private String duplicate(String extractContent, String historyContent, String prompt, String url, String agentId, String agentVersion, String token) {
        int maxRetries = scheduleTaskConfig.getMaxRetryCount(); // 从配置中获取最大重试次数
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if ("fastgpt".equals(agentConfig.getAgentType())) {
                    return duplicateFastGPT(extractContent, historyContent, prompt, url, agentId, agentVersion, token);
                } else if ("bailian".equals(agentConfig.getAgentType())) {
                    return duplicateBailian(extractContent, historyContent, prompt, url, agentId, agentVersion, token);
                } else {
                    return duplicateFacade(extractContent, historyContent, prompt, url, agentId, agentVersion, token);
                }
            } catch (Exception e) {
                log.warn("智能体请求失败，第{}次尝试失败: {}", attempt + 1, e.getMessage());
                if (attempt == maxRetries) {
                    // 所有重试都失败了，抛出异常
                    throw e;
                }
                // 等待一段时间再重试
                Thread.sleep(1000 * (attempt + 1)); // 递增等待时间
            }
        }
        return null; // 不会到达这里
    }

    /**
     * 智能体查重过程 (人工智能门户)
     */
    private String duplicateFacade(String extractContent, String historyContent, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        // 请求 session
        String sessionId = agentUtil.createSession(agentId, agentVersion);
        
        // 封装请求智能体参数
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("input", extractContent);
        if(historyContent != null) {
            // 本地文档库
            textMap.put("history", historyContent);
            textMap.put("prompt", prompt);
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("agentId", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("stream", false);
        bodyMap.put("text", JSON.toJSONString(textMap));
        String paramJson = JSON.toJSONString(bodyMap);
        // String url = "172.29.61.1:17400/api/v1/chat/completions";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        log.info("请求参数：{}", paramJson);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(paramJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非200状态码（如401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Map<String, Object> dataMap = (Map<String, Object>)responseMap.get("data");
            String answer = (String) dataMap.get("answer");
            log.info("智能体返回结果：{}", answer);
            return JsonUtil.processJson(answer);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            // 任务完成后清理 session
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
        }
    }



    /**
     * 智能体查重 (fastgpt)
     */
    private String duplicateFastGPT(String extractContent, String historyContent, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        // 封装请求智能体参数
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("input", extractContent);
        if(historyContent != null) {
            // 外部知识库
            contentMap.put("history", historyContent);
            contentMap.put("prompt", prompt);
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("chatId", "asd221");
        bodyMap.put("stream", false);
        bodyMap.put("detail", false);
        Map<String, Object> messagesMap = new HashMap<>();
        List<Map<String, Object>> messageList = new ArrayList<>();
        messagesMap.put("role", "user");
        messagesMap.put("content", JSON.toJSONString(contentMap));
        messageList.add(messagesMap);
        bodyMap.put("messages", messageList);
        String paramJson = JSON.toJSONString(bodyMap);
//        String url = "172.29.61.1:17400/api/v1/chat/completions";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
//        String api = "http://172.29.61.1:17400/api/v1/chat/completions";
//        String token = "Bearer fastgpt-eLvUDJId3GJAWIrHN1VZCgfb0xSqw78UT8nJFsiGWqYMDx2WD5F4aDX8OGbfaH9";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 添加Authorization头
                .addHeader("Content-Type", "application/json")  // 声明JSON类型
                .post(RequestBody.create(paramJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非200状态码（如401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>)responseMap.get("choices");
            Map<String, Object> choiceMap = choices.get(0);
            Map<String, Object> messageMap = (Map<String, Object>) choiceMap.get("message");
            String duplicateResult = (String) messageMap.get("content");
            log.info("duplicate:{}", duplicateResult);
            return JsonUtil.processJson(duplicateResult);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 智能体查重过程处理 (百炼)
     * curl -X 'POST' http:/xxx/xlm-gateway-scbfev/sfm-api-gateway/gateway/agent/api/run \
     * -H 'Authorization: Bearer YOUR_APP_KEY' \
     * -H 'Content-Type: application/json' \
     * -d '{
     *     "agentCode":xxxx,
     *     "agentVersion":xxxx,
     *     "stream":false,
     *     "trace":false,
     *     "delta":false,
     *     "sessionId": "7af650e7-ed57-498d-8081-fe1dea5361fa",
     *     "message":{
     *         "text":"帮我订一张明天从上海去苏黎世的机票",
     *         "metadata":{},
     *         "attachments":[]
     *     }
     */
    private String duplicateBailian(String extractContent, String historyContent, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        // 请求 session
        String sessionId = agentUtil.createSession(agentId, agentVersion);

        // 封装请求智能体参数
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("input", extractContent);
        if(historyContent != null) {
            // 本地文档库
            textMap.put("history", historyContent);
            textMap.put("prompt", prompt);
        }
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", JSON.toJSONString(textMap));
        messageMap.put("metadata", new HashMap<>());
        messageMap.put("attachments", new ArrayList<>());

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("agentCode", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("stream", false);
        bodyMap.put("delta", false);
        bodyMap.put("trace", false);
        bodyMap.put("message", messageMap);
        String paramJson = JSON.toJSONString(bodyMap);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        log.info("请求参数：{}", paramJson);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(paramJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非200状态码（如401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            log.info("智能体返回结果：{}", responseJson);
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Map<String, Object> dataMap = (Map<String, Object>)responseMap.get("data");
            Map<String, Object> messageData = (Map<String, Object>)dataMap.get("message");
            List<Map<String, Object>> contentList = (List<Map<String, Object>>)messageData.get("content");

            // 提取文本内容
            StringBuilder textContent = new StringBuilder();
            for (Map<String, Object> contentItem : contentList) {
                if ("text".equals(contentItem.get("type"))) {
                    Map<String, Object> textObj = (Map<String, Object>)contentItem.get("text");
                    String value = (String)textObj.get("value");
                    textContent.append(value);
                }
            }

            String answer = textContent.toString();
//            log.info("智能体返回结果：{}", answer);
            return JsonUtil.processJson(answer);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            // 任务完成后清理 session
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
        }
    }

}