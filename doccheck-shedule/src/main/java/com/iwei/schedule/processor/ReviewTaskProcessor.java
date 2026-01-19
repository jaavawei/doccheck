package com.iwei.schedule.processor;

import com.alibaba.fastjson2.JSON;
import com.iwei.common.config.AgentConfig;
import com.iwei.common.tool.JsonUtil;
import com.iwei.repository.entity.RepositoryDoc;
import com.iwei.repository.mapper.RepositoryReviewMapper;
import com.iwei.rule.entity.RuleExtract;
import com.iwei.rule.entity.RuleReview;
import com.iwei.rule.mapper.RuleExtractMapper;
import com.iwei.rule.mapper.RuleReviewMapper;
import com.iwei.common.tool.AgentUtil;
import com.iwei.schedule.config.ScheduleTaskConfig;
import com.iwei.task.entity.ScheduleReview;
import com.iwei.task.mapper.ScheduleReviewMapper;
import com.iwei.common.enums.TaskStatusEnum;
import com.iwei.task.entity.TaskReviewInfo;
import com.iwei.task.mapper.TaskReviewInfoMapper;
import javax.annotation.Resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档审查任务处理器
 *
 * @author: zhaokangwei
 */
@Component
@Slf4j
public class ReviewTaskProcessor implements ReviewTaskProcessorAdaptor {

    @Resource
    private ScheduleReviewMapper scheduleReviewMapper;
    @Resource
    private TaskReviewInfoMapper taskReviewInfoMapper;
    @Resource
    private RepositoryReviewMapper repositoryReviewMapper;
    @Resource
    private RuleExtractMapper ruleExtractMapper;
    @Resource
    private RuleReviewMapper ruleReviewMapper;
    @Resource
    private AgentUtil agentUtil;
    @Resource
    private AgentConfig agentConfig;
    @Resource
    private ScheduleTaskConfig scheduleTaskConfig;

    /**
     * 处理单个任务
     */
    @Override
    public void processTask(ScheduleReview task) {
        log.info("开始处理任务：" + task.getId());

        // 更新任务状态为处理中
        ScheduleReview scheduleReview = new ScheduleReview();
        scheduleReview.setId(task.getId());
        scheduleReview.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        boolean statusUpdated = scheduleReviewMapper.update(scheduleReview);
        TaskReviewInfo taskReviewInfo = new TaskReviewInfo();
        taskReviewInfo.setId(task.getReviewId());
        taskReviewInfo.setTaskStatus(TaskStatusEnum.EXECUTING.getCode());
        taskReviewInfoMapper.updateById(taskReviewInfo);

        // 多线程竞争时使用乐观锁
        if (!statusUpdated) {
            log.info("任务{}状态已被其他线程修改，跳过处理", task.getId());
            return;
        }

        try {
            // 获取审查任务的详细信息
            ScheduleReview review = scheduleReviewMapper.queryById(task.getId());
            // 获取审查内容
            Integer reviewId = review.getReviewId();
            RuleReview ruleReview = ruleReviewMapper.queryById(taskReviewInfoMapper.queryById(reviewId).getRuleReviewId());
             // String prompt = ruleReview.getContent();
            String url = ruleReview.getAgentUrl();
            String agentId = ruleReview.getAgentId();
            String agentVersion = ruleReview.getAgentVersion();
            String token = ruleReview.getAgentToken();
            String report;

            List<String> prompts = getPrompts(reviewId);
            // docContents中包含该项目所有文件的提取内容，反复利用
            List<Map> docContents = getDocContents(reviewId);
            List<String> results = new ArrayList<>();
            for (int i = 0; i < prompts.size() - 1; i++) {
                String rule = prompts.get(i);
                String result = review(JSON.toJSONString(docContents), rule, url, agentId, agentVersion, token);
                result = JsonUtil.processJson(result);
                log.info("任务处理结果：{}", result);
                // 封装所有结果
                results.add(result);
            }
            report = review("审查结果数据：\n" + JSON.toJSONString(results) + "\n" + "项目基础数据：\n" + JSON.toJSONString(docContents), prompts.get(prompts.size() - 1), url, agentId, agentVersion, token);

            log.info("任务处理结果：{}", report);
            taskReviewInfo.setReviewResult(report);

            // 更新任务状态为已完成
            scheduleReview.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            scheduleReviewMapper.update(scheduleReview);
            taskReviewInfo.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
            taskReviewInfoMapper.updateById(taskReviewInfo);
            log.info("任务{}处理完成", task.getId());
        } catch (Exception e) {
            log.error("处理任务{}时发生错误：", task.getId(), e);
            // 更新任务状态为失败
            scheduleReview.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            scheduleReviewMapper.update(scheduleReview);
            taskReviewInfo.setTaskStatus(TaskStatusEnum.FAILED.getCode());
            taskReviewInfoMapper.updateById(taskReviewInfo);
        }
    }

