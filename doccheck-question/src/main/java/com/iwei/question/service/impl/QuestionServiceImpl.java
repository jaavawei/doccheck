package com.iwei.question.service.impl;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.config.AgentConfig;
import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.AgentUtil;
import com.iwei.question.entity.App;
import com.iwei.question.entity.QuestionLog;
import com.iwei.question.entity.SendContentSseEmitter;
import com.iwei.question.entity.vo.QuestionLogVo;
import com.iwei.question.mapper.AppMapper;
import com.iwei.question.mapper.QuestionLogMapper;
import com.iwei.question.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {
    
    @Resource
    private AgentConfig agentConfig;

    @Resource
    private AgentUtil agentUtil;

    @Resource
    private AppMapper appMapper;

    @Resource
    private QuestionLogMapper questionLogMapper;
    
    /**
     * 获取会话创建URL
     */
    private String getSessionCreateUrl() {
        return agentConfig.getCreateUrl();
    }
    
    /**
     * 获取会话删除URL
     */
    private String getSessionDeleteUrl() {
        return agentConfig.getDeleteUrl();
    }
    
    /**
     * 获取会话令牌
     */
    private String getSessionCreateToken() {
        return agentConfig.getCreateToken();
    }

    /**
     * 获取会话删除令牌
     */
    private String getSessionDeleteToken() {
        return agentConfig.getDeleteToken();
    }
    
    /**
     * 模拟流式输出接口 - 将一次性返回的内容模拟成流式输出
     */
    public void sendContentSseEmitter2(SendContentSseEmitter sendContentSseEmitter, SseEmitter sseEmitter) {
        try {
            // 模拟一次性获取到的完整内容
            String fullContent = "这是模拟的一次性完整内容，我们将把它切分成小块模拟流式输出。" +
                    "在实际应用中，这里可能是来自AI模型或其他服务的一整段回复。" +
                    "通过将其逐字或逐词发送，我们可以模拟流式传输的效果。" +
                    "这种技术常用于聊天机器人、实时数据更新等场景。";

            List<ServerSentEvent<String>> events = new ArrayList<>();
            for (int i = 0; i < fullContent.length(); i++) {
                String data = fullContent.substring(i, i + 1);
                events.add(ServerSentEvent.<String>builder().event("data").data(data).build());
            }
            Flux<ServerSentEvent<String>> flux = Flux.fromIterable(events)
                    .delayElements(Duration.ofMillis(10));

            // 发起请求
            StringBuilder allAnswer = new StringBuilder();

            Mono<Void> completionSignal = flux
                    .doOnNext(sse -> {
                        String event = sse.event();
                        log.info("event:{}", event);

                        if ("data".equals(event)) {
                            String data = sse.data();
                            // 返回给客户端的文本
                            // 每当接收到一个事件时执行的操作
                            if (!data.equals("[DONE]") && !data.startsWith(":")) {
                                // log.info("value：{}", data);
                                allAnswer.append(data);
                                try {
                                    Map<String, String> dataMap = new HashMap<>();
                                    dataMap.put("data", data);
                                    sseEmitter.send(SseEmitter.event().name("data").data(dataMap));

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    })
                    .doOnError(throwable -> {
                        log.error("SSE stream error: {}", throwable.getMessage());
                    })
                    .doFinally(signalType -> {
                        // 当事件流结束或发生错误时触发此方法
                        System.out.println("SSE stream has finished or an error occurred.");
                        // 在这里执行你想要的操作
                        try {
                            sseEmitter.send(SseEmitter.event().name("done").data(""));

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        sseEmitter.complete();
                        log.info("接口已完成！");

                        if (!StringUtils.isBlank(allAnswer)) {
                            log.info("finalResultBuilder:{}", allAnswer);
                            log.info("开始保存日志");
                            // 保存会话
                            QuestionLog questionLog = new QuestionLog();
                            questionLog.setQuestion(sendContentSseEmitter.getQuestion());
                            questionLog.setAnswer(allAnswer.toString());
                            questionLog.setAppId(sendContentSseEmitter.getAppId());
                            questionLog.setFileId(JSON.toJSONString(sendContentSseEmitter.getFileIds()));
                            questionLog.setUuid(sendContentSseEmitter.getUuid());
                            questionLog.setSessionId(sendContentSseEmitter.getSessionId());
                            questionLog.setProjectInfo(sendContentSseEmitter.getProjectInfo());
                            questionLog.setQaAction(sendContentSseEmitter.getQuestion());
                            questionLog.setCreatedAt(new Date());
                            questionLog.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                            questionLogMapper.insert(questionLog);
                            log.info("日志入库成功");
                        }

                    })
                    .then(); // 返回一个空的 Mono，当上游完成时它也会完成
            completionSignal.subscribe();
        } catch (Exception e) {
            log.error("初始化流式输出时发生错误: ", e);
            try {
                sseEmitter.send(SseEmitter.event().name("error").data("初始化失败"));
            } catch (IOException ioException) {
                log.error("发送初始化错误信息失败: ", ioException);
            } finally {
                sseEmitter.complete();
            }
        }
    }



    /*
     * 流式输出接口
     */
    @Override
    public void sendContentSseEmitter1(SendContentSseEmitter sendContentSseEmitter, SseEmitter sseEmitter) {
        try {
            String chatUrl = getSessionCreateUrl();
            String apiKey = getSessionCreateToken();
            String jsonStr = JSON.toJSONString(sendContentSseEmitter);
            
            WebClient webClient = WebClient.create();
            StringBuilder finalResultBuilder = new StringBuilder();
            
            // 发起请求
            Flux<ServerSentEvent<String>> flux = webClient.post()
                    .uri(chatUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(jsonStr), String.class)
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {});
            
            AtomicBoolean thinkFlag = new AtomicBoolean(false);
            
            Mono<Void> completionSignal = flux
                    .doOnNext(sse -> {
                        String event = sse.event();
                        log.info("event: {}", event);
                        
                        if ("answer".equals(event) || "fastAnswer".equals(event)) {
                            String data = sse.data();
                            // 返回给客户端的文本
                            // 每当接收到一个事件时执行的操作
                            if (!data.equals("[DONE]") && !data.startsWith(":")) {
                                // 处理数据
                                finalResultBuilder.append(data);
                                try {
                                    sseEmitter.send(SseEmitter.event().name("answer").data(data));
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        } else if ("updateVariables".equals(event)) {
                            // 多轮对话变量
                            Object sseObj = sse.data();
                            String data = JSON.toJSONString(sseObj);
                            try {
                                sseEmitter.send(SseEmitter.event().name("variables").data(data));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else if ("error".equals(event)) {
                            String data = sse.data();
                            log.error("api 流式输出报错：{}", data);
                            // 如果出现发送错误，直接发送错误信号给客户端
                            Map<String, String> contentMap = new HashMap<>();
                            contentMap.put("content", "很抱歉，目前有点忙碌，没能听清您的问题，麻烦您再问一次，谢谢！");
                            try {
                                sseEmitter.send(SseEmitter.event().name("answer").data(JSON.toJSONString(contentMap)));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } finally {
                                sseEmitter.complete();
                            }
                        }
                    })
                    .doFinally(signalType -> {
                        // 当事件流结束或发生错误时触发此方法
                        System.out.println("SSE stream has finished or an error occurred.");
                        // 在这里执行你想要的操作
                        sseEmitter.complete();
                        log.info("接口已完成！");
                        
                        if (StringUtils.isNoneBlank(finalResultBuilder.toString())) {
                            log.info("finalResultBuilder:{}", finalResultBuilder);
                            log.info("开始保存日志");
                            // 日志参数封装
                            // sendContentmd.setAnswer(finalResultBuilder.toString());
                            
                            // 保存会话
                            // ModelSendContentPO modelSendContentPO = ModelSendContentConvert.INSTANCE.add2po(sendContentParam);
                            // modelSendContentPO.setCreator(remoteAddr);
                            // modelSendContentPO.setFileName(sendContentParam.getFileName());
                            // modelSendContentPO.setUniqueName(sendContentParam.getFilePath());
                            // modelSendContentPO.setAppId(sendContentParam.getAppId());
                            // modelSendContentPO.setUpdateTime(new Date());
                            // modelSendContentPO.setChatId(sendContentParam.getChatId());
                            // modelSendContentPO.setId(sendContentParam.getId());
                            // Long l1 = System.currentTimeMillis();
                            // if (StringUtils.isEmpty(modelSendContentPO.getId())) {
                            //     modelSendContentPO.setCreateTime(new Date());
                            // }
                            // log.info("保存到日志信息：{}", JSON.toJSONString(modelSendContentPO)); // 使用FastJSON2替换JSONUtil
                            // modelSendContentService.saveOrUpdate(modelSendContentPO);
                            // log.info("日志入库用时：{}秒", (System.currentTimeMillis() - l1) / 1000);
                        }
                    })
                    .then(); // 返回一个空的 Mono，当上游完成时它也会完成
            
            completionSignal.subscribe();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出现发送错误，直接发送错误信号给客户端
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("content", "很抱歉，目前有点忙碌，没能听清您的问题，麻烦您再问一次，谢谢！");
            try {
                sseEmitter.send(SseEmitter.event().data(JSON.toJSONString(contentMap)));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                sseEmitter.complete();
            }
        }
    }


    /**
     * 创建会话
     */
    @Override
    public QuestionLog createSession(QuestionLogVo questionLogVo) {
        App app = appMapper.selectById(questionLogVo.getAppId());
        String sessionId = agentUtil.createSession(app.getAgentId(), app.getAgentVersion());
        QuestionLog questionLog = new QuestionLog();
        questionLog.setSessionId(sessionId);
        questionLog.setUuid(UUID.randomUUID().toString());
        return questionLog;
    }

    /**
     * 删除会话
     */
    @Override
    public void deleteSession(QuestionLogVo questionLogVo) {
        App app = appMapper.selectById(questionLogVo.getAppId());
        agentUtil.deleteSession(questionLogVo.getSessionId(), app.getAgentId(), app.getAgentVersion());
    }

    /**
     * 查询历史记录列表
     */
    @Override
    public PageResult<QuestionLog> historyList(QuestionLogVo questionLogVo) {
        Integer pageSize = questionLogVo.getPageSize();
        Integer pageNum = questionLogVo.getPageNum();
        PageResult<QuestionLog> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNum == null ? PageInfoEnum.PAGE_NO.getCode() : pageNum);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        int total = questionLogMapper.countHistoryList();
        if (total == 0) {
            return pageResult;
        }
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
        List<QuestionLog> questionLogs = questionLogMapper.queryHistoryList(pageSize, offset);
        pageResult.setRecords(questionLogs);
        return pageResult;
    }

    /**
     * 查询历史记录
     */
    @Override
    public PageResult<QuestionLog> historyDetail(QuestionLogVo questionLogVo) {

        Integer pageSize = questionLogVo.getPageSize();
        Integer pageNum = questionLogVo.getPageNum();
        PageResult<QuestionLog> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNum == null ? PageInfoEnum.PAGE_NO.getCode() : pageNum);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);
        String uuid = questionLogVo.getUuid();
        QuestionLog questionLog = new QuestionLog();
        questionLog.setUuid(uuid);

        Integer total = questionLogMapper.countByCondition(questionLog);
        if (total == 0) {
            return pageResult;
        }

        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
        List<QuestionLog> questionLogs = questionLogMapper.queryByCondition(questionLog, pageSize, offset);
        pageResult.setRecords(questionLogs);
        pageResult.setTotal(total);
        return pageResult;
    }
}