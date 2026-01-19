package com.iwei.common.controller;

import com.iwei.common.config.AgentConfig;
import com.iwei.common.entity.Result;
import javax.annotation.Resource;

import com.iwei.common.tool.BatchSplitUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/common/config")
@Slf4j
public class CommonConfigController {

    @Resource
    private AgentConfig agentConfig;

    /**
     * 获取创建会话 url
     */
    @GetMapping("/createUrl")
    public Result<Integer> getCreateUrl() {
        try {
            return Result.ok(agentConfig.getCreateUrl());
        } catch (Exception e) {
            log.error("获取创建sessionUrl失败", e);
            return Result.fail("获取创建sessionUrl失败: " + e.getMessage());
        }
    }

    /**
     * 更新创建会话 url
     */
    @PostMapping("/createUrl")
    public Result<Boolean> updateCreateUrl(@RequestBody String url) {
        try {
            agentConfig.setCreateUrl(url);
            log.info("createUrl被更新为 {}", agentConfig.getCreateUrl());
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新创建sessionUrl失败，value: {}", url, e);
            return Result.fail("更新创建sessionUrl失败: " + e.getMessage());
        }
    }


    /**
     * 获取删除会话 url
     */
    @GetMapping("/deleteUrl")
    public Result<Integer> getDeleteUrl() {
        try {
            return Result.ok(agentConfig.getDeleteUrl());
        } catch (Exception e) {
            log.error("获取删除sessionUrl失败", e);
            return Result.fail("获取删除sessionUrl失败: " + e.getMessage());
        }
    }

    /**
     * 更新删除会话 url
     */
    @PostMapping("/deleteUrl")
    public Result<Boolean> updateDeleteUrl(@RequestBody String url) {
        try {
            agentConfig.setDeleteUrl(url);
            log.info("deleteUrl被更新为 {}", agentConfig.getDeleteUrl());
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新删除sessionUrl失败，value: {}", url, e);
            return Result.fail("更新删除sessionUrl失败: " + e.getMessage());
        }
    }

    /**
     * 获取创建会话 token
     */
    @GetMapping("/createToken")
    public Result<Integer> getCreateToken() {
        try {
            return Result.ok(agentConfig.getCreateToken());
        } catch (Exception e) {
            log.error("获取创建sessionToken失败", e);
            return Result.fail("获取创建sessionToken失败: " + e.getMessage());
        }
    }

    /**
     * 更新创建会话 token
     */
    @PostMapping("/createToken")
    public Result<Boolean> updateCreateToken(@RequestBody String token) {
        try {
            agentConfig.setCreateToken(token);
            log.info("createToken被更新为 {}", agentConfig.getCreateToken());
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新创建sessionToken失败，value: {}", token, e);
            return Result.fail("更新创建sessionToken失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取删除会话 token
     */
    @GetMapping("/deleteToken")
    public Result<Integer> getDeleteToken() {
        try {
            return Result.ok(agentConfig.getDeleteToken());
        } catch (Exception e) {
            log.error("获取删除sessionToken失败", e);
            return Result.fail("获取删除sessionToken失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新删除会话 token
     */
    @PostMapping("/deleteToken")
    public Result<Boolean> updateDeleteToken(@RequestBody String token) {
        try {
            agentConfig.setDeleteToken(token);
            log.info("deleteToken被更新为 {}", agentConfig.getDeleteToken());
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新删除sessionToken失败，value: {}", token, e);
            return Result.fail("更新删除sessionToken失败: " + e.getMessage());
        }
    }

    /**
     * 获取批量插入数据量
     */
    @GetMapping("/insertBatchSize")
    public Result<Integer> getInsertBatchSize() {
        try {
            return Result.ok(BatchSplitUtil.getBatchSize());
        } catch (Exception e) {
            log.error("获取批量插入数据量失败", e);
            return Result.fail("获取批量插入数据量失败: " + e.getMessage());
        }
    }

    /**
     * 更新批量插入数据量
     */
    @PostMapping("/insertBatchSize")
    public Result<Boolean> updateInsertBatchSize(@RequestBody Integer size) {
        try {
            BatchSplitUtil.setBatchSize(size);
            log.info("insertBatchSize被更新为 {}", BatchSplitUtil.getBatchSize());
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新批量插入数据量失败，value: {}", size, e);
            return Result.fail("更新批量插入数据量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取会话过期时间
     */
    @GetMapping("/sessionExpireTime")
    public Result<Long> getSessionExpireTime() {
        try {
            return Result.ok(agentConfig.getSessionExpireTime());
        } catch (Exception e) {
            log.error("获取会话过期时间失败", e);
            return Result.fail("获取会话过期时间失败: " + e.getMessage());
        }
    }

    /**
     * 更新智能体上传文件 url
     */
    @PostMapping("/uploadUrl")
    public Result<Boolean> updateUploadUrl(@RequestBody Map<String, String> param) {
        try {
            String uploadUrl = param.get("uploadUrl");
            if (uploadUrl == null) {
                return Result.fail("参数 uploadUrl 不能为空");
            }
            agentConfig.setUploadFileUrl(uploadUrl);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新智能体上传文件 url失败，value: {}", param, e);
            return Result.fail("更新智能体上传文件 url失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新智能体类型
     */
    @PostMapping("/agentType")
    public Result<Boolean> updateSessionExpireTime(@RequestBody Map<String, String> param) {
        try {
            String agentType = param.get("agentType");
            if (agentType == null) {
                return Result.fail("参数agentType不能为空");
            }
            agentConfig.setAgentType(agentType);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("更新智能体类型失败，value: {}", param, e);
            return Result.fail("更新智能体类型失败: " + e.getMessage());
        }
    }

}