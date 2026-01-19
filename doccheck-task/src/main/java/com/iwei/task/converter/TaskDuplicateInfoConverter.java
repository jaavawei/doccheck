package com.iwei.task.converter;

import com.iwei.task.entity.TaskDuplicateInfo;
import com.iwei.task.entity.vo.TaskDuplicateInfoVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 查重任务信息表（主任务表）实体类与vo转换器
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface TaskDuplicateInfoConverter {
    TaskDuplicateInfoConverter INSTANCE = Mappers.getMapper(TaskDuplicateInfoConverter.class);

    TaskDuplicateInfo convertVoToTaskDuplicateInfo(TaskDuplicateInfoVo TaskDuplicateInfoVo);

    TaskDuplicateInfoVo convertTaskDuplicateInfoToVo(TaskDuplicateInfo TaskDuplicateInfo);

    // Entity List -> Vo List
    List<TaskDuplicateInfoVo> convertListToVoList(List<TaskDuplicateInfo> list);

    // Vo List -> Entity List
    List<TaskDuplicateInfo> convertVoListToList(List<TaskDuplicateInfoVo> voList);
}
