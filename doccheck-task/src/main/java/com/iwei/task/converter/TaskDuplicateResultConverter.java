package com.iwei.task.converter;

import com.iwei.task.entity.TaskDuplicateResult;
import com.iwei.task.entity.vo.TaskDuplicateResultVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 查重任务结果实体类与vo转换器
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface TaskDuplicateResultConverter {
    TaskDuplicateResultConverter INSTANCE = Mappers.getMapper(TaskDuplicateResultConverter.class);

    TaskDuplicateResult convertVoToTaskDuplicateResult(TaskDuplicateResultVo TaskDuplicateResultVo);

    TaskDuplicateResultVo convertTaskDuplicateResultToVo(TaskDuplicateResult TaskDuplicateResult);

    // Entity List -> Vo List
    List<TaskDuplicateResultVo> convertListToVoList(List<TaskDuplicateResult> list);

    // Vo List -> Entity List
    List<TaskDuplicateResult> convertVoListToList(List<TaskDuplicateResultVo> voList);
}
