package com.iwei.rule.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.rule.entity.vo.RuleDuplicateVo;
import com.iwei.rule.service.RuleDuplicateService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * 查重规则 controller
 *
 * @auther: zhaokangwei
 */
@RestController
@RequestMapping("/rule/duplicate")
@Slf4j
public class RuleDuplicateController {

    @Resource
    private RuleDuplicateService ruleDuplicateService;

    /**
     * 查询查重规则列表
     */
    @GetMapping("/queryRuleList")
    public Result<PageResult<RuleDuplicateVo>> queryRuleList(@RequestParam(required = false) String ruleName,
                                                           @RequestParam(required = false) String operateUser,
                                                           @RequestParam(required = false) Integer pageNo,
                                                           @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RuleDuplicateVo> pageResult = ruleDuplicateService.queryRuleList(ruleName, operateUser, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("SubjectCategoryController.queryRuleList.error:{}", e.getMessage(), e);
            return Result.fail("新增查重规则失败");
        }
    }

    /**
     * 新增查重规则
     */
    @PostMapping("/addRule")
    public Result<Boolean> addRule(@RequestBody RuleDuplicateVo ruleDuplicateVo) {
        try {
            log.info("RuleDuplicateController.add.ruleDuplicateVo:{}", JSON.toJSONString(ruleDuplicateVo));
            Preconditions.checkArgument(!StringUtils.isBlank(ruleDuplicateVo.getRuleName()), "查重规则名称不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleDuplicateVo.getContent()), "查重内容不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleDuplicateVo.getAgentUrl()), "智能体api不能为空");
            ruleDuplicateService.add(ruleDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.addRule.error:{}", e.getMessage(), e);
            return Result.fail("新增查重规则失败");
        }
    }

    /**
     * 删除查重规则
     */
    @DeleteMapping("/deleteRule")
    public Result<Boolean> deleteRule(@RequestBody RuleDuplicateVo ruleDuplicateVo) {
        try {
            log.info("RuleDuplicateController.add.ruleDuplicateVo:{}", JSON.toJSONString(ruleDuplicateVo));
            Preconditions.checkArgument(!(ruleDuplicateVo.getId() == null), "查重规则id不能为空");
            boolean b = ruleDuplicateService.remove(ruleDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.add.error:{}", e.getMessage(), e);
            return Result.fail("删除查重规则失败");
        }
    }

    /**
     * 更新查重规则
     */
    @PutMapping("/updateRule")
    public Result<Boolean> updateRule(@RequestBody RuleDuplicateVo ruleDuplicateVo) {
        try {
            log.info("RuleDuplicateController.updateRule.ruleDuplicateVo:{}", JSON.toJSONString(ruleDuplicateVo));
            Preconditions.checkArgument(!(ruleDuplicateVo.getId() == null), "查重规则id不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleDuplicateVo.getContent()), "查重内容不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(ruleDuplicateVo.getAgentUrl()), "智能体api不能为空");
            // 三个主要字段可以为空，dao字段识别为空的不更新
            boolean b = ruleDuplicateService.update(ruleDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.updateRule.error:{}", e.getMessage(), e);
            return Result.fail("更新查重规则失败");
        }
    }
}
