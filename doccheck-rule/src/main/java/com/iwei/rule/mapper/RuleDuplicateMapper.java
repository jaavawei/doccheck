package com.iwei.rule.mapper;

import com.iwei.rule.entity.RuleDuplicate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查重规则表mapper
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface RuleDuplicateMapper {

    /**
     * 新增查重规则
     */
    int insert(RuleDuplicate ruleDuplicate);

    /**
     * 根据id更新
     */
    int updateById(RuleDuplicate ruleDuplicate);


    /**
     * 根据条件查询数量
     */
    int countByCondition(RuleDuplicate ruleDuplicate);

    /**
     * 根据条件分页查询
     */
    List<RuleDuplicate> queryPageByCondition(@Param("ruleDuplicate") RuleDuplicate ruleDuplicate,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    /**
     * 查询所有id和名称
     */
    List<RuleDuplicate> queryIdAndName();

    /**
     * 根据id查询
     */
    RuleDuplicate queryById(Integer id);
}

