package com.iwei.rule.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.rule.entity.vo.RuleExtractVo;
import com.iwei.rule.service.RuleExtractService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;


/**
 * 提取规则 controller
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/rule/extract")
@Slf4j
public class RuleExtractController {

    @Resource
    private RuleExtractService ruleExtractService;

    /**
     * 查询提取规则列表
     */
    @GetMapping("/queryRuleList")
    public Result<PageResult<RuleExtractVo>> queryRuleList(@RequestParam(required = false) String ruleName,
                                                           @RequestParam(required = false) String operateUser,
                                                           @RequestParam(required = false) Integer pageNo,
                                                           @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RuleExtractVo> pageResult = ruleExtractService.queryRuleList(ruleName, operateUser, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("RuleExtractController.queryRuleList.error:{}", e.getMessage(), e);
            return Result.fail("查询提取规则列表失败");
        }
    }

    /**
     * 新增提取规则
     */
    @PostMapping("/addRule")
    public Result<Boolean> addRule(@RequestBody RuleExtractVo ruleExtractVo) {
        try {
            log.info("RuleExtractController.add.ruleExtractVo:{}", JSON.toJSONString(ruleExtractVo));
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getRuleName()), "提取规则名称不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getElements()), "提取规则要素不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getAgentUrl()), "智能体api不能为空");
            ruleExtractService.add(ruleExtractVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleExtractController.addRule.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除提取规则
     */
    @DeleteMapping("/deleteRule")
    public Result<Boolean> deleteRule(@RequestBody RuleExtractVo ruleExtractVo) {
        try {
            log.info("RuleExtractController.add.ruleExtractVo:{}", JSON.toJSONString(ruleExtractVo));
            Preconditions.checkArgument(!(ruleExtractVo.getId() == null), "提取规则id不能为空");
            boolean b = ruleExtractService.remove(ruleExtractVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleExtractController.deleteRule.error:{}", e.getMessage(), e);
            return Result.fail("删除提取规则失败");
        }
    }

    /**
     * 更新提取规则
     */
    @PutMapping("/updateRule")
    public Result<Boolean> updateRule(@RequestBody RuleExtractVo ruleExtractVo) {
        try {
            log.info("RuleExtractController.updateRule.ruleExtractVo:{}", JSON.toJSONString(ruleExtractVo));
            Preconditions.checkArgument(!(ruleExtractVo.getId() == null), "提取规则id不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getRuleName()), "提取规则名称不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getElements()), "提取规则要素不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleExtractVo.getAgentUrl()), "智能体api不能为空");
            // 三个主要字段可以为空，dao字段识别为空的不更新
            boolean b = ruleExtractService.update(ruleExtractVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RuleExtractController.updateRule.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询全部提取规则id和名称
     */
    @GetMapping ("/queryAllRuleIdAndName")
    public Result<PageResult<RuleExtractVo>> queryAllRuleIdAndName() {
        try {
            List<RuleExtractVo> list = ruleExtractService.queryAllRuleIdAndName();
            return Result.ok(list);
        } catch (Exception e) {
            log.error("RuleExtractController.queryAllRuleIdAndName.error:{}", e.getMessage(), e);
            return Result.fail("查询全部提取规则失败");
        }
    }
}

