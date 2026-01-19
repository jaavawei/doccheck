package com.iwei.repository.service.impl;

import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.DataSourceEnum;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.PageInfoEnum;
import com.iwei.common.tool.BatchSplitUtil;
import com.iwei.common.tool.FileNameUtil;
import com.iwei.repository.convert.RepositoryReviewConverter;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.RepositoryReview;
import com.iwei.repository.entity.RepositoryReviewDocMapping;
import com.iwei.repository.entity.RepositoryReviewExtractMapping;
import com.iwei.repository.entity.vo.RepositoryReviewVo;
import com.iwei.repository.mapper.RepositoryReviewDocMappingMapper;
import com.iwei.repository.mapper.RepositoryReviewExtractMappingMapper;
import com.iwei.repository.mapper.RepositoryReviewMapper;
import com.iwei.repository.service.RepositoryReviewService;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.mapper.RuleExtractMapper;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审查库表service实现类
 *
 * @auther: zhaokangwei
 */
@Service
public class RepositoryReviewServiceImpl implements RepositoryReviewService {

    @Resource
    private RepositoryReviewMapper repositoryReviewMapper;
    @Resource
    private RepositoryReviewExtractMappingMapper repositoryReviewExtractMappingMapper;
    @Resource
    private RepositoryReviewDocMappingMapper repositoryReviewDocMappingMapper;
    @Autowired
    private RuleExtractMapper ruleExtractMapper;

    /**
     * 新增审查库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(RepositoryReviewVo repositoryReviewVo) {
        RepositoryReview repositoryReview = RepositoryReviewConverter.INSTANCE.convertVoToRepositoryReview(repositoryReviewVo);
        // 确保审查库名称不重复
        List<String> repoNames = repositoryReviewMapper.queryIdAndName().stream().map(RepositoryReview::getRepositoryReviewName).collect(Collectors.toList());
        String newName = FileNameUtil.generateUniqueFileName(repositoryReview.getRepositoryReviewName(), repoNames);
        repositoryReview.setRepositoryReviewName(newName);

        repositoryReview.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        repositoryReviewMapper.insert(repositoryReview);
        
        if(repositoryReviewVo.getDataSource() == DataSourceEnum.EXTERNAL.getCode()) {
            // 外部资源库不需要更新映射表
            return;
        }
        
        // 插入 审查库-提取规则 映射表
        if (repositoryReviewVo.getRuleExtractIds() != null && !repositoryReviewVo.getRuleExtractIds().isEmpty()) {
            List<RepositoryReviewExtractMapping> ruleMappingList = new ArrayList<>();
            for (Integer ruleExtractId : repositoryReviewVo.getRuleExtractIds()) {
                RepositoryReviewExtractMapping repositoryReviewExtractMapping = new RepositoryReviewExtractMapping();
                repositoryReviewExtractMapping.setRuleExtractId(ruleExtractId);
                repositoryReviewExtractMapping.setRepositoryReviewId(repositoryReview.getId());
                repositoryReviewExtractMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                ruleMappingList.add(repositoryReviewExtractMapping);
            }
            List<List<RepositoryReviewExtractMapping>> ruleMappingLists = BatchSplitUtil.splitList(ruleMappingList);
            for (List<RepositoryReviewExtractMapping> list : ruleMappingLists) {
                repositoryReviewExtractMappingMapper.batchInsert(list);
            }
        }

        // 插入 审查库-文档库 映射表
        if (repositoryReviewVo.getRepositoryDocIds() != null && !repositoryReviewVo.getRepositoryDocIds().isEmpty()) {
            List<RepositoryReviewDocMapping> docMappingList = new ArrayList<>();
            for (Integer repositoryDocId : repositoryReviewVo.getRepositoryDocIds()) {
                RepositoryReviewDocMapping repositoryReviewDocMapping = new RepositoryReviewDocMapping();
                repositoryReviewDocMapping.setRepositoryDocId(repositoryDocId);
                repositoryReviewDocMapping.setRepositoryReviewId(repositoryReview.getId());
                repositoryReviewDocMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                docMappingList.add(repositoryReviewDocMapping);
            }
            List<List<RepositoryReviewDocMapping>> docMappingLists = BatchSplitUtil.splitList(docMappingList);
            for (List<RepositoryReviewDocMapping> list : docMappingLists) {
                repositoryReviewDocMappingMapper.batchInsert(list);
            }
        }
    }

    /**
     * 更新审查库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReview(RepositoryReviewVo repositoryReviewVo) {
        // 更新审查库表
        RepositoryReview repositoryReview = RepositoryReviewConverter.INSTANCE.convertVoToRepositoryReview(repositoryReviewVo);
        repositoryReviewMapper.updateById(repositoryReview);

        // 如果是外部资源库，不需要更新映射表
        if (repositoryReviewVo.getDataSource() == DataSourceEnum.EXTERNAL.getCode()) {
            return;
        }

        // 删除原有的 审查库-提取规则 映射数据（逻辑删除）
        RepositoryReviewExtractMapping extractMapping = new RepositoryReviewExtractMapping();
        extractMapping.setRepositoryReviewId(repositoryReviewVo.getId());
        extractMapping.setDelFlg(DelFlgEnum.DELETED.getCode());
        repositoryReviewExtractMappingMapper.updateByReviewId(extractMapping);

        // 插入新的 审查库-提取规则 映射表
        if (repositoryReviewVo.getRuleExtractIds() != null && !repositoryReviewVo.getRuleExtractIds().isEmpty()) {
            List<RepositoryReviewExtractMapping> ruleMappingList = new ArrayList<>();
            for (Integer ruleExtractId : repositoryReviewVo.getRuleExtractIds()) {
                RepositoryReviewExtractMapping repositoryReviewExtractMapping = new RepositoryReviewExtractMapping();
                repositoryReviewExtractMapping.setRuleExtractId(ruleExtractId);
                repositoryReviewExtractMapping.setRepositoryReviewId(repositoryReviewVo.getId());
                repositoryReviewExtractMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                ruleMappingList.add(repositoryReviewExtractMapping);
            }
            List<List<RepositoryReviewExtractMapping>> ruleMappingLists = BatchSplitUtil.splitList(ruleMappingList);
            for (List<RepositoryReviewExtractMapping> list : ruleMappingLists) {
                repositoryReviewExtractMappingMapper.batchInsert(list);
            }
        }

        // 删除原有的 审查库-文档库 映射数据（逻辑删除）
        RepositoryReviewDocMapping docMapping = new RepositoryReviewDocMapping();
        docMapping.setRepositoryReviewId(repositoryReviewVo.getId());
        docMapping.setDelFlg(DelFlgEnum.DELETED.getCode());
        repositoryReviewDocMappingMapper.updateByReviewId(docMapping);

        // 插入新的 审查库-文档库 映射表
        if (repositoryReviewVo.getRepositoryDocIds() != null && !repositoryReviewVo.getRepositoryDocIds().isEmpty()) {
            List<RepositoryReviewDocMapping> docMappingList = new ArrayList<>();
            for (Integer repositoryDocId : repositoryReviewVo.getRepositoryDocIds()) {
                RepositoryReviewDocMapping repositoryReviewDocMapping = new RepositoryReviewDocMapping();
                repositoryReviewDocMapping.setRepositoryDocId(repositoryDocId);
                repositoryReviewDocMapping.setRepositoryReviewId(repositoryReviewVo.getId());
                repositoryReviewDocMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                docMappingList.add(repositoryReviewDocMapping);
            }
            List<List<RepositoryReviewDocMapping>> docMappingLists = BatchSplitUtil.splitList(docMappingList);
            for (List<RepositoryReviewDocMapping> list : docMappingLists) {
                repositoryReviewDocMappingMapper.batchInsert(list);
            }
        }
    }

    /**
     * 逻辑删除审查库
     */
    @Override
    public void deleteReview(RepositoryReviewVo repositoryReviewVo) {
        RepositoryReview repositoryReview = new RepositoryReview();
        repositoryReview.setId(repositoryReviewVo.getId());
        repositoryReview.setDelFlg(DelFlgEnum.DELETED.getCode());
        repositoryReviewMapper.updateById(repositoryReview);
    }

