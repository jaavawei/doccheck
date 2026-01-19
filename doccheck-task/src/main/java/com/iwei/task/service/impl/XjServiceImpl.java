package com.iwei.task.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson2.JSON;
import com.iwei.common.enums.DelFlgEnum;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.common.tool.BatchSplitUtil;
import com.iwei.repository.entity.*;
import com.iwei.repository.mapper.*;
import com.iwei.task.entity.ProjectStationLineMapping;
import com.iwei.task.entity.ScheduleXj;
import com.iwei.task.entity.TaskDuplicateDoc;
import com.iwei.task.mapper.ProjectStationLineMappingMapper;
import com.iwei.task.mapper.ScheduleXjMapper;
import com.iwei.task.mapper.TaskDuplicateDocMapper;
import com.iwei.task.service.XjService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.concurrent.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class XjServiceImpl implements XjService {

    @Resource
    ProjectStationLineMappingMapper projectStationLineMappingMapper;
    @Resource
    RepositoryDocMapper repositoryDocMapper;
    @Resource
    RepositoryDuplicateMapper repositoryDuplicateMapper;
    @Resource
    RepositoryDuplicateDocMappingMapper repositoryDuplicateDocMappingMapper;
    @Resource
    StationLineMapper stationLineMapper;
    @Resource
    RepositoryDocStationLineMappingMapper repositoryDocStationLineMappingMapper;
    @Resource
    ScheduleXjMapper scheduleXjMapper;
    @Resource
    TaskDuplicateDocMapper taskDuplicateDocMapper;

    /**
     * 解析结构化文件（Excel）提取子任务
     */
    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void parseStationLine(MultipartFile file) {

        // 解析 excel 文件
        ExcelParserListener listener = new ExcelParserListener();
        EasyExcel.read(file.getInputStream(), listener).sheet().headRowNumber(0).doRead();
        List<ProjectStationLineMapping> resultList = listener.getResultList();
        List<RepositoryDoc> docList = listener.getDocList();
        if (resultList.isEmpty()) {
            throw new RuntimeException("请检查结构化文档内容是否为空或格式是否正确");
        }
        // 使用BatchSplitUtil进行分批插入
        List<List<ProjectStationLineMapping>> resultLists = BatchSplitUtil.splitList(resultList);
        for (List<ProjectStationLineMapping> list : resultLists) {
            projectStationLineMappingMapper.batchInsert(list);
        }
        // 使用BatchSplitUtil进行分批插入
        List<List<RepositoryDoc>> docLists = BatchSplitUtil.splitList(docList);
        for (List<RepositoryDoc> list : docLists) {
            repositoryDocMapper.batchInsert(list);
        }
        log.info("first done");
        log.info("second done");
        // 创建所有stationLine
        List<String> stationLineNameList = projectStationLineMappingMapper.queryDistinctStationLineName();
        // List<StationLine> stationLineList = new ArrayList<>();
        for (String stationLineName : stationLineNameList) {
            StationLine stationLine = stationLineMapper.queryByName(stationLineName);
            if (stationLine == null) {
                stationLine = new StationLine();
                stationLine.setStationLineName(stationLineName);
                stationLine.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                stationLineMapper.insert(stationLine);
                // stationLineList.add(stationLine);
            }
        }
        log.info("third done");
        // stationLineMapper.batchInsert(stationLineList);

        // 对于每个文档都找到对应的station，并插入映射表
        // List<RepositoryDocStationLineMapping> docStationLineMappingList = new ArrayList<>();
        for (RepositoryDoc doc : docList) {
            String extractContent = doc.getExtractContent();
            HashMap<String, String> extractMap = JSON.parseObject(extractContent, HashMap.class);
            String stationLineName = extractMap.get("站线名称");
            String[] stationLineNames = stationLineName.split(",");
            List<StationLine> stationLines = stationLineMapper.queryByNames(stationLineNames);
            // 插入映射表
            List<RepositoryDocStationLineMapping> docStationLineMappingList = new ArrayList<>();
            for (StationLine stationLine : stationLines) {
                RepositoryDocStationLineMapping docStationLineMapping = new RepositoryDocStationLineMapping();
                docStationLineMapping.setStationLineId(stationLine.getId());
                docStationLineMapping.setDocId(doc.getId());
                docStationLineMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
                docStationLineMappingList.add(docStationLineMapping);
            }
            // 使用BatchSplitUtil进行分批插入
            List<List<RepositoryDocStationLineMapping>> docStationLineMappingLists = BatchSplitUtil.splitList(docStationLineMappingList);
            for (List<RepositoryDocStationLineMapping> list : docStationLineMappingLists) {
                repositoryDocStationLineMappingMapper.batchInsert(list);
            }
        }
        log.info("fourth done");
    }

    /**
     * 自定义监听器，处理Excel解析逻辑
     */
    private static class ExcelParserListener implements ReadListener<Map<Integer, Object>> {
        private List<String> extractNames;
        private final List<ProjectStationLineMapping> resultList = new ArrayList<>();
        private final List<RepositoryDoc> docList = new ArrayList<>();
        private int rowNum = 0;

        @Override
        public void invoke(Map<Integer, Object> rowData, AnalysisContext context) {
            // log.info(JSON.toJSONString(rowData));
            rowNum++;

            if (rowNum == 1) {
                // 第一行为提取内容名
                extractNames = new ArrayList<>();
                for (int i = 1; ; i++) { // 循环获取列索引1、2、3...的值
                    Object value = rowData.get(i);
                    if (value == null) break; // 没有更多列时退出
                    extractNames.add(String.valueOf(value));
                }
                return;
            }

            if (rowNum >= 2) {
                // 从第二行开始处理，列索引0为taskName
                Object taskNameObj = rowData.get(0);
                if (taskNameObj == null) {
                    // 任务名为空，跳过此行
                    return;
                }
                String taskName = String.valueOf(taskNameObj);
                // ProjectStationLineMapping projectStationLineMapping= new ProjectStationLineMapping();
                Map<String, String> dataMap = new HashMap<>();
                Map<String, String> extract = new HashMap<>();
                RepositoryDoc repositoryDoc = new RepositoryDoc();
                repositoryDoc.setStatus(2);
                // 列索引1开始对应extractNames中的内容
                for (int i = 1; i <= extractNames.size(); i++) {

                    String extractName = extractNames.get(i - 1); // i-1对应extractNames的索引
                    String value = String.valueOf(rowData.get(i)).trim();
                    if(extractName.equals("项目名称")) {
                        dataMap.put(extractName, value);
                        extract.put(extractName, value);
                        repositoryDoc.setDocName(value + ".pdf");
                        repositoryDoc.setProjectName(value);
                    }
                    if(extractName.equals("项目编码")) {
                        dataMap.put(extractName, value);
                        extract.put(extractName, value);
                    }
                    if(extractName.equals("站线名称")) {
                        dataMap.put(extractName, value);
                        dataMap.put("项目关键信息", value);
                        extract.put(extractName, value);
                    }
                    if(extractName.equals("站线编码")) {
                        dataMap.put(extractName, value);
                        extract.put(extractName, value);
                    }
                    if (extractName.equals("实施单位名称")) {
                        extract.put(extractName, value);
                    }
                    if (extractName.equals("计划年度")) {
                        extract.put(extractName, value);
                    }

                    if(extractName.equals("设备现状") || extractName.equals("存在问题") || extractName.equals("方案规模") || extractName.equals("立项依据")) {
                        extract.put(extractName, value);
                    }


                }
                String extractContent = JSON.toJSONString(extract);
                repositoryDoc.setExtractContent(extractContent);
                docList.add(repositoryDoc);

                // 将多个站线编码均解析出来，形成多条记录
                for (String s : dataMap.get("站线名称").split(",")) {
                    ProjectStationLineMapping projectStationLineMapping = new ProjectStationLineMapping();
                    projectStationLineMapping.setProjectName(dataMap.get("项目名称"));
                    projectStationLineMapping.setProjectCode(dataMap.get("项目编码"));
                    projectStationLineMapping.setStationLineName(s);
                    projectStationLineMapping.setStationLineCode(dataMap.get("站线编码"));
                    projectStationLineMapping.setImplOrg(extract.get("实施单位名称"));
                    projectStationLineMapping.setPlanYear(extract.get("计划年度"));
                    projectStationLineMapping.setProjectMsg(dataMap.get("项目关键信息"));
                    resultList.add(projectStationLineMapping);
                }
            }
        }

        public List<ProjectStationLineMapping> getResultList() { return resultList; }
        public List<RepositoryDoc> getDocList() { return docList; }

        @Override
        public boolean hasNext(AnalysisContext context) { return true; }
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    @Override
    @Transactional
    public void base() {
        RepositoryDuplicate repositoryDuplicate = new RepositoryDuplicate();
        repositoryDuplicate.setRepositoryDuplicateName("设备查重库");
        repositoryDuplicate.setDataSource(1);
        repositoryDuplicate.setId(22);
        repositoryDuplicate.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
        repositoryDuplicateMapper.insert(repositoryDuplicate);
        List<RepositoryDoc> repositoryDocs = repositoryDocMapper.queryAll();
        List<RepositoryDuplicateDocMapping> repositoryDuplicateDocMappings = new ArrayList<>();
        repositoryDocs.forEach(repositoryDoc -> {
            RepositoryDuplicateDocMapping repositoryDuplicateDocMapping = new RepositoryDuplicateDocMapping();
            repositoryDuplicateDocMapping.setRepositoryDocId(repositoryDoc.getId());
            repositoryDuplicateDocMapping.setRepositoryDuplicateId(22);
            repositoryDuplicateDocMapping.setDelFlg(DelFlgEnum.UN_DELETED.getCode());
            repositoryDuplicateDocMappings.add(repositoryDuplicateDocMapping);
        });
        BatchSplitUtil.splitList(repositoryDuplicateDocMappings).forEach(list -> repositoryDuplicateDocMappingMapper.batchInsert(list));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoc() {
        List<RepositoryDoc> repositoryDocs = repositoryDocMapper.queryAll();
        // 更新文档的一些基础信息
        for (RepositoryDoc repositoryDoc : repositoryDocs) {
            String extractContent = repositoryDoc.getExtractContent();
            Map<String, String> extractMap = JSON.parseObject(extractContent, Map.class);
            if (extractMap != null) {
                repositoryDoc.setProjectCode(extractMap.get("项目编码"));
                repositoryDoc.setPlanYear(extractMap.get("计划年度"));
                repositoryDoc.setProjectMsg(extractMap.get("站线名称"));
                repositoryDoc.setImplOrg(extractMap.get("实施单位名称"));
            }
            repositoryDoc.setDocUrl("/tmp/doc");
            repositoryDoc.setDuplicateFlg(-1);
            repositoryDocMapper.updateById(repositoryDoc);
            // 更新文档查询状态：是否已完成
            Integer all = scheduleXjMapper.countByDocId(repositoryDoc.getId());
            Integer done = scheduleXjMapper.countByDocIdAndStatus(repositoryDoc.getId(), TaskStatusEnum.COMPLETED.getCode());
            if (done == 0) {
                repositoryDoc.setDuplicateStatus(0); // 等待中
            } else {
                repositoryDoc.setDuplicateStatus(done.equals(all) ? 2 : 1); // 已完成 ： 执行中
            }
        }
//        // 更新文档是否已完成与是否重复
//        List<ScheduleXj> tasks = scheduleXjMapper.queryAll();
//        Set<Integer> dSet = new HashSet<>();
//        for (ScheduleXj task : tasks) {
//            RepositoryDoc docF = repositoryDocMapper.queryById(task.getDocFId());
//            RepositoryDoc docS = repositoryDocMapper.queryById(task.getDocSId());
//            // 首先任务是存在的，更新重复标识为0
//            docF.setDuplicateFlg(0);
//            docS.setDuplicateFlg(0);
//            if (task.getDuplicateFlg() == 1) {
//                dSet.add(task.getDocFId());
//                dSet.add(task.getDocSId());
//            }
//            repositoryDocMapper.updateById(docF);
//            repositoryDocMapper.updateById(docS);
//        }
//        RepositoryDoc doc = new RepositoryDoc();
//        doc.setDuplicateFlg(1);
//        for (Integer id : dSet) {
//            doc.setId(id);
//            repositoryDocMapper.updateById(doc);
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateToDoc() {
        List<RepositoryDoc> repositoryDocs = repositoryDocMapper.queryAll();
        for (RepositoryDoc repositoryDoc : repositoryDocs) {
            TaskDuplicateDoc taskDuplicateDoc = new TaskDuplicateDoc();
            taskDuplicateDoc.setInfoId(1);
            taskDuplicateDoc.setRepoDocId(repositoryDoc.getId());
            taskDuplicateDoc.setDuplicateFlg(repositoryDoc.getDuplicateFlg());
            taskDuplicateDoc.setTaskStatus(repositoryDoc.getDuplicateStatus());
            taskDuplicateDocMapper.insert(taskDuplicateDoc);
        }
    }
}
