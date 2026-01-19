package com.iwei.question.mapper;

import com.iwei.question.entity.App;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppMapper {

    /**
     * 根据ID查询应用信息
     */
    App selectById(Integer id);

    /**
     * 插入应用信息
     */
    int insert(App app);

    /**
     * 更新应用信息
     */
    int updateById(App app);

    /**
     * 根据ID删除应用信息
     */
    int deleteById(Integer id);
}