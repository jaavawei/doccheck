package com.iwei.repository.convert;

import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


/**
 * 文档库实体类与Vo转换器
 */
@Mapper
public interface RepositoryDocConverter {
    RepositoryDocConverter INSTANCE = Mappers.getMapper(RepositoryDocConverter.class);

    RepositoryDoc convertVoToRepositoryDoc(RepositoryDocVo RepositoryDocVo);

    RepositoryDocVo convertRepositoryDocToVo(RepositoryDoc RepositoryDoc);

    List<RepositoryDocVo> convertListToVoList(List<RepositoryDoc> list);

    List<RepositoryDoc> convertVoListToList(List<RepositoryDocVo> voList);

}

