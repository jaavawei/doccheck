package com.iwei.repository.mapper;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.StationLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站线Mapper接口
 *
 * @author: zhaokangwei
 */
@Mapper
public interface StationLineMapper {

    /**
     * 根据ID查询站线
     * @param id 站线ID
     * @return 站线对象
     */
    StationLine selectById(@Param("id") Integer id);

    /**
     * 查询所有未删除的站线
     * @return 站线列表
     */
    List<StationLine> selectAll();

    /**
     * 插入站线记录
     * @param stationLine 站线对象
     * @return 影响行数
     */
    int insert(StationLine stationLine);

    /**
     * 根据ID更新站线记录
     * @param stationLine 站线对象
     * @return 影响行数
     */
    int updateById(StationLine stationLine);

    /**
     * 根据ID逻辑删除站线记录
     * @param id 站线ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 根据查重库ID查询站线
     * @param repositoryDuplicateId 仓库重复ID
     * @return 站线列表
     */
    List<StationLine> queryByRepoDuplicateId(Integer repositoryDuplicateId);

    /**
     * 根据站线名称查询站线
     * @param stationLineName 站线名称
     * @return 站线对象
     */
    StationLine queryByName(String stationLineName);

    /**
     * 批量插入站线记录
     * @param stationLineList 站线列表
     */
    void batchInsert(List<StationLine> stationLineList);

    /**
     * 根据站线名称批量查询站线
     * @param stationLineNames 站线名称列表
     * @return 站线列表
     */
    List<StationLine> queryByNames(String[] stationLineNames);

    /**
     * 根据文档ID查询站线
     * @param sourceId 文档ID
     * @return 站线列表
     */
    List<StationLine> queryByDocId(Integer sourceId);

    List<Integer> queryStationLineIdByDocId(Integer sourceId);

    List<RepositoryDoc> queryByStationIdAndRepoDuplicateId(Integer stationLineId, Integer repositoryDuplicateId);
}