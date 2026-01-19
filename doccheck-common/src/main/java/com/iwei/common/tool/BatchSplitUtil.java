package com.iwei.common.tool;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 列表分批工具类
 */
@Component
public class BatchSplitUtil {

    @Value("${sql.batch-size}")
    private static int DEFAULT_BATCH_SIZE = 2000;

    private static int BATCH_SIZE;

    @PostConstruct
    private void init() {
        BATCH_SIZE = DEFAULT_BATCH_SIZE;
    }

    public static int getBatchSize() {
        return BATCH_SIZE;
    }

    public static void setBatchSize(int batchSize) {
        BATCH_SIZE = batchSize;
    }

    /**
     * 拆分列表为多个批次
     * @param list 待拆分的大列表
     * @param batchSize 每批大小
     * @return 拆分后的批次列表
     */
    public static <T> List<List<T>> splitList(List<T> list, int batchSize) {
        List<List<T>> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        // 计算总批次
        int totalSize = list.size();
        int batchNum = (totalSize + batchSize - 1) / batchSize;

        for (int i = 0; i < batchNum; i++) {
            // 计算当前批的起始和结束索引
            int start = i * batchSize;
            int end = Math.min(start + batchSize, totalSize);
            // 截取子列表
            List<T> subList = list.subList(start, end);
            result.add(subList);
        }
        return result;
    }

    /**
     * 拆分列表
     */
    public static <T> List<List<T>> splitList(List<T> list) {
        return splitList(list, BATCH_SIZE);
    }
}
