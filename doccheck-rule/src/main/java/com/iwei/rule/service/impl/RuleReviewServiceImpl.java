package com.iwei.rule.service.impl;

import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.FileNameUtil;
import com.iwei.rule.convert.RuleReviewConverter;
import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.entity.vo.RuleReviewVo;
import com.iwei.rule.mapper.RuleReviewMapper;
import com.iwei.rule.service.RuleReviewService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审查规则表service实现类
 *
 * @auther: zhaokangwei
 */
@Service
public class RuleReviewServiceImpl implements RuleReviewService {

    @Resource
    private RuleReviewMapper ruleReviewMapper;

    @Override
    public RuleReview getById(Integer id) {
        return ruleReviewMapper.queryById(id);
    }

    /**
     * 新增审查规则
     */
    @Override
    public void addRuleReview(RuleReviewVo ruleReviewVo) {
        RuleReview ruleReview = RuleReviewConverter.INSTANCE.convertVoToRuleReview(ruleReviewVo);
        ruleReview.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        List<String> ruleNames = ruleReviewMapper.queryIdAndName().stream().map(RuleReview::getRuleName).collect(Collectors.toList());
        String newName = FileNameUtil.generateUniqueFileName(ruleReview.getRuleName(), ruleNames);
        ruleReview.setRuleName(newName);
        ruleReviewMapper.insert(ruleReview);
    }

    /**
     * 更新审查规则
     */
    @Override
    public void updateRuleReview(RuleReviewVo ruleReviewVo) {
        RuleReview ruleReview = RuleReviewConverter.INSTANCE.convertVoToRuleReview(ruleReviewVo);
        ruleReviewMapper.updateById(ruleReview);
    }

    /**
     * 逻辑删除审查规则
     */
    @Override
    public void deleteRuleReview(RuleReviewVo ruleReviewVo) {
        RuleReview ruleReview = new RuleReview();
        ruleReview.setId(ruleReviewVo.getId());
        ruleReview.setDelFlg(DelFlgEnum.DELETED.getCode());
        ruleReviewMapper.updateById(ruleReview);
    }

    /**
     * 查询审查规则列表
     */
    @Override
    public PageResult<RuleReviewVo> queryRuleReviewList(String ruleName, String operateUser, Integer pageNo, Integer pageSize) {
        PageResult<RuleReviewVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RuleReviewVo ruleReviewVo = new RuleReviewVo();
        ruleReviewVo.setRuleName(ruleName);
        ruleReviewVo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = ruleReviewMapper.countByCondition(ruleReviewVo);
        if (total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RuleReview> list = ruleReviewMapper.queryPageByCondition(ruleReviewVo, pageResult.getPageSize(), offset);
        List<RuleReviewVo> voList = RuleReviewConverter.INSTANCE.convertListToVoList(list);

        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

}