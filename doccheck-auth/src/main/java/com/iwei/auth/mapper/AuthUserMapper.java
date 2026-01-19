package com.iwei.auth.mapper;

import com.iwei.auth.entity.AuthUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AuthUserMapper {

    /*
     *根据条件查询用户信息
     */
    List<AuthUser> queryByCondition(AuthUser authUser);
}
