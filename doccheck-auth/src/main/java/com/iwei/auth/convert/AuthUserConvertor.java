package com.iwei.auth.convert;

import com.iwei.auth.entity.AuthUser;
import com.iwei.auth.entity.vo.AuthUserVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户权限实体类与Vo转换类
 *
 * @auther: zhaokangwei
 */
@Mapper
public interface AuthUserConvertor {

    AuthUserConvertor INSTANCE = Mappers.getMapper(AuthUserConvertor.class);

    AuthUser convertVoToAuthUser(AuthUserVo authUserVo);
}
