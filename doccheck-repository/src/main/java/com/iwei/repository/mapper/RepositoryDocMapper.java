package com.iwei.repository.mapper;


import com.iwei.repository.entity.RepositoryDoc;
import java.util.List;

/**
 * 文档库表mapper
 *
 * @auther: zhaokangwei
 */

public interface RepositoryDocMapper {

    /**
     * 根据id查询
     */
    RepositoryDoc queryById(Integer id);

    /**
     * 查询所有
     */
    List<RepositoryDoc> queryAll();

    int insert(RepositoryDoc repositoryDoc);

    int batchInsert(List<RepositoryDoc> list);

    int updateById(RepositoryDoc repositoryDoc);

    int deleteById(Integer id);

    /**
     * 根据条件计数
     */
    int countByCondition(RepositoryDoc repositoryDoc);

    /**
     * 根据条件查询list
     */
    List<RepositoryDoc> queryPageByCondition(RepositoryDoc repositoryDoc, Integer pageSize, int offset);

    /**
     * 根据多个 ExtractRuleId 查找
     */
    List<RepositoryDoc> queryByExtractRuleIds(List<Integer> extractRuleIds);

    /*
     * 根据多个 id 查询
     */
    List<RepositoryDoc> queryByIds(List<Integer> ids);

    /*
     * 查询所有项目名称
     */
    List<String> queryProjectNames();

    /*
     * 查询项目名称下所有文档
     */
    List<RepositoryDoc> queryByProjectNames(List<String> projectNames);

    /*
     * 根据多个 ExtractRuleId 和 多个 ProjectName 查找
     */
    List<RepositoryDoc> queryByExtractRuleIdsAndProjectNames(List<Integer> ruleExtractIds, List<String> projectNames);

    /**
     * 根据 StationId 和 RepositoryDuplicateId 查找
     */
    List<RepositoryDoc> queryByStationIdAndRepoDuplicateId(Integer stationLineId, Integer repositoryDuplicateId);

    /**
     * 查找一个站线和一个查重库里的所有文档
     */
    List<RepositoryDoc> queryUnderStationIdAndRepoDuplicateId(Integer repositoryDocId, Integer repositoryDuplicateId);

    List<Integer> queryDocIdByStationLineIdList(List<Integer> stationLineIdList);

    List<Integer> queryDocIdByStationLineId(Integer stationLineId);

    Integer countProjectNames(String projectName);

    List<String> queryPageProjectNames(String projectName, Integer pageSize, Integer offset);
}
