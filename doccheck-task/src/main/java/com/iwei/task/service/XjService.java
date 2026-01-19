package com.iwei.task.service;

import org.springframework.web.multipart.MultipartFile;

public interface XjService {

    public void parseStationLine(MultipartFile file);

    void base();

    void updateDoc();

    void updateToDoc();
}
