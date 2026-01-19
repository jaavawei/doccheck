package com.iwei.schedule.processor;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.ProjectTypeEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.common.tool.JsonUtil;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.mapper.RepositoryDocMapper;
import com.iwei.repository.mapper.RepositoryDuplicateMapper;
import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.mapper.RuleDuplicateMapper;
import com.iwei.common.tool.AgentUtil;
import com.iwei.schedule.config.ScheduleTaskConfig;
import com.iwei.task.entity.*;
import com.iwei.task.mapper.*;
import javax.annotation.Resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新疆任务处理器
 *
 * @auther: zhaokangwei
 */
@Component
@Slf4j
public class XjTaskProcessor implements XjTaskProcessorAdaptor {

    @Resource
    private ScheduleDuplicateMapper scheduleDuplicateMapper;

    @Resource
    private ScheduleXjMapper scheduleXjMapper;

    @Resource
    private TaskDuplicateInfoMapper taskDuplicateInfoMapper;

    @Resource
    private TaskDuplicateDocMapper taskDuplicateDocMapper;

    @Resource
    private RepositoryDocMapper repositoryDocMapper;

    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;

    @Resource
    private RuleDuplicateMapper ruleDuplicateMapper;

    @Resource
    private ProjectStationLineMappingMapper projectStationLineMappingMapper;

//    @Resource
//    private FileService fileService;
//    @Value("${prompt-post}")
//    private String PROMPT_POST;
    
    @Resource
    private AgentUtil agentUtil;
    
    @Resource
    private ScheduleTaskConfig scheduleTaskConfig;
    
    @Value("${schedule.task.agent.type:default}")
    private String agentType;

