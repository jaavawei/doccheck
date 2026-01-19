package com.iwei.repository.service;

import com.iwei.common.entity.PageResult;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文档库表service
 *
 * @auther: zhaokangwei
 */
public interface RepositoryDocService {
    /**
     * 根据 id 查询
     */
    RepositoryDoc queryById(Integer id);

    /**
     * 新增文档库
     */
    void add(RepositoryDocVo repositoryDocVo) throws IOException;

    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    void addBatch(RepositoryDocVo repositoryDocVo, int batchSize);

    /**
     * 更新文档库
     */
    boolean update(RepositoryDocVo repositoryDocVo);

    /**
     * 删除文档库
     */
    void delete(RepositoryDocVo repositoryDocVo);

    /**
     * 查询文档库列表
     */
    PageResult<RepositoryDocVo> queryDocList(String docName, String projectName, Integer projectType, Integer pageNo, Integer pageSize);

    /**
     * 重新提取文档内容
     */
    void reextract(List<Integer> ids);

    /**
     * 查询文档库详情
     */
    RepositoryDocVo queryDocDetail(Integer id);

    /**
     * 根据提取id查询关联文档
     */
    List<RepositoryDocVo> queryByRuleExtracts(List<Integer> ruleExtractIds);

    /**
     * 查询预览文件流
     */
    void previewDoc(Integer id);

    /**
     * 下载文档
     */
    void downloadDoc(Integer id);

    /*
     * 导出结构化数据
     */
    void export(List<Integer> ids, Integer exportType);

    /*
     * 查询所有项目名称
     */
    PageResult<String> queryProjectNames(String projectName, Integer pageNo, Integer pageSize);

    /*
     * 查询项目名称下所有文档
     */
    List<RepositoryDocVo> queryByProjectNames(List<String> projectNames);

    /**
     * 根据提取规则id和项目名称查询关联文档
     */
    List<RepositoryDocVo> queryByRuleExtractsAndProjectNames(RepositoryDocVo repositoryDocVo);
}
