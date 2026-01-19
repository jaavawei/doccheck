package com.iwei.repository.service.impl;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.entity.PageResult;
import com.iwei.common.enums.*;
import com.iwei.common.tool.*;
import com.iwei.oss.service.FileService;
import com.iwei.repository.convert.RepositoryDocConverter;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.entity.ScheduleExtract;
import com.iwei.repository.entity.vo.RepositoryDocVo;
import com.iwei.repository.mapper.RepositoryDocMapper;
import com.iwei.repository.mapper.ScheduleExtractMapper;
import com.iwei.repository.service.RepositoryDocService;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.mapper.RuleExtractMapper;

import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档库表service实现类
 *
 * @auther: zhaokangwei
 */
@Slf4j
@Service
public class RepositoryDocServiceImpl implements RepositoryDocService {

    @Resource
    private RepositoryDocMapper repositoryDocMapper;
    @Resource
    private RuleExtractMapper ruleExtractMapper;
    @Resource
    private FileService fileService;
    @Resource
    private ScheduleExtractMapper scheduleExtractMapper;
    @Resource
    private DocUtil docUtil;

    /**
     * 根据 id 查询
     */
    @Override
    public RepositoryDoc queryById(Integer id) {
        return repositoryDocMapper.queryById(id);
    }

    /**
     * 新增文档库
     * 共分4步：1.上传文档到oss 2.解析文档 3.智能体提取 4.存储数据库
     */
    @Override
    @SneakyThrows
    public void add(RepositoryDocVo repositoryDocVo) {
        List<MultipartFile> files = repositoryDocVo.getFiles();

        // 填充所有文档公共字段
        RepositoryDoc repositoryDoc = RepositoryDocConverter.INSTANCE.convertVoToRepositoryDoc(repositoryDocVo);
        repositoryDoc.setStatus(TaskStatusEnum.UNEXECUTED.getCode());
        repositoryDoc.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        RuleExtract ruleExtract = ruleExtractMapper.queryById(repositoryDoc.getRuleExtractId());
        String path = ruleExtract.getBucket();
        path = repositoryDocVo.getProjectName() + "/" + path;
        // 每个文档分别处理
        for (MultipartFile file : files) {
            String fileName = FileNameUtil.processFileName(file.getOriginalFilename());
            log.info("fileName:" + fileName);
//            // 确保同一提取规则 & 同一项目名称下没有重名文件
//            List<String> docNames = repositoryDocMapper.queryByExtractRuleIds(Arrays.asList(ruleExtract.getId()))
//                    .stream()
//                    .map(RepositoryDoc::getDocName)
//                    .toList();
//            fileName = FileNameUtil.generateUniqueFileName(fileName, docNames);
//            log.info("fileName:" + fileName);
            String url = fileService.uploadFile(file.getInputStream(), fileName, "repository", path);

            // 存储数据到 repository_doc 表
            repositoryDoc.setDocName(fileName);
            repositoryDoc.setDocUrl(url);
            repositoryDocMapper.insert(repositoryDoc);

            // 存储任务到 schedule_extract 表
            ScheduleExtract scheduleExtract = new ScheduleExtract();
            scheduleExtract.setSourceId(repositoryDoc.getId());
            scheduleExtract.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
            scheduleExtract.setTaskSource(ScheduleTaskSourceEnum.REPOSITORY.getCode());
            scheduleExtract.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            scheduleExtractMapper.insert(scheduleExtract);

        }
    }

