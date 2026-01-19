package com.iwei.schedule.processor;

import com.iwei.common.tool.TikaUtil;
import com.iwei.repository.entity.ScheduleExtract;
import lombok.SneakyThrows;

import java.io.InputStream;

/**
 * 文档提取任务处理适配器
 *
 * @auther: zhaokangwei
 */
public interface ExtractTaskProcessorAdaptor {


    /**
     * 处理任务
     */
    public void processTask(ScheduleExtract task);

    /**
     * 解析文档
     */
    @SneakyThrows
    default String parseDoc(InputStream inputStream) {
        String content = TikaUtil.extractText(inputStream, "doc");
        inputStream.close();
        // System.out.println("解析内容如下:" + content);
        return content;
    }
}
