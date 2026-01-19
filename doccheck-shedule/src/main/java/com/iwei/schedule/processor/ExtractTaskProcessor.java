package com.iwei.schedule.processor;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.config.AgentConfig;
import com.iwei.common.entity.HeadingNode;
import com.iwei.common.tool.*;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.ScheduleTaskSourceEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.oss.service.FileService;
import com.iwei.repository.entity.*;
import com.iwei.repository.mapper.*;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.mapper.RuleExtractMapper;
import com.iwei.schedule.config.ScheduleTaskConfig;
import com.iwei.task.entity.ScheduleDuplicate;
import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.mapper.ScheduleDuplicateMapper;
import com.iwei.task.mapper.ScheduleXjMapper;
import com.iwei.task.mapper.TaskDuplicateDocMapper;
import com.iwei.task.mapper.TaskDuplicateFileMapper;
import javax.annotation.Resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档提取任务执行器
 *
 * @auther: zhaokangwei
 */
@Component
@Slf4j
public class ExtractTaskProcessor implements ExtractTaskProcessorAdaptor {

    @Resource
    private ScheduleExtractMapper scheduleExtractMapper;
    @Resource
    private RepositoryDocMapper repositoryDocMapper;
    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    private TaskDuplicateDocMapper taskDuplicateDocMapper;
    @Resource
    private RuleExtractMapper ruleExtractMapper;
    @Resource
    private ScheduleDuplicateMapper scheduleDuplicateMapper;
    @Resource
    private FileService fileService;
    @Resource
    private TaskDuplicateFileMapper taskDuplicateFileMapper;
    @Resource
    private StationLineMapper stationLineMapper;
    @Resource
    private RepositoryDocStationLineMappingMapper repositoryDocStationLineMappingMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private RepositoryDocDeviceMappingMapper repositoryDocDeviceMappingMapper;
    @Resource
    private RepositoryDuplicateDocMappingMapper repositoryDuplicateDocMappingMapper;
    @Resource
    private ScheduleXjMapper scheduleXjMapper;

    @Value("extract_api_token")
    private String EXTRACT_API_TOKEN;

    @Resource
    private ScheduleTaskConfig scheduleTaskConfig;

    @Resource
    private AgentConfig agentConfig;

    @Resource
    private AgentUtil agentUtil;

    @Resource
    private DocUtil docUtil;

