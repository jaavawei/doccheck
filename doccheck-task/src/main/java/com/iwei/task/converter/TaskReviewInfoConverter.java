package com.iwei.task.converter;

import com.iwei.task.entity.TaskReviewInfo;
import com.iwei.task.entity.vo.TaskReviewInfoVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 审查任务信息表 converter
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface TaskReviewInfoConverter {
    TaskReviewInfoConverter INSTANCE = Mappers.getMapper(TaskReviewInfoConverter.class);

    /**
     * TaskReviewInfo 转换为 TaskReviewInfoVo
     */
    TaskReviewInfoVo convertToVo(TaskReviewInfo taskReviewInfo);

    /**
     * TaskReviewInfoVo 转换为 TaskReviewInfo
     */
    TaskReviewInfo convertToEntity(TaskReviewInfoVo taskReviewInfoVo);

    /**
     * TaskReviewInfo List 转换为 TaskReviewInfoVo List
     */
    List<TaskReviewInfoVo> convertToVoList(List<TaskReviewInfo> taskReviewInfoList);

    /**
     * TaskReviewInfoVo List 转换为 TaskReviewInfo List
     */
    List<TaskReviewInfo> convertToEntityList(List<TaskReviewInfoVo> taskReviewInfoVoList);
}