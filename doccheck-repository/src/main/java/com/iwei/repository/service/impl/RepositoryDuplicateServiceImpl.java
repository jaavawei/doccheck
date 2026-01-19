package com.iwei.repository.service.impl;

import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DataSourceEnum;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.BatchSplitUtil;
import com.iwei.common.tool.FileNameUtil;
import com.iwei.repository.convert.RepositoryDuplicateConverter;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.entity.RepositoryDuplicateDocMapping;
import com.iwei.repository.entity.RepositoryDuplicateExtractMapping;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import com.iwei.repository.mapper.RepositoryDocMapper;
import com.iwei.repository.mapper.RepositoryDuplicateDocMappingMapper;
import com.iwei.repository.mapper.RepositoryDuplicateExtractMappingMapper;
import com.iwei.repository.mapper.RepositoryDuplicateMapper;
import com.iwei.repository.service.RepositoryDuplicateService;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.mapper.RuleExtractMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查重库表service实现类
 *
 * @auther: zhaokangwei
 */
@Service
public class RepositoryDuplicateServiceImpl implements RepositoryDuplicateService {

    @Resource
    private RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    private RepositoryDuplicateDocMappingMapper repositoryDuplicateDocMappingMapper;
    @Autowired
    private RuleExtractMapper ruleExtractMapper;

    @Override
    public RepositoryDuplicate getById(Integer id) {
        return repositoryDuplicateMapper.queryById(id);
    }

    /**
     * 新增查重库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDuplicate(RepositoryDuplicateVo repositoryDuplicateVo) {
        RepositoryDuplicate repositoryDuplicate = RepositoryDuplicateConverter.INSTANCE.convertVoToRepositoryDuplicate(repositoryDuplicateVo);
        // 确保查重库名称不重复
        List<String> repoNames = repositoryDuplicateMapper.queryIdAndName().stream().map(RepositoryDuplicate::getRepositoryDuplicateName).collect(Collectors.toList());
        String newName = FileNameUtil.generateUniqueFileName(repositoryDuplicate.getRepositoryDuplicateName(), repoNames);
        repositoryDuplicate.setRepositoryDuplicateName(newName);
        repositoryDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        repositoryDuplicateMapper.insert(repositoryDuplicate);
        if(repositoryDuplicateVo.getDataSource() == DataSourceEnum.EXTERNAL.getCode()) {
            // 本地资源库需要额外更新 mapping 表，外部资源库则直接返回
            return;
        }

        // 插入 查重库-文档库 映射表
        List<Integer> RepositoryDocIds = repositoryDuplicateVo.getRepositoryDocIds();
        List<RepositoryDuplicateDocMapping> docMappingList = new ArrayList<>();
        for (Integer repositoryDocId : RepositoryDocIds) {
            RepositoryDuplicateDocMapping repositoryDuplicateDocMapping = new RepositoryDuplicateDocMapping();
            repositoryDuplicateDocMapping.setRepositoryDocId(repositoryDocId);
            repositoryDuplicateDocMapping.setRepositoryDuplicateId(repositoryDuplicate.getId());
            repositoryDuplicateDocMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            docMappingList.add(repositoryDuplicateDocMapping);
        }
        List<List<RepositoryDuplicateDocMapping>> docMappingLists = BatchSplitUtil.splitList(docMappingList);
        for (List<RepositoryDuplicateDocMapping> list : docMappingLists) {
            repositoryDuplicateDocMappingMapper.batchInsert(list);
        }


    }

    /**
     * 更新查重库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDuplicate(RepositoryDuplicateVo repositoryDuplicateVo) {
        RepositoryDuplicate repositoryDuplicate = RepositoryDuplicateConverter.INSTANCE.convertVoToRepositoryDuplicate(repositoryDuplicateVo);
        repositoryDuplicateMapper.updateById(repositoryDuplicate);
        if(repositoryDuplicateVo.getDataSource() == DataSourceEnum.EXTERNAL.getCode()) {
            // 本地资源库需要额外更新 mapping 表，外部资源库则直接返回
            return;
        }

        // 删除原有的查重库文档库映射表
        RepositoryDuplicateDocMapping docMapping = new RepositoryDuplicateDocMapping();
        docMapping.setRepositoryDuplicateId(repositoryDuplicateVo.getId());
        docMapping.setDelFlg(DelFlgEnum.DELETED.getCode());
        repositoryDuplicateDocMappingMapper.updateByDuplicateId(docMapping);

        // 在 查重库-文档库 映射表中插入新的记录
        List<Integer> RepositoryDocIds = repositoryDuplicateVo.getRepositoryDocIds();
        List<RepositoryDuplicateDocMapping> docMappingList = new ArrayList<>();
        for (Integer repositoryDocId : RepositoryDocIds) {
            RepositoryDuplicateDocMapping repositoryDuplicateDocMapping = new RepositoryDuplicateDocMapping();
            repositoryDuplicateDocMapping.setRepositoryDocId(repositoryDocId);
            repositoryDuplicateDocMapping.setRepositoryDuplicateId(repositoryDuplicate.getId());
            repositoryDuplicateDocMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            docMappingList.add(repositoryDuplicateDocMapping);
        }
        List<List<RepositoryDuplicateDocMapping>> docMappingLists = BatchSplitUtil.splitList(docMappingList);
        for (List<RepositoryDuplicateDocMapping> list : docMappingLists) {
            repositoryDuplicateDocMappingMapper.batchInsert(list);
        }
    }

    /**
     * 逻辑删除查重库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDuplicate(RepositoryDuplicateVo repositoryDuplicateVo) {
        RepositoryDuplicate repositoryDuplicate = new RepositoryDuplicate();
        repositoryDuplicate.setId(repositoryDuplicateVo.getId());
        repositoryDuplicate.setDelFlg(DelFlgEnum.DELETED.getCode());
        repositoryDuplicateMapper.updateById(repositoryDuplicate);
    }

    /**
     * 查询查重库列表
     */
    @Override
    public PageResult<RepositoryDuplicateVo> queryDuplicateList(String repositoryDuplicateName, Integer dataSource, Integer pageNo, Integer pageSize) {
        PageResult<RepositoryDuplicateVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RepositoryDuplicateVo repositoryDuplicateVo = new RepositoryDuplicateVo();
        repositoryDuplicateVo.setRepositoryDuplicateName(repositoryDuplicateName);
        repositoryDuplicateVo.setDataSource(dataSource);
        repositoryDuplicateVo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = repositoryDuplicateMapper.countByCondition(repositoryDuplicateVo);
        if (total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RepositoryDuplicate> list = repositoryDuplicateMapper.queryPageByCondition(repositoryDuplicateVo, pageSize, offset);
        List<RepositoryDuplicateVo> voList = RepositoryDuplicateConverter.INSTANCE.convertListToVoList(list);
        for (RepositoryDuplicateVo vo : voList) {
            if (vo.getDataSource() == DataSourceEnum.INTERNAL.getCode()) {
                RuleExtract ruleExtract = ruleExtractMapper.queryById(vo.getRuleExtractId());
                vo.setRuleExtractName(ruleExtract.getRuleName());
                List<RepositoryDoc> repositoryDocList = repositoryDuplicateMapper.queryDocByDuplicateId(vo.getId());
                vo.setRepositoryDocs(repositoryDocList);
            }
        }

        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

}