    /**
     * 查询审查库列表
     */
    @Override
    public PageResult<RepositoryReviewVo> queryReviewList(String repositoryReviewName, String projectName, Integer projectType, Integer pageNo, Integer pageSize) {
        PageResult<RepositoryReviewVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RepositoryReviewVo repositoryReviewVo = new RepositoryReviewVo();
        repositoryReviewVo.setRepositoryReviewName(repositoryReviewName);
        repositoryReviewVo.setProjectName(projectName);
        repositoryReviewVo.setProjectType(projectType);
        repositoryReviewVo.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = repositoryReviewMapper.countByCondition(repositoryReviewVo);
        if (total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RepositoryReview> list = repositoryReviewMapper.queryPageByCondition(repositoryReviewVo, pageSize, offset);
        List<RepositoryReviewVo> voList = RepositoryReviewConverter.INSTANCE.convertListToVoList(list);
        
        for (RepositoryReviewVo vo : voList) {
            if (vo.getDataSource() == DataSourceEnum.INTERNAL.getCode()) {
                // 获取关联的文档
                List<RepositoryDoc> repositoryDocList = repositoryReviewMapper.queryDocByReviewId(vo.getId());
                vo.setRepositoryDocs(repositoryDocList);
                
                // 获取关联的提取规则
                List<RuleExtract> ruleExtractList = repositoryReviewMapper.queryRuleByReviewId(vo.getId());
                vo.setRuleExtracts(ruleExtractList);
                
                // 提取规则名称列表
                List<String> ruleExtractNames = new ArrayList<>();
                for (RuleExtract ruleExtract : ruleExtractList) {
                    ruleExtractNames.add(ruleExtract.getRuleName());
                }
                vo.setRuleExtractNames(ruleExtractNames);

                // 关联的文档项目名称列表
                List<String> projectNames = repositoryDocList.stream().map(RepositoryDoc::getProjectName).collect(Collectors.toSet()).stream().collect(Collectors.toList());
                vo.setProjectNames(projectNames);

            }
        }

        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

}