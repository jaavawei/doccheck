package com.iwei.repository.controller;

import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.repository.entity.vo.RepositoryReviewVo;
import com.iwei.repository.service.RepositoryReviewService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 审查库 controller
 *
 * @auther: zhaokangwei
 */
@Slf4j
@RestController
@RequestMapping("/repository/review")
public class RepositoryReviewController {

    @Resource
    private RepositoryReviewService repositoryReviewService;

    /**
     * 新增审查库
     */
    @PostMapping("/addReview")
    public Result<Boolean> addReview(@RequestBody RepositoryReviewVo repositoryReviewVo) {
        try {
            log.info("repositoryReviewVo:{}", repositoryReviewVo);
            Preconditions.checkArgument(!(StringUtils.isBlank(repositoryReviewVo.getRepositoryReviewName())), "审查库名称不能为空");
            Preconditions.checkArgument(!(repositoryReviewVo.getDataSource() == null), "数据来源不能为空");
//            Preconditions.checkArgument(!(repositoryReviewVo.getProjectName() == null), "项目名称不能为空");
//            Preconditions.checkArgument(!(repositoryReviewVo.getProjectType() == null), "项目类型不能为空");
//            Preconditions.checkArgument(!(repositoryReviewVo.getProjectYear() == null), "项目年份不能为空");
            repositoryReviewService.addReview(repositoryReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryReviewController.addReview.error:{}", e.getMessage(), e);
            return Result.fail("新增审查库失败");
        }
    }

    /**
     * 更新审查库
     */
    @PutMapping("/updateReview")
    public Result<Boolean> updateReview(@RequestBody RepositoryReviewVo repositoryReviewVo) {
        try {
            log.info("repositoryReviewVo:{}", repositoryReviewVo);
            Preconditions.checkArgument(!(repositoryReviewVo.getId() == null), "审查库id不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(repositoryReviewVo.getRepositoryReviewName())), "审查库名称不能为空");
            Preconditions.checkArgument(!(repositoryReviewVo.getDataSource() == null), "数据来源不能为空");
            Preconditions.checkArgument(!(repositoryReviewVo.getProjectName() == null), "项目名称不能为空");
            repositoryReviewService.updateReview(repositoryReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryReviewController.updateReview.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 删除审查库
     */
    @DeleteMapping("/deleteReview")
    public Result<Boolean> deleteReview(@RequestBody RepositoryReviewVo repositoryReviewVo) {
        try {
            log.info("repositoryReviewVo:{}", repositoryReviewVo);
            Preconditions.checkArgument(!(repositoryReviewVo.getId() == null), "审查库id不能为空");
            repositoryReviewService.deleteReview(repositoryReviewVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryReviewController.deleteReview.error:{}", e.getMessage(), e);
            return Result.fail("删除审查库失败");
        }
    }

    /**
     * 查询审查库列表
     */
    @GetMapping("/queryReviewList")
    public Result<PageResult<RepositoryReviewVo>> queryReviewList(@RequestParam(required = false) String repositoryReviewName,
                                                                  @RequestParam(required = false) String projectName,
                                                                  @RequestParam(required = false) Integer projectType,
                                                                  @RequestParam(required = false) Integer pageNo,
                                                                  @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RepositoryReviewVo> pageResult = repositoryReviewService.queryReviewList(repositoryReviewName, projectName, projectType, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("RepositoryReviewController.queryReviewList.error:{}", e.getMessage(), e);
            return Result.fail("查询审查库列表失败");
        }
    }

}