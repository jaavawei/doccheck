package com.iwei.repository.service;

import com.iwei.common.entity.PageResult;
import com.iwei.repository.entity.vo.RepositoryReviewVo;

/**
 * 审查库表service
 *
 * @auther: zhaokangwei
 */
public interface RepositoryReviewService {

    /**
     * 新增审查库
     */
    void addReview(RepositoryReviewVo repositoryReviewVo);

    /**
     * 更新审查库
     */
    void updateReview(RepositoryReviewVo repositoryReviewVo);

    /**
     * 删除审查库
     */
    void deleteReview(RepositoryReviewVo repositoryReviewVo);

    /**
     * 分页查询审查库列表
     */
    PageResult<RepositoryReviewVo> queryReviewList(String repositoryReviewName, String projectName, Integer projectType, Integer pageNo, Integer pageSize);
}