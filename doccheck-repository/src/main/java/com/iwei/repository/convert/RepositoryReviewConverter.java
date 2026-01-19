package com.iwei.repository.convert;

import com.iwei.repository.entity.RepositoryReview;
import com.iwei.repository.entity.vo.RepositoryReviewVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 审查库实体类与Vo转换器
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface RepositoryReviewConverter {
    RepositoryReviewConverter INSTANCE = Mappers.getMapper(RepositoryReviewConverter.class);

    RepositoryReview convertVoToRepositoryReview(RepositoryReviewVo RepositoryReviewVo);

    RepositoryReviewVo convertRepositoryReviewToVo(RepositoryReview RepositoryReview);

    List<RepositoryReviewVo> convertListToVoList(List<RepositoryReview> list);

    List<RepositoryReview> convertVoListToList(List<RepositoryReviewVo> voList);
}