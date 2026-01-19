package com.iwei.question.entity;

import lombok.Data;
import java.util.Date;

@Data
public class QuestionFile {
    /**
     * 主键
     */
    private Integer id;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件URL
     */
    private String url;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 删除标志 0-正常 1-删除
     */
    private Integer delFlg;
}