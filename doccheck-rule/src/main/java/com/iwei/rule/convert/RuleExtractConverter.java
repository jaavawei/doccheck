package com.iwei.rule.convert;

import com.iwei.rule.entity.vo.RuleExtractVo;
import com.iwei.rule.entity.RuleExtract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 提取规则实体类与Vo转换器
 */
@Mapper
public interface RuleExtractConverter {
    RuleExtractConverter INSTANCE = Mappers.getMapper(RuleExtractConverter.class);

    RuleExtract convertVoToRuleExtract(RuleExtractVo ruleExtractVo);

    RuleExtractVo convertRuleExtractToVo(RuleExtract ruleExtract);

    List<RuleExtractVo> convertListToVoList(List<RuleExtract> list);

    List<RuleExtract> convertVoListToList(List<RuleExtractVo> voList);

}

