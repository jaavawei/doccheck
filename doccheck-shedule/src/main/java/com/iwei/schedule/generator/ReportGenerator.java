package com.iwei.schedule.generator;

import com.alibaba.fastjson2.JSON;
import com.iwei.schedule.entity.ProjectQuery;
import com.iwei.schedule.entity.SimilarProject;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 查重报告生成器
 *
 * @author: zhaokangwei
 */
public class ReportGenerator {
    private static TimeZone CN_TZ;

    static {
        // 初始化中国时区
        try {
            CN_TZ = TimeZone.getTimeZone("Asia/Shanghai");
        } catch (Exception e) {
            CN_TZ = TimeZone.getTimeZone("GMT+8");
        }
    }

    public static String generateReportMd(Object inputQ, Object resultA) {
        return generateReportMd(inputQ, resultA, 5, 70.0f, 50.0f);
    }

    public static String generateReportMd(
            Object inputQ,
            Object resultA,
            int topN,
            float highThreshold,
            float mediumThreshold
    ) {
        // 解析输入参数
        ProjectQuery projectQuery = parseInputQ(inputQ);
        List<SimilarProject> similarProjects = parseResultA(resultA);

        // 按总分降序排序
        similarProjects.sort((p1, p2) -> {
            float score1 = safeTotal(p1.getTotalScore());
            float score2 = safeTotal(p2.getTotalScore());
            return Float.compare(score2, score1); // 降序
        });

        // 构建Markdown内容
        List<String> lines = new ArrayList<>();
        lines.add("### 项目查重分析报告\n");
        lines.add("#### 一、基本信息\n");

        // 添加基本信息
        lines.add("- **当前项目名称**：" + clean(projectQuery.getProjectName()) + "  ");
        lines.add("- **建设内容**：" + clean(projectQuery.getConstructionContent()) + "  ");
        lines.add("- **查重时间（中国时区）**：" + getCurrentCNTime() + "  ");
        lines.add("- **分析维度**：项目名称 / 建设内容（目标、方法、场景、指标、范围）  \n");
        lines.add("---\n");

        // 添加相似项目表格
        lines.add("#### 二、相似项目匹配结果（Top " + topN + "）\n");
        lines.add("| 序号 | 历史项目名称 | 总相似度评分 | 核心相似内容 | 建议处理 |");
        lines.add("|------|----------------|:-------------:|--------------|:--------:|");

        if (similarProjects.isEmpty()) {
            lines.add("| - | - | - | - | - |");
        } else {
            int limit = Math.min(topN, similarProjects.size());
            for (int i = 0; i < limit; i++) {
                SimilarProject project = similarProjects.get(i);
                int index = i + 1;
                StringBuilder nameBuilder = new StringBuilder();
                nameBuilder.append("" +
                        "<span class=\"hover-tooltip____\">"
                            + clean(project.getProjectName())
                            + "<span class=\"tooltip-text\">"
                                + project.getExtractContent().replace("\n", "<br>")
                            + "</span>"
                        + "</span>");
                String name = nameBuilder.toString();
                float score = safeTotal(project.getTotalScore());
                String scoreStr = String.format("%.1f", score);
                String simDesc = clean(project.getSimilarityDescription(), 100);
                String suggestion;

                if (score >= highThreshold) {
                    suggestion = "人工复核";
                } else if (score >= mediumThreshold) {
                    suggestion = "建议复核";
                } else {
                    suggestion = "相似性较低";
                }

                lines.add(String.format("| %d | %s | %s | %s | %s |",
                        index, name, scoreStr, simDesc, suggestion));
            }
        }

        lines.add("\n---\n");

        // 添加总结建议
        lines.add("#### 三、查重总结建议\n");
        if (!similarProjects.isEmpty()) {
            SimilarProject topProject = similarProjects.get(0);
            float topScore = safeTotal(topProject.getTotalScore());
            String topName = clean(topProject.getProjectName(), 120);
            String topSim = clean(topProject.getSimilarityDescription(), 200);
            String summary;

            if (topScore >= highThreshold) {
                summary = String.format("该项目与历史项目“%s”存在较高重复性（%.1f%%），建议人工复核，" +
                                "重点核查技术路线、指标与预期成果以避免重复立项。",
                        topName, topScore);
            } else if (topScore >= mediumThreshold) {
                summary = String.format("该项目与历史项目“%s”存在一定程度相似（%.1f%%），建议进行详细复核，" +
                                "对相似维度做针对性调整。",
                        topName, topScore);
            } else {
                summary = String.format("该项目与历史项目“%s”相似性较低（%.1f%%），可按常规流程推进。",
                        topName, topScore);
            }

            lines.add(summary);
            lines.add("\n");
            lines.add("- **核心相似内容**：" + topSim);
            lines.add("- **建议核查项**：功能实现细节、量化指标、验证/示范方案及知识产权风险。");
        } else {
            lines.add("未检测到历史匹配记录，可按常规流程推进。");
        }

        // 合并所有行
        return String.join("\n", lines);
    }

    // 解析输入的查询项目信息
    private static ProjectQuery parseInputQ(Object inputQ) {
        if (inputQ == null) {
            return new ProjectQuery();
        }

        if (inputQ instanceof ProjectQuery) {
            return (ProjectQuery) inputQ;
        }

        if (inputQ instanceof String) {
            try {
                return JSON.parseObject((String) inputQ, ProjectQuery.class);
            } catch (Exception e) {
                return new ProjectQuery();
            }
        }

        return new ProjectQuery();
    }

    // 解析输入的相似项目列表
    private static List<SimilarProject> parseResultA(Object resultA) {
        List<SimilarProject> list = new ArrayList<>();
        if (resultA == null) {
            return list;
        }

        if (resultA instanceof List) {
            for (Object item : (List<?>) resultA) {
                if (item instanceof SimilarProject) {
                    list.add((SimilarProject) item);
                }
            }
            return list;
        }

        if (resultA instanceof String) {
            try {
                return JSON.parseArray((String) resultA, SimilarProject.class);
            } catch (Exception e) {
                return list;
            }
        }

        return list;
    }

    // 安全解析总分值
    private static float safeTotal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0f;
        }

        try {
            String s = value.trim();
            if (s.endsWith("%")) {
                s = s.substring(0, s.length() - 1);
            }
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0.0f;
        }
    }

    // 清理字符串并限制长度
    private static String clean(String s) {
        return clean(s, 100);
    }

    private static String clean(String s, int maxChars) {
        if (s == null) {
            return "";
        }

        String t = s.replace("\n", " ").replace("\r", " ").trim();
        if (t.length() <= maxChars) {
            return t;
        } else {
            return t.substring(0, maxChars - 1) + "…";
        }
    }

    // 获取中国时区当前时间
    private static String getCurrentCNTime() {
        try {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(CN_TZ);
            return formatter.format(now);
        } catch (Exception e) {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return formatter.format(now);
        }
    }
}
