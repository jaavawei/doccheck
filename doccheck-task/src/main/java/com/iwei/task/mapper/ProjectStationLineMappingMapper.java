package com.iwei.task.mapper;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.task.entity.ProjectStationLineMapping;
import org.apache.catalina.LifecycleState;

import java.util.List;
import java.util.Map;

public interface ProjectStationLineMappingMapper {
    /*
     * 插入记录
     */
    void insert(ProjectStationLineMapping record);

    /*
     * 根据条件查询
     */
    List<ProjectStationLineMapping> queryByCondition(ProjectStationLineMapping record);

    /*
     * 批量插入
     */
    void batchInsert(List<ProjectStationLineMapping> resultList);

    /*
     * 查询站线名称
     */
    List<String> queryDistinctStationLineName();

    /*
     * 查询同一站线下的所有文档
     */
    List<RepositoryDoc> queryDocsByStationLineNameAndRepositoryDuplicateId(String stationLineName, Integer repositoryDuplicateId);

    ProjectStationLineMapping queryByDocId(Integer docId);

}
