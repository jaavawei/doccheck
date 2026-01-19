package com.iwei.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwei.question.entity.QuestionFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionFileMapper {
    
    /**
     * 插入一条记录
     */
    int insert(QuestionFile questionFile);

    /**
     * 批量插入记录
     */
    int batchInsert(List<QuestionFile> questionFileList);
    
    /**
     * 根据ID查询记录
     */
    QuestionFile queryById(@Param("id") Integer id);
    
    /**
     * 查询所有记录
     */
    List<QuestionFile> queryAll();

    List<QuestionFile> queryByCondition(QuestionFile questionFile);

}