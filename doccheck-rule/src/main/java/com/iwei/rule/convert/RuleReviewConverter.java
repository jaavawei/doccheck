package com.iwei.rule.convert;

import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.entity.vo.RuleReviewVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 审查规则实体类与Vo转换器
 */
@Mapper
public interface RuleReviewConverter {
    RuleReviewConverter INSTANCE = Mappers.getMapper(RuleReviewConverter.class);

    RuleReview convertVoToRuleReview(RuleReviewVo ruleReviewVo);

    RuleReviewVo convertRuleReviewToVo(RuleReview ruleReview);

    // Entity List -> Vo List
    List<RuleReviewVo> convertListToVoList(List<RuleReview> list);

    // Vo List -> Entity List
    List<RuleReview> convertVoListToList(List<RuleReviewVo> voList);

}