package com.iwei.task.converter;

import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.entity.vo.TaskDuplicateDocVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 查重任务文档表（子任务表）实体类与vo转换器
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface TaskDuplicateDocConverter {
    TaskDuplicateDocConverter INSTANCE = Mappers.getMapper(TaskDuplicateDocConverter.class);

    TaskDuplicateDoc convertVoToTaskDuplicateDoc(TaskDuplicateDocVo TaskDuplicateDocVo);

    TaskDuplicateDocVo convertTaskDuplicateDocToVo(TaskDuplicateDoc TaskDuplicateDoc);

    // Entity List -> Vo List
    List<TaskDuplicateDocVo> convertListToVoList(List<TaskDuplicateDoc> list);

    // Vo List -> Entity List
    List<TaskDuplicateDoc> convertVoListToList(List<TaskDuplicateDocVo> voList);
}