    /**
     * 处理单个任务
     */
    @Override
    public void processTask(ScheduleXj task) {
        log.info("开始处理任务：" + JSON.toJSONString(task));

        // 更新任务状态为处理中
        ScheduleXj scheduleXj = new ScheduleXj();
        scheduleXj.setId(task.getId());
        scheduleXj.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        boolean statusUpdated = scheduleXjMapper.update(scheduleXj) > 0;
        // 多线程竞争时使用乐观锁
        if (!statusUpdated) {
            log.info("任务{}状态已被其他线程修改，跳过处理", task.getId());
            return;
        }

        try {
            RepositoryDoc docF = repositoryDocMapper.queryById(task.getDocFId());
            RepositoryDoc docS = repositoryDocMapper.queryById(task.getDocSId());
            String extractContentF = docF.getExtractContent();
            String extractContentS = docS.getExtractContent();
            TaskDuplicateInfo info = taskDuplicateInfoMapper.queryById(task.getSourceId());
            RuleDuplicate ruleDuplicate = ruleDuplicateMapper.queryById(info.getRuleDuplicateId());
            String prompt = ruleDuplicate.getContent();
            String url = ruleDuplicate.getAgentUrl();
            String agentId = ruleDuplicate.getAgentId();
            String agentVersion = ruleDuplicate.getAgentVersion();
            String token = ruleDuplicate.getAgentToken();

            // 按照智能体要求格式封装 input
            Map<String, Object> input = new HashMap<>();
            Map<String, String> projectA = new HashMap<>();
            // ProjectStationLineMapping projectStationLineMappingA = projectStationLineMappingMapper.queryByDocId(task.getDocFId());
            projectA.put("项目名称", docF.getProjectName());
            Map<String, String> contentA = JSON.parseObject(extractContentF, Map.class);
            projectA.put("设备现状", contentA.get("设备现状"));
            projectA.put("存在问题及实施必要性", contentA.get("存在问题"));
            projectA.put("建设方案", contentA.get("方案规模"));
            projectA.put("项目类型", ProjectTypeEnum.getByCode(docF.getProjectType()).getName());
            Map<String, String> projectB = new HashMap<>();
            projectB.put("项目名称", docS.getProjectName());
            Map<String, String> contentB = JSON.parseObject(extractContentS, Map.class);
            projectB.put("设备现状", contentB.get("设备现状"));
            projectB.put("存在问题及实施必要性", contentB.get("存在问题"));
            projectB.put("建设方案", contentB.get("方案规模"));
            projectB.put("项目类型", ProjectTypeEnum.getByCode(docS.getProjectType()).getName());
            input.put("项目A", projectA);
            input.put("项目B", projectB);
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("input", input);
            String result = duplicate(docF, docS, prompt, url, agentId, agentVersion, token);
            // log.info("result:", result);
            Map<String, String> resultMap = JSON.parseObject(result, Map.class);
            task.setDuplicateFlg(resultMap.get("result").equals("重复") ? 1 : 0);
            task.setDuplicateMsg(resultMap.get("FinalResult"));
            Map<String, String> adviceMap = new HashMap<>();
            adviceMap.put("analysis", resultMap.get("analysis"));
            adviceMap.put("suggestions", resultMap.get("suggestions"));
            task.setAdvice(JSON.toJSONString(adviceMap));
//            Map<String, Object> result1 = JSON.parseObject(result);
//            String resultString = (String) result1.get("result");
//            Map<String, Object> resultMap = JSON.parseObject(resultString);
//            task.setDuplicateFlg(String.valueOf(resultMap.get("results")));
//            task.setAdvice(String.valueOf(resultMap.get("查重建议")));
            task.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            scheduleXjMapper.update(task);


            // 更新两个文档的查重状态，是否重复与是否查重完成
            TaskDuplicateDoc tDocF = taskDuplicateDocMapper.queryByInfoIdAndRepoDocId(task.getSourceId(), docF.getId());
            TaskDuplicateDoc tDocS = taskDuplicateDocMapper.queryByInfoIdAndRepoDocId(task.getSourceId(), docS.getId());
            if (tDocF == null) {
                tDocF = new TaskDuplicateDoc();
                tDocF.setInfoId(task.getSourceId());
                tDocF.setRepoDocId(docF.getId());
                tDocF.setDuplicateFlg(-1);
                tDocF.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                tDocF.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                taskDuplicateDocMapper.insert(tDocF);
            }
            if (tDocS == null) {
                tDocS = new TaskDuplicateDoc();
                tDocS.setInfoId(task.getSourceId());
                tDocS.setRepoDocId(docS.getId());
                tDocS.setDuplicateFlg(-1);
                tDocS.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                tDocS.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                taskDuplicateDocMapper.insert(tDocS);
            }
            if(task.getDuplicateFlg() == 1) {
                // 有一个子任务重复了，那么两个任务都重复了
                tDocF.setDuplicateFlg(1);
                tDocS.setDuplicateFlg(1);
            } else {
                if (tDocF.getDuplicateFlg() == -1) {
                    tDocF.setDuplicateFlg(0);
                }
                if (tDocS.getDuplicateFlg() == -1) {
                    tDocS.setDuplicateFlg(0);
                }
            }
            Integer fDone = scheduleXjMapper.countByDocIdAndStatus(docF.getId(), TaskStatusEnum.COMPLETED.getCode());
            Integer SDone = scheduleXjMapper.countByDocIdAndStatus(docS.getId(), TaskStatusEnum.COMPLETED.getCode());
            Integer fTotal = scheduleXjMapper.countByDocId(docF.getId());
            Integer sTotal = scheduleXjMapper.countByDocId(docS.getId());
            if(fDone == fTotal) {
                tDocF.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            } else {
                tDocF.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
            }
            if(SDone == sTotal) {
                tDocS.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            } else {
                tDocS.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
            }
            taskDuplicateDocMapper.updateById(tDocF);
            taskDuplicateDocMapper.updateById(tDocS);

            log.info("执行任务结束！");
        } catch (Exception e) {
            log.error("任务{}处理失败：{}", task.getId(), e.getMessage(), e);
            // 更新任务状态为失败
            task.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            task.setErrorMsg(StringUtils.abbreviate(e.getMessage(), 60));
            scheduleXjMapper.update(task);
        }
    }


