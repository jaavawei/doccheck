package com.iwei.repository.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.iwei.common.entity.PageResult;
import com.iwei.common.entity.Result;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.repository.service.RepositoryDocService;
import com.iwei.repository.service.impl.RepositoryDocServiceImpl;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.entity.vo.RuleExtractVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文档库 controller
 *
 * @auther: zhaokangwei
 */
@Slf4j
@RestController
@RequestMapping("/repository/doc")
public class RepositoryDocController {

    @Resource
    private RepositoryDocService repositoryDocService;

    /**
     * 查询文档库列表
     */
    @GetMapping("/queryDocList")
    public Result<PageResult<RepositoryDocVo>> queryDocList(@RequestParam(required = false) String docName,
                                                           @RequestParam(required = false) String projectName,
                                                           @RequestParam(required = false) Integer projectType,
                                                           @RequestParam(required = false) Integer pageNo,
                                                           @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<RepositoryDocVo> pageResult = repositoryDocService.queryDocList(docName, projectName, projectType, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryDocList.error:{}", e.getMessage(), e);
            return Result.fail("查询文档库列表失败");
        }
    }

    /**
     * 查询文档库详情
     */
    @GetMapping("/queryDocExtractDetail")
    public Result<RepositoryDocVo> queryDocExtractDetail(@RequestParam(required = true) Integer id) {
        try {
            RepositoryDocVo repositoryDocVo = repositoryDocService.queryDocDetail(id);
            return Result.ok(repositoryDocVo);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryDocDetail.error:{}", e.getMessage(), e);
            return Result.fail("查询文档库详情失败");
        }
    }

    /**
     * 获取预览文档流
     */
    @GetMapping("/previewDoc")
    public void previewDoc(@RequestParam(required = true) Integer id) {
        try {
            repositoryDocService.previewDoc(id);
        } catch (Exception e) {
            log.error("RepositoryDocController.previewDocStream.error:{}", e.getMessage(), e);
        }

    }

    /**
     * 下载文档
     */
    @GetMapping("/downloadDoc")
    public void downloadDoc(@RequestParam(required = true) Integer id) {
        try {
            repositoryDocService.downloadDoc(id);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("RepositoryDocController.previewDocStream.error:{}", e.getMessage(), e);
        }
    }

    /**
     * 新增文档库
     */
    @PostMapping("/addDoc")
    public Result<Boolean> addDoc(@ModelAttribute RepositoryDocVo repositoryDocVo) {
        try {
            Preconditions.checkArgument(!(repositoryDocVo.getRuleExtractId() == null), "提取规则Id不能为空");
            Preconditions.checkArgument(!(repositoryDocVo.getFiles() == null || repositoryDocVo.getFiles().size() < 1),
                    "至少上传一个文件");
            
            // 如果文件数量超过100个，则使用分批处理
            if (repositoryDocVo.getFiles().size() > 100) {
                ((RepositoryDocServiceImpl) repositoryDocService).addBatch(repositoryDocVo, 100);
            } else {
                repositoryDocService.add(repositoryDocVo);
            }
            
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryDocController.addDoc.error:{}", e.getMessage(), e);
            return Result.fail("新增文档库失败:" + e.getMessage());
        }
    }

    /**
     * 新增文档库（指定批次大小）
     */
    @PostMapping("/addDocBatch")
    public Result<Boolean> addDocBatch(@ModelAttribute RepositoryDocVo repositoryDocVo, 
                                       @RequestParam(defaultValue = "100") Integer batchSize) {
        try {
            Preconditions.checkArgument(!(repositoryDocVo.getRuleExtractId() == null), "提取规则Id不能为空");
            Preconditions.checkArgument(!(repositoryDocVo.getFiles() == null || repositoryDocVo.getFiles().size() < 1),
                    "至少上传一个文件");
            Preconditions.checkArgument(batchSize > 0 && batchSize <= 1000, "批次大小必须在1-1000之间");
            
            ((RepositoryDocServiceImpl) repositoryDocService).addBatch(repositoryDocVo, batchSize);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryDocController.addDocBatch.error:{}", e.getMessage(), e);
            return Result.fail("新增文档库失败:" + e.getMessage());
        }
    }

    /**
     * 重新提取文档内容
     */
    @PutMapping("/reextract")
    public Result<Boolean> reextract(@RequestBody RepositoryDocVo repositoryDocVo) {
        try {
            List<Integer> ids = repositoryDocVo.getIds();
            log.info("RepositoryDocController.updateDoc.ids:{}", JSON.toJSONString(ids));
            Preconditions.checkArgument(!(ids == null || ids.size() < 1), "文档Id不能为空");
            repositoryDocService.reextract(ids);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryDocController.reextract.error:{}", e.getMessage(), e);
            return Result.fail("重新提取文档失败");
        }
    }