    /**
     * 审查
     */
    @SneakyThrows
    private String review(String extractContent, String rule, String url, String agentId, String agentVersion, String token) {
        int maxRetries = scheduleTaskConfig.getMaxRetryCount(); // 从配置中获取最大重试次数

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if ("fastgpt".equals(agentConfig.getAgentType())) {
                    return reviewFastGPT(extractContent, rule, url, agentId, agentVersion, token);
                } else if ("bailian".equals(agentConfig.getAgentType())) {
                    return reviewBailian(extractContent, rule, url, agentId, agentVersion, token);
                } else {
                    return reviewFacade(extractContent, rule, url, agentId, agentVersion, token);
                }
            } catch (Exception e) {
                log.warn("智能体请求失败，第{}次尝试失败: {}", attempt + 1, e.getMessage());
                if (attempt == maxRetries) {
                    // 所有重试都失败了，抛出异常
                    throw e;
                }
                // 等待一段时间再重试
                Thread.sleep(1000 * (attempt + 1)); // 递增等待时间
            }
        }
        return null; // 不会到达这里
    }

    /**
     * 审查 (人工智能门户)
     */
    @SneakyThrows
    private String reviewFacade(String extractContent, String rule, String url, String agentId, String agentVersion, String token) {
        return null;
    }

    /**
     * 审查 (百炼)
     */
    private String reviewBailian(String extractContent, String rule, String url, String agentId, String agentVersion, String token) {
        return null;
    }

    /**
     * 审查 (fastGPT)
     */
    @SneakyThrows
    private String reviewFastGPT(String extractContent, String rule, String url, String agentId, String agentVersion, String token) {


                // 获取新的 sessionId
                String sessionId = agentUtil.createSession(agentId, agentVersion);

                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("input", extractContent);
                contentMap.put("prompt", rule);
                List<Map<String, Object>> messageList = new ArrayList<>();
                Map<String, Object> messagesMap = new HashMap<>();
                messagesMap.put("role", "user");
                messagesMap.put("content", JSON.toJSONString(contentMap));
                Map<String, Object> paramMap = new HashMap<>();
                messageList.add(messagesMap);
                paramMap.put("messages", messageList);
                paramMap.put("chatId", sessionId);
                paramMap.put("stream", false);
                paramMap.put("detail", false);
                String paramJson = JSON.toJSONString(paramMap);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                        .build();
                // 构建请求
                // String api = "http://172.29.61.1:17400/api/v1/chat/completions";
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(paramJson, MediaType.parse("application/json; charset=utf-8")))
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (! response.isSuccessful()) {
                        throw new IOException("请求失败，状态码：" + response.code() + "，信息：" + response.body().string());
                    }
                    // 返回响应体内容
                    String responseJson = response.body() != null ? response.body().string() : "无响应内容";
                    Map<String, Object> responseMap = JSON.parseObject(responseJson, Map.class);
                    List<Map<String, Object>> choices = (List<Map<String, Object>>)responseMap.get("choices");
                    Map<String, Object> choiceMap = choices.get(0);
                    Map<String, Object> messageMap = (Map<String, Object>) choiceMap.get("message");
                    String reviewResult = (String) messageMap.get("content");
                    Map<String, Object> resultMap = JSON.parseObject(reviewResult, Map.class);
                    // log.info("duplicate:{}", reviewResult);
                    return String.valueOf(resultMap.get("result"));
                } finally {
                    // 任务完成后清理session
                    agentUtil.deleteSession(sessionId, agentId, agentVersion);
                }

    }

    /*
     * 封装同一项目下所有提取内容
     */
    private List<Map> getDocContents(Integer reviewId) {
        List<Map> docContents = new ArrayList<>();
        List<RepositoryDoc> repositoryDocs = repositoryReviewMapper.queryDocByReviewId(reviewId);
        for (RepositoryDoc repositoryDoc : repositoryDocs) {
            Map<String, Object> mp = new HashMap<>();
            RuleExtract ruleExtract = ruleExtractMapper.queryById(repositoryDoc.getRuleExtractId());
            mp.put("文件类型", ruleExtract.getRuleName());
            mp.put("文件名称", repositoryDoc.getDocName());
            mp.put("文件结构化", repositoryDoc.getExtractContent());
            docContents.add(mp);
        }
        return docContents;
    }

    /*
     * 获取所有提示词
     */
    private List<String> getPrompts(Integer reviewId) {
        List<String> prompts = ruleReviewMapper.queryByTaskReviewId(reviewId).stream()
                .map(RuleReview::getContent).collect(Collectors.toList());
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：任务书/合同与外委合同\n" +
//                "2.审查规则：- 中标单位与外委合同乙方一致性：成交/中标通知书中中标单位须与外委合同乙方一一对应，否则判一类异常。- 项目参与单位不得列入外委单位：任务书中的承担单位若出现在外委合同乙方中，则判一类异常。- 核心服务内容不得外委：任务书中主要服务内容如出现在外委合同中，则判一类异常。- 同一研究内容不得有多份外委合同：外委合同服务内容重复，判一类异常。- 外协测试与外委合同内容隔离：任务书有外委测试费用时必须有测试合同，且测试合同内容不得出现在外委合同中，否则判二类异常。- 外委合同配套中标通知书：任务书有外委支出必须有对应合同，缺合同判二类；有合同须附成交通知书，且通知书项目名称与合同名称一致，否则判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：工作报告\n" +
//                "2.审查规则：- 目录结构完整性：工作报告目录须与模板完全一致，且包含“后续展望”部分，否则判二类异常。\n " +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：研究报告\n" +
//                "2.审查规则：- 目录结构完整性：工作报告目录须与模板完全一致，且包含“后续展望”部分，否则判二类异常。- 专项报告与子报告区分：任务书单独列出的专项报告不得以子报告替代，替代或缺失判二类异常。- 审核流程合规：研究报告须含编写、校对、审核、批准人员及签名，缺一环节判二类异常。- 报告标题一致性：子课题报告首页标题须与任务书中研究内容完全匹配，否则判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：测试报告/第三方测试报告\n" +
//                "2.审查规则：- 必须提交第三方测试报告：任务书含测试费用则须有第三方测试报告，缺失判一类异常。- 关键指标检测完整性：测试报告须包含任务书要求的全部关键检测指标，缺失判二类异常。- 见证试验方案与专家名单：开展见证试验须提供试验方案及专家论证名单，缺一项或名单含项目承担单位专家判二类异常。 测试机构资质：软件测试报告须附 CNAS 等资质证明，缺失判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：应用证明\n" +
//                "2.审查规则：- 出具单位合规性：应用证明须由省公司本部或二级及以上下属单位出具，其他单位出具判二类异常。- 应用时间与效果描述：证明中须明确项目应用时间及实际效果，缺失判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：决算报告/审计报告\n" +
//                "2.审查规则：- 模板格式合规：决算与审计报告须严格遵循各自模板，格式不符判一类异常。- 预算与使用一致性：决算报告预算金额须与任务书一致，使用金额不得超预算且不得低于预算80%（低于需附说明），否则判二类异常。- 审计资质与结论：审计机构须具备资质，审计日期不得早于决算日期，否则判一类异常。- 报告编号与日期：须包含合同编号和签发日期，缺失判二类异常。- 审计人员资格：审计报告须附审计人员执业资格证复印件，缺失判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。");
//        prompts.add("我给你提供的是一个项目中所有文件的结构化数据，我需要你帮我对其中一种特定类型的文件内容进行审查，并将审查结果严格按照json格式给出。\n" +
//                "\n" +
//                "1.文件类型：论文/专利/软件著作权\n" +
//                "2.审查规则：- 成果时间范围：专利申请、软著申请、论文发表时间须在项目开始至验收时间内，否则判一类异常。- 作者/权人身份：论文作者须在项目组成员中，专利申请人须包含甲方单位，否则判一类异常。- 论文检索材料完整性：三大检索论文须附期刊封面、目录页、正文页及检索证明，缺一项判二类异常。\n" +
//                "3.输出格式：\n" +
//                "{\n" +
//                "“文件类型”: “*****\",\n" +
//                "“问题详情”: [\n" +
//                "{\n" +
//                "“问题类别”: “一类/二类问题”，\n" +
//                "“问题描述”: “*****”\n" +
//                "}\n" +
//                "]\n" +
//                "}\n" +
//                "\n" +
//                "注意：我给你提供的数据为所有文件的结构化数据，你需要从中找到被指定的文件类型的数据，并进行审查，如果审查规则中包含了其他文件的内容，则你可进行参考。如果没找到指定文件类型，视为没有问题。当前文件类型为：论文/专利/软件著作权，其中一篇论文或专利的内容可能分在多个文件中上传，你需要识别到同一篇论文或专利的所有文件，进行统一审查。");
        prompts.add("请严格按照以下要求，根据我提供的JSON数据生成“科技项目验收资料专家意见详表”，输出仅保留纯Markdown表格，无任何多余文字（如解释、说明语句）。\n" +
                "\n" +
                "一、表格模板\n" +
                "将我提供的项目基础信息与审查意见JSON数据，精准映射到以下固定结构的Markdown表格中：\n" +
                "| 项目名称 | XX项目名称全称 |\n" +
                "| --- | --- |\n" +
                "| 项目牵头单位 | xx单位全称 |\n" +
                "| 项目参与单位 | xx单位全称 |\n" +
                "| 专家意见 | |\n" +
                "| 一、文件命名/自查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 二、文件内容/自查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 三、文件格式/自查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 四、文件结构/自查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 五、文件内容/审查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 六、文件格式/审查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 七、文件结构/审查表 | “一类问题”/“二类问题”+问题详情，无问题填“/” |\n" +
                "| 八、项目整体评价 | 请根据上述检查结果，对项目整体进行评价 |\n" +
                "\n" +
                "二、数据来源\n" +
                "1. 项目基础信息：从我提供的JSON数据中提取项目名称、牵头单位和参与单位。\n" +
                "2. 专家意见：将审查结果中的问题按照问题类别（一类/二类）分别填入对应的自查表/审查表中。如果某个类别没有问题，则填“/”。\n" +
                "\n" +
                "三、注意事项\n" +
                "1. 输出必须是纯Markdown格式，不能包含任何解释性文字。\n" +
                "2. 表格中的内容必须严格按照JSON数据中的信息填写，不能添加额外内容。\n" +
                "3. 问题详情需要简洁明了，直接引用JSON数据中的描述。\n" +
                "4. 项目整体评价需要综合所有问题，给出客观的总结性评价。\n" +
                "5. 如果JSON数据中没有提供某个字段的信息，则对应表格单元格填“/”。");
        return prompts;
    }
}