    /**
     * 新增文档库（分批处理）
     * 
     * @param repositoryDocVo 文档信息
     * @param batchSize 每批处理的文件数量
     */
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBatch(RepositoryDocVo repositoryDocVo, int batchSize) {
        List<MultipartFile> files = repositoryDocVo.getFiles();
        
        // 记录失败的文件
        List<String> failedFiles = new ArrayList<>();
        
        // 分批处理文件，避免事务超时
        for (int i = 0; i < files.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, files.size());
            List<MultipartFile> batchFiles = files.subList(i, endIndex);
            
            // 创建批次的RepositoryDocVo
            RepositoryDocVo batchDocVo = new RepositoryDocVo();
            batchDocVo.setProjectName(repositoryDocVo.getProjectName());
            batchDocVo.setProjectType(repositoryDocVo.getProjectType());
            batchDocVo.setRuleExtractId(repositoryDocVo.getRuleExtractId());
            batchDocVo.setFiles(batchFiles);
            
            // 处理当前批次，增加错误处理和重试机制
            try {
                add(batchDocVo);
            } catch (Exception e) {
                log.error("处理批次文件时发生错误，尝试重试", e);
                boolean retrySuccess = false;
                
                // 重试机制，最多重试3次
                for (int retry = 1; retry <= 3; retry++) {
                    try {
                        log.info("第{}次重试处理批次文件", retry);
                        Thread.sleep(1000 * retry); // 等待一段时间再重试
                        add(batchDocVo);
                        retrySuccess = true;
                        break;
                    } catch (Exception retryException) {
                        log.error("第{}次重试失败", retry, retryException);
                    }
                }
                
                // 如果重试都失败了，记录失败的文件
                if (!retrySuccess) {
                    for (MultipartFile file : batchFiles) {
                        failedFiles.add(file.getOriginalFilename());
                        log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                    }
                }
            }
        }
        