    /**
     * 修改文档库
     */
    @PutMapping("/updateDocExtractContent")
    public Result<Boolean> updateDocExtractContent(@RequestBody RepositoryDocVo repositoryDocVo) {
        try {
            log.info("RepositoryDocController.updateDoc.repositoryDocVo:{}", JSON.toJSONString(repositoryDocVo));
            Preconditions.checkArgument(!(repositoryDocVo.getId() == null), "id不能为空");
            Preconditions.checkArgument(!(StringUtils.isBlank(repositoryDocVo.getExtractContent())), "提取内容不能为空");
            repositoryDocService.update(repositoryDocVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryDocController.updateDoc.error:{}", e.getMessage(), e);
            return Result.fail("更新文档库失败");
        }
    }

    /**
     * 删除文档库
     */
    @DeleteMapping("/deleteDoc")
    public Result<Boolean> deleteDoc(@RequestBody RepositoryDocVo repositoryDocVo) {
        try {
            log.info("RepositoryDocController.deleteDoc.repositoryDocVo:{}", JSON.toJSONString(repositoryDocVo));
            Preconditions.checkArgument(!(repositoryDocVo.getIds() == null || repositoryDocVo.getIds().size() < 1), "ids不能为空");
            repositoryDocService.delete(repositoryDocVo);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("RepositoryDocController.deleteDoc.error:{}", e.getMessage(), e);
            return Result.fail("删除文档库失败");
        }
    }

    /**
     * 查询关联文档
     */
    @GetMapping("/queryRelatedDoc")
    public Result<List<RepositoryDocVo>> queryByRuleExtract(@RequestParam List<Integer> ruleExtractIds) {
        try {
            Preconditions.checkArgument(!(ruleExtractIds == null || ruleExtractIds.size() < 1),"提取规则Id不能为空");
            List<RepositoryDocVo> list = repositoryDocService.queryByRuleExtracts(ruleExtractIds);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryRelatedDoc.error:{}", e.getMessage(), e);
            return Result.fail("查询关联文档失败");
        }
    }

    /*
     * 导出结构化数据
     */
    @GetMapping("/export")
    public void exportDocs(@RequestParam List<Integer> ids, Integer exportType) {
        try{
            Preconditions.checkArgument(!(ids == null || ids.size() < 1), "文档Id不能为空");
            Preconditions.checkArgument(!(exportType == null), "导出类型不能为空");
            repositoryDocService.export(ids, exportType);
        } catch (Exception e) {
            log.error("RepositoryDocController.exportDocs.error:{}", e.getMessage(), e);
            return;
        }
    }

    /*
     * 查询所有项目名称
     */
    @GetMapping("/queryProjectNames")
    public Result<PageResult<String>> queryProjectNames(@RequestParam(required = false) String projectName,
                                                        @RequestParam(required = false) Integer pageNo,
                                                        @RequestParam(required = false) Integer pageSize) {
        try {
            PageResult<String> pageResult = repositoryDocService.queryProjectNames(projectName, pageNo, pageSize);
            return Result.ok(pageResult);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryProjectNames.error:{}", e.getMessage(), e);
            return Result.fail("查询项目名失败");
        }
    }

    /*
     * 查询项目名称下所有文档
     */
    @GetMapping("/queryByProjectNames")
    public Result<List<RepositoryDocVo>> queryByProjectNames(@RequestParam List<String> projectNames) {
        try {
            Preconditions.checkArgument(!(projectNames == null || projectNames.size() < 1), "项目名不能为空");
            List<RepositoryDocVo> list = repositoryDocService.queryByProjectNames(projectNames);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryByProjectNames.error:{}", e.getMessage(), e);
            return Result.fail("根据项目名查询文档失败");
        }
    }


    /*
     * 查询项目名称下所有文档
     */
    @PostMapping("/queryByRuleExtractIdsAndProjectNames")
    public Result<List<RepositoryDocVo>> queryByRuleExtractsAndProjectNames(@RequestBody RepositoryDocVo repositoryDocVo) {
        try {
            List<RepositoryDocVo> list = repositoryDocService.queryByRuleExtractsAndProjectNames(repositoryDocVo);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("RepositoryDocController.queryByRuleExtractsAndProjectNames.error:{}", e.getMessage(), e);
            return Result.fail("根据规则和项目名查询文档失败");
        }
    }

}
