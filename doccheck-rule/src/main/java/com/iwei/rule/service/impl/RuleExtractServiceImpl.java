package com.iwei.rule.service.impl;

import com.alibaba.fastjson.JSON;
import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.FileNameUtil;
import com.iwei.rule.convert.RuleExtractConverter;
import com.iwei.rule.entity.vo.RuleExtractVo;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.mapper.RuleExtractMapper;
import com.iwei.rule.service.RuleExtractService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.digester.Rule;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 提取规则表service实现类
 *
 * @auther: zhaokangwei
 */
@Service
@Slf4j
public class RuleExtractServiceImpl implements RuleExtractService {

    @Resource
    private RuleExtractMapper ruleExtractMapper;

    /**
     * 新增提取规则
     */
    @Override
    public int add(RuleExtractVo ruleExtractVo) {
        RuleExtract ruleExtract = RuleExtractConverter.INSTANCE.convertVoToRuleExtract(ruleExtractVo);
        // 生成一个 uuid 作为 bucket 名
        String bucket = UUID.randomUUID().toString().replaceAll("-", "");
        ruleExtract.setBucket(bucket);
        ruleExtract.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        List<String> ruleNames = ruleExtractMapper.queryAllRuleIdAndName().stream().map(RuleExtract::getRuleName).collect(Collectors.toList());
        String newName = FileNameUtil.generateUniqueFileName(ruleExtract.getRuleName(), ruleNames);
        ruleExtract.setRuleName(newName);
        log.info("RuleExtractServiceImpl.add.ruleExtract{}", ruleExtract);
        return ruleExtractMapper.insert(ruleExtract);
    }

    /**
     * 根据 id 更新
     */
    @Override
    public boolean update(RuleExtractVo ruleExtractVo) {
        RuleExtract ruleExtract = RuleExtractConverter.INSTANCE.convertVoToRuleExtract(ruleExtractVo);
        ruleExtract.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        log.info("RuleExtractServiceImpl.update.ruleExtract{}", JSON.toJSONString(ruleExtract));
        int count = ruleExtractMapper.updateById(ruleExtract);
        return count > 0;
    }

    /**
     * 逻辑删除，调用 update
     */
    @Override
    public boolean remove(RuleExtractVo ruleExtractVo) {
        RuleExtract ruleExtract = new RuleExtract();
        ruleExtract.setId(ruleExtractVo.getId());
        ruleExtract.setDelFlg(DelFlgEnum.DELETED.getCode());
        log.info("RuleExtractServiceImpl.remove.ruleExtract{}", JSON.toJSONString(ruleExtract));
        int count = ruleExtractMapper.updateById(ruleExtract);
        return count > 0;
    }

    /**
     * 分页查询提取规则列表
     */
    @Override
    public PageResult<RuleExtractVo> queryRuleList(String ruleName, String operateUser, Integer pageNo, Integer pageSize) {

        PageResult<RuleExtractVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RuleExtract ruleExtract = new RuleExtract();
        ruleExtract.setRuleName(ruleName);
        ruleExtract.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = ruleExtractMapper.countByCondition(ruleExtract);
        if(total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RuleExtract> list = ruleExtractMapper.queryPageByCondition(ruleExtract, offset, pageResult.getPageSize());
        List<RuleExtractVo> voList = RuleExtractConverter.INSTANCE.convertListToVoList(list);
        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }


    /**
     * 查询全部提取规则id和名称
     */
    @Override
    public List<RuleExtractVo> queryAllRuleIdAndName() {
        List<RuleExtract> list = ruleExtractMapper.queryAllRuleIdAndName();
        List<RuleExtractVo> voList = RuleExtractConverter.INSTANCE.convertListToVoList(list);
        return voList;
    }
}
