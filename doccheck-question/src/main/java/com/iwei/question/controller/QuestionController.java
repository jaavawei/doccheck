package com.iwei.question.controller;

import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.question.entity.QuestionLog;
import com.iwei.question.entity.SendContentSseEmitter;
import com.iwei.question.entity.vo.QuestionLogVo;
import com.iwei.question.service.QuestionService;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.acl.Permission;
import java.util.List;

/**
 * 首页问答
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 流式输出接口
     */
    @PostMapping(value = "/sendContentSseEmitter1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendContentSseEmitter1(@RequestBody SendContentSseEmitter sendContentSseEmitter) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        questionService.sendContentSseEmitter1(sendContentSseEmitter, sseEmitter);
        return sseEmitter;
    }
    
    /**
     * 模拟流式输出接口
     */
    @PostMapping(value = "/sendContentSseEmitter2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendContentSseEmitter2(@RequestBody SendContentSseEmitter sendContentSseEmitter) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        questionService.sendContentSseEmitter2(sendContentSseEmitter, sseEmitter);
        return sseEmitter;
    }

    @PostMapping("/createSession")
    public Result<QuestionLog> createSession(@RequestBody QuestionLogVo questionLogVo) {
        try {
            return Result.ok(questionService.createSession(questionLogVo));
        } catch (Exception e) {
            return Result.fail("创建session失败：" + e.getMessage());
        }
    }

    @PostMapping("/deleteSession")
    public Result<String> deleteSession(@RequestBody QuestionLogVo questionLogVo) {
        try {
            questionService.deleteSession(questionLogVo);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail("创建session失败：" + e.getMessage());
        }
    }

    /**
     * 查询历史记录列表
     */
    @PostMapping("/historyList")
    public Result<PageResult<QuestionLog>> historyList(@RequestBody QuestionLogVo questionLogvo) {
        try {
            return Result.ok(questionService.historyList(questionLogvo));
        } catch (Exception e) {
            return Result.fail("请求历史记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询历史记录详情
     */
    @PostMapping("/historyDetail")
    public Result<PageResult<QuestionLog>> historyDetail(@RequestBody QuestionLogVo questionLogVo) {
        try {
            Preconditions.checkArgument(!(questionLogVo.getUuid() == null), "uuid 不能为 null");
            return Result.ok(questionService.historyDetail(questionLogVo));
        } catch (Exception e) {
            return Result.fail("请求历史记录失败：" + e.getMessage());
        }
    }

}
