package com.iwei.rule.controller;

import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.rule.entity.vo.RuleReviewVo;
import com.iwei.rule.service.RuleReviewService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * 审查规则 controller
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/rule/review")
@Slf4j
public class RuleReviewController {
    @Resource
    private RuleReviewService ruleReviewService;

    /**
     * 查询审查规则列表
     */
    @GetMapping("/queryRuleList")
    public Result<PageResult<RuleReviewVo>> queryRuleReviewList(@RequestParam(required = false) String ruleName,
                                                                @RequestParam(required = false) String operateUser,
                                                                @RequestParam(required = false) Integer pageNo,
                                                                @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RuleReviewVo> pageResult = ruleReviewService.queryRuleReviewList(ruleName, operateUser, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("RuleReviewController.queryRuleReviewList.error:{}", e.getMessage(), e);
            return Result.fail("查询审查规则列表失败");
        }
    }

    /**
     * 新增审查规则
     */
    @PostMapping("/addRule")
    public Result<Boolean> addRuleReview(@RequestBody RuleReviewVo ruleReviewVo) {
        try {
            log.info("ruleReviewVo:{}", ruleReviewVo);
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getRuleName())), "规则名称不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getContent())), "规则内容不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getAgentUrl())), "智能体api不能为空");
            ruleReviewService.addRuleReview(ruleReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleReviewController.addRuleReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除审查规则
     */
    @DeleteMapping("/deleteRule")
    public Result<Boolean> deleteRuleReview(@RequestBody RuleReviewVo ruleReviewVo) {
        try {
            log.info("ruleReviewVo:{}", ruleReviewVo);
            Preconditions.checkArgument(!(ruleReviewVo.getId() == null), "审查规则id不能为空");
            ruleReviewService.deleteRuleReview(ruleReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleReviewController.deleteRuleReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新审查规则
     */
    @PutMapping("/updateRule")
    public Result<Boolean> updateRuleReview(@RequestBody RuleReviewVo ruleReviewVo) {
        try {
            log.info("ruleReviewVo:{}", ruleReviewVo);
            Preconditions.checkArgument(!(ruleReviewVo.getId() == null), "规则id不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getRuleName())), "规则名称不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getContent())), "规则内容不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(ruleReviewVo.getAgentUrl())), "智能体api不能为空");
            ruleReviewService.updateRuleReview(ruleReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleReviewController.updateRuleReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
}