    @SneakyThrows
    private String duplicate(RepositoryDoc docF, RepositoryDoc docS, String prompt, String url, String agentId, String agentVersion, String token) {
        int maxRetries = scheduleTaskConfig.getMaxRetryCount(); // 从配置中获取最大重试次数
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if ("bailian".equalsIgnoreCase(agentType)) {
                    return duplicateBailian(docF, docS, prompt, url, agentId, agentVersion, token);
                } else if ("fastgpt".equalsIgnoreCase(agentType)) {
                    return duplicateFastGPT(docF, docS, prompt, url, agentId, agentVersion, token);
                } else {
                    return duplicateFacade(docF, docS, prompt, url, agentId, agentVersion, token);
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
     * 智能体查重 (人工智能门户)
     */
    private String duplicateFacade(RepositoryDoc docF, RepositoryDoc docS, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> projectA = new HashMap<>();
        // ProjectStationLineMapping projectStationLineMappingA = projectStationLineMappingMapper.queryByDocId(task.getDocFId());
        projectA.put("项目名称", docF.getProjectName());
        Map<String, Object> contentA = JSON.parseObject(docF.getExtractContent(), Map.class);
        projectA.put("设备现状", contentA.get("设备现状"));
        projectA.put("存在问题及实施必要性", contentA.get("存在问题"));
        projectA.put("建设方案", contentA.get("方案规模"));
        projectA.put("项目类型", ProjectTypeEnum.getByCode(docF.getProjectType()).getName());
        Map<String, String> projectB = new HashMap<>();
        projectB.put("项目名称", docS.getProjectName());
        Map<String, String> contentB = JSON.parseObject(docS.getExtractContent(), Map.class);
        projectB.put("设备现状", contentB.get("设备现状"));
        projectB.put("存在问题及实施必要性", contentB.get("存在问题"));
        projectB.put("建设方案", contentB.get("方案规模"));
        projectB.put("项目类型", ProjectTypeEnum.getByCode(docS.getProjectType()).getName());
        input.put("项目A", projectA);
        input.put("项目B", projectB);
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("input", input);

        // 获取 sessionId
        String sessionId = agentUtil.createSession(agentId, agentVersion);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("agentId", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("stream", false);
        bodyMap.put("text", JSON.toJSONString(textMap));
        String paramJson = JSON.toJSONString(bodyMap);
        log.info("请求参数：" + paramJson);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 使用 sessionId 作为 token
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
            Map<String, Object> dataMap = (Map<String, Object>)responseMap.get("data");
            String answer = (String) dataMap.get("content");
            log.info("智能体返回结果: {}", answer);
            return JsonUtil.processJson(answer);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            // 任务完成后清理session
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
        }
    }

    /**
     * 智能体查重 (fastgpt)
     */
    private String duplicateFastGPT(RepositoryDoc docF, RepositoryDoc docS, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        // 封装请求智能体参数
        Map<String, Object> input = new HashMap<>();
        Map<String, String> projectA = new HashMap<>();
        projectA.put("项目名称", docF.getProjectName());
        Map<String, String> contentA = JSON.parseObject(docF.getExtractContent(), Map.class);
        projectA.put("设备现状", contentA.get("设备现状"));
        projectA.put("存在问题及实施必要性", contentA.get("存在问题"));
        projectA.put("建设方案", contentA.get("方案规模"));
        projectA.put("项目类型", ProjectTypeEnum.getByCode(docF.getProjectType()).getName());
        Map<String, String> projectB = new HashMap<>();
        projectB.put("项目名称", docS.getProjectName());
        Map<String, String> contentB = JSON.parseObject(docS.getExtractContent(), Map.class);
        projectB.put("设备现状", contentB.get("设备现状"));
        projectB.put("存在问题及实施必要性", contentB.get("存在问题"));
        projectB.put("建设方案", contentB.get("方案规模"));
        projectB.put("项目类型", ProjectTypeEnum.getByCode(docS.getProjectType()).getName());
        input.put("项目A", projectA);
        input.put("项目B", projectB);
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("input", JSON.toJSONString(input));

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
        log.info(paramJson);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
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
     * 智能体查重 (百炼)
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
    private String duplicateBailian(RepositoryDoc docF, RepositoryDoc docS, String prompt, String url, String agentId, String agentVersion, String token) throws InterruptedException {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> projectA = new HashMap<>();
        // ProjectStationLineMapping projectStationLineMappingA = projectStationLineMappingMapper.queryByDocId(task.getDocFId());
        projectA.put("项目名称", docF.getProjectName());
        Map<String, Object> contentA = JSON.parseObject(docF.getExtractContent(), Map.class);
        projectA.put("设备现状", contentA.get("设备现状"));
        projectA.put("存在问题及实施必要性", contentA.get("存在问题"));
        projectA.put("建设方案", contentA.get("方案规模"));
        projectA.put("项目类型", ProjectTypeEnum.getByCode(docF.getProjectType()).getName());
        Map<String, String> projectB = new HashMap<>();
        projectB.put("项目名称", docS.getProjectName());
        Map<String, String> contentB = JSON.parseObject(docS.getExtractContent(), Map.class);
        projectB.put("设备现状", contentB.get("设备现状"));
        projectB.put("存在问题及实施必要性", contentB.get("存在问题"));
        projectB.put("建设方案", contentB.get("方案规模"));
        projectB.put("项目类型", ProjectTypeEnum.getByCode(docS.getProjectType()).getName());
        input.put("项目A", projectA);
        input.put("项目B", projectB);
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("input", input);
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", JSON.toJSONString(textMap));
        messageMap.put("metadata", new HashMap<>());
        messageMap.put("attachments", new ArrayList<>());

        // 获取 sessionId
        String sessionId = agentUtil.createSession(agentId, agentVersion);

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("agentCode", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("stream", false);
        bodyMap.put("trace", false);
        bodyMap.put("delta", false);
        bodyMap.put("message", JSON.toJSONString(messageMap));
        String paramJson = JSON.toJSONString(bodyMap);
        log.info("请求参数：" + paramJson);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 使用 sessionId 作为 token
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
            log.info("智能体返回结果: {}", answer);
            return JsonUtil.processJson(answer);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            // 任务完成后清理 session
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
        }
    }

}