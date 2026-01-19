package com.iwei.rule.convert;

import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.entity.vo.RuleDuplicateVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查重规则实体类与Vo转换器
 */
@Mapper
public interface RuleDuplicateConverter {
    RuleDuplicateConverter INSTANCE = Mappers.getMapper(RuleDuplicateConverter.class);

    RuleDuplicate convertVoToRuleDuplicate(RuleDuplicateVo ruleDuplicateVo);

    RuleDuplicateVo convertRuleDuplicateToVo(RuleDuplicate ruleDuplicate);

    // Entity List -> Vo List
    List<RuleDuplicateVo> convertListToVoList(List<RuleDuplicate> list);

    // Vo List -> Entity List
    List<RuleDuplicate> convertVoListToList(List<RuleDuplicateVo> voList);

}