        // 如果有失败的文件，抛出异常并包含失败信息
        if (!failedFiles.isEmpty()) {
            throw new RuntimeException("以下文件上传失败: " + String.join(", ", failedFiles));
        }
    }

    /**
     * 更新文档库
     */
    @Override
    public boolean update(RepositoryDocVo repositoryDocVo) {
        RepositoryDoc repositoryDoc = RepositoryDocConverter.INSTANCE.convertVoToRepositoryDoc(repositoryDocVo);
        Map<String, String> extractMap = JSON.parseObject(repositoryDoc.getExtractContent(), Map.class);
        if (extractMap != null) {
            repositoryDoc.setProjectCode(extractMap.get("项目编码"));
            repositoryDoc.setPlanYear(extractMap.get("计划年度"));
            repositoryDoc.setProjectMsg(extractMap.get("站线名称"));
            repositoryDoc.setImplOrg(extractMap.get("实施单位名称"));
            repositoryDoc.setProjectName(extractMap.get("项目名称"));
        }
        int count = repositoryDocMapper.updateById(repositoryDoc);
        return count > 0;
    }

    /**
     * 逻辑删除文档库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RepositoryDocVo repositoryDocVo) {
        for (Integer id : repositoryDocVo.getIds()) {
            RepositoryDoc repositoryDoc = new RepositoryDoc();
            repositoryDoc.setId(id);
            // 更新删除标识
            repositoryDoc.setDelFlg(DelFlgEnum.DELETED.getCode());
            repositoryDocMapper.updateById(repositoryDoc);
        }
    }

    /**
     * 分页查询文档库列表
     */
    @Override
    @SneakyThrows
    public PageResult<RepositoryDocVo> queryDocList(String docName, String projectName, Integer projectType, Integer pageNo, Integer pageSize) {

        PageResult<RepositoryDocVo> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);

        RepositoryDoc repositoryDoc = new RepositoryDoc();
        repositoryDoc.setProjectName(projectName);
        repositoryDoc.setProjectType(projectType);
        repositoryDoc.setDocName(docName);
        repositoryDoc.setDelFlg(DelFlgEnum.UN_DELETED.getCode());

        // 查询满足条件的总记录数
        int total = repositoryDocMapper.countByCondition(repositoryDoc);
        if (total == 0) {
            // 没有数据，无需查询，直接返回
            return pageResult;
        }

        // 继续根据条件分页查询记录
        int offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();// 偏移量
        List<RepositoryDoc> list = repositoryDocMapper.queryPageByCondition(repositoryDoc, pageSize, offset);
        List<RepositoryDocVo> voList = RepositoryDocConverter.INSTANCE.convertListToVoList(list);

        for (RepositoryDocVo vo : voList) {
            // 填入提取规则内容、名称
            RuleExtract ruleExtract = ruleExtractMapper.queryById(vo.getRuleExtractId());
            if (ruleExtract != null) {
                vo.setRuleExtractName(ruleExtract.getRuleName());
                vo.setRuleExtractContent(ruleExtract.getElements());
            } else {
                vo.setRuleExtractName("已删除");
                vo.setRuleExtractContent("已删除");
            }
            // 封装预览url
//            try {
//                String base64Encoded = Base64.getEncoder().encodeToString(vo.getDocUrl().getBytes(StandardCharsets.UTF_8));
//                String urlEncoded = URLEncoder.encode(base64Encoded, StandardCharsets.UTF_8.name());
//                String previewUrl = kkviewUrl + urlEncoded;
//                vo.setPreviewUrl(previewUrl);
//            } catch (Exception e) {
//                throw new RuntimeException("预览url构建失败", e);
//            }

        }
        pageResult.setRecords(voList);
        pageResult.setTotal(total);

        return pageResult;
    }

    /**
     * 查询文档库详情
     */
    @Override
    public RepositoryDocVo queryDocDetail(Integer id) {
        RepositoryDoc repositoryDoc = repositoryDocMapper.queryById(id);
        String ruleExtractName = ruleExtractMapper.queryById(repositoryDoc.getRuleExtractId()).getRuleName();

        RepositoryDocVo repositoryDocVo = new RepositoryDocVo();
        repositoryDocVo.setId(repositoryDoc.getId());
        repositoryDocVo.setDocName(repositoryDoc.getDocName());
        repositoryDocVo.setRuleExtractName(ruleExtractName);
        if (repositoryDoc.getStatus() == TaskStatusEnum.COMPLETED.getCode()) {
            // 只有状态为提取完成时才返回提取内容
            repositoryDocVo.setExtractContent(repositoryDoc.getExtractContent());
        }

        return repositoryDocVo;
    }

    /**
     * 重新提取文档内容(批量)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reextract(List<Integer> ids) {
        for (Integer id : ids) {
            // 更新提取任务表中的状态为待开始
            ScheduleExtract scheduleExtract = new ScheduleExtract();
            scheduleExtract.setSourceId(id);
            scheduleExtract.setTaskStatus(TaskStatusEnum.UNEXECUTED.getCode());
            scheduleExtract.setTaskSource(ScheduleTaskSourceEnum.REPOSITORY.getCode());
            scheduleExtractMapper.updateBySourceId(scheduleExtract);
            // 更新文档库中状态
            RepositoryDoc repositoryDoc = new RepositoryDoc();
            repositoryDoc.setId(id);
            repositoryDoc.setStatus(TaskStatusEnum.UNEXECUTED.getCode());
            repositoryDocMapper.updateById(repositoryDoc);
        }
    }

    /**
     * 查询关联文档
     */
    @Override
    public List<RepositoryDocVo> queryByRuleExtracts(List<Integer> ruleExtractIds) {
        List<RepositoryDocVo> repositoryDocVoList = new ArrayList<>();
        List<RepositoryDoc> repositoryDocList = repositoryDocMapper.queryByExtractRuleIds(ruleExtractIds);
        for (RepositoryDoc repositoryDoc : repositoryDocList) {
            // 提取需要信息
            RepositoryDocVo repositoryDocVo = new RepositoryDocVo();
            repositoryDocVo.setId(repositoryDoc.getId());
            repositoryDocVo.setDocName(repositoryDoc.getDocName());
            repositoryDocVoList.add(repositoryDocVo);
        }
        return repositoryDocVoList;
    }

    /**
     * 查询文档预览流
     */
    @Override
    @SneakyThrows
    public void previewDoc(Integer id) {
        RepositoryDoc repositoryDoc = repositoryDocMapper.queryById(id);
        if (repositoryDoc == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        InputStream inputStream = fileService.downloadFile(repositoryDoc.getDocUrl());
        HttpServletResponse response = SpringContextUtil.getHttpServletResponse();
        String fileName = repositoryDoc.getDocName();

        try (InputStream is = inputStream;
             OutputStream os = response.getOutputStream()) {

            // 设置文件类型
            if (fileName.endsWith(".pdf")) {
                response.setContentType("application/pdf");
            } else if (fileName.endsWith(".docx")) {
                response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            } else {
                String fileExtension = DocUtil.getFileExtension(fileName);
                // 转换文件为 pdf
                fileName.replace(fileExtension, ".pdf");
                response.setContentType("application/pdf");
                // 处理中文文件名
                String encodedDocName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
                // inline表示预览，attachment为下载
                response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedDocName);

                docUtil.convertToPDF(is, os, fileExtension);
                return;
            }

            // 处理中文文件名
            String encodedDocName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            // inline表示预览，attachment为下载
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedDocName);

            // 把输入流写到请求体输出流中
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush(); // 强制输出剩余数据
        } catch (Exception e) {
            log.error("RepositoryDocController.previewDocStream.error:{}", e.getMessage(), e);
            throw new RuntimeException("预览文档时发生错误", e);
        }
    }


    /*
     * 下载文档
     */
    @Override
    public void downloadDoc(Integer id) {
        RepositoryDoc repositoryDoc = repositoryDocMapper.queryById(id);
        if (repositoryDoc == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        InputStream inputStream = fileService.downloadFile(repositoryDoc.getDocUrl());
        HttpServletResponse response = SpringContextUtil.getHttpServletResponse();
        String fileName = repositoryDoc.getDocName();

        try (InputStream is = inputStream;
             OutputStream os = response.getOutputStream()) {

            // 设置文件类型
            response.setContentType("application/octet-stream");

            // 处理中文文件名
            String encodedDocName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            // inline表示预览，attachment为下载
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedDocName);

            // 把输入流写到请求体输出流中
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush(); // 强制输出剩余数据
        } catch (Exception e) {
            log.error("RepositoryDocController.previewDocStream.error:{}", e.getMessage(), e);
            throw new RuntimeException("预览文档时发生错误", e);
        }
    }

    /*
     * 导出结构化数据
     */
    @Override
    @SneakyThrows
    public void export(List<Integer> ids, Integer exportType) {
        List<RepositoryDoc> repositoryDocs = repositoryDocMapper.queryByIds(ids);
        if (exportType == ExportTypeEnum.EXCEL.getCode()) {
            exportExcel(repositoryDocs);
        } else if (exportType == ExportTypeEnum.JSON.getCode()) {
            exportJson(repositoryDocs);
        }

    }

    /*
     * 查询所有项目名称
     */
    @Override
    @SneakyThrows
    public PageResult<String> queryProjectNames(String projectName, Integer pageNo, Integer pageSize) {
        PageResult<String> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo == null ? PageInfoEnum.PAGE_NO.getCode() : pageNo);
        pageResult.setPageSize(pageSize == null ? PageInfoEnum.PAGE_SIZE.getCode() : pageSize);
        Integer total = repositoryDocMapper.countProjectNames(projectName);
        if (total == 0) {
            return pageResult;
        }
        Integer offset = (pageResult.getPageNo() - 1) * pageResult.getPageSize();
        List<String> projectNames = repositoryDocMapper.queryPageProjectNames(projectName, pageResult.getPageSize(), offset);
        pageResult.setRecords(projectNames);
        pageResult.setTotal(total);
        return pageResult;
    }

    /*
     * 查询项目名称下所有文档
     */
    @Override
    @SneakyThrows
    public List<RepositoryDocVo> queryByProjectNames(List<String> projectNames) {
        List<RepositoryDocVo> repositoryDocVoList = new ArrayList<>();
        List<RepositoryDoc> repositoryDocList = repositoryDocMapper.queryByProjectNames(projectNames);
        for (RepositoryDoc repositoryDoc : repositoryDocList) {
            // 提取需要信息
            RepositoryDocVo repository = new RepositoryDocVo();
            repository.setId(repositoryDoc.getId());
            repository.setDocName(repositoryDoc.getDocName());
        }
        return repositoryDocVoList;
    }

    /*
     * 根据提取规则id和项目名称查询文档
     */
    @Override
    public List<RepositoryDocVo> queryByRuleExtractsAndProjectNames(RepositoryDocVo repositoryDocVo) {
         List<RepositoryDocVo> repositoryDocVoList = new ArrayList<>();
         List<Integer> ruleExtractIds = repositoryDocVo.getRuleExtractIds();
         List<String> projectNames = repositoryDocVo.getProjectNames();
         List<RepositoryDoc> repositoryDocList = repositoryDocMapper.queryByExtractRuleIdsAndProjectNames(ruleExtractIds, projectNames);
         for (RepositoryDoc repositoryDoc : repositoryDocList) {
             // 提取需要信息
             RepositoryDocVo repository = new RepositoryDocVo();
             repository.setId(repositoryDoc.getId());
             repository.setDocName(repositoryDoc.getDocName());
             repositoryDocVoList.add(repository);
         }
         return repositoryDocVoList;
    }

    /*
     * 导出数据为 excel
     */
    private void exportExcel(List<RepositoryDoc> repositoryDocs) throws IOException {
        // 根据提取规则 id 对文档进行分类
        Map<Integer, List<RepositoryDoc>> docGroup = repositoryDocs.stream()
                .collect(Collectors.groupingBy(RepositoryDoc::getRuleExtractId));

        Map<String, Map<String, List<List<String>>>> sheetData = new HashMap<>();
        for (Map.Entry<Integer, List<RepositoryDoc>> entry : docGroup.entrySet()) {
            Integer ruleExtractId = entry.getKey();
            List<RepositoryDoc> docList = entry.getValue();
            RuleExtract ruleExtract = ruleExtractMapper.queryById(ruleExtractId);
            String elements = ruleExtract.getElements();
            String[] elementsArray = elements.split("[,，]");
            // 提取要素作为表头
            List<String> elementsList = new ArrayList<>(Arrays.asList(elementsArray));
            elementsList.add(0, "文档名称");
            List<List<String>> head = elementsList.stream()
                    .map(element -> Collections.singletonList(element))
                    .collect(Collectors.toList());

            // 封装数据列表
            List<List<String>> dataList = new ArrayList<>();
            for (RepositoryDoc doc : docList) {
                // 解析每个文档的结构化数据为 map
                String extractContent = doc.getExtractContent();
                List<Map> ml;
                if (JSON.isValidArray(extractContent)) {
                    ml = JSON.parseArray(extractContent, Map.class);
                } else if (JSON.isValidObject(extractContent)) {
                    ml = new ArrayList<>();
                    Map<String, Object> map = JSON.parseObject(extractContent, Map.class);
                    ml.add(map);
                } else {
                    throw new IllegalArgumentException("Invalid JSON format");
                }
                for (Map<String, String> map : ml) {
                    // 每行数据，一个 doc 可能占多个行，一个 map 对应一个行
                    List<String> rowData = new ArrayList<>();
                    rowData.add(doc.getDocName());
                    for (String element : elementsArray) {
                        rowData.add(map.get(element));
                    }
                    dataList.add(rowData);
                }
            }
            Map<String, List<List<String>>> sheetMap = new HashMap<>();
            sheetMap.put("head", head);
            sheetMap.put("data", dataList);
            sheetData.put(ruleExtract.getRuleName(), sheetMap);
        }
        ExcelUtil.exportExcel(SpringContextUtil.getHttpServletResponse(), "文档结构化数据", sheetData);
    }

    /*
     * 导出数据为 .json
     */
    private void exportJson(List<RepositoryDoc> repositoryDocs) {
        List<String> dataList = repositoryDocs.stream().map(RepositoryDoc::getExtractContent).collect(Collectors.toList());
        JsonUtil.exportJson(SpringContextUtil.getHttpServletResponse(), "文档结构化数据", dataList);
    }

}

