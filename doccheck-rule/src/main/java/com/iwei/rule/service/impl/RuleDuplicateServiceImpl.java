package com.iwei.rule.service.impl;

import com.alibaba.fastjson.JSON;
import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.FileNameUtil;
import com.iwei.rule.convert.RuleDuplicateConverter;
import com.iwei.rule.entity.RuleDuplicate;
import com.iwei.rule.entity.vo.RuleDuplicateVo;
import com.iwei.rule.mapper.RuleDuplicateMapper;
import com.iwei.rule.service.RuleDuplicateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查重规则表service实现类
 *
 * @auther: zhaokangwei
 */
@Service
@Slf4j
public class RuleDuplicateServiceImpl implements RuleDuplicateService {

    @Autowired
    private RuleDuplicateMapper ruleDuplicateMapper;

    /**
     * 新增查重规则
     */
    @Override
    public int add(RuleDuplicateVo ruleDuplicateVo) {
        RuleDuplicate ruleDuplicate = RuleDuplicateConverter.INSTANCE.convertVoToRuleDuplicate(ruleDuplicateVo);
        ruleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        List<String> ruleNames = ruleDuplicateMapper.queryIdAndName().stream().map(RuleDuplicate::getRuleName).collect(Collectors.toList());
        String newName = FileNameUtil.generateUniqueFileName(ruleDuplicateVo.getRuleName(), ruleNames);
        ruleDuplicate.setRuleName(newName);
        log.info("RuleDuplicateServiceImpl.add.ruleDuplicate{}", ruleDuplicate);
        return ruleDuplicateMapper.insert(ruleDuplicate);
    }

    /**
     * 根据 id 更新
     */
    @Override
    public boolean update(RuleDuplicateVo ruleDuplicateVo) {
        RuleDuplicate ruleDuplicate = RuleDuplicateConverter.INSTANCE.convertVoToRuleDuplicate(ruleDuplicateVo);
        ruleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        log.info("RuleDuplicateServiceImpl.update.ruleDuplicate{}", JSON.toJSONString(ruleDuplicate));
        int count = ruleDuplicateMapper.updateById(ruleDuplicate);
        return count > 0;
    }

    /**
     * 逻辑删除，调用 update
     */
    @Override
    public boolean remove(RuleDuplicateVo ruleDuplicateVo) {
        RuleDuplicate ruleDuplicate = new RuleDuplicate();
        ruleDuplicate.setId(ruleDuplicateVo.getId());
        ruleDuplicate.setDelFlg(DelFlgEnum.DELETED.getCode());
        log.info("RuleDuplicateServiceImpl.remove.ruleDuplicate{}", JSON.toJSONString(ruleDuplicate));
        int count = ruleDuplicateMapper.updateById(ruleDuplicate);
        return count > 0;
    }

    /**
     * 分页查询查重规则列表
     */
    @Override
    public PageResult<RuleDuplicateVo> queryRuleList(String ruleName, String operateUser, Integer pageNo, Integer pageSize) {

        PageResult<RuleDuplicateVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RuleDuplicate ruleDuplicate = new RuleDuplicate();
        ruleDuplicate.setRuleName(ruleName);

        // ruleDuplicate.setUpdatedBy(operateUser);
        // 只查询未删除的记录
        ruleDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = ruleDuplicateMapper.countByCondition(ruleDuplicate);
        if(total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RuleDuplicate> list = ruleDuplicateMapper.queryPageByCondition(ruleDuplicate, offset, pageResult.getPageSize());
        List<RuleDuplicateVo> voList = RuleDuplicateConverter.INSTANCE.convertListToVoList(list);
        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

}