    /**
     * 处理单个任务
     */
    @Override
    public void processTask(ScheduleExtract task) {
        log.info("开始处理任务：" + task.getId());

        // 更新任务状态为处理中
        ScheduleExtract scheduleExtract = new ScheduleExtract();
        scheduleExtract.setId(task.getId());
        scheduleExtract.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        boolean statusUpdated = scheduleExtractMapper.update(scheduleExtract);

        // 多线程竞争时使用乐观锁
        if (!statusUpdated) {
            log.info("任务" + task.getId() + "状态已被其他线程修改，跳过处理");
            return;
        }
        RepositoryDoc repositoryDoc = new RepositoryDoc();
        TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
        // 同步修改来源库表（文档库表或查重子任务表）中状态为处理中, 并获取提取规则 id
        Integer ruleExtractId;
        String docUrl;
        if(task.getTaskSource() == ScheduleTaskSourceEnum.REPOSITORY.getCode()) {
            // 文档库表
            repositoryDoc.setStatus(TaskStatusEnum.EXECUTING.getCode());
            repositoryDoc.setId(task.getSourceId());
            repositoryDocMapper.updateById(repositoryDoc);
            repositoryDoc = repositoryDocMapper.queryById(repositoryDoc.getId());
            ruleExtractId = repositoryDoc.getRuleExtractId();
            docUrl = repositoryDoc.getDocUrl();
        } else {
            // 查重子任务表
            taskDuplicateDoc.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
            taskDuplicateDoc.setId(task.getSourceId());
            taskDuplicateDocMapper.updateById(taskDuplicateDoc);
            Integer duplicateId = taskDuplicateDocMapper.queryRepositoryDuplicateId(taskDuplicateDoc.getId());
            ruleExtractId = repositoryDuplicateMapper.queryById(duplicateId).getRuleExtractId();
            docUrl = taskDuplicateFileMapper.queryUrlByDocId(taskDuplicateDoc.getId());
        }

        try {
            log.info("执行任务中...");
            // 获取文档 inputStream
            InputStream inputStream = fileService.downloadFile(docUrl);
            // 解析文档
            String content = parseDoc(inputStream);
            content = content.replace("\"", "");
            // String content = MinerUUtil.parsePDF(inputStream);
            if (content == null || content.length() == 0) {
                inputStream = fileService.downloadFile(docUrl);
                content =parseDoc(inputStream);
            }
            // log.info("解析出文档内容：{}", content);
            log.info("解析出length: " + content.length());

            // 可研文档要进行内容提取
            // StringBuilder extractStringBuilder = new StringBuilder("");
            if (content.length() > scheduleTaskConfig.getMaxLength()) {
                StringBuilder sb = new StringBuilder();
                SplitterUtil splitterUtil = new SplitterUtil();
                HeadingNode headingNode = splitterUtil.splitByHeading(content);
                sb.append(headingNode.getContent()).append("\n");

                try {
//                    String s1 = splitterUtil.findHeadingByNameAndLevel(headingNode, "概述", 1);
//                    String s2 = splitterUtil.findHeadingByNameAndLevel(headingNode, "项目建设必要性", 2);
//                    String s3 = splitterUtil.findHeadingByNameAndLevel(headingNode, "现状分析", 2);
//                    String s4 = splitterUtil.findHeadingByNameAndLevel(headingNode, "建设内容和规模", 332);
//                    String s5 = splitterUtil.findHeadingByNameAndLevel(headingNode, "项目建设方案", 1);
                    List<String> s1s = splitterUtil.findHeadingsByName(headingNode, "概述");
                    List<String> s2s = splitterUtil.findHeadingsByName(headingNode, "总论");
                    List<String> s3s = splitterUtil.findHeadingsByName(headingNode, "建设");
                    List<String> s4s = splitterUtil.findHeadingsByName(headingNode, "现状");
                    // List<String> s2s = splitterUtil.findHeadingsByName(headingNode, "业务需求描述");

                    // 将四个列表中的内容串联起来
                    for (String s1 : s1s) {
                        sb.append(s1).append("\n");
                    }
                    for (String s2 : s2s) {
                        sb.append(s2).append("\n");
                    }
                    for (String s3 : s3s) {
                        sb.append(s3).append("\n");
                    }
                    for (String s4 : s4s) {
                        sb.append(s4).append("\n");
                    }
                    content = sb.toString();
                } catch (Exception e) {
                    // 如果提取过程中出现任何异常，则保留原始content内容
                    log.warn("内容提取失败，将保留原始内容: " + e.getMessage());
                }
            }
            if (content.length() > scheduleTaskConfig.getMaxLength()) {
                content = content.substring(0, scheduleTaskConfig.getMaxLength());
            }
            log.info("摘取后length: " + content.length());


            // 封装请求智能体参数
            RuleExtract ruleExtract = ruleExtractMapper.queryById(ruleExtractId);

            String ruleName = ruleExtract.getRuleName();
            String elements = ruleExtract.getElements();
            String agentToken = ruleExtract.getAgentToken();
            String agentId = ruleExtract.getAgentId();
            String agentVersion = ruleExtract.getAgentVersion();
            String prompt = ruleExtract.getPrompt();
            String url = ruleExtract.getAgentUrl();

            String extractContent = extract(ruleName, elements, content, prompt, agentId, agentVersion, url, agentToken);

            // 更新任务状态为已完成
            scheduleExtract.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            scheduleExtractMapper.update(scheduleExtract);

            // 更新来源表的内容
            if(task.getTaskSource() == ScheduleTaskSourceEnum.REPOSITORY.getCode()) {
                // 文档库表
                repositoryDoc.setStatus(TaskStatusEnum.COMPLETED.getCode());
                repositoryDoc.setExtractContent(extractContent);

                Map<String, String> extractMap = new HashMap<>();
                if (extractContent != null) {
                    extractMap = JSON.parseObject(extractContent, Map.class);
//                extractMap.put("项目类型", "新型电网基建");
                    repositoryDoc.setExtractContent(JSON.toJSONString(extractMap));

                    repositoryDoc.setProjectCode(extractMap.get("项目编码"));
//                    repositoryDoc.setProjectName(extractMap.get("项目名称"));
                    if (extractMap.get("计划年度") != null) {
                        repositoryDoc.setPlanYear(extractMap.get("计划年度").replaceAll("\\D+", ""));
                    }
                    repositoryDoc.setProjectMsg(extractMap.get("站线名称"));
                    repositoryDoc.setImplOrg(extractMap.get("实施单位名称"));
                } else {
                    repositoryDoc.setExtractContent(null);
                }
                // repositoryDoc.setProjectType(1);
                repositoryDocMapper.updateById(repositoryDoc);

                // 把站线名称提取出来
                String stationLineNames = extractMap.get("站线名称");
                if (stationLineNames != null && !stationLineNames.trim().isEmpty()) {
                    stationLineNames = stationLineNames.trim();
                    String[] stationLineNameList = stationLineNames.split("[,，、]+");
                    log.info("站线名称：{}", JSON.toJSONString(stationLineNameList));
                    List<RepositoryDocStationLineMapping> repositoryDocStationLineMappingList = new ArrayList<>();
                    for (String stationLineName : stationLineNameList) {
                        stationLineName = stationLineName.trim().replaceAll("\\s+", "");
                        if (!stationLineName.isEmpty()) {
                            StationLine stationLine = stationLineMapper.queryByName(stationLineName);
                            if (stationLine == null) {
                                stationLine = new StationLine();
                                // 不存在当前站线名称的站线，新建一个
                                stationLine.setStationLineName(stationLineName);
                                stationLine.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                                stationLineMapper.insert(stationLine);
                            }
                            log.info("站线：{}", JSON.toJSONString(stationLine));
                            // 根据当前文档和站线创建一个 mapping
                            RepositoryDocStationLineMapping repositoryDocStationLineMapping = new RepositoryDocStationLineMapping();
                            repositoryDocStationLineMapping.setStationLineId(stationLine.getId());
                            repositoryDocStationLineMapping.setDocId(task.getSourceId());
                            repositoryDocStationLineMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                            repositoryDocStationLineMappingList.add(repositoryDocStationLineMapping);
                        }
                    }
                    List<List<RepositoryDocStationLineMapping>> repositoryDocStationLineMappingLists = BatchSplitUtil.splitList(repositoryDocStationLineMappingList);
                    for (List<RepositoryDocStationLineMapping> list : repositoryDocStationLineMappingLists) {
                        repositoryDocStationLineMappingMapper.batchInsert(list);
                    }
                }

                // 把设备名称提取出来
                String deviceNames = extractMap.get("设备名称");
                if (deviceNames != null && !deviceNames.trim().isEmpty()) {
                    deviceNames = deviceNames.trim();
                    String[] deviceNameList = deviceNames.split("[,，、]+");
                    log.info("设备名称：{}", JSON.toJSONString(deviceNameList));
                    List<RepositoryDocDeviceMapping> repositoryDocDeviceMappingList = new ArrayList<>();
                    for (String deviceName : deviceNameList) {
                        deviceName = deviceName.trim().replaceAll("\\s+", "");
                        if (!deviceName.isEmpty()) {
                            Device device = deviceMapper.queryByName(deviceName);
                            if (device == null) {
                                device = new Device();
                                // 不存在当前设备名称的设备，新建一个
                                device.setDeviceName(deviceName);
                                device.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                                deviceMapper.insert(device);
                            }
                            log.info("设备：{}", JSON.toJSONString(device));
                            // 根据当前文档和设备创建一个 mapping
                            RepositoryDocDeviceMapping repositoryDocDeviceMapping = new RepositoryDocDeviceMapping();
                            repositoryDocDeviceMapping.setDeviceId(device.getId());
                            repositoryDocDeviceMapping.setDocId(task.getSourceId());
                            repositoryDocDeviceMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                            repositoryDocDeviceMappingList.add(repositoryDocDeviceMapping);
                        }
                    }
                    List<List<RepositoryDocDeviceMapping>> repositoryDocDeviceMappingLists = BatchSplitUtil.splitList(repositoryDocDeviceMappingList);
                    for (List<RepositoryDocDeviceMapping> list : repositoryDocDeviceMappingLists) {
                        repositoryDocDeviceMappingMapper.batchInsert(list);
                    }
                }
//                // 后续均为 append 内容
//                // 把新增的文档添加到 base 查重库
//                RepositoryDuplicateDocMapping repositoryDuplicateDocMapping = new RepositoryDuplicateDocMapping();
//                repositoryDuplicateDocMapping.setRepositoryDuplicateId(1);
//                repositoryDuplicateDocMapping.setRepositoryDocId(task.getSourceId());
//                repositoryDuplicateDocMappingMapper.insert(repositoryDuplicateDocMapping);
//                // 查询出所有站线
//                List<Integer> stationLineIdList = stationLineMapper.queryStationLineIdByDocId(task.getSourceId());
//                for (Integer stationLineId : stationLineIdList) {
//                    StationLine stationLine = stationLineMapper.selectById(stationLineId);
//                    List<Integer> docIdList = repositoryDocMapper.queryDocIdByStationLineId(stationLineId);
//                    for (Integer docId : docIdList) {
//                        if (docId.equals(task.getSourceId())) continue;
//                        ScheduleXj scheduleXj = new ScheduleXj();
//                        scheduleXj.setDocFId(task.getSourceId());
//                        scheduleXj.setDocSId(docId);
//                        scheduleXj.setSourceId(5);
//                        scheduleXj.setStationLineName(stationLine.getStationLineName());
//                        scheduleXj.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
//                        scheduleXj.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
//                        scheduleXjMapper.insert(scheduleXj);
//                    }
//                }

            } else {
                // 查重子任务表
                taskDuplicateDoc.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
                taskDuplicateDoc.setExtractContent(extractContent);
                taskDuplicateDocMapper.updateById(taskDuplicateDoc);
                // 添加到查重定时任务表
                ScheduleDuplicate scheduleDuplicate = new ScheduleDuplicate();
                scheduleDuplicate.setDocId(taskDuplicateDoc.getId());
                scheduleDuplicate.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
                scheduleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                scheduleDuplicateMapper.insert(scheduleDuplicate);
            }
            log.info("执行任务结束！");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("任务" + task.getId() + "处理失败：" + e.getMessage());
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("errorMsg", "文档内容提取失败：" + e.getMessage());
            // 更新任务状态为失败
            scheduleExtract.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            scheduleExtractMapper.update(scheduleExtract);
            if(task.getTaskSource() == ScheduleTaskSourceEnum.REPOSITORY.getCode()) {
                // 文档库表
                repositoryDoc.setStatus(TaskStatusEnum.FAILED.getCode());
                repositoryDoc.setExtractContent(JSON.toJSONString(errorMsg));
                // repositoryDoc.setErrorMsg("文档内容提取失败：" + e.getMessage());
                repositoryDocMapper.updateById(repositoryDoc);
            } else {
                // 查重子任务表
                taskDuplicateDoc.setTaskStatus(TaskStatusEnum.FAILED.getCode());
                taskDuplicateDoc.setExtractContent(JSON.toJSONString(errorMsg));
                // taskDuplicateDoc.setErrorMsg("文档内容提取失败：" + e.getMessage());
                taskDuplicateDocMapper.updateById(taskDuplicateDoc);
            }
        }
    }


