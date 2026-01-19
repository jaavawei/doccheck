package com.iwei.repository.convert;

import com.iwei.repository.entity.RepositoryDuplicate;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 查重库实体类与Vo转换器
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface RepositoryDuplicateConverter {
    RepositoryDuplicateConverter INSTANCE = Mappers.getMapper(RepositoryDuplicateConverter.class);

    RepositoryDuplicate convertVoToRepositoryDuplicate(RepositoryDuplicateVo RepositoryDuplicateVo);

    RepositoryDuplicateVo convertRepositoryDuplicateToVo(RepositoryDuplicate RepositoryDuplicate);

    List<RepositoryDuplicateVo> convertListToVoList(List<RepositoryDuplicate> list);

    List<RepositoryDuplicate> convertVoListToList(List<RepositoryDuplicateVo> voList);
}
