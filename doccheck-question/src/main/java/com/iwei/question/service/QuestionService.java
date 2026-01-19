package com.iwei.question.service;

import com.iwei.common.entity.PageResult;
import com.iwei.question.entity.QuestionLog;
import com.iwei.question.entity.SendContentSseEmitter;
import com.iwei.question.entity.vo.QuestionLogVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface QuestionService {

    /**
     * 流式输出接口 - 将智能体的流式输出返回给前端
     */
    void sendContentSseEmitter1(SendContentSseEmitter sendContentSseEmitter, SseEmitter sseEmitter);
    
    /**
     * 模拟流式输出接口 - 将一次性返回的内容模拟成流式输出
     */
    void sendContentSseEmitter2(SendContentSseEmitter sendContentSseEmitter, SseEmitter sseEmitter);

    /**
     * 创建会话
     */
    QuestionLog createSession(QuestionLogVo questionLogVo);

    /**
     * 删除会话
     */
    void deleteSession(QuestionLogVo questionLogVo);

    /**
     * 获取历史记录列表
     */
    PageResult<QuestionLog> historyList(QuestionLogVo questionLogVo);

    /**
     * 获取历史记录详情
     */
    PageResult<QuestionLog> historyDetail(QuestionLogVo questionLogVo);

}
