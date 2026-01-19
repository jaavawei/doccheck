package com.iwei.task.controller;


import com.iwei.common.entity.Result;
import com.iwei.task.service.XjService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/xj")
public class XjController {

    @Resource
    private XjService xjService;

    /**
     * 上传站线信息文档
     */
    @PostMapping("/uploadStationLine")
    public Result<Boolean> addTaskReview(@RequestParam("file") MultipartFile file) {
        try {
            xjService.parseStationLine(file);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.updateDoc.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 创建 base 查重库
     */
    @PostMapping("/base")
    public Result<Boolean> base() {
        try {
            xjService.base();
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.updateDoc.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/updateDoc")
    public Result<Boolean> updateDoc() {
        try {
            xjService.updateDoc();
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.updateDoc.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/updateToDoc")
    public Result<Boolean> updateToDoc() {
        try {
            xjService.updateToDoc();
            return Result.ok(true);
        } catch (Exception e) {
            log.error("TaskReviewController.updateToDoc.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

}
