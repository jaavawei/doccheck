package com.iwei.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwei.question.entity.QuestionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionLogMapper {
    
    /**
     * 插入一条记录
     */
    int insert(QuestionLog questionLog);
    
    /**
     * 根据ID查询记录
     */
    QuestionLog queryById(@Param("id") Integer id);
    
    /**
     * 查询所有记录
     */
    List<QuestionLog> queryAll();
    
    /**
     * 根据条件查询记录
     */
    List<QuestionLog> queryByCondition(@Param("questionLog") QuestionLog questionLog, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    /**
     * 查询历史记录列表
     */
    List<QuestionLog> queryHistoryList(@Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    /**
     * 查询历史记录列表数量
     */
    Integer countHistoryList();

    /**
     * 根据条件查询记录数量
     */
    Integer countByCondition(QuestionLog questionLog);
}