    @SneakyThrows
    private String extract(String ruleName, String elements, String content, String prompt, String agentId, String agentVersion, String url, String token) {
        int maxRetries = scheduleTaskConfig.getMaxRetryCount(); // 从配置中获取最大重试次数
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if ("fastgpt".equals(agentConfig.getAgentType())) {
                    return extractFastGPT(ruleName, elements, content, prompt, agentId, agentVersion, url, token);
                } else if ("bailian".equals(agentConfig.getAgentType())) {
                    return extractBailian(ruleName, elements, content, prompt, agentId, agentVersion, url, token);
                } else {
                    return extractFacade(ruleName, elements, content, prompt, agentId, agentVersion, url, token);
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
     * 文档内容提取操作 (人工智能门户)
     */
    private String extractFacade(String ruleName, String elements, String content, String prompt, String agentId, String agentVersion, String url, String token) throws InterruptedException {

        // 获取 sessionId
        String sessionId = agentUtil.createSession(agentId, agentVersion);
        File file = null;
        List<String> files = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();
        Map<String, Object> input = new HashMap<>();
        input.put("提取规则名称", ruleName);
        input.put("提取规则要素", elements);
        if ("file".equals(scheduleTaskConfig.getExtractType())) {
            try {
                file = docUtil.createTxtFile(content);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String fileId = agentUtil.uploadFile(sessionId, agentId, agentVersion, token, file);
            files.add(fileId);
            input.put("文档提取内容", "");
        } else {
            input.put("文档提取内容", content);
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("agentId", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("stream", false);
        bodyMap.put("text", JSON.toJSONString(input));
        bodyMap.put("metadata", metadata);
        bodyMap.put("files", files);
        String bodyJson = JSON.toJSONString(bodyMap);
        log.info("请求参数：" + bodyJson);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 使用 sessionId 作为 token
                .addHeader("Content-Type", "application/json")  // 声明 JSON 类型
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非200状态码（如401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
            Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");
            String extractContent = String.valueOf(dataMap.get("answer"));
            log.info("智能体返回内容：" + extractContent);
            return JsonUtil.processJson(extractContent);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
            docUtil.delete(file);
        }
    }

    /**
     * 文档内容提取操作 (fastgpt)
     */
    private String extractFastGPT(String ruleName, String elements, String content, String prompt, String agentId, String agentVersion, String url, String token) throws InterruptedException {

        Map<String, String> input = new HashMap<>();
        input.put("提取规则名称", ruleName);
        input.put("提取规则要素", elements);
        input.put("文档提取内容", content);
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("input", input);
        if(prompt != null) {
            contentMap.put("prompt", prompt);
        }
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("chatId", "1");
        bodyMap.put("stream", false);
        bodyMap.put("detail", false);
        HashMap messageMap = new HashMap();
        List<Map<String, Object>> messageList = new ArrayList<>();
        messageMap.put("role", "user");
        messageMap.put("content", JSON.toJSONString(contentMap));
        messageList.add(messageMap);
        bodyMap.put("messages", messageList);
        String bodyJson = JSON.toJSONString(bodyMap);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        // api = "http://172.29.61.1:17400/api/v1/chat/completions";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 添加Authorization头
                .addHeader("Content-Type", "application/json")  // 声明JSON类型
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        log.info("请求参数：" + bodyJson);
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
            Map<String, Object> returnMessageMap = (Map<String, Object>) choiceMap.get("message");
            String extractContent = (String) returnMessageMap.get("content");
            log.info("extractContent:{}", extractContent);
            Map<String, Object> extractMap = JSON.parseObject(extractContent, Map.class);
            String result = String.valueOf(extractMap.get("result"));
            if (result != null) {
                return JsonUtil.processJson(result);
            }
            return JsonUtil.processJson(extractContent);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        }
    }

    /**
     * 文档内容提取操作 (百炼)
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
    private String extractBailian(String ruleName, String elements, String content, String prompt, String agentId, String agentVersion, String url, String token) throws InterruptedException {

        // 请求 sessionId
        String sessionId = agentUtil.createSession(agentId, agentVersion);

        Map<String, Object> input = new HashMap<>();
        input.put("提取规则名称", ruleName);
        input.put("提取规则要素", elements);
        input.put("文档提取内容", content);

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", JSON.toJSONString(input));
        messageMap.put("metadata", new HashMap<>());
        messageMap.put("attachments", new ArrayList<>());

        Map<String, Object> bodyMap = new HashMap<>();

        bodyMap.put("agentCode", agentId);
        bodyMap.put("agentVersion", agentVersion);
        bodyMap.put("sessionId", sessionId);
        bodyMap.put("trace", false);
        bodyMap.put("stream", false);
        bodyMap.put("delta", false);
        bodyMap.put("message", messageMap);
        String bodyJson = JSON.toJSONString(bodyMap);
        log.info("请求参数：" + bodyJson);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", token) // 使用 sessionId 作为 token
                .addHeader("Content-Type", "application/json")  // 声明 JSON 类型
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 非200状态码（如401、404）
                throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
            }
            // 返回响应体内容
            String responseJson = response.body() != null ? response.body().string() : "无响应内容";
            log.info("智能体返回内容：" + responseJson);
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

            String extractContent = textContent.toString();
            return JsonUtil.processJson(extractContent);
        } catch (IOException e) {
            throw new RuntimeException("请求发送失败：" + e.getMessage(), e);
        } finally {
            // 任务完成后清理 session
            agentUtil.deleteSession(sessionId, agentId, agentVersion);
        }
    }

}