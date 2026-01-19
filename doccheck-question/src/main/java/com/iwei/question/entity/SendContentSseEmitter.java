package com.iwei.question.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
public class SendContentSseEmitter {

    private String question;

    private String sessionId;

    private Integer appId;

    private String uuid;

    private String projectInfo;

    private List<String> fileIds;
}
