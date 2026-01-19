package com.iwei.repository.controller;

import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.common.enums.DataSourceEnum;
import com.iwei.repository.entity.vo.RepositoryDuplicateVo;
import com.iwei.repository.service.RepositoryDuplicateService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * 查重库 controller
 *
 * @auther: zhaokangwei
 */
@Slf4j
@RestController
@RequestMapping("/repository/duplicate")
public class RepositoryDuplicateController {

    @Resource
    private RepositoryDuplicateService repositoryDuplicateService;

    /**
     * 新增查重库
     */
    @PostMapping("/addDuplicate")
    public Result<Boolean> addDuplicate(@RequestBody RepositoryDuplicateVo repositoryDuplicateVo) {

        try {
            log.info("repositoryDuplicateVo:{}", repositoryDuplicateVo);
            Preconditions.checkArgument(!(StringUtils.isBlank(repositoryDuplicateVo.getRepositoryDuplicateName())),"查重库名称不能为空");
            Preconditions.checkArgument(!(repositoryDuplicateVo.getDataSource() == null),"数据来源不能为空");
            Preconditions.checkArgument(!(repositoryDuplicateVo.getRuleExtractId() == null),"提取规则id不能为空");
            if (repositoryDuplicateVo.getDataSource() == DataSourceEnum.INTERNAL.getCode()) {
                Preconditions.checkArgument(!(repositoryDuplicateVo.getRepositoryDocIds() == null || repositoryDuplicateVo.getRepositoryDocIds().isEmpty()),"至少选择一个文档");
            }
            repositoryDuplicateService.addDuplicate(repositoryDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.addDuplicate.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新查重库
     */
    @PutMapping("/updateDuplicate")
    public Result<Boolean> updateDuplicate(@RequestBody RepositoryDuplicateVo repositoryDuplicateVo) {

        try {
            log.info("repositoryDuplicateVo:{}", repositoryDuplicateVo);
            Preconditions.checkArgument(!(repositoryDuplicateVo.getId() == null),"查重库id不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(repositoryDuplicateVo.getRepositoryDuplicateName())),"查重库名称不能为空");
            Preconditions.checkArgument(!(repositoryDuplicateVo.getDataSource() == null),"数据来源不能为空");
            Preconditions.checkArgument(!(repositoryDuplicateVo.getRuleExtractId() == null),"提取规则id不能为空");
            if (repositoryDuplicateVo.getDataSource() == DataSourceEnum.INTERNAL.getCode()) {
                Preconditions.checkArgument(!(repositoryDuplicateVo.getRepositoryDocIds() == null || repositoryDuplicateVo.getRepositoryDocIds().isEmpty()),"至少选择一个文档");
            }
            repositoryDuplicateService.updateDuplicate(repositoryDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.updateDuplicate.error:{}", e.getMessage(), e);
            return Result.fail("更新查重库失败");
        }
    }

    /**
     * 删除查重库
     */
    @DeleteMapping("/deleteDuplicate")
    public Result<Boolean> deleteDuplicate(@RequestBody RepositoryDuplicateVo repositoryDuplicateVo) {
        try {
            log.info("repositoryDuplicateVo:{}", repositoryDuplicateVo);
            Preconditions.checkArgument(!(repositoryDuplicateVo.getId() == null),"查重库id不能为空");
            repositoryDuplicateService.deleteDuplicate(repositoryDuplicateVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.deleteDuplicate.error:{}", e.getMessage(), e);
            return Result.fail("删除查重库失败");
        }
    }

    /**
     * 查询查重库列表
     */
    @GetMapping("/queryDuplicateList")
    public Result<PageResult<RepositoryDuplicateVo>> queryDuplicateList(@RequestParam(required = false) String repositoryDuplicateName,
                                                            @RequestParam(required = false) Integer dataSource,
                                                            @RequestParam(required = false) Integer pageNo,
                                                            @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RepositoryDuplicateVo> pageResult = repositoryDuplicateService.queryDuplicateList(repositoryDuplicateName, dataSource,
                    pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("SubjectCategoryController.queryDocList.error:{}", e.getMessage(), e);
            return Result.fail("查询查重库列表失败");
        }
    }